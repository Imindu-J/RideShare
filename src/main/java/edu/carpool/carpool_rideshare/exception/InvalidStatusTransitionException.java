package edu.carpool.carpool_rideshare.exception;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException (String message){
        super(message);
    }    
}
