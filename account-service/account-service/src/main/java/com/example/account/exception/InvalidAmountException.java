package com.example.account.exception;

public class InvalidAmountException extends RuntimeException{
    public InvalidAmountException(String msg){
        super(msg);
    }
}
