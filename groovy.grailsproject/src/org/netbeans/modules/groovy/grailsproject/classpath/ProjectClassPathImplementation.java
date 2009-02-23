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
import org.netbeans.api.project.Project;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileChangeListener;

final class ProjectClassPathImplementation implements ClassPathImplementation, FileChangeListener {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private List<PathResourceImplementation> resources;

    private final GrailsProjectConfig projectConfig;

    private final File projectRoot;

    private ProjectClassPathImplementation(GrailsProjectConfig projectConfig) {
        this.projectConfig = projectConfig;
        this.projectRoot = FileUtil.toFile(projectConfig.getProject().getProjectDirectory());
    }

    public static ProjectClassPathImplementation forProject(Project project) {
        ProjectClassPathImplementation impl = new ProjectClassPathImplementation(GrailsProjectConfig.forProject(project));

        File pluginsDir = FileUtil.normalizeFile(new File(FileUtil.toFile(project.getProjectDirectory()), "plugins")); // NOI18N
        File libDir = FileUtil.normalizeFile(new File(FileUtil.toFile(project.getProjectDirectory()), "lib")); // NOI18N

        // it is weakly referenced
        // FIXME in 1.1 we have to listen on application.properties
        FileUtil.addFileChangeListener(impl, pluginsDir);
        FileUtil.addFileChangeListener(impl, libDir);
        return impl;
    }

    public synchronized List<PathResourceImplementation> getResources() {
        if (this.resources == null) {
            this.resources = this.getPath();
        }
        return this.resources;
    }

    private List<PathResourceImplementation> getPath() {
        List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        // lib directory from project root
        addLibs(projectRoot, result);
        //File pluginsDir = new File(projectRoot, "plugins"); // NOI18N
        File pluginsDir = getPluginsDir();
        if (pluginsDir.isDirectory()) {
            for (String name : pluginsDir.list()) {
                File file = new File(pluginsDir, name);
                if (file.isDirectory()) {
                    // lib directories of installed plugins
                    addLibs(file, result);
                    // sources of installed plugins
                    addSources(file, result);
                }
            }
        }

        return Collections.unmodifiableList(result);
    }

    private static void addLibs(File root, List<PathResourceImplementation> result) {
        File[] jars = new File(root, "lib").listFiles(); // NOI18N
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

    private File getPluginsDir() {
        GrailsPlatform platform = projectConfig.getGrailsPlatform();
        if (GrailsPlatform.Version.VERSION_1_1.compareTo(platform.getVersion()) <= 0) {
            File buildConfig = new File(projectRoot,
                    "grails-app" + File.separator + "conf" + File.separator + "BuildConfig.groovy"); // NOI18N

            // FIXME global plugins and buildConfig
//            if (!buildConfig.exists() || !buildConfig.canRead()) {
                File pluginsDirFile;
                String pluginsDir = System.getProperty("grails.project.plugins.dir"); // NOI18N
                if (pluginsDir == null) {
                    File projectWorkDirFile;
                    String projectWorkDir = System.getProperty("grails.project.work.dir"); // NOI18N
                    if (projectWorkDir == null) {
                        File workDirFile;
                        String workDir = System.getProperty("grails.work.dir"); // NOI18N
                        if (workDir == null) {
                            workDir = System.getProperty("user.home"); // NOI18N
                            workDir = workDir + File.separator + ".grails" + File.separator + platform.getVersion(); // NOI18N
                            workDirFile = new File(workDir);
                        } else {
                            workDirFile = new File(workDir);
                            if (!workDirFile.isAbsolute()) {
                                workDirFile = new File(projectRoot, workDir);
                            }
                        }
                        projectWorkDirFile = new File(workDirFile, "projects" + File.separator + projectRoot.getName()); // NOI18N
                    } else {
                        projectWorkDirFile = new File(projectWorkDir);
                        if (!projectWorkDirFile.isAbsolute()) {
                            projectWorkDirFile = new File(projectRoot, projectWorkDir);
                        }
                    }
                    pluginsDirFile = new File(projectWorkDirFile, "plugins"); // NOI18N
                } else {
                    pluginsDirFile = new File(pluginsDir);
                    if (!pluginsDirFile.isAbsolute()) {
                        pluginsDirFile = new File(projectRoot, pluginsDir);
                    }
                }

                System.out.println(pluginsDirFile.getAbsolutePath());
                return pluginsDirFile;
//            } else {
//
//            }
        }

        System.out.println(new File(projectRoot, "plugins").getAbsolutePath());
        return new File(projectRoot, "plugins"); // NOI18N
    }

    // XXX I am handling plugin sources as 'library' for owning project, is that correct?
    private static void addSources(File root, List<PathResourceImplementation> result) {
        SourceRoots sourceRoots = new SourceRoots(FileUtil.toFileObject(root));
        for (URL url : sourceRoots.getRootURLs()) {
            result.add(ClassPathSupport.createResource(url));
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
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
        synchronized (this) {
            this.resources = null;
        }
        this.support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
    }
}
