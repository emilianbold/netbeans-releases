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

/*
 * ApplicationDescriptorHandlerTest.java
 * JUnit based test
 *
 * Created on 18 November 2005, 18:15
 */
package org.netbeans.modules.mobility.project;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.*;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.ui.wizard.ConfigurationsSelectionPanel;
import org.netbeans.modules.masterfs.MasterFileSystem;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.TemplateWizard;
import org.openide.util.Exceptions;

/**
 *
 * @author lukas
 */
public class ApplicationDescriptorHandlerTest extends NbTestCase {
    
    static FileSystem mfs=null;
    static AntProjectHelper p=null;
    static J2MEProject pt=null;
    static String dir=null;
    static File dirFile=null;
    static FileObject src=null;
    final File api;
    final File tmp;
    
    static final Object syncObj=new Object();
    static
    { //Prepare for project creation
        TestUtil.setLookup( new Object[] {
            TestUtil.testProjectFactory(),
            TestUtil.testProjectChooserFactory(),
            TestUtil.testLogger(J2MEActionProvider.COMMAND_BUILD),
            TestUtil.testFileLocator(),
            TestUtil.testErrManager(),
        }, ApplicationDescriptorHandlerTest.class.getClassLoader());
        
        mfs=MasterFileSystem.settingsFactory(null);
        assertNotNull(mfs);
        
        Logger.getLogger("org.openide.util.RequestProcessor").addHandler(new Handler() {
                public void publish(LogRecord record) {
                    String s=record.getMessage();
                    if (s==null)
                        return;
                    if (s.startsWith("Work finished") &&
                            s.indexOf("J2MEProject$6")!=-1 &&
                            s.indexOf("RequestProcessor")!=-1) {
                        synchronized (syncObj) {
                            syncObj.notify();
                        }
                    }
                }
                public void flush() {}
                public void close() throws SecurityException {}
            });
    }
    
    void waitFinished()
    {
        while (true)
        {
            try   {
                syncObj.wait();
                break;
            }
            catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    public ApplicationDescriptorHandlerTest(String testName) throws IOException {
        super(testName);
        
        api=getGoldenFile("midpapi20.zip");
        tmp=getGoldenFile("Midlet.java");

        FileObject fobj=Repository.getDefault().getDefaultFileSystem().getRoot();
        fobj=FileUtil.createFolder(fobj,"Templates");
        fobj=FileUtil.createFolder(fobj,"MIDP");
        FileObject template=FileUtil.toFileObject(tmp);
        src=fobj.getFileObject("Midlet.java");
        if (src==null)
            src=FileUtil.copyFile(template,fobj,"Midlet");
        assertNotNull(src);
        TestUtil.setEnv();
    }
    
    protected void setUp() {
        try {
            super.setUp();
            dir=getWorkDir().getAbsolutePath();
            dirFile=getWorkDir();
            clearWorkDir();
            
            TemplateWizard wiz=new TemplateWizard();
            synchronized(syncObj) {
                p=J2MEProjectGenerator.createNewProject(dirFile,"MyProject"+dirFile.getName(),null,new ArrayList(10),
                        (List)wiz.getProperty(ConfigurationsSelectionPanel.CONFIGURATION_TEMPLATES));
                waitFinished();
            }
            TestUtil.setHelper(p);
            /* Set classpath to fake midp */
            EditableProperties props=p.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            props.setProperty("platform.bootclasspath",api.getAbsolutePath());
            p.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
            
            File build=File.createTempFile("build",".properties",dirFile);
            System.setProperty("user.properties.file",build.getAbsolutePath());
            
            pt=new J2MEProject(p);
            
            J2MEActionProvider prov = pt.getLookup().lookup(J2MEActionProvider.class);
            assertNotNull( prov );
            
            ProjectManager.getDefault().saveAllProjects();
            String result;
            synchronized (TestUtil.rootStr) {
                prov.invokeAction(prov.COMMAND_BUILD,null);
                result=TestUtil.waitFinished();
            }
            assertNull(result,result);
        } catch (Exception ex) {
            assertTrue(ex.getLocalizedMessage(),true);
        }
    }
    
    protected void tearDown() throws Exception {
        TestUtil.deleteProject(pt);
    }
    
    public static Test suite() {
        class MySuite extends TestSuite {
            MySuite() {super(ApplicationDescriptorHandlerTest.class);}
            public void run(TestResult result) {
                super.run(result);
                TestUtil.removePlatform(ApplicationDescriptorHandlerTest.class);
            }
        }
        TestSuite suite = new MySuite();
        return suite;
    }
    
    /**
     * Test of getDefault method, of class org.netbeans.modules.mobility.project.ApplicationDescriptorHandler.
     */
    public void testGetDefault() {
        System.out.println("getDefault");
        
        ApplicationDescriptorHandler expResult = ApplicationDescriptorHandler.getDefault();
        ApplicationDescriptorHandler result = ApplicationDescriptorHandler.getDefault();
        
        //Test that ApplicationDescriptionHandler is a singleton
        assertEquals(expResult, result);
    }
    
    /**
     * Test of handleRename method, of class org.netbeans.modules.mobility.project.ApplicationDescriptorHandler.
     */
    public void testHandleRename() throws IOException {
        final String orig="MIDlet-1: Midlet, , hello.Midlet\n";
        final String newp="MIDlet-1: Midlet, ,hello.NewName\n";
        System.out.println("handleRename");
        
        ApplicationDescriptorHandler result = ApplicationDescriptorHandler.getDefault();
        FileObject file=mfs.findResource(dir+File.separator+"src"+File.separator+"hello"+File.separator+"Midlet.java");
        EditableProperties props=p.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String origProperty=props.getProperty("manifest.midlets");
        assertEquals(orig,origProperty);
        
        synchronized (TestUtil.rootStr) {
            result.handleRename(file,"NewName");
            TestUtil.waitFinished();
        }
        props=p.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String newProperty=props.getProperty("manifest.midlets");
        assertEquals(newp,newProperty);
    }
    
    /**
     * Test of handleMove method, of class org.netbeans.modules.mobility.project.ApplicationDescriptorHandler.
     */
    public void testHandleMove() throws IOException {
        final String orig="MIDlet-1: Midlet, , hello.Midlet\n";
        final String newp="MIDlet-1: Midlet, ,newfold.Midlet\n";
        System.out.println("handleMove");
        
        ApplicationDescriptorHandler result = ApplicationDescriptorHandler.getDefault();
        FileObject file=mfs.findResource(dir+File.separator+"src"+File.separator+"hello"+File.separator+"Midlet.java");
        FileObject origFold=mfs.findResource(dir+File.separator+"src");
        FileObject folder=FileUtil.createFolder(origFold,"newfold");
        EditableProperties props=p.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String origProperty=props.getProperty("manifest.midlets");
        assertEquals(orig,origProperty);
        
        synchronized (TestUtil.rootStr) {
            result.handleMove(file,folder);
            TestUtil.waitFinished();
        }
        props=p.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String newProperty=props.getProperty("manifest.midlets");
        assertEquals(newp,newProperty);
    }
    
    /**
     * Test of handleDelete method, of class org.netbeans.modules.mobility.project.ApplicationDescriptorHandler.
     */
    public void testHandleDelete() throws IOException {
        final String orig="MIDlet-1: Midlet, , hello.Midlet\n";
        final String newp="";
        System.out.println("handleDelete");
        
        ApplicationDescriptorHandler result = ApplicationDescriptorHandler.getDefault();
        
        FileObject file=mfs.findResource(dir+File.separator+"src"+File.separator+"hello"+File.separator+"Midlet.java");
        EditableProperties props=p.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String origProperty=props.getProperty("manifest.midlets");
        assertEquals(orig,origProperty);
        synchronized (TestUtil.rootStr) {
            result.handleDelete(file);
            TestUtil.waitFinished();
        }
        props=p.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String newProperty=props.getProperty("manifest.midlets");
        assertEquals(newp,newProperty);
    }
}
