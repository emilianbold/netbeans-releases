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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Main.java
 *
 * Created on April 6, 2004, 3:39 PM
 */
package org.netbeans.modules.j2me.cdc.project.ui.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.NoSuchElementException;
import javax.swing.JComponent;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.mobility.project.PropertyDescriptor;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2me.cdc.platform.CDCPlatform;
import org.netbeans.modules.j2me.cdc.project.CDCPropertiesDescriptor;
import org.netbeans.modules.mobility.project.DefaultPropertiesDescriptor;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformInstallPanel;
import org.netbeans.modules.mobility.project.ui.wizard.PlatformSelectionPanel;
import org.netbeans.modules.mobility.project.ui.wizard.ProjectPanel;
import org.netbeans.spi.mobility.project.ProjectPropertiesDescriptor;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author  David Kaspar
 */
public class ImportCDCProjectWizardIterator implements TemplateWizard.Iterator {
    
    boolean platformInstall;
    int currentIndex;
    PlatformInstallPanel.WizardPanel platformPanel;
    ImportCDCProjectPanel.WizardPanel sourcesPanel;
    ProjectPanel.WizardPanel projectPanel;
    static final String CDC55="org.netbeans.modules.j2me.cdc.nbproject";
    static final String CDCTOOLKIT="org.netbeans.modules.j2me.cdc.project";
    static final String CDC55TYPE="CDC Pack 5.5";
    static final String CDCTOOLKITTYPE="CDC Toolkit";
    final String prjType;
    
    public ImportCDCProjectWizardIterator()
    {
        this(CDC55);
    }
    
    public static ImportCDCProjectWizardIterator toolkit()
    {
        return new ImportCDCProjectWizardIterator(CDCTOOLKIT);
    }
    
    ImportCDCProjectWizardIterator(String type)
    {
        prjType=type;
    }
    
    public void addChangeListener(@SuppressWarnings("unused")
	final javax.swing.event.ChangeListener changeListener) {
    }
    
    public void removeChangeListener(@SuppressWarnings("unused")
	final javax.swing.event.ChangeListener changeListener) {
    }
    
    public org.openide.WizardDescriptor.Panel current() {
        if (platformInstall) {
            switch (currentIndex) {
                case 0: return platformPanel;
                case 1: return sourcesPanel;
                case 2: return projectPanel;
            }
        } else {
            switch (currentIndex) {
                case 0: return sourcesPanel;
                case 1: return projectPanel;
            }
        }
        throw new IllegalStateException();
    }
    
    public boolean hasNext() {
        if (platformInstall)
            return currentIndex < 2;
        return currentIndex < 1;
    }
    
    public boolean hasPrevious() {
        return currentIndex > 0;
    }
    
    public void initialize(final org.openide.loaders.TemplateWizard templateWizard) {
        platformInstall = PlatformInstallPanel.isPlatformInstalled(CDCPlatform.PLATFORM_CDC) ^ true;
        if (platformInstall)
            platformPanel = new PlatformInstallPanel.WizardPanel(CDCPlatform.PLATFORM_CDC);
        sourcesPanel = new ImportCDCProjectPanel.WizardPanel(prjType);
        projectPanel = new ProjectPanel.WizardPanel(false, true);        
        currentIndex = 0;
        updateStepsList();
    }
    
    public void uninitialize(@SuppressWarnings("unused")
	final org.openide.loaders.TemplateWizard templateWizard) {
        platformPanel = null;
        sourcesPanel = null;
        projectPanel = null;
        currentIndex = -1;
    }
    
    public java.util.Set<DataObject> instantiate(final org.openide.loaders.TemplateWizard templateWizard) throws java.io.IOException {
        final String location = (String) templateWizard.getProperty(ImportCDCProjectPanel.PROJECT_LOCATION);
        final File newLocation = (File) templateWizard.getProperty(ProjectPanel.PROJECT_LOCATION);
        final String name = (String) templateWizard.getProperty(ProjectPanel.PROJECT_NAME);
        
        final EditableProperties oldep=new EditableProperties();
        oldep.load(new FileInputStream(location+File.separator+AntProjectHelper.PROJECT_PROPERTIES_PATH));
        File f=new File(location+File.separator+AntProjectHelper.PRIVATE_PROPERTIES_PATH);        
        final EditableProperties oldpriv=new EditableProperties();
        if (f.exists())
            oldpriv.load(new FileInputStream(f));
        JavaPlatform platforms[]=JavaPlatformManager.getDefault().getPlatforms(null,new Specification(CDCPlatform.PLATFORM_CDC,null));
       
        PlatformSelectionPanel.PlatformDescription pdesc=new PlatformSelectionPanel.PlatformDescription();
        AntProjectHelper helper=J2MEProjectGenerator.createProject(newLocation,name,pdesc,new J2MEProjectGenerator.ProjectGeneratorCallback() {
            public void doPostGeneration(Project project, AntProjectHelper helper, FileObject projectLocation, 
                                         File projectLocationFile, ArrayList<String> configurations) throws IOException 
            {
                final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                final EditableProperties priv = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);


                if (oldep.getProperty("main.class.applet")!=null && oldep.getProperty("main.class.applet").equals("true"))
                    ep.setProperty(CDCPropertiesDescriptor.MAIN_CLASS_CLASS,"applet");
                else if (oldep.getProperty("main.class.xlet")!=null && oldep.getProperty("main.class.xlet").equals("true"))
                    ep.setProperty(CDCPropertiesDescriptor.MAIN_CLASS_CLASS,"xlet");
                else
                    ep.setProperty(CDCPropertiesDescriptor.MAIN_CLASS_CLASS,"main"); 

                //Copy properties of individual property descriptors
                for (ProjectPropertiesDescriptor p : Lookup.getDefault().lookup(new Lookup.Template<ProjectPropertiesDescriptor>(ProjectPropertiesDescriptor.class)).allInstances() ) {
                    for (PropertyDescriptor d : p.getPropertyDescriptors()) {
                        String name=d.getName();
                        if (d.isShared()) {
                            String s=oldep.getProperty(name);
                            if (s!=null)
                                ep.setProperty(name,s);
                        }
                        else
                        {
                            String s=oldpriv.getProperty(name);
                            if (s!=null)
                                priv.setProperty(name,s);
                        }
                    }
                }
                
                FileObject src = projectLocation.createFolder("src");  //NOI18N
                File srcFile = FileUtil.toFile(src);
                FileObject lib = projectLocation.createFolder("lib");  //NOI18N
                File libFile = FileUtil.toFile(lib);
                FileObject res = projectLocation.createFolder("resources");  //NOI18N
                File resFile = FileUtil.toFile(res);
                J2MEProjectGenerator.copyJavaFolder(new File(location, "src"), srcFile, J2MEProjectGenerator.IMPORT_SRC_EXCLUDES);  //NOI18N
                J2MEProjectGenerator.copyFolder(new File(location, "resources"), resFile, J2MEProjectGenerator.IMPORT_SRC_EXCLUDES);  //NOI18N
                J2MEProjectGenerator.copyFolder(new File(location, "lib"), libFile, J2MEProjectGenerator.IMPORT_EXCLUDES);  //NOI18N
                
                String pa=oldep.getProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE);
                if (pa!=null)
                    ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_ACTIVE_DESCRIPTION,pa.replace('_',' '));
                ep.setProperty(DefaultPropertiesDescriptor.PLATFORM_TRIGGER,"CDC");
                ep.setProperty(DefaultPropertiesDescriptor.LIBS_CLASSPATH,oldep.getProperty("javac.classpath"));
                ep.setProperty(DefaultPropertiesDescriptor.SRC_DIR,oldep.getProperty(DefaultPropertiesDescriptor.SRC_DIR));

                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
            }});
        NewCDCProjectWizardIterator.createManifest(helper.getProjectDirectory(),NewCDCProjectWizardIterator.MANIFEST_FILE);
        return Collections.singleton(DataObject.find(helper.getProjectDirectory()));
    }
    
    public String name() {
        return current().getComponent().getName();
    }
    
    public void nextPanel() {
        if (!hasNext())
            throw new NoSuchElementException();
        currentIndex ++;
        updateStepsList();
    }
    
    public void previousPanel() {
        if (!hasPrevious())
            throw new NoSuchElementException();
        currentIndex --;
        updateStepsList();
    }
    
    void updateStepsList() {
        final JComponent component = (JComponent) current().getComponent();
        if (component == null)
            return;
        String[] list;
        if (platformInstall) {
            list = new String[] {
                NbBundle.getMessage(PlatformInstallPanel.class, "TITLE_Platform"), // NOI18N
                NbBundle.getMessage(ImportCDCProjectPanel.class, "TITLE_Project",prjType.equals(CDC55)?CDC55TYPE:CDCTOOLKITTYPE), // NOI18N
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
            };
        } else {
            list = new String[] {
                NbBundle.getMessage(ImportCDCProjectPanel.class, "TITLE_Project",prjType.equals(CDC55)?CDC55TYPE:CDCTOOLKITTYPE), // NOI18N
                NbBundle.getMessage(ProjectPanel.class, "TITLE_Project"), // NOI18N
            };
        }
        component.putClientProperty("WizardPanel_contentData", list); // NOI18N
        component.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(currentIndex)); // NOI18N
    }
    
}
