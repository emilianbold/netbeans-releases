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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import junit.framework.AssertionFailedError;
import org.fakepkg.FakeHandler;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/** Tests that cover some basic aspects of a Proxy/JarClassLoader.
 *
 * @author Petr Nejedly
 */
public class JarClassLoaderTest extends NbTestCase {

    public JarClassLoaderTest(String name) {
        super(name);
    }

    /** directory full of JAR files to test */
    protected File jars;

    protected void setUp() throws Exception {
        jars = new File(JarClassLoaderTest.class.getResource("jars").getFile());
    }


    public void testCanLoadFromDefaultPackage() throws Exception {
        File jar = new File(jars, "default-package-resource.jar");
        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0]);
        
        assertStreamContent(jcl.getResourceAsStream("package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/resource.txt"), "content");

        assertURLsContent(jcl.getResources("package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("/package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("resource.txt"), "content");
        assertURLsContent(jcl.getResources("/resource.txt"), "content");
    }

    public void testCanLoadFromDefaultPackageCached() throws Exception {
        final File jar = new File(jars, "default-package-resource-cached.jar");

        Module fake = new Module(null, null, null, null) {
	    public List<File> getAllJars() {throw new UnsupportedOperationException();}
            public void setReloadable(boolean r) { throw new UnsupportedOperationException();}
            public void reload() throws IOException { throw new UnsupportedOperationException();}
            protected void classLoaderUp(Set<Module> parents) throws IOException {throw new UnsupportedOperationException();}
            protected void classLoaderDown() { throw new UnsupportedOperationException();}
            protected void cleanup() { throw new UnsupportedOperationException();}
            protected void destroy() { throw new UnsupportedOperationException("Not supported yet.");}
            public boolean isFixed() { throw new UnsupportedOperationException("Not supported yet.");}
            public Object getLocalizedAttribute(String attr) { throw new UnsupportedOperationException("Not supported yet.");}

            public Manifest getManifest() {
                try {
                    return new JarFile(jar, false).getManifest();
                } catch (IOException ex) {
                        throw new AssertionFailedError(ex.getMessage());
                }
            }

        };

        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0], false, fake);
        
        assertStreamContent(jcl.getResourceAsStream("package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/resource.txt"), "content");

        assertURLsContent(jcl.getResources("package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("/package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("resource.txt"), "content");
        assertURLsContent(jcl.getResources("/resource.txt"), "content");
    }

    private void assertURLsContent(Enumeration<URL> urls, String ... contents) throws IOException {
        for (String content : contents) {
            assertTrue("Enough entries", urls.hasMoreElements());
            assertStreamContent(urls.nextElement().openStream(), content);
        }
        assertFalse("Too many entries", urls.hasMoreElements());
    }
    
    private void assertStreamContent(InputStream str, String content) throws IOException {
        assertNotNull("Resource found", str);
        byte[] data = new byte[content.length()];
        DataInputStream dis = new DataInputStream(str);
        try {
            dis.readFully(data);
        } finally {
            dis.close();
        }
        assertEquals(new String(data), content);
    }
}
