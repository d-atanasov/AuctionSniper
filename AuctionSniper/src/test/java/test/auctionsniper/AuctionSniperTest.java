package test.auctionsniper;

import static auctionsniper.SniperState.BIDDING;
import static auctionsniper.SniperState.LOST;
import static auctionsniper.SniperState.WINNING;
import static auctionsniper.SniperState.WON;
import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import auctionsniper.Auction;
import auctionsniper.AuctionEventListener;
import auctionsniper.AuctionSniper;
import auctionsniper.SniperListener;
import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;

public class AuctionSniperTest {
    protected static final String ITEM_ID = "item-id";
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    private final States sniperState = context.states("sniper");
    private final SniperListener sniperListener = context.mock(SniperListener.class);
    private final Auction auction = context.mock(Auction.class);
    private final AuctionSniper sniper = new AuctionSniper(ITEM_ID, auction, sniperListener);

    @Test
    public void reportsLostWhenAuctionClosesImmediately() {
        context.checking(new Expectations() {
            {
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 0, 0, LOST));
            }
        });
        sniper.auctionClosed();
    }

    @Test
    public void bidsHigherAndReportsBiddingWhenNewPriceArrives() {
        final int price = 1001;
        final int increment = 25;
        final int bid = price + increment;

        context.checking(new Expectations() {
            {
                oneOf(auction).bid(bid);
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, price, bid, BIDDING));
            }
        });
        sniper.currentPrice(price, increment, AuctionEventListener.PriceSource.FromOtherBidder);
    }

    @Test
    public void reportsIsWinningWhenCurrentPriceComesFromSniper() {
        context.checking(new Expectations() {
            {
                ignoring(auction);
                allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(BIDDING)));
                then(sniperState.is("bidding"));
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, WINNING));
                when(sniperState.is("bidding"));
            }
        });
        sniper.currentPrice(123, 12, AuctionEventListener.PriceSource.FromOtherBidder);
        sniper.currentPrice(135, 45, AuctionEventListener.PriceSource.FromSniper);
    }

    @Test
    public void reportsLostIfAuctionClosesWhenBidding() {
        context.checking(new Expectations() {
            {
                ignoring(auction);
                allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(BIDDING)));
                then(sniperState.is("bidding"));
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 123, 168, LOST));
                when(sniperState.is("bidding"));
            }
        });
        sniper.currentPrice(123, 45, AuctionEventListener.PriceSource.FromOtherBidder);
        sniper.auctionClosed();
    }

    @Test
    public void reportsWonIfAuctionClosesWhenWinning() {
        context.checking(new Expectations() {
            {
                ignoring(auction);
                allowing(sniperListener).sniperStateChanged(with(aSniperThatIs(WINNING)));
                then(sniperState.is("winning"));
                atLeast(1).of(sniperListener).sniperStateChanged(new SniperSnapshot(ITEM_ID, 135, 135, WON));
                when(sniperState.is("winning"));
            }
        });
        sniper.currentPrice(123, 45, AuctionEventListener.PriceSource.FromSniper);
        sniper.auctionClosed();
    }

    private Matcher<SniperSnapshot> aSniperThatIs(final SniperState state) {
        return new FeatureMatcher<SniperSnapshot, SniperState>(equalTo(state), "sniper that is ", "was") {
            @Override
            protected SniperState featureValueOf(SniperSnapshot actual) {
                return actual.state;
            }
        };
    }
}
