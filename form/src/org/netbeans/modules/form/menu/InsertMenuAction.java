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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.form.menu;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.SwingUtilities;
import org.netbeans.modules.form.RADComponent;
import org.netbeans.modules.form.RADComponentCookie;
import org.netbeans.modules.form.palette.PaletteItem;
import org.netbeans.modules.form.palette.PaletteUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * An action which adds a JMenu to a JMenuBar
 * @author Joshua Marinacci
 */
public class InsertMenuAction extends NodeAction {

    private static String name;
    
    public String getName() {
        if (name == null) 
            name = org.openide.util.NbBundle.getBundle(InsertMenuAction.class)
                     .getString("ACT_InsertMenu"); // NOI18N/
        return name;
    }

    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes != null && activatedNodes.length == 1) {
            RADComponentCookie radCookie = (RADComponentCookie)activatedNodes[0]
                                            .getCookie(RADComponentCookie.class);
            final RADComponent metacomp = radCookie == null ? null :
                                      radCookie.getRADComponent();
            
            //find the first JMenuBar item in the palette
            PaletteItem[] items = PaletteUtils.getAllItems();
            for (PaletteItem item : items) {
                Class clazz = item.getComponentClass();
                if (clazz != null && JMenu.class.isAssignableFrom(clazz)) {
                    final PaletteItem it = item;
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            MenuEditLayer.addComponentToEndOfMenu(metacomp, it);
                        }
                    });
                    return;
                }
            }
        }
    }

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx("gui.containers.designing"); // NOI18N
    }
}
