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
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectType;
import org.netbeans.modules.php.project.ui.wizards.LocalServer;
import org.netbeans.modules.php.rt.utils.PhpProjectSharedConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Tomas Mysik
 */
public class NewPhpProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    private WizardDescriptor descriptor;
    private WizardDescriptor.Panel[] panels;
    private int index;

    public void initialize(WizardDescriptor wizard) {
        descriptor = wizard;
        index = 0;
        panels = createPanels();

        // wizard title
        String title = NbBundle.getMessage(NewPhpProjectWizardIterator.class, "TXT_PhpProject");
        descriptor.putProperty("NewProjectWizard_Title", title); // NOI18N
    }

    public void uninitialize(WizardDescriptor wizard) {
        wizard.putProperty(ConfigureProjectPanel.PROJECT_NAME, null);
        wizard.putProperty(ConfigureProjectPanel.PROJECT_DIR, null);
        wizard.putProperty(ConfigureProjectPanel.SET_AS_MAIN, null);
        wizard.putProperty(ConfigureProjectPanel.WWW_FOLDER, null);
        wizard.putProperty(ConfigureProjectPanel.LOCAL_SERVERS, null);
        wizard.putProperty(ConfigureProjectPanel.URL, null);
        wizard.putProperty(ConfigureProjectPanel.CREATE_INDEX_FILE, null);
        wizard.putProperty(ConfigureProjectPanel.INDEX_FILE, null);
        wizard.putProperty(ConfigureProjectPanel.ENCODING, null);

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

        // project
        File projectDirectory = (File) descriptor.getProperty(ConfigureProjectPanel.PROJECT_DIR);
        String projectName = (String) descriptor.getProperty(ConfigureProjectPanel.PROJECT_NAME);
        AntProjectHelper helper = createProject(projectDirectory, projectName);
        resultSet.add(helper.getProjectDirectory());

        // sources
        FileObject sourceDir = createSourceRoot(helper);
        resultSet.add(sourceDir);

        // index file
        String indexFile = (String) descriptor.getProperty(ConfigureProjectPanel.INDEX_FILE);
        if (indexFile != null) {
            msg = NbBundle.getMessage(
                    NewPhpProjectWizardIterator.class, "LBL_NewPhpProjectWizardIterator_WizardProgress_CreatingIndexFile");
            handle.progress(msg, 4);

            FileObject template = Templates.getTemplate(descriptor);
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
        return panels[index];
    }

    public void addChangeListener(ChangeListener l) {
    }

    public void removeChangeListener(ChangeListener l) {
    }

    private WizardDescriptor.Panel[] createPanels() {
        String[] steps = new String[] {
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_ProjectTitleName"),
            NbBundle.getBundle(NewPhpProjectWizardIterator.class).getString("LBL_ProjectServer"),
        };

        return new WizardDescriptor.Panel[] {
            new ConfigureProjectPanel(steps),
            new ConfigureServerPanel(steps),
        };
    }

    private AntProjectHelper createProject(File dir, String name) throws IOException {
        FileObject projectFO = FileUtil.createFolder(new File(dir, name));
        AntProjectHelper helper = ProjectGenerator.createProject(projectFO, PhpProjectType.TYPE);

        // configure
        Element data = helper.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(PhpProjectType.PROJECT_CONFIGURATION_NAMESPACE,
                PhpProjectSharedConstants.PHP_PROJECT_NAME);
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        helper.putPrimaryConfigurationData(data, true);

        EditableProperties properties = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        configureSources(helper, properties);
        configureEncoding(properties);

        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, properties);

        Project project = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
        ProjectManager.getDefault().saveProject(project);

        return helper;
    }

    private File getSources(AntProjectHelper helper) {
        LocalServer localServer = (LocalServer) descriptor.getProperty(ConfigureProjectPanel.WWW_FOLDER);
        if (ConfigureProjectPanel.isProjectFolder(localServer)) {
            File projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
            return new File(projectDirectory, ConfigureProjectPanel.DEFAULT_SOURCE_FOLDER);
        }
        return new File(localServer.getSrcRoot());
    }

    private void configureSources(AntProjectHelper helper, EditableProperties properties) {
        File srcDir = getSources(helper);
        File projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        String srcPath = PropertyUtils.relativizeFile(projectDirectory, srcDir);
        if (srcPath.startsWith("../")) { // NOI18N
            // relative path, change to absolute
            srcPath = srcDir.getAbsolutePath();
        }

        properties.setProperty(PhpProject.SRC, srcPath);
    }

    private void configureEncoding(EditableProperties properties) {
        Charset charset = (Charset) descriptor.getProperty(ConfigureProjectPanel.ENCODING);
        properties.setProperty(PhpProject.SOURCE_ENCODING, charset.name());
    }

    private FileObject createSourceRoot(AntProjectHelper helper) throws IOException {
        return FileUtil.createFolder(getSources(helper));
    }

    private DataObject createIndexFile(FileObject template, FileObject sourceDir) throws IOException {
        String indexFileName = getIndexFileName(template.getExt());

        DataFolder dataFolder = DataFolder.findFolder(sourceDir);
        DataObject dataTemplate = DataObject.find(template);
        return dataTemplate.createFromTemplate(dataFolder, indexFileName);
    }

    private String getIndexFileName(String plannedExt) {
        String name = (String) descriptor.getProperty(ConfigureProjectPanel.INDEX_FILE);
        String ext = "." + plannedExt; // NOI18N
        if (name.endsWith(ext)) {
            return name.substring(0, name.length() - ext.length());
        }
        return name;
    }
}
