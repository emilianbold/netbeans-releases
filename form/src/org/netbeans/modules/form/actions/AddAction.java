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

import javax.swing.*;

import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.*;

import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteMenuView;
import org.netbeans.modules.form.*;

/**
 * @author Tomas Pavek
 */

public class AddAction extends NodeAction {

    private static String name;
    private static String menuText;

    public String getName() {
        if (name == null)
            name = org.openide.util.NbBundle.getBundle(AddAction.class)
                     .getString("ACT_Add"); // NOI18N
        return name;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddAction.class);
    }

    protected void performAction(Node[] activatedNodes) {
    }

    protected boolean enable(Node[] activatedNodes) {
        for (int i=0; i < activatedNodes.length; i++) {
            Node node = activatedNodes[i];
            
            FormCookie formCookie =
                (FormCookie) node.getCookie(FormCookie.class);
            if (formCookie == null)
                return false;

            RADComponentCookie radCookie =
                (RADComponentCookie) node.getCookie(RADComponentCookie.class);
            if (radCookie != null
                    && !(radCookie.getRADComponent() instanceof ComponentContainer))
                return false;
        }
        return true;
    }

    public JMenuItem getMenuPresenter() {
        return getPopupPresenter();
    }

    public JMenuItem getPopupPresenter() {
        JMenuItem menu = new PaletteMenuView(
            new NodeAcceptor() {
                public boolean acceptNodes(Node[] nodes) {
                    if (nodes.length == 0)
                        return false;

                    PaletteItem paletteItem = (PaletteItem)
                                         nodes[0].getCookie(PaletteItem.class);
                    if (paletteItem == null)
                        return false;

                    Node[] activatedNodes = getActivatedNodes();
                    if (activatedNodes.length == 0)
                        return false;

                    boolean added = false;

                    for (int i=0; i < activatedNodes.length; i++) {
                        Node node = activatedNodes[i];
            
                        FormCookie formCookie = (FormCookie)
                            node.getCookie(FormCookie.class);
                        if (formCookie == null)
                            continue;

                        RADComponentCookie radCookie = (RADComponentCookie)
                            node.getCookie(RADComponentCookie.class);
                        RADComponent targetComponent;
                        if (radCookie != null) {
                            targetComponent = radCookie.getRADComponent();
                            if (!(targetComponent instanceof ComponentContainer))
                                continue;
                        }
                        else targetComponent = null;

                        RADComponent newComp =
                            formCookie.getFormModel().getComponentCreator()
                                .createComponent(paletteItem, targetComponent, null);

                        if (newComp != null)
                            added = true;
                    }

                    return added;
                }
            }
        );

        if (menuText == null)
            menuText = org.openide.util.NbBundle.getBundle(AddAction.class)
                         .getString("ACT_Add"); // NOI18N
        menu.setText(menuText);
        menu.setIcon(null);

        return menu;
    }
}
