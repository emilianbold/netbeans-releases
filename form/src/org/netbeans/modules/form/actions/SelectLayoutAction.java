/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form.actions;

import java.util.ArrayList;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.text.MessageFormat;

import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.netbeans.modules.form.palette.*;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.layoutsupport.*;

/**
 * Action for setting layout on selected container(s).
 */

public class SelectLayoutAction extends NodeAction {

    private static String name;

     /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(SelectLayoutAction.class)
                     .getString("ACT_SelectLayout"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SelectLayoutAction.class);
    }

    /**
     * Standard perform action extended by actually activated nodes.
     *
     * @param activatedNodes gives array of actually activated nodes.
     */
    protected void performAction(Node[] activatedNodes) {
    }

    /**
    * Test whether the action should be enabled based
    * on the currently activated nodes.
    *
    * @param activatedNodes current activated nodes, may be empty but not <code>null</code>
    * @return <code>true</code> to be enabled, <code>false</code> to be disabled
    */
    protected boolean enable(Node[] activatedNodes) {
        // Fix of 43921 that allows us to leave fix of 39035 untouched
        activatedNodes = ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
        for (int i=0; i < activatedNodes.length; i++) {
            RADVisualContainer container = getContainer(activatedNodes[i]);
            if (container == null)
                return false;

            if (container.getLayoutSupport().isDedicated())
                return false;
        }
        return true;
    }

    private static RADVisualContainer getContainer(Node node) {
        RADComponentCookie radCookie = (RADComponentCookie)
            node.getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualContainer)
                return (RADVisualContainer) metacomp;
        }
        return null;
    }
    
    /**
     * Returns a JMenuItem that presents the Action, that implements this
     * interface, in a MenuBar.
     * @return the JMenuItem representation for the Action
     */
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    private PaletteItem[] getAllLayouts() {
        PaletteItem[] allItems = CPManager.getDefault().getAllItems();
        ArrayList layoutsList = new ArrayList();
        for (int i = 0; i < allItems.length; i++) {
            if (allItems[i].isLayout()) {
                layoutsList.add(allItems[i]);
            }
        }

        PaletteItem[] layouts = new PaletteItem[layoutsList.size()];
        layoutsList.toArray(layouts);
        return layouts;
    }

    /**
     * Returns a JMenuItem that presents the Action, that implements this
     * interface, in a Popup Menu.
     * @return the JMenuItem representation for the Action
     */
    public JMenuItem getPopupPresenter() {
        JMenu layoutMenu = new LayoutMenu(getName());
        layoutMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(layoutMenu, SelectLayoutAction.class.getName());
        return layoutMenu;
    }
    
    private class LayoutMenu extends org.openide.awt.JMenuPlus {
        private boolean initialized = false;
        
        private LayoutMenu(String name) {
            super(name);
        }
        
        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            Node[] nodes = getActivatedNodes();

            if ((nodes.length != 0) && !initialized) {
                popup.removeAll();
                PaletteItem[] layouts = getAllLayouts();
                for (int i = 0; i < layouts.length; i++) {
                    JMenuItem mi = new JMenuItem(layouts[i].getNode().getDisplayName());
                    HelpCtx.setHelpIDString(mi, SelectLayoutAction.class.getName());
                    popup.add(mi);
                    mi.addActionListener(new LayoutActionListener(nodes, layouts[i]));
                }
                initialized = true;
            }
            return popup;
        }
    }

    class LayoutActionListener implements ActionListener
    {
        private Node[] activatedNodes;
        private PaletteItem paletteItem;

        LayoutActionListener(Node[] activatedNodes, PaletteItem paletteItem) {
            this.activatedNodes = activatedNodes;
            this.paletteItem = paletteItem;
        }

        public void actionPerformed(ActionEvent evt) {
            if (activatedNodes == null)
                return; // due to the Swing bug with popup menus, it can be null

            for (int i = 0; i < activatedNodes.length; i++) {
                RADVisualContainer container = getContainer(activatedNodes[i]);
                if (container == null)
                    continue;
                
                // set the selected layout on the activated container
                container.getFormModel().getComponentCreator().createComponent(
                    paletteItem, container, null);
            }
        }
    }
}
