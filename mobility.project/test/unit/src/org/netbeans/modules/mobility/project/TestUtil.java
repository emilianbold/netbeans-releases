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
 * TestUtil.java
 *
 * Created on April 20, 2005, 11:29 AM
 *
 */
package org.netbeans.modules.mobility.project;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import junit.framework.Assert;
import org.apache.tools.ant.module.spi.AntEvent;
import org.apache.tools.ant.module.spi.AntLogger;
import org.apache.tools.ant.module.spi.AntSession;
import org.netbeans.api.project.Project;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.netbeans.junit.Manager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.cldcplatform.J2MEPlatform;
import org.netbeans.modules.mobility.cldcplatform.UEIEmulatorConfiguratorImpl;
import org.netbeans.modules.mobility.cldcplatform.startup.PostInstallJ2meAction;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.xml.sax.SAXException;


/**
 * Helper class for test bag purposes
 * @author Jesse Glick, Michal Skvor
 */
public class TestUtil extends ProxyLookup {
    
    static public  final   String rootStr="mobility";
    static private String message;
    static private AntProjectHelper p=null;
    static private J2MEPlatform instance=null;
    
    static {
        TestUtil.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestUtil.class.getName());
        Assert.assertEquals(TestUtil.class, Lookup.getDefault().getClass());
    }
    
    private static TestUtil DEFAULT;
    
    /** Creates a new instance of TestUtil */
    public TestUtil() {
        Assert.assertNull(DEFAULT);
        DEFAULT = this;
        setLookups(new Lookup[] {
            Lookups.singleton(TestUtil.class.getClassLoader()),
        });
        System.setProperty("netbeans.user","test/tiredTester");
    }
    
    static public J2MEPlatform getPlatform()
    {
        if (instance==null)
        {
            createPlatform();
            instance = new UEIEmulatorConfiguratorImpl(System.getProperty("platform.home")).getPlatform();
        }
        return instance;
    }
    
    static public void setEnv() {
        getPlatform();
        /*Add the layer file */
        URL res=J2MEProject.class.getClassLoader().getResource("org/netbeans/modules/mobility/project/ui/resources/layer.xml");
        FileSystem fs=null;
        try
        {
            fs = new XMLFileSystem(res);
        } 
        catch (SAXException ex)
        {
            Assert.fail("layer.xml not found");
        }
        
        Collection<FileSystem> layers=new ArrayList(Arrays.asList(((SystemFileSystem)Repository.getDefault().getDefaultFileSystem()).getLayers()));
        layers.add(fs);
        ((SystemFileSystem)Repository.getDefault().getDefaultFileSystem()).setLayers(layers.toArray(new FileSystem[layers.size()]));
        /**************/
        
        final String rootIDE=File.separator+"netbeans"+File.separator+"ide8";
        final String rootAnt=File.separator+"java1"+File.separator+"ant";
        /* Hack to get ant directories */
        String classPath=System.getProperty("java.class.path");
        int index=classPath.indexOf(rootIDE);
        int id1=classPath.lastIndexOf(File.pathSeparator,index)==-1?0:classPath.lastIndexOf(File.pathSeparator,index)+1;
        String path=classPath.substring(id1,index+rootIDE.length());
        String root=new File(path).getParent();
        
        String antExt="org-netbeans-modules-mobility-antext.jar";        
        URL url=PostInstallJ2meAction.class.getProtectionDomain().getCodeSource().getLocation();
        String antLib=new File(url.getFile()).getParent()+File.separator+antExt;
        /*****************************/
        NbTestCase.assertTrue("Ant environment not found",new File(root+rootAnt).exists());
        System.setProperty("test.ant.home",root+rootAnt);
        System.setProperty("test.bridge.jar","${test.ant.home}/nblib/bridge.jar");
        //System.setProperty("libs.j2me_ant_ext.classpath",root+root Ext);
        System.setProperty("libs.j2me_ant_ext.classpath",antLib);
        System.setProperty("libs.ant-contrib.classpath","${netbeans.dest.dir}/${cluster}/modules/ext/ant-contrib-1.0b3.jar");
        Logger.getLogger("org.openide.util.RequestProcessor").addHandler(new Handler() {
            public void publish(LogRecord record) {
                String s=record.getMessage();
                if (s==null)
                    return;
                if (s.startsWith("Work finished Inactive RequestProcessor") &&
                        s.indexOf("org.netbeans.modules.mobility.project.ApplicationDescriptorHandler$1")!=-1 &&
                        s.indexOf("RequestProcessor")!=-1) {
                    synchronized (rootStr) {
                        rootStr.notify();
                    }
                }
            }
            public void flush() {}
            public void close() throws SecurityException {}
        });
        Logger.getLogger("org.openide.util.RequestProcessor").setLevel(Level.FINE);
    }
    
    /**
     * Set the global default lookup.
     */
    public static void setLookup(Lookup l) {
        DEFAULT.setLookups(new Lookup[] {l});
    }
    
    public static void setLookup(Lookup[] l) {
        DEFAULT.setLookups(l);
    }
    
    /**
     * Set the global default lookup with some fixed instances including META-INF/services/*.
     */
    public static void setLookup(Object[] instances, ClassLoader cl) {
        DEFAULT.setLookups(new Lookup[] {
            Lookups.fixed(instances),
            Lookups.metaInfServices(cl),
            Lookups.singleton(cl),
        });
    }
    
    public static void setHelper(AntProjectHelper hlp) {
        p=hlp;
    }
    /**
     * Create a testing project factory which recognizes directories containing
     * a subdirectory called "testproject".
     * If that subdirectory contains a file named "broken" then loading the project
     * will fail with an IOException.
     */
    public static ProjectFactory testProjectFactory() {
        return new TestProjectFactory();
    }
    
    public static AntLogger testLogger(String command) {
        return new MyAntLogger(command);
    }
    
    public static ErrorManager testErrManager() {
        return new MyErrorManager();
    }
    
    public static InstalledFileLocator testFileLocator() {
        return new IFL();
    }
    
    public static ProjectChooserFactory testProjectChooserFactory() {
        return new TestProjectChooserFactory();
    }
    
    
    private static void createPlatform() {
        final String rootWTK=File.separator+"test"+File.separator+"emulators"+File.separator;
        final String rootMobility=File.separator+rootStr+File.separator;
        String wtkStr="wtk22";
        String destPath=Manager.getWorkDirPath();
        String osarch=System.getProperty("os.name",null);
        String ossuf=null;
        NbTestCase.assertNotNull(osarch);
        if (osarch.toLowerCase().indexOf("windows")!=-1)
            ossuf="22_win";
        else if (osarch.toLowerCase().indexOf("linux")!=-1)
            ossuf="22_linux";
        else if (osarch.toLowerCase().indexOf("sunos")!=-1) {
            /* For Solaris we have just wtk21 */
            ossuf="21_sunos";
            wtkStr="wtk21";
        } else
            NbTestCase.fail("Operating system architecture: "+osarch+" not supported");
        
        ZipFile zip=null;
        String zipPath=null;
        String wtkPath=System.getProperty("wtk.base.dir");
        if (wtkPath==null)
        {
            /* Get a path to wtk - dirty hack but I don't know any better way */
            String classPath=System.getProperty("java.class.path");
            String platPath;
            int index=classPath.indexOf(rootMobility);
            if (index==-1) {
                /* Running as a part of validity test */
                String s2=Manager.getWorkDirPath();
                platPath=new File(s2).getParentFile().getParentFile().getParentFile().getParent()+rootMobility+rootWTK;
            } else {
                /* Running as a part of xtest test */
                int id1=classPath.lastIndexOf(File.pathSeparator,index)==-1?0:classPath.lastIndexOf(File.pathSeparator,index)+1;
                platPath=classPath.substring(id1,index+rootMobility.length())+rootWTK;
            }
            zipPath=platPath+"wtk"+ossuf+".zip";
        }
        else
            zipPath=wtkPath;
        
        try {   
            zip = new ZipFile(zipPath);
            Enumeration files = zip.entries();
            while (files.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) files.nextElement();
                if (entry.isDirectory())
                    new File(destPath, entry.getName()).mkdirs();
                else {
                    /* Extract only if not already present */
                    File test=new File(destPath+"/"+entry.getName());
                    if (!(test.isFile() && test.length() == entry.getSize()))
                        copy(zip.getInputStream(entry), entry.getName(), new File(destPath));
                }
            }

            //Modify scripts
            NbTestCase.assertTrue(destPath+File.separator+"emulator"+File.separator+wtkStr,new File(destPath+File.separator+"emulator"+File.separator+wtkStr).exists());
            PostInstallJ2meAction.installAction(destPath+File.separator+"emulator"+File.separator+wtkStr);

            if (osarch.indexOf("Windows")==-1)
                java.lang.Runtime.getRuntime().exec("chmod -R +x "+destPath+File.separator+"emulator"+File.separator+
                        wtkStr+File.separator+"bin");
        } catch (IOException ex) {
            NbTestCase.assertTrue("WTK zip file ("+zipPath+") not found or corrupted. Please add the correct zip file to run the tests",false);
        }
        finally {
            if (zip != null) try { zip.close(); } catch (IOException e) {}
        }
        System.setProperty("platform.home",destPath+File.separator+"emulator"+File.separator+wtkStr);
        System.setProperty("platform.type","UEI-1.0");
    }
    
    public static void removePlatform(Class clazz) {
        rmdir(new File(Manager.getWorkDirPath() + File.separator + clazz.getName()+File.separator+"emulator"));
    }
    
    /**
     * waitFinished
     * must be called from the section synchronized on TestUtil.rootStr
     * @return String of error message or null
     */
    public static String waitFinished() {
        assert Thread.currentThread().holdsLock(rootStr);
        
        while (true)
        {
            try {
                rootStr.wait();
                break;
            } catch (InterruptedException ex) {}
        }
        return message!=null?new String(message):null;
    }
    
    private static void  rmdir(File dir) {
        if (dir.isDirectory()) {
            File list[]=dir.listFiles();
            for (int i=0;i<list.length;i++) {
                if (list[i].isDirectory()) rmdir(list[i]);
                else   list[i].delete();
            }
            dir.delete();
        }
    }
    
    
    private static void copy(InputStream input, String file, File target) throws IOException {
        if (input == null  ||  file == null  ||  "".equals(file)) //NOI18N
            return;
        File output = new File(target, file);
        output.getParentFile().mkdirs();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(output);
            copy(input, fos);
        } finally {
            if (input != null) try { input.close(); } catch (IOException e) {}
            if (fos != null) try { fos.close(); } catch (IOException e) {}
        }
    }
    
    private static void copy(InputStream is, OutputStream os) throws IOException {
        final byte[] BUFFER = new byte[4096];
        int len;
        
        for (;;) {
            len = is.read(BUFFER);
            if (len == -1) return;
            os.write(BUFFER, 0, len);
        }
    }
    
    static public void cpDir(FileObject source,FileObject target) throws IOException {
        FileObject ch[]=source.getChildren();
        for (int i=0;i<ch.length;i++)
            if (ch[i].isFolder())
                cpDir(ch[i],target.createFolder(ch[i].getNameExt()));
            else
                ch[i].copy(target,ch[i].getName(),ch[i].getExt());
    }
    
    static void deleteProject(J2MEProject project) {
        FileObject projectFolder = project.getProjectDirectory();
        
        try {
            ProjectOperations.notifyDeleting(project);
            projectFolder.delete();
            ProjectOperations.notifyDeleted(project);
        } catch (Exception e) {
        }
    }
    
    /**
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
    /**
     * Delete a file and all subfiles.
     */
    public static void deleteRec(File f) throws IOException {
        if (f.isDirectory()) {
            File[] kids = f.listFiles();
            if (kids == null) {
                throw new IOException("List " + f);
            }
            for (int i = 0; i < kids.length; i++) {
                deleteRec(kids[i]);
            }
        }
        if (!f.delete()) {
            throw new IOException("Delete " + f);
        }
    }
    
    private static final class TestProjectChooserFactory implements ProjectChooserFactory {
        
        File file;
        
        public javax.swing.JFileChooser createProjectChooser() {
            return null;
        }
        
        public org.openide.WizardDescriptor.Panel createSimpleTargetChooser(Project project, org.netbeans.api.project.SourceGroup[] folders, org.openide.WizardDescriptor.Panel bottomPanel) {
            return null;
        }
        
        public File getProjectsFolder() {
            return file;
        }
        
        public void setProjectsFolder(File file) {
            this.file = file;
        }
        
    }
    
    /**
     * Testing project factory
     */
    private static final class TestProjectFactory implements ProjectFactory {
        
        public boolean isProject(FileObject projectDirectory) {
            return false;
        }
        
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws java.io.IOException {
//            return new TestProject(projectDirectory, state);
            return null;
        }
        
        public void saveProject(Project project) throws java.io.IOException, ClassCastException {
        }
    }
    
    /**
     * Testing Project
     */
    private static final class TestProject implements Project {
        
        private final FileObject dir;
        final ProjectState state;
        
        public TestProject(FileObject dir, ProjectState state) {
            this.dir = dir;
            this.state = state;
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public FileObject getProjectDirectory() {
            return dir;
        }
        
        public String toString() {
            return "testproject:" + getProjectDirectory().getNameExt();
        }
    }
    
    /**
     *    AntLogger
     */
    private final static class MyAntLogger extends AntLogger {
        final String command;
        
        MyAntLogger(String comm) {
            command=comm;
        }
        
        public void buildFinished(AntEvent event) {
            synchronized (rootStr) {
                message=event.getException()!=null?event.getException().getMessage():null;
                rootStr.notify();
            }
        }
        
        public void buildInitializationFailed(AntEvent event) {
            synchronized (rootStr) {
                message=event.getException()!=null?event.getException().getMessage():null;
                rootStr.notify();
            }
        }
        
        public String[] interestedInTargets(AntSession session) {
            return new String[]{command};
        }
        public boolean interestedInSession(AntSession session) {
            
            return true;
        }
    };
    
    private final static class MyErrorManager extends ErrorManager {
        public void log(int severity, String s) {
            if (severity==ErrorManager.INFORMATIONAL && s.startsWith("Work finished Inactive RequestProcessor") &&
                    s.indexOf("org.netbeans.modules.mobility.project.ApplicationDescriptorHandler$1")!=-1 &&
                    s.indexOf("RequestProcessor")!=-1) {
                synchronized (rootStr) {
                    rootStr.notify();
                }
            }
        }
        
        public ErrorManager getInstance(String name) {
            if (name.startsWith("org.openide.util.RequestProcessor"))
                return new MyErrorManager();
            return ErrorManager.getDefault();
        }
        
        public Throwable attachAnnotations(Throwable t, ErrorManager.Annotation[] arr) {
            return t;
        }
        
        public ErrorManager.Annotation[] findAnnotations(Throwable t) {
            return null;
        }
        
        public Throwable annotate(Throwable t, int severity, String message, String localizedMessage, Throwable stackTrace, Date date) {
            return t;
        }
        
        public void notify(int severity, Throwable t) {
        }
    }
    
    private final static class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String rPath, String codeNameBase, boolean localized) {
            File f=new File(rPath);
            String relativePath=f.getPath();
            if (relativePath.equals("ant"+File.separator+"nblib"+File.separator+"bridge.jar")) {
                String path = System.getProperty("test.bridge.jar");
                NbTestCase.assertNotNull("must set test.bridge.jar", path);
                if (p!=null)
                    path=p.getStandardPropertyEvaluator().evaluate(path);
                return new File(path);
            } else if (relativePath.equals("ant")) {
                String path = System.getProperty("test.ant.home");
                NbTestCase.assertNotNull("must set test.ant.home", path);
                if (p!=null)
                    path=p.getStandardPropertyEvaluator().evaluate(path);
                return new File(path);
            } else if (relativePath.startsWith("ant"+File.separator)) {
                String path = System.getProperty("test.ant.home");
                NbTestCase.assertNotNull("must set test.ant.home", path);
                if (p!=null)
                    path=p.getStandardPropertyEvaluator().evaluate(path);
                return new File(path, relativePath.substring(4).replace('/', File.separatorChar));
            }  else if (relativePath.startsWith("modules"+File.separator+"ext"+File.separator+"junit-")) {
                String path="${netbeans.dest.dir}"+File.separator+relativePath;
                if (p!=null)
                    path=p.getStandardPropertyEvaluator().evaluate(path);
                return new File(path);
            } else {
                return null;
            }
        }
    }
    
}
