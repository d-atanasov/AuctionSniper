package test.auctionsniper;

import static org.hamcrest.Matchers.equalTo;

import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import auctionsniper.Auction;
import auctionsniper.AuctionHouse;
import auctionsniper.AuctionSniper;
import auctionsniper.SniperCollector;
import auctionsniper.SniperLauncher;

public class SniperLauncherTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    private final Auction auction = context.mock(Auction.class);
    private final AuctionHouse auctionHouse = context.mock(AuctionHouse.class);
    private final SniperCollector sniperCollector = context.mock(SniperCollector.class);
    private final States auctionState = context.states("auction state").startsAs("not joined");
    private final SniperLauncher launcher = new SniperLauncher(auctionHouse, sniperCollector);

    @Test
    public void addsNewSniperToCollectorAndThenJoinsAuction() {
        final String itemId = "item 123";
        context.checking(new Expectations() {
            {
                allowing(auctionHouse).auctionFor(itemId);
                will(returnValue(auction));
                oneOf(auction).addAuctionEventListener(with(sniperForItem(itemId)));
                when(auctionState.is("not joined"));
                oneOf(sniperCollector).addSniper(with(sniperForItem(itemId)));
                when(auctionState.is("not joined"));
                oneOf(auction).join();
                then(auctionState.is("joined"));
            }
        });
        launcher.joinAuction(itemId);
    }

    protected Matcher<AuctionSniper> sniperForItem(String itemId) {
        return new FeatureMatcher<AuctionSniper, String>(equalTo(itemId), "sniper with item id", "item") {
            @Override
            protected String featureValueOf(AuctionSniper actual) {
                return actual.getSnapshot().itemId;
            }
        };
    }
}
