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
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;

/** Used to call "Help|Help Contents" main menu item,
 * or F1 shortcut. It can also be used
 * to invoke help on a property sheet from popup menu.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class HelpAction extends Action {

    // String used in property sheets
    private static final String popupPath = Bundle.getString("org.openide.explorer.propertysheet.Bundle", "CTL_Help");
    private static final String helpMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Help")
                                         + "|" 
                                         + Bundle.getStringTrimmed("org.netbeans.modules.usersguide.Bundle", "Menu/Help/org-netbeans-modules-usersguide-master.xml");
    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0);

    /** Creates new HelpAction instance for master help set (Help|Contents)
     * or for generic use e.g. in property sheets.
     */
    public HelpAction() {
        super(helpMenu, popupPath, keystroke);
    }
}
