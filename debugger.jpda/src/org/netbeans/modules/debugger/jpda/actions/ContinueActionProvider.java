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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.request.EventRequestManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.util.Operator;
import org.openide.util.RequestProcessor;


/**
 *
 * @author  Jan Jancura
 */
public class ContinueActionProvider extends JPDADebuggerActionProvider 
implements Runnable {
    
    private boolean j2meDebugger = false;
    
    private volatile boolean doingAction;
    
    
    public ContinueActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        Map properties = (Map) contextProvider.lookupFirst (null, Map.class);
        if (properties != null)
            j2meDebugger = properties.containsKey ("J2ME_DEBUGGER");
        setProviderToDisableOnLazyAction(this);
        RequestProcessor.getDefault ().post (this, 200);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_CONTINUE);
    }
    
    public void doAction (Object action) {
        doingAction = true;
        doLazyAction(new Runnable() {
            public void run() {
                getDebuggerImpl ().resume ();
                doingAction = false;
            }
        });
    }
    
    protected void checkEnabled (int debuggerState) {
        if (j2meDebugger) {
            setEnabled (
                ActionsManager.ACTION_CONTINUE,
                debuggerState == JPDADebugger.STATE_STOPPED
            );
            return;
        }
        VirtualMachine vm = getDebuggerImpl ().getVirtualMachine ();
        if (vm == null) {
            setEnabled (
                ActionsManager.ACTION_PAUSE,
                false
            );
            return;
        }
        try {
            List l = vm.allThreads ();
            int i, k = l.size ();
            for (i = 0; i < k; i++) {
                ThreadReference tr = (ThreadReference) l.get (i);
                if (tr.isSuspended ()) {
                    setEnabled (
                        ActionsManager.ACTION_CONTINUE,
                        true
                    );
                    return;
                }
            }
        } catch (VMDisconnectedException ex) {
        }
        setEnabled (
            ActionsManager.ACTION_CONTINUE,
            false
        );
    }
    
    public void run () {
        if (getDebuggerImpl ().getState () == JPDADebugger.STATE_DISCONNECTED)
            return;
        if (!doingAction) {
            checkEnabled (getDebuggerImpl ().getState ());
        }
        RequestProcessor.getDefault ().post (this, 200);
    }
}
