/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.LoggerFactory;
import org.openide.ErrorManager;

/**
 * Provides access to an eclipse workspace.
 *
 * @author mkrauskopf
 */
public final class Workspace {

    /** Logger for this class. */
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(Workspace.class);
    
    /** Represents variable in Eclipse project's classpath. */
    static class Variable {
        private String name;
        private String location;
        
        String getName() {
            return name;
        }
        
        void setName(String name) {
            this.name = name;
        }
        
        String getLocation() {
            return location;
        }
        
        void setLocation(String location) {
            this.location = location;
        }
        
        public String toString() {
            return name + " = " + location;
        }
        
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Variable)) return false;
            final Variable var = (Variable) obj;
            if (name != null ? !name.equals(var.name) :var.name != null)
                return false;
            if (location != null ? !location.equals(var.location) : var.location != null)
                return false;
            return true;
        }
        
        public int hashCode() {
            int result = 17;
            result = 37 * result + System.identityHashCode(name);
            result = 37 * result + System.identityHashCode(location);
            return result;
        }
    }
    
    private static final String RUNTIME_SETTINGS =
            ".metadata/.plugins/org.eclipse.core.runtime/.settings/";
    static final String CORE_PREFERENCE =
            RUNTIME_SETTINGS + "org.eclipse.jdt.core.prefs";
    static final String LAUNCHING_PREFERENCES =
            RUNTIME_SETTINGS + "org.eclipse.jdt.launching.prefs";
    
    static final String RESOURCE_PROJECTS_DIR =
            ".metadata/.plugins/org.eclipse.core.resources/.projects";
    
    static final String DEFAULT_JRE_CONTAINER =
            "org.eclipse.jdt.launching.JRE_CONTAINER";
    
    private File corePrefFile;
    private File launchingPrefsFile;
    private File resourceProjectsDir;
    private File workspaceDir;
    
    private Set variables;
    private Set projects = new HashSet();
    private Map jreContainers;
    private Map userLibraries;
    
    /**
     * Returns <code>Workspace</code> instance representing Eclipse Workspace
     * found in the given <code>workspaceDir</code>. If a workspace is not found
     * in the specified directory, <code>null</code> is returned.
     *
     * @return either a <code>Workspace</code> instance or null if a given
     *      <code>workspaceDir</code> doesn't contain valid Eclipse workspace.
     */
    static Workspace createWorkspace(File workspaceDir) {
        if (!EclipseUtils.isRegularWorkSpace(workspaceDir)) {
            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL,
                    "There is not a regular workspace in " + workspaceDir);
            return null;
        }
        Workspace workspace = new Workspace(workspaceDir);
        return workspace;
    }
    
    /** Sets up a workspace directory. */
    private Workspace(File workspaceDir) {
        this.workspaceDir = workspaceDir;
        corePrefFile = new File(workspaceDir, CORE_PREFERENCE);
        launchingPrefsFile = new File(workspaceDir, LAUNCHING_PREFERENCES);
        resourceProjectsDir = new File(workspaceDir, RESOURCE_PROJECTS_DIR);
    }
    
    File getDirectory() {
        return workspaceDir;
    }
    
    File getCorePreferenceFile() {
        return corePrefFile;
    }
    
    File getLaunchingPrefsFile() {
        return launchingPrefsFile;
    }
    
    File getResourceProjectsDir() {
        return resourceProjectsDir;
    }
    
    void addVariable(Variable var) {
        if (variables == null) {
            variables = new HashSet();
        }
        variables.add(var);
    }
    
    Set getVariables() {
        return variables;
    }
    
    void setJREContainers(Map jreContainers) {
        this.jreContainers = jreContainers;
    }
    
    void addProject(EclipseProject project) {
        projects.add(project);
    }
    
    void addUserLibrary(String libName, Collection jars) {
        if (userLibraries == null) {
            userLibraries = new HashMap();
        }
        userLibraries.put(libName, jars);
    }
    
    Collection getJarsForUserLibrary(String libRawPath) {
        Collection retVal = Collections.EMPTY_LIST;
        if (userLibraries != null) {
            retVal = (Collection) userLibraries.get(libRawPath);
        }
        return retVal;
    }
    
    /**
     * Tries to find an <code>EclipseProject</code> in the workspace and either
     * returns its instance or null in the case it's not found.
     */
    EclipseProject getProjectByRawPath(String rawPath) {
        EclipseProject project = null;
        for (Iterator it = projects.iterator(); it.hasNext(); ) {
            EclipseProject prj = (EclipseProject) it.next();
            // rawpath = /name
            if (prj.getName().equals(rawPath.substring(1))) {
                project = prj;
            }
        }
        if (project == null) {
            logger.info("Project with raw path \"" + rawPath + "\" cannot" + // NOI18N
                    " be found in project list: " + projects); // NOI18N
        }
        return project;
    }
    
    public Set getProjects() {
        return projects;
    }
    
    String getProjectAbsolutePath(String projectName) {
        for (Iterator it = projects.iterator(); it.hasNext(); ) {
            EclipseProject project = ((EclipseProject) it.next());
            if (project.getName().equals(projectName)) {
                return project.getDirectory().getAbsolutePath();
            }
        }
        return null;
    }
    
    /**
     * Returns JDK used for compilation of project with the specified
     * projectDirName.
     */
    String getJDKDirectory(String jreContainer) {
        if (jreContainer != null) {
            if (!DEFAULT_JRE_CONTAINER.equals(jreContainer)) {
                // JRE name seems to be after the last slash
                jreContainer = jreContainer.substring(jreContainer.lastIndexOf('/') + 1);
            }
            for (Iterator it = jreContainers.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                if (entry.getKey().equals(jreContainer)) {
                    return (String) entry.getValue();
                }
            }
        }
        return null;
    }
}
