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

package org.netbeans.modules.web.project.api;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.*;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.Repository;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.queries.FileEncodingQuery;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.classpath.ClassPathSupport;
import org.netbeans.modules.web.project.classpath.WebProjectClassPathExtender;
import org.netbeans.modules.web.project.ui.customizer.PlatformUiSupport;
import org.netbeans.modules.j2ee.common.FileSearchUtility;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.w3c.dom.NodeList;


/**
 * Create a fresh WebProject from scratch or by importing and exisitng web module
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class WebProjectUtilities {
    
    /**
     * BluePrints source structure
     */
    public static final String SRC_STRUCT_BLUEPRINTS = "BluePrints"; //NOI18N
    
    /**
     * Jakarta source structure
     */
    public static final String SRC_STRUCT_JAKARTA = "Jakarta"; //NOI18N
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "web"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_JAVA_FOLDER = "java"; //NOI18N
    private static final String DEFAULT_CONF_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    private static final String DEFAULT_TEST_FOLDER = "test"; //NOI18N
    
    private static final String WEB_INF = "WEB-INF"; //NOI18N
    private static final String SOURCE_ROOT_REF = "${" + WebProjectProperties.SOURCE_ROOT + "}"; //NOI18N
    
    public static final String MINIMUM_ANT_VERSION = "1.6";
    
    private static final Logger LOGGER = Logger.getLogger(WebProjectUtilities.class.getName());
    
    private WebProjectUtilities() {}
    
    /**
     * Create a new empty web project.
     *
     * @deprecated Use {@link #createProject(WebProjectCreateData)}
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper createProject(File dir, String name, String serverInstanceID, String sourceStructure, String j2eeLevel, String contextPath)
            throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setServerInstanceID(serverInstanceID);
        createData.setSourceStructure(sourceStructure);
        createData.setJavaEEVersion(j2eeLevel);
        createData.setContextPath(contextPath);
        return createProject(createData);
    }
    
    /**
     * Creates a new empty web project.
     * @param createData the object encapsulating necessary data to create the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(final WebProjectCreateData createData) throws IOException {
        File dir = createData.getProjectDir();
        String name = createData.getName();
        String serverInstanceID = createData.getServerInstanceID();
        String sourceStructure = createData.getSourceStructure();
        String j2eeLevel = createData.getJavaEEVersion();
        String contextPath = createData.getContextPath();
        String javaPlatformName = createData.getJavaPlatformName();
        String sourceLevel = createData.getSourceLevel();
        
        assert dir != null: "Project folder can't be null"; //NOI18N
        assert name != null: "Project name can't be null"; //NOI18N
        assert serverInstanceID != null: "Server instance ID can't be null"; //NOI18N
        assert sourceStructure != null: "Source structure can't be null"; //NOI18N
        assert j2eeLevel != null: "Java EE version can't be null"; //NOI18N
        
        final boolean createBluePrintsStruct = SRC_STRUCT_BLUEPRINTS.equals(sourceStructure);
        final boolean createJakartaStructure = SRC_STRUCT_JAKARTA.equals(sourceStructure);
        
        final FileObject fo = FileUtil.createFolder(dir);
        AntProjectHelper h = setupProject(fo, name, serverInstanceID, j2eeLevel);
        
        FileObject srcFO = fo.createFolder(DEFAULT_SRC_FOLDER);
        FileObject confFolderFO = null;
        
        if (createBluePrintsStruct) {
            srcFO.createFolder(DEFAULT_JAVA_FOLDER);
            confFolderFO = srcFO.createFolder(DEFAULT_CONF_FOLDER);
        }
        
        if(createJakartaStructure) {
            confFolderFO = fo.createFolder(DEFAULT_CONF_FOLDER);
        }
        
        //create default manifest
        if(confFolderFO != null) {
            FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-project/MANIFEST.MF"), confFolderFO, "MANIFEST"); //NOI18N
        }
        
        //test folder
        FileUtil.createFolder(fo, DEFAULT_TEST_FOLDER);
        
        FileObject webFO = fo.createFolder(DEFAULT_DOC_BASE_FOLDER);
        final FileObject webInfFO = webFO.createFolder(WEB_INF);
        // create web.xml
        // PENDING : should be easier to define in layer and copy related FileObject (doesn't require systemClassLoader)
        String webXMLContent = null;
        if (J2eeModule.JAVA_EE_5.equals(j2eeLevel))
            webXMLContent = readResource(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-project/web-2.5.xml").getInputStream()); //NOI18N
        else if (WebModule.J2EE_14_LEVEL.equals(j2eeLevel))
            webXMLContent = readResource(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-project/web-2.4.xml").getInputStream()); //NOI18N
        else if (WebModule.J2EE_13_LEVEL.equals(j2eeLevel))
            webXMLContent = readResource(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-project/web-2.3.xml").getInputStream()); //NOI18N
        assert webXMLContent != null : "Cannot find web.xml template for J2EE specification level:" + j2eeLevel;
        final String webXmlText = webXMLContent;
        FileSystem fs = webInfFO.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject webXML = FileUtil.createData(webInfFO, "web.xml");//NOI18N
                FileLock lock = webXML.lock();
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(webXML.getOutputStream(lock)));
                try {
                    bw.write(webXmlText);
                } finally {
                    bw.close();
                    lock.releaseLock();
                }
            }
        });
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element sourceRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        
        Element rootSrc = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
        rootSrc.setAttribute("id",WebProjectProperties.SRC_DIR);   //NOI18N
        rootSrc.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_src.dir")); //NOI18N
        sourceRoots.appendChild(rootSrc);
        if (createBluePrintsStruct)
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER + "/" + DEFAULT_JAVA_FOLDER); // NOI18N
        else
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER); // NOI18N
        
        data.appendChild(sourceRoots);
        Element testRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        
        Element rootTest = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
        rootTest.setAttribute("id",WebProjectProperties.TEST_SRC_DIR);   //NOI18N
        rootTest.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_test.src.dir")); //NOI18N
        testRoots.appendChild(rootTest);
        ep.setProperty(WebProjectProperties.TEST_SRC_DIR, DEFAULT_TEST_FOLDER); // NOI18N
        
        data.appendChild(testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        ep.put(WebProjectProperties.SOURCE_ROOT, createBluePrintsStruct ? DEFAULT_SRC_FOLDER : "."); //NOI18N
        
        ep.setProperty(WebProjectProperties.WEB_DOCBASE_DIR, DEFAULT_DOC_BASE_FOLDER);
        if (createBluePrintsStruct) {
            ep.setProperty(WebProjectProperties.SRC_DIR, "${" + WebProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_JAVA_FOLDER);
            ep.setProperty(WebProjectProperties.CONF_DIR, "${" + WebProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_CONF_FOLDER);
        } else {
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER);
        }
        
        if(createJakartaStructure) {
            ep.setProperty(WebProjectProperties.CONF_DIR, DEFAULT_CONF_FOLDER);
        }
        
        ep.setProperty(WebProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        ep.setProperty(WebProjectProperties.LIBRARIES_DIR, "${" + WebProjectProperties.WEB_DOCBASE_DIR + "}/" + WEB_INF + "/lib"); //NOI18N
        
        ep.setProperty(WebProjectProperties.WEBINF_DIR, DEFAULT_DOC_BASE_FOLDER + "/" + WEB_INF);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        UpdateHelper updateHelper = ((WebProject) p).getUpdateHelper();
        
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel != null && (sourceLevel.equals("1.6") || sourceLevel.equals("1.7")))
            sourceLevel = "1.5";
        PlatformUiSupport.storePlatform(ep, updateHelper, javaPlatformName, sourceLevel != null ? new SpecificationVersion(sourceLevel) : null);
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ProjectManager.getDefault().saveProject(p);
        
        ProjectWebModule pwm = (ProjectWebModule) p.getLookup().lookup(ProjectWebModule.class);
        if (pwm != null) //should not be null
            pwm.setContextPath(contextPath);
        
        return h;
    }
    
    public static Set<FileObject> ensureWelcomePage(FileObject webRoot, FileObject dd) throws IOException {
        Set resultSet = new HashSet();
        try {
            WebApp ddRoot = DDProvider.getDefault().getDDRoot(dd);
            WelcomeFileList welcomeFiles = ddRoot.getSingleWelcomeFileList();
            if (welcomeFiles == null) {
                welcomeFiles = (WelcomeFileList) ddRoot.createBean("WelcomeFileList");
                ddRoot.setWelcomeFileList(welcomeFiles);
            }
            if (welcomeFiles.sizeWelcomeFile() == 0) {
                //create default index.jsp
                FileObject indexJSPFo = createIndexJSP(webRoot);
                assert indexJSPFo != null : "webRoot: " + webRoot + ", defaultJSP: index";//NOI18N
                // Returning FileObject of main class, will be called its preferred action
                resultSet.add(indexJSPFo);
                welcomeFiles.addWelcomeFile("index.jsp"); //NOI18N
                ddRoot.write(dd);
            }
        } catch (ClassNotFoundException cnfe) {
            LOGGER.log(Level.SEVERE, cnfe.getLocalizedMessage(), cnfe);
        }
        return resultSet;
    }
    
    private static FileObject createIndexJSP(FileObject webFolder) throws IOException {
        FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/JSP_Servlet/JSP.jsp" ); // NOI18N
        
        if (jspTemplate == null)
            return null; // Don't know the template
        
        DataObject mt = DataObject.find(jspTemplate);
        DataFolder webDf = DataFolder.findFolder(webFolder);
        return mt.createFromTemplate(webDf, "index").getPrimaryFile(); // NOI18N
    }
    
    
    /**
     * Creates a web project from esisting sources.
     *
     * @deprecated Use {@link #importProject(WebProjectCreateData)}
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper importProject(File dir, String name, FileObject wmFO, FileObject javaRoot, FileObject docBase, FileObject libFolder, String j2eeLevel, String serverInstanceID, String buildfile) throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setWebModuleFO(wmFO);
        createData.setSourceFolders(new File[] {FileUtil.toFile(javaRoot)});
        createData.setTestFolders(null);
        createData.setDocBase(docBase);
        createData.setLibFolder(libFolder);
        createData.setJavaEEVersion(j2eeLevel);
        createData.setServerInstanceID(serverInstanceID);
        createData.setBuildfile(buildfile);
        return importProject(createData);
    }
    
    /**
     * Creates a web project from esisting sources.
     *
     * @deprecated Use {@link #importProject(WebProjectCreateData)}
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper importProject(final File dir, String name, FileObject wmFO, final File[] sourceFolders, File[] tstFolders, FileObject docBase, FileObject libFolder, String j2eeLevel, String serverInstanceID, String buildfile) throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setWebModuleFO(wmFO);
        createData.setSourceFolders(sourceFolders);
        createData.setTestFolders(tstFolders);
        createData.setDocBase(docBase);
        createData.setLibFolder(libFolder);
        createData.setJavaEEVersion(j2eeLevel);
        createData.setServerInstanceID(serverInstanceID);
        createData.setBuildfile(buildfile);
        return importProject(createData);
    }
    
    /**
     * Creates a web project from existing sources.
     * @param createData the object encapsulating necessary data to create the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper importProject(final WebProjectCreateData createData) throws IOException {
        final File dir = createData.getProjectDir();
        String name = createData.getName();
        FileObject wmFO = createData.getWebModuleFO();
        final File[] sourceFolders = createData.getSourceFolders();
        File[] tstFolders = createData.getTestFolders();
        FileObject docBase = createData.getDocBase();
        FileObject libFolder = createData.getLibFolder();
        String j2eeLevel = createData.getJavaEEVersion();
        String serverInstanceID = createData.getServerInstanceID();
        String buildfile = createData.getBuildfile();
        String javaPlatformName = createData.getJavaPlatformName();
        String sourceLevel = createData.getSourceLevel();
        boolean javaSourceBased = createData.getJavaSourceBased();
        FileObject webInfFolder = createData.getWebInfFolder();
        
        assert dir != null: "Project folder can't be null"; //NOI18N
        assert name != null: "Project name can't be null"; //NOI18N
        assert wmFO != null: "File object representation of the imported web project location can't be null";   //NOI18N
        assert sourceFolders != null: "Source package root can't be null";   //NOI18N
        assert docBase != null: "Web Pages folder can't be null";   //NOI18N
        assert serverInstanceID != null: "Server instance ID can't be null"; //NOI18N
        assert j2eeLevel != null: "Java EE version can't be null"; //NOI18N
        
        FileObject fo = FileUtil.createFolder(dir);
        
        final AntProjectHelper antProjectHelper = setupProject(fo, name, serverInstanceID, j2eeLevel);
        final WebProject p = (WebProject) ProjectManager.getDefault().findProject(antProjectHelper.getProjectDirectory());
        final ReferenceHelper referenceHelper = p.getReferenceHelper();
        EditableProperties ep = new EditableProperties(true);
        
        if (FileUtil.isParentOf(fo, wmFO) || fo.equals(wmFO)) {
            ep.setProperty(WebProjectProperties.SOURCE_ROOT, "."); //NOI18N
        } else {
            ep.setProperty(WebProjectProperties.SOURCE_ROOT,
                    referenceHelper.createForeignFileReference(FileUtil.toFile(wmFO), null));
        }
        ep.setProperty(WebProjectProperties.WEB_DOCBASE_DIR, createFileReference(referenceHelper, fo, wmFO, docBase));
        
        final File[] testFolders = tstFolders;
        // issue 89278: do not fire file change events under ProjectManager.MUTEX,
        // it is deadlock-prone
        fo.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() {
                try {
                    ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction() {
                        public Object run() throws Exception {
                            Element data = antProjectHelper.getPrimaryConfigurationData(true);
                            Document doc = data.getOwnerDocument();

                            Element sourceRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
                            data.appendChild(sourceRoots);
                            Element testRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                            data.appendChild(testRoots);

                            NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
                            assert nl.getLength() == 1;
                            sourceRoots = (Element) nl.item(0);
                            nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                            assert nl.getLength() == 1;
                            testRoots = (Element) nl.item(0);
                            for (int i=0; i<sourceFolders.length; i++) {
                                String propName = "src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                                String srcReference = referenceHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                                Element root = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                                root.setAttribute("id",propName);   //NOI18N
                                root.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_src.dir")); //NOI18N
                                sourceRoots.appendChild(root);
                                EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                props.put(propName,srcReference);
                                antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                            }

                            if (testFolders == null || testFolders.length == 0) {
                                EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                props.put("test.src.dir", "");
                                antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                            } else {
                                for (int i=0; i<testFolders.length; i++) {
                                    if (!testFolders[i].exists()) {
                                        testFolders[i].mkdirs();
                                    }

                                    String name = testFolders[i].getName();
                                    String propName = "test." + name + ".dir";    //NOI18N
                                    int rootIndex = 1;
                                    EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                    while (props.containsKey(propName)) {
                                        rootIndex++;
                                        propName = "test." + name + rootIndex + ".dir";   //NOI18N
                                    }
                                    String testReference = referenceHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                                    Element root = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                                    root.setAttribute("id",propName);   //NOI18N
                                    root.setAttribute("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_test.src.dir")); //NOI18N
                                    testRoots.appendChild(root);
                                    props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                                    props.put(propName,testReference);
                                    antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                                }
                            }
                            antProjectHelper.putPrimaryConfigurationData(data,true);
                            ProjectManager.getDefault().saveProject(p);
                            return null;
                        }
                    });
                } catch (MutexException me ) {
                    Exceptions.printStackTrace(me);
                }
            }
        });
        
        if (libFolder != null) {
            ep.setProperty(WebProjectProperties.LIBRARIES_DIR, createFileReference(referenceHelper, fo, wmFO, libFolder));
            
            //add libraries from the specified folder in the import wizard
            if (libFolder.isFolder()) {
                FileObject children [] = libFolder.getChildren();
                List libs = new LinkedList();
                for (int i = 0; i < children.length; i++) {
                    if (FileUtil.isArchiveFile(children[i]))
                        libs.add(children[i]);
                }
                FileObject[] libsArray = new FileObject[libs.size()];
                libs.toArray(libsArray);
                WebProjectClassPathExtender classPathExtender = (WebProjectClassPathExtender) p.getLookup().lookup(WebProjectClassPathExtender.class);
                classPathExtender.addArchiveFiles(WebProjectProperties.JAVAC_CLASSPATH, libsArray, ClassPathSupport.TAG_WEB_MODULE_LIBRARIES);
                //do we really need to add the listener? commenting it out
                //libFolder.addFileChangeListener(p);
            }
        }
        
        if (!GeneratedFilesHelper.BUILD_XML_PATH.equals(buildfile)) {
            ep.setProperty(WebProjectProperties.BUILD_FILE, buildfile);
        }
        
        //creates conf.dir property and tries to simply guess it
        //(it would be nice to have a possibily to set this property in the wizard)
        Enumeration ch = FileSearchUtility.getChildrenToDepth(fo, 4, true);
        String confDir = ""; //NOI18N
        while (ch.hasMoreElements()) {
            FileObject f = (FileObject) ch.nextElement();
            if (f.isFolder() && f.getName().equalsIgnoreCase("conf")) { //NOI18N
                confDir = FileUtil.getRelativePath(fo, f);
                break;
            }
        }
        if (confDir.equals("")) { //NOI18N
            // if no conf directory was found, create default directory (#82147)
            fo.createFolder(DEFAULT_CONF_FOLDER);
            ep.setProperty(WebProjectProperties.CONF_DIR, DEFAULT_CONF_FOLDER);
        } else
            ep.setProperty(WebProjectProperties.CONF_DIR, confDir); //NOI18N
        
        //create resource.dir property, by default set to "setup"
        //(it would be nice to have a possibily to set this property in the wizard)
        ep.setProperty(WebProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        
        String webInfDir = createFileReference(referenceHelper, fo, wmFO, webInfFolder);
        ep.setProperty(WebProjectProperties.WEBINF_DIR, webInfDir);
        
        ep.setProperty(WebProjectProperties.JAVA_SOURCE_BASED,javaSourceBased+"");
        
        UpdateHelper updateHelper = ((WebProject) p).getUpdateHelper();
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel != null && (sourceLevel.equals("1.6") || sourceLevel.equals("1.7")))
            sourceLevel = "1.5";
        PlatformUiSupport.storePlatform(ep, updateHelper, javaPlatformName, sourceLevel != null ? new SpecificationVersion(sourceLevel) : null);
        
        // Utils.updateProperties() prevents problems caused by modification of properties in AntProjectHelper
        // (e.g. during createForeignFileReference()) when local copy of properties is concurrently modified
        Utils.updateProperties(antProjectHelper, AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ProjectManager.getDefault().saveProject(p);
        
        return antProjectHelper;
    }
    
    private static String createFileReference(ReferenceHelper refHelper, FileObject projectFO, FileObject sourceprojectFO, FileObject referencedFO) {
        if (FileUtil.isParentOf(projectFO, referencedFO)) {
            return relativePath(projectFO, referencedFO);
        } else if (FileUtil.isParentOf(sourceprojectFO, referencedFO)) {
            String s = relativePath(sourceprojectFO, referencedFO);
            return s.length() > 0 ? SOURCE_ROOT_REF + "/" + s : SOURCE_ROOT_REF; //NOI18N
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }
    
    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent))
            return ""; // NOI18N
        if (!FileUtil.isParentOf(parent, child))
            throw new IllegalArgumentException("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath().substring(parent.getPath().length() + 1);
    }
    
    private static AntProjectHelper setupProject(FileObject dirFO, String name, String serverInstanceID, String j2eeLevel) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, WebProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION)); // NOI18N
        data.appendChild(minant);
        
        Element wmLibs = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-libraries"); //NOI18N
        data.appendChild(wmLibs);
        
        Element addLibs = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-additional-libraries"); //NOI18N
        data.appendChild(addLibs);
        
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // XXX the following just for testing, TBD:
        ep.setProperty(WebProjectProperties.DIST_DIR, "dist"); // NOI18N
        ep.setProperty(WebProjectProperties.DIST_WAR, "${"+WebProjectProperties.DIST_DIR+"}/${" + WebProjectProperties.WAR_NAME + "}"); // NOI18N
        ep.setProperty(WebProjectProperties.DIST_WAR_EAR, "${" + WebProjectProperties.DIST_DIR+"}/${" + WebProjectProperties.WAR_EAR_NAME + "}"); //NOI18N
        
        ep.setProperty(WebProjectProperties.JAVAC_CLASSPATH, ""); // NOI18N
        
        ep.setProperty(WebProjectProperties.JSPCOMPILATION_CLASSPATH, "${jspc.classpath}:${javac.classpath}");
        
        ep.setProperty(WebProjectProperties.J2EE_PLATFORM, j2eeLevel);
        
        ep.setProperty(WebProjectProperties.WAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".war"); // NOI18N
        //XXX the name of the dist.ear.jar file should be different, but now it cannot be since the name is used as a key in module provider mapping
        ep.setProperty(WebProjectProperties.WAR_EAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".war"); // NOI18N
        
        ep.setProperty(WebProjectProperties.WAR_COMPRESS, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, ""); // NOI18N
        
        ep.setProperty(WebProjectProperties.LAUNCH_URL_RELATIVE, ""); // NOI18N
        ep.setProperty(WebProjectProperties.DISPLAY_BROWSER, "true"); // NOI18N
        Deployment deployment = Deployment.getDefault();
        ep.setProperty(WebProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID(serverInstanceID));
        
        ep.setProperty(WebProjectProperties.JAVAC_DEBUG, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVAC_DEPRECATION, "false"); // NOI18N
        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] { // NOI18N
            "# " + NbBundle.getMessage(WebProjectUtilities.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        
        ep.setProperty(WebProjectProperties.JAVAC_TEST_CLASSPATH, new String[] {
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}", // NOI18N
        });
        ep.setProperty(WebProjectProperties.RUN_TEST_CLASSPATH, new String[] {
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty(WebProjectProperties.DEBUG_TEST_CLASSPATH, new String[] {
            "${run.test.classpath}", // NOI18N
        });
        
        ep.setProperty(WebProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(WebProjectProperties.BUILD_TEST_CLASSES_DIR, "${build.dir}/test/classes"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_TEST_RESULTS_DIR, "${build.dir}/test/results"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_WEB_DIR, "${"+WebProjectProperties.BUILD_DIR+"}/web"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_EAR_WEB_DIR, "${"+WebProjectProperties.BUILD_DIR+"}/web"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_GENERATED_DIR, "${"+WebProjectProperties.BUILD_DIR+"}/generated"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_CLASSES_DIR, "${"+WebProjectProperties.BUILD_WEB_DIR+"}/WEB-INF/classes"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_EAR_CLASSES_DIR, "${"+WebProjectProperties.BUILD_EAR_WEB_DIR+"}/WEB-INF/classes"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_WEB_EXCLUDES, "${"+ WebProjectProperties.BUILD_CLASSES_EXCLUDES +"}"); //NOI18N
        ep.setProperty(WebProjectProperties.DIST_JAVADOC_DIR, "${"+WebProjectProperties.DIST_DIR+"}/javadoc"); // NOI18N
        ep.setProperty(WebProjectProperties.NO_DEPENDENCIES, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        // #113297
        ep.setProperty(WebProjectProperties.DEBUG_CLASSPATH, "${build.classes.dir.real}:${"+WebProjectProperties.JAVAC_CLASSPATH+"}:${"+WebProjectProperties.J2EE_PLATFORM_CLASSPATH+"}"); // NOI18N
        
        ep.setProperty("runmain.jvmargs", ""); // NOI18N
        ep.setComment("runmain.jvmargs", new String[] { // NOI18N
            "# " + NbBundle.getMessage(WebProjectUtilities.class, "COMMENT_runmain.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(WebProjectUtilities.class, "COMMENT_runmain.jvmargs_2"), // NOI18N
        }, false);
        
        ep.setProperty(WebProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_ENCODING, "${" + WebProjectProperties.SOURCE_ENCODING + "}"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        
        ep.setProperty(WebProjectProperties.COMPILE_JSPS, "false"); // NOI18N
        
        // use the default encoding
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(WebProjectProperties.SOURCE_ENCODING, enc.name());
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(WebProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        
        // set j2ee.platform.classpath
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        if (!j2eePlatform.getSupportedSpecVersions(J2eeModule.WAR).contains(j2eeLevel)) {
            Logger.getLogger("global").log(Level.WARNING,
                    "J2EE level:" + j2eeLevel + " not supported by server " + Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID) + " for module type WAR"); // NOI18N
        }
        String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
        ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
        
        // set j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH,
                    Utils.toClasspathString(wsClasspath));
        }
        
        // set j2ee.platform.wsimport.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSIMPORT)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSIMPORT);
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSIMPORT_CLASSPATH,
                    Utils.toClasspathString(wsClasspath));
        }
        
        // set j2ee.platform.wsgen.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSGEN)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSGEN);
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSGEN_CLASSPATH,
                    Utils.toClasspathString(wsClasspath));
        }
        
        // set j2ee.platform.jsr109 support
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_JSR109)) {
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_JSR109_SUPPORT,
                    "true"); //NOI18N
        }
        
        // ant deployment support
        File projectFolder = FileUtil.toFile(dirFO);
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, WebProjectProperties.ANT_DEPLOY_BUILD_SCRIPT),
                    J2eeModule.WAR, serverInstanceID);
        } catch (IOException ioe) {
            Logger.getLogger("global").log(Level.INFO, null, ioe);
        }
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        if (deployAntPropsFile != null) {
            ep.setProperty(WebProjectProperties.DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        
        return h;
    }
    
    private static String readResource(InputStream is) throws IOException {
        // read the config from resource first
        StringBuffer sb = new StringBuffer();
        String lineSep = System.getProperty("line.separator");//NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = br.readLine();
        while (line != null) {
            sb.append(line);
            sb.append(lineSep);
            line = br.readLine();
        }
        br.close();
        return sb.toString();
    }
    
}
