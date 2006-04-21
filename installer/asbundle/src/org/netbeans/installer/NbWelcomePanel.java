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

import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;
import com.installshield.wizard.swing.SwingWizardUI;

import java.awt.Component;

public class NbWelcomePanel extends TextDisplayPanel {
    
    private static final String BUNDLE = "$L(org.netbeans.installer.Bundle,";
    
    public NbWelcomePanel() {
        setTextSource(TEXT_PROPERTY);
        setContentType(HTML_CONTENT_TYPE);
        setDescription("");
    }
    
    public boolean queryEnter(WizardBeanEvent evt) {
        boolean okay = super.queryEnter(evt);
        if (isJDKVersionSupported()) {
            setText(resolveString(BUNDLE + "InstallWelcomePanel.text,"
            + BUNDLE + "Product.displayName),"
            + BUNDLE + "AS.name))"));
        } else {
            if (Util.isMacOSX()) {
                setText(resolveString(BUNDLE + "InstallWelcomePanel.error,"
                + System.getProperty("java.version") + ","
                + BUNDLE + "InstallWelcomePanel.jdkURLMacOSX))"));
            } else {
                setText(resolveString(BUNDLE + "InstallWelcomePanel.error,"
                + System.getProperty("java.version") + ","
                + BUNDLE + "InstallWelcomePanel.jdkURL))"));
            }
        }
        return okay;
    }
    
    public boolean entered(WizardBeanEvent event) {
        if (!isJDKVersionSupported()) {
            if (event.getUserInterface() instanceof SwingWizardUI) {
                SwingWizardUI ui = (SwingWizardUI) event.getUserInterface();
                Component nextButton = ui.getNavigationController().next();
                nextButton.setEnabled(false);
                ui.getNavigationController().setCancelType(SwingWizardUI.NavigationController.CLOSE);
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
    
}
