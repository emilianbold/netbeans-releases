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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.etl.ui.view.wizards;

import org.openide.WizardDescriptor;

/**
 * Extends name input panel by implementing the WizardDescriptor.FinishablePanel interface to
 * allow successful closure of the wizard from this panel.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class ETLCollaborationWizardNameFinishPanel extends ETLCollaborationWizardNamePanel implements WizardDescriptor.FinishablePanel {

    /**
     * No-arg constructor for this wizard descriptor.
     */
    public ETLCollaborationWizardNameFinishPanel() {
        super();
    }

    /**
     * Create the wizard finish panel descriptor, using the given owner and panel title.
     * 
     * @param myOwner ETLWizard that owns this panel
     * @param panelTitle text to display as panel title
     */
    public ETLCollaborationWizardNameFinishPanel(ETLCollaborationWizard myOwner, String panelTitle) {
        super(myOwner, panelTitle);
    }

    public boolean isFinishPanel() {
        return true;
    }
}

