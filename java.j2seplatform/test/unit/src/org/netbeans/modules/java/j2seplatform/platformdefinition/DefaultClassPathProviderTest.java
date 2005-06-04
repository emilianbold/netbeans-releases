/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.platformdefinition;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.DataOutputStream;
import java.net.URL;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.SourceForBinaryQuery;



import org.netbeans.api.project.TestUtil;
import org.netbeans.core.startup.layers.ArchiveURLMapper;



import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.MasterURLMapper;

import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;


import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;


import org.openide.util.lookup.Lookups;


/**
 *
 * @author  tom
 */
public class DefaultClassPathProviderTest extends NbTestCase implements Lookup.Provider {
    
    private static final int FILE_IN_PACKAGE = 0;
    private static final int FILE_IN_BAD_PACKAGE = 1;
    private static final int FILE_IN_DEFAULT_PACKAGE = 2;

    private static final byte[] CLASS_FILE_DATA = {
        (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe, 0x00, 0x00, 0x00, 0x2e, 0x00, 0x0d, 0x0a, 0x00, 0x03, 0x00, 0x0a, 0x07, 0x00, 0x0b, 0x07, 0x00,
        0x0c, 0x01, 0x00, 0x06, 0x3c, 0x69, 0x6e, 0x69, 0x74, 0x3e, 0x01, 0x00, 0x03, 0x28, 0x29, 0x56, 0x01, 0x00, 0x04, 0x43,
        0x6f, 0x64, 0x65, 0x01, 0x00, 0x0f, 0x4c, 0x69, 0x6e, 0x65, 0x4e, 0x75, 0x6d, 0x62, 0x65, 0x72, 0x54, 0x61, 0x62, 0x6c,
        0x65, 0x01, 0x00, 0x0a, 0x53, 0x6f, 0x75, 0x72, 0x63, 0x65, 0x46, 0x69, 0x6c, 0x65, 0x01, 0x00, 0x09, 0x54, 0x65, 0x73,
        0x74, 0x2e, 0x6a, 0x61, 0x76, 0x61, 0x0c, 0x00, 0x04, 0x00, 0x05, 0x01, 0x00, 0x09, 0x74, 0x65, 0x73, 0x74, 0x2f, 0x54,
        0x65, 0x73, 0x74, 0x01, 0x00, 0x10, 0x6a, 0x61, 0x76, 0x61, 0x2f, 0x6c, 0x61, 0x6e, 0x67, 0x2f, 0x4f, 0x62, 0x6a, 0x65,
        0x63, 0x74, 0x00, 0x21, 0x00, 0x02, 0x00, 0x03, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00, 0x04, 0x00, 0x05,
        0x00, 0x01, 0x00, 0x06, 0x00, 0x00, 0x00, 0x1d, 0x00, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00, 0x05, 0x2a, (byte)0xb7, 0x00, 0x01,
        (byte)0xb1, 0x00, 0x00, 0x00, 0x01, 0x00, 0x07, 0x00, 0x00, 0x00, 0x06, 0x00, 0x01, 0x00, 0x00, 0x00, 0x03, 0x00, 0x01, 0x00,
        0x08, 0x00, 0x00, 0x00, 0x02, 0x00, 0x09
    };

    private FileObject srcRoot;
    private FileObject[] srcFile = new FileObject[3];
    private FileObject[] compileRoots;
    private FileObject[] execRoots;
    private FileObject[] libSourceRoots;
    private FileObject execTestDir;
    private Lookup lookup;
    
    /** Creates a new instance of DefaultClassPathProviderTest */
    public DefaultClassPathProviderTest (String testName) {
        super (testName);
        TestUtil.setLookup(Lookups.proxy(this));
    }
    
    
    protected void tearDown () throws Exception {
        this.srcRoot = null;
        this.compileRoots = null;
        super.tearDown();
    }
    
    
    protected void setUp() throws Exception {
        this.clearWorkDir();
        super.setUp();
        FileObject workDir = FileUtil.toFileObject(this.getWorkDir());
        assertNotNull("MasterFS is not configured.", workDir);
        this.srcRoot = workDir.createFolder("src");
        this.compileRoots = new FileObject[3];
        for (int i=0; i< this.compileRoots.length; i++) {
            this.compileRoots[i] = workDir.createFolder("lib_"+Integer.toString(i));
        }
        ClassPath cp = ClassPathSupport.createClassPath(this.compileRoots);
        GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, new ClassPath[] {cp});
        this.execRoots = new FileObject[2];
        this.execRoots[0] = this.compileRoots[2];
        this.execRoots[1] = workDir.createFolder("lib_OnlyExec");
        cp = ClassPathSupport.createClassPath(this.execRoots);
        GlobalPathRegistry.getDefault().register (ClassPath.EXECUTE, new ClassPath[]{cp});
        this.libSourceRoots = new FileObject[2];
        for (int i=0; i< libSourceRoots.length; i++) {
            this.libSourceRoots[i] = workDir.createFolder ("libSrc_"+Integer.toString(i));
        }
        cp = ClassPathSupport.createClassPath (this.libSourceRoots);
        GlobalPathRegistry.getDefault().register (ClassPath.SOURCE, new ClassPath[]{cp});
        execTestDir = workDir.createFolder("exec");
    }
    
    
    
    public void testFindClassPath () throws IOException {
        FileObject artefact = getSourceFile (FILE_IN_PACKAGE);
        ClassPathProvider cpp = new DefaultClassPathProvider ();
        ClassPath cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {this.srcRoot});
        cp = cpp.findClassPath(artefact, ClassPath.COMPILE);        
        assertNotNull ("DefaultClassPathProvider returned null for COMPILE",cp);
        assertEquals("Invalid length of classpath for COMPILE",this.compileRoots.length + 1, cp.getRoots().length);
        FileObject[] resRoots = new FileObject[this.compileRoots.length + 1];
        System.arraycopy(this.compileRoots,0,resRoots,0,this.compileRoots.length);
        resRoots[this.compileRoots.length] = this.execRoots[1];
        assertRootsEquals ("Invalid classpath roots for COMPILE", cp, resRoots);
        cp = cpp.findClassPath(artefact, ClassPath.BOOT);
        assertNotNull ("DefaultClassPathProvider returned null for BOOT",cp);
        JavaPlatform dp = JavaPlatformManager.getDefault().getDefaultPlatform();
        assertEquals("Invalid length of classpath for BOOT",dp.getBootstrapLibraries().getRoots().length, cp.getRoots().length);
        assertRootsEquals ("Invalid classpath roots for BOOT", cp, dp.getBootstrapLibraries().getRoots());

        artefact = getSourceFile (FILE_IN_DEFAULT_PACKAGE);
        cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {this.srcRoot});
        
        artefact = getSourceFile (FILE_IN_BAD_PACKAGE);
        cp = cpp.findClassPath(artefact, ClassPath.SOURCE);
        assertNotNull ("DefaultClassPathProvider returned null for SOURCES",cp);
        assertEquals("Invalid length of classpath for SOURCE",1,cp.getRoots().length);
        FileObject badRoot = this.srcRoot.getFileObject ("test");
        assertRootsEquals ("Invalid classpath roots for SOURCE", cp, new FileObject[] {badRoot});      //ERROR
        FileObject classFile = getClassFile();
        cp = cpp.findClassPath(classFile, ClassPath.EXECUTE);
        assertNotNull ("DefaultClassPathProvider returned null for EXECUTE",cp);
        assertEquals("Invalid length of classpath for EXECUTE",1,cp.getRoots().length);
        assertEquals("Illegal classpath for EXECUTE: ",cp.getRoots()[0],this.execTestDir);
    }
    
    
    private static void assertRootsEquals (String message, ClassPath cp, FileObject[] roots) {
        Set/*FileObject*/ cpRoots = new HashSet(Arrays.asList(cp.getRoots ()));
        assertEquals(message, cpRoots.size(), roots.length);
        for (int i=0; i< roots.length; i++) {
            if (!cpRoots.contains(roots[i])) {
                assertTrue(message, false);
            }
        }
    }
    
    private synchronized FileObject getSourceFile (int type) throws IOException {
        if (this.srcFile[type]==null) {
            assertNotNull (this.srcRoot);
            switch (type) {
                case FILE_IN_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,"test","Test","package test;\npublic class Test {}");                    
                    break;
                case FILE_IN_DEFAULT_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,null,"DefaultTest","public class DefaultTest {}");                    
                    break;
                case FILE_IN_BAD_PACKAGE:
                    this.srcFile[type] = createFile (this.srcRoot,"test","BadTest","package bad;\npublic class BadTest {}");                    
                    break;
                default:
                    throw new IllegalArgumentException ();
            }
        }
        return this.srcFile[type];
    }

    private synchronized FileObject getClassFile () throws IOException {
        FileObject fo = this.execTestDir.getFileObject("test/Test.class");
        if (fo == null) {
            fo = execTestDir.createFolder("test");
            fo = fo.createData("Test","class");
            FileLock lock = fo.lock();
            try {
                DataOutputStream out = new DataOutputStream (fo.getOutputStream(lock));
                try {
                    out.write(CLASS_FILE_DATA);
                    out.flush();
                } finally {
                    out.close();
                }
            } finally {
                lock.releaseLock();
            }
        }
        return fo;
    }

    private static FileObject createFile (FileObject root, String folderName, String name, String body) throws IOException {
        if (folderName != null) {
            FileObject tmp = root.getFileObject(folderName,null);
            if (tmp == null) {
                tmp = root.createFolder (folderName);
            }
            root = tmp;
        }
        FileObject file = root.createData (name,"java");
        FileLock lock = file.lock();
        try {
            PrintWriter out = new PrintWriter ( new OutputStreamWriter (file.getOutputStream(lock)));
            try {
                out.println (body);
            } finally {
                out.close ();
            }
        } finally {
            lock.releaseLock();
        }
        return file;
    }
    
    
    
    private class SFBQI implements SourceForBinaryQueryImplementation {
        
        
        public SFBQI () {
        }
        
        public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
            for (int i = 0; i < execRoots.length; i++) {
                try {
                    URL url = execRoots[i].getURL ();
                    if (url.equals (binaryRoot)) {
                        return new SourceForBinaryQuery.Result () {
                    
                            public FileObject[] getRoots () {                        
                                return libSourceRoots;
                            }
                    
                            public void addChangeListener (ChangeListener l) {
                            }
                    
                            public void removeChangeListener (ChangeListener l) {
                            }
                        };
                    }
                } catch (Exception e) {}                
            }
            return null;
        }
    }
        
   
            
    
    public synchronized Lookup getLookup() {
        if (this.lookup == null) {
            this.lookup = Lookups.fixed (
                new Object[] {
                    new ArchiveURLMapper (),
                    new MasterURLMapper(),
                    new JavaPlatformProviderImpl(),
                    new SFBQI (),
                });
        }
        return this.lookup;
    }    
    
    
}
