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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutdesign.*;

/**
 * Action class providing popup menu presenter for setthesamesize submenu.
 *
 * @author Martin Grebac
 */

public class ChooseSameSizeAction extends NodeAction {

    protected boolean enable(Node[] nodes) {
        List comps = FormUtils.getSelectedLayoutComponents(nodes);
        return ((comps != null) && (comps.size() > 1));
    }
    
    public String getName() {
        return NbBundle.getMessage(ChooseSameSizeAction.class, "ACT_ChooseSameSize"); // NOI18N
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
            NbBundle.getMessage(ChooseSameSizeAction.class, "ACT_ChooseSameSize")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, ChooseSameSizeAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createSameSizeSubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
    }

    private void createSameSizeSubmenu(JMenu menu) {
        if (menu.getMenuComponentCount() > 0) {
            menu.removeAll();
        }
        
        Node[] nodes = getActivatedNodes();
        
        List components = FormUtils.getSelectedLayoutComponents(nodes);
        if ((components == null) || (components.size() < 1)) { //FFF
            return;
        }

        ResourceBundle bundle = NbBundle.getBundle(ChooseSameSizeAction.class);

        JCheckBoxMenuItem sameSizeItemH = new SameSizeMenuItem(
                bundle.getString("CTL_SameSizeHorizontal"), // NOI18N
                components,
                LayoutConstants.HORIZONTAL);
        JCheckBoxMenuItem sameSizeItemV = new SameSizeMenuItem(
                bundle.getString("CTL_SameSizeVertical"), // NOI18N
                components,
                LayoutConstants.VERTICAL);
        
        RADComponent c = (RADComponent)components.get(0);
        LayoutModel lModel = c.getFormModel().getLayoutModel();
        
        int hLinked = lModel.areComponentsLinkSized(getComponentIds(components), LayoutConstants.HORIZONTAL);
        int vLinked = lModel.areComponentsLinkSized(getComponentIds(components), LayoutConstants.VERTICAL);
        
        if (components.size() > 1) {
            if (hLinked != LayoutConstants.INVALID) {
                sameSizeItemH.setSelected((hLinked == LayoutConstants.TRUE) ? true : false);
            } else {
                sameSizeItemH.setEnabled(false);
            }

            if (vLinked != LayoutConstants.INVALID) {
                sameSizeItemV.setSelected((vLinked == LayoutConstants.TRUE) ? true : false);
            } else {
                sameSizeItemV.setEnabled(false);
            }
        } else {
            sameSizeItemH.setSelected((hLinked == LayoutConstants.TRUE) ? true : false);
            if (hLinked != LayoutConstants.TRUE) {
                sameSizeItemH.setEnabled(false);
            }
            sameSizeItemV.setSelected((vLinked == LayoutConstants.TRUE) ? true : false);
            if (vLinked != LayoutConstants.TRUE) {
                sameSizeItemV.setEnabled(false);
            }
        }

        sameSizeItemH.addActionListener(getMenuItemListener());
        sameSizeItemV.addActionListener(getMenuItemListener());

        HelpCtx.setHelpIDString(sameSizeItemH, ChooseSameSizeAction.class.getName());
        HelpCtx.setHelpIDString(sameSizeItemV, ChooseSameSizeAction.class.getName());        

        menu.add(sameSizeItemH);
        menu.add(sameSizeItemV);
    }

    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new SameSizeMenuItemListener();
        return menuItemListener;
    }

    // --------

    private static class SameSizeMenuItem extends JCheckBoxMenuItem {
        private int dimension;
        private List components;

        SameSizeMenuItem(String text, List components, int direction) {
            super(text);
            this.components = components;
            this.dimension = direction;
        }
        
        int getDimension() {
            return dimension;
        }

        List getRADComponents() {
            return components;
        }
    }

    private static class SameSizeMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            Object source = evt.getSource();
            if (!(source instanceof SameSizeMenuItem)) {
                return;
            }
            SameSizeMenuItem mi = (SameSizeMenuItem) source;
            if (!mi.isEnabled()) {
                return;
            }
            List radComponents = mi.getRADComponents();
            RADComponent rc = (RADComponent)radComponents.get(0);
            RADVisualContainer visCont = ((RADVisualComponent)rc).getParentContainer();

            for (int i = 0; i < radComponents.size(); i++) {
                RADComponent rcomp = (RADComponent)radComponents.get(i);
                if (!((RADVisualComponent)rcomp).getParentContainer().equals(visCont)) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                NbBundle.getMessage(ChooseSameSizeAction.class, "TXT_ComponentsNotInOneContainer") //NOI18N
                            )
                    );
                    return;
                }
            }
                        
            FormModel formModel = rc.getFormModel();
            LayoutModel layoutModel = formModel.getLayoutModel();
            Object layoutUndoMark = layoutModel.getChangeMark();
            javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
            boolean autoUndo = true;

            try {
                List compIds = getComponentIds(mi.getRADComponents());
                int dimension = mi.getDimension();
                if (mi.isSelected()) {
                    FormDesigner designer = FormEditor.getFormDesigner(formModel);
                    LayoutDesigner lDesigner = designer.getLayoutDesigner();
                    Iterator iter = compIds.iterator();
                    while (iter.hasNext()) {
                        String compId = (String)iter.next();
                        LayoutComponent lc = layoutModel.getLayoutComponent(compId);
                        if (lDesigner.isComponentResizing(lc, dimension)) {
                            lDesigner.setComponentResizing(lc, dimension, false);
                        }
                    }
                    layoutModel.setSameSize(compIds, dimension);
                } else {
                    layoutModel.unsetSameSize(compIds, dimension);
                }
                autoUndo = false;
            } finally {
                formModel.fireContainerLayoutChanged(visCont, null, null, null);
                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    formModel.addUndoableEdit(ue);
                }
                if (autoUndo) {
                    formModel.forceUndoOfCompoundEdit();
                }
            }
        }
    }
    
    private static List getComponentIds(List/*<RADComponent>*/ components) {
        List ids = new ArrayList();
        Iterator i = components.iterator();
        while (i.hasNext()) {
            RADComponent rc = (RADComponent)i.next();
            if (rc != null) {
                ids.add(rc.getId());
            }
        }
        return ids;
    }
    
    private ActionListener menuItemListener;
}
