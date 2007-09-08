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
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.spi.debugger.ContextProvider;
import org.openide.util.RequestProcessor;

/**
 * Pop the current stack.
 *
 * @author gordonp
 */
public class PopToHereActionProvider extends GdbDebuggerActionProvider {
    
    /** Creates a new instance of PopToHereActionProvider */
    public PopToHereActionProvider(ContextProvider lookupProvider) {
        super(lookupProvider);
        getDebugger().addPropertyChangeListener(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    /**
     * Returns set of actions supported by this ActionsProvider.
     *
     * @return set of actions supported by this ActionsProvider
     */
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_POP_TOPMOST_CALL);
    }

    /**
     * Called when the action is called (action button is pressed).
     *
     * @param action an action which has been called
     */
    public void doAction(Object action) {
        if (getDebugger() != null) {
            synchronized (getDebugger().LOCK) {
                if (action == ActionsManager.ACTION_POP_TOPMOST_CALL) {
                    getDebugger().popTopmostCall();
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
            setEnabled(i.next(), debuggerState == getDebugger().STATE_STOPPED &&
                    getDebugger().getStackDepth() > 1);
        }
    }
    
}
