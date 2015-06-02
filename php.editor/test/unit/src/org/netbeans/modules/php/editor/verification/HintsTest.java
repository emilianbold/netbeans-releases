/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.verification;

import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class HintsTest extends PHPHintsTestBase {

    public HintsTest(String testName) {
        super(testName);
    }

    public void testModifiersCheckHint() throws Exception {
        checkHints(new ModifiersCheckHintError(), "testModifiersCheckHint.php");
    }

    public void testAbstractClassInstantiationHint() throws Exception {
        checkHints(new AbstractClassInstantiationHintError(), "testAbstractClassInstantiationHint.php");
    }

    public void testAbstractClassInstantiationHint_02() throws Exception {
        checkHints(new AbstractClassInstantiationHintError(), "testAbstractClassInstantiationHint_02.php");
    }

    public void testImplementAbstractMethodsHint() throws Exception {
        checkHints(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHint.php");
    }

    public void testMethodRedeclarationHint() throws Exception {
        checkHints(new MethodRedeclarationHintError(), "testMethodRedeclarationHint.php");
    }

    public void testTypeRedeclarationHint() throws Exception {
        checkHints(new TypeRedeclarationHintError(), "testTypeRedeclarationHint.php");
    }

    public void testWrongOrderOfArgsHint() throws Exception {
        checkHints(new WrongOrderOfArgsHint(), "testWrongOrderOfArgsHint.php");
    }

    public void testUnusedUsesHint() throws Exception {
        checkHints(new UnusedUsesHint(), "testUnusedUsesHint.php");
    }

    public void testAmbiguousComparisonHint() throws Exception {
        checkHints(new AmbiguousComparisonHint(), "testAmbiguousComparisonHint.php");
    }

    public void testVarDocSuggestion() throws Exception {
        checkHints(new VarDocSuggestion(), "testVarDocSuggestion.php", "$foo^Bar;");
    }

    public void testAssignVariableSuggestion() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "myFnc();^");
    }

    public void testAssignVariableSuggestion_02() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "die('message');^");
    }

    public void testAssignVariableSuggestion_03() throws Exception {
        checkHints(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "exit('message');^");
    }

    public void testIdenticalComparisonSuggestion() throws Exception {
        checkHints(new IdenticalComparisonSuggestion(), "testIdenticalComparisonSuggestion.php", "if ($a == true)^ {}");
    }

    public void testIntroduceSuggestion_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "new MyClass();^");
    }

    public void testIntroduceSuggestion_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->bar;^");
    }

    public void testIntroduceSuggestion_03() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->method();^");
    }

    public void testIntroduceSuggestion_04() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::CON;^");
    }

    public void testIntroduceSuggestion_05() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::stMeth();^");
    }

    public void testIntroduceSuggestion_06() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::$stFld;^");
    }

    public void testAddUseImportSuggestion_01() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testAddUseImportSuggestion_01.php", "new Foo\\Bar();^");
    }

    public void testAddUseImportSuggestion_02() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testAddUseImportSuggestion_02.php", "new Foox\\Barx();^");
    }

    public void testIssue223842() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue223842.php", "Foo::{\"\"}();^");
    }

    public void testIfBracesHint_01() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_01.php");
    }

    public void testIfBracesHint_02() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_02.php");
    }

    public void testIfBracesHint_03() throws Exception {
        checkHints(new BracesHint.IfBracesHint(), "testIfBracesHint_03.php");
    }

    public void testDoWhileBracesHint() throws Exception {
        checkHints(new BracesHint.DoWhileBracesHint(), "testDoWhileBracesHint.php");
    }

    public void testWhileBracesHint() throws Exception {
        checkHints(new BracesHint.WhileBracesHint(), "testWhileBracesHint.php");
    }

    public void testForBracesHint() throws Exception {
        checkHints(new BracesHint.ForBracesHint(), "testForBracesHint.php");
    }

    public void testForEachBracesHint() throws Exception {
        checkHints(new BracesHint.ForEachBracesHint(), "testForEachBracesHint.php");
    }

    public void testGetSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.GetSuperglobalHint(), "testGetSuperglobalsHint.php");
    }

    public void testPostSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.PostSuperglobalHint(), "testPostSuperglobalsHint.php");
    }

    public void testCookieSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.CookieSuperglobalHint(), "testCookieSuperglobalsHint.php");
    }

    public void testServerSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.ServerSuperglobalHint(), "testServerSuperglobalsHint.php");
    }

    public void testEnvSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.EnvSuperglobalHint(), "testEnvSuperglobalsHint.php");
    }

    public void testRequestSuperglobalsHint() throws Exception {
        checkHints(new SuperglobalsHint.RequestSuperglobalHint(), "testRequestSuperglobalsHint.php");
    }

    public void testEmptyStatementHint() throws Exception {
        checkHints(new EmptyStatementHint(), "testEmptyStatementHint.php");
    }

    public void testUnreachableStatementHint() throws Exception {
        checkHints(new UnreachableStatementHint(), "testUnreachableStatementHint.php");
    }

    public void testUnreachableStatementHint_02() throws Exception {
        checkHints(new UnreachableStatementHint(), "testUnreachableStatementHint_02.php");
    }

    public void testParentConstructorCallHint() throws Exception {
        checkHints(new ParentConstructorCallHint(), "testParentConstructorCallHint.php");
    }

    public void testIssue224940() throws Exception {
        checkHints(new AddUseImportSuggestion(), "testIssue224940.php", "echo \"foo $whatever \\n^\";");
    }

    public void testErrorControlOperatorHint() throws Exception {
        checkHints(new ErrorControlOperatorHint(), "testErrorControlOperatorHint.php");
    }

    public void testIssue226494() throws Exception {
        checkHints(new MethodRedeclarationHintError(), "testIssue226494.php");
    }

    public void testClosingDelimUseCase01() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase01.php");
    }

    public void testClosingDelimUseCase02() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase02.php");
    }

    public void testClosingDelimUseCase03() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testClosingDelimUseCase03.php");
    }

    public void testIssue227081() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue227081.php");
    }

    public void testIssue229529() throws Exception {
        checkHints(new AmbiguousComparisonHint(), "testIssue229529.php");
    }

    public void testIssue234983() throws Exception {
        checkHints(new ParentConstructorCallHint(), "testIssue234983.php");
    }

    public void testInitializeFieldSuggestion_01() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_01.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testInitializeFieldSuggestion_02() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_02.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testInitializeFieldSuggestion_03() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_03.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testInitializeFieldSuggestion_04() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testInitializeFieldSuggestion_04.php", "function __construct(\\Bar\\Baz $f^oo) {");
    }

    public void testTooManyReturnStatements() throws Exception {
        checkHints(new TooManyReturnStatementsHint(), "testTooManyReturnStatements.php");
    }

    public void testIssue229522() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue229522.php", "function __construct($par^am) {");
    }

    public void testIssue237726_01() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237726_01.php");
    }

    public void testIssue237726_02() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237726_02.php");
    }

    public void testIssue237768() throws Exception {
        checkHints(new UnnecessaryClosingDelimiterHint(), "testIssue237768.php");
    }

    public void testWrongParamNameHint() throws Exception {
        checkHints(new WrongParamNameHint(), "testWrongParamNameHint.php");
    }

    public void testIssue239277_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue239277.php", "Foo::ahoj(^);");
    }

    public void testIssue239277_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue239277.php", "Bat::$bar^z;");
    }

    public void testIssue239640() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue239640.php", "public function __construct(array $get = array(), array $post = array()^);");
    }

    public void testIssue239640_01() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue239640.php", "public function __construct(array $get = array(), array $post2 = array()^);");
    }

    public void testIssue241824_01() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue241824.php", "(new \\MyFoo(\"Whatever can be here\"))->myFnc()^;");
    }

    public void testIssue241824_02() throws Exception {
        checkHints(new IntroduceSuggestion(), "testIssue241824.php", "(new \\MyFoo(\"Whatever can be here\"))->notMyFnc()^;");
    }

    public void testArraySyntaxSuggestion_01() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_54_OR_NEWER), "testArraySyntaxSuggestion.php", "$foo = ar^ray(");
    }

    public void testArraySyntaxSuggestion_02() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_54_OR_NEWER), "testArraySyntaxSuggestion.php", "11, ^22,");
    }

    public void testArraySyntaxSuggestion_03() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_54_OR_NEWER), "testArraySyntaxSuggestion.php", "2, ^3);");
    }

    public void testArraySyntaxSuggestion_04() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_54_OR_NEWER), "testArraySyntaxSuggestion.php", "$boo = a^rray(");
    }

    public void testArraySyntaxSuggestion_05() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_54_OR_NEWER), "testArraySyntaxSuggestion.php", "\"sdf\" => array(^1, 2, 3)");
    }

    public void testArraySyntaxSuggestion_06() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_54_OR_NEWER), "testArraySyntaxSuggestion.php", ")^; //huhu");
    }

    public void testIssue248013_01() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_53_OR_OLDER), "testArraySyntaxSuggestion.php", "$foo = ar^ray(");
    }

    public void testIssue248013_02() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_53_OR_OLDER), "testArraySyntaxSuggestion.php", "11, ^22,");
    }

    public void testIssue248013_03() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_53_OR_OLDER), "testArraySyntaxSuggestion.php", "2, ^3);");
    }

    public void testIssue248013_04() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_53_OR_OLDER), "testArraySyntaxSuggestion.php", "$boo = a^rray(");
    }

    public void testIssue248013_05() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_53_OR_OLDER), "testArraySyntaxSuggestion.php", "\"sdf\" => array(^1, 2, 3)");
    }

    public void testIssue248013_06() throws Exception {
        checkHints(new ArraySyntaxSuggesionStub(PhpVersion.PHP_53_OR_OLDER), "testArraySyntaxSuggestion.php", ")^; //huhu");
    }

    public void testIssue249306() throws Exception {
        checkHints(new InitializeFieldSuggestion(), "testIssue249306.php", "function __construct(...$f^oo) {");
    }

    private static final class ArraySyntaxSuggesionStub extends ArraySyntaxSuggestion {

        private final PhpVersion phpVersion;

        public ArraySyntaxSuggesionStub(PhpVersion phpVersion) {
            this.phpVersion = phpVersion;
        }

        @Override
        protected boolean isAtLeastPhp54(FileObject fileObject) {
            return phpVersion.isAtLeastPhp54();
        }

    }

    private enum PhpVersion {

        PHP_53_OR_OLDER {
            @Override
            public boolean isAtLeastPhp54() {
                return false;
            }
        },
        PHP_54_OR_NEWER {
            @Override
            public boolean isAtLeastPhp54() {
                return true;
            }
        };

        public abstract boolean isAtLeastPhp54();

    }

}
