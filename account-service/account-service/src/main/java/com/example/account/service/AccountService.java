package com.example.account.service;

import com.example.account.entity.Account;
import com.example.account.exception.AccountNotFoundException;
import com.example.account.exception.DuplicateEmailException;
import com.example.account.exception.InsufficientDataException;
import com.example.account.exception.InvalidAmountException;
import com.example.account.repository.AccountRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final ObservationRegistry registry;

    private final AccountRepository accountRepository;

    public AccountService(ObservationRegistry registry, AccountRepository accountRepository) {
        this.registry = registry;
        this.accountRepository = accountRepository;
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
        return accountRepository.save(account);
    }

    @Transactional
    @CircuitBreaker(name = "accountService", fallbackMethod = "handleWithdrawalFallback")
    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        validateAmount(amount);
        validateSufficientBalance(account, amount);
        account.setBalance(account.getBalance().subtract(amount));
        return accountRepository.save(account);
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
}
