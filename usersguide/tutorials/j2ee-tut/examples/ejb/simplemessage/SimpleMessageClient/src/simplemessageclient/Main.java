/*
 * Main.java
 *
 * Created on March 23, 2005, 11:39 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package simplemessageclient;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *
 * @author blaha
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Context jndiContext = null;
        ConnectionFactory connectionFactory = null;
        Connection connection = null;
        Session session = null;
        Destination destination = null;
        MessageProducer messageProducer = null;
        TextMessage message = null;
        final int NUM_MSGS = 3;
        
        try {
            jndiContext = new InitialContext();
        } catch (NamingException e) {
            System.out.println("Could not create JNDI " + "context: " +
                    e.toString());
            System.exit(1);
        }
        
        try {
            connectionFactory =
                    (ConnectionFactory) jndiContext.lookup(
                    "jms/SimpleMessageDestinationFactory");
            destination =
                    (Queue) jndiContext.lookup("jms/SimpleMessageBean");
        } catch (NamingException e) {
            System.out.println("JNDI lookup failed: " + e.toString());
            System.exit(1);
        }
        
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            messageProducer = session.createProducer(destination);
            message = session.createTextMessage();
            
            for (int i = 0; i < NUM_MSGS; i++) {
                message.setText("This is message " + (i + 1));
                System.out.println("Sending message: " + message.getText());
                messageProducer.send(message);
            }
            
            System.out.println("To see if the bean received the messages,");
            System.out.println(
                    " check <install_dir>/domains/domain1/logs/server.log.");
        } catch (JMSException e) {
            System.out.println("Exception occurred: " + e.toString());
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
            System.exit(0);
        }
    }
    
}
