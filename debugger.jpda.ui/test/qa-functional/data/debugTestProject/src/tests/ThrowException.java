package tests;

import java.util.Vector;

/** Test class throwing exception to be caught by exception breakpoint.
 *
 * @author Jiri Kovalsky
 */
public class ThrowException {
    
    public static void main(String args[]) {
        Vector vector = new Vector();
        vector.add(new Integer(1));
        
        try {
            vector.get(1);
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            System.out.println("Error: There is no 2nd item.");
        }
        
        String number = (String) vector.get(0);
    }
}