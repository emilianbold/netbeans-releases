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
import org.netbeans.jellytools.Bundle;

/** Used to call "File|New Project..."  main menu item, "org.netbeans.modules.project.ui.actions.NewProject" action
 *  or Ctrl+Shift+N shortcut.<br>
 * Usage:
 * <pre>
 *  new NewProjectAction.performMenu();
 *  new NewProjectAction().performShortcut();
 * </pre>
 * @see Action
 * @see ActionNoBlock
 * @author tb115823
 */
public class NewProjectAction extends ActionNoBlock {

    /** File|New Project..." main menu path. */
    private  static final String menuPath = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File")
                                            + "|"
                                            + Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_NewProjectAction_Name");
    
    /** Creates new NewProjectAction instance. */
    public NewProjectAction() {
        super(menuPath, null, "org.netbeans.modules.project.ui.actions.NewProject");
    }
    
}
