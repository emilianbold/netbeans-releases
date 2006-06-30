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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
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
        try {
            getDebuggerImpl ().resume ();
        } finally {
            doingAction = false;
        }
    }
    
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doingAction = true;
        doLazyAction(new Runnable() {
            public void run() {
                try {
                    getDebuggerImpl ().resume ();
                } finally {
                    try {
                        actionPerformedNotifier.run();
                    } finally {
                        doingAction = false;
                    }
                }
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
                try {
                    if (tr.isSuspended ()) {
                        setEnabled (
                            ActionsManager.ACTION_CONTINUE,
                            true
                        );
                        return;
                    }
                } catch (ObjectCollectedException ocex) {
                    // The thread just died - ignore
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
