/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.AssertionFailedError;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;

/** Tests that cover some basic aspects of a Proxy/JarClassLoader.
 *
 * @author Petr Nejedly
 */
public class JarClassLoaderTest extends NbTestCase {

    private static Logger LOGGER = Logger.getLogger(ProxyClassLoader.class.getName());


    public JarClassLoaderTest(String name) {
        super(name);
    }

    /** directory full of JAR files to test */
    protected File jars;
    /** directory full of testing roots */
    protected File dirs;

    @Override
    protected void setUp() throws Exception {
        LOGGER.setUseParentHandlers(false);
        LOGGER.setLevel(Level.OFF);
        jars = new File(JarClassLoaderTest.class.getResource("jars").getFile());
        dirs = new File(JarClassLoaderTest.class.getResource("dirs").getFile());
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

    public void testCanLoadFromDefaultPackageDirs() throws Exception {
        File dir = new File(dirs, "default-package-resource");
        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(dir), new ProxyClassLoader[0]);
        
        assertStreamContent(jcl.getResourceAsStream("package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/package/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("META-INF/services/resource.txt"), "content");
        assertStreamContent(jcl.getResourceAsStream("/META-INF/services/resource.txt"), "content");

        assertURLsContent(jcl.getResources("package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("/package/resource.txt"), "content");
        assertURLsContent(jcl.getResources("resource.txt"), "content");
        assertURLsContent(jcl.getResources("/resource.txt"), "content");
    }

    public void testFromNonExistentJAR() throws Exception {
        File jar = new File(jars, "default-package-resource.jar");
        File snd = new File(jars, "copy.jar");
        FileInputStream is = new FileInputStream(jar);
        FileOutputStream os = new FileOutputStream(snd);
        FileUtil.copy(is, os);
        is.close();
        os.close();
        
        JarClassLoader jcl = new JarClassLoader(Collections.singletonList(jar), new ProxyClassLoader[0]);
        JarClassLoader.initializeCache();

        URL u = jcl.getResource("package/resource.txt");
        //assertURLsContent(u, "content");
        
        URL n = new URL(u.toExternalForm().replaceAll("default-package-resource.jar", "copy.jar"));
        
        assertStreamContent(u.openStream(), "content");
        
        CharSequence log = Log.enable("org.netbeans.JarClassLoader", Level.WARNING);
        assertStreamContent(n.openStream(), "content");
        if (log.toString().indexOf("Cannot find") == -1) {
            fail("There should be a warning:\n" + log);
        }

        CharSequence log2 = Log.enable("org.netbeans.JarClassLoader", Level.WARNING);
        assertStreamContent(n.openStream(), "content");
        assertEquals("No second log:\n" + log2, -1, log2.toString().indexOf("Cannot find"));
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
