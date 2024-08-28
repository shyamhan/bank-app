package com.example.account.service;

import com.example.account.entity.Account;
import com.example.account.entity.PendingTransaction;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.exception.DuplicateEmailException;
import com.example.account.exception.InsufficientDataException;
import com.example.account.exception.InvalidAmountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.PendingTransactionRepository;
import com.example.account.util.TransactionType;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.observation.ObservationRegistry;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private static final Log logger = LogFactory.getLog(AccountService.class);

    private final ObservationRegistry registry;
    private final AccountRepository accountRepository;
    private final PendingTransactionRepository pendingTransactionRepository;
    private final NotificationServiceClient notificationServiceClient;

    public AccountService(ObservationRegistry registry, AccountRepository accountRepository,
                          PendingTransactionRepository pendingTransactionRepository,
                          NotificationServiceClient notificationServiceClient) {
        this.registry = registry;
        this.accountRepository = accountRepository;
        this.pendingTransactionRepository = pendingTransactionRepository;
        this.notificationServiceClient=notificationServiceClient;
    }

    @Transactional
    public Account createAccount(Account account) {
        validateAccountCreation(account);
        account.setBalance(BigDecimal.ZERO);
        return accountRepository.save(account);
    }

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "handleDepositFallback")
    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        validateAmount(amount);
        account.setBalance(account.getBalance().add(amount));
        Account savedAccount = accountRepository.save(account);
        try {
            notificationServiceClient.sendNotification(account.getEmail(), TransactionType.DEPOSIT.toString(), String.valueOf(amount));
        }catch(Exception e){
            logger.info(e.getMessage());
        }
        return savedAccount;
    }

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "handleWithdrawalFallback")
    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        validateAmount(amount);
        validateSufficientBalance(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
        Account savedAccount = accountRepository.save(account);
        notificationServiceClient.sendNotification(account.getEmail(), TransactionType.WITHDRAW.toString(), String.valueOf(amount));
        return savedAccount;
    }

    @CircuitBreaker(name = "accountService", fallbackMethod = "handleBalanceInquiryFallback")
    public BigDecimal checkBalance(Long accountId) {
        Account account = findAccountById(accountId);
        return account.getBalance();
    }

    private void validateAccountCreation(Account account) {
        if (accountRepository.findByEmail(account.getEmail()).isPresent()) {
            throw new DuplicateEmailException("An account with this email already exists.");
        }
        if (account.getName() == null || account.getEmail() == null || account.getPhoneNumber() == null || account.getPassword() == null) {
            throw new InsufficientDataException("All fields (name, email, phone number, password) are required.");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }

    private void validateSufficientBalance(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InvalidAmountException("Insufficient balance.");
        }
    }

    private Account findAccountById(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for ID: " + accountId));
    }

    // Fallback Methods
    private Account handleDepositFallback(Long accountId, BigDecimal amount, Throwable t) {
        // Handle fallback logic here, without direct tracing calls
        throw new RuntimeException("Deposit failed. Please try again later.", t);
    }

    private Account handleWithdrawalFallback(Long accountId, BigDecimal amount, Throwable t) {
        // Handle fallback logic here, without direct tracing calls
        throw new RuntimeException("Withdrawal failed. Please try again later.", t);
    }

    private BigDecimal handleBalanceInquiryFallback(Long accountId, Throwable t) {
        // Handle fallback logic here, without direct tracing calls
        return BigDecimal.ZERO;
    }

    @Transactional
    public boolean prepareWithdraw(Long transactionId, Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) >= 0) {
            PendingTransaction transaction = new PendingTransaction();
            transaction.setTransactionId(transactionId);
            transaction.setAccountId(accountId);
            transaction.setAmount(amount);
            transaction.setType(TransactionType.WITHDRAW);
            transaction.setStatus("PENDING");
            pendingTransactionRepository.save(transaction);
            return true;
        }
        return false;
    }

    @Transactional
    public boolean prepareDeposit(Long transactionId, Long accountId, BigDecimal amount) {
        PendingTransaction transaction = new PendingTransaction();
        transaction.setTransactionId(transactionId);
        transaction.setAccountId(accountId);
        transaction.setAmount(amount);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setStatus("PENDING");
        pendingTransactionRepository.save(transaction);
        return true;
    }

    @Transactional
    public void commitTransaction(Long transactionId) {
        PendingTransaction transaction = pendingTransactionRepository.findByTransactionId(transactionId);
        Account account = null;
        if (transaction != null && "PENDING".equals(transaction.getStatus())) {
            account = accountRepository.findById(transaction.getAccountId())
                    .orElseThrow(() -> new RuntimeException("Account not found"));

            if (transaction.getType() == TransactionType.WITHDRAW) {
                account.setBalance(account.getBalance().subtract(transaction.getAmount()));
            } else if (transaction.getType() == TransactionType.DEPOSIT) {
                account.setBalance(account.getBalance().add(transaction.getAmount()));
            }

            accountRepository.save(account);
            transaction.setStatus("COMMITTED");
            pendingTransactionRepository.save(transaction);
            if(account != null){
                notificationServiceClient.sendNotification(account.getEmail(), transaction.getType().toString(), String.valueOf(transaction.getAmount()));
            }
        }
    }

    @Transactional
    public void rollbackTransaction(Long transactionId) {
        PendingTransaction transaction = pendingTransactionRepository.findByTransactionId(transactionId);
        if (transaction != null && "PENDING".equals(transaction.getStatus())) {
            transaction.setStatus("ABORTED");
            pendingTransactionRepository.save(transaction);
        }
    }
}
