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

package org.netbeans.installer;

import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.swing.SwingWizardUI;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.wizard.service.WizardServicesUI;

import java.awt.Component;

public class NbWelcomePanel extends TextDisplayPanel
{
    public NbWelcomePanel() {
        setTextSource(TEXT_PROPERTY);
        setContentType(HTML_CONTENT_TYPE);
        setDescription("");
    }
    
    public boolean queryEnter(WizardBeanEvent evt) {
        logEvent(this, Log.DBG, "queryEnter ENTER");
        boolean okay = super.queryEnter(evt);
        String errMessage = "";
        if (Util.isLinuxOS() && isAMD64BitJVM()) {
            setText(resolveString("$L(org.netbeans.installer.Bundle,InstallWelcomePanel.text,"
            + "$L(org.netbeans.installer.Bundle,Product.displayName),"
            + "$L(org.netbeans.installer.Bundle,AS.name))")
            + resolveString("$L(org.netbeans.installer.Bundle,InstallWelcomePanel.error,"
            + "$L(org.netbeans.installer.Bundle,AS.name))"));
        } else {
            setText(resolveString("$L(org.netbeans.installer.Bundle,InstallWelcomePanel.text,"
            + "$L(org.netbeans.installer.Bundle,Product.displayName),"
            + "$L(org.netbeans.installer.Bundle,AS.name))"));
        }
        return okay;
    }
    
    public boolean entered(WizardBeanEvent event)
    {
        if (Util.isLinuxOS() && isAMD64BitJVM()) {
            if (event.getUserInterface() instanceof SwingWizardUI) {
                SwingWizardUI ui = (SwingWizardUI) event.getUserInterface();
                Component nextButton = ui.getNavigationController().next();
                nextButton.setEnabled(false);
                ui.getNavigationController().setCancelType(SwingWizardUI.NavigationController.CLOSE);
            }
        }
        return true;
    }
    
    /** Returns true if it runs on 64bit JVM on Linux/AMD64. We must check also Win 64
     * as we will have access to testing machine with 64bit Windows.
     */
    private boolean isAMD64BitJVM () {
        return System.getProperty("os.arch").startsWith("amd64");
    }
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
}
