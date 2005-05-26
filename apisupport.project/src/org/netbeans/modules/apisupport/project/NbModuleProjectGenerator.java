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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Properties;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Servers for generating new NetBeans Modules templates.
 *
 * @author Martin Krauskopf
 */
public class NbModuleProjectGenerator {
    
    // XXX obsolete - polish or remove
    private static final String SUITE_LOCATOR_PATH =
            "nbproject" + File.separator + "suite-locator.properties"; // NOI18N

    private static final String PLATFORM_PROPERTIES_PATH =
            "nbproject" + File.separator + "platform.properties"; // NOI18N
            
    
    /** Use static factory methods instead. */
    private NbModuleProjectGenerator() {/* empty constructor*/}
    
    /** Creates empty suite using given platform. */
    public static void createSuite(File suiteRoot, String platform) throws
            IOException {
        if (!suiteRoot.exists()) {
            suiteRoot.mkdirs();
        }
        Properties suiteProps = new Properties();
        suiteProps.put("netbeans.dest.dir", platform); // NOI18N
        suiteProps.put("harness.dir","${netbeans.dest.dir}/harness"); // NOI18N
        FileOutputStream suiteFOS = new FileOutputStream(
                new File(suiteRoot, "suite.properties")); // NOI18N
        suiteProps.store(suiteFOS, null);
        suiteFOS.close();
    }
    
    /** Generates standalone NetBeans Module. */
    public static void createStandAloneModule(File projectDir, String cnb,
            String name, String bundlePath, String layerPath) throws IOException {
        final FileObject dirFO = NbModuleProjectGenerator.createProjectDir(projectDir);
        if (ProjectManager.getDefault().findProject(dirFO) != null) {
            throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
        }
        NbModuleProjectGenerator.createProjectXML(dirFO, cnb, true);
        NbModuleProjectGenerator.createBuildScript(dirFO, cnb);
        NbModuleProjectGenerator.createPlatformProperties(dirFO);
        NbModuleProjectGenerator.createManifest(dirFO, cnb, bundlePath, layerPath);
        NbModuleProjectGenerator.createBundle(dirFO, bundlePath, name);
        NbModuleProjectGenerator.createLayer(dirFO, layerPath);
    }
    
    /**
     * Creates basic <em>nbbuild/project.xml</em> or whatever
     * <code>AntProjectHelper.PROJECT_XML_PATH</code> is pointing to.
     */
    private static void createProjectXML(FileObject projectDir,
            String cnb, boolean standalone) throws IOException {
        ProjectXMLManager.generateEmptyTemplate(
                createFileObject(projectDir, AntProjectHelper.PROJECT_XML_PATH),
                cnb, standalone);
    }
    
    // XXX obsolete - polish or remove
    private static void createSuiteLocator(FileObject projectDir,
            String suiteRoot) throws IOException {
        Properties suiteLocatorProps = new Properties();
        FileObject fo = FileUtil.toFileObject(new File(suiteRoot));
        // check if this is first or second level module
        String upPath = projectDir.getParent().equals(fo) ? ".." : "../.."; // NOI18N
        suiteLocatorProps.put("suite.properties", "${basedir}/" + upPath + // NOI18N
                "/suite.properties"); // NOI18N
        
        FileObject suiteLocFO = NbModuleProjectGenerator.createFileObject(
                projectDir, NbModuleProjectGenerator.SUITE_LOCATOR_PATH);
        FileLock lock = suiteLocFO.lock();
        try {
            OutputStream suiteLocFOS = suiteLocFO.getOutputStream(lock);
            try {
                suiteLocatorProps.store(suiteLocFOS, null);
            } finally {
                suiteLocFOS.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
    
    /**
     * Creates basic <em>build.xml</em> or whatever
     * <code>GeneratedFilesHelper.BUILD_XML_PATH</code> is pointing to.
     */
    private static void createBuildScript(FileObject projectDir, String cnb) throws
            IOException {
        FileObject buildScript = NbModuleProjectGenerator.createFileObject(
                projectDir, GeneratedFilesHelper.BUILD_XML_PATH);
        Document prjDoc = XMLUtil.createDocument("project", null, null, null); // NOI18N
        Element prjEl = prjDoc.getDocumentElement();
        prjEl.setAttribute("name", projectDir.getNameExt()); // NOI18N
        prjEl.setAttribute("default", "netbeans"); // NOI18N
        prjEl.setAttribute("basedir", "."); // NOI18N
        
        Element el = prjDoc.createElement("description"); // NOI18N
        // XXX should this be localized?
        el.appendChild(prjDoc.createTextNode("Builds, tests, and runs the " + // NOI18N
                "project " + cnb)); // NOI18N
        prjEl.appendChild(el);
        
        el = prjDoc.createElement("import"); // NOI18N
        el.setAttribute("file", "nbproject/build-impl.xml"); // NOI18N
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
    
    private static void createPlatformProperties(FileObject projectDir) throws IOException {
        FileObject plafPropsFO = NbModuleProjectGenerator.createFileObject(
                projectDir, NbModuleProjectGenerator.PLATFORM_PROPERTIES_PATH);
        EditableProperties props = new EditableProperties(true);
        // XXX temporary - user will choose a platform in the wizard
        props.put("nbplatform.active", "default"); // NOI18N
        NbModuleProjectGenerator.storeProperties(plafPropsFO, props);
    }
    
    private static void createManifest(FileObject projectDir, String cnb,
            String bundlePath, String layerPath) throws IOException {
        FileObject manifestFO = NbModuleProjectGenerator.createFileObject(
                projectDir, "manifest.mf"); // NOI18N
        ManifestManager.createManifest(manifestFO, cnb, "1.0", bundlePath, layerPath); // NOI18N
    }
    
    private static void createBundle(FileObject projectDir, String bundlePath,
            String name) throws IOException {
        FileObject bundleFO = NbModuleProjectGenerator.createFileObject(
                projectDir, "src" + File.separator + bundlePath); // NOI18N
        EditableProperties props = new EditableProperties(true);
        props.put("OpenIDE-Module-Name", name); // NOI18N
        NbModuleProjectGenerator.storeProperties(bundleFO, props);
    }
    
    private static void createLayer(FileObject projectDir, String layerPath) throws IOException {
        FileObject layerFO =  Repository.getDefault().getDefaultFileSystem().
                findResource("org-netbeans-modules-apisupport-project/layer_template.xml"); //NOI18N
        assert layerFO != null : "Cannot find layer template"; // NOI18N
        int lastSlashPos = layerPath.lastIndexOf('/');
        String layerDir = layerPath.substring(0, lastSlashPos);
        String layerName = layerPath.substring(lastSlashPos + 1,
                layerPath.length() - 4);  // ".xml" <- 4
        FileObject destDir = FileUtil.createFolder(projectDir, "src/" + layerDir); // NOI18N
        FileUtil.copyFile(layerFO, destDir, layerName);
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

    /** Just utility method. */
    private static void storeProperties(FileObject bundleFO, EditableProperties props) throws IOException {
        FileLock lock = bundleFO.lock();
        try {
            props.store(bundleFO.getOutputStream(lock));
        } finally {
            lock.releaseLock();
        }
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
}


