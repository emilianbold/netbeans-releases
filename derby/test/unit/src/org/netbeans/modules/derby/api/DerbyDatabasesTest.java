/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.derby.api;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.derby.DerbyDatabasesImpl;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.test.TestBase;
import org.openide.modules.InstalledFileLocator;

/**
 *
 * @author Andrei Badea
 */
public class DerbyDatabasesTest extends TestBase {

    private File systemHome;

    public DerbyDatabasesTest(String testName) {
        super(testName);
    }

    public void setUp() throws Exception {
        clearWorkDir();

        systemHome = new File(getWorkDir(), ".netbeans-derby");
        systemHome.mkdirs();

        DerbyOptions.getDefault().setSystemHome(systemHome.getAbsolutePath());
    }

    public void testGetFirstFreeDatabaseName() throws Exception {
        assertEquals("testdb", DerbyDatabases.getFirstFreeDatabaseName("testdb"));

        new File(systemHome, "testdb").createNewFile();

        assertEquals("testdb1", DerbyDatabases.getFirstFreeDatabaseName("testdb"));

        new File(systemHome, "testdb1").createNewFile();

        assertEquals("testdb2", DerbyDatabases.getFirstFreeDatabaseName("testdb"));
    }

    public void testDatabaseExists() throws Exception {
        assertFalse(DerbyDatabases.databaseExists(""));
        assertFalse(DerbyDatabases.databaseExists("testdb"));

        new File(systemHome, "testdb").createNewFile();

        assertTrue(DerbyDatabases.databaseExists("testdb"));
    }

    public void testGetFirstIllegalCharacter() throws Exception {
        assertEquals((int)File.separatorChar, DerbyDatabases.getFirstIllegalCharacter("a" + File.separatorChar + "b"));
        assertEquals((int)'/', DerbyDatabases.getFirstIllegalCharacter("a/b"));
    }

    public void testExtractSampleDatabase() throws Exception {
        setLookup(new Object[] { new SampleDatabaseLocator() });

        DerbyDatabasesImpl.getDefault().extractSampleDatabase("newdb");
        File newDBDir = new File(systemHome, "newdb");
        Set sampleDBFiles = new HashSet(Arrays.asList(newDBDir.list()));

        assertEquals(3, sampleDBFiles.size());
        assertTrue(sampleDBFiles.contains("log"));
        assertTrue(sampleDBFiles.contains("seg0"));
        assertTrue(sampleDBFiles.contains("service.properties"));
    }

    public void testDatabaseNotExtractedToExistingDirectoryIssue80122() throws Exception {
        setLookup(new Object[] { new SampleDatabaseLocator() });

        File sampleDir = new File(systemHome, "sample");
        sampleDir.mkdirs();

        assertEquals("There should be no files in the sample directory", 0, sampleDir.listFiles().length);

        DerbyDatabasesImpl.getDefault().extractSampleDatabase("sample");

        assertEquals("Should not have extracted the sample database to an existing directory", 0, sampleDir.listFiles().length);
    }

    private static final class SampleDatabaseLocator extends InstalledFileLocator {

        public File directory;

        public SampleDatabaseLocator() {
            File derbyModule = new File(URI.create(DerbyOptions.class.getProtectionDomain().getCodeSource().getLocation().toExternalForm()));
            directory = derbyModule.getParentFile().getParentFile();
        }

        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if ("modules/ext/derbysampledb.zip".equals(relativePath)) {
                return new File(directory, relativePath);
            }
            return null;
        }
    }
}
