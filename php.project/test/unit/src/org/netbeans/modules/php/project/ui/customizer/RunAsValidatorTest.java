/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.ui.customizer;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class RunAsValidatorTest extends NbTestCase {

    private static final String VALID_URL = "http://localhost/";
    private static final String INDEX_NAME = "index.php";

    private File INDEX;


    public RunAsValidatorTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        INDEX = new File(getWorkDir(), INDEX_NAME);
        assertTrue("Test file should be created", INDEX.createNewFile());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
    }

    public void testValidateWebFields() throws Exception {
        // fileobject
        assertNull(RunAsValidator.validateWebFields(VALID_URL, FileUtil.toFileObject(getWorkDir()), INDEX_NAME, null));
        // errors
        assertNotNull(RunAsValidator.validateWebFields(VALID_URL, (FileObject) null, "abc.php", null));
        assertNotNull(RunAsValidator.validateWebFields(VALID_URL, FileUtil.toFileObject(getWorkDir()), "abc.php", null));
        assertNotNull(RunAsValidator.validateWebFields(VALID_URL, FileUtil.toFileObject(getDataDir()), INDEX_NAME, null));

        /// file
        assertNull(RunAsValidator.validateWebFields(VALID_URL, getWorkDir(), INDEX_NAME, null));
        // errors
        assertNotNull(RunAsValidator.validateWebFields(VALID_URL, getWorkDir(), "abc.php", null));
        assertNotNull(RunAsValidator.validateWebFields(VALID_URL, getDataDir(), INDEX_NAME, null));
    }

    public void testValidateWorkDir() {
        assertNull(RunAsValidator.validateWorkDir(getWorkDirPath(), false));
        assertNull(RunAsValidator.validateWorkDir(getWorkDirPath(), true));
        assertNull(RunAsValidator.validateWorkDir(null, true));
        assertNull(RunAsValidator.validateWorkDir("", true));
        // errors
        assertNotNull(RunAsValidator.validateWorkDir(null, false));
        assertNotNull(RunAsValidator.validateWorkDir("", false));
        assertNotNull(RunAsValidator.validateWorkDir("/non-existing-dir/", false));
        assertNotNull(RunAsValidator.validateWorkDir(INDEX.getAbsolutePath(), false));
        assertNotNull(RunAsValidator.validateWorkDir(INDEX.getName(), false));
    }

    public void testValidateUploadDirectory() {
        assertNull(RunAsValidator.validateUploadDirectory("/upload/path/to/project", false));
        assertNull(RunAsValidator.validateUploadDirectory("/upload/path/to/project", true));
        assertNull(RunAsValidator.validateUploadDirectory(null, true));
        assertNull(RunAsValidator.validateUploadDirectory("", true));
        // errors
        assertNotNull(RunAsValidator.validateUploadDirectory(null, false));
        assertNotNull(RunAsValidator.validateUploadDirectory("", false));
        assertNotNull(RunAsValidator.validateUploadDirectory("\\", true));
        assertNotNull(RunAsValidator.validateUploadDirectory("a\\b", true));
        assertNotNull(RunAsValidator.validateUploadDirectory("no/slash/", false));
        assertNotNull(RunAsValidator.validateUploadDirectory("no/slash/", true));
    }

    public void testSanitizeUploadDirectory() {
        assertEquals("/upload", RunAsValidator.sanitizeUploadDirectory("/upload", false));
        assertEquals("/upload", RunAsValidator.sanitizeUploadDirectory("/upload/", false));
        assertEquals("/", RunAsValidator.sanitizeUploadDirectory("/", false));
        assertEquals("", RunAsValidator.sanitizeUploadDirectory("", true));
        assertEquals(null, RunAsValidator.sanitizeUploadDirectory(null, true)); // correct?
        assertEquals("aaa", RunAsValidator.sanitizeUploadDirectory("aaa", true));
    }

    public void testValidateIndexFile() throws Exception {
        assertNull(RunAsValidator.validateIndexFile(getWorkDir(), INDEX_NAME, null));
        assertNull(RunAsValidator.validateIndexFile(getWorkDir(), null, null));
        // errors
        assertNotNull(RunAsValidator.validateIndexFile(getWorkDir(), "abc.php", null));
        assertNotNull(RunAsValidator.validateIndexFile(getWorkDir(), "/abc.php", null));
        assertNotNull(RunAsValidator.validateIndexFile(getWorkDir(), "\\abc.php", null));
        assertNotNull(RunAsValidator.validateIndexFile(getWorkDir(), "../" + getWorkDir().getName() + "/" + INDEX_NAME, null));
        assertNotNull(RunAsValidator.validateIndexFile(getWorkDir(), "a/../" + INDEX_NAME, null));
    }

    public void testComposeUrlHint() throws Exception {
        assertEquals(VALID_URL + INDEX_NAME, RunAsValidator.composeUrlHint(VALID_URL, INDEX_NAME, null));
        assertEquals(VALID_URL + INDEX_NAME + "?a=b", RunAsValidator.composeUrlHint(VALID_URL, INDEX_NAME, "a=b"));
        assertEquals("", RunAsValidator.composeUrlHint(null, INDEX_NAME, null));
        // errors
        boolean error = false;
        try {
            RunAsValidator.composeUrlHint("a", INDEX_NAME, null);
            fail("Should not get here");
        } catch (RunAsValidator.InvalidUrlException exc) {
            error = true;
        }
        assertTrue(error);
    }

}
