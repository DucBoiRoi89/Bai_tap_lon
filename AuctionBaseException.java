public class AuctionBaseException extends Exception {  // Base Exception cho cả hệ thống
    public AuctionBaseException(String message) { 
        super(message) ; 
    }

    public AuctionBaseException(String message, Throwable cause) { 
        super(message, cause) ; 
    }
}
