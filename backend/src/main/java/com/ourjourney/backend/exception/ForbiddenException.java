package com.ourjourney.backend.exception;

public class ForbiddenException extends RuntimeException{
    public ForbiddenException (String message){
        super(message);
    }
}
