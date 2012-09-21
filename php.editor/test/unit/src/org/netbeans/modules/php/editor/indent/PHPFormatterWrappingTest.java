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
package org.netbeans.modules.php.editor.indent;

import java.util.HashMap;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPFormatterWrappingTest extends PHPFormatterTestBase {

    public PHPFormatterWrappingTest(String testName) {
        super(testName);
    }

    public void testWrapMethodCallArg01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg01.php", options);
    }

    public void testWrapMethodCallArg02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg02.php", options);
    }

    public void testWrapMethodCallArg03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg03.php", options);
    }

    public void testWrapMethodCallArg04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg04.php", options);
    }

    public void testWrapMethodCallArg05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/methodCallArg05.php", options);
    }

    public void testWrapMethodParams01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams01.php", options);
    }

    public void testWrapMethodParams02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams02.php", options);
    }

    public void testWrapMethodParams03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams03.php", options);
    }

    public void testWrapMethodParams04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams04.php", options);
    }

    public void testWrapMethodParams05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/methodParams05.php", options);
    }

    public void testWrapMethodParams06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.alignMultilineMethodParams, true);
        reformatFileContents("testfiles/formatting/wrapping/methodParams06.php", options);
    }

    public void testWrapMethodParams07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/methodParams07.php", options);
    }

    public void testWrapInterfaces01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/interfaces01.php", options);
    }

    public void testWrapInterfaces02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/interfaces02.php", options);
    }

    public void testWrapInterfaces03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/interfaces03.php", options);
    }

    public void testWrapInterfaces04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapExtendsImplementsList, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/interfaces04.php", options);
    }

    public void testWrapInterfaces05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapExtendsImplementsKeyword, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapExtendsImplementsList, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.alignMultilineImplements, true);
        reformatFileContents("testfiles/formatting/wrapping/interfaces05.php", options);
    }

    public void testMethodChainCall01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.spaceAroundObjectOps, false);
        reformatFileContents("testfiles/formatting/wrapping/methodChainCall_01.php", options);
    }

    public void testWrappingForStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/forStatement01.php", options);
    }

    public void testWrappingForStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/forStatement02.php", options);
    }

    public void testWrappingForStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.initialIndent, 6);
        reformatFileContents("testfiles/formatting/wrapping/forStatement03.php", options);
    }

    public void testWrappingForStatement04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/forStatement04.php", options);
    }

    public void testWrappingForStatement05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/forStatement05.php", options);
    }

    public void testWrappingForStatement06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement06.php", options);
    }

    public void testWrappingForStatement07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.initialIndent, 6);
        reformatFileContents("testfiles/formatting/wrapping/forStatement07.php", options);
    }

    public void testWrappingForStatement08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/wrapping/forStatement08.php", options);
    }

    public void testWrappingForStatement09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement09.php", options);
    }

    public void testWrappingForStatement10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/forStatement10.php", options);
    }

    public void testWrappingWhileStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement01.php", options);
    }

    public void testWrappingWhileStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement02.php", options);
    }

    public void testWrappingWhileStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.initialIndent, 5);
        reformatFileContents("testfiles/formatting/wrapping/whileStatement03.php", options);
    }

    public void testWrappingDoWhileStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/doStatement01.php", options);
    }

    public void testWrappingDoWhileStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/doStatement02.php", options);
    }

    public void testWrappingDoWhileStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/doStatement03.php", options);
    }

    public void testWrappingIfStatement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement01.php", options);
    }

    public void testWrappingIfStatement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement02.php", options);
    }

    public void testWrappingIfStatement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.initialIndent, 54);
        reformatFileContents("testfiles/formatting/wrapping/ifStatement03.php", options);
    }

    public void testWrappingFor01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/for01.php", options);
    }

    public void testWrappingFor02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/for02.php", options);
    }

    public void testWrappingBlock01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/block01.php", options);
    }

    public void testWrappingBlock02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        reformatFileContents("testfiles/formatting/wrapping/block02.php", options);
    }

    public void testWrappingBlock03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, true);
        reformatFileContents("testfiles/formatting/wrapping/block03.php", options);
    }

    public void testWrappingBlock04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        reformatFileContents("testfiles/formatting/wrapping/block04.php", options);
    }

    public void testWrappingBlock05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/wrapping/block05.php", options);
    }

    public void testWrappingBlock06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBlockBraces, false);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/wrapping/block06.php", options);
    }

    public void testWrappingStatements01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/statements01.php", options);
    }

    public void testWrappingStatements02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapStatementsOnTheLine, false);
        options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/wrapping/statements02.php", options);
    }

    public void testWrappingStatements03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapStatementsOnTheLine, false);
        options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/wrapping/statements03.php", options);
    }

    public void testWrappingStatements04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/statements04.php", options);
    }

    public void testWrappingBinaryOps01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/wrapping/binaryOps01.php", options);
    }

    public void testWrappingBinaryOps02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterBinOps, false);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps02.php", options);
    }

    public void testWrappingBinaryOps03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinOps, false);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps03.php", options);
    }

    public void testWrappingBinaryOps04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterBinOps, false);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps04.php", options);
    }

    public void testWrappingBinaryOps05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterBinOps, true);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps05.php", options);
    }

    public void testWrappingBinaryOps06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinOps, true);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps06.php", options);
    }

    public void testWrappingBinaryOps07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterBinOps, true);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps07.php", options);
    }

    public void testTernaryOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp01.php", options);
    }

    public void testTernaryOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp02.php", options);
    }

    public void testTernaryOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/ternaryOp03.php", options);
    }

    public void testIssue189722_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/issue189722_01.php", options);
    }

    public void testIssue189722_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/issue189722_02.php", options);
    }

    public void testIssue189722_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/issue189722_03.php", options);
    }

    public void testIssue189722_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/issue189722_04.php", options);
    }

    public void testIssue189722_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/issue189722_05.php", options);
    }

    public void testIssue189722_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/issue189722_06.php", options);
    }

    public void testIssue211933_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/issue211933_01.php", options);
    }

    public void testIssue211933_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/issue211933_02.php", options);
    }

    public void testIssue211933_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatting/wrapping/issue211933_03.php", options);
    }
}
