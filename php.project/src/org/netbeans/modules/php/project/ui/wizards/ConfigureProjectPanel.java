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

import java.awt.Component;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.MutableComboBoxModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
public class ConfigureProjectPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel,
        SourcesPanelVisual.WebFolderNameProvider, ChangeListener {

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

    private final ChangeSupport changeSupport = new ChangeSupport(this);
    private ConfigureProjectPanelVisual configureProjectPanelVisual;
    LocationPanelVisual locationPanelVisual;
    SourcesPanelVisual sourcesPanelVisual;
    OptionsPanelVisual optionsPanelVisual;
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
            sourcesPanelVisual = configureProjectPanelVisual.getSourcesPanelVisual();
            optionsPanelVisual = configureProjectPanelVisual.getOptionsPanelVisual();
        }
        return configureProjectPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureProjectPanel.class.getName());
    }

    public void readSettings(Object settings) {
        getComponent();
        descriptor = (WizardDescriptor) settings;

        unregisterListeners();

        // location
        locationPanelVisual.setProjectLocation(getProjectLocation().getAbsolutePath());
        locationPanelVisual.setProjectName(getProjectName());

        // sources
        MutableComboBoxModel localServers = getLocalServers();
        if (localServers != null) {
            sourcesPanelVisual.setLocalServerModel(localServers);
        }
        LocalServer wwwFolder = getLocalServer();
        if (wwwFolder != null) {
            sourcesPanelVisual.selectSourcesLocation(wwwFolder);
        }
        sourcesPanelVisual.setUrl(getUrl());

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

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;

        // location
        d.putProperty(PROJECT_DIR, new File(locationPanelVisual.getProjectLocation()));
        d.putProperty(PROJECT_NAME, locationPanelVisual.getProjectName());

        // sources
        LocalServer sourceRoot = sourcesPanelVisual.getSourcesLocation();
        d.putProperty(WWW_FOLDER, sourceRoot);
        d.putProperty(LOCAL_SERVERS, sourcesPanelVisual.getLocalServerModel());
        d.putProperty(URL, sourcesPanelVisual.getUrl());

        // options
        d.putProperty(CREATE_INDEX_FILE, optionsPanelVisual.isCreateIndex());
        d.putProperty(INDEX_FILE, optionsPanelVisual.getIndexName());
        d.putProperty(ENCODING, optionsPanelVisual.getEncoding());
        d.putProperty(SET_AS_MAIN, optionsPanelVisual.isSetAsMain());
    }

    public boolean isValid() {
        getComponent();
        String error = validateLocation();
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
        descriptor.putProperty("WizardPanel_errorMessage", " "); // NOI18N
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
        return (MutableComboBoxModel) descriptor.getProperty(LOCAL_SERVERS);
    }

    private String getUrl() {
        String url = (String) descriptor.getProperty(URL);
        if (url == null) {
            url = "http://localhost/" + getProjectName(); // NOI18N
        }
        return url;
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

        // sources
        sourcesPanelVisual.addSourcesListener(this);

        // options
        optionsPanelVisual.addOptionsListener(this);
    }

    private void unregisterListeners() {
        // location
        locationPanelVisual.removeLocationListener(this);

        // sources
        sourcesPanelVisual.removeSourcesListener(this);

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

    private String validateLocation() {
        String projectLocation = locationPanelVisual.getProjectLocation();
        String projectName = locationPanelVisual.getProjectName();
        String projectPath = locationPanelVisual.getFullProjectPath();

        if (!Utils.isValidFileName(projectName)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectName");
        }

        File f = new File(projectLocation).getAbsoluteFile();
        if (Utils.getCanonicalFile(f) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectLocation");
        }
        return validateProjectDirectory(projectPath, "Project"); // NOI18N
    }

    private String validateSources() {
        LocalServer localServer = sourcesPanelVisual.getSourcesLocation();
        if (!isProjectFolder(localServer)) {
            String sourcesLocation = localServer.getSrcRoot();

            File sources = new File(sourcesLocation);
            if (!Utils.isValidFileName(sources.getName())) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalSourcesName");
            }

            String err = validateProjectDirectory(sourcesLocation, "Sources"); // NOI18N
            if (err != null) {
                return err;
            }
        }

        String url = sourcesPanelVisual.getUrl();
        if (!SourcesPanelVisual.isValidUrl(url)) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_InvalidUrl");
        }

        return null;
    }

    private String validateOptions() {
        if (optionsPanelVisual.isCreateIndex()) {
            String indexName = optionsPanelVisual.getIndexName();
            if (!Utils.isValidFileName(indexName)) {
                return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalIndexName");
            }
        }
        return null;
    }

    private String validateProjectDirectory(String projectPath, String type) {
        // not allow to create project on unix root folder, see #82339
        File cfl = Utils.getCanonicalFile(new File(projectPath));
        if (Utilities.isUnix() && cfl != null && cfl.getParentFile().getParent() == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "InRootNotSupported");
        }

        final File destFolder = new File(projectPath).getAbsoluteFile();
        if (Utils.getCanonicalFile(destFolder) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_Illegal" + type + "Location");
        }

        File projLoc = FileUtil.normalizeFile(destFolder);
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "FolderReadOnly");
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_Illegal" + type + "Location");
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "FolderExists");
        }
        return null;
    }

    public void stateChanged(ChangeEvent e) {
        fireChangeEvent();
    }
}
