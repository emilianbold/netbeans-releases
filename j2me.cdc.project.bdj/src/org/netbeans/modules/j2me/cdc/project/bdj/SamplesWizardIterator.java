/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
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

package org.netbeans.modules.j2me.cdc.project.bdj;

import java.awt.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.project.CDCPropertiesDescriptor;
import org.netbeans.modules.j2me.cdc.project.ui.wizards.NewCDCProjectWizardIterator;
import org.netbeans.modules.j2me.cdc.project.ui.wizards.PanelConfigurePlatform;
import org.netbeans.modules.j2me.cdc.project.ui.wizards.PanelConfigureProject;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformInstallPanel;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;

public class SamplesWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N

    private static final String MANIFEST_FILE = "manifest.mf"; // NOI18N

    private static final long serialVersionUID = 1L;
    
    private int type = NewCDCProjectWizardIterator.TYPE_SAMPLE;
    
    private int index;
    private WizardDescriptor.Panel[] panels;
    private WizardDescriptor wiz;
    private String platform;
    private String preferredName;
    
    public SamplesWizardIterator() {}
    
    public static SamplesWizardIterator createIterator() {
        return new SamplesWizardIterator();
    }
        
    private WizardDescriptor.Panel[] createPanels () {
        int i = getNumberOfSuitableCdcPlatforms(platform);
        return i != 0 ?
            new WizardDescriptor.Panel[] {
                new PanelConfigurePlatform(platform),
                new PanelConfigureProject( this.type, preferredName )} :
            new WizardDescriptor.Panel[] {
                new PlatformInstallPanel.WizardPanel(platform,CDCPlatform.PLATFORM_CDC),
                new PanelConfigurePlatform(platform),
                new PanelConfigureProject( this.type, preferredName )
            };
    }

    private String[] createSteps() {
        int i = getNumberOfSuitableCdcPlatforms(platform);
        return i != 0 ?
            new String[] {
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_SelectPlatform") ,
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_ConfigureProject") }:
            new String[] {
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_Step_AddPlatform"), //NOI18N
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_SelectPlatform") ,
                NbBundle.getMessage(NewCDCProjectWizardIterator.class,"LAB_ConfigureProject")
            };
    }
    
    public Set/*<FileObject>*/ instantiate() throws IOException {
                
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        File dirPr = (File)wiz.getProperty("projdir");        //NOI18N
        if (dirPr != null) {
            dirPr = FileUtil.normalizeFile(dirPr);
        }
    
        String name = (String)wiz.getProperty("name");        //NOI18N

        final String activePlatform = (String)wiz.getProperty("activePlatform");       //NOI18N
        final String activeDevice   = (String)wiz.getProperty("activeDevice");         //NOI18N
        final String activeProfile  = (String)wiz.getProperty("activeProfile");        //NOI18N
        final FileObject template = Templates.getTemplate(wiz);
        PlatformSelectionPanel.PlatformDescription pd=(PlatformSelectionPanel.PlatformDescription) wiz.getProperty(PlatformSelectionPanel.PLATFORM_DESCRIPTION);
        AntProjectHelper h =J2MEProjectGenerator.createProject(dirPr, name, pd,new J2MEProjectGenerator.ProjectGeneratorCallback() {
            public void doPostGeneration(Project p, final AntProjectHelper h, FileObject dir, File projectLocationFile, ArrayList<String> configurations) throws IOException 
            {
                
                createManifest(dir, MANIFEST_FILE);                
                final String mainData = unZipFile(template.getInputStream(), dir);
                final FileObject lib = dir.getFileObject("lib");
                if (lib != null){
                    final ReferenceHelper refHelper = (ReferenceHelper) p.getLookup().lookup(ReferenceHelper.class);
                    try {
                        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
                            public Object run() throws Exception {
                                final List<String> entries = new ArrayList<String>();
                                final FileObject[] libs = lib.getChildren();
                                for (int i = 0; i < libs.length; i++) {
                                    String ref = refHelper.createForeignFileReference(FileUtil.normalizeFile(FileUtil.toFile(libs[i])), null);
                                    entries.add(ref + ((i < libs.length - 1) ? ";" : ""));
                                }

                                EditableProperties editableProps = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                editableProps.setProperty("extra.classpath", entries.toArray(new String[entries.size()]));
                                editableProps.setProperty("libs.classpath", entries.toArray(new String[entries.size()]));
                                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, editableProps); // #47609
                                return null;
                            }
                        });
                    } catch (MutexException me ) {
                        ErrorManager.getDefault().notify (me);
                    }
                }
                
                final FileObject res = dir.getFileObject("resources");
                if (res != null){
                    final ReferenceHelper refHelper = (ReferenceHelper) p.getLookup().lookup(ReferenceHelper.class);
                    try {
                        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
                            public Object run() throws Exception {
                                String ref = ";"+refHelper.createForeignFileReference(FileUtil.normalizeFile(FileUtil.toFile(res)), null);

                                EditableProperties editableProps = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                editableProps.setProperty("libs.classpath", editableProps.getProperty("libs.classpath")+ref);
                                h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, editableProps); // #47609
                                return null;
                            }
                        });
                    } catch (MutexException me ) {
                        ErrorManager.getDefault().notify (me);
                    }
                }

                JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms (activePlatform, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
                if (platforms.length != 0){
                    CDCPlatform cdcplatform = (CDCPlatform)platforms[0];
                    final EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);                
                    ep.setProperty(CDCPropertiesDescriptor.APPLICATION_NAME, p.getProjectDirectory().getNameExt());
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE, cdcplatform.getAntName()); // NOI18N        
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION, cdcplatform.getDisplayName()); // NOI18N        
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_TRIGGER, "CDC"); // NOI18N        
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_TYPE, cdcplatform.getType()); // NOI18N        
                    String classVersion = cdcplatform.getClassVersion();
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_DEVICE, activeDevice); // NOI18N
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_PROFILE, activeProfile); // NOI18N
                    if (mainData != null){
                        Properties prop = new Properties();
                        prop.load(new ByteArrayInputStream(mainData.getBytes()));
                        ep.setProperty(CDCPropertiesDescriptor.MAIN_CLASS, prop.getProperty("main.class", "")); //NOI18N
                        ep.setProperty(CDCPropertiesDescriptor.MAIN_CLASS_CLASS, prop.getProperty("main.type", "xlet")); //NOI18N
                    }                    
                    //add bootclasspath
                    NewCDCProjectWizardIterator.generatePlatformProperties(cdcplatform, activeDevice, activeProfile, ep); // NOI18N
                    ep.setProperty(DefaultPropertiesDescriptor.JAVAC_SOURCE, classVersion != null ? classVersion : "1.3"); // NOI18N
                    ep.setProperty(DefaultPropertiesDescriptor.JAVAC_TARGET, classVersion != null ? classVersion : "1.3"); // NOI18N
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                } else {
                    throw new IllegalArgumentException("No CDC platform installed");// NOI18N
                } 
            }
        });

        resultSet.add (h.getProjectDirectory ());
        dirPr = (dirPr != null) ? dirPr.getParentFile() : null;
        if (dirPr != null && dirPr.exists()) {
            ProjectChooser.setProjectsFolder (dirPr);    
        }
        return resultSet;
    }
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;        
        FileObject template = Templates.getTemplate(wiz);
        preferredName = template.getName();
        this.wiz.putProperty("name", preferredName);
        platform = (String) template.getAttribute("platform");
        
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
        this.wiz.putProperty("additionalProperties", new Properties());
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty("projdir",null);
        this.wiz.putProperty("additionalProperties", null);
        this.wiz.putProperty("projdir",null);           //NOI18N
        this.wiz.putProperty("name",null);          //NOI18N
        this.wiz.putProperty("mainClass",null);         //NOI18N
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format("{0} of {1}",
                new Object[] {new Integer(index + 1), new Integer(panels.length)});
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
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    private static String unZipFile(InputStream source, FileObject projectRoot) throws IOException {
        String mainClass = null;
        try {
            ZipInputStream str = new ZipInputStream(source);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(projectRoot, entry.getName());
                    str.closeEntry();
                } else {
                    if ("main".equals(entry.getName())){ //ignore                        
                        byte[] b = new byte[4096];
                        int i = str.read(b);
                        str.closeEntry();
                        mainClass = new String(b, 0, i);
                        continue;
                    }                    
                    FileObject fo = FileUtil.createData(projectRoot, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            str.closeEntry();
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
        return mainClass;
    }

    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @param path the relative path of the file
     * @throws IOException in case of problems
     */
    private static void createManifest(FileObject dir, String path) throws IOException {
        FileObject manifest = dir.createData(MANIFEST_FILE);
        FileLock lock = manifest.lock();
        try {
            OutputStream os = manifest.getOutputStream(lock);
            try {
                PrintWriter pw = new PrintWriter(os);
                pw.println("Manifest-Version: 1.0"); // NOI18N
                pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                pw.println(); // safest to end in \n\n due to JRE parsing bug
                pw.flush();
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    static int getNumberOfSuitableCdcPlatforms(String platformType){
        Set<String> accepted = null;
        if (platformType != null){
            accepted = new HashSet<String>();
            StringTokenizer st = new StringTokenizer(platformType, ",");
            while(st.hasMoreTokens()){
                accepted.add(st.nextToken());
            }
        }
        
            
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms (null, new Specification(CDCPlatform.PLATFORM_CDC,null));    //NOI18N
        if (accepted == null)
            return platforms.length;
        
        List<JavaPlatform> plf = new ArrayList<JavaPlatform>();
        for (JavaPlatform platform : platforms) {
            if (accepted.contains( ((CDCPlatform)platform).getType()))
                plf.add(platform);
        }
        return plf.size();
    }    
}
