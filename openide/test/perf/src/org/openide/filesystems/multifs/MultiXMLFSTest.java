/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.filesystems.multifs;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.localfs.LocalFSTest;
import org.openide.filesystems.xmlfs.XMLFSTest;
import org.openide.filesystems.xmlfs.XMLFSTest.ResourceComposer;

/**
 * Base class for simulation of module layers. It creates several layers, each filled
 * with some number of .instance files. Each layer is zipped into one jar. 
 * The jars also contain class files.
 */
public class MultiXMLFSTest extends FSTest {
    
    private FSWrapper[] wrappers;
    private static final int MAGIC = 10;
    private static final String RES_EXT = ".instance";
    private static final String RES_NAME = LocalFSTest.PACKAGE.replace('/', '-').concat(LocalFSTest.RES_NAME);
    private MultiFileSystem mfs;
    
    /** Creates new XMLFSGenerator */
    public MultiXMLFSTest(String name) {
        super(name);
    }

    /** Set up given number of FileObjects */
    public FileObject[] setUpFileObjects(int foCount) throws Exception {
        int foChunk = foCount / MAGIC;
        int delta = foCount - (foCount / MAGIC) * MAGIC;
        wrappers = new FSWrapper[MAGIC];
        int last = wrappers.length;
        for (int i = 1; i < last; i++) {
            wrappers[i] = createXMLFSinJar(foChunk, i * foChunk);
        }
        wrappers[0] = createLocalFS(foChunk + delta, 0);
        FileSystem[] fss = new FileSystem[last];
        for (int i = 0; i < last; i++) {
            fss[i] = wrappers[i].getFS();
        }
        
        mfs = new MultiFileSystem(fss);
        FileObject res = mfs.findResource(LocalFSTest.PACKAGE);
        return res.getChildren();
        //return null;
    }
    
    /** Empty */
    protected void postSetUp() {
    }
    
    /** Free resources */
    protected void tearDownFileObjects(FileObject[] fos) throws Exception {
        for (int i = 0; i < wrappers.length; i++) {
            delete(wrappers[i].getFile());
        }
    }
    
    /** @return this mfs */
    public MultiFileSystem getMultiFileSystem() {
        return mfs;
    }
    
    /** @return wrappers array */
    public FSWrapper[] getFSWrappers() {
        return wrappers;
    }
    
    private static FSWrapper createLocalFS(int foCount, int foBase) throws Exception {
        File mnt = createTempFolder();
        LocalFSTest.createFiles(foCount, 0, mnt);
        
        LocalFileSystem localFS = new LocalFileSystem();
        localFS.setRootDirectory(mnt);
        URLClassLoader cloader = new URLClassLoader(new URL[] { mnt.toURL() });
        
        return new FSWrapper(cloader, localFS, mnt);
    }
    
    private static FSWrapper createXMLFSinJar(int foCount, int foBase) throws Exception {
        File tmp = createTempFolder();
        File destFolder = LocalFSTest.createFiles(foCount, foBase, tmp);
        compileFolder(tmp, destFolder);
        File xmlbase = XMLFSTest.generateXMLFile(destFolder, new ResourceComposer(RES_NAME, RES_EXT, foCount, foBase));
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        URLClassLoader cloader = new URLClassLoader(new URL[] { jar.toURL() });
        URL res = cloader.findResource(LocalFSTest.PACKAGE + xmlbase.getName());
        XMLFileSystem xmlfs = new XMLFileSystem();
        xmlfs.setXmlUrl(res, false);
        
        return new FSWrapper(cloader, xmlfs, tmp);
    }
    
    private static void compileFolder(File root, File destFolder) throws Exception {
        File[] files = destFolder.listFiles();
        //StringBuffer sb = new StringBuffer(3000);
        String[] args = new String[files.length + 3];
        args[0] = "javac";
        args[1] = "-classpath";
        args[2] = System.getProperty("java.class.path");
        
        for (int i = 3; i < args.length; i++) {
            args[i] = files[i - 3].getCanonicalPath();
        }
        
        File stdlog = new File(root, "stdcompilerlog.txt");
        File errlog = new File(root, "errcompilerlog.txt");
        
        PrintStream stdps = new PrintStream(new FileOutputStream(stdlog));
        PrintStream errps = new PrintStream(new FileOutputStream(errlog));
        
        Process p = Runtime.getRuntime().exec(args);
        CopyMaker cma, cmb;
        Thread tha = new Thread(cma = new CopyMaker(p.getInputStream(), stdps));
        tha.start();
        Thread thb = new Thread(cmb = new CopyMaker(p.getErrorStream(), errps));
        thb.start();
        
        p.waitFor();
        tha.join();
        thb.join();
        
        stdps.close();
        errps.close();
        
        if (cma.e != null) {
            throw cma.e;
        }
        if (cmb.e != null) {
            throw cmb.e;
        }
    }
    
    static final class CopyMaker implements Runnable {
        InputStream is;
        PrintStream os;
        Exception e;
        
        CopyMaker(InputStream is, PrintStream os) {
            this.is = is;
            this.os = os;
        }
        
        public void run() {
            try {
                Utilities.copyIS(is, os);
            } catch (Exception ee) {
                e = ee;
            }
        }
    }
    
    /** Wrapper for FS and its disk location */
    public static final class FSWrapper {
        private FileSystem fs;
        private File tmp;
        private URLClassLoader cloader;
        
        /** new FSWrapper */
        public FSWrapper(URLClassLoader cl, FileSystem fs, File tmp) {
            this.fs = fs;
            this.tmp = tmp;
            this.cloader = cl;
        }
        
        public FileSystem getFS() {
            return fs;
        }
        
        public File getFile() {
            return tmp;
        }
        
        public URLClassLoader getClassLoader() {
            return cloader;
        }
    }
    
    public static void main(String[] args) throws Exception {
        MultiXMLFSTest mtest = new MultiXMLFSTest("first test");
        mtest.setUpFileObjects(500);
        System.out.println("done");
    }
}
