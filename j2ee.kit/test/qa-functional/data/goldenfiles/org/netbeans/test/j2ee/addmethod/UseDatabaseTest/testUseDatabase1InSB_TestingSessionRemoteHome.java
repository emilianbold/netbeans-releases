
package test;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;


/**
 * This is the home interface for TestingSession enterprise bean.
 */
public interface TestingSessionRemoteHome extends EJBHome {
    
    
    
    /**
     *
     */
    TestingSessionRemote create()  throws CreateException, RemoteException;
    
    
}
