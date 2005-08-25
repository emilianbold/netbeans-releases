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
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.SuiteProvider;
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
    private static final String CATEGORY_DOCUMENTING = "Documenting"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final LocalizedBundleInfo bundleInfo;
    
    private SingleModuleProperties moduleProps;
    
    public CustomizerProviderImpl(final Project project, final AntProjectHelper helper,
            final PropertyEvaluator evaluator, final LocalizedBundleInfo bundleInfo) {
        super(project);
        this.helper = helper;
        this.evaluator = evaluator;
        this.bundleInfo = bundleInfo;
    }
    
    void storeProperties() throws IOException {
        moduleProps.storeProperties();
    }
    
    protected void prepareData() {
        Lookup lookup = getProject().getLookup();
        SuiteProvider sp = (SuiteProvider) lookup.lookup(SuiteProvider.class);
        NbModuleTypeProvider nmtp = (NbModuleTypeProvider) lookup.lookup(NbModuleTypeProvider.class);
        if (moduleProps == null) { // first initialization
            moduleProps = new SingleModuleProperties(helper, evaluator,
                    sp, nmtp.getModuleType(), bundleInfo);
            init();
        }
        moduleProps.refresh(nmtp.getModuleType(), sp);
    }
    
    private void init() {
        ProjectCustomizer.Category sources = createCategory(CATEGORY_SOURCES, "LBL_ConfigSources"); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(CATEGORY_LIBRARIES, "LBL_ConfigLibraries"); // NOI18N
        ProjectCustomizer.Category display = createCategory(CATEGORY_DISPLAY, "LBL_ConfigDisplay"); // NOI18N
        ProjectCustomizer.Category versioning = createCategory(CATEGORY_VERSIONING, "LBL_ConfigVersioning"); // NOI18N
        ProjectCustomizer.Category compiling = createCategory(CATEGORY_COMPILING, "LBL_ConfigCompiling"); // NOI18N
        ProjectCustomizer.Category packaging = createCategory(CATEGORY_PACKAGING, "LBL_ConfigPackaging"); // NOI18N
        ProjectCustomizer.Category documenting = createCategory(CATEGORY_DOCUMENTING, "LBL_ConfigDocumenting"); // NOI18N
        
        // XXX this is a little clumsy (will be replaced by some general mechanism, now it is rather a demo)
        // (servers just for checking if category is valid during first invocation)
        CustomizerVersioning versioningPanel = new CustomizerVersioning(moduleProps);
        versioning.setValid(versioningPanel.isCustomizerValid());
        
        ProjectCustomizer.Category build = ProjectCustomizer.Category.create(
                CATEGORY_BUILD,
                NbBundle.getMessage(CustomizerProviderImpl.class, "LBL_ConfigBuild"),
                null,
                new ProjectCustomizer.Category[] { compiling, packaging, documenting }
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
        createPanel(documenting, new CustomizerDocumenting(moduleProps));
        
        listenToPanels();
    }
    
}

