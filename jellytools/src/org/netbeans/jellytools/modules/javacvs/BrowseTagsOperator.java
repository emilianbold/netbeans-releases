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