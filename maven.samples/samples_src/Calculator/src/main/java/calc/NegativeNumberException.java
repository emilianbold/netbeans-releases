/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package calc;

/**
 *
 * @author mkuchtiak
 */
public class NegativeNumberException extends Exception {

    /**
     * Creates a new instance of <code>NegatineNumberException</code> without detail message.
     */
    public NegativeNumberException() {
    }


    /**
     * Constructs an instance of <code>NegatineNumberException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public NegativeNumberException(String msg) {
        super(msg);
    }
}
