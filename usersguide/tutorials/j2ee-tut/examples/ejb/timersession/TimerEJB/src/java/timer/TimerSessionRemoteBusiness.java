
package timer;


/**
 * This is the business interface for TimerSession enterprise bean.
 */
public interface TimerSessionRemoteBusiness {
    void myCreateTimer(long intervalDuration) throws java.rmi.RemoteException;
    
}
