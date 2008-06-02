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

import java.util.List;
import org.netbeans.modules.php.project.environment.PhpEnvironment.DocumentRoot;
import org.netbeans.modules.php.project.ui.SourcesFolderNameProvider;
import org.netbeans.modules.php.project.ui.LocalServer;
import java.awt.Component;
import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ConfigureProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, SourcesFolderNameProvider, ChangeListener {

    static final LocalServer DEFAULT_LOCAL_SERVER;

    static final String DEFAULT_SOURCES_FOLDER = "web"; // NOI18N

    static final String PROJECT_NAME = "projectName"; // NOI18N
    static final String PROJECT_DIR = "projectDir"; // NOI18N
    static final String SET_AS_MAIN = "setAsMain"; // NOI18N
    static final String SOURCES_FOLDER = "sourcesFolder"; // NOI18N
    static final String LOCAL_SERVERS = "localServers"; // NOI18N
    static final String CREATE_INDEX_FILE = "createIndexFile"; // NOI18N
    static final String INDEX_FILE = "indexFile"; // NOI18N
    static final String ENCODING = "encoding"; // NOI18N
    static final String ROOTS = "roots"; // NOI18N

    private final String[] steps;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ConfigureProjectPanelVisual configureProjectPanelVisual = null;
    private WizardDescriptor descriptor = null;
    private String originalProjectName = null;

    static {
        String msg = NbBundle.getMessage(ConfigureProjectPanel.class, "LBL_UseProjectFolder",
                File.separator, ConfigureProjectPanel.DEFAULT_SOURCES_FOLDER);
        DEFAULT_LOCAL_SERVER = new LocalServer(null, null, msg, false);
    }

    public ConfigureProjectPanel(String[] steps) {
        this.steps = steps;
    }

    public Component getComponent() {
        if (configureProjectPanelVisual == null) {
            configureProjectPanelVisual = new ConfigureProjectPanelVisual(this);
        }
        return configureProjectPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureProjectPanel.class.getName());
    }

    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        // do not fire events now
        configureProjectPanelVisual.removeConfigureProjectListener(this);

        // project
        configureProjectPanelVisual.setProjectName(getProjectName());
        configureProjectPanelVisual.setProjectFolder(getProjectFolder().getAbsolutePath());

        // sources
        configureProjectPanelVisual.setLocalServerModel(getLocalServers());
        LocalServer sourcesLocation = getLocalServer();
        if (sourcesLocation != null) {
            configureProjectPanelVisual.selectSourcesLocation(sourcesLocation);
        }

        // index file
        Boolean createIndex = isCreateIndex();
        if (createIndex != null) {
            configureProjectPanelVisual.setCreateIndex(createIndex);
        }
        String indexName = getIndexName();
        if (indexName != null) {
            configureProjectPanelVisual.setIndexName(indexName);
        }

        // encoding
        Charset encoding = getEncoding();
        if (encoding != null) {
            configureProjectPanelVisual.setEncoding(encoding);
        }

        // set as main project
        Boolean setAsMain = isSetAsMain();
        if (setAsMain != null) {
            configureProjectPanelVisual.setSetAsMain(setAsMain);
        }

        configureProjectPanelVisual.addConfigureProjectListener(this);
        fireChangeEvent();
    }

    public void storeSettings(WizardDescriptor settings) {
        // project
        settings.putProperty(PROJECT_DIR, FileUtil.normalizeFile(configureProjectPanelVisual.getProjectFolderFile()));
        settings.putProperty(PROJECT_NAME, configureProjectPanelVisual.getProjectName());

        // sources
        settings.putProperty(SOURCES_FOLDER, configureProjectPanelVisual.getSourcesLocation());
        settings.putProperty(LOCAL_SERVERS, configureProjectPanelVisual.getLocalServerModel());

        // index file
        settings.putProperty(CREATE_INDEX_FILE, configureProjectPanelVisual.isCreateIndex());
        settings.putProperty(INDEX_FILE, configureProjectPanelVisual.getIndexName());

        // encoding
        settings.putProperty(ENCODING, configureProjectPanelVisual.getEncoding());

        // set as main project
        settings.putProperty(SET_AS_MAIN, configureProjectPanelVisual.isSetAsMain());
    }

    public boolean isValid() {
        getComponent();
        descriptor.putProperty("WizardPanel_errorMessage", " "); // NOI18N
        String error = validateProject();
        if (error != null) {
            descriptor.putProperty("WizardPanel_errorMessage", error); // NOI18N
            return false;
        }
        error = validateSources();
        if (error != null) {
            descriptor.putProperty("WizardPanel_errorMessage", error); // NOI18N
            return false;
        }
        error = validateIndexFile();
        if (error != null) {
            descriptor.putProperty("WizardPanel_errorMessage", error); // NOI18N
            return false;
        }
        return true;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public boolean isFinishPanel() {
        // XXX
        Boolean isServerValid = (Boolean) descriptor.getProperty(ConfigureServerPanel.SERVER_IS_VALID);
        return isServerValid == null || isServerValid;
    }

    static boolean isProjectFolder(LocalServer localServer) {
        return DEFAULT_LOCAL_SERVER.equals(localServer);
    }

    public String getSourcesFolderName() {
        getComponent();
        return configureProjectPanelVisual.getProjectName();
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    String[] getSteps() {
        return steps;
    }

    String getProjectName() {
        String projectName = (String) descriptor.getProperty(PROJECT_NAME);
        if (projectName == null) {
            // this can happen only for the first time
            projectName = getDefaultFreeName(ProjectChooser.getProjectsFolder());
            descriptor.putProperty(PROJECT_NAME, projectName);
            originalProjectName = projectName;
        }
        return projectName;
    }

    private File getProjectFolder() {
        File projectFolder = (File) descriptor.getProperty(PROJECT_DIR);
        if (projectFolder == null) {
            projectFolder = new File(ProjectChooser.getProjectsFolder(), getProjectName());
            descriptor.putProperty(PROJECT_DIR, projectFolder);
        }
        return projectFolder;
    }

    private String getDefaultFreeName(File projectFolder) {
        int i = 1;
        String projectName;
        do {
            projectName = validFreeProjectName(projectFolder, i++);
        } while (projectName == null);
        return projectName;
    }

    private String getIndexName() {
        return (String) descriptor.getProperty(INDEX_FILE);
    }

    private Charset getEncoding() {
        return (Charset) descriptor.getProperty(ENCODING);
    }

    private LocalServer getLocalServer() {
        return (LocalServer) descriptor.getProperty(SOURCES_FOLDER);
    }

    private MutableComboBoxModel getLocalServers() {
        MutableComboBoxModel model = (MutableComboBoxModel) descriptor.getProperty(LOCAL_SERVERS);
        if (model != null) {
            return model;
        }
        return getOSDependentLocalServers();
    }

    private MutableComboBoxModel getOSDependentLocalServers() {
        MutableComboBoxModel model = new LocalServer.ComboBoxModel(DEFAULT_LOCAL_SERVER);

        String projectName = getSourcesFolderName();
        List<DocumentRoot> roots = PhpEnvironment.get().getDocumentRoots();
        descriptor.putProperty(ROOTS, roots);
        for (DocumentRoot root : roots) {
            LocalServer ls = new LocalServer(root.getDocumentRoot() + File.separator + projectName);
            model.addElement(ls);
            if (root.isPreferred()) {
                model.setSelectedItem(ls);
            }
        }
        return model;
    }

    private Boolean isCreateIndex() {
        return (Boolean) descriptor.getProperty(CREATE_INDEX_FILE);
    }

    private Boolean isSetAsMain() {
        return (Boolean) descriptor.getProperty(SET_AS_MAIN);
    }

    private String validFreeProjectName(File parentFolder, int index) {
        String name = MessageFormat.format(NbBundle.getMessage(ConfigureProjectPanel.class, "TXT_DefaultProjectName"),
                new Object[] {index});
        File file = new File(parentFolder, name);
        if (file.exists()) {
            return null;
        }
        return name;
    }

    private String validateProject() {
        String projectName = configureProjectPanelVisual.getProjectName();
        if (projectName.trim().length() == 0) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectName");
        }
        File projectFolder = configureProjectPanelVisual.getProjectFolderFile();
        if (!Utils.isValidFileName(projectFolder)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectFolder");
        }
        String projectPath = projectFolder.getAbsolutePath();
        String err = Utils.validateProjectDirectory(projectPath, "Project", true, false);
        if (err != null) {
            return err;
        }
        warnIfNotEmpty(projectPath, "Project"); // NOI18N
        return null;
    }

    private String validateSources() {
        String err = null;
        LocalServer localServer = configureProjectPanelVisual.getSourcesLocation();
        if (!isProjectFolder(localServer)) {
            String sourcesLocation = localServer.getSrcRoot();

            File sources = FileUtil.normalizeFile(new File(sourcesLocation));
            if (sourcesLocation.trim().length() == 0
                    || !Utils.isValidFileName(sources)) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalSourcesName");
            }

            err = Utils.validateProjectDirectory(sourcesLocation, "Sources", true, true); // NOI18N
            if (err != null) {
                return err;
            }

            warnIfNotEmpty(sourcesLocation, "Sources"); // NOI18N
        }
        err = validateSourcesAndCopyTarget();
        if (err != null) {
            return err;
        }
        return null;
    }

    private String validateIndexFile() {
        String indexName = configureProjectPanelVisual.getIndexName();
        if (!Utils.isValidFileName(indexName)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalIndexName");
        }
        if (configureProjectPanelVisual.isCreateIndex()) {
            // check whether the index file already exists
            LocalServer localServer = configureProjectPanelVisual.getSourcesLocation();
            if (!isProjectFolder(localServer)) {
                File indexFile = new File(localServer.getSrcRoot(), indexName);
                if (indexFile.exists()) {
                    return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IndexNameExists");
                }
            }
        }
        return null;
    }

    // #131023
    private String validateSourcesAndCopyTarget() {
        Boolean isValid = (Boolean) descriptor.getProperty(ConfigureServerPanel.SERVER_IS_VALID);
        if (isValid != null && !isValid) {
            // some error there, need to be fixed, so do not compare
            return null;
        }
        Boolean copyFiles = (Boolean) descriptor.getProperty(ConfigureServerPanel.COPY_FILES);
        if (copyFiles == null || !copyFiles) {
            return null;
        }
        LocalServer sources = configureProjectPanelVisual.getSourcesLocation();
        String sourcesSrcRoot = sources.getSrcRoot();
        if (isProjectFolder(sources)) {
            File project = configureProjectPanelVisual.getProjectFolderFile();
            File src = new File(project, DEFAULT_SOURCES_FOLDER);
            sourcesSrcRoot = src.getAbsolutePath();
        }
        LocalServer copyTarget = (LocalServer) descriptor.getProperty(ConfigureServerPanel.COPY_TARGET);
        File normalized = FileUtil.normalizeFile(new File(copyTarget.getSrcRoot()));
        String cpTarget = normalized.getAbsolutePath();
        return Utils.validateSourcesAndCopyTarget(sourcesSrcRoot, cpTarget);
    }

    // type - Project | Sources
    private void warnIfNotEmpty(String location, String type) {
        // warn if the folder is not empty
        File destFolder = new File(location);
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // folder exists and is not empty - but just warning
            String warning = NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "NotEmpty");
            descriptor.putProperty("WizardPanel_errorMessage", warning); // NOI18N
        }
    }

    // we will do this only if the name equals to the project directory and not vice versa
    private void adjustProjectNameAndLocation() {
        assert originalProjectName != null;
        String projectName = configureProjectPanelVisual.getProjectName();
        if (projectName.length() == 0) {
            // invalid situation, do not change anything
            return;
        }
        if (originalProjectName.equals(projectName)) {
            // no change in project name
            return;
        }
        File projectFolderFile = configureProjectPanelVisual.getProjectFolderFile();
        String projectFolder = projectFolderFile.getName();
        if (!originalProjectName.equals(projectFolder)) {
            // already "disconnected"
            return;
        }
        originalProjectName = projectName;
        File newProjecFolder = new File(projectFolderFile.getParentFile(), projectName);
        // because JTextField.setText() calls document.remove() and then document.insert() (= 2 events!), just remove and readd the listener
        configureProjectPanelVisual.removeConfigureProjectListener(this);
        configureProjectPanelVisual.setProjectFolder(newProjecFolder.getAbsolutePath());
        configureProjectPanelVisual.addConfigureProjectListener(this);
    }

    public void stateChanged(ChangeEvent e) {
        adjustProjectNameAndLocation();
        fireChangeEvent();
    }
}
