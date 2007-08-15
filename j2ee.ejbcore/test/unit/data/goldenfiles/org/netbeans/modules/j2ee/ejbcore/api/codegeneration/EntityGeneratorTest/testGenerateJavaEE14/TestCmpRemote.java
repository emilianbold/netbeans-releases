/*
 * TestCmpRemote.java
 * 
 * Created on {date}, {time}
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testGenerateJavaEE14;

import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 *
 * @author {user}
 */
public interface TestCmpRemote extends EJBObject {

    java.lang.Long getPk() throws RemoteException;
    
}
