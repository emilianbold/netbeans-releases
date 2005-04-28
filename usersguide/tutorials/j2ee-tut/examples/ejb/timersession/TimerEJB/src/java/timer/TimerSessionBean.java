package timer;

import javax.ejb.*;

/**
 * This is the bean class for the TimerSessionBean enterprise bean.
 * Created Apr 27, 2005 9:18:37 AM
 * @author blaha
 */
public class TimerSessionBean implements javax.ejb.SessionBean, timer.TimerSessionRemoteBusiness, TimedObject {
    private javax.ejb.SessionContext context;
    
    // <editor-fold defaultstate="collapsed" desc="EJB infrastructure methods. Click the + sign on the left to edit the code.">
    // TODO Add code to acquire and use other enterprise resources (DataSource, JMS, enterprise bean, Web services)
    // TODO Add business methods or web service operations
    /**
     * @see javax.ejb.SessionBean#setSessionContext(javax.ejb.SessionContext)
     */
    public void setSessionContext(javax.ejb.SessionContext aContext) {
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
    
    public void ejbTimeout(Timer timer) {
        System.out.println("TimerSessionBean: ejbTimeout ");
    }
    
    // Add business logic below. (Right-click in editor and choose
    // "EJB Methods > Add Business Method" or "Web Service > Add Operation")

    public void myCreateTimer(long intervalDuration) {
        System.out.println("TimerSessionBean: start createTimer ");

        TimerService timerService = context.getTimerService();
        Timer timer =
            timerService.createTimer(intervalDuration, "created timer");
    }
    
    
    
}
