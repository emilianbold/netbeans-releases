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
import java.nio.charset.Charset;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import static org.netbeans.modules.xslt.project.XsltproConstants.*;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.NbBundle;
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
    public static AntProjectHelper createProject(File dir, final String name) throws IOException {
        final FileObject fo = createProjectDir(dir);
//        dir.mkdirs();
//        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
//        File rootF = dir;
//        while (rootF.getParentFile() != null) {
//            rootF = rootF.getParentFile();
//        }
//        final FileObject fo = FileUtil.toFileObject (rootF);
//        assert fo != null : "At least disk roots must be mounted! " + rootF;
//        fo.getFileSystem().refresh(false);
//        final FileObject fo = FileUtil.toFileObject (dir);

        // vlv # 113228
        if (fo == null) {
          throw new IOException("Can't create " + dir.getName());
        }
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir;

        final AntProjectHelper[] h = new AntProjectHelper[1];
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    h[0] = setupProject(fo, name);
                    FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
            // Bing bpelasa        FileObject bpelasaRoot = srcRoot.createFolder(DEFAULT_BPELASA_FOLDER); //NOI18N

            // TODO m
                    FileObject bpelasaRoot = srcRoot;
                    FileObject transformmapFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-xsltpro/transformmap.xml"), bpelasaRoot, "transformmap"); //NOI18N
            // TODO r
            //        FileObject nbProjectRoot = FileUtil.toFileObject(new File(dir, DEFAULT_NBPROJECT_DIR)); // NOI18N
            //        FileObject genPortmap = Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-xsltpro/genPortmap.xsl");
            //        System.out.println("genPortmap: "+genPortmap);
            //        if (genPortmap != null) {
            //            FileObject genPortmapFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-xsltpro/genPortmap.xsl"), nbProjectRoot, "genPortmap"); //NOI18N
            //        }

                    EditableProperties ep = h[0].getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep.put (IcanproProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
                    ep.setProperty(IcanproProjectProperties.META_INF, "${"+IcanproProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
            // Bing bpelasa       ep.setProperty(SRC_DIR, "${"+SOURCE_ROOT+"}/"+DEFAULT_BPELASA_FOLDER); //NOI18N
                    ep.setProperty(IcanproProjectProperties.SRC_DIR, "${"+IcanproProjectProperties.SOURCE_ROOT+"}"); //NOI18N
                    ep.setProperty(IcanproProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
                    h[0].putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

                    Project p = ProjectManager.getDefault().findProject(h[0].getProjectDirectory ());
                    ProjectManager.getDefault().saveProject(p);
                } catch(IOException ex) {
                    ioe[0] = ex;
                    return;
                }
            }
        });

        if (ioe[0] != null) {
            throw ioe[0];
        }

        return h[0];
    }
    
    
    private static FileObject createProjectDir(File dir) throws IOException {
        FileObject dirFO;
        if(!dir.exists()) {
            //Refresh before mkdir not to depend on window focus
            refreshFileSystem (dir);
            dir.mkdirs();
            refreshFileSystem (dir);
        }
        dirFO = FileUtil.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + dir; // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
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

        // vlv # 113228
        if (fo == null) {
          throw new IOException("Can't create " + dir.getName());
        }
        assert fo.isFolder() : "Not really a dir: " + dir;
        AntProjectHelper h = setupProject (fo, name);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        if (FileUtil.isParentOf (fo, wmFO) || fo.equals (wmFO)) {
            ep.put (IcanproProjectProperties.SOURCE_ROOT, "."); //NOI18N
            ep.setProperty(IcanproProjectProperties.SRC_DIR, relativePath (fo, javaRoot)); //NOI18N
            ep.setProperty(IcanproProjectProperties.META_INF, relativePath (fo, configFilesBase)); //NOI18N
        } else {
            File wmRoot = FileUtil.toFile (wmFO);
            ep.put (IcanproProjectProperties.SOURCE_ROOT, wmRoot.getAbsolutePath ());
            String configFilesPath = relativePath (wmFO, configFilesBase);
            configFilesPath = configFilesPath.length () > 0 
                    ? "${"+IcanproProjectProperties.SOURCE_ROOT+"}/" + configFilesPath // NOI18N 
                    : "${"+IcanproProjectProperties.SOURCE_ROOT+"}"; //NOI18N
            String javaPath = relativePath (wmFO, javaRoot);
            javaPath = javaPath.length () > 0 
                    ? "${"+IcanproProjectProperties.SOURCE_ROOT+"}/" + javaPath // NOI18N
                    : "${"+IcanproProjectProperties.SOURCE_ROOT+"}"; //NOI18N
            ep.setProperty(IcanproProjectProperties.SRC_DIR, javaPath);
            ep.setProperty(IcanproProjectProperties.META_INF, configFilesPath);
        }
        if (! GeneratedFilesHelper.BUILD_XML_PATH.equals (buildfile)) {
            ep.setProperty (IcanproProjectProperties.BUILD_FILE, buildfile);
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);
        
        return h;
    }
    
    private static String relativePath (FileObject parent, FileObject child) {
        if (child.equals (parent))
            return EMPTY_STRING; 
        if (!FileUtil.isParentOf (parent, child))
            throw new IllegalArgumentException ("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath ().substring (parent.getPath ().length () + 1);
    }
    
    private static AntProjectHelper setupProject (FileObject dirFO, String name) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO,
                XsltproProjectType.TYPE);
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
        ep.setProperty(IcanproProjectProperties.DIST_DIR, "dist"); // NOI18N
        ep.setProperty(IcanproProjectProperties.DIST_JAR,
                "${" + IcanproProjectProperties.DIST_DIR + "}/" + name + ".zip"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JAR_NAME, name + ".jar");
        ep.setProperty(IcanproProjectProperties.JAR_COMPRESS, "false");
//        ep.setProperty(JAR_CONTENT_ADDITIONAL, "");
        Deployment deployment = Deployment.getDefault();
        String serverInstanceID = deployment.getDefaultServerInstanceID();
        ep.setProperty(IcanproProjectProperties.JAVAC_SOURCE, "1.4");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEPRECATION, "false");
// todo r
        ep.setProperty(VALIDATION_FLAG, "false");

        ep.setProperty(IcanproProjectProperties.JAVAC_TARGET, "1.4");


        ep.setProperty(IcanproProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(IcanproProjectProperties.BUILD_GENERATED_DIR,
                "${" + IcanproProjectProperties.BUILD_DIR + "}/generated"); // NOI18N
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_DIR,
                "${" + IcanproProjectProperties.BUILD_DIR + "}/jar"); // NOI18N
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs"); // NOI18N
        ep.setProperty(IcanproProjectProperties.DIST_JAVADOC_DIR,
                "${" + IcanproProjectProperties.DIST_DIR + "}/javadoc"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        ep.setProperty(IcanproProjectProperties.DEBUG_CLASSPATH,
                "${" + IcanproProjectProperties.JAVAC_CLASSPATH + "}:${" + IcanproProjectProperties.BUILD_CLASSES_DIR + "}"); // NOI18N
        ep.setProperty(IcanproProjectProperties.WSDL_CLASSPATH, "");
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(IcanproProjectProperties.SOURCE_ENCODING, enc.name());
        
        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_SE_TYPE, "sun-xslt-engine"); // NOI18N
        ep.setProperty(IcanproProjectProperties.SERVICE_UNIT_DESCRIPTION, 
                NbBundle.getMessage(XsltproProjectGenerator.class, "TXT_Service_Unit_Description")); // NOI18N
        
        // todo r
        ep.setProperty("jbi.se.type", "sun-bpel-engine"); // NOI18N
        ep.setProperty("jbi.service-unit.description", 
                NbBundle.getMessage(XsltproProjectGenerator.class, "TXT_Service_Unit_Description")); // NOI18N

        
        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_ROOT, "nbproject/private"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_ROOT, "nbproject/deployment"); // NOI18N
        ep.setProperty(IcanproProjectProperties.BC_DEPLOYMENT_JAR,
                "${" + IcanproProjectProperties.BUILD_DIR + "}/" + "BCDeployment.jar"); // NOI18N
        ep.setProperty(IcanproProjectProperties.SE_DEPLOYMENT_JAR,
                "${" + IcanproProjectProperties.BUILD_DIR + "}/" + "SEDeployment.jar"); // NOI18N
        //============= End of IcanPro========================================//
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_FILE, "ComponentInformation.xml"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_FILE, "default.xml"); // NOI18N
        //============= End of IcanPro========================================//
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }


}
