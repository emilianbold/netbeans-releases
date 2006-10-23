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
import org.netbeans.jellytools.ProjectsTabOperator;

/** Used to call "Edit|Replace" main menu item,
 * "org.openide.actions.ReplaceAction" or Ctrl+H shortcut.
 * @see Action
 * @see ActionNoBlock
 * @author Roman Strobl
 */
public class ReplaceAction extends ActionNoBlock {
    // "Edit|Replace..."
    private static final String replaceMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
                                              + "|"
                                              + Bundle.getStringTrimmed("org.openide.actions.Bundle", "Replace");
    
    /** creates new ReplaceAction instance */
    public ReplaceAction() {
        super(replaceMenu, null, "org.openide.actions.ReplaceAction");
    }
}
