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
package org.netbeans.modules.findbugs;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class RunFindBugsTest extends NbTestCase {

    public RunFindBugsTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        super.setUp();
    }

    private FileObject sourceRoot;
    private FileObject testSource;

    private void prepareTest(String code) throws Exception {
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        this.sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        testSource = FileUtil.createData(sourceRoot, "test/Test.java");

        assertNotNull(testSource);

        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);

        SourceUtilsTestUtil.compileRecursively(sourceRoot);
    }


    public void testRunFindBugs() throws Exception {
        prepareTest("package test;\n" +
                    "public class Test {\n" +
                    "    public int test() {\n" +
                    "        String str = null;\n" +
                    "        return str.length();\n" +
                    "    }\n" +
                    "}\n");

        List<String> errors = new ArrayList<String>();

        for (ErrorDescription ed : RunFindBugs.runFindBugs(null, null, null, sourceRoot, null, null, null)) {
            errors.add(ed.toString());
        }

        assertEquals(Arrays.asList("4:8-4:28:verifier:Null pointer dereference of str in test.Test.test()",
                                   "4:8-4:28:verifier:Load of known null value in test.Test.test()"),
                     errors);

        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), "package test;\n" +
                                                                    "public class Test {\n" +
                                                                    "    public int test() {\n" +
                                                                    "        String str = \"foobar\";\n" +
                                                                    "        return str.length();\n" +
                                                                    "    }\n" +
                                                                    "}\n");

        SourceUtilsTestUtil.compileRecursively(sourceRoot);

        assertEquals(0, RunFindBugs.runFindBugs(null, null, null, sourceRoot, null, null, null).size());
    }

    public void testFieldAnnotation() throws Exception {
        prepareTest("package test;\n" +
                    "public class Test {\n" +
                    "    private String str;\n" +
                    "}\n");

        List<String> errors = new ArrayList<String>();

        for (ErrorDescription ed : RunFindBugs.runFindBugs(null, null, null, sourceRoot, null, null, null)) {
            errors.add(ed.toString());
        }

        assertEquals(Arrays.asList("2:19-2:22:verifier:Unused field: test.Test.str"),
                     errors);
    }

    public void DtestMethodAnnotation() throws Exception {
        prepareTest("package test;\n" +
                    "public class Test {\n" +
                    "    private void str() {};\n" +
                    "}\n");

        List<String> errors = new ArrayList<String>();

        for (ErrorDescription ed : RunFindBugs.runFindBugs(null, null, null, sourceRoot, null, null, null)) {
            errors.add(ed.toString());
        }

        assertEquals(Arrays.asList("2:17-2:20:verifier:Private method test.Test.str() is never called"),
                     errors);
    }

    public void DtestClassAnnotation() throws Exception {
        prepareTest("package test;\n" +
                    "public class Test implements java.io.Serializable {\n" +
                    "}\n");

        List<String> errors = new ArrayList<String>();

        for (ErrorDescription ed : RunFindBugs.runFindBugs(null, null, null, sourceRoot, null, null, null)) {
            errors.add(ed.toString());
        }

        assertEquals(Arrays.asList("1:13-1:17:verifier:test.Test is Serializable; consider declaring a serialVersionUID"),
                     errors);
    }
}
