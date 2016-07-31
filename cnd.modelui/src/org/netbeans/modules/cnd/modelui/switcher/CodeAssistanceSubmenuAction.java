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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelui.switcher;

import java.util.ArrayList;
import java.util.Collection;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Alexander Simon, Vladimir Voskresensky
 */
public class CodeAssistanceSubmenuAction extends NodeAction {

    private final Collection<Action> items;

    public CodeAssistanceSubmenuAction() {
        items = new ArrayList<Action>();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return createSubMenu();
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return createSubMenu();
    }

    private JMenuItem createSubMenu() {
        if (items.isEmpty()) {
            items.addAll(Utilities.actionsForPath("NativeProjects/CodeAssistanceActions")); // NOI18N
        }

        JMenu menu = new JMenu(getName());

        if (items != null) {
            for (Action action : items) {
                if (action instanceof Presenter.Popup) {
                    JMenuItem item = ((Presenter.Popup) action).getPopupPresenter();
                    menu.add(item);
                } else if (action instanceof Presenter.Menu) {
                    JMenuItem item = ((Presenter.Menu) action).getMenuPresenter();
                    menu.add(item);
                } else {
                    menu.add(action);
                }
            }
        }
        menu.setEnabled(!items.isEmpty());
        return menu;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public final String getName() {
        return NbBundle.getMessage(CodeAssistanceSubmenuAction.class, "LBL_CodeAssistanceAction_Name"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
