/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.openide.util;

import java.awt.event.ActionEvent;

import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CallableSystemAction;

/** Allows Node action to get access to special tricks in CallableSystemAction.
 */
public abstract class ActionsBridge extends Object {
    /** thread to run actions in */
    private static RequestProcessor RP = new RequestProcessor("Module-Actions", Integer.MAX_VALUE); // NOI18N
    
    
    /** Invokes an action.
     */
    protected abstract void invokeAction (javax.swing.Action action, java.awt.event.ActionEvent ev);

    public static void doPerformAction(CallableSystemAction action, final org.netbeans.modules.openide.util.ActionsBridge.ActionRunnable r) {
        assert java.awt.EventQueue.isDispatchThread() : "Action " + action.getClass().getName() +
        " may not be invoked from the thread " + Thread.currentThread().getName() +
        ", only the event queue: http://www.netbeans.org/download/4_1/javadoc/OpenAPIs/apichanges.html#actions-event-thread";

        if (r.async && !r.needsToBeSynchronous()) {
            Runnable r2 = new Runnable() {
                    public void run() {
                        r.doRun();
                    }
                };

            RP.post(r2);
        } else {
            r.run();
        }
    }
    
    /** Special class that can be passed to invokeAction and delegates
     * to correct values
     */
    public static abstract class ActionRunnable implements javax.swing.Action {
        final ActionEvent ev;
        final org.openide.util.actions.SystemAction action;
        final boolean async;

        public ActionRunnable(ActionEvent ev, org.openide.util.actions.SystemAction action, boolean async) {
            this.ev = ev;
            this.action = action;
            this.async = async;
        }

        public final boolean needsToBeSynchronous() {
            return "waitFinished".equals(ev.getActionCommand()); // NOI18N
        }

        public final void doRun() {
            ActionsBridge bridge = (ActionsBridge)org.openide.util.Lookup.getDefault().lookup (ActionsBridge.class);
            if (bridge != null) {
                bridge.invokeAction (this, ev);
            } else {
                this.actionPerformed(ev);
            }
        }

        protected abstract void run();

        public final void actionPerformed(ActionEvent e) {
            run();
        }

        public final void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
            throw new java.lang.UnsupportedOperationException();
        }

        public final Object getValue(String key) {
            return action.getValue(key);
        }

        public final boolean isEnabled() {
            return action.isEnabled();
        }

        public final void putValue(String key, Object value) {
            throw new java.lang.UnsupportedOperationException();
        }

        public final void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
            throw new java.lang.UnsupportedOperationException();
        }

        public final void setEnabled(boolean b) {
            throw new java.lang.UnsupportedOperationException();
        }
    }
    // end of ActionRunnable
}
