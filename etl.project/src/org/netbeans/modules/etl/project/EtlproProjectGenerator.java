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

package org.netbeans.modules.etl.project;

import java.io.File;
import java.io.IOException;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create a fresh EjbProject from scratch or by importing and exisitng web module 
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class EtlproProjectGenerator {
    
    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_BPELASA_FOLDER = "bpelasa"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    private static final String DEFAULT_NBPROJECT_DIR = "nbproject"; //NOI18N
    
    private EtlproProjectGenerator() {}

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

        // vlv # 113228
        if (fo == null) {
          throw new IOException("Can't create " + dir.getName());
        }
        assert fo.isFolder() : "Not really a dir: " + dir;
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir;
        AntProjectHelper h = setupProject (fo, name, j2eeLevel);
        fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N

//        FileObject nbProjectRoot = FileUtil.toFileObject(new File(dir, DEFAULT_NBPROJECT_DIR)); // NOI18N
//        FileObject genPortmapFile = FileUtil.copyFile(Repository.getDefault().getDefaultFileSystem().findResource("org-netbeans-modules-etl-project/genPortmap.xsl"), nbProjectRoot, "genPortmap"); //NOI18N
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put (IcanproProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.META_INF, "${"+IcanproProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.SRC_DIR, "${"+IcanproProjectProperties.SOURCE_ROOT+"}"); //NOI18N
        ep.setProperty(IcanproProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory ());
        ProjectManager.getDefault().saveProject(p);

        return h;
    }


    private static AntProjectHelper setupProject (FileObject dirFO, String name, String j2eeLevel) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, EtlproProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(EtlproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(EtlproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // ep.setProperty(IcanproProjectProperties.JAVAC_CLASSPATH, "${libs.j2ee14.classpath}");
        ep.setProperty(IcanproProjectProperties.DIST_DIR, "dist");
        ep.setProperty(IcanproProjectProperties.DIST_JAR, "${"+IcanproProjectProperties.DIST_DIR+"}/" + name + ".zip");
        ep.setProperty(IcanproProjectProperties.J2EE_PLATFORM, j2eeLevel);
        ep.setProperty(IcanproProjectProperties.JAR_NAME, name + ".jar");
        ep.setProperty(IcanproProjectProperties.JAR_COMPRESS, "false");
        
//        Deployment deployment = Deployment.getDefault ();
//        String serverInstanceID = deployment.getDefaultServerInstanceID ();
//        ep.setProperty(IcanproProjectProperties.J2EE_SERVER_TYPE, deployment.getServerID (serverInstanceID));
        ep.setProperty(IcanproProjectProperties.JAVAC_SOURCE, "1.4");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEPRECATION, "false");
        
        ep.setProperty(IcanproProjectProperties.JAVAC_TARGET, "1.4");
        
        ep.setProperty(IcanproProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(IcanproProjectProperties.BUILD_GENERATED_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/generated");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(IcanproProjectProperties.DIST_JAVADOC_DIR, "${"+IcanproProjectProperties.DIST_DIR+"}/javadoc");
        ep.setProperty(IcanproProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(IcanproProjectProperties.DEBUG_CLASSPATH, "${"+IcanproProjectProperties.JAVAC_CLASSPATH+"}:${"+IcanproProjectProperties.BUILD_CLASSES_DIR+"}");

        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_SETYPE_PREFIX, "sun-etl-engine"); // NOI18N
        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_ALIAS, "This Assembly Unit"); // NOI18N
        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_DESCRIPTION, "Represents this Assembly Unit"); // NOI18N
        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_ALIAS, "This Application Sub-Assembly"); // NOI18N
        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION, "This represents the Application Sub-Assembly"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_ROOT, "nbproject/private"); // NOI18N
        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_ROOT, "nbproject/deployment"); // NOI18N

        ep.setProperty(IcanproProjectProperties.BC_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "BCDeployment.jar");
        ep.setProperty(IcanproProjectProperties.SE_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "SEDeployment.jar");
        //============= End of IcanPro========================================//

        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        //ep.setProperty(IcanproProjectProperties.J2EE_SERVER_INSTANCE, serverInstanceID);
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
