
package bank;

import java.math.BigDecimal;


/**
 * This is the business interface for SavingsAccount enterprise bean.
 */
public interface SavingsAccountRemoteBusiness {
    String getId() throws java.rmi.RemoteException;

    String getFirstName() throws java.rmi.RemoteException;

    String getLastName() throws java.rmi.RemoteException;

    void setLastName(java.lang.String lastName) throws java.rmi.RemoteException;

    BigDecimal getBalance() throws java.rmi.RemoteException;

    void credit(java.math.BigDecimal credit) throws java.rmi.RemoteException;
    void debit(BigDecimal amount) throws InsufficientBalanceException, java.rmi.RemoteException;
   
}
