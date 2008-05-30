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

package org.netbeans.modules.php.project.ui.wizards;

import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.ui.SourcesFolderNameProvider;
import org.netbeans.modules.php.project.ui.LocalServer;
import java.awt.Component;
import java.io.File;
import java.util.List;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.ui.Utils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ConfigureServerPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener {

    static final String COPY_FILES = "copyFiles"; // NOI18N
    static final String COPY_TARGET = "copyTarget"; // NOI18N
    static final String COPY_TARGETS = "copyTargets"; // NOI18N
    static final String SERVER_IS_VALID = "serverIsValid"; // NOI18N

    private final SourcesFolderNameProvider sourcesFolderNameProvider;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private final String[] steps;
    private ConfigureServerPanelVisual configureServerPanelVisual = null;
    private WizardDescriptor descriptor;

    public ConfigureServerPanel(String[] steps, SourcesFolderNameProvider sourcesFolderNameProvider) {
        this.sourcesFolderNameProvider = sourcesFolderNameProvider;
        this.steps = steps;
    }

    public Component getComponent() {
        if (configureServerPanelVisual == null) {
            configureServerPanelVisual = new ConfigureServerPanelVisual(this, sourcesFolderNameProvider);
        }
        return configureServerPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureServerPanel.class.getName());
    }

    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        unregisterListeners();

        // copying enabled?
        configureServerPanelVisual.setState(isProjectFolderSelected());
        if (!isProjectFolderSelected()) {
            configureServerPanelVisual.setCopyFiles(false);
        } else {
            Boolean copyFiles = isCopyFiles();
            if (copyFiles != null) {
                configureServerPanelVisual.setCopyFiles(copyFiles);
            }
        }

        configureServerPanelVisual.setLocalServerModel(getLocalServerModel());
        LocalServer wwwFolder = getLocalServer();
        if (wwwFolder != null) {
            configureServerPanelVisual.selectLocalServer(wwwFolder);
        }

        registerListeners();
        fireChangeEvent();
    }

    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(COPY_FILES, configureServerPanelVisual.isCopyFiles());
        settings.putProperty(COPY_TARGET, configureServerPanelVisual.getLocalServer());
        settings.putProperty(COPY_TARGETS, configureServerPanelVisual.getLocalServerModel());
    }

    public boolean isValid() {
        getComponent();
        descriptor.putProperty("WizardPanel_errorMessage", " "); // NOI18N
        String error = validateServerLocation();
        if (error != null) {
            descriptor.putProperty("WizardPanel_errorMessage", error); // NOI18N
            descriptor.putProperty(SERVER_IS_VALID, false);
            return false;
        }
        descriptor.putProperty(SERVER_IS_VALID, true);
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        return true;
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    String[] getSteps() {
        return steps;
    }

    private boolean isProjectFolderSelected() {
        LocalServer localServer = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        return ConfigureProjectPanel.isProjectFolder(localServer);
    }

    private Boolean isCopyFiles() {
        return (Boolean) descriptor.getProperty(COPY_FILES);
    }

    private LocalServer getLocalServer() {
        return (LocalServer) descriptor.getProperty(COPY_TARGET);
    }

    private MutableComboBoxModel getLocalServerModel() {
        MutableComboBoxModel model = (MutableComboBoxModel) descriptor.getProperty(COPY_TARGETS);
        if (model != null) {
            return model;
        }
        model = new LocalServer.ComboBoxModel();

        List<DocumentRoot> roots = PhpEnvironment.get().getDocumentRoots(sourcesFolderNameProvider.getSourcesFolderName());
        for (DocumentRoot root : roots) {
            LocalServer ls = new LocalServer(root.getDocumentRoot());
            model.addElement(ls);
        }
        return model;
    }

    private String validateServerLocation() {
        if (!configureServerPanelVisual.isCopyFiles()) {
            return null;
        }

        LocalServer copyTarget = configureServerPanelVisual.getLocalServer();
        String sourcesLocation = copyTarget.getSrcRoot();
        File sources = FileUtil.normalizeFile(new File(sourcesLocation));
        if (sourcesLocation == null
                || !Utils.isValidFileName(sources)) {
            return NbBundle.getMessage(ConfigureServerPanel.class, "MSG_IllegalFolderName");
        }

        String err = Utils.validateProjectDirectory(sourcesLocation, "Folder", false, true); // NOI18N
        if (err != null) {
            return err;
        }
        err = validateSourcesAndCopyTarget();
        if (err != null) {
            return err;
        }
        // warn about visibility of source folder
//        String url = (String) descriptor.getProperty(ConfigureProjectPanel.URL);
//        String warning = NbBundle.getMessage(ConfigureServerPanel.class, "MSG_TargetFolderVisible", url);
//        descriptor.putProperty("WizardPanel_errorMessage", warning); // NOI18N
        return null;
    }

    // #131023
    private String validateSourcesAndCopyTarget() {
        LocalServer sources = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        assert sources != null;
        String sourcesSrcRoot = sources.getSrcRoot();
        if (ConfigureProjectPanel.isProjectFolder(sources)) {
            File projectLocation = (File) descriptor.getProperty(ConfigureProjectPanel.PROJECT_DIR);
            String projectName = (String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME);
            assert projectLocation != null;
            assert projectName != null;
            File project = new File(projectLocation, projectName);
            File src = FileUtil.normalizeFile(new File(project, ConfigureProjectPanel.DEFAULT_SOURCES_FOLDER));
            sourcesSrcRoot = src.getAbsolutePath();
        }
        File normalized = FileUtil.normalizeFile(new File(configureServerPanelVisual.getLocalServer().getSrcRoot()));
        String copyTarget = normalized.getAbsolutePath();
        return Utils.validateSourcesAndCopyTarget(sourcesSrcRoot, copyTarget);
    }

    private void registerListeners() {
        configureServerPanelVisual.addServerListener(this);
    }

    private void unregisterListeners() {
        configureServerPanelVisual.removeServerListener(this);
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }

//    private void adjustUrl() {
//        String currentUrl = configureProjectPanelVisual.getUrl();
//        if (!defaultUrl.equals(currentUrl)) {
//            return;
//        }
//        LocalServer sources = configureProjectPanelVisual.getSourcesLocation();
//        String url = null;
//        if (isProjectFolder(sources)) {
//            // project/web => check project name and url
//            // XXX copy-to-folder should be added
//            String correctUrl = getDefaultUrl(getSourcesFolderName());
//            if (!defaultUrl.equals(correctUrl)) {
//                url = correctUrl;
//            }
//        } else {
//            // /var/www or similar => check source folder name and url
//            String srcRoot = sources.getSrcRoot();
//            @SuppressWarnings("unchecked")
//            List<DocumentRoot> roots = (List<DocumentRoot>) descriptor.getProperty(ROOTS);
//            for (DocumentRoot root : roots) {
//                String docRoot = root.getDocumentRoot() + File.separator;
//                if (srcRoot.startsWith(docRoot)) {
//                    String urlSuffix = srcRoot.replaceFirst(Pattern.quote(docRoot), ""); // NOI18N
//                    // handle situations like: /var/www/xxx///// or c:\\apache\htdocs\aaa\bbb
//                    url = root.getUrl() + urlSuffix.replaceAll(Pattern.quote(File.separator) + "+", "/"); // NOI18N
//                    if (!url.endsWith("/")) { // NOI18N
//                        url += "/"; // NOI18N
//                    }
//                    break;
//                }
//            }
//            if (url == null) {
//                // not found => get the name of the sources
//                url = "http://localhost/" + new File(sources.getSrcRoot()).getName() + "/"; // NOI18N
//            }
//        }
//        if (url != null && !defaultUrl.equals(url)) {
//            defaultUrl = url;
//            configureProjectPanelVisual.setUrl(url);
//        }
//    }
//    private String getDefaultUrl(String projectName) {
//        return "http://localhost/" + projectName + "/" + DEFAULT_SOURCES_FOLDER + "/"; // NOI18N
//    }
}
