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
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectType;
import org.netbeans.modules.php.project.api.PhpLanguageOptions;
import org.netbeans.modules.php.project.connections.spi.RemoteConfiguration;
import org.netbeans.modules.php.project.ui.LocalServer;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.RunAsType;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties.UploadFiles;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Tomas Mysik
 */
public class NewPhpProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    public static enum WizardType {
        NEW,
        EXISTING,
    }

    private final WizardType wizardType;
    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel[] panels;
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

    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        index = 0;
        panels = createPanels();
        // normally we would do it in uninitialize but we have listener on ide options (=> NPE)
        initDescriptor(wizard);
    }

    public void uninitialize(WizardDescriptor wizard) {
        panels = null;
        descriptor = null;
    }

    public Set instantiate() throws IOException {
        assert false : "Cannot call this method if implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }

    public Set instantiate(ProgressHandle handle) throws IOException {
        final Set<FileObject> resultSet = new HashSet<FileObject>();

        handle.start(5);

        String msg = NbBundle.getMessage(
                NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingProject");
        handle.progress(msg, 3);

        // #140346
        // first, create sources
        FileObject sourceDir = createSourceRoot();
        boolean existingSources = sourceDir.getChildren(false).hasMoreElements();

        // project
        File projectDirectory = null;
        if (isProjectFolderUsed()) {
            projectDirectory = (File) descriptor.getProperty(ConfigureProjectPanel.PROJECT_DIR);
        } else {
            projectDirectory = FileUtil.toFile(sourceDir);
        }
        String projectName = (String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME);
        AntProjectHelper helper = createProject(projectDirectory, projectName);
        resultSet.add(helper.getProjectDirectory());

        resultSet.add(sourceDir);

        // Usage Logging
        logUsage(helper.getProjectDirectory(), sourceDir, getRunAsType(), isCopyFiles());

        // index file
        if (existingSources) {
            // we have sources => try to find index file and open it
            String indexName = (String) descriptor.getProperty(RunConfigurationPanel.INDEX_FILE);
            if (indexName == null) {
                // run configuration panel not shown at all
                indexName = RunConfigurationPanel.DEFAULT_INDEX_FILE;
            }
            FileObject indexFile = sourceDir.getFileObject(indexName);
            if (indexFile != null && indexFile.isValid()) {
                resultSet.add(indexFile);
            }
        } else {
            // sources directory is empty
            msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingIndexFile");
            handle.progress(msg, 4);

            FileObject template = null;
            RunAsType runAsType = (RunAsType) descriptor.getProperty(RunConfigurationPanel.RUN_AS);
            if (runAsType == null) {
                // run configuration panel not shown at all
                template = Templates.getTemplate(descriptor);
            } else {
                switch (runAsType) {
                    case SCRIPT:
                        template = Repository.getDefault().getDefaultFileSystem().findResource("Templates/Scripting/EmptyPHP"); // NOI18N
                        break;
                    default:
                        template = Templates.getTemplate(descriptor);
                        break;
                }
            }
            assert template != null : "Template for Index PHP file cannot be null";
            DataObject indexDO = createIndexFile(template, sourceDir);
            if (indexDO != null) {
                resultSet.add(indexDO.getPrimaryFile());
            }
        }

        msg = NbBundle.getMessage(
                NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_PreparingToOpen");
        handle.progress(msg, 5);

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

    public WizardDescriptor.Panel current() {
        // wizard title
        String title = NbBundle.getMessage(NewPhpProjectWizardIterator.class, wizardType == WizardType.NEW ? "TXT_PhpProject" : "TXT_ExistingPhpProject");
        descriptor.putProperty("NewProjectWizard_Title", title); // NOI18N
        return panels[index];
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel[] createPanels() {
        String[] steps = new String[] {
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_ProjectNameLocation"),
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_RunConfiguration"),
        };

        ConfigureProjectPanel configureProjectPanel = new ConfigureProjectPanel(steps, wizardType);
        return new WizardDescriptor.Panel[] {
            configureProjectPanel,
            new RunConfigurationPanel(steps, configureProjectPanel, wizardType),
        };
    }

    // prevent incorrect default values (empty project => back => existing project)
    private void initDescriptor(WizardDescriptor settings) {
        settings.putProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_DIR, null);
        settings.putProperty(ConfigureProjectPanel.PROJECT_NAME, null);
        settings.putProperty(ConfigureProjectPanel.SOURCES_FOLDER, null);
        settings.putProperty(ConfigureProjectPanel.LOCAL_SERVERS, null);
        settings.putProperty(ConfigureProjectPanel.ENCODING, null);
        settings.putProperty(RunConfigurationPanel.RUN_AS, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_FILES, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGET, null);
        settings.putProperty(RunConfigurationPanel.COPY_SRC_TARGETS, null);
        settings.putProperty(RunConfigurationPanel.URL, null);
        settings.putProperty(RunConfigurationPanel.INDEX_FILE, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_CONNECTION, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_DIRECTORY, null);
        settings.putProperty(RunConfigurationPanel.REMOTE_UPLOAD, null);
    }

    private boolean isProjectFolderUsed() {
        return (Boolean) descriptor.getProperty(ConfigureProjectPanel.IS_PROJECT_DIR_USED);
    }
    private AntProjectHelper createProject(File dir, String name) throws IOException {
        FileObject projectFO = FileUtil.createFolder(dir);
        AntProjectHelper helper = ProjectGenerator.createProject(projectFO, PhpProjectType.TYPE);

        // configure
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        helper.putPrimaryConfigurationData(data, true);

        EditableProperties projectProperties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties privateProperties = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

        configureSources(helper, projectProperties, privateProperties);
        configureEncoding(projectProperties, privateProperties);
        configureTags(projectProperties, privateProperties);
        configureIncludePath(projectProperties, privateProperties);
        // #146882
        configureUrl(projectProperties, privateProperties);

        if (getRunAsType() != null) {
            // run configuration panel shown
            configureCopySources(projectProperties, privateProperties);
            configureIndexFile(projectProperties, privateProperties);
            configureRunConfiguration(projectProperties, privateProperties);
        }

        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties);

        Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        ProjectManager.getDefault().saveProject(project);

        return helper;
    }

    private File getSources() {
        LocalServer localServer = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.SOURCES_FOLDER);
        return FileUtil.normalizeFile(new File(localServer.getSrcRoot()));
    }

    private void configureSources(AntProjectHelper helper, EditableProperties projectProperties, EditableProperties privateProperties) {
        File srcDir = getSources();
        File projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        String srcPath = PropertyUtils.relativizeFile(projectDirectory, srcDir);
        assert srcPath != null : String.format("Sources must be relativized: [project: %s, sources: %s]", projectDirectory, srcDir);
        projectProperties.setProperty(PhpProjectProperties.SRC_DIR, srcPath);
        projectProperties.setProperty(PhpProjectProperties.WEB_ROOT, "."); // NOI18N
    }

    private void configureCopySources(EditableProperties projectProperties, EditableProperties privateProperties) {
        privateProperties.put(PhpProjectProperties.COPY_SRC_FILES, String.valueOf(isCopyFiles()));
        privateProperties.put(PhpProjectProperties.COPY_SRC_TARGET, getCopySrcTarget());
    }

    private String getCopySrcTarget() {
        String copyTargetString = ""; // NOI18N
        LocalServer localServer = (LocalServer) descriptor.getProperty(RunConfigurationPanel.COPY_SRC_TARGET);
        if (localServer.getSrcRoot().length() > 0) {
            copyTargetString = FileUtil.normalizeFile(new File(localServer.getSrcRoot())).getAbsolutePath();
        }
        return copyTargetString;
    }

    private void configureIndexFile(EditableProperties projectProperties, EditableProperties privateProperties) {
        String indexFile = (String) descriptor.getProperty(RunConfigurationPanel.INDEX_FILE);
        privateProperties.setProperty(PhpProjectProperties.INDEX_FILE, indexFile);
    }

    private void configureEncoding(EditableProperties projectPoperties, EditableProperties privateProperties) {
        Charset charset = (Charset) descriptor.getProperty(ConfigureProjectPanel.ENCODING);
        projectPoperties.setProperty(PhpProjectProperties.SOURCE_ENCODING, charset.name());
        // #136917
        FileEncodingQuery.setDefaultEncoding(charset);
    }

    private void configureTags(EditableProperties projectPoperties, EditableProperties privateProperties) {
        projectPoperties.setProperty(PhpProjectProperties.SHORT_TAGS, String.valueOf(PhpLanguageOptions.SHORT_TAGS_ENABLED));
        projectPoperties.setProperty(PhpProjectProperties.ASP_TAGS, String.valueOf(PhpLanguageOptions.ASP_TAGS_ENABLED));
    }

    private void configureIncludePath(EditableProperties projectProperties, EditableProperties privateProperties) {
        projectProperties.setProperty(PhpProjectProperties.INCLUDE_PATH, "${" + PhpProjectProperties.GLOBAL_INCLUDE_PATH + "}"); // NOI18N
    }

    private void configureUrl(EditableProperties projectProperties, EditableProperties privateProperties) {
        String url = (String) descriptor.getProperty(RunConfigurationPanel.URL);
        if (url == null) {
            // #146882
            url = RunConfigurationPanel.getUrlForSources(wizardType, descriptor);
        }

        privateProperties.put(PhpProjectProperties.URL, url);
    }

    private void configureRunConfiguration(EditableProperties projectProperties, EditableProperties privateProperties) {
        PhpProjectProperties.RunAsType runAs = getRunAsType();
        privateProperties.put(PhpProjectProperties.RUN_AS, runAs.name());
        switch (runAs) {
            case LOCAL:
            case SCRIPT:
                // nothing to store
                break;
            case REMOTE:
                configureRunAsRemoteWeb(projectProperties, privateProperties);
                break;
            default:
                assert false : "Unhandled RunAsType type: " + runAs;
                break;
        }
    }

    private RunAsType getRunAsType() {
        return (RunAsType) descriptor.getProperty(RunConfigurationPanel.RUN_AS);
    }

    private void configureRunAsRemoteWeb(EditableProperties projectProperties, EditableProperties privateProperties) {
        RemoteConfiguration remoteConfiguration = (RemoteConfiguration) descriptor.getProperty(RunConfigurationPanel.REMOTE_CONNECTION);
        String remoteDirectory = (String) descriptor.getProperty(RunConfigurationPanel.REMOTE_DIRECTORY);
        PhpProjectProperties.UploadFiles uploadFiles = (UploadFiles) descriptor.getProperty(RunConfigurationPanel.REMOTE_UPLOAD);

        privateProperties.put(PhpProjectProperties.REMOTE_CONNECTION, remoteConfiguration.getName());
        privateProperties.put(PhpProjectProperties.REMOTE_DIRECTORY, remoteDirectory);
        privateProperties.put(PhpProjectProperties.REMOTE_UPLOAD, uploadFiles.name());
    }

    private FileObject createSourceRoot() throws IOException {
        return FileUtil.createFolder(getSources());
    }

    private DataObject createIndexFile(FileObject template, FileObject sourceDir) throws IOException {
        String indexFileName = getIndexFileName(template.getExt());
        assert indexFileName != null;

        DataFolder dataFolder = DataFolder.findFolder(sourceDir);
        DataObject dataTemplate = DataObject.find(template);
        return dataTemplate.createFromTemplate(dataFolder, indexFileName);
    }

    private String getIndexFileName(String plannedExt) {
        String name = (String) descriptor.getProperty(RunConfigurationPanel.INDEX_FILE);
        if (name == null) {
            // run configuration panel not shown at all
            name = RunConfigurationPanel.DEFAULT_INDEX_FILE;
        }
        String ext = "." + plannedExt; // NOI18N
        if (name.endsWith(ext)) {
            return name.substring(0, name.length() - ext.length());
        }
        return name;
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

    // http://wiki.netbeans.org/UsageLoggingSpecification
    private void logUsage(FileObject projectDir, FileObject sourceDir, RunAsType runAs, Boolean copyFiles) {
        assert projectDir != null;
        assert sourceDir != null;

        LogRecord logRecord = new LogRecord(Level.INFO, "USG_PROJECT_CREATE_PHP"); // NOI18N
        logRecord.setLoggerName(PhpProject.USG_LOGGER_NAME);
        logRecord.setResourceBundle(NbBundle.getBundle(NewPhpProjectWizardIterator.class));
        logRecord.setResourceBundleName(NewPhpProjectWizardIterator.class.getPackage().getName() + ".Bundle"); // NOI18N
        logRecord.setParameters(new Object[] {
            FileUtil.isParentOf(projectDir, sourceDir) ?  "EXTRA_SRC_DIR_NO" : "EXTRA_SRC_DIR_YES", // NOI18N
            runAs != null ? runAs.name() : "", // NOI18N
            "1", // NOI18N
            (copyFiles != null && copyFiles == Boolean.TRUE) ? "COPY_FILES_YES" : "COPY_FILES_NO" // NOI18N
        });
        Logger.getLogger(PhpProject.USG_LOGGER_NAME).log(logRecord);
    }
}
