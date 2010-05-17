/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.bridge.wizard;

import java.awt.Component;
import java.awt.Dialog;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.soa.dndbridge.api.VariableType;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.WLMModel;
import org.netbeans.modules.wlm.model.api.WLMModelFactory;
import org.netbeans.modules.workflow.project.WorkflowproProjectGenerator;
import org.netbeans.modules.worklist.editor.spi.CatalogSupportProvider;
import org.netbeans.modules.worklist.editor.spi.TaskDefinitionProvider;
import org.netbeans.modules.worklist.editor.spi.WSDLProvider;
import org.netbeans.modules.worklist.editor.spi.WizardProvider;
import org.netbeans.modules.worklist.editor.spi.impl.WLMCatalogSupportProvider;
import org.netbeans.modules.worklist.editor.spi.impl.WLMTaskDefinitionProvider;
import org.netbeans.modules.worklist.editor.spi.impl.WLMWSDLProvider;
import org.netbeans.modules.worklist.editor.spi.impl.WLMWizardProvider;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public final class WLMWizardAction {

    //private WizardDescriptor.Panel[] panels;
    private ArrayList<WizardDescriptor.Panel> panels;
    FileObject sourceLocation;
    List<VariableType> messages;
    private String namespaceBase;

    public WLMWizardAction(FileObject sourceLocation, 
            List<VariableType> messages, 
            String namespaceBase) 
    {
        this.sourceLocation = sourceLocation;
        this.messages = messages;
        this.namespaceBase = namespaceBase;
    }

    public WSDLModel performAction() {
        WizardDescriptor.Panel[] type = new WizardDescriptor.Panel[]{};
        WizardDescriptor.Panel[] panels2 = getPanels().toArray(type);
        WizardDescriptor wizardDescriptor = new WizardDescriptor(panels2);
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}"));
        wizardDescriptor.setTitle("Human Task List Wizard");
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        boolean cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        WSDLModel taskServiceWsdlModel = null;
        if (!cancelled) {
            String projectLocation = ((String) wizardDescriptor.getProperty(WLMVisualPanel1.PROJECT_LOCATION)).trim();
            String projectName = ((String) wizardDescriptor.getProperty(WLMVisualPanel1.DEFAULT_PROJECT_NAME)).trim();
            String j2eeLevel = (String) wizardDescriptor.getProperty(WLMVisualPanel1.J2EE_LEVEL);

            String wlmProjectLocation = projectLocation + "//" + projectName;

            File dirF = new File(wlmProjectLocation);
            try {
                AntProjectHelper helper = WorkflowproProjectGenerator.createProject(dirF, projectName, j2eeLevel);
                Project wlmProject = ProjectManager.getDefault().findProject(helper.getProjectDirectory());

                String taskName = (String) wizardDescriptor.getProperty(WLMVisualPanel1.DEFAULT_TASK_NAME);

                String wsdlName = taskName;
                taskServiceWsdlModel = createWSDLFile(wizardDescriptor, wsdlName);
// BACKUP:
//                String wsdlFilePath = sourceLocation.getPath() + "/src/" + wsdlName + WSDLProvider.WSDL_SUFFIX + "." + WSDLProvider.WSDL_EXT;
                String wsdlFilePath = sourceLocation.getPath() + "/src/services/" + wsdlName + "/" + wsdlName + WSDLProvider.WSDL_SUFFIX + "." + WSDLProvider.WSDL_EXT;

                WLMModel wlmModel = createWFFile(wizardDescriptor, taskName, wlmProjectLocation);
                createCatalogReference(wlmProject, wsdlFilePath);
                createWSDLImport(wlmModel, taskServiceWsdlModel);

                Project[] projects = {wlmProject};

                try {
                    ProjectManager.getDefault().saveProject(wlmProject);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }

                OpenProjects.getDefault().open(projects, false);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return taskServiceWsdlModel;
    }

    private WSDLModel createWSDLFile(WizardDescriptor wizardDescriptor, String wsdlName) {
        Message inputMessageType = (Message) wizardDescriptor.getProperty(WLMVisualPanel2.INPUT_MESSAGE_TYPE);
        Message outputMessageType = (Message) wizardDescriptor.getProperty(WLMVisualPanel2.OUTPUT_MESSAGE_TYPE);
        Message faultMessageType = (Message) wizardDescriptor.getProperty(WLMVisualPanel2.FAULT_MESSAGE_TYPE);

        String sourceProjectFolder = sourceLocation.getPath();
// BACKUP        
//        WSDLModel wsdlModel = new WLMWSDLProvider().generateWSDL(wsdlName, 
//                sourceProjectFolder + "/src", namespaceBase,  
//                inputMessageType, outputMessageType, faultMessageType);
        WSDLModel wsdlModel = new WLMWSDLProvider().generateWSDL(wsdlName, 
                sourceProjectFolder + "/src/services/" + wsdlName, namespaceBase,  
                inputMessageType, outputMessageType, faultMessageType);
        return wsdlModel;

    }

    private WLMModel createWFFile(WizardDescriptor wizardDescriptor, String taskName, String newWLMProjectLocation) {
        TaskDefinitionProvider definitionProvider = new WLMTaskDefinitionProvider();
        WLMModel wlmModel = definitionProvider.createWLMTaskDefinitionFile(taskName, newWLMProjectLocation + "/src");
        TTask task = null;
        if (wlmModel.startTransaction()) {
            try {
                task = getWFTask(wizardDescriptor, wlmModel);
            } finally {
                wlmModel.endTransaction();
            }
        }
        definitionProvider.configureWFTask(wlmModel, task);
        return wlmModel;
    }

    private TTask getWFTask(WizardDescriptor wizardDescriptor, WLMModel wlmModel) {
        String taskName = (String) wizardDescriptor.getProperty(WLMVisualPanel1.DEFAULT_TASK_NAME);
        TTask task = wlmModel.getTask();
        task.setName(taskName);
        return task;
    }
    private void createCatalogReference(Project wlmProject, String wsdlName) {
        CatalogSupportProvider catalogSupport = new WLMCatalogSupportProvider();
        catalogSupport.createCatalogReference(wlmProject, wsdlName);
    }

    private void createWSDLImport(WLMModel wlmModel, WSDLModel wsdlModel) {
        TaskDefinitionProvider definitionProvider = new WLMTaskDefinitionProvider();
        definitionProvider.createWSDLImport(wlmModel, wsdlModel);
    }

    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private ArrayList<WizardDescriptor.Panel> getPanels() {

        if (panels == null) {
            panels = new ArrayList();
            panels.add(new WLMWizardPanel1(sourceLocation));
            panels.add(new WLMWizardPanel2(messages));

            WizardProvider wlmWizardProvider = new WLMWizardProvider();
            ArrayList<WizardDescriptor.Panel> wlmEditorPanels = wlmWizardProvider.getPanels();
            panels.addAll(wlmEditorPanels);

            String[] steps = new String[panels.size()];
            for (int i = 0; i < panels.size(); i++) {
                Component c = panels.get(i).getComponent();
                // Default step name to component name of panel. Mainly useful
                // for getting the name of the target chooser to appear in the
                // list of steps.
                steps[i] = c.getName();
                if (c instanceof JComponent) { // assume Swing components

                    JComponent jc = (JComponent) c;
                    // Sets step number of a component
                    jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i));
                    // Sets steps names for a panel
                    jc.putClientProperty("WizardPanel_contentData", steps);
                    // Turn on subtitle creation on each step
                    jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE);
                    // Show steps on the left side with the image on the background
                    jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE);
                    // Turn on numbering of all steps
                    jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE);
                }
            }
        }
        return panels;
    }

    public String getName() {
        return "Start Sample Wizard";
    }

//    @Override
//    public String iconResource() {
//        return null;
//    }
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }//    @Override
//    protected boolean asynchronous() {
//        return false;
//    }
}
