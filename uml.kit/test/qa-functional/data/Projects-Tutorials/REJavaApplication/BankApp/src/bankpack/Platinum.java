/*
 * Platinum.java
 *
 * Created on August 11, 2005, 1:53 PM
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
public class Platinum extends BankAccount{
    
    private static final double overdraftLimit = 2500.00;
    
    private double availableFunds;
    
    //private static final java.math.BigDecimal overdraftLimit = new BigDecimal(2500.00);
    
    /** Creates a new instance of Platinum */
    public Platinum() {
    }
    
    public Platinum(java.lang.String num, double initAmount) {
    super(num,initAmount);
    availableFunds = overdraftLimit + getBalance();
    }
    
    public String accountType() {
        return "Platinume Account:       ";
    }
    
    public void withdraw(double val) throws bankpack.NoAvailableFundsException {
    if (val > getBalance()){
            useOverdraftLimit(val);
        }
        else
            super.withdraw(val);
    }
    
    public double getOverDraftLimit() {
        return overdraftLimit;
    }    
    
    private void useOverdraftLimit(double val) {
    availableFunds -= val ;
    }    
        
    public double getAvailableFunds() {
        return availableFunds;
    }    

    
}
