/*
 * testRegexpSimple.java
 *
 * Created on January 24, 2005, 2:35 PM
 */

package search_replace.SearchTest;

/**
 *
 * @author rs155161
 */
public class testRegexpSimple {
    
    /** Creates a new instance of testRegexpSimple */
    public testRegexpSimple() {
        /** Search for: [aA][hH][oO][jJ][0-9]{1,3}
         * aHoJ0     - ok
         * ahoj1     - ok
         * aHOJ2     - ok
         * Ahoj123   - ok
         * Ahojx     - not ok
         * Ahoj4     - ok
         */
    }
    
}
