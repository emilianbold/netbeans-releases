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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.spi.project.libraries.support;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 */
public class LibrariesSupportTest extends NbTestCase {

    public LibrariesSupportTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test of convertFilePathToURL method, of class LibrariesSupport.
     */
    public void testConvertFilePathToURL() {
        String path = "/aa/bb/c c.ext".replace('/', File.separatorChar);
        URL u = LibrariesSupport.convertFilePathToURL(path);
        assertEquals("file:/aa/bb/c%20c.ext", u.toExternalForm());
        path = "../zz/re l.ext".replace('/', File.separatorChar);
        u = LibrariesSupport.convertFilePathToURL(path);
        assertEquals("file:../zz/re%20l.ext", u.toExternalForm());
    }

    /**
     * Test of convertURLToFilePath method, of class LibrariesSupport.
     */
    public void testConvertURLToFilePath() throws MalformedURLException{
        URL u = new URL("file:/aa/bb/c%20c.ext");
        String path = LibrariesSupport.convertURLToFilePath(u);
        assertEquals("/aa/bb/c c.ext".replace('/', File.separatorChar), path);
        u = new URL("file:../zz/re%20l.ext");
        path = LibrariesSupport.convertURLToFilePath(u);
        assertEquals("../zz/re l.ext".replace('/', File.separatorChar), path);
    }

    /**
     * Test of isAbsoluteURL method, of class LibrariesSupport.
     */
    public void testIsAbsoluteURL() throws MalformedURLException{
        URL u = new URL("file", null, "/test/absolute");
        assertTrue(u.toExternalForm(), LibrariesSupport.isAbsoluteURL(u));
        u = new URL("file", null, "../relative");
        assertFalse(u.toExternalForm(), LibrariesSupport.isAbsoluteURL(u));
    }

    /**
     * Test of resolveLibraryEntryFileObject method, of class LibrariesSupport.
     */
    public void testResolveLibraryEntry() throws Exception {
        File f = new File(this.getWorkDir(), "knihovna.properties");
        File f2 = new File(this.getWorkDir(), "bertie.jar");
        f.createNewFile();
        f2.createNewFile();
        FileObject fo = LibrariesSupport.resolveLibraryEntryFileObject(
                f.toURI().toURL(), 
                new URL("file", null, "bertie.jar"));
        assertEquals(f2.getPath(), FileUtil.toFile(fo).getPath());
        fo = LibrariesSupport.resolveLibraryEntryFileObject(
                null, 
                f2.toURI().toURL());
        assertEquals(f2.getPath(), FileUtil.toFile(fo).getPath());
    }

}
