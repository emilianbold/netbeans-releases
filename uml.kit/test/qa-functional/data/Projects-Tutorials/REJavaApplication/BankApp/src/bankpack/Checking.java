/*
 * Checking.java
 *
 * Created on August 11, 2005, 1:40 PM
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
public class Checking extends BankAccount {
    private static final double overdraftPenalty = 25.00;

    
    /** Creates a new instance of Checking */
    public Checking() {
    }
    public Checking(java.lang.String num, double initAmount) {
    super(num,initAmount);
    }
    
    public void withdraw(double val) throws bankpack.NoAvailableFundsException {
        
        if (getBalance() <= val){
            deductOverDraftPenalty();
            
        }
        else
            super.withdraw(val);
    }
    
    public String accountType(){
        return "Checking Account:       ";
    }
    
    public String toString()
    { return super.toString() + getMessage();
    }
    
    private double getOverdraftPenalty() {
        return overdraftPenalty;
    }
    
    private void deductOverDraftPenalty(){
        try{
            super.withdraw(getOverdraftPenalty());
        }
        catch(Exception ex){ex.printStackTrace();
        }
    }

    
}
