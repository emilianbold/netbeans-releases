/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.form.actions;

import java.util.ArrayList;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.Border;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Action for setting layout on selected container(s). Presented only in
 * contextual menus within the Form Editor.
 */

public class SelectLayoutAction extends CallableSystemAction {

    private static String name;

     /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    @Override
    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(SelectLayoutAction.class)
                     .getString("ACT_SelectLayout"); // NOI18N
        return name;
    }

    /** Help context where to find more about the action.
     * @return the help context for this action
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public boolean isEnabled() {
        Node[] nodes = getNodes();
        for (int i=0; i < nodes.length; i++) {
            RADVisualContainer container = getContainer(nodes[i]);
            if (container == null || container.hasDedicatedLayoutSupport())
                return false;
        }
        return true;
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu layoutMenu = new LayoutMenu(getName());
        layoutMenu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(layoutMenu, SelectLayoutAction.class.getName());
        return layoutMenu;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public void performAction() {
    }

    // -------

    private static Node[] getNodes() {
        // using NodeAction and global activated nodes is not reliable
        // (activated nodes are set with a delay after selection in
        // ComponentInspector)
        return ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
    }

    private static RADVisualContainer getContainer(Node node) {
        RADComponentCookie radCookie = node.getCookie(RADComponentCookie.class);
        if (radCookie != null) {
            RADComponent metacomp = radCookie.getRADComponent();
            if (metacomp instanceof RADVisualContainer)
                return (RADVisualContainer) metacomp;
        }
        return null;
    }

    private static PaletteItem[] getAllLayouts() {
        PaletteItem[] allItems = PaletteUtils.getAllItems();
        java.util.List<PaletteItem> layoutsList = new ArrayList<PaletteItem>();
        for (int i = 0; i < allItems.length; i++) {
            if (allItems[i].isLayout()) {
                layoutsList.add(allItems[i]);
            }
        }

        PaletteItem[] layouts = new PaletteItem[layoutsList.size()];
        layoutsList.toArray(layouts);
        return layouts;
    }

    private static class LayoutMenu extends org.openide.awt.JMenuPlus {
        private boolean initialized = false;

        private LayoutMenu(String name) {
            super(name);
        }

        @Override
        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            Node[] nodes = getNodes();

            if (nodes.length != 0 && !initialized) {
                popup.removeAll();

                JMenuItem mi = new JMenuItem(NbBundle.getMessage(SelectLayoutAction.class, "NAME_FreeDesign")); // NOI18N
                popup.add(mi);
                mi.addActionListener(new LayoutActionListener(null));
                popup.addSeparator();

                RADVisualContainer container = getContainer(nodes[0]);                
                boolean hasFreeDesignSupport = RADVisualContainer.isFreeDesignContainer(container);
                if(hasFreeDesignSupport){
                    setBoldFontForMenuText(mi);                        
                }
                
                PaletteItem[] layouts = getAllLayouts();
                for (int i = 0; i < layouts.length; i++) {
                    mi = new JMenuItem(layouts[i].getNode().getDisplayName());
                    HelpCtx.setHelpIDString(mi, SelectLayoutAction.class.getName());                    
                    addSortedMenuItem(popup, mi);
                    mi.addActionListener(new LayoutActionListener(layouts[i]));
                    if(!hasFreeDesignSupport && isContainersLayout(container, layouts[i])){
                        setBoldFontForMenuText(mi);                                                                        
                    }                     
                }
                initialized = true;
            }
            return popup;
        }
        
        private boolean isContainersLayout(RADVisualContainer container, PaletteItem layout){
            return container != null 
                   && (container.getLayoutSupport().getLayoutDelegate().getSupportedClass() == layout.getComponentClass() 
                       || (container.getLayoutSupport().getLayoutDelegate().getClass() == org.netbeans.modules.form.layoutsupport.delegates.NullLayoutSupport.class &&
                           layout.getComponentClass() == org.netbeans.modules.form.layoutsupport.delegates.NullLayoutSupport.class));
        }

        private static void addSortedMenuItem(JPopupMenu menu, JMenuItem menuItem) {
            int n = menu.getComponentCount();
            String text = menuItem.getText();
            for (int i = 2; i < n; i++) { // 2 -> Free Design Item & Separator shouldn't be sorted
                if(menu.getComponent(i) instanceof JMenuItem){
                    String tx = ((JMenuItem)menu.getComponent(i)).getText();
                    if (text.compareTo(tx) < 0) {
                        menu.add(menuItem, i);
                        return;
                    }
                }
            }
            menu.add(menuItem);
        }

        private static void setBoldFontForMenuText(JMenuItem mi) {
            java.awt.Font font = mi.getFont();
            mi.setFont(font.deriveFont(font.getStyle() | java.awt.Font.BOLD));
        }
    
    }

    private static class LayoutActionListener implements ActionListener {
        private PaletteItem paletteItem;

        LayoutActionListener(PaletteItem paletteItem) {
            this.paletteItem = paletteItem;
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            Node[] nodes = getNodes();
            for (int i = 0; i < nodes.length; i++) {
                RADVisualContainer container = getContainer(nodes[i]);
                if (container == null)
                    continue;

                if (paletteItem != null) {
                    // set the selected layout on the container
                    container.getFormModel().getComponentCreator().createComponent(
                        paletteItem.getComponentClassSource(), container, null);
                }
                else if (container.getLayoutSupport() != null) {
                    convertToNewLayout(container);
                }
            }
        }
    }

    private static void convertToNewLayout(RADVisualContainer metacont) {
        FormModel formModel = metacont.getFormModel();
        FormDesigner formDesigner = FormEditor.getFormDesigner(formModel);
        if (!formDesigner.isInDesigner(metacont)) {
            String message = NbBundle.getMessage(SelectLayoutAction.class, "MSG_CannotSwitchToFreeDesign"); // NOI18N
            NotifyDescriptor nd = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
            return;
        }
        LayoutModel layoutModel = formModel.getLayoutModel();

        formModel.setNaturalContainerLayout(metacont);

        Container cont = metacont.getContainerDelegate(formDesigner.getComponent(metacont));
        Insets insets = new Insets(0, 0, 0, 0);
        if (cont instanceof JComponent) {
            Border border = ((JComponent)cont).getBorder();
            if (border != null) {
                insets = border.getBorderInsets(cont);
            }
        }

        Map<String, Rectangle> idToBounds = new HashMap<String, Rectangle>();
        Rectangle notKnown = new Rectangle();
        for (RADVisualComponent metacomp : metacont.getSubComponents()) {
            Component comp = (Component)formDesigner.getComponent(metacomp);
            if (comp == null) {
                comp = (Component)metacomp.getBeanInstance(); // Issue 65919
            }
            Rectangle bounds = comp.getBounds();
            Dimension dim = comp.getPreferredSize();
            if (bounds.equals(notKnown)) { // Issue 65919
                bounds.setSize(dim);
            }
            bounds = new Rectangle(bounds.x - insets.left, bounds.y - insets.top, bounds.width, bounds.height);
            idToBounds.put(metacomp.getId(), bounds);
        }

        Object layoutUndoMark = layoutModel.getChangeMark();
        javax.swing.undo.UndoableEdit ue = layoutModel.getUndoableEdit();
        boolean autoUndo = true;
        try {
            formDesigner.getLayoutDesigner().copyLayoutFromOutside(idToBounds, metacont.getId(), false);
            autoUndo = false;
        } finally {
            if (!layoutUndoMark.equals(layoutModel.getChangeMark())) {
                formModel.addUndoableEdit(ue);
            }
            if (autoUndo) {
                formModel.forceUndoOfCompoundEdit();
            } else {
                FormEditor.updateProjectForNaturalLayout(formModel);
            }
        }
    }
}
