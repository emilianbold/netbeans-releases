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

import org.netbeans.modules.compapp.projects.jbi.api.JbiDefaultComponentInfo;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;

import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.compapp.projects.jbi.ComponentInfoGenerator;
import org.netbeans.modules.compapp.jbiserver.JbiManager;
import org.netbeans.modules.compapp.projects.jbi.api.JbiProjectConstants;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentDocument;


/**
 * Create a fresh EjbProject from scratch or by importing and exisitng web module  in one of the
 * recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class JbiProjectGenerator {
    private static final String DEFAULT_CONF_FOLDER = "conf"; // NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; // NOI18N
    private static final String DEFAULT_JBIASA_FOLDER = JbiProjectConstants.FOLDER_JBIASA;
    private static final String DEFAULT_COMPONENTASA_FOLDER = "jbiServiceUnits"; // NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; // NOI18N
    private static final String DEFAULT_JBI_ROUTING = "true"; // NOI18N
    private static final String DEFAULT_JBI_SA_INTERNAL_ROUTING = "true"; // NOI18N
    // Start Test Framework
    private static final String DEFAULT_TEST_FOLDER = "test"; // NOI18N
    private static final String DEFAULT_TEST_RESULTS_FOLDER = "${basedir}/test/results"; // NOI18N
    // End Test Framework

    private JbiProjectGenerator() {
    }

    /**
     * Create a new empty project.
     *
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @param j2eeLevel DOCUMENT ME!
     *
     * @return the helper object permitting it to be further customized
     *
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name, String j2eeLevel)
        throws IOException {
        dir.mkdirs();

        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;

        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }

        FileObject fo = FileUtil.toFileObject(rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject(dir);

        // vlv # 113228
        if (fo == null) {
          throw new IOException("Can't create " + dir.getName());
        }
        assert fo.isFolder() : "Not really a dir: " + dir; // NOI18N
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir; // NOI18N

        AntProjectHelper h = setupProject(fo, name, j2eeLevel);
        
        FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N  
        srcRoot.createFolder(DEFAULT_JBIASA_FOLDER); 
                      
        FileObject confRoot = srcRoot.createFolder(DEFAULT_CONF_FOLDER); 
        
        // Start Test Framework
        fo.createFolder(DEFAULT_TEST_FOLDER); 
        // End Test Framework
        
        // Create default component info config files
        // 04/12/06, NB FO returns different path format for Unix and Windoz..
        String confDir = FileUtil.toFile(confRoot).getPath();
        new ComponentInfoGenerator(confDir).doIt();
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(JbiProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); 
        ep.setProperty(
            JbiProjectProperties.META_INF,
            "${" + JbiProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_CONF_FOLDER // NOI18N
        ); 
        ep.setProperty(
            JbiProjectProperties.SRC_DIR,
            "${" + JbiProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_JBIASA_FOLDER // NOI18N
        ); 
        ep.setProperty(
            JbiProjectProperties.SRC_BUILD_DIR, 
            "${" + JbiProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_COMPONENTASA_FOLDER // NOI18N
        ); 
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(JbiProjectProperties.SOURCE_ENCODING, enc.name());
        ep.setProperty(JbiProjectProperties.JBI_ROUTING, DEFAULT_JBI_ROUTING);
        ep.setProperty(JbiProjectProperties.JBI_SA_INTERNAL_ROUTING, DEFAULT_JBI_SA_INTERNAL_ROUTING);
        // Start Test Framework
        ep.setProperty(JbiProjectProperties.TEST_DIR, DEFAULT_TEST_FOLDER);
        ep.setProperty(JbiProjectProperties.TEST_RESULTS_DIR, DEFAULT_TEST_RESULTS_FOLDER);
        // End Test Framework
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);

        CasaHelper.createDefaultCasaFileObject((JbiProject)p);
        
        return h;
    }

    /**
     * DOCUMENT ME!
     *
     * @param dir DOCUMENT ME!
     * @param name DOCUMENT ME!
     * @param wmFO DOCUMENT ME!
     * @param javaRoot DOCUMENT ME!
     * @param configFilesBase DOCUMENT ME!
     * @param j2eeLevel DOCUMENT ME!
     * @param buildfile DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    public static AntProjectHelper importProject(
        File dir, String name, FileObject wmFO, FileObject javaRoot, FileObject configFilesBase,
        String j2eeLevel, String buildfile
    ) throws IOException {
        dir.mkdirs();

        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;

        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }

        // XXX add code to set meta inf directory  (meta-inf and java src)
        FileObject fo = FileUtil.toFileObject(rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF; // NOI18N
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject(dir);

        // vlv # 113228
        if (fo == null) {
          throw new IOException("Can't create " + dir.getName());
        }
        assert fo.isFolder() : "Not really a dir: " + dir; // NOI18N

        AntProjectHelper h = setupProject(fo, name, j2eeLevel);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        if (FileUtil.isParentOf(fo, wmFO) || fo.equals(wmFO)) {
            ep.put(JbiProjectProperties.SOURCE_ROOT, "."); // NOI18N
            ep.setProperty(JbiProjectProperties.SRC_DIR, relativePath(fo, javaRoot)); 
            ep.setProperty(JbiProjectProperties.META_INF, relativePath(fo, configFilesBase)); 
        } else {
            File wmRoot = FileUtil.toFile(wmFO);
            ep.put(JbiProjectProperties.SOURCE_ROOT, wmRoot.getAbsolutePath());

            String configFilesPath = relativePath(wmFO, configFilesBase);
            configFilesPath = (configFilesPath.length() > 0)
                ? ("${" + JbiProjectProperties.SOURCE_ROOT + "}/" + configFilesPath) // NOI18N
                : ("${" + JbiProjectProperties.SOURCE_ROOT + "}"); // NOI18N 

            String javaPath = relativePath(wmFO, javaRoot);
            javaPath = (javaPath.length() > 0)
                ? ("${" + JbiProjectProperties.SOURCE_ROOT + "}/" + javaPath) // NOI18N
                : ("${" + JbiProjectProperties.SOURCE_ROOT + "}"); // NOI18N
            ep.setProperty(JbiProjectProperties.SRC_DIR, javaPath);
            ep.setProperty(JbiProjectProperties.META_INF, configFilesPath);
        }

        if (!GeneratedFilesHelper.BUILD_XML_PATH.equals(buildfile)) {
            ep.setProperty(JbiProjectProperties.BUILD_FILE, buildfile);
        }

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);

        return h;
    }

    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent)) {
            return ""; // NOI18N
        }

        if (!FileUtil.isParentOf(parent, child)) {
            throw new IllegalArgumentException(
                NbBundle.getMessage(JbiProjectGenerator.class, "MSG_Cannot_find_relative_path", parent, child) // NOI18N
            );
        }

        return child.getPath().substring(parent.getPath().length() + 1);
    }

    private static AntProjectHelper setupProject(FileObject dirFO, String name, String j2eeLevel)
        throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, JbiProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(
                JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name" // NOI18N
            ); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);

        Element minant = doc.createElementNS(
                JbiProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version" // NOI18N
            ); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        // ep.setProperty(JbiProjectProperties.JAVAC_CLASSPATH, "${libs.j2ee14.classpath}"); 
        ep.setProperty(JbiProjectProperties.DIST_DIR, "dist"); // NOI18N
        ep.setProperty(
            JbiProjectProperties.DIST_JAR,
            "${" + JbiProjectProperties.DIST_DIR + "}/" + name + ".zip" // NOI18N
        );
        ep.setProperty(JbiProjectProperties.J2EE_PLATFORM, j2eeLevel);
        //ep.setProperty(JbiProjectProperties.JAR_NAME, name + ".jar"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAR_COMPRESS, "false"); // NOI18N

        //        ep.setProperty(JbiProjectProperties.JAR_CONTENT_ADDITIONAL, "");
        Deployment deployment = Deployment.getDefault();
//        String serverInstanceID = deployment.getDefaultServerInstanceID();
//        ep.setProperty(
//            JbiProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID(serverInstanceID)
//        );       
                
        ep.setProperty(JbiProjectProperties.JAVAC_SOURCE, "1.4"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVAC_DEBUG, "true"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVAC_DEPRECATION, "false"); // NOI18N

        ep.setProperty(JbiProjectProperties.JAVAC_TARGET, "1.4"); // NOI18N

        ep.setProperty(JbiProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(
            JbiProjectProperties.BUILD_GENERATED_DIR,
            "${" + JbiProjectProperties.BUILD_DIR + "}/generated" // NOI18N
        );
        ep.setProperty(
            JbiProjectProperties.BUILD_CLASSES_DIR, "${" + JbiProjectProperties.BUILD_DIR + "}/jar" // NOI18N
        );
        ep.setProperty(
            JbiProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs" // NOI18N
        );
        ep.setProperty(
            JbiProjectProperties.DIST_JAVADOC_DIR,
            "${" + JbiProjectProperties.DIST_DIR + "}/javadoc" // NOI18N
        );
        ep.setProperty(JbiProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        ep.setProperty(
            JbiProjectProperties.DEBUG_CLASSPATH,
            "${" + JbiProjectProperties.JAVAC_CLASSPATH + "}:${" + // NOI18N
            JbiProjectProperties.BUILD_CLASSES_DIR + "}" // NOI18N 
        );
        ep.setProperty(JbiProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_ENCODING, ""); // NOI18N
        ep.setProperty(JbiProjectProperties.JAVADOC_PREVIEW, "true"); // NOI18N

        //============= Start of JBI ========================================//
        ep.setProperty(JbiProjectProperties.SERVICE_ASSEMBLY_ID, name); 
        ep.setProperty(
            JbiProjectProperties.SERVICE_ASSEMBLY_DESCRIPTION, 
            "Represents the Service Assembly of " + name); // NOI18N
        ep.setProperty(
            JbiProjectProperties.SERVICE_UNIT_DESCRIPTION,
            "Represents this Service Unit"); // NOI18N
        //============= End of JBI ========================================//
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        
        String[] serverInstanceIDs = JbiManager.getAppServers();   
        if (serverInstanceIDs != null && serverInstanceIDs.length == 1) {
            ep.setProperty(JbiProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceIDs[0]);
        }      

        //============= Start of JBI ========================================//
        ep.setProperty(JbiProjectProperties.JBI_COMPONENT_CONF_FILE, "ComponentInformation.xml"); // NOI18N


        //============= End of JBI ========================================//
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

        Project p = ProjectManager.getDefault().findProject(dirFO);
        ProjectManager.getDefault().saveProject(p);

        return h;
    }
}
