
package test;

import java.rmi.RemoteException;


/**
 * This is the business interface for TestingSession enterprise bean.
 */
public interface TestingSessionRemoteBusiness {
    String testBusinessMethod1() throws RemoteException;

    String testBusinessMethod2(String a, int b) throws Exception, java.rmi.RemoteException;
    
}
