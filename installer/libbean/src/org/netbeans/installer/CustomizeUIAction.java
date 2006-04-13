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
