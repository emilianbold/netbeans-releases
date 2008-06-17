/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core.spi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.Project;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;

/**
 * Data about Eclipse project to import.
 */
public final class ProjectImportModel {

    private EclipseProject project;
    private String projectLocation;
    private JavaPlatform platform;
    private List<Project> alreadyImportedProjects;

    public ProjectImportModel(EclipseProject project, String projectLocation, JavaPlatform platform, List<Project> alreadyImportedProjects) {
        this.project = project;
        this.projectLocation = projectLocation;
        this.platform = platform;
        this.alreadyImportedProjects = alreadyImportedProjects;
    }

    public String getProjectName() {
        return project.getName();
    }

    public File getEclipseProjectFolder() {
        return project.getDirectory();
    }

    public File getEclipseWorkspaceFolder() {
        return project.getWorkspace().getDirectory();
    }

    /**
     * Folder in which to create NetBeans project. In case NetBeans projects are
     * imported into the same location as Eclipse one the folder will already exist.
     */
    public String getNetBeansProjectLocation() {
        return projectLocation;
    }
    
    public Set<String> getEclipseNatures() {
        return project.getNatures();
    }

    public List<DotClassPathEntry> getEclipseClassPathEntries() {
        return project.getClassPathEntries();
    }
    
    public List<DotClassPathEntry> getEclipseSourceRoots() {
        return filterSourceRootsForTests(false);
    }
    
    public File[] getEclipseSourceRootsAsFileArray() {
        return convertToFileArray(getEclipseSourceRoots());
    }
    
    public List<DotClassPathEntry> getEclipseTestSourceRoots() {
        return filterSourceRootsForTests(true);
    }
    
    public File[] getEclipseTestSourceRootsAsFileArray() {
        return convertToFileArray(getEclipseTestSourceRoots());
    }

    private final Map<File,Boolean> looksLikeTests = new HashMap<File,Boolean>();
    private List<DotClassPathEntry> filterSourceRootsForTests(boolean test) {
        List<DotClassPathEntry> all = project.getSourceRoots();
        if (!hasJUnitOnClassPath()) {
            if (test) {
                return Collections.emptyList();
            } else {
                return all;
            }
        }
        List<DotClassPathEntry> result = new ArrayList<DotClassPathEntry>(all.size());
        for (DotClassPathEntry entry : all) {
            File r = new File(entry.getAbsolutePath());
            Boolean isTest;
            synchronized (looksLikeTests) {
                isTest = looksLikeTests.get(r);
                if (isTest == null) {
                    isTest = hasTests(r);
                    looksLikeTests.put(r, isTest);
                }
            }
            if (!test ^ isTest) {
                result.add(entry);
            }
        }
        return result;
    }

    private boolean hasJUnitOnClassPath() {
        for (DotClassPathEntry entry : getEclipseClassPathEntries()) {
            if (entry.getKind() == DotClassPathEntry.Kind.CONTAINER && entry.getRawPath().startsWith("org.eclipse.jdt.junit.JUNIT_CONTAINER/")) {
                return true;
            }
            // XXX could be a little laxer, e.g. JSPWiki uses: <classpathentry kind="lib" path="tests/lib/junit.jar"/>
        }
        return false;
    }

    /** Crude heuristic to see if a source root contains some sort of JUnit tests. */
    private boolean hasTests(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            File[] kids = fileOrDir.listFiles();
            if (kids != null) {
                for (File kid : kids) {
                    if (hasTests(kid)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return fileOrDir.getName().endsWith("Test.java");
        }
    }

    public JavaPlatform getJavaPlatform() {
        return platform;
    }
    
    public String getEclipseVersion() {
        // TODO: could be useful for client to fork their import of needed
        return null;
    }

    public DotClassPathEntry getOutput() {
        return project.getOutput();
    }

    /**
     * Returns list of already imported projects. Handy for resolving project
     * dependencies.
     */
    public List<Project> getAlreadyImportedProjects() {
        return Collections.<Project>unmodifiableList(alreadyImportedProjects);
    }
    
    private static File[] convertToFileArray(List<DotClassPathEntry> entries) {
        List<File> res = new ArrayList<File>();
        for (DotClassPathEntry entry : entries) {
            res.add(new File(entry.getAbsolutePath()));
        }
        return res.toArray(new File[res.size()]);
    }

}
