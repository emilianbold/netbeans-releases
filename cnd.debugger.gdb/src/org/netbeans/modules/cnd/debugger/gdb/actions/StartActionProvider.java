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

package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.util.Collections;
import java.util.Set;

import org.openide.util.RequestProcessor;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;


/**
 *
 * @author gordonp
 */
public class StartActionProvider extends ActionsProvider {
    
    private GdbDebugger debuggerImpl;
    private ContextProvider lookupProvider;
    private static final boolean startVerbose =
                System.getProperty("netbeans.debugger.start") != null;

    /**
     * Creates a new instance of StartActionProvider
     */
    public StartActionProvider(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debuggerImpl = (GdbDebugger) lookupProvider.lookupFirst(null, GdbDebugger.class);
        
    }
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_START);
    }
    
    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public void doAction(Object action) {
        
        if (action instanceof String) {
            debuggerImpl.startDebugger();
        }
    }
    
    /**
     * Should return a state of given action.
     *
     * @param action action
     */
    public boolean isEnabled(Object action) {
        return true;
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
    public void postAction(final Object action, final Runnable actionPerformedNotifier) {
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
}
