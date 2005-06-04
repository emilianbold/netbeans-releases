/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import org.netbeans.core.startup.NbURLStreamHandlerFactory;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

// XXX testNbfsURLStreamHandler

/**
 * Test functionality of internal URLs.
 * @author Jesse Glick
 */
public class NbURLStreamHandlerFactoryTest extends NbTestCase {
    
    private static final String[] RESOURCES = {
        "/test/something",
        "/test/something_ja",
        "/test/something_foo",
        "/test/something_foo_ja",
        "/test/something.html",
        "/test/something_ja.html",
        "/test/something_foo.html",
        "/test/something_foo_ja.html",
        "/test/something.html.template",
        "/test/something.html_ja.template",
        "/test/something.html_foo.template",
        "/test/something.html_foo_ja.template",
        "/test.dir/something.html",
        "/test.dir/something_ja.html",
        "/test.dir/something_foo.html",
        "/test.dir/something_foo_ja.html",
        "/test.dir/something",
        "/test.dir/something_ja",
        "/test.dir/something_foo",
        "/test.dir/something_foo_ja",
    };
    
    static {
        System.setProperty("org.openide.util.Lookup", L.class.getName());
        assertTrue(Lookup.getDefault() instanceof L);
        URL.setURLStreamHandlerFactory(new org.netbeans.core.startup.NbURLStreamHandlerFactory());
    }
    
    public NbURLStreamHandlerFactoryTest(String name) {
        super(name);
    }
    
    public void testNbResourceStreamHandlerAndURLStreamHandlerFactoryMerging() throws Exception {
        // Basic usage of nbres.
        checkNbres("/test/something.html");
        // Test usage with no extension.
        checkNbres("/test/something");
        // Check usage with multiple extensions.
        checkNbres("/test/something.html.template");
        // Check bad URL.
        URL u = new URL("nbres:/bogus");
        try {
            u.openConnection().connect();
            fail("Should not be able to connect to a bogus nbres URL!");
        } catch (IOException e) {
            // OK.
        }
        // Now test nbresloc. First, basic usage.
        checkNbresLoc("/test/something", ".html");
        // Check bare resources.
        checkNbresLoc("/test/something", "");
        // And, double-extension resources (cf. #53130).
        checkNbresLoc("/test/something.html", ".template");
        // Check also dots in the path prefix.
        checkNbresLoc("/test.dir/something", ".html");
        checkNbresLoc("/test.dir/something", "");
        // Check bad URL.
        u = new URL("nbresloc:/bogus");
        try {
            u.openConnection().connect();
            fail("Should not be able to connect to a bogus nbresloc URL!");
        } catch (IOException e) {
            // OK.
        }
    }
    
    private static void checkNbres(String path) throws Exception {
        URL u = new URL("nbres:" + path);
        assertEquals(path, suck(u));
        assertEquals(path.endsWith(".html") ? "text/html" : null, contentType(u));
        assertEquals(path.length(), contentLength(u));
    }
    
    private static void checkNbresLoc(String base, String ext) throws Exception {
        String path = base + ext;
        String type = ext.equals(".html") ? "text/html" : null;
        URL u = new URL("nbresloc:" + path);
        // Should initially be getting an unlocalized version:
        NbBundle.setBranding(null);
        Locale.setDefault(Locale.US);
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
        // Make sure branding works.
        NbBundle.setBranding("foo");
        path = base + "_foo" + ext;
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
        // Check unbranded but localized resources.
        NbBundle.setBranding(null);
        Locale.setDefault(Locale.JAPAN);
        path = base + "_ja" + ext;
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
        // Check both together.
        NbBundle.setBranding("foo");
        path = base + "_foo_ja" + ext;
        assertEquals(path, suck(u));
        assertEquals(type, contentType(u));
        assertEquals(path.length(), contentLength(u));
    }
    
    private static String suck(URL u) throws Exception {
        URLConnection conn = u.openConnection();
        InputStream is = conn.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(is, baos);
        is.close();
        return baos.toString("UTF-8");
    }
    
    private static String contentType(URL u) throws Exception {
        URLConnection conn = u.openConnection();
        return conn.getContentType();
    }
    
    private static int contentLength(URL u) throws Exception {
        URLConnection conn = u.openConnection();
        return conn.getContentLength();
    }
    
    public static final class L extends ProxyLookup {
        public L() {
            super(new Lookup[] {
                Lookups.fixed(new Object[] {
                    new TestClassLoader(),
                    new NbURLStreamHandlerFactory.Standard(),
                    new TestURLStreamHandlerFactory(),
                }),
            });
        }
    }

    /**
     * Only serves requests for resources in {@link #RESOURCES}.
     * "/foo" is serviced with the URL "testurl:/foo".
     */
    private static final class TestClassLoader extends ClassLoader {
        
        protected URL findResource(String name) {
            if (!name.startsWith("/")) {
                name = "/" + name;
            }
            if (Arrays.asList(RESOURCES).contains(name)) {
                try {
                    return new URL("testurl:" + name);
                } catch (MalformedURLException e) {
                    assert false : e;
                    return null;
                }
            } else {
                return null;
            }
        }

        protected Enumeration findResources(String name) throws IOException {
            URL u = findResource(name);
            if (u != null) {
                return Enumerations.singleton(u);
            } else {
                return Enumerations.empty();
            }
        }
        
    }
    
    private static final class TestURLStreamHandlerFactory implements URLStreamHandlerFactory {
        
        public URLStreamHandler createURLStreamHandler(String protocol) {
            if (protocol.equals("testurl")) {
                return new TestURLStreamHandler();
            } else {
                return null;
            }
        }
        
    }
    
    private static final class TestURLStreamHandler extends URLStreamHandler {
        
        protected URLConnection openConnection(URL u) throws IOException {
            return new TestURLConnection(u);
        }
        
    }
    
    /**
     * Serves up a URL by just returning the path as its text.
     * Content length should match contents.
     * Content type is text/html for *.html, null for all else.
     */
    private static final class TestURLConnection extends URLConnection {
        
        private final String path;
        
        public TestURLConnection(URL u) {
            super(u);
            assertEquals("testurl", u.getProtocol());
            this.path = u.getPath();
        }

        public void connect() throws IOException {}
        
        public int getContentLength() {
            return path.length();
        }

        public String getContentType() {
            if (path.endsWith(".html")) {
                return "text/html";
            } else {
                return null;
            }
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(path.getBytes("UTF-8"));
        }

    }
    
}
