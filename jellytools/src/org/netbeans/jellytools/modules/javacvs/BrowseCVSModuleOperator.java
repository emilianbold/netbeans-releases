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
package org.netbeans.jellytools.modules.javacvs;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Class implementing all necessary methods for handling "Browse CVS Module" dialog.
 * It is open from Checkout Wizard.
 * <br>
 * Usage:<br>
 * <pre>
 *      ModuleToCheckoutStepOperator moduleOper = new ModuleToCheckoutStepOperator();
 *      BrowseCVSModuleOperator browseModuleOper = moduleOper.browseModule();
 *      browseModuleOper.selectModule("/repository|module");
 *      browseModuleOper.ok();
 * </pre>
 * 
 * 
 * @author Jiri.Skrivanek@sun.com
 * @see CheckoutWizardOperator
 * @see ModuleToCheckoutStepOperator
 */
public class BrowseCVSModuleOperator extends NbDialogOperator {
    
    /** Waits for "Browse CVS Module" dialog. */
    public BrowseCVSModuleOperator() {
        super(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.selectors.Bundle", "BK2019"));
    }
    
    private JTreeOperator _tree;
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find tree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator tree() {
        if (_tree==null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** Selects a module denoted by path.
     * @param path path to module (e.g. "/cvs|mymodule")
     */
    public void selectModule(String path) {
        new Node(tree(), path).select();
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /**
     * Performs verification of BrowseCVSModuleOperator by accessing all its components.
     */
    public void verify() {
        tree();
    }
}

