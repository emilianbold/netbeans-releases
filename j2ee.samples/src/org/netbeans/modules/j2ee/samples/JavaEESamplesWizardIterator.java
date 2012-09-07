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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.samples;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.examples.WebSampleProjectGenerator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class JavaEESamplesWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    protected transient WizardDescriptor wiz;
    
    public JavaEESamplesWizardIterator() {}
    
    public static JavaEESamplesWizardIterator createIterator() {
        return new JavaEESamplesWizardIterator();
    }
    
    protected WizardDescriptor.Panel[] createPanels() {
        boolean specifyPrjName = "web".equals(Templates.getTemplate(wiz).getAttribute("prjType")); // NOI18N
        return new WizardDescriptor.Panel[] {
            new JavaEESamplesWizardPanel(false, specifyPrjName)
        };
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(JavaEESamplesWizardIterator.class, "LBL_CreateProjectStep") // NOI18N
        };
    }
    
    @Override
    public Set<FileObject> instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator."; // NOI18N
        return null;
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start(2);
        handle.progress(NbBundle.getMessage(JavaEESamplesWizardIterator.class, "LBL_NewSampleProjectWizardIterator_WizardProgress_CreatingProject"), 1); // NOI18N

        Set resultSet = new LinkedHashSet();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(WizardProperties.PROJ_DIR));
        String name = (String)wiz.getProperty(WizardProperties.NAME);
        FileObject template = Templates.getTemplate(wiz);

        FileObject dir = null;
        if ("web".equals(template.getAttribute("prjType"))) { // NOI18N
            // Use generator from web.examples to create project with specified name
            dir = WebSampleProjectGenerator.createProjectFromTemplate(template, dirF, name);
        }
        else {
            // Unzip prepared project only (no way to change name of the project)
            // FIXME: should be modified to create projects with specified name (project.xml files in sub-projects should be modified too)
            // FIXME: web.examples and j2ee.samples modules may be merged into one module
            createFolder(dirF);
            dir = FileUtil.toFileObject(dirF);
            unZipFile(template.getInputStream(), dir);
            WebSampleProjectGenerator.configureServer(dir);
            for (FileObject child : dir.getChildren()) {
                WebSampleProjectGenerator.configureServer(child);
            }
        }

        ProjectManager.getDefault().clearNonProjectCache();
        handle.progress(NbBundle.getMessage(JavaEESamplesWizardIterator.class, "LBL_NewSampleProjectWizardIterator_WizardProgress_PreparingToOpen"), 2); // NOI18N

        // Always open top dir as a project:
        resultSet.add(dir);
        // Look for nested projects to open as well:
        Enumeration e = dir.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }

        File parent = dirF.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }

        handle.finish();
        return resultSet;
    }
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardProperties.SELECTED_INDEX, new Integer(i));
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardProperties.CONTENT_DATA, steps);
            }
        }
        
        FileObject template = Templates.getTemplate(wiz);
        
        wiz.putProperty(WizardProperties.NAME, template.getName());
    }
    
    @Override
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJ_DIR, null);
        this.wiz.putProperty(WizardProperties.NAME, null);
        this.wiz = null;
        panels = null;
    }
    
    @Override
    public String name() {
        return NbBundle.getMessage(JavaEESamplesWizardIterator.class, "LBL_Order", 
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
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
    public WizardDescriptor.Panel current() {
        // Not sure how but issue 217645 proves that it might happen
        if (panels == null) {
            panels = createPanels();
        }
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    @Override
    public final void addChangeListener(ChangeListener l) {}
    @Override
    public final void removeChangeListener(ChangeListener l) {}
    
    private static void unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
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
            }
        } finally {
            source.close();
        }
    }
    
    /** TODO: replace with FileUtil.createFolder(File) in trunk. */
    private static FileObject createFolder(File dir) throws IOException {
        Stack stack = new Stack();
        while (!dir.exists()) {
            stack.push(dir.getName());
            dir = dir.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(dir);
        if (dirFO == null) {
            refreshFileSystem(dir);
            dirFO = FileUtil.toFileObject(dir);
        }
        assert dirFO != null;
        while (!stack.isEmpty()) {
            dirFO = dirFO.createFolder((String)stack.pop());
        }
        return dirFO;
    }
    
    private static void refreshFileSystem(final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }

}
