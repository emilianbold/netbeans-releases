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

import com.sun.jdi.ThreadReference;

import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;


import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.spi.viewmodel.NoInformationException;

import org.netbeans.modules.debugger.jpda.ui.SourcePath;


/**
* Representation of a debugging session.
*
* @author   Jan Jancura
*/
public class MakeCallerCurrentActionProvider extends JPDADebuggerAction {
    
    private ContextProvider lookupProvider;

    
    public MakeCallerCurrentActionProvider (ContextProvider lookupProvider) {
        super (
            (JPDADebugger) lookupProvider.lookupFirst 
                (null, JPDADebugger.class)
        );
        this.lookupProvider = lookupProvider;
        getDebuggerImpl ().addPropertyChangeListener 
            (JPDADebugger.PROP_CURRENT_CALL_STACK_FRAME, this);
    }
    
    public Set getActions () {
        return Collections.singleton (ActionsManager.ACTION_MAKE_CALLER_CURRENT);
    }

    public void doAction (Object action) {
        JPDAThread t = getDebuggerImpl ().getCurrentThread ();
        if (t == null) return;
        int i = getCurrentCallStackFrameIndex (getDebuggerImpl ());
        if (i >= (t.getStackDepth () - 1)) return;
        setCurrentCallStackFrameIndex (getDebuggerImpl (), ++i, lookupProvider);
    }
    
    protected void checkEnabled (int debuggerState) {
        if (debuggerState == getDebuggerImpl ().STATE_STOPPED) {
            JPDAThread t = getDebuggerImpl ().getCurrentThread ();
            if (t != null) {
                int i = getCurrentCallStackFrameIndex (getDebuggerImpl ());
                setEnabled (
                    ActionsManager.ACTION_MAKE_CALLER_CURRENT,
                    i < (t.getStackDepth () - 1)
                );
                return;
            }
        }
        setEnabled (
            ActionsManager.ACTION_MAKE_CALLER_CURRENT,
            false
        );
    }
    
    static int getCurrentCallStackFrameIndex (JPDADebugger debuggerImpl) {
        try {
            JPDAThread t = debuggerImpl.getCurrentThread ();
            if (t == null) return -1;
            CallStackFrame csf = debuggerImpl.getCurrentCallStackFrame ();
            if (csf == null) return -1;
            CallStackFrame s[] = t.getCallStack ();
            int i, k = s.length;
            for (i = 0; i < k; i++)
                if (csf.equals (s [i])) return i;
        } catch (NoInformationException e) {
        }
        return -1;
    }
    
    static void setCurrentCallStackFrameIndex (
        JPDADebugger debuggerImpl,
        int index,
        final ContextProvider lookupProvider
    ) {
        try {
            JPDAThread t = debuggerImpl.getCurrentThread ();
            if (t == null) return;
            if (t.getStackDepth () <= index) return;
            final CallStackFrame csf = t.getCallStack (index, index + 1) [0];
            csf.makeCurrent ();
        } catch (NoInformationException e) {
        }
    }
}
