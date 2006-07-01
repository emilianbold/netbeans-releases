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
package org.openide.actions;

import org.openide.util.Lookup;
import org.openide.util.actions.SystemAction;

import java.awt.event.ActionEvent;

import java.beans.*;

import javax.swing.Action;


/** Collects access methods to implementation depended functionality
* for actions package.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public abstract class ActionManager extends Object {
    /** name of property that is fired when set of context actions
    * changes.
    */
    public static final String PROP_CONTEXT_ACTIONS = "contextActions"; // NOI18N

    /** Utility field used by event firing mechanism. */
    private PropertyChangeSupport supp = null;

    /**
     * Get the default action manager from lookup.
     * @return some default instance
     * @since 4.2
     */
    public static ActionManager getDefault() {
        ActionManager am = (ActionManager) Lookup.getDefault().lookup(ActionManager.class);

        if (am == null) {
            am = new Trivial();
        }

        return am;
    }

    /** Get all registered actions that should be displayed
    * by tools action.
    * Can contain <code>null</code>s that will be replaced by separators.
    *
    * @return array of actions
    */
    public abstract SystemAction[] getContextActions();

    /** Invokes action in a RequestPrecessor dedicated to performing
     * actions.
     * Nonabstract since 4.11.
     * @deprecated Just use {@link java.awt.event.ActionListener#actionPerformed} directly instead. Since 4.11.
     */
    public void invokeAction(Action a, ActionEvent e) {
        a.actionPerformed(e);
    }

    /** Registers PropertyChangeListener to receive events.
     * @param listener The listener to register.
     */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        if (supp == null) {
            supp = new PropertyChangeSupport(this);
        }

        supp.addPropertyChangeListener(listener);
    }

    /** Removes PropertyChangeListener from the list of listeners.
     * @param listener The listener to remove.
     */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (supp != null) {
            supp.removePropertyChangeListener(listener);
        }
    }

    /** Notifies all registered listeners about the event.
     * @param name property name
     * @param o old value
     * @param n new value
     */
    protected final void firePropertyChange(String name, Object o, Object n) {
        if (supp != null) {
            supp.firePropertyChange(name, o, n);
        }
    }

    /**
     * Trivial impl.
     * @see "#32092"
     */
    private static final class Trivial extends ActionManager {
        public SystemAction[] getContextActions() {
            return new SystemAction[0];
        }
    }
}
