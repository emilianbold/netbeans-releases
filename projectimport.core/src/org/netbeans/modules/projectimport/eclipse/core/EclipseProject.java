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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.projectimport.eclipse.core.Workspace.Variable;
import org.netbeans.modules.projectimport.eclipse.core.spi.DotClassPathEntry;
import org.netbeans.modules.projectimport.eclipse.core.spi.ProjectTypeFactory;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.Lookup;

/**
 * Represents Eclipse project structure.
 *
 * @author mkrauskopf
 */
public final class EclipseProject implements Comparable {

    /** Logger for this class. */
    private static final Logger logger =
            Logger.getLogger(EclipseProject.class.getName());
    
    private static final Lookup.Result<? extends ProjectTypeFactory> projectTypeFactories =
        Lookup.getDefault().lookupResult (ProjectTypeFactory.class);
    
    private Boolean importSupported;
    private ProjectTypeFactory projectFactory;
    private Set<EclipseProject> projectsWeDependOn;
    
    static final String PROJECT_FILE = ".project"; // NOI18N
    static final String CLASSPATH_FILE = ".classpath"; // NOI18N
    
    private Workspace workspace;
    
    private String name;
    private boolean internal = true;
    private DotClassPath cp;
    private Set<String> natures;
    
    private final File projectDir;
    private final File cpFile;
    private final File prjFile;
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
            logger.fine(projectDir + " doesn't contain regular Eclipse project."); // NOI18N
            return null;
        }
        return new EclipseProject(projectDir);
    }
    
    /** Sets up a project directory. */
    private EclipseProject(File projectDir) {
        this.projectDir = projectDir;
        this.cpFile = new File(projectDir, CLASSPATH_FILE);
        this.prjFile = new File(projectDir, PROJECT_FILE);
    }

    void setNatures(Set<String> natures) {
        this.natures = natures;
    }
    
    void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }
    
    public Workspace getWorkspace() {
        return workspace;
    }
    
    void setClassPath(DotClassPath cp) {
        this.cp = cp;
        calculateAbsolutePaths();
        evaluateContainers();
    }
    
    public List<DotClassPathEntry> getClassPathEntries() {
        return cp.getClassPathEntries();
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
    
    /**
     * Returns metadata file containing information about this projects. I.e.
     * normally <em>.project</em> file withing the project's directory. See
     * {@link #PROJECT_FILE}.
     */
    File getProjectFile() {
        return prjFile;
    }
    
    /**
     * Returns metadata file containing information about this projects. I.e.
     * normally <em>.classpath</em> file withing the project's directory. See
     * {@link #CLASSPATH_FILE}.
     */
    File getClassPathFile() {
        return cpFile;
    }
    
    public Set<String> getNatures() {
        return natures;
    }
    
    /**
     * Can this Eclipse project be converted to NetBeans or not?
     */
    public boolean isImportSupported() {
        if (importSupported == null) {
            importSupported = Boolean.FALSE;
            for (ProjectTypeFactory factory : projectTypeFactories.allInstances()) {
                if (factory.canHandle(getNatures())) {
                    this.projectFactory = factory;
                    importSupported = Boolean.TRUE;
                    break;
                }
            }
        }
        return importSupported.booleanValue();
    }
    
    ProjectTypeFactory getProjectTypeFactory() {
        return projectFactory;
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
            jdkDirectory = workspace.getJDKDirectory(cp.getJREContainer().getRawPath());
            logger.finest("Resolved JDK directory: " + jdkDirectory); // NOI18N
            // jdkDirectory = workspace.getJDKDirectory(projectDir.getName());
        }
        return jdkDirectory;
    }
    
    /** Convenient delegate to <code>ClassPath</code> */
    public List<DotClassPathEntry> getSourceRoots() {
        return cp.getSourceRoots();
    }
    
    /**
     * Returns collection of <code>EclipseProject</code> this project requires.
     */
    public Set<EclipseProject> getProjects() {
        if (workspace != null && projectsWeDependOn == null) {
            projectsWeDependOn = new HashSet();
            for (DotClassPathEntry cp : getClassPathEntries()) {
                if (cp.getKind() != DotClassPathEntry.Kind.PROJECT) {
                    continue;
                }
                EclipseProject prj = workspace.getProjectByRawPath(cp.getRawPath());
                if (prj != null) {
                    projectsWeDependOn.add(prj);
                }
            }
        }
        return projectsWeDependOn == null ?
            Collections.EMPTY_SET : projectsWeDependOn;
    }

    private void evaluateContainers() {
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            if (entry.getKind() != DotClassPathEntry.Kind.CONTAINER) {
                continue;
            }
            ClassPathContainerResolver.resolve(entry);
        }
    }
    
    void setupEvaluatedContainers() throws IOException {
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            if (entry.getKind() != DotClassPathEntry.Kind.CONTAINER) {
                continue;
            }
            ClassPathContainerResolver.setup(workspace, entry);
        }
    }
    
    void setupEnvironmentVariables() throws IOException {
        EditableProperties ep = PropertyUtils.getGlobalProperties();
        boolean changed = false;
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            if (entry.getKind() != DotClassPathEntry.Kind.VARIABLE) {
                continue;
            }
            String s = entry.getRawPath();
            int index = s.indexOf('/');
            s = s.substring(0,index);
            for (Variable v : workspace.getVariables()) {
                if (v.getName().equals(s)) {
                    if (ep.getProperty(s) == null) {
                        ep.setProperty(s, v.getLocation());
                        changed = true;
                    }
                    continue;
                }
            }
        }
        if (changed) {
            PropertyUtils.putGlobalProperties(ep);
        }
    }
    
    private void calculateAbsolutePaths() {
        for (DotClassPathEntry entry : cp.getClassPathEntries()) {
            setAbsolutePathForEntry(entry);
        }
        for (DotClassPathEntry entry : cp.getSourceRoots()) {
            setAbsolutePathForEntry(entry);
        }
        setAbsolutePathForEntry(cp.getOutput());
    }
    
    /**
     * Inteligently sets absolute path for a given entry with recongnizing of
     * links, projects, variables, relative and absolute entries.
     * If it is not possible (e.g. workspace Varible is not found) sets abs.
     * path to null.
     */
    private void setAbsolutePathForEntry(DotClassPathEntry entry) {
        // set abs. path default (null)
        entry.setAbsolutePath(null);
        
        // try to resolve entry as a CONTAINER
        if (entry.getKind() == DotClassPathEntry.Kind.CONTAINER) {
            // we don't support CONTAINERs so we don't care about them here
            // (we support JRE/JDK containers but those are solved elsewhere)
            return;
        }
        
        // try to resolve entry as a VARIABLE
        if (entry.getKind() == DotClassPathEntry.Kind.VARIABLE) {
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
        if (entry.getKind() == DotClassPathEntry.Kind.PROJECT) {
            if (workspace != null) {
                entry.setAbsolutePath(workspace.getProjectAbsolutePath(
                        entry.getRawPath().substring(1)));
            }
            //            else {
            //                ErrorManager.getDefault().log(ErrorManager.WARNING, "workspace == null");
            //            }
            return;
        }
        
        // not VARIABLE, not PROJECT, not LINK -> either source root or library
        if (!(new File(entry.getRawPath()).isAbsolute())) {
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
    private Workspace.Variable getVariable(DotClassPathEntry entry) {
        return getVariable(entry.getRawPath());
    }
    
    public String toString() {
        return "EclipseProject[" + getName() + ", " + getDirectory() + "]"; // NOI18N
    }
    
    /* name is enough for now */
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof EclipseProject)) return false;
        final EclipseProject ePrj = (EclipseProject) obj;
        if (!name.equals(ePrj.name)) return false;
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
