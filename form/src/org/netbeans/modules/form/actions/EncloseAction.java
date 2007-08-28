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

package org.netbeans.modules.form.actions;

import java.util.ArrayList;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.undo.UndoableEdit;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.layoutdesign.LayoutModel;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.util.actions.NodeAction;

public class EncloseAction extends NodeAction {

    public String getName() {
        return org.openide.util.NbBundle.getBundle(EncloseAction.class)
                     .getString("ACT_EncloseInContainer"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(Node[] nodes) {
        List comps = getComponents(nodes);
        return ((comps != null) && getContainer(comps) != null);
    }

    protected void performAction(Node[] nodes) {
    }

    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    public JMenuItem getPopupPresenter() {
        JMenu menu = new ContainersMenu(getName(), getComponents(getActivatedNodes()));
        menu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(menu, EncloseAction.class.getName());
        return menu;
    }

    protected boolean asynchronous() {
        return false;
    }

    // -------

    private static List getComponents(Node[] nodes) {
        return FormUtils.getSelectedLayoutComponents(nodes);
    }

    private static RADVisualContainer getContainer(List components) {
        RADVisualContainer commonParent = null;
        for (Object comp : components) {
            if (comp instanceof RADVisualComponent) {
                RADVisualContainer parent = ((RADVisualComponent)comp).getParentContainer();
                if (parent == null || (commonParent != null && parent != commonParent)) {
                    return null;
                }
                if (commonParent == null) {
                    commonParent = parent;
                }
            } else {
                return null;
            }
        }
        return commonParent;
    }

    private static PaletteItem[] getAllContainers() {
        ArrayList list = new ArrayList();
        for (PaletteItem item : PaletteUtils.getAllItems()) {
            if (PaletteItem.TYPE_CHOOSE_BEAN.equals(item.getExplicitComponentType())) {
                continue;
            }
            Class cls = item.getComponentClass();
            if (cls != null
                  && JComponent.class.isAssignableFrom(cls)
                  && !MenuElement.class.isAssignableFrom(cls)
                  && FormUtils.isContainer(cls)) {
                list.add(item);
            }
        }
        return (PaletteItem[]) list.toArray(new PaletteItem[list.size()]);
    }

    private static class ContainersMenu extends JMenu {
        private boolean initialized = false;
        private List<RADComponent> components;

        private ContainersMenu(String name, List<RADComponent> components) {
            super(name);
            this.components = components;
        }

        public JPopupMenu getPopupMenu() {
            JPopupMenu popup = super.getPopupMenu();
            if (!initialized) {
                popup.removeAll();
                for (PaletteItem item : getAllContainers()) {
                    JMenuItem mi = new JMenuItem(item.getNode().getDisplayName());
                    HelpCtx.setHelpIDString(mi, EncloseAction.class.getName());                    
                    addSortedMenuItem(popup, mi);
                    mi.addActionListener(new EncloseActionListener(item));
                }
                initialized = true;
            }
            return popup;
        }
        
        private static void addSortedMenuItem(JPopupMenu menu, JMenuItem menuItem) {
            String text = menuItem.getText();
            for (int i = 0; i < menu.getComponentCount(); i++) {
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

        private class EncloseActionListener implements ActionListener {
            private PaletteItem paletteItem;

            EncloseActionListener(PaletteItem paletteItem) {
                this.paletteItem = paletteItem;
            }

            public void actionPerformed(ActionEvent evt) {
                RADVisualContainer metacont = getContainer(components);
                if (metacont != null) {
                    FormModel formModel = metacont.getFormModel();
                    MetaComponentCreator creator = formModel.getComponentCreator();
                    if (metacont.getLayoutSupport() == null) { // free design
                        LayoutModel layoutModel = formModel.getLayoutModel();
                        Object layoutUndoMark = layoutModel.getChangeMark();
                        UndoableEdit layoutEdit = layoutModel.getUndoableEdit();
                        boolean autoUndo = true; // in case of unexpected error, for robustness
                        try {
                            RADVisualContainer newCont = (RADVisualContainer)
                                    creator.createComponent(paletteItem.getComponentClassSource(), metacont, null);
                            layoutModel.removeComponent(newCont.getId(), false); // to be added by LayoutDesigner
//                            creator.moveComponent(newCont, metacont);
                            String[] compIds = new String[components.size()];
                            int i = 0;
                            for (RADComponent metacomp : components) {
                                creator.moveComponent(metacomp, newCont);
                                compIds[i++] = metacomp.getId();
                            }
                            FormEditor.getFormDesigner(formModel).getLayoutDesigner()
                                    .encloseInContainer(compIds, newCont.getId());
                            autoUndo = false;
                        } finally {
                            if (layoutUndoMark != null && !layoutUndoMark.equals(layoutModel.getChangeMark())) {
                                formModel.addUndoableEdit(layoutEdit);
                            }
                            if (autoUndo) {
                                formModel.forceUndoOfCompoundEdit();
                            }
                        }
                    } else { // old layout support
                        // [TBD]
                    }
                }
            }
        }
    }

}
