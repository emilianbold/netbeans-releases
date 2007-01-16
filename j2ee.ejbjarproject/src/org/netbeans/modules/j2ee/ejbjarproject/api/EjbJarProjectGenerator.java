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

package org.netbeans.modules.j2ee.ejbjarproject.api;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Stack;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.AntDeploymentHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.ejbjarproject.*;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.PlatformUiSupport;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Create a fresh EjbProject from scratch or by importing and exisitng ejb module 
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class EjbJarProjectGenerator {
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_TEST_FOLDER = "test"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_JAVA_FOLDER = "java"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    public static final String MINIMUM_ANT_VERSION = "1.6";
    
    private EjbJarProjectGenerator() {}

    /**
     * Create a new empty EjbJar project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String j2eeLevel, String serverInstanceID) throws IOException {
        FileObject fo = createProjectDir(dir);

        FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
        srcRoot.createFolder(DEFAULT_JAVA_FOLDER); //NOI18N
        FileObject testRoot = fo.createFolder(DEFAULT_TEST_FOLDER);
        FileObject confRoot = srcRoot.createFolder(DEFAULT_DOC_BASE_FOLDER); // NOI18N
        
        //create a default manifest
        FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-j2ee-ejbjarproject/MANIFEST.MF"), confRoot, "MANIFEST"); //NOI18N
        
        AntProjectHelper h = setupProject (fo, name, "src", "test", null, null, null, j2eeLevel, serverInstanceID);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put (EjbJarProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(EjbJarProjectProperties.META_INF, "${"+EjbJarProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
        ep.setProperty(EjbJarProjectProperties.SRC_DIR, "${"+EjbJarProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_JAVA_FOLDER); //NOI18N
        ep.setProperty(EjbJarProjectProperties.META_INF_EXCLUDES, "sun-cmp-mappings.xml"); // NOI18N
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);

        // create ejb-jar.xml
        if (!J2eeModule.JAVA_EE_5.equals(j2eeLevel)) {
            String resource = "org-netbeans-modules-j2ee-ejbjarproject/ejb-jar-2.1.xml";
            FileObject ddFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource(resource), confRoot, "ejb-jar"); //NOI18N
            EjbJar ejbJar = DDProvider.getDefault().getMergedDDRoot(
                    org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(ddFile).getMetadataUnit());
            ejbJar.setDisplayName(name);
            ejbJar.write(ddFile);
        }

        return h;
    }
    
    /**
     * Import project from source or exploded archive
     * @param dir root directory of project
     * @param name name of the project
     * @param sourceFolders Array of folders that hold the projects source
     * or exploded archive
     * @param testFolders folders that hold test code for the project
     * @param configFilesBase Folder that holds the projects config files 
     * like deployment descriptors
     * @param libFolder the libraries associated with the project   
     * @param j2eeLevel spec revision level
     * @param serverInstanceID id of target server
     * @param fromJavaSources flag whether the project is from source or 
     * exploded archive of class files
     * @throws java.io.IOException if something goes wrong
     * @return The AntProjectHelper for the project
     */
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders,
            final File configFilesBase, final File libFolder, final String j2eeLevel, 
            String serverInstanceID, boolean fromJavaSources) throws IOException {
        
        AntProjectHelper retVal = importProject(dir,name,sourceFolders,testFolders,
                configFilesBase,libFolder,j2eeLevel,serverInstanceID);
        EditableProperties subEp = retVal.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        subEp.setProperty(EjbJarProjectProperties.JAVA_SOURCE_BASED,fromJavaSources+""); // NOI18N
        retVal.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,subEp);
        Project subP = ProjectManager.getDefault().findProject(retVal.getProjectDirectory());
        ProjectManager.getDefault().saveProject(subP); 
        return retVal;
    }
    
   public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders, 
            final File configFilesBase, final File libFolder, final String j2eeLevel, String serverInstanceID) throws IOException {
        assert sourceFolders != null && testFolders != null: "Package roots can't be null";   //NOI18N
        final FileObject dirFO = createProjectDir (dir);
        // this constructor creates only java application type
        final AntProjectHelper h = setupProject(dirFO, name, null, null, 
                configFilesBase, (libFolder == null ? null : libFolder), null, j2eeLevel, serverInstanceID);
        final EjbJarProject p = (EjbJarProject) ProjectManager.getDefault().findProject(dirFO);
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
        ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction () {
            public Object run() throws Exception {
                Element data = h.getPrimaryConfigurationData(true);
                Document doc = data.getOwnerDocument();
                NodeList nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");
                assert nl.getLength() == 1;
                Element sourceRoots = (Element) nl.item(0);
                nl = data.getElementsByTagNameNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                assert nl.getLength() == 1;
                Element testRoots = (Element) nl.item(0);
                for (int i=0; i<sourceFolders.length; i++) {
                    String propName = "src.dir" + (i == 0 ? "" : Integer.toString (i+1)); //NOI18N
                    String srcReference = refHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                    Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                    root.setAttribute ("id",propName);   //NOI18N
                    sourceRoots.appendChild(root);
                    EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props.put(propName,srcReference);
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                }

                if (testFolders.length == 0) {
                    String testLoc = NbBundle.getMessage (EjbJarProjectGenerator.class,"TXT_DefaultTestFolderName");
                    File f = new File (dir,testLoc);
                    f.mkdirs();
                    String propName = "test.src.dir";
                    Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                    root.setAttribute ("id",propName);   //NOI18N
                    root.setAttribute ("name",NbBundle.getMessage(EjbJarProjectGenerator.class, "NAME_test.src.dir"));
                    testRoots.appendChild(root);
                    EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    props.put(propName,testLoc);
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                }
                else {
                    for (int i=0; i<testFolders.length; i++) {
                        if (!testFolders[i].exists()) {
                            testFolders[i].mkdirs();
                        }
                        String propName = "test.src.dir" + (i == 0 ? "" : Integer.toString (i+1)); //NOI18N
                        String testReference = refHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                        Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                        root.setAttribute ("id",propName);   //NOI18N
                        testRoots.appendChild(root);
                        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                        props.put(propName,testReference);
                        h.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                    }
                }
                h.putPrimaryConfigurationData(data,true);
                ProjectManager.getDefault().saveProject (p);
                return null;
            }
        });
        } catch (MutexException me ) {
            ErrorManager.getDefault().notify (me);
        }
        // AB: fix for #53170: if j2eeLevel is 1.4 and ejb-jar.xml is version 2.0, we upgrade it to version 2.1
        FileObject ejbJarXml = FileUtil.toFileObject(configFilesBase).getFileObject("ejb-jar.xml"); // NOI18N
        if (ejbJarXml != null) {
            try {
                EjbJar root = DDProvider.getDefault().getMergedDDRoot(
                        org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJar(ejbJarXml).getMetadataUnit());
                boolean writeDD = false;
                if (new BigDecimal(EjbJar.VERSION_2_0).equals(root.getVersion()) && j2eeLevel.equals(EjbJarProjectProperties.J2EE_1_4)) { // NOI18N
                    root.setVersion(new BigDecimal(EjbJar.VERSION_2_1));
                    writeDD = true;
                }
                // also set the display name if not set (#55733)
                String dispName = root.getDefaultDisplayName();
                if (null == dispName || dispName.trim().length() == 0) {
                    root.setDisplayName(name);
                    writeDD = true;
                }
                if (writeDD)
                    root.write(ejbJarXml);
            }
            catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
        return h;
    }

    private static String createFileReference(ReferenceHelper refHelper, FileObject projectFO, FileObject referencedFO) {
        if (FileUtil.isParentOf(projectFO, referencedFO)) {
            return relativePath(projectFO, referencedFO);
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }
    
    private static String relativePath (FileObject parent, FileObject child) {
        if (child.equals (parent))
            return "";
        if (!FileUtil.isParentOf (parent, child))
            throw new IllegalArgumentException ("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath ().substring (parent.getPath ().length () + 1);
    }
    
    private static AntProjectHelper setupProject (FileObject dirFO, String name, 
            String srcRoot, String testRoot, File configFiles, File libraries, String resources, 
            String j2eeLevel, String serverInstanceID) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, EjbJarProjectType.TYPE);
        final EjbJarProject prj = (EjbJarProject) ProjectManager.getDefault().findProject(h.getProjectDirectory());
        final ReferenceHelper referenceHelper = prj.getReferenceHelper();
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION));
        data.appendChild(minant);

        // TODO: ma154696: not sure if needed
//        Element addLibs = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "ejb-module-additional-libraries"); //NOI18N
//        data.appendChild(addLibs);
                
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","src.dir");   //NOI18N
            root.setAttribute ("name",NbBundle.getMessage(EjbJarProjectGenerator.class, "NAME_src.dir"));
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild (sourceRoots);
        Element testRoots = doc.createElementNS(EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS (EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute ("id","test.src.dir");   //NOI18N
            root.setAttribute ("name",NbBundle.getMessage(EjbJarProjectGenerator.class, "NAME_test.src.dir"));
            testRoots.appendChild (root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild (testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        if (resources != null) {
            ep.setProperty(EjbJarProjectProperties.RESOURCE_DIR, resources);
        } else {
            ep.setProperty(EjbJarProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        }
        
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = v.toString();
        // #89131: these levels are not actually distinct from 1.5.
        if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7"))
            sourceLevel = "1.5";       
        ep.setProperty(EjbJarProjectProperties.JAVAC_SOURCE, sourceLevel); //NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVAC_TARGET, sourceLevel); //NOI18N

        ep.setProperty(EjbJarProjectProperties.JAVAC_CLASSPATH, "");
        
        ep.setProperty(EjbJarProjectProperties.DIST_DIR, "dist");
        ep.setProperty(EjbJarProjectProperties.DIST_JAR, "${"+EjbJarProjectProperties.DIST_DIR+"}/" + "${" + EjbJarProjectProperties.JAR_NAME + "}");
        //XXX the name of the dist.ear.jar file should be different, but now it cannot be since the name is used as a key in module provider mapping
        ep.setProperty(EjbJarProjectProperties.DIST_EAR_JAR, "${"+EjbJarProjectProperties.DIST_DIR+"}/" + "${" + EjbJarProjectProperties.JAR_NAME + "}");
        ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM, j2eeLevel);
        ep.setProperty(EjbJarProjectProperties.JAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".jar");
        ep.setProperty(EjbJarProjectProperties.JAR_COMPRESS, "false");
//        ep.setProperty(EjbJarProjectProperties.JAR_CONTENT_ADDITIONAL, "");
        
        Deployment deployment = Deployment.getDefault ();
        ep.setProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID (serverInstanceID));
        ep.setProperty(EjbJarProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(EjbJarProjectProperties.JAVAC_DEPRECATION, "false");
        
        ep.setProperty(EjbJarProjectProperties.JAVAC_TEST_CLASSPATH, new String[] {
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}", // NOI18N
        });
        ep.setProperty(EjbJarProjectProperties.RUN_TEST_CLASSPATH, new String[] {
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty(EjbJarProjectProperties.DEBUG_TEST_CLASSPATH, new String[] {
            "${run.test.classpath}", // NOI18N
        });
        
        ep.setProperty(EjbJarProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(EjbJarProjectProperties.BUILD_TEST_CLASSES_DIR, "${build.dir}/test/classes"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.BUILD_TEST_RESULTS_DIR, "${build.dir}/test/results"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.BUILD_GENERATED_DIR, "${"+EjbJarProjectProperties.BUILD_DIR+"}/generated");
        ep.setProperty(EjbJarProjectProperties.BUILD_CLASSES_DIR, "${"+EjbJarProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(EjbJarProjectProperties.BUILD_EAR_CLASSES_DIR, "${"+EjbJarProjectProperties.BUILD_DIR+"}/ear-module");
        ep.setProperty(EjbJarProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(EjbJarProjectProperties.DIST_JAVADOC_DIR, "${"+EjbJarProjectProperties.DIST_DIR+"}/javadoc");
        ep.setProperty(EjbJarProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(EjbJarProjectProperties.DEBUG_CLASSPATH, "${"+EjbJarProjectProperties.JAVAC_CLASSPATH+"}:${"+EjbJarProjectProperties.BUILD_CLASSES_DIR+"}");
        ep.setProperty(EjbJarProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
        ep.setProperty(EjbJarProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N        
        ep.setProperty(EjbJarProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N        

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        if (configFiles != null) {
            String ref = createFileReference(referenceHelper, dirFO, FileUtil.toFileObject(configFiles));
            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty(EjbJarProjectProperties.META_INF, ref);
            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }
        if (libraries != null) {
            String ref = createFileReference(referenceHelper, dirFO, FileUtil.toFileObject(libraries));
            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty(EjbJarProjectProperties.LIBRARIES_DIR, ref);
            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
        }
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
        
        // set j2ee.platform.classpath
        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        if (!j2eePlatform.getSupportedSpecVersions(J2eeModule.EJB).contains(j2eeLevel)) {
            ErrorManager.getDefault().log(ErrorManager.WARNING, "J2EE level:" + j2eeLevel + " not supported by server " + Deployment.getDefault().getServerInstanceDisplayName(serverInstanceID) + " for module type EJB");
        }
        String classpath = Utils.toClasspathString(j2eePlatform.getClasspathEntries());
        ep.setProperty(EjbJarProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
        
        // set j2ee.platform.wscompile.classpath
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_WSCOMPILE)) { 
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_WSCOMPILE);
            ep.setProperty(WebServicesConstants.J2EE_PLATFORM_WSCOMPILE_CLASSPATH, 
                    Utils.toClasspathString(wsClasspath));
        }
        
        // ant deployment support
        File projectFolder = FileUtil.toFile(dirFO);
        try {
            AntDeploymentHelper.writeDeploymentScript(new File(projectFolder, EjbJarProjectProperties.ANT_DEPLOY_BUILD_SCRIPT),
                    J2eeModule.EJB, serverInstanceID);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ioe);
        }
        File deployAntPropsFile = AntDeploymentHelper.getDeploymentPropertiesFile(serverInstanceID);
        if (deployAntPropsFile != null) {
            ep.setProperty(EjbJarProjectProperties.DEPLOY_ANT_PROPS_FILE, deployAntPropsFile.getAbsolutePath());
        }
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }

    private static FileObject createProjectDir (File dir) throws IOException {
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

    private static void refreshFileSystem (final File dir) throws FileStateInvalidException {
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject dirFO = FileUtil.toFileObject(rootF);
        assert dirFO != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        dirFO.getFileSystem().refresh(false);
    }

    // AB: this method is also called from the enterprise application, so we can't pass UpdateHelper here
    // well, actually we can, but let's not expose too many classes
    public static void setPlatform(final AntProjectHelper helper, final String platformName, final String sourceLevel) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    EjbJarProject project = (EjbJarProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                    UpdateHelper updateHelper = project.getUpdateHelper();
                    EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    String finalPlatformName = platformName;
                    if (finalPlatformName == null) 
                        finalPlatformName = JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
                    
                    // #89131: these levels are not actually distinct from 1.5.
                    String srcLevel = sourceLevel;
                    if (sourceLevel.equals("1.6") || sourceLevel.equals("1.7"))
                        srcLevel = "1.5";       
                    PlatformUiSupport.storePlatform(ep, updateHelper, finalPlatformName, srcLevel != null ? new SpecificationVersion(srcLevel) : null);
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
