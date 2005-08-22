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
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.*;
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;

/**
 * Action class providing popup menu presenter for setthesamesize submenu.
 *
 * @author Martin Grebac
 */

public class ChooseSameSizeAction extends NodeAction {

    protected boolean enable(Node[] nodes) {
        List comps = getRADComponents(nodes);
        return ((comps != null) && (comps.size() > 0));
    }
    
    public String getName() {
        return NbBundle.getMessage(EventsAction.class, "ACT_ChooseSameSize"); // NOI18N
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

    private List/*RADComponent*/ getRADComponents(Node[] nodes) {
        if ((nodes == null) || (nodes.length < 1))
            return null;

        ArrayList components = new ArrayList();
        for (int i=0; i<nodes.length; i++) {
            RADComponentCookie radCookie =
                (RADComponentCookie) nodes[i].getCookie(RADComponentCookie.class);
            if (radCookie != null) {
                RADComponent metacomp = radCookie.getRADComponent();
                if ((metacomp instanceof RADVisualComponent)) {
                    RADVisualContainer visCont = ((RADVisualComponent)metacomp).getParentContainer();
                    if ((visCont!= null) && (visCont.getLayoutSupport() == null)) {
                        components.add(metacomp);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
        return components;
    }
        
    private void createSameSizeSubmenu(JMenu menu) {
        if (menu.getMenuComponentCount() > 0) {
            menu.removeAll();
        }
        
        Node[] nodes = getActivatedNodes();
        
        List components = getRADComponents(nodes);
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
            
            if (!mi.isSelected()) {
                FormModel formModel = rc.getFormModel();
                LayoutModel layoutModel = formModel.getLayoutModel();
                Object layoutUndoMark = layoutModel.getChangeMark();
                javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();

                layoutModel.unsetSameSize(getComponentIds(mi.getRADComponents()), mi.getDimension());

                formModel.fireContainerLayoutChanged(visCont, null, null, null);

                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    formModel.addUndoableEdit(ue);
                }
            } else {

                FormModel formModel = rc.getFormModel();
                LayoutModel layoutModel = formModel.getLayoutModel();
                Object layoutUndoMark = layoutModel.getChangeMark();
                javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();

                layoutModel.setSameSize(getComponentIds(mi.getRADComponents()), mi.getDimension());

                formModel.fireContainerLayoutChanged(visCont, null, null, null);

                if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                    formModel.addUndoableEdit(ue);
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
