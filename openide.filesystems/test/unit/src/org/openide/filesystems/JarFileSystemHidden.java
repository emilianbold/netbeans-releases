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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.openide.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class JarFileSystemHidden extends NbTestCase {

    public JarFileSystemHidden(String name) {
        super(name);
    }

    public void testLazyJarForNonExistingConstructor() throws Exception {
        File f = new File(getWorkDir(), "broken.jar");
        f.createNewFile();

        JarFileSystem fs = new JarFileSystem(f);

        URL u = fs.getRoot().toURL();
        assertNotNull("URL is OK", u);
        if (!u.toExternalForm().startsWith("jar:file") || !u.toExternalForm().endsWith("broken.jar!/")) {
            fail("Unexpected URL: " + u);

        }
        assertEquals("No children", 0, fs.getRoot().getChildren().length);
    }

    public void testEagerJarForNonExistingSetter() throws Exception {
        File f = new File(getWorkDir(), "broken.jar");
        f.createNewFile();

        JarFileSystem fs = new JarFileSystem();
        try {
            fs.setJarFile(f);
            fail("This shall fail, with JarException as the file cannot be opened");
        } catch (FSException ex) {
            assertTrue(ex.getMessage(), ex.getMessage().contains("Error in JAR"));
        }

        assertEquals("No children", 0, fs.getRoot().getChildren().length);
    }

    public void testLazyOpen() throws Exception {
        File f = new File(getWorkDir(), "ok.jar");
        JarOutputStream jos = new JarOutputStream(new FileOutputStream(f));
        jos.putNextEntry(new JarEntry("one"));
        jos.putNextEntry(new JarEntry("two.txt"));
        jos.putNextEntry(new JarEntry("3.txt"));
        jos.close();

        CharSequence log = Log.enable(JarFileSystem.class.getName(), Level.FINE);
        JarFileSystem fs = new JarFileSystem(f);
        final String match = "opened: " + f.getAbsolutePath();
        if (log.toString().contains(match)) {
            fail("The file " + f + " shall not be opened when fs created:\n" + log);
        }

        URL u = fs.getRoot().toURL();
        assertNotNull("URL is OK", u);
        if (!u.toExternalForm().startsWith("jar:file") || !u.toExternalForm().endsWith("ok.jar!/")) {
            fail("Unexpected URL: " + u);
        }
        if (log.toString().contains(match)) {
            fail("The file " + f + " shall not be opened yet:\n" + log);
        }
        assertEquals("Three files", 3, fs.getRoot().getChildren().length);
        if (!log.toString().contains(match)) {
            fail("The file " + f + " shall be opened now:\n" + log);
        }
    }

}
