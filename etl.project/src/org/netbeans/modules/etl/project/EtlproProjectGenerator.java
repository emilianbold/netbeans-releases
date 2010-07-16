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

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;

import org.netbeans.modules.compapp.projects.base.ui.customizer.IcanproProjectProperties;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.dm.virtual.db.model.DBExplorerUtil;
import org.netbeans.modules.etl.ui.ETLEditorSupport;
//import org.netbeans.modules.sql.framework.common.utils.DBExplorerUtil;
import org.netbeans.modules.sql.framework.common.utils.MigrationUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Utilities;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Create a fresh EjbProject from scratch or by importing and exisitng web module 
 * in one of the recognized directory structures.
 *
 * @author Pavel Buzek
 */
public class EtlproProjectGenerator {

    private static transient final Logger mLogger = Logger.getLogger(EtlproProjectGenerator.class.getName());
    //Trimming the initial spaces
    private static final String DEFAULT_DOC_BASE_FOLDER = "conf";//NOI18N
    public static final String DEFAULT_SRC_FOLDER = "Collaborations";//NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup";//NOI18N
    private static final String DEFAULT_BPELASA_FOLDER = "bpelasa"; //NOI18N

    private static final String DEFAULT_BUILD_DIR = "build";//NOI18N
    public static final String DEFAULT_DATA_DIR = "data";//NOI18N
    private static final String DEFAULT_DB_DIR = "Default"; //NOI18N

    public static final String DEFAULT_DATABASES_DIR = "databases"; //NOI18N

    private static final String DEFAULT_NBPROJECT_DIR = "nbproject";//NOI18N
    private static final String DEFAULT_FLATFILE_JDBC_URL_PREFIX = "jdbc:axiondb:";
    private static FileObject dbObj = null;
    private static File databases = null;
    private static FileObject data = null;
    public static String PRJ_LOCATION_DIR = "";
    private static String prjName = null;
    private static String fs = File.separator;

    private EtlproProjectGenerator() {
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
        prjName = name;
        // XXX clumsy way to refresh, but otherwise it doesn't work for new folders
        File rootF = dir;
        PRJ_LOCATION_DIR = rootF.getPath();
        while (rootF.getParentFile() != null) {
            rootF = rootF.getParentFile();
        }
        FileObject fo = FileUtil.toFileObject(rootF);
        assert fo != null : "At least disk roots must be mounted! " + rootF;
        fo.getFileSystem().refresh(false);
        //fo = FileUtil.toFileObject(dir);
        fo = FileUtil.toFileObject(new File(dir, ""));

        // vlv # 113228
        if (fo == null) {
            throw new IOException("Can't create " + dir.getName());
        }
        assert fo.isFolder() : "Not really a dir: " + dir;
        assert fo.getChildren().length == 0 : "Dir must have been empty: " + dir;
        AntProjectHelper h = setupProject(fo, name, j2eeLevel);
        fo.createFolder(DEFAULT_SRC_FOLDER); // NOI18N

        data = fo.createFolder(DEFAULT_DATA_DIR); // NOI18N         

        databases = new File(PRJ_LOCATION_DIR + fs + DEFAULT_NBPROJECT_DIR + fs + "private" + fs + DEFAULT_DATABASES_DIR);
        dbObj = FileUtil.createFolder(databases);
        FileObject defaultFileObj = dbObj.createFolder(DEFAULT_DB_DIR);
        //dbObj.lock();  
        ETLEditorSupport.setProjectInfo(name, PRJ_LOCATION_DIR, true);


        String dbName = FileUtil.toFile(defaultFileObj).getAbsolutePath();
        createDefaultDatabase(dbName);
        setMigrationUtil();

        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(IcanproProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N

        ep.setProperty(IcanproProjectProperties.META_INF, "${" + IcanproProjectProperties.SOURCE_ROOT + "}/" + DEFAULT_DOC_BASE_FOLDER); //NOI18N

        ep.setProperty(IcanproProjectProperties.SRC_DIR, "${" + IcanproProjectProperties.SOURCE_ROOT + "}"); //NOI18N

        ep.setProperty(IcanproProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);

        Project p = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(p);

        return h;
    }

    private static AntProjectHelper setupProject(FileObject dirFO, String name, String j2eeLevel) throws IOException {
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
        ep.setProperty(IcanproProjectProperties.DIST_JAR, "${" + IcanproProjectProperties.DIST_DIR + "}/" + name + ".zip");
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
        ep.setProperty(IcanproProjectProperties.BUILD_GENERATED_DIR, "${" + IcanproProjectProperties.BUILD_DIR + "}/generated");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_DIR, "${" + IcanproProjectProperties.BUILD_DIR + "}/jar");
        ep.setProperty(IcanproProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs");
        ep.setProperty(IcanproProjectProperties.DIST_JAVADOC_DIR, "${" + IcanproProjectProperties.DIST_DIR + "}/javadoc");
        ep.setProperty(IcanproProjectProperties.JAVA_PLATFORM, "default_platform");
        ep.setProperty(IcanproProjectProperties.DEBUG_CLASSPATH, "${" + IcanproProjectProperties.JAVAC_CLASSPATH + "}:${" + IcanproProjectProperties.BUILD_CLASSES_DIR + "}");

        //============= Start of IcanPro========================================//
        ep.setProperty(IcanproProjectProperties.JBI_SETYPE_PREFIX, "sun-etl-engine"); // NOI18N

        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_ALIAS, "This Assembly Unit"); // NOI18N

        ep.setProperty(IcanproProjectProperties.ASSEMBLY_UNIT_DESCRIPTION, "Represents this Assembly Unit"); // NOI18N

        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_ALIAS, "This Application Sub-Assembly"); // NOI18N

        ep.setProperty(IcanproProjectProperties.APPLICATION_SUB_ASSEMBLY_DESCRIPTION, "This represents the Application Sub-Assembly"); // NOI18N

        ep.setProperty(IcanproProjectProperties.JBI_COMPONENT_CONF_ROOT, "nbproject/private"); // NOI18N

        ep.setProperty(IcanproProjectProperties.JBI_DEPLOYMENT_CONF_ROOT, "nbproject/deployment"); // NOI18N            

        ep.setProperty(IcanproProjectProperties.BC_DEPLOYMENT_JAR, "${" + IcanproProjectProperties.BUILD_DIR + "}/" + "BCDeployment.jar");
        ep.setProperty(IcanproProjectProperties.SE_DEPLOYMENT_JAR, "${" + IcanproProjectProperties.BUILD_DIR + "}/" + "SEDeployment.jar");
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

    public static void createDefaultDatabase(String name) {
        // Modified for Other OS - Solaris
        /*File f = new File(name + fs + DEFAULT_DB_DIR);
        try {
        FileUtil.createFolder(f);
        } catch (IOException ex) {
        Exceptions.printStackTrace(ex);
        }*/
        String url = DEFAULT_FLATFILE_JDBC_URL_PREFIX + prjName+"_"+DEFAULT_DB_DIR + ":" + name;
        char[] ch = name.toCharArray();
        if (ch == null) {
            String nbBundle10 = "No Database name specified.";//mLoc.t("BUND723: No Database name specified.");

            NotifyDescriptor d =
                    new NotifyDescriptor.Message(nbBundle10/*.substring(15)*/, NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        } /*else if (f.exists()) {
        String nbBundle11 = mLoc.t("PRJS001: Database {0} already exists.", name);
        NotifyDescriptor d =
        new NotifyDescriptor.Message(Localizer.parse(nbBundle11), NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
        }*/ else {
            Connection conn = null;
            try {
                conn = DBExplorerUtil.createConnection("org.axiondb.jdbc.AxionDriver", url, "sa", "sa");
            } catch (Exception ex) {
                String nbBundle12 = "Axion driver could not be loaded.";//mLoc.t("BUND724: Axion driver could not be loaded.");

                NotifyDescriptor d =
                        new NotifyDescriptor.Message(nbBundle12/*.substring(15)*/, NotifyDescriptor.INFORMATION_MESSAGE);
                DialogDisplayer.getDefault().notify(d);
            } finally {
                try {
                    if (conn != null) {
                        conn.createStatement().execute("shutdown");
                        conn.close();
                    }
                } catch (SQLException ex) {
                    conn = null;
                }
            }
        }
    }

    //Need for Migration - Start
    public static File getDatabasesFolder() {
        return databases;
    }

    public static String getDatabasesFolderPath() {
        //return databases.getPath();
        String path = FileUtil.toFile(dbObj).getAbsolutePath();
        /*if (Utilities.isWindows()) {
        path = path.replace("\\", "/"); // NOI18N
        }*/
        return path;
    }

    public static String getDataFolderPath() {
        //return data.getPath();
        String path = FileUtil.toFile(data).getAbsolutePath();
        if (Utilities.isWindows()) {
            path = path.replace("\\", "/"); // NOI18N

        }
        return path;
    }
    
    public static void setMigrationUtil() {
        String path = FileUtil.toFile(data).getAbsolutePath();
        if (Utilities.isWindows()) {
            path = path.replace("\\", "/"); // NOI18N

        }
        String path1 = FileUtil.toFile(dbObj).getAbsolutePath();
        MigrationUtils.setDatabasesFolder(databases);
        MigrationUtils.setDataFolderPath(path);
        MigrationUtils.setDatabasesFolderPath(path1);
    }
    //Need for Migration - End  
}
