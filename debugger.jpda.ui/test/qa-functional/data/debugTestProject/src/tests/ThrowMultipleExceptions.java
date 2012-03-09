package tests;

import java.util.Vector;

/** Test class throwing exceptions to be caught by exception breakpoint.
 *
 * @author Jiri Kovalsky
 */
public class ThrowMultipleExceptions {
    
    public static void main(String args[]) {
        Vector vector = new Vector();
        vector.add(new Integer(1));
        
        for (int i = 0; i < 5; i++) {
            try {
                String number = (String) vector.get(0);
            } catch (java.lang.ClassCastException e) {
                System.out.println("Error: Item is not a string.");
            }
        }
    }
}