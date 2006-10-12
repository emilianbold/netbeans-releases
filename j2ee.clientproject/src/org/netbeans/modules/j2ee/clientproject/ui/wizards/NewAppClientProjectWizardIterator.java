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

package org.netbeans.modules.j2ee.clientproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.clientproject.ui.FoldersListSettings;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Application Client project.
 */
public class NewAppClientProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    static final String PROP_NAME_INDEX = "nameIndex";      //NOI18N
    
    private static final long serialVersionUID = 1L;
    
    public static NewAppClientProjectWizardIterator library() {
        return new NewAppClientProjectWizardIterator();
    }
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new PanelConfigureProject()
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(NewAppClientProjectWizardIterator.class,"LAB_ConfigureProject"),
        };
    }
    
    
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        File dirF = (File)wiz.getProperty(WizardProperties.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        String name = (String)wiz.getProperty(WizardProperties.NAME);
        String mainClass = (String)wiz.getProperty(WizardProperties.MAIN_CLASS);
        
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        
        AntProjectHelper h = AppClientProjectGenerator.createProject(dirF, name, mainClass, j2eeLevel, serverInstanceID);
        if (mainClass != null && mainClass.length() > 0) {
            try {
                //String sourceRoot = "src"; //(String)j2seProperties.get (J2SEProjectProperties.SRC_DIR);
                FileObject sourcesRoot = h.getProjectDirectory().getFileObject("src/java");        //NOI18N
                FileObject mainClassFo = getMainClassFO(sourcesRoot, mainClass);
                assert mainClassFo != null : "sourcesRoot: " + sourcesRoot + ", mainClass: " + mainClass;        //NOI18N
                // Returning FileObject of main class, will be called its preferred action
                resultSet.add(mainClassFo);
            } catch (Exception x) {
                ErrorManager.getDefault().notify(x);
            }
        }
        FileObject dir = FileUtil.toFileObject(dirF);
        
        Project earProject = (Project) wiz.getProperty(WizardProperties.EAR_APPLICATION);
        AppClientProject createdAppClientProject = (AppClientProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdAppClientProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addCarModule(createdAppClientProject.getAPICar());
            }
        }
        
        // remember last used server
	FoldersListSettings.getDefault().setLastUsedServer(serverInstanceID);
        
        // downgrade the Java platform or src level to 1.4
        String platformName = (String)wiz.getProperty(WizardProperties.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(WizardProperties.SOURCE_LEVEL);
        if (platformName != null || sourceLevel != null) {
            AppClientProjectGenerator.setPlatform(h, platformName, sourceLevel);
        }
        
        // Returning FileObject of project diretory.
        // Project will be open and set as main
        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        FoldersListSettings.getDefault().setNewApplicationCount(index.intValue());
        resultSet.add(dir);
        
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder(dirF);
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
        //set the default values of the sourceRoot and the testRoot properties
        this.wiz.putProperty(WizardProperties.SOURCE_ROOT, new File[0]);
        this.wiz.putProperty(WizardProperties.TEST_ROOT, new File[0]);
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) { // #74316
            this.wiz.putProperty(WizardProperties.PROJECT_DIR, null);
            this.wiz.putProperty(WizardProperties.NAME, null);
            this.wiz.putProperty(WizardProperties.MAIN_CLASS, null);
            this.wiz = null;
        }
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewAppClientProjectWizardIterator.class,"LAB_IteratorName"),
                new Object[] {new Integer(index + 1), new Integer(panels.length) });
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
    
    // helper methods, finds mainclass's FileObject
    private FileObject getMainClassFO(FileObject sourcesRoot, String mainClass) {
        // replace '.' with '/'
        mainClass = mainClass.replace('.', '/');
        
        // ignore unvalid mainClass ???
        
        return sourcesRoot.getFileObject(mainClass+ ".java"); // NOI18N
    }
    
    static String getPackageName(String displayName) {
        StringBuffer builder = new StringBuffer();
        boolean firstLetter = true;
        for (int i=0; i< displayName.length(); i++) {
            char c = displayName.charAt(i);
            if ((!firstLetter && Character.isJavaIdentifierPart(c)) || (firstLetter && Character.isJavaIdentifierStart(c))) {
                firstLetter = false;
                if (Character.isUpperCase(c)) {
                    c = Character.toLowerCase(c);
                }
                builder.append(c);
            }
        }
        return builder.length() == 0 ? NbBundle.getMessage(NewAppClientProjectWizardIterator.class,"TXT_DefaultPackageName") : builder.toString();
    }
    
}
