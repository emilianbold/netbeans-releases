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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class MakeCalleeCurrentActionProvider extends JPDADebuggerAction {

    private ContextProvider lookupProvider;
    
    
    public MakeCalleeCurrentActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebugger) lookupProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
        getDebuggerImpl ().addPropertyChangeListener 
            (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_MAKE_CALLEE_CURRENT);
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
                    ActionsManager.ACTION_MAKE_CALLEE_CURRENT,
                    i > 0
                );
                return;
            }
        }
        setEnabled (
            ActionsManager.ACTION_MAKE_CALLEE_CURRENT,
            false
        );
    }
}
