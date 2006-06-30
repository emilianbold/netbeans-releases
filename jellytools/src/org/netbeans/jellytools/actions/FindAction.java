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

/** Used to call "Find" popup menu item, "Edit|Find" main menu item,
 * "org.openide.actions.FindAction" or Ctrl+F shortcut.
 * @see Action
 * @see ActionNoBlock
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class FindAction extends ActionNoBlock {
    private static final String findPopup = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Find");
    private static final String findMenu = Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Edit")
    + "|"
    + findPopup;
    private static final Shortcut findShortcut = new Shortcut(KeyEvent.VK_F, KeyEvent.CTRL_MASK);
    
    /** creates new FindAction instance */
    public FindAction() {
        super(findMenu, findPopup, "org.openide.actions.FindAction", findShortcut);
    }
    
    /** Performs action through API. It selects projects node first.
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        new ProjectsTabOperator().tree().selectRow(0);
        super.performAPI();
    }
    
    /** Performs action through shortcut. It selects projects node first.
     * @throws UnsupportedOperationException if no shortcut is defined */
    public void performShortcut() {
        new ProjectsTabOperator().tree().selectRow(0);
        super.performShortcut();
    }

}