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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.ui.Evaluator;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Invokes the expression evaluator GUI
 *
 * @author Martin Entlicher
 */
public class EvaluateAction extends AbstractAction implements PropertyChangeListener,
                                                              Runnable {
    
    private EnableListener listener;
    private JPDADebugger lastDebugger;

    public EvaluateAction () {
        listener = new EnableListener (this);
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                listener);
        putValue (
            Action.NAME, 
            NbBundle.getMessage(EvaluateAction.class, "CTL_Evaluate") // NOI18N
        );
        checkEnabled();
    }
    
    private synchronized boolean canBeEnabled() {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return false;
        JPDADebugger debugger = (JPDADebugger) de.lookupFirst(null, JPDADebugger.class);
        if (lastDebugger != null && debugger != lastDebugger) {
            lastDebugger.removePropertyChangeListener(
                    JPDADebugger.PROP_CURRENT_THREAD,
                    this);
            lastDebugger = null;
        }
        if (debugger != null) {
            lastDebugger = debugger;
            debugger.addPropertyChangeListener(
                    JPDADebugger.PROP_CURRENT_THREAD,
                    this);
            return (debugger.getCurrentThread() != null);
        } else {
            return false;
        }
    }
    
    private void checkEnabled() {
        SwingUtilities.invokeLater(this);
    }
    
    public void run() {
        setEnabled(canBeEnabled());
    }
    
    public void actionPerformed (ActionEvent evt) {
        DebuggerEngine de = DebuggerManager.getDebuggerManager().getCurrentEngine();
        if (de == null) return ;
        JPDADebugger debugger = (JPDADebugger) de.lookupFirst(null, JPDADebugger.class);
        if (debugger == null) return ;
        Evaluator.open(debugger);
    }
    
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                synchronized (this) {
                    if (lastDebugger != null) {
                        setEnabled(lastDebugger.getCurrentThread() != null);
                    }
                }
            }
        });
    }
    
    protected void finalize() throws Throwable {
        DebuggerManager.getDebuggerManager().removeDebuggerListener(
                DebuggerManager.PROP_CURRENT_ENGINE,
                listener);
    }

        
    private static class EnableListener extends DebuggerManagerAdapter {
        
        private Reference actionRef;
        
        public EnableListener(EvaluateAction action) {
            actionRef = new WeakReference(action);
        }
        
        public void propertyChange(PropertyChangeEvent evt) {
            EvaluateAction action = (EvaluateAction) actionRef.get();
            if (action != null) {
                action.checkEnabled();
            }
        }
        
    }

}
