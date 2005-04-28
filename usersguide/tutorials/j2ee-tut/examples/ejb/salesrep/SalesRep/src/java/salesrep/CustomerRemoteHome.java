
package salesrep;

import java.rmi.RemoteException;
import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.FinderException;


/**
 * This is the home interface for Customer enterprise bean.
 */
public interface CustomerRemoteHome extends javax.ejb.EJBHome {

    public CustomerRemote create(String customerId, String salesRepId, String name)
        throws RemoteException, CreateException;
    
    salesrep.CustomerRemote findByPrimaryKey(java.lang.String customerId)  throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    public Collection findBySalesRep(String salesRepId) throws FinderException, RemoteException;
    
}
