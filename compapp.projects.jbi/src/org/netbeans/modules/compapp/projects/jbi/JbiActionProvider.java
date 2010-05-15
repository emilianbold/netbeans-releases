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
package org.netbeans.modules.compapp.projects.jbi;

import com.sun.esb.management.common.ManagementRemoteException;
import com.sun.jbi.ui.common.ServiceAssemblyInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.jbiserver.JbiManager;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.*;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.*;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.projects.jbi.api.JbiBuildListener;
import org.netbeans.modules.compapp.projects.jbi.api.ProjectValidator;
import org.netbeans.modules.compapp.test.ui.TestcaseNode;
import org.netbeans.modules.sun.manager.jbi.management.JBIMBeanTaskResultHandler;
import org.netbeans.modules.sun.manager.jbi.management.wrapper.api.RuntimeManagementServiceWrapper;
import org.netbeans.modules.sun.manager.jbi.util.ServerInstance;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.ui.support.DefaultProjectOperations;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileUtil;

/**
 * Action provider of the Web project. This is the place where to do strange things to Web actions.
 * E.g. compile-single.
 */
public class JbiActionProvider implements ActionProvider {

    // Definition of commands
    private static final String[] supportedActions = {
        COMMAND_CLEAN,
        JbiProjectConstants.COMMAND_UNDEPLOY,
        JbiProjectConstants.COMMAND_REDEPLOY,
        JbiProjectConstants.COMMAND_DEPLOY,
        JbiProjectConstants.COMMAND_JBIBUILD,
        JbiProjectConstants.COMMAND_JBICLEANBUILD,
        JbiProjectConstants.COMMAND_JBICLEANCONFIG,
        JbiProjectConstants.COMMAND_VALIDATEPORTMAPS,
        JbiProjectConstants.COMMAND_TEST,
        COMMAND_DELETE,
        COMMAND_COPY,
        COMMAND_MOVE,
        COMMAND_RENAME,
        COMMAND_BUILD,
        COMMAND_REBUILD,
        COMMAND_DEBUG
    };

    private JbiProject project;

    // Ant project helper of the project
    private AntProjectHelper antProjectHelper;

    /** Map from commands to ant targets */
    private Map<String, String[]> commands;

    private static final String TARGET_NAME_RUN = "run"; // NOI18N
    private static final String TARGET_NAME_RUN_JBI_DEPLOY_WITHOUT_BUILD = "run-jbi-deploy-without-build"; // NOI18N
    private static final String TARGET_NAME_UNDEPLOY = "undeploy"; // NOI18N
    private static final String TARGET_NAME_JBI_BUILD = "jbi-build"; // NOI18N
    private static final String TARGET_NAME_JBI_CLEAN_BUILD = "jbi-clean-build"; // NOI18N
    private static final String TARGET_NAME_JBI_CLEAN_CONFIG = "jbi-clean-config"; // NOI18N
    private static final String TARGET_NAME_TEST = "test"; // NOI18N
    private static final String TARGET_NAME_DEBUG = "debug"; // NOI18N
    private static final String TARGET_NAME_CLEAN = "clean"; // NOI18N

    /**
     * Creates a new JbiActionProvider object.
     *
     * @param project DOCUMENT ME!
     * @param antProjectHelper DOCUMENT ME!
     */
    public JbiActionProvider(JbiProject project, AntProjectHelper antProjectHelper) {
        commands = new HashMap<String, String[]>();
        commands.put(JbiProjectConstants.COMMAND_REDEPLOY, new String[]{TARGET_NAME_RUN});
        commands.put(JbiProjectConstants.COMMAND_DEPLOY, new String[]{TARGET_NAME_RUN});
        commands.put(JbiProjectConstants.COMMAND_UNDEPLOY, new String[]{TARGET_NAME_UNDEPLOY});
        commands.put(JbiProjectConstants.COMMAND_JBIBUILD, new String[]{TARGET_NAME_JBI_BUILD});
        commands.put(JbiProjectConstants.COMMAND_JBICLEANCONFIG, new String[]{TARGET_NAME_JBI_CLEAN_CONFIG});
        commands.put(JbiProjectConstants.COMMAND_JBICLEANBUILD, new String[]{TARGET_NAME_JBI_CLEAN_BUILD});
        commands.put(JbiProjectConstants.COMMAND_TEST, new String[]{TARGET_NAME_TEST});
        //commands.put(JbiProjectConstants.COMMAND_VALIDATEPORTMAPS, new String[]{"validate-portmaps"});

        // map common project action to jbi ones
        commands.put(COMMAND_CLEAN, new String[]{TARGET_NAME_CLEAN});
        commands.put(COMMAND_BUILD, new String[]{TARGET_NAME_JBI_BUILD});
        commands.put(COMMAND_REBUILD, new String[]{TARGET_NAME_JBI_CLEAN_BUILD});
        commands.put(COMMAND_DEBUG, new String[]{TARGET_NAME_DEBUG});

        this.antProjectHelper = antProjectHelper;
        this.project = project;
    }

    /**
     * @return array of targets or null to stop execution; can return empty array
     */
    /*private*/
    String[] getTargetNames(String command, Lookup context, Properties p)
            throws IllegalArgumentException {
        String[] targetNames = commands.get(command);
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
    public void invokeAction(final String command, Lookup context)
            throws IllegalArgumentException {

        // #160224
        JbiProjectProperties props = project.getProjectProperties();
        Boolean skipBuildWhenDeploy = (Boolean) props.get(JbiProjectProperties.SKIP_BUILD_WHEN_DEPLOY);

        if (skipBuildWhenDeploy) { // skip "jbi-build" ant task
            commands.put(JbiProjectConstants.COMMAND_REDEPLOY, new String[]{TARGET_NAME_RUN_JBI_DEPLOY_WITHOUT_BUILD});
            commands.put(JbiProjectConstants.COMMAND_DEPLOY, new String[]{TARGET_NAME_RUN_JBI_DEPLOY_WITHOUT_BUILD});
        } else {
            commands.put(JbiProjectConstants.COMMAND_REDEPLOY, new String[]{TARGET_NAME_RUN});
            commands.put(JbiProjectConstants.COMMAND_DEPLOY, new String[]{TARGET_NAME_RUN});
        }

        // starting server could be time consuming
        new Thread(new Runnable() {

            public void run() {

                if (COMMAND_DELETE.equals(command)) {
                    DefaultProjectOperations.performDefaultDeleteOperation(project);
                    return;
                }

                if (COMMAND_COPY.equals(command)) {
                    DefaultProjectOperations.performDefaultCopyOperation(project);
                    return;
                }

                if (COMMAND_MOVE.equals(command)) {
                    DefaultProjectOperations.performDefaultMoveOperation(project);
                    return;
                }

                if (COMMAND_RENAME.equals(command)) {
                    DefaultProjectOperations.performDefaultRenameOperation(project, null);
                    return;
                }

                if (command.equals(JbiProjectConstants.COMMAND_TEST)&& !setupTests()) {
                    return;
                }

                if (command.equals(JbiProjectConstants.COMMAND_JBICLEANCONFIG)) {
                    NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                            NbBundle.getMessage(JbiActionProvider.class, "MSG_CleanServiceAssembly"), // NOI18N
                            NbBundle.getMessage(JbiActionProvider.class, "TTL_CleanServiceAssembly"), // NOI18N
                            NotifyDescriptor.OK_CANCEL_OPTION);
                    if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.OK_OPTION) {
                        return;
                    }
                }

                Properties p = new Properties();
                String[] targetNames = commands.get(command);

                JbiProjectProperties properties = project.getProjectProperties();

                if (command.equals(JbiProjectConstants.COMMAND_DEPLOY) ||
                        command.equals(JbiProjectConstants.COMMAND_REDEPLOY) ||
                        command.equals(JbiProjectConstants.COMMAND_JBICLEANCONFIG) ||
                        command.equals(JbiProjectConstants.COMMAND_JBIBUILD) ||
                        command.equals(JbiProjectConstants.COMMAND_JBICLEANBUILD)) {

                    refreshAssemblyInfo(project);
                    
                    saveCasaChanges(project);

                    setWSITCallbackProjectsProperty(project, p);
                    setJAXWSHandlersProperty(project, p);
                    setJAXRSHandlersProperty(project, p);

                    if (!validateSubProjects()) {
                        return;
                    }
                }

                if (command.equals(JbiProjectConstants.COMMAND_DEPLOY) ||
                        command.equals(JbiProjectConstants.COMMAND_REDEPLOY) ||
                        command.equals(JbiProjectConstants.COMMAND_UNDEPLOY) ||
                        command.equals(COMMAND_DEBUG) ||
                        command.equals(JbiProjectConstants.COMMAND_TEST)) {
                    
                    // 4/11/08 OSGi support
                    // 02/04/09, IZ#153580, disable fuji deployment
                    /*
                    Boolean osgiSupport = (Boolean) properties.get(JbiProjectProperties.OSGI_SUPPORT);
                    if (osgiSupport) {
                        String osgiContainerDir = (String) properties.get(JbiProjectProperties.OSGI_CONTAINER_DIR);
                        if (osgiContainerDir == null || osgiContainerDir.trim().length() == 0) {
                            NotifyDescriptor d = new NotifyDescriptor.Message(
                                    NbBundle.getMessage(JbiActionProvider.class,
                                    "MSG_CHOOSE_OSGI_CONTAINER_FIRST"), // NOI18N
                                    NotifyDescriptor.ERROR_MESSAGE);
                            DialogDisplayer.getDefault().notify(d);
                            return;
                        }
                    } else*/

                    if (!JbiManager.isSelectedServer(project)) {
                        return;
                    }

                } else if (targetNames == null) {
                    throw new IllegalArgumentException(command);
                }

                // Make sure the SA is deployed and started. It is more effiecient
                // this way than calling the deploy Ant task blindly in the Ant script.
                if (command.equals(JbiProjectConstants.COMMAND_TEST)) {

                    // Make sure the app server is running.
                    // (block until server is ready)
                    JbiManager.startServer(project, true);

                    String serverInstance = antProjectHelper.getStandardPropertyEvaluator().
                            getProperty(JbiProjectProperties.J2EE_SERVER_INSTANCE);

                    try {
                        ServerInstance server = AdministrationServiceHelper.
                                getServerInstance(System.getProperty("netbeans.user"),
                                serverInstance);
                        p.setProperty("server.password", server.getPassword());

                        RuntimeManagementServiceWrapper mgmtServiceWrapper =
                                AdministrationServiceHelper.getRuntimeManagementServiceWrapper(server);
                        mgmtServiceWrapper.clearServiceAssemblyStatusCache();

                        String saID = (String) properties.get(JbiProjectProperties.SERVICE_ASSEMBLY_ID);
                        ServiceAssemblyInfo saStatus = mgmtServiceWrapper.getServiceAssembly(saID, "server");
                        if (saStatus == null) { // not deployed
                            // Add the deploy target to the target list.
                            // (Alternatively, we could call adminService.deployServiceAssembly
                            // directly, but then we need to worry about project build.)
                            List<String> targetList = new ArrayList<String>();
                            String[] extraTargetNames = commands.get(JbiProjectConstants.COMMAND_DEPLOY);
                            targetList.addAll(Arrays.asList(extraTargetNames));
                            targetList.addAll(Arrays.asList(targetNames));
                            targetNames = targetList.toArray(new String[]{});
                        } else if (!saStatus.getState().equals(ServiceAssemblyInfo.STARTED_STATE)) {
                            // simply start the service assembly
                            String result = mgmtServiceWrapper.startServiceAssembly(saID, "server");
                            boolean success = JBIMBeanTaskResultHandler.showRemoteInvokationResult(
                                    "Start", saID, result); // NOI18N
                            if (!success) {
                                return;
                            }
                        }
                    } catch (ManagementRemoteException e) {
                        NotifyDescriptor d = new NotifyDescriptor.Message(e.getMessage(),
                                NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                        return;
                    }
                }

                JbiBuildListener jbiBuildListener = getBuildListener(command);
                if (jbiBuildListener != null) {
                    jbiBuildListener.buildStarted();
                }

                ExecutorTask executorTask = null;
                try {
                    executorTask = ActionUtils.runTarget(findBuildXml(), targetNames, p);
                    executorTask.waitFinished(); // FIXME: not guaranteed

                    if (command.equals(JbiProjectConstants.COMMAND_TEST)) {                       
                        FileObject testDir = project.getTestDirectory();
                        if (testDir != null) {
                            String fileName = FileUtil.toFile(testDir).getPath() +
                                    "/selected-tests.properties"; // NOI18N
                            try {
                                BufferedReader reader = new BufferedReader(new FileReader(fileName));
                                String line = reader.readLine();
                                assert line.startsWith("testcases=");
                                String testCaseNames = line.substring(line.indexOf('=')); // NOI18N
                                for (String testCaseName : testCaseNames.split(",")) { // NOI18N
                                    FileObject testCaseDir = testDir.getFileObject(testCaseName.trim());
                                    TestcaseNode.setTestCaseRunning(testCaseDir, false);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                } finally {
                    if (jbiBuildListener != null) {
                        jbiBuildListener.buildCompleted(
                                executorTask == null || executorTask.result() == 0);
                    }
                }

            }
        }).start();
    }

    // Check the sub/self projects' project.xml files. If any file has a newer
    // time stamp than compapp's ASI.xml, then we update ASI.xml. This
    // guarantees that renamed sub/self project gets properly reflected in ASI.
    private void refreshAssemblyInfo(JbiProject jbiProject) {
        try {
            FileObject asiFO = jbiProject.getProjectDirectory().getFileObject(
                    "src/conf/" + JbiProject.ASSEMBLY_INFO_FILE_NAME); // NOI18N
            Date asiTimestamp = asiFO.lastModified();

            Set<Project> projects = new HashSet<Project>();
            projects.add(jbiProject);

            SubprojectProvider spp = jbiProject.getLookup().lookup(SubprojectProvider.class);
            projects.addAll(spp.getSubprojects());

            for (Project p : projects) {
                FileObject projectXmlFO = p.getProjectDirectory().
                        getFileObject("nbproject/project.xml"); // NOI18N
                Date projectXmlTimestamp = projectXmlFO.lastModified();

                if (projectXmlTimestamp.after(asiTimestamp)) {
                    //System.out.println("Updating Assembly Information...");
                    jbiProject.getProjectProperties().saveAssemblyInfo();
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setWSITCallbackProjectsProperty(JbiProject project,
            Properties p) {
        // call WSIT Java Callback Project...
        String cbProjects = buildWSITJavaCallbackProject(project);
        if (cbProjects != null) {
            p.setProperty("WsitCallbackProjects", cbProjects);
        }
    }

    private void setJAXWSHandlersProperty(JbiProject project,
            Properties p) {
        // get JAX-WS Handler Jars...
        String jaxwsHandlerJars = getJAXWSHandlerJars(project);
        if (jaxwsHandlerJars != null) {
            p.setProperty("JAXWSHandlerJars", jaxwsHandlerJars);
        }
    }

    private void setJAXRSHandlersProperty(JbiProject project,
            Properties p) {
        // get JAX-RS Handler Jars...
        String jaxrsHandlerJars = getJAXRSHandlerJars(project);
        if (jaxrsHandlerJars != null) {
            p.setProperty("JAXRSHandlerJars", jaxrsHandlerJars);
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
                        return casaDO.getLookup().lookup(JbiBuildListener.class);
                    }
                } catch (DataObjectNotFoundException e) {
                    // ignore the error
                }
            }
        }
        return null;
    }

    private boolean validateSubProjects() {
        try {
            JbiProjectProperties properties = project.getProjectProperties();
            @SuppressWarnings("unchecked")
            List<VisualClassPathItem> itemList =
                    (List) properties.get(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);

            List<Project> subProjects = new ArrayList<Project>();
            Map<Class, Project> subProjectTypeMap = new HashMap<Class, Project>();
            for (VisualClassPathItem item : itemList) {
                AntArtifact aa = item.getAntArtifact();
                if (aa != null) { // aa is null if the corresponding project has been deleted
                    Project subProject = aa.getProject();
                    subProjects.add(subProject);
                    subProjectTypeMap.put(subProject.getClass(), subProject);
                }
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

    private String buildWSITJavaCallbackProject(JbiProject project) {
        // process casa and check for Java callback project setting...
        Set<String> cbProjects = CasaHelper.getWSITCallbackProjects(project);
        return buildJavaProjects(cbProjects);
    }

    private String getJAXWSHandlerJars(JbiProject project) {
        // process casa and check for JAX-WS handler setting...
        Set<String> handlerJars = CasaHelper.getJAXWSHandlerJars(project);
        return getSemicolonDelimitedString(handlerJars);
    }

    private String getJAXRSHandlerJars(JbiProject project) {
        // process casa and check for JAX-RS handler setting...
        Set<String> handlerJars = CasaHelper.getJAXRSHandlerJars(project);
        return getSemicolonDelimitedString(handlerJars);
    }

    private static String getSemicolonDelimitedString(Collection<String> collection) {

        StringBuffer sb = new StringBuffer();

        if (collection != null) {
            for (String item : collection) {
                sb.append(item);
                sb.append(";"); // NOI18N
            }

            if (sb.length() > 0) {
                sb = sb.deleteCharAt(sb.length() - 1); // strip the last ";"
            }
        }

        return sb.toString();
    }

    private String buildJavaProjects(Set<String> projectPaths) {
        // process casa and check for Java callback project setting...
        String projects = null;
        boolean first = true;
        for (String projectPath : projectPaths) {
            // System.out.println("Invoke building: "+pLoc);
            File buildxml = new File(projectPath + "/build.xml"); // NOI18N
            FileObject bfo = FileUtil.toFileObject(buildxml);
            String[] targets = new String[] { "compile" }; // NOI18N
            Properties p = null;
            try {
                ExecutorTask executorTask = ActionUtils.runTarget(bfo, targets, p);
                if (!first) {
                    projects += ";" + projectPath;  // NOI18N
                } else {
                    first = false;
                    projects = projectPath;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return projects;
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

    private boolean setupTests() {
        try {
            FileObject testDir = project.getTestDirectory();
            if (testDir == null) {
                testDir = project.createTestDirectory();
                // TODO: logical view should be updated upon test dir creation

                String msg = NbBundle.getMessage(JbiActionProvider.class, "MSG_NoTestCase"); // NOI18N
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);

                return false;
            }

            //Generate a list of test folders
            Enumeration testFolders = testDir.getFolders(false); // no recursion
            List<String> runnableTestCaseNames = new ArrayList<String>();
            final List<String> skippedTestCaseNames = new ArrayList<String>();
            while (testFolders.hasMoreElements()) {
                FileObject testFolder = (FileObject) testFolders.nextElement();
                String testFolderName = testFolder.getNameExt();
                //accumulate everything except "results" folder and other well-known folders
                if (!testFolderName.equals("results") && !testFolderName.equalsIgnoreCase("cvs")) { // NOI18N
                    if (!TestcaseNode.isTestCaseRunning(testFolder)) {
                        runnableTestCaseNames.add(testFolderName);
                    } else {
                        skippedTestCaseNames.add(testFolderName);
                    }
                }
            }

            if (skippedTestCaseNames.size() > 0) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        String msg = NbBundle.getMessage(JbiActionProvider.class,
                                "MSG_SkipTestCaseInProgress", skippedTestCaseNames); // NOI18N
                        NotifyDescriptor d = new NotifyDescriptor.Message(
                                msg, NotifyDescriptor.INFORMATION_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                    }
                });
            }

            if (runnableTestCaseNames.size() == 0 && skippedTestCaseNames.size() == 0) {
                String msg = NbBundle.getMessage(JbiActionProvider.class, "MSG_NoTestCase"); // NOI18N
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
                return false;
            }

            Collections.sort(runnableTestCaseNames);

            String testCasesCSV = ""; // NOI18N
            for (String testCaseName : runnableTestCaseNames) {
                testCasesCSV += testCaseName + ","; // NOI18N
            }
            if (testCasesCSV.length() > 1) {
                testCasesCSV = testCasesCSV.substring(0, testCasesCSV.length() - 1);
            }

            //write csv to all-tests.properties
            String fileName = FileUtil.toFile(testDir).getPath() + "/all-tests.properties"; // NOI18N
            // EditableProperties takes care of encoding.
            EditableProperties props = new EditableProperties();
            props.setProperty("testcases", testCasesCSV);    // NOI18N
            FileOutputStream outStream = new FileOutputStream(fileName);
            props.store(outStream);
            outStream.close();

            //write csv to selected-tests.properties
            //org.netbeans.modules.compapp.catd.ConfiguredTests expects this
            fileName = FileUtil.toFile(testDir).getPath() + "/selected-tests.properties"; // NOI18N
            // EditableProperties takes care of encoding.
            props = new EditableProperties();
            props.setProperty("testcases", testCasesCSV);    // NOI18N
            outStream = new FileOutputStream(fileName);
            props.store(outStream);
            outStream.close();
            return true;
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
        }

        return false;
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
        String comps = antProjectHelper.getStandardPropertyEvaluator().
                getProperty(JbiProjectProperties.JBI_CONTENT_ADDITIONAL);

        return (comps == null) || (comps.trim().length() < 1);
    }

   
}
