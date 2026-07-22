package edu.carpool.carpool_rideshare.exception;

public class SeatUnavailableException extends RuntimeException{
    public SeatUnavailableException(String message){
        super(message);
    }
    
}
