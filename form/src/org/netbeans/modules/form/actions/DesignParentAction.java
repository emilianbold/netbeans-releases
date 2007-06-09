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
import java.util.Vector;
import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

import org.netbeans.modules.form.*;
import org.openide.util.SharedClassObject;

/**
 * Class used to represent the parent hierarchy of a single selected component in
 * the UI editor. This allows the user to quickly jump to editing a parent without
 * having to move their mouse to the inspector window and perform an action.
 * @author Wade Chandler
 */

public class DesignParentAction extends NodeAction {
    
    private static EditFormAction editFormAction = (EditFormAction)
            SharedClassObject.findObject(EditFormAction.class, true);
    
    protected boolean enable(Node[] nodes) {
        boolean ret = false;
        if(nodes!=null&&nodes.length==1&&nodes[0] instanceof RADComponentNode){
            RADComponentNode ln = (RADComponentNode)nodes[0];
            RADComponent lc = ln.getRADComponent();
            if(lc.getParentComponent()!=null){
                ret = true;
            }else{
                ret = false;
            }
        }else{
            ret = false;
        }
        return ret;
    }
    
    public String getName() {
        return NbBundle.getMessage(DesignParentAction.class, "ACT_DesignParentAction"); // NOI18N
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
                NbBundle.getMessage(DesignParentAction.class, "ACT_DesignParentAction")); // NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, DesignParentAction.class.getName());
        createSubmenu(popupMenu);
        return popupMenu;
    }
    
    private void createSubmenu(JMenu menu) {
        Node[] nodes = getActivatedNodes();
        Vector<DesignParentMenuItem> pmis = new Vector<DesignParentMenuItem>(10);
        List components = FormUtils.getSelectedLayoutComponents(nodes);
        if ((components == null) || (components.size() > 1)) {
            return;
        }
        RADComponent lc = (RADComponent)components.get(0);
        RADComponent pc = lc.getParentComponent();
        boolean isp = false;
        while(pc!=null){
            DesignParentMenuItem mi = new DesignParentMenuItem(pc);
            isp = pc.getParentComponent()==null||pc==pc.getParentComponent();
            if(isp){
                mi.setText(NbBundle.getMessage(DesignParentAction.class, "ACT_DesignParentTopMenuItemName")); // NOI18N
            }
            pmis.add(mi);
            mi.addActionListener(getMenuItemListener());
            pc = pc.getParentComponent();
            if(isp){
                pc=null;
            }
        }
        for(int i = pmis.size()-1; i>=0; i--){
            menu.add(pmis.elementAt(i));
        }
    }
    
    
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new DesignParentMenuItemListener();
        return menuItemListener;
    }
    
    class DesignParentMenuItem extends JMenuItem {
        private RADComponent radc = null;
        public DesignParentMenuItem(RADComponent c){
            radc = c;
            if(c!=null){
                this.setText(c.getName());
            }
        }
        public void setRADComponent(RADComponent radc){
            this.radc = radc;
        }
        public RADComponent getRADComponent(){
            return this.radc;
        }
    }
    
    private static class DesignParentMenuItemListener implements ActionListener {
        public void actionPerformed(ActionEvent evt) {
            
            Object source = evt.getSource();
            if( source == null || !(source instanceof DesignParentMenuItem) ){
                return;
            }
            
            DesignParentMenuItem mi = (DesignParentMenuItem)source;
            
            RADComponent lc = (RADComponent)mi.getRADComponent();
            FormModel fm = lc.getFormModel();
            FormDesigner fd = FormEditor.getFormDesigner(fm);
            if (lc instanceof RADVisualContainer) {
                FormDesigner designer = FormEditor.getFormDesigner(lc.getFormModel());
                if (designer != null) {
                    designer.setTopDesignComponent((RADVisualComponent)lc, true);
                    designer.requestActive();
                }
                
                editFormAction.setEnabled(
                        lc.getFormModel().getTopRADComponent() != lc);
            }
        }
    }
    
    private ActionListener menuItemListener;
}
