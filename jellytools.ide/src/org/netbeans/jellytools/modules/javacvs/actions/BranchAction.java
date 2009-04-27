/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools.modules.javacvs.actions;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.actions.ActionNoBlock;

/** Used to call "CVS|Branch" popup or "Versioning|Branches|Branch main menu item.
 * @see ActionNoBlock
 * @author Jiri.Skrivanek@sun.com
 */
public class BranchAction extends ActionNoBlock {

    /** "Versioning" menu item. */
    private static final String VERSIONING_ITEM = Bundle.getStringTrimmed(
           "org.netbeans.modules.versioning.Bundle", "Menu/Versioning");
    // "CVS"
    private static final String CVS_ITEM = Bundle.getString(
            "org.netbeans.modules.versioning.system.cvss.ui.actions.Bundle",
            "CTL_MenuItem_CVSCommands_Label");
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
        super(VERSIONING_ITEM+"|"+BRANCHES_ITEM+"|"+BRANCH_ITEM, CVS_ITEM+"|"+BRANCH_POPUP_ITEM);
    }

    /** Performs main menu with exact name.
     * @param filename name of file
     */
    public void performMenu(String filename) {
        String oldMenuPath = this.menuPath;
        // CVS|Branches|Branch "filename"
        this.menuPath = VERSIONING_ITEM+"|"+BRANCHES_ITEM+"|"+
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

