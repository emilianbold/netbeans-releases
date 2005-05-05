
package storagebin;


/**
 * This is the remote interface for Widget enterprise bean.
 */
public interface WidgetRemote extends javax.ejb.EJBObject, storagebin.WidgetRemoteBusiness {
    String getDescription() throws java.rmi.RemoteException;

    double getPrice() throws java.rmi.RemoteException;
    
    
}
