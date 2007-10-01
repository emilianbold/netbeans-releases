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

import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;

import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteMenuView;
import org.netbeans.modules.form.*;
import org.netbeans.modules.form.project.ClassPathUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 * Action allowing to choose a component from palette content and add it to
 * the selected containers in current form. Presented only in contextual menus
 * within the Form Editor.
 *
 * @author Tomas Pavek
 */

public class AddAction extends CallableSystemAction {

    private static String name;

    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(AddAction.class)
                     .getString("ACT_Add"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddAction.class);
    }

    @Override
    public boolean isEnabled() {
        Node[] nodes = getNodes();
        for (int i=0; i < nodes.length; i++) {
            FormCookie formCookie = nodes[i].getCookie(FormCookie.class);
            if (formCookie == null)
                return false;

            RADComponentCookie radCookie = nodes[i].getCookie(RADComponentCookie.class);
            if (radCookie != null
                  && !(radCookie.getRADComponent() instanceof ComponentContainer))
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
        JMenuItem menu = new PaletteMenuView(
            new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    if (nodes.length != 1)
                        return false;

                    PaletteItem paletteItem = nodes[0].getCookie(PaletteItem.class);
                    if (paletteItem == null)
                        return false;
                    String chooseBeanType = null;
                    if (PaletteItem.TYPE_CHOOSE_BEAN.equals(paletteItem.getExplicitComponentType())) {
                        NotifyDescriptor.InputLine desc = new NotifyDescriptor.InputLine(
                            FormUtils.getBundleString("MSG_Choose_Bean"), // NOI18N
                            FormUtils.getBundleString("TITLE_Choose_Bean")); // NOI18N
                        DialogDisplayer.getDefault().notify(desc);
                        if (NotifyDescriptor.OK_OPTION.equals(desc.getValue())) {
                            chooseBeanType = desc.getInputText();
                        } else {
                            return false;
                        }
                    }

                    nodes = getNodes();
                    if (nodes.length == 0)
                        return false;

                    boolean added = false;

                    for (int i=0; i < nodes.length; i++) {
                        FormCookie formCookie = nodes[i].getCookie(FormCookie.class);
                        if (formCookie == null)
                            continue;

                        RADComponentCookie radCookie = nodes[i].getCookie(RADComponentCookie.class);
                        RADComponent targetComponent;
                        if (radCookie != null) {
                            targetComponent = radCookie.getRADComponent();
                            if (!(targetComponent instanceof ComponentContainer))
                                continue;
                        }
                        else targetComponent = null;

                        FormModel formModel = formCookie.getFormModel();
                        if (chooseBeanType != null) {
                            paletteItem.setComponentClassSource(ClassPathUtils.getProjectClassSource(
                                    FormEditor.getFormDataObject(formModel).getPrimaryFile(), chooseBeanType));
                        }
                        MetaComponentCreator creator = formModel.getComponentCreator();
                        if (creator.canAddComponent(paletteItem.getComponentClass(), targetComponent)
                            && creator.createComponent(paletteItem.getComponentClassSource(),
                                               targetComponent, null) != null) {
                            added = true;
                        }
                    }

                    return added;
                }
            }
        );

        menu.setText(getName());
        menu.setEnabled(isEnabled());
        HelpCtx.setHelpIDString(menu, AddAction.class.getName());
        return menu;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    public void performAction() {
    }

    // -------

    private static Node[] getNodes() {
        // using NodeAction and global activated nodes is not reliable
        // (activated nodes are set with a delay after selection in
        // ComponentInspector)
        return ComponentInspector.getInstance().getExplorerManager().getSelectedNodes();
    }
}
