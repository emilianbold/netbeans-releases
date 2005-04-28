package beans;
import java.util.logging.Logger;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;

/**
 * This is the bean class for the SimpleMessageBean enterprise bean.
 * Created Mar 23, 2005 11:11:52 AM
 * @author blaha
 */
public class SimpleMessageBean implements javax.ejb.MessageDrivenBean, javax.jms.MessageListener {
    static final Logger logger = Logger.getLogger("SimpleMessageBean");
    private javax.ejb.MessageDrivenContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click on the + sign on the left to edit the code.">
    
    /**
     * @see javax.ejb.MessageDrivenBean#setMessageDrivenContext(javax.ejb.MessageDrivenContext)
     */
    public void setMessageDrivenContext(javax.ejb.MessageDrivenContext aContext) {
        logger.info("In SimpleMessageBean.setMessageDrivenContext()");
        context = aContext;
    }
    
    /**
     * See section 15.4.4 of the EJB 2.0 specification
     * See section 15.7.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
        logger.info("In SimpleMessageBean.ejbCreate()");
    }
    
    /**
     * @see javax.ejb.MessageDrivenBean#ejbRemove()
     */
    public void ejbRemove() {
        // TODO release any resource acquired in ejbCreate.
        // The code here should handle the possibility of not getting invoked
        // See section 15.7.3 of the EJB 2.1 specification
        logger.info("In SimpleMessageBean.ejbRemove()");
    }
    
    // </editor-fold>
    
    
    public void onMessage(javax.jms.Message aMessage) {
        TextMessage msg = null;      
        try {
            if (aMessage instanceof TextMessage) {
                msg = (TextMessage) aMessage;
                logger.info("MESSAGE BEAN: Message received: " + msg.getText());
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
