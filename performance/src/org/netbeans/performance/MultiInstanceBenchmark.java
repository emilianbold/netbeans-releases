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
 * Base class for benchmarks that need a separate instance for every
 * iteration and need to have them prepared before the actual test. 
 *
 * The benchmark based on this class needs only to implement method
 * createInstance, and implement test methods that will use array
 * of created instances, named instances. It can also override
 * preSetUp method to do some per-run initialization. It is called
 * at the very beginning of setUp().
 * 
 * Example:
 * <PRE>
 * class ListTest extends MultiInstanceBenchmark {
 *
 *     public ListTest( String name ) {
 *         super( name, new Object[] {
 *             new Integer(10), new Integer(100), new Integer(1000)
 *         }
 *     }
 *
 *     protected Object createInstance() {
 *         return new ArrayList();
 *     }
 *
 *     public void testAppend() {
 *         int count = getIterationsCount();
 *         int arg = ((Integer)getArgument()).intValue();
 *
 *         while( count-- > 0 ) {
 *             for( int i=0; i<arg; i++ ) {
 *                 ((List)instances[count]).add( null );
 *             }
 *         }
 *     }
 * }
 *
 * @author  Petr Nejedly
 * @version 0.9
 */
public abstract class MultiInstanceBenchmark extends Benchmark {
    
    public MultiInstanceBenchmark( String name ) {
	super( name );
    }

    public MultiInstanceBenchmark( String name, Object[] args ) {
	super( name, args );
    }
    

    protected Object[] instances;

    protected void setUp() throws Exception {
	preSetUp();
	
	int iters = getIterationCount();
	
	instances = new Object[iters];
	for( int i=0; i<iters; i++ ) {
	    instances[i] = createInstance();
	}
    }

    protected void preSetUp() {
    }
    
    protected abstract Object createInstance();

    protected void tearDown() throws Exception {
	instances = null;
    }
    
}
