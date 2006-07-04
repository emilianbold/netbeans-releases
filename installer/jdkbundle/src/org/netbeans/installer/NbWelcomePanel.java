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

import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.swing.SwingWizardUI;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.wizard.service.WizardServicesUI;

import java.awt.Component;

public class NbWelcomePanel extends TextDisplayPanel {
    
    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";
    
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
        if (isJDKVersionSupported()) {
            if (Util.isWindowsOS()) {
                if (Util.isAdmin()) {
                    setText(resolveString(BUNDLE + "InstallWelcomePanel.text,"
                    + BUNDLE + "JDK.name),"
                    + BUNDLE + "Product.displayName),"
                    + BUNDLE + "JDK.name),"
                    + BUNDLE + "Product.displayName))"));
                } else {
                    if (Util.isJDKAlreadyInstalled()) {
                        setText(resolveString(BUNDLE + "InstallWelcomePanel.text,"
                        + BUNDLE + "JDK.name),"
                        + BUNDLE + "Product.displayName),"
                        + BUNDLE + "JDK.name),"
                        + BUNDLE + "Product.displayName))"));
                    } else {
                        setText(resolveString(BUNDLE + "InstallWelcomePanel.errorText,"
                        + BUNDLE + "JDK.name),"
                        + BUNDLE + "Product.displayName),"
                        + BUNDLE + "JDK.name))"));
                    }
                }
            } else {
                setText(resolveString(BUNDLE + "InstallWelcomePanel.text,"
                + BUNDLE + "JDK.name),"
                + BUNDLE + "Product.displayName),"
                + BUNDLE + "JDK.name),"
                + BUNDLE + "Product.displayName))"));
            }
        } else {
            setText(resolveString(BUNDLE + "InstallWelcomePanel.error,"
            + System.getProperty("java.version") + ","
            + BUNDLE + "InstallWelcomePanel.jdkURL))"));
        }
        return okay;
    }
    
    public boolean entered (WizardBeanEvent event) {
        if (!isJDKVersionSupported()) {
            if (event.getUserInterface() instanceof SwingWizardUI) {
                SwingWizardUI ui = (SwingWizardUI) event.getUserInterface();
                Component nextButton = ui.getNavigationController().next();
                nextButton.setEnabled(false);
                ui.getNavigationController().setCancelType(SwingWizardUI.NavigationController.CLOSE);
            }
        } else if (Util.isWindowsOS() && !Util.isAdmin() && !Util.isJDKAlreadyInstalled()) {
        //#48249: Check if user is admin on Windows. JDK installer does not run
        //when user does not have admin rights.
            if (event.getUserInterface() instanceof SwingWizardUI) {
                SwingWizardUI ui = (SwingWizardUI) event.getUserInterface();
                //logEvent(this, Log.DBG, "entered ui: " + ui.getClass().getName());
                Component nextButton = ui.getNavigationController().next();
                //logEvent(this, Log.DBG, "entered nextButton: " + nextButton.getClass().getName());
                nextButton.setEnabled(false);
                ui.getNavigationController().setCancelType(SwingWizardUI.NavigationController.CLOSE);
                
                String dialogTitle = resolveString(BUNDLE + "InstallWelcomePanel.dialogTitle)");
                String dialogMsg = resolveString(BUNDLE + "NbWelcomePanel.notAdminMessage)");
                showErrorMsg(dialogTitle,dialogMsg);
            }
        }
        return true;
    }
    
    private boolean isJDKVersionSupported () {
        String javaSpecVer = System.getProperty("java.specification.version");
        logEvent(this,Log.DBG,"java.specification.version: " + javaSpecVer);
        if ("1.4".compareTo(javaSpecVer) >= 0) {
            return false;
        } else {
            return true;
        }
    }
    
    protected void showErrorMsg(String title, String msg) {
        try {
            getWizard().getServices().displayUserMessage(title, msg, WizardServicesUI.ERROR);
        } catch (Exception e) {
            throw new Error();
        }
    }
}
