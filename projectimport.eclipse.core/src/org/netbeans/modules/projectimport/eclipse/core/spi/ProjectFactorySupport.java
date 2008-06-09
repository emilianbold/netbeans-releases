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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Misc helper methods for implementors of ProjectTypeFactory.
 * 
 */
public class ProjectFactorySupport {

    /** Logger for this class. */
    private static final Logger LOG =
            Logger.getLogger(ProjectFactorySupport.class.getName());
    
    /**
     * Default translation of eclipse classpath to netbeans classpath. Should
     * be useful for most of the project types.
     */
    public static void updateProjectClassPath(AntProjectHelper helper, ProjectImportModel model, 
            List<String> importProblems) throws IOException {
        assert model.getEclipseSourceRootsAsFileArray().length > 0 : model.getProjectName(); // XXX handle more gracefully (add an import problem)
        FileObject sourceRoot = FileUtil.toFileObject(model.getEclipseSourceRootsAsFileArray()[0]);
        for (DotClassPathEntry entry : model.getEclipseClassPathEntries()) {
            addItemToClassPath(helper, entry, model.getNetBeansProjectLocation(), model.getProjectName(), importProblems, sourceRoot);
        }
    }

    /**
     * Convenience method for synchronization of projects metadata. Must be used together with {@link #calculateKey}.
     */
    public static void synchronizeProjectClassPath(Project project, AntProjectHelper helper, ProjectImportModel model, 
            String oldKey, String newKey, List<String> importProblems) throws IOException {
        // compare old and new key and add and remove items from classpath;
        FileObject sourceRoot = FileUtil.toFileObject(model.getEclipseSourceRootsAsFileArray()[0]);
        
        // add new CP items:
        StringTokenizer st = new StringTokenizer(newKey, ";");
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.startsWith("src") || t.startsWith("output") || t.startsWith("jre")) {
                continue;
            }
            if (!oldKey.contains(t)) {
                DotClassPathEntry entry = findEntryByEncodedValue(model.getEclipseClassPathEntries(), t);
                // TODO: items appended to the end of classpath
                addItemToClassPath(helper, entry, model.getNetBeansProjectLocation(), model.getProjectName(), importProblems, sourceRoot);
            }
        }
        
        // remove removed CP items:
        st = new StringTokenizer(oldKey, ";");
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.startsWith("src") || t.startsWith("output") || t.startsWith("jre")) {
                continue;
            }
            if (!newKey.contains(t)) {
                removeOldItemFromClassPath(project, helper, t.substring(0, t.indexOf("=")), t.substring(t.indexOf("=")+1), sourceRoot);
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
    
    /**
     * Convenience impl of key creation.
     */
    public static String calculateKey(ProjectImportModel model) {
        StringBuffer sb = new StringBuffer();
        List<DotClassPathEntry> all = new ArrayList<DotClassPathEntry>();
        all.addAll(model.getEclipseSourceRoots());
        all.addAll(model.getEclipseTestSourceRoots());
        all.addAll(model.getEclipseClassPathEntries());
        for (DotClassPathEntry entry : all) {
            String oneItem = encodeDotClassPathEntryToKey(entry);
            if (oneItem != null) {
                sb.append(oneItem);
                sb.append(";");
            }
        }
        // TODO: commented out JRE till EclipseProjectReference.getEclipseProject is fixed.
        //sb.append("jre="+model.getJavaPlatform().getDisplayName()+";");
        if (model.getOuput() != null) {
            sb.append("output="+model.getOuput().getRawPath()+";");
        }
        return sb.toString().replace("con=;", ""); // remove empty container entries
    }
    
    private static String encodeDotClassPathEntryToKey(DotClassPathEntry entry) {
        String value = getValueTag(entry);
        if (value.length() == 0) {
            return null;
        }
        return getKindTag(entry.getKind()) + "=" + value;
    }
    
    private static String getKindTag(DotClassPathEntry.Kind kind) {
        switch (kind) {
            case PROJECT:
                return "prj";
            case LIBRARY:
                return "file";
            case VARIABLE:
                return "var";
            case CONTAINER:
                return "ant";
            case OUTPUT:
                return "out";
            case SOURCE:
            default:
                return "src";
        }
    }

    private static String getValueTag(DotClassPathEntry entry) {
        switch (entry.getKind()) {
            case PROJECT:
                return entry.getRawPath().substring(1); // project name
            case VARIABLE:
                String v[] = EclipseUtils.splitVariable(entry.getRawPath());
                return PropertyUtils.getUsablePropertyName(v[0]) + v[1]; // variable name
            case CONTAINER:
                return entry.getContainerMapping(); // mapping as produced by container resolver
            case LIBRARY:
            case OUTPUT:
            case SOURCE:
            default:
                return entry.getRawPath(); // file path
        }
    }
    
    /**
     * Adds single DotClassPathEntry to NB project classpath.
     */
    private static boolean addItemToClassPath(AntProjectHelper helper, DotClassPathEntry entry, String nbProjLocation, String projName, List<String> importProblems, FileObject sourceRoot/*, AntProjectHelper helper*/) throws IOException {
        if (entry.getKind() == DotClassPathEntry.Kind.PROJECT) {
            File proj = new File(nbProjLocation + File.separatorChar + entry.getRawPath().substring(1));
            if (!proj.exists()) {
                // TODO: perhaps search NetBeans OpenProjectList for a project of that name and use it if found one.
                importProblems.add("Project " + projName + " depends on project " + entry.getRawPath() + " which cannot be found at " + proj);
                return true;
            }
            FileObject fo = FileUtil.toFileObject(proj);
            assert fo != null : proj;
            Project p = ProjectManager.getDefault().findProject(fo);
            if (p == null) {
                throw new IOException("cannot find project for " + fo);
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
            // add property directly to Ant property
            addToBuildProperties(helper, "javac.classpath", ProjectFactorySupport.asAntVariable(entry));
//            ProjectClassPathModifier.addRoots(new URI[]{new URI(null, null, ProjectFactorySupport.asAntVariable(entry), null)}, sourceRoot, ClassPath.COMPILE);
        } else if (entry.getKind() == DotClassPathEntry.Kind.CONTAINER) {
            String antProperty = entry.getContainerMapping();
            if (antProperty != null && antProperty.length() > 0) {
                // add property directly to Ant property
                addToBuildProperties(helper, "javac.classpath", "${"+antProperty+"}");
//                  ProjectClassPathModifier.addRoots(new URI[]{new URI(null, null, "${" + antProperty + "}", null)}, sourceRoot, ClassPath.COMPILE);
            }
        }
        return false;
    }
    
    /**
     * Remove single classpath item (in encoded key form) from NB project classpath.
     */
    private static void removeOldItemFromClassPath(Project project, AntProjectHelper helper, String encodedKind, String encodedValue, FileObject sourceRoot) throws IOException {
        if ("prj".equals(encodedKind)) {
            SubprojectProvider subProjs = project.getLookup().lookup(SubprojectProvider.class);
            if (subProjs != null) {
                for (Project p : subProjs.getSubprojects()) {
                    ProjectInformation info = p.getLookup().lookup(ProjectInformation.class);
                    if (info.getName().equals(encodedValue)) {
                        AntArtifact[] artifact = AntArtifactQuery.findArtifactsByType(p, JavaProjectConstants.ARTIFACT_TYPE_JAR);
                        List<URI> elements = new ArrayList<URI>();
                        for (AntArtifact art : artifact) {
                            elements.addAll(Arrays.asList(art.getArtifactLocations()));
                        }
                        ProjectClassPathModifier.removeAntArtifacts(artifact, elements.toArray(new URI[elements.size()]), sourceRoot, ClassPath.COMPILE);
                        break;
                    }
                }
            }
        } else if ("file".equals(encodedKind)) {
            ProjectClassPathModifier.removeRoots(new URL[]{FileUtil.urlForArchiveOrDir(new File(encodedValue))}, sourceRoot, ClassPath.COMPILE);
        } else if ("var".equals(encodedKind)) {
            String v[] = EclipseUtils.splitVariable(encodedValue);
            removeFromBuildProperties(helper, "javac.classpath", "${var."+v[0]+"}"+v[1]);
        } else if ("ant".equals(encodedKind)) {
            removeFromBuildProperties(helper, "javac.classpath", "${"+encodedValue+"}");
        }
    }

    /**
     * Add given value to given classpath-like Ant property.
     */
    private static void addToBuildProperties(AntProjectHelper helper, String property, String valueToAppend) {
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String cp = ep.getProperty(property);
        if (cp == null) {
            cp = "";
        } else {
            cp += ":";
        }
        cp += valueToAppend;
        String[] arr = PropertyUtils.tokenizePath(cp);
        for (int i = 0; i < arr.length - 1; i++) {
            arr[i] += ":";
        }
        ep.setProperty(property, arr);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }

    /**
     * Remove given value to given classpath-like Ant property.
     */
    private static void removeFromBuildProperties(AntProjectHelper helper, String property, String referenceToRemove) {
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String cp = ep.getProperty(property);
        if (cp != null && referenceToRemove != null) {
            cp = cp.replace(referenceToRemove, "");
        }
        String[] arr = PropertyUtils.tokenizePath(cp);
        for (int i = 0; i < arr.length - 1; i++) {
            arr[i] += ":";
        }
        ep.setProperty(property, arr);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
    }

    private static DotClassPathEntry findEntryByEncodedValue(List<DotClassPathEntry> eclipseClassPathEntries, String t) {
        for (DotClassPathEntry e : eclipseClassPathEntries) {
            if (t.equals(encodeDotClassPathEntryToKey(e))) {
                return e;
            }
        }
        throw new IllegalStateException("cannot find entry '"+t+"' in "+eclipseClassPathEntries);
    }

    /**
     * Converts VARIABLE classpath entry to Ant property, eg.
     * SOME_ROOT/lib/a.jar -> ${var.SOME_ROOT}/lib/a.jar
     */
    private static String asAntVariable(DotClassPathEntry entry) {
        if (entry.getKind() != DotClassPathEntry.Kind.VARIABLE) {
            throw new IllegalStateException("not a VARIABLE entry "+entry);
        }
        String s[] = EclipseUtils.splitVariable(entry.getRawPath());
        String varName = PropertyUtils.getUsablePropertyName(s[0]);
        return "${var."+varName+"}"+s[1];
    }

}
