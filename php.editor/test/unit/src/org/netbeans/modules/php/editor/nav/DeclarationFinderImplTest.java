/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.nav;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.gsf.api.CancellableTask;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.gsf.api.DeclarationFinder.DeclarationLocation;

/**
 *
 * @author Jan Lahoda
 */
public class DeclarationFinderImplTest extends TestBase {

    public DeclarationFinderImplTest(String testName) {
        super(testName);
    }

    public void testSimpleFindDeclaration1() throws Exception {
        performTestSimpleFindDeclaration("<?php\n^$name = \"test\";\n echo \"$na|me\";\n?>");
    }

    public void testSimpleFindDeclaration2() throws Exception {
        performTestSimpleFindDeclaration("<?php\n^$name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n?>");
    }

    public void testSimpleFindDeclaration3() throws Exception {
        performTestSimpleFindDeclaration("<?php\n^$name = \"test\";\n$name = \"test\";\n echo \"$na|me\";\n$name = \"test\";\n?>");
    }

    public void testSimpleFindDeclaration4() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "$name = \"test\";\n" +
                                         "function foo(^$name) {\n" +
                                         "    echo \"$na|me\";\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration5() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "^$name = \"test\";\n" +
                                         "function foo($name) {\n" +
                                         "}\n" +
                                         "echo \"$na|me\";\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration6() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "$name = \"test\";\n" +
                                         "^function foo($name) {\n" +
                                         "}\n" +
                                         "fo|o($name);\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration7() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "^class name {\n" +
                                         "}\n" +
                                         "$r = new na|me();\n" +
                                         "?>");
    }

    public void testSimpleFindDeclaration8() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "class name {\n" +
                                         "    ^function test() {" +
                                         "    }" +
                                         "}\n" +
                                         "$r = new name();\n" +
                                         "$r->te|st();" +
                                         "?>");
    }

    public void testSimpleFindDeclaration9() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "^$name = \"test\";\n" +
                                         "function foo($name) {\n" +
                                         "}\n" +
                                         "foo($na|me);\n" +
                                         "?>");
    }

    public void testFindDeclarationInOtherFile1() throws Exception {
        performTestSimpleFindDeclaration(1,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "^function foo() {}\n" +
                                         "?>");
    }

    public void testFindDeclarationInOtherFile2() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "^function foo() {}\n" +
                                         "?>");
    }

    public void testFindDeclarationInOtherFile3() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "$r = new fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "^class foo {}\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class foo {}\n" +
                                         "?>");
    }

    public void testFunctionsInGlobalScope1() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "^function foo() {}\n" +
                                         "function bar() {\n" +
                                         "    fo|o();\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testClassInGlobalScope1() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "function foo() {" +
                                         "    ^class bar {}\n" +
                                         "}\n" +
                                         "$r = new b|ar();\n" +
                                         "?>");
    }

    public void testArrayVariable() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "^$foo = array();\n" +
                                         "$f|oo['test'] = array();\n" +
                                         "?>");
    }

    public void testResolveUseBeforeDeclaration() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "fo|o();\n" +
                                         "^function foo() {}\n" +
                                         "?>",
                                         "<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testShowAllDeclarationsWhenUnknownForFunctions() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "^function foo() {}\n" +
                                         "?>",
                                          "<?php\n" +
                                         "^function foo() {}\n" +
                                         "?>");
    }

    public void testShowAllDeclarationsWhenUnknownForClasses() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "$r = new fo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "^class foo {}\n" +
                                         "?>",
                                          "<?php\n" +
                                         "^class foo {}\n" +
                                         "?>");
    }

    public void testDefines1() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "define(^'test', 'test');\n" +
                                         "echo \"a\".te|st.\"b\";\n" +
                                         "?>");
    }

    public void testDefines2() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "echo \"a\".te|st.\"b\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "^define('test', 'test');\n" +
                                         "?>");
    }

    public void testGoToInherited() throws Exception {
        performTestSimpleFindDeclaration(0,
                                         "<?php\n" +
                                         "class foo {\n" +
                                         "    ^function test() {}\n" +
                                         "}\n" +
                                         "class bar extends foo {\n" +
                                         "}\n" +
                                         "$r = new bar();\n" +
                                         "$r->te|st();" +
                                         "?>");
    }

    public void testGoToInclude() throws Exception {
        performTestSimpleFindDeclaration(2,
                                         "<?php\n" +
                                         "include \"te|sta.php\";\n" +
                                         "?>",
                                         "^<?php\n" +
                                         "function foo() {}\n" +
                                         "?>");
    }

    public void testGoToInstanceVar() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "class test {\n" +
                                         "    function ftest($name) {\n" +
                                         "        $this->na|me = $name;\n" +
                                         "    }\n" +
                                         "    ^var $name;\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testGoToForward() throws Exception {
        performTestSimpleFindDeclaration("<?php\n" +
                                         "class test {\n" +
                                         "    function ftest($name) {\n" +
                                         "        $this->na|me();\n" +
                                         "    }\n" +
                                         "    ^function name() {}\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testMethodInOtherFile() throws Exception {
        performTestSimpleFindDeclaration(-1,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "$r = new foo();\n" +
                                         "$r->ffo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class foo {\n" +
                                         "    ^function ffoo() {\n" +
                                         "    }\n" +
                                         "}\n" +
                                         "?>");
    }

    public void testMethodInOtherFileWithInheritance() throws Exception {
        performTestSimpleFindDeclaration(-1,
                                         "<?php\n" +
                                         "include \"testa.php\";\n" +
                                         "$r = new foo2();\n" +
                                         "$r->ffo|o();\n" +
                                         "?>",
                                         "<?php\n" +
                                         "include \"testb.php\";\n" +
                                         "class foo2 extends foo3 {}\n" +
                                         "?>",
                                         "<?php\n" +
                                         "class foo3 {\n" +
                                         "    ^function ffoo() {\n" +
                                         "    }\n" +
                                         "}\n" +
                                         "?>");
    }
    
    public void testExtendedClass() throws Exception {
        String userClass = TestUtilities.copyFileToString(new File(getDataDir(), "testfiles/classUser.php"));
        userClass = userClass.replace("extends Man {", "extends M|an {");
        String manClass = TestUtilities.copyFileToString(new File(getDataDir(), "testfiles/classMan.php"));
        manClass = manClass.replace("class Man {", "^class Man {");
        performTestSimpleFindDeclaration(-1, userClass, manClass);
    }

    private void performTestSimpleFindDeclaration(int declarationFile, String... code) throws Exception {
        assertTrue(code.length > 0);

        Set<Golden> golden = new HashSet<Golden>();

        for (int cntr = 0; cntr < code.length; cntr++) {
            int i = code[cntr].replaceAll("\\|", "").indexOf('^');

            if (i != (-1)) {
                golden.add(new Golden(cntr, i));

                code[cntr] = code[cntr].replaceAll("\\^", "");
            }
        }

        int caretOffset = code[0].indexOf('|');

        code[0] = code[0].replaceAll("\\|", "");

        assertTrue(caretOffset != (-1));
        assertFalse(golden.isEmpty());

        performTestSimpleFindDeclaration(code, caretOffset, golden);
    }

    private void performTestSimpleFindDeclaration(String code) throws Exception {
        int caretOffset = code.replaceAll("\\^", "").indexOf('|');
        int declOffset = code.replaceAll("\\|", "").indexOf('^');

        assertTrue(caretOffset != (-1));
        assertTrue(declOffset != (-1));

        performTestSimpleFindDeclaration(code.replaceAll("\\^", "").replaceAll("\\|", ""), caretOffset, declOffset);
    }

    private void performTestSimpleFindDeclaration(String code, final int caretOffset, final int declarationOffset) throws Exception {
        performTestSimpleFindDeclaration(new String[] {code}, caretOffset, 0, declarationOffset);
    }

    private void performTestSimpleFindDeclaration(String[] code, final int caretOffset, final int declarationFile, final int declarationOffset) throws Exception {
        performTestSimpleFindDeclaration(code, caretOffset, Collections.singleton(new Golden(declarationFile, declarationOffset)));
    }

    private void performTestSimpleFindDeclaration(String[] code, final int caretOffset, final Set<Golden> golden) throws Exception {
        performTest(code, new CancellableTask<CompilationInfo>() {
            public void cancel() {}
            public void run(CompilationInfo parameter) throws Exception {
                DeclarationLocation found = DeclarationFinderImpl.findDeclarationImpl(parameter, caretOffset);

                assertNotNull(found.getFileObject());
                Set<Golden> result = new HashSet<Golden>();

                result.add(new Golden(found.getFileObject().getNameExt(), found.getOffset()));

                for (AlternativeLocation l : found.getAlternativeLocations()) {
                    result.add(new Golden(l.getLocation().getFileObject().getNameExt(), l.getLocation().getOffset()));
                }

                assertEquals(golden, result);
            }
        });
    }

    private static final class Golden {
        private String declarationFile;
        private int declarationOffset;

        public Golden(int declarationFile, int declarationOffset) {
            this(computeFileName(declarationFile - 1), declarationOffset);
        }

        public Golden(String declarationFile, int declarationOffset) {
            this.declarationFile = declarationFile;
            this.declarationOffset = declarationOffset;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Golden other = (Golden) obj;
            if (this.declarationFile != other.declarationFile && (this.declarationFile == null || !this.declarationFile.equals(other.declarationFile))) {
                return false;
            }
            if (this.declarationOffset != other.declarationOffset) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + (this.declarationFile != null ? this.declarationFile.hashCode() : 0);
            hash = 29 * hash + this.declarationOffset;
            return hash;
        }

        @Override
        public String toString() {
            return "[Golden: " + declarationFile + ":" + declarationOffset + "]";
        }

    }

}
