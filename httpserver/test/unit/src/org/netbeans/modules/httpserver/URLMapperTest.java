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

package org.netbeans.modules.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;

/** Test for HttpServerURLMapper.
 *
 * @author Radim Kubacki, Petr Jiricka
 */
public class URLMapperTest extends NbTestCase {

    private HashSet extProtocols;
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public URLMapperTest(String testName) {
        super (testName);
    }
    
    /** method used for explicit testsuite definition
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new NbTestSuite(URLMapperTest.class);
        return suite;
    }
    
    
    protected void setUp() throws IOException {
        extProtocols = new HashSet ();
        extProtocols.add ("http");
        extProtocols.add ("file");
        extProtocols.add ("ftp");
    }
    
    /** simple test case
     */
    public void testFileURLMapping() throws Exception {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/httpserver/testResource.txt");

        URLMapper mapper = getMapper();
        URL url = mapper.getURL(fo,  URLMapper.EXTERNAL);
        checkFileObjectURLMapping(fo, url, getMapper());
    }
    
    public void testEmptyFileURLMapping() throws Exception {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/httpserver/empty");
        
        URLMapper mapper = getMapper();
        URL url = mapper.getURL(fo,  URLMapper.EXTERNAL);
        checkFileObjectURLMapping(fo, url, getMapper());
    }
    
    public void testDirURLMapping() throws Exception {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/httpserver");
        
        URLMapper mapper = getMapper();
        URL url = mapper.getURL(fo,  URLMapper.EXTERNAL);
        checkFileObjectURLMapping(fo, url, getMapper());
    }
    
    public void testFileWithSpacesURLMapping() throws Exception {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/httpserver/dir with spaces/file with spaces.txt");
        
        URLMapper mapper = getMapper();
        URL url = mapper.getURL(fo,  URLMapper.EXTERNAL);
        if (url != null) {
            // the case that the URL is null will be caught anyhow later
            if (url.toExternalForm().indexOf(' ') != -1) {
                fail("External URL contains spaces: " + url);
            }
        }
        checkFileObjectURLMapping(fo, url, getMapper());
    }
    
    public void testFSRootURLMapping() throws Exception {
        FileObject fo = Repository.getDefault().
            findResource("org/netbeans/test/httpserver/testResource.txt").getFileSystem().getRoot();
        
        URLMapper mapper = getMapper();
        URL url = mapper.getURL(fo,  URLMapper.EXTERNAL);
        checkFileObjectURLMapping(fo, url, getMapper());
    }
    
    public void testPageWithAnchorMapping() throws Exception {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/httpserver/Page.html");
        
        URLMapper mapper = getMapper();
        URL url = mapper.getURL(fo,  URLMapper.EXTERNAL);
        if (url != null) {
            // the case that the URL is null will be caught anyhow later
            url = new URL (url, "#A(a, b, c)");
        }
        checkFileObjectURLMapping(fo, url, getMapper());
    }
    
    public void testInternalMapping() throws Exception {
        FileObject fo = Repository.getDefault().findResource("org/netbeans/test/httpserver/testResource.txt");
        assertNotNull("File tested is null " + fo, fo);

        URLMapper mapper = getMapper();
        URL url = mapper.getURL(fo,  URLMapper.INTERNAL);
        // our mapper does not provide mapping for these
        assertNull("Internal mapping for file " + fo + " should be null: " + url, url);
    }
    
    /** Some URLs shouldn't be wrapped.
     * @param url tested URL.
     */
/*    public void testDummyMapping () throws Exception {
        // URLs that should not be wrapped
        URL [] urls = {
            null,
            new URL ("http://www.netbeans.org/")
        };
        
        for (int i = 0; i<urls.length; i++) {
            URL url = urls[i];
            URL wrapped = WrapperServlet.createHttpURL(url);
            assertEquals("URLs "+url+" and "+wrapped+" ought to be the same.", url, wrapped);
        }
    }*/
    
    private URLMapper getMapper() {
        return new HttpServerURLMapper();
    }
    
    private void checkFileObjectURLMapping(FileObject fo, URL url, URLMapper mapper) throws Exception {
        log ("Testing " + fo);
        log ("     -> " + url);
        assertNotNull("The file tested is null.", fo);
        assertNotNull("Mapper does not produce a URL for file " + fo, url);
        FileObject newFo[] = mapper.getFileObjects(url);
        assertNotNull("Mapper does not produce file for URL " + url, newFo);
        if (newFo.length != 1) {
            fail("Mapper returned array of size " + newFo.length + " for URL " + url);
        }
        assertEquals("Mapping does not produce the original object: " + fo + " != " + newFo, fo, newFo[0]);
        // compare the streams
        URL u2 = fo.getURL();
        compareStream(url.openStream(), u2.openStream());
    }
    /**
     * @param u1 original URL
     * @param u2 wrapped URL
     * @param compare controls whether content of streams should be compared.
     * @throws Exception  
     */
/*    private void checkWrapping (URL u1, URL u2, boolean compare) throws Exception {
        log ("Testing "+u1+" and "+u2);
        assertNotNull(u1+" is not wrapped", u2);
        assertTrue("Invalid anchor.", (u1.getRef() == null && u2.getRef() == null) ||
                                      u1.getRef().equals(u2.getRef())
        );
        assertTrue("Doesn't wrap to external protocol", extProtocols.contains (u2.getProtocol()));
        if (compare) {
            compareStream(u1.openStream(), u2.openStream());
        }
        // make sure that the URL does not contain spaces (but only if the scheme is not file)
        if (!"file".equals(u2.getProtocol())) {
            String ext = u2.toExternalForm();
            int ref = ext.indexOf('#');
            if (ref != -1) {
                ext = ext.substring(0, ref);
            }
            int spaceIndex = ext.indexOf(' ');
            if (spaceIndex != -1) {
                fail("URL " + u2 + " contains spaces.");
            }
        }
    }*/
    
    /** Compares content of two streams. 
     */
    private static void compareStream (InputStream i1, InputStream i2) throws Exception {
        for (int i = 0; true; i++) {
            int c1 = i1.read ();
            int c2 = i2.read ();

            assertEquals (i + "th bytes are different", c1, c2);
            
            if (c1 == -1) return;
        }
    }
}
