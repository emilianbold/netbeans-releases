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

import org.netbeans.modules.php.project.ui.WebFolderNameProvider;
import org.netbeans.modules.php.project.ui.LocalServer;
import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.text.MessageFormat;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.project.ui.Utils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Tomas Mysik
 */
public class ConfigureProjectPanel implements WizardDescriptor.Panel<WizardDescriptor>,
        WizardDescriptor.FinishablePanel<WizardDescriptor>, WebFolderNameProvider, ChangeListener {

    static final LocalServer DEFAULT_LOCAL_SERVER;

    static final String DEFAULT_SOURCE_FOLDER = "web"; // NOI18N

    static final String PROJECT_NAME = "projectName"; // NOI18N
    static final String PROJECT_DIR = "projectDir"; // NOI18N
    static final String SET_AS_MAIN = "setAsMain"; // NOI18N
    static final String WWW_FOLDER = "wwwFolder"; // NOI18N
    static final String LOCAL_SERVERS = "localServers"; // NOI18N
    static final String URL = "url"; // NOI18N
    static final String CREATE_INDEX_FILE = "createIndexFile"; // NOI18N
    static final String INDEX_FILE = "indexFile"; // NOI18N
    static final String ENCODING = "encoding"; // NOI18N

    private static final FilenameFilter APACHE_FILENAME_FILTER = new FilenameFilter() {
        public boolean accept(File dir, String name) {
            return name.toLowerCase().startsWith("apache"); // NOI18N
        }
    };

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ConfigureProjectPanelVisual configureProjectPanelVisual;
    private LocationPanelVisual locationPanelVisual;
    private OptionsPanelVisual optionsPanelVisual;
    private WizardDescriptor descriptor;
    private final String[] steps;

    static {
        String msg = NbBundle.getMessage(ConfigureProjectPanel.class, "LBL_UseProjectFolder",
                File.separator, ConfigureProjectPanel.DEFAULT_SOURCE_FOLDER);
        DEFAULT_LOCAL_SERVER = new LocalServer(null, null, msg, false);
    }

    public ConfigureProjectPanel(String[] steps) {
        this.steps = steps;
    }

    public Component getComponent() {
        if (configureProjectPanelVisual == null) {
            configureProjectPanelVisual = new ConfigureProjectPanelVisual(this);
            locationPanelVisual = configureProjectPanelVisual.getLocationPanelVisual();
            optionsPanelVisual = configureProjectPanelVisual.getOptionsPanelVisual();
        }
        return configureProjectPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureProjectPanel.class.getName());
    }

    public void readSettings(WizardDescriptor settings) {
        getComponent();
        descriptor = settings;

        unregisterListeners();

        // project
        locationPanelVisual.setProjectLocation(getProjectLocation().getAbsolutePath());
        locationPanelVisual.setProjectName(getProjectName());

        // sources
        locationPanelVisual.setLocalServerModel(getLocalServers());
        LocalServer wwwFolder = getLocalServer();
        if (wwwFolder != null) {
            locationPanelVisual.selectSourcesLocation(wwwFolder);
        }
        locationPanelVisual.setUrl(getUrl());

        // options
        Boolean createIndex = isCreateIndex();
        if (createIndex != null) {
            optionsPanelVisual.setCreateIndex(createIndex);
        }
        String indexName = getIndexName();
        if (indexName != null) {
            optionsPanelVisual.setIndexName(indexName);
        }
        Boolean setAsMain = isSetAsMain();
        if (setAsMain != null) {
            optionsPanelVisual.setSetAsMain(setAsMain);
        }

        registerListeners();
        fireChangeEvent();
    }

    public void storeSettings(WizardDescriptor settings) {
        // project
        settings.putProperty(PROJECT_DIR, FileUtil.normalizeFile(new File(locationPanelVisual.getProjectLocation())));
        settings.putProperty(PROJECT_NAME, locationPanelVisual.getProjectName());

        // sources
        settings.putProperty(WWW_FOLDER, locationPanelVisual.getSourcesLocation());
        settings.putProperty(LOCAL_SERVERS, locationPanelVisual.getLocalServerModel());
        settings.putProperty(URL, locationPanelVisual.getUrl());

        // options
        settings.putProperty(CREATE_INDEX_FILE, optionsPanelVisual.isCreateIndex());
        settings.putProperty(INDEX_FILE, optionsPanelVisual.getIndexName());
        settings.putProperty(ENCODING, optionsPanelVisual.getEncoding());
        settings.putProperty(SET_AS_MAIN, optionsPanelVisual.isSetAsMain());
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
        error = validateOptions();
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
        Boolean isServerValid = (Boolean) descriptor.getProperty(ConfigureServerPanel.SERVER_IS_VALID);
        return isServerValid == null || isServerValid;
    }

    static boolean isProjectFolder(LocalServer localServer) {
        return DEFAULT_LOCAL_SERVER.equals(localServer);
    }

    public String getWebFolderName() {
        getComponent();
        return locationPanelVisual.getProjectName();
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    String[] getSteps() {
        return steps;
    }

    File getProjectLocation() {
        File projectLocation = (File) descriptor.getProperty(PROJECT_DIR);
        if (projectLocation == null
                || projectLocation.getParentFile() == null
                || !projectLocation.isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
            descriptor.putProperty(PROJECT_DIR, projectLocation);
        }
        return projectLocation;
    }

    String getProjectName() {
        String projectName = (String) descriptor.getProperty(PROJECT_NAME);
        if (projectName == null) {
            projectName = getDefaultFreeName(getProjectLocation());
        }
        return projectName;
    }

    private String getDefaultFreeName(File projectLocation) {
        int i = 1;
        String projectName;
        do {
            projectName = validFreeProjectName(projectLocation, i++);
        } while (projectName == null);
        return projectName;
    }

    private String getIndexName() {
        return (String) descriptor.getProperty(INDEX_FILE);
    }

    private LocalServer getLocalServer() {
        return (LocalServer) descriptor.getProperty(WWW_FOLDER);
    }

    private MutableComboBoxModel getLocalServers() {
        MutableComboBoxModel model = (MutableComboBoxModel) descriptor.getProperty(LOCAL_SERVERS);
        if (model != null) {
            return model;
        }
        return getOSDependentLocalServers();
    }

    private MutableComboBoxModel getOSDependentLocalServers() {
        MutableComboBoxModel model = locationPanelVisual.getLocalServerModel();
        assert model.getSize() == 0;

        model.addElement(DEFAULT_LOCAL_SERVER);
        if (isSolaris()) {
            fillSolarisLocalServers(model);
        } else if (Utilities.isWindows()) {
            fillWindowsLocalServers(model);
        } else if (Utilities.isMac()) {
            fillMacLocalServers(model);
        } else if (Utilities.isUnix()) {
            fillUnixLocalServers(model);
        }

        if (model.getSelectedItem() == null) {
            model.setSelectedItem(DEFAULT_LOCAL_SERVER);
        }
        return model;
    }

    private boolean isSolaris() {
        return (Utilities.getOperatingSystem() & Utilities.OS_SOLARIS) != 0
                || (Utilities.getOperatingSystem() & Utilities.OS_SUNOS) != 0;
    }

    private void fillUnixLocalServers(final MutableComboBoxModel model) {
        LocalServer selected = null;

        String webFolderName = getWebFolderName();
        File userDir = new File(System.getProperty("user.home"), "public_html"); // NOI18N
        if (userDir.isDirectory()) {
            LocalServer userDirLS = new LocalServer(getFolderName(userDir, webFolderName));
            model.addElement(userDirLS);
            if (selected == null) {
                selected = userDirLS;
                String user = System.getProperty("user.name"); // NOI18N
                setDefaultUrl("~" + user + "/" + webFolderName); // NOI18N
            }
        }

        File www = new File("/var/www"); // NOI18N
        File wwwLocalhost = new File(www, "localhost"); // NOI18N
        if (wwwLocalhost.isDirectory()) {
            LocalServer wwwLocalhostLS = new LocalServer(getFolderName(wwwLocalhost, webFolderName));
            model.addElement(wwwLocalhostLS);
            if (selected == null) {
                selected = wwwLocalhostLS;
                setDefaultUrl(webFolderName);
            }
        } else if (www.isDirectory()) {
            LocalServer wwwLS = new LocalServer(getFolderName(www, webFolderName));
            model.addElement(wwwLS);
            if (selected == null) {
                selected = wwwLS;
                setDefaultUrl(webFolderName);
            }
        }

        if (selected != null) {
            model.setSelectedItem(selected);
        }
    }

    private void fillSolarisLocalServers(final MutableComboBoxModel model) {
        File htDocs = getSolarisHtDocsDirectory();
        if (htDocs == null) {
            return;
        }
        String webFolderName = getWebFolderName();
        LocalServer htDocsLS = new LocalServer(getFolderName(htDocs, webFolderName));
        model.addElement(htDocsLS);
        model.setSelectedItem(htDocsLS);
        setDefaultUrl(webFolderName);
    }

    private void fillWindowsLocalServers(final MutableComboBoxModel model) {
        File htDocs = getWindowsHtDocsDirectory();
        if (htDocs == null) {
            return;
        }
        String webFolderName = getWebFolderName();
        LocalServer htDocsLS = new LocalServer(getFolderName(htDocs, webFolderName));
        model.addElement(htDocsLS);
        model.setSelectedItem(htDocsLS);
        setDefaultUrl(webFolderName);
    }

    private void fillMacLocalServers(final MutableComboBoxModel model) {
        String webFolderName = getWebFolderName();
        File mamp = new File("/Applications/MAMP/htdocs"); // NOI18N
        if (mamp.isDirectory()) {
            LocalServer mampLS = new LocalServer(getFolderName(mamp, webFolderName));
            model.addElement(mampLS);
            model.setSelectedItem(mampLS);
            setDefaultUrl(webFolderName);
        }
    }

    private String getFolderName(File location, String name) {
        return new File(location, name).getAbsolutePath();
    }

    private String getUrl() {
        String url = (String) descriptor.getProperty(URL);
        if (url == null) {
            url = getDefaultUrl();
        }
        return url;
    }

    private String getDefaultUrl() {
        return "http://localhost/" + getProjectName() + "/" + DEFAULT_SOURCE_FOLDER + "/"; // NOI18N
    }

    private void setDefaultUrl(String urlPart) {
        descriptor.putProperty(URL, "http://localhost/" + urlPart + "/"); // NOI18N
    }

    private File getSolarisHtDocsDirectory() {
        File varDir = new File("/var/"); // NOI18N
        if (!varDir.isDirectory()) {
            return null;
        }
        String[] apaches = varDir.list(APACHE_FILENAME_FILTER);
        if (apaches == null || apaches.length == 0) {
            return null;
        }
        for (String apache : apaches) {
            File htDocs = findHtDocs(new File(varDir, apache), null);
            if (htDocs.isDirectory()) {
                return htDocs;
            }
        }
        return null;
    }

    private File getWindowsHtDocsDirectory() {
        // list all harddrives (C - Z)
        for (int i = 12; i < 36; i++) {
            char hardDrive = Character.toUpperCase(Character.forDigit(i, Character.MAX_RADIX));
            File programFiles = new File(hardDrive + ":\\Program Files"); // NOI18N
            if (!programFiles.isDirectory()) {
                continue;
            }
            File htDocs = findHtDocs(programFiles, APACHE_FILENAME_FILTER);
            if (htDocs != null) {
                return htDocs;
            }
        }
        return null;
    }

    private File findHtDocs(File startDir, FilenameFilter filenameFilter) {
        String[] subDirs = startDir.list(filenameFilter);
        if (subDirs == null || subDirs.length == 0) {
            return null;
        }
        for (String subDir : subDirs) {
            File apacheDir = new File(startDir, subDir);
            File htDocs = new File(apacheDir, "htdocs"); // NOI18N
            if (htDocs.isDirectory()) {
                return htDocs;
            }
            return findHtDocs(apacheDir, filenameFilter);
        }
        return null;
    }

    private Boolean isCreateIndex() {
        return (Boolean) descriptor.getProperty(CREATE_INDEX_FILE);
    }

    private Boolean isSetAsMain() {
        return (Boolean) descriptor.getProperty(SET_AS_MAIN);
    }

    private void registerListeners() {
        // location
        locationPanelVisual.addLocationListener(this);

        // options
        optionsPanelVisual.addOptionsListener(this);
    }

    private void unregisterListeners() {
        // location
        locationPanelVisual.removeLocationListener(this);

        // options
        optionsPanelVisual.removeOptionsListener(this);
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
        String projectLocation = locationPanelVisual.getProjectLocation();
        String projectName = locationPanelVisual.getProjectName();
        String projectPath = locationPanelVisual.getFullProjectPath();

        if (!Utils.isValidFileName(projectName)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectName");
        }

        File f = FileUtil.normalizeFile(new File(projectLocation)).getAbsoluteFile();
        if (Utils.getCanonicalFile(f) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectLocation");
        }
        return Utils.validateProjectDirectory(projectPath, "Project", false, false); // NOI18N
    }

    private String validateSources() {
        String err = null;
        LocalServer localServer = locationPanelVisual.getSourcesLocation();
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

            // warn if the folder is not empty
            File destFolder = new File(sourcesLocation);
            File[] kids = destFolder.listFiles();
            if (destFolder.exists() && kids != null && kids.length > 0) {
                // folder exists and is not empty - but just warning
                String warning = NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_SourcesNotEmpty");
                descriptor.putProperty("WizardPanel_errorMessage", warning); // NOI18N
            }
        }
        err = validateSourcesAndCopyTarget();
        if (err != null) {
            return err;
        }

        String url = locationPanelVisual.getUrl();
        if (!Utils.isValidUrl(url)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_InvalidUrl");
        } else if (!url.endsWith("/")) { // NOI18N
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_UrlNotTrailingSlash");
        }

        return null;
    }

    private String validateOptions() {
        String indexName = optionsPanelVisual.getIndexName();
        if (!Utils.isValidFileName(indexName)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalIndexName");
        }
        if (optionsPanelVisual.isCreateIndex()) {
            // check whether the index file already exists
            LocalServer localServer = locationPanelVisual.getSourcesLocation();
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
        LocalServer sources = locationPanelVisual.getSourcesLocation();
        String sourcesSrcRoot = sources.getSrcRoot();
        if (isProjectFolder(sources)) {
            File project = new File(locationPanelVisual.getProjectLocation(), locationPanelVisual.getProjectName());
            File src = FileUtil.normalizeFile(new File(project, DEFAULT_SOURCE_FOLDER));
            sourcesSrcRoot = src.getAbsolutePath();
        }
        LocalServer copyTarget = (LocalServer) descriptor.getProperty(ConfigureServerPanel.COPY_TARGET);
        File normalized = FileUtil.normalizeFile(new File(copyTarget.getSrcRoot()));
        String cpTarget = normalized.getAbsolutePath();
        return Utils.validateSourcesAndCopyTarget(sourcesSrcRoot, cpTarget);
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
}
