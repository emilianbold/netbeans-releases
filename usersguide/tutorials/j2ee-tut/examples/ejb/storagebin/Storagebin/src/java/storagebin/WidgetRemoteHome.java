
package storagebin;

import javax.ejb.CreateException;


/**
 * This is the home interface for Widget enterprise bean.
 */
public interface WidgetRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    storagebin.WidgetRemote findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException, java.rmi.RemoteException;

    storagebin.WidgetRemote create(java.lang.String widgetId, java.lang.String description, double price) throws CreateException, java.rmi.RemoteException;
    
    
}
