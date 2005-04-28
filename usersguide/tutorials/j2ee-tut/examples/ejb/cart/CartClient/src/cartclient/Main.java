/*
 * Main.java
 *
 * Created on March 25, 2005, 10:25 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package cartclient;

import cart.CartRemote;
import cart.CartRemoteHome;
import exception.BookException;
import java.util.Enumeration;
import java.util.Vector;
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
            Context ctx = new InitialContext();
            Object objRef = ctx.lookup("ejb/CartBean");
            CartRemoteHome home =
                    (CartRemoteHome)PortableRemoteObject.narrow(objRef, CartRemoteHome.class);
            
            CartRemote shoppingCart = home.create("Duke DeEarl", "123");
            
            shoppingCart.addBook("The Martian Chronicles");
            shoppingCart.addBook("2001 A Space Odyssey");
            shoppingCart.addBook("The Left Hand of Darkness");
            
            Vector bookList = new Vector();
            
            bookList = shoppingCart.getContents();
            
            Enumeration enumer = bookList.elements();
            
            while (enumer.hasMoreElements()) {
                String title = (String) enumer.nextElement();
                
                System.out.println(title);
            }
            
            shoppingCart.removeBook("Alice in Wonderland");
            shoppingCart.remove();
            
            System.exit(0);
            
        }catch(BookException ex){
            System.err.println("Caught a BookException " + ex.getMessage());
            System.exit(0);
        }catch(Exception ex){
            System.err.println("Caught an unexpected exception: " + ex.getMessage());
            System.exit(1);
        }
    }
    
}
