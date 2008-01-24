package test;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.*;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * This is the bean class for the TestingSessionBean enterprise bean.
 * Created 29.4.2005 15:24:25
 * @author lm97939
 */
public class TestingSessionBean implements SessionBean, TestingSessionRemoteBusiness, TestingSessionLocalBusiness {
    private SessionContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods or web service operations
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(SessionContext aContext) {
        context = aContext;
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbActivate()
     */
    public void ejbActivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbPassivate()
     */
    public void ejbPassivate() {
        
    }
    
    /**
     * @see javax.ejb.SessionBean#ejbRemove()
     */
    public void ejbRemove() {
        
    }
    // </editor-fold>
    
    /**
     * See section 7.10.3 of the EJB 2.0 specification
     * See section 7.11.3 of the EJB 2.1 specification
     */
    public void ejbCreate() {
        // TODO implement ejbCreate if necessary, acquire resources
        // This method has access to the JNDI context so resource aquisition
        // spanning all methods can be performed here such as home interfaces
        // and data sources.
    }
    
    
    
    // Add business logic below. (Right-click in editor and choose
    // "EJB Methods > Add Business Method" or "Web Service > Add Operation")

    public String testBusinessMethod1() {
        //TODO implement testBusinessMethod1
        return null;
    }

    public String testBusinessMethod2(String a, int b) throws Exception {
        //TODO implement testBusinessMethod2
        return null;
    }

    private TestingEntityLocalHome lookupTestingEntityBean() {
        try {
            Context c = new InitialContext();
            TestingEntityLocalHome rv = (TestingEntityLocalHome) c.lookup("java:comp/env/ejb/TestingEntityBean");
            return rv;
        }
        catch(NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }

    private TestingEntityRemoteHome lookupMyTestingEntityBean() {
        try {
            Context c = new InitialContext();
            Object remote = c.lookup("java:comp/env/ejb/MyTestingEntityBean");
            TestingEntityRemoteHome rv = (TestingEntityRemoteHome) PortableRemoteObject.narrow(remote, TestingEntityRemoteHome.class);
            return rv;
        }
        catch(NamingException ne) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE,"exception caught" ,ne);
            throw new RuntimeException(ne);
        }
    }

    private Message createJMSMessageForTestingMessageDestination(Session session, Object messageData) throws JMSException {
        // TODO create and populate message to send
        // javax.jms.TextMessage tm = session.createTextMessage();
        // tm.setText(messageData.toString());
        // return tm;
    }

    private void sendJMSMessageToTestingMessageDestination(Object messageData) throws NamingException, JMSException {
        Context c = new InitialContext();
        ConnectionFactory cf = (ConnectionFactory) c.lookup("java:comp/env/jms/TestingMessageDestinationFactory");
        Connection conn = null;
        Session s = null;
        try { 
            conn = cf.createConnection();
            s = conn.createSession(false,s.AUTO_ACKNOWLEDGE);
            Destination destination = (Destination) c.lookup("java:comp/env/jms/TestingMessageDestination");
            MessageProducer mp = s.createProducer(destination);
            mp.send(createJMSMessageForTestingMessageDestination(s,messageData));
        } finally {
            if (s != null) {
                s.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    
    
}
