/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javacard.project;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

/**
 * Provides SourceForBinaryQuery implementation over libraries belonging to
 * a JCProject
 *
 * @author Tim Boudreau
 */
class JCSourceForBinaryQuery implements SourceForBinaryQueryImplementation {

    private final JCProject project;

    JCSourceForBinaryQuery(JCProject project) {
        this.project = project;
    }

    public Result findSourceRoots(URL binaryRoot) {
        try {
            ResolvedDependencies deps = project.syncGetResolvedDependencies();
            URL jarRoot = deJar(binaryRoot);
            File test = new File(jarRoot.toURI());
            for (ResolvedDependency dep : deps.all()) {
                File f = dep.resolveFile(ArtifactKind.ORIGIN);
                if (f.equals(test)) {
                    File sources = dep.resolveFile(ArtifactKind.SOURCES_PATH);
                    if (sources != null && sources.exists()) {
                        URL url = wrapJar (sources.toURI().toURL());
                        FileObject fo = URLMapper.findFileObject(url);
                        if (fo != null) {
                            return new R (FileUtil.toFileObject(
                                    FileUtil.normalizeFile(sources)));
                        }
                    }
                }
            }
            return null;
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static URL deJar(final URL url) {
        if (url == null) {
            return null;
        }
        final URL u = FileUtil.getArchiveFile(url);
        return u == null ? url : u;
    }

    public static URL wrapJar(final URL url) {
        if (url == null) {
            return null;
        }
        final String name = url.toExternalForm().toLowerCase();
        if (name.endsWith(".jar") || name.endsWith(".zip")) {
            return FileUtil.getArchiveRoot(url);  //NOI18N
        }
        if (!name.endsWith("/")) {
            try {
                return new URL(url.toExternalForm() + "/"); //NOI18N
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return url;
    }

    public static boolean isParentOf(final URL root, final URL file) {
        if (root == null || file == null) {
            return false;
        }
        return deJar(file).toExternalForm().startsWith(deJar(root).toExternalForm());
    }

    private static final class R implements Result, FileChangeListener {
        private FileObject root;
        private final ChangeSupport supp = new ChangeSupport (this);

        private R(FileObject root) {
            this.root = root;
            root.addFileChangeListener(FileUtil.weakFileChangeListener(this, root));
        }

        public FileObject[] getRoots() {
            return root.isValid() ? new FileObject[] { root } : new FileObject[0];
        }

        public void addChangeListener(ChangeListener l) {
            supp.addChangeListener(l);
        }

        public void removeChangeListener(ChangeListener l) {
            supp.removeChangeListener(l);
        }

        public void fileFolderCreated(FileEvent fe) {
            supp.fireChange();
        }

        public void fileDataCreated(FileEvent fe) {
            supp.fireChange();
        }

        public void fileChanged(FileEvent fe) {
            supp.fireChange();
        }

        public void fileDeleted(FileEvent fe) {

        }

        public void fileRenamed(FileRenameEvent fe) {
            supp.fireChange();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
        }
    }
}
