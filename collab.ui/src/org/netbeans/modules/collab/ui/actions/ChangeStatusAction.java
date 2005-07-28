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
package org.netbeans.modules.collab.ui.actions;

import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.*;

import java.awt.event.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;


/**
 *
 *
 * @author Todd Fast, todd.fast@sun.com
 */
public class ChangeStatusAction extends NodeAction implements Presenter.Popup {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    public static final SystemAction[] GROUPED_ACTIONS = new SystemAction[] {
            SystemAction.get(StatusOnlineAction.class), SystemAction.get(StatusBusyAction.class),
            SystemAction.get(StatusAwayAction.class), SystemAction.get(StatusInvisibleAction.class),
        };

    /**
     *
     *
     */
    public String getName() {
        return NbBundle.getMessage(ChangeStatusAction.class, "LBL_ChangeStatusAction_Name"); // NOI18N
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     *
     *
     */
    public JMenuItem getPopupPresenter() {
        return new LazyMenu();
    }

    /**
     *
     *
     */
    public boolean isEnabled() {
        return true;
    }

    /**
     *
     *
     */
    protected boolean enable(Node[] node) {
        // Will never be called
        return true;
    }

    /**
     *
     *
     */
    public void actionPerformed(ActionEvent event) {
        assert false : "Should never be called: " + event; // NOI18N
    }

    /**
     *
     *
     */
    protected void performAction(org.openide.nodes.Node[] node) {
        // Do nothing; will never be called
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Avoids constructing submenu until it will be needed.
     */
    private class LazyMenu extends JMenu {
        private JLabel label = new JLabel();

        /**
         *
         *
         */
        public LazyMenu() {
            super(ChangeStatusAction.this.getName());
        }

        /**
         *
         *
         */
        public JPopupMenu getPopupMenu() {
            if (getItemCount() > 0) {
                removeAll();
            }

            if (getItemCount() == 0) {
                SystemAction[] actions = GROUPED_ACTIONS;

                for (int i = 0; i < actions.length; i++) {
                    SystemAction action = actions[i];

                    if (action == null) {
                        addSeparator();
                    } else if (action instanceof Presenter.Popup) {
                        JMenuItem item = ((Presenter.Popup) action).getPopupPresenter();
                        add(item);
                    } else {
                        assert false : "Action had no popup presenter: " + action; // NOI18N
                    }
                }
            }

            return super.getPopupMenu();
        }
    }
}
