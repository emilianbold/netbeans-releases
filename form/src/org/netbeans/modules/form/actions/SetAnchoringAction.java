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

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action class providing popup menu presenter for setanchoring submenu.
 *
 * @author Martin Grebac
 */

public class SetAnchoringAction extends NodeAction {

    private JCheckBoxMenuItem[] items;
    
    protected boolean enable(Node[] nodes) {
        List comps = LayoutUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && (comps.size() > 0));
    }
    
    public String getName() {
        return NbBundle.getMessage(SetAnchoringAction.class, "ACT_SetAnchoring"); // NOI18N
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
            NbBundle.getMessage(SetAnchoringAction.class, "ACT_SetAnchoring")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, SetAnchoringAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createAnchoringSubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createAnchoringSubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        List components = LayoutUtils.getSelectedLayoutComponents(nodes);
        if ((components == null) || (components.size() < 1)) {
            return;
        }
        if (!(menu.getMenuComponentCount() > 0)) {
            ResourceBundle bundle = NbBundle.getBundle(SetAnchoringAction.class);

            JCheckBoxMenuItem leftItem = new AnchoringMenuItem(
                    bundle.getString("CTL_AnchorLeft"), // NOI18N
                    components,
                    0);
            JCheckBoxMenuItem rightItem = new AnchoringMenuItem(
                    bundle.getString("CTL_AnchorRight"), // NOI18N
                    components,
                    1);
            JCheckBoxMenuItem topItem = new AnchoringMenuItem(
                    bundle.getString("CTL_AnchorTop"), // NOI18N
                    components,
                    2);
            JCheckBoxMenuItem bottomItem = new AnchoringMenuItem(
                    bundle.getString("CTL_AnchorBottom"), // NOI18N
                    components,
                    3);
            items = new JCheckBoxMenuItem[] {leftItem, rightItem, topItem, bottomItem};
            for (int i=0; i<4; i++) {
                items[i].addActionListener(getMenuItemListener());
                HelpCtx.setHelpIDString(items[i], SetAnchoringAction.class.getName());
                menu.add(items[i]);
            }
        }        
        updateState(components);
    }

    private void updateState(List components) {
        if ((components == null) || (components.size() < 1)) {
            return;
        }
        FormModel formModel = ((RADComponent)components.get(0)).getFormModel();
        LayoutModel layoutModel = formModel.getLayoutModel();
        FormDesigner formDesigner = FormEditor.getFormDesigner(formModel);
        LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
        Iterator iter = components.iterator();
        boolean[] matchAlignment = new boolean[4];
        boolean[] cannotChangeTo = new boolean[4];
        while (iter.hasNext()) {
            RADComponent radC = (RADComponent)iter.next();
            String id = radC.getId();
            LayoutComponent comp = layoutModel.getLayoutComponent(id);
            int[][] alignment = new int[][] {
                layoutDesigner.getAdjustableComponentAlignment(comp, LayoutConstants.HORIZONTAL),
                        layoutDesigner.getAdjustableComponentAlignment(comp, LayoutConstants.VERTICAL)};
                        for (int i=0; i<4; i++) {
                            if ((alignment[i/2][1] & (1 << i%2)) == 0) { // the alignment cannot be changed
                                cannotChangeTo[i] = true;
                            }
                            if (alignment[i/2][0] != -1) {
                                matchAlignment[i] = matchAlignment[i] || (alignment[i/2][0] == i%2);
                            }
                        }
        }
        for (int i=0; i<4; i++) {
            boolean match;
            boolean miss;
            match = matchAlignment[i];
            miss = matchAlignment[2*(i/2) + 1 - i%2];
            items[i].setEnabled((match || miss) && (!cannotChangeTo[i]));
            items[i].setSelected(!miss && match);
        }        
    }
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new AnchoringMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class AnchoringMenuItem extends JCheckBoxMenuItem {
        private int direction;
        private List components;

        AnchoringMenuItem(String text, List components, int direction) {
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

    private static class AnchoringMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof AnchoringMenuItem)) {
                return;
            }
            AnchoringMenuItem mi = (AnchoringMenuItem) source;
            if (!mi.isEnabled()) {
                return;
            }
            int index = mi.getDirection();
            FormModel formModel = ((RADComponent)mi.getRADComponents().get(0)).getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            FormDesigner formDesigner = FormEditor.getFormDesigner(formModel);
            LayoutDesigner layoutDesigner = formDesigner.getLayoutDesigner();
            Set containers = new HashSet();
            Iterator iter = mi.getRADComponents().iterator();
            while (iter.hasNext()) {
                RADComponent radC = (RADComponent)iter.next();
                String compId = radC.getId();
                LayoutComponent layoutComp = layoutModel.getLayoutComponent(compId);
                boolean changed = false;
                int[] alignment = layoutDesigner.getAdjustableComponentAlignment(layoutComp, index/2);
                if (((alignment[1] & (1 << index%2)) != 0) && (alignment[0] != index%2)) {
                    layoutDesigner.adjustComponentAlignment(layoutComp, index/2, index%2);
                    changed = true;
                }
                if (changed) {
                    RADVisualComponent comp = (RADVisualComponent)formModel.getMetaComponent(compId);
                    containers.add(comp.getParentContainer());
                }
            }
            iter = containers.iterator();
            while (iter.hasNext()) {
                formModel.fireContainerLayoutChanged((RADVisualContainer)iter.next(), null, null, null);
            }
            if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                formModel.addUndoableEdit(ue);
            }
        }
    }
        
    private ActionListener menuItemListener;
}
