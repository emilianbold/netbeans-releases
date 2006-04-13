/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
