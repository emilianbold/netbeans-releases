/*
 * BookException.java
 *
 * Created on March 25, 2005, 10:16 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package exception;

/**
 *
 * @author blaha
 */
public class BookException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>BookException</code> without detail message.
     */
    public BookException() {
    }
    
    
    /**
     * Constructs an instance of <code>BookException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public BookException(String msg) {
        super(msg);
    }
}
