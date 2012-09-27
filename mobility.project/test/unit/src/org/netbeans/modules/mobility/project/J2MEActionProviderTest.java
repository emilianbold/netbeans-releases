/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

/*
 * J2MEActionProviderTest.java
 * JUnit based test
 *
 * Created on April 21, 2005, 1:21 PM
 */
package org.netbeans.modules.mobility.project;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import junit.framework.*;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.classpath.J2MEProjectClassPathExtender;
import org.netbeans.modules.project.ui.OpenProjectList;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Michal Skvor & Lukas Waldmann
 */
public class J2MEActionProviderTest extends NbTestCase {
    
    static final Object syncObj=new Object();
    static
    {
        TestUtil.setLookup( new Object[] {
            //TestUtil.testProjectFactory(),
            TestUtil.testFileLocator(),
            TestUtil.testLogger(J2MEActionProvider.COMMAND_COMPILE_SINGLE)
        }, J2MEActionProvider.class.getClassLoader());
        
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
    
    public J2MEActionProviderTest(String testName) {
        super(testName);
        TestUtil.setEnv();
    }
    
    J2MEActionProvider actionProvider;
    AntProjectHelper antProjectHelper;
    FileObject sources;
    
    private FileObject scratchDir;
    private FileObject projectDir;
    private DataFolder sourcePkg1;
    private DataFolder sourcePkg2;
    private DataObject someSource1;
    private DataObject someSource2;
    private DataObject someSource3;
    private final String LCP="${reference.xx.xx.1}:/xxx/xxx:${libs.x}/xxxx.xxx";
    
    void waitFinished(FileObject fo) {
        try {
            File f = FileUtil.toFile(fo);
            assert f != null;
            File waitFor = new File(f, "nbproject/project.properties");
            int ct = 10;
            while (!waitFor.exists() && ct++ < 100) {
                Thread.sleep(200);
            }
            //Make sure it's initialized
            Thread.sleep(200);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        
        scratchDir = TestUtil.makeScratchDir( this );
        projectDir = scratchDir.createFolder( "testProject" );
        File build=File.createTempFile("build",".properties",FileUtil.toFile(projectDir));
        System.setProperty("user.properties.file",build.getAbsolutePath());
        
        synchronized(syncObj) {
            antProjectHelper =
                J2MEProjectGenerator.createNewProject(
                FileUtil.toFile( projectDir ), "testProject", null, null, null );
            waitFinished(projectDir);
        }
        TestUtil.setHelper(antProjectHelper);
        /* Set classpath to fake midp */
        EditableProperties props=antProjectHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        props.setProperty("platform.bootclasspath",getGoldenFile("midpapi20.zip").getAbsolutePath());
        props.setProperty("manifest.midlets","MIDlet-1: Main, , foo.Main\n");
        props.setProperty("manifest.others","MIDlet-Vendor: Vendor\nMIDlet-Version: 1.0\nMIDlet-Name: Test\n");
        props.setProperty("platform.apis","MMAPI-1.0,WMA-1.1,wtklib/customjmf.jar,wtklib/kvem.jar,wtklib/kenv.zip,wtklib/lime.jar,wtklib/ktools.zip,wtklib/kdp.jar,wtklib/gcf-op.jar");

        /* Set classpath to fake classpath */
        props.setProperty("libs.classpath",LCP);
        
        antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,props);
        
        ProjectManager pm = ProjectManager.getDefault();
        Project p = pm.findProject( projectDir );
        assertNotNull (p);
        
        actionProvider = p.getLookup().lookup(J2MEActionProvider.class);
        assertNotNull( actionProvider );
        ProjectManager.getDefault().saveAllProjects();
        
        sources = projectDir.getFileObject( "src" );
        
        FileObject pkg = sources.createFolder( "foo" );
        sourcePkg1 = DataFolder.findFolder(pkg);
        FileObject fo = pkg.createData("Boo1.java");
        createBoo(fo,"1");
        
        someSource1 = DataObject.find(fo);
        fo = sources.getFileObject("foo").createData("Main.java");
        createMain(fo);
        someSource2 = DataObject.find(fo);
        fo = sources.getFileObject("foo").createData("Boo2.java");
        createBoo(fo,"2");
        someSource3 = DataObject.find(fo);
        
        pkg = sources.createFolder("foo2");
        sourcePkg2 = DataFolder.findFolder(pkg);
        fo = sources.getFileObject("foo2").createData("Boo3.java");
        createBooEx(fo,"3");
        
        
        assertNotNull(someSource1);
        assertNotNull(someSource2);
        assertNotNull(someSource3);
        assertNotNull(DataObject.find(fo));
        OpenProjectList.getDefault().close(OpenProjectList.getDefault().getOpenProjects(), true);        
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(J2MEActionProviderTest.class);
        
        return suite;
    }
    
    private void createMain(FileObject fo) throws Exception {
        FileLock lock = fo.lock();
        PrintWriter pw = new PrintWriter(fo.getOutputStream(lock));
        pw.println("package foo;");
        pw.println("import javax.microedition.midlet.MIDlet;");
        pw.println("public class Main extends MIDlet { public static void main(String[] args){System.out.println(\"main\"); }");
        pw.println("public void startApp() {} public void pauseApp() {} public void destroyApp(boolean un) {}}");
        pw.flush();
        pw.close();
        lock.releaseLock();
    }
    
    private void createBoo(FileObject fo,String par) throws Exception {
        FileLock lock = fo.lock();
        PrintWriter pw = new PrintWriter(fo.getOutputStream(lock));
        pw.println("package foo;");
        pw.println("import javax.microedition.midlet.MIDlet;");
        pw.println("public class Boo"+par+" extends MIDlet { public void main(String[] args){System.out.println(\"Boo"+par+"\"); }");
        pw.println("public void startApp() {} public void pauseApp() {} public void destroyApp(boolean un) {}}");
        pw.flush();
        pw.close();
        lock.releaseLock();
    }
    
    private void createBooEx(FileObject fo,String par) throws Exception {
        FileLock lock = fo.lock();
        PrintWriter pw = new PrintWriter(fo.getOutputStream(lock));
        pw.println("package foo2;");
        pw.println("import javax.microedition.midlet.MIDlet;");
        pw.println("public class Boo"+par+" extends MIDlet { public void main(String[] args){System.out.println(\"Boo"+par+"\"); }");
        pw.println("public void startApp() {} public void pauseApp() {} public void destroyApp(boolean un) {}}");
        pw.flush();
        pw.close();
        lock.releaseLock();
    }
    
    
    public void testEncodeUrl() throws Exception {
        Class cl=Class.forName("org.netbeans.modules.mobility.project.J2MEActionProvider");
        Class par[]=new Class[] { String.class };
        Method m=cl.getDeclaredMethod("encodeURL",par);
        Object p[]=new Object[] {"/servlet/org.netbeans.modules.mobility.project.jam.JAMServlet/"};
        m.setAccessible(true);
        String res=(String)m.invoke(actionProvider,p);
        assertEquals(p[0],res);
    }
    
    public void testOtherOperations() throws IOException {
        HashMap m1=new HashMap();
        m1.put("build.xml","build.xml");
        m1.put("nbproject","nbproject");
        J2MEProject p= (J2MEProject)ProjectManager.getDefault().findProject(projectDir);
        ReferenceHelper refs = p.getLookup().lookup(ReferenceHelper.class);
        assertNotNull( actionProvider );
        J2MEProjectOperations op=new J2MEProjectOperations(p,antProjectHelper,refs,actionProvider);
        List l1=op.getDataFiles();
        assertTrue(l1.size()==1);
        assertTrue( ((FileObject)(l1.get(0))).getName().equals("src"));
        List l2=op.getMetadataFiles();
        assertTrue(l2.size()==2);
        for (int i=0;i<l2.size();i++) {
            String name=((FileObject)(l2.get(0))).getName();
            assertNotNull(m1.get(name));
        }
        
        try {
            
            ProjectOperations.notifyMoving(p);
            FileObject xdir=projectDir.getParent().createFolder("Moved");
            FileObject cpdir=projectDir.getParent().createFolder("Copied");
            
            ProjectOperations.notifyCopying(p);
            TestUtil.cpDir(projectDir,cpdir);
            J2MEProject newP = (J2MEProject)ProjectManager.getDefault().findProject(cpdir);
            ProjectOperations.notifyCopied(p,newP,FileUtil.toFile(projectDir),"Copied");
            assertTrue(ProjectManager.getDefault().isValid(p));
            assertTrue(ProjectManager.getDefault().isValid(newP));
            
            TestUtil.cpDir(projectDir,xdir);
            ProjectManager.getDefault().clearNonProjectCache();
            newP = (J2MEProject)ProjectManager.getDefault().findProject(xdir);
            ProjectOperations.notifyMoved(p, newP, FileUtil.toFile(projectDir), "Moved");
            assertTrue(!ProjectManager.getDefault().isValid(p));
            assertTrue(ProjectManager.getDefault().isValid(newP));
            AntProjectHelper hp=newP.getLookup().lookup(AntProjectHelper.class);
            TestUtil.setHelper(hp);
            /* try to add another root for better code coverage */
            EditableProperties ep=hp.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            ep.setProperty("src.dir",System.getProperty("java.io.tmpdir"));
            hp.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
            new J2MEProject(hp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    
    public void testHooks() throws IOException {
        J2MEProject p= (J2MEProject)ProjectManager.getDefault().findProject(projectDir);
        OpenProjectList.getDefault().open(p,true);
        Project prj[]=OpenProjectList.getDefault().getOpenProjects();
        
        for (Project p1:prj)
            System.out.println(((J2MEProject)(p)).getName());
        
        assertTrue(prj.length == 1);
        assertTrue(prj[0]==p);
        OpenProjectList.getDefault().close(new Project[] {p},false);
        prj=OpenProjectList.getDefault().getOpenProjects();
        assertTrue(prj.length == 0);
        AntArtifactProvider ant=p.getLookup().lookup(AntArtifactProvider.class);
        AntArtifact a[]=ant.getBuildArtifacts();
        assertTrue(a.length==1);
        assertTrue(a[0].getProject()==p);
        assertTrue(a[0].getArtifactLocations().length==1);
        assertTrue(a[0].getArtifactLocations()[0].getPath().equals("dist/testProject.jar"));
        assertTrue("jar".equals(a[0].getTargetName()));
        assertTrue(JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a[0].getType()));
        assertTrue("".equals(a[0].getProperties().getProperty("config.active",null)));
        assertTrue(a[0].getID()!=null);
        
        EditableProperties ep=antProjectHelper.getProperties(antProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.clear();
        antProjectHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
        OpenProjectList.getDefault().open(p,true);
        prj=OpenProjectList.getDefault().getOpenProjects();
        assertTrue(prj.length == 1);
        assertTrue(prj[0]==p);
        ep=antProjectHelper.getProperties(antProjectHelper.PROJECT_PROPERTIES_PATH);
        assertEquals(ep.getProperty(DefaultPropertiesDescriptor.JAVAC_ENCODING),System.getProperty("file.encoding"));
    }
    
    public void testSupportedActions() {
        String act[]=actionProvider.getSupportedActions();
        assertTrue(act.length>11);
    }

    public void testActions() throws Exception {
        Lookup context;
        String[] targets;
        String result;
        HashMap map1=new HashMap(3);
        HashMap map2=new HashMap(4);
        HashMap map3=new HashMap(2);
        
        map1.put("Main.class","Main.class");
        map1.put("Boo1.class","Boo1.class");
        map1.put("Boo2.class","Boo2.class");
        
        map2.put("foo/Main.class","Main.class");
        map2.put("foo/Boo1.class","Boo1.class");
        map2.put("foo/Boo2.class","Boo2.class");
        map2.put("foo2/Boo3.class","Boo3.class");
        
        map3.put("testProject.jar","testProject.jar");
        map3.put("testProject.jad","testProject.jad");
        
        actionProvider.isActionEnabled("",null);
        // Test for COMMAND_COMPILE_SINGLE
        context = Lookups.fixed( someSource2 );
        targets = actionProvider.getTargetNames( J2MEActionProvider.COMMAND_COMPILE_SINGLE);
        assertNotNull( "must found some targets for COMMAND_COMPILE_SINGLE", targets );
        assertEquals("There must be one target for COMMAND_COMPILE_SINGLE", 1, targets.length);
        assertEquals("Unexpected target name", "compile-single", targets[0]);
        
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_COMPILE_SINGLE,context);
            result=TestUtil.waitFinished();
        }

        // Below here will not run because unit tests are running against
        //JRE and ant cannot find a compiler
        /*

        String path = "build/compiled/foo";
        FileObject compileDir = projectDir.getFileObject ("build/compiled/foo");
        assertNotNull ("Compilation dir " + FileUtil.toFile (projectDir).getPath() +  path + " does not exist", compileDir);
        
        //Check if only Main class was compiled
        File files[]=FileUtil.toFile(projectDir.getFileObject("build/compiled/foo/")).listFiles();
        for (int i=0;i<files.length;i++) {
            assertEquals(files[i].getName(),"Main.class");
        }
        
        context = Lookups.fixed(sourcePkg1 );
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_COMPILE_SINGLE,context);
            result=TestUtil.waitFinished();
        }
        //Check for the build exception
        assertNull(result,result);
        
        //Check if only Main class was compiled
        files=FileUtil.toFile(projectDir.getFileObject("build/compiled/foo/")).listFiles();
        for (int i=0;i<files.length;i++) {
            assertNotNull(map1.remove(files[i].getName()));
        }
        assertEquals(map1.size(),0);
        
        context = Lookups.fixed(sourcePkg1, sourcePkg2);
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_COMPILE_SINGLE,context);
            result=TestUtil.waitFinished();
        }
        //Check for the build exception
        assertNull(result,result);
        
        //Check if only Main class was compiled
        projectDir.getFileSystem().refresh(true);
        Enumeration en=projectDir.getFileObject("build/compiled/").getData(true);
        while (en.hasMoreElements()) {
            FileObject fobj=(FileObject)en.nextElement();
            if (!fobj.isFolder())
                assertNotNull(map2.remove(fobj.getParent().getName()+"/"+fobj.getNameExt()));
        }
        assertEquals(map2.size(),0);
        
        // Now try deploy
        targets = actionProvider.getTargetNames( J2MEActionProvider.COMMAND_DEPLOY);
        assertNotNull( "must found some targets for COMMAND_DEPLOY", targets );
        assertEquals("There must be one target for COMMAND_DEPLOY", 1, targets.length);
        assertEquals("Unexpected target name", "deploy", targets[0]);
        
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_DEPLOY,null);
            result=TestUtil.waitFinished();
        }
        //Check for the build exception
        assertNull(result,result);
        //Check if dist was created
        files=FileUtil.toFile(projectDir.getFileObject("dist")).listFiles();
        for (int i=0;i<files.length;i++) {
            assertNotNull(map3.remove(files[i].getName()));
        }
        assertEquals(map3.size(),0);
        
        
        // And what about javadoc
        targets = actionProvider.getTargetNames( J2MEActionProvider.COMMAND_JAVADOC);
        assertNotNull( "must found some targets for COMMAND_JAVADOC", targets );
        assertEquals("There must be one target for COMMAND_JAVADOC", 1, targets.length);
        assertEquals("Unexpected target name", "javadoc", targets[0]);
        
        context = Lookups.fixed(someSource1 );
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_JAVADOC,context);
            result=TestUtil.waitFinished();
        }
        //Check for the build exception
        assertNull(result,result);
        //Check for docs
        files=FileUtil.toFile(projectDir.getFileObject("dist/doc")).listFiles();
        assertNotNull(files);
        assertTrue(files.length>0);
        
        // DEBUG
        boolean excp=false;
        Project pr=ProjectManager.getDefault().findProject(projectDir);
        AntArtifactProvider refs = pr.getLookup().lookup(AntArtifactProvider.class);
        AntArtifact art[]=refs.getBuildArtifacts();
        URI locs[]=art[0].getArtifactLocations();
        ReferenceHelper refsh = pr.getLookup().lookup(ReferenceHelper.class);
        ProjectConfigurationsHelper pcfgh = pr.getLookup().lookup(ProjectConfigurationsHelper.class);
        J2MEProjectClassPathExtender instance = new J2MEProjectClassPathExtender(pr,antProjectHelper,refsh,pcfgh);
        boolean res = instance.addAntArtifact(art[0], locs[0]);
        assertTrue(res);
        try {
            SwingUtilities.invokeAndWait(
                    new Runnable() {
                public void run() {
                    actionProvider.invokeAction(J2MEActionProvider.COMMAND_DEBUG,null);
                }
            }); } catch (Exception ex) {
                excp=true;
            }
        assertTrue(excp);
        
        // Now try clean
        targets = actionProvider.getTargetNames( J2MEActionProvider.COMMAND_CLEAN);
        assertNotNull( "must found some targets for COMMAND_CLEAN", targets );
        assertEquals("There must be one target for COMMAND_CLEAN", 1, targets.length);
        assertEquals("Unexpected target name", "clean", targets[0]);
        
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_CLEAN,null);
            result=TestUtil.waitFinished();
        }
        //Check for the build exception
        assertNull(result,result);
        
        //Check if it was cleaned properly
        assertNotNull(projectDir.getFileObject("dist"));
        files=FileUtil.toFile(projectDir.getFileObject("dist")).listFiles();
        assertNotNull(files);
        assertEquals(files.length,0);
        
        assertNotNull(projectDir.getFileObject("build"));
        files=FileUtil.toFile(projectDir.getFileObject("build")).listFiles();
        assertNotNull(files);
        assertEquals(files.length,0);
         */
        
        /*
         * We can not test clean-all as a dialog is going to be shown
         * 
        // And now CLEAN_ALL
        targets = actionProvider.getTargetNames( J2MEActionProvider.COMMAND_CLEAN_ALL);
        assertNotNull( "must found some targets for COMMAND_CLEAN_ALL", targets );
        assertEquals("There must be one target for COMMAND_CLEAN_ALL", 1, targets.length);
        assertEquals("Unexpected target name", "clean-all", targets[0]);
        
        synchronized(TestUtil.rootStr) {
            actionProvider.invokeAction(J2MEActionProvider.COMMAND_CLEAN_ALL,null);
            result=TestUtil.waitFinished();
        }
        //Check for the build exception
        assertNull(result,result);
        
        //Check if it was cleaned properly
        projectDir.getFileSystem().refresh(true);
        assertNull(projectDir.getFileObject("dist"));
        assertNull(projectDir.getFileObject("build"));
         * */
    }
}
