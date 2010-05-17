/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.web.client.javascript.debugger.filesystem;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.NbBundle;

/**
 *
 * @author quynguyen
 */
public class URLFileObjectTest extends NbTestCase {
    private static final String TEST_URL = "http://localhost:8080/WebApplication01/test.js";
    private static final String TEST_URL2 = "http://localhost:8080/WebApplication01/test2.js";
    
    private URLFileObject instance;
    private URLContentProvider provider;
    
    public URLFileObjectTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        provider = new FileURLProvider();
        URL contentURL = new URL(TEST_URL);
        
        instance = (URLFileObject)URLFileObjectFactory.getFileObject(provider, contentURL);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        instance = null;
    }
    
    public void testCaching() throws InterruptedException, IOException {
        System.out.println("Cache persistence test");
        URL contentURL = new URL(TEST_URL);
        Object getter = URLFileObjectFactory.getFileObject(provider, contentURL);
        
        assertTrue("Factory returned different FileObject", getter == instance);
        getter = null;
        
        WeakReference<URLFileObject> foRef = new WeakReference<URLFileObject>(instance);
        instance = null;
        
        assertNotNull("URLFileObject not held by cache", foRef.get());
        
        WeakReference<URLContentProvider> providerRef = new WeakReference<URLContentProvider>(provider);
        provider = null;
        
        assertGC("ContentProvider incorrectly held by cache", providerRef);
    }

    public void testSerialization() {
        System.out.println("URLFileObject serialization test");
        
        try {
            // serialize
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(output);
            oos.writeObject(instance);
            oos.close();
            
            // deserialize
            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(input);
            Object o = ois.readObject();
            
            // compare result
            assertTrue("Deserialized type not correct", o instanceof URLFileObject);
            
            URLFileObject copy = (URLFileObject)o;
            assertEquals(copy.getPath(), instance.getPath());
            assertNull( ((URLFileSystem)copy.getFileSystem()).getContentProvider());
        }catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected Exception encountered while running test");
        }
    }
    
    /**
     * Test of getInputStream method, of class URLFileObject.
     */
    public void testGetInputStream() {
        System.out.println("getInputStream");
        
        try {
            InputStream stream = instance.getInputStream();
            assertTrue(stream.read() != -1);
            stream.close();
            
            ((URLFileSystem)instance.getFileSystem()).setContentProvider(null);
            stream = instance.getInputStream();
            String expectedResult = NbBundle.getMessage(URLFileObject.class, "NO_CONTENT_MSG");
            assertTrue(expectedResult != null && expectedResult.length() > 0);
            
            InputStream byteStream = instance.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(byteStream));
            String result = reader.readLine();
            reader.close();
            
            assertEquals(expectedResult, result);
        }catch (Exception ex) {
            ex.printStackTrace();
            fail("Exception encountered during test");
        }
    }
    
    /**
     * Test of getSourceURL method, of class URLFileObject.
     */
    public void testGetSourceURL() {
        System.out.println("getSourceURL");
        
        URL result = instance.getSourceURL();
        String resultSpec = result.toExternalForm();
        
        assertEquals(resultSpec, TEST_URL);
    }

    public void testGetURL() throws FileStateInvalidException {
        System.out.println("getURL");
        
        URL expResult = instance.getSourceURL();
        URL result = instance.getURL();
        
        assertEquals(expResult.toExternalForm(), result.toExternalForm());
        
        URL mapperResult = URLMapper.findURL(instance, URLMapper.NETWORK);
        assertEquals(mapperResult.toExternalForm(), result.toExternalForm());
    }
    
    /**
     * Test of getFileSystem method, of class URLFileObject.
     */
    public void testGetFileSystem() throws Exception {
        System.out.println("getFileSystem");

        FileSystem result = instance.getFileSystem();
        
        assertNotNull(result);
        assertTrue(result instanceof URLFileSystem);
    }

    /**
     * Test of getParent method, of class URLFileObject.
     */
    public void testGetParent() {
        System.out.println("getParent");
        
        FileObject parent = instance.getParent();
        
        assertNotNull(parent);
        assertTrue(parent instanceof URLRootFileObject);
    }

    /**
     * Test of isRoot method, of class URLFileObject.
     */
    public void testIsRoot() {
        System.out.println("isRoot");
        
        boolean isRoot = instance.isRoot();
        
        assertFalse(isRoot);
        assertTrue(instance.getParent().isRoot());
    }

    /**
     * Test of isData method, of class URLFileObject.
     */
    public void testIsData() {
        System.out.println("isData");

        boolean result = instance.isData();
        assertTrue(result);
        assertFalse(instance.getParent().isData());
    }

    /**
     * Test of isFolder method, of class URLFileObject.
     */
    public void testIsFolder() {
        System.out.println("isFolder");

        boolean result = instance.isFolder();
        assertFalse(result);
        assertTrue(instance.getParent().isFolder());
    }

    /**
     * Test of isValid method, of class URLFileObject.
     */
    public void testIsValid() {
        System.out.println("isValid");

        boolean result = instance.isValid();
        assertTrue(result);
    }

    /**
     * Test of getAttribute method, of class URLFileObject.
     */
    public void testGetAttribute() throws IOException {
        System.out.println("getAttribute");
        
        String attrName = "testAttribute";
        Object expResult = new Object();
        instance.setAttribute(attrName, expResult);
        Object result = instance.getAttribute(attrName);
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getAttributes method, of class URLFileObject.
     */
    public void testGetAttributes() throws IOException {
        System.out.println("getAttributes");
        
        String attrName = "testAttribute";
        Object expResult = new Object();
        instance.setAttribute(attrName, expResult);        
        
        Enumeration<String> result = instance.getAttributes();
        assertTrue(result.hasMoreElements());
    }

    /**
     * Test of lock method, of class URLFileObject.
     */
    public void testLock() throws Exception {
        System.out.println("lock");


        FileLock result = instance.lock();
        
        assertEquals(FileLock.NONE, result);
    }

    /**
     * Test of getChildren method, of class URLFileObject.
     */
    public void testGetChildren() throws Exception {
        System.out.println("getChildren");
        
        FileObject[] result = instance.getChildren();
        assertTrue(result == null || result.length == 0);
        
        URL url = new URL(TEST_URL2);
        URLFileObject urlfo = URLFileObjectFactory.getFileObject(provider, url);
        
        FileObject[] rootChildren = instance.getParent().getChildren();
        
        assertTrue(rootChildren != null && rootChildren.length == 2);
        assertTrue(rootChildren[0] == instance || rootChildren[1] == instance);
        assertTrue(rootChildren[0] == urlfo || rootChildren[1] == urlfo);
    }

    /**
     * Test of getFileObject method, of class URLFileObject.
     */
    public void testGetFileObject() {
        System.out.println("getFileObject");
        
        FileObject result = instance.getFileObject(TEST_URL);
        assertNull(result);
        
        FileObject thisInstance = instance.getParent().getFileObject(TEST_URL);
        assertTrue(instance == thisInstance);
    }

    /**
     * Test of canWrite method, of class URLFileObject.
     */
    public void testCanWrite() {
        System.out.println("canWrite");
        boolean expResult = false;
        boolean result = instance.canWrite();
        
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class URLFileObject.
     */
    public void testGetName() {
        System.out.println("getName");
        String expResult = "test";
        
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getExt method, of class URLFileObject.
     */
    public void testGetExt() {
        System.out.println("getExt");
        String expResult = "js";
        String result = instance.getExt();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPath method, of class URLFileObject.
     */
    public void testGetPath() {
        System.out.println("getPath");

        String result = instance.getPath();
        assertEquals(TEST_URL, result);
    }
}
