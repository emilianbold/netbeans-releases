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

/** Used to call "CVS|Merge Changes from Branch..." popup or
 * "CVS|Branches|Merge Changes from Branch..." main menu item.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class MergeAction extends ActionNoBlock {

    // "CVS"
    private static final String CVS_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.Bundle", "Menu/CVS");
    // "Merge Changes from Branch..."
    private static final String MERGE_POPUP_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
            "CTL_MenuItem_MergeBranch");
    // "Branches"
    private static final String BRANCHES_ITEM = Bundle.getStringTrimmed(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.tag.Bundle",
            "CTL_MenuItem_BranchesMenu");
    
    /** Creates new MergeAction instance. */
    public MergeAction() {
        super(CVS_ITEM+"|"+BRANCHES_ITEM+"|"+MERGE_POPUP_ITEM, CVS_ITEM+"|"+MERGE_POPUP_ITEM);
    }
}

