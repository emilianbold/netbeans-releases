/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
        
        //javaPrjDir.mkdirs();
        FileObject template = Templates.getTemplate(wiz);
        FileObject javaPrjFO = FileUtil.createFolder(javaPrjDir);
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
