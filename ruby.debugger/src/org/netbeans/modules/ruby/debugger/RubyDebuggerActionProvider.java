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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.ruby.debugger.breakpoints.RubyBreakpoint;
import org.netbeans.spi.debugger.ActionsProviderSupport;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerEngineProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.rubyforge.debugcommons.RubyDebugEventListener;
import org.rubyforge.debugcommons.RubyDebugEvent;

/**
 * @author Martin Krauskopf
 */
public final class RubyDebuggerActionProvider extends ActionsProviderSupport implements RubyDebugEventListener {
    
    private static final Set<Object> ACTIONS;
    
    static {
        Set<Object> s = new HashSet<Object>();
        s.add(ActionsManager.ACTION_KILL);
        s.add(ActionsManager.ACTION_CONTINUE);
        s.add(ActionsManager.ACTION_STEP_INTO);
        s.add(ActionsManager.ACTION_STEP_OVER);
        s.add(ActionsManager.ACTION_STEP_OUT);
        s.add(ActionsManager.ACTION_RUN_TO_CURSOR);
        ACTIONS = Collections.unmodifiableSet(s);
    }
    
    private Semaphore frontEndSemaphore = new Semaphore(1, true);
    private Semaphore backEndSemaphore = new Semaphore(1, true);
    
    private ContextProviderWrapper contextProvider;
    private RubyDebuggerEngineProvider engineProvider;
    private final RubySession rubySession;
    private boolean terminated;
    
    public RubyDebuggerActionProvider(final ContextProvider contextProvider) {
        engineProvider = (RubyDebuggerEngineProvider) contextProvider.
                lookupFirst(null, DebuggerEngineProvider.class);
        this.contextProvider = new ContextProviderWrapper(contextProvider);
        rubySession = this.contextProvider.getRubySession();
        frontEndSemaphore = new Semaphore(1, true);
        backEndSemaphore = new Semaphore(1, true);
        
        // backends comes first
        boolean acquired = frontEndSemaphore.tryAcquire();
        assert acquired;
        
        // init actions
        for (Object action : ACTIONS) {
            setEnabled(action, true);
        }
    }
    
    @Override
    public Set<Object> getActions() {
        return ACTIONS;
    }
    
    @Override
    public void doAction(final Object action) {
        Util.finest("Performing \"" + action + '"');
        if (action == ActionsManager.ACTION_KILL) {
            finish(true);
            return;
        }
        if (frontEndSemaphore.getQueueLength() > 10) {
            Util.info("Too much pending events (> 10). Action \"" + action + "\" is rejected."); // NOI18N
            return;
        }
        try {
            frontEndSemaphore.acquire();
        } catch (InterruptedException e) {
            Util.severe(e);
            return;
        }
        if (terminated) {
            Util.info("Flushing cached actions: " + action + ", process is terminated.");
            frontEndSemaphore.release();
            return; // ignore cached actions
        }
        if (action == ActionsManager.ACTION_CONTINUE) {
            rubySession.resume();
            contextProvider.fireModelChanges();
        } else if (action == ActionsManager.ACTION_STEP_INTO) {
            rubySession.stepInto();
        } else if (action == ActionsManager.ACTION_RUN_TO_CURSOR) {
            rubySession.runToCursor();
        } else if (action == ActionsManager.ACTION_STEP_OUT) {
            rubySession.stepReturn();
        } else if (action == ActionsManager.ACTION_STEP_OVER) {
            rubySession.stepOver();
        }
        contextProvider.getSessionsModel().fireChanges();
        backEndSemaphore.release();
    }
    
    /**
     * Fed by backends.
     *
     * @param event backend event
     */
    public void onDebugEvent(final RubyDebugEvent event) {
        if (event.isTerminateType()) {
            finish(false);
            frontEndSemaphore.release();
            int beEvents = backEndSemaphore.getQueueLength();
            if (beEvents > 0) {
                Util.finest("Flushing " + beEvents + " backend events.");
                backEndSemaphore.release(beEvents);
            }
            return;
        }
        try {
            backEndSemaphore.acquire();
        } catch (InterruptedException e) {
            Util.severe(e);
            return;
        }
        if (terminated) {
            Util.info("Flushing pending event: " + event + ", process is terminated."); // NOI18N
            backEndSemaphore.release();
            return;
        }
        if (event.isSuspensionType()) {
            String absPath = rubySession.resolveAbsolutePath(event.getFilePath());
            if (absPath != null) {
                FileObject fo = FileUtil.toFileObject(new File(absPath));
                if (event.isStepping() || (fo != null && RubyBreakpoint.isBreakpointOnLine(fo, event.getLine()))) {
                    stopHere(event);
                } else {
                    event.getRubyThread().resume();
                    backEndSemaphore.release();
                    return;
                }
            }
        } else {
            assert false : "Unkown event type: " + event;
        }
        frontEndSemaphore.release();
    }
    
    /**
     * @param terminate whether termination of the underlaying process should be
     *        forced.
     */
    private void finish(boolean terminate) {
        Util.finest("Finishing session: " + rubySession.getName());
        if (terminated) {
            Util.warning("Finish is not supposed to be called when a process is already terminated");
            return;
        }
        rubySession.finish(this, terminate);
        engineProvider.getDestructor().killEngine();
        EditorUtil.unmarkCurrent();
        terminated = true;
    }
    
    private void stopHere(final RubyDebugEvent suspensionEvent) {
        rubySession.suspend(suspensionEvent.getRubyThread(), contextProvider);
    }
    
}
