/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance
 * with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html or
 * http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file and
 * include the License file at http://www.netbeans.org/cddl.txt. If applicable, add
 * the following below the CDDL Header, with the fields enclosed by brackets []
 * replaced by your own identifying information:
 * 
 *     "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
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
import org.netbeans.installer.utils.StringUtils;
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
    
    
    public static final String DEFAULT_PROGRESS_TITLE_LOCAL =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.progress.local.title"); //NOI18N
    public static final String PROGRESS_TITLE_LOCAL_PROPERTY =
            "progress.title.local";//NOI18N
    
    public static final String DEFAULT_PROGRESS_TITLE_REMOTE =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.progress.remote.title"); //NOI18N
    public static final String PROGRESS_TITLE_REMOTE_PROPERTY =
            "progress.title.remote";//NOI18N
    
    public static final String DEFAULT_DOWNLOAD_FAILED_EXCEPTION =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.failed"); //NOI18N
    public static final String DOWNLOAD_FAILED_EXCEPTION_PROPERTY =
            "download.failed";//NOI18N
    
    public static final String DEFAULT_DEPENDENT_FAILED_EXCEPTION =
            ResourceUtils.getString(DownloadConfigurationLogicAction.class,
            "DCLA.dependent.failed"); //NOI18N
    
    public static final String DEPENDENT_FAILED_EXCEPTION_PROPERTY =
            "download.dependent.failed"; //NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public DownloadConfigurationLogicAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
        setProperty(PROGRESS_TITLE_LOCAL_PROPERTY, DEFAULT_PROGRESS_TITLE_LOCAL);
        setProperty(PROGRESS_TITLE_REMOTE_PROPERTY, DEFAULT_PROGRESS_TITLE_REMOTE);
        setProperty(DOWNLOAD_FAILED_EXCEPTION_PROPERTY, DEFAULT_DOWNLOAD_FAILED_EXCEPTION);
        setProperty(DEPENDENT_FAILED_EXCEPTION_PROPERTY, DEFAULT_DEPENDENT_FAILED_EXCEPTION);
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
        for (int i = 0; i < products.size(); i++) {
            // get the handle of the current item
            final Product product = products.get(i);
            
            // initiate the progress for the current element
            currentProgress = new Progress();
            
            overallProgress.addChild(currentProgress, percentageChunk);
            try {
                String prop = product.getRegistryType() == RegistryType.REMOTE ?
                    PROGRESS_TITLE_REMOTE_PROPERTY :
                    PROGRESS_TITLE_LOCAL_PROPERTY;
                String overallProgressTitle = StringUtils.format(
                        getProperty(prop), product.getDisplayName());
                
                overallProgress.setTitle(overallProgressTitle);
                
                product.downloadLogic(currentProgress);
                
                // ensure that the current progress has reached the complete state
                // (sometimes it just does not happen and we're left over with 99%)
                currentProgress.setPercentage(Progress.COMPLETE);
                
                // check for cancel status
                if (isCanceled()) return;
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            } catch (DownloadException e) {
                // wrap the download exception with a more user-friendly one
                final InstallationException error = new InstallationException(
                        StringUtils.format(
                        getProperty(DOWNLOAD_FAILED_EXCEPTION_PROPERTY),
                        product.getDisplayName()), e);
                
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
                        String exString = StringUtils.format(
                                getProperty(DEPENDENT_FAILED_EXCEPTION_PROPERTY),
                                dependent.getDisplayName(),
                                product.getDisplayName());
                        
                        final InstallationException dependentError =
                                new InstallationException(exString, error);
                        
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
