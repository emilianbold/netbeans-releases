package beans;

import javax.ejb.*;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import java.util.logging.Logger; 

/**
 * This is the bean class for the SimpleMessageBean enterprise bean.
 * Created May 16, 2006 4:03:36 PM
 * @author ak199487
 */
public class SimpleMessageBean implements MessageDrivenBean, MessageListener {
    private MessageDrivenContext context;
    static final Logger logger =Logger.getLogger("SimpleMessageBean"); 

    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    
    /**
     * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
     */
    public void setMessageDrivenContext(MessageDrivenContext aContext) {
        context = aContext;
    }
    
    /**
     * See section 15.4.4 of the EJB 2.0 specification
     * See section 15.7.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    }
    
    /**
     * @see javax.ejb.MessageDrivenBean#ejbRemove()
     */
    public void ejbRemove() {
        // TODO release any resource acquired in ejbCreate.
        // The code here should handle the possibility of not getting invoked
        // See section 15.7.3 of the EJB 2.1 specification
    }
    
    // </editor-fold>
    
    public void onMessage(Message aMessage) {
         TextMessage msg = null;

  try {
    if (aMessage instanceof TextMessage) {
      msg = (TextMessage) aMessage;
      logger.info("MESSAGE BEAN: Message received: " +
        msg.getText());
    } else {
      logger.warning("Message of wrong type: " +
        aMessage.getClass().getName());
    }
  } catch (JMSException e) {
    e.printStackTrace();
   context.setRollbackOnly();
  } catch (Throwable te) {
    te.printStackTrace();
  }
} 


    
    
}
