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
package org.netbeans.modules.collab.ui.actions;

import java.awt.*;
import javax.swing.JPanel;

import org.openide.util.*;
import org.openide.util.actions.NodeAction;
import org.openide.windows.WindowManager;

/**
 *
 *
 * @author Todd Fast <todd.fast@sun.com>
 */
public class TestAction extends NodeAction {
    protected boolean enable(org.openide.nodes.Node[] node) {
        return true;
    }

    public String getName() {
        return "Test";
    }

    protected String iconResource() {
        return "org/openide/resources/actions/empty.gif";
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean asynchronous() {
        return true;
    }

    protected void performAction(org.openide.nodes.Node[] node) {
        System.out.println("----------");

        Container container = WindowManager.getDefault().getMainWindow();
        listChildren(container, "");
    }

    private void listChildren(Container parent, String indent) {
        for (int i = 0; i < parent.getComponentCount(); i++) {
            Component component = parent.getComponent(i);
            System.out.println(indent + component.getClass() /*+" ["+component+"]"*/);

            if (component instanceof Container) {
                if (component.getClass().getName().equals("org.netbeans.core.windows.view.ui.StatusLine")) {
                    JPanel panel = (JPanel) component.getParent();

                    //					System.out.println(component.getParent().toString());
                    //					System.out.println(component.toString());
                }

                listChildren((Container) component, indent + "  ");
            }
        }

        //TabControl
    }
}
