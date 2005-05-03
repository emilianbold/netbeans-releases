
package order;

import java.util.Collection;
import javax.ejb.CreateException;


/**
 * This is the home interface for Order enterprise bean.
 */
public interface OrderRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    order.OrderRemote findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException, java.rmi.RemoteException;

    order.OrderRemote create(java.lang.String orderId, java.lang.String customerId, java.lang.String status, double totalPrice, java.util.ArrayList lineItems) throws CreateException, java.rmi.RemoteException;
    
    public Collection findByProductId(String productId) throws javax.ejb.FinderException, java.rmi.RemoteException;
    
}
