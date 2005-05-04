
package roster;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.EJBHome;


/**
 * This is the home interface for RosterBean enterprise bean.
 */
public interface RosterRemoteHome extends EJBHome {

    RosterRemote create()  throws CreateException, RemoteException;
    
    
}
