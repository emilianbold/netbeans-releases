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
package org.openide.util.enum;

import java.util.Enumeration;
import org.netbeans.performance.MultiInstanceIntArgBenchmark;

public abstract class EnumBenchmark extends MultiInstanceIntArgBenchmark {
    
    public EnumBenchmark( String name ) {
	super( name, new Integer[] { i(16), i(100), i(1000) } );
    }

    public void testEnumerationBlind() {
	int count = getIterationCount();
	int arg = getIntArg();
	
	while( count-- > 0 ) {
	    Enumeration e = (Enumeration)instances[count];
	    for( int i=0; i<arg; i++ ) e.nextElement();
	}
    }
    
    public void testEnumerationTesting() {
	int count = getIterationCount();
	
	while( count-- > 0 ) {
	    Enumeration e = (Enumeration)instances[count];
	    while( e.hasMoreElements() ) e.nextElement();
	}
    }    
}
