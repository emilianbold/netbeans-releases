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
package org.openide.filesystems.xmlfs;

import java.io.*;
import java.net.URLClassLoader;
import java.net.URL;

import org.openide.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.filesystems.localfs.LocalFSTest;
import org.openide.filesystems.data.SerialData;

/**
 * Tests an XMLFileSystem zipped in a jar.
 */
public class XMLinJarFSTest extends XMLFSTest {
    
    /** Creates new XMLFSGenerator */
    public XMLinJarFSTest(String name) {
        super(name);
    }

    /** Set up given number of FileObjects */
    protected FileObject[] setUpFileObjects(int foCount) throws Exception {
        tmp = createTempFolder();
        destFolder = LocalFSTest.createFiles(foCount, 0, tmp);
        File xmlbase = generateXMLFile(destFolder, foCount, 0, LocalFSTest.RES_EXT);
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        URLClassLoader cloader = new URLClassLoader(new URL[] { jar.toURL() });
        URL res = cloader.findResource(PACKAGE + xmlbase.getName());
        xmlfs = new XMLFileSystem();
        xmlfs.setXmlUrl(res, false);

        FileObject pkg = xmlfs.findResource(PACKAGE);
        return pkg.getChildren();
    }
    
    /*
    public static void main(String[] args) throws Exception {
        XMLinJarFSTest a = new XMLinJarFSTest("test");
        FileObject[] fos = a.setUpFileObjects(78);
        for (int i = 0; i < fos.length; i++) {
            System.out.println(fos[i]);
        }
    }
     */
}
