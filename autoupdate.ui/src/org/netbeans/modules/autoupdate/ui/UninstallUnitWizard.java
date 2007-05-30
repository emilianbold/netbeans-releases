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
import org.netbeans.modules.autoupdate.ui.wizards.OperationWizardModel;
import org.netbeans.modules.autoupdate.ui.wizards.UninstallUnitWizardIterator;
import org.netbeans.modules.autoupdate.ui.wizards.UninstallUnitWizardModel;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Jiri Rechtacek
 */
public class UninstallUnitWizard {
    
    /** Creates a new instance of InstallUnitWizard */
    public UninstallUnitWizard () {}
    
    public boolean invokeWizard () {
        return invokeWizardImpl (true, null);
    }
    
    public boolean invokeWizard (boolean doEnable) {
        return invokeWizardImpl (null, doEnable ? Boolean.TRUE : Boolean.FALSE);
    }
    
    private boolean invokeWizardImpl (Boolean doUninstall, Boolean doEnable) {
        assert doUninstall != null || doEnable != null : "At least one action is enabled";
        assert ! (doUninstall != null && doEnable != null) : "Only once action is enabled";
        assert doUninstall == null || Containers.forUninstall () != null : "The OperationContainer<OperationSupport> forUninstall must exist!";
        assert doUninstall != null || !doEnable || (doEnable && Containers.forEnable () != null) : "The OperationContainer<OperationSupport> forEnable must exist!";
        assert doUninstall != null || doEnable || (! doEnable && Containers.forDisable () != null) : "The OperationContainer<OperationSupport> forDisable must exist!";
        
        UninstallUnitWizardModel model = new UninstallUnitWizardModel (doUninstall != null
                ? OperationWizardModel.OperationType.UNINSTALL : doEnable ? OperationWizardModel.OperationType.ENABLE : OperationWizardModel.OperationType.DISABLE);
        WizardDescriptor.Iterator<WizardDescriptor> iterator = new UninstallUnitWizardIterator (model);
        WizardDescriptor wizardDescriptor = new WizardDescriptor (iterator);
        wizardDescriptor.setModal (true);
        
        // {0} will be replaced by WizardDescriptor.Panel.getComponent().getName()
        // {1} will be replaced by WizardDescriptor.Iterator.name()
        wizardDescriptor.setTitleFormat (new MessageFormat("{1}"));
        wizardDescriptor.setTitle (NbBundle.getMessage (UninstallUnitWizard.class, "UninstallUnitWizard_Title"));
        
        Dialog dialog = DialogDisplayer.getDefault ().createDialog (wizardDescriptor);
        dialog.setVisible (true);
        dialog.toFront ();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        return !cancelled;
    }
    
}
