/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.visualweb.insync;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import junit.framework.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sc32560, jdeva
 * 
 */
public class InsyncTestBase extends NbTestCase {
    private static final String SYS_PROP_SAX_PARSER_FACTORY = "javax.xml.parsers.SAXParserFactory"; // NOI18N
    private static final String SYS_PROP_DOM_PARSER_FACTORY = "javax.xml.parsers.DocumentBuilderFactory"; // NO18N
    
    public InsyncTestBase(String name) {
        super(name);
    }
    
    private Project project;
    
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        /**MockLookup.setLookup(
                Lookups.fixed(l, new DummyXMLEncodingImpl()),
                Lookups.metaInfServices(l)); */
        //FileUtil.setMIMEType("xml", "text/x-jsf+xml");

        //MockServices.setServices(MockOpenProjectsTrampoline.class);
        openProject();
        setUpForFacesModelSet();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        destroyProject();
    }       
    
    public void setUpForFacesModelSet() {
        // Needed for faces container initialization
        System.setProperty(SYS_PROP_SAX_PARSER_FACTORY, "org.netbeans.core.startup.SAXFactoryImpl");
        System.setProperty(SYS_PROP_DOM_PARSER_FACTORY, "org.netbeans.core.startup.DOMFactoryImpl");        
    }
    
    public Project openProject() throws IOException {
        // Needed for
        ClassLoader l = getClass().getClassLoader();
        MockLookup.setLookup(Lookups.fixed(l), Lookups.metaInfServices(l));
        
        String zipResource = "VWJavaEE5.zip";
        String zipPath = InsyncTestBase.class.getResource(zipResource).getPath();
        NbTestCase.assertNotNull(zipPath);
        File archiveFile = new File(zipPath);

        // FileObject destFileObj = TestUtil.makeScratchDir(this);
        FileObject destFileObj = FileUtil.toFileObject(getWorkDir());
        unZipFile(archiveFile, destFileObj);
        NbTestCase.assertTrue(destFileObj.isValid());
        FileObject testApp = destFileObj.getFileObject("VWJavaEE5");
        System.out.println("Children of VWJavaEE5:" + Arrays.toString(testApp.getChildren()));
        //        assertTrue( ProjectManager.getDefault().isProject(testApp));
        project = ProjectManager.getDefault().findProject(testApp);
        NbTestCase.assertNotNull(project);
        OpenProjects.getDefault().open(new Project[]{project}, false);
        return project;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void destroyProject() throws IOException {
        OpenProjects.getDefault().close(new Project[]{project});
        project.getProjectDirectory().delete();
    }
    
    private static void unZipFile(File archiveFile, FileObject destDir) throws IOException {
        FileInputStream fis = new FileInputStream(archiveFile);
        try {
            ZipInputStream str = new ZipInputStream(fis);
            ZipEntry entry;
            while ((entry = str.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    FileUtil.createFolder(destDir, entry.getName());
                } else {
                    FileObject fo = FileUtil.createData(destDir, entry.getName());
                    FileLock lock = fo.lock();
                    try {
                        OutputStream out = fo.getOutputStream(lock);
                        try {
                            FileUtil.copy(str, out);
                        } finally {
                            out.close();
                        }
                    } finally {
                        lock.releaseLock();
                    }
                }
            }
        } finally {
            fis.close();
        }
    }    
}
