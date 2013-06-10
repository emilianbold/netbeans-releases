/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testGenerateJavaEE70;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author {user}
 */
@MessageDriven(activationConfig =  {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "TestMessageDestination")
    })
public class TestMDBQueueBean implements MessageListener {
    
    public TestMDBQueueBean() {
    }

    @Override
    public void onMessage(Message message) {
    }
    
}
