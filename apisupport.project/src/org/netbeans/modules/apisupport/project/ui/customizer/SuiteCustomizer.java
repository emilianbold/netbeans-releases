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
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * Adding ability for a NetBeans Suite modules to provide a GUI customizer.
 *
 * @author Martin Krauskopf
 */
public final class SuiteCustomizer extends BasicCustomizer {
    
    // Programmatic names of categories
    static final String SOURCES = "Sources"; // NOI18N
    static final String LIBRARIES = "Libraries"; // NOI18N
    public static final String APPLICATION = "Application"; // NOI18N
    public static final String APPLICATION_CREATE_STANDALONE_APPLICATION = "standaloneApp"; // NOI18N
    static final String SPLASH_SCREEN = "SplashScreen"; // NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    
    private SuiteProperties suiteProps;
    
    public SuiteCustomizer(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator) {
        super(project, "Projects/org-netbeans-modules-apisupport-project-suite/Customizer");
        this.helper = helper;
        this.evaluator = evaluator;
    }
    
    void storeProperties() throws IOException {
        suiteProps.triggerLazyStorages();
        suiteProps.storeProperties();
    }
    
    void dialogCleanup() {
        suiteProps = null;
    }    
    
    void postSave() { /* nothing needs to be done for now */ }
    
    protected Lookup prepareData() {
        Set<NbModuleProject> subModules = SuiteUtils.getSubProjects(getProject());
        suiteProps = new SuiteProperties((SuiteProject) getProject(), helper, evaluator, subModules);
        return Lookups.fixed(suiteProps, getProject());
    }
}

