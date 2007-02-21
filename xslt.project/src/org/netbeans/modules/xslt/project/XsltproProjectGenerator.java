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
package org.netbeans.modules.xslt.project;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
//import org.netbeans.modules.bpel.project.ui.customizer.IcanproProjectProperties;
import static org.netbeans.modules.xslt.project.XsltproConstants.*;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class XsltproProjectGenerator {

    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_BPELASA_FOLDER = "bpelasa"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    private static final String DEFAULT_NBPROJECT_DIR = "nbproject"; // NOI18N
    
    private XsltproProjectGenerator() {
    }
    
    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String j2eeLevel) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject (rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject (dir);
        assert fo != null : "No such dir on disk: " + dir;
        assert fo.isFolder() : "Not really a dir: " + dir;
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir;
        AntProjectHelper h = setupProject (fo, name, j2eeLevel);
        FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
// Bing bpelasa        FileObject bpelasaRoot = srcRoot.createFolder(DEFAULT_BPELASA_FOLDER); //NOI18N
        
// TODO m
        FileObject bpelasaRoot = srcRoot;
        FileObject xsltmapFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-xsltpro/xsltmap.xml"), bpelasaRoot, "xsltmap"); //NOI18N

// TODO a
        FileObject nbProjectRoot = FileUtil.toFileObject(new File(dir, DEFAULT_NBPROJECT_DIR)); // NOI18N
        FileObject genPortmap = Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-xsltpro/genPortmap.xsl");
//        System.out.println("genPortmap: "+genPortmap);
        if (genPortmap != null) {
            FileObject genPortmapFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-xsltpro/genPortmap.xsl"), nbProjectRoot, "genPortmap"); //NOI18N
        }
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put (SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(META_INF, "${"+SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
// Bing bpelasa       ep.setProperty(SRC_DIR, "${"+SOURCE_ROOT+"}/"+DEFAULT_BPELASA_FOLDER); //NOI18N
        ep.setProperty(SRC_DIR, "${"+SOURCE_ROOT+"}"); //NOI18N
        ep.setProperty(RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);

        return h;
    }
    
    public static AntProjectHelper importProject (File dir, String name, FileObject wmFO, FileObject javaRoot, FileObject configFilesBase, String j2eeLevel, String buildfile) throws IOException {
        dir.mkdirs();
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        // XXX add code to set meta inf directory  (meta-inf and java src)
        FileObject fo = FileUtil.toFileObject (rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject (dir);
        assert fo != null : "No such dir on disk: " + dir;
        assert fo.isFolder() : "Not really a dir: " + dir;
        AntProjectHelper h = setupProject (fo, name, j2eeLevel);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (FileUtil.isParentOf (fo, wmFO) || fo.equals (wmFO)) {
            ep.put (SOURCE_ROOT, "."); //NOI18N
            ep.setProperty(SRC_DIR, relativePath (fo, javaRoot)); //NOI18N
            ep.setProperty(META_INF, relativePath (fo, configFilesBase)); //NOI18N
        } else {
            File wmRoot = FileUtil.toFile (wmFO);
            ep.put (SOURCE_ROOT, wmRoot.getAbsolutePath ());
            String configFilesPath = relativePath (wmFO, configFilesBase);
            configFilesPath = configFilesPath.length () > 0 ? "${"+SOURCE_ROOT+"}/" + configFilesPath : "${"+SOURCE_ROOT+"}"; //NOI18N
            String javaPath = relativePath (wmFO, javaRoot);
            javaPath = javaPath.length () > 0 ? "${"+SOURCE_ROOT+"}/" + javaPath : "${"+SOURCE_ROOT+"}"; //NOI18N
            ep.setProperty(SRC_DIR, javaPath);
            ep.setProperty(META_INF, configFilesPath);
        }
        if (! GeneratedFilesHelper.BUILD_XML_PATH.equals (buildfile)) {
            ep.setProperty (BUILD_FILE, buildfile);
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);
        
        return h;
    }
    
    private static String relativePath (FileObject parent, FileObject child) {
        if (child.equals (parent))
            return "";
        if (!FileUtil.isParentOf (parent, child))
            throw new IllegalArgumentException ("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath ().substring (parent.getPath ().length () + 1);
    }
    
    private static AntProjectHelper setupProject (FileObject dirFO, String name, String j2eeLevel) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, XsltproProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(XsltproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(XsltproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // ep.setProperty(JAVAC_CLASSPATH, "${libs.j2ee14.classpath}");
        ep.setProperty(DIST_DIR, "dist");
        ep.setProperty(DIST_JAR, "${"+DIST_DIR+"}/" + name + ".zip");
        ep.setProperty(J2EE_PLATFORM, j2eeLevel);
        ep.setProperty(JAR_NAME, name + ".jar");
        ep.setProperty(JAR_COMPRESS, "false");
//        ep.setProperty(JAR_CONTENT_ADDITIONAL, "");
        
        Deployment deployment = Deployment.getDefault ();
        String serverInstanceID = deployment.getDefaultServerInstanceID ();
        ep.setProperty(J2EE_SERVER_TYPE, deployment.getServerID (serverInstanceID));
        ep.setProperty(JAVAC_SOURCE, "1.4");
        ep.setProperty(JAVAC_DEBUG, "true");
        ep.setProperty(JAVAC_DEPRECATION, "false");
// todo r
        ep.setProperty(VALIDATION_FLAG, "false");
        
        ep.setProperty(JAVAC_TARGET, "1.4");

        
        ep.setProperty(BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(BUILD_GENERATED_DIR, "${"+BUILD_DIR+"}/generated");
        ep.setProperty(BUILD_CLASSES_DIR, "${"+BUILD_DIR+"}/jar");
        ep.setProperty(BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(DIST_JAVADOC_DIR, "${"+DIST_DIR+"}/javadoc");
        ep.setProperty(JAVA_PLATFORM, "default_platform");
        ep.setProperty(DEBUG_CLASSPATH, "${"+JAVAC_CLASSPATH+"}:${"+BUILD_CLASSES_DIR+"}");

        //============= Start of IcanPro========================================//
        ep.setProperty(JBI_SETYPE_PREFIX, "com.sun.xsltse"); // NOI18N
        ep.setProperty(ASSEMBLY_UNIT_ALIAS, "This Assembly Unit"); // NOI18N
        ep.setProperty(ASSEMBLY_UNIT_DESCRIPTION, "Represents this Assembly Unit"); // NOI18N
        ep.setProperty(APPLICATION_SUB_ASSEMBLY_ALIAS, "This Application Sub-Assembly"); // NOI18N
        ep.setProperty(APPLICATION_SUB_ASSEMBLY_DESCRIPTION, "This represents the Application Sub-Assembly"); // NOI18N
        ep.setProperty(JBI_COMPONENT_CONF_ROOT, "nbproject/private"); // NOI18N
        ep.setProperty(JBI_DEPLOYMENT_CONF_ROOT, "nbproject/deployment"); // NOI18N

        ep.setProperty(BC_DEPLOYMENT_JAR, "${"+BUILD_DIR+"}/" + "BCDeployment.jar");
        ep.setProperty(SE_DEPLOYMENT_JAR, "${"+BUILD_DIR+"}/" + "SEDeployment.jar");
        //============= End of IcanPro========================================//

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        ep.setProperty(J2EE_SERVER_INSTANCE, serverInstanceID);
        //============= Start of IcanPro========================================//
        ep.setProperty(JBI_COMPONENT_CONF_FILE, "ComponentInformation.xml"); // NOI18N
        ep.setProperty(JBI_DEPLOYMENT_CONF_FILE, "default.xml"); // NOI18N

        ep.setProperty(NETBEANS_HOME, System.getProperty(NETBEANS_HOME)); // NOI18N
        //============= End of IcanPro========================================//

        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }


}
