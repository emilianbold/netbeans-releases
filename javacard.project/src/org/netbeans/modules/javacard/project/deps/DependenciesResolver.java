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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.javacard.project.JCProject;
import org.netbeans.modules.javacard.project.JCProjectType;
import org.netbeans.spi.project.SubprojectProvider;
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
    public static boolean log = Boolean.getBoolean("DependenciesLogger.log");

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
                System.err.println("isValid ResolveFile " + dep.getPropertyName(a) + " to " + f);
                result &= f != null && f.exists();
                System.err.println(" result " + (f != null && f.exists()));
                if (log) {
                    System.err.println("Resolve artifact for " + dep.getID() + " kind " + a.name() + " result " + f);
                }
                if (!result) {
                    System.err.println("Bailing with invalid on " + a);
                    break;
                }
            }
        }
        System.err.println("Return valid " + result + " for " + dep.getID());
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
            boolean isCommonParent = ignoreCase? f.getPath().toLowerCase().startsWith(proj.getPath().toLowerCase()) :
                f.getPath().startsWith(proj.getPath());
            System.err.println("Check common parent " + f.getPath() + " and " + proj.getPath() + " : " + isCommonParent);
            if (isCommonParent) {
                relPath = f.getPath().substring(proj.getPath().length());
                System.err.println("Found match '" + relPath +"' appending backup");
                for (int i=0; i < backup; i++) {
                    if (relPath.startsWith(File.separator)) {
                        relPath = ".." + relPath;
                    } else {
                        relPath =  "../" + relPath;
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
                relPath = relPath.replace(File.separatorChar, '/');
            }
            System.err.println("RelPath for " + absolutePath + " is " + relPath);
            return new Path(relPath, true);
        } else {
            FileObject fo = FileUtil.toFileObject (FileUtil.normalizeFile(f));
            if (fo == null) {
                //Does not exist but could be created
                return new Path (absolutePath.replace(File.separatorChar, '/'), false);
            } else {
                return new Path (fo.getPath(), false);
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
        File file = resolveFile (dep, artifact);
        return file == null ? null : FileUtil.toFileObject (FileUtil.normalizeFile(file));
    }

    public File resolveFile(Dependency dep, ArtifactKind kind) {
        File result = resolveFile (dep.getPropertyName(kind));
        try {
            return result == null ? null : result.getCanonicalFile();
        } catch (IOException ex) {
            IOException nue = new IOException ("Exception canonicalizing " + result.getPath());
            nue.initCause(ex);
            Exceptions.printStackTrace(nue);
            return result;
        }
    }

    private File resolveFile(String propName) {
        String val = eval.getProperty(propName);
        System.err.println("Evaluated property " + propName + " as " + val);
        if (val == null || "".equals(val)) {
            return null;
        }
        if (File.separatorChar != '/') {
            val = val.replace('/', File.separatorChar);
        }
        File result = new File (FileUtil.toFile(projDir), val);
        if (!result.exists()) {
            result = new File (val);
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
                                            "Project at " + p.getProjectDirectory().getPath() + " returns " +
                                            "invalid URI for Ant Artifact " + a + ": " + uri);
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

//    public FileObject[] resolveArtifacts(Dependency dep, ArtifactKind artifact) {
//        String propName = dep.getPropertyName(artifact);
//        if (!dep.getKind().supportedArtifacts().contains(artifact)) {
//            throw new IllegalArgumentException("Dependencies of type " + dep.getKind() + //NOI18N
//                    "(" + dep.getID() + ")" + " do not support the artifact type " + artifact.name() + "(" + artifact + ")"); //NOI18N
//        }
//        String value = eval.evaluate('{' + propName + '}'); //NOI18N
//        return resolveFileObjects(value);
//    }
//
//    private FileObject[] resolveFileObjects(String antPath) {
//        FileObject result = resolveFileObject(antPath);
//        return result == null ? null : new FileObject[]{result};
//    }
//
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

    public void save(JCProject project, ResolvedDependencies dependencies, Element depsRoot) {
//        assert !EventQueue.isDispatchThread() : "Saving project props on EQ not allowed"; //NOI18N
        if (!ProjectManager.mutex().isWriteAccess()) {
            throw new IllegalStateException("Not in ProjectManager.mutex().writeAccess()"); //NOI18N
        }
        NodeList nl = depsRoot.getChildNodes();
        if (nl != null) {
            int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                Node n = nl.item(i);
                if (n instanceof Element) {
                    depsRoot.removeChild(n);
                }
            }
        }
        AntProjectHelper helper = project.getAntProjectHelper();
        EditableProperties pubProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        boolean privChanged = false;
        boolean pubChanged = false;
        FileObject root = project.getProjectDirectory();
        for (ResolvedDependency d : dependencies.all()) {
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
        for (ResolvedDependency r : dependencies.all()) {
            Dependency dep = r.getDependency();
            Element el = depsRoot.getOwnerDocument().createElementNS(JCProjectType.PROJECT_CONFIGURATION_NAMESPACE, DependenciesParser.DEP);
            el.setAttribute(DependenciesParser.ID, dep.getID());
            el.setAttribute(DependenciesParser.KIND, dep.getKind().name());
            el.setAttribute(DependenciesParser.DEPLOYMENT_STRATEGY, dep.getDeploymentStrategy().name());
            System.err.println("Appending child " + el + " attributes " + el.getAttributes());
            depsRoot.appendChild(el);
        }
        if (privChanged) {
            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privProps);
        }
        if (pubChanged) {
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, pubProps);
        }
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
