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

package org.netbeans.modules.uml.samples.bankapp;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.uml.core.support.UMLLogger;
import org.netbeans.modules.uml.project.UMLProjectGenerator;
import org.netbeans.modules.uml.project.ui.common.JavaSourceRootsUI;
import org.netbeans.modules.uml.project.ui.wizards.NewUMLProjectWizardIterator;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class UMLBankAppSampleWizardIterator 
    implements WizardDescriptor.InstantiatingIterator
{
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;
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
        FileObject template = Templates.getTemplate(wiz);
        FileObject javaPrjFO = FileUtil.createFolder(javaPrjDir);
         unZipFile(template.getInputStream(), javaPrjFO);
        UMLLogger.logMessage("Java path in sample wizard: "+wiz.getProperty("projdir")+"; Normalized path: "+javaPrjDir+"; Java Project Name: "+wiz.getProperty("name"), Level.INFO);
        
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
                    WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i)); // NOI18N
                
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
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
        InputStream source, FileObject projectRoot)
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
                            if (fo.getName().equals("README")) // NOI18N
                                    readmeFO = fo;
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
    
    private void renameJavaProjectTokens(
        File javaPrjDir, String javaPrjName)
    {
        File[] files = javaPrjDir.listFiles();        
        for (int i=0; i < files.length; i++)
        {
            if (files[i].isDirectory() && 
                files[i].getName().equals("nbproject")) // NOI18N
            {
                FileWriter xml = null;
                Document prXmlDoc= null;
                try
                {
                    File projectXml=new File(new File(javaPrjDir,files[i].getName()),"project.xml");//NOI18N
                    FileObject projectXmlFo=FileUtil.toFileObject(projectXml);
                    InputStream is=projectXmlFo.getInputStream();
                    UMLLogger.logMessage("Project file for replacement: "+projectXml, Level.INFO);
                    try {
                            prXmlDoc=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
                            NodeList names=prXmlDoc.getElementsByTagName("name");//NOI18N
                            if(names.item(0)!=null)
                            {
                                Node name=names.item(0);
                                name.setTextContent(javaPrjName);
                            }
                    } catch (ParserConfigurationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    catch (SAXException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    finally
                    {
                        if(is!=null)is.close();
                    }
                   OutputStream os=projectXmlFo.getOutputStream();
                   DOMSource xmlSource=new DOMSource(prXmlDoc);
                   StreamResult strRes=new StreamResult(os);
                    try {
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        try {
                            transformer.transform(xmlSource, strRes);
                        } catch (TransformerException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    } catch (TransformerConfigurationException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                   finally
                   {
                       if(os!=null)os.close();
                   }
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
