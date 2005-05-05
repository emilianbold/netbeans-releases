/*
 * Main.java
 *
 * Created on March 23, 2005, 1:09 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package savingsaccountclient;

import bank.SavingsAccountRemote;
import bank.SavingsAccountRemoteHome;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Iterator;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 *
 * @author blaha
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
        try{
        Context initial = new InitialContext();
            Object objref =
                initial.lookup("ejb/SavingsAccountBean");
                                                                                            
            SavingsAccountRemoteHome home =
                (SavingsAccountRemoteHome) PortableRemoteObject.narrow(objref,
                    SavingsAccountRemoteHome.class);
                                                                                            
            BigDecimal zeroAmount = new BigDecimal("0.00");
            SavingsAccountRemote duke =
                home.create("123", "Duke", "Earl", zeroAmount);
                                                                                            
            duke.credit(new BigDecimal("88.50"));
            duke.debit(new BigDecimal("20.25"));
                                                                                            
            BigDecimal balance = duke.getBalance();                                                                                           
            System.out.println("balance = " + balance);
            duke.remove();
            
            SavingsAccountRemote joe = home.create("836", "Joe", "Jones", zeroAmount);
                                                                                            
            joe.credit(new BigDecimal("34.55"));
                                                                                            
            SavingsAccountRemote jones = home.findByPrimaryKey("836");
                                                                                            
            jones.debit(new BigDecimal("2.00"));
            balance = jones.getBalance();
            System.out.println("balance = " + balance);
                                                                                            
            SavingsAccountRemote pat = home.create("456", "Pat", "Smith", zeroAmount);
                                                                                            
            pat.credit(new BigDecimal("44.77"));
                                                                                            
            SavingsAccountRemote john =
                home.create("730", "John", "Smith", zeroAmount);
                                                                                            
            john.credit(new BigDecimal("19.54"));
                                                                                            
            SavingsAccountRemote mary =
                home.create("268", "Mary", "Smith", zeroAmount);
                                                                                            
            mary.credit(new BigDecimal("100.07"));
                                                                                            
            Collection c = home.findLastName("Smith");
            Iterator i = c.iterator();
                                                                                            
            while (i.hasNext()) {
                SavingsAccountRemote account = (SavingsAccountRemote) i.next();
                String id = (String) account.getPrimaryKey();
                BigDecimal amount = account.getBalance();
                                                                                            
                System.out.println(id + ": " + amount);
            }
                                                                                            
            c = home.findInRange(new BigDecimal("20.00"),
                    new BigDecimal("99.00"));
            i = c.iterator();
                                                                                            
            while (i.hasNext()) {
                SavingsAccountRemote account = (SavingsAccountRemote) i.next();
                String id = (String) account.getPrimaryKey();
                BigDecimal amount = account.getBalance();
                            System.out.println(id + ": " + amount);
            }
                                                                                            
            SavingsAccountRemote pete =
                home.create("904", "Pete", "Carlson", new BigDecimal("5.00"));
            SavingsAccountRemote sally =
                home.create("905", "Sally", "Fortney", new BigDecimal("8.00"));
                                                                                            
            home.ChargeForLowBalance(new BigDecimal("10.00"),
                new BigDecimal("1.00"));
                                                                                            
            BigDecimal reducedAmount = pete.getBalance();
                                                                                            
            System.out.println(reducedAmount);
            reducedAmount = sally.getBalance();
            System.out.println(reducedAmount);
                                                                                            
            System.exit(0);
            
        }catch(Exception ex){
            System.err.println("Exception is caught: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
}
