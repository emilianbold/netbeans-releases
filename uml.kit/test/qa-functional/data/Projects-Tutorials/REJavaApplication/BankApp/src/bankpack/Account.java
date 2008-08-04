/*
 * Account.java
 *
 * Created on August 11, 2005, 1:21 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package bankpack;

/**
 *
 * @author Administrator
 */
public interface Account {
    public double getBalance();
    public String getAccountNumber();
    
    public void withdraw(double val) throws bankpack.NoAvailableFundsException;
    
    public void deposit(double val);
    
    public String accountType();
    
}
