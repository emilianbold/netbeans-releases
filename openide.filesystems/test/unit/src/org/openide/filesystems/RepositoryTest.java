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
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
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
        assertEquals(2, r.getChildren().length);
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
