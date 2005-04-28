
package roster;


/**
 * This is the home interface for RosterBean enterprise bean.
 */
public interface RosterRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    roster.RosterRemote create()  throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
