/*
 * InsufficientBalanceException.java
 *
 * Created on March 23, 2005, 4:26 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package bank;

/**
 *
 * @author blaha
 */
public class InsufficientBalanceException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>InsufficientBalanceException</code> without detail message.
     */
    public InsufficientBalanceException() {
    }
    
    
    /**
     * Constructs an instance of <code>InsufficientBalanceException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public InsufficientBalanceException(String msg) {
        super(msg);
    }
}
