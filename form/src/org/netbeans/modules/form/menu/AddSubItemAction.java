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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.form.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.netbeans.modules.form.ComponentInspector;
import org.netbeans.modules.form.FormDesigner;
import org.netbeans.modules.form.FormEditor;
import org.netbeans.modules.form.FormUtils;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADComponentNode;
import org.netbeans.modules.form.actions.AlignAction;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * Action class providing popup menu presenter for add submenu for JMenu components.
 *
 * @author Joshua Marinacci
 */
public class AddSubItemAction extends NodeAction {
    private static final boolean DEBUG = false;
    public AddSubItemAction() {
        p("add sub item action is created");
    }

    private JMenuItem[] items;
    
    //fix this
    protected boolean enable(Node[] nodes) {
        return true; 
    }
    
    public String getName() {
        return "Insert";
        //return NbBundle.getMessage(AlignAction.class, "ACT_Align"); // NOI18N
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
        JMenu popupMenu = new JMenu("Insert");
            //NbBundle.getMessage(AlignAction.class, "ACT_Align")); // NOI18N
        
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

 
    private static void p(String s) {
        if(DEBUG) {
            System.out.println(s);
        }
    }
    
    
    // this add listener works by finding a matching item in the palette
    // and calling the usual addComponentToEndOfMenu routine to do the
    // actual adding.
    private class AddListener implements ActionListener {
        private Class clazz;
        
        public AddListener(Class clazz) {
            this.clazz = clazz;
        }
        public void actionPerformed(ActionEvent e) {
            //List components = FormUtils.getSelectedLayoutComponents(nodes);
            Node[] nds = getNodes();
            for(Node nd : nds) {
                if(nd instanceof RADComponentNode) {
                    RADComponentNode rnode = (RADComponentNode) nd;
                    RADComponent comp = rnode.getRADComponent();
                    p("adding nodes to: comp = " + comp);
                    PaletteItem[] items = PaletteUtils.getAllItems();
                    for(PaletteItem item : items) {
                        if(clazz == item.getComponentClass()) {
                            p("found the menu item palette stuff");
                            MenuEditLayer.addComponentToEndOfMenu(comp, item);
                            return;
                        }
                    }
                }
            }
        }
    };
    
    
    /* This creates a menu of components to add. It's based on the contents of
     * the 'SwingMenus' palette category, but JMenuBar and JPopupMenu are excluded
     * because they cannot be added as children of JMenu.
     * 
     * */
    private void createAlignSubmenu(JMenu menu) {
        final Node[] nodes = getActivatedNodes();
        final List components = FormUtils.getSelectedLayoutComponents(nodes);
        //only create this menu the first time it is called
        p("checking to creating the menu");
        if (!(menu.getMenuComponentCount() > 0)) {
            p("creating the menu");
            // extract the list of menu related components from the palette
            Node[] categories = PaletteUtils.getCategoryNodes(PaletteUtils.getPaletteNode(), false);
            //p("categories");
            for(Node cat : categories) {
                p("cat = " + cat + " " + cat.getDisplayName() + " " + cat.getClass().getName());
                if("SwingMenus".equals(cat.getName())) {
                    p("found swing menus");
                    Node[] items = PaletteUtils.getItemNodes(cat, false);
                    for(Node item : items) {
                        PaletteItem paletteItem = (PaletteItem)item.getLookup().lookup( PaletteItem.class );
                        if( null != paletteItem) {
                            
                            if(JMenuBar.class.isAssignableFrom(paletteItem.getComponentClass())) continue;
                            if(JPopupMenu.class.isAssignableFrom(paletteItem.getComponentClass())) continue;
                            
                            JMenuItem menuitem = new JMenuItem(paletteItem.getComponentClass().getSimpleName());
                            menuitem.addActionListener(new AddListener(paletteItem.getComponentClass()));
                            menu.add(menuitem);
                        }
                    }
                }
            }
            
            
            /*
            ResourceBundle bundle = NbBundle.getBundle(AlignAction.class);
            JMenuItem centerVItem = new AlignMenuItem(
                    bundle.getString("CTL_AlignVCenter"), // NOI18N
                    components,
                    11);*/
            /*
            items = new JMenuItem[] {leftGroupItem, rightGroupItem, centerHGroupItem,
                upGroupItem, downGroupItem, centerVGroupItem, leftItem, rightItem,
                centerHItem, upItem, downItem, centerVItem};
            for (int i=0; i < items.length; i++) {
                items[i].addActionListener(getMenuItemListener());
                items[i].setEnabled(false);
                HelpCtx.setHelpIDString(items[i], AlignAction.class.getName());
                menu.add(items[i]);
                if (i+1 == items.length/2) {
                    menu.addSeparator();
                }
            }*/
        }
        updateState(components);
    }

    //i'm not sure what this does. it came from AlignAction
    private void updateState(List components) {
        if ((components == null) || (components.size()<2)) {
            return;
        }
        RADComponent rc = (RADComponent)components.get(0);
        FormDesigner formDesigner = FormEditor.getFormDesigner(rc.getFormModel());
        java.util.Collection col = formDesigner.getDesignerActions(true);
        int n = col.size();
        assert n == (items.length / 2);
        Action[] actions = (Action[]) col.toArray(new Action[n]);
        for (int i=0; i < n; i++) {
            items[i].setEnabled(actions[i].isEnabled());
            items[i+n].setEnabled(actions[i].isEnabled());
        }
    }
    /*
    private ActionListener getMenuItemListener() {
        if (menuItemListener == null)
            menuItemListener = new AlignMenuItemListener();
        return menuItemListener;
    }*/

    // --------
/*
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
  */  
    private static Node[] getNodes() {
        // using NodeAction and global activated nodes is not reliable
        // (activated nodes are set with a delay after selection in
        // ComponentInspector)
        return ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
    }
}
