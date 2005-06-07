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

package org.netbeans.modules.apisupport.project;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Servers for generating new NetBeans Modules templates.
 *
 * @author Martin Krauskopf
 */
public class NbModuleProjectGenerator {
    
    private static final String PLATFORM_PROPERTIES_PATH =
            "nbproject/platform.properties"; // NOI18N
    
    /** Use static factory methods instead. */
    private NbModuleProjectGenerator() {/* empty constructor*/}
    
    /** Generates standalone NetBeans Module. */
    public static void createStandAloneModule(File projectDir, String cnb,
            String name, String bundlePath, String layerPath, String platformID) throws IOException {
        final FileObject dirFO = NbModuleProjectGenerator.createProjectDir(projectDir);
        if (ProjectManager.getDefault().findProject(dirFO) != null) {
            throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
        }
        createProjectXML(dirFO, cnb, NbModuleProject.TYPE_STANDALONE);
        createPlatformProperties(dirFO, platformID);
        createManifest(dirFO, cnb, bundlePath, layerPath);
        createBundle(dirFO, bundlePath, name);
        createLayer(dirFO, layerPath);
        createEmptyTestDir(dirFO);
        ModuleList.refresh();
        ProjectManager.getDefault().clearNonProjectCache();
    }
    
    /** Generates suite component NetBeans Module. */
    public static void createSuiteComponentModule(File projectDir, String cnb,
            String name, String bundlePath, String layerPath, File suiteDir) throws IOException {
        final FileObject dirFO = NbModuleProjectGenerator.createProjectDir(projectDir);
        if (ProjectManager.getDefault().findProject(dirFO) != null) {
            throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
        }
        createProjectXML(dirFO, cnb, NbModuleProject.TYPE_SUITE_COMPONENT);
        createSuiteProperties(dirFO, suiteDir);
        createManifest(dirFO, cnb, bundlePath, layerPath);
        createBundle(dirFO, bundlePath, name);
        createLayer(dirFO, layerPath);
        createEmptyTestDir(dirFO);
        appendToSuite(dirFO, suiteDir);
        ModuleList.refresh();
        ProjectManager.getDefault().clearNonProjectCache();
    }
    
    /** 
     * Generates NetBeans Module within the netbeans.org CVS tree.
     */
    public static void createNetBeansOrgModule(File projectDir, String cnb,
            String name, String bundlePath, String layerPath) throws IOException {
        File nborg = ModuleList.findNetBeansOrg(projectDir);
        if (nborg == null) {
            throw new IllegalArgumentException(projectDir + " doesn't " + // NOI18N
                    "point to directory within the netbeans.org CVS tree"); // NOI18N
        }
        final FileObject dirFO = NbModuleProjectGenerator.createProjectDir(projectDir);
        if (ProjectManager.getDefault().findProject(dirFO) != null) {
            throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
        }
        createBuildXML(dirFO, cnb, nborg);
        createProjectXML(dirFO, cnb, NbModuleProject.TYPE_NETBEANS_ORG);
        createManifest(dirFO, cnb, bundlePath, layerPath);
        createBundle(dirFO, bundlePath, name);
        createLayer(dirFO, layerPath);
        createEmptyTestDir(dirFO);
        ModuleList.refresh();
        ProjectManager.getDefault().clearNonProjectCache();
    }
    
    /**
     * Creates basic <em>nbbuild/project.xml</em> or whatever
     * <code>AntProjectHelper.PROJECT_XML_PATH</code> is pointing to for
     * <em>standalone</em> or <em>module in suite</em> module.
     */
    private static void createProjectXML(FileObject projectDir,
            String cnb, int type) throws IOException {
        ProjectXMLManager.generateEmptyModuleTemplate(
                createFileObject(projectDir, AntProjectHelper.PROJECT_XML_PATH),
                cnb, type);
    }
    
    /**
     * Creates basic <em>build.xml</em> or whatever
     * <code>GeneratedFilesHelper.BUILD_XML_PATH</code> is pointing to.
     */
    private static void createBuildXML(FileObject projectDir, String cnb,
            File nborg) throws IOException {
        FileObject buildScript = NbModuleProjectGenerator.createFileObject(
                projectDir, GeneratedFilesHelper.BUILD_XML_PATH);
        Document prjDoc = XMLUtil.createDocument("project", null, null, null); // NOI18N
        Element prjEl = prjDoc.getDocumentElement();
        prjEl.setAttribute("name", PropertyUtils.relativizeFile(nborg, 
                FileUtil.toFile(projectDir)));
        prjEl.setAttribute("default", "netbeans"); // NOI18N
        prjEl.setAttribute("basedir", "."); // NOI18N
        
        Element el = prjDoc.createElement("description"); // NOI18N
        el.appendChild(prjDoc.createTextNode("Builds, tests, and runs the " + // NOI18N
                "project " + cnb)); // NOI18N
        prjEl.appendChild(el);
        
        el = prjDoc.createElement("import"); // NOI18N
        el.setAttribute("file", PropertyUtils.relativizeFile(FileUtil.toFile(projectDir), 
                new File(nborg, "nbbuild/templates/projectized.xml"))); // NOI18N
        prjEl.appendChild(el);
        
        // store document to disk
        FileLock lock = buildScript.lock();
        try {
            OutputStream os = buildScript.getOutputStream(lock);
            try {
                XMLUtil.write(prjDoc, os, "UTF-8"); // NOI18N
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private static void createSuiteProperties(FileObject projectDir, File suiteDir) throws IOException {
        File projectDirF = FileUtil.toFile(projectDir);
        String suiteLocation;
        String suitePropertiesLocation;
        if (CollocationQuery.areCollocated(projectDirF, suiteDir)) {
            suiteLocation = "${basedir}/" + PropertyUtils.relativizeFile(projectDirF, suiteDir); // NOI18N
            suitePropertiesLocation = "nbproject/suite.properties"; // NOI18N
        } else {
            suiteLocation = suiteDir.getAbsolutePath();
            suitePropertiesLocation = "nbproject/private/suite-private.properties"; // NOI18N
        }
        EditableProperties props = new EditableProperties(true);
        props.setProperty("suite.dir", suiteLocation); // NOI18N
        FileObject suiteProperties = createFileObject(projectDir, suitePropertiesLocation);
        storeProperties(suiteProperties, props);
    }
    
    /** 
     * Appends currently created project in the <code>projectDir<code> to a 
     * suite project contained in the <code>suiteDir</code>. Also intelligently 
     * decides whether an added project is relative to a destination suite or 
     * absolute and uses either <em>nbproject/project.properties</em> or
     * <em>nbproject/private/private.properties</em> appropriately.
     */
    private static void appendToSuite(FileObject projectDir, File suiteDir) throws IOException {
        File projectDirF = FileUtil.toFile(projectDir);
        File suiteGlobalPropsFile = new File(suiteDir, "nbproject/project.properties"); // NOI18N
        FileObject suiteGlobalPropFO;
        if (suiteGlobalPropsFile.exists()) {
            suiteGlobalPropFO = FileUtil.toFileObject(suiteGlobalPropsFile);
        } else {
            suiteGlobalPropFO = createFileObject(suiteGlobalPropsFile);
        }
        EditableProperties globalProps = loadProperties(suiteGlobalPropFO);
        String projectPropKey = "project." + projectDirF.getName(); // NOI18N
        if (CollocationQuery.areCollocated(projectDirF, suiteDir)) {
            // XXX the generating of relative path doesn't seem's too clever, check it
            globalProps.setProperty(projectPropKey,
                    PropertyUtils.relativizeFile(suiteDir, projectDirF));
        } else {
            File suitePrivPropsFile = new File(suiteDir, "nbproject/private/private.properties"); // NOI18N
            FileObject suitePrivPropFO;
            if (suitePrivPropsFile.exists()) {
                suitePrivPropFO = FileUtil.toFileObject(suitePrivPropsFile);
            } else {
                suitePrivPropFO = createFileObject(suitePrivPropsFile);
            }
            EditableProperties privProps= loadProperties(suitePrivPropFO);
            privProps.setProperty(projectPropKey, projectDirF.getAbsolutePath());
            storeProperties(suitePrivPropFO, privProps);
        }
        String modulesProp = globalProps.getProperty("modules"); // NOI18N
        modulesProp = modulesProp == null ? "" : modulesProp + ":"; // NOI18N
        globalProps.setProperty("modules", (modulesProp + "${" + projectPropKey + "}").split("(?<=:)", -1)); // NOI18N
        storeProperties(suiteGlobalPropFO, globalProps);
    }
    
    private static void createPlatformProperties(FileObject projectDir, String platformID) throws IOException {
        FileObject plafPropsFO = createFileObject(
                projectDir, NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH);
        EditableProperties props = new EditableProperties(true);
        props.put("nbplatform.active", platformID); // NOI18N
        storeProperties(plafPropsFO, props);
    }
    
    private static void createManifest(FileObject projectDir, String cnb,
            String bundlePath, String layerPath) throws IOException {
        FileObject manifestFO = createFileObject(
                projectDir, "manifest.mf"); // NOI18N
        ManifestManager.createManifest(manifestFO, cnb, "1.0", bundlePath, layerPath); // NOI18N
    }
    
    private static void createBundle(FileObject projectDir, String bundlePath,
            String name) throws IOException {
        FileObject bundleFO = createFileObject(
                projectDir, "src" + File.separator + bundlePath); // NOI18N
        EditableProperties props = new EditableProperties(true);
        props.put("OpenIDE-Module-Name", name); // NOI18N
        NbModuleProjectGenerator.storeProperties(bundleFO, props);
    }
    
    private static void createLayer(FileObject projectDir, String layerPath) throws IOException {
        FileObject layerFO = createFileObject(projectDir, "src/" + layerPath); // NOI18N
        FileLock lock = layerFO.lock();
        try {
            InputStream is = NbModuleProjectGenerator.class.getResourceAsStream("ui/resources/layer_template.xml"); // NOI18N
            try {
                OutputStream os = layerFO.getOutputStream(lock);
                try {
                    FileUtil.copy(is, os);
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    private static void createEmptyTestDir(FileObject projectDir) throws IOException {
        projectDir.createFolder("test/unit/src"); // NOI18N
    }
    
    /**
     * Creates project projectDir if it doesn't already exist and returns representing
     * <code>FileObject</code>.
     */
    private static FileObject createProjectDir(File dir) throws IOException {
        // XXX Hmmm, inspired by J2SEProject, probably just call FO|FU.createFolder
        if(!dir.exists()) {
            refreshFolder(dir);
            if (!dir.mkdirs()) {
                throw new IOException("Can not create project folder \"" // NOI18N
                        + dir.getAbsolutePath() + "\"");   //NOI18N
            }
            refreshFileSystem(dir);
        }
        FileObject dirFO = FileUtil.toFileObject(dir);
        if (dirFO == null) {
            throw new IOException("No such dir on disk: " + dir); // NOI18N
        }
        assert dirFO.isFolder() : "Not really a dir: " + dir; // NOI18N
        return dirFO;
    }
    
    /**
     * Refreshes the given <code>projectDir</code> or a nearest existing directory.
     */
    private static void refreshFolder(File dir) {
        // XXX Hmmm, inspired by J2SEProject, probably just call FO|FU.createFolder
        while (!dir.exists()) {
            dir = dir.getParentFile();
            if (dir == null) {
                return;
            }
        }
        FileObject fo = FileUtil.toFileObject(dir);
        if (fo != null) {
            fo.refresh(false);
        }
    }
    
    private static void refreshFileSystem(final File dir) throws FileStateInvalidException {
        // XXX Hmmm, inspired by J2SEProject, probably just call FO|FU.createFolder
        File root = dir;
        while (root.getParentFile() != null) {
            root = root.getParentFile();
        }
        FileObject rootFO = FileUtil.toFileObject(root);
        if (rootFO != null) {
            rootFO.getFileSystem().refresh(false);
        } else {
            assert false : "At least disk roots must be mounted! " + root; // NOI18N
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot resolve" + // NOI18N
                    "file object for " + root.getAbsolutePath()); // NOI18N
        }
    }

    /** Just utility method to store <code>EditableProperties</code>. */
    private static void storeProperties(FileObject propsFO, EditableProperties props) throws IOException {
        FileLock lock = propsFO.lock();
        try {
            OutputStream os = propsFO.getOutputStream(lock);
            try {
                props.store(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /** Just utility method to load <code>EditableProperties</code>. */
    private static EditableProperties loadProperties(FileObject propsFO) throws IOException {
        InputStream is = null;
        EditableProperties props = new EditableProperties(true);
        try {
            is = propsFO.getInputStream();
            props.load(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return props;
    }

    /**
     * Creates a new <code>FileObject</code>.
     * Throws <code>IllegalArgumentException</code> if such an object already
     * exists. Throws <code>IOException</code> if creation fails.
     */
    private static FileObject createFileObject(FileObject dir, String relToDir) throws IOException {
        FileObject createdFO = dir.getFileObject(relToDir);
        if (createdFO != null) {
            throw new IllegalArgumentException("File " + createdFO + " already exists."); // NOI18N
        }
        createdFO = FileUtil.createData(dir, relToDir);
        return createdFO;
    }
    
    /**
     * Creates a new <code>FileObject</code>.
     * Throws <code>IllegalArgumentException</code> if such an object already
     * exists. Throws <code>IOException</code> if creation fails.
     */
    private static FileObject createFileObject(File fileToCreate) throws IOException {
        File parent = fileToCreate.getParentFile();
        if (parent == null) {
            throw new IllegalArgumentException("Cannot create: " + fileToCreate);
        }
        if (!parent.exists()) {
            parent.mkdirs();
        }
        return createFileObject(
                FileUtil.toFileObject(parent), fileToCreate.getName());
    }
}
