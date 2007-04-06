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

import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.plaf.PopupMenuUI;
import org.netbeans.modules.form.menu.MenuEditLayer;

/** A custom PopupMenuUI which uses our special hacked popup factory.
 * We use this rather than replacing the global popup factory so that it won't
 * affect NetBeans itself.
 *
 * @author joshua.marinacci@sun.com
 */
class VisualDesignerPopupMenuUI extends PopupMenuUI {
    private final MenuEditLayer layer;

    PopupMenuUI ui;
    public VisualDesignerPopupMenuUI(MenuEditLayer layer, PopupMenuUI ui) {
        this.layer = layer;
        this.ui = ui;
    }
    
    public boolean isPopupTrigger(MouseEvent e) {
        return ui.isPopupTrigger(e);
    }
    public Popup getPopup(JPopupMenu popup, int x, int y) {
        //PopupFactory popupFactory = PopupFactory.getSharedInstance();
        PopupFactory popupFactory = layer.hackedPopupFactory;
        return popupFactory.getPopup(popup.getInvoker(), popup, x, y);
    }
}