/*
 * TestMDBTopicBean.java
 *
 * Created on {date}, {time}
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package testGenerateJavaEE50;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author {user}
 */
@MessageDriven(mappedName = "jms/TestMDBTopicBean", activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic"),
        @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
        @ActivationConfigProperty(propertyName = "clientId", propertyValue = "TestMDBTopicBean"),
        @ActivationConfigProperty(propertyName = "subscriptionName", propertyValue = "TestMDBTopicBean")
    })
public class TestMDBTopicBean implements MessageListener {
    
    public TestMDBTopicBean() {
    }

    public void onMessage(Message message) {
    }
    
}
