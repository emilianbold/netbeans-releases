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

/** Used to call "CVS|Switch to Branch..." popup or "CVS|Branches|Switch to Branch" main menu item.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class SwitchToBranchAction extends ActionNoBlock {

    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle", "Menu/CVS");
    // "Switch to Branch..."
    private static final String SWITCH_TO_BRANCH_POPUP_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
            "CTL_MenuItem_SwitchBranch");
    // "Branches"
    private static final String BRANCHES_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
            "CTL_MenuItem_BranchesMenu");
    // "Switch "filename" to Branch..."
    private static final String SWITCH_TO_BRANCH_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
            "CTL_MenuItem_SwitchBranch_Context");
    
    
    /** Creates new SwitchToBranchAction instance. */
    public SwitchToBranchAction() {
        super(CVS_ITEM+"|"+BRANCHES_ITEM+"|"+SWITCH_TO_BRANCH_ITEM, CVS_ITEM+"|"+SWITCH_TO_BRANCH_POPUP_ITEM);
    }

    /** Performs main menu with exact file name.
     * @param filename file name
     */
    public void performMenu(String filename) {
        String oldMenuPath = this.menuPath;
        // CVS|Branches|Branch "filename"
        this.menuPath = CVS_ITEM+"|"+BRANCHES_ITEM+"|"+
                Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
                                        "CTL_MenuItem_SwitchBranch_Context",
                                        new String[] {filename});
        try {
            super.performMenu();
        } finally {
            this.menuPath = oldMenuPath;
        }
    }
}

