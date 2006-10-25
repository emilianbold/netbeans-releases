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

package org.netbeans.modules.j2ee.earproject.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.EarProjectGenerator;
import org.netbeans.modules.j2ee.earproject.EarProjectType;
import org.netbeans.modules.j2ee.earproject.ui.FoldersListSettings;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Wizard to create a new Enterprise Application project.
 * @author Jesse Glick
 */
public class NewEarProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    transient WizardDescriptor wiz;
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new PanelConfigureProject(PROP_NAME_INDEX,
                    NbBundle.getBundle(NewEarProjectWizardIterator.class),
                    new HelpCtx(this.getClass())),
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName")
        };
    }
    
    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(9);
        handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        // Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        Boolean createWAR = (Boolean) wiz.getProperty(WizardProperties.CREATE_WAR);
        String warName = null;
        if (createWAR.booleanValue()) {
            warName = (String) wiz.getProperty(WizardProperties.WAR_NAME);
        }
        Boolean createJAR = (Boolean) wiz.getProperty(WizardProperties.CREATE_JAR);
        String ejbJarName = null;
        if (createJAR.booleanValue()) {
            ejbJarName = (String) wiz.getProperty(WizardProperties.JAR_NAME);
        }
        Boolean createCAR = (Boolean) wiz.getProperty(WizardProperties.CREATE_CAR);
        String carName = null;
        String mainClass = null;
        if (createCAR.booleanValue()) {
            carName = (String) wiz.getProperty(WizardProperties.CAR_NAME);
            mainClass = (String) wiz.getProperty(WizardProperties.MAIN_CLASS);
        }
        String platformName = (String)wiz.getProperty(WizardProperties.JAVA_PLATFORM);
        String sourceLevel = (String)wiz.getProperty(WizardProperties.SOURCE_LEVEL);
        // remember last used server
        FoldersListSettings.getDefault().setLastUsedServer(serverInstanceID);
        return testableInstantiate(dirF,name,j2eeLevel, serverInstanceID, warName,
                ejbJarName, carName, mainClass, platformName, sourceLevel, handle);
    }
    
    /** <strong>Package private for unit test only</strong>. */
    static Set<FileObject> testableInstantiate(File dirF, String name, String j2eeLevel,
            String serverInstanceID, String warName, String ejbJarName, String carName,
            String mainClass, String platformName, String sourceLevel, ProgressHandle handle) throws IOException {
        Set<FileObject> resultSet = new LinkedHashSet<FileObject>();
        AntProjectHelper h = EarProjectGenerator.createProject(dirF, name, j2eeLevel, serverInstanceID, sourceLevel);
        if (handle != null)
            handle.progress(2);
        FileObject dir = FileUtil.toFileObject(FileUtil.normalizeFile(dirF));
        Project p = ProjectManager.getDefault().findProject(dir);
        EarProject earProject = (EarProject) p.getLookup().lookup(EarProject.class);
        if (null != earProject) {
            Application app = null;
            try {
                app = DDProvider.getDefault().getDDRoot(earProject.getAppModule().getDeploymentDescriptor());
                app.setDisplayName(name);
                app.write(earProject.getAppModule().getDeploymentDescriptor());
            } catch (IOException ioe) {
                ErrorManager.getDefault().log(ioe.getLocalizedMessage());
            }
        }
        
        resultSet.add(dir);
        
        AuxiliaryConfiguration aux = h.createAuxiliaryConfiguration();
        ReferenceHelper refHelper = new ReferenceHelper(h, aux, h.getStandardPropertyEvaluator());
        EarProjectProperties epp = new EarProjectProperties((EarProject) p, refHelper, new EarProjectType());
        Project webProject = null;
        if (null != warName) {
            File webAppDir = new File(dirF, warName);
            
            WebProjectCreateData createData = new WebProjectCreateData();
            createData.setProjectDir(FileUtil.normalizeFile(webAppDir));
            createData.setName(warName);
            createData.setServerInstanceID(serverInstanceID);
            createData.setSourceStructure(WebProjectUtilities.SRC_STRUCT_BLUEPRINTS);
            createData.setJavaEEVersion(EarProjectGenerator.checkJ2eeVersion(j2eeLevel, serverInstanceID, J2eeModule.WAR));
            createData.setContextPath('/' + warName); //NOI18N
            createData.setJavaPlatformName(platformName);
            createData.setSourceLevel(sourceLevel);
            if (handle != null)
                handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_WAR"), 3);
            AntProjectHelper webHelper = WebProjectUtilities.createProject(createData);           
            if (handle != null)
                handle.progress(4);

            FileObject webAppDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(webAppDir));
            webProject = ProjectManager.getDefault().findProject(webAppDirFO);
            epp.addJ2eeSubprojects(new Project[] { webProject });
            resultSet.add(webAppDirFO);
        }
        Project appClient = null;
        if (null != carName) {
            File carDir = new File(dirF,carName);
            if (handle != null)
                handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_AppClient"), 5);
            AntProjectHelper clientHelper = AppClientProjectGenerator.createProject(
                    FileUtil.normalizeFile(carDir), carName, mainClass,
                    EarProjectGenerator.checkJ2eeVersion(j2eeLevel, serverInstanceID,
                    J2eeModule.CLIENT), serverInstanceID);
            if (handle != null)
                handle.progress(6);

            if (platformName != null || sourceLevel != null) {
                AppClientProjectGenerator.setPlatform(clientHelper, platformName, sourceLevel);
            }
            FileObject carDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(carDir));
            appClient = ProjectManager.getDefault().findProject(carDirFO);
            
            epp.addJ2eeSubprojects(new Project[] { appClient });
            resultSet.add(carDirFO);
        }
        if (null != ejbJarName) {
            File ejbJarDir = new File(dirF,ejbJarName);
            if (handle != null)
                handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_EJB"), 7);
            AntProjectHelper ejbHelper = EjbJarProjectGenerator.createProject(FileUtil.normalizeFile(ejbJarDir),ejbJarName,
                    EarProjectGenerator.checkJ2eeVersion(j2eeLevel, serverInstanceID, J2eeModule.EJB), serverInstanceID);
            if (handle != null)
                handle.progress(8);

            if (platformName != null || sourceLevel != null) {
                EjbJarProjectGenerator.setPlatform(ejbHelper, platformName, sourceLevel);
            }
            FileObject ejbJarDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(ejbJarDir));
            Project ejbJarProject = ProjectManager.getDefault().findProject(ejbJarDirFO);
            epp.addJ2eeSubprojects(new Project[] { ejbJarProject });
            resultSet.add(ejbJarDirFO);
            EarProjectGenerator.addEJBToClassPaths(ejbJarProject, appClient, webProject); // #74123
        }
        updateModuleURI(warName, carName, epp);
        NewEarProjectWizardIterator.setProjectChooserFolder(dirF);
        
        if (handle != null)
            handle.progress(NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_NewEarProjectWizardIterator_WizardProgress_PreparingToOpen"), 9);
        
        return resultSet;
    }
    
    static void setProjectChooserFolder(final File dirF) {
        File parentF = (dirF != null) ? dirF.getParentFile() : null;
        if (parentF != null && parentF.exists()) {
            ProjectChooser.setProjectsFolder(parentF);
        }
    }
    
    private static void updateModuleURI(final String warName,
            final String carName, final EarProjectProperties epp) {
        String clientModuleURI = null;
        if (warName != null) {
            // genereate application client related properties
            String[] webURIs = epp.getWebUris();
            assert webURIs.length == 1 : "Exactly one application client " +
                    "may be generated during creation. Is: " + webURIs.length; // NOI18N
            clientModuleURI = webURIs[0];
        } else if (carName != null) {
            // genereate application client related properties
            String[] appClientURIs = epp.getAppClientUris();
            assert appClientURIs.length == 1 : "Exactly one application client " +
                    "may be generated during creation. Is: " + appClientURIs.length; // NOI18N
            clientModuleURI = appClientURIs[0];
        }
        if (clientModuleURI != null) {
            epp.put(EarProjectProperties.CLIENT_MODULE_URI, clientModuleURI);
            epp.store();
        }
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
                jc.putClientProperty("WizardPanel_contentSelectedIndex", i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.PROJECT_DIR,null);
            this.wiz.putProperty(WizardProperties.NAME,null);
        }
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(
                NbBundle.getMessage(NewEarProjectWizardIterator.class, "LBL_WizardStepsCount"),
                index + 1, panels.length);
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
    
    // helper methods, finds indexJSP's FileObject
    FileObject getIndexJSPFO(FileObject webRoot, String indexJSP) {
        // XXX: ignore unvalid mainClass?
        return webRoot.getFileObject(indexJSP.replace('.', '/'), "jsp"); // NOI18N
    }
    
}
