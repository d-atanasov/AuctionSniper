package test.auctionsniper;

import static test.endtoend.auctionsniper.ApplicationRunner.SNIPER_ID;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import auctionsniper.AuctionEventListener;
import auctionsniper.AuctionMessageTranslator;

public class AuctionMessageTranslatorTest {
    private static final Chat UNUSED_CHAT = null;
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    private final AuctionEventListener listener = context.mock(AuctionEventListener.class);
    private final AuctionMessageTranslator translator = new AuctionMessageTranslator(SNIPER_ID, listener);

    @Test
    public void notfiesAuctionClosedWhenCloseMessageReceived() {
        context.checking(new Expectations() {
            {
                oneOf(listener).auctionClosed();
            }
        });
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: CLOSE;");
        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromOtherBidder() {
        context.checking(new Expectations() {
            {
                exactly(1).of(listener).currentPrice(192, 7, AuctionEventListener.PriceSource.FromOtherBidder);
            }
        });
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 192; Increment: 7; Bidder: Someone else;");
        translator.processMessage(UNUSED_CHAT, message);
    }

    @Test
    public void notifiesBidDetailsWhenCurrentPriceMessageReceivedFromSniper() {
        context.checking(new Expectations() {
            {
                exactly(1).of(listener).currentPrice(234, 5, AuctionEventListener.PriceSource.FromSniper);
            }
        });
        Message message = new Message();
        message.setBody("SOLVersion: 1.1; Event: PRICE; CurrentPrice: 234; Increment: 5; Bidder: " + SNIPER_ID + ";");
        translator.processMessage(UNUSED_CHAT, message);
    }
}
