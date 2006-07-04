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

import com.installshield.wizard.WizardAction;
import com.installshield.wizard.WizardBeanEvent;
import com.installshield.wizard.WizardBuilderSupport;
import com.installshield.wizard.WizardUI;
import com.installshield.wizard.awt.AWTWizardUI;

import java.awt.Color;
import javax.swing.JComponent;

/** This class is used to customize Wizard UI.
 */
public class CustomizeUIAction extends WizardAction {
    
    public void build (WizardBuilderSupport support) {
        try {
            support.putClass(Util.class.getName());
        } catch (Exception ex) {
            System.out.println(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
    
    public void execute (WizardBeanEvent evt) {
        customizeNavigationButtons();
    }
    
    /** Set navigation button background */
    private void customizeNavigationButtons () {
        WizardUI wizardUI = getWizard().getUI();
        if (wizardUI instanceof AWTWizardUI) {
            AWTWizardUI awtWizardUI = (AWTWizardUI) wizardUI;
            AWTWizardUI.NavigationController controler = awtWizardUI.getNavigationController();
	    if (Util.isMacOSX()) {
		if (controler.back() instanceof JComponent) {
		    ((JComponent) controler.back()).setOpaque(false);
		}
		if (controler.next() instanceof JComponent) {
		    ((JComponent) controler.next()).setOpaque(false);
		}
		if (controler.cancel() instanceof JComponent) {
		    ((JComponent) controler.cancel()).setOpaque(false);
		}
	    } else {
                controler.back().setBackground(Color.lightGray);
	        controler.next().setBackground(Color.lightGray);
	        controler.cancel().setBackground(Color.lightGray);
	    }
        }
    }
}
