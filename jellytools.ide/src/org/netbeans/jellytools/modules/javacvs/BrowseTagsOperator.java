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
package org.netbeans.jellytools.modules.javacvs;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;


/** Class implementing all necessary methods for handling "Browse Tags" dialog.
 * It is opened from Checkout wizard.
 * <br>
 * Usage:<br>
 * <pre>
 *      ModuleToCheckoutStepOperator moduleOper = new ModuleToCheckoutStepOperator();
 *      BrowseTagsOperator browseTagsOper = moduleOper.browseBranch();
 *      browseTagsOper.selectTag("myTag");
 *      browseTagsOper.ok();
 *</pre>
 *
 * @see CheckoutWizardOperator
 * @see ModuleToCheckoutStepOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class BrowseTagsOperator extends NbDialogOperator {

    /** Waits for "Browse Tags" dialog. */
    public BrowseTagsOperator() {
        super(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.selectors.Bundle", 
                "BK2012"));
    }

    private JTreeOperator _tree;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find tree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator tree() {
        if (_tree == null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** Selects a branch or tag denoted by path.
     * @param path path to branch or tag (e.g. "Branches|mybranch")
     */
    public void selectPath(String path) {
        new Node(tree(), path).select();
    }

    /** Selects a branch of given name.
     * @param name name of branch to be selected (e.g. "mybranch")
     */
    public void selectBranch(String name) {
        String branchesLabel = Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.selectors.Bundle",
                "BK2008");
        selectPath(branchesLabel+"|"+name);
    }

    /** Selects a tag of given name.
     * @param name name of tag to be selected (e.g. "mytag")
     */
    public void selectTag(String name) {
        String tagsLabel = Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.selectors.Bundle",
                "BK2009");
        selectPath(tagsLabel+"|"+name);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of BrowseTagsOperator by accessing all its components.
     */
    public void verify() {
        tree();
    }
}
