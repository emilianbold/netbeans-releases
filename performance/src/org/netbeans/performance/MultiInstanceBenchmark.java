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
