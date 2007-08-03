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
package org.netbeans.jellytools.modules.debugger.actions;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.nodes.Node;

/** Used to call "Run|Run File|Debug "MyClass.java"" main menu item,
 * "Debug File" popup menu item or CTRL+Shift+F5 shortcut.
 * @see org.netbeans.jellytools.actions.Action
 * @author Jiri.Skrivanek@sun.com
 */
public class DebugAction extends Action {

    // "Run"
    private static final String runItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject");
    // "Run File"
    private static final String runFileItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Menu/RunProject/RunOther");
    // "Debug File"
    private static final String popupPath = 
            Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_DebugFile_Action");
    private static final KeyStroke keystroke = System.getProperty("os.name").toLowerCase().indexOf("mac") > -1 ?
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.META_MASK|KeyEvent.SHIFT_MASK) :
            KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    
    /** Creates new DebugAction instance. */
    public DebugAction() {
        super(null, popupPath, keystroke);
    }
    
    /** Performs action through main menu. 
     * @param node node to be selected before action
     */
    public void performMenu(Node node) {
        this.menuPath = runItem+"|"+runFileItem+"|"+
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.actions.Bundle",
                                        "LBL_DebugSingleAction_Name",
                                        new Object[] {new Integer(1), node.getText()});
        super.performMenu(node);
    }
}
