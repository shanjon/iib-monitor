package com.newrelic.fit.plugins.messagebroker;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import com.ibm.mq.jms.MQTopic;
import com.ibm.mq.jms.MQTopicConnection;
import com.ibm.mq.jms.MQTopicConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;

/**
 * Java class to connect to MQ. Post and Retreive messages.
 *
 */
public class FetchResourceStatistics {

	String host = "127.0.0.1";
	int port = 1419;
	String channel = "SYSTEM.DEF.SVRCONN";
	private String qmgr = "TestQMgr";

	//private String topicName = "testerTopic";
	private String topicName = "$SYS/Broker/+/ResourceStatistics/#";
	//Context jndiContext = null;
	MQTopicConnectionFactory topicConnectionFactory = null;
	MQTopicConnection topicConnection = null;
	TopicSession topicSession = null;
	Topic topic = null;
	TopicSubscriber topicSubscriber = null;
	TextListener topicListener = null;

	public static void main(String[] args) {
		FetchResourceStatistics stats = new FetchResourceStatistics();
		stats.publishMessage();
		stats.startSubscriber();
		try {
			Thread.sleep(250000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		stats.closeSubscriber();
		stats.finish();
	}

	/**
	 * The TextListener class implements the MessageListener interface by
	 * defining an onMessage method for the DurableSubscriber class.
	 */
	private class TextListener implements MessageListener {
		//final SampleUtilities.DoneLatch monitor = new SampleUtilities.DoneLatch();

		/**
		 * Casts the message to a TextMessage and displays its text. A non-text
		 * message is interpreted as the end of the message stream, and the
		 * message listener sets its monitor state to all done processing
		 * messages.
		 *
		 * @param message
		 *            the incoming message
		 */
		public void onMessage(Message message) {
			System.out.println("Message arrived");
			if (message instanceof TextMessage) {
				TextMessage msg = (TextMessage) message;

				try {
					System.out.println("SUBSCRIBER: " + "Reading message: " + msg.getText());
				} catch (JMSException e) {
					System.err.println("Exception in " + "onMessage(): " + e.toString());
				}
			} else {
				//monitor.allDone();
			}
		}
	}

	/**
	 * Constructor: looks up a connection factory and topic and creates a
	 * connection and session.
	 */
	public FetchResourceStatistics() {

		/*
		 * Create a JNDI API InitialContext object if none exists yet.
		 */
		try {
			topicConnectionFactory = new MQTopicConnectionFactory();

			topicConnectionFactory.setHostName(host);
			topicConnectionFactory.setPort(port);
			topicConnectionFactory.setQueueManager(qmgr);
			topicConnectionFactory.setChannel(channel);
			topicConnectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not create Topic Connection " + e.toString());
			System.exit(1);
		}

		try {
			boolean login = false;
			if (login) {
				topicConnection = (MQTopicConnection) topicConnectionFactory.createTopicConnection("Prakash Reddy", "");
			} else {
				topicConnection = (MQTopicConnection) topicConnectionFactory.createTopicConnection();
			}

			topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = (MQTopic) topicSession.createTopic(topicName);

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Connection problem: " + e.toString());
			if (topicConnection != null) {
				try {
					topicConnection.close();
				} catch (JMSException ee) {
					e.printStackTrace();
				}
			}
			System.exit(1);
		}
	}
	
	public void publishMessage() {
		try {
			TopicPublisher publisher = topicSession.createPublisher(topic);
			long uniqueNumber = System.currentTimeMillis() % 1000;
		    TextMessage message = topicSession.createTextMessage("hello at " + uniqueNumber);
			publisher.publish(message);
			topicConnection.start();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/**
	 * Stops connection, then creates durable subscriber, registers message
	 * listener (TextListener), and starts message delivery; listener displays
	 * the messages obtained.
	 */
	public void startSubscriber() {
		try {
			System.out.println("Starting subscriber");
			//topicConnection.stop();
			topicSubscriber = topicSession.createSubscriber(topic);
			topicListener = new TextListener();
			topicSubscriber.setMessageListener(topicListener);

			System.out.println("Subscriber started");
		} catch (JMSException e) {
			System.err.println("Exception occurred: " + e.toString());
			e.printStackTrace();
		}
	}

	/**
	 * Blocks until publisher issues a control message indicating end of publish
	 * stream, then closes subscriber.
	 */
	public void closeSubscriber() {
		try {
			//topicListener.monitor.waitTillDone();
			System.out.println("Closing subscriber");
			topicSubscriber.close();
		} catch (JMSException e) {
			System.err.println("Exception occurred: " + e.toString());
		}
	}

	/**
	 * Closes the connection.
	 */
	public void finish() {
		if (topicConnection != null) {
			try {
				System.out.println("Unsubscribing from " + "durable subscription");
				topicSession.unsubscribe(topicName);
				topicConnection.close();
			} catch (JMSException e) {
			}
		}
	}
}
