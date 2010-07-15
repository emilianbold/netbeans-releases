/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package pack;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.PrePassivate;
import javax.ejb.Remove;
import javax.jms.Message;
import javax.jms.MessageListener;

/**
 *
 * @author den
 */
@MessageDriven(mappedName = "a", activationConfig =  {
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")
    })
public class NewMessageBean implements MessageListener {
    
    public NewMessageBean() {
    }

    @Remove
    public void onMessage(Message message) {
    }

    public void operation(){
        
    }

    public void ejbActivate(){

    }

    public void ejbPassivate(){

    }

    public void ejbRemove(){

    }

    public void setSessionContext(){

    }

    public void setEntityContext(){

    }

    public void unsetEntityContext(){

    }

    public void setMessageDrivenContext(){

    }

    @PrePassivate
    public void ejbStore(){
        
    }
    
}
