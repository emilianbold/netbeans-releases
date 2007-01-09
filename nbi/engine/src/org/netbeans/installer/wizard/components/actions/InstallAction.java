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
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardPanel;
import org.netbeans.installer.wizard.components.actions.CompositeWizardAction.CompositeWizardActionUi;

public class InstallAction extends CompositeWizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DIALOG_TITLE_PROPERTY = WizardPanel.TITLE_PROPERTY;
    public static final String DEFAULT_DIALOG_TITLE = ResourceUtils.getString(InstallAction.class, "IA.dialog.title");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public InstallAction() {
        setProperty(DIALOG_TITLE_PROPERTY, DEFAULT_DIALOG_TITLE);
    }
    
    public boolean canExecuteForward() {
        return Registry.getInstance().getComponentsToInstall().size() > 0;
    }
    
    public boolean isPointOfNoReturn() {
        return true;
    }
    
    public void execute() {
        final Registry registry = Registry.getInstance();
        final List<Product> components = registry.getComponentsToInstall();
        final int percentageChunk = Progress.COMPLETE / components.size();
        final int percentageLeak = Progress.COMPLETE % components.size();
        
        final Map<Product, Progress> progresses = new HashMap<Product, Progress>();
        
        overallProgress = new CompositeProgress();
        overallProgress.setTitle("Installing selected components");
        overallProgress.setPercentage(percentageLeak);
        
        ((CompositeWizardActionUi) getWizardUi()).setOverallProgress(overallProgress);
        for (Product component:components) {
            currentProgress = new Progress();
            currentProgress.setTitle("Installing " + component.getDisplayName());
            ((CompositeWizardActionUi) getWizardUi()).setCurrentProgress(currentProgress);
            
            overallProgress.addChild(currentProgress, percentageChunk);
            try {
                component.install(currentProgress);
                
                if (canceled)  {
                    currentProgress.setCanceled(false);
                    component.rollback(currentProgress);
                    
                    for (Product toUninstall: registry.getComponentsInstalledDuringThisSession()) {
                        toUninstall.setStatus(Status.TO_BE_UNINSTALLED);
                    }
                    for (Product toUninstall: registry.getComponentsToUninstall()) {
                        component.rollback(progresses.get(component));
                    }
                    break;
                }
                
                progresses.put(component, currentProgress);
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            }  catch (InstallationException e) {
                // adjust the component's status and save this error - it will
                // be reused later at the PostInstallSummary
                component.setStatus(Status.NOT_INSTALLED);
                component.setInstallationError(e);
                
                // since the component failed to install  - we should remove the
                // depending components from our plans to install
                for(Product dependent : Registry.getInstance().getDependingComponents(component)) {
                    if (dependent.getStatus()  == Status.TO_BE_INSTALLED) {
                        InstallationException dependentError = new InstallationException("Could not install " + dependent.getDisplayName() + ", since the installation of " + component.getDisplayName() + "failed", e);
                        
                        dependent.setStatus(Status.NOT_INSTALLED);
                        dependent.setInstallationError(dependentError);
                        
                        components.remove(dependent);
                    }
                }
                
                // finally notify the user of what has happened
                LogManager.log(ErrorLevel.ERROR, e);
            } catch (UninstallationException e) {
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