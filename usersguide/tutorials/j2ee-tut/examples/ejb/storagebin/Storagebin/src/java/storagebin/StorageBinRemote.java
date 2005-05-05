
package storagebin;


/**
 * This is the remote interface for StorageBin enterprise bean.
 */
public interface StorageBinRemote extends javax.ejb.EJBObject, storagebin.StorageBinRemoteBusiness {
    String getWidgetId() throws java.rmi.RemoteException;

    int getQuantity() throws java.rmi.RemoteException;
    
    
}
