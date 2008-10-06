/*
 * Main.java
 *
 * Created on August 12, 2005, 10:52 AM
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
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Checking checkingAccount = new Checking("1234",150);
        Saving savingAccount = new Saving("12399", 100,0.06);
        savingAccount.deposit(700.00);
        try{
            checkingAccount.withdraw(60.00);
            
        }
        
        catch(Exception ex){ex.printStackTrace();
        }
        System.out.println("Checking Balance is: " +
                checkingAccount.getBalance() );
        System.out.println("Saving Balance is: " +
                savingAccount.getBalance() );
    }
    
}
