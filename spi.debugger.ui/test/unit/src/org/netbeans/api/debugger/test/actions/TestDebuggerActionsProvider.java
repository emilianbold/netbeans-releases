/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.debugger.test.actions;

import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.DebuggerManager;
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
    private LookupProvider  lookupProvider;
    private Set             supportedActions;

    public TestDebuggerActionsProvider(LookupProvider lookupProvider) {
        debuggerImpl = (TestDebugger) lookupProvider.lookupFirst(TestDebugger.class);
        this.lookupProvider = lookupProvider;
        supportedActions = new HashSet();
        supportedActions.add(DebuggerManager.ACTION_CONTINUE);
        supportedActions.add(DebuggerManager.ACTION_FIX);
        supportedActions.add(DebuggerManager.ACTION_MAKE_CALLEE_CURRENT);
        supportedActions.add(DebuggerManager.ACTION_MAKE_CALLER_CURRENT);
        supportedActions.add(DebuggerManager.ACTION_PAUSE);
        supportedActions.add(DebuggerManager.ACTION_POP_TOPMOST_CALL);
        supportedActions.add(DebuggerManager.ACTION_RESTART);
        supportedActions.add(DebuggerManager.ACTION_RUN_INTO_METHOD);
        supportedActions.add(DebuggerManager.ACTION_RUN_TO_CURSOR);
        supportedActions.add(DebuggerManager.ACTION_STEP_INTO);
        supportedActions.add(DebuggerManager.ACTION_STEP_OUT);
        supportedActions.add(DebuggerManager.ACTION_STEP_OVER);
        supportedActions.add(DebuggerManager.ACTION_TOGGLE_BREAKPOINT);
    }

    public Set getActions () {
        return supportedActions;
    }

    public void doAction (Object action) {
        if (debuggerImpl == null) return;
        final TestDICookie cookie = (TestDICookie) lookupProvider.lookupFirst(TestDICookie.class);
        cookie.addInfo(action);
    }

    public boolean isEnabled (Object action) {
        return true;
    }

    public void addActionsProviderListener (ActionsProviderListener l) {}
    public void removeActionsProviderListener (ActionsProviderListener l) {}
}
