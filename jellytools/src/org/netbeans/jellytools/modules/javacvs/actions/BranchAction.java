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
package org.netbeans.jellytools.modules.javacvs.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** Used to call "CVS|Branch" popup or "CVS|Branches|Branch main menu item.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class BranchAction extends ActionNoBlock {

    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle", "Menu/CVS");
    // "Branch..."
    private static final String BRANCH_POPUP_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle",
            "CTL_PopupMenuItem_Branch");
    // "Branches"
    private static final String BRANCHES_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
            "CTL_MenuItem_BranchesMenu");
    // "Branch "filename""
    private static final String BRANCH_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle", 
            "CTL_MenuItem_Branch_Context");
    
    
    /** Creates new BranchAction instance. */
    public BranchAction() {
        super(CVS_ITEM+"|"+BRANCHES_ITEM+"|"+BRANCH_ITEM, CVS_ITEM+"|"+BRANCH_POPUP_ITEM);
    }

    /** Performs main menu with exact name.
     * @param filename name of file
     */
    public void performMenu(String filename) {
        String oldMenuPath = this.menuPath;
        // CVS|Branches|Branch "filename"
        this.menuPath = CVS_ITEM+"|"+BRANCHES_ITEM+"|"+
                Bundle.getStringTrimmed("org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle", 
                                        "CTL_MenuItem_Branch_Context", 
                                        new String[] {filename});
        try {
            super.performMenu();
        } finally {
            this.menuPath = oldMenuPath;
        }
    }
}

