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

package org.netbeans.modules.form.actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action class providing popup menu presenter for align submenu.
 *
 * @author Martin Grebac
 */

public class AlignAction extends NodeAction {

    private JMenuItem[] items;
    
    protected boolean enable(Node[] nodes) {
        List comps = FormUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && (comps.size() > 1));
    }
    
    public String getName() {
        return NbBundle.getMessage(AlignAction.class, "ACT_Align"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] activatedNodes) { }

    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    /**
     * Returns a JMenuItem that presents this action in a Popup Menu.
     * @return the JMenuItem representation for the action
     */
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenu(
            NbBundle.getMessage(AlignAction.class, "ACT_Align")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, AlignAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createAlignSubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }


    private void createAlignSubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        List components = FormUtils.getSelectedLayoutComponents(nodes);
        if (!(menu.getMenuComponentCount() > 0)) {
            ResourceBundle bundle = NbBundle.getBundle(AlignAction.class);

            JMenuItem leftGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupLeft"), // NOI18N
                    components,
                    0);
            JMenuItem rightGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupRight"), // NOI18N
                    components,
                    1);
            JMenuItem upGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupUp"), // NOI18N
                    components,
                    2);
            JMenuItem downGroupItem = new AlignMenuItem(
                    bundle.getString("CTL_GroupDown"), // NOI18N
                    components,
                    3);
            JMenuItem leftItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignLeft"), // NOI18N
                    components,
                    4);
            JMenuItem rightItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignRight"), // NOI18N
                    components,
                    5);
            JMenuItem upItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignUp"), // NOI18N
                    components,
                    6);
            JMenuItem downItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignDown"), // NOI18N
                    components,
                    7);
            items = new JMenuItem[] {leftGroupItem, rightGroupItem, upGroupItem,
                downGroupItem, leftItem, rightItem, upItem, downItem};
            for (int i=0; i<8; i++) {
                items[i].addActionListener(getMenuItemListener());
                items[i].setEnabled(false);
                HelpCtx.setHelpIDString(items[i], AlignAction.class.getName());
                menu.add(items[i]);
                if (i == 3) menu.addSeparator();
            }
        }        
        updateState(components);
    }

    private void updateState(List components) {
        if ((components == null) || (components.size()<2)) {
            return;
        }
        RADComponent rc = (RADComponent)components.get(0);
        FormDesigner formDesigner = FormEditor.getFormDesigner(rc.getFormModel());
        for (int i=0; i<4; i++) {
            Action a = (Action)formDesigner.getDesignerActions(true).toArray()[i];
            items[i].setEnabled(a.isEnabled());
            items[i+4].setEnabled(a.isEnabled());
        }
    }
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new AlignMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class AlignMenuItem extends JMenuItem {
        private int direction;
        private List components;

        AlignMenuItem(String text, List components, int direction) {
            super(text);
            this.components = components;
            this.direction = direction;
        }
        
        int getDirection() {
            return direction;
        }

        List getRADComponents() {
            return components;
        }
    }

    private static class AlignMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof AlignMenuItem)) {
                return;
            }
            AlignMenuItem mi = (AlignMenuItem) source;
            if (!mi.isEnabled()) {
                return;
            }
            int index = mi.getDirection();
            RADComponent radC = (RADComponent)mi.getRADComponents().get(0);
            FormModel fm = radC.getFormModel();
            FormDesigner fd = FormEditor.getFormDesigner(fm);
            ((Action)fd.getDesignerActions(false).toArray()[index]).actionPerformed(evt);            
        }
    }
        
    private ActionListener menuItemListener;
}
