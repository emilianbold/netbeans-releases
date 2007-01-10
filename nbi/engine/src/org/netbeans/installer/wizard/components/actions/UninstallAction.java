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
import org.netbeans.installer.product.utils.Status;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;

public class UninstallAction extends WizardAction {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String DEFAULT_TITLE = 
            ResourceUtils.getString(UninstallAction.class, 
            "UA.dialog.title");
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private CompositeProgress overallProgress;
    private Progress          currentProgress;
    
    public UninstallAction() {
        setProperty(TITLE_PROPERTY, DEFAULT_TITLE);
    }
    
    public void execute() {
        final Registry registry = Registry.getInstance();
        final List<Product> components = registry.getComponentsToUninstall();
        final int percentageChunk = Progress.COMPLETE / components.size();
        final int percentageLeak = Progress.COMPLETE % components.size();
        
        overallProgress = new CompositeProgress();
        overallProgress.setTitle("Uninstalling selected components");
        overallProgress.setPercentage(percentageLeak);
        
        getWizardUi().setProgress(overallProgress);
        for (Product component: components) {
            currentProgress = new Progress();
            currentProgress.setTitle("Uninstalling " + component.getDisplayName());
            
            overallProgress.addChild(currentProgress, percentageChunk);
            try {
                component.uninstall(currentProgress);
                
                // sleep a little so that the user can perceive that something
                // is happening
                SystemUtils.sleep(200);
            }  catch (UninstallationException e) {
                // adjust the component's status and save this error - it will
                // be reused later at the PostInstallSummary
                component.setStatus(Status.INSTALLED);
                component.setUninstallationError(e);
                
                // since the component failed to uninstall  - we should remove
                // the components it depends on from our plans to uninstall
                for(Product requirement : Registry.getInstance().getRequiredComponents(component)) {
                    if (requirement.getStatus() == Status.TO_BE_UNINSTALLED) {
                        UninstallationException requirementError = new UninstallationException("Could not uninstall " + requirement.getDisplayName() + ", since the uninstallation of " + component.getDisplayName() + "failed", e);
                        
                        requirement.setStatus(Status.INSTALLED);
                        requirement.setUninstallationError(requirementError);
                        
                        components.remove(requirement);
                    }
                }
                
                // finally notify the user of what has happened
                ErrorManager.notify(ErrorLevel.ERROR, e);
            }
        }
    }
    
    public boolean canExecuteForward() {
        return Registry.getInstance().getComponentsToUninstall().size() > 0;
    }
    
    public boolean isPointOfNoReturn() {
        return true;
    }
}