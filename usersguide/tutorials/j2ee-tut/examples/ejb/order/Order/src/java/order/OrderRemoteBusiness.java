
package order;

import java.util.ArrayList;

/**
 * This is the business interface for Order enterprise bean.
 */
public interface OrderRemoteBusiness {
	
	ArrayList getLineItems() throws java.rmi.RemoteException;

    String getCustomerId() throws java.rmi.RemoteException;

    double getTotalPrice() throws java.rmi.RemoteException;

    String getStatus() throws java.rmi.RemoteException;	
    
}
