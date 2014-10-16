/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.ojet.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.html.ojet.OJETUtils;
import org.netbeans.modules.web.clientproject.api.network.NetworkException;
import org.netbeans.modules.web.clientproject.api.network.NetworkSupport;
import org.netbeans.modules.web.clientproject.createprojectapi.ClientSideProjectGenerator;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectProperties;
import org.netbeans.modules.web.clientproject.createprojectapi.CreateProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

public final class NewProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final Logger LOGGER = Logger.getLogger(NewProjectWizardIterator.class.getName());

    private static final String SKELETON_URL = "https://ukc1-twvpn-1.oraclevpn.com/+CSCO+0h756767633A2F2F66797030317576752E68662E62656E7079722E70627A3A38303830++/hudson/job/OJET_Build/lastSuccessfulBuild/artifact/apps/public_samples/OracleJET_QuickStartBasic.zip"; // NOI18N
    private static final File SKELETON_TMP_FILE = new File(System.getProperty("java.io.tmpdir"), "OracleJET_QuickStartBasic.zip"); // NOI18N
    private static final String ZIP_MIME_TYPE = "application/zip"; // NOI18N

    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> baseWizard;
    private final Pair<WizardDescriptor.FinishablePanel<WizardDescriptor>, String> toolsWizard;

    private int index;
    private WizardDescriptor.Panel<WizardDescriptor>[] panels;
    private WizardDescriptor wizardDescriptor;


    private NewProjectWizardIterator() {
        baseWizard = CreateProjectUtils.createBaseWizardPanel("OracleJETApplication"); // NOI18N
        toolsWizard = CreateProjectUtils.createToolsWizardPanel();
    }

    @TemplateRegistration(
            folder = "Project/ClientSide",
            displayName = "#NewProjectWizardIterator.newProject.displayName",
            description = "../resources/NewOracleJETProjectDescription.html",
            iconBase = OJETUtils.OJET_ICON_PATH,
            position = 250)
    @NbBundle.Messages("NewProjectWizardIterator.newProject.displayName=Oracle JET QuickStart Basic")
    public static NewProjectWizardIterator newOracleJETProject() {
        return new NewProjectWizardIterator();
    }

    @NbBundle.Messages({
        "NewProjectWizardIterator.progress.creating=Creating project...",
        "NewProjectWizardIterator.progress.downloading=Downloading QuickStart...",
        "NewProjectWizardIterator.progress.unpacking=Unpacking QuickStart...",
    })
    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(Bundle.NewProjectWizardIterator_progress_creating());

        Set<FileObject> files = new HashSet<>();

        File projectDir = FileUtil.normalizeFile((File) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_DIRECTORY));
        if (!projectDir.isDirectory()
                && !projectDir.mkdirs()) {
            throw new IOException("Cannot create project directory: " + projectDir);
        }
        FileObject projectDirectory = FileUtil.toFileObject(projectDir);
        assert projectDirectory != null : "FileObject must be found for " + projectDir;
        files.add(projectDirectory);

        CreateProjectProperties createProperties = new CreateProjectProperties()
                .setProjectDir(projectDirectory)
                .setProjectName((String) wizardDescriptor.getProperty(CreateProjectUtils.PROJECT_NAME))
                .setSiteRootFolder(""); // NOI18N
        Project project = ClientSideProjectGenerator.createProject(createProperties);

        // quickstart
        setupQuickStart(handle, files, projectDirectory);

        // tools
        CreateProjectUtils.instantiateTools(project, wizardDescriptor);

        handle.finish();
        return files;
    }

    @Override
    public Set instantiate() throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        wizardDescriptor = wizard;
        // #245975
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                initializeInternal();
            }
        });
    }

    void initializeInternal() {
        assert EventQueue.isDispatchThread();
        index = 0;
        panels = new WizardDescriptor.Panel[] {
            baseWizard.first(),
            toolsWizard.first(),
        };
        // Make sure list of steps is accurate.
        List<String> steps = Arrays.asList(
                baseWizard.second(),
                toolsWizard.second()
        );

        // XXX should be lazy
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert steps.get(i) != null : "Missing name for step: " + i;
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps.toArray(new String[0]));
                // name
                jc.setName(steps.get(i));
            }
        }
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_DIRECTORY, null);
        wizardDescriptor.putProperty(CreateProjectUtils.PROJECT_NAME, null);
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        wizardDescriptor.putProperty("NewProjectWizard_Title", Bundle.NewProjectWizardIterator_newProject_displayName()); // NOI18N
        return panels[index];
    }

    @NbBundle.Messages({
        "# {0} - current step index",
        "# {1} - number of steps",
        "NewProjectWizardIterator.name={0} of {1}"
    })
    @Override
    public String name() {
        return Bundle.NewProjectWizardIterator_name(index + 1, panels.length);
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        // noop
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        // noop
    }

    @NbBundle.Messages("NewProjectWizardIterator.error.download=Oracle JET QuickStart Basic")
    private void setupQuickStart(ProgressHandle handle, Set<FileObject> files, FileObject projectDirectory) throws IOException {
        try {
            // download
            handle.progress(Bundle.NewProjectWizardIterator_progress_downloading());
            NetworkSupport.downloadWithProgress(SKELETON_URL, SKELETON_TMP_FILE, Bundle.NewProjectWizardIterator_progress_downloading());

            // check
            if (!isZipFile(SKELETON_TMP_FILE)) {
                // likely not in oracle network
                if (NetworkSupport.showNetworkErrorDialog(Bundle.NewProjectWizardIterator_error_download())) {
                    setupQuickStart(handle, files, projectDirectory);
                }
            } else {
                // unzip
                handle.progress(Bundle.NewProjectWizardIterator_progress_unpacking());
                unzip(SKELETON_TMP_FILE.getAbsolutePath(), FileUtil.toFile(projectDirectory));

                // index file
                files.add(projectDirectory.getFileObject("index.html")); // NOI18N
            }
        } catch (NetworkException ex) {
            LOGGER.log(Level.INFO, "Failed to download Oracle JET QuickStart", ex);
            if (NetworkSupport.showNetworkErrorDialog(Bundle.NewProjectWizardIterator_error_download())) {
                setupQuickStart(handle, files, projectDirectory);
            }
        } catch (InterruptedException ex) {
            // cancelled, should not happen
            assert false;
        }
    }

    private static boolean isZipFile(File file) {
        assert file != null;
        if (!file.exists()) {
            return false;
        }
        return ZIP_MIME_TYPE.equals(FileUtil.getMIMEType(FileUtil.toFileObject(file), ZIP_MIME_TYPE));
    }

    private static void unzip(String zipPath, File targetDirectory) throws IOException {
        assert zipPath != null;
        assert targetDirectory != null;

        try (ZipFile zipFile = new ZipFile(zipPath)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                File destinationFile = new File(targetDirectory, zipEntry.getName());
                ensureParentExists(destinationFile);
                copyZipEntry(zipFile, zipEntry, destinationFile);
            }
        }
    }

    private static void ensureParentExists(File file) throws IOException {
        File parent = file.getParentFile();
        if (!parent.isDirectory()) {
            if (!parent.mkdirs()) {
                throw new IOException("Cannot create parent directories for " + file.getAbsolutePath());
            }
        }
    }

    private static void copyZipEntry(ZipFile zipFile, ZipEntry zipEntry, File destinationFile) throws IOException {
        if (zipEntry.isDirectory()) {
            return;
        }
        try (InputStream inputStream = zipFile.getInputStream(zipEntry); FileOutputStream outputStream = new FileOutputStream(destinationFile)) {
            FileUtil.copy(inputStream, outputStream);
        }
    }

}
