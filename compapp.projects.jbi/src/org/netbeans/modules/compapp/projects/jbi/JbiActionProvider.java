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

package org.netbeans.modules.compapp.projects.jbi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.NoSelectedServerWarning;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.OpenEditorAction;
import org.netbeans.modules.compapp.jbiserver.JbiManager;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ReferenceHelper;

import org.openide.*;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;

import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.util.Lookup;
import org.openide.util.NbBundle;

import java.io.IOException;
import java.util.Enumeration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.awt.Dialog;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBuildListener;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBuildTask;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectHelper;
import org.netbeans.modules.compapp.projects.jbi.api.ProjectValidator;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;
import org.openide.util.Task;
import org.openide.util.TaskListener;


/**
 * Action provider of the Web project. This is the place where to do strange things to Web actions.
 * E.g. compile-single.
 */
public class JbiActionProvider implements ActionProvider {
    // Definition of commands
    private static final String[] supportedActions = {
        COMMAND_CLEAN, JbiProjectConstants.COMMAND_UNDEPLOY,
        JbiProjectConstants.COMMAND_REDEPLOY, JbiProjectConstants.COMMAND_DEPLOY,
        JbiProjectConstants.COMMAND_JBIBUILD, JbiProjectConstants.COMMAND_JBICLEANBUILD,
        JbiProjectConstants.COMMAND_JBICLEANCONFIG,
        JbiProjectConstants.COMMAND_VALIDATEPORTMAPS,
        // Start Test Framework
        JbiProjectConstants.COMMAND_TEST,
        // End Test Framework
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_DEBUG
    };
    
    /**
     * DOCUMENT ME!
     */
    JbiProject project;
    
    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;
    private ReferenceHelper refHelper;
    
    /** Map from commands to ant targets */
    Map /*<String,String[]>*/ commands;
    
    /**
     * Creates a new JbiActionProvider object.
     *
     * @param project DOCUMENT ME!
     * @param antProjectHelper DOCUMENT ME!
     * @param refHelper DOCUMENT ME!
     */
    public JbiActionProvider(
            JbiProject project, AntProjectHelper antProjectHelper, ReferenceHelper refHelper
            ) {
        commands = new HashMap();
        commands.put(COMMAND_CLEAN, new String[] {"clean"}); // NOI18N
        commands.put(JbiProjectConstants.COMMAND_REDEPLOY, new String[] {"run"}); // NOI18N
        commands.put(JbiProjectConstants.COMMAND_DEPLOY, new String[] {"run"}); // NOI18N
        commands.put(JbiProjectConstants.COMMAND_UNDEPLOY, new String[] {"undeploy"}); // NOI18N
        commands.put(JbiProjectConstants.COMMAND_JBIBUILD, new String[] {"jbi-build"}); // NOI18N
        commands.put(JbiProjectConstants.COMMAND_JBICLEANCONFIG, new String[] {"jbi-clean-config"}); // NOI18N
        commands.put(JbiProjectConstants.COMMAND_JBICLEANBUILD, new String[] {"jbi-clean-build"}); // NOI18N
        commands.put(JbiProjectConstants.COMMAND_VALIDATEPORTMAPS, new String[] {"validate-portmaps"}); // NOI18N
        // Start Test Framework
        commands.put(JbiProjectConstants.COMMAND_TEST, new String[] {"test"}); // NOI18N
        // End Test Framework
        
        // map common project action to jbi ones
        commands.put(COMMAND_BUILD, new String[] {"jbi-build"}); // NOI18N
        commands.put(COMMAND_REBUILD, new String[] {"jbi-clean-build"}); // NOI18N
        commands.put(COMMAND_DEBUG, new String[] {"debug"}); // NOI18N
        // end map common project action to jbi ones
        
        this.antProjectHelper = antProjectHelper;
        this.project = project;
        this.refHelper = refHelper;
    }
    
    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/ String[] getTargetNames(String command, Lookup context, Properties p) throws IllegalArgumentException {
        String[] targetNames = (String[])commands.get(command);
        return targetNames;
    }
    
    private FileObject findBuildXml() {
        return project.getProjectDirectory().getFileObject(project.getBuildXmlName());
    }
    
    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String[] getSupportedActions() {
        return supportedActions;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param command DOCUMENT ME!
     * @param context DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public void invokeAction(String command, Lookup context)
    throws IllegalArgumentException {
        
        if (COMMAND_DELETE.equals(command)) {
            DefaultProjectOperations.performDefaultDeleteOperation(project);
            return ;
        }
        
        if (COMMAND_COPY.equals(command)) {
            DefaultProjectOperations.performDefaultCopyOperation(project);
            return ;
        }
        
        if (COMMAND_MOVE.equals(command)) {
            DefaultProjectOperations.performDefaultMoveOperation(project);
            return ;
        }
        
        if (COMMAND_RENAME.equals(command)) {
            DefaultProjectOperations.performDefaultRenameOperation(project, null);
            return ;
        }
        
        Properties p = null;
        String[] targetNames = (String[]) commands.get(command);
        
        if(command.equals(JbiProjectConstants.COMMAND_TEST)) {
            setupTests();
        }
        
        if (command.equals(JbiProjectConstants.COMMAND_DEPLOY) ||
                command.equals(JbiProjectConstants.COMMAND_REDEPLOY) ||
                command.equals(JbiProjectConstants.COMMAND_UNDEPLOY) ||
                command.equals(COMMAND_DEBUG) ||
                command.equals(JbiProjectConstants.COMMAND_TEST)) {
            
            /*if (isProjectEmpty()) {
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(
                        NbBundle.getMessage(
                        JbiActionProvider.class, "MSG_EmptyJbiProjectError" // NOI18N
                        ),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return;
            }*/
            
            if (!isSelectedServer()) {
                return;
            }
            if (!validateSubProjects()) {
                return;
            }
        } else if (command.equals(JbiProjectConstants.COMMAND_JBICLEANCONFIG) ||
                command.equals(JbiProjectConstants.COMMAND_JBIBUILD) ||
                command.equals(JbiProjectConstants.COMMAND_JBICLEANBUILD)) {
            
            if (command.equals(JbiProjectConstants.COMMAND_JBICLEANCONFIG)) {
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(JbiActionProvider.class, "MSG_CleanConfig"), // NOI18N
                        NbBundle.getMessage(JbiActionProvider.class, "TTL_CleanConfig"), // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                    return;
                }
            }
            
            saveCasaChanges(project);
            
            if (!validateSubProjects()) {
                return;
            }
            
        } else {
            p = null;
            
            if (targetNames == null) {
                throw new IllegalArgumentException(command);
            }
        }
        
        final JbiBuildListener jbiBuildListener = getBuildListener(command);

        try {
            final ExecutorTask executorTask = 
                    ActionUtils.runTarget(findBuildXml(), targetNames, p);
            
            if (jbiBuildListener != null) {                
                JbiBuildTask buildTask = new JbiBuildTask() {
                    public boolean isFinished() {
                        return executorTask.isFinished();
                    }
                    public int getResult() {
                        return executorTask.result();
                    }
                };
                jbiBuildListener.buildStarted(buildTask);
                
                executorTask.addTaskListener(new TaskListener() {
                    public void taskFinished(Task task) {
                        jbiBuildListener.buildCompleted(executorTask.result() == 0);
                    }
                });                
            }
            
            if (command.equals(JbiProjectConstants.COMMAND_DEPLOY) ||
                    command.equals(JbiProjectConstants.COMMAND_JBICLEANCONFIG) ||
                    command.equals(JbiProjectConstants.COMMAND_JBIBUILD) ||
                    command.equals(JbiProjectConstants.COMMAND_JBICLEANBUILD)) {
                executorTask.addTaskListener(new TaskListener() {
                    public void taskFinished(Task task) {
                        CasaHelper.registerCasaFileListener(project);
                    }
                });
            }

        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    private JbiBuildListener getBuildListener(String command) {
        if (command.equals(JbiProjectConstants.COMMAND_DEPLOY) ||
                command.equals(JbiProjectConstants.COMMAND_JBICLEANCONFIG) ||
                command.equals(JbiProjectConstants.COMMAND_JBIBUILD) ||
                command.equals(JbiProjectConstants.COMMAND_JBICLEANBUILD)) {
            FileObject casaFO = CasaHelper.getCasaFileObject(project, false);
            if (casaFO != null) {
                try {
                    DataObject casaDO = DataObject.find(casaFO);
                    if (casaDO != null) {
                        return (JbiBuildListener) casaDO.getLookup().lookup(JbiBuildListener.class);
                    }
                } catch (DataObjectNotFoundException e) {
                    ; // ignore the error
                }
            }
        }
        return null;
    }
    
    private boolean validateSubProjects(){
        try {
            JbiProjectProperties properties = project.getProjectProperties();
            List<VisualClassPathItem> itemList =
                    (List) properties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);
            
            List<Project> subProjects = new ArrayList<Project>();
            Map<Class, Project> subProjectTypeMap = new HashMap<Class, Project>();
            for (VisualClassPathItem item : itemList) {
                Project subProject = item.getAntArtifact().getProject();
                subProjects.add(subProject);                
                subProjectTypeMap.put(subProject.getClass(), subProject);
            }
            
            Collection<? extends ProjectValidator> validators = 
                    Lookup.getDefault().lookupAll(ProjectValidator.class);

            for (ProjectValidator validator : validators) {
                String result = validator.validateProjects(subProjects);

                if (result != null) {
                    //  very strange
                    NotifyDescriptor d = new NotifyDescriptor.Message(
                            result, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(d);
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }
    
    private void saveCasaChanges(JbiProject project) {
        // Save casa   
        CasaHelper.saveCasa(project);

        // Save casa wsdl
        FileObject casaWsdlFO = CasaHelper.getCompAppWSDLFileObject(project);
        if (casaWsdlFO != null) {
            try {
                DataObject dataObject = DataObject.find(casaWsdlFO);
                SaveCookie saveCookie = dataObject.getCookie(SaveCookie.class);
                if (saveCookie != null) {
                    saveCookie.save();
                }
            } catch (Exception ex) {
                // failed to load casa...
            }
        }
        
        // TODO: save other wsdls in compapp
    }
    
    
    private void setupTests() {
        try {
            FileObject testDir = project.getTestDirectory();
            if (testDir == null) {
                NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(JbiActionProvider.class, "MSG_NoTestDirError"), // NOI18N
                        NbBundle.getMessage(JbiActionProvider.class, "TTL_NoTestDirError"), // NOI18N
                        NotifyDescriptor.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                    testDir = project.createTestDirectory();
                } else {
                    return;
                }
            }
            
            //Generate a list of test folders
            Enumeration testFolders = testDir.getFolders(false); // no recursion
            List<String> testCaseNameList = new ArrayList();
            while (testFolders.hasMoreElements()) {
                FileObject testFolder = (FileObject)testFolders.nextElement();
                String testFolderName = testFolder.getName();
                //accumulate everything except "results" folder and other well-known folders
                if (!testFolderName.equals("results") && !testFolderName.equalsIgnoreCase("cvs")) { // NOI18N
                    testCaseNameList.add(testFolderName);
                }
            }
            
            Collections.sort(testCaseNameList);
            
            String testCasesCSV = ""; // NOI18N
            for (String testCaseName : testCaseNameList) {
                testCasesCSV +=  testCaseName + ","; // NOI18N
            }
            if (testCasesCSV.length() > 1) {
                testCasesCSV = testCasesCSV.substring(0, testCasesCSV.length() - 1);
            }
            String testCasesProperty = "testcases=" + testCasesCSV; // NOI18N
            
            //write csv to all-tests.properties
            String fileName = FileUtil.toFile(testDir).getPath() + "/all-tests.properties"; // NOI18N
            BufferedWriter fw = new BufferedWriter(new FileWriter(fileName));
            fw.write(testCasesProperty, 0, testCasesProperty.length());
            fw.close();
            
//            //read all-tests.properties
//            BufferedReader fr = new BufferedReader(new FileReader(fileName));
//            testCasesProperty = fr.readLine();
//            fr.close();
            
            //write csv to selected-tests.properties
            //org.netbeans.modules.compapp.catd.ConfiguredTests expects this
            fileName = FileUtil.toFile(testDir).getPath() + "/selected-tests.properties"; // NOI18N
            fw = new BufferedWriter(new FileWriter(fileName));
            fw.write(testCasesProperty, 0, testCasesProperty.length());
            fw.close();
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param command DOCUMENT ME!
     * @param context DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean isActionEnabled(String command, Lookup context) {
        if (findBuildXml() == null) {
            return false;
        }
        
        return true;
    }
    
    // Private methods -----------------------------------------------------
    private boolean isDebugged() {
        return false;
    }
    
    private boolean isProjectEmpty() {
        String comps = antProjectHelper.getStandardPropertyEvaluator().getProperty(
                JbiProjectProperties.JBI_CONTENT_ADDITIONAL
                );
        
        return ((comps == null) || (comps.trim().length() < 1));
    }
    
    private boolean isSelectedServer() {
        String instance = antProjectHelper.getStandardPropertyEvaluator().getProperty(
                JbiProjectProperties.J2EE_SERVER_INSTANCE
                );
        boolean selected = true;
        
        if ((instance == null) || !JbiManager.isAppServer(instance)) {
            String[] serverIDs = JbiManager.getAppServers();
            
            if (serverIDs.length < 1) {
                NotifyDescriptor d =
                        new NotifyDescriptor.Message(
                        NbBundle.getMessage(
                        JbiActionProvider.class, "MSG_NoInstalledServerError" // NOI18N
                        ),
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return false;
            }
            
            NoSelectedServerWarning panel = new NoSelectedServerWarning( serverIDs );
            
            Object[] options = new Object[] {
                DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION
            };
            DialogDescriptor desc = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(
                    NoSelectedServerWarning.class, "CTL_NoSelectedServerWarning_Title" // NOI18N
                    ),
                    true, options, options[0], DialogDescriptor.DEFAULT_ALIGN, null, null
                    );
            Dialog dlg = DialogDisplayer.getDefault().createDialog(desc);
            dlg.setVisible(true);
            
            if (desc.getValue() != options[0]) {
                selected = false;
            } else {
                instance = panel.getSelectedInstance();
                selected = instance != null;
                
                if (selected) {
//                    JbiProjectProperties wpp = new JbiProjectProperties(
//                            project, antProjectHelper, refHelper
//                        );
//                    wpp.put(JbiProjectProperties.J2EE_SERVER_INSTANCE, instance);
//                    wpp.store();
                    JbiProjectProperties projectProperties = project.getProjectProperties();
                    projectProperties.put(JbiProjectProperties.J2EE_SERVER_INSTANCE, instance);
                    projectProperties.store();
                }
            }
            
            dlg.dispose();
        }
        
        if ((instance == null) || (!selected)) {
            NotifyDescriptor d =
                    new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                    JbiActionProvider.class, "MSG_NoSelectedServerError" // NOI18N
                    ),
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
            return false;
//        } else if (!JbiManager.isRunningAppServer(instance)) {
//            NotifyDescriptor d =
//                    new NotifyDescriptor.Message(
//                    NbBundle.getMessage(
//                    JbiActionProvider.class, "MSG_NoRunningServerError" // NOI18N
//                    ),
//                    NotifyDescriptor.ERROR_MESSAGE);
//            DialogDisplayer.getDefault().notify(d);
//            return false;
        }
        
        return true;
    }
}
