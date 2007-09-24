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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.modelui.switcher;

import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander Simon
 */
public class CodeAssistanceSubmenuAction extends NodeAction {

    private JMenu subMenu;

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return subMenu;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        createSubMenu();
        return subMenu;
    }

    private void createSubMenu() {
        if (subMenu == null) {
            subMenu = new JMenu(getName()); 
        }
        subMenu.removeAll();
        boolean enabled = false;
        for(Action action : addActionsFromLayers()){
            if (action instanceof Presenter.Menu) {
                JMenuItem item = ((Presenter.Menu)action).getMenuPresenter();
                subMenu.add(item);
            } else {
                subMenu.add(action);
            }
            enabled = true;
        }
        subMenu.setEnabled(enabled);
    }

    private List<Action> addActionsFromLayers() {
        String path = "NativeProjects/Actions"; // NOI18N
        List<Action> popup = new ArrayList<Action>();
        Lookup look = Lookups.forPath(path);
        for (Object next : look.lookupAll(Object.class)) {
            if (next instanceof Action) {
                popup.add((Action) next);
            }
        }
        return popup;
    }

    protected void performAction(Node[] activatedNodes) {
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public String getName() {
        return NbBundle.getMessage(CodeAssistanceSubmenuAction.class, "LBL_CodeAssistanceAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}