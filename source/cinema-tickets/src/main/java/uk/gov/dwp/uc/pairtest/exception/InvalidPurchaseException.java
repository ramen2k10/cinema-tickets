package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {
    private static final String EXCEPTION_NAME = "InvalidTicketPurchaseException";

    public InvalidPurchaseException(){
        super();
    }

    public InvalidPurchaseException(String message){
        super(message);
    }

    public InvalidPurchaseException(String message, Throwable cause){
        super(message, cause);
    }

    @Override
    public String toString() {
        return EXCEPTION_NAME+": "+this.getMessage();
    }
}
