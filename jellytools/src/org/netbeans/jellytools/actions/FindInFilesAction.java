/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.awt.event.KeyEvent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.operators.ComponentOperator;

/** Used to call "Find" popup menu item, "Edit|Find in Projects" main menu item,
 * "org.netbeans.modules.search.project.ProjectsSearchAction" or Ctrl+Shift+P shortcut.
 * @see Action
 * @see ActionNoBlock
 */
 public class FindInFilesAction extends ActionNoBlock {
    private static final String menu =
        Bundle.getStringTrimmed("org.netbeans.core.Bundle",
                                "Menu/Edit") +
        "|" +
        Bundle.getStringTrimmed("org.netbeans.modules.search.project.Bundle",
                                "LBL_SearchProjects");
    private static final String popup =
        Bundle.getStringTrimmed("org.openide.actions.Bundle",
                                "Find");
    private static final Shortcut shortcut =
        new Shortcut(KeyEvent.VK_P, KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK);
    
    /** Creates new instance. */
    public FindInFilesAction() {
        super(menu, popup, "org.netbeans.modules.search.project.ProjectsSearchAction", shortcut);
    }
    
    /** Throws UnsupportedOperationException because FindInFilesAction
     * doesn't have shortcut representation on every component but only
     * on nodes. Use {@link #performShortcut(Node)}
     * or {@link #performShortcut(Node[])} instead.
     * @param component ComponentOperator instance
     */
    public void performShortcut(ComponentOperator component) {
        throw new UnsupportedOperationException("FindInFilesAction has shortcut representation only on nodes.");
    }
    
    /** Performs action through API. It selects a node first.
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        new ProjectsTabOperator().tree().selectRow(0);
        super.performAPI();
    }
    
    /** Performs action through shortcut. It selects a node first.
     * @throws UnsupportedOperationException if no shortcut is defined */
    public void performShortcut() {
        new ProjectsTabOperator().tree().selectRow(0);
        super.performShortcut();
    }
}
