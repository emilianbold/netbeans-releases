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

package org.netbeans.modules.debugger.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;

import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;


/**
 *
 * @author   Jan Jancura
 */
public abstract class DebuggerAction extends AbstractAction {

    public DebuggerAction () {
        new Listener (this);
        setEnabled (getCurrentActionsManager ().isEnabled (getAction ()));
    }
    
    public abstract Object getAction ();
    
    public void actionPerformed (ActionEvent evt) {
        getCurrentActionsManager ().doAction (
                getAction ()
            );
    }
        
    private static ActionsManager getCurrentActionsManager () {
        return DebuggerManager.getDebuggerManager ().
            getCurrentEngine () == null ? 
            DebuggerManager.getDebuggerManager ().getActionsManager () :
            DebuggerManager.getDebuggerManager ().getCurrentEngine ().
                getActionsManager ();
    }

    
    // innerclasses ............................................................
    
    /**
     * Listens on DebuggerManager on PROP_CURRENT_ENGINE and on current engine
     * on PROP_ACTION_STATE and updates state of this action instance.
     */
    static class Listener extends DebuggerManagerAdapter 
    implements ActionsManagerListener {
        
        private ActionsManager  currentActionsManager;
        private WeakReference   ref;

        
        Listener (DebuggerAction da) {
            ref = new WeakReference (da);
            DebuggerManager.getDebuggerManager ().addDebuggerListener (
                DebuggerManager.PROP_CURRENT_ENGINE,
                this
            );
            updateCurrentActionsManager ();
        }
        
        public void propertyChange (PropertyChangeEvent evt) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            updateCurrentActionsManager ();
            final boolean en = currentActionsManager.isEnabled (da.getAction ());
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    da.setEnabled (en);
                }
            });
        }
        
        public void actionPerformed (Object action) {
        }
        public void actionStateChanged (
            final Object action, 
            final boolean enabled
        ) {
            final DebuggerAction da = getDebuggerAction ();
            if (da == null) return;
            if (action != da.getAction ()) return;
            SwingUtilities.invokeLater (new Runnable () {
                public void run () {
                    da.setEnabled (enabled);
                }
            });
        }
        
        private void updateCurrentActionsManager () {
            ActionsManager newActionsManager = getCurrentActionsManager ();
            if (currentActionsManager == newActionsManager) return;
            
            if (currentActionsManager != null)
                currentActionsManager.removeActionsManagerListener
                    (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            if (newActionsManager != null)
                newActionsManager.addActionsManagerListener
                    (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
            currentActionsManager = newActionsManager;
        }
        
        private DebuggerAction getDebuggerAction () {
            DebuggerAction da = (DebuggerAction) ref.get ();
            if (da == null) {
                DebuggerManager.getDebuggerManager ().removeDebuggerListener (
                    DebuggerManager.PROP_CURRENT_ENGINE,
                    this
                );
                if (currentActionsManager != null)
                    currentActionsManager.removeActionsManagerListener 
                        (ActionsManagerListener.PROP_ACTION_STATE_CHANGED, this);
                currentActionsManager = null;
                return null;
            }
            return da;
        }
    }
}

