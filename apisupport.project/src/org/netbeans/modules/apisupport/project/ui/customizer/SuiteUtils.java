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
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.NbModuleProjectGenerator;
import org.netbeans.modules.apisupport.project.NbModuleTypeProvider;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.SuiteProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Utility methods for miscellaneous suite module operations like moving its
 * subModules between individual suites, removing subModules, adding and other
 * handy methods.
 * All methods should be run within <em>ProjectManager.mutex().writeAccess()<em> (???)
 *
 * @author Martin Krauskopf
 */
final class SuiteUtils {
    
    // XXX also match "${dir}/somedir/${anotherdir}"
    private static final String ANT_PURE_PROPERTY_REFERENCE_REGEXP = "\\$\\{\\p{Graph}+\\}"; // NOI18N
    
    private static final String MODULES_PROPERTY = "modules"; // NOI18N
    
    private final SuiteProperties suiteProps;
    
    private SuiteUtils(final SuiteProperties suiteProps) {
        this.suiteProps = suiteProps;
    }
    
    public static void replaceSubModules(final SuiteProperties suiteProps) throws IOException {
        SuiteUtils utils = new SuiteUtils(suiteProps);
        Set/*<Project>*/ currentModules = suiteProps.getSubModules();
        Set/*<Project>*/ origSubModules = suiteProps.getOrigSubModules();
        
        // remove removed modules
        for (Iterator it = origSubModules.iterator(); it.hasNext(); ) {
            NbModuleProject origModule = (NbModuleProject) it.next();
            if (!currentModules.contains(origModule)) {
                utils.removeModule(origModule);
            }
        }
        
        // add new modules
        for (Iterator it = currentModules.iterator(); it.hasNext(); ) {
            NbModuleProject currentModule = (NbModuleProject) it.next();
            if (origSubModules.contains(currentModule)) {
                continue;
            }
            SuiteProject suite = SuiteUtils.findSuite(currentModule);
            if (suite != null) {
                // detach module from its current suite
                SuiteUtils.removeModule(suite, currentModule);
                ProjectManager.getDefault().saveProject(suite);
            }
            // attach it to the new suite
            utils.attachSubModuleToSuite(currentModule);
        }
    }
    
    private static void removeModule(final SuiteProject suite, final NbModuleProject subModule) throws IOException {
        SubprojectProvider spp = (SubprojectProvider) suite.getLookup().lookup(SubprojectProvider.class);
        Set/*<Project>*/ subModules = spp.getSubprojects();
        SuiteProperties suiteProps = new SuiteProperties(suite, suite.getHelper(),
                suite.getEvaluator(), subModules);
        SuiteUtils utils = new SuiteUtils(suiteProps);
        utils.removeModule(subModule);
        suiteProps.storeProperties();
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
    private void removeModule(NbModuleProject subModule) {
        
        NbModuleTypeProvider nmtp = (NbModuleTypeProvider) subModule.
                getLookup().lookup(NbModuleTypeProvider.class);
        assert nmtp.getModuleType() == NbModuleTypeProvider.SUITE_COMPONENT : "Not a suite component"; // NOI18N
        try {
            // clean up a subModule
            
            // XXX after a few calls this finally calls
            // AntProjectListener.configurationXmlChanged() which in turns
            // tries to load subModule's classpath (see
            // NbModuleProject.computeModuleClasspath). But it is still
            // computing classpath like if the subModule was
            // suite-component because project.xml is not written yet. Has
            // to be solved somehow. Probably need more controll over the
            // process or manually write or... ?
            SuiteUtils.setNbModuleType(subModule, NbModuleTypeProvider.STANDALONE);
            
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
            
            // save subModule
            ProjectManager.getDefault().saveProject(subModule);
            
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
                suiteProps.setProperty(MODULES_PROPERTY, getAntProperty(pieces));
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
        return removed;
    }
    
    private void attachSubModuleToSuite(Project subModule) throws IOException {
        // adjust suite project's properties
        File projectDirF = FileUtil.toFile(subModule.getProjectDirectory());
        File suiteDirF = suiteProps.getProjectDirectoryFile();
        String projectPropKey = "project." + projectDirF.getName(); // NOI18N
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
    
    private static void setNbModuleType(Project module, NbModuleTypeProvider.NbModuleType type) throws IOException {
        // XXX do not cast to NbModuleProject - find better way (e.g. provide method in NbModuleTypeProvider)
        ProjectXMLManager pxm = new ProjectXMLManager(((NbModuleProject) module).getHelper());
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
    
    /** Returns suite for the given suite component. */
    private static SuiteProject findSuite(Project suiteComponent) throws IOException {
        SuiteProvider sp = (SuiteProvider) suiteComponent.getLookup().lookup(SuiteProvider.class);
        Project suite = null;
        if (sp != null && sp.getSuiteDirectory() != null) {
            suite = ProjectManager.getDefault().findProject(
                    FileUtil.toFileObject(sp.getSuiteDirectory()));
        }
        return (SuiteProject) suite;
    }
    
}
