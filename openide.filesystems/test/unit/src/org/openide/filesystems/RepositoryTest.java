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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.filesystems;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import junit.framework.TestCase;
import org.openide.util.Enumerations;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jaroslav Tulach
 */
public class RepositoryTest extends TestCase {
    
    public RepositoryTest(String testName) {
        super(testName);
    }

    protected @Override void setUp() throws Exception {
        super.setUp();
        ExternalUtil.repository = null;
        MockLookup.setInstances();
    }

    public void testContentOfFileSystemIsInfluencedByLookup () throws Exception {
        FileSystem mem = FileUtil.createMemoryFileSystem();
        String dir = "/yarda/own/file";
        org.openide.filesystems.FileUtil.createFolder (mem.getRoot (), dir);
        
        // XXX fails to test that Repo contents are right from *initial* lookup
        // (try commenting out 'resultChanged(null);' in ExternalUtil.MainFS - still passes)
        assertNull ("File is not there yet", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
        MockLookup.setInstances(mem);
        try {
            assertNotNull ("The file is there now", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
        } finally {
            MockLookup.setInstances();
        }
        assertNull ("File is no longer there", Repository.getDefault ().getDefaultFileSystem ().findResource (dir));
    }

    public void testRepositoryIncludesAllLayers() throws Exception {
        Thread.currentThread().setContextClassLoader(new ClassLoader() {
            protected @Override Enumeration<URL> findResources(String name) throws IOException {
                if (name.equals("META-INF/MANIFEST.MF")) {
                    return Enumerations.array(literalURL("OpenIDE-Module-Layer: foo/layer.xml\n"), literalURL("OpenIDE-Module-Layer: bar/layer.xml\n"));
                } else {
                    return super.findResources(name);
                }
            }
            protected @Override URL findResource(String name) {
                if (name.equals("foo/layer.xml")) {
                    return RepositoryTest.class.getResource("test-layer-1.xml");
                } else if (name.equals("bar/layer.xml")) {
                    return RepositoryTest.class.getResource("test-layer-2.xml");
                } else {
                    return super.findResource(name);
                }
            }
        });
        FileObject r = Repository.getDefault().getDefaultFileSystem().getRoot();
        assertEquals(3, r.getChildren().length);  // org.openide.filesystems.resources.layer.xml, test-layer-1.xml, test-layer-2.xml
        assertNotNull(r.getFileObject("foo"));
        assertNotNull(r.getFileObject("bar"));
    }
    private static URL literalURL(final String content) {
        try {
            return new URL("literal", null, 0, content,new URLStreamHandler() {
                protected URLConnection openConnection(URL u) throws IOException {
                    return new URLConnection(u) {
                        public @Override InputStream getInputStream() throws IOException {
                            return new ByteArrayInputStream(content.getBytes());
                        }
                        public @Override void connect() throws IOException {}
                    };
                }
            });
        } catch (MalformedURLException x) {
            throw new AssertionError(x);
        }
    }

}
