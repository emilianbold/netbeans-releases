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
 */

package org.netbeans.modules.ant.freeform.spi.support;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.modules.ant.freeform.FreeformProjectGenerator;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.ant.freeform.ui.BasicProjectInfoWizardPanel;
import org.netbeans.modules.ant.freeform.ui.TargetMappingWizardPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.WizardDescriptor;
import org.openide.util.NbCollections;

/**
 * Support for New Project Wizard.
 * <div class="nonnormative">
 * <p>
 * Typical usage of these methods is:
 * </p>
 * <ol>
 * <li>create implementation of {@link org.openide.WizardDescriptor.InstantiatingIterator}
 *   which uses two general panels ({@link #createBasicProjectInfoWizardPanel} and 
 *   {@link #createTargetMappingWizardPanel}) plus your own domain specific ones.</li>
 * <li>in implementation of {@link org.openide.WizardDescriptor.InstantiatingIterator.instantiate}
 *   method call both {@link #instantiateBasicProjectInfoWizardPanel} and 
 *   {@link #instantiateTargetMappingWizardPanel} methods to setup project
 *   and update list of target mappings and then use returned AntProjectHelper
 *   to store your domain specific metadata</li>
 * <li>do not forget to call both {@link #uninitializeBasicProjectInfoWizardPanel} and
 *   {@link #uninitializeTargetMappingWizardPanel} methods in your 
 *   {@link org.openide.WizardDescriptor.InstantiatingIterator.uninitialize} to clean up 
 *   panels</li>
 * </ol>
 * </div>
 *
 * @author  David Konecny
 */
public class NewFreeformProjectSupport {
    
    /** Ant script. Type: java.io.File */
    public static final String PROP_ANT_SCRIPT = "antScript"; // <java.io.File> NOI18N
    
    /** Project name. Type: String */
    public static final String PROP_PROJECT_NAME = "projectName"; // <String> NOI18N
    
    /** Original project base folder. Type: java.io.File */
    public static final String PROP_PROJECT_LOCATION = "projectLocation"; // <java.io.File> NOI18N
    
    /** NetBeans project folder. Type: java.io.File */
    public static final String PROP_PROJECT_FOLDER = "projectFolder"; // <java.io.File> NOI18N
    
    private NewFreeformProjectSupport() {}
   
    /**
     * Returns Basic Project Info panel suitable for new project wizard.
     * Panel gathers info about original project folder, NB projetc folder, 
     * project name and Ant script.
     */
    public static final WizardDescriptor.Panel createBasicProjectInfoWizardPanel() {
        return new BasicProjectInfoWizardPanel();
    }
    
    /**
     * Instantiate project according to information gathered in 
     * Basic Project Info panel. The method must to be called
     * under ProjectManager.writeMutex.
     */
    public static final AntProjectHelper instantiateBasicProjectInfoWizardPanel(WizardDescriptor wiz) throws IOException {
        // XXX assert ProjectManager.mutex().isWriteAccess()
        File antScript = (File)wiz.getProperty(PROP_ANT_SCRIPT);
        String projName = (String)wiz.getProperty(PROP_PROJECT_NAME);
        File projLocation = (File)wiz.getProperty(PROP_PROJECT_LOCATION);
        File projectFolder = (File)wiz.getProperty(PROP_PROJECT_FOLDER);
        if (antScript.getParentFile().equals(projectFolder) && antScript.getName().equals("build.xml")) { // NOI18N
            // default location of build file
            antScript = null;
        }
        return FreeformProjectGenerator.createProject(projLocation, projectFolder, projName, antScript);
    }

    /**
     * Returns Target Mapping panel suitable for new project wizard. The panel
     * contains mappings of standard IDE actions like clean, build, javadoc, 
     * run and test. Other IDE actions can be added.
     *
     * @param targets list of additional targets to be shown in wizard panel. List
     * of TargetDescriptor instances. Order is relevant.
     */
    public static final WizardDescriptor.Panel createTargetMappingWizardPanel(List<TargetDescriptor> targets) {
        return new TargetMappingWizardPanel(targets);
    }
    
    /**
     * Update project with information gathered in Target Mapping panel.
     * The method must to be called under ProjectManager.writeMutex.
     */
    public static final void instantiateTargetMappingWizardPanel(AntProjectHelper helper, WizardDescriptor wiz) {
        List<FreeformProjectGenerator.TargetMapping> mappings = NbCollections.checkedListByCopy(
                (List) wiz.getProperty(TargetMappingWizardPanel.PROP_TARGET_MAPPINGS),
                FreeformProjectGenerator.TargetMapping.class, true);
        
        FreeformProjectGenerator.putTargetMappings(helper, mappings);
        FreeformProjectGenerator.putContextMenuAction(helper, mappings);
    }

    /**
     * Uninitialize Basic Project Info panel after wizard was instantiated.
     */
    public static void uninitializeBasicProjectInfoWizardPanel(WizardDescriptor wiz) {
        wiz.putProperty(NewFreeformProjectSupport.PROP_ANT_SCRIPT, null);
        wiz.putProperty(NewFreeformProjectSupport.PROP_PROJECT_NAME, null);
        wiz.putProperty(NewFreeformProjectSupport.PROP_PROJECT_LOCATION, null);
        wiz.putProperty(NewFreeformProjectSupport.PROP_PROJECT_FOLDER, null);
    }
    

    /**
     * Uninitialize Target Mapping panel after wizard was instantiated.
     */
    public static void uninitializeTargetMappingWizardPanel(WizardDescriptor wiz) {
        wiz.putProperty(TargetMappingWizardPanel.PROP_TARGET_MAPPINGS, null);
    }
    
}
