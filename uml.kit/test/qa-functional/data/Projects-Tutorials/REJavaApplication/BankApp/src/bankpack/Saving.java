/*
 * Saving.java
 *
 * Created on August 11, 2005, 1:55 PM
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
public class Saving extends BankAccount{
    
    /** Creates a new instance of Saving */
    public Saving(){
    }
    
    public Saving(java.lang.String num, double initAmount, double rate) {
        super(num, initAmount, rate);
       
    }
    
    public String accountType() {
        return "Saving Account:       ";
    }
    
    public void addInterest() {
        deposit(getInterestRate() * getBalance());
    }    

    
    
}
