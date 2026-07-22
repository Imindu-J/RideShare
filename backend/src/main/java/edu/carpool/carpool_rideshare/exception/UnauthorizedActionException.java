package edu.carpool.carpool_rideshare.exception;

public class UnauthorizedActionException extends RuntimeException{
    public UnauthorizedActionException(String message){
        super(message);
    }
    
}
