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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutDesigner;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.layoutdesign.LayoutUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action class providing popup menu presenter for setresizability submenu.
 *
 * @author Martin Grebac
 */

public class SetResizabilityAction extends NodeAction {

    private JCheckBoxMenuItem[] items;
    
    protected boolean enable(Node[] nodes) {
        List comps = LayoutUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && (comps.size() > 0));
    }
    
    public String getName() {
        return NbBundle.getMessage(SetResizabilityAction.class, "ACT_SetResizability"); // NOI18N
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
            NbBundle.getMessage(SetResizabilityAction.class, "ACT_SetResizability")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, SetResizabilityAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createResizabilitySubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createResizabilitySubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        List components = LayoutUtils.getSelectedLayoutComponents(nodes);
        if ((components == null) || (components.size() < 1)) {
            return;
        }
        if (!(menu.getMenuComponentCount() > 0)) {
            ResourceBundle bundle = NbBundle.getBundle(SetResizabilityAction.class);

            JCheckBoxMenuItem hItem = new ResizabilityMenuItem(
                    bundle.getString("CTL_ResizabilityH"), // NOI18N
                    components,
                    0);
            JCheckBoxMenuItem vItem = new ResizabilityMenuItem(
                    bundle.getString("CTL_ResizabilityV"), // NOI18N
                    components,
                    1);
            items = new JCheckBoxMenuItem[] {hItem, vItem};
            
            RADComponent rc = (RADComponent)components.get(0);
            FormDesigner formDesigner = FormEditor.getFormDesigner(rc.getFormModel());
            for (int i=0; i<2; i++) {
                items[i].addActionListener(getMenuItemListener());
                HelpCtx.setHelpIDString(items[i], SetResizabilityAction.class.getName());
                menu.add(items[i]);
            }
        }
        updateState(components);
    }

    private void updateState(List components) {
        if ((components == null) || (components.size()<1)) {
            return;
        }
        RADComponent rc = (RADComponent)components.get(0);
        FormDesigner formDesigner = FormEditor.getFormDesigner(rc.getFormModel());
        formDesigner.updateResizabilityActions();
        for (int i=0; i<2; i++) {
            items[i].setEnabled(formDesigner.getResizabilityButtons()[i].isEnabled());
            items[i].setSelected(formDesigner.getResizabilityButtons()[i].isSelected());
        }
    }
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new ResizabilityMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class ResizabilityMenuItem extends JCheckBoxMenuItem {
        private int direction;
        private List components;

        ResizabilityMenuItem(String text, List components, int direction) {
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

    private static class ResizabilityMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof ResizabilityMenuItem)) {
                return;
            }
            ResizabilityMenuItem mi = (ResizabilityMenuItem) source;
            if (!mi.isEnabled()) {
                return;
            }
            int index = mi.getDirection();
            RADComponent radC = (RADComponent)mi.getRADComponents().get(0);
            FormModel fm = radC.getFormModel();
            FormDesigner fd = FormEditor.getFormDesigner(fm);
            fd.getResizabilityButtons()[index].setSelected(!fd.getResizabilityButtons()[index].isSelected());
            ((Action)fd.getResizabilityActions().toArray()[index]).actionPerformed(evt);            
        }
    }
        
    private ActionListener menuItemListener;
}
