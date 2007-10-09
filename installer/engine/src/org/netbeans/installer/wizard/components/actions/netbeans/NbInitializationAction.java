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
 *
 */

package org.netbeans.installer.wizard.components.actions.netbeans;

import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.WizardAction;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.actions.DownloadConfigurationLogicAction;
import org.netbeans.installer.wizard.components.actions.InitializeRegistryAction;
import org.netbeans.installer.wizard.components.actions.SearchForJavaAction;

/**
 *
 * @author Dmitry Lipin
 */
public class NbInitializationAction extends WizardAction{
    private InitializeRegistryAction initReg;
    private DownloadConfigurationLogicAction downloadLogic;
    private SearchForJavaAction searchJava;
    private WizardAction currentAction ;
    
    public NbInitializationAction() {
        setProperty(TITLE_PROPERTY,
                DEFAULT_TITLE);
        setProperty(DESCRIPTION_PROPERTY,
                DEFAULT_DESCRIPTION);
        
        initReg = new InitializeRegistryAction();
        downloadLogic = new DownloadConfigurationLogicAction();
        searchJava = new SearchForJavaAction();
    }
    
    public void execute() {
        final CompositeProgress progress = new CompositeProgress(this.getWizardUi());
        progress.setTitle(getProperty(TITLE_PROPERTY));
        progress.synchronizeDetails(false);
        if(initReg.canExecuteForward()) {
            currentAction = initReg;
            initReg.setWizard(getWizard());
            initReg.execute();
        }
        
        if(downloadLogic.canExecuteForward()) {
            currentAction = downloadLogic;
            downloadLogic.setWizard(getWizard());
            downloadLogic.execute();
        }
        
        if(searchJava.canExecuteForward()) {
            boolean doSearch = false;
            List <Product> toInstall = Registry.getInstance().getProductsToInstall();
            for(Product product : toInstall) {
                try {
                    for(WizardComponent component : product.getLogic().getWizardComponents()) {
                        if(component instanceof  SearchForJavaAction) {
                            doSearch = true;
                            break;
                        }
                    }
                } catch (InitializationException e) {
                    LogManager.log(e);
                }
            }
            if(doSearch) {
                currentAction = searchJava;
                Progress javaSearchProgress = new Progress();
                progress.addChild(javaSearchProgress,100);
                searchJava.setWizard(getWizard());
                searchJava.execute(javaSearchProgress);
            }
        }
    }
        
    public static final String DEFAULT_TITLE = ResourceUtils.getString(
            NbInitializationAction.class,
            "NIA.title"); // NOI18N
    public static final String PROGRESS_TITLE = ResourceUtils.getString(
            NbInitializationAction.class,
            "NIA.progress.title"); // NOI18N
    public static final String DEFAULT_DESCRIPTION = ResourceUtils.getString(
            NbInitializationAction.class,
            "NIA.description"); // NOI18N*/
    
}
