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

package org.netbeans.modules.form.actions;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import org.netbeans.modules.form.layoutdesign.LayoutUtils;

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

            JMenuItem leftItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignLeft"), // NOI18N
                    components,
                    0);
            JMenuItem rightItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignRight"), // NOI18N
                    components,
                    1);
            JMenuItem upItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignUp"), // NOI18N
                    components,
                    2);
            JMenuItem downItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignDown"), // NOI18N
                    components,
                    3);
            items = new JMenuItem[] {leftItem, rightItem, upItem, downItem};
            for (int i=0; i<4; i++) {
                items[i].addActionListener(getMenuItemListener());
                items[i].setEnabled(false);
                HelpCtx.setHelpIDString(items[i], AlignAction.class.getName());
                menu.add(items[i]);
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
            Action a = (Action)formDesigner.getDesignerActions().toArray()[i];
            items[i].setEnabled(a.isEnabled());
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
            ((Action)fd.getDesignerActions().toArray()[index]).actionPerformed(evt);            
        }
    }
        
    private ActionListener menuItemListener;
}
