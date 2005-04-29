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

package org.openide.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import org.netbeans.junit.NbTestCase;

/**
 * Test functionality of URLMapper.
 * @author Jesse Glick
 */
public class URLMapperTest extends NbTestCase {
    
    public URLMapperTest(String name) {
        super(name);
    }
    
    /**
     * Check that jar: URLs are correctly mapped back into JarFileSystem resources.
     * @see "#39190"
     */
    public void testJarMapping() throws Exception {
        clearWorkDir();
        File workdir = getWorkDir();
        File jar = new File(workdir, "test.jar");
        String textPath = "x.txt";
        OutputStream os = new FileOutputStream(jar);
        try {
            JarOutputStream jos = new JarOutputStream(os);
            jos.setMethod(ZipEntry.STORED);
            JarEntry entry = new JarEntry(textPath);
            entry.setSize(0L);
            entry.setTime(System.currentTimeMillis());
            entry.setCrc(new CRC32().getValue());
            jos.putNextEntry(entry);
            jos.flush();
            jos.close();
        } finally {
            os.close();
        }
        assertTrue("JAR was created", jar.isFile());
        assertTrue("JAR is not empty", jar.length() > 0L);
        JarFileSystem jfs = new JarFileSystem();
        jfs.setJarFile(jar);
        Repository.getDefault().addFileSystem(jfs);
        FileObject rootFO = jfs.getRoot();
        FileObject textFO = jfs.findResource(textPath);
        assertNotNull("JAR contains a/b.txt", textFO);
        String rootS = "jar:" + jar.toURI() + "!/";
        URL rootU = new URL(rootS);
        URL textU = new URL(rootS + textPath);
        assertEquals("correct FO -> URL for root", rootU, URLMapper.findURL(rootFO, URLMapper.EXTERNAL));
        assertEquals("correct FO -> URL for " + textPath, textU, URLMapper.findURL(textFO, URLMapper.EXTERNAL));
        assertTrue("correct URL -> FO for root", Arrays.asList(URLMapper.findFileObjects(rootU)).contains(rootFO));
        assertTrue("correct URL -> FO for " + textPath, Arrays.asList(URLMapper.findFileObjects(textU)).contains(textFO));
    }
    
}
