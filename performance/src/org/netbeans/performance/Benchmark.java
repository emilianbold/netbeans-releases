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
import junit.framework.Assert;
import junit.framework.AssertionFailedError;

/**
 *
 * @author  Petr Nejedly
 * @version 0.9
 */
public class Benchmark extends Assert implements Test {
    
    private String name;
    private String className;
    private static final Object[] emptyObjectArray = new Object[0];
    private static final Class[] emptyClassArray = new Class[0];
    private boolean realRun = false;
    
    private int iterations;
    private Object argument;
    
    private Object[] arguments;
    
    /** Creates new Benchmark without arguments for given test method
     * @param name the name fo the testing method
     */    
    public Benchmark( String name ) {
        this( name, new Object[] { new Object() {
            public String toString() {
                return "";
            }
        }} );
    }

    /** Creates new Benchmark for given test method with given set of arguments
     * @param name the name fo the testing method
     * @param args the array of objects describing arguments to testing method
     */    
    public Benchmark( String name, Object[] args ) {
        this.name = name;
        arguments = args; // should we clone it?
	
	String fullName = getClass().getName();
	int idx = fullName.lastIndexOf( "." );
	className = (idx >= 0) ? fullName.substring( idx+1 ) : fullName;
    }
    
    // things to override by the implementation of a particular Benchmark

    /** This method is called before the actual test method to allow
     * the benchmark to prepare accordingly to informations available
     * through {@link #getIterationCount}, {@link #getArgument} and {@link #getName}.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void setUp() throws Exception {
    }

    /** This method is called after every finished test method.
     * It is intended to be used to free all the resources allocated
     * during {@link #setUp} or the test itself.
     * This method can use assertions to signal failure of the test.
     * @throws Exception This method can throw any exception which is treated as a error in the testing code
     * or testing enviroment.
     */
    protected void tearDown() throws Exception {
    }
    
    /** This method can be overriden by a benchmark that can be resource
     * intensive when set up for more iterations to limit the number
     * of iterations.
     * @return the maximal iteration count the benchmark is able to handle
     *  without loss of precision due swapping, gc()ing and so on.
     * Its return value can depend on values returned by {@link #getName}
     * and {@link #getParameter}.
     */
    protected int getMaxIterationCount() {
        return 50000;
    }
    
    // things to call from a particular benchmark

    /** How many iterations to perform.
     * @return the iteration count the benchmark should perform or
     * the {@link #setUp} should prepare the benchmark for.
     */
    protected final int getIterationCount() {
        return iterations;
    }
    
    /** Which test is to be performed.
     * @return the name of the test method that will be performed.
     * Benchmark writers could use this information during the
     * {@link #setUp} to prepare different conditions for different tests.
     */
    protected final String getName() {
        return name;
    }
    
    /** For which argument the test runs
     * @return the object describing the argument.
     * It will be one of the objects specified in {@link #Benchmark(String,Object[])}
     * constructor or {@link #setArgumentArray(Object[])}
     * It will be <CODE>null</CODE> for tests that didn't specify any argument.
     */    
    protected final Object getArgument() {
        return argument;
    }
    
    /** Sets the set of arguments for this test.
     * @param args the array of objects describing arguments to testing method
     */
    protected final void setArgumentArray( Object[] args ) {
        arguments = args; //do clone ??
    }
    
    
    // the rest is implemetation
    /** How many tests should this Test perform
     * @return the number of tests this Test should perform during
     * {@link #run} method
     */
    public int countTestCases() {
        return 1;
    }

    public final void run( TestResult result ) {
        try {
            Method testMethod = getClass().getMethod( name, emptyClassArray );
            for( int a=0; a<arguments.length; a++ ) {
                
                result.startTest( this );
                
                try {
                    doOneArgument( testMethod, arguments[a] );
                } catch( AssertionFailedError err ) {
                    result.addFailure( this, err );
                } catch( ThreadDeath td ) {
                    throw td;                  // need to propagate this
                } catch( Throwable t ) {
                    result.addError( this, t );
                }
                
                result.endTest( this );
            }
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    
    private void doOneArgument( Method testMethod, Object argument ) throws Exception {
            setArgument( argument );
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
            StringBuffer sb = new StringBuffer(100);
            argument2String(argument, sb);
            String argString = sb.toString();
            if( argString.length() > 0 ) argString = "@" + argString;
            System.out.println( className + ':'+ name + argString +
                ": iter=" + iters + 
                ", min=" + 1000000f*realMin + 
                ", avg=" + 1000000f*avgTime +
                ", max=" + 1000000f*realMax );
    }
    
    /** Handles arrays */
    private static void argument2String(Object argument, StringBuffer sb) {
        if (argument instanceof Object[]) {
            Object[] arg = (Object[]) argument;
            sb.append('[');
            for (int i = 0; i < arg.length - 1; i++) {
                argument2String(arg[i], sb);
                sb.append(',').append(' ');
            }
            argument2String(arg[arg.length - 1], sb);
            sb.append(']');
        } else {
            sb.append(argument.toString());
        }
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

    private void setIterationCount( int count ) {
        iterations = count; 
    }

    private void setArgument( Object arg ) {
        argument = arg;
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
