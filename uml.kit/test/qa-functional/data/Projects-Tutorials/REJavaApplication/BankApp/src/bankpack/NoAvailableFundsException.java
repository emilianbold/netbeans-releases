/*
 * NoAvailableFundsException.java
 *
 * Created on August 11, 2005, 1:24 PM
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
public class NoAvailableFundsException extends java.lang.Exception {
    
    /** Creates a new instance of NoAvailableFundsException */
    public NoAvailableFundsException() {
    }

     /**
     * Constructs an instance of <code>NoAvailableFundsException</code> with the specified detail message.
     * @param msg the detail message.
     */

     public NoAvailableFundsException(String msg) {
        super(msg);

    }
    
}
