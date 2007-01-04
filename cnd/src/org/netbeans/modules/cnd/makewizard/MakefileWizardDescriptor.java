/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package  org.netbeans.modules.cnd.makewizard;

import java.util.Iterator;
import javax.swing.JButton;
import org.openide.WizardDescriptor;

public class MakefileWizardDescriptor extends WizardDescriptor {

    private JButton finishButton;
    private boolean finishEnabled;

    public MakefileWizardDescriptor(Iterator it) {
	super(it);

	finishButton = null;
	finishEnabled = false;
    }


    protected void updateState() {
	super.updateState();

	if (finishButton == null) {
	    finishButton = MakefileWizard.getMakefileWizard().getFinishButton();
	}
	if (finishButton != null) {
	    finishButton.setEnabled(finishEnabled);
	}
    }

    /**
     *  We need to reenable often because each button press disables the
     *  Finish button.
     */
    public void setFinishEnabled(boolean tf) {
	finishEnabled = tf;
	if (finishButton == null) {
	    finishButton = MakefileWizard.getMakefileWizard().getFinishButton();
	}
	finishButton.setEnabled(tf);
    }
}
