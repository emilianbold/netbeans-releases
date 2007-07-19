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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import javax.swing.Timer;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.api.*;
import org.netbeans.modules.cnd.repository.spi.*;

/**
 * RepositoryListener implementation.
 * Watches implicit and explicit opening of units;
 * ensures that implicitly opened units are closed 
 * after the specified interval has passed
 * @author Vladimir Kvashin
 */
public class RepositoryListenerImpl implements RepositoryListener {
    
    /** Singleton's instance */
    private static final RepositoryListenerImpl instance = new RepositoryListenerImpl();
    
    /** Interval, in seconds, after which implicitely opened unit should be closed */
    private static final int IMPLICIT_CLOSE_INTERVAL = Integer.getInteger("cnd.implicit.close.interval", 10); // NOI18N
    
    /** A shutdown hook to guarantee that repository is shutted down */
    private static class RepositoryShutdownHook extends Thread {
	
	public RepositoryShutdownHook() {
	    setName("Repository Shutdown Hook Thread"); // NOI18N
	}
	
	public void run() {
	    RepositoryUtils.shutdown();
	}
    }
    
    /** 
     * A pair of (unit name, timer) 
     * used to track implicitely opened units
     */
    private class UnitTimer implements ActionListener {
	
	private String unitName;
	private Timer timer;
	
	public UnitTimer(String unitName, int interval) {
	    this.unitName = unitName;
	    timer = new Timer(interval, this);
	    timer.start();
	}

	public void actionPerformed(ActionEvent e) {
	    timeoutElapsed(unitName);
	}
	
	public void cancel() {
	    timer.stop();
	}
    }
    
    /** Access both unitTimers and explicitelyOpened only under this lock! */
    private Object lock = new String("Repository listener lock"); //NOI18N
    
    /** 
     * Implicitly opened units.
     * Access only under the lock!
     */
    private Map<String, UnitTimer> unitTimers = new HashMap<String, UnitTimer>();

    /** 
     * Explicitly opened units.
     * Access only under the lock!
     */
    private Set<String> explicitelyOpened = new HashSet<String>();
    
    private  RepositoryListenerImpl() {
	Runtime.getRuntime().addShutdownHook(new RepositoryShutdownHook());
    }
    
    /** Singleton's getter */
    public static RepositoryListenerImpl instance() {
	return instance;
    }
    
    /** RepositoryListener implementation */
    public boolean unitOpened(final String unitName) {
	if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: unitOpened %s\n", unitName);
	synchronized (lock) {
	    if( ! explicitelyOpened.contains(unitName) ) {
		if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: implicit open !!! %s\n", unitName);
		unitTimers.put(unitName, new UnitTimer(unitName, IMPLICIT_CLOSE_INTERVAL*1000));
	    }
	}
        return true;
    }

    /** RepositoryListener implementation */
    public void unitClosed(final String unitName) {
	if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: unitClosed %s\n", unitName);
	synchronized (lock) {
	    killTimer(unitName);
	    explicitelyOpened.remove(unitName);
	}
    }

    /** RepositoryListener implementation */
    public void anExceptionHappened(final String unitName, RepositoryException exc) {
        assert exc != null;
        if (exc.getCause() != null) {
            exc.getCause().printStackTrace(System.err);
        }
    }

    // NB: un-synchronized!
    private void killTimer(String unitName) {
	UnitTimer unitTimer = unitTimers.remove(unitName);
	if( unitTimer != null ) {
	    if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: killing timer for %s\n", unitName);
	    unitTimer.cancel();
	}
    }
    
    public void onExplicitOpen(String unitName) {
	if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: onExplicitOpen %s\n", unitName);
	synchronized (lock) {
	    killTimer(unitName);
	    explicitelyOpened.add(unitName);
	}
    }
    
    public void onExplicitClose(String unitName) {
	if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: onExplicitClose %s\n", unitName);
    }

    private void timeoutElapsed(String unitName) {
	if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: timeout elapsed for %s\n", unitName);
	synchronized (lock) {
	    UnitTimer unitTimer = unitTimers.remove(unitName);
	    if( unitTimer != null ) {
		if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: scheduling closure for %s\n", unitName);
		unitTimer.cancel();
		scheduleClosing(unitName, Collections.EMPTY_SET);
	    }
	}
    }
    
    private void scheduleClosing(final String unitName,  final Set<String> requiredUnits) {
	CsmModelAccessor.getModel().enqueue(new Runnable() {
	    public void run() {
		if( TraceFlags.TRACE_REPOSITORY_LISTENER ) System.err.printf("RepositoryListener: closing implicitely opened unit%s\n", unitName);
		RepositoryUtils.closeUnit(unitName, Collections.EMPTY_SET);
	    }
	}, "Closing implicitly opened project"); // NOI18N
    }
}
