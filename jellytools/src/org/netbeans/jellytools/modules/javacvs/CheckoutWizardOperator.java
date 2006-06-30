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
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jellytools.modules.javacvs.actions.CheckoutAction;
import org.netbeans.jemmy.JemmyException;

/** Class implementing all necessary methods for handling "Checkout" wizard.
 * It is opened from main menu CVS|Checkout....
 * <br>
 * Usage:<br>
 * <pre>
 *      CheckoutWizardOperator.invoke();
 *      CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
 *      cvsRootOper.setPassword("password");
 *      cvsRootOper.setCVSRoot(":pserver:user@host:repository");
 *      cvsRootOper.next();
 *      ModuleToCheckoutStepOperator moduleOper = new ModuleToCheckoutStepOperator();
 *      moduleOper.setModule("module");
 *      moduleOper.setBranch("branch");
 *      moduleOper.setLocalFolder("/tmp");
 *      moduleOper.finish();
 * </pre>
 *
 * @see BrowseCVSModuleOperator
 * @see BrowseTagsOperator
 * @see CVSRootStepOperator
 * @see EditCVSRootOperator
 * @see ModuleToCheckoutStepOperator
 * @see ProxyConfigurationOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.CheckoutAction
 * @author Jiri.Skrivanek@sun.com
 */
public class CheckoutWizardOperator extends WizardOperator {
    
    /** Waits for dialog with "Checkout" title. */
    public CheckoutWizardOperator() {
        super(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle",
                "BK0007")
              );
    }
    
    /** Invokes new wizard and returns instance of CheckoutWizardOperator.
     * @return  instance of CheckoutWizardOperator
     */
    public static CheckoutWizardOperator invoke() {
        new CheckoutAction().perform();
        return new CheckoutWizardOperator();
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /**
     * Goes through the wizard and fill supplied parameter. Some of them can be
     * null if not applicable or you want to use defaults.
     * @param cvsRoot CVS root - must not be null
     * @param password password - can be null
     * @param module module name - can be null
     * @param branch branch - can be null
     * @param localFolder local folder path - can be null
     */
    public void doCheckout(String cvsRoot, String password, String module, String branch, String localFolder) {
        CheckoutWizardOperator.invoke();
        CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
        if(password != null) {
            cvsRootOper.setPassword(password);
        }
        if(cvsRoot == null) {
            throw new JemmyException("CVS root must not be null."); // NOI18N
        }
        cvsRootOper.setCVSRoot(cvsRoot);
        cvsRootOper.next();
        ModuleToCheckoutStepOperator moduleOper = new ModuleToCheckoutStepOperator();
        if(module != null) {
            moduleOper.setModule(module);
        }
        if(branch != null) {
            moduleOper.setBranch(branch);
        }
        if(localFolder != null) {
            moduleOper.setLocalFolder(localFolder);
        }
        moduleOper.finish();
    }
}
