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

/** Used to call "Build|Compile File" main menu item, "Compile File" popup menu or F9 shortcut.
 * @see Action
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 */
public class CompileAction extends Action {

    // Build|Compile
    private static final String compileMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Build")+"|"
                                            +Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_CompileSingleAction_Name");
    private static final KeyStroke keystroke = KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0);
    // Compile File
    private static final String compilePopup = Bundle.getString("org.netbeans.modules.java.project.Bundle", "LBL_CompileFile_Action");
    
    /** creates new CompileAction instance */    
    public CompileAction() {
        super(compileMenu, compilePopup, keystroke);
    }
}
