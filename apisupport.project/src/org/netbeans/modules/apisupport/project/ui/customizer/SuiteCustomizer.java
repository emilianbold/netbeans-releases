/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.IOException;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.NbBundle;

/**
 * Adding ability for a NetBeans Suite modules to provide a GUI customizer.
 *
 * @author Martin Krauskopf
 */
public final class SuiteCustomizer extends BasicCustomizer {
    
    // Programmatic names of categories
    private static final String SOURCES = "Sources"; // NOI18N
    private static final String LIBRARIES = "Libraries"; // NOI18N
    private static final String MODULE_LIST = "ModuleList"; // NOI18N
    private static final String BUILD = "Build"; // NOI18N
    private static final String BASIC_BRANDING = "BasicBranding"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    
    private SuiteProperties suiteProps;
    
    public SuiteCustomizer(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator) {
        super(project);
        this.helper = helper;
        this.evaluator = evaluator;
    }
    
    void storeProperties() throws IOException {
        suiteProps.storeProperties();
    }
    
    protected void prepareData() {
        SubprojectProvider spp = (SubprojectProvider) getProject().
                getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ subModules = spp.getSubprojects();
        if (suiteProps == null) { // first initialization
            suiteProps = new SuiteProperties(getProject(), helper, evaluator, subModules);
            init();
        } else {
            suiteProps.refresh(subModules);
        }
    }
    
    private void init() {
        ProjectCustomizer.Category sources = createCategory(SOURCES, "LBL_ConfigSources"); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(LIBRARIES, "LBL_ConfigLibraries"); // NOI18N
        ProjectCustomizer.Category moduleList = createCategory(MODULE_LIST, "LBL_ConfigModuleList"); // NOI18N
        ProjectCustomizer.Category basicBranding = createCategory(BASIC_BRANDING, "LBL_BasicBranding"); // NOI18N
        
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                BUILD,
                NbBundle.getMessage(SuiteProperties.class, "LBL_Build"),
                null,
                new ProjectCustomizer.Category[] { basicBranding, moduleList }
        );
        
        setCategories(new ProjectCustomizer.Category[] {
            sources, libraries, build
        });
        
        createPanel(sources, new SuiteCustomizerSources(suiteProps));
        createPanel(libraries, new SuiteCustomizerLibraries(suiteProps));
        createPanel(moduleList, new SuiteCustomizerModuleList(suiteProps));
        createPanel(basicBranding, new SuiteCustomizerBasicBranding(suiteProps));
        
        listenToPanels();
    }
    
}

