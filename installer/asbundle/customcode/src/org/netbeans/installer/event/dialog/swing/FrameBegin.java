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

package org.netbeans.installer.event.dialog.swing;


import com.installshield.event.ui.*;
import com.installshield.wizard.swing.*;
import com.installshield.ui.controls.*;

public class FrameBegin {

    public void buttonClickednext(ISControlContext context) {
        	
        SwingWizardUI wizardUI = (SwingWizardUI)context.getWizardUI();
        wizardUI.doNext();
    }

    public void buttonClickedcancel(ISControlContext context) {
    	
        SwingWizardUI wizardUI = (SwingWizardUI)context.getWizardUI();
        wizardUI.doCancel();

    }

    public void initializeUIBegin(ISDialogFrameContext context) {
        ISFrame frame = context.getISFrame();

        ISButton nextButton = frame.getButton("next");
        if (nextButton != null) {
            nextButton.setEnabled(true);
        }

        ISButton cancelButton = frame.getButton("cancel");
        if (cancelButton != null) {
            cancelButton.setEnabled(true);
        }

    }
}
