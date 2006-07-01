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
package org.openide.filesystems.xmlfs;

import java.io.*;
import java.net.URLClassLoader;
import java.net.URL;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.filesystems.localfs.LocalFSTest;
import org.openide.filesystems.data.SerialData;

/**
 * Tests an XMLFileSystem zipped in a jar.
 */
public class XMLinJarFSTest extends XMLFSTest {

    URLClassLoader cloader;

    /** Creates new XMLFSGenerator */
    public XMLinJarFSTest(String name) {
        super(name);
    }

    /** Set up given number of FileObjects */
    protected FileObject[] setUpFileObjects(int foCount) throws Exception {
        tmp = createTempFolder();
        destFolder = LocalFSTest.createFiles(foCount, 0, tmp);
        File xmlbase = generateXMLFile(destFolder, new ResourceComposer(LocalFSTest.RES_NAME, LocalFSTest.RES_EXT, foCount, 0));
        File jar = Utilities.createJar(tmp, "jarxmlfs.jar");
        cloader = new URLClassLoader(new URL[] { jar.toURL() });
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
