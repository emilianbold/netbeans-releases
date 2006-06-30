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

package org.netbeans.api.debugger.test.actions;

import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.test.TestDebugger;
import org.netbeans.api.debugger.test.TestDICookie;

import java.util.*;

/**
 * Provides all debugging actions and records when they are performed.
 *
 * @author Maros Sandor
 */
public class TestDebuggerActionsProvider extends ActionsProvider {

    private TestDebugger    debuggerImpl;
    private ContextProvider  lookupProvider;
    private Set             supportedActions;

    public TestDebuggerActionsProvider(ContextProvider lookupProvider) {
        debuggerImpl = (TestDebugger) lookupProvider.lookupFirst(null, TestDebugger.class);
        this.lookupProvider = lookupProvider;
        supportedActions = new HashSet();
        supportedActions.add(ActionsManager.ACTION_CONTINUE);
        supportedActions.add(ActionsManager.ACTION_FIX);
        supportedActions.add(ActionsManager.ACTION_MAKE_CALLEE_CURRENT);
        supportedActions.add(ActionsManager.ACTION_MAKE_CALLER_CURRENT);
        supportedActions.add(ActionsManager.ACTION_PAUSE);
        supportedActions.add(ActionsManager.ACTION_POP_TOPMOST_CALL);
        supportedActions.add(ActionsManager.ACTION_RESTART);
        supportedActions.add(ActionsManager.ACTION_RUN_INTO_METHOD);
        supportedActions.add(ActionsManager.ACTION_RUN_TO_CURSOR);
        supportedActions.add(ActionsManager.ACTION_STEP_INTO);
        supportedActions.add(ActionsManager.ACTION_STEP_OUT);
        supportedActions.add(ActionsManager.ACTION_STEP_OVER);
        supportedActions.add(ActionsManager.ACTION_TOGGLE_BREAKPOINT);
    }

    public Set getActions () {
        return supportedActions;
    }

    public void doAction (Object action) {
        if (debuggerImpl == null) return;
        final TestDICookie cookie = (TestDICookie) lookupProvider.lookupFirst(null, TestDICookie.class);
        cookie.addInfo(action);
    }

    public boolean isEnabled (Object action) {
        return true;
    }

    public void addActionsProviderListener (ActionsProviderListener l) {}
    public void removeActionsProviderListener (ActionsProviderListener l) {}
}
