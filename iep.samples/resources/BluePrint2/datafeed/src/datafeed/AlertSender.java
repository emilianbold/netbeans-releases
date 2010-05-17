/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package datafeed;

import java.util.List;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import com.sun.messaging.Queue;
import com.sun.messaging.QueueConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.TextMessage;

public class AlertSender implements Runnable {
    List<Alert> alertList;
    boolean stop;
    boolean stopped;
    Object stopMonitor = new Object();
    Thread thread;

    public AlertSender(List<Alert> alertList) {
        this.alertList = alertList;
        thread = new Thread(this);
        thread.start();
    }
    
    public void run() {
        stop = false;
        stopped = false;
        QueueConnection connection = null;
        QueueSession session = null;
        Queue q = null;
        QueueSender sender = null;
        try {
            QueueConnectionFactory connFactory = new QueueConnectionFactory();
            connFactory.setProperty("imqBrokerHostName", "localhost");
            connFactory.setProperty("imqBrokerHostPort", "7676");
            connection = connFactory.createQueueConnection();
            session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
            q = new com.sun.messaging.Queue("sequencer_DeviceAlerts");
            sender = session.createSender(q);
            
            for (int i = 0, I = alertList.size(); !stop && i < I; i++) {
                try {
                    Alert alert = alertList.get(i);
                    TextMessage textMessage = session.createTextMessage();
                    textMessage.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
                    textMessage.setText(alert.toXml());
                    System.out.println("Sending alert " + alert);
                    sender.send(textMessage);
                } catch (JMSException e) {
                    e.printStackTrace();
                }    
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
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
