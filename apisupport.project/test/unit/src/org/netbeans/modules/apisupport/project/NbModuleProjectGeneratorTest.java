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
import java.io.OutputStream;
import java.io.PrintWriter;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 * NbModuleProjectGenerator tests.
 *
 * @author mkrauskopf
 */
public class NbModuleProjectGeneratorTest extends NbTestCase {
    // TODO test both firstLevel and secondLevel modules and also NetBeans CVS
    // tree modules

    public NbModuleProjectGeneratorTest(String testName) {
        super(testName);
    }
    
    private static final String[] CREATED_FILES = {
        "build.xml",
        "manifest.mf",
        "nbproject/suite-locator.properties",
        "nbproject/project.xml",
        "src",
        "src/org/company/example/testModule/resources/Bundle.properties",
        "src/org/company/example/testModule/resources/layer.xml",
    };
    
    protected void setUp() throws Exception {
        clearWorkDir();
        // create layerTemplate
        FileObject root = Repository.getDefault().getDefaultFileSystem().getRoot();
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
        super.setUp();
    }
    
    // XXX also should test content created files (XMLs, properties) and
    // created suite.
    public void testCreateProject() throws Exception {
        // XXX huh? is this a good way?
        String defPlatform = getDataDir().getParentFile().getParentFile().getParent();
        File suiteDir = new File(getWorkDir(), "testSuite");
        NbModuleProjectGenerator.createSuite(suiteDir, defPlatform);
        
        File prjDir = new File(new File(suiteDir, "testModule"), "secondLevel");
        
        NbModuleProjectGenerator.createExternalProject(
                prjDir, "Testing Module", "org.company.example.testModule", 
                "testModule", 
                "org/company/example/testModule/resources/Bundle.properties",
                "org/company/example/testModule/resources/layer.xml",
                suiteDir.getPath());
        FileObject fo = FileUtil.toFileObject(prjDir);
        for (int i=0; i < CREATED_FILES.length; i++) {
            assertNotNull(CREATED_FILES[i]+" file/folder cannot be found",
                    fo.getFileObject(CREATED_FILES[i]));
        }
    }
}
