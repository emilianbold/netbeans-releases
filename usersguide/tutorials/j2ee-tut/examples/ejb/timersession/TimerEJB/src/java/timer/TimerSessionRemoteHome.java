
package timer;


/**
 * This is the home interface for TimerSession enterprise bean.
 */
public interface TimerSessionRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    timer.TimerSessionRemote create()  throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
