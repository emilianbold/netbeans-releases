
package salesrep;

import java.util.ArrayList;


/**
 * This is the business interface for SalesRep enterprise bean.
 */
public interface SalesRepRemoteBusiness {
    String getName() throws java.rmi.RemoteException;
    
    void setName(java.lang.String name) throws java.rmi.RemoteException;
    
    ArrayList getCustomerIds() throws java.rmi.RemoteException;
    
}
