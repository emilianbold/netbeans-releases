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

package org.netbeans.modules.websvc.rest.samples.ui;


import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.rest.samples.util.RestSampleUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author Peter Liu
 */
public class SampleWizardIterator  implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;

    public static final String PROJDIR = "projdir"; // NOI18N
    public static final String NAME = "name"; // NOI18N
    private FileObject myProjectFolder;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    public transient WizardDescriptor wiz;
    private String projectConfigNamespace = "http://www.netbeans.org/ns/web-project/3"; //NOI18N
 
    public SampleWizardIterator() {}
        
    public static SampleWizardIterator createIterator() {
        return new SampleWizardIterator();
    }

    protected WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new SampleWizardPanel(),
        };
    }
    
    protected String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(SampleWizardIterator.class, "MSG_NameAndLocation"),
        };
    }
    
    public FileObject getProjectDirectory() {
        return myProjectFolder;
    }
    
    public void setProjectConfigNamespace(String namespace) {
        this.projectConfigNamespace = namespace;
    }
    
    @Override
    public Set<FileObject> instantiate() throws IOException {
        Set resultSet = new LinkedHashSet();
        
        File projectDirFile = FileUtil.normalizeFile((File) wiz.getProperty(PROJDIR));
        projectDirFile.mkdirs();
        
        FileObject projectDirFileObject = FileUtil.toFileObject(projectDirFile);
        
        String projectName = (String) wiz.getProperty(NAME);
        FileObject template = Templates.getTemplate(wiz);
        RestSampleUtils.unZipFile(template.getInputStream(), projectDirFileObject);
        ProjectManager.getDefault().clearNonProjectCache();
        
        if (projectConfigNamespace != null) {
            RestSampleUtils.setProjectName(projectDirFileObject, projectConfigNamespace, projectName);
        }
 
        myProjectFolder = projectDirFileObject;
    
        // Always open top dir as a project:
        resultSet.add(projectDirFileObject);
        // Look for nested projects to open as well:
        Enumeration e = projectDirFileObject.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }

        File projectParentDir = projectDirFile.getParentFile();
        if (projectParentDir != null && projectParentDir.exists()) {
            ProjectChooser.setProjectsFolder(projectParentDir);
        }

        return resultSet;
    }
    
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
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(PROJDIR,null);
        this.wiz.putProperty(NAME,null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return current().getComponent().getName();
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
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public void addChangeListener(ChangeListener l) {}
    public void removeChangeListener(ChangeListener l) {}
    
    protected void replaceTokens(FileObject dir, String[] files, String[][] tokens) throws IOException {
        for(String file: files) {
            replaceToken(dir.getFileObject(file), tokens); //NoI18n
        }     
    }
    protected void replaceToken(FileObject fo, String[][] tokens) throws IOException {
        if(fo == null)
            return;
        FileLock lock = fo.lock();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(  
                    new FileInputStream(FileUtil.toFile(fo)), 
                    Charset.forName("UTF-8")));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                for(int i=0;i<tokens.length;i++)
                    line = line.replaceAll(tokens[i][0], tokens[i][1]);
                sb.append(line);
                sb.append("\n");
            }
            OutputStreamWriter writer = new OutputStreamWriter(fo.getOutputStream(lock), "UTF-8");
            try {
                writer.write(sb.toString());
            } finally {
                writer.close();
            }
        } finally {
            lock.releaseLock();
            if ( reader!= null) {
                reader.close();
            }
        }        
    }
}
