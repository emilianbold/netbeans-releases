/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.nodejs.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.options.NodeJsOptions;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferences;
import org.netbeans.modules.javascript.nodejs.ui.actions.NodeJsActionProvider;
import org.netbeans.modules.javascript.nodejs.util.FileUtils;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

public final class NodeJsSupport {

    static final Logger LOGGER = Logger.getLogger(NodeJsSupport.class.getName());

    final Project project;
    private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    final PreferenceChangeListener optionsListener = new OptionsListener();
    final PreferenceChangeListener preferencesListener = new PreferencesListener();
    private final PropertyChangeListener packageJsonListener = new PackageJsonListener();
    private final FileChangeListener nodeSourcesListener = new NodeSourcesListener();
    final NodeJsPreferences preferences;
    private final ActionProvider actionProvider;
    final NodeJsSourceRoots sourceRoots;
    private final PackageJson packageJson;


    private NodeJsSupport(Project project) {
        assert project != null;
        this.project = project;
        actionProvider = new NodeJsActionProvider(project);
        sourceRoots = new NodeJsSourceRoots(project);
        preferences = new NodeJsPreferences(project);
        packageJson = new PackageJson(project);
    }

    @ProjectServiceProvider(service = NodeJsSupport.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static NodeJsSupport create(Project project) {
        NodeJsSupport support = new NodeJsSupport(project);
        // listeners
        NodeJsOptions nodeJsOptions = NodeJsOptions.getInstance();
        nodeJsOptions.addPreferenceChangeListener(WeakListeners.create(PreferenceChangeListener.class, support.optionsListener, nodeJsOptions));
        return support;
    }

    public static NodeJsSupport forProject(Project project) {
        NodeJsSupport support = project.getLookup().lookup(NodeJsSupport.class);
        assert support != null : "NodeJsSupport should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return support;
    }

    public NodeJsPreferences getPreferences() {
        return preferences;
    }

    public ActionProvider getActionProvider() {
        return actionProvider;
    }

    public List<URL> getSourceRoots() {
        return sourceRoots.getSourceRoots();
    }

    public PackageJson getPackageJson() {
        return packageJson;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public void firePropertyChanged(String propertyName, Object oldValue, Object newValue) {
        propertyChangeSupport.firePropertyChange(new PropertyChangeEvent(project, propertyName, oldValue, newValue));
    }

    public void fireSourceRootsChanged() {
        sourceRoots.resetSourceRoots();
        firePropertyChanged(NodeJsPlatformProvider.PROP_SOURCE_ROOTS, null, null);
    }

    void projectOpened() {
        FileUtil.addFileChangeListener(nodeSourcesListener, FileUtils.getNodeSources());
        preferences.addPreferenceChangeListener(preferencesListener);
        packageJson.addPropertyChangeListener(packageJsonListener);
        packageJson.init();
    }

    public void projectClosed() {
        FileUtil.removeFileChangeListener(nodeSourcesListener, FileUtils.getNodeSources());
        preferences.removePreferenceChangeListener(preferencesListener);
        packageJson.removePropertyChangeListener(packageJsonListener);
        packageJson.cleanup();
    }

    //~ Inner classes

    private final class OptionsListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String projectName = project.getProjectDirectory().getNameExt();
            if (!preferences.isEnabled()) {
                LOGGER.log(Level.FINE, "Change event in node.js options ignored, node.js not enabled in project {0}", projectName);
                return;
            }
            String key = evt.getKey();
            LOGGER.log(Level.FINE, "Processing change event {0} in node.js options in project {1}", new Object[] {key, projectName});
            if (NodeJsOptions.NODE_PATH.equals(key)
                    && preferences.isDefaultNode()) {
                fireSourceRootsChanged();
            }
        }

    }

    private final class PreferencesListener implements PreferenceChangeListener {

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            String projectName = project.getProjectDirectory().getNameExt();
            boolean enabled = preferences.isEnabled();
            String key = evt.getKey();
            LOGGER.log(Level.FINE, "Processing change event {0} in node.js preferences in project {1}", new Object[] {key, projectName});
            if (NodeJsPreferences.ENABLED.equals(key)) {
                firePropertyChanged(NodeJsPlatformProvider.PROP_ENABLED, !enabled, enabled);
            } else if (!enabled) {
                LOGGER.log(Level.FINE, "Change event in node.js preferences ignored, node.js not enabled in project {0}", projectName);
            } else if (NodeJsPreferences.NODE_DEFAULT.equals(key)) {
                fireSourceRootsChanged();
            } else if (NodeJsPreferences.NODE_PATH.equals(key)
                    && !preferences.isDefaultNode()) {
                fireSourceRootsChanged();
            }
        }

    }

    private final class PackageJsonListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String projectName = project.getProjectDirectory().getNameExt();
            if (!preferences.isEnabled()) {
                LOGGER.log(Level.FINE, "Property change event in package.json ignored, node.js not enabled in project {0}", projectName);
                return;
            }
            String propertyName = evt.getPropertyName();
            LOGGER.log(Level.FINE, "Processing property change event {0} in package.json in project {1}", new Object[]{propertyName, projectName});
            if (PackageJson.PROP_NAME.equals(propertyName)) {
                firePropertyChanged(NodeJsPlatformProvider.PROP_PROJECT_NAME, evt.getOldValue(), evt.getNewValue());
            } else if (PackageJson.PROP_SCRIPTS_START.equals(propertyName)) {
                firePropertyChanged(NodeJsPlatformProvider.PROP_START_FILE, evt.getOldValue(), evt.getNewValue());
            } else {
                assert false : "Unknown event: " + propertyName;
            }
        }

    }

    private final class NodeSourcesListener extends FileChangeAdapter {

        @Override
        public void fileFolderCreated(FileEvent fe) {
            String projectName = project.getProjectDirectory().getNameExt();
            if (!preferences.isEnabled()) {
                LOGGER.log(Level.FINE, "File change event in node sources ignored, node.js not enabled in project {0}", projectName);
                return;
            }
            NodeExecutable node = NodeExecutable.forProject(project, false);
            if (node == null) {
                return;
            }
            Version version = node.getVersion();
            if (version == null) {
                return;
            }
            if (fe.getFile().getNameExt().equals(version.toString())) {
                LOGGER.log(Level.FINE, "Processing file change event in node sources in project {0}", projectName);
                fireSourceRootsChanged();
            }
        }

    }

}
