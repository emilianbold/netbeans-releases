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

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.DebuggerInfo;
import org.netbeans.api.debugger.test.TestDICookie;
import org.netbeans.api.debugger.test.TestDebugger;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;


/**
* Provider for the Kill action in the test debugger.
*
* @author Maros Sandor
*/
public class KillActionProvider extends ActionsProvider {

    private ContextProvider lookupProvider;
    private TestDebugger debugger;

    public KillActionProvider (ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
        debugger = (TestDebugger) lookupProvider.lookupFirst
            (null, TestDebugger.class);
    }

    public boolean isEnabled(Object action) {
        return true;
    }

    public void addActionsProviderListener(ActionsProviderListener l) {}
    public void removeActionsProviderListener(ActionsProviderListener l) {}

    public Set getActions() {
        return Collections.singleton (ActionsManager.ACTION_KILL);
    }
        
    public void doAction (Object action) {
        debugger.finish();
        DebuggerInfo di = (DebuggerInfo) lookupProvider.lookupFirst
            (null, DebuggerInfo.class);
        TestDICookie tic = (TestDICookie) di.lookupFirst(null, TestDICookie.class);
        tic.addInfo(ActionsManager.ACTION_KILL);
    }
}
