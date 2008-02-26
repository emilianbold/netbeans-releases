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
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;

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
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.project.ui.PanelSharability;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;

/**
 * Wizard to create a new Web project.
 * @author Jesse Glick, Radko Najman
 */
public class NewWebProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    
    static final String PROP_NAME_INDEX = "nameIndex"; //NOI18N
    static final String UI_LOGGER_NAME = "org.netbeans.ui.web.project"; //NOI18N

    /** Create a new wizard iterator. */
    public NewWebProjectWizardIterator() {}
        
    private String[] createSteps() {
	String[] steps;
	if (WebFrameworks.getFrameworks().size() > 0)
	    steps = new String[] {
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
                NbBundle.getMessage(NewWebProjectWizardIterator.class, "PanelShareabilityVisual.label"),
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP2_Frameworks") //NOI18N
	    };
	else
	    steps = new String[] {
		NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NWP1_ProjectTitleName"), //NOI18N
                NbBundle.getMessage(NewWebProjectWizardIterator.class, "PanelShareabilityVisual.label")
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
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }

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
        
        createData.setLibrariesDefinition(
                SharabilityUtility.getLibraryLocation((String) wiz.getProperty(PanelSharability.WIZARD_SHARED_LIBRARIES)));
        createData.setServerLibraryName((String) wiz.getProperty(PanelSharability.WIZARD_SERVER_LIBRARY));
        
        AntProjectHelper h = WebProjectUtilities.createProject(createData);
        handle.progress(2);
        
        FileObject dir = FileUtil.toFileObject(dirF);

        Integer index = (Integer) wiz.getProperty(PROP_NAME_INDEX);
        if(index != null) {
            UserProjectSettings.getDefault().setNewProjectCount(index.intValue());
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
        UserProjectSettings.getDefault().setLastUsedServer(servInstID);
        SharableLibrariesUtils.setLastProjectSharable(createData.getLibrariesDefinition() != null);
	
        // save last project location
        dirF = (dirF != null) ? dirF.getParentFile() : null;
        if (dirF != null && dirF.exists()) {
            ProjectChooser.setProjectsFolder (dirF);
        }

        resultSet.add(dir);

        WebModule apiWebModule = createdWebProject.getAPIWebModule();
        //add framework extensions
        List selectedExtenders = (List) wiz.getProperty(WizardProperties.EXTENDERS);
        if (selectedExtenders != null){
            handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_AddingFrameworks"), 3);
            for(int i = 0; i < selectedExtenders.size(); i++) {
                Object o = ((WebModuleExtender) selectedExtenders.get(i)).extend(apiWebModule);
                if (o != null && o instanceof Set)
                    resultSet.addAll((Set)o);
            }
        }

        FileObject webRoot = h.getProjectDirectory().getFileObject("web");//NOI18N
        resultSet.addAll(WebProjectUtilities.ensureWelcomePage(webRoot, apiWebModule.getDeploymentDescriptor()));
        
        handle.progress(NbBundle.getMessage(NewWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_PreparingToOpen"), 4);

        LogRecord logRecord = new LogRecord(Level.INFO, "UI_WEB_PROJECT_CREATE");  //NOI18N
        logRecord.setLoggerName(UI_LOGGER_NAME);                   //NOI18N
        logRecord.setResourceBundle(NbBundle.getBundle(NewWebProjectWizardIterator.class));
        
        List <String> selectedFrameworkNames = (List<String>) wiz.getProperty(WizardProperties.FRAMEWORK_NAMES);
        int frameworkCount = (selectedFrameworkNames != null) ? selectedFrameworkNames.size() : 0;
        Object[] parameters = new Object [5 + frameworkCount];
        parameters[0] = Deployment.getDefault().getServerID( createData.getServerInstanceID());
        parameters[1] = createData.getServerInstanceID();
        parameters[2] = createData.getJavaEEVersion();
        parameters[3] = createData.getSourceLevel();
        parameters[4] = createData.getSourceStructure();
        if (selectedFrameworkNames != null) {
            for (int i = 0; i < selectedFrameworkNames.size(); i++) {
                parameters[5 + i] = selectedFrameworkNames.get(i);
            }
        }
        
        logRecord.setParameters(parameters);
        Logger.getLogger(UI_LOGGER_NAME).log(logRecord);
        
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

	if (WebFrameworks.getFrameworks().size() > 0)
	    //standard panels + configurable framework panel
	    panels = new WizardDescriptor.Panel[] {
		new PanelConfigureProject(),
                new PanelSharability(WizardProperties.PROJECT_DIR, WizardProperties.SERVER_INSTANCE_ID, true),
		new PanelSupportedFrameworks()
	    };
	else
	    //no framework available, don't show framework panel
	    panels = new WizardDescriptor.Panel[] {
		new PanelConfigureProject(),
                new PanelSharability(WizardProperties.PROJECT_DIR, WizardProperties.SERVER_INSTANCE_ID, true)
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
}
