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
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.Hashtable;

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
public class MultiXMLFSTest extends FSTest implements SoftResetter {
    
    public static final String XMLFS_NO_KEY = "XMLFS_NO";
    private FSWrapper[] wrappers;
    private static final String RES_EXT = ".instance";
    private MultiFileSystem mfs;
    
    private FileObject[] ret;
    private int fsCount;
    private int foChunk;
    private int delta;
    private int[] bases;
    private int foCount2;
    
    private static final String getResource(int base) {
        return LocalFSTest.getPackage(base).replace('/', '-').concat(LocalFSTest.RES_NAME);
    }
    
    /** Creates new XMLFSGenerator */
    public MultiXMLFSTest(String name) {
        super(name);
    }

    /** Creates new XMLFSGenerator */
    public MultiXMLFSTest(String name, Object[] args) {
        super(name, args);
    }
    
    /** Set up given number of FileObjects */
    public FileObject[] setUpFileObjects(int foCount) throws Exception {
        Map param = (Map) getArgument();
        fsCount = ((Integer) param.get(XMLFS_NO_KEY)).intValue();
        foChunk = foCount / fsCount;
        delta = foCount - (foCount / fsCount) * fsCount;
        foCount2 = foCount;
        wrappers = new FSWrapper[fsCount];
        int last = wrappers.length;
        bases = new int[last];
        for (int i = 1; i < last; i++) {
            int ibase = i * foChunk;
            wrappers[i] = createXMLFSinJar(foChunk, ibase);
            bases[i] = ibase;
        }
        
        wrappers[0] = createLocalFS(foChunk + delta, 0);
        softSetUp();
        return ret;
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
    
    /** Creates args for this instance of Benchmark */
    protected Map[] createArguments() {
        Map[] map = super.createArguments();
        Map[] newMap = new Map[map.length * 2];
        
        System.arraycopy(map, 0, newMap, 0, map.length);
        
        for (int i = map.length; i < newMap.length; i++) {
            newMap[i] = cloneMap(map[i - map.length]);
            newMap[i].put(XMLFS_NO_KEY, new Integer(50));
        }
        
        return newMap;
    }
    
    /** Creates a Map with default arguments values */
    protected Map createDefaultMap() {
        Map map = super.createDefaultMap();
        map.put(XMLFS_NO_KEY, new Integer(10));
        return map;
    }    
    
    /** Soft setUp for this test */
    public void softSetUp() throws Exception {
        int last = wrappers.length;
        FileSystem[] fss = new FileSystem[last];
        for (int i = 0; i < last; i++) {
            wrappers[i].reset();
            fss[i] = wrappers[i].getFS();
        }
        
        mfs = new MultiFileSystem(fss);
        
        ret = new FileObject[foCount2];
        for (int i = 0; i < last; i++) {
            FileObject res = mfs.findResource(LocalFSTest.getPackage(bases[i]));
            FileObject[] tmp = res.getChildren();
            int pos = i * foChunk + Math.min(i, 1) * delta;
            System.arraycopy(tmp, 0, ret, pos, tmp.length);
        }        
        
    }
    
    public FileObject[] getFileObjects() {
        return ret;
    }
    
    /** Soft tearDown for this test */
    public void softTearDown() throws Exception {
    }
    
    /** Clones given Map by casting to a cloneable class - HashMap, Hashtable, or TreeMap */
    private static final Map cloneMap(Map toClone) {
        if (toClone instanceof HashMap) {
            return (Map) ((HashMap) toClone).clone();
        } else if (toClone instanceof Hashtable) {
            return (Map) ((Hashtable) toClone).clone();
        } else if (toClone instanceof TreeMap) {
            return (Map) ((TreeMap) toClone).clone();
        }
        
        return null;
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
        
        return new LocalFSWrapper(mnt);
    }
    
    private static FSWrapper createXMLFSinJar(int foCount, int foBase) throws Exception {
        File tmp = createTempFolder();
        File destFolder = LocalFSTest.createFiles(foCount, foBase, tmp);
        compileFolder(tmp, destFolder);
        File xmlbase = XMLFSTest.generateXMLFile(destFolder, new ResourceComposer(getResource(foBase), RES_EXT, foCount, foBase));
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        String xres = LocalFSTest.getPackage(foBase) + xmlbase.getName();
        return new XMLFSWrapper(tmp, jar, xres);
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
    public abstract static class FSWrapper {
        FileSystem fs;
        private File tmp;
        URLClassLoader cloader;
        
        /** new FSWrapper */
        FSWrapper(File tmp) {
            this.tmp = tmp;
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
        
        abstract void reset() throws Exception;
    }
    
    /** Wrapper for an XMLFS */
    static final class XMLFSWrapper extends FSWrapper {
        private String xres;
        private File jar;
        
        /** new FSWrapper */
        XMLFSWrapper(File tmp, File jar, String res) {
            super(tmp);
            this.xres = res;
            this.jar = jar;
        }
        
        void reset() throws Exception {
            cloader = new URLClassLoader(new URL[] { jar.toURL() });
            URL res = cloader.findResource(xres);
            XMLFileSystem xmlfs = new XMLFileSystem();
            xmlfs.setXmlUrl(res, false);
            fs = xmlfs;
        }
    }
    
    /** Wrapper for a LocalFS */
    static final class LocalFSWrapper extends FSWrapper {
        
        LocalFSWrapper(File tmp) {
            super(tmp);
        }
        
        void reset() throws Exception {
            LocalFileSystem localFS = new LocalFileSystem();
            localFS.setRootDirectory(getFile());
            cloader = new URLClassLoader(new URL[] { getFile().toURL() });
            fs = localFS;
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
