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

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestResult;

/**
 *
 * @author  pn97942
 * @version 
 */
public class Benchmark implements Test {
    
    private String name;
    private static final Object[] emptyObjectArray = new Object[0];
    private static final Class[] emptyClassArray = new Class[0];
    private boolean realRun = false;
    
    private int iterations;
    private Object parameter = new Object(); // PENDING
    
    public Benchmark( String name ) {
        this.name = name;
    }
    
    // things to override by the implementation of a particular Benchmark
    
    /** This method is called before the actual test method to allow
     * the benchmark to prepare accordingly to informations available
     * through getIterationCount, getParameter and getName. 
     */
    protected void setUp() {
    }

    /** This method is called after every finished test method.
     * It is intended to be used to free all the resources allocated
     * during <CODE>setUp</CODE> or the test itself.
     */
    protected void tearDown() {
    }
    
    /** This method can be overriden by a benchmark that can be resource
     * intensive when set up for more iterations to limit the number
     * of iterations.
     * @return the maximal iteration count the benchmark is able to handle
     *  without loss of precision due swapping, gc()ing and so on.
     * Its return value can depend on values returned by <CODE>getName</CODE>
     * and <CODE>getParameter</CODE>.
     */
    protected int getMaxIterationCount() {
        return 50000;
    }
    
    // things to call from a particular benchmark

    /** How many iterations to perform.
     * @return the iteration count the benchmark should perform or
     * the <CODE>setUp()</CODE> should prepare the benchmark for.
     * Benchmark writers could use this information during
     */
    protected final int getIterationCount() {
        return iterations;
    }
    
    /** Which test is to be performed.
     * @return the name of the test method that will be performed.
     * Benchmark writers could use this information during the
     * <CODE>setUp</CODE> to prepare different conditions for different tests.
     */
    protected final String getName() {
        return name;
    }
    
    protected final Object getParameter() {
        return parameter;
    }
    
    
    // the rest is implemetation
    
    public int countTestCases() {
        return 1;
    }

    public final void run( TestResult result ) {
        try {
            Method testMethod = getClass().getMethod( name, emptyClassArray );
        
            // class loading and so on...
            realRun = false;
            doOneMeasurement( testMethod, 1 );
            
            // Do the adaptive measurement, the test have to take at least 200ms
            // to be taken representative enough
            long time = 0;
            int iters = 1;
            int maxIters = getMaxIterationCount();
            
            do {
                realRun = false;
                for( ;; ) {
                    time = doOneMeasurement( testMethod, iters );
                    if( time >= 200 ) break;
                    if( 2*iters > maxIters ) break; // fuse
                    iters *= 2;
                }
                
                if( 2*iters > maxIters ) break; // additional round won't help
                
                // A check it the calibrated iteration count is sufficient
                // when running with realRun == true
                realRun = true;
                time = doOneMeasurement( testMethod, iters );
            } while( time < 200 );
            
            // do the real measurement
            realRun = true;
            long min=Long.MAX_VALUE;
            long max=0;
            long sum=0;
            for( int run = 0; run < 3; run++ ) {
                time = doOneMeasurement( testMethod, iters );
                if( time < min ) min = time;
                if( time > max ) max = time;
                sum += time;
            }
            
            // compute time in [s] per iteration
            float realMin  = ((float)min) / 1000 / iters;
            float realMax  = ((float)max) / 1000 / iters;
            float avgTime  = ((float)sum) / 1000 / iters / 3;
            
            // PENDING - add real reporting stuff here
            System.out.println( name + ": iter=" + iters + 
                ", min=" + 1000000f*realMin + 
                ", avg=" + 1000000f*avgTime +
                ", max=" + 1000000f*realMax );
        
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    private void setIterationCount( int count ) {
        iterations = count; 
    }
    
    private long doOneMeasurement( Method testMethod, int iterations ) throws Exception {
        setIterationCount( iterations );
        setUp();
        cooling();

        long time = System.currentTimeMillis();
        testMethod.invoke( this, emptyObjectArray );
        time = System.currentTimeMillis() - time;

        tearDown();
        return time;
    }
    
    private void cooling() {
        if( !realRun ) return;
        System.gc();
        System.gc();
        try {
            Thread.sleep( 300 );
        } catch( InterruptedException exc ) {}
        System.gc();
        try {
            Thread.sleep( 2000 );
        } catch( InterruptedException exc ) {}
    }

}
