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
 * FileBuiltQueryImplTest.java
 * JUnit based test
 *
 * Created on 14 February 2006, 16:42
 */
package org.netbeans.modules.mobility.project.queries;

import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import junit.framework.*;
import java.io.File;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.platform.JavaPlatformProvider;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.project.J2MEActionProvider;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.J2MEProjectGenerator;
import org.netbeans.modules.mobility.project.TestUtil;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author lukas
 */
public class FileBuiltQueryImplTest extends NbTestCase {
    static AntProjectHelper aph=null;
    static FileObject projDir = null;
    
    static class MyProvider implements JavaPlatformProvider {
        final static J2MEPlatform.Device devices[];
        final static J2MEPlatform plat;
        
        
        static
        {
            devices=new J2MEPlatform.Device[] {
                new J2MEPlatform.Device("d1","d2",null,new J2MEPlatform.J2MEProfile[0] ,null)
            };
            plat=new J2MEPlatform("n1","cp","t1","d1",null,null,null,null,null,devices);
        }
        
        public JavaPlatform[] getInstalledPlatforms() {
            return new JavaPlatform[] { plat};
        }
        
        public JavaPlatform getDefaultPlatform() {
            return plat;
        }
        
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }
        
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
    static final Object syncObj=new Object();
    static
    {
        TestUtil.setLookup( new Object[] {
            TestUtil.testProjectFactory(),
            TestUtil.testFileLocator(),
            TestUtil.testProjectChooserFactory(),
            TestUtil.testLogger("J2MEActionProvider.COMMAND_COMPILE_SINGLE"),
            new MyProvider()
        }, FileBuiltQueryImplTest.class.getClassLoader());
        
         Logger.getLogger("org.openide.util.RequestProcessor").addHandler(new Handler() {
                public void publish(LogRecord record) {
                    String s=record.getMessage();
                    if (s==null)
                        return;
                    if (s.startsWith("Work finished") &&
                            s.indexOf("J2MEProject$6")!=-1 &&
                            s.indexOf("RequestProcessor")!=-1) {
                        synchronized (syncObj) {
                            System.out.println("XXXFinished: "+s);
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
      
    public FileBuiltQueryImplTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        TestUtil.setEnv();
        File wtkdemo=getGoldenFile("WTKDemo");
        projDir=FileUtil.toFileObject(getWorkDir()).createFolder("Demo");
        TestUtil.cpDir(FileUtil.toFileObject(wtkdemo),projDir);
        
        synchronized(syncObj) {
            aph=J2MEProjectGenerator.
                createProjectFromWtkProject(FileUtil.toFile(projDir),"WTKDemo",null,FileUtil.toFile(projDir).getAbsolutePath());
            waitFinished();
        }
        assertNotNull(aph);
        TestUtil.setHelper(aph);
        File build=File.createTempFile("build",".properties",FileUtil.toFile(projDir));
        System.setProperty("user.properties.file",build.getAbsolutePath());
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FileBuiltQueryImplTest.class);
        
        return suite;
    }
    
    /**
     * Test of JavadocForBinaryQueryImpl class
     */
    public void testJavadocForBinaryQuery() throws Exception {
        System.out.println("getStatus");
        J2MEProject p= (J2MEProject)ProjectManager.getDefault().findProject(projDir);
        JavadocForBinaryQueryImpl instance=p.getLookup().lookup(JavadocForBinaryQueryImpl.class);
        assertNotNull(instance);
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        projDir.getParent().createFolder("doc2");
        ep.setProperty("libs.classpath","../doc2");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        
        JavadocForBinaryQuery.Result result=instance.findJavadoc(projDir.getURL());
        assertNotNull(result);
        URL roots[]=result.getRoots();
        assertNotNull(roots);
        assertTrue(roots.length==0);
        
        result=instance.findJavadoc(aph.resolveFile("dist").toURL());
        assertNotNull(result);
        roots=result.getRoots();
        assertNotNull(roots);
        assertTrue(roots.length>0);
        
        assertEquals(roots[0].getFile(),projDir.getURL().getFile()+"dist/doc/");
        
        //To cover property changes listener
        ChangeListener list=new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            }
        };
        result.addChangeListener(list);
        
        
        ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("libs.classpath","../doc");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        
        result.removeChangeListener(list);
    }
    
    /**
     * Test of getStatus method, of class org.netbeans.modules.mobility.project.queries.FileBuiltQueryImpl.
     */
    public void testGetStatus() throws Exception {
        System.out.println("getStatus");
        J2MEProject p= (J2MEProject)ProjectManager.getDefault().findProject(projDir);
        FileBuiltQueryImpl instance =p.getLookup().lookup(FileBuiltQueryImpl.class);
        assertNotNull(instance);
        FileObject fo=projDir.getFileObject("src/hello/Midlet.java");
        FileBuiltQuery.Status result = instance.getStatus(projDir);
        assertNull(result);
        result = instance.getStatus(fo);
        assertNotNull(result);
        //To cover property changes listener
        ChangeListener list=new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
            }
        };
        result.addChangeListener(list);
        boolean res=result.isBuilt();
        assertFalse(res);
        J2MEActionProvider actionProvider = p.getLookup().lookup(J2MEActionProvider.class);
        DataObject dobj=DataObject.find(fo);
        assertNotNull(dobj);
        Lookup context = Lookups.fixed(new DataObject[] {dobj});
        String ret=null;
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_COMPILE_SINGLE,context);
            ret=TestUtil.waitFinished();
        }
        //Check for the build exception
        assertNull(ret,ret);
        
        EditableProperties ep=aph.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty("src.dir","src2");
        aph.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        
        
        //And now extend test coverage
        res=result.isBuilt();
        assertTrue(res);
        String s=result.toString();
        assertNotNull(s);
        assertTrue(s.indexOf(dobj.getName())!=-1);
        dobj.rename("NewMidlet.java");
        result = instance.getStatus(dobj.getPrimaryFile());
        assertNotNull(result);
        res=result.isBuilt();
        assertFalse(res);
        //Let the RequestProcessor finish its tasks
        Thread.sleep(10);
        dobj.delete();
        
        result.removeChangeListener(list);
    }
}
