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

package org.netbeans.modules.groovy.grailsproject.config;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.groovy.grails.api.GrailsPlatform;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.GrailsProject;
import org.netbeans.modules.groovy.grailsproject.plugins.GrailsPlugin;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 *
 * @author Petr Hejl
 */
public class BuildConfig {

    private static final Logger LOGGER = Logger.getLogger(BuildConfig.class.getName());

    private final GrailsProject project;

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    private Object buildSettingsInstance;

    private File projectRoot;

    private FileChangeListener listener = new BuildConfigListener();

    private boolean initialized;

    public BuildConfig(GrailsProject project) {
        this.project = project;
    }

    public synchronized void load() {
        if (!initialized) {
            reload();
            initialized = true;
        }
    }

    private synchronized void reload() {
        long start = System.currentTimeMillis();

        File newProjectRoot = FileUtil.toFile(project.getProjectDirectory());
        assert newProjectRoot != null;

        if (!newProjectRoot.equals(projectRoot)) {
            String buildConfig = "grails-app" + File.separator + "conf"
                    + File.separator + "BuildConfig.groovy"; // NOI18N

            if (listener != null && projectRoot != null) {
                FileUtil.removeFileChangeListener(listener, new File(projectRoot, buildConfig));
            }
            FileUtil.addFileChangeListener(listener, new File(newProjectRoot, buildConfig));
            projectRoot = newProjectRoot;
        }


        buildSettingsInstance = loadBuildSettings();
        LOGGER.log(Level.INFO, "Took {0} ms to load BuildSettings for {1}",
                new Object[] {(System.currentTimeMillis() - start), project.getProjectDirectory().getNameExt()});
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public synchronized File getProjectPluginsDir() {
        load();

        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            return getProjectPluginsDir11();
        }
        return getProjectPluginsDir10();
    }

    private File getProjectPluginsDir10() {
        assert Thread.holdsLock(this);
        return new File(projectRoot, "plugins"); // NOI18N
    }

    private File getProjectPluginsDir11() {
        assert Thread.holdsLock(this);
        try {
            if (buildSettingsInstance != null) {
                Method getProjectPluginsDirMethod = buildSettingsInstance.getClass().getMethod("getProjectPluginsDir", // NOI18N
                        new Class[] {});
                Object value = getProjectPluginsDirMethod.invoke(buildSettingsInstance, new Object[] {});

                if (value instanceof File) {
                    File file = (File) value;
                    if (!file.isAbsolute()) {
                        file = new File(projectRoot, file.getPath());
                    }
                    return file;
                }
            }
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        GrailsPlatform platform = config.getGrailsPlatform();
        if (platform.isConfigured()) {
            return new File(System.getProperty("user.home"), ".grails" + File.separator // NOI18N
                    + config.getGrailsPlatform().getVersion().toString() + File.separator
                    + "projects" + File.separator + projectRoot.getName() + File.separator + "plugins"); // NOI18N
        }
        return null;
    }

    public synchronized File getGlobalPluginsDir() {
        load();

        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            return getGlobalPluginsDir11();
        }
        return getGlobalPluginsDir10();
    }

    private File getGlobalPluginsDir10() {
        assert Thread.holdsLock(this);
        return null;
    }

    private File getGlobalPluginsDir11() {
        assert Thread.holdsLock(this);
        try {
            if (buildSettingsInstance != null) {
                Method getGlobalPluginsDirMethod = buildSettingsInstance.getClass().getMethod("getGlobalPluginsDir", // NOI18N
                        new Class[] {});
                Object value = getGlobalPluginsDirMethod.invoke(buildSettingsInstance, new Object[] {});

                if (value instanceof File) {
                    File file = (File) value;
                    if (!file.isAbsolute()) {
                        file = new File(projectRoot, file.getPath());
                    }
                    return file;
                }
            }
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        GrailsProjectConfig config = GrailsProjectConfig.forProject(project);
        GrailsPlatform platform = config.getGrailsPlatform();
        if (platform.isConfigured()) {
            return new File(System.getProperty("user.home"), ".grails" + File.separator // NOI18N
                    + config.getGrailsPlatform().getVersion().toString() + File.separator + "global-plugins"); // NOI18N
        }
        return null;
    }

    public synchronized List<GrailsPlugin> getLocalPlugins() {
        load();

        if (GrailsPlatform.Version.VERSION_1_1.compareTo(GrailsProjectConfig.forProject(project).getGrailsPlatform().getVersion()) <= 0) {
            return getLocalPlugins11();
        }
        return getLocalPlugins10();
    }

    private List<GrailsPlugin> getLocalPlugins10() {
        assert Thread.holdsLock(this);
        return Collections.emptyList();
    }

    private List<GrailsPlugin> getLocalPlugins11() {
        assert Thread.holdsLock(this);
        try {
            if (buildSettingsInstance != null) {
                Method getConfigMethod = buildSettingsInstance.getClass().getMethod("getConfig", // NOI18N
                        new Class[] {});
                Object configValue = getConfigMethod.invoke(buildSettingsInstance, new Object[] {});

                Method toPropertiesMethod = configValue.getClass().getMethod("toProperties", new Class[] {}); // NOI18N
                Object converted = toPropertiesMethod .invoke(configValue, new Object[] {});

                if (converted instanceof Properties) {
                    Properties properties = (Properties) converted;
                    List<GrailsPlugin> plugins = new ArrayList<GrailsPlugin>();
                    for (Enumeration e = properties.propertyNames(); e.hasMoreElements();) {
                        String key = (String) e.nextElement();
                        if (key.startsWith("grails.plugin.location.")) { // NOI18N
                            String value = properties.getProperty(key);
                            key = key.substring("grails.plugin.location.".length());
                            plugins.add(new GrailsPlugin(key, null, null, value));
                        }
                    }
                }
            }
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }

        return Collections.emptyList();
    }

    private Object loadBuildSettings() {
        assert Thread.holdsLock(this);
        GrailsPlatform platform = GrailsProjectConfig.forProject(project).getGrailsPlatform();
        if (!platform.isConfigured()) {
            return null;
        }

        ClassLoader loader = platform.getClassPath().getClassLoader(true);
        URLClassLoader urlLoader;
        if (loader instanceof URLClassLoader) {
            urlLoader = (URLClassLoader) loader;
        } else {
            urlLoader = new URLClassLoader(new URL[] {}, loader);
        }

        try {
            Class clazz = urlLoader.loadClass("grails.util.BuildSettings"); // NOI18N
            Constructor contructor = clazz.getConstructor(File.class, File.class);
            Object instance = contructor.newInstance(platform.getGrailsHome(), projectRoot);

            Method setRootLoaderMethod = clazz.getMethod("setRootLoader", new Class[] {URLClassLoader.class}); // NOI18N
            setRootLoaderMethod.invoke(instance, new Object[] {urlLoader});

            Method loadConfigMethod = clazz.getMethod("loadConfig", new Class[] {}); // NOI18N
            loadConfigMethod.invoke(instance, new Object[] {});

            return instance;
        } catch (ClassNotFoundException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (NoSuchMethodException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (InstantiationException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (IllegalAccessException ex) {
            LOGGER.log(Level.FINE, null, ex);
        } catch (InvocationTargetException ex) {
            LOGGER.log(Level.FINE, null, ex);
        }
        return null;
    }

    private class BuildConfigListener implements FileChangeListener {

        public void fileAttributeChanged(FileAttributeEvent fe) {
            // noop
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

        public void fireChange() {
            reload();
            changeSupport.fireChange();
        }

    }
}
