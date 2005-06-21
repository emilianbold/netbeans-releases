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

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.apisupport.project.ui.customizer.ComponentFactory.SuiteSubModulesListModel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Provides convenient access to a lot of Suite Module's properties.
 *
 * @author Martin Krauskopf
 */
final class SuiteProperties extends ModuleProperties {
    
    private NbPlatform platform;
    private Set/*<Project>*/ subModules;
    
    // XXX don't know exact allowed ant property format. We might use something
    // more gentle like "\\$\\{[\\p{Alnum}-_\\.]+\\}"?
    private static final String ANT_PROPERTY_REGEXP = "\\$\\{\\p{Graph}+\\}"; // NOI18N
    
    private static final String MODULES_PROPERTY = "modules"; // NOI18N
    
    // models
    private SuiteSubModulesListModel moduleListModel;
    
    /**
     * Creates a new instance of SuiteProperties
     */
    SuiteProperties(AntProjectHelper helper, PropertyEvaluator evaluator,
            Set/*<Project>*/ subModules) {
        super(helper, evaluator);
        this.subModules = subModules;
        platform = NbPlatform.getPlatformByID(
                evaluator.getProperty("nbplatform.active")); // NOI18N
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
            removeAllModules();
            addModules(model.getSubModules());
            super.storeProperties();
        }
    }
    
    /**
     * Remove properties relating to submodules from
     * <em>nbproject/project.properties</em> and
     * <em>nbproject/private/private.properties</em> appropriately which
     * actually means <em>modules</em> property as well ass all properties for
     * individual submodules.
     */
    private void removeAllModules() {
        String modules = getProjectProperties().getProperty(MODULES_PROPERTY);
        if (modules != null) {
            String[] pieces = PropertyUtils.tokenizePath(modules);
            for (int i = 0; i < pieces.length; i++) {
                String module = pieces[i];
                // every submodules created by GUI customizer has its own
                // property but user should add manually path into the modules
                // property directly. So lets delete all project properties
                if (module.matches(ANT_PROPERTY_REGEXP)) {
                    String key = module.substring(2, module.length() - 1);
                    removeProperty(key);
                    removePrivateProperty(key);
                } // else nothing - a module is removed by removing "modules" property
            }
        }
        getProjectProperties().setProperty(MODULES_PROPERTY, "");
    }
    
    private void addModules(Set/*<Project>*/ projects) throws IOException {
        for (Iterator it = projects.iterator(); it.hasNext(); ) {
            Project prj = (Project) it.next();
            appendToSuite(prj.getProjectDirectory(), getHelper().getProjectDirectory());
        }
    }
    
    // XXX stolen from NbModuleProjectGenerator.appendToSuite - get rid of
    // duplicated code!
    private void appendToSuite(FileObject projectDir, FileObject suiteDir) throws IOException {
        File projectDirF = FileUtil.toFile(projectDir);
        File suiteDirF = FileUtil.toFile(suiteDir);
        String projectPropKey = "project." + projectDirF.getName(); // NOI18N
        if (CollocationQuery.areCollocated(projectDirF, suiteDirF)) {
            // XXX the generating of relative path doesn't seem's too clever, check it
            setProperty(projectPropKey,
                    PropertyUtils.relativizeFile(suiteDirF, projectDirF));
        } else {
            setPrivateProperty(projectPropKey, projectDirF.getAbsolutePath());
        }
        String modulesProp = getProperty("modules"); // NOI18N
        if (modulesProp == null) {
            modulesProp = "";
        }
        if (modulesProp.length() > 0) {
            modulesProp += ":"; // NOI18N
        }
        modulesProp += "${" + projectPropKey + "}"; // NOI18N
        setProperty("modules", modulesProp.split("(?<=:)", -1)); // NOI18N
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
