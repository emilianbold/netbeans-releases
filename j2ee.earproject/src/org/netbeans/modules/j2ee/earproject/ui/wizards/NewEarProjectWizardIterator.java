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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;

import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.EarProject;

import org.netbeans.spi.project.support.ant.AntProjectHelper;

import org.openide.util.NbBundle;

import org.netbeans.modules.j2ee.common.ui.wizards.WizardProperties;
import org.openide.util.HelpCtx;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;

import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectType;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectGenerator;
import org.netbeans.modules.web.project.WebProjectGenerator;
import org.openide.ErrorManager;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick
 */
public class NewEarProjectWizardIterator implements WizardDescriptor.InstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N

    /** Create a new wizard iterator. */
    public NewEarProjectWizardIterator() {}
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new org.netbeans.modules.j2ee.common.ui.wizards.PanelConfigureProject(PROP_NAME_INDEX, 
                NbBundle.getBundle(NewEarProjectWizardIterator.class),
                new HelpCtx(this.getClass())),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/wizards/Bundle").getString("LBL_NWP1_ProjectTitleName") //NOI18N
        };
    }
    
    public Set instantiate() throws IOException {
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        String contextPath = (String) wiz.getProperty(WizardProperties.CONTEXT_PATH);
        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        Boolean createWAR = (Boolean) wiz.getProperty(WizardProperties.CREATE_WAR);
        String warName = null;
        if (createWAR.booleanValue()) {
            warName = (String) wiz.getProperty(WizardProperties.WAR_NAME);
        }
        Boolean createJAR = (Boolean) wiz.getProperty(WizardProperties.CREATE_JAR);
        String jarName = null;
        if (createJAR.booleanValue()) {
            jarName = (String) wiz.getProperty(WizardProperties.JAR_NAME);
        }
        String platformName = (String)wiz.getProperty(WizardProperties.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(WizardProperties.SOURCE_LEVEL);
        
        return testableInstantiate(dirF,name,j2eeLevel, serverInstanceID, contextPath, warName,jarName, platformName, sourceLevel);
    }
    
    Set testableInstantiate(File dirF, String name, String j2eeLevel,
            String serverInstanceID, String contextPath, String warName, String jarName,
            String platformName, String sourceLevel) throws IOException {
        Set resultSet = new HashSet();
        AntProjectHelper h = EarProjectGenerator.createProject(dirF, name, j2eeLevel, serverInstanceID, contextPath, sourceLevel);
        FileObject dir = FileUtil.toFileObject(FileUtil.normalizeFile(dirF));
        Project p = ProjectManager.getDefault().findProject(dir);
        // XXX -- this code may be necessary for 54381 (once 54534 is addressed)
//        try {
//            FileObject fileToOpen = dir.getFileObject("src/conf/application.xml");
//            assert fileToOpen != null : "cannot find the file to open: src/conf/application.xml";
//            resultSet.add(fileToOpen);
//        } catch (Exception x) {
//            ErrorManager.getDefault().log(ErrorManager.INFORMATIONAL, x.getLocalizedMessage());
//        }
        EarProject earProject = (EarProject) p.getLookup().lookup(EarProject.class);
        if (null != earProject) {
            Application app = null;
            try {
                app = DDProvider.getDefault().getDDRoot(earProject.getAppModule().getDeploymentDescriptor());
                app.setDisplayName(name);
                app.write(earProject.getAppModule().getDeploymentDescriptor());
            } catch (java.io.IOException ioe) {
                org.openide.ErrorManager.getDefault().log(ioe.getLocalizedMessage());
            }
        }
        resultSet.add(dir);
        AuxiliaryConfiguration aux = h.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(h, aux, h.getStandardPropertyEvaluator());
        EarProjectProperties epp = new EarProjectProperties((EarProject) p, refHelper, new EarProjectType());
        if (null != warName) {
            File webAppDir = new File(dirF, name+"-war"); // NOI18N
            h = WebProjectGenerator.createProject(FileUtil.normalizeFile(webAppDir),
                    warName,
                    serverInstanceID,
                    WebProjectGenerator.SRC_STRUCT_BLUEPRINTS,
                    j2eeLevel, "/"+warName); //NOI18N
            if (platformName != null || sourceLevel != null) {
                WebProjectGenerator.setPlatform(h, platformName, sourceLevel);
            }
            FileObject dir2 = FileUtil.toFileObject(FileUtil.normalizeFile(webAppDir));
            p = ProjectManager.getDefault().findProject(dir2);
            epp.addJ2eeSubprojects(new Project[] { p });
        }
        if (null != jarName) {
            File ejbJarDir = new File(dirF,name+"-ejb"); // NOI18N
            h = EjbJarProjectGenerator.createProject(FileUtil.normalizeFile(ejbJarDir),jarName,
                    j2eeLevel, serverInstanceID);
            if (platformName != null || sourceLevel != null) {
                EjbJarProjectGenerator.setPlatform(h, platformName, sourceLevel);
            }
            FileObject dir2 = FileUtil.toFileObject(FileUtil.normalizeFile(ejbJarDir));
            p = ProjectManager.getDefault().findProject(dir2);
            epp.addJ2eeSubprojects(new Project[] { p });
        }
        
        // Returning set of FileObject of project diretory.
        // Project will be open and set as main
        return resultSet;
    }
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    transient WizardDescriptor wiz;
    
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
        if (null != wiz) {
        this.wiz.putProperty(WizardProperties.PROJECT_DIR,null);
        this.wiz.putProperty(WizardProperties.NAME,null);
        }
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getBundle("org/netbeans/modules/j2ee/earproject/ui/wizards/Bundle").getString("LBL_WizardStepsCount"), new String[] {(new Integer(index + 1)).toString(), (new Integer(panels.length)).toString()}); //NOI18N
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
    
    // helper methods, finds indexJSP's FileObject
    FileObject getIndexJSPFO(FileObject webRoot, String indexJSP) {
        // replace '.' with '/'
        indexJSP = indexJSP.replace ('.', '/'); // NOI18N
        
        // ignore unvalid mainClass ???
        
        return webRoot.getFileObject (indexJSP, "jsp"); // NOI18N
    }
}
