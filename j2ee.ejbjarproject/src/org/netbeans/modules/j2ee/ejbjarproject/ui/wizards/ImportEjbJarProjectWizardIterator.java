/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbjarproject.ui.wizards;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.ui.support.ProjectChooser;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectGenerator;
import org.netbeans.modules.j2ee.ejbjarproject.Utils;


/**
 * Wizard to create a new Web project for an existing web module.
 * @author Pavel Buzek
 */
public class ImportEjbJarProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
//    private boolean imp = true;

    // Make sure list of steps is accurate.
    private static final String[] STEPS = new String[]{
                NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_IW_ImportTitle"), //NOI18N
                NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LAB_ConfigureSourceRoots") //NOI18N
            };

    /** Create a new wizard iterator. */
    public ImportEjbJarProjectWizardIterator() {}
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ImportLocation(),
            new PanelSourceFolders.Panel()
        };
    }

    public Set/*<DataObject>*/ instantiate() throws IOException/*, IllegalStateException*/ {
        Set resultSet = new HashSet ();
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        File dirSrcF = (File) wiz.getProperty (WizardProperties.SOURCE_ROOT);
        File[] sourceFolders = (File[]) wiz.getProperty(WizardProperties.JAVA_ROOT);
        File[] testFolders = (File[]) wiz.getProperty(WizardProperties.TEST_ROOT);
        File configFilesFolder = (File) wiz.getProperty(WizardProperties.CONFIG_FILES_FOLDER);
        File libName = (File) wiz.getProperty(WizardProperties.LIB_FOLDER);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        
        EjbJarProjectGenerator.importProject(dirF, name, sourceFolders, testFolders, configFilesFolder, libName, j2eeLevel, serverInstanceID);
        for (int i=0; i<sourceFolders.length; i++) {
            FileObject srcFo = FileUtil.toFileObject(sourceFolders[i]);
            if (srcFo != null) {
                resultSet.add (srcFo);
            }
        }

        FileObject dir = FileUtil.toFileObject (dirF);

        Project earProject = (Project) wiz.getProperty(WizardProperties.EAR_APPLICATION);
        EjbJarProject createdEjbJarProject = (EjbJarProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdEjbJarProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addEjbJarModule(createdEjbJarProject.getAPIEjbJar());
            }
        }
        
        resultSet.add (dir);

        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);    
        }
                        
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        Utils.setSteps(panels, STEPS);
    }

    public void uninitialize(WizardDescriptor wiz) {
        this.wiz.putProperty(WizardProperties.PROJECT_DIR, null);
        this.wiz.putProperty(WizardProperties.NAME, null);
        this.wiz.putProperty (WizardProperties.SOURCE_ROOT, null);
        this.wiz.putProperty(WizardProperties.JAVA_ROOT, null);
        this.wiz.putProperty(WizardProperties.TEST_ROOT, null);
        this.wiz.putProperty(WizardProperties.CONFIG_FILES_FOLDER, null);
        this.wiz.putProperty(WizardProperties.LIB_FOLDER, null);
        this.wiz.putProperty(WizardProperties.SERVER_INSTANCE_ID, null);
        this.wiz.putProperty(WizardProperties.J2EE_LEVEL, null);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(ImportEjbJarProjectWizardIterator.class, "LBL_WizardStepsCount"), 
            new Object[] {(new Integer(index + 1)), (new Integer(panels.length))}); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panels.length - 1;
    }
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        if (!hasNext()) throw new NoSuchElementException();
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
}
