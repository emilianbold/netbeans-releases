
package cart;


/**
 * This is the local-home interface for Cart enterprise bean.
 */
public interface CartLocalHome extends javax.ejb.EJBLocalHome {
    
    
    
    /**
     *
     */
    cart.CartLocal create()  throws javax.ejb.CreateException;

    cart.CartLocal create(java.lang.String person) throws javax.ejb.CreateException;

    cart.CartLocal create(java.lang.String person, java.lang.String id) throws javax.ejb.CreateException;
    
    
}
