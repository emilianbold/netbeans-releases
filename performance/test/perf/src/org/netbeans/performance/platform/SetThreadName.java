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
 * Benchmark measuring how long it takes to set the name of the Thread.
 * Yarda's concenr was to not slow down the request processor by
 * pooling threads and changing the thread's name frequently.
 *
 * @author  Petr Nejedly
 * @version 1.0
 */
public class SetThreadName extends Benchmark {

    public SetThreadName(String name) {
        super( name );
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }

    Thread t = new Thread();

    /**
     */
    public void testSetThreadName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setName("Thread #" + count);
        }
    }

    /**
     */
    public void testSetFixedName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setName("Thread #A");
	    t.setName("Thread #B");
        }
    }

    /**
     */
    public void testCreateName() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    String s = "Thread #" + count;
        }
    }
    
    public static void main( String[] args ) {
	simpleRun( SetThreadName.class );
    }

}
