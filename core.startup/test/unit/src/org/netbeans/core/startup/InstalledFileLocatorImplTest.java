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

package org.netbeans.core.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
/**
 * Test functionality of InstalledFileLocatorImpl.
 * @author Jesse Glick
 */
public class InstalledFileLocatorImplTest extends NbTestCase {

    static {
        InstalledFileLocatorImplTest.class.getClassLoader().setDefaultAssertionStatus(true);
    }
    
    public InstalledFileLocatorImplTest(String name) {
        super(name);
    }
    
    private File scratch, nbhome, nbuser, nbdir1, nbdir2;
    private InstalledFileLocator ifl;
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        scratch = getWorkDir();
        nbhome = new File(scratch, "nbhome");
        touch(file(nbhome, "a/b"));
        touch(file(nbhome, "a/c"));
        touch(file(nbhome, "d"));
        touch(file(nbhome, "e/f/g"));
        touch(file(nbhome, "loc/x.html"));
        touch(file(nbhome, "loc/x_ja.html"));
        touch(file(nbhome, "loc/x_foo.html"));
        touch(file(nbhome, "loc/x_foo_ja.html"));
        touch(file(nbhome, "loc/y.html"));
        touch(file(nbhome, "h_ja"));
        nbuser = new File(scratch, "nbuser");
        touch(file(nbuser, "a/b"));
        nbdir1 = new File(scratch, "nbdir1");
        touch(file(nbdir1, "e/f/g"));
        nbdir2 = new File(scratch, "nbdir2");
        touch(file(nbdir2, "h"));
        touch(file(nbdir2, "loc/y_foo.html"));
        File nbdirx = new File(scratch, "nbdirx"); // nonexistent dir
        System.setProperty("netbeans.home", nbhome.getAbsolutePath());
        System.setProperty("netbeans.user", nbuser.getAbsolutePath());
        System.setProperty("netbeans.dirs",
            nbdir1.getAbsolutePath() + File.pathSeparatorChar +
            nbdir2.getAbsolutePath() + File.pathSeparatorChar +
            // Useless trailing separator intentional:
            nbdirx.getAbsolutePath() + File.pathSeparatorChar);
        NbBundle.setBranding("foo");
        Locale.setDefault(Locale.JAPAN);
        ifl = new InstalledFileLocatorImpl();
    }
    private static File file(File dir, String path) {
        return new File(dir, path.replace('/', File.separatorChar));
    }
    private static void touch(File f) throws IOException {
        File p = f.getParentFile();
        if (!p.exists()) {
            if (!p.mkdirs()) {
                throw new IOException(p.getAbsolutePath());
            }
        }
        OutputStream os = new FileOutputStream(f);
        os.close();
    }

    /**
     * Test file locating.
     * Note that there can be only one test method because the IFLI static
     * block initializes the dir list immediately with the given system
     * properties, so another test in the same VM would not be able to change them.
     */
    public void testLocate() throws Exception {
        InstalledFileLocatorImpl.prepareCache();
        assertEquals("[cache] found a simple file", file(nbhome, "d"), ifl.locate("d", null, false));
        assertEquals("[cache] did not find a nonexistent file", null, ifl.locate("d2", null, false));
        assertEquals("[cache] found an override in nbuser", file(nbuser, "a/b"), ifl.locate("a/b", null, false));
        assertEquals("[cache] found a non-override in nbhome", file(nbhome, "a/c"), ifl.locate("a/c", null, false));
        assertEquals("[cache] found an overridden dir in nbuser", file(nbuser, "a"), ifl.locate("a", null, false));
        assertEquals("[cache] did not find a nonexistent file in an existing dir", null, ifl.locate("a/x", null, false));
        assertEquals("[cache] found a multilevel override in an nbdirs component", file(nbdir1, "e/f/g"), ifl.locate("e/f/g", null, false));
        assertEquals("[cache] all nbdirs components scanned", file(nbdir2, "h"), ifl.locate("h", null, false));
        assertEquals("[cache] localized and branded resource can be found", file(nbhome, "loc/x_foo_ja.html"), ifl.locate("loc/x.html", null, true));
        assertEquals("[cache] nbdirs can override location of a branded resource", file(nbdir2, "loc/y_foo.html"), ifl.locate("loc/y.html", null, true));
        assertEquals("[cache] but look in all dirs for most specific resource first", file(nbhome, "h_ja"), ifl.locate("h", null, true));
        assertEquals("[cache] localized lookup a no-op for nonlocalized files", file(nbuser, "a/b"), ifl.locate("a/b", null, true));
        InstalledFileLocatorImpl.discardCache();
        assertEquals("[no cache] found a simple file", file(nbhome, "d"), ifl.locate("d", null, false));
        assertEquals("[no cache] did not find a nonexistent file", null, ifl.locate("d2", null, false));
        touch(file(nbhome, "d2"));
        assertEquals("[no cache] but did find a newly added file", file(nbhome, "d2"), ifl.locate("d2", null, false));
        assertEquals("[no cache] found an override in nbuser", file(nbuser, "a/b"), ifl.locate("a/b", null, false));
        assertEquals("[no cache] found a non-override in nbhome", file(nbhome, "a/c"), ifl.locate("a/c", null, false));
        assertEquals("[no cache] found an overridden dir in nbuser", file(nbuser, "a"), ifl.locate("a", null, false));
        assertEquals("[no cache] did not find a nonexistent file in an existing dir", null, ifl.locate("a/x", null, false));
        assertEquals("[no cache] found a multilevel override in an nbdirs component", file(nbdir1, "e/f/g"), ifl.locate("e/f/g", null, false));
        assertEquals("[no cache] all nbdirs components scanned", file(nbdir2, "h"), ifl.locate("h", null, false));
        assertEquals("[no cache] localized and branded resource can be found", file(nbhome, "loc/x_foo_ja.html"), ifl.locate("loc/x.html", null, true));
        assertEquals("[no cache] nbdirs can override location of a branded resource", file(nbdir2, "loc/y_foo.html"), ifl.locate("loc/y.html", null, true));
        assertEquals("[no cache] but look in all dirs for most specific resource first", file(nbhome, "h_ja"), ifl.locate("h", null, true));
        assertEquals("[no cache] localized lookup a no-op for nonlocalized files", file(nbuser, "a/b"), ifl.locate("a/b", null, true));
    }
    
}
