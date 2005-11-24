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
import org.netbeans.jellytools.actions.Action;

/** Used to call "CVS|Diff..." popup or "CVS|Diff" main menu item.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class DiffAction extends Action {

    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle", "Menu/CVS");
    // "Diff..."
    private static final String DIFF_POPUP_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle",
            "CTL_PopupMenuItem_Diff");
    // "Diff "filename""
    private static final String DIFF_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
            "CTL_MenuItem_Diff_Context");
    
    
    /** Creates new DiffAction instance. */
    public DiffAction() {
        super(CVS_ITEM+"|"+DIFF_ITEM, CVS_ITEM+"|"+DIFF_POPUP_ITEM);
    }

    /** Performs main menu with exact name.
     * @param filename name of file
     */
    public void performMenu(String filename) {
        String oldMenuPath = this.menuPath;
        // CVS|Branches|Branch "filename"
        this.menuPath = CVS_ITEM+"|"+DIFF_ITEM+"|"+
                Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                "CTL_MenuItem_Diff_Context",
                new String[] {filename});
        try {
            super.performMenu();
        } finally {
            this.menuPath = oldMenuPath;
        }
    }
}

