/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.collections;

import org.netbeans.performance.Benchmark;
import java.util.*;

public class ListTestArrayListForN extends ListTest {

    /** Creates new FSTest */
    public ListTestArrayListForN(String name) {
        super( name );
    }
    
    protected List createList( int size ) {
        return new ArrayList( size );
    }
    
    public static void main( String[] args ) {
	simpleRun( ListTestArrayListForN.class );
    }
}
