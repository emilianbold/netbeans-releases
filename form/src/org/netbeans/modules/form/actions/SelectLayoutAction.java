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
import javax.swing.event.MenuListener;
import javax.swing.event.MenuEvent;
import java.text.MessageFormat;

import org.openide.TopManager;
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

public class SelectLayoutAction extends NodeAction
{
    static final long serialVersionUID = 4760011790717781801L;

     /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(SelectLayoutAction.class).getString("ACT_SelectLayout");
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(SelectLayoutAction.class);
    }

    /** Icon resource.
     * @return name of resource for icon
     */
    protected String iconResource() {
        return "/org/netbeans/modules/form/resources/selectLayout.gif"; // NOI18N
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
        for (int i=0; i < activatedNodes.length; i++) {
            RADVisualContainer container = getContainer(activatedNodes[i]);
            if (container == null)
                return false;

            LayoutSupport ls = container.getLayoutSupport();
            if (ls != null && ls.getLayoutClass() == null
                    && ls.getClass() != NullLayoutSupport.class)
                return false;

            FormDesigner designer = container.getFormModel().getFormDesigner();
            if (!designer.isInDesignedTree(container))
                return false;
        }
        return true;
    }

    private static RADVisualContainer getContainer(Node node) {
        RADVisualContainer container = null;
        FormLayoutCookie layoutCookie =
            (FormLayoutCookie) node.getCookie(FormLayoutCookie.class);

        if (layoutCookie != null) {
            container = layoutCookie.getLayoutNode().getLayoutSupport().getContainer();
//            System.err.println("**** container = " + container); // XXX
        }
        else {
            RADComponentCookie nodeCookie =
                (RADComponentCookie) node.getCookie(RADComponentCookie.class);
            if (nodeCookie != null) {
                if (nodeCookie.getRADComponent() instanceof RADVisualContainer) {
                    container = (RADVisualContainer) nodeCookie.getRADComponent();
                }
            }
        }
        return container;
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
        JMenu popupMenu = new org.openide.awt.JMenuPlus(getName());
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, SelectLayoutAction.class.getName());
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu =(JMenu)e.getSource();
                if (menu.getMenuComponentCount() > 0) { // [IAN - Patch for Swing 1.1, which throws NullPointerException if removeAll is called on empty uninitialized JMenu]
                    menu.removeAll();
                }
                Node[] nodes = getActivatedNodes();
                if (nodes.length == 0)
                    return;

                PaletteItem[] layouts = getAllLayouts();

                for (int i = 0; i < layouts.length; i++) {
                    JMenuItem mi = new JMenuItem(layouts[i].getDisplayName());
                    HelpCtx.setHelpIDString(mi, SelectLayoutAction.class.getName());
                    menu.add(mi);
                    mi.addActionListener(new LayoutActionListener(nodes, layouts[i]));
                }
            }
            
            public void menuDeselected(MenuEvent e) {}
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
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
            if (activatedNodes == null) return;
            // due to the Swing bug with popup menus, it can be null

            for (int i = 0; i < activatedNodes.length; i++) {
                RADVisualContainer container = getContainer(activatedNodes[i]);
                if (container == null) continue;
                
                // set the selected layout on the activated container
                LayoutSupport layoutSupport = null;
                try {
                    layoutSupport = paletteItem.createLayoutSupportInstance();
                }
                catch (Exception e) {
                    if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                        e.printStackTrace();

                    TopManager.getDefault().notify(new NotifyDescriptor.Message(
                        MessageFormat.format(
                            FormEditor.getFormBundle().getString("FMT_ERR_LayoutInit"),
                            new Object[] { paletteItem.getItemClass().getName(),
                                            e.getClass().getName() }),
                        NotifyDescriptor.ERROR_MESSAGE));

                    return;
                }

                if (layoutSupport == null) {
                    TopManager.getDefault().notify(new NotifyDescriptor.Message(
                        MessageFormat.format(
                            FormEditor.getFormBundle().getString("FMT_ERR_LayoutNotFound"),
                            new Object[] { paletteItem.getItemClass().getName() }),
                        NotifyDescriptor.ERROR_MESSAGE));

                    return;
                }

                container.getFormModel().setContainerLayout(container,
                                                            layoutSupport);
            }
        }
    }
}
