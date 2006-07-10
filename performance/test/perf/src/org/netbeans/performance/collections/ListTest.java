/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.performance.collections;

import org.netbeans.performance.MultiInstanceIntArgBenchmark;
import java.util.*;

public abstract class ListTest extends MultiInstanceIntArgBenchmark {

    /** Creates new FSTest */
    public ListTest(String name) {
        super( name, new Integer[] { i(1), i(5), i(10), i(100), i(1000) });
    }

    protected int getMaxIterationCount() {
        /* 50MBs / size of a filled list */
        int param = ((Integer)getArgument()).intValue();
        int itemSize = 20;
        int size = param*itemSize + 200;
        return 50000000/size;
    }

    protected final Object createInstance() {
	// create list instance
	List inst = createList( getIntArg() );
	
	//fill it with data for ToArray test
	if( "testToArray".equals( getName() ) ) {
	    int size = getIntArg();
            while( size-- > 0 ) inst.add( null );
	}
	
	return inst;
    }
    
    protected abstract List createList( int size );
    
    public void testAppend() throws Exception {
        int count = getIterationCount();
        int magnitude = getIntArg();

        while( count-- > 0 ) {
            // do the stuff here, 
            List act = (List)instances[count];
            for( int number = 0; number < magnitude; number++ ) {
                act.add( null );
            }
        }
    }    

    public void testToArray() throws Exception {
        int count = getIterationCount();
        int magnitude = getIntArg();

        while( count-- > 0 ) {
            // do the stuff here, 
            List act = (List)instances[count];
	    Object[] arr = act.toArray();
        }
    }    

}
