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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.project.connections.RemoteClient;
import org.netbeans.modules.php.project.connections.TransferFile;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.actions.DownloadCommand;
import org.netbeans.modules.php.project.ui.actions.RemoteCommand;
import org.netbeans.modules.php.project.ui.actions.RemoteCommand.DefaultOperationMonitor;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.modules.php.project.util.PhpProjectGenerator;
import org.netbeans.modules.php.project.util.PhpProjectGenerator.ProjectProperties;
import org.netbeans.modules.php.spi.phpmodule.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender;
import org.netbeans.modules.php.spi.phpmodule.PhpModuleExtender.ExtendingException;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.windows.InputOutput;

/**
 * Minor note to frameworks - the 1st framework "wins", it means that e.g. web root sets
 * the 1st framework from the list, other frameworks are ignored.
 * @author Tomas Mysik
 */
public class NewPhpProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    public static enum WizardType {
        NEW,
        EXISTING,
        REMOTE,
    }

    private final WizardType wizardType;
    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private int index;

    public NewPhpProjectWizardIterator() {
        this(WizardType.NEW);
    }

    private NewPhpProjectWizardIterator(WizardType wizardType) {
        this.wizardType = wizardType;
    }

    public static NewPhpProjectWizardIterator existing() {
        return new NewPhpProjectWizardIterator(WizardType.EXISTING);
    }

    public static NewPhpProjectWizardIterator remote() {
        return new NewPhpProjectWizardIterator(WizardType.REMOTE);
    }

    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        index = 0;
        panels = createPanels();
        // normally we would do it in uninitialize but we have listener on ide options (=> NPE)
        initDescriptor(wizard);
    }

    public void uninitialize(WizardDescriptor wizard) {
        Panel<WizardDescriptor> current = current();
        // #158483
        if (current instanceof CancelablePanel) {
            ((CancelablePanel) current).cancel();
        }
        panels = null;
        descriptor = null;
    }

    public Set<FileObject> instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();

        Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders = getFrameworkExtenders();

        PhpProjectGenerator.ProjectProperties createProperties = new PhpProjectGenerator.ProjectProperties(
                getProjectDirectory(),
                getSources(descriptor),
                (String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME),
                wizardType == WizardType.REMOTE ? RunAsType.REMOTE : getRunAsType(),
                (Charset) descriptor.getProperty(ConfigureProjectPanel.ENCODING),
                getUrl(),
                wizardType == WizardType.REMOTE ? null : getIndexFile(frameworkExtenders),
                descriptor,
                isCopyFiles(),
                getCopySrcTarget(),
                (RemoteConfiguration) descriptor.getProperty(RunConfigurationPanel.REMOTE_CONNECTION),
                (String) descriptor.getProperty(RunConfigurationPanel.REMOTE_DIRECTORY),
                wizardType == WizardType.REMOTE ? UploadFiles.ON_SAVE : (UploadFiles) descriptor.getProperty(RunConfigurationPanel.REMOTE_UPLOAD));

        PhpProjectGenerator.Monitor monitor = null;
        switch (wizardType) {
            case NEW:
            case EXISTING:
                monitor = new LocalProgressMonitor(handle, frameworkExtenders);
                break;
            case REMOTE:
                monitor = new RemoteProgressMonitor(handle);
                break;
            default:
                throw new IllegalArgumentException("Unknown wizard type: " + wizardType);
        }

        AntProjectHelper helper = PhpProjectGenerator.createProject(createProperties, monitor);
        resultSet.add(helper.getProjectDirectory());

        Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        PhpModule phpModule = project.getLookup().lookup(PhpModule.class);
        assert phpModule != null : "PHP module must exist!";
        FileObject sources = FileUtil.toFileObject(createProperties.getSourcesDirectory());
        resultSet.add(sources);

        // post process
        switch (wizardType) {
            case NEW:
                extendPhpModule(phpModule, frameworkExtenders, monitor, resultSet);
                break;
            case REMOTE:

                downloadRemoteFiles(getRemoteFiles(), createProperties, monitor);
                break;
        }

        // update project properties
        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        List<PhpModuleProperties> phpModuleProperties = getPhpModuleProperties(phpModule, frameworkExtenders);

        FileObject indexFile = setIndexFile(createProperties, projectProperties, privateProperties, phpModuleProperties);
        if (indexFile != null && indexFile.isValid()) {
            resultSet.add(indexFile);
        }
        setWebRoot(createProperties, projectProperties, privateProperties, phpModuleProperties);
        setTests(createProperties, projectProperties, privateProperties, phpModuleProperties);

        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);
        ProjectManager.getDefault().saveProject(project);

        return resultSet;
    }

    public String name() {
        return NbBundle.getMessage(NewPhpProjectWizardIterator.class, "LBL_IteratorName", index + 1, panels.length);
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel<WizardDescriptor> current() {
        setTitle();
        return panels[index];
    }

    private void setTitle() {
        // #158483
        if (descriptor != null) {
            // wizard title
            String msgKey = null;
            switch (wizardType) {
                case NEW:
                    msgKey = "TXT_PhpProject"; // NOI18N
                    break;
                case EXISTING:
                    msgKey = "TXT_ExistingPhpProject"; // NOI18N
                    break;
                case REMOTE:
                    msgKey = "TXT_RemotePhpProject"; // NOI18N
                    break;
                default:
                    throw new IllegalArgumentException("Unknown wizard type: " + wizardType);
            }

            descriptor.putProperty("NewProjectWizard_Title", NbBundle.getMessage(NewPhpProjectWizardIterator.class, msgKey)); // NOI18N
        }
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel<WizardDescriptor>[] createPanels() {
        String step2 = null;
        String step3 = null;
        switch (wizardType) {
            case NEW:
                step2 = "LBL_RunConfiguration"; // NOI18N
                step3 = "LBL_Frameworks"; // NOI18N
                break;
            case EXISTING:
                step2 = "LBL_RunConfiguration"; // NOI18N
                break;
            case REMOTE:
                step2 = "LBL_RemoteConfiguration"; // NOI18N
                step3 = "LBL_RemoteConfirmation"; // NOI18N
                break;
            default:
                throw new IllegalArgumentException("Unknown wizard type: " + wizardType);
        }
        List<String> steps = new ArrayList<String>(3);
        steps.add(NbBundle.getMessage(NewPhpProjectWizardIterator.class, "LBL_ProjectNameLocation"));
        steps.add(NbBundle.getMessage(NewPhpProjectWizardIterator.class, step2));
        if (step3 != null) {
            steps.add(NbBundle.getMessage(NewPhpProjectWizardIterator.class, step3));
        }
        String[] stepsArray = steps.toArray(new String[steps.size()]);

        WizardDescriptor.Panel<WizardDescriptor> panel3 = null;
        switch (wizardType) {
            case NEW:
                panel3 = new PhpFrameworksPanel(stepsArray);
                break;
            case EXISTING:
                break;
            case REMOTE:
                panel3 = new RemoteConfirmationPanel(stepsArray);
                break;
            default:
                throw new IllegalArgumentException("Unknown wizard type: " + wizardType);
        }
        ConfigureProjectPanel configureProjectPanel = new ConfigureProjectPanel(stepsArray, wizardType);

        List<WizardDescriptor.Panel<WizardDescriptor>> pnls = new ArrayList<Panel<WizardDescriptor>>(steps.size());
        pnls.add(configureProjectPanel);
        pnls.add(new RunConfigurationPanel(stepsArray, configureProjectPanel, wizardType));
        if (panel3 != null) {
            pnls.add(panel3);
        }
        @SuppressWarnings("unchecked")
        WizardDescriptor.Panel<WizardDescriptor>[] pnlsArray = (WizardDescriptor.Panel<WizardDescriptor>[]) Array.newInstance(WizardDescriptor.Panel.class, pnls.size());
        return pnls.toArray(pnlsArray);
    }

    // prevent incorrect default values (empty project => back => existing project)
    private void initDescriptor(WizardDescriptor settings) {
        settings.putProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_DIR, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_NAME, null);
        settings.putProperty(ConfigureProjectPanel.SOURCES_FOLDER, null);
        settings.putProperty(ConfigureProjectPanel.LOCAL_SERVERS, null);
        settings.putProperty(ConfigureProjectPanel.ENCODING, null);
        settings.putProperty(RunConfigurationPanel.VALID, null);
        settings.putProperty(RunConfigurationPanel.RUN_AS, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_FILES, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGET, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGETS, null);
        settings.putProperty(RunConfigurationPanel.URL, null);
        settings.putProperty(RunConfigurationPanel.INDEX_FILE, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_CONNECTION, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_DIRECTORY, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_UPLOAD, null);
        settings.putProperty(PhpFrameworksPanel.VALID, null);
        settings.putProperty(PhpFrameworksPanel.EXTENDERS, null);
        settings.putProperty(RemoteConfirmationPanel.REMOTE_FILES, null);
    }

    private File getProjectDirectory() {
        if ((Boolean) descriptor.getProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED)) {
            return (File) descriptor.getProperty(ConfigureProjectPanel.PROJECT_DIR);
        }
        return null;
    }

    static File getSources(WizardDescriptor descriptor) {
        LocalServer localServer = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        return new File(localServer.getSrcRoot());
    }

    private RunAsType getRunAsType() {
        return (RunAsType) descriptor.getProperty(RunConfigurationPanel.RUN_AS);
    }

    private String getUrl() {
        String url = (String) descriptor.getProperty(RunConfigurationPanel.URL);
        if (url == null) {
            // #146882
            url = RunConfigurationPanel.getUrlForSources(wizardType, descriptor);
        }
        return url;
    }

    private String getIndexFile(Map<PhpFrameworkProvider, PhpModuleExtender> frameworkProviders) {
        if (frameworkProviders != null && !frameworkProviders.isEmpty()) {
            // no index for php framework
            return null;
        }
        String indexName = (String) descriptor.getProperty(RunConfigurationPanel.INDEX_FILE);
        if (indexName == null) {
            // run configuration panel not shown at all
            indexName = RunConfigurationPanel.DEFAULT_INDEX_FILE;
        }
        return indexName;
    }

    private Boolean isCopyFiles() {
        PhpProjectProperties.RunAsType runAs = getRunAsType();
        if (runAs == null) {
            return null;
        }
        boolean copyFiles = false;
        switch (runAs) {
            case LOCAL:
                Boolean tmp = (Boolean) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_FILES);
                if (tmp != null && tmp) {
                    copyFiles = true;
                }
                break;
            default:
                // noop
                break;
        }
        return copyFiles;
    }

    private File getCopySrcTarget() {
        if (getRunAsType() == null) {
            return null;
        }
        LocalServer localServer = (LocalServer) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_TARGET);
        if (StringUtils.hasText(localServer.getSrcRoot())) {
            return new File(localServer.getSrcRoot());
        }
        return null;
    }

    private void extendPhpModule(PhpModule phpModule, Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders,
            PhpProjectGenerator.Monitor monitor, Set<FileObject> filesToOpen) {
        assert wizardType == WizardType.NEW : "Extending not allowed for: " + wizardType;
        assert monitor instanceof LocalProgressMonitor;

        LocalProgressMonitor localMonitor = (LocalProgressMonitor) monitor;
        if (!frameworkExtenders.isEmpty()) {
            localMonitor.startingExtending();

            for (Entry<PhpFrameworkProvider, PhpModuleExtender> entry : frameworkExtenders.entrySet()) {
                PhpFrameworkProvider frameworkProvider = entry.getKey();
                assert frameworkProvider != null;

                localMonitor.extending(frameworkProvider.getName());
                PhpModuleExtender phpModuleExtender = entry.getValue();
                if (phpModuleExtender != null) {
                    try {
                        Set<FileObject> newFiles = phpModuleExtender.extend(phpModule);
                        assert newFiles != null;
                        filesToOpen.addAll(newFiles);
                    } catch (ExtendingException ex) {
                        warnUser(ex.getFailureMessage());
                    }
                }
            }
        }

        localMonitor.finishingExtending();
    }

    private void warnUser(String message) {
        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    @SuppressWarnings("unchecked")
    private Set<TransferFile> getRemoteFiles() {
        return (Set<TransferFile>) descriptor.getProperty(RemoteConfirmationPanel.REMOTE_FILES);
    }

    private void downloadRemoteFiles(Set<TransferFile> forDownload, ProjectProperties projectProperties, PhpProjectGenerator.Monitor monitor) {
        assert wizardType == WizardType.REMOTE : "Download not allowed for: " + wizardType;
        assert monitor instanceof RemoteProgressMonitor;
        assert forDownload != null;
        assert !forDownload.isEmpty();

        RemoteProgressMonitor remoteMonitor = (RemoteProgressMonitor) monitor;
        remoteMonitor.startingDownload();

        FileObject sources = FileUtil.toFileObject(projectProperties.getSourcesDirectory());
        RemoteConfiguration remoteConfiguration = projectProperties.getRemoteConfiguration();
        InputOutput remoteLog = RemoteCommand.getRemoteLog(remoteConfiguration.getDisplayName());
        DefaultOperationMonitor downloadOperationMonitor = new DefaultOperationMonitor("LBL_Downloading"); // NOI18N
        RemoteClient remoteClient = new RemoteClient(remoteConfiguration, new RemoteClient.AdvancedProperties()
                    .setInputOutput(remoteLog)
                    .setOperationMonitor(downloadOperationMonitor)
                    .setAdditionalInitialSubdirectory(projectProperties.getRemoteDirectory())
                    .setPreservePermissions(false));
        DownloadCommand.download(remoteClient, remoteLog, downloadOperationMonitor, projectProperties.getName(), sources, forDownload);

        remoteMonitor.finishingDownload();
    }

    private Map<PhpFrameworkProvider, PhpModuleExtender> getFrameworkExtenders() {
        @SuppressWarnings("unchecked")
        Map<PhpFrameworkProvider, PhpModuleExtender> frameworkProviders = (Map<PhpFrameworkProvider, PhpModuleExtender>) descriptor.getProperty(PhpFrameworksPanel.EXTENDERS);
        if (frameworkProviders == null) {
            frameworkProviders = Collections.emptyMap();
        }
        return frameworkProviders;
    }

    private List<PhpModuleProperties> getPhpModuleProperties(PhpModule phpModule, Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders) {
        if (frameworkExtenders.isEmpty()) {
            return Collections.emptyList();
        }
        List<PhpModuleProperties> phpModuleProperties = new ArrayList<PhpModuleProperties>(frameworkExtenders.size());
        for (PhpFrameworkProvider frameworkProvider : frameworkExtenders.keySet()) {
            phpModuleProperties.add(frameworkProvider.getPhpModuleProperties(phpModule));
        }
        return phpModuleProperties;
    }

    private FileObject setIndexFile(PhpProjectGenerator.ProjectProperties createProperties, EditableProperties projectProperties,
            EditableProperties privateProperties, List<PhpModuleProperties> phpModuleProperties) {
        String indexFile = createProperties.getIndexFile();
        switch (wizardType) {
            case NEW:
                if (indexFile == null) {
                    for (PhpModuleProperties properties : phpModuleProperties) {
                        FileObject frameworkIndex = properties.getIndexFile();
                        if (frameworkIndex != null) {
                            indexFile = PropertyUtils.relativizeFile(createProperties.getSourcesDirectory(), FileUtil.toFile(frameworkIndex));
                            assert !indexFile.startsWith("../");
                            break; // 1st wins
                        }
                    }
                }
                break;
            case REMOTE:
                // try to find index file for downloaded files
                indexFile = getIndexFile(null);
                break;
        }

        if (indexFile == null) {
            return null;
        }

        privateProperties.setProperty(PhpProjectProperties.INDEX_FILE, indexFile);
        return FileUtil.toFileObject(createProperties.getSourcesDirectory()).getFileObject(indexFile);
    }

    private void setWebRoot(PhpProjectGenerator.ProjectProperties createProperties, EditableProperties projectProperties,
            EditableProperties privateProperties, List<PhpModuleProperties> phpModuleProperties) {
        for (PhpModuleProperties properties : phpModuleProperties) {
            FileObject webRoot = properties.getWebRoot();
            if (webRoot != null) {
                String relPath = PropertyUtils.relativizeFile(createProperties.getSourcesDirectory(), FileUtil.toFile(webRoot));
                assert relPath != null && !relPath.startsWith("../") : "WebRoot must be underneath Sources";
                projectProperties.setProperty(PhpProjectProperties.WEB_ROOT, relPath);
                break; // 1st wins
            }
        }
    }

    private void setTests(PhpProjectGenerator.ProjectProperties createProperties, EditableProperties projectProperties,
            EditableProperties privateProperties, List<PhpModuleProperties> phpModuleProperties) {
        if (phpModuleProperties.isEmpty()) {
            return;
        }

        File projectDir = createProperties.getProjectDirectory();
        if (projectDir == null) {
            projectDir = createProperties.getSourcesDirectory();
        }
        assert projectDir != null;

        for (PhpModuleProperties properties : phpModuleProperties) {
            FileObject tests = properties.getTests();
            if (tests != null) {
                File testDir = FileUtil.toFile(tests);
                // relativize path
                String testPath = PropertyUtils.relativizeFile(projectDir, testDir);
                if (testPath == null) {
                    // path cannot be relativized => use absolute path (any VCS can be hardly use, of course)
                    testPath = testDir.getAbsolutePath();
                }
                projectProperties.setProperty(PhpProjectProperties.TEST_SRC_DIR, testPath);
                break; // 1st wins
            }
        }
    }

    private static final class LocalProgressMonitor implements PhpProjectGenerator.Monitor {
        private final ProgressHandle handle;
        private final int units;
        private int unit = 0;

        private LocalProgressMonitor(ProgressHandle handle, Map<PhpFrameworkProvider, PhpModuleExtender> frameworkExtenders) {
            assert handle != null;
            assert frameworkExtenders != null;

            this.handle = handle;
            units = 5 + 2 * frameworkExtenders.size();
        }

        public void starting() {
            handle.start(units);

            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingProject");
            handle.progress(msg, 2);
        }

        public void creatingIndexFile() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingIndexFile");
            handle.progress(msg, 4);
        }

        public void finishing() {
        }

        public void startingExtending() {
            unit = 5;
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_StartingExtending");
            handle.progress(msg, unit);
        }

        public void extending(String framework) {
            unit += 2;
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_Extending", framework);
            handle.progress(msg, unit);
        }

        public void finishingExtending() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_PreparingToOpen");
            handle.progress(msg, units);
        }
    }

    private static final class RemoteProgressMonitor implements PhpProjectGenerator.Monitor {
        private final ProgressHandle handle;

        public RemoteProgressMonitor(ProgressHandle handle) {
            assert handle != null;
            this.handle = handle;
        }

        public void starting() {
            handle.start(10);

            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingProject");
            handle.progress(msg, 2);
        }

        public void creatingIndexFile() {
            assert false : "Should not get here";
        }

        public void finishing() {
        }

        public void startingDownload() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_StartingDownload");
            handle.progress(msg, 5);
        }

        public void finishingDownload() {
            String msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_PreparingToOpen");
            handle.progress(msg, 10);
        }
    }
}
