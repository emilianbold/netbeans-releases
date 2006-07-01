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
