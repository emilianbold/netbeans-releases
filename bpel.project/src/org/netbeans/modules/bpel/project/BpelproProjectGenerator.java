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
package org.netbeans.modules.bpel.project;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import org.openide.ErrorManager;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create a fresh EjbProject from scratch or by importing and exisitng web module
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class BpelproProjectGenerator {

    private static final String DEFAULT_DOC_BASE_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_BPELASA_FOLDER = "bpelasa"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N

    private BpelproProjectGenerator() {}

    /**
     * Create a new empty J2SE project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File dir, String name) throws IOException {
        dir.mkdirs();
        File rootF = dir;

        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject(rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject(dir);
        assert fo != null : "No such dir on disk: " + dir;
        assert fo.isFolder() : "Not really a dir: " + dir;
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir;
        AntProjectHelper h = setupProject(fo, name);
        FileObject srcRoot = fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N
        FileObject bpelasaRoot = srcRoot;

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(IcanproProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.META_INF, "${"+IcanproProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_DOC_BASE_FOLDER); //NOI18N
        ep.setProperty(IcanproProjectProperties.SRC_DIR, "${"+IcanproProjectProperties.SOURCE_ROOT+"}"); //NOI18N
        ep.setProperty(IcanproProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);

        createCatalogXml(h.getProjectDirectory());

        return h;
    }

    // vlv # 111020
    private static void createCatalogXml(FileObject project) {
      try {
        FileObject resource = Repository.getDefault().getDefaultFileSystem().findResource("bpel-project-resources/catalog.xml");
        FileUtil.copyFile(resource, project, "catalog", "xml"); // NOI18N
      }
      catch (IOException e) {
      }
    }

    public static AntProjectHelper importProject(File dir, String name, FileObject wmFO, FileObject javaRoot, FileObject configFilesBase, String j2eeLevel, String buildfile) throws IOException {
        dir.mkdirs();
        File rootF = dir;

        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject(rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        fo = FileUtil.toFileObject(dir);
        assert fo != null : "No such dir on disk: " + dir;
        assert fo.isFolder() : "Not really a dir: " + dir;
        AntProjectHelper h = setupProject(fo, name);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);

        if (FileUtil.isParentOf(fo, wmFO) || fo.equals(wmFO)) {
            ep.put(IcanproProjectProperties.SOURCE_ROOT, "."); //NOI18N
            ep.setProperty(IcanproProjectProperties.SRC_DIR, relativePath(fo, javaRoot)); //NOI18N
            ep.setProperty(IcanproProjectProperties.META_INF, relativePath(fo, configFilesBase)); //NOI18N
        } else {
            File wmRoot = FileUtil.toFile(wmFO);
            ep.put(IcanproProjectProperties.SOURCE_ROOT, wmRoot.getAbsolutePath());
            String configFilesPath = relativePath(wmFO, configFilesBase);
            configFilesPath = configFilesPath.length() > 0 ? "${"+IcanproProjectProperties.SOURCE_ROOT+"}/" + configFilesPath : "${"+IcanproProjectProperties.SOURCE_ROOT+"}"; //NOI18N
            String javaPath = relativePath(wmFO, javaRoot);
            javaPath = javaPath.length() > 0 ? "${"+IcanproProjectProperties.SOURCE_ROOT+"}/" + javaPath : "${"+IcanproProjectProperties.SOURCE_ROOT+"}"; //NOI18N
            ep.setProperty(IcanproProjectProperties.SRC_DIR, javaPath);
            ep.setProperty(IcanproProjectProperties.META_INF, configFilesPath);
        }
        if (! GeneratedFilesHelper.BUILD_XML_PATH.equals(buildfile)) {
            ep.setProperty(IcanproProjectProperties.BUILD_FILE, buildfile);
        }
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);

        return h;
    }

    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent))
            return "";
        if (!FileUtil.isParentOf(parent, child))
            throw new IllegalArgumentException("Cannot find relative path, " + parent + " is not parent of " + child);
        return child.getPath().substring(parent.getPath().length() + 1);
    }

    private static AntProjectHelper setupProject(FileObject dirFO, String name) throws IOException {
        AntProjectHelper h = ProjectGenerator.createProject(dirFO, BpelproProjectType.TYPE);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(BpelproProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6")); // NOI18N
        data.appendChild(minant);
        h.putPrimaryConfigurationData(data, true);

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(IcanproProjectProperties.DIST_DIR, "dist");
        ep.setProperty(IcanproProjectProperties.DIST_JAR, "${"+IcanproProjectProperties.DIST_DIR+"}/" + name + ".zip");
        ep.setProperty(IcanproProjectProperties.JAR_NAME, name + ".jar");
        ep.setProperty(IcanproProjectProperties.JAR_COMPRESS, "false");

        Deployment deployment = Deployment.getDefault();
        ep.setProperty(IcanproProjectProperties.JAVAC_SOURCE, "1.4");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEBUG, "true");
        ep.setProperty(IcanproProjectProperties.JAVAC_DEPRECATION, "false");
        ep.setProperty(ProjectConstants.VALIDATION_FLAG, "false");
        ep.setProperty(IcanproProjectProperties.JAVAC_TARGET, "1.4");

        ep.setProperty(IcanproProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(IcanproProjectProperties.BUILD_GENERATED_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/generated");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_DIR, "${"+IcanproProjectProperties.BUILD_DIR+"}/jar");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(IcanproProjectProperties.DIST_JAVADOC_DIR, "${"+IcanproProjectProperties.DIST_DIR+"}/javadoc");
        ep.setProperty(IcanproProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(IcanproProjectProperties.DEBUG_CLASSPATH, "${"+IcanproProjectProperties.JAVAC_CLASSPATH+"}:${"+IcanproProjectProperties.BUILD_CLASSES_DIR+"}");
        ep.setProperty(IcanproProjectProperties.WSDL_CLASSPATH, "");
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        ep.setProperty(IcanproProjectProperties.SOURCE_ENCODING, enc.name());

        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_SE_TYPE, "sun-bpel-engine"); // NOI18N
        ep.setProperty(IcanproProjectProperties.SERVICE_UNIT_DESCRIPTION, 
                NbBundle.getMessage(BpelproProjectGenerator.class, "TXT_Service_Unit_Description")); // NOI18N

        // vlv # 109451 todo r
        ep.setProperty("jbi.se.type", "sun-bpel-engine"); // NOI18N
        ep.setProperty("jbi.service-unit.description", 
                NbBundle.getMessage(BpelproProjectGenerator.class, "TXT_Service_Unit_Description")); // NOI18N

        ep.setProperty(IcanproProjectProperties.BC_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "BCDeployment.jar");
        ep.setProperty(IcanproProjectProperties.SE_DEPLOYMENT_JAR, "${"+IcanproProjectProperties.BUILD_DIR+"}/" + "SEDeployment.jar");
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
