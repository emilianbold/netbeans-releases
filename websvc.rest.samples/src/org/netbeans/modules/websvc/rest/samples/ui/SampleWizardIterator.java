/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.rest.samples.ui;


import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.websvc.rest.samples.ui.SampleWizardPanel;
import org.netbeans.modules.websvc.rest.samples.util.Log;
import org.netbeans.modules.websvc.rest.samples.util.RestSampleProjectProperties;
import org.netbeans.modules.websvc.rest.samples.util.RestSampleUtils;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
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
    private Project myProject;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    public transient WizardDescriptor wiz;
    private boolean addJerseyLibrary = true;
    private String projectConfigNamespace = RestSampleProjectProperties.WEB_PROJECT_CONFIGURATION_NAMESPACE;
 
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
            NbBundle.getMessage(SampleWizardIterator.class, "MSG_SampleProject"),
        };
    }
    
    public Project getProject() {
        return myProject;
    }
    
    public void setProjectConfigNamespace(String namespace) {
        this.projectConfigNamespace = namespace;
    }
    
    public void setAddJerseyLibrary(boolean flag) {
        addJerseyLibrary = flag;
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
        Set resultSet = new LinkedHashSet();
        File dirF = FileUtil.normalizeFile((File) wiz.getProperty(PROJDIR));
        dirF.mkdirs();
        
        String name = (String) wiz.getProperty(NAME);
        FileObject template = Templates.getTemplate(wiz);

        FileObject dir = FileUtil.toFileObject(dirF);
        
        // All projects associated with the sample are created within a 
        // top level directory.       
        dir = dir.createFolder(name);
        dirF = FileUtil.toFile(dir);
    
        RestSampleUtils.unZipFile(template.getInputStream(), dir);
        
        if (projectConfigNamespace != null)
            RestSampleUtils.setProjectName(dir, projectConfigNamespace, name);
 
        Project p = ProjectManager.getDefault().findProject(dir);
        myProject = p;
    
        if (addJerseyLibrary)
            RestSampleUtils.addJerseyLibrary(myProject);
        
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
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
        return MessageFormat.format("{0} of {1}", //NOI18N
            new Object[] {new Integer (index + 1), new Integer (panels.length)});
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
        try {
            BufferedReader reader = new BufferedReader(new FileReader(FileUtil.toFile(fo)));
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
        }        
    }
}
