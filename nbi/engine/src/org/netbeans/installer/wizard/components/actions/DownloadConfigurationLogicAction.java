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
package org.netbeans.installer.wizard.components.actions;

import java.util.List;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryType;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

public class DownloadConfigurationLogicAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = 
            ResourceUtils.getString(DownloadConfigurationLogicAction.class, 
            "DCLA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = 
            ResourceUtils.getString(DownloadConfigurationLogicAction.class, 
            "DCLA.description"); // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public DownloadConfigurationLogicAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    public boolean canExecuteForward() {
        for (Product product: Registry.getInstance().getProductsToInstall()) {
            if (!product.isLogicDownloaded()) {
                return true;
            }
        }
        
        return false;
    }
    
    public void execute() {
        final Registry registry = Registry.getInstance();
        final List<Product> products = registry.getProductsToInstall();
        final int percentageChunk = Progress.COMPLETE / products.size();
        final int percentageLeak  = Progress.COMPLETE % products.size();
        
        overallProgress = new CompositeProgress();
        overallProgress.setPercentage(percentageLeak);
        overallProgress.synchronizeDetails(true);
        
        getWizardUi().setProgress(overallProgress);
        for (Product product: products) {
            // initiate the progress for the current element
            currentProgress = new Progress();
            
            overallProgress.addChild(currentProgress, percentageChunk);            
            try {
                if (product.getRegistryType() == RegistryType.REMOTE) {
                    overallProgress.setTitle("Downloading configuration logic for " + product.getDisplayName());
                } else {
                    overallProgress.setTitle("Exctracting configuration logic for " + product.getDisplayName());
                }
                
                product.downloadLogic(currentProgress);
                
                // ensure that the current progress has reached the complete state
                // (sometimes it just does not happen and we're left over with 99%)
                currentProgress.setPercentage(Progress.COMPLETE);
                
                // check for cancel status
                if (canceled) return;
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            }  catch (DownloadException e) {
                // wrap the download exception with a more user-friendly one
                final InstallationException error = new InstallationException(
                        "Failed to download installation logic for " + product.getDisplayName(), 
                        e);
                
                // adjust the product's status and save this error - it will
                // be reused later at the PostInstallSummary
                product.setStatus(Status.NOT_INSTALLED);
                product.setInstallationError(error);
                
                // since the configuration logic for the current product failed to 
                // be downloaded, we should cancel the installation of the products
                // that may require this one
                for(Product dependent: registry.getProducts()) {
                    if ((dependent.getStatus()  == Status.TO_BE_INSTALLED) && 
                            registry.satisfiesRequirement(product, dependent)) {
                        final InstallationException dependentError = new InstallationException("Could not install " + dependent.getDisplayName() + ", since the installation of " + product.getDisplayName() + "failed", error);
                        
                        dependent.setStatus(Status.NOT_INSTALLED);
                        dependent.setInstallationError(dependentError);
                        
                        products.remove(dependent);
                    }
                }
                
                // finally notify the user of what has happened
                ErrorManager.notify(ErrorLevel.ERROR, error);
            }
        }
    }
    
    public void cancel() {
        if (currentProgress != null) {
            currentProgress.setCanceled(true);
        }
        
        if (overallProgress != null) {
            overallProgress.setCanceled(true);
        }
        
        super.cancel();
    }
}