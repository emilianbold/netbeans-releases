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
	t.start();
	t2.start();
    }

    protected int getMaxIterationCount() {
	return Integer.MAX_VALUE;
    }

    Thread t = new Thread() {
	public void run() {
	    try {
		Thread.sleep(1000000000);
	    } catch (InterruptedException e) {
	    }
	}
    };

    static class Thread2 extends Thread {
	int prio;
	
	public void run() {
	    try {
		Thread.sleep(1000000000);
	    } catch (InterruptedException e) {
	    }
	}
	
	public void setPrio(int p) {
	    if (prio == p) return;
	    setPriority(p);
	    prio = p;
	}
    }

    Thread2 t2 = new Thread2();
    

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

    /**
     */
    public void testSetPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setPriority(4+(count%3));
        }
    }

    /**
     */
    public void testSetFixedPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t.setPriority(5);
        }
    }

    /**
     */
    public void testSetSimilarPriority() throws Exception {
        int count = getIterationCount();

        while( count-- > 0 ) {
	    t2.setPrio(4+((count/100)%3));
        }
    }

    
    public static void main( String[] args ) {
	simpleRun( SetThreadName.class );
    }

}
