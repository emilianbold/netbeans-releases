/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package search_replace;

import org.netbeans.junit.NbTestSuite;

/**
 * Test of search and replace functionality in editor.
 *
 * @author Roman Strobl
 */
public class SearchAndReplaceTestSuite extends NbTestSuite {
    
    public SearchAndReplaceTestSuite() {
        super("Search and Replace");
        
        addTestSuite(SearchAndReplaceTest.class);
        //addTestSuite(SearchTest.class);
        //addTestSuite(ReplaceTest.class);
    }
    
    public static NbTestSuite suite() {
        return new SearchAndReplaceTestSuite();
    }
    
}
