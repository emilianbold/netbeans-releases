/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xtest.examples;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JComponent;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * @author Jiri Skrivanek
 */
public class PaintAppProjectIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 4L;

    int currentIndex;
    PanelConfigureProject basicPanel;
    private transient WizardDescriptor wiz;

    static Object create() {
        return new PaintAppProjectIterator();
    }
    
    public PaintAppProjectIterator () {
    }
    
    public void addChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener (javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current () {
        return basicPanel;
    }
    
    public boolean hasNext () {
        return false;
    }
    
    public boolean hasPrevious () {
        return false;
    }
    
    public void initialize (org.openide.loaders.TemplateWizard templateWizard) {
        this.wiz = templateWizard;
        templateWizard.putProperty(WizardProperties.NAME, NbBundle.getMessage(PaintAppProjectIterator.class, "TXT_ProjectName_PaintApp"));
        
        basicPanel = new PanelConfigureProject();
        currentIndex = 0;
        updateStepsList ();
    }
    
    public void uninitialize (org.openide.loaders.TemplateWizard templateWizard) {
        basicPanel = null;
        currentIndex = -1;
        this.wiz.putProperty("projdir",null);           //NOI18N
        this.wiz.putProperty("name",null);          //NOI18N
    }
    
    public java.util.Set instantiate (org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        Set resultSet = new LinkedHashSet();
        File projectLocation = FileUtil.normalizeFile((File)wiz.getProperty(WizardProperties.PROJECT_DIR));
        String name = (String)wiz.getProperty(WizardProperties.NAME);
        projectLocation.mkdirs();
        
        FileObject template = templateWizard.getTemplate().getPrimaryFile();
        FileObject projectLocationFO = FileUtil.toFileObject(projectLocation);
        unzip(template.getInputStream(), projectLocationFO);
        updateXTestProperties(projectLocation, name);
        
        // Always open top dir as a project:
        resultSet.add(projectLocationFO);
        // Look for nested projects to open as well:
        Enumeration e = projectLocationFO.getFolders(true);
        while (e.hasMoreElements()) {
            FileObject subfolder = (FileObject) e.nextElement();
            if (ProjectManager.getDefault().isProject(subfolder)) {
                resultSet.add(subfolder);
            }
        }
        
        File parent = projectLocation.getParentFile();
        if (parent != null && parent.exists()) {
            ProjectChooser.setProjectsFolder(parent);
        }
        
        return resultSet;
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        throw new NoSuchElementException ();
    }
    
    public void previousPanel() {
        throw new NoSuchElementException ();
    }
    
    void updateStepsList() {
        JComponent component = (JComponent) current ().getComponent ();
        if (component == null) {
            return;
        }
        String[] list;
        list = new String[] {
            NbBundle.getMessage(PanelConfigureProject.class, "LBL_NWP1_ProjectTitleName"), // NOI18N
        };
        component.putClientProperty ("WizardPanel_contentData", list); // NOI18N
        component.putClientProperty ("WizardPanel_contentSelectedIndex", new Integer (currentIndex)); // NOI18N
        component.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage(PaintAppProjectIterator.class, "TXT_Title_PaintApp")); //NOI18N
    }
    
    private static void unzip(InputStream source, FileObject projectRoot) throws IOException {
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
    
    /** Updates properties xtest.home and netbeans.user in Paint/test/build.xml script.
     */
    private static void updateXTestProperties(File projectLocation, String projectName) throws IOException {
        try {
            FileObject prjLoc = FileUtil.toFileObject(projectLocation);
            String filePath = "Paint/test/build.xml";  // NOI18N
            File buildXml = FileUtil.toFile(prjLoc.getFileObject(filePath));
            File xtestHome = InstalledFileLocator.getDefault().
                    locate("xtest-distribution", "org.netbeans.modules.xtest", false);  // NOI18N
            if(xtestHome == null) {
                // it should not happen
                return;
            }
            Document doc = XMLUtil.parse(new InputSource(buildXml.toURI().toString()), false, true, null, null);
            NodeList nlist = doc.getElementsByTagName("property");  //NOI18N
            for (int i=0; i < nlist.getLength(); i++) {
                Element e = (Element)nlist.item(i);
                if(e.getAttribute("name").equals("xtest.home")) { // NOI18N
                    e.setAttribute("location", xtestHome.getAbsolutePath());// NOI18N
                }
            }
            saveXml(doc, prjLoc, filePath);                    
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
    }
    
    /**
     * Save an XML config file to a named path.
     * If the file does not yet exist, it is created.
     */
    private static void saveXml(Document doc, FileObject dir, String path) throws IOException {
        FileObject xml = FileUtil.createData(dir, path);
        FileLock lock = xml.lock();
        try {
            OutputStream os = xml.getOutputStream(lock);
            try {
                XMLUtil.write(doc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
}
