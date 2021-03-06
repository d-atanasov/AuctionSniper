package test.auctionsniper.ui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.Assert.assertEquals;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

import org.hamcrest.Matcher;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import auctionsniper.SniperSnapshot;
import auctionsniper.SniperState;
import auctionsniper.ui.Column;
import auctionsniper.ui.SnipersTableModel;
import auctionsniper.util.Defect;

public class SnipersTableModelTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery();
    private TableModelListener listener = context.mock(TableModelListener.class);
    private final SnipersTableModel model = new SnipersTableModel();

    @Before
    public void attachModelListener() {
        model.addTableModelListener(listener);
    }

    @Test
    public void hasEnoughColumns() {
        assertThat(model.getColumnCount(), equalTo(Column.values().length));
    }

    @Test
    public void setsSniperValuesInColumns() {
        SniperSnapshot joining = SniperSnapshot.joining("item123");
        SniperSnapshot bidding = new SniperSnapshot("item123", 555, 666, SniperState.BIDDING);
        context.checking(new Expectations() {
            {
                allowing(listener).tableChanged(with(anyInsertionEvent()));
                oneOf(listener).tableChanged(with(aChangeInRow(0)));
            }
        });
        model.addSniper(joining);
        model.sniperStateChanged(bidding);
        assertRowMatchesSnapshot(0, bidding);
    }

    @Test
    public void setsUpColumnHeadings() {
        for (Column column : Column.values()) {
            assertEquals(column.name, model.getColumnName(column.ordinal()));
        }
    }

    @Test
    public void notifiesListenersWhenAddingASniper() {
        SniperSnapshot joining = SniperSnapshot.joining("item123");
        context.checking(new Expectations() {
            {
                oneOf(listener).tableChanged(with(anInsertionAtRow(0)));
            }
        });
        assertEquals(0, model.getRowCount());
        model.addSniper(joining);
        assertEquals(1, model.getRowCount());
        assertRowMatchesSnapshot(0, joining);
    }

    @Test
    public void holdsSnipersInAdditionOrder() {
        context.checking(new Expectations() {
            {
                ignoring(listener);
            }
        });
        model.addSniper(SniperSnapshot.joining("item 0"));
        model.addSniper(SniperSnapshot.joining("item 1"));
        assertEquals("item 0", cellValue(0, Column.ITEM_IDENTIFIER));
        assertEquals("item 1", cellValue(1, Column.ITEM_IDENTIFIER));
    }

    @Test
    public void updatesCorrectRowForSniper() {
        SniperSnapshot snapshot1 = SniperSnapshot.joining("item 0");
        SniperSnapshot snapshot2 = SniperSnapshot.joining("item 1");

        context.checking(new Expectations() {
            {
                allowing(listener).tableChanged(with(anyInsertionEvent()));
                oneOf(listener).tableChanged(with(aChangeInRow(1)));
            }
        });

        model.addSniper(snapshot1);
        model.addSniper(snapshot2);

        SniperSnapshot winning2 = snapshot2.winning(123);
        model.sniperStateChanged(winning2);

        assertRowMatchesSnapshot(1, winning2);
    }

    @Test(expected = Defect.class)
    public void throwsDefectIfNoExistingSniperForAnUpdate() {
        model.sniperStateChanged(new SniperSnapshot("item 1", 123, 234, SniperState.WINNING));
    }

    private void assertRowMatchesSnapshot(int row, SniperSnapshot snapshot) {
        assertEquals(snapshot.itemId, cellValue(row, Column.ITEM_IDENTIFIER));
        assertEquals(snapshot.lastPrice, cellValue(row, Column.LAST_PRICE));
        assertEquals(snapshot.lastBid, cellValue(row, Column.LAST_BID));
        assertEquals(SnipersTableModel.textFor(snapshot.state), cellValue(row, Column.SNIPER_STATE));
    }

    private Object cellValue(int rowIndex, Column column) {
        return model.getValueAt(rowIndex, column.ordinal());
    }

    Matcher<TableModelEvent> anInsertionAtRow(final int row) {
        return samePropertyValuesAs(
                new TableModelEvent(model, row, row, TableModelEvent.ALL_COLUMNS, TableModelEvent.INSERT));
    }

    Matcher<TableModelEvent> anyInsertionEvent() {
        return hasProperty("type", equalTo(TableModelEvent.INSERT));
    }

    private Matcher<TableModelEvent> aChangeInRow(int row) {
        return samePropertyValuesAs(new TableModelEvent(model, row));
    }
}
