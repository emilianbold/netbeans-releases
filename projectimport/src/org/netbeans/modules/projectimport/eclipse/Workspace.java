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

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.openide.ErrorManager;

/**
 * Provides access to an eclipse workspace.
 *
 * @author mkrauskopf
 */
public final class Workspace {
    
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
        return (Collection) userLibraries.get(libRawPath);
    }
    
    /**
     * Tries to find an <code>EclipseProject</code> in the workspace and either
     * returns its instance or null in the case it's not found.
     */
    EclipseProject getProjectByRawPath(String rawPath) {
        for (Iterator it = projects.iterator(); it.hasNext(); ) {
            EclipseProject prj = (EclipseProject) it.next();
            // rawpath = /name
            if (prj.getName().equals(rawPath.substring(1))) {
                return prj;
            }
        }
        //        return (EclipseProject) projects.get(rawPath);
        return null;
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
        if (!DEFAULT_JRE_CONTAINER.equals(jreContainer)) {
            // JRE name seems to be after the last slash
            jreContainer = jreContainer.substring(jreContainer.lastIndexOf('/') + 1);
        }
        if (jreContainer != null) {
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
