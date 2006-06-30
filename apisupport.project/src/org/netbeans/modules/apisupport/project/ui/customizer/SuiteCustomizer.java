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
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
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
    public static final String APPLICATION = "Application"; // NOI18N
    public static final String APPLICATION_CREATE_STANDALONE_APPLICATION = "standaloneApp"; // NOI18N
    private static final String SPLASH_SCREEN = "SplashScreen"; // NOI18N
    
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
        Set/*<Project>*/ subModules = SuiteUtils.getSubProjects(getProject());
        if (suiteProps == null) { // first initialization
            suiteProps = new SuiteProperties((SuiteProject) getProject(), helper, evaluator, subModules);
            init();
        } else {
            suiteProps.refresh(subModules);
        }
    }
    
    private void init() {
        ProjectCustomizer.Category sources = createCategory(SOURCES, "LBL_ConfigSources"); // NOI18N
        ProjectCustomizer.Category libraries = createCategory(LIBRARIES, "LBL_ConfigLibraries"); // NOI18N
        ProjectCustomizer.Category splashBranding = createCategory(SPLASH_SCREEN, "LBL_SplashBranding"); // NOI18N
        ProjectCustomizer.Category application = ProjectCustomizer.Category.create(
                APPLICATION,
                NbBundle.getMessage(SuiteProperties.class, "LBL_Application"),
                null,
                new ProjectCustomizer.Category[] { splashBranding }
        );
        
        setCategories(new ProjectCustomizer.Category[] {
            sources, libraries, application
        });

        createPanel(sources, new SuiteCustomizerSources(suiteProps));
        createPanel(libraries, new SuiteCustomizerLibraries(suiteProps));
        createPanel(application, new SuiteCustomizerBasicBranding(suiteProps));        
        createPanel(splashBranding, new SuiteCustomizerSplashBranding(suiteProps));
        
        listenToPanels();
    }
    
}

