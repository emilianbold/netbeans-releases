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
        //#48249: Check if user is admin on Windows. JDK installer does not run
        //when user does not have admin rights.
        if (Util.isWindowsOS()) {
            if (Util.isAdmin()) {
                setText(resolveString("$L(org.netbeans.installer.Bundle, InstallWelcomePanel.text,"
                + "$L(org.netbeans.installer.Bundle,JDK.name),"
                + "$L(org.netbeans.installer.Bundle,Product.displayName),"
                + "$L(org.netbeans.installer.Bundle,JDK.name),"
                + "$L(org.netbeans.installer.Bundle,Product.displayName))"));
            } else {
                if (Util.isJDKAlreadyInstalled()) {
                    setText(resolveString("$L(org.netbeans.installer.Bundle, InstallWelcomePanel.text,"
                    + "$L(org.netbeans.installer.Bundle,JDK.name),"
                    + "$L(org.netbeans.installer.Bundle,Product.displayName),"
                    + "$L(org.netbeans.installer.Bundle,JDK.name),"
                    + "$L(org.netbeans.installer.Bundle,Product.displayName))"));
                } else {
                    setText(resolveString("$L(org.netbeans.installer.Bundle, InstallWelcomePanel.errorText,"
                    + "$L(org.netbeans.installer.Bundle,JDK.name),"
                    + "$L(org.netbeans.installer.Bundle,Product.displayName),"
                    + "$L(org.netbeans.installer.Bundle,JDK.name))"));
                }
            }
        } else {
            setText(resolveString("$L(org.netbeans.installer.Bundle, InstallWelcomePanel.text,"
            + "$L(org.netbeans.installer.Bundle,JDK.name),"
            + "$L(org.netbeans.installer.Bundle,Product.displayName),"
            + "$L(org.netbeans.installer.Bundle,JDK.name),"
            + "$L(org.netbeans.installer.Bundle,Product.displayName))"));
        }
        return okay;
    }
    
    public boolean entered(WizardBeanEvent event)
    {
        //#48249: Check if user is admin on Windows. JDK installer does not run
        //when user does not have admin rights.
        if (Util.isWindowsOS() && !Util.isAdmin() && !Util.isJDKAlreadyInstalled()) {
            if (event.getUserInterface() instanceof SwingWizardUI) {
                SwingWizardUI ui = (SwingWizardUI) event.getUserInterface();
                //logEvent(this, Log.DBG, "entered ui: " + ui.getClass().getName());
                Component nextButton = ui.getNavigationController().next();
                //logEvent(this, Log.DBG, "entered nextButton: " + nextButton.getClass().getName());
                nextButton.setEnabled(false);
                ui.getNavigationController().setCancelType(SwingWizardUI.NavigationController.CLOSE);
                
                String dialogTitle = resolveString("$L(org.netbeans.installer.Bundle,InstallWelcomePanel.dialogTitle)");
                String dialogMsg = resolveString("$L(org.netbeans.installer.Bundle,NbWelcomePanel.notAdminMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
            }
        }
        return true;
    }
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
}
