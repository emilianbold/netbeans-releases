/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.projectimport.eclipse;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.Set;

/**
 * Represents Eclipse project structure.
 *
 * @author mkrauskopf
 */
public final class EclipseProject implements Comparable {
    
    static final String PROJECT_FILE = ".project";
    static final String CLASSPATH_FILE = ".classpath";
    
    private Workspace workspace;
    
    private String name;
    private boolean internal = true;
    private boolean javaNature;
    private ClassPath cp;
    private Set links;
    
    private File projectDir;
    private File cpFile;
    private File prjFile;
    private String jdkDirectory;
    
    /**
     * Returns <code>EclipseProject</code> instance representing Eclipse project
     * found in the given <code>projectDir</code>. If a project is not found in
     * the specified directory, <code>null</code> is returned.
     *
     * @return either a <code>EclipseProject</code> instance or null if a given
     *      <code>projectDir</code> doesn't contain valid Eclipse project.
     */
    static EclipseProject createProject(File projectDir) {
        if (!EclipseUtils.isRegularProject(projectDir)) {
            return null;
        }
        EclipseProject project = new EclipseProject(projectDir);
        return project;
    }
    
    /** Sets up a project directory. */
    private EclipseProject(File projectDir) {
        this.projectDir = projectDir;
        this.cpFile = new File(projectDir, CLASSPATH_FILE);
        this.prjFile = new File(projectDir, PROJECT_FILE);
    }
    
    void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
    
    public Workspace getWorkspace() {
        return workspace;
    }
    
    void setClassPath(ClassPath cp) {
        this.cp = cp;
    }
    
    ClassPath getClassPath() {
        return cp;
    }
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name = name;
    }
    
    void setInternal(boolean internal) {
        this.internal = internal;
    }
    
    public boolean isInternal() {
        return internal;
    }
    
    public File getDirectory() {
        return projectDir;
    }
    
    File getProjectFile() {
        return prjFile;
    }
    
    File getClassPathFile() {
        return cpFile;
    }
    
    public boolean hasJavaNature() {
        return javaNature;
    }

    void setJavaNature(boolean javaNature) {
        this.javaNature = javaNature;
    }
    
    /**
     * Returns JDK directory for platform this project uses. Can be null in a
     * case when a JDK was set for an eclipse project in Eclipse then the
     * directory with JDK was deleted from filesystem and then a project is
     * imported to NetBeans.
     *
     * @return JDK directory for the project
     */
    public String getJDKDirectory() {
        if (jdkDirectory == null && workspace != null) {
            jdkDirectory = workspace.getJDKDirectory(cp.getJREContainer());
            // jdkDirectory = workspace.getJDKDirectory(projectDir.getName());
        }
        return jdkDirectory;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getSourceRoots() {
        return cp.getSourceRoots();
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public File[] getAllSourceRootsFiles() {
        // internal sources
        Object[] srcRoots = cp.getSourceRoots().toArray();
        // external sources
        Object[] extSrcRoots = cp.getExternalSourceRoots().toArray();
        File[] files = new File[srcRoots.length + extSrcRoots.length];
        for (int i = 0; i < srcRoots.length; i++) {
            files[i] = new File(((ClassPathEntry)srcRoots[i]).getAbsolutePath());
        }
        for (int i = 0; i < extSrcRoots.length; i++) {
            files[srcRoots.length + i] = new File(((ClassPathEntry)extSrcRoots[i]).getAbsolutePath());
        }
        return files;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public File[] getAllLibrariesFiles() {
        // internal libraries
        Object[] libs = cp.getLibraries().toArray();
        // external libraries
        Object[] extLibs = cp.getExternalLibraries().toArray();
        File[] files = new File[libs.length + extLibs.length];
        for (int i = 0; i < libs.length; i++) {
            files[i] = new File(((ClassPathEntry)libs[i]).getAbsolutePath());
        }
        for (int i = 0; i < extLibs.length; i++) {
            files[libs.length + i] = new File(((ClassPathEntry)extLibs[i]).getAbsolutePath());
        }
        return files;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getExternalSourceRoots() {
        return cp.getExternalSourceRoots();
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getLibraries() {
        return cp.getLibraries();
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getExternalLibraries() {
        return cp.getExternalLibraries();
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getProjectsEntries() {
        return cp.getProjects();
    }
    
    private Collection projectsWeDependOn;
    public Collection getProjects() {
        if (projectsWeDependOn == null) {
            projectsWeDependOn = new ArrayList();
            for (Iterator it = cp.getProjects().iterator(); it.hasNext(); ) {
                ClassPathEntry cp = (ClassPathEntry) it.next();
                projectsWeDependOn.add(workspace.getProjectByRawPath(
                        cp.getRawPath()));
            }
        }
        return projectsWeDependOn;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getVariables() {
        return cp.getVariables();
    }
    
    void addLink(ClassPath.Link link) {
        if (links == null) {
            links = new HashSet();
        }
        links.add(link);
    }
    
    /**
     * Inteligently sets absolute path for a given entry with recongnizing of
     * links, projects, variables, relative and absolute entries.
     * If it is not possible (e.g. workspace Varible is not found) sets abs.
     * path to null.
     */
    void setAbsolutePathForEntry(ClassPathEntry entry) {
        // set abs. path default (null)
        entry.setAbsolutePath(null);
        
        if (entry.getType() == ClassPathEntry.TYPE_VARIABLE) {
            Workspace.Variable var = getVariable(entry);
            if (var != null) {
                entry.setAbsolutePath(var.getLocation());
            }
            return;
        }
        if (entry.getType() == ClassPathEntry.TYPE_PROJECT) {
            if (workspace != null) {
                entry.setAbsolutePath(workspace.getProjectAbsolutePath(
                        entry.getRawPath().substring(1)));
            }
            //            else {
            //                ErrorManager.getDefault().log(ErrorManager.WARNING, "workspace == null");
            //            }
            return;
        }
        ClassPath.Link link = getLink(entry);
        if (link != null) {
            // change type from source to source link
            entry.setType(ClassPathEntry.TYPE_LINK);
            entry.setAbsolutePath(link.getLocation());
            return;
        }
        if (entry.isRawPathRelative()) {
            entry.setAbsolutePath(projectDir.getAbsolutePath() + File.separator
                    + entry.getRawPath());
        } else {
            entry.setAbsolutePath(entry.getRawPath());
        }
    }
    
    /**
     * Recongises if a given entry represents variable. If yes returns variable
     * it represents otherwise null. Note that this method returns null if
     * workspace wasn't set for this project.
     */
    Workspace.Variable getVariable(ClassPathEntry entry) {
        if (workspace == null) {
            // workspace wasn't set for this project
            return null;
        }
        Set variables = workspace.getVariables();
        if (variables != null) {
            for (Iterator it = workspace.getVariables().iterator(); it.hasNext(); ) {
                Workspace.Variable variable = (Workspace.Variable) it.next();
                if (variable.getName().equals(entry.getRawPath())) {
                    return variable;
                }
            }
        }
        return null;
    }
    
    /**
     * Recongises if a given entry represents link. If yes returns link it
     * represents otherwise null.
     */
    ClassPath.Link getLink(ClassPathEntry entry) {
        if (links != null) {
            for (Iterator it = links.iterator(); it.hasNext(); ) {
                ClassPath.Link link = (ClassPath.Link) it.next();
                if (link.getName().equals(entry.getRawPath())) {
                    return link;
                }
            }
        }
        return null;
    }
    
    public String toString() {
        return getName();
    }
    
    /* name is enough for now */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EclipseProject)) return false;
        final EclipseProject ePrj = (EclipseProject) obj;
        if (name != ePrj.name) return false;
        return true;
    }
    
    /* name is enough for now */
    public int hashCode() {
        int result = 17;
        result = 37 * result + System.identityHashCode(name);
        return result;
    }
    
    /**
     * Compares projects based on theirs <code>name</code>s. Projects which has
     * null-name will be last.
     */
    public int compareTo(Object o) {
        String name1 = getName();
        String name2 = null;
        if (o instanceof EclipseProject) {
            name2 = ((EclipseProject) o).getName();
        }
        if (name2 == null) {
            return (name1 == null ? 0 : -1);
        }
        return (name1 == null ? 1 : name1.compareToIgnoreCase(name2));
    }
}
