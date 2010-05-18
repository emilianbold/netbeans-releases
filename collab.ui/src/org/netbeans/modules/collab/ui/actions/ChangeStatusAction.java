/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
