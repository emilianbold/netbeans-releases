/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle; 

/** Used to call "File|New File..."  main menu item,
 *  or Ctrl+N shortcut.
 * @see Action
 * @see ActionNoBlock
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
    /** Ctrl+N */
    private static final Shortcut shortcut = new Shortcut(KeyEvent.VK_N, KeyEvent.CTRL_MASK);
    
    /** Creates new NewFileAction instance. */
    public NewFileAction() {
        super(menuPath, popupPath + "|" + popupSubPath, null, shortcut);
    }

    /** Create new NewFileAction instance with name of template for
    * popup operation (only popup mode allowed).
    * @param templateName name of template shown in submenu (e.g. "Main")
    */
    public NewFileAction(String templateName) {
        super(null, popupPath+"|"+templateName);
    }
}
