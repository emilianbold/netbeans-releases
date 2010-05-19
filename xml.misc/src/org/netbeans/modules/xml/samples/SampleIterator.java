/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xml.samples;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import static org.netbeans.modules.xml.misc.UI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.06.14
 */
public abstract class SampleIterator implements WizardDescriptor.InstantiatingIterator {

    public SampleIterator(String module, String name) {
        myName = name;
        myModule = module;
    }

    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] { new SamplePanel(myName) };
    }

    protected String[] createSteps() {
        return new String[] { i18n(SampleIterator.class, "LBL_Name_and_Location") }; // NOI18N
    }

    protected FileObject getProjectDir() {
        return myDir;
    }

    public Set<FileObject> instantiate() throws IOException {
        final Set<FileObject> set = new LinkedHashSet<FileObject>();

        FileUtil.runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                File dir = FileUtil.normalizeFile((File) myWizard.getProperty(SampleVisual.PROJECT_DIR));
                dir.mkdirs();
                myDir = FileUtil.toFileObject(dir);
                FileObject template = Templates.getTemplate(myWizard);
                String name = (String) myWizard.getProperty(SampleVisual.PROJECT_NAME);
                myDir = myDir.createFolder(name);
                set.add(myDir);
                unzip(template, myDir);
                changeName(myDir, name, template.getName());
                set.addAll(createProjectApp(myDir.getParent(), name));
            }
        });
        return set;
    }

    protected Set<FileObject> createProjectApp(FileObject folder, String name) throws IOException {
        Set<FileObject> set = new LinkedHashSet<FileObject>();
        FileObject dir = addProject(set, folder, name + "Application", "org-netbeans-modules-" + myModule + "-samples-resources-zip/" + myName + "Application.zip"); // NOI18N
        changeName(dir, name, myName);
        addModule(dir, getProjectDir());
        set.add(dir);
        return set;
    }

    protected FileObject addProject(Set<FileObject> set, FileObject folder, String name, String zip) throws IOException {
        FileObject dir = folder.createFolder(name);
        unzip(FileUtil.getConfigFile(zip), dir);
        set.add(dir);
        return dir;
    }

    protected FileObject renameCompApp(FileObject compAppFolder, String newCompAppName, String oldCompAppName) throws IOException {
        changeName(compAppFolder, newCompAppName, oldCompAppName);
        FileObject casaFileObject = compAppFolder.getFileObject("src").getFileObject("conf").getFileObject(oldCompAppName, "casa"); // NOI18N
        FileLock fileLock = casaFileObject.lock();

        try {
            casaFileObject.rename(fileLock, newCompAppName, "casa"); // NOI18N
        } finally {
            fileLock.releaseLock();
        }
        return casaFileObject;
    }

    protected void changeName(FileObject project, String newName, String name) {
        renameInFile(project.getFileObject("nbproject/project.xml"), newName, name); // NOI18N
        renameInFile(project.getFileObject("nbproject/project.properties"), newName, name); // NOI18N
    }

    protected final void renameInFile(FileObject file, String newName, String name) {
        if (file == null) {
            return;
        }
        try {
            String text = readContent(file.getInputStream());
            text = replace(text, name, newName);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(file.getOutputStream(), "UTF-8")); // NOI18N

            try {
                writer.write(text);
            } finally {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initialize(WizardDescriptor wizard) {
        myWizard = wizard;
        myIndex = 0;
        myPanels = createPanels();
        String[] steps = createSteps();

        for (int i = 0; i < myPanels.length; i++) {
            Component c = myPanels[i].getComponent();

            if (steps[i] == null) {
                steps[i] = c.getName();
            }
            if (!(c instanceof JComponent)) {
                continue;
            }
            JComponent component = (JComponent) c;
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
            component.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
        }
    }

    public void uninitialize(WizardDescriptor wizard) {
        myWizard.putProperty(SampleVisual.PROJECT_DIR, null);
        myWizard.putProperty(SampleVisual.PROJECT_NAME, null);
        myWizard = null;
        myPanels = null;
    }

    public String name() {
        return i18n(SampleVisual.class, "LBL_Step_of", new Integer(myIndex + 1).toString(), new Integer(myPanels.length).toString()); // NOI18N
    }

    public boolean hasNext() {
        return myIndex < myPanels.length - 1;
    }

    public boolean hasPrevious() {
        return myIndex > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        myIndex++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        myIndex--;
    }

    public WizardDescriptor.Panel current() {
        return myPanels[myIndex];
    }

    public void addChangeListener(ChangeListener listener) {}

    public void removeChangeListener(ChangeListener listener) {}

    protected void unzip(FileObject source, FileObject rootFolder) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source.getInputStream());
            ZipEntry entry;

            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                FileObject fo = FileUtil.createData(rootFolder, entry.getName());
                FileLock lock = fo.lock();

                try {
                    OutputStream out = fo.getOutputStream(lock);

                    try {
                        FileUtil.copy(str, out);
                    } finally {
                        out.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        } finally {
            source.getInputStream().close();
        }
    }

    private static String readContent(InputStream stream) throws IOException {
        StringBuilder builder = new StringBuilder();
        String separator = System.getProperty("line.separator"); // NOI18N
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8")); // NOI18N

        try {
            String line = reader.readLine();

            while (line != null) {
                builder.append(line);
                builder.append(separator);
                line = reader.readLine();
            }
        } finally {
            reader.close();
        }
        return builder.toString();
    }

    protected void addModule(FileObject compAppDir, FileObject dir) throws IOException {
        Project project = ProjectManager.getDefault().findProject(compAppDir);
        Project module = ProjectManager.getDefault().findProject(dir);
        AntArtifactProvider provider = (AntArtifactProvider) module.getLookup().lookup(AntArtifactProvider.class);

        if (provider == null) {
            return;
        }
        AntArtifact[] artifacts = provider.getBuildArtifacts();

        if (artifacts == null) {
            return;
        }

        for (AntArtifact artifact : artifacts) {
            String type = artifact.getType();

            if (type.startsWith("CAPS.asa:")) { // NOI18N
                addArtifact(project, artifact);
            }
            else if ("war".equalsIgnoreCase(type)) { // NOI18N
                addArtifact(project, artifact);
            }
        }
    }

    protected abstract void addArtifact(Project project, AntArtifact artifact);

    private String myName;
    private String myModule;
    private FileObject myDir;
    private transient int myIndex;
    private transient WizardDescriptor myWizard;
    private transient WizardDescriptor.Panel[] myPanels;
}
