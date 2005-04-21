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
package org.openide.actions;

import org.openide.awt.Actions;
import org.openide.awt.JInlineMenu;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

import java.awt.Component;
import java.awt.event.ActionEvent;

import java.beans.PropertyChangeListener;

import java.util.Collection;

import javax.swing.Action;
import javax.swing.JMenuItem;


/** Get properties of a node.
*
* @see NodeOperation#showProperties(Node[])
* @author   Ian Formanek, Jan Jancura
*/
public class PropertiesAction extends NodeAction {
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            NodeOperation.getDefault().showProperties(activatedNodes[0]);
        } else {
            NodeOperation.getDefault().showProperties(activatedNodes);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null) {
            return false;
        }

        // This is not quite as exact as checking if the *intersection* of their
        // properties is also nonempty, but it is pretty close.
        for (int i = 0; i < activatedNodes.length; i++) {
            if (activatedNodes[i].getPropertySets().length > 0) {
                return true;
            }
        }

        return false;
    }

    public JMenuItem getPopupPresenter() {
        JMenuItem prop = new Actions.MenuItem(this, false);

        CustomizeAction customizeAction = (CustomizeAction) SystemAction.get(CustomizeAction.class);

        if (customizeAction.isEnabled()) {
            JInlineMenu mi = new JInlineMenu();
            mi.setMenuItems(new JMenuItem[] { new Actions.MenuItem(customizeAction, false), prop });

            return mi;
        } else {
            return prop;
        }
    }

    public String getName() {
        return NbBundle.getMessage(PropertiesAction.class, "Properties");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PropertiesAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/properties.gif"; // NOI18N
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new DelegateAction(this, actionContext);
    }

    /** Delegate action for clonned context. Used to provide a special
     * support for getPopupPresenter.
     */
    private static final class DelegateAction implements Action, Presenter.Menu, Presenter.Toolbar, Presenter.Popup {
        /** action to delegate to */
        private PropertiesAction delegate;

        /** lookup we try to work in */
        private Lookup lookup;

        public DelegateAction(PropertiesAction a, Lookup actionContext) {
            this.delegate = a;
            this.lookup = actionContext;
        }

        private Node[] nodes() {
            Collection c = lookup.lookup(new Lookup.Template(Node.class)).allInstances();

            return (Node[]) c.toArray(new Node[c.size()]);
        }

        /** Overrides superclass method, adds delegate description. */
        public String toString() {
            return super.toString() + "[delegate=" + delegate + "]"; // NOI18N
        }

        /** Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            delegate.performAction(nodes());
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            // ignore
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            // ignore
        }

        public void putValue(String key, Object o) {
        }

        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        public boolean isEnabled() {
            return delegate.enable(nodes());
        }

        public void setEnabled(boolean b) {
            assert false;
        }

        public JMenuItem getMenuPresenter() {
            return new Actions.MenuItem(this, true);
        }

        public JMenuItem getPopupPresenter() {
            JMenuItem prop = new Actions.MenuItem(this, false);

            Action customizeAction = (CustomizeAction) SystemAction.get(CustomizeAction.class);

            // Retrieve context sensitive action instance if possible.
            if ((lookup != null) && customizeAction instanceof ContextAwareAction) {
                customizeAction = ((ContextAwareAction) customizeAction).createContextAwareInstance(lookup);
            }

            if (customizeAction.isEnabled()) {
                JInlineMenu mi = new JInlineMenu();
                mi.setMenuItems(new JMenuItem[] { new Actions.MenuItem(customizeAction, false), prop });

                return mi;
            } else {
                return prop;
            }
        }

        public Component getToolbarPresenter() {
            return new Actions.ToolbarButton(this);
        }
    }
     // end of DelegateAction
}
