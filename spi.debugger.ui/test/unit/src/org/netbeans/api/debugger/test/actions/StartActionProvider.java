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

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.test.TestDebugger;
import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;


/**
 * Provider for the Start action in the test debugger.
 *
 * @author Maros Sandor
*/
public class StartActionProvider extends ActionsProvider {

    private TestDebugger    debuggerImpl;
    private LookupProvider  lookupProvider;
    
    
    public StartActionProvider (LookupProvider lookupProvider) {
        debuggerImpl = (TestDebugger) lookupProvider.lookupFirst(TestDebugger.class);
        this.lookupProvider = lookupProvider;
    }
    
    public Set getActions () {
        return Collections.singleton (DebuggerManager.ACTION_START);
    }
    
    public void doAction (Object action) {
        if (debuggerImpl == null) return;
        final TestDICookie cookie = (TestDICookie) lookupProvider.lookupFirst(TestDICookie.class);
        cookie.addInfo("start");
    }

    public boolean isEnabled (Object action) {
        return true;
    }

    public void addActionsProviderListener (ActionsProviderListener l) {}
    public void removeActionsProviderListener (ActionsProviderListener l) {}
}
