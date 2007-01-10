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
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.wizard.components.WizardSequence;
import org.netbeans.installer.wizard.components.actions.CreateRedistributableBundleAction;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.DownloadInstallationDataAction;
import org.netbeans.installer.wizard.components.actions.InstallAction;
import org.netbeans.installer.wizard.components.actions.UninstallAction;
import org.netbeans.installer.wizard.components.panels.PostCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PostInstallSummaryPanel;
import org.netbeans.installer.wizard.components.panels.PreCreateBundleSummaryPanel;
import org.netbeans.installer.wizard.components.panels.SelectedComponentsLicensesPanel;
import org.netbeans.installer.wizard.components.panels.netbeans.NbPreInstallSummaryPanel;

/**
 *
 * @author Kirill Sorokin
 */
public class MainSequence extends WizardSequence {
    public void executeForward() {
        final Registry        registry    = Registry.getInstance();
        final List<Product> toInstall   = registry.getComponentsToInstall();
        final List<Product> toUninstall = registry.getComponentsToUninstall();
        
        // remove all current children (if there are any), as the components 
        // selection has probably changed and we need to rebuild from scratch
        getChildren().clear();
        
        // the set of wizard components differs greatly depending on the execution
        // mode - if we're installing, we ask for input, run a wizard sequence for 
        // each selected component and then download and install; if we're creating 
        // a bundle, we only need to download and package things
        switch (Installer.getInstance().getExecutionMode()) {
            case NORMAL:
                if (toInstall.size() > 0) {
                    addChild(new DownloadConfigurationLogicAction());
                    addChild(new SelectedComponentsLicensesPanel());
                    
                    for (Product component: toInstall) {
                        addChild(new ProductComponentWizardSequence(component));
                    }
                }
                
                addChild(new NbPreInstallSummaryPanel());
                
                if (toUninstall.size() > 0) {
                    addChild(new UninstallAction());
                }
                
                if (toInstall.size() > 0) {
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
                // there is no real way to recover from this fancy error, so we 
                // inform the user and die
                ErrorManager.notifyCritical(
                        "A terrible and weird error happened - installer's " +
                        "execution mode is not recognized");
        }
        
        super.executeForward();
    }
    
    public boolean canExecuteForward() {
        return true;
    }
    
    public boolean canExecuteBackward() {
        return false;
    }
    
    public boolean isPointOfNoReturn() {
        return true;
    }
}