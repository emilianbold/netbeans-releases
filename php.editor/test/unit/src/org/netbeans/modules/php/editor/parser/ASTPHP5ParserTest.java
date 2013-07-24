/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.parser;

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java_cup.runtime.Symbol;
import org.netbeans.modules.php.editor.lexer.PHPLexerUtils;
import org.netbeans.modules.php.editor.parser.astnodes.Program;

/**
 *
 * @author Petr Pisl
 */
public class ASTPHP5ParserTest extends ParserTestBase {

    public ASTPHP5ParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGotoStatment() throws Exception {
        performTest("parser/gotostatement");
    }
    public void testLambdaFunction() throws Exception {
        performTest("parser/lambdaFunction");
    }
    public void testLambdaFunctionWithParams() throws Exception {
        performTest("parser/lambdaFunctionWithParams");
    }
    public void testLambdaFunctionWithParamsWithVars() throws Exception {
        performTest("parser/lambdaFunctionWithParamsWithVars");
    }
    public void testLambdaFunctionWithParamsWithVarsWithStatements() throws Exception {
        performTest("parser/lambdaFunctionWithParamsWithVarsWithStatements");
    }
    public void testMultipleBracketedNamespaces() throws Exception {
        performTest("parser/multipleBracketedNamespaces");
    }
    public void testMultipleUnBracketedNamespaces1() throws Exception {
        performTest("parser/multipleUnBracketedNamespaces1");
    }
    public void testMultipleUnBracketedNamespaces2() throws Exception {
        performTest("parser/multipleUnBracketedNamespaces2");
    }
    public void testNamespaceDeclaration() throws Exception {
        performTest("parser/namespaceDeclaration");
    }
    public void testSubNamespaceDeclaration() throws Exception {
        performTest("parser/subNamespaceDeclaration");
    }
    public void testNamespaceElementDeclarations() throws Exception {
        performTest("parser/namespaceElementDeclarations");
    }
    public void testNowDoc() throws Exception {
        performTest("parser/nowDoc");
    }
    public void testRefLambdaFunctionWithParamsWithVarsWithStatements() throws Exception {
        performTest("parser/refLambdaFunctionWithParamsWithVarsWithStatements");
    }
    public void testTernaryOperator() throws Exception {
        performTest("parser/ternaryOperator");
    }
    public void testUseGlobal() throws Exception {
        performTest("parser/useGlobal");
    }
    public void testUseGlobalSubNamespace() throws Exception {
        performTest("parser/useGlobalSubNamespace");
    }
    public void testUseNamespaceAs() throws Exception {
        performTest("parser/useNamespaceAs");
    }
    public void testUseSimple() throws Exception {
        performTest("parser/useSimple");
    }
    public void testUseSubNamespace() throws Exception {
        performTest("parser/useSubNamespace");
    }
    public void testTextSearchQuery () throws Exception {
        // testing real file from phpwiki
        performTest("parser/TextSearchQuery");
    }

    public void testPHPDoc () throws Exception {
        //unfinished phpdoc
        performTest("parser/test01");
    }

    public void testNowdoc_01 () throws Exception {
        performTest("parser/nowdoc01");
    }

    public void testNowdoc_02() throws Exception {
        performTest("parser/nowdoc02");
    }

    public void testNowdoc_03() throws Exception {
        performTest("parser/nowdoc_000");
    }

    public void testNowdoc_04() throws Exception {
        performTest("parser/nowdoc_001");
    }

    public void testNowdoc_05() throws Exception {
        performTest("parser/nowdoc_002");
    }

    public void testNowdoc_06() throws Exception {
        performTest("parser/nowdoc_003");
    }

    public void testNowdoc_07() throws Exception {
        performTest("parser/nowdoc_004");
    }

    public void testNowdoc_08() throws Exception {
        performTest("parser/nowdoc_005");
    }

    public void testNowdoc_09() throws Exception {
        performTest("parser/nowdoc_006");
    }

    public void testNowdoc_10() throws Exception {
        performTest("parser/nowdoc_007");
    }

    public void testNowdoc_11() throws Exception {
        performTest("parser/nowdoc_008");
    }

    public void testNowdoc_12() throws Exception {
        performTest("parser/nowdoc_009");
    }

    public void testNowdoc_13() throws Exception {
        performTest("parser/nowdoc_010");
    }

    public void testNowdoc_14() throws Exception {
        performTest("parser/nowdoc_011");
    }

    public void testNowdoc_15() throws Exception {
        performTest("parser/nowdoc_012");
    }

    public void testNowdoc_16() throws Exception {
        performTest("parser/nowdoc_013");
    }

    public void testNowdoc_17() throws Exception {
        performTest("parser/nowdoc_014");
    }

    public void testNowdoc_18() throws Exception {
        performTest("parser/nowdoc_015");
    }

    public void testHereDoc_01() throws Exception {
        performTest("parser/heredoc00");
    }

    public void testHereDoc_02() throws Exception {
        performTest("parser/heredoc01");
    }

    public void testHereDoc_03() throws Exception {
        performTest("parser/heredoc_001");
    }

    public void testHereDoc_04() throws Exception {
        performTest("parser/heredoc_002");
    }

    public void testHereDoc_05() throws Exception {
        performTest("parser/heredoc_003");
    }

    public void testHereDoc_06() throws Exception {
        performTest("parser/heredoc_004");
    }

    public void testHereDoc_07() throws Exception {
        performTest("parser/heredoc_005");
    }

    public void testHereDoc_08() throws Exception {
        performTest("parser/heredoc_006");
    }

    public void testHereDoc_09() throws Exception {
        performTest("parser/heredoc_007");
    }

    public void testHereDoc_10() throws Exception {
        performTest("parser/heredoc_008");
    }

    public void testHereDoc_11() throws Exception {
        performTest("parser/heredoc_009");
    }

    public void testHereDoc_12() throws Exception {
        performTest("parser/heredoc_010");
    }

    public void testHereDoc_13() throws Exception {
        performTest("parser/heredoc_011");
    }

    public void testHereDoc_14() throws Exception {
        performTest("parser/heredoc_012");
    }

    public void testHereDoc_15() throws Exception {
        performTest("parser/heredoc_013");
    }

    public void testHereDoc_16() throws Exception {
        performTest("parser/heredoc_014");
    }

    public void testHereDoc_17() throws Exception {
        performTest("parser/heredoc_015");
    }

    public void testVarCommentSimple01() throws Exception {
        performTest("parser/simple01");
    }

    public void testVarCommentSimple02() throws Exception {
        performTest("parser/simple02");
    }

    public void testVarCommentMixedType01() throws Exception {
        performTest("parser/mixed01");
    }

    public void testVarCommentMixedType02() throws Exception {
        performTest("parser/mixed02");
    }

    public void testMagicMethod01() throws Exception {
        performTest("parser/magicMethod01");
    }

    public void testMagicMethod02() throws Exception {
        performTest("parser/magicMethod02");
    }

    public void testMagicMethod03() throws Exception {
        performTest("parser/magicMethod03");
    }

    public void testMagicMethod04() throws Exception {
        performTest("parser/magicMethod04");
    }

    public void testMagicMethod05() throws Exception {
        performTest("parser/magicMethod05");
    }

    public void testMagicMethod06() throws Exception {
        performTest("parser/magicMethod06");
    }

    public void testMagicMethod07() throws Exception {
        performTest("parser/magicMethod07");
    }

    public void testTraits_01() throws Exception {
        performTest("parser/traits_01");
    }

    public void testTraits_02() throws Exception {
        performTest("parser/traits_02");
    }

    public void testTraits_03() throws Exception {
        performTest("parser/traits_03");
    }

    public void testTraits_04() throws Exception {
        performTest("parser/traits_04");
    }

    public void testTraits_05() throws Exception {
        performTest("parser/traits_05");
    }

    public void testTraits_06() throws Exception {
        performTest("parser/traits_06");
    }

    public void testShortArrays_01() throws Exception {
        performTest("parser/shortArrays_01");
    }

    public void testShortArrays_02() throws Exception {
        performTest("parser/shortArrays_02");
    }

    public void testShortArraysStaticScalar_01() throws Exception {
        performTest("parser/shortArraysStaticScalar_01");
    }

    public void testShortArraysStaticScalar_02() throws Exception {
        performTest("parser/shortArraysStaticScalar_02");
    }

    public void testShortEchoSyntax() throws Exception {
        performTest("parser/shortEchoSyntax");
    }

    public void testAnonymousObjectVariable() throws Exception {
        performTest("parser/anonymousObjectVariable");
    }

    public void testFieldArraysWithArrayDereferencing() throws Exception {
        performTest("parser/fieldArraysWithArrayDereferencing");
    }

    public void testArrayDereferencing_01() throws Exception {
        performTest("parser/arrayDereferencing_01");
    }

    public void testArrayDereferencing_02() throws Exception {
        performTest("parser/arrayDereferencing_02");
    }

    public void testArrayDereferencing_03() throws Exception {
        performTest("parser/arrayDereferencing_03");
    }

    public void testArrayDereferencing_04() throws Exception {
        performTest("parser/arrayDereferencing_04");
    }

    public void testArrayDereferencing_05() throws Exception {
        performTest("parser/arrayDereferencing_05");
    }

    public void testArrayDereferencing_06() throws Exception {
        performTest("parser/arrayDereferencing_06");
    }

    public void testArrayDereferencing_07() throws Exception {
        performTest("parser/arrayDereferencing_07");
    }

    public void testArrayDereferencing_08() throws Exception {
        performTest("parser/arrayDereferencing_08");
    }

    public void testArrayDereferencing_09() throws Exception {
        performTest("parser/arrayDereferencing_09");
    }

    public void testBinaryNotation_01() throws Exception {
        performTest("parser/binaryNotation_01");
    }

    public void testBinaryNotation_02() throws Exception {
        performTest("parser/binaryNotation_02");
    }

    public void testBinaryNotation_03() throws Exception {
        performTest("parser/binaryNotation_03");
    }

    public void testBinaryNotation_04() throws Exception {
        performTest("parser/binaryNotation_04");
    }

    public void testStaticExpressionCall_01() throws Exception {
        performTest("parser/staticExpressionCall_01");
    }

    public void testStaticExpressionCall_02() throws Exception {
        performTest("parser/staticExpressionCall_02");
    }

    public void testCfunction() throws Exception {
        performTest("parser/cfunction");
    }

    public void testInstanceOfExpression() throws Exception {
        performTest("parser/instanceOfExpression");
    }

    public void testIssue170712() throws Exception {
        performTest("parser/issue170712");
    }

    public void testIssue200501() throws Exception {
        performTest("parser/issue200501");
    }

    public void testIssue213423() throws Exception {
        performTest("parser/issue213423");
    }

    public void testFinally_01() throws Exception {
        performTest("parser/finally_01");
    }

    public void testFinally_02() throws Exception {
        performTest("parser/finally_02");
    }

    public void testListInForeach() throws Exception {
        performTest("parser/listInForeach");
    }

    @Override
    protected String getTestResult(String filename) throws Exception {
        File testFile = new File(getDataDir(), "testfiles/" + filename + ".php");
        StringBuffer result = new StringBuffer();
        String content = PHPLexerUtils.getFileContent(testFile);
        ASTPHP5Scanner scanner = new ASTPHP5Scanner(new StringReader(content));

        Symbol symbol;
        result.append("<testresult testFile='").append(testFile.getName()).append("'>\n");
        result.append("    <scanner>\n");
        do {
            symbol = scanner.next_token();
            result.append("        <token id='").append(Utils.getASTScannerTokenName(symbol.sym)).append("' start='");
            result.append(symbol.left).append("' end='").append(symbol.right + "'>\n");
            result.append("            <text>");
            result.append(PHPLexerUtils.getXmlStringValue(content.substring(symbol.left, symbol.right)));
            result.append("</text>\n");
            result.append("        </token>\n");
        } while (symbol.sym != ASTPHP5Symbols.EOF);
        result.append("    </scanner>\n");

        scanner.reset(new FileReader(testFile));
        ASTPHP5Parser parser = new ASTPHP5Parser(scanner);
        Symbol root = parser.parse();
        if (root != null){
            Program rootnode = (Program)root.value;

            result.append((new PrintASTVisitor()).printTree(rootnode, 1));
        }
        result.append("</testresult>\n");
        return result.toString();
    }
}
