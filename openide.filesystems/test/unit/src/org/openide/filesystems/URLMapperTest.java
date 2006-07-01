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
