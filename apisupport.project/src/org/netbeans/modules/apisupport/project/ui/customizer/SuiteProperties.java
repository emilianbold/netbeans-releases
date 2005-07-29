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
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.SuiteSubModulesListModel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;

/**
 * Provides convenient access to a lot of Suite Module's properties.
 *
 * @author Martin Krauskopf
 */
final class SuiteProperties extends ModuleProperties {
    
    private NbPlatform platform;
    
    /** Project the current properties represents. */
    private Project project;
    
    /** Represent original set of sub-modules. */
    private final Set/*<Project>*/ origSubModules;
    
    /** Represent currently set set of sub-modules. */
    private Set/*<Project>*/ subModules;
    
    // models
    private SuiteSubModulesListModel moduleListModel;
    
    /**
     * Creates a new instance of SuiteProperties
     */
    SuiteProperties(Project project, AntProjectHelper helper,
            PropertyEvaluator evaluator, Set/*<Project>*/ subModules) {
        super(helper, evaluator);
        this.project = project;
        this.origSubModules = Collections.unmodifiableSet(subModules);
        this.subModules = subModules;
        platform = NbPlatform.getPlatformByID(
                evaluator.getProperty("nbplatform.active")); // NOI18N
    }

    Project getProject() {
        return project;
    }
    
    Map/*<String, String>*/ getDefaultValues() {
        return Collections.EMPTY_MAP; // no default value (yet)
    }
    
    NbPlatform getActivePlatform() {
        return platform;
    }
    
    void setActivePlatform(NbPlatform newPlaf) {
        this.platform = newPlaf;
    }
    
    /**
     * Stores cached properties. This is called when the user press <em>OK</em>
     * button in the properties customizer. If <em>Cancel</em> button is
     * pressed properties will not be saved,.
     */
    void storeProperties() throws IOException {
        ModuleProperties.storePlatform(getHelper(), platform);
        
        // store submodules if they've changed
        SuiteSubModulesListModel model = getModulesListModel();
        if (model.isChanged()) {
            SuiteUtils.replaceSubModules(this);
            super.storeProperties();
        }
    }
    
    Set/*<Project>*/ getSubModules() {
        return getModulesListModel().getSubModules();
    }
    
    Set/*<Project>*/ getOrigSubModules() {
        return origSubModules;
    }

    /**
     * Returns list model of module's dependencies regarding the currently
     * selected platform.
     */
    SuiteSubModulesListModel getModulesListModel() {
        if (moduleListModel == null) {
            moduleListModel = new SuiteSubModulesListModel(subModules);
        }
        return moduleListModel;
    }
    
}
