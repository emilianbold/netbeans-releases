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
package org.netbeans.performance;

/**
 * Base class for MultiInstance benchmarks that use Integer arguments. 
 *
 * @author  Petr Nejedly
 * @version 0.1
 */
public abstract class MultiInstanceIntArgBenchmark extends MultiInstanceBenchmark {
    
    public MultiInstanceIntArgBenchmark( String name, Integer[] args ) {
	super( name, args );
    }
    
    protected static Integer i( int i ) {
	return new Integer( i );
    }
    
    protected int getIntArg() {
	return ((Integer)getArgument()).intValue();
    }
}
