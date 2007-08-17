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
 * StepActionProvider.java
 *
 * Created on July 11, 2006, 2:25 PM
 */
package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.openide.util.RequestProcessor;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;

import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;

/**
 * Implements non visual part of stepping through code in gdb debugger.
 * It supports standard debugging actions Continue, Step Into, Step Over, 
 * Step Out, and RunToCursor (the last two are not implemented yet). 
 * 
 */
public class StepActionProvider extends GdbDebuggerActionProvider {
    
    /** 
     * Creates a new instance of StepActionProvider
     *
     * @param lookupProvider a context provider
     */
    public StepActionProvider(ContextProvider lookupProvider) {
        super(lookupProvider);
    }
    
    // ActionProviderSupport ...................................................
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions() {
        return new HashSet (Arrays.asList (new Object[] {
            ActionsManager.ACTION_STEP_INTO,
            ActionsManager.ACTION_STEP_OUT,
            ActionsManager.ACTION_STEP_OVER,
            ActionsManager.ACTION_CONTINUE
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
        if (getDebuggerImpl() != null) {
            synchronized (getDebuggerImpl().LOCK) {
                if (action == ActionsManager.ACTION_STEP_INTO) {
                    getDebuggerImpl().stepInto();
                    return;
                }
                if (action == ActionsManager.ACTION_STEP_OUT) {
                    getDebuggerImpl().stepOut();
                    return;
                }
                if (action == ActionsManager.ACTION_STEP_OVER) {
                    getDebuggerImpl().stepOver();
                    return;
                }
                if (action == ActionsManager.ACTION_CONTINUE) {
                    getDebuggerImpl().resume();
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
    protected void checkEnabled(String debuggerState) {
        Iterator i = getActions().iterator();
        while (i.hasNext()) {
            setEnabled(i.next(), debuggerState == getDebuggerImpl().STATE_STOPPED);
        }
    }
}
