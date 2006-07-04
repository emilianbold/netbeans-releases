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

package org.netbeans.installer.cluster;

import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.swing.SwingWizardUI;
import com.installshield.wizardx.panels.TextDisplayPanel;
import com.installshield.util.Log;

import java.awt.Component;

import org.netbeans.installer.Util;

public class NbWelcomePanel extends TextDisplayPanel {
    
    private static final String BUNDLE = "$L(org.netbeans.installer.cluster.Bundle,";
    
    public NbWelcomePanel() {
        setTextSource(TEXT_PROPERTY);
        setContentType(HTML_CONTENT_TYPE);
        setDescription("");
    }
    
    public boolean queryEnter(WizardBeanEvent evt) {
        boolean okay = super.queryEnter(evt);
        if (isJDKVersionSupported()) {
            setText(resolveString(BUNDLE + "InstallWelcomePanel.text)"));
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
