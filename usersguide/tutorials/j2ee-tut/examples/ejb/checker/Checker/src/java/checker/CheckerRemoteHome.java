
package checker;


/**
 * This is the home interface for CheckerBean enterprise bean.
 */
public interface CheckerRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    checker.CheckerRemote create(String person)  throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
