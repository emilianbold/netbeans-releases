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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.LoggerFactory;

/**
 * Represents Eclipse project structure.
 *
 * @author mkrauskopf
 */
public final class EclipseProject implements Comparable {
    
    /** Logger for this class. */
    private static final Logger logger =
            LoggerFactory.getDefault().createLogger(EclipseProject.class);
    
    static final String PROJECT_FILE = ".project";
    static final String CLASSPATH_FILE = ".classpath";
    
    private Workspace workspace;
    
    private String name;
    private boolean internal = true;
    private boolean javaNature;
    private ClassPath cp;
    private Set links;
    private Set otherNatures;
    
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
    
    /**
     * Returns project's name.
     */
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
    
    public Set getOtherNatures() {
        return otherNatures;
    }
    
    void addOtherNature(String nature) {
        if (otherNatures == null) {
            otherNatures = new HashSet();
        }
        logger.fine("Project " + getName() + " has another nature: " + // NOI18N
                nature);
        otherNatures.add(nature);
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
            logger.finest("Getting JDK directory for project " + this.getName()); // NOI18N                    
            jdkDirectory = workspace.getJDKDirectory(cp.getJREContainer());
            // jdkDirectory = workspace.getJDKDirectory(projectDir.getName());
        }
        return jdkDirectory;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getSourceRoots() {
        return cp.getSourceRoots();
    }
    
    /**
     * Returns map of file-label(java.io.File-java.lang.String) entries
     * representing Eclipse project's source roots.
     */
    public Map getAllSourceRoots() {
        Map rootsLabels = new HashMap();
        
        // internal sources
        Collection srcRoots = cp.getSourceRoots();
        for (Iterator it = srcRoots.iterator(); it.hasNext(); ) {
            ClassPathEntry cpe = (ClassPathEntry) it.next();
            rootsLabels.put(new File(cpe.getAbsolutePath()), cpe.getRawPath());
        }
        // external sources
        Collection extSrcRoots = cp.getExternalSourceRoots();
        for (Iterator it = extSrcRoots.iterator(); it.hasNext(); ) {
            ClassPathEntry cpe = (ClassPathEntry) it.next();
            rootsLabels.put(new File(cpe.getAbsolutePath()), cpe.getRawPath());
        }
        
        return rootsLabels;
    }
    
    /**
     * Returns all libraries on the project classpath as Collection of
     * <code>java.io.File</code>s.
     */
    public Collection getAllLibrariesFiles() {
        Collection files = new ArrayList();
        // internal libraries
        for (Iterator it = cp.getLibraries().iterator(); it.hasNext(); ) {
            files.add(new File(((ClassPathEntry)it.next()).getAbsolutePath()));
            
        }
        // external libraries
        for (Iterator it = cp.getExternalLibraries().iterator(); it.hasNext(); ) {
            files.add(new File(((ClassPathEntry)it.next()).getAbsolutePath()));
        }
        // jars in user libraries
        for (Iterator it = getUserLibrariesJars().iterator(); it.hasNext(); ) {
            files.add(new File((String) it.next()));
        }
        // variables
        for (Iterator it = cp.getVariables().iterator(); it.hasNext(); ) {
            ClassPathEntry entry = (ClassPathEntry)it.next();
            // in case a variable wasn't resolved
            if (entry.getAbsolutePath() != null) {
                files.add(new File(entry.getAbsolutePath()));
            }
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
    
    public Collection getUserLibrariesJars() {
        Collection userLibrariesJars = new HashSet();
        if (workspace != null) {
            for (Iterator it = cp.getUserLibraries().iterator(); it.hasNext(); ) {
                userLibrariesJars.addAll(
                        workspace.getJarsForUserLibrary((String) it.next()));
            }
        }
        return userLibrariesJars;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public Collection getProjectsEntries() {
        return cp.getProjects();
    }
    
    private Set projectsWeDependOn;
    
    /**
     * Returns collection of <code>EclipseProject</code> this project requires.
     */
    public Set getProjects() {
        if (workspace != null) {
            if (projectsWeDependOn == null) {
                projectsWeDependOn = new HashSet();
                for (Iterator it = cp.getProjects().iterator(); it.hasNext(); ) {
                    ClassPathEntry cp = (ClassPathEntry) it.next();
                    projectsWeDependOn.add(workspace.getProjectByRawPath(
                            cp.getRawPath()));
                }
            }
        }
        return projectsWeDependOn == null ?
            Collections.EMPTY_SET : projectsWeDependOn;
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
        
        // try to resolve entry as a CONTAINER
        if (entry.getType() == ClassPathEntry.TYPE_CONTAINER) {
            // we don't support CONTAINERs so we don't care about them here
            // (we support JRE/JDK containers but those are solved elsewhere)
            return;
        }
        
        // try to resolve entry as a VARIABLE
        if (entry.getType() == ClassPathEntry.TYPE_VARIABLE) {
            String rawPath = entry.getRawPath();
            int slashIndex = rawPath.indexOf('/');
            if (slashIndex != -1) {
                Workspace.Variable parent = getVariable(
                        rawPath.substring(0, slashIndex));
                if (parent != null) {
                    entry.setAbsolutePath(parent.getLocation() +
                            rawPath.substring(slashIndex));
                }
            } else {
                Workspace.Variable var = getVariable(entry);
                if (var != null) {
                    entry.setAbsolutePath(var.getLocation());
                }
            }
            return;
        }
        
        // try to resolve entry as a PROJECT
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
        
        // try to resolve entry as a LINK
        ClassPath.Link link = getLink(entry.getRawPath());
        if (link != null) {
            logger.finest("Found link for entry \"" + entry + "\": " + link); // NOI18N
            if (new File(link.getLocation()).exists()) {
                // change type from source to source link
                entry.setType(ClassPathEntry.TYPE_LINK);
                entry.setAbsolutePath(link.getLocation());
            } else {
                logger.info("Not able to resolve absolute path for classpath" + // NOI18N
                        " entry \"" + entry.getRawPath() + "\". This classpath" + // NOI18N
                        " entry is external source which points to PATH VARIABLE" + // NOI18N
                        " which points to final destination. This feature will be" + // NOI18N
                        " supported in future version of Importer."); // NOI18N
                entry.setType(ClassPathEntry.TYPE_UNKNOWN);
            }
            return;
        }
        
        // not VARIABLE, not PROJECT, not LINK -> either source root or library
        if (entry.isRawPathRelative()) {
            // internal src or lib
            entry.setAbsolutePath(projectDir.getAbsolutePath() + File.separator
                    + entry.getRawPath());
        } else {
            // external src or lib
            entry.setAbsolutePath(entry.getRawPath());
        }
    }
    
    /**
     * Find variable for the given variable rawPath. Note that this method
     * returns <code>null</code> if workspace wasn't set for the project.
     */
    private Workspace.Variable getVariable(String rawPath) {
        if (workspace == null) {
            // workspace wasn't set for this project
            logger.fine("Workspace wasn't set for the project \"" + getName() + "\""); // NOI18N
            return null;
        }
        Set variables = workspace.getVariables();
        if (variables != null) {
            for (Iterator it = workspace.getVariables().iterator(); it.hasNext(); ) {
                Workspace.Variable variable = (Workspace.Variable) it.next();
                if (variable.getName().equals(rawPath)) {
                    return variable;
                }
            }
        }
        logger.info("Cannot resolve variable for raw path: " + rawPath); // NOI18N
        return null;
    }
    
    /**
     * Recongises if a given entry represents variable. If yes returns variable
     * it represents otherwise null. Note that this method returns null if
     * workspace wasn't set for this project.
     */
    private Workspace.Variable getVariable(ClassPathEntry entry) {
        return getVariable(entry.getRawPath());
    }
    
    /**
     * Recongises if a given entry represents link. If yes returns link it
     * represents otherwise null.
     */
    private ClassPath.Link getLink(String linkName) {
        if (links != null) {
            for (Iterator it = links.iterator(); it.hasNext(); ) {
                ClassPath.Link link = (ClassPath.Link) it.next();
                if (link.getName().equals(linkName)) {
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
