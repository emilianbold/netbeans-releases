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
    private MultiFileSystem mfs;
    
    private static final String getResource(int base) {
        return LocalFSTest.getPackage(base).replace('/', '-').concat(LocalFSTest.RES_NAME);
    }
    
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
        int[] bases = new int[last];
        for (int i = 1; i < last; i++) {
            int ibase = i * foChunk;
            wrappers[i] = createXMLFSinJar(foChunk, ibase);
            bases[i] = ibase;
        }
        
        wrappers[0] = createLocalFS(foChunk + delta, 0);
        FileSystem[] fss = new FileSystem[last];
        for (int i = 0; i < last; i++) {
            fss[i] = wrappers[i].getFS();
        }
        
        FileObject[] ret = new FileObject[foCount];
        mfs = new MultiFileSystem(fss);
        for (int i = 0; i < last; i++) {
            FileObject res = mfs.findResource(LocalFSTest.getPackage(bases[i]));
            FileObject[] tmp = res.getChildren();
            int pos = i * foChunk + Math.min(i, 1) * delta;
            System.arraycopy(tmp, 0, ret, pos, tmp.length);
        }        
        
        return ret;
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
        File xmlbase = XMLFSTest.generateXMLFile(destFolder, new ResourceComposer(getResource(foBase), RES_EXT, foCount, foBase));
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        URLClassLoader cloader = new URLClassLoader(new URL[] { jar.toURL() });
        String xres = LocalFSTest.getPackage(foBase) + xmlbase.getName();
        URL res = cloader.findResource(xres);
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
    /*
    public static void main(String[] args) throws Exception {
        MultiXMLFSTest mtest = new MultiXMLFSTest("first test");
        mtest.setUpFileObjects(500);
        System.out.println("done");
        
        System.out.println(mtest.wrappers[1].getClassLoader().loadClass("org.openide.filesystems.data50.JavaSrc55"));
    }
     */
}
