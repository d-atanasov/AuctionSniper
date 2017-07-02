package auctionsniper.xmpp;

public class XMPPAuctionException extends Exception {
    private static final long serialVersionUID = 1L;

    public XMPPAuctionException(String message, Exception cause) {
        super(message, cause);
    }
}
