
package checker;


/**
 * This is the business interface for CheckerBean enterprise bean.
 */
public interface CheckerRemoteBusiness {
    double applyDiscount(double amount) throws java.rmi.RemoteException;
    
}
