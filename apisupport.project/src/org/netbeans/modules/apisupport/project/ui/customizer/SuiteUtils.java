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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProjectGenerator;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Utility methods for miscellaneous suite module operations like moving its
 * submodules between individual suites, removing submodules, adding and other
 * handy methods.
 * All methods should be run within <em>ProjectManager.mutex().writeAccess()<em> (???)
 *
 * @author Martin Krauskopf
 */
public class SuiteUtils {
    
    // XXX don't know exact allowed ant property format. We might use something
    // more gentle like "\\$\\{[\\p{Alnum}-_\\.]+\\}"?
    private static final String ANT_PROPERTY_REGEXP = "\\$\\{\\p{Graph}+\\}"; // NOI18N
    
    private static final String MODULES_PROPERTY = "modules"; // NOI18N
    
    private SuiteUtils() {};
    
    public static void replaceSubModules(SuiteProperties suiteProps) throws IOException {
        removeRemovedSubModules(suiteProps);
        addModules(suiteProps);
    }
    
    /**
     * Detach the given <code>subModule</code> from the given
     * <code>suite</code>. This actually means deleting
     * <em>nbproject/suite.properties</em> and eventually
     * <em>nbproject/private/suite-private.properties</em> if it exists from
     * <code>subModule</code>'s base directory. Also set the
     * <code>subModule</code>'s type to standalone. Then it intelligently clear
     * <code>suite</code>'s properties (see {@link #removeSubModuleFromSuite})
     * for details).
     * <p>
     * Also saves both, <code>suite</code> and <code>subModule</code>, using
     * {@link ProjectManager#saveProject}.
     */
    private static void detachSubModuleFromSuite(Project suite, Project subModule) {
        NbModuleTypeProvider nmtp = (NbModuleTypeProvider) subModule.
                getLookup().lookup(NbModuleTypeProvider.class);
        if (nmtp.getModuleType() == NbModuleTypeProvider.SUITE_COMPONENT) {
            try {
                // clean up a submodule
                
                // XXX after a few calls this finally calls
                // AntProjectListener.configurationXmlChanged() which in turns
                // tries to load subModule's classpath (see
                // NbModuleProject.computeModuleClasspath). But it is still
                // computing classpath like if the subModule was
                // suite-component because project.xml is not written yet. Has
                // to be solved somehow. Probably need more controll over the
                // process or manually write or... ?
                setNbModuleType(subModule, NbModuleTypeProvider.STANDALONE);
                
                File subModuleF = FileUtil.toFile(subModule.getProjectDirectory());
                FileObject fo = FileUtil.toFileObject(new File(subModuleF, "nbproject/suite.properties")); // NOI18N
                if (fo != null) {
                    fo.delete();
                }
                fo = FileUtil.toFileObject(new File(subModuleF, "nbproject/private/suite-private.properties")); // NOI18N
                if (fo != null) {
                    fo.delete();
                }
                
                // copy platform.properties if it doesn't exist yet
                File suiteF = FileUtil.toFile(suite.getProjectDirectory());
                FileObject plafPropsFO = FileUtil.toFileObject(new File(suiteF, "/nbproject/platform.properties")); // NOI18N
                FileObject subModuleNbProject = FileUtil.toFileObject(new File(subModuleF, "nbproject")); // NOI18N
                if (subModuleNbProject.getFileObject("platform.properties") == null) {
                    FileUtil.copyFile(plafPropsFO, subModuleNbProject, "platform"); // NOI18N
                }
                ProjectManager.getDefault().saveProject(subModule);
                
                // clean up a suite
                removeSubModuleFromSuite(suite, subModule);
                ProjectManager.getDefault().saveProject(suite);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }
    
    /**
     * Use {@link #detachSubModuleFromSuite} for complete detaching.
     * <p>
     * Intelligently (XXX not so truthful in the meantime) removes the given
     * <code>subModule</code> from the given <code>suite</code>'s properties
     * and <b>saves</b> those properties into a disk.
     * </p>
     */
    private static void removeSubModuleFromSuite(Project suite, Project subModule) throws IOException {
        // load project.properties
        FileObject suiteFO = suite.getProjectDirectory();
        File suiteF = FileUtil.toFile(suiteFO);
        FileObject subModuleFO = subModule.getProjectDirectory();
        File subModuleF = FileUtil.toFile(subModuleFO);
        FileObject projectPropsFO = suiteFO.getFileObject(SuiteProjectGenerator.PROJECT_PROPERTIES_PATH);
        assert projectPropsFO != null : "Suite project doesn't have project.properties"; // NOI18N
        EditableProperties projectProps = loadProperties(projectPropsFO);
        // load private.properties
        FileObject privatePropsFO = suiteFO.getFileObject(SuiteProjectGenerator.PRIVATE_PROPERTIES_PATH);
        EditableProperties privateProps = privatePropsFO == null ? new EditableProperties(true) : loadProperties(privatePropsFO);
        
        if (removeSubModule(projectProps, privateProps, suiteF, subModuleF)) {
            // store properties files to a disk
            Util.storeProperties(projectPropsFO, projectProps);
            if (privatePropsFO != null) {
                Util.storeProperties(privatePropsFO, privateProps);
            }
        }
    }
    
    /**
     * Adjust <em>modules</em> property together with removing appropriate
     * other properties from <code>projectProps</code> and
     * <code>privateProps</code>.
     *
     * @return wheter something has changed or not
     */
    private static boolean removeSubModule(
            EditableProperties projectProps, EditableProperties privateProps,
            File suiteF, File subModuleF) {
        String modulesProp = projectProps.getProperty(MODULES_PROPERTY);
        boolean removed = false;
        if (modulesProp != null) {
            List pieces = new ArrayList(Arrays.asList(PropertyUtils.tokenizePath(modulesProp)));
            for (Iterator it = pieces.iterator(); it.hasNext(); ) {
                String module = (String) it.next();
                // every submodules created by GUI customizer has its own
                // property but user should add manually path into the modules
                // property directly.
                if (module.matches(ANT_PROPERTY_REGEXP)) {
                    String key = module.substring(2, module.length() - 1);
                    
                    String value = projectProps.getProperty(key);
                    if (value != null &&
                            PropertyUtils.resolveFile(suiteF, value).equals(subModuleF)) {
                        projectProps.remove(key);
                        it.remove();
                        removed = true;
                        break;
                    }
                    
                    value = privateProps.getProperty(key);
                    if (value != null &&
                            new File(value).equals(subModuleF)) {
                        privateProps.remove(key);
                        it.remove();
                        removed = true;
                        break;
                    }
                } else {
                    if (new File(module).equals(subModuleF)) {
                        it.remove();
                        removed = true;
                        break;
                    }
                }
            }
            if (removed) {
                String [] newPieces = new String[pieces.size()];
                projectProps.setProperty(MODULES_PROPERTY, (String[]) pieces.toArray(newPieces));
            }
        }
        return removed;
    }
    
    private static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        EditableProperties projectProps = new EditableProperties(true);
        InputStream is = propsFO.getInputStream();
        try {
            projectProps.load(is);
        } finally {
            is.close();
        }
        return projectProps;
    }
    
    /**
     * Removes removed submodues only. Calls {@link #detachSubModuleFromSuite}
     * for each such a module.
     */
    private static void removeRemovedSubModules(SuiteProperties suiteProps) throws IOException {
        Set/*<Project>*/ subModules = suiteProps.getSubModules();
        Set/*<Project>*/ origSubModules = suiteProps.getOrigSubModules();
        for (Iterator it = origSubModules.iterator(); it.hasNext(); ) {
            Project subModule = (Project) it.next();
            if (!subModules.contains(subModule)) {
                detachSubModuleFromSuite(suiteProps.getProject(), subModule);
                removeSubModule(suiteProps.getProjectProperties(),
                        suiteProps.getPrivateProperties(),
                        suiteProps.getProjectDirectoryFile(),
                        FileUtil.toFile(subModule.getProjectDirectory()));
            }
        }
    }
    
    private static void addModules(SuiteProperties suiteProps) throws IOException {
        Set/*<Project>*/ subModules = suiteProps.getSubModules();
        Set/*<Project>*/ origSubModules = suiteProps.getOrigSubModules();
        for (Iterator it = subModules.iterator(); it.hasNext(); ) {
            Project subModule = (Project) it.next();
            if (origSubModules.contains(subModule)) {
                continue;
            }
            Project suite = getSuite(subModule);
            if (suite != null) {
                // detach module from his current suite
                detachSubModuleFromSuite(suite, subModule);
            }
            // attach it to the new suite
            attachSubModuleToSuite(suiteProps, subModule,
                    suiteProps.getHelper().getProjectDirectory());
        }
    }
    
    /** Returns suite for the given suite component. */
    private static Project getSuite(Project comp) throws IOException {
        SuiteProvider sp = (SuiteProvider) comp.getLookup().lookup(SuiteProvider.class);
        Project suite = null;
        if (sp != null && sp.getSuiteDirectory() != null) {
            suite = ProjectManager.getDefault().findProject(
                    FileUtil.toFileObject(sp.getSuiteDirectory()));
        }
        return suite;
    }
    
    // XXX stolen from NbModuleProjectGenerator.appendToSuite - get rid of
    // duplicated code!
    private static void attachSubModuleToSuite(SuiteProperties suiteProps, Project subModule,
            FileObject suiteDir) throws IOException {
        
        // adjust suite project's properties
        File projectDirF = FileUtil.toFile(subModule.getProjectDirectory());
        File suiteDirF = FileUtil.toFile(suiteDir);
        String projectPropKey = "project." + projectDirF.getName(); // NOI18N
        if (CollocationQuery.areCollocated(projectDirF, suiteDirF)) {
            // XXX the generating of relative path doesn't seem's too clever, check it
            suiteProps.setProperty(projectPropKey,
                    PropertyUtils.relativizeFile(suiteDirF, projectDirF));
        } else {
            suiteProps.setPrivateProperty(projectPropKey, projectDirF.getAbsolutePath());
        }
        String modulesProp = suiteProps.getProperty(MODULES_PROPERTY);
        if (modulesProp == null) {
            modulesProp = "";
        }
        if (modulesProp.length() > 0) {
            modulesProp += ":"; // NOI18N
        }
        modulesProp += "${" + projectPropKey + "}"; // NOI18N
        suiteProps.setProperty(MODULES_PROPERTY, modulesProp.split("(?<=:)", -1)); // NOI18N
        
        // adjust subModule's properties
        NbModuleProjectGenerator.createSuiteProperties(subModule.getProjectDirectory(), suiteDirF);
        setNbModuleType(subModule, NbModuleTypeProvider.SUITE_COMPONENT);
        ProjectManager.getDefault().saveProject(subModule);
    }
    
    private static void setNbModuleType(Project module, NbModuleTypeProvider.NbModuleType type) throws IOException {
        // XXX do not cast to NbModuleProject - find better way (e.g. provide method in NbModuleTypeProvider)
        ProjectXMLManager pxm = new ProjectXMLManager(((NbModuleProject) module).getHelper());
        pxm.setModuleType(type);
    }
    
}
