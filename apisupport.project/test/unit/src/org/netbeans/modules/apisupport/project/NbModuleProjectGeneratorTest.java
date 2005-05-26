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
import java.io.PrintWriter;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;

/**
 * NbModuleProjectGenerator tests.
 *
 * @author mkrauskopf
 */
public class NbModuleProjectGeneratorTest extends TestBase {
    // TODO test both firstLevel and secondLevel modules and also NetBeans CVS
    // tree modules
    
    public NbModuleProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    private static final String[] CREATED_FILES = {
        "build.xml",
        "manifest.mf",
        "nbproject/project.xml",
        "src",
        "src/org/example/testModule/resources/Bundle.properties",
        "src/org/example/testModule/resources/layer.xml",
    };
    
    
    protected void setUp() throws Exception {
        super.setUp();
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
        FileObject parent = root.getFileObject("org-netbeans-modules-apisupport-project");
        if (parent != null) {
            parent.delete();
        }
        // prepare data (layerTemplate)
        final FileSystem fs = Repository.getDefault().getDefaultFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            public void run() throws java.io.IOException {
                FileObject root = fs.getRoot();
                FileObject parent = root.createFolder("org-netbeans-modules-apisupport-project");
                FileObject layerTemplate = parent.createData("layer_template.xml");
                FileLock lock = layerTemplate.lock();
                PrintWriter pw = new PrintWriter(layerTemplate.getOutputStream(lock));
                try {
                    pw.println("\"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\"");
                    pw.println("<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.1//EN\" \"http://www.netbeans.org/dtds/filesystem-1_1.dtd\">");
                    pw.println("<filesystem>");
                    pw.println("</filesystem>");
                } finally {
                    lock.releaseLock();
                    pw.close();
                }
            }
        });
    }
    
    // XXX also should test content created files (XMLs, properties) and
    // created suite.
    public void testCreateStandAloneModule() throws Exception {
        // XXX check below lines
        String defPlatform = getDataDir().getParentFile().getParentFile().getParent();
        File targetPrjDir = new File(getWorkDir(), "testModule");
        
        NbModuleProjectGenerator.createStandAloneModule(
                targetPrjDir,
                "org.example.testModule", // cnb
                "Testing Module", // display name
                "org/example/testModule/resources/Bundle.properties",
                "org/example/testModule/resources/layer.xml");
        FileObject fo = FileUtil.toFileObject(targetPrjDir);
        // Make sure generated files are created too - simulate project opening.
        Project p = ProjectManager.getDefault().findProject(fo);
        assertNotNull("have a project in " + targetPrjDir, p);
        NbModuleProject.OpenedHook hook = (NbModuleProject.OpenedHook) p.getLookup().lookup(NbModuleProject.OpenedHook.class);
        assertNotNull("has an OpenedHook", hook);
        hook.projectOpened(); // protected but can use package-private access
        // check generated module
        for (int i=0; i < CREATED_FILES.length; i++) {
            assertNotNull(CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(CREATED_FILES[i]));
        }
    }
}
