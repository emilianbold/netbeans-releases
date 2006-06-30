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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.io.IOException;
import junit.framework.*;
import junit.framework.*;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.InstalledFileLocator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author vkraemer
 */
public class EarProjectGeneratorTest extends TestCase {
    
    /**
     * Test of createProject method, of class org.netbeans.modules.j2ee.earproject.EarProjectGenerator.
     */
    public void testCreateProject() {

        System.out.println("testCreateProject");
        
        // TODO add your test code below by replacing the default call to fail.
        //fail("The test case is empty.");
        FileObject dir[] = null;
        try {
            File f = File.createTempFile("vbktest","ZZZZZZ");
            File tmpdir = f.getParentFile();
            f.delete();
            f = new File(tmpdir,"EarProjectGeneratorTest");
            f.mkdirs();
            dir = FileUtil.fromFile(f);
            //String typeName = t.getType();
            EarProjectGenerator.createProject(f, "EarProjectGeneratorTest", 
                "1.4", null);
            //fail("this should not work");
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail("this looks wrong");
        } catch (Throwable t) {
            t.printStackTrace();
            fail("caught an unexpected exception: "+t.getClass().toString());
        } finally {
            try {
                if (null != dir && null != dir[0]) {
                    dir[0].delete();
                }
            } catch (Throwable t) {
                System.out.println("bummer there");
            }
        }
    }

//    public void testImportProject() {
//
//        System.out.println("testImportProject");
//        
//        // TODO add your test code below by replacing the default call to fail.
//        //fail("The test case is empty.");
//        FileObject dir[] = null;
//        try {
//            File f = File.createTempFile("vbktest","ZZZZZZ");
//            File tmpdir = f.getParentFile();
//            f.delete();
//            f = new File(tmpdir,"EarProjectImportTest");
//            f.mkdirs();
//            dir = FileUtil.fromFile(f);
//            //String typeName = t.getType();
//            AntProjectHelper h = EarProjectGenerator.createProject(f, "EarProjectImportTest", 
//                "1.4", null);
//            //h.
//            //FileObject fileToDelete = dir[0].getFileObject("nbproject");
//            // XXX need to close the project, the project generator is making
//            // sure there isn't a project in the directory about to be used.
//            //ProjectManager.getDefault().
//            //fileToDelete.delete();
//            //fileToDelete = dir[0].getFileObject("build.xml");
//            //fileToDelete.
//            
//            EarProjectGenerator.importProject(f, "EarProjectImportTestImport",  "1.4");
//            //fail("this should not work");
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//            fail("this looks wrong");
//        } catch (Throwable t) {
//            t.printStackTrace();
//            fail("caught an unexpected exception: "+t.getClass().toString());
//        } finally {
//            try {
//                if (null != dir && null != dir[0]) {
////                    dir[0].delete();
//                }
//            } catch (Throwable t) {
//                System.out.println("bummer there");
//            }
//        }
//    }

    public EarProjectGeneratorTest(java.lang.String testName) {
        super(testName);
    }
    
    /**
     * Test of importProject method, of class org.netbeans.modules.j2ee.earproject.EarProjectGenerator.
     *
    public void testImportProject() {

        System.out.println("testImportProject");
        
        // TODO add your test code below by replacing the default call to fail.
        fail("The test case is empty.");
    }
    */
    
}
