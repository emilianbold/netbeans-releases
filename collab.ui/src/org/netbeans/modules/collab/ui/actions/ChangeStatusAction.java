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
package org.netbeans.modules.collab.ui.actions;

import java.awt.event.*;
import javax.swing.*;

import org.openide.nodes.Node;
import org.openide.util.*;
import org.openide.util.actions.*;

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
