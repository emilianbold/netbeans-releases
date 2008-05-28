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

package org.netbeans.modules.projectimport.spi;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Misc helper methods for implementors of ProjectTypeFactory.
 * 
 */
public class ProjectFactorySupport {

    /**
     * Converts VARIABLE classpath entry to Ant property, eg.
     * SOME_ROOT/lib/a.jar -> ${SOME_ROOT}/lib/a.jar
     */
    public static String asAntVariable(DotClassPathEntry entry) {
        if (entry.getKind() != DotClassPathEntry.Kind.VARIABLE) {
            throw new IllegalStateException("not a VARIABLE entry "+entry);
        }
        String s = entry.getRawPath();
        if (!s.endsWith(File.separator)) {
            s += "!/";
        }
        int index = s.indexOf('/');
        return "${"+s.substring(0,index)+"}"+s.substring(index);
    }

    /**
     * Default translation of eclipse classpath to netbeans classpath. Should
     * be useful for most of the project types.
     */
    public static void updateProjectClassPath(AntProjectHelper helper, ProjectImportModel model, 
            List<String> importProblems) throws IOException {
        FileObject sourceRoot = FileUtil.toFileObject(model.getEclipseSourceRootsAsFileArray()[0]);
        for (DotClassPathEntry entry : model.getEclipseClassPathEntries()) {

            if (entry.getKind() == DotClassPathEntry.Kind.PROJECT) {
                File proj = new File(model.getNetBeansProjectLocation() + File.separatorChar + entry.getRawPath().substring(1));
                if (!proj.exists()) {
                    // TODO: perhaps search NetBeans OpenProjectList for a project of that name and use it if found one.
                    importProblems.add("Project " + model.getProjectName() + " depends on project " + entry.getRawPath() + " which cannot be found at "+proj);
                    continue;
                }
                FileObject fo = FileUtil.toFileObject(proj);
                assert fo != null : proj;
                Project p = ProjectManager.getDefault().findProject(fo);
                if (p == null) {
                    throw new IOException("cannot find project for "+fo);
                }
                AntArtifact[] artifact = AntArtifactQuery.findArtifactsByType(p, JavaProjectConstants.ARTIFACT_TYPE_JAR);
                List<URI> elements = new ArrayList<URI>();
                for (AntArtifact art : artifact) {
                    elements.addAll(Arrays.asList(art.getArtifactLocations()));
                }
                ProjectClassPathModifier.addAntArtifacts(artifact, elements.toArray(new URI[elements.size()]), sourceRoot, ClassPath.COMPILE);
            } else if (entry.getKind() == DotClassPathEntry.Kind.LIBRARY) {
                ProjectClassPathModifier.addRoots(new URL[]{FileUtil.urlForArchiveOrDir(new File(entry.getAbsolutePath()))}, sourceRoot, ClassPath.COMPILE);
            } else if (entry.getKind() == DotClassPathEntry.Kind.VARIABLE) {
                try {
                    ProjectClassPathModifier.addRoots(new URI[]{new URI(null, null, ProjectFactorySupport.asAntVariable(entry), null)}, sourceRoot, ClassPath.COMPILE);
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else if (entry.getKind() == DotClassPathEntry.Kind.CONTAINER) {
                String antProperty = entry.getContainerMapping();
                if (antProperty != null && antProperty.length() > 0) {
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    String cp = ep.getProperty("javac.classpath");
                    if (cp == null) {
                        cp = "";
                    } else {
                        cp += ":";
                    }
                    cp += "${" + antProperty + "}";
                    String arr[] = PropertyUtils.tokenizePath(cp);
                    for (int i=0; i<arr.length-1; i++) {
                        arr[i] += ":";
                    }
                    ep.setProperty("javac.classpath", arr);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                }
            }
        }
    }
    
    /**
     * TBD if this is really neede or not.
     */
    public static void updateSourceRootLabels(List<DotClassPathEntry> sources, SourceRoots roots) {
        URL[] rootURLs = roots.getRootURLs();
        String[] labels = new String[rootURLs.length];
        for (int i = 0; i < rootURLs.length; i++) {
            for (DotClassPathEntry e : sources) {
                if (rootURLs[i].getFile().equals(e.getAbsolutePath())) {
                    labels[i] = e.getRawPath();
                    break;
                }
            }
        }
        roots.putRoots(rootURLs, labels);
    }
    
    public static String calculateKey(ProjectImportModel model) {
        // TODO: iterate over all DotClassPathEntry items and generate key from kind and path
        return null;
    }

    public static void persistEclipseLink(Project project, String updaterKey, ProjectImportModel model) throws IOException {
        // get AUX
        // store eclipse project location
        // store latest timestamp of either .classpath or .project
        // store key
    }
    
    public static boolean isUpdatedNeeded(Project project) {
        // get AUX
        // get timestamps and eclipse folder
        // compare timestamp; if change then:
        //    create EclipseProject
        //    call EclipseProject.getProjectTypeFactory().calculateKey()
        //    return: keys are not equal
        return false;
    }
    
    public static void updatedProject(Project project) {
        if (!isUpdatedNeeded(project)) {
            return;
        }
        // get AUX
        // create EclipseProject
        // call EclipseProject.getProjectTypeFactory().update(project, PIM, oldkey)
    }
    
    // TODO: add ProjectOpenHook calling updatedProject in open and close
    
}
