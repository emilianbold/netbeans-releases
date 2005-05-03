
package converter;


/**
 * This is the home interface for Converter enterprise bean.
 */
public interface ConverterRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    converter.ConverterRemote create()  throws javax.ejb.CreateException, java.rmi.RemoteException;
    
    
}
