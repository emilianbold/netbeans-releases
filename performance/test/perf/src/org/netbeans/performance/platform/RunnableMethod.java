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

package org.netbeans.performance.platform;

import org.netbeans.performance.*;
import java.util.*;
import java.lang.reflect.Method;

/**
 * The benchmark comparing time to construct and run an anonymous Runnable
 * versus time to construct a single special Runable working over a Method.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class RunnableMethod extends Benchmark {
    
    public RunnableMethod(String name) {
        super( name, new Integer[] {
            new Integer(1), new Integer(100), new Integer( 10000 )
        });
    }
    
    /** Create new instance of unnamed Runnable implemented as independent
     * inner class for every round.
     */
    public void testUsingRealRunnable() {
        int count = getIterationCount();
        final int workload = ((Integer)getArgument()).intValue();
        
        while( count-- > 0 ) {
            new Runnable() {
                public void run() {
                    for( int i=0; i < workload; i++ );
                }
            }.run();
        }
    }
    
    
    /** Create new instance of unnamed Runnable implemented as a direct caller
     * to a worker method for every round.
     */
    public void testUsingDelegatingRunnable() {
        int count = getIterationCount();
        final int workload = ((Integer)getArgument()).intValue();
        
        while( count-- > 0 ) {
            new Runnable() {
                public void run() {
                    workerMethod( workload );
                }
            }.run();
        }
    }
    
    /** Create a new instance of MethodRunner - a class that will delegate
     * its task to passed Method - for every round. 
     */
    public void testUsingMethodRunner() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        
        while( count-- > 0 ) {
            Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
            mtd.setAccessible(true);
            new MethodRunner( mtd, this, new Object[] { new Integer(workload) } ).run();
        }
    }

    /** Create a new instance of MethodRunner - a class that will delegate
     * its task to passed Method - for every round, but looking up the Method
     * only once.  
     */
    public void testUsingMethodRunnerWithCachedMethod() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
        mtd.setAccessible(true);
        
        while( count-- > 0 ) {
            new MethodRunner( mtd, this, new Object[] { new Integer(workload) } ).run();
        }
    }

    /** Create a new instance of MethodRunner - a class that will delegate
     * its task to passed Method - for every round, but looking up the Method
     * and creating argument parring array only once.  
     */
    public void testUsingMethodRunnerWithCachedParams() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
        mtd.setAccessible(true);
        Object[] params = new Object[] { new Integer(workload) };
        
        while( count-- > 0 ) {
            new MethodRunner( mtd, this, params ).run();
        }
    }

    /** Create just one instance of MethodRunner - a class that will delegate
     * its task to passed Method - and call this single instance for every
     * round.  
     */
    public void testUsingCachedMethodRunner() throws Exception {
        int count = getIterationCount();
        int workload = ((Integer)getArgument()).intValue();
        Method mtd = getClass().getDeclaredMethod( "workerMethod", new Class[] { Integer.TYPE } );
        mtd.setAccessible(true);
        Object[] params = new Object[] { new Integer(workload) };
        MethodRunner mr = new MethodRunner( mtd, this, params );

        while( count-- > 0 ) {
            mr.run();
        }
    }

    public static void main( String[] args ) {
	junit.textui.TestRunner.run( new junit.framework.TestSuite( RunnableMethod.class ) );
    }    
    
    /* ----------------------------- */
    private void workerMethod( int workload ) {
        for( int i=0; i < workload; i++ );
    }

    private static final class MethodRunner implements Runnable {
        private Method mtd;
        private Object obj;
        private Object[] args;
        
        public MethodRunner( Method mtd, Object obj, Object[] args ) {
            this.mtd = mtd;
            this.obj = obj;
            this.args = args;
        }
        
        public void run() {
            try {
                mtd.invoke( obj, args );
            } catch( Throwable t ) {
                t.printStackTrace();
                junit.framework.Assert.fail( "Exception: " + t.getMessage() );
            }
        }
    }
}