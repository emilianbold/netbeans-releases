/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.project.api;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.project.*;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.*;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;

import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.ui.customizer.PlatformUiSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.modules.SpecificationVersion;
import org.w3c.dom.NodeList;

/**
 * Create a fresh WebProject from scratch or by importing and exisitng web module 
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class WebProjectUtilities {
    public static final String SRC_STRUCT_BLUEPRINTS = "BluePrints"; //NOI18N
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

    private WebProjectUtilities() {}
    
    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String serverInstanceID, String sourceStructure, String j2eeLevel, String contextPath)
            throws IOException {
        
        final boolean createBluePrintsStruct = SRC_STRUCT_BLUEPRINTS.equals(sourceStructure);
        final boolean createJakartaStructure = SRC_STRUCT_JAKARTA.equals(sourceStructure);
        
        final FileObject fo = Utils.getValidEmptyDir(dir);
        AntProjectHelper h = setupProject (fo, name, serverInstanceID, j2eeLevel);
        
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
        if (WebModule.J2EE_14_LEVEL.equals(j2eeLevel))
            webXMLContent = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-project/web-2.4.xml").getInputStream ()); //NOI18N
        else if (WebModule.J2EE_13_LEVEL.equals(j2eeLevel))
            webXMLContent = readResource (Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-web-project/web-2.3.xml").getInputStream ()); //NOI18N
        assert webXMLContent != null : "Cannot find web.xml template for J2EE specification level:" + j2eeLevel;
        final String webXmlText = webXMLContent;
        FileSystem fs = webInfFO.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws IOException {
                FileObject webXML = FileUtil.createData(webInfFO, "web.xml");//NOI18N
                FileLock lock = webXML.lock();
                try {
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(webXML.getOutputStream(lock)));
                    bw.write(webXmlText);
                    bw.close();
                }
                finally {
                    lock.releaseLock();
                }
            }
        });
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element sourceRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N

        Element rootSrc = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
        rootSrc.setAttribute ("id",WebProjectProperties.SRC_DIR);   //NOI18N
        rootSrc.setAttribute ("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_src.dir")); //NOI18N
        sourceRoots.appendChild(rootSrc);
        if (createBluePrintsStruct)
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER + "/" + DEFAULT_JAVA_FOLDER); // NOI18N
        else
            ep.setProperty(WebProjectProperties.SRC_DIR, DEFAULT_SRC_FOLDER); // NOI18N            
            
        data.appendChild (sourceRoots);
        Element testRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N

        Element rootTest = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
        rootTest.setAttribute ("id",WebProjectProperties.TEST_SRC_DIR);   //NOI18N
        rootTest.setAttribute ("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_test.src.dir")); //NOI18N
        testRoots.appendChild (rootTest);
        ep.setProperty(WebProjectProperties.TEST_SRC_DIR, DEFAULT_TEST_FOLDER); // NOI18N

        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        ep.put (WebProjectProperties.SOURCE_ROOT, createBluePrintsStruct ? DEFAULT_SRC_FOLDER : "."); //NOI18N
        
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
        ep.setProperty(WebProjectProperties.LIBRARIES_DIR, "${" + WebProjectProperties.WEB_DOCBASE_DIR + "}/WEB-INF/lib"); //NOI18N
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);

        //create default index.jsp
        createIndexJSP(webFO);
        
        ProjectWebModule pwm = (ProjectWebModule) p.getLookup ().lookup (ProjectWebModule.class);
        if (pwm != null) //should not be null
            pwm.setContextPath(contextPath);
        
        return h;
    }

    public static AntProjectHelper importProject(File dir, String name, FileObject wmFO, FileObject javaRoot, FileObject docBase, FileObject libFolder, String j2eeLevel, String serverInstanceID, String buildfile) throws IOException {    
        return importProject(dir, name, wmFO, new File[] {FileUtil.toFile(javaRoot)}, null, docBase, libFolder, j2eeLevel, serverInstanceID, buildfile);
    }
    
    public static AntProjectHelper importProject(final File dir, String name, FileObject wmFO, final File[] sourceFolders, File[] tstFolders, FileObject docBase, FileObject libFolder, String j2eeLevel, String serverInstanceID, String buildfile) throws IOException {
        assert sourceFolders != null: "Source package root can't be null";   //NOI18N
        
        FileObject fo = Utils.getValidDir(dir);
        
        final AntProjectHelper antProjectHelper = setupProject(fo, name, serverInstanceID, j2eeLevel);
        final WebProject p = (WebProject) ProjectManager.getDefault().findProject(antProjectHelper.getProjectDirectory());
        final ReferenceHelper referenceHelper = p.getReferenceHelper();        
        EditableProperties ep = new EditableProperties();
        
        if (FileUtil.isParentOf(fo, wmFO) || fo.equals(wmFO)) {
            ep.setProperty(WebProjectProperties.SOURCE_ROOT, "."); //NOI18N
        } else {
            ep.setProperty(WebProjectProperties.SOURCE_ROOT,
                    referenceHelper.createForeignFileReference(FileUtil.toFile(wmFO), null));
        }
        ep.setProperty(WebProjectProperties.WEB_DOCBASE_DIR, createFileReference(referenceHelper, fo, wmFO, docBase));
        
        final File[] testFolders = tstFolders;
        try {
        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
            public Object run() throws Exception {
                Element data = antProjectHelper.getPrimaryConfigurationData(true);
                Document doc = data.getOwnerDocument();
                
                Element sourceRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
                data.appendChild (sourceRoots);
                Element testRoots = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                data.appendChild (testRoots);
        
                NodeList nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
                assert nl.getLength() == 1;
                sourceRoots = (Element) nl.item(0);
                nl = data.getElementsByTagNameNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                assert nl.getLength() == 1;
                testRoots = (Element) nl.item(0);
                for (int i=0; i<sourceFolders.length; i++) {
                    EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    String propName = "src.dir" + (i == 0 ? "" : Integer.toString (i+1)); //NOI18N
                    String srcReference = referenceHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                    Element root = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                    root.setAttribute ("id",propName);   //NOI18N
                    root.setAttribute ("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_src.dir")); //NOI18N
                    sourceRoots.appendChild(root);
                    props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props.put(propName,srcReference);
                    antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                }

                if (testFolders == null || testFolders.length == 0) {
                    EditableProperties props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props.put("test.src.dir", ""); 
                    antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                }
                else {
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
                        Element root = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                        root.setAttribute ("id",propName);   //NOI18N
                        root.setAttribute ("name",NbBundle.getMessage(WebProjectUtilities.class, "NAME_test.src.dir")); //NOI18N
                        testRoots.appendChild(root);
                        props = antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                        props.put(propName,testReference);
                        antProjectHelper.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    }
                }
                antProjectHelper.putPrimaryConfigurationData(data,true);
                ProjectManager.getDefault().saveProject (p);
                return null;
            }
        });
        } catch (MutexException me ) {
            ErrorManager.getDefault().notify (me);
        }

        if (libFolder != null) {
            ep.setProperty(WebProjectProperties.LIBRARIES_DIR,
            createFileReference(referenceHelper, fo, wmFO, libFolder));
        }
        if (!GeneratedFilesHelper.BUILD_XML_PATH.equals(buildfile)) {
            ep.setProperty(WebProjectProperties.BUILD_FILE, buildfile);
        }
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

    private static String relativePath (FileObject parent, FileObject child) {
        if (child.equals (parent))
            return ""; // NOI18N
        if (!FileUtil.isParentOf (parent, child))
            throw new IllegalArgumentException ("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath ().substring (parent.getPath ().length () + 1);
    }

    private static AntProjectHelper setupProject (FileObject dirFO, String name, String serverInstanceID, String j2eeLevel) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, WebProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        
        Element wmLibs = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "web-module-libraries"); //NOI18N        
        data.appendChild (wmLibs);
        
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
        ep.setProperty(WebProjectProperties.WAR_PACKAGE, "true"); // NOI18N
        ep.setProperty(WebProjectProperties.WAR_CONTENT_ADDITIONAL, ""); // NOI18N
        
        ep.setProperty(WebProjectProperties.LAUNCH_URL_RELATIVE, ""); // NOI18N
        ep.setProperty(WebProjectProperties.DISPLAY_BROWSER, "true"); // NOI18N
        Deployment deployment = Deployment.getDefault ();
        ep.setProperty(WebProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID (serverInstanceID));
            
        ep.setProperty(WebProjectProperties.JAVAC_SOURCE, "${default.javac.source}"); //NOI18N
        ep.setProperty(WebProjectProperties.JAVAC_TARGET, "${default.javac.target}"); //NOI18N
        
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
        ep.setProperty(WebProjectProperties.BUILD_EAR_WEB_DIR, "${"+WebProjectProperties.BUILD_DIR+"}/ear-module"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_GENERATED_DIR, "${"+WebProjectProperties.BUILD_DIR+"}/generated"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_CLASSES_DIR, "${"+WebProjectProperties.BUILD_WEB_DIR+"}/WEB-INF/classes"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_EAR_CLASSES_DIR, "${"+WebProjectProperties.BUILD_EAR_WEB_DIR+"}/WEB-INF/classes"); // NOI18N        
        ep.setProperty(WebProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form"); // NOI18N
        ep.setProperty(WebProjectProperties.BUILD_WEB_EXCLUDES, "${"+ WebProjectProperties.BUILD_CLASSES_EXCLUDES +"}"); //NOI18N
        ep.setProperty(WebProjectProperties.DIST_JAVADOC_DIR, "${"+WebProjectProperties.DIST_DIR+"}/javadoc"); // NOI18N
        ep.setProperty(WebProjectProperties.NO_DEPENDENCIES, "false"); // NOI18N
        ep.setProperty(WebProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        ep.setProperty(WebProjectProperties.DEBUG_CLASSPATH, "${"+WebProjectProperties.JAVAC_CLASSPATH+"}:${"+WebProjectProperties.BUILD_CLASSES_DIR+"}:${"+WebProjectProperties.BUILD_EAR_CLASSES_DIR+"}"); // NOI18N
        
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
        ep.setProperty(WebProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
        ep.setProperty(WebProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N        
        ep.setProperty(WebProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N

        ep.setProperty(WebProjectProperties.COMPILE_JSPS, "false"); // NOI18N        
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(WebProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        
        // set j2ee.platform.classpath
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
        ep.setProperty(WebProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
        
        // set j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(WebServicesConstants.WSCOMPILE)) { 
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(WebServicesConstants.WSCOMPILE);
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH, 
                    Utils.toClasspathString(wsClasspath));
        }
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        
        return h;
    }

    private static Element createLibraryElement(Document doc, String libraryName) {
        Element servletLib = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "library"); //NOI18N
        Element servletLibName = doc.createElementNS (WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, "file"); //NOI18N
        servletLibName.appendChild (doc.createTextNode ("${libs." + libraryName + ".classpath}")); //NOI18N
        servletLib.appendChild (servletLibName);
        return servletLib;
    }

    private static void createIndexJSP(FileObject webFolder) throws IOException {
        FileObject jspTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/JSP_Servlet/JSP.jsp" ); // NOI18N

        if (jspTemplate == null)
            return; // Don't know the template
                
        DataObject mt = DataObject.find(jspTemplate);        
        DataFolder webDf = DataFolder.findFolder(webFolder);        
        mt.createFromTemplate(webDf, "index"); // NOI18N
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
    
    // AB: this method is also called from the enterprise application, so we can't pass UpdateHelper here
    // well, actually we can, but let's not expose too many classes
    public static void setPlatform(final AntProjectHelper helper, final String platformName, final String sourceLevel) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    WebProject project = (WebProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                    UpdateHelper updateHelper = project.getUpdateHelper();
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    PlatformUiSupport.storePlatform(ep, updateHelper, platformName, sourceLevel != null ? new SpecificationVersion(sourceLevel) : null);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(helper.getProjectDirectory()));
                }
                catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        });
    }
}
