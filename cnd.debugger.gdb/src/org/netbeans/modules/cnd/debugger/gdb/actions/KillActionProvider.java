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

/*
 * KillActionProvider.java
 *
 * Created on July 20, 2006, 0:25 AM
 */

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openide.util.RequestProcessor;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
 * Termination of a debugging session.
 *
 */
public class KillActionProvider extends ActionsProvider {
    
    private ContextProvider lookupProvider;
    private GdbDebugger debuggerImpl;
    
    public KillActionProvider(ContextProvider lookupProvider) {
        debuggerImpl = (GdbDebugger) lookupProvider.lookupFirst
                (null, GdbDebugger.class);
        //super (debuggerImpl);
        this.lookupProvider = lookupProvider;
    }
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions() {
        return new HashSet(Arrays.asList(new Object[] {
            ActionsManager.ACTION_KILL
        }));
    }
    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public void doAction(Object action) {
        runAction(action);
    }
    
    /**
     * Runs the action. This method invokes the appropriate method in GdbDebugger
     * 
     * @param action an action which has been called
     */
    public void runAction(final Object action) {
        if (debuggerImpl != null) {
            synchronized (debuggerImpl.LOCK) {
                if (action == ActionsManager.ACTION_KILL) {
                    debuggerImpl.finish();
                    return;
                }
            }
        }
    }
    
    /**
     * Post the action and let it process asynchronously.
     * The default implementation just delegates to {@link #doAction}
     * in a separate thread and returns immediately.
     *
     * @param action The action to post
     * @param actionPerformedNotifier run this notifier after the action is
     *        done.
     * @since 1.5
     */
    public void postAction(final Object action,
            final Runnable actionPerformedNotifier) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                try {
                    doAction(action);
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    /**
     * Should return a state of given action.
     *
     * @param action action
     */
    public boolean isEnabled(Object action) {
        if (debuggerImpl != null) {
            synchronized (debuggerImpl.LOCK) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public void addActionsProviderListener(ActionsProviderListener l) {
        
    }
    
    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public void removeActionsProviderListener(ActionsProviderListener l) {
        
    }
    
    /* Not implemented yet.
    protected void checkEnabled (int debuggerState) {
        setEnabled (ActionsManager.ACTION_KILL, true);
    }
     **/
}
