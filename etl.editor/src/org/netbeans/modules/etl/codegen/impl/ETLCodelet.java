
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
package org.netbeans.modules.etl.codegen.impl;


//import com.sun.jbi.ui.devtool.bpms.common.model.visitor.XMLParseVisitorException;
//import com.sun.jbi.ui.devtool.bpms.common.model.wsdl.Operation;
//import com.sun.jbi.ui.devtool.bpms.common.model.wsdl.PortType;
//import com.sun.jbi.ui.devtool.bpms.common.model.wsdl.WSDLDefinitions;
//import com.sun.jbi.ui.devtool.bpms.common.model.wsdl.WSDLDocument;
//import com.sun.jbi.ui.devtool.bpms.common.model.wsdl.WSDLMessage;
//import com.sun.jbi.ui.devtool.codegen.framework.model.CodeGenException;
//import com.sun.jbi.ui.devtool.codegen.framework.model.CodeGenFileSystem;
//import com.sun.jbi.ui.devtool.codegen.framework.model.CodeGenFramework;
//import com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet;
//import com.sun.jbi.ui.devtool.codegen.framework.model.ErrorBundle;
//import com.sun.jbi.ui.devtool.codegen.framework.model.FileGeneratorCodelet;
//import com.sun.jbi.ui.devtool.codegen.framework.model.ValidationCodelet;
//import com.sun.jbi.ui.devtool.codegen.framework.model.CodeGenFileSystem.CodeGenFolder;
//import com.sun.jbi.ui.devtool.codegen.framework.model.util.CodeGenHelper;
//import com.sun.jbi.ui.devtool.codegen.framework.model.util.DeploymentProfileGenerator;
//import com.sun.jbi.ui.devtool.codegen.framework.model.util.DeploymentProfileGenerator.MDBAttributes;
//import com.sun.jbi.ui.devtool.codegen.framework.runtime.DeployedElementName;
//import com.sun.jbi.ui.devtool.configuration.IConfiguration;
//import com.sun.jbi.ui.devtool.connector.repository.EWayConfiguration;
//import com.sun.jbi.ui.devtool.connector.repository.ExternalApplication;
//import com.sun.jbi.ui.devtool.connector.repository.ExternalSystemType;
//import com.sun.jbi.ui.devtool.connector.repository.ExternalType;
//import com.sun.jbi.ui.devtool.deployment.repository.ProjectDeployment;
//import com.sun.jbi.ui.devtool.etl.codegen.ETLCodegenUtil;
//import com.sun.jbi.ui.devtool.etl.codegen.ETLProcessFlowGeneratorFactory;
//import com.sun.jbi.ui.devtool.etl.model.ETLDefinition;
//import com.sun.jbi.ui.devtool.etl.model.ETLDefinitionProjectElement;
//import com.sun.jbi.ui.devtool.etl.model.impl.ETLDefinitionImpl;
//import com.sun.jbi.ui.devtool.etl.runtime.ejb.ETLRuntimeHandler;
//import com.sun.jbi.ui.devtool.etl.utils.ETLDeploymentConstants;
//import com.sun.jbi.ui.devtool.etl.utils.MessageManager;
//import com.sun.jbi.ui.devtool.logicalhost.repository.IntegrationServer;
//import com.sun.jbi.ui.devtool.model.common.Environment;
//import com.sun.jbi.ui.devtool.model.common.EnvironmentElement;
//import com.sun.jbi.ui.devtool.model.common.cme.CMLink;
//import com.sun.jbi.ui.devtool.model.common.cme.CMNode;
//import com.sun.jbi.ui.devtool.model.common.cme.Connectable;
//import com.sun.jbi.ui.devtool.model.common.cme.ConnectorConfiguration;
//import com.sun.jbi.ui.devtool.model.common.cme.ConnectorNode;
//import com.sun.jbi.ui.devtool.model.common.cme.Deployable;
//import com.sun.jbi.ui.devtool.model.common.cme.Port;
//import com.sun.jbi.ui.devtool.model.common.cme.ProcessingNode;
//import com.sun.jbi.ui.devtool.repository.RepositoryException;
//import com.sun.jbi.ui.devtool.sql.framework.exception.BaseException;
//import com.sun.jbi.ui.devtool.sql.framework.model.ValidationInfo;
//import com.sun.jbi.ui.devtool.sql.framework.utils.Logger;
//import com.sun.jbi.ui.devtool.sql.framework.utils.StringUtil;
//import com.sun.jbi.ui.devtool.sql.framework.utils.XmlUtil;

/**
 * Generates code for a given (@link Deployable) that includes ejb and resource adapter.
 * Resource adapters are shared by mutiple collab participating in the same CME
 *
 * @author Sudhi Seshachala
 * @author Ahimanikya Satapathy
 * @author Jonathan Giron
 * @author Girish Patil
 * @version $Revision$
 */

public class ETLCodelet /*implements FileGeneratorCodelet, DeploymentProfileMgrCodelet, ValidationCodelet*/ {

//    public static final String EXTERNAL_APPLICATION_TYPE_FLATFILEDB = "STCDBADAPTER"; // NOI18N
//	private static final String LOG_CATEGORY = ETLCodelet.class.getName();
//    private static final MessageManager MESSAGE_MGR = MessageManager.getManager(ETLCodelet.class);
//    private static final String CONTAINER_TXN_MODE = "NotSupported";
//    private static final List DEPENDENT_CLASS_NAMES = new ArrayList();    
//
//    static {
//        /*
//         *  Note:  the following JARs are in the RTS server classpath, so they won't be declared below:
//         *  <pre>
//         *  - activation.jar
//         *  - commons-collections[-3.x].jar
//         *  - commons-logging.jar
//         *  - jakarta-regexp.jar
//         *  </pre>
//         */
//        // com.sun.jbi.ui.devtool.etl.engineapi.jar: eTL (engine)
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.engine.ETLTask");
//
//        // com.sun.jbi.ui.devtool.etl.engineimpl.jar: eTL (engine)
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.engine.impl.InitTask");
//
//        // com.sun.jbi.ui.devtool.etl.utils.jar: eTL (engine)
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.utils.ETLException");
//
//        // com.sun.jbi.ui.devtool.sqlframework.common.jar: eTL, sqlframework, flatfiledb (various)
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.sql.framework.exception.BaseException");
//
//        // commons-jxpath.jar: eTL (runtime.ETLBeanMessageImpl)
//        DEPENDENT_CLASS_NAMES.add("org.apache.commons.jxpath.JXPathContext");
//
//        // com.sun.jbi.ui.devtool.etl.monitor.jar: eTL (ETL MBean server)
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.monitor.mbeans.ETLMBeanLoader");
//
//        // AxionDB.jar: eTL, sqlframework, flatfiledb (various)
//        DEPENDENT_CLASS_NAMES.add("org.axiondb.jdbc.AxionDriver");
//
//        // add axion query jar org.axiondb.service
//        DEPENDENT_CLASS_NAMES.add("org.axiondb.service.AxionDBQuery");
//
//        // add eView API jar "com.sun.jbi.ui.devtool.sbmeapi.jar"
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.sbmeapi.StandardizationAPI");
//
//        // add eView API Impl/Lib jar "com.sun.jbi.ui.devtool.sbmeapiimpl.jar"
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.sbmeapi.impl.StandConfigFilesAccessImpl");
//
//        // add eInsight jar, required by com.sun.jbi.ui.devtool.etl.runtime.ETLBeanMessageFactory
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.bpms.bpel.runtime.BeanMessageFactory");
//        
//        // com.sun.jbi.ui.devtool.sunconfigapi_runtime.jar
//        DEPENDENT_CLASS_NAMES.add("com.sun.jbi.ui.devtool.configuration.AttributeTransformer");
//        
//
//        // >
//        // > Listed below are JARs which require special handling.
//        // >
//
//        // NOTE:  We don't use UnmodifiableIntIterator in our code, but reference it here
//        // because it doesn't exist in obsolete versions of commons-collections JARs which
//        // also include tbe primitives package.
//
//        // commons-primitives-1.0.jar: AxionDB (various)
//        DEPENDENT_CLASS_NAMES.add("org.apache.commons.collections.primitives.decorators.UnmodifiableIntIterator");
//    }
//
//    private static final List RUNTIME_CLASS_NAMES = new ArrayList();
//    static {
//        RUNTIME_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.runtime.ETLBeanMessageImpl");
//        RUNTIME_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.runtime.ETLBeanMessageImpl$1");
//        RUNTIME_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.runtime.ETLPersistableMessageImpl");
//        RUNTIME_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.runtime.ETLBeanMessageFactory");
//        RUNTIME_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.runtime.StartETLEngine");
//        RUNTIME_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.runtime.RuntimeUtil");        
//        RUNTIME_CLASS_NAMES.add("com.sun.jbi.ui.devtool.etl.runtime.ejb.ETLRuntimeHandler");        
//    }
//
//    private static final List RESOURCE_NAMES = new ArrayList();
//    static {
//    	RESOURCE_NAMES.add("com/sun/jbi/ui/devtool/etl/runtime/eTLAlert.properties");
//    }
//    
//    private Deployable deployable;
//    private DeployedElementName deployedName;
//
//    private CodeGenFolder ejbFolder;
//    private CodeGenFramework framework;
//    private CodeGenHelper helper;
//    private ProjectDeployment projectDeployment;
//    private ETLDefinitionProjectElement etlDef;
//    private DeploymentProfileGenerator dpGen;
//    private String runtimeServerWorkspaceDir;
//
//    public ETLCodelet(ProjectDeployment pd, CodeGenFramework fw, Deployable theDeployable) throws CodeGenException {
//        this.framework = fw;
//        this.deployable = theDeployable;
//        this.projectDeployment = pd;
//        this.dpGen = fw.createDeploymentProfileHelper();
//
//        try {
//            this.etlDef = (ETLDefinitionProjectElement) ((ProcessingNode) deployable).getProcessingDefinition();
//        } catch (RepositoryException repoEx) {
//            String errMsg = MESSAGE_MGR.getString("ERR_REPOSITORY_EX");
//            Logger.print(Logger.ERROR, LOG_CATEGORY, errMsg, repoEx);
//            throw new CodeGenException(errMsg);
//        }
//
//        try {
//            this.deployedName = framework.getDeployedElementName(deployable);
//        } catch (CodeGenException e) {
//            Logger.print(Logger.DEBUG, LOG_CATEGORY, "Failed to initialize in ETLCodelet", e);
//            throw new CodeGenException(MESSAGE_MGR.getString("ERR_BADNAME_CONSTRUCTOR"));
//        }
//    }
//
//    // NOTE: codegen framework calls generateFiles() first and then
//    // generateDeploymentProfile() get called.
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet#generateDeploymentProfile
//     */
//    public void generateDeploymentProfile(CodeGenFileSystem fs) throws CodeGenException {
//        try {
//            // Specify the name space for ETL Runtime Handler
//            addEnv(ETLDeploymentConstants.STC_NAME_SPACE, deployedName.getNamespace());
//
//            // Specify the engine file name generated earlier
//            addEnv(ETLDeploymentConstants.ETL_ENGINE_FILE, StringUtil.escapeNonAlphaNumericCharacters(getLocalPart()) + ".xml");
//
//            // Specify the Collaboration name
//            addEnv(ETLDeploymentConstants.COLLAB_NAME, getLocalPart());
//
//            // Specify the Project name
//            String projName = ((ProcessingNode) this.deployable).getParentConnectivityMap().getParentProject().getName();
//            projName = getHelper().convertString(projName);
//            addEnv(ETLDeploymentConstants.PROJECT_NAME, projName);
//
//            // Specify the class name for ETLHandler; do we still need it ?
//            addEnv(ETLDeploymentConstants.CLASS_NAME, ETLRuntimeHandler.class.getName());
//
//            // Specify the operation name; currently we only support "execute"
//            addEnv(ETLDeploymentConstants.OPERATION_NAME_TAG, ETLDeploymentConstants.OPERATION_NAME);
//
//            // Specify relative path to engine working directory in app-server file
//            // system, assuming we use whatever working directory is supplied by the
//            // app-server as the root.
//            addEnv(ETLDeploymentConstants.ETL_ENGINE_INSTANCE_DB_DIR, getEngineInstanceDBDirectory());
//            addEnv(ETLDeploymentConstants.ETL_MONITOR_DB_DIR, getEngineMonitorDBDirectory());
//            addEnv(ETLDeploymentConstants.RUNTIME_SERVER_WORKSPACE_DIR, getRuntimeServerWorkspaceDir());
//
//            // Get Project Deployment Reference
//            ProjectDeployment projDeployment = framework.getProjectDeployment();
//
//            // Get the deployment envronment to which the project has been deployed
//            Environment environment = projDeployment.getEnvironment();
//            EnvironmentElement envElement = projDeployment.getDeployedElement(this.deployable, environment);
//
//            // Get the Integration server from the envronment
//            String is = ((IntegrationServer) envElement).getName();
//            addEnv(ETLDeploymentConstants.IS, is);
//
//            // Get the LogicalHost server from the envronment
//            String lh = ((IntegrationServer) envElement).getParentLogicalHost().getName();
//            addEnv(ETLDeploymentConstants.LH, lh);
//
//            // Copy required jar files to EJBFolder
//            copyDependentJars(fs);
//
//            // Specify the deployment name
//            addEnv(ETLDeploymentConstants.DEPLOYMENT_NAME, projDeployment.getName());
//
//            // Specify the environment name
//            String environmentName = environment.getName();
//            addEnv(ETLDeploymentConstants.ENVIRONMENT_NAME, environmentName);
//
//            // Set container transaction mode
//            dpGen.addContainerTransactionForDeployedService(this, ejbFolder, null, "*", null, CONTAINER_TXN_MODE);
//
//            // validate the WSDL
//            validateWSDL(etlDef);
//        } catch (BaseException baseEx) {
//            String errMsg = MESSAGE_MGR.getString("ERR_BASE_EX");
//            Logger.print(Logger.ERROR, LOG_CATEGORY, errMsg, baseEx);
//            throw new CodeGenException(errMsg, baseEx);
//        } catch (RepositoryException repoEx) {
//            String errMsg = MESSAGE_MGR.getString("ERR_REPOSITORY_EX");
//            Logger.print(Logger.ERROR, LOG_CATEGORY, errMsg, repoEx);
//            throw new CodeGenException(errMsg, repoEx);
//        }
//    }
//
//    public void generateFiles(CodeGenFileSystem codeGenFileSystem) throws CodeGenException {
//        // STEP1:Create and Initialize the EJB folder for Runtime Handler
//        Logger.print(Logger.DEBUG, LOG_CATEGORY, "Code generation: generating files for " + this.getLocalPart());
//
//        ejbFolder = codeGenFileSystem.createEJBFolder(this);
//        Logger.print(Logger.DEBUG, LOG_CATEGORY, "Code generation: target dir = " + ejbFolder.getDir());
//
//        try {
//            // STEP2: Generate eTL engine file
//            // Use collabServiceName as it is guaranteed to be unique in multi collab CMAP.
//            String collabServiceName = StringUtil.escapeNonAlphaNumericCharacters(getLocalPart()) + ".xml";
//            Collection pnLinks = getProcessingNode().getLinks();
//            GenerateEngineFile fileGenerator = new GenerateEngineFile(this, pnLinks, this.etlDef, this.projectDeployment);
//            String engineFile = fileGenerator.getEngineTaskNodes();
//            writeEngineFile(engineFile, collabServiceName);
//        } catch (RepositoryException e) {
//            String errMsg = MESSAGE_MGR.getString("ERR_REPOSIT_EX_GENERATE_FILES");
//            Logger.print(Logger.ERROR, LOG_CATEGORY, errMsg, e.toString());
//            throw new CodeGenException(errMsg, e);
//        } catch (Exception t) {
//            String errMsg = MESSAGE_MGR.getString("ERR_THROWABLE_GENERATE_FILES");
//            Logger.print(Logger.ERROR, LOG_CATEGORY, errMsg, t);
//            if (t instanceof CodeGenException) {
//                errMsg = ((CodeGenException) t).getLocalizedMessage();
//            }
//            throw new CodeGenException(errMsg, t);
//        }
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.Codelet#getDebugName
//     */
//    public String getDebugName() {
//        return "eTL Integrator Codelet";
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet#getRequestHandlerAttributes
//     */
//    public DeployedServiceAttributes getDeployedServiceAttributes() {
//        return new DeployedServiceAttributes() {
//            public String getClassName() {
//                return ETLRuntimeHandler.class.getName();
//            }
//        };
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet#getEntryPointAttributes
//     */
//    public MDBAttributes getEntryPointAttributes() {
//        return null;
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.Codelet#getOwner
//     */
//    public Deployable getOwner() {
//        return this.deployable;
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet#isRequestHandler()
//     */
//    public boolean isDeployedService() {
//        return true;
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet#isEntryPointForRequestHandler
//     */
//    public boolean isEntryPointToDeployedService() {
//        return false;
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet#postDeploymentProfileGeneration
//     */
//    public void postDeploymentProfileGeneration(CodeGenFileSystem fs) throws CodeGenException {
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.FileGeneratorCodelet#postFileGeneration
//     */
//    public void postFileGeneration(CodeGenFileSystem fs) throws CodeGenException {
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.DeploymentProfileMgrCodelet#preDeploymentProfileGeneration
//     */
//    public void preDeploymentProfileGeneration(CodeGenFileSystem fs) throws CodeGenException {
//    }
//
//    /*
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.FileGeneratorCodelet#preFileGeneration
//     */
//    public void preFileGeneration(CodeGenFileSystem fs) throws CodeGenException {
//    }
//
//    /**
//     * @see com.sun.jbi.ui.devtool.codegen.framework.model.ValidationCodelet#validate()
//     */
//    public ErrorBundle validate(){
//        ErrorBundle errBundle = framework.createErrorBundle();
//        try {
//            validateETLDefinition(errBundle);
//            validateDeplymentConfiguration(errBundle);
//            // TODO move below validation to SQLDefination.
//            ETLProcessFlowGeneratorFactory.validateExecutionMode(this.getETLDefinition(etlDef).getSQLDefinition());
//        }catch (Exception ex) {
//            int i = getNextSequenceForErrorBundle(errBundle);
//            errBundle.addError(framework.createErrorEntry(projectDeployment, new Integer(i++), "Exception:" + ex.getMessage()));
//        }
//        return errBundle;
//    }
//
//    public String getExternalSystemType(CMLink link) throws RepositoryException {
//        CMNode destNode = link.getDestinationNode();
//        Connectable connectable = null;
//        if (destNode instanceof ConnectorNode) {
//            connectable = ((ConnectorNode) destNode).getConnectable();
//            return getDeploymentDBType(connectable);
//        }
//        return null;
//    }
//
//    /**
//     * Gets full name of ExternalSystem type associated with the given CMLink.
//     *
//     * @param link CMLink whose ExternalSystem type name is to be obtained
//     * @return full name of ExternalSystem type, or " <Unknown>" if non-existent.
//     */
//    public String getExternalSystemTypeName(CMLink link) throws RepositoryException {
//        ConnectorConfiguration connConfig = link.getConnectorConfiguration();
//        ExternalSystemType extSystemType = null;
//        
//        if (connConfig != null){
//            ExternalType extType = ((EWayConfiguration) connConfig).getExternalType();
//            // Some migrated AxionDB's configuration have null values.
//            if (extType != null){
//            	extSystemType = extType.getExternalSystemType();
//            }
//        }
//
//        return (extSystemType != null) ? extSystemType.getName() : MESSAGE_MGR.getString("LBL_UNKNOWN");
//    }
//
//    
//    /**
//     * Generates a unique relative path to engine working directory.
//     * @return String representing relative path to engine working directory on app server
//     */
//    String getEngineInstanceDBDirectory() throws BaseException {
//        return ETLCodegenUtil.getEngineInstanceWorkingFolder();
//    }
//
//    /**
//     * Generates a unique relative path to monitor db directory.
//     * @return String representing relative path to monitor db directory on app server
//     */
//    String getEngineMonitorDBDirectory() throws BaseException {
//        return ETLCodegenUtil.getMonitorDBDir(this.getDeployable());
//    }
//
//
//    private String getDeploymentDBType(Connectable connectable) throws RepositoryException {
//        if (connectable != null && connectable instanceof ExternalApplication) {
//            ExternalApplication extApp = (ExternalApplication) connectable;
//            return extApp.getExternalApplicationType().getModuleName();
//        }
//        return EXTERNAL_APPLICATION_TYPE_FLATFILEDB; 
//    }
//
//    /**
//     * @return Returns the deployedName.
//     */
//    protected DeployedElementName getDeployedName() {
//        return deployedName;
//    }
//
//    protected ETLDefinition getETLDefinition(ETLDefinitionProjectElement etldef) throws RepositoryException, BaseException {
//        String content = etldef.getETLDefinitionContent();
//        Element elem = XmlUtil.loadXMLString(content);
//        ETLDefinition etlDefn = new ETLDefinitionImpl(elem, etldef);
//        return etlDefn;
//    }
//
//    private void addEnv(String key, String value) throws CodeGenException {
//        String type = ETLDeploymentConstants.STRING;
//        this.dpGen.addEnv(this.ejbFolder, getLocalPart(), key, type, value);
//    }
//
//    private void copyDependentJars(CodeGenFileSystem codeGenFileSystem) throws CodeGenException {
//
//        // Add other required jar files to the EAR file and have the RAR file refer it.
//        Iterator iter = DEPENDENT_CLASS_NAMES.iterator();
//        while (iter.hasNext()) {
//            String className = (String) iter.next();
//            if (!addJarForClass(className, codeGenFileSystem)) {
//                throw new CodeGenException(MESSAGE_MGR.getString("ERR_JARMISSING_COPY_DEPENDENT_JARS", className));
//            }
//        }
//
//        // Just to package what is needed for ETLRuntimeHandler
//        packageRuntime(ejbFolder.getDir());
//    }
//
//
//    /**
//     * Gets path to library JAR file, if any, that contains the given Class.
//     *
//     * @param clazz Class whose containing JAR is to be located
//     * @return String representing path to containing JAR, or null if no such JAR exists
//     */
//    private String getFilePath(Class clazz) {
//        File jarFile = (clazz != null) ? getHelper().getJar(getClass().getClassLoader(), clazz) : null;
//        return (jarFile != null) ? jarFile.getAbsolutePath() : null;
//    }
//
//    /**
//     * Gets path to library JAR file, if any, that contains the Class represented by the
//     * given String.
//     *
//     * @param clazz Class whose containing JAR is to be located
//     * @return String representing path to containing JAR, or null if no such JAR exists
//     */
//    private String getFilePath(String className) {
//        try {
//            ClassLoader cl = getClass().getClassLoader();
//            return getFilePath(cl.loadClass(className));
//        } catch (ClassNotFoundException cnf) {
//            return null;
//        }
//    }
//
//    /**
//     * Locates the JAR, if any, containing the class represented by the given classname
//     * String and adds it to the EAR.
//     *
//     * @param className name of class whose JAR is to be added
//     * @param cgFileSystem ref to CodeGenFileSystem representing EAR image
//     * @return true if JAR was located for <code>className</code> and copied to
//     * <code>cgFileSystem</code>, false otherwise
//     */
//    private boolean addJarForClass(String className, CodeGenFileSystem cgFileSystem) {
//        if (Logger.isDebugEnabled(LOG_CATEGORY)) {
//            Logger.print(Logger.DEBUG, LOG_CATEGORY, "Resolving JAR for class " + className);
//        }
//
//        String filePath = getFilePath(className);
//        if (filePath != null) {
//            if (Logger.isDebugEnabled(LOG_CATEGORY)) {
//                Logger.print(Logger.DEBUG, LOG_CATEGORY, "Resolved " + className + " to JAR " + filePath);
//            }
//
//            File jarFile = new File(filePath);
//            cgFileSystem.addApplicationAPI(jarFile, this);
//            return true;
//        }
//
//        return false;
//    }
//
//    /**
//     * Gets instance of CodeGenHelper for this codelet instance.
//     *
//     * @return associated CodeGenHelper instance
//     */
//    private CodeGenHelper getHelper() {
//        if (this.helper == null) {
//            this.helper = framework.createCodeGenHelper();
//        }
//        return this.helper;
//    }
//
//    private String getLocalPart() {
//        return deployedName.getLocalPart();
//    }
//
//    private ProcessingNode getProcessingNode() throws CodeGenException {
//        try {
//            return (ProcessingNode) this.deployable;
//        } catch (ClassCastException e) {
//            String msg = MESSAGE_MGR.getString("ERR_CLASSCAST_EX_GETPROCESSINGNODE");
//            Logger.print(Logger.WARN, LOG_CATEGORY, msg);
//            throw new CodeGenException(msg);
//        }
//    }
//
//    private void packageRuntime(File outputDir) throws CodeGenException {
//        CodeGenHelper cgHelper = getHelper();
//        
//        // Packages individual classes
//        String className = null;
//        Iterator iter = RUNTIME_CLASS_NAMES.iterator();        
//        try {
//            while (iter.hasNext()) {
//                className = (String) iter.next();
//                cgHelper.copyJavaClassToFile(className, getClass().getClassLoader(), outputDir);
//            }
//        } catch (IOException e) {
//            throw new CodeGenException(MESSAGE_MGR.getString("ERR_IO_EX_PACKAGERUNTIME", className));
//        }
//
//        // Package individual resources
//        String resourceName = null;
//        iter = RESOURCE_NAMES.iterator();        
//        try {
//            while (iter.hasNext()) {
//            	resourceName = (String) iter.next();
//                cgHelper.copyResourceToFile(resourceName, getClass().getClassLoader(), outputDir);
//            }
//        } catch (IOException e) {
//            throw new CodeGenException(MESSAGE_MGR.getString("ERR_IO_EX_PACKAGERUNTIME", className));
//        }
//        
//    }
//
//    private int  validateETLDefinition(ErrorBundle errorBundle) {
//        int i = 1;
//        try {
//            i = getNextSequenceForErrorBundle(errorBundle);
//            List invalidObjectList = this.getETLDefinition(etlDef).validate();
//            if (invalidObjectList.size() > 0) {
//                Iterator iter = invalidObjectList.iterator();
//                while (iter.hasNext()) {
//                    ValidationInfo invalidObj = (ValidationInfo) iter.next();
//                    if (invalidObj.getValidationType() == ValidationInfo.VALIDATION_ERROR) {
//                        String errorMessage = MESSAGE_MGR.getString("ERR_ENTRY_VALIDATECONFIG", invalidObj.getDescription());
//                        errorBundle.addError(this.framework.createErrorEntry(etlDef, new Integer(i++), errorMessage));
//                    }
//                }
//            }
//        } catch (Exception e) {
//            errorBundle.addError(this.framework.createErrorEntry(etlDef, new Integer(i), e.toString()));
//        }
//        return i;
//    }
//
//    private int getNextSequenceForErrorBundle(ErrorBundle errorBundle) {
//        int i = 1;
//
//        if (errorBundle == null) {
//            errorBundle = framework.createErrorBundle();
//        }
//
//        Collection tc = errorBundle.getErrors() ;
//        if (tc != null) {
//            i = (i <= tc.size())?(tc.size()+1): i;
//        }
//        return i;
//    }
//
//    private void validateDeplymentConfiguration(ErrorBundle errorBundle){
//        try {
//            validateDBConfiguarion(errorBundle);
//            validateRuntimeServerConfiguarion(errorBundle);
//
//        }catch (CodeGenException ex) {
//            int i = getNextSequenceForErrorBundle(errorBundle);
//            errorBundle.addError(this.framework.createErrorEntry(etlDef, new Integer(i++), ex.getMessage()));
//        }catch (RepositoryException ex) {
//            int i = getNextSequenceForErrorBundle(errorBundle);
//            errorBundle.addError(this.framework.createErrorEntry(etlDef, new Integer(i++), ex.getMessage()));
//        }
//    }
//
//    private int validateDBConfiguarion(ErrorBundle errorBundle)
//			throws CodeGenException, RepositoryException {
//		int i = getNextSequenceForErrorBundle(errorBundle);
//		String portName = null;
//		String externalSystemType = null;
//		String extSysTypeName = null;
//		String errorMessage = null;
//		String linkName = null;
//		
//		Collection pnLinks = getProcessingNode().getLinks();
//		ETLDefinitionProjectElement etlProcessDefinition = this.etlDef;
//		CMLink link = null;
//		Port port = null;
//		com.sun.jbi.ui.devtool.model.common.cme.PortType portType = null;	
//		ConnectorConfiguration connectorConfig = null;
//		IConfiguration config = null;		
//		EnvironmentElement ee = null; 
//		
//		Iterator it = pnLinks.iterator();
//		while (it.hasNext()) {
//			Object objRef = it.next();
//			if (objRef != null && objRef instanceof CMLink) {
//				link = (CMLink) objRef;
//				portName = link.getSourcePort();
//				port = null;
//				externalSystemType = getExternalSystemType(link);
//				extSysTypeName = "<Unknown>";
//				config = null;
//				errorMessage = null;
//				linkName = link.getName();
//				ee = projectDeployment.getDeployedElement(link, projectDeployment.getEnvironment());
//
//				if (portName != null) {
//					port = etlProcessDefinition.getDestination(portName);
//					if (port != null) {
//						if ((!EXTERNAL_APPLICATION_TYPE_FLATFILEDB.equalsIgnoreCase(externalSystemType))) {
//							portType = port.getPortType();
//							connectorConfig = link.getConnectorConfiguration();
//
//							if (portType != null) {
//								config = ETLCodegenHelper.getIConfiguration(connectorConfig, link.getName());
//								if ((config == null) || (config.getSections() == null)) {
//									errorMessage = MESSAGE_MGR.getString("ERR_CONN_CONFIG_MISSING", linkName);
//									errorBundle.addError(this.framework.createErrorEntry(ee, new Integer(i++), errorMessage));
//								} else {
//									extSysTypeName = getExternalSystemTypeName(link);
//									Map sectionMap = config.getSections();
//									if (!sectionMap.containsKey("JDBCConnectorSettings")) {
//										errorMessage = MESSAGE_MGR.getString("ERR_CONN_CONFIG_SECTION_JDBC_MISSING", linkName);
//										errorBundle.addError(this.framework.createErrorEntry(ee, new Integer(i++), errorMessage));
//									}
//								}
//							}
//
//						}
//
//						try {
//							config = ETLCodegenHelper.getExternalSystemConfiguration(link, this.projectDeployment, extSysTypeName);
//							if (config == null) {
//								errorMessage = MESSAGE_MGR.getString("ERR_CONN_PARAMS_MISSING", ee.getName(), extSysTypeName);
//								errorBundle.addError(this.framework.createErrorEntry(ee, new Integer(i++), errorMessage));
//							}
//						} catch (Exception ex) {
//							errorMessage = MESSAGE_MGR.getString("ERR_CONN_PARAMS_MISSING", ee.getName(), extSysTypeName);
//							errorBundle.addError(this.framework.createErrorEntry(ee, new Integer(i++), errorMessage));
//						}						
//					}
//				}
//			}
//		}
//		return i;
//	}
//
//    private void validateRuntimeServerConfiguarion(ErrorBundle errorBundle){
//        String tWorkspaceDir = null;
//
//        if (errorBundle == null) {
//            errorBundle = framework.createErrorBundle();
//        }
//
//
//        tWorkspaceDir = ETLCodegenUtil.getRSWorkspaceDir(framework, errorBundle, projectDeployment, getDeployable());
//
//        if (tWorkspaceDir != null) {
//            this.runtimeServerWorkspaceDir = tWorkspaceDir;
//        }
//    }
//
//    private void validateWSDL(ETLDefinitionProjectElement def) throws RepositoryException, XMLParseVisitorException, CodeGenException {
//        WSDLDocument doc = def.getWSDLDocument();
//        WSDLDefinitions wsdlDef = doc.getDocumentDefinitions();
//        Collection portTypes = wsdlDef.getPortTypes();
//        Collection messages = wsdlDef.getMessages();
//        List messageList = new ArrayList();
//
//        if (portTypes == null || portTypes.size() == 0) {
//            String mesg = MESSAGE_MGR.getString("ERR_NOPORTTYPES_VALIDATEWSDL", getLocalPart());
//            Logger.print(Logger.ERROR, LOG_CATEGORY, mesg);
//            throw new CodeGenException(mesg);
//        }
//
//        for (Iterator it = portTypes.iterator(); it.hasNext();) {
//            PortType type = (PortType) it.next();
//            Collection operations = type.getOperations();
//
//            if (operations == null || operations.size() == 0) {
//                String mesg = MESSAGE_MGR.getString("ERR_NOOPERATIONS_VALIDATEWSDL", type.getName());
//                Logger.print(Logger.ERROR, LOG_CATEGORY, mesg);
//                throw new CodeGenException(mesg);
//            }
//
//            for (Iterator opIt = operations.iterator(); opIt.hasNext();) {
//                Operation op = (Operation) opIt.next();
//                messageList.add(op.getInput().getMessage());
//                messageList.add(op.getOutput().getMessage());
//            }
//
//            if (messageList.size() == 0) {
//                String mesg = MESSAGE_MGR.getString("ERR_NOMESSAGETYPES_VALIDATEWSDL", getLocalPart());
//                Logger.print(Logger.ERROR, LOG_CATEGORY, mesg);
//                throw new CodeGenException(mesg);
//            }
//        }
//
//        Logger.print(Logger.DEBUG, LOG_CATEGORY, "Message List = " + messageList);
//        for (Iterator it = messageList.iterator(); it.hasNext();) {
//            String mesgName = (String) it.next();
//            boolean messageTypeFound = false;
//            for (Iterator ms = messages.iterator(); ms.hasNext();) {
//                WSDLMessage wMesg = (WSDLMessage) ms.next();
//                String mesg = wMesg.getName();
//                Logger.print(Logger.DEBUG, LOG_CATEGORY, "message type = " + mesg);
//                if (mesgName.equals("tns:" + mesg)) {
//                    messageTypeFound = true;
//                    mesgName = mesg;
//                    break;
//                }
//            }
//
//            if (!messageTypeFound) {
//                String msg = MESSAGE_MGR.getString("ERR_NOMSGMATCHFOROPER_VALIDATEWSDL", mesgName);
//                Logger.print(Logger.DEBUG, LOG_CATEGORY, msg);
//                throw new CodeGenException(msg);
//            }
//        }
//    }
//
//    private void writeEngineFile(String engineContent, String fileName) throws IOException {
//        String filePath = ejbFolder.getDir() + File.separator;
//        File etlEngineFile = new File(filePath + fileName);
//
//        Logger.print(Logger.DEBUG, LOG_CATEGORY, "Creating eTL engine file: " + etlEngineFile);
//        FileWriter writer = new FileWriter(etlEngineFile);
//        writer.write(engineContent);
//        writer.close();
//    }
//
//    public Deployable getDeployable() {
//        return this.deployable;
//    }
//
//    public String getRuntimeServerWorkspaceDir() {
//        return this.runtimeServerWorkspaceDir;
//    }
}
