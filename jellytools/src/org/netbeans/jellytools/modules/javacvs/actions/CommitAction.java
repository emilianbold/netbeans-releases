/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.modules.javacvs.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** Used to call "CVS|Commit..." popup or "CVS|Commit..." main menu item.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class CommitAction extends ActionNoBlock {

    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle", "Menu/CVS");
    // "Commit..."
    private static final String COMMIT_POPUP_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle",
            "CTL_PopupMenuItem_Commit");
    // "Commit "filename"..."
    private static final String COMMIT_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.commit.Bundle",
            "CTL_MenuItem_Commit_Context");
    
    /** Creates new CommitAction instance. */
    public CommitAction() {
        super(CVS_ITEM+"|"+COMMIT_ITEM, CVS_ITEM+"|"+COMMIT_POPUP_ITEM);
    }
    
    /** Performs main menu with exact file name.
     * @param filename file name
     */
    public void performMenu(String filename) {
        String oldMenuPath = this.menuPath;
        // CVS|Commit "filename"...
        this.menuPath = CVS_ITEM+"|"+
            Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.commit.Bundle",
                "CTL_MenuItem_Commit_Context",
                 new String[] {filename});
        try {
            super.performMenu();
        } finally {
            this.menuPath = oldMenuPath;
        }
    }
}

