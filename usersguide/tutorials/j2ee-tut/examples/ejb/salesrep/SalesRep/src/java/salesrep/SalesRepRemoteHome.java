
package salesrep;

import java.rmi.RemoteException;
import javax.ejb.CreateException;


/**
 * This is the home interface for SalesRep enterprise bean.
 */
public interface SalesRepRemoteHome extends javax.ejb.EJBHome {
    
    public SalesRepRemote create(String salesRepId, String name)
        throws RemoteException, CreateException;
    SalesRepRemote findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    
}
