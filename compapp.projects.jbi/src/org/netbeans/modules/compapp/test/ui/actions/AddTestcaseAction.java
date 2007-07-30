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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.compapp.test.ui.actions;

import java.io.BufferedOutputStream;
import java.util.List;
import javax.wsdl.extensions.soap.SOAPOperation;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.test.ui.TestNode;
import org.netbeans.modules.compapp.test.ui.PropertySpec;
import org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseConstants;
import org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseNameWizardPanel;
import org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseOperationWizardPanel;
import org.netbeans.modules.compapp.test.ui.wizards.NewTestcaseWsdlWizardPanel;
import org.netbeans.modules.compapp.test.wsdl.BindingSupport;

import java.awt.Component;
import java.awt.Dialog;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.openide.NotifyDescriptor;

import org.openide.nodes.Node;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.filesystems.FileObject;
import java.util.logging.Level;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.explorer.view.TreeView;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileUtil;
import org.openide.windows.TopComponent;

/**
 * DOCUMENT ME!
 *
 * @author Bing Lu
 * @author Jun Qian
 */
public class AddTestcaseAction extends NodeAction implements NewTestcaseConstants {
    private static final java.util.logging.Logger mLog =
            java.util.logging.Logger.getLogger("org.netbeans.modules.compapp.projects.jbi.ui.actions.AddTestcaseAction"); // NOI18N
    
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1 || activatedNodes[0].getCookie(TestCookie.class) == null) {
            return false;
        }
        return true;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    protected boolean asynchronous() {
        return false;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param activatedNodes DOCUMENT ME!
     */
    protected void performAction(Node[] activatedNodes) {
        TestCookie tc = ((TestCookie) activatedNodes[0].getCookie(TestCookie.class));
        if (tc == null) {
            return;
        }
        final TestNode testNode = tc.getTestNode();
        JbiProject project = testNode.getProject();
        FileObject testDir = testNode.getTestDir();
        
        WizardDescriptor wizardDescriptor = new WizardDescriptor(getPanels(project, testDir));
        // {0} will be replaced by WizardDesriptor.Panel.getComponent().getName()
        wizardDescriptor.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wizardDescriptor.setTitle(
                NbBundle.getMessage(AddTestcaseAction.class, "LBL_Create_new_testcase")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        if (wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION) {
            return;
        }
        mLog.info("User finished the wizard"); // NOI18N
        final String name = (String) wizardDescriptor.getProperty(TESTCASE_NAME);
        mLog.info("Got name in NewSchemaAction: " + name); // NOI18N
        BindingSupport bindingSupport = (BindingSupport)wizardDescriptor.getProperty(BINDING_SUPPORT);
        BindingOperation bindingOp = (BindingOperation)wizardDescriptor.getProperty(BINDING_OPERATION);
        Map param = new HashMap();
        param.put(BindingSupport.BUILD_OPTIONAL, Boolean.TRUE);
       
        try {            
            String inputStr = bindingSupport.buildRequest(bindingOp, param);
            
            FileObject testcaseDir = FileUtil.createFolder(testDir, name);
            String fileName = FileUtil.toFile(testcaseDir).getPath() + "/Input.xml"; // NOI18N
            BufferedOutputStream fw = new BufferedOutputStream(new FileOutputStream(fileName));
            fw.write(inputStr.getBytes("UTF-8")); // NOI18N
            fw.close();
            
            FileUtil.createData(testcaseDir, "Output.xml");  // NOI18N
            
            // Create the results directory and results/<testcasename> directory now.
            // This is to avoid a threading issue durint first run result checking.
            String resultsDirName = "results";  // NOI18N  // FIXME
            FileObject resultsFO = testDir.getFileObject(resultsDirName);
            if (resultsFO == null) {
                resultsFO = FileUtil.createFolder(testDir, resultsDirName);
            }
            FileUtil.createFolder(resultsFO, name);
            
            // Populate properties
            EditableProperties properties = new EditableProperties(true);
            properties.setProperty(PropertySpec.DESCRIPTION.getName(), "testcase " + name); // NOI18N
            
            String[] endPoints = bindingSupport.getEndpoints();
            if (endPoints == null || endPoints.length == 0) {
                properties.setProperty(PropertySpec.DESTINATION.getName(), "");  // NOI18N
                String msg = NbBundle.getMessage(AddTestcaseAction.class,
                        "MSG_NO_ENDPOINT_DEFINITION"); // NOI18N
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } else {
                properties.setProperty(PropertySpec.DESTINATION.getName(), endPoints[0]);  // NOI18N
                String[] comment = new String[endPoints.length - 1];
                for (int i = 1; i < endPoints.length; i++) {
                    comment[i-1] = "#" + PropertySpec.DESTINATION.getName() + "=" + endPoints[i]; // NOI18N
                }
                properties.setComment(PropertySpec.DESTINATION.getName(), comment, false);
            }
            
            String soapActionURI = ""; // NOI18N
            List list = bindingOp.getExtensibilityElements();
            for (Object elem : list) {
                if (elem instanceof SOAPOperation) {
                    SOAPOperation soapOperation = (SOAPOperation) elem;
                    String uri = soapOperation.getSoapActionURI();
                    if (uri != null) {
                        soapActionURI = uri;
                        break;
                    }
                }
            }
            
            properties.setProperty(PropertySpec.SOAP_ACTION.getName(), soapActionURI);  
            properties.setProperty(PropertySpec.INPUT_FILE.getName(), "Input.xml");  // NOI18N
            properties.setProperty(PropertySpec.OUTPUT_FILE.getName(), "Output.xml");  // NOI18N
            properties.setProperty(PropertySpec.CONCURRENT_THREADS.getName(),
                    PropertySpec.CONCURRENT_THREADS.getDefaultValue().toString());
            properties.setProperty(PropertySpec.INVOKES_PER_THREAD.getName(),
                    PropertySpec.INVOKES_PER_THREAD.getDefaultValue().toString());
            properties.setProperty(PropertySpec.TEST_TIMEOUT.getName(),
                    PropertySpec.TEST_TIMEOUT.getDefaultValue().toString());
            properties.setProperty(PropertySpec.CALCULATE_THROUGHPUT.getName(),
                    PropertySpec.CALCULATE_THROUGHPUT.getDefaultValue().toString());
            
            properties.setProperty(PropertySpec.COMPARISON_TYPE.getName(),
                    PropertySpec.COMPARISON_TYPE.getDefaultValue().toString());  // NOI18N
            properties.setComment(PropertySpec.COMPARISON_TYPE.getName(),
                    new String[]{"#" + PropertySpec.COMPARISON_TYPE.getName() + "'s possible values: identical|binary|equals"}, false);  // NOI18N
            
            properties.setProperty(PropertySpec.FEATURE_STATUS.getName(),
                    PropertySpec.FEATURE_STATUS.getDefaultValue().toString()); 
            properties.setComment(PropertySpec.FEATURE_STATUS.getName(),
                    new String[]{"#" + PropertySpec.FEATURE_STATUS.getName() + "'s possible values: progress|done"}, false);  // NOI18N
            
            //Write properties to disk
            //FileObject propFile = FileUtil.createData(testcaseDir, "Invoke.properties");  // NOI18N
            //FileObject propFile = FileUtil.createData(testcaseDir, "Concurrent.properties");  // NOI18N
            //ByteArrayOutputStream baos = new ByteArrayOutputStream();
            //properties.store(baos);
            //final byte[] data = baos.toByteArray();
            //lock = propFile.lock();
            //FileUtil.copy(new ByteArrayInputStream(data),  // NOI18N
            //              propFile.getOutputStream(lock));
            //lock.releaseLock();
            fileName = FileUtil.toFile(testcaseDir).getPath() + "/Concurrent.properties";   // NOI18N
            FileOutputStream outStream = new FileOutputStream(fileName);
            properties.store(outStream);
            outStream.close();
            
            // Since testcaseDir is created before *.properties,
            // FileChangeListener may be notified before *.properties,
            // hence FileChangeListener may fail to find that the new folder contains *.properties,
            // hence this second notificatioin is necessary.
            testNode.getFileChangeListener().fileFolderCreated(new FileEvent(testDir));
            
            
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
            }
            
            final  Node activatedNode = activatedNodes[0];
            
            SwingUtilities.invokeLater(new Runnable() {
                
                public void run() {
                    // Automatically expand the Project tree to expose the new input node
                    TopComponent topComponent = TopComponent.getRegistry().getActivated();
                    
                    if (topComponent.getComponent(0) instanceof TreeView) {
                        
                        TreeView treeView = (TreeView) topComponent.getComponent(0);
                        
                        // Expand the test node
                        if (!treeView.isExpanded(activatedNode)) {
                            treeView.expandNode(activatedNode);
                        }
                        
                        Node testChildNode = activatedNode.getChildren().findChild(name);
                        
                        if (testChildNode != null) {
                            // Expand the test case node
                            treeView.expandNode(testChildNode);
                            
                            // Look for TestCaseChild named Input
                            Node testCaseChildNode = testChildNode.getChildren().findChild("Input"); // NOI18N
                            
                            if (testCaseChildNode != null) {
                                //Select the input node and open it
                                EditCookie openCookie = (EditCookie) testCaseChildNode.getCookie(EditCookie.class);
                                if (openCookie != null) {
                                    openCookie.edit();
                                }
                            }
                        }
                    }
                }});
                
        } catch (Exception e) {
            String msg = NbBundle.getMessage(AddTestcaseAction.class, 
                    "MSG_Failed_to_Add_Testcase", e.getMessage()); // NOI18N
//            mLog.log(Level.SEVERE, msg, e);
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getName() {
        return NbBundle.getMessage(AddTestcaseAction.class, "LBL_AddTestcaseAction_Name");  // NOI18N
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        
        // If you will provide context help then use:
        // return new HelpCtx(AddTestcaseAction.class);
    }
    
    /**
     * Initialize panels representing individual wizard's steps and sets
     * various properties for them influencing wizard appearance.
     */
    private WizardDescriptor.Panel[] getPanels(Project project, FileObject testDir) {
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[] {
            new NewTestcaseNameWizardPanel(testDir),
            new NewTestcaseWsdlWizardPanel(project),
            new NewTestcaseOperationWizardPanel()
        };
        String[] steps = new String[panels.length];
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            // Default step name to component name of panel. Mainly useful
            // for getting the name of the target chooser to appear in the
            // list of steps.
            steps[i] = c.getName();
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Sets step number of a component
                jc.putClientProperty("WizardPanel_contentSelectedIndex", new Integer(i)); // NOI18N
                // Sets steps names for a panel
                jc.putClientProperty("WizardPanel_contentData", steps); // NOI18N
                // Turn on subtitle creation on each step
                jc.putClientProperty("WizardPanel_autoWizardStyle", Boolean.TRUE); // NOI18N
                // Show steps on the left side with the image on the background
                jc.putClientProperty("WizardPanel_contentDisplayed", Boolean.TRUE); // NOI18N
                // Turn on numbering of all steps
                jc.putClientProperty("WizardPanel_contentNumbered", Boolean.TRUE); // NOI18N
            }
        }
        return panels;
    }
}