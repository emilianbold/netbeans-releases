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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Utility methods for miscellaneous suite module operations like moving its
 * subModules between individual suites, removing subModules, adding and other
 * handy methods.
 *
 * @author Martin Krauskopf
 */
public final class SuiteUtils {
    
    // XXX also match "${dir}/somedir/${anotherdir}"
    private static final String ANT_PURE_PROPERTY_REFERENCE_REGEXP = "\\$\\{\\p{Graph}+\\}"; // NOI18N
    
    private static final String PRIVATE_PLATFORM_PROPERTIES = "nbproject/private/platform-private.properties"; // NOI18N
    
    static final String MODULES_PROPERTY = "modules"; // NOI18N
    
    private final SuiteProperties suiteProps;
    
    private SuiteUtils(final SuiteProperties suiteProps) {
        this.suiteProps = suiteProps;
    }
    
    /**
     * Reads needed information from the given {@link SuiteProperties} and
     * appropriately replace its all modules with new ones.
     * Acquires write access.
     */
    public static void replaceSubModules(final SuiteProperties suiteProps) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    SuiteUtils utils = new SuiteUtils(suiteProps);
                    Set/*<Project>*/ currentModules = suiteProps.getSubModules();
                    Set/*<Project>*/ origSubModules = suiteProps.getOrigSubModules();
                    
                    // remove removed modules
                    for (Iterator it = origSubModules.iterator(); it.hasNext(); ) {
                        NbModuleProject origModule = (NbModuleProject) it.next();
                        if (!currentModules.contains(origModule)) {
                            Util.err.log("Removing module: " + origModule); // NOI18N
                            utils.removeModule(origModule);
                        }
                    }
                    
                    // add new modules
                    for (Iterator it = currentModules.iterator(); it.hasNext(); ) {
                        NbModuleProject currentModule = (NbModuleProject) it.next();
                        if (SuiteUtils.contains(suiteProps.getProject(), currentModule)) {
                            Util.err.log("Module \"" + currentModule + "\" or a module with the same CNB is already contained in the suite."); // NOI18N
                            continue;
                        }
                        utils.addModule(currentModule);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Adds the given module to the given suite if it is not already contained
     * there. If the module is already suite component of another suite it will
     * be appropriatelly removed from it (i.e moved from module's current suite
     * to the given suite).
     * Acquires write access.
     */
    public static void addModule(final SuiteProject suite, final NbModuleProject project) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    final SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(),
                            suite.getEvaluator(), getSubProjects(suite));
                    if (!SuiteUtils.contains(suite, project)) {
                        SuiteUtils utils = new SuiteUtils(suiteProps);
                        utils.addModule(project);
                        suiteProps.storeProperties();
                    } else {
                        Util.err.log("Module \"" + project + "\" or a module with the same CNB is already contained in the suite."); // NOI18N
                    }
                    ProjectManager.getDefault().saveProject(suite);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Removes module from its current suite if the given module is a suite
     * component. Does nothing otherwise.
     * Acquires write access.
     */
    public static void removeModuleFromSuite(final NbModuleProject suiteComponent) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    SuiteProject suite = SuiteUtils.findSuite(suiteComponent);
                    if (suite != null) {
                        // detach module from its current suite
                        SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(),
                                suite.getEvaluator(), getSubProjects(suite));
                        SuiteUtils utils = new SuiteUtils(suiteProps);
                        utils.removeModule(suiteComponent);
                        suiteProps.storeProperties();
                        ProjectManager.getDefault().saveProject(suite);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    private void addModule(final NbModuleProject project) throws IOException, IllegalArgumentException {
        SuiteUtils.removeModuleFromSuite(project);
        // attach it to the new suite
        attachSubModuleToSuite(project);
    }
    
    /**
     * Detach the given <code>subModule</code> from the suite. This actually
     * means deleting its <em>nbproject/suite.properties</em> and eventually
     * <em>nbproject/private/suite-private.properties</em> if it exists from
     * <code>subModule</code>'s base directory. Also set the
     * <code>subModule</code>'s type to standalone. Then it accordingly set the
     * <code>suite</code>'s properties (see {@link #removeFromProperties})
     * for details).
     * <p>
     * Also saves <code>subModule</code> using {@link ProjectManager#saveProject}.
     * </p>
     */
    private void removeModule(final NbModuleProject subModule) {
        NbModuleTypeProvider.NbModuleType type = Util.getModuleType(subModule);
        assert type == NbModuleTypeProvider.SUITE_COMPONENT : "Not a suite component";
        try {
            subModule.getProjectDirectory().getFileSystem().runAtomicAction(new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    subModule.setRunInAtomicAction(true);
                    try {
                        // remove both suite properties files
                        FileObject subModuleDir = subModule.getProjectDirectory();
                        FileObject fo = subModuleDir.getFileObject(
                                "nbproject/suite.properties"); // NOI18N
                        if (fo != null) {
                            fo.delete();
                        }
                        fo = subModuleDir.getFileObject(
                                "nbproject/private/suite-private.properties"); // NOI18N
                        if (fo != null) {
                            fo.delete();
                        }
                        
                        // copy suite's platform.properties to the module (needed by standalone module)
                        FileObject plafPropsFO = suiteProps.getProject().getProjectDirectory().
                                getFileObject("nbproject/platform.properties"); // NOI18N
                        FileObject subModuleNbProject = subModuleDir.getFileObject("nbproject"); // NOI18N
                        if (subModuleNbProject.getFileObject("platform.properties") == null) { // NOI18N
                            FileUtil.copyFile(plafPropsFO, subModuleNbProject, "platform"); // NOI18N
                        }
                        EditableProperties props = subModule.getHelper().getProperties(PRIVATE_PLATFORM_PROPERTIES);
                        if (props.getProperty("user.properties.file") == null) { // NOI18N
                            String nbuser = System.getProperty("netbeans.user"); // NOI18N
                            if (nbuser != null) {
                                props.setProperty("user.properties.file", new File(nbuser, "build.properties").getAbsolutePath()); // NOI18N
                                subModule.getHelper().putProperties(PRIVATE_PLATFORM_PROPERTIES, props);
                            } else {
                                Util.err.log("netbeans.user system property is not defined. Skipping " + PRIVATE_PLATFORM_PROPERTIES + " creation."); // NOI18N
                            }
                        }
                        
                        SuiteUtils.setNbModuleType(subModule, NbModuleTypeProvider.STANDALONE);
                        // save subModule
                        ProjectManager.getDefault().saveProject(subModule);
                    } finally {
                        subModule.setRunInAtomicAction(false);
                    }
                }
            });
            
            // now clean up the suite
            removeFromProperties(subModule);
        } catch (IOException ex) {
            ErrorManager.getDefault().notify(ex);
        }
    }
    
    /**
     * Adjust <em>modules</em> property together with removing appropriate
     * other properties from <code>projectProps</code> and
     * <code>privateProps</code>.
     *
     * @return wheter something has changed or not
     */
    private boolean removeFromProperties(NbModuleProject moduleToRemove) {
        String modulesProp = suiteProps.getProperty(MODULES_PROPERTY);
        boolean removed = false;
        if (modulesProp != null) {
            List pieces = new ArrayList(Arrays.asList(PropertyUtils.tokenizePath(modulesProp)));
            for (Iterator piecesIt = pieces.iterator(); piecesIt.hasNext(); ) {
                String unevaluated = (String) piecesIt.next();
                String evaluated = suiteProps.getEvaluator().evaluate(unevaluated);
                if (evaluated == null) {
                    Util.err.log("Cannot evaluate " + unevaluated + " property."); // NOI18N
                    continue;
                }
                if (moduleToRemove.getProjectDirectory() !=
                        suiteProps.getHelper().resolveFileObject(evaluated)) {
                    continue;
                }
                piecesIt.remove();
                String[] newModulesProp = getAntProperty(pieces);
                suiteProps.setProperty(MODULES_PROPERTY, newModulesProp);
                removed = true;
                // if the value is pure reference also tries to remove that
                // reference which is nice to have. Otherwise just do nothing.
                if (unevaluated.matches(ANT_PURE_PROPERTY_REFERENCE_REGEXP)) {
                    String key = unevaluated.substring(2, unevaluated.length() - 1);
                    suiteProps.removeProperty(key);
                    suiteProps.removePrivateProperty(key);
                }
                break;
            }
        }
        if (!removed) {
            Util.err.log("Removing of " + moduleToRemove + " was unsuccessful."); // NOI18N
        }
        return removed;
    }
    
    private void attachSubModuleToSuite(Project subModule) throws IOException {
        // adjust suite project's properties
        File projectDirF = FileUtil.toFile(subModule.getProjectDirectory());
        File suiteDirF = suiteProps.getProjectDirectoryFile();
        String projectPropKey = generatePropertyKey(subModule);
        if (CollocationQuery.areCollocated(projectDirF, suiteDirF)) {
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
    
    /** Generates unique property key suitable for a given modules. */
    private String generatePropertyKey(final Project subModule) {
        String key = "project." + ProjectUtils.getInformation(subModule).getName(); // NOI18N
        String[] keys = suiteProps.getProperty(MODULES_PROPERTY).split("(?<=:)", -1); // NOI18N
        int index = 0;
        while (Arrays.binarySearch(keys, "${" + key + "}") >= 0) { // NOI18N
            key += "_" + ++index; // NOI18N
        }
        return key;
    }
    
    private static void setNbModuleType(Project module, NbModuleTypeProvider.NbModuleType type) throws IOException {
        ProjectXMLManager pxm = new ProjectXMLManager(((NbModuleProject) module));
        pxm.setModuleType(type);
    }
    
    private String[] getAntProperty(final Collection pieces) {
        List l = new ArrayList();
        for (Iterator it = pieces.iterator(); it.hasNext();) {
            String piece = (String) it.next() + (it.hasNext() ? ":" : ""); // NOI18N
            l.add(piece);
        }
        String [] newPieces = new String[l.size()];
        return (String[]) l.toArray(newPieces);
    }
    
    /**
     * Returns suite for the given suite component.
     * Acquires write access.
     */
    public static SuiteProject findSuite(final Project suiteComponent) throws IOException {
        try {
            return (SuiteProject) ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction(){
                public Object run() throws Exception {
                    SuiteProvider sp = (SuiteProvider) suiteComponent.getLookup().lookup(SuiteProvider.class);
                    Project suite = null;
                    if (sp != null && sp.getSuiteDirectory() != null) {
                        suite = ProjectManager.getDefault().findProject(
                                FileUtil.toFileObject(sp.getSuiteDirectory()));
                    }
                    return (SuiteProject) suite;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }

    /**
     * Returns whether a given suite already contains a given project or a
     * project with the same code name base.
     */
    public static boolean contains(final SuiteProject suite, final NbModuleProject project) {
        Set/*<Project>*/ subModules = getSubProjects(suite);
        if (subModules.contains(project)) {
            return true;
        }
        for (Iterator it = subModules.iterator(); it.hasNext();) {
            NbModuleProject p = (NbModuleProject) it.next();
            if (p.getCodeNameBase().equals(project.getCodeNameBase())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Utility method to acquire modules contains within a given suite. Just
     * delegates to {@link SubprojectProvider#getSubprojects()}.
     */
    public static Set/*<Project>*/ getSubProjects(final Project suite) {
        SubprojectProvider spp = (SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class);
        return spp.getSubprojects();
    }
    
}
