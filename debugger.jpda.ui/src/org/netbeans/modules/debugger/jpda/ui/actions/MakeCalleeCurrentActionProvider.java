/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.util.Collections;
import java.util.Set;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.LookupProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class MakeCalleeCurrentActionProvider extends JPDADebuggerAction {
    
    private LookupProvider lookupProvider;
    
    
    public MakeCalleeCurrentActionProvider (LookupProvider lookupProvider) {
        super (
            (JPDADebugger) lookupProvider.lookupFirst 
                (JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
        getDebuggerImpl ().addPropertyChangeListener 
            (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public Set getActions () {
        return Collections.singleton (DebuggerManager.ACTION_MAKE_CALLEE_CURRENT);
    }

    public void doAction (Object action) {
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) return;
        int i = MakeCallerCurrentActionProvider.getCurrentCallStackFrameIndex 
            (getDebuggerImpl ());
        if (i == 0) return;
        MakeCallerCurrentActionProvider.setCurrentCallStackFrameIndex 
            (getDebuggerImpl (), --i, lookupProvider);
    }
    
    protected void checkEnabled (int debuggerState) {
        if (debuggerState == getDebuggerImpl ().STATE_STOPPED) {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t != null) {
                int i = MakeCallerCurrentActionProvider.getCurrentCallStackFrameIndex 
                    (getDebuggerImpl ());
                setEnabled (
                    DebuggerManager.ACTION_MAKE_CALLEE_CURRENT,
                    i > 0
                );
                return;
            }
        }
        setEnabled (
            DebuggerManager.ACTION_MAKE_CALLEE_CURRENT,
            false
        );
    }
}
