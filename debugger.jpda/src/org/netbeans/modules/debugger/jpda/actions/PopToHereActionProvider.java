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
public class PopToHereActionProvider extends JPDADebuggerActionProvider {
    
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
        runAction();
    }
    
    public void postAction(Object action, final Runnable actionPerformedNotifier) {
        doLazyAction(new Runnable() {
            public void run() {
                try {
                    runAction();
                } finally {
                    actionPerformedNotifier.run();
                }
            }
        });
    }
    
    public void runAction() {
        try {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            t.getCallStack (0, 1) [0].popFrame ();
        } catch (AbsentInformationException ex) {
        }
    }
    
    protected void checkEnabled (int debuggerState) {
        if (!getDebuggerImpl().canPopFrames()) {
            setEnabled (
                ActionsManager.ACTION_POP_TOPMOST_CALL,
                false
            );
            return;
        }
        JPDAThread t;
        synchronized (getDebuggerImpl().LOCK) {
            if (debuggerState == getDebuggerImpl ().STATE_STOPPED) {
		t = getDebuggerImpl ().getCurrentThread ();
            } else {
                t = null;
            }
        }
        if (t == null) {
            setEnabled (
                ActionsManager.ACTION_POP_TOPMOST_CALL,
                false
            );
        } else {
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
        }
    }
}
