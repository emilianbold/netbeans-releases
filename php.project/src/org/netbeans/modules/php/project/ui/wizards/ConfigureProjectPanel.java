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
import java.text.MessageFormat;
import java.util.regex.Pattern;
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
    static final String ROOTS = "roots"; // NOI18N

    private final String[] steps;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ConfigureProjectPanelVisual configureProjectPanelVisual;
    private LocationPanelVisual locationPanelVisual;
    private OptionsPanelVisual optionsPanelVisual;
    private WizardDescriptor descriptor;
    private String defaultUrl = ""; // NOI18N

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
        String url = getUrl();
        if (url != null) {
            locationPanelVisual.setUrl(url);
        } else {
            adjustUrl();
        }

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

    public String getSourcesFolderName() {
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

    private String getUrl() {
        return (String) descriptor.getProperty(URL);
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

    private String getDefaultUrl(String projectName) {
        return "http://localhost/" + projectName + "/" + DEFAULT_SOURCE_FOLDER + "/"; // NOI18N
    }

    private void adjustUrl() {
        String currentUrl = locationPanelVisual.getUrl();
        if (!defaultUrl.equals(currentUrl)) {
            return;
        }
        LocalServer sources = locationPanelVisual.getSourcesLocation();
        String url = null;
        if (isProjectFolder(sources)) {
            // project/web => check project name and url
            // XXX copy-to-folder should be added
            String correctUrl = getDefaultUrl(getSourcesFolderName());
            if (!defaultUrl.equals(correctUrl)) {
                url = correctUrl;
            }
        } else {
            // /var/www or similar => check source folder name and url
            String srcRoot = sources.getSrcRoot();
            @SuppressWarnings("unchecked")
            List<DocumentRoot> roots = (List<DocumentRoot>) descriptor.getProperty(ROOTS);
            for (DocumentRoot root : roots) {
                String docRoot = root.getDocumentRoot() + File.separator;
                if (srcRoot.startsWith(docRoot)) {
                    String urlSuffix = srcRoot.replaceFirst(Pattern.quote(docRoot), ""); // NOI18N
                    // handle situations like: /var/www/xxx///// or c:\\apache\htdocs\aaa\bbb
                    url = root.getUrl() + urlSuffix.replaceAll(Pattern.quote(File.separator) + "+", "/"); // NOI18N
                    if (!url.endsWith("/")) { // NOI18N
                        url += "/"; // NOI18N
                    }
                    break;
                }
            }
            if (url == null) {
                // not found => get the name of the sources
                url = "http://localhost/" + new File(sources.getSrcRoot()).getName() + "/"; // NOI18N
            }
        }
        if (url != null && !defaultUrl.equals(url)) {
            defaultUrl = url;
            locationPanelVisual.setUrl(url);
        }
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
        adjustUrl();
        fireChangeEvent();
    }
}
