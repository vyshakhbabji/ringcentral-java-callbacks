package core;

/**
 * This is a RingCentral Generic Exception Class
 */
public class RingCentralException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public RingCentralException(String message) {
        super(message);
    }

    public RingCentralException(String message, Throwable t) {
        super(message, t);
    }

    public RingCentralException(Throwable t) {
        super(t);
    }

}
