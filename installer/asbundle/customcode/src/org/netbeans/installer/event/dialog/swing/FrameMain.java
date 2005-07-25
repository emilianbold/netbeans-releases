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

package org.netbeans.installer.event.dialog.swing;


import com.installshield.event.ui.*;
import com.installshield.wizard.swing.*;
import com.installshield.ui.controls.*;

public class FrameMain {

    public void buttonClickedback(ISControlContext context) {
    	
        SwingWizardUI wizardUI = (SwingWizardUI)context.getWizardUI();
        wizardUI.doPrevious();
    }

    public void buttonClickednext(ISControlContext context) {
    	
        SwingWizardUI wizardUI = (SwingWizardUI)context.getWizardUI();
        wizardUI.doNext();
    }

    public void buttonClickedcancel(ISControlContext context) {
    	
        SwingWizardUI wizardUI = (SwingWizardUI)context.getWizardUI();
        wizardUI.doCancel();
    }

    public void initializeUIMain(ISDialogFrameContext context) {
        ISFrame frame = context.getISFrame();

        ISButton nextButton = frame.getButton("next");
        if (nextButton != null) {
            nextButton.setEnabled(true);
        }

        ISButton backButton = frame.getButton("back");
        if (backButton != null) {
            backButton.setEnabled(true);
        }

        ISButton cancelButton = frame.getButton("cancel");
        if (cancelButton != null) {
            cancelButton.setEnabled(true);
        }

    }
}