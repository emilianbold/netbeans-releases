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
import org.netbeans.jellytools.RepositoryTabOperator;

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
    
    /** Performs action through API. It selects Filesystems node first.
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        new RepositoryTabOperator().getRootNode().select();
        super.performAPI();
    }
    
    /** Performs action through shortcut. It selects Filesystems node first.
     * @throws UnsupportedOperationException if no shortcut is defined */
    public void performShortcut() {
        new RepositoryTabOperator().getRootNode().select();
        super.performShortcut();
    }

}