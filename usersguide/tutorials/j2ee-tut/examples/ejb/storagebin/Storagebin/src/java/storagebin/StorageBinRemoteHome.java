
package storagebin;

import javax.ejb.CreateException;


/**
 * This is the home interface for StorageBin enterprise bean.
 */
public interface StorageBinRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    storagebin.StorageBinRemote findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException, java.rmi.RemoteException;

    storagebin.StorageBinRemote create(java.lang.String storageBinId, java.lang.String widgetId, int quantity) throws CreateException, java.rmi.RemoteException;

    storagebin.StorageBinRemote findByWidgetId(java.lang.String widgetId) throws javax.ejb.FinderException, java.rmi.RemoteException;
    
    
}
