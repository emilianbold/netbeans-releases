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

package org.netbeans.modules.j2ee.clientproject.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Stack;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.j2ee.clientproject.AppClientProvider;
import org.netbeans.modules.j2ee.clientproject.UpdateHelper;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.PlatformUiSupport;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Creates a AppClientProject from scratch according to some initial configuration.
 */
public class AppClientProjectGenerator {
    
    private static final String DEFAULT_CONF_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_TEST_FOLDER = "test"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_JAVA_FOLDER = "java"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    public static final String MINIMUM_ANT_VERSION = "1.6.5"; // NOI18N
    
    private static final String MANIFEST_FILE = "MANIFEST.MF"; // NOI18N
    
    private AppClientProjectGenerator() {}
    
    /**
     * Create a new Application client project.
     *
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @param mainClass the name for the main class
     * @param j2eeLevel defined in <code>org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule<code>
     * @param serverInstanceID provided by j2eeserver module
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String mainClass, String j2eeLevel, String serverInstanceID) throws IOException {
        FileObject fo = createProjectDir(dir);
        
        FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER);
        FileObject javaRoot = srcRoot.createFolder(DEFAULT_JAVA_FOLDER);
        FileObject confRoot = srcRoot.createFolder(DEFAULT_CONF_FOLDER);
        fo.createFolder(DEFAULT_TEST_FOLDER);
        
        // create application-client.xml
        String resource = (J2eeModule.JAVA_EE_5.equals(j2eeLevel)
                ? "org-netbeans-modules-j2ee-clientproject/application-client-5.xml" // NOI18N
                : "org-netbeans-modules-j2ee-clientproject/application-client-1.4.xml"); // NOI18N
        FileObject ddFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource(resource), confRoot, "application-client"); //NOI18N
        AppClient appClient = DDProvider.getDefault().getDDRoot(ddFile);
        appClient.setDisplayName(name);
        appClient.write(ddFile);
        
        AntProjectHelper h = createProject(fo, name, DEFAULT_SRC_FOLDER, DEFAULT_TEST_FOLDER,
                null, null, null,mainClass, j2eeLevel, serverInstanceID);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(AppClientProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(AppClientProjectProperties.META_INF, "${"+AppClientProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_CONF_FOLDER); //NOI18N
        ep.setProperty(AppClientProjectProperties.SRC_DIR, "${"+AppClientProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_JAVA_FOLDER); //NOI18N
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(fo);
        ProjectManager.getDefault().saveProject(p);
        
        if ( mainClass != null ) {
            createMainClass( mainClass, javaRoot );
        }
        
        createManifest(confRoot, MANIFEST_FILE);
        
        return h;
    }
    
    /**
     * Imports an existing Application client project into NetBeans project.
     *
     * @param dir the top-level directory (need not yet exist but if it does it must be empty) - "nbproject" location
     * @param name the name for the project
     * @param sourceFolders top-level location(s) of java sources - must not be null
     * @param testFolders top-level location(s) of test(s) - must not be null
     * @param confFolder top-level location of configuration file(s) folder - must not be null
     * @param libFolder top-level location of libraries
     * @param j2eeLevel defined in <code>org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule<code>
     * @param serverInstanceID provided by j2eeserver module
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders, final File confFolder, final File libFolder, String j2eeLevel, String serverInstanceID) throws IOException {
        assert sourceFolders != null && testFolders != null: "Package roots can't be null";   //NOI18N
        final FileObject dirFO = createProjectDir(dir);
        final AntProjectHelper h = createProject(dirFO, name, null, null,
                confFolder.getAbsolutePath(), (libFolder == null ? null : libFolder.getAbsolutePath()),
                null, null, j2eeLevel, serverInstanceID);
        final AppClientProject p = (AppClientProject) ProjectManager.getDefault().findProject(dirFO);
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
            ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction() {
                public Object run() throws Exception {
                    Element data = h.getPrimaryConfigurationData(true);
                    Document doc = data.getOwnerDocument();
                    NodeList nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots"); // NOI18N
                    assert nl.getLength() == 1;
                    Element sourceRoots = (Element) nl.item(0);
                    nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                    assert nl.getLength() == 1;
                    Element testRoots = (Element) nl.item(0);
                    for (int i=0; i<sourceFolders.length; i++) {
                        String propName = "src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                        String srcReference = refHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                        Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                        root.setAttribute("id",propName);   //NOI18N
                        sourceRoots.appendChild(root);
                        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put(propName,srcReference);
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    }
                    
                    if (testFolders.length == 0) {
                        String testLoc = NbBundle.getMessage(AppClientProjectGenerator.class,"TXT_DefaultTestFolderName");
                        File f = new File(dir,testLoc);
                        f.mkdirs();
                        String propName = "test.src.dir"; // NOI18N
                        Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                        root.setAttribute("id",propName);   //NOI18N
                        root.setAttribute("name",NbBundle.getMessage(AppClientProjectGenerator.class, "NAME_test.src.dir"));
                        testRoots.appendChild(root);
                        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put(propName,testLoc);
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    } else {
                        for (int i=0; i<testFolders.length; i++) {
                            if (!testFolders[i].exists()) {
                                testFolders[i].mkdirs();
                            }
                            String propName = "test.src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                            String testReference = refHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                            Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                            root.setAttribute("id",propName);   //NOI18N
                            testRoots.appendChild(root);
                            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                            props.put(propName,testReference);
                            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        }
                    }
                    h.putPrimaryConfigurationData(data,true);
                    ProjectManager.getDefault().saveProject(p);
                    return null;
                }
            });
        } catch (MutexException me ) {
            ErrorManager.getDefault().notify(me);
        }
        // AB: fix for #53170: if j2eeLevel is 1.4 and application-client.xml is version 1.3, we upgrade it to version 1.4
        FileObject confFolderFO = FileUtil.toFileObject(confFolder);
        FileObject appClientXML = confFolderFO == null ? null
                : confFolderFO.getFileObject(AppClientProvider.FILE_DD);
        if (appClientXML != null) {
            try {
                //AppClient root = DDProvider.getDefault().getDDRoot(Car.getCar(appClientXML));
                AppClient root = DDProvider.getDefault().getDDRoot(appClientXML);
                boolean writeDD = false;
                if (new BigDecimal(AppClient.VERSION_1_3).equals(root.getVersion()) && J2eeModule.J2EE_14.equals(j2eeLevel)) { // NOI18N
                    root.setVersion(new BigDecimal(AppClient.VERSION_1_4));
                    writeDD = true;
                }
                // also set the display name if not set (#55733)
                String dispName = root.getDefaultDisplayName();
                if (null == dispName || dispName.trim().length() == 0) {
                    root.setDisplayName(name);
                    writeDD = true;
                }
                if (writeDD) {
                    root.write(appClientXML);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        } else {
            // XXX just temporary, since now the import would fail due to another bug
            String resource = (J2eeModule.JAVA_EE_5.equals(j2eeLevel)
                    ? "org-netbeans-modules-j2ee-clientproject/application-client-5.xml" // NOI18N
                    : "org-netbeans-modules-j2ee-clientproject/application-client-1.4.xml"); // NOI18N
            FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource(resource),
                    confFolderFO, "application-client"); //NOI18N
        }
        createManifest(confFolderFO, MANIFEST_FILE);
        return h;
    }
    
    /**
     * Imports an existing Application client project into NetBeans project 
     * with a flag to specify whether the project contains java source files 
     * or was created from an exploded archive.
     * @return the helper object permitting it to be further customized
     * @param fromJavaSource indicate whether the project is "from" source or an exploded archive    
     * @param dir the top-level directory (need not yet exist but if it does it must be empty) - "nbproject" location
     * @param name the name for the project
     * @param sourceFolders top-level location(s) of java sources - must not be null
     * @param testFolders top-level location(s) of test(s) - must not be null
     * @param confFolder top-level location of configuration file(s) folder - must not be null
     * @param libFolder top-level location of libraries
     * @param j2eeLevel defined in <code>org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule<code>
     * @param serverInstanceID provided by j2eeserver module
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders, final File confFolder, 
            final File libFolder, String j2eeLevel, String serverInstanceID,boolean fromJavaSource) throws IOException {
        AntProjectHelper h = importProject(dir,name,sourceFolders,testFolders,
                confFolder,libFolder,j2eeLevel,serverInstanceID);
        EditableProperties subEp = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        subEp.setProperty(AppClientProjectProperties.JAVA_SOURCE_BASED,fromJavaSource+""); // NOI18N
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,subEp);
        Project subP = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(subP);
        return h;    
    }
    private static AntProjectHelper createProject(FileObject dirFO, String name,
            String srcRoot, String testRoot, String configFiles, String libraries,
            String resources, String mainClass, String j2eeLevel,
            String serverInstanceID) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, AppClientProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION)); // NOI18N
        data.appendChild(minant);
        
        //TODO: ma154696: not sure if needed
//        Element addLibs = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "ejb-module-additional-libraries"); //NOI18N
//        data.appendChild(addLibs);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute("id","src.dir");   //NOI18N
            root.setAttribute("name",NbBundle.getMessage(AppClientProjectGenerator.class, "NAME_src.dir"));
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild(sourceRoots);
        Element testRoots = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute("id","test.src.dir");   //NOI18N
            root.setAttribute("name",NbBundle.getMessage(AppClientProjectGenerator.class, "NAME_test.src.dir"));
            testRoots.appendChild(root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild(testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        if (configFiles != null) {
            ep.setProperty(AppClientProjectProperties.META_INF, configFiles);
        }
        if (libraries != null) {
            ep.setProperty(AppClientProjectProperties.LIBRARIES_DIR, libraries);
        }
        
        if (resources != null) {
            ep.setProperty(AppClientProjectProperties.RESOURCE_DIR, resources);
        } else {
            ep.setProperty(AppClientProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        }
        
        //XXX the name of the dist.ear.jar file should be different, but now it cannot be since the name is used as a key in module provider mapping
        ep.setProperty(AppClientProjectProperties.DIST_EAR_JAR, "${"+AppClientProjectProperties.DIST_DIR+"}/" + "${" + AppClientProjectProperties.JAR_NAME + "}"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty(AppClientProjectProperties.BUILD_EAR_CLASSES_DIR, "${"+AppClientProjectProperties.BUILD_DIR+"}/ear-module"); // NOI18N
        
        ep.setProperty("dist.dir", "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
//        ep.setProperty("dist.jar", "${dist.dir}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty(AppClientProjectProperties.DIST_JAR, "${"+AppClientProjectProperties.DIST_DIR+"}/" + "${" + AppClientProjectProperties.JAR_NAME + "}"); // NOI18N
        ep.setProperty("javac.classpath", new String[0]); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            "${run.classpath}", // NOI18N
        });
        ep.setProperty("jar.compress", "false"); // NOI18N
        if (mainClass != null) {
            ep.setProperty("main.class", mainClass); // NOI18N
        }
        
        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] { // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = v.toString();
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7"))
            sourceLevel = "1.5";       
        ep.setProperty(AppClientProjectProperties.JAVAC_SOURCE, sourceLevel); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVAC_TARGET, sourceLevel); // NOI18N
        
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty("javac.test.classpath", new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}", // NOI18N
        });
        ep.setProperty("run.test.classpath", new String[] { // NOI18N
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
            "${run.test.classpath}", // NOI18N
        });
        
        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
        //ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N
        
        ep.setProperty("build.dir", DEFAULT_BUILD_DIR); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        //ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/jar"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        
        ep.setProperty("run.jvmargs", ""); // NOI18N
        ep.setComment("run.jvmargs", new String[] { // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);
        
        ep.setProperty(AppClientProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        
        Deployment deployment = Deployment.getDefault();
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        if (!j2eePlatform.getSupportedSpecVersions(J2eeModule.CLIENT).contains(j2eeLevel)) {
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    NbBundle.getMessage(AppClientProjectGenerator.class, "MSG_Warning_SpecLevelNotSupported",  // NOI18N
                    new Object[] {j2eeLevel, Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID)}));
        }
        ep.setProperty(AppClientProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID(serverInstanceID));
        ep.setProperty(AppClientProjectProperties.J2EE_PLATFORM, j2eeLevel);
        ep.setProperty("manifest.file", "${" +AppClientProjectProperties.META_INF + "}/" + MANIFEST_FILE); // NOI18N

        String mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS);
        if (mainClassArgs != null && !mainClassArgs.equals("")) {
            ep.put(AppClientProjectProperties.APPCLIENT_MAINCLASS_ARGS, mainClassArgs);
        } else if ((mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, AppClientProjectProperties.CLIENT_NAME)) 
                        != null) {
            ep.put(AppClientProjectProperties.CLIENT_NAME, mainClassArgs);
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        //private.properties
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        // set j2ee.appclient environment
        File[] accrt = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME);
        ep.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_RUNTIME, Utils.toClasspathString(accrt));
        String acMain = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS);
        if (acMain != null) {
            ep.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_MAINCLASS, acMain);
        }
        String jvmOpts = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_JVM_OPTS);
        if (jvmOpts != null) {
            ep.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_JVMOPTS, jvmOpts);
        }
        String args = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME,
                AppClientProjectProperties.J2EE_PLATFORM_APPCLIENT_ARGS);
        if (args != null) {
            ep.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_ARGS, args);
        }
        
        ep.setProperty(AppClientProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        // set j2ee.platform.classpath
        String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
        ep.setProperty(AppClientProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
        // set j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
            ep.setProperty(WebServicesClientConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH,
                    Utils.toClasspathString(wsClasspath));
        }
        
        //WORKAROUND for --retrieve option in asadmin deploy command
        //works only for local domains
        //see also http://www.netbeans.org/issues/show_bug.cgi?id=82929
        if ("J2EE".equals(deployment.getServerID(serverInstanceID))) { // NOI18N
            File asRoot = j2eePlatform.getPlatformRoots()[0];
            File exFile = new File(asRoot, "lib/javaee.jar"); // NOI18N
            InstanceProperties ip = InstanceProperties.getInstanceProperties(serverInstanceID);
            if (exFile.exists()) {
                ep.setProperty("wa.copy.client.jar.from", // NOI18N
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/generated/xml/j2ee-modules").getAbsolutePath()); // NOI18N
            } else {
                ep.setProperty("wa.copy.client.jar.from", // NOI18N
                        new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/applications/j2ee-modules").getAbsolutePath()); // NOI18N
            }
        } else {
            ep.remove("wa.copy.client.jar.from"); // NOI18N
        }
        
        // ant deployment support
        File projectFolder = FileUtil.toFile(dirFO);
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, AppClientProjectProperties.ANT_DEPLOY_BUILD_SCRIPT),
                    J2eeModule.CLIENT, serverInstanceID);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        if (deployAntPropsFile != null) {
            ep.setProperty(AppClientProjectProperties.DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        
        return h;
    }
    
    private static FileObject createProjectDir(File dir) throws IOException {
        Stack stack = new Stack ();
        while (!dir.exists()) {
            stack.push (dir.getName());
            dir = dir.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject (dir);
        if (dirFO == null) {
            refreshFileSystem(dir);
            dirFO = FileUtil.toFileObject (dir);
        }
        assert dirFO != null;
        while (!stack.isEmpty()) {
            dirFO = dirFO.createFolder((String)stack.pop());
        }
        return dirFO;
    }
    
    private static void createMainClass( String mainClassName, FileObject srcFolder ) throws IOException {
        
        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        } else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }
        
        if ( mName.length() == 0 ) {
            return;
        }
        
        FileObject mainTemplate = Repository.getDefault().getDefaultFileSystem().findResource( "Templates/Classes/Main.java" ); // NOI18N
        
        if ( mainTemplate == null ) {
            return; // Don't know the template
        }
        
        DataObject mt = DataObject.find( mainTemplate );
        
        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );
        mt.createFromTemplate( pDf, mName );
        
    }
    
    
    private static void refreshFileSystem(final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }
    
    /**
     * Set J2SE platform to be used.
     *
     * @param helper "reference" to project to be updated
     * @param platformName the name of the J2SE platform
     * @param sourceLevel the source level to be set
     */
    // AB: this method is also called from the enterprise application, so we can't pass UpdateHelper here
    // well, actually we can, but let's not expose too many classes
    public static void setPlatform(final AntProjectHelper helper, final String platformName, final String sourceLevel) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    AppClientProject project = (AppClientProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                    UpdateHelper updateHelper = project.getUpdateHelper();
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    String finalPlatformName = platformName;
                    if (finalPlatformName == null) {
                        finalPlatformName = JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
                    }
                    
                    // #89131: these levels are not actually distinct from 1.5.
                    String srcLevel = sourceLevel;
                    if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7"))
                        srcLevel = "1.5";       
                    PlatformUiSupport.storePlatform(ep, updateHelper, finalPlatformName, srcLevel != null ? new SpecificationVersion(srcLevel) : null);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(helper.getProjectDirectory()));
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        });
    }
    
    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @param path the relative path of the file
     * @throws IOException in case of problems
     */
    private static void createManifest(FileObject dir, String path) throws IOException {
        if (dir.getFileObject(path) == null) {
            FileObject manifest = FileUtil.createData(dir, path);
            FileLock lock = manifest.lock();
            try {
                OutputStream os = manifest.getOutputStream(lock);
                try {
                    PrintWriter pw = new PrintWriter(os);
                    pw.println("Manifest-Version: 1.0"); // NOI18N
                    pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                    pw.println(); // safest to end in \n\n due to JRE parsing bug
                    pw.flush();
                } finally {
                    os.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
    }
    
}


