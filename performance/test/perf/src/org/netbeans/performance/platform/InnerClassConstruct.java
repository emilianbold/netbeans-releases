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

import org.netbeans.performance.Benchmark;

/**
 * The Benchmark measuring the difference between using public and
 * private constructor of the private inner class.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class InnerClassConstruct extends Benchmark {

    public InnerClassConstruct(String name) {
        super( name );
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }

    /**
     * Pour into the call stack and then create an object.
     * Used as a reference to divide the time between recursive decline
     * and Exception creation.
     */
    public void testCreatePrivate() throws Exception {
        int count = getIterationCount();
    
        while( count-- > 0 ) {
	    new Priv();
        }
    }

    /**
     * Create an Exception deep in the call stack, filling its stack trace.
     */
    public void testCreatePublic() throws Exception {
        int count = getIterationCount();
    
        while( count-- > 0 ) {
            new Publ();
        }
    }

    public static void main( String[] args ) {
	simpleRun( InnerClassConstruct.class );
    }

    private final class Priv {}
    
    private final class Publ {
	public Publ() {}
    }

}
