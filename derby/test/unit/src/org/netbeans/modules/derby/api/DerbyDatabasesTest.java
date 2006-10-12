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

package org.netbeans.modules.derby.api;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

        DerbyDatabases.extractSampleDatabase("newdb");
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

        DerbyDatabases.extractSampleDatabase("sample");

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
