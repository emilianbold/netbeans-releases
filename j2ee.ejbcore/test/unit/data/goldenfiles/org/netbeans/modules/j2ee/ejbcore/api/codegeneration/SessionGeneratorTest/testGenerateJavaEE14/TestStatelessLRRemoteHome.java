/*
 * TestStatelessLRRemoteHome.java
 *
 * Created on {date}, {time}
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;

/**
 *
 * @author {user}
 */
public interface TestStatelessLRRemoteHome extends EJBHome {

    testGenerateJavaEE14.TestStatelessLRRemote create()  throws CreateException, RemoteException;
    
}
