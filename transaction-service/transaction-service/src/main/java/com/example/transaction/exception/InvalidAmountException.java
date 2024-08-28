package com.example.transaction.exception;

public class InvalidAmountException extends RuntimeException{
    public InvalidAmountException(String msg){
        super(msg);
    }
}
