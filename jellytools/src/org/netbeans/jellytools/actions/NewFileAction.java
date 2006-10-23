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

/** Used to call "File|New File..."  main menu item, "New|File/Folder" popup menu item,
 * "org.netbeans.modules.project.ui.actions.NewFile" action
 * or Ctrl+N shortcut.<br>
 * Usage:
 * <pre>
 *    new NewFileAction().performMenu();
 *    new NewFileAction().performPopup();
 *    new NewFileAction().performShortcut();
 * </pre>
 * @see Action
 * @see ActionNoBlock
 * @author tb115823
 */
public class NewFileAction extends ActionNoBlock {
    
    /** "New" popup menu item. */
    private static final String popupPath = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_PopupName");
    
    /** "File..." popup menu sub item. */
    private static final String popupSubPath = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_File_PopupName");
        
    /** File|New File..." main menu path. */
    private  static final String menuPath = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File")
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewFileAction_Name");
    
    /** Creates new NewFileAction instance. */
    public NewFileAction() {
        super(menuPath, popupPath + "|" + popupSubPath, "org.netbeans.modules.project.ui.actions.NewFile");
    }

    /** Create new NewFileAction instance with name of template for
    * popup operation (only popup mode allowed).
    * @param templateName name of template shown in submenu (e.g. "Java Main Class")
    */
    public NewFileAction(String templateName) {
        super(null, popupPath+"|"+templateName);
    }
}
