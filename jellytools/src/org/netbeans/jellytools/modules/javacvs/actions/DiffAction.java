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
import org.netbeans.jellytools.actions.Action;

/** Used to call "CVS|Diff..." popup or "Versioning|Diff" main menu item.
 * @see Action
 * @author Jiri.Skrivanek@sun.com
 */
public class DiffAction extends Action {

    /** "Versioning" menu item. */
    private static final String VERSIONING_ITEM = Bundle.getStringTrimmed(
           "org.netbeans.modules.versioning.Bundle", "Menu/Window/Versioning");
    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.Bundle",
            "CTL_MenuItem_CVSCommands_Label");
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
        super(VERSIONING_ITEM+"|"+DIFF_ITEM, CVS_ITEM+"|"+DIFF_POPUP_ITEM);
    }

    /** Performs main menu with exact name.
     * @param filename name of file
     */
    public void performMenu(String filename) {
        String oldMenuPath = this.menuPath;
        // CVS|Branches|Branch "filename"
        this.menuPath = VERSIONING_ITEM+"|"+DIFF_ITEM+"|"+
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

