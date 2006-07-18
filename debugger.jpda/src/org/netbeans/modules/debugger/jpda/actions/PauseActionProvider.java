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
* Representation of a debugging session.
*
* @author   Jan Jancura
* @author  Marian Petras
*/
public class PauseActionProvider extends JPDADebuggerActionProvider {
    
    private volatile boolean doingAction;
    
    
    public PauseActionProvider (ContextProvider contextProvider) {
        super (
            (JPDADebuggerImpl) contextProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        setProviderToDisableOnLazyAction(this);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_PAUSE);
    }

    public void doAction (Object action) {
        doingAction = true;
        try {
            ((JPDADebuggerImpl) getDebuggerImpl ()).suspend ();
        } finally {
            doingAction = false;
        }
    }
    
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doingAction = true;
        doLazyAction(new Runnable() {
            public void run() {
                try {
                    ((JPDADebuggerImpl) getDebuggerImpl ()).suspend ();
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
        setEnabled (
            ActionsManager.ACTION_PAUSE,
            debuggerState == JPDADebugger.STATE_RUNNING
        );
    }
    
}
