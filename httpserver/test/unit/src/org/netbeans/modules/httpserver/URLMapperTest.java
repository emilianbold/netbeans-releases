/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.*;
import java.util.*;

import junit.framework.*;
import org.netbeans.junit.*;
import org.openide.filesystems.*;

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
    
    
    /** method called before each testcase
     */
    protected void setUp() throws IOException {
        extProtocols = new HashSet ();
        extProtocols.add ("http");
        extProtocols.add ("file");
        extProtocols.add ("ftp");
    }
    
    /** method called after each testcase<br>
     * resets Jemmy WaitComponentTimeout
     */
    protected void tearDown() {
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
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
