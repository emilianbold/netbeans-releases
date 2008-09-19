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
import org.netbeans.modules.php.project.ui.SourcesFolderProvider;
import org.netbeans.modules.php.project.ui.LocalServer;
import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.environment.PhpEnvironment;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class ConfigureProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>,
        SourcesFolderProvider, ChangeListener {

    static final String PROJECT_NAME = "projectName"; // NOI18N
    static final String PROJECT_DIR = "projectDir"; // NOI18N
    static final String IS_PROJECT_DIR_USED = "isProjectDirUsed"; // NOI18N
    static final String SET_AS_MAIN = "setAsMain"; // NOI18N
    static final String SOURCES_FOLDER = "sourcesFolder"; // NOI18N
    static final String LOCAL_SERVERS = "localServers"; // NOI18N
    static final String ENCODING = "encoding"; // NOI18N
    static final String ROOTS = "roots"; // NOI18N

    private static final FilenameFilter NB_FILENAME_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return "nbproject".equals(name); // NOI18N
        }
    };

    private final String[] steps;
    private final NewPhpProjectWizardIterator.WizardType wizardType;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ConfigurableProjectPanel configureProjectPanelVisual = null;
    private WizardDescriptor descriptor = null;
    private String originalProjectName = null;
    private String originalSources = null;

    public ConfigureProjectPanel(String[] steps, NewPhpProjectWizardIterator.WizardType wizardType) {
        this.steps = steps;
        this.wizardType = wizardType;
    }

    public Component getComponent() {
        if (configureProjectPanelVisual == null) {
            switch (wizardType) {
                case NEW:
                    configureProjectPanelVisual = new ConfigureNewProjectPanelVisual(this);
                    break;
                case EXISTING:
                    configureProjectPanelVisual = new ConfigureExistingProjectPanelVisual(this);
                    break;
                default:
                    assert false : "Unknown wizard type: " + wizardType;
                    break;
            }
            addListeners();
        }
        return configureProjectPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureProjectPanel.class.getName() + "." + wizardType);
    }

    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        // project
        switch (wizardType) {
            case NEW:
                // sources - we need them first because of free project name
                configureProjectPanelVisual.setLocalServerModel(getLocalServers());
                LocalServer sourcesLocation = getLocalServer();
                if (sourcesLocation != null) {
                    configureProjectPanelVisual.selectSourcesLocation(sourcesLocation);
                }

                // set project name only for empty project
                configureProjectPanelVisual.setProjectName(getProjectName());
                break;
            case EXISTING:
                // noop
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
                break;
        }
        configureProjectPanelVisual.setProjectFolder(getProjectFolder().getAbsolutePath());

        // encoding
        configureProjectPanelVisual.setEncoding(getEncoding());
    }

    private void addListeners() {
        configureProjectPanelVisual.addConfigureProjectListener(this);
    }

    private void removeListeners() {
        configureProjectPanelVisual.removeConfigureProjectListener(this);
    }

    public void storeSettings(WizardDescriptor settings) {
        // project - we have to save it as it is because one can navigate back and forward
        //  => the project folder equals to sources
        settings.putProperty(IS_PROJECT_DIR_USED, configureProjectPanelVisual.isProjectFolderUsed());
        settings.putProperty(PROJECT_DIR, FileUtil.normalizeFile(getProjectFolderFile()));
        settings.putProperty(PROJECT_NAME, configureProjectPanelVisual.getProjectName());

        // sources
        settings.putProperty(SOURCES_FOLDER, configureProjectPanelVisual.getSourcesLocation());
        settings.putProperty(LOCAL_SERVERS, configureProjectPanelVisual.getLocalServerModel());

        // encoding
        settings.putProperty(ENCODING, configureProjectPanelVisual.getEncoding());

        // set as main project - never set as main
        settings.putProperty(SET_AS_MAIN, false);
    }

    /**
     * @return <b>non-normalized</b> {@link File file} for project folder or <code>null</code> if no text is present.
     */
    public File getProjectFolderFile() {
        String projectFolder = configureProjectPanelVisual.getProjectFolder();
        if (projectFolder.length() == 0) {
            return null;
        }
        return new File(projectFolder);
    }

    public boolean isFinishPanel() {
        return isRunConfigurationStepValid();
    }

    public boolean isValid() {
        getComponent();
        descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, " "); // NOI18N
        String error = null;
        // different order of validation for each wizard type
        switch (wizardType) {
            case NEW:
                error = validateProject();
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error); // NOI18N
                    return false;
                }
                error = validateSources(false);
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error); // NOI18N
                    return false;
                }
                break;
            case EXISTING:
                error = validateSources(true);
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error); // NOI18N
                    return false;
                }
                error = validateProject();
                if (error != null) {
                    descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, error); // NOI18N
                    return false;
                }
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
                break;
        }

        return true;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    public File getSourcesFolder() {
        getComponent();
        return FileUtil.normalizeFile(new File(configureProjectPanelVisual.getSourcesLocation().getSrcRoot()));
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
            switch (wizardType) {
                case NEW:
                    assert false : "Project name must be already set during getting possible sourcce directories";
                    break;
                case EXISTING:
                    projectName = getDefaultFreeName(ProjectChooser.getProjectsFolder());
                    break;
                default:
                    assert false : "Unknown wizard type: " + wizardType;
                    break;
            }
            assert projectName != null : "Project name must be already set during getting possible sourcce directories";
            descriptor.putProperty(PROJECT_NAME, projectName);
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

    private Charset getEncoding() {
        Charset enc = (Charset) descriptor.getProperty(ENCODING);
        if (enc == null) {
            // #136917
            enc = FileEncodingQuery.getDefaultEncoding();
        }
        return enc;
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
        // first, get preferred document root because we need to find free folder name for project
        File preferredRoot = ProjectChooser.getProjectsFolder();
        List<DocumentRoot> roots = PhpEnvironment.get().getDocumentRoots();
        for (DocumentRoot root : roots) {
            if (root.isPreferred()) {
                preferredRoot = new File(root.getDocumentRoot());
                break;
            }
        }
        descriptor.putProperty(ROOTS, roots);

        String projectName = getDefaultFreeName(preferredRoot);
        descriptor.putProperty(PROJECT_NAME, projectName);
        MutableComboBoxModel model = new LocalServer.ComboBoxModel(new LocalServer(getProjectFolder().getAbsolutePath()));
        for (DocumentRoot root : roots) {
            LocalServer ls = new LocalServer(root.getDocumentRoot() + File.separator + projectName);
            ls.setHint(root.getHint());
            model.addElement(ls);
            if (root.isPreferred()) {
                model.setSelectedItem(ls);
            }
        }
        return model;
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
        if (!configureProjectPanelVisual.isProjectFolderUsed()) {
            return null;
        }
        File projectFolder = getProjectFolderFile();
        if (projectFolder == null
                || !Utils.isValidFileName(projectFolder)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectFolder");
        }
        String err = Utils.validateProjectDirectory(projectFolder, "Project", true, false);
        if (err != null) {
            return err;
        }
        if (isProjectAlready(projectFolder)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectAlreadyProject");
        }
        warnIfNotEmpty(projectFolder.getAbsolutePath(), "Project"); // NOI18N
        return null;
    }

    // #137230
    private boolean isProjectAlready(File projectFolder) {
        if (!projectFolder.exists()) {
            return false;
        }
        File[] kids = projectFolder.listFiles(NB_FILENAME_FILTER);
        return kids != null && kids.length > 0;
    }

    private String validateSources(boolean children) {
        String err = null;
        LocalServer localServer = configureProjectPanelVisual.getSourcesLocation();
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

        if (children) {
            if (!sources.isDirectory()) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalSourcesName");
            }
            String[] files = sources.list();
            if (files == null || files.length == 0) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_SourcesEmpty");
            }
        }

        if (configureProjectPanelVisual.isProjectFolderUsed()) {
            // project folder used => validate relativity of sources and project folder
            File projectFolder = getProjectFolderFile();
            if (projectFolder != null
                    && PropertyUtils.relativizeFile(FileUtil.normalizeFile(projectFolder), sources) == null) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_SourcesAndProjectCannotBeRelativized");
            }
        } else {
            // project folder not used => validate sources as project folder
            if (isProjectAlready(sources)) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_SourcesAlreadyProject");
            }
        }

        err = validateSourcesAndCopyTarget();
        if (err != null) {
            return err;
        }

        switch (wizardType) {
            case NEW:
                warnIfNotEmpty(sourcesLocation, "Sources"); // NOI18N
                break;
        }

        return null;
    }

    // #131023
    private String validateSourcesAndCopyTarget() {
        if (!isRunConfigurationStepValid()) {
            // some error there, need to be fixed, so do not compare
            return null;
        }
        Boolean copyFiles = (Boolean) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_FILES);
        if (copyFiles == null || !copyFiles) {
            return null;
        }
        LocalServer sources = configureProjectPanelVisual.getSourcesLocation();
        String sourcesSrcRoot = sources.getSrcRoot();
        LocalServer copyTarget = (LocalServer) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_TARGET);
        File normalized = FileUtil.normalizeFile(new File(copyTarget.getSrcRoot()));
        String cpTarget = normalized.getAbsolutePath();
        return Utils.validateSourcesAndCopyTarget(sourcesSrcRoot, cpTarget);
    }

    private boolean isRunConfigurationStepValid() {
        Boolean isValid = (Boolean) descriptor.getProperty(RunConfigurationPanel.VALID);
        if (isValid != null) {
            return isValid;
        }
        return true;
    }

    // type - Project | Sources
    private void warnIfNotEmpty(String location, String type) {
        // warn if the folder is not empty
        File destFolder = new File(location);
        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // folder exists and is not empty - but just warning
            String warning = NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "NotEmpty");
            descriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, warning); // NOI18N
        }
    }

    // we will do this only if the name equals to the project directory and not vice versa
    private void projectNameChanged() {
        String projectName = configureProjectPanelVisual.getProjectName();
        if (projectName.length() == 0) {
            // invalid situation, do not change anything
            return;
        }
        if (originalProjectName == null) {
            originalProjectName = projectName;
        }
        if (projectName.equals(originalProjectName)) {
            // no change in project name
            return;
        }

        adjustProjectFolder(originalProjectName, projectName);
        adjustSources(originalProjectName, projectName);

        originalProjectName = projectName;
    }

    private void adjustProjectFolder(String originalProjectName, String projectName) {
        File projectFolderFile = getProjectFolderFile();
        if (projectFolderFile == null) {
            // invalid folder given, just ignore it
            return;
        }
        String projectFolder = projectFolderFile.getName();
        if (!originalProjectName.equals(projectFolder)) {
            // already "disconnected"
            return;
        }

        File newProjecFolder = new File(projectFolderFile.getParentFile(), projectName);
        configureProjectPanelVisual.setProjectFolder(newProjecFolder.getAbsolutePath());
    }

    private void adjustSources(String originalProjectName, String projectName) {
        LocalServer.ComboBoxModel model = (LocalServer.ComboBoxModel) configureProjectPanelVisual.getLocalServerModel();
        boolean fire = false;
        for (int i = 0; i < model.getSize(); ++i) {
            LocalServer ls = model.getElementAt(i);
            File src = new File(ls.getSrcRoot());
            if (originalProjectName.equals(src.getName())) {
                File newSrc = new File(src.getParentFile(), projectName);
                ls.setSrcRoot(newSrc.getAbsolutePath());
                fire = true;
            }
        }
        if (fire) {
            model.fireContentsChanged();
        }
    }

    private void sourceFolderChanged() {
        String sources = configureProjectPanelVisual.getSourcesLocation().getSrcRoot();
        if (sources.length() == 0) {
            // invalid situation, do not change anything
            return;
        }
        if (sources.equals(originalSources)) {
            // no change in sources
            return;
        }
        adjustProjectName(originalSources, sources);
        String projectName = new File(sources).getName();
        String originalName = null;
        if (originalSources == null) {
            // only for the first time => project folder *must* be valid
            assert getProjectFolderFile() != null;
            originalName = getProjectFolderFile().getName();
        } else {
            originalName = new File(originalSources).getName();
        }
        adjustProjectFolder(originalName, projectName);
        originalSources = sources;
    }

    private void adjustProjectName(String originalSources, String sources) {
        if (originalSources != null) {
            String sourcesFolder = new File(originalSources).getName();
            String projectName = configureProjectPanelVisual.getProjectName();
            if (!sourcesFolder.equals(projectName)) {
                // already "disconnected"
                return;
            }
        }
        String newProjectName = new File(sources).getName();
        configureProjectPanelVisual.setProjectName(newProjectName);
    }

    public void stateChanged(ChangeEvent e) {
        // because JTextField.setText() calls document.remove() and then document.insert() (= 2 events!), just remove and readd the listener
        removeListeners();
        switch (wizardType) {
            case NEW:
                projectNameChanged();
                break;
            case EXISTING:
                sourceFolderChanged();
                break;
            default:
                assert false : "Unknown wizard type: " + wizardType;
                break;
        }
        addListeners();
        fireChangeEvent();
    }
}
