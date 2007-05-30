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

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.Popup;

/** A custom Popup container for menu items that doesn't use a real top level component.
 * Instead it uses a JPanel which is lightweight and can be put into the MenuEditLayer
 *
 * @author joshua.marinacci@sun.com
 */
class VisualDesignerJPanelPopup extends Popup {
    private static final boolean DEBUG = false;
    JComponent cont;
    JMenu menu;
    VisualDesignerPopupFactory fact;

    
    public VisualDesignerJPanelPopup(JComponent cont, JMenu menu, VisualDesignerPopupFactory fact) {
        this.cont = cont;
        this.menu = menu;
        this.fact = fact;
    }
    
    // when this menu is shown hide all of the other menus
    public void show() {
        p("VisualDesignerJPanelPopup.show(): hiding other menus");
        // hide all menus except this one
        fact.hideOtherMenus(menu);
        cont.setVisible(true);
    }
    
    public void hide() {
        p("hiding");
    }
    
    private boolean isAncestor(JMenu menu) {
        return fact.canvas.isAncestor(this.menu, menu);
    }
    
    private void p(String string) {
        if(DEBUG) {
            System.out.println(string);
        }
    }
    
}
