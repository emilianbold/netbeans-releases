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

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.universe.LocalizedBundleInfo;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Adding ability for a NetBeans modules to provide a GUI customizer.
 *
 * @author Martin Krauskopf
 */
public final class CustomizerProviderImpl extends BasicCustomizer {
    
    // Programmatic names of categories
    private static final String CATEGORY_SOURCES = "Sources"; // NOI18N
    private static final String CATEGORY_DISPLAY = "Display"; // NOI18N
    private static final String CATEGORY_LIBRARIES = "Libraries"; // NOI18N
    public static final String CATEGORY_VERSIONING = "Versioning"; // NOI18N
    public static final String SUBCATEGORY_VERSIONING_PUBLIC_PACKAGES = "publicPackages"; // NOI18N
    private static final String CATEGORY_BUILD = "Build"; // NOI18N
    private static final String CATEGORY_COMPILING = "Compiling"; // NOI18N
    private static final String CATEGORY_PACKAGING = "Packaging"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    
    private SingleModuleProperties moduleProps;
    
    public CustomizerProviderImpl(final Project project, final AntProjectHelper helper,
            final PropertyEvaluator evaluator) {
        super(project);
        this.helper = helper;
        this.evaluator = evaluator;
    }
    
    void storeProperties() throws IOException {
        moduleProps.storeProperties();
    }
    
    void postSave() throws IOException {
        if (moduleProps.isModuleListRefreshNeeded()) {
            moduleProps.getModuleList().refresh();
            moduleProps.setModuleListRefreshNeeded(false);
        }
    }
    
    protected void prepareData() {
        Lookup lookup = getProject().getLookup();
        SuiteProvider sp = (SuiteProvider) lookup.lookup(SuiteProvider.class);
        NbModuleProvider.NbModuleType type = Util.getModuleType((NbModuleProject) getProject());
        if (moduleProps == null) { // first initialization
            moduleProps = new SingleModuleProperties(helper, evaluator, sp, type,
                    (LocalizedBundleInfo.Provider) lookup.lookup(LocalizedBundleInfo.Provider.class));
            init();
        } else {
            moduleProps.refresh(type, sp);
        }
    }
    
    private void init() {
        ProjectCustomizer.Category sources = createCategory(CATEGORY_SOURCES, "LBL_ConfigSources"); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(CATEGORY_LIBRARIES, "LBL_ConfigLibraries"); // NOI18N
        ProjectCustomizer.Category display = createCategory(CATEGORY_DISPLAY, "LBL_ConfigDisplay"); // NOI18N
        ProjectCustomizer.Category versioning = createCategory(CATEGORY_VERSIONING, "LBL_ConfigVersioning"); // NOI18N
        ProjectCustomizer.Category compiling = createCategory(CATEGORY_COMPILING, "LBL_ConfigCompiling"); // NOI18N
        ProjectCustomizer.Category packaging = createCategory(CATEGORY_PACKAGING, "LBL_ConfigPackaging"); // NOI18N
        
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                CATEGORY_BUILD,
                NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_ConfigBuild"),
                null,
                new ProjectCustomizer.Category[] { compiling, packaging }
        );
        
        setCategories(new ProjectCustomizer.Category[] {
            sources, libraries, display, versioning, build
        });

        createPanel(sources, new CustomizerSources(moduleProps));
        createPanel(libraries, new CustomizerLibraries(moduleProps));
        createPanel(display, new CustomizerDisplay(moduleProps));
        createPanel(versioning, new CustomizerVersioning(moduleProps));
        createPanel(compiling, new CustomizerCompiling(moduleProps));
        createPanel(packaging, new CustomizerPackaging(moduleProps));
        
        listenToPanels();
    }
    
}
