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

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.netbeans.api.progress.ProgressHandle;

import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.web.api.webmodule.WebFrameworkSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.web.project.ui.FoldersListSettings;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick, Radko Najman
 */
public class NewWebProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(NewWebProjectWizardIterator.class.getName());
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N

    /** Create a new wizard iterator. */
    public NewWebProjectWizardIterator() {}
        
    private String[] createSteps() {
	String[] steps;
	if (WebFrameworkSupport.getFrameworkProviders().size() > 0)
	    steps = new String[] {
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP2_Frameworks") //NOI18N
	    };
	else
	    steps = new String[] {
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
	    };
	
        return steps;
    }
    
    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(4);
        handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set resultSet = new HashSet();

        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        String servInstID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dirF);
        createData.setName((String) wiz.getProperty(WizardProperties.NAME));
        createData.setServerInstanceID(servInstID);
        createData.setSourceStructure((String)wiz.getProperty(WizardProperties.SOURCE_STRUCTURE));
        if (createData.getSourceStructure() == null) {
            createData.setSourceStructure(WebProjectUtilities.SRC_STRUCT_BLUEPRINTS);
        }
        createData.setJavaEEVersion((String) wiz.getProperty(WizardProperties.J2EE_LEVEL));
        createData.setContextPath((String) wiz.getProperty(WizardProperties.CONTEXT_PATH));
        createData.setJavaPlatformName((String) wiz.getProperty(WizardProperties.JAVA_PLATFORM));
        createData.setSourceLevel((String) wiz.getProperty(WizardProperties.SOURCE_LEVEL));
        AntProjectHelper h = WebProjectUtilities.createProject(createData);
        handle.progress(2);
        
        FileObject dir = FileUtil.toFileObject(dirF);

        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        if(index != null) {
            FoldersListSettings.getDefault().setNewProjectCount(index.intValue());
        }
        wiz.putProperty(WizardProperties.NAME, null); // reset project name

        Project earProject = (Project) wiz.getProperty(WizardProperties.EAR_APPLICATION);
        
        WebProject createdWebProject = (WebProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdWebProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addWebModule(createdWebProject.getAPIWebModule());
            }
        }

        //remember last used server
        FoldersListSettings.getDefault().setLastUsedServer(servInstID);
	
        // save last project location
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
        }

        resultSet.add(dir);

        WebModule apiWebModule = createdWebProject.getAPIWebModule();
        //add framework extensions
        List selectedFrameworks = (List) wiz.getProperty(WizardProperties.FRAMEWORKS);
        if (selectedFrameworks != null){
            handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_AddingFrameworks"), 3);
            for(int i = 0; i < selectedFrameworks.size(); i++) {
                Object o = ((WebFrameworkProvider) selectedFrameworks.get(i)).extend(apiWebModule);
                if (o != null && o instanceof Set)
                    resultSet.addAll((Set)o);
            }
        }

        try {
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(apiWebModule.getDeploymentDescriptor());
            WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
            if (welcomeFiles == null) {
                LOGGER.log(Level.INFO, "no welcome file list");
                welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList");
                ddRoot.setWelcomeFileList(welcomeFiles);
            }
            if (welcomeFiles.sizeWelcomeFile() == 0) {
                LOGGER.log(Level.INFO, "welcome file list empty");
                FileObject webRoot = h.getProjectDirectory().getFileObject("web");//NOI18N
                //create default index.jsp
                FileObject indexJSPFo = createIndexJSP(webRoot);
                assert indexJSPFo != null : "webRoot: " + webRoot + ", defaultJSP: index";//NOI18N
                // Returning FileObject of main class, will be called its preferred action
                resultSet.add (indexJSPFo);
                welcomeFiles.addWelcomeFile("index.jsp"); //NOI18N
                ddRoot.write(apiWebModule.getDeploymentDescriptor());
            }
        } catch (ClassNotFoundException cnfe) {
            LOGGER.log(Level.SEVERE, cnfe.getLocalizedMessage(), cnfe);
        }
        handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_PreparingToOpen"), 4);

        // Returning set of FileObject of project diretory. 
        // Project will be open and set as main
        return resultSet;
    }
    
    private transient int index;
    private transient int panelsCount;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;

	if (WebFrameworkSupport.getFrameworkProviders().size() > 0)
	    //standard panels + configurable framework panel
	    panels = new WizardDescriptor.Panel[] {
		new PanelConfigureProject(),
		new PanelSupportedFrameworks()
	    };
	else
	    //no framework available, don't show framework panel
	    panels = new WizardDescriptor.Panel[] {
		new PanelConfigureProject(),
	    };
        panelsCount = panels.length;
        
        // Make sure list of steps is accurate.
        String[] steps = createSteps();
        for (int i = 0; i < steps.length; i++) {
            Component c = panels[i].getComponent();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent)c;
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        if (this.wiz != null) {
            this.wiz.putProperty(WizardProperties.PROJECT_DIR,null);
            this.wiz.putProperty(WizardProperties.NAME,null);
            this.wiz = null;
        }
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_WizardStepsCount"), Integer.toString(index + 1), Integer.toString(panels.length)); //NOI18N
    }
    
    public boolean hasNext() {
        return index < panelsCount - 1;
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
    private FileObject getIndexJSPFO(FileObject webRoot, String indexJSP) {
        // replace '.' with '/'
        indexJSP = indexJSP.replace ('.', '/'); // NOI18N
        
        // ignore unvalid mainClass ???
        
        return webRoot.getFileObject (indexJSP, "jsp"); // NOI18N
}
    private static FileObject createIndexJSP(FileObject webFolder) throws IOException {
        FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/JSP_Servlet/JSP.jsp" ); // NOI18N

        if (jspTemplate == null)
            return null; // Don't know the template
                
        DataObject mt = DataObject.find(jspTemplate);        
        DataFolder webDf = DataFolder.findFolder(webFolder);        
        return mt.createFromTemplate(webDf, "index").getPrimaryFile(); // NOI18N
    }

}
