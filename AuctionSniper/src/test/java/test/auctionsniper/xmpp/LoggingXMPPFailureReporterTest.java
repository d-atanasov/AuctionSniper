package test.auctionsniper.xmpp;

import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;

import auctionsniper.xmpp.LoggingXMPPFailureReporter;

public class LoggingXMPPFailureReporterTest {
    @Rule
    public final JUnitRuleMockery context = new JUnitRuleMockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    final Logger logger = context.mock(Logger.class);
    final LoggingXMPPFailureReporter reporter = new LoggingXMPPFailureReporter(logger);

    @AfterClass
    public static void resetLogging() {
        LogManager.getLogManager().reset();
    }

    @Test
    public void writesMessageTranslationFailureToLog() {
        context.checking(new Expectations() {
            {
                oneOf(logger).severe("<auction id> " + "Could not translate message \"bad message\" "
                        + "because \"java.lang.Exception: bad\"");
            }
        });
        reporter.cannotTranslateMessage("auction id", "bad message", new Exception("bad"));
    }
}
