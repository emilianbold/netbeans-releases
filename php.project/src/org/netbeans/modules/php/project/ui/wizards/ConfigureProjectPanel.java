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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.MessageFormat;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
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
public class ConfigureProjectPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    static final String PROJECT_NAME = "projectName"; // NOI18N
    static final String PROJECT_DIR = "projectDir"; // NOI18N
    static final String SET_AS_MAIN = "setAsMain"; // NOI18N
    static final String WWW_FOLDER = "wwwFolder"; // NOI18N
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

    public ConfigureProjectPanel() {
        steps = new String[] {
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_ProjectTitleName"),
            //NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_ServerConfiguration"),
        };
    }

    public Component getComponent() {
        if (configureProjectPanelVisual == null) {
            configureProjectPanelVisual = new ConfigureProjectPanelVisual(this);
            locationPanelVisual = configureProjectPanelVisual.getLocationPanelVisual();
            sourcesPanelVisual = configureProjectPanelVisual.getSourcesPanelVisual();
            optionsPanelVisual = configureProjectPanelVisual.getOptionsPanelVisual();

            // listeners
            DocumentListener listener = new LocationListener();
            locationPanelVisual.addProjectLocationListener(listener);
            locationPanelVisual.addProjectNameListener(listener);

            // sources

            // options
            ActionListener defaultActionListener = new DefaultActionListener();
            DocumentListener defaultDocumentListener = new DefaultDocumentListener();
            optionsPanelVisual.addCreateIndexListener(defaultActionListener);
            optionsPanelVisual.addIndexNameListener(defaultDocumentListener);
        }
        return configureProjectPanelVisual;
    }

    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureProjectPanel.class.getName());
    }

    public void readSettings(Object settings) {
        getComponent();
        descriptor = (WizardDescriptor) settings;

        // location
        String projectLocation = getProjectLocation().getAbsolutePath();
        String projectName = getProjectName();
        File projectFolder = new File(projectLocation, projectName);
        locationPanelVisual.setProjectLocation(projectLocation);
        locationPanelVisual.setProjectName(projectName);
        locationPanelVisual.setCreatedProjectFolder(projectFolder.getAbsolutePath());

        // sources
    }

    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;

        // location
        d.putProperty(PROJECT_NAME, locationPanelVisual.getProjectName());
        d.putProperty(PROJECT_DIR, locationPanelVisual.getProjectLocation());
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
                || !projectLocation.getParentFile().isDirectory()) {
            projectLocation = ProjectChooser.getProjectsFolder();
        } else {
            projectLocation = projectLocation.getParentFile();
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
        // not allow to create project on unix root folder, see #82339
        File cfl = Utils.getCanonicalFile(new File(projectPath));
        if (Utilities.isUnix() && cfl != null && cfl.getParentFile().getParent() == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectInRootNotSupported");
        }

        final File destFolder = new File(projectPath).getAbsoluteFile();
        if (Utils.getCanonicalFile(destFolder) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectLocation");
        }

        File projLoc = FileUtil.normalizeFile(destFolder);
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectFolderReadOnly");
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_IllegalProjectLocation");
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_ProjectFolderExists");
        }
        return null;
    }

    private String validateSources() {
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

    private class LocationListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            String projectLocation = locationPanelVisual.getProjectLocation();
            String projectName = locationPanelVisual.getProjectName();

            File f = new File(projectLocation, projectName);
            locationPanelVisual.setCreatedProjectFolder(f.getAbsolutePath());

            fireChangeEvent();
        }
    }

    private class DefaultActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            fireChangeEvent();
        }
    }

    private class DefaultDocumentListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void removeUpdate(DocumentEvent e) {
            processUpdate();
        }

        public void changedUpdate(DocumentEvent e) {
            processUpdate();
        }

        private void processUpdate() {
            fireChangeEvent();
        }
    }
}
