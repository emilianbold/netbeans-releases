/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Action class providing popup menu presenter for add submenu for JMenu components.
 *
 * @author Joshua Marinacci
 */
public class AddSubItemAction extends NodeAction {
    private JMenuItem[] items;
    
    //fix this
    protected boolean enable(Node[] nodes) {
        return true; 
    }
    
    public String getName() {
        return NbBundle.getMessage(AddSubItemAction.class, "ACT_AddFromPalette"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected void performAction(Node[] activatedNodes) { }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    /**
     * Returns a JMenuItem that presents this action in a Popup Menu.
     * @return the JMenuItem representation for the action
     */
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu popupMenu = new JMenu(NbBundle.getMessage(AddSubItemAction.class, "ACT_AddFromPalette")); //NOI18N
        
        popupMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(popupMenu, AlignAction.class.getName());
        
        popupMenu.addMenuListener(new MenuListener() {
            public void menuSelected(MenuEvent e) {
                JMenu menu = (JMenu) e.getSource();
                createInsertSubmenu(menu);
            }
            
            public void menuDeselected(MenuEvent e) {}
            
            public void menuCanceled(MenuEvent e) {}
        });
        return popupMenu;
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
                    PaletteItem[] items = PaletteUtils.getAllItems();
                    for(PaletteItem item : items) {
                        if(clazz == item.getComponentClass()) {
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
    private void createInsertSubmenu(JMenu menu) {
        final Node[] nodes = getActivatedNodes();
        final List components = FormUtils.getSelectedLayoutComponents(nodes);
        //only create this menu the first time it is called
        if (!(menu.getMenuComponentCount() > 0)) {
            // extract the list of menu related components from the palette
            Node[] categories = PaletteUtils.getCategoryNodes(PaletteUtils.getPaletteNode(), false);
            //p("categories");
            for(Node cat : categories) {
                if("SwingMenus".equals(cat.getName())) {
                    Node[] items = PaletteUtils.getItemNodes(cat, false);
                    for(Node item : items) {
                        PaletteItem paletteItem = item.getLookup().lookup( PaletteItem.class );
                        if( null != paletteItem) {
                            
                            if(JMenuBar.class.isAssignableFrom(paletteItem.getComponentClass())) continue;
                            if(JPopupMenu.class.isAssignableFrom(paletteItem.getComponentClass())) continue;
                            
                            JMenuItem menuitem = new JMenuItem(paletteItem.getNode().getDisplayName());
                            menuitem.addActionListener(new AddListener(paletteItem.getComponentClass()));
                            menu.add(menuitem);
                        }
                    }
                }
            }
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
        java.util.Collection<Action> col = formDesigner.getDesignerActions(true);
        int n = col.size();
        assert n == (items.length / 2);
        Action[] actions = col.toArray(new Action[n]);
        for (int i=0; i < n; i++) {
            items[i].setEnabled(actions[i].isEnabled());
            items[i+n].setEnabled(actions[i].isEnabled());
        }
    }

    private static Node[] getNodes() {
        // using NodeAction and global activated nodes is not reliable
        // (activated nodes are set with a delay after selection in
        // ComponentInspector)
        return ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
    }
}
