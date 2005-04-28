
package salesrep;

import java.rmi.RemoteException;


/**
 * This is the business interface for Customer enterprise bean.
 */
public interface CustomerRemoteBusiness {
    public String getSalesRepId() throws RemoteException;
                                                                                         
    public String getName() throws RemoteException;
                                                                                         
    public void setSalesRepId(String salesRepId) throws RemoteException;
                                                                                         
    public void setName(String name) throws RemoteException;
}
