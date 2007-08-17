/*
 * Copyright (c) 2007, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * * Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * 
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of Sun Microsystems, Inc. nor the names of its contributors
 *   may be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.netbeans.modules.uml.samples.bankapp;

import java.awt.Component;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.uml.project.UMLProjectGenerator;
import org.netbeans.modules.uml.project.UMLProjectHelper;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI;
import org.netbeans.modules.uml.project.ui.wizards.NewUMLProjectWizardIterator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class UMLBankAppSampleWizardIterator 
    implements WizardDescriptor.InstantiatingIterator
{
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;

    private FileObject umlProjectZipFO;
    private FileObject readmeFO;
    
    public UMLBankAppSampleWizardIterator()
    {}
    
    public static UMLBankAppSampleWizardIterator createIterator()
    {
        return new UMLBankAppSampleWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels()
    {
        return new WizardDescriptor.Panel[] {
            new UMLBankAppSampleWizardPanel(),
        };
    }
    
    private String[] createSteps()
    {
        return new String[] 
        {
            NbBundle.getMessage(UMLBankAppSampleWizardIterator.class, 
                "LBL_CreateProjectStep") // NOI18N
        };
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException
    {
        Set resultSet = new LinkedHashSet();

        // Create Java Project
        //////////////////////

        File javaPrjDir = 
            FileUtil.normalizeFile((File)wiz.getProperty("projdir")); // NOI18N
        
        javaPrjDir.mkdirs();
        FileObject template = Templates.getTemplate(wiz);
        FileObject javaPrjFO = FileUtil.toFileObject(javaPrjDir);
        unZipFile(template.getInputStream(), javaPrjFO, false);
        
        renameJavaProjectTokens(javaPrjDir, wiz.getProperty("name").toString()); // NOI18N
        
        // Always open top dir as a project:
        resultSet.add(javaPrjFO);
        
        // Look for nested projects to open as well:
        Enumeration e = javaPrjFO.getFolders(true);
        while (e.hasMoreElements())
        {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder))
                resultSet.add(subfolder);
        }
        
        File parent = javaPrjDir.getParentFile();
        if (parent != null && parent.exists())
            ProjectChooser.setProjectsFolder(parent);

        if (readmeFO != null)
            resultSet.add(readmeFO);
        
        // Create UML Project
        /////////////////////
        
        File umlPrjDir = 
            FileUtil.normalizeFile((File)wiz.getProperty("umlprojdir")); // NOI18N
  
        Project javaProject = ProjectManager.getDefault().findProject(javaPrjFO);

        UMLProjectGenerator.createRevEngProject(
            umlPrjDir, wiz.getProperty("umlname").toString(),  // NOI18N
            javaProject, JavaSourceRootsUI.createModel(javaProject), null, 
            NewUMLProjectWizardIterator.TYPE_REVERSE_ENGINEER);
        
        // Always open top dir as a project:
        resultSet.add(FileUtil.toFileObject(umlPrjDir));
        
        return resultSet;
    }
    
    
    public void initialize(WizardDescriptor wiz)
    {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < panels.length; i++)
        {
            Component c = panels[i].getComponent();
            if (steps[i] == null)
            {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent)
            { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(
                    "WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz)
    {
        this.wiz.putProperty("projdir",null); // NOI18N
        this.wiz.putProperty("name",null); // NOI18N
        this.wiz = null;
        panels = null;
    }
    
    public String name()
    {
        return MessageFormat.format("{0} of {1}", // NOI18N
            new Object[] {new Integer(index + 1), new Integer(panels.length)});
    }
    
    public boolean hasNext()
    {
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious()
    {
        return index > 0;
    }
    
    public void nextPanel()
    {
        if (!hasNext())
        {
            throw new NoSuchElementException();
        }
        index++;
    }
    
    public void previousPanel()
    {
        if (!hasPrevious())
        {
            throw new NoSuchElementException();
        }
        index--;
    }
    
    public WizardDescriptor.Panel current()
    {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l)
    {}
    public final void removeChangeListener(ChangeListener l)
    {}
    
    private void unZipFile(
        InputStream source, FileObject projectRoot, boolean isUmlPrj)
        throws IOException
    {
        try
        {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            
            while ((entry = str.getNextEntry()) != null)
            {
                if (entry.isDirectory())
                    FileUtil.createFolder(projectRoot, entry.getName());
                
                else
                {
                    FileObject fo = 
                        FileUtil.createData(projectRoot, entry.getName());
                    
                    FileLock lock = fo.lock();
                
                    try
                    {
                        OutputStream out = fo.getOutputStream(lock);
                        try
                        {
                            if (!isUmlPrj)
                            {
                                if (fo.getNameExt().equals("umlProject.zip")) // NOI18N
                                    umlProjectZipFO = fo;

                                else if (fo.getName().equals("README")) // NOI18N
                                    readmeFO = fo;
                            }

                            FileUtil.copy(str, out);
                        }
                        
                        finally
                        {
                            out.close();
                        }
                    }
                    
                    finally
                    {
                        lock.releaseLock();
                    }
                }
            }
        }
        
        finally
        {
            source.close();
        }
    }
    
    private final static String FILE_CONTENTS_JAVA_PROJECT_XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + System.getProperty("line.separator") + // NOI18N
        "<project xmlns=\"http://www.netbeans.org/ns/project/1\">" + System.getProperty("line.separator") + // NOI18N
        "    <type>org.netbeans.modules.java.j2seproject</type>" + System.getProperty("line.separator") + // NOI18N
        "    <configuration>" + System.getProperty("line.separator") + // NOI18N
        "        <data xmlns=\"http://www.netbeans.org/ns/j2se-project/2\">" + System.getProperty("line.separator") + // NOI18N
        "            <name>{0}</name>" + System.getProperty("line.separator") + // NOI18N
        "            <minimum-ant-version>1.6</minimum-ant-version>" + System.getProperty("line.separator") + // NOI18N
        "            <source-roots>" + System.getProperty("line.separator") + // NOI18N
        "                <root id=\"src.dir\"/>" + System.getProperty("line.separator") + // NOI18N
        "            </source-roots>" + System.getProperty("line.separator") + // NOI18N
        "            <test-roots>" + System.getProperty("line.separator") + // NOI18N
        "                <root id=\"test.src.dir\"/>" + System.getProperty("line.separator") + // NOI18N
        "            </test-roots>" + System.getProperty("line.separator") + // NOI18N
        "        </data>" + System.getProperty("line.separator") + // NOI18N
        "    </configuration>" + System.getProperty("line.separator") + // NOI18N
        "</project>" + System.getProperty("line.separator"); // NOI18N
    
    private void renameJavaProjectTokens(
        File javaPrjDir, String javaPrjName)
    {
        File[] files = javaPrjDir.listFiles();

        String[] args = {javaPrjName};
        
        for (int i=0; i < files.length; i++)
        {
            if (files[i].isDirectory() && 
                files[i].getName().equals("nbproject")) // NOI18N
            {
                FileWriter xml = null;

                try
                {
                    xml = new FileWriter(files[i].getAbsolutePath() + 
                        File.separatorChar + "project.xml"); // NOI18N

                    xml.flush();

                    xml.write(MessageFormat.format(
                        FILE_CONTENTS_JAVA_PROJECT_XML, args));
                }
                
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                
                finally
                {
                    try
                    {
                        if (xml != null)
                            xml.close();
                    }
                    
                    catch (IOException ex)
                    {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

}
