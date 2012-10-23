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

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPHintsTest extends PHPHintsTestBase {

    public PHPHintsTest(String testName) {
        super(testName);
    }

    public void testModifiersCheckHint() throws Exception {
        checkHintsInStartEndFile(new ModifiersCheckHintError(), "testModifiersCheckHint.php");
    }

    public void testAbstractClassInstantiationHint() throws Exception {
        checkHintsInStartEndFile(new AbstractClassInstantiationHintError(), "testAbstractClassInstantiationHint.php");
    }

    public void testAbstractClassInstantiationHint_02() throws Exception {
        checkHintsInStartEndFile(new AbstractClassInstantiationHintError(), "testAbstractClassInstantiationHint_02.php");
    }

    public void testImplementAbstractMethodsHint() throws Exception {
        checkHintsInStartEndFile(new ImplementAbstractMethodsHintError(), "testImplementAbstractMethodsHint.php");
    }

    public void testMethodRedeclarationHint() throws Exception {
        checkHintsInStartEndFile(new MethodRedeclarationHintError(), "testMethodRedeclarationHint.php");
    }

    public void testTypeRedeclarationHint() throws Exception {
        checkHintsInStartEndFile(new TypeRedeclarationHintError(), "testTypeRedeclarationHint.php");
    }

    public void testWrongOrderOfArgsHint() throws Exception {
        checkHintsInStartEndFile(new WrongOrderOfArgsHint(), "testWrongOrderOfArgsHint.php");
    }

    public void testUnusedUsesHint() throws Exception {
        checkHintsInStartEndFile(new UnusedUsesHint(), "testUnusedUsesHint.php");
    }

    public void testAmbiguousComparisonHint() throws Exception {
        checkHintsInStartEndFile(new AmbiguousComparisonHint(), "testAmbiguousComparisonHint.php");
    }

    public void testVarDocSuggestion() throws Exception {
        checkSuggestion(new VarDocSuggestion(), "testVarDocSuggestion.php", "$foo^Bar;");
    }

    public void testAssignVariableSuggestion() throws Exception {
        checkSuggestion(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "myFnc();^");
    }

    public void testAssignVariableSuggestion_02() throws Exception {
        checkSuggestion(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "die('message');^");
    }

    public void testAssignVariableSuggestion_03() throws Exception {
        checkSuggestion(new AssignVariableSuggestion(), "testAssignVariableSuggestion.php", "exit('message');^");
    }

    public void testIdenticalComparisonSuggestion() throws Exception {
        checkSuggestion(new IdenticalComparisonSuggestion(), "testIdenticalComparisonSuggestion.php", "if ($a == true)^ {}");
    }

    public void testIntroduceSuggestion_01() throws Exception {
        checkSuggestion(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "new MyClass();^");
    }

    public void testIntroduceSuggestion_02() throws Exception {
        checkSuggestion(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->bar;^");
    }

    public void testIntroduceSuggestion_03() throws Exception {
        checkSuggestion(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "$foo->method();^");
    }

    public void testIntroduceSuggestion_04() throws Exception {
        checkSuggestion(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::CON;^");
    }

    public void testIntroduceSuggestion_05() throws Exception {
        checkSuggestion(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::stMeth();^");
    }

    public void testIntroduceSuggestion_06() throws Exception {
        checkSuggestion(new IntroduceSuggestion(), "testIntroduceSuggestion.php", "Omg::$stFld;^");
    }

    public void testAddUseImportSuggestion_01() throws Exception {
        checkSuggestion(new AddUseImportSuggestion(), "testAddUseImportSuggestion_01.php", "new Foo\\Bar();^");
    }

    public void testAddUseImportSuggestion_02() throws Exception {
        checkSuggestion(new AddUseImportSuggestion(), "testAddUseImportSuggestion_02.php", "new Foox\\Barx();^");
    }

}
