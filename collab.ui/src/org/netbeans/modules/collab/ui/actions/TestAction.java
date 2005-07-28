/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.actions;

import org.openide.*;
import org.openide.awt.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.util.*;

import javax.swing.*;

//import org.netbeans.core.windows.view.ui.tabcontrol.*;
import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.ui.*;


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
                if (component instanceof org.openide.awt.ToolbarPool) {
                    System.out.println(component.getParent().toString());
                    System.out.println(component.toString());
                }

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
