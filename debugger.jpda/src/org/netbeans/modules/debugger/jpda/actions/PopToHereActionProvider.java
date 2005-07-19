/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import java.util.Collections;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class PopToHereActionProvider extends JPDADebuggerActionProvider implements Runnable {
    
    private ContextProvider lookupProvider;

    
    public PopToHereActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebuggerImpl) lookupProvider.lookupFirst 
                (null, JPDADebugger.class) 
        );
        this.lookupProvider = lookupProvider;
        setProviderToDisableOnLazyAction(this);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_POP_TOPMOST_CALL);
    }

    public void doAction (Object action) {
        doLazyAction(this);
    }
    
    public void run() {
        try {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            t.getCallStack (0, 1) [0].popFrame ();
        } catch (AbsentInformationException ex) {
        }
    }
    
    protected void checkEnabled (int debuggerState) {
        synchronized (getDebuggerImpl().LOCK) {
            if (debuggerState == getDebuggerImpl ().STATE_STOPPED) {
                JPDAThread t = getDebuggerImpl ().getCurrentThread ();
                if (t == null) {
                    setEnabled (
                        ActionsManager.ACTION_POP_TOPMOST_CALL,
                        false
                    );
                    return;
                }
                synchronized (t) {
                    if (!t.isSuspended()) {
                        setEnabled (
                            ActionsManager.ACTION_POP_TOPMOST_CALL,
                            false
                        );
                    } else {
                        setEnabled (
                            ActionsManager.ACTION_POP_TOPMOST_CALL,
                            t.getStackDepth () > 1
                        );
                    }
                }
                return;
            }
            setEnabled (
                ActionsManager.ACTION_POP_TOPMOST_CALL,
                false
            );
        }
    }
}
