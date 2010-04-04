/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.project.deps;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.JCProjectType;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Tim Boudreau
 */
public final class DependenciesResolver {

    private final FileObject projDir;
    private final PropertyEvaluator eval;

    public DependenciesResolver(FileObject projDir, PropertyEvaluator eval) {
        this.projDir = projDir;
        this.eval = eval;
    }

    /**
     * Determine if a dependency is valid - if all files needed by it actually
     * exist
     * @param dep A dependency
     * @return whether or not the dependency can be resolved
     */
    public boolean isValid(Dependency dep) {
        boolean result = true;
        for (ArtifactKind a : dep.getKind().supportedArtifacts()) {
            if (!a.mayBeNull()) {
                File f = resolveFile(dep.getPropertyName(a));
                result &= f != null && f.exists();
                if (!result) {
                    break;
                }
            }
        }
        return result;
    }

    public Path resolveAntPath(String absolutePath) {
        if (absolutePath == null) {
            return null;
        }
        File f = new File(absolutePath);
        File proj = FileUtil.toFile(projDir);
        String relPath = null;
        int backup = 0;
        boolean ignoreCase = Utilities.isWindows();
        do {
            boolean isCommonParent = ignoreCase ? f.getPath().toLowerCase().startsWith(proj.getPath().toLowerCase()) : f.getPath().startsWith(proj.getPath());
            if (isCommonParent) {
                relPath = f.getPath().substring(proj.getPath().length());
                for (int i = 0; i < backup; i++) {
                    if (relPath.startsWith(File.separator)) {
                        relPath = ".." + relPath; //NOI18N
                    } else {
                        relPath = "../" + relPath; //NOI18N
                    }
                }
                break;
            } else {
                proj = proj.getParentFile();
                backup++;
            }
        } while (proj != null);
        if (relPath != null) {
            if (File.separatorChar != '/') {
                relPath = relPath.replace(File.separatorChar, '/'); //NOI18N
            }
            return new Path(relPath, true);
        } else {
            FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
            if (fo == null) {
                //Does not exist but could be created
                return new Path(absolutePath.replace(File.separatorChar, '/'), false); //NOI18N
            } else {
                return new Path(fo.getPath(), false);
            }
        }
    }

    /**
     * Resolve a single artifact files.  Note that if the particular artifact
     * file may represent multiple files (ArtifactKind.mayBeMultipleFiles() -
     * currently this applies only to source paths), you probably want to use
     * <code>resolveArtifacts()</code>.
     * @param dep the dependency
     * @param artifact The kind of artifact
     * @return A fileobject, if one can be resolved
     */
    public FileObject resolveArtifact(Dependency dep, ArtifactKind artifact) {
        File file = resolveFile(dep, artifact);
        return file == null ? null : FileUtil.toFileObject(FileUtil.normalizeFile(file));
    }

    public File resolveFile(Dependency dep, ArtifactKind kind) {
        File result = resolveFile(dep.getPropertyName(kind));
        try {
            return result == null ? null : result.getCanonicalFile();
        } catch (IOException ex) {
            IOException nue = new IOException("Exception canonicalizing " + result.getPath()); //NOI18N
            nue.initCause(ex);
            Exceptions.printStackTrace(nue);
            return result;
        }
    }

    private File resolveFile(String propName) {
        String val = eval.getProperty(propName);
        if (val == null || "".equals(val)) {
            return null;
        }
        if (File.separatorChar != '/') {
            val = val.replace('/', File.separatorChar);
        }
        File result = new File(FileUtil.toFile(projDir), val);
        if (!result.exists()) {
            result = new File(val);
        }
        return result;
    }

    public File[] resolveClasspath(Dependencies deps, boolean pruneDuplicates, boolean fullClosure) {
        List<File> l = new ArrayList<File>();
        for (Dependency d : deps.all()) {
            FileObject origin = this.getArtifact(d, ArtifactKind.ORIGIN);
            if (d.getKind().isOriginAFolder()) {
                Project p = FileOwnerQuery.getOwner(origin);
                if (p != null) {
                    AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
                    for (AntArtifact a : prov.getBuildArtifacts()) {
                        if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                            for (URI uri : a.getArtifactLocations()) {
                                try {
                                    File f = new File(uri.toURL().getFile()).getAbsoluteFile();
                                    if (pruneDuplicates) {
                                        int ix = l.indexOf(f);
                                        if (ix >= 0) {
                                            l.remove(ix);
                                        }
                                    }
                                    l.add(f);
                                } catch (MalformedURLException ex) {
                                    Logger.getLogger(DependenciesResolver.class.getName()).log(Level.INFO,
                                            "Project at " + p.getProjectDirectory().getPath() + " returns " + //NOI18N
                                            "invalid URI for Ant Artifact " + a + ": " + uri); //NOI18N
                                }
                            }
                        }
                    }
                    if (fullClosure) {
//                        findFullClosure(p, l, pruneDuplicates);
                    }
                }
            } else {
                l.add(FileUtil.toFile(origin));
            }
        }
        return l.toArray(new File[l.size()]);
    }

    private FileObject resolveFileObject(String path) {
        //PENDING:  actually handle multiple paths in an Ant-friendly way...or not
        FileObject fo = projDir.getFileObject(path);
        if (fo == null) {
            if (File.separatorChar != '/') {
                path = path.replace('/', File.separatorChar);
                if (Utilities.isUnix() && path.charAt(0) != File.separatorChar) {
                    path = File.separatorChar + path;
                }
            }
            File f = new File(path);
            if (f.exists()) {
                fo = FileUtil.toFileObject(FileUtil.normalizeFile(f));
            }
        }
        return fo;
    }

    FileObject getArtifact(Dependency dep, ArtifactKind artifact) {
        String prop = dep.getPropertyName(artifact);
        String val = eval.evaluate("{" + prop + "}"); //NOI18N
        return val == null ? null : resolveFileObject(val);
    }

    public static final Logger LOGGER = Logger.getLogger(DependenciesResolver.class.getName());
    public void save(JCProject project, ResolvedDependencies dependencies, Element cfgRoot) throws IOException {
        LOGGER.log (Level.FINER, "Save project metadata {0}", project.getProjectDirectory()); //NOI18N
//        assert !EventQueue.isDispatchThread() : "Saving project props on EQ not allowed"; //NOI18N
        if (!ProjectManager.mutex().isWriteAccess()) {
            throw new IllegalStateException("Not in ProjectManager.mutex().writeAccess()"); //NOI18N
        }
        NodeList nls = cfgRoot.getElementsByTagNameNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, DependenciesParser.DEPS);
        if (nls.getLength() == 0) {
            throw new IOException("<dependencies> section missing from project.xml");
        }
        if (nls.getLength() > 1) {
            throw new IOException("project.xml contains multiple <dependencies> sections in " + project.getProjectDirectory().getPath());
        }
        cfgRoot.removeChild(nls.item(0));
        Element nue = cfgRoot.getOwnerDocument().createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, DependenciesParser.DEPS);
        AntProjectHelper helper = project.getAntProjectHelper();
        EditableProperties pubProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        boolean privChanged = false;
        boolean pubChanged = false;
        FileObject root = project.getProjectDirectory();
        List<? extends ResolvedDependency> l = dependencies.all();
        for (ResolvedDependency d : l) {
            Dependency dep = d.getDependency();
            for (ArtifactKind kind : d.getModifiedArtifactKinds()) {
                String propName = dep.getPropertyName(kind);
                String path = d.getPath(kind);
                if (path == null) {
                    privChanged |= privProps.remove(propName) != null;
                    pubChanged |= pubProps.remove(propName) != null;
                } else {
                    File f = new File(path);
                    FileObject file = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                    String relPath = FileUtil.getRelativePath(root, file);
                    if (relPath == null) {
                        //Not in file's tree, store in private properties
                        privProps.put(propName, file.getPath());
                        privChanged = true;
                    } else {
                        pubProps.put(propName, relPath);
                        pubChanged = true;
                    }
                }
            }
        }
        for (ResolvedDependency r : l) {
            Dependency dep = r.getDependency();
            Element el = nue.getOwnerDocument().createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, DependenciesParser.DEP);
            el.setAttribute(DependenciesParser.ID, dep.getID());
            el.setAttribute(DependenciesParser.KIND, dep.getKind().name());
            el.setAttribute(DependenciesParser.DEPLOYMENT_STRATEGY, dep.getDeploymentStrategy().name());
            LOGGER.log (Level.FINER, "Created element {0} for {1} ({2})", new Object[] { el, dep, r}); //NOI18N
            nue.appendChild(el);
        }
        cfgRoot.appendChild(nue);
        if (privChanged) {
            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privProps);
        }
        if (pubChanged) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pubProps);
        }
        //XXX delete use of class.path var after build scripts updated
        //to handle new-style complex dependencies
        //Must do this after the calls to putProperties to ensure all
        //paths are properly resolved.
        StringBuilder sb = new StringBuilder();
        for (ResolvedDependency dep : dependencies.all()) {
            //For now, just store absolute paths as in 6.7
            File f = dep.resolveFile(ArtifactKind.ORIGIN);
            if (f != null) {
                if (dep.getKind().isProjectDependency()) {
                    FileObject fo = dep.resolve(ArtifactKind.ORIGIN);
                    if (fo != null) {
                        Project p = FileOwnerQuery.getOwner(fo);
                        if (p != null) {
                            AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
                            if (prov != null) {
                                for (AntArtifact a : prov.getBuildArtifacts()) {
                                    for (URI uri : a.getArtifactLocations()) {
                                        File f1;
                                        try {
                                            f1 = new File(uri);
                                        } catch (IllegalArgumentException e) { //non-absolute URI
                                            File proj = FileUtil.toFile(p.getProjectDirectory());
                                            f1 = new File(proj, uri.toString());
                                        }
                                        if (sb.length() > 0) {
                                            sb.append(File.pathSeparatorChar);
                                        }
                                        sb.append(f1.getAbsolutePath());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (sb.length() > 0) {
                        sb.append(File.pathSeparatorChar);
                    }
                }
                sb.append(f.getAbsolutePath());
            }
        }
        LOGGER.log (Level.FINER, "Set deprecated class.path prop to {0}", sb); //NOI18N
        pubProps.setProperty(ProjectPropertyNames.PROJECT_PROP_CLASS_PATH, sb.toString());
        project.getAntProjectHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pubProps);
        try {
            ProjectManager.getDefault().saveProject(project);
            //end deletia
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
        //end deletia
    }
//    private void findFullClosure(Project project, List<File> l, boolean pruneDuplicates) {
//        SubprojectProvider subs = project.getLookup().lookup(SubprojectProvider.class);
//        if (subs != null) {
//            for (Project p : subs.getSubprojects()) {
//                //PENDING
//            }
//        }
//    }
}
