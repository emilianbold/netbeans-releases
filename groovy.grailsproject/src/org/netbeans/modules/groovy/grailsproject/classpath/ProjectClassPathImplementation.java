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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.grailsproject.classpath;

import java.beans.PropertyChangeEvent;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.SourceCategory;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPlugin;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPluginSupport;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileChangeListener;
import org.openide.util.WeakListeners;

final class ProjectClassPathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final Logger LOGGER = Logger.getLogger(ProjectClassPathImplementation.class.getName());

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private List<PathResourceImplementation> resources;

    private final GrailsProjectConfig projectConfig;

    private final File projectRoot;

    private File pluginsDir;

    private File globalPluginsDir;

    private PluginsLibListener listenerPluginsLib;

    private ProjectClassPathImplementation(GrailsProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
        this.projectRoot = FileUtil.toFile(projectConfig.getProject().getProjectDirectory());
    }

    public static ProjectClassPathImplementation forProject(Project project) {
        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        ProjectClassPathImplementation impl = new ProjectClassPathImplementation(config);

        config.addPropertyChangeListener(WeakListeners.propertyChange(impl, config));

        return impl;
    }

    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resources == null) {
            this.resources = this.getPath();
        }
        return this.resources;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (GrailsProjectConfig.GRAILS_PROJECT_PLUGINS_DIR_PROPERTY.equals(evt.getPropertyName())
                || GrailsProjectConfig.GRAILS_GLOBAL_PLUGINS_DIR_PROPERTY.equals(evt.getPropertyName())) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
        }
    }

    private List<PathResourceImplementation> getPath() {
        assert Thread.holdsLock(this);

        List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        // lib directory from project root
        addLibs(projectRoot, result);


        File oldPluginsDir = pluginsDir;
        File currentPluginsDir = ((GrailsProject) projectConfig.getProject()).getBuildConfig().getProjectPluginsDir();

        if (pluginsDir == null || !pluginsDir.equals(currentPluginsDir)) {
            LOGGER.log(Level.FINE, "Project plugins dir changed from {0} to {1}",
                    new Object[] {pluginsDir, currentPluginsDir});
            this.pluginsDir = currentPluginsDir;
        }

        if (pluginsDir.isDirectory()) {
            if (GrailsPlatform.Version.VERSION_1_1.compareTo(projectConfig.getGrailsPlatform().getVersion()) <= 0) {
                List<GrailsPlugin> plugins = new GrailsPluginSupport((GrailsProject) projectConfig.getProject())
                        .loadInstalledPlugins11();
                Set<String> pluginDirs = new HashSet<String>();
                for (GrailsPlugin plugin : plugins) {
                    pluginDirs.add(plugin.getDirName());
                }

                addPlugins(pluginsDir, result, pluginDirs);
            } else {
                addPlugins(pluginsDir, result, null);
            }
        }

        // TODO philosophical question: Is the global plugin boot or compile classpath?
        File oldGlobalPluginsDir = globalPluginsDir;
        File currentGlobalPluginsDir = ((GrailsProject) projectConfig.getProject()).getBuildConfig().getGlobalPluginsDir();
        if (globalPluginsDir == null || !globalPluginsDir.equals(currentGlobalPluginsDir)) {
            LOGGER.log(Level.FINE, "Project plugins dir changed from {0} to {1}",
                    new Object[] {pluginsDir, currentPluginsDir});
            this.globalPluginsDir = currentGlobalPluginsDir;
        }

        if (globalPluginsDir != null && globalPluginsDir.isDirectory()) {
            addPlugins(globalPluginsDir, result, null);
        }

        if (listenerPluginsLib == null) {
            File libDir = FileUtil.normalizeFile(new File(projectRoot, "lib")); // NOI18N

            listenerPluginsLib = new PluginsLibListener(this);
            FileUtil.addFileChangeListener(listenerPluginsLib, libDir);
        }

        // project plugins listener
        updateListener(listenerPluginsLib, oldPluginsDir, currentPluginsDir);

        // global plugins listener
        updateListener(listenerPluginsLib, oldGlobalPluginsDir, currentGlobalPluginsDir);

        return Collections.unmodifiableList(result);
    }

    private void updateListener(FileChangeListener listener, File oldDir, File newDir) {
        if (oldDir == null || !oldDir.equals(newDir)) {
            if (oldDir != null) {
                FileUtil.removeFileChangeListener(listener, oldDir);
            }
            if (newDir != null) {
                FileUtil.addFileChangeListener(listener, newDir);
            }
        }
    }

    private void addPlugins(File dir, List<PathResourceImplementation> result, Set<String> names) {
        for (String name : dir.list()) {
            File file = new File(dir, name);
            if (file.isDirectory() && (names == null || names.contains(name))) {
                // lib directories of installed plugins
                addLibs(file, result);
            }
        }
    }

    private static void addLibs(File root, List<PathResourceImplementation> result) {
        File[] jars = new File(root, SourceCategory.LIB.getRelativePath()).listFiles();
        if (jars != null) {
            for (File f : jars) {
                try {
                    if (f.isFile()) {
                        URL entry = f.toURI().toURL();
                        if (FileUtil.isArchiveFile(entry)) {
                            entry = FileUtil.getArchiveRoot(entry);
                            result.add(ClassPathSupport.createResource(entry));
                        }
                    }
                } catch (MalformedURLException mue) {
                    assert false : mue;
                }
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    private static class PluginsLibListener implements FileChangeListener {

        private final ProjectClassPathImplementation impl;

        public PluginsLibListener(ProjectClassPathImplementation impl) {
            this.impl = impl;
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            fireChange();
        }

        public void fileChanged(FileEvent fe) {
            fireChange();
        }

        public void fileDataCreated(FileEvent fe) {
            fireChange();
        }

        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        public void fileFolderCreated(FileEvent fe) {
            fireChange();
        }

        public void fileRenamed(FileRenameEvent fe) {
            fireChange();
        }

        private void fireChange() {
            synchronized (impl) {
                impl.resources = null;
            }
            impl.support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
        }
    }

}
