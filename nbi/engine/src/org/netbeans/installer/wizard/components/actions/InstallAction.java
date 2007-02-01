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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

public class InstallAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE =
            ResourceUtils.getString(InstallAction.class,
            "IA.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION =
            ResourceUtils.getString(InstallAction.class,
            "IA.description"); // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public InstallAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY, DEFAULT_DESCRIPTION);
    }
    
    public boolean canExecuteForward() {
        return Registry.getInstance().getProductsToInstall().size() > 0;
    }
    
    public boolean isPointOfNoReturn() {
        return true;
    }
    
    public void execute() {
        final Registry registry = Registry.getInstance();
        final List<Product> products = registry.getProductsToInstall();
        final int percentageChunk = Progress.COMPLETE / products.size();
        final int percentageLeak = Progress.COMPLETE % products.size();
        
        final Map<Product, Progress> progresses = new HashMap<Product, Progress>();
        
        overallProgress = new CompositeProgress();
        overallProgress.setPercentage(percentageLeak);
        overallProgress.synchronizeDetails(true);
        
        getWizardUi().setProgress(overallProgress);
        for (Product product: products) {
            currentProgress = new Progress();
            
            overallProgress.addChild(currentProgress, percentageChunk);
            overallProgress.setTitle("Installing " + product.getDisplayName());
            try {
                product.install(currentProgress);
                
                if (canceled)  {
                    currentProgress.setCanceled(false);
                    product.rollback(currentProgress);
                    
                    for (Product toRollback: registry.getProducts(DetailedStatus.INSTALLED_SUCCESSFULLY)) {
                        toRollback.setStatus(Status.TO_BE_UNINSTALLED);
                    }
                    for (Product toRollback: registry.getProducts(DetailedStatus.INSTALLED_WITH_WARNINGS)) {
                        toRollback.setStatus(Status.TO_BE_UNINSTALLED);
                    }
                    for (Product toRollback: registry.getProductsToUninstall()) {
                        toRollback.rollback(progresses.get(toRollback));
                    }
                    break;
                }
                
                progresses.put(product, currentProgress);
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            } catch (Throwable e) {
                if (!(e instanceof InstallationException)) {
                    e = new InstallationException("Unknown Error", e);
                }
                
                // adjust the product's status and save this error - it will
                // be reused later at the PostInstallSummary
                product.setStatus(Status.NOT_INSTALLED);
                product.setInstallationError(e);
                
                // since the current product failed to install, we should cancel the
                // installation of the products that may require this one
                for(Product dependent: registry.getProducts()) {
                    if ((dependent.getStatus()  == Status.TO_BE_INSTALLED) &&
                            registry.satisfiesRequirement(product, dependent)) {
                        final InstallationException dependentError = new InstallationException("Could not install " + dependent.getDisplayName() + ", since the installation of " + product.getDisplayName() + "failed", e);
                        
                        dependent.setStatus(Status.NOT_INSTALLED);
                        dependent.setInstallationError(dependentError);
                        
                        products.remove(dependent);
                    }
                }
                
                // finally notify the user of what has happened
                LogManager.log(ErrorLevel.ERROR, e);
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