/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ws.qaf;

import java.awt.Container;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CleanJavaProjectAction;
import org.netbeans.jellytools.actions.OutputWindowViewAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.project.ui.test.ProjectSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Base class for web services UI tests
 * @author lukas
 */
public abstract class WebServicesTestBase extends J2eeTestCase {

    protected static final ServerType REGISTERED_SERVER;
    private static final Logger LOGGER = Logger.getLogger(WebServicesTestBase.class.getName());
    private Project project;
    private String projectName;
    private ProjectType projectType;
    private JavaEEVersion javaEEversion;

    static {
        //First found server will be used by tests
        if (ServerType.GLASSFISH_V3.isAutoRegistered()) {
            REGISTERED_SERVER = ServerType.GLASSFISH_V3;
        } else if (ServerType.GLASSFISH.isAutoRegistered()) {
            REGISTERED_SERVER = ServerType.GLASSFISH;
        } else if (ServerType.TOMCAT.isAutoRegistered()) {
            REGISTERED_SERVER = ServerType.TOMCAT;
        } else if (ServerType.JBOSS.isAutoRegistered()) {
            REGISTERED_SERVER = ServerType.JBOSS;
        } else {
            REGISTERED_SERVER = null;
        }
    }

    /**
     * Enum type to hold project specific settings (like ie. project category
     * label, project template name, etc.)
     */
    protected enum ProjectType {

        JAVASE_APPLICATION,
        WEB,
        EJB,
        APPCLIENT,
        SAMPLE,
        MAVEN_SE,
        MAVEN_WEB,
        MAVEN_EJB;

        /**
         * Get project template category name
         *
         * @return category name
         */
        public String getCategory() {
            switch (this) {
                case JAVASE_APPLICATION:
                    //Java
                    return Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard");
                case WEB:
                    //Java Web
                    return Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web");
                case EJB:
                    //Java EE
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.earproject.ui.wizards.Bundle", "Templates/Project/J2EE");
                case APPCLIENT:
                    //Java EE
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.earproject.ui.wizards.Bundle", "Templates/Project/J2EE");
                case SAMPLE:
                    //Samples
                    return Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "Templates/Project/Samples");
                case MAVEN_SE:
                case MAVEN_WEB:
                case MAVEN_EJB:
                    //Maven
                    return Bundle.getStringTrimmed("org.netbeans.modules.maven.newproject.Bundle", "Templates/Project/Maven2");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Get project template project type name
         *
         * @return project type name
         */
        public String getProjectTypeName() {
            switch (this) {
                case JAVASE_APPLICATION:
                    //Java Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle", "Templates/Project/Standard/emptyJ2SE.xml");
                case WEB:
                    //Web Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.wizards.Bundle", "Templates/Project/Web/emptyWeb.xml");
                case EJB:
                    //EJB Module
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjarproject.ui.wizards.Bundle", "Templates/Project/J2EE/emptyEjbJar.xml");
                case APPCLIENT:
                    //Enterprise Application Client
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.clientproject.ui.wizards.Bundle", "Templates/Project/J2EE/emptyCar.xml");
                case MAVEN_SE:
                    //Maven Project
                    return Bundle.getStringTrimmed("org.netbeans.modules.maven.newproject.Bundle", "Templates/Project/Maven2/Archetypes");
                case MAVEN_WEB:
                    //Maven Web Application
                    return Bundle.getStringTrimmed("org.netbeans.modules.maven.newproject.Bundle", "Templates/Project/Maven2/WebApp");
                case MAVEN_EJB:
                    //Maven EJB Module
                    return Bundle.getStringTrimmed("org.netbeans.modules.maven.newproject.Bundle", "Templates/Project/Maven2/EJB");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Get index of Server JComboBox in new project wizard
         *
         * @return index of Server JComboBox or -1 if there's none
         */
        public int getServerComboBoxIndex() {
            switch (this) {
                case MAVEN_SE:
                case MAVEN_WEB:
                case MAVEN_EJB:
                case JAVASE_APPLICATION:
                    return -1;
                case WEB:
                case EJB:
                case APPCLIENT:
                    return 1;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Get index of Java EE version JComboBox in new project wizard
         *
         * @return index of Java EE version JComboBox or -1 if there's none
         */
        public int getServerVersionComboBoxIndex() {
            switch (this) {
                case MAVEN_SE:
                case JAVASE_APPLICATION:
                    return -1;
                case MAVEN_WEB:
                case MAVEN_EJB:
                    return 0;
                case WEB:
                case APPCLIENT:
                case EJB:
                    return 2;
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public boolean isAntBasedProject() {
            return this.ordinal() < 5;
        }
    }

    /**
     * Enum type to hold supported JavaEE versions
     */
    protected enum JavaEEVersion {

        J2EE14,
        JAVAEE5,
        JAVAEE6;

        @Override
        public String toString() {
            switch (this) {
                case J2EE14:
                    //J2EE 1.4
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.common.Bundle", "LBL_J2EESpec_14");
                case JAVAEE5:
                    //Java EE 5
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.common.Bundle", "LBL_JavaEESpec_5");
                case JAVAEE6:
                    //Java EE 6
                    return Bundle.getStringTrimmed("org.netbeans.api.j2ee.core.Bundle", "JavaEE6Full.displayName");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    /**
     * Enum type to hold supported servers
     */
    protected enum ServerType {

        SJSAS,
        GLASSFISH,
        GLASSFISH_V3,
        TOMCAT,
        JBOSS;

        @Override
        public String toString() {
            switch (this) {
                case SJSAS:
                    //Sun Java System Application Server
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.dm.Bundle", "FACTORY_DISPLAYNAME");
                case GLASSFISH:
                    //GlassFish V2
                    String label = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.Bundle", "LBL_GLASSFISH_V2");
                    //Need only "GlassFish" to be able to handle both versions (v1, v2)
                    return label.substring(0, label.length() - 3);
                case GLASSFISH_V3:
                    //GlassFish V3
                    return Bundle.getStringTrimmed("org.netbeans.modules.glassfish.javaee.Bundle", "TXT_DisplayName");
                case TOMCAT:
                    //Tomcat
                    return Bundle.getStringTrimmed("org.netbeans.modules.tomcat5.util.Bundle", "LBL_DefaultDisplayName");
                case JBOSS:
                    //JBoss Application Server
                    return Bundle.getStringTrimmed("org.netbeans.modules.j2ee.jboss4.Bundle", "SERVER_NAME");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        /**
         * Check if given server is present in the IDE
         *
         * @return true if server is registered in the IDE, false otherwise
         */
        public boolean isAutoRegistered() {
            switch (this) {
                case SJSAS:
                case GLASSFISH:
                    return System.getProperty("com.sun.aas.installRoot") != null; //NOI18N
                case GLASSFISH_V3:
                    return System.getProperty("org.glassfish.v3ee6.installRoot") != null; //NOI18N
                case TOMCAT:
                    return System.getProperty("tomcat.home") != null; //NOI18N
                case JBOSS:
                    return System.getProperty("jboss.home") != null; //NOI18N
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    /**
     * Default constructor.
     *
     * @param testName name of particular test case
     */
    public WebServicesTestBase(String name) {
        super(name);
        setProjectName(getProjectName());
        setProjectType(getProjectType());
        setJavaEEversion(getJavaEEversion());
    }

    public void assertServerRunning() {
        if (!(REGISTERED_SERVER.equals(ServerType.GLASSFISH) || REGISTERED_SERVER.equals(ServerType.GLASSFISH_V3))) {
            LOGGER.info("not yet supported for server: " + REGISTERED_SERVER.toString());
            return;
        }
        J2eeServerNode gf = getServerNode();
        gf.refresh();
        if (gf.isCollapsed()) {
            gf.expand();
        }
        assertTrue("Server is not running", 0 < gf.getChildren().length);
    }

    /**
     * Get the name of the project to be used by test case
     *
     * @return name of the project
     */
    protected abstract String getProjectName();

    /**
     * Get the name of the sample project's category (ie. Web Services)
     *
     * @return name of the project
     */
    protected String getSamplesCategoryName() {
        return "";
    }

    /**
     * Get a Project instance used by test case
     *
     * @return a Project instance
     */
    protected Project getProject() {
        return project;
    }

    /**
     * Get <code>Node</code> for the project used by test case
     *
     * @return an instance of <code>ProjectRootNode</code>
     */
    protected ProjectRootNode getProjectRootNode() {
        return ProjectsTabOperator.invoke().getProjectRootNode(getProjectName());
    }

    /**
     * Get <code>FileObject</code> representing default project source root
     *
     * @return default project source root
     */
    protected FileObject getProjectSourceRoot() {
        Sources s = getProject().getLookup().lookup(Sources.class);
        SourceGroup[] sg = s.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        return sg[0].getRootFolder();
    }

    /**
     * Java EE version set for test case, JavaEEVersion.JAVAEE5
     * is used by default
     * Override this method to use different Java EE version
     *
     * @return Java EE version set for test case
     */
    protected JavaEEVersion getJavaEEversion() {
        return JavaEEVersion.JAVAEE5;
    }

    /**
     * Project type set for test case, ProjectType.WEB is used by default
     * Override this method to use different ProjectType
     *
     * @return ProjectType set for test case
     */
    protected ProjectType getProjectType() {
        return ProjectType.WEB;
    }

    /**
     * Method responsible for checking and setting up environment for particular
     * test case, mainly for setting up project to be used by test case.<br/>
     *
     * Following logic is used for setting up a project (Note that
     * <code>getProjectName()</code> method is used for getting correct project
     * name):<br/>
     * <ol>
     *  <li>look for a project in <i>projects</i> directory in data directory
     *      and if project is found there then open it in the IDE
     *  </li>
     *  <li>look for a project in <code>getWorkDir().getParentFile().getParentFile()</code>
     *      or in <code>System.getProperty("java.io.tmpdir")</code> directory
     *      (if some parent file does not exist), if project is found then open it
     *      in the IDE</li>
     *  <li>if project is not found then it will be created from scratch</li>
     * </ol>
     *
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        assertNotNull("No server has been found", REGISTERED_SERVER); //NOI18N
        if (!ProjectType.SAMPLE.equals(getProjectType())) {
            if (ServerType.TOMCAT.equals(REGISTERED_SERVER) && !ProjectType.WEB.equals(getProjectType()) && !ProjectType.JAVASE_APPLICATION.equals(getProjectType())) {
                fail("Tomcat does not support: " + getProjectType().getProjectTypeName() + "s."); //NOI18N
            }
            System.out.println("########  TestCase: " + getName() + "  #######"); //NOI18N
            System.out.println("########  Server: " + REGISTERED_SERVER.toString() + "  #######"); //NOI18N
            File projectRoot = new File(getDataDir(), "projects/" + getProjectName()); //NOI18N
            if (projectRoot.exists()) {
                project = (Project) ProjectSupport.openProject(new File(getDataDir(), "projects/" + getProjectName()));
                checkMissingServer(getProjectName());
            } else {
                projectRoot = new File(getProjectsRootDir(), projectName);
                LOGGER.info("Using project in: " + projectRoot.getAbsolutePath()); //NOI18N
                if (!projectRoot.exists()) {
                    project = createProject(projectName, getProjectType(), getJavaEEversion());
                    // closing Tasks tab
                    new OutputWindowViewAction().performMenu();
                    new ActionNoBlock("Window|Tasks", null).performMenu();
                    new ActionNoBlock("Window|Close Window", null).performMenu();
                    //set server for maven projects
                    if (ProjectType.MAVEN_WEB.equals(getProjectType()) || ProjectType.MAVEN_EJB.equals(getProjectType())) {
                        //Properties
                        String propLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_Properties_Action");
                        new ActionNoBlock(null, propLabel).performPopup(getProjectRootNode());
                        new EventTool().waitEvent(2000);
                        // "Project Properties"
                        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
                        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
                        // select "Run" category
                        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
                        new JComboBoxOperator(propertiesDialogOper).selectItem(REGISTERED_SERVER.toString());
                        // confirm properties dialog
                        propertiesDialogOper.ok();
                    }
                } else {
                    openProjects(projectRoot.getAbsolutePath());
                    FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(projectRoot));
                    assertNotNull("FO cannot be null", fo); //NOI18N
                    project = ProjectManager.getDefault().findProject(fo);
                    checkMissingServer(projectName);
                }
            }
            assertNotNull("Project cannot be null!", project); //NOI18N
        }
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        project = null;
        projectName = null;
        //save IDE log in workdir
        FileObject ud = FileUtil.toFileObject(new File(System.getProperty("netbeans.user"))); //NOI18N
        FileObject log = ud.getFileObject("var/log/messages.log"); //NOI18N
        File f = new File(getWorkDir(), "messages.log"); //NOI18N
        if (f.exists()) {
            f.delete();
        }
        FileObject copy = FileUtil.toFileObject(getWorkDir()).createData("messages", "log"); //NOI18N
        InputStream is = log.getInputStream();
        FileLock lock = copy.lock();
        OutputStream os = copy.getOutputStream(lock);
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
            os.close();
            lock.releaseLock();
        }
    }

    /**
     * Start a server
     */
    public void testStartServer() throws IOException {
        J2eeServerNode serverNode = getServerNode();
        serverNode.start();
        dumpOutput();
    }

    /**
     * Stop a server
     */
    public void testStopServer() throws IOException {
        J2eeServerNode serverNode = getServerNode();
        serverNode.stop();
        new EventTool().waitNoEvent(2000);
        dumpOutput();
    }

    /**
     * Helper method to be used by subclasses to create new project according
     * to given parameters. Default server registered in the IDE will be used.
     *
     * @param name project or sample name
     * @param type project type
     * @param javaeeVersion server type, can be null
     * @return created project
     * @throws java.io.IOException
     */
    protected Project createProject(String name, ProjectType type, JavaEEVersion javaeeVersion) throws IOException {
        // project category & type selection step
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        npwo.lstProjects().setComparator(new Operator.DefaultStringComparator(true, true));
        if (ProjectType.SAMPLE.equals(type)) {
            npwo.selectCategory(type.getCategory() + "|" + getSamplesCategoryName());
            npwo.selectProject(name);
            name = getProjectName();
        } else {
            npwo.selectCategory(type.getCategory());
            npwo.selectProject(type.getProjectTypeName());
        }
        npwo.next();
        // project name & location selection step
        NewWebProjectNameLocationStepOperator op = new NewWebProjectNameLocationStepOperator();
        op.txtProjectName().setText(name);
        if (ProjectType.SAMPLE.equals(type)) {
            op.txtLocation().setText(getWorkDirPath());
        } else {
            File projectLocation = null;
            projectLocation = getProjectsRootDir();
            op.txtProjectLocation().setText(projectLocation.getAbsolutePath());
        }
        LOGGER.info("Creating project in: " + op.txtProjectLocation().getText()); //NOI18N
        if (!(ProjectType.SAMPLE.equals(type) || ProjectType.JAVASE_APPLICATION.equals(type))) {
            //second panel in New Web, Ejb and AppClient project wizards
            if (ProjectType.APPCLIENT.equals(type) || ProjectType.EJB.equals(type) || ProjectType.WEB.equals(type)) {
                op.next();
                //choose server type and Java EE version
                JComboBoxOperator jcboServer = new JComboBoxOperator(op, type.getServerComboBoxIndex());
                jcboServer.selectItem(REGISTERED_SERVER.toString());
            }
            JComboBoxOperator jcboVersion = new JComboBoxOperator(op, type.getServerVersionComboBoxIndex());
            jcboVersion.selectItem(javaeeVersion.toString());
        }
        if (!type.isAntBasedProject()) {
            op.btFinish().pushNoBlock();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                //ignore
            }
            if (JDialogOperator.findJDialog("Message", true, true) != null) {
                new JButtonOperator(new JDialogOperator("Message")).push();
            }
            // Opening Projects
            String openingProjectsTitle = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle", "LBL_Opening_Projects_Progress");
            waitDialogClosed(openingProjectsTitle);
        } else {
            op.finish();
        }
        if (ProjectType.SAMPLE.equals(type)) {
            checkMissingServer(name);
        }
        // wait project appear in projects view
        ProjectRootNode node = ProjectsTabOperator.invoke().getProjectRootNode(name);
        // wait classpath scanning finished
        waitScanFinished();
        // get a project instance to return
        Project p = ((org.openide.nodes.Node) node.getOpenideNode()).getLookup().lookup(Project.class);
        assertNotNull("Project instance has not been found", p);
        return p;
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <i>Web Services</i> category
     *
     * @param p project where to create new file
     * @param fileType file type name from web services category
     */
    protected void createNewWSFile(Project p, String fileType) {
        // Web Services
        String webServicesLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.client.wizard.Bundle", "Templates/WebServices");
        createNewFile(p, webServicesLabel, fileType);
    }

    /**
     * Helper method to be used by subclasses to create new file of given
     * <code>fileType</code> from <code>fileCategory</code> category
     *
     * @param p project where to create new file
     * @param fileType file type name from web services category
     */
    protected void createNewFile(Project p, String fileCategory, String fileType) {
        // file category & filetype selection step
        NewFileWizardOperator nfwo = NewFileWizardOperator.invoke();
        new EventTool().waitNoEvent(500);
        nfwo.treeCategories().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.lstFileTypes().setComparator(new Operator.DefaultStringComparator(true, true));
        nfwo.cboProject().selectItem(p.toString());
        nfwo.selectCategory(fileCategory);
        nfwo.selectFileType(fileType);
        nfwo.next();
    }

    /**
     * Deploy a project
     *
     * @param projectName name of the project to be deployed
     */
    protected void deployProject(String projectName) throws IOException {
        new CleanJavaProjectAction().perform();
        //Deploy
        String deployProjectLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.Bundle", "LBL_RedeployAction_Name");
        performProjectAction(projectName, deployProjectLabel);
    }

    /**
     * Run a project
     *
     * @param projectName name of the project to be run
     */
    protected void runProject(String projectName) throws IOException {
        //Run
        String runLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.Bundle", "LBL_RunAction_Name");
        performProjectAction(projectName, runLabel);
    }

    /**
     * Undeploy a project
     *
     * @param projectName name of the project to be undeployed
     */
    protected void undeployProject(String projectName) throws IOException {
        assertServerRunning();
        J2eeServerNode serverNode = getServerNode();
        serverNode.expand();
        //Applications
        String applicationsLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Applications");
        //Web Applications
        String webLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_WebModules");
        //EJB Modules
        String ejbLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_EjbModules");
        //App Client Modules
        String appclientLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_AppClientModules");
        //Refresh
        String refreshLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.actions.Bundle", "LBL_RefreshAction");
        //Undeploy
        String undeployLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Undeploy");
        Node appsNode = null;
        switch (getProjectType()) {
            case SAMPLE:
            case WEB:
            case MAVEN_WEB:
                if (ServerType.TOMCAT.equals(REGISTERED_SERVER)) {
                    appsNode = new Node(serverNode, webLabel);
                } else if (ServerType.GLASSFISH_V3.equals(REGISTERED_SERVER)) {
                    appsNode = new Node(serverNode, applicationsLabel);
                } else {
                    appsNode = new Node(serverNode, applicationsLabel + "|" + webLabel);
                }
                break;
            case EJB:
            case MAVEN_EJB:
                if (ServerType.GLASSFISH_V3.equals(REGISTERED_SERVER)) {
                    appsNode = new Node(serverNode, applicationsLabel);
                } else {
                    appsNode = new Node(serverNode, applicationsLabel + "|" + ejbLabel);
                }
                break;
            case APPCLIENT:
                appsNode = new Node(serverNode, applicationsLabel + "|" + appclientLabel);
                break;
        }
        appsNode.expand();
        appsNode.callPopup().pushMenu(refreshLabel);
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 30000); //NOI18N
        Node n = new Node(appsNode, projectName);
        n.callPopup().pushMenu(undeployLabel);
        new EventTool().waitNoEvent(2000);
        appsNode.callPopup().pushMenu(refreshLabel);
        new EventTool().waitNoEvent(2000);
        dumpOutput();
    }

    /**
     * Save content of output tabs into test's working directory.
     * Might be useful for diagnosing possible test failures
     *
     * @throws java.io.IOException
     */
    protected void dumpOutput() throws IOException {
        OutputOperator oo = OutputOperator.invoke();
        oo.requestFocus();
        JTabbedPaneOperator jtpo = null;
        if (null != JTabbedPaneOperator.findJTabbedPane((Container) oo.getSource(), ComponentSearcher.getTrueChooser(""))) {
            jtpo = new JTabbedPaneOperator(oo);
        }
        if (jtpo != null) {
            for (int i = 0; i < jtpo.getTabCount(); i++) {
                String tabTitle = jtpo.getTitleAt(i);
                if (!jtpo.getComponentAt(i).isShowing()) {
                    continue;
                }
                jtpo.selectPage(i);
                OutputTabOperator oto = null;
                if (tabTitle.indexOf("<html>") < 0) { //NOI18N
                    oto = new OutputTabOperator(tabTitle.trim());
                } else {
                    oto = new OutputTabOperator(tabTitle.substring(9, 19).trim());
                }
                oto.requestFocus();
                writeToFile(oto.getText(),
                        new File(getWorkDir(), tabTitle.trim().replace(' ', '_') + ".txt")); //NOI18N
            }
        } else {
            OutputTabOperator oto = oo.getOutputTab(""); //NOI18N
            writeToFile(oto.getText(),
                    new File(getWorkDir(), "default_out.txt")); //NOI18N
        }
    }

    /**
     * Wait until dialog with title <code>dialogTitle</code> is closed
     *
     * @param dialogTitle title of the dialog to be closed
     */
    protected void waitDialogClosed(String dialogTitle) {
        try {
            // wait at most 60 second until progress dialog dismiss
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000); //NOI18N
            new NbDialogOperator(dialogTitle).waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
    }

    private void performProjectAction(String projectName, String actionName) throws IOException {
        ProjectRootNode node = new ProjectsTabOperator().getProjectRootNode(projectName);
        node.performPopupAction(actionName);
        OutputTabOperator oto = new OutputTabOperator(projectName);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 600000); //NOI18N
        if (!getProjectType().isAntBasedProject()) {
            oto.waitText("Total time:"); //NOI18N
            dumpOutput();
            assertTrue("Build failed", oto.getText().indexOf("BUILD SUCCESSFUL") > -1); //NOI18N
            assertTrue("Deploy failed", oto.getText().indexOf("[ERROR]") < 0); //NOI18N
        } else {
            //Ant projects
            oto.waitText("(total time: "); //NOI18N
            dumpOutput();
            assertTrue("Build failed", oto.getText().indexOf("BUILD SUCCESSFUL") > -1); //NOI18N
        }


    }

    private void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    private void setJavaEEversion(JavaEEVersion javaEEversion) {
        this.javaEEversion = javaEEversion;
    }

    private void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    /**
     * Write given <code>text</code> into given <code>file</code>.
     *
     * @param text text to be written
     * @param file file to be created
     */
    private void writeToFile(String text, File file) {
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            os.write(text.getBytes());
            os.flush();
        } catch (IOException ioe) {
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    protected void checkMissingServer(String project) {
        // check missing target server dialog is shown
        // "Open Project"
        String openProjectTitle = Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "MSG_Broken_Server_Title");
        boolean needToSetServer = false;
        if (JDialogOperator.findJDialog(openProjectTitle, true, true) != null) {
            new NbDialogOperator(openProjectTitle).close();
            needToSetServer = true;
        }
        // Set as Main Project
        String setAsMainProjectItem = Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle", "LBL_SetAsMainProjectAction_Name");
        new Action(null, setAsMainProjectItem).perform(new ProjectsTabOperator().getProjectRootNode(project));
        if (needToSetServer) {
            // open project properties
            ProjectsTabOperator.invoke().getProjectRootNode(project).properties();
            // "Project Properties"
            String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
            NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
            // select "Run" category
            new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
            // not display browser on run
//            String displayBrowserLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_CustomizeRun_DisplayBrowser_JCheckBox");
//            new JCheckBoxOperator(propertiesDialogOper, displayBrowserLabel).setSelected(false);
            // set default server
            new JComboBoxOperator(propertiesDialogOper).setSelectedIndex(0);
            // confirm properties dialog
            propertiesDialogOper.ok();
        }
        // if setting default server, it scans server jars; otherwise it continues immediatelly
        waitScanFinished();
    }

    protected File getProjectsRootDir() throws IOException {
        File f = getWorkDir();
        LOGGER.fine("Working directory is set to: " + f.getAbsolutePath());
        if (f != null) {
            f = f.getParentFile();
            if (f != null) {
                f = f.getParentFile();
            } else {
                return new File(System.getProperty("java.io.tmpdir"));
            }
        } else {
            return new File(System.getProperty("java.io.tmpdir"));
        }
        return f;
    }

    protected J2eeServerNode getServerNode() {
        switch (REGISTERED_SERVER) {
            case GLASSFISH:
                return getServerNode(Server.GLASSFISH);
            case GLASSFISH_V3:
                return getServerNode(Server.GLASSFISH_V3);
            case JBOSS:
                return getServerNode(Server.JBOSS);
            case TOMCAT:
                return getServerNode(Server.TOMCAT);
        }
        return getServerNode(Server.ANY);
    }
}
