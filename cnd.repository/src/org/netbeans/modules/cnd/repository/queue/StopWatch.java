/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.queue;

/**
 * A simple class for timing
 * Copy-pasted from org.netbeans.modules.cnd.modelimpl.debug.Diagnostic.StopWatch
 * TODO: move to a common utility package
 * @author Vladimir Kvashin
 */
public class StopWatch {
    
    private long time;
    private long lastStart;
    private boolean running;
    
    public StopWatch() {
	this(true);
    }
    
    public StopWatch(boolean start) {
	time = 0;
	if( start ) {
	    start();
	}
    }
    
    public void start() {
	running = true;
	lastStart = System.nanoTime();
    }
    
    public void stop() {
	running = false;
	time += System.nanoTime() - lastStart;
    }
    
    public void stopAndReport(String text) {
	stop();
	report(text);
    }
    
    public void report(String text) {
	System.err.println(' ' + text + ' ' + time + " ms");
    }
    
    public boolean isRunning() {
	return running;
    }
}
