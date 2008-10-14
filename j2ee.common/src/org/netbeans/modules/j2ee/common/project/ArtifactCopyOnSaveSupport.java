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

package org.netbeans.modules.j2ee.common.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider.DeployOnSaveSupport;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Petr Hejl
 * @since 1.32
 */
public abstract class ArtifactCopyOnSaveSupport implements FileChangeSupportListener,
            PropertyChangeListener, AntProjectListener {

    private static final Logger LOGGER = Logger.getLogger(ArtifactCopyOnSaveSupport.class.getName());

    private final List<ArtifactListener> listeners = new ArrayList<ArtifactListener>();

    private final Map<File, String> listeningTo = new HashMap<File, String>();

    private final String destDirProperty;

    private final PropertyEvaluator evaluator;

    private final AntProjectHelper antHelper;

    private boolean synchronize;

    private volatile String destDir;

    public ArtifactCopyOnSaveSupport(String destDirProperty, PropertyEvaluator evaluator,
            AntProjectHelper antHelper) {
        super();
        this.destDirProperty = destDirProperty;
        this.evaluator = evaluator;
        this.antHelper = antHelper;
    }

    public synchronized void enableArtifactSynchronization(boolean synchronize) {
        this.synchronize = synchronize;
    }

    public final void addArtifactListener(ArtifactListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            boolean init = listeners.isEmpty();
            listeners.add(listener);
            if (init) {
                initialize();
                reload();
            }
        }
    }

    public final void removeArtifactListener(ArtifactListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                close();
            }
        }
    }

    public final void initialize() {
        close();
        destDir = evaluator.getProperty(destDirProperty);
        evaluator.addPropertyChangeListener(this);
        antHelper.addAntProjectListener(this);
    }

    protected abstract Map<ClassPathSupport.Item, String> getArtifacts();

    protected ArtifactListener.Artifact filterArtifact(ArtifactListener.Artifact artifact) {
        return artifact;
    }

    public final synchronized void reload() {
        Map<File, String> toRemove  = new HashMap<File, String>(listeningTo);
        for (Map.Entry<ClassPathSupport.Item, String> entry : getArtifacts().entrySet()) {
            ClassPathSupport.Item item = entry.getKey();
            if (!item.isBroken() && item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
                // FIXME more precise check when we should ignore it
                if (item.getArtifact().getProject().getLookup().lookup(J2eeModuleProvider.class) != null) {
                    continue;
                }
                File scriptLocation = item.getArtifact().getScriptLocation().getAbsoluteFile();
                if (!scriptLocation.isDirectory()) {
                    scriptLocation = scriptLocation.getParentFile();
                }

                String path = entry.getValue();
                if (path != null) {
                    for (URI artifactURI : item.getArtifact().getArtifactLocations()) {
                        File file = null;
                        if (artifactURI.isAbsolute()) {
                            file = new File(artifactURI);
                        } else {
                            file = new File(scriptLocation, artifactURI.getPath());
                        }
                        file = FileUtil.normalizeFile(file);

                        if (!listeningTo.containsKey(file)) {
                            FileChangeSupport.DEFAULT.addListener(this, file);
                            listeningTo.put(file, path);
                            if (synchronize) {
                                try {
                                    updateFile(file, path);
                                } catch (IOException ex) {
                                    LOGGER.log(Level.FINE, "Initial copy failed", ex);
                                }
                            }
                        }
                        toRemove.remove(file);
                    }
                }
            }
        }

        for (Map.Entry<File, String> removeEntry : toRemove.entrySet()) {
            FileChangeSupport.DEFAULT.removeListener(this, removeEntry.getKey());
            listeningTo.remove(removeEntry.getKey());
            if (synchronize) {
                deleteFile(removeEntry.getKey(), removeEntry.getValue());
            }
        }
    }

    public final void close() {
        synchronized (this) {
            for (Map.Entry<File, String> entry : listeningTo.entrySet()) {
                FileChangeSupport.DEFAULT.removeListener(this, entry.getKey());
            }
            listeningTo.clear();
        }
        antHelper.removeAntProjectListener(this);
        evaluator.removePropertyChangeListener(this);
    }

    public final void propertyChange(PropertyChangeEvent evt) {
        if (ProjectProperties.JAVAC_CLASSPATH.equals(evt.getPropertyName())) {
            LOGGER.log(Level.FINEST, "Classpath changed");
            reload();
        } else if (destDirProperty.equals(evt.getPropertyName())) {
            // TODO copy all files ?
            destDir = evaluator.getProperty(destDirProperty);
        }
    }

    public final void configurationXmlChanged(AntProjectEvent ev) {
        if (AntProjectHelper.PROJECT_XML_PATH.equals(ev.getPath())) {
            LOGGER.log(Level.FINEST, "Project XML changed");
            reload();
        }
    }

    public final void propertiesChanged(AntProjectEvent ev) {
        // noop
    }

    public final void fileCreated(FileChangeSupportEvent event) {
        updateFile(event);
    }

    public final void fileModified(FileChangeSupportEvent event) {
        updateFile(event);
    }

    public final void fileDeleted(FileChangeSupportEvent event) {
        // noop - this usually means clean
    }

    private void fireArtifactChange(File file) {
        List<ArtifactListener> toFire = null;
        synchronized (this) {
            toFire = new ArrayList<ArtifactListener>(listeners);
        }

        Iterable<ArtifactListener.Artifact> iterable = Collections.singleton(
                filterArtifact(ArtifactListener.Artifact.forFile(file).referencedLibrary()));
        for (ArtifactListener listener : toFire) {
            listener.artifactsUpdated(iterable);
        }
    }

    private void updateFile(FileChangeSupportEvent event) {
        File sourceFile = null;
        String path = null;

        synchronized (this) {
            sourceFile = FileUtil.normalizeFile(event.getPath());
            path = listeningTo.get(event.getPath());
            if (path == null) {
                return;
            }
        }
        try {
            updateFile(sourceFile, path);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private void updateFile(File sourceFile, String destPath) throws IOException {
        assert sourceFile != null;
        assert destPath != null;

        FileObject webBuildBase = destDir == null ? null : antHelper.resolveFileObject(destDir);

        if (webBuildBase == null) {
            return;
        }

        FileObject sourceObject = FileUtil.toFileObject(sourceFile);
        if (sourceObject == null) {
            LOGGER.log(Level.FINE, "Source file does not exist");
            return;
        }

        FileObject destFile = FileUtil.createData(webBuildBase, destPath + "/" + sourceObject.getNameExt());
        copy(sourceObject, destFile);

        // fire event
        File dest = FileUtil.toFile(destFile);
        if (dest != null) {
            fireArtifactChange(dest);
        }
        LOGGER.log(Level.FINE, "Artifact jar successfully copied " + sourceFile.getAbsolutePath()
                + " " + sourceFile.length());
    }

    private void deleteFile(File sourceFile, String destPath) {
        assert sourceFile != null;
        assert destPath != null;

        FileObject webBuildBase = destDir == null ? null : antHelper.resolveFileObject(destDir);

        if (webBuildBase == null) {
            return;
        }

        FileObject destFile = null;
        try {
            destFile = FileUtil.createData(webBuildBase, destPath + "/" + sourceFile.getName());

            if (destFile == null) {
                return;
            }

            destFile.delete();
            LOGGER.log(Level.FINE, "Artifact jar successfully deleted");
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            if ("jar".equals(destFile.getExt())) { // NOI18N
                // try to zero it out at least
                try {
                    zeroOutArchive(destFile);
                    LOGGER.log(Level.FINE, "Artifact jar successfully zeroed out");
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, "Could not zero out archive", ioe);
                }
            }
        }

        // fire event
        if (destFile != null) {
            File dest = FileUtil.toFile(destFile);
            if (dest != null) {
                fireArtifactChange(dest);
            }
        }
    }

    private void copy(FileObject sourceFile, FileObject destFile) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        FileLock fl = null;
        try {
            is = sourceFile.getInputStream();
            fl = destFile.lock();
            os = destFile.getOutputStream(fl);
            FileUtil.copy(is, os);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (fl != null) {
                fl.releaseLock();
            }
        }
    }

    private void zeroOutArchive(FileObject garbage) throws IOException {
        OutputStream fileToOverwrite = garbage.getOutputStream();
        try {
            JarOutputStream jos = new JarOutputStream(fileToOverwrite);
            try {
                jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF")); // NOI18N
                // UTF-8 guaranteed on any platform
                jos.write("Manifest-Version: 1.0\n".getBytes("UTF-8")); // NOI18N
            } finally {
                jos.close();
            }
        } finally {
            fileToOverwrite.close();
        }
    }
}
