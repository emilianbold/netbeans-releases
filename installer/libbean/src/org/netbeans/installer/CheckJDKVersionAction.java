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

package org.netbeans.installer;

import com.installshield.util.Log;
import com.installshield.wizard.WizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.service.WizardServicesUI;

/** 
 * Checks if installer runs on supported JDK version. JDK 1.5 and later
 * is supported.
 */
public class CheckJDKVersionAction extends WizardAction {
    
    public void build (WizardBuilderSupport support) {
    }
    
    public void execute (WizardBeanEvent evt) {
        checkJDKVersion();
    }
    
    /** Set navigation button background */
    private void checkJDKVersion () {
        String javaSpecVer = System.getProperty("java.specification.version");
        logEvent(this,Log.DBG,"java.specification.version: " + javaSpecVer);
        if ("1.4".compareTo(javaSpecVer) >= 0) {
            try {
                String msg = resolveString("$L(org.netbeans.installer.Bundle,InstallWizard.UnsupportedJDKVersion)");
                String title = resolveString("$L(org.netbeans.installer.Bundle,InstallWizard.title)");
                getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
            } catch (Exception e) {
                throw new Error();
            }
            getWizard().exit(1,"ERROR: Unsupported JDK version " + System.getProperty("java.version") + "\n");
        }
    }

}
