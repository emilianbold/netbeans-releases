
package bank;

import java.math.BigDecimal;


/**
 * This is the home interface for SavingsAccount enterprise bean.
 */
public interface SavingsAccountRemoteHome extends javax.ejb.EJBHome {
    
    
    
    /**
     *
     */
    bank.SavingsAccountRemote findByPrimaryKey(java.lang.String key)  throws javax.ejb.FinderException, java.rmi.RemoteException;

    bank.SavingsAccountRemote create(java.lang.String id, java.lang.String firstName, java.lang.String lastName, java.math.BigDecimal balance) throws javax.ejb.CreateException, java.rmi.RemoteException;

    java.util.Collection findInRange(BigDecimal low, BigDecimal high) throws javax.ejb.FinderException, java.rmi.RemoteException;

    java.util.Collection findLastName(java.lang.String lastName) throws javax.ejb.FinderException, java.rmi.RemoteException;

    void ChargeForLowBalance(BigDecimal minimumBalance, BigDecimal charge) throws InsufficientBalanceException, java.rmi.RemoteException;
    
    
}
