/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datafeed;

import com.sun.messaging.Queue;
import com.sun.messaging.QueueConnectionFactory;
import java.util.Map;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.QueueConnection;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

public class MissingAlertRequestHandler implements Runnable {
    Map<String, Alert> alertRegistry;
    boolean stop;
    boolean stopped;
    Object stopMonitor = new Object();
    Thread thread;
    
    static String getAttribute(String msg, String attributeName) {
        int i = msg.indexOf("<" + attributeName + ">");
        if (i < 0) {
            return null;
        }
        int end = msg.indexOf("</" + attributeName + ">", i);
        if (end < 0) {
            return null;
        }
        int start = i + 2 + attributeName.length(); // 2 for < and >
        String attribute = msg.substring(start, end);
        return attribute;
    }
    
    public MissingAlertRequestHandler(Map<String, Alert> alertRegistry) {
        this.alertRegistry = alertRegistry;
        thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        stop = false;
        stopped = false;
        QueueConnection connection = null;
        QueueSession session = null;
        Queue alertQ = null;
        QueueSender sender = null;
        Queue requestQ = null;
        QueueReceiver receiver = null;
        try {
            QueueConnectionFactory connFactory = new QueueConnectionFactory();
            connFactory.setProperty("imqBrokerHostName", "localhost");
            connFactory.setProperty("imqBrokerHostPort", "7676");
            connection = connFactory.createQueueConnection();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            alertQ = new com.sun.messaging.Queue("sequencer_DeviceAlerts");
            sender = session.createSender(alertQ);
            requestQ = new com.sun.messaging.Queue("sequencer_MissingAlertRequests");
            receiver = session.createReceiver(requestQ);
            connection.start();
            TextMessage receivedTextMessage = null;
            while (!stop) {
                try {
                    receivedTextMessage = (TextMessage) receiver.receive(1000);
                    if (stop) {
                        return;
                    }
                    if (receivedTextMessage == null) {
                        continue;
                    }
                    String receivedMessage = ((TextMessage) receivedTextMessage).getText();
                    String deviceId = getAttribute(receivedMessage, "deviceID");
                    String sequenceNum = getAttribute(receivedMessage, "sequenceNum");
                    System.out.println("        Received quest for alert: " + deviceId + "." + sequenceNum);
                    
                    // Query the registry
                    Alert alert = alertRegistry.get(Alert.makeKey(deviceId, sequenceNum));
                    
                    if (alert == null) {
                        System.out.println("        Cannot find alert for " + deviceId + "." + sequenceNum);
                        continue;
                    }
                    TextMessage textMessage = session.createTextMessage();
                    textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
                    textMessage.setText(alert.toXml());
                    System.out.println("        Sending alert " + alert);
                    sender.send(textMessage);
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (sender != null) {
                try {
                    sender.close();
                } catch (Exception e) {
                }
            }
            if (receiver != null) {
                try {
                    receiver.close();
                } catch (Exception e) {
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
            stopped = true;
            synchronized(stopMonitor) {
                stopMonitor.notifyAll();
            }
        }
    }
    
    public void stop() {
        stop = true;
        while (!stopped) {
            synchronized(stopMonitor) {
                try {
                    stopMonitor.wait();
                } catch (InterruptedException e) {
                }    
            }
        }    
    }
}
