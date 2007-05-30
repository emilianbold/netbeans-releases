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

package org.netbeans.modules.autoupdate.ui;

import java.awt.Dialog;
import java.text.MessageFormat;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizardIterator;
import org.netbeans.modules.autoupdate.ui.wizards.InstallUnitWizardModel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallUnitWizard {
    /** Creates a new instance of InstallUnitWizard */
    public InstallUnitWizard () {}
    
    public boolean invokeWizard (OperationContainer<InstallSupport> container) {
        assert container != null : "The OperationContainer<InstallSupport> must exist!";
        InstallUnitWizardModel model = new InstallUnitWizardModel (container);
        WizardDescriptor.Iterator<WizardDescriptor> iterator = new InstallUnitWizardIterator (model);
        WizardDescriptor wizardDescriptor = new WizardDescriptor (iterator);
        wizardDescriptor.setModal (true);
        
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wizardDescriptor.setTitleFormat (new MessageFormat(NbBundle.getMessage (InstallUnitWizard.class, "InstallUnitWizard_MessageFormat")));
        wizardDescriptor.setTitle (NbBundle.getMessage (InstallUnitWizard.class, "InstallUnitWizard_Title"));
        
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        dialog.setVisible (true);
        dialog.toFront ();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        //TODO: must be fixed to return true if the wizard was properly finished
        return !cancelled;
    }
    
}
