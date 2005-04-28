
package cart;


/**
 * This is the home interface for Cart enterprise bean.
 */
public interface CartRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    cart.CartRemote create()  throws javax.ejb.CreateException, java.rmi.RemoteException;

    cart.CartRemote create(java.lang.String person) throws javax.ejb.CreateException, java.rmi.RemoteException;

    cart.CartRemote create(java.lang.String person, java.lang.String id) throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
