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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import org.netbeans.modules.j2ee.common.FileSearchUtility;
import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;

import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.common.project.ui.PanelSharability;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.ProjectWebModule;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.j2ee.common.project.ui.UserProjectSettings;

/**
 * Wizard to create a new Web project for an existing web module.
 * @author Pavel Buzek, Radko Najman
 */
public class ImportWebProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {
    
    private static final long serialVersionUID = 1L;
    private String buildfileName = GeneratedFilesHelper.BUILD_XML_PATH;

    private String moduleLoc;
    
    /** Create a new wizard iterator. */
    public ImportWebProjectWizardIterator () {}
    
    private WizardDescriptor.Panel[] createPanels() {
        return new WizardDescriptor.Panel[] {
            new ImportWebProjectWizardIterator.ThePanel(),
            new PanelSharability(WizardProperties.PROJECT_DIR, WizardProperties.SERVER_INSTANCE_ID, false),
            new PanelSourceFolders.Panel()
        };
    }
    
    private String[] createSteps() {
        return new String[] {
            NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_IW_Step1"), //NOI18N
            NbBundle.getMessage(ImportWebProjectWizardIterator.class, "PanelShareabilityVisual.label"),            
            NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_IW_Step2") //NOI18N
        };
    }
    
    public Set instantiate() throws IOException {
        assert false : "This method cannot be called if the class implements WizardDescriptor.ProgressInstantiatingIterator.";
        return null;
    }
        
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        handle.start(3);
        handle.progress(NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_CreatingProject"), 1);
        
        Set resultSet = new HashSet();
        
        File dirF = (File) wiz.getProperty(WizardProperties.PROJECT_DIR);
        if (dirF != null) {
            dirF = FileUtil.normalizeFile(dirF);
        }
        
        File dirSrcF = (File) wiz.getProperty (WizardProperties.SOURCE_ROOT);
        
        String name = (String) wiz.getProperty(WizardProperties.NAME);
        String contextPath = (String) wiz.getProperty(WizardProperties.CONTEXT_PATH);
        String docBaseName = (String) wiz.getProperty(WizardProperties.DOC_BASE);
        File[] sourceFolders = (File[]) wiz.getProperty(WizardProperties.JAVA_ROOT);
        File[] testFolders = (File[]) wiz.getProperty(WizardProperties.TEST_ROOT);
        String libName = (String) wiz.getProperty(WizardProperties.LIB_FOLDER);
        String serverInstanceID = (String) wiz.getProperty(WizardProperties.SERVER_INSTANCE_ID);
        String j2eeLevel = (String) wiz.getProperty(WizardProperties.J2EE_LEVEL);
        String webInfFolder = (String) wiz.getProperty(WizardProperties.WEBINF_FOLDER);
        
        FileObject wmFO = FileUtil.toFileObject (dirSrcF);
        assert wmFO != null : "No such dir on disk: " + dirSrcF;
        assert wmFO.isFolder() : "Not really a dir: " + dirSrcF;
        
        FileObject docBase;
        FileObject webInf;
        FileObject libFolder;
        if (docBaseName == null || docBaseName.equals("")) //NOI18N
            docBase = FileSearchUtility.guessDocBase(wmFO);
        else {
            File f = new File(docBaseName);
            docBase = FileUtil.toFileObject(f);
        }
        
        if (webInfFolder == null || webInfFolder.equals("")) //NOI18N
            webInf = FileSearchUtility.guessWebInf(wmFO);
        else {
            File f = new File(webInfFolder);
            webInf = FileUtil.toFileObject(f);
        }

        if (libName == null || libName.equals("")) //NOI18N
            libFolder = FileSearchUtility.guessLibrariesFolder(wmFO);
        else {
            File f = new File(libName);
            libFolder = FileUtil.toFileObject(f);
        }

        if(j2eeLevel == null) {
            j2eeLevel = WebModule.J2EE_14_LEVEL;
        }

        String buildfile = getBuildfile();
        
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dirF);
        createData.setName(name);
        createData.setWebModuleFO(wmFO);
        createData.setSourceFolders(sourceFolders);
        createData.setTestFolders(testFolders);
        createData.setDocBase(docBase);
        createData.setLibFolder(libFolder);
        createData.setJavaEEVersion(j2eeLevel);
        createData.setServerInstanceID(serverInstanceID);
        createData.setBuildfile(buildfile);
        createData.setJavaPlatformName((String) wiz.getProperty(WizardProperties.JAVA_PLATFORM));
        createData.setSourceLevel((String) wiz.getProperty(WizardProperties.SOURCE_LEVEL));       
        createData.setWebInfFolder(webInf);
        
        createData.setLibrariesDefinition(SharabilityUtility.getLibraryLocation((String)wiz.getProperty(PanelSharability.WIZARD_SHARED_LIBRARIES)));
        createData.setServerLibraryName((String) wiz.getProperty(PanelSharability.WIZARD_SERVER_LIBRARY));
        
        WebProjectUtilities.importProject(createData);       
        handle.progress(2);
        
        FileObject dir = FileUtil.toFileObject(dirF);
        
        resultSet.add(dir);
        
        Project earProject = (Project) wiz.getProperty(WizardProperties.EAR_APPLICATION);
        WebProject createdWebProject = (WebProject) ProjectManager.getDefault().findProject(dir);
        if (earProject != null && createdWebProject != null) {
            Ear ear = Ear.getEar(earProject.getProjectDirectory());
            if (ear != null) {
                ear.addWebModule(createdWebProject.getAPIWebModule());
            }
        }

        Project p = ProjectManager.getDefault().findProject(dir);        
        ProjectWebModule wm = (ProjectWebModule) p.getLookup ().lookup (ProjectWebModule.class);
        if (wm != null) //should not be null
            wm.setContextPath(contextPath);

        Integer index = (Integer) wiz.getProperty(NewWebProjectWizardIterator.PROP_NAME_INDEX);
        if (index != null) {
            UserProjectSettings.getDefault().setNewProjectCount(index.intValue());
        }
        wiz.putProperty(WizardProperties.NAME, null); // reset project name

        //remember last used server
        UserProjectSettings.getDefault().setLastUsedServer(serverInstanceID);

        handle.progress(NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_NewWebProjectWizardIterator_WizardProgress_PreparingToOpen"), 3);
        
        return resultSet;
    }
    
    private String getBuildfile() {
        return buildfileName;
    }
    
    private void setBuildfile(String name) {
        buildfileName = name;
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
                jc.putClientProperty("NewProjectWizard_Title", NbBundle.getMessage(ImportWebProjectWizardIterator.class, "TXT_WebExtSources")); // NOI18N
                // Step #.
                jc.putClientProperty("WizardPanel_contentSelectedIndex", Integer.valueOf(i)); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
            }
        }
    }
    public void uninitialize(WizardDescriptor wiz) {
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return MessageFormat.format(NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_WizardStepsCount"), Integer.toString(index + 1), Integer.toString(panels.length)); //NOI18N
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

    public final class ThePanel implements WizardDescriptor.ValidatingPanel, WizardDescriptor.FinishablePanel {

        private ImportLocationVisual panel;
        private WizardDescriptor wizardDescriptor;
        
        private ThePanel () {
        }
        
        public java.awt.Component getComponent () {
            if (panel == null) {
                panel = new ImportLocationVisual (this);
            }
            return panel;
        }
        
        public HelpCtx getHelp() {
            return new HelpCtx(ThePanel.class);
        }

        public boolean isValid () {
            getComponent();
            return panel.valid(wizardDescriptor);
        }
        
        private final ChangeSupport changeSupport = new ChangeSupport(this);
        public void addChangeListener(ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        public void removeChangeListener(ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }
        protected void fireChangeEvent() {
            changeSupport.fireChange();
        }
        public void readSettings (Object settings) {
            wizardDescriptor = (WizardDescriptor) settings;        
            panel.read(wizardDescriptor);

            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = ((JComponent) panel).getClientProperty("NewProjectWizard_Title"); //NOI18N
            if (substitute != null)
                wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); //NOI18N
        }
        
        public void storeSettings (Object settings) {
            WizardDescriptor d = (WizardDescriptor) settings;
            panel.store(d);
            ((WizardDescriptor) d).putProperty ("NewProjectWizard_Title", null); //NOI18N
            
            moduleLoc = panel.moduleLocationTextField.getText().trim();
        }

        public void validate() throws WizardValidationException {
            File dirF = new File(panel.projectLocationTextField.getText());
            if (!new File(dirF, GeneratedFilesHelper.BUILD_XML_PATH).exists()) {
                setBuildfile(GeneratedFilesHelper.BUILD_XML_PATH);
            } else {
                File buildFile = new File(dirF, getBuildfile());
                if (buildFile.exists()) {
                    JButton ok = createButton(
                            "LBL_IW_Buildfile_OK", "ACS_IW_BuildFileDialog_OKButton_LabelMnemonic", //NOI18N
                            "LBL_IW_BuildFileDialog_OK_LabelMnemonic"); //NOI18N
                    JButton cancel = createButton(
                            "LBL_IW_Buildfile_Cancel", "ACS_IW_BuildFileDialog_CancelButton_LabelMnemonic", //NOI18N
                            "LBL_IW_BuildFileDialog_Cancel_LabelMnemonic"); //NOI18N
                    final ImportBuildfile ibf = new ImportBuildfile(buildFile, ok);
                    DialogDescriptor descriptor = new DialogDescriptor(ibf,
                            NbBundle.getMessage(ImportWebProjectWizardIterator.class, "LBL_IW_BuildfileTitle"), //NOI18N
                            true, new Object[]{ok, cancel}, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                            null, null);
                    Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                    dialog.setVisible(true);
                    if (descriptor.getValue() != ok) {
                        throw new WizardValidationException(panel.projectLocationTextField, "", "");
                    }
                    setBuildfile(ibf.getBuildName());
                }
            }
        }

        private JButton createButton(String labelId, String labelMnemonicId, String mnemonicId) {
            JButton button = new JButton(NbBundle.getMessage(ImportWebProjectWizardIterator.class, labelId));
            button.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(ImportWebProjectWizardIterator.class, labelMnemonicId));
            button.setMnemonic(NbBundle.getMessage(ImportWebProjectWizardIterator.class, mnemonicId).charAt(0));
            return button;
        }

        public boolean isFinishPanel() {
            return false;
        }

    }
    
}
