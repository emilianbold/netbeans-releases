/*
 * testRegexpComplex.java
 *
 * Created on January 24, 2005, 2:50 PM
 */

package search_replace.SearchTest;

/**
 *
 * @author rs155161
 */
public class testRegexpComplex {
    
    /** Creates a new instance of testRegexpComplex */
    public testRegexpComplex() {
        /** Search for: a?B*c{2}[dD]e{1,}\.F{1,2}\s[^g]
         * ccde.F n           - ok
         * aBBBBccDeeeee.FF x - ok
         * aBBBBccDeeeee.FF g - not ok
         * accDe.F z          - ok
         * aBBccdee?F z       - not ok
         * ccde.FF  z         - ok
         * aBccDe.FFmz        - not ok
         */
    }
    
}
