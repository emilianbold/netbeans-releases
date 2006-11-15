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
 *
 * $Id$
 */
package org.netbeans.installer.wizard.components.sequences;

import java.util.List;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.ProductComponent;
import org.netbeans.installer.product.ProductRegistry;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.*;
import org.netbeans.installer.wizard.components.actions.CreateRedistributableBundleAction;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.actions.InstallAction;
import org.netbeans.installer.wizard.components.actions.UninstallAction;
import org.netbeans.installer.wizard.components.panels.PostCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PostInstallSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PreCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PreInstallSummaryPanel;
import org.netbeans.installer.wizard.components.panels.SelectedComponentsLicensesPanel;

/**
 *
 * @author ks152834
 */
public class MainSequence extends WizardSequence {
    public void executeForward(final Wizard wizard) {
        List<ProductComponent> componentsToInstall = ProductRegistry.getInstance().getComponentsToInstall();
        List<ProductComponent> componentsToUninstall = ProductRegistry.getInstance().getComponentsToUninstall();
        
        removeAllChildren();
        
        switch (Installer.getInstance().getExecutionMode()) {
            case NORMAL:
                if (componentsToInstall.size() > 0) {
                    addChild(new DownloadConfigurationLogicAction());
                    addChild(new SelectedComponentsLicensesPanel());
                    
                    for (int i = 0; i < componentsToInstall.size(); i++) {
                        addChild(new ProductComponentWizardSequence(componentsToInstall.get(i)));
                    }
                }
                
                addChild(new PreInstallSummaryPanel());
                
                if (componentsToUninstall.size() > 0) {
                    addChild(new UninstallAction());
                }
                
                if (componentsToInstall.size() > 0) {
                    addChild(new DownloadInstallationDataAction());
                    addChild(new InstallAction());
                }
                
                addChild(new PostInstallSummaryPanel());
                break;
            case CREATE_BUNDLE:
                addChild(new PreCreateBundleSummaryPanel());
                addChild(new DownloadConfigurationLogicAction());
                addChild(new DownloadInstallationDataAction());
                addChild(new CreateRedistributableBundleAction());
                addChild(new PostCreateBundleSummaryPanel());
                break;
            default:
                ErrorManager.notify(ErrorLevel.CRITICAL, 
                        "A terrible and weird error happened - installer's " +
                        "execution mode is not recognized");
        }
        
        super.executeForward(wizard);
    }
    
    public boolean canExecuteForward() {
        return true;
    }
    
    public boolean canExecuteBackward() {
        return false;
    }
}