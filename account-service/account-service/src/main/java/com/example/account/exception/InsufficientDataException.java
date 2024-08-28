package com.example.account.exception;

public class InsufficientDataException extends RuntimeException{
    public InsufficientDataException(String msg){
        super(msg);
    }
}
