package test.endtoend.auctionsniper;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.hamcrest.Matchers;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;
import org.junit.Assert;

public class SingleMessageListener implements MessageListener {
	private final ArrayBlockingQueue<Message> messages = new ArrayBlockingQueue<Message>(1);

	public void processMessage(Chat chat, Message message) {
		messages.add(message);
	}

	public void receivesAMessage() throws InterruptedException {
		Assert.assertThat("Message", messages.poll(5, TimeUnit.SECONDS), Matchers.is(Matchers.notNullValue()));
	}
}
