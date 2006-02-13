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
import com.installshield.wizard.*;

public class FrameFinish {
    public void buttonClickedfinish(ISControlContext context) {
        
        //determine if dialog is actually the last in the sequence
        //call doNext() if not last
        boolean isLast = false;
        try {
            WizardBean nextBean =
                context.getWizard().getIterator().getNext(
                    context.getWizard().getCurrentBean());
            if (nextBean == context.getWizard().getIterator().end()) {
                isLast = true;
            }
        }
        catch (Exception e) {
        }

        SwingWizardUI wizardUI = (SwingWizardUI)context.getWizardUI();
		wizardUI.doNext();
        if (isLast)
            wizardUI.finish(false);
    }

    public void initializeUIFinish(ISDialogFrameContext context) {

        ISFrame frame = context.getISFrame();

        ISButton finishButton = frame.getButton("finish");
        if (finishButton != null) {
            finishButton.setEnabled(true);
        }

    }

}