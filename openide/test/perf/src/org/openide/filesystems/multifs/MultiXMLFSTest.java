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

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.localfs.LocalFSTest;
import org.openide.filesystems.xmlfs.XMLFSTest;

/**
 * Base class for simulation of module layers.
 */
public class MultiXMLFSTest extends FSTest {
    
    private FSWrapper[] filesystems;
    private static final int MAGIC = 50;
    private static final String RES_EXT = ".instance";
    
    /** Creates new XMLFSGenerator */
    public MultiXMLFSTest(String name) {
        super(name);
    }

    /** Set up given number of FileObjects */
    protected FileObject[] setUpFileObjects(int foCount) throws Exception {
        int foChunk = foCount / MAGIC;
        int delta = foCount - (foCount / MAGIC) * MAGIC;
        filesystems = new FSWrapper[MAGIC];
        int last = filesystems.length;
        for (int i = 1; i < last; i++) {
            filesystems[i] = createXMLFSinJar(foChunk, i * foChunk, RES_EXT);
        }
        filesystems[0] = createLocalFS(foChunk + delta, 0);
        FileSystem[] fss = new FileSystem[last];
        for (int i = 1; i < last; i++) {
            fss[i] = filesystems[i].getFS();
        }
        
        MultiFileSystem mfs = new MultiFileSystem(fss);
        FileObject res = mfs.findResource(LocalFSTest.PACKAGE);
        return res.getChildren();
        //return null;
    }
    
    protected void tearDownFileObjects(FileObject[] fos) throws Exception {
        for (int i = 0; i < filesystems.length; i++) {
            delete(filesystems[i].getFile());
        }
    }
    
    private static FSWrapper createLocalFS(int foCount, int foBase) throws Exception {
        File mnt = createTempFolder();
        LocalFSTest.createFiles(foCount, 0, mnt);
        
        LocalFileSystem localFS = new LocalFileSystem();
        localFS.setRootDirectory(mnt);
        
        return new FSWrapper(localFS, mnt);
    }
    
    private static FSWrapper createXMLFSinJar(int foCount, int foBase, String resExt) throws Exception {
        File tmp = createTempFolder();
        File destFolder = LocalFSTest.createFiles(foCount, foBase, tmp);
        File xmlbase = XMLFSTest.generateXMLFile(destFolder, foCount, 0, resExt);
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        URLClassLoader cloader = new URLClassLoader(new URL[] { jar.toURL() });
        URL res = cloader.findResource(LocalFSTest.PACKAGE + xmlbase.getName());
        XMLFileSystem xmlfs = new XMLFileSystem();
        xmlfs.setXmlUrl(res, false);
        
        return new FSWrapper(xmlfs, tmp);
    }
    
    /** Wrapper for FS and its disk location */
    private static final class FSWrapper {
        private FileSystem fs;
        private File tmp;
        
        /** new FSWrapper */
        public FSWrapper(FileSystem fs, File tmp) {
            this.fs = fs;
            this.tmp = tmp;
        }
        
        public FileSystem getFS() {
            return fs;
        }
        
        public File getFile() {
            return tmp;
        }
    }
    
    public static void main(String[] args) throws Exception {
        MultiXMLFSTest mtest = new MultiXMLFSTest("first test");
        mtest.setUpFileObjects(500);
        System.out.println("done");
    }
}
