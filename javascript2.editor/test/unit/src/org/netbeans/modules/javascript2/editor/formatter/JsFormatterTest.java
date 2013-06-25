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
package org.netbeans.modules.javascript2.editor.formatter;

import jdk.nashorn.internal.ir.FunctionNode;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import static junit.framework.Assert.assertNull;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.csl.api.test.CslTestBase.IndentPrefs;
import org.netbeans.modules.editor.indent.api.Indent;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.javascript2.editor.JsTestBase;
import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.modules.javascript2.editor.api.lexer.LexUtilities;
import org.netbeans.modules.javascript2.editor.parser.JsParser;
import org.netbeans.modules.javascript2.editor.parser.JsParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Hejl
 */
public class JsFormatterTest extends JsTestBase {

    private String FORMAT_START_MARK = "/*FORMAT_START*/"; //NOI18N
    private String FORMAT_END_MARK = "/*FORMAT_END*/"; //NOI18N

    public JsFormatterTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        reformatFileContents("testfiles/simple.js",new IndentPrefs(4, 4));
    }

    public void testSimpleIndented() throws Exception {
        reindentFileContents("testfiles/simple.js", null);
    }

    public void testScriptInput() throws Exception {
        reformatFileContents("testfiles/scriptInput.js",new IndentPrefs(4, 4));
    }

    public void testScriptInputBroken() throws Exception {
        reformatFileContents("testfiles/scriptInputBroken.js",new IndentPrefs(4, 4));
    }

    public void testTrailingSpaces1() throws Exception {
        format("var a = 1;   \nvar b = 3;                   \n",
                "var a = 1;\nvar b = 3;\n", new IndentPrefs(4, 4));
    }

    public void testTrailingSpaces2() throws Exception {
        format("var a = 1;   \nvar b = 3;                   \n         \n",
                "var a = 1;\nvar b = 3;\n\n", new IndentPrefs(4, 4));
    }

    public void testIndentation1() throws Exception {
        format("\n var a = 1;   \n        var b = 3;                   \n",
                "\nvar a = 1;\nvar b = 3;\n", new IndentPrefs(4, 4));
    }

    public void testIndentation2() throws Exception {
        format(" var a = 1;   \n        var b = 3;                   \n",
                "var a = 1;\nvar b = 3;\n", new IndentPrefs(4, 4));
    }

    public void testFunctions1() throws Exception {
        reformatFileContents("testfiles/formatter/functions1.js",new IndentPrefs(4, 4));
    }

    public void testFunctions1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions1.js");
    }

    public void testFunctions1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions1.js", null);
    }

    public void testFunctions2() throws Exception {
        reformatFileContents("testfiles/formatter/functions2.js",new IndentPrefs(4, 4));
    }

    public void testFunctions2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions2.js");
    }

    public void testFunctions2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions2.js", null);
    }

    public void testFunctions3() throws Exception {
        reformatFileContents("testfiles/formatter/functions3.js",new IndentPrefs(4, 4));
    }

    public void testFunctions3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions3.js");
    }

    public void testFunctions3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions3.js", null);
    }

    public void testFunctions4() throws Exception {
        reformatFileContents("testfiles/formatter/functions4.js",new IndentPrefs(4, 4));
    }

    public void testFunctions4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions4.js");
    }

    public void testFunctions4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions4.js", null);
    }

    public void testFunctions5() throws Exception {
        reformatFileContents("testfiles/formatter/functions5.js",new IndentPrefs(4, 4));
    }

    public void testFunctions5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions5.js");
    }

    public void testFunctions5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions5.js", null);
    }

    public void testFunctions6Default() throws Exception {
        reformatFileContents("testfiles/formatter/functions6.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctions6Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeMethodDeclParen, true);
        reformatFileContents("testfiles/formatter/functions6.js",
                options, ".inverted.formatted");
    }

    public void testFunctions6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions6.js");
    }

    public void testFunctions6Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions6.js", null);
    }

    public void testFunctions7() throws Exception {
        reformatFileContents("testfiles/formatter/functions7.js",new IndentPrefs(4, 4));
    }

    public void testFunctions7Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functions7.js");
    }

    public void testFunctions7Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functions7.js", null);
    }

    public void testFunctionDeclaration1() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeMethodDeclParen, true);
        reformatFileContents("testfiles/formatter/functionDeclaration1.js", options);
    }

    public void testFunctionDeclaration1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration1.js");
    }

    public void testFunctionDeclaration1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration1.js", null);
    }

    public void testFunctionDeclaration2Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionDeclaration2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinMethodDeclParens, true);
        reformatFileContents("testfiles/formatter/functionDeclaration2.js", options, ".inverted.formatted");
    }

    public void testFunctionDeclaration2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration2.js");
    }

    public void testFunctionDeclaration2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration2.js", null);
    }

    public void testFunctionDeclaration3Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionDeclaration3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeMethodDeclLeftBrace, false);
        reformatFileContents("testfiles/formatter/functionDeclaration3.js", options, ".inverted.formatted");
    }

    public void testFunctionDeclaration3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration3.js");
    }

    public void testFunctionDeclaration3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration3.js", null);
    }

    public void testFunctionDeclaration4() throws Exception {
        reformatFileContents("testfiles/formatter/functionDeclaration4.js",new IndentPrefs(4, 4));
    }

    public void testFunctionDeclaration4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration4.js");
    }

    public void testFunctionDeclaration4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration4.js", null);
    }

    public void testFunctionDeclaration5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionDeclaration5.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionDeclaration5Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionDeclaration5.js", options, ".wrapNever.formatted");
    }

    public void testFunctionDeclaration5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodParams, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionDeclaration5.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionDeclaration5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionDeclaration5.js");
    }

    public void testFunctionDeclaration5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionDeclaration5.js", null);
    }

    public void testFunctionCall1Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionCall1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionCall1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeMethodCallParen, true);
        reformatFileContents("testfiles/formatter/functionCall1.js", options, ".inverted.formatted");
    }

    public void testFunctionCall1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall1.js");
    }

    public void testFunctionCall1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall1.js", null);
    }

    public void testFunctionCall2Default() throws Exception {
        reformatFileContents("testfiles/formatter/functionCall2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFunctionCall2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinMethodCallParens, true);
        reformatFileContents("testfiles/formatter/functionCall2.js", options, ".inverted.formatted");
    }

    public void testFunctionCall2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall2.js");
    }

    public void testFunctionCall2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall2.js", null);
    }

    public void testFunctionCall3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionCall3.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionCall3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionCall3.js", options, ".wrapNever.formatted");
    }

    public void testFunctionCall3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionCall3.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionCall3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall3.js");
    }

    public void testFunctionCall3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall3.js", null);
    }

    public void testFunctionCall4Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionCall4.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionCall4Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionCall4.js", options, ".wrapNever.formatted");
    }

    public void testFunctionCall4IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapMethodCallArgs, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionCall4.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionCall4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall4.js");
    }

    public void testFunctionCall4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall4.js", null);
    }

    public void testFunctionCall5WrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapAlways.formatted");
    }

    public void testFunctionCall5WrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapNever.formatted");
    }

    public void testFunctionCall5WrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapIfLong.formatted");
    }

    public void testFunctionCall5WrapBeforeAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterDotInChainedMethodCalls, false);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapBeforeAlways.formatted");
    }

    public void testFunctionCall5WrapBeforeNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterDotInChainedMethodCalls, false);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapBeforeNever.formatted");
    }

    public void testFunctionCall5WrapBeforeIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapChainedMethodCalls, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterDotInChainedMethodCalls, false);
        reformatFileContents("testfiles/formatter/functionCall5.js", options, ".wrapBeforeIfLong.formatted");
    }

    public void testFunctionCall5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/functionCall5.js");
    }

    public void testFunctionCall5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/functionCall5.js", null);
    }

    public void testComments1() throws Exception {
        reformatFileContents("testfiles/formatter/comments1.js",new IndentPrefs(4, 4));
    }

    public void testComments1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/comments1.js");
    }

    public void testComments1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/comments1.js", null);
    }

    public void testComments2() throws Exception {
        reformatFileContents("testfiles/formatter/comments2.js",new IndentPrefs(4, 4));
    }

    public void testComments3() throws Exception {
        reformatFileContents("testfiles/formatter/comments3.js",new IndentPrefs(4, 4));
    }

    public void testObjects1() throws Exception {
        reformatFileContents("testfiles/formatter/objects1.js",new IndentPrefs(4, 4));
    }

    public void testObjects1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects1.js");
    }

    public void testObjects2() throws Exception {
        reformatFileContents("testfiles/formatter/objects2.js",new IndentPrefs(4, 4));
    }

    public void testObjects2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects2.js");
    }

    public void testObjects3() throws Exception {
        reformatFileContents("testfiles/formatter/objects3.js",new IndentPrefs(4, 4));
    }

    public void testObjects3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects3.js");
    }

    public void testObjects4Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects4.js", options, ".wrapAlways.formatted");
    }

    public void testObjects4Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects4.js", options, ".wrapNever.formatted");
    }

    public void testObjects4IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects4.js", options, ".wrapIfLong.formatted");
    }

    public void testObjects4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/objects4.js");
    }

    public void testObjects5() throws Exception {
        reformatFileContents("testfiles/formatter/objects5.js",new IndentPrefs(4, 4));
    }

    public void testObjects6() throws Exception {
        reformatFileContents("testfiles/formatter/objects6.js",new IndentPrefs(4, 4));
    }

    public void testObjects7Default() throws Exception {
        reformatFileContents("testfiles/formatter/objects7.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testObjects7Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeColon, true);
        options.put(FmtOptions.spaceAfterColon, false);
        reformatFileContents("testfiles/formatter/objects7.js", options, ".inverted.formatted");
    }

    public void testObjects8Spaces() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinBraces, true);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".spaces.formatted");
    }

    public void testObjects8Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".wrapAlways.formatted");
    }

    public void testObjects8Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".wrapNever.formatted");
    }

    public void testObjects8IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".wrapIfLong.formatted");
    }

    public void testObjects8ObjectOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".objectOnlyWrapAlways.formatted");
    }

    public void testObjects8ObjectOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".objectOnlyWrapNever.formatted");
    }

    public void testObjects8ObjectOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".objectOnlyWrapIfLong.formatted");
    }

    public void testObjects8PropertiesOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".propertiesOnlyWrapAlways.formatted");
    }

    public void testObjects8PropertiesOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".propertiesOnlyWrapNever.formatted");
    }

    public void testObjects8PropertiesOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects8.js", options, ".propertiesOnlyWrapIfLong.formatted");
    }

    public void testObjects9Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/objects9.js", options, ".wrapAlways.formatted");
    }

    public void testObjects9Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/objects9.js", options, ".wrapNever.formatted");
    }

    public void testObjects9IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/objects9.js", options, ".wrapIfLong.formatted");
    }

    public void testSwitch1() throws Exception {
        reformatFileContents("testfiles/formatter/switch1.js",new IndentPrefs(4, 4));
    }

    public void testSwitch1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch1.js");
    }

    public void testSwitch1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/switch1.js", null);
    }

    public void testSwitch2() throws Exception {
        reformatFileContents("testfiles/formatter/switch2.js",new IndentPrefs(4, 4));
    }

    public void testSwitch2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch2.js");
    }

    public void testSwitch3Default() throws Exception {
        reformatFileContents("testfiles/formatter/switch3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testSwitch3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinSwitchParens, true);
        reformatFileContents("testfiles/formatter/switch3.js",
                options, ".inverted.formatted");
    }

    public void testSwitch3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch3.js");
    }

    public void testSwitch3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/switch3.js", null);
    }

    public void testSwitch4Default() throws Exception {
        reformatFileContents("testfiles/formatter/switch4.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testSwitch4Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeSwitchLeftBrace, false);
        reformatFileContents("testfiles/formatter/switch4.js",
                options, ".inverted.formatted");
    }

    public void testSwitch4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch4.js");
    }

    public void testSwitch5() throws Exception {
        reformatFileContents("testfiles/formatter/switch5.js",new IndentPrefs(4, 4));
    }

    public void testSwitch5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch5.js");
    }

    public void testSwitch6() throws Exception {
        reformatFileContents("testfiles/formatter/switch6.js",new IndentPrefs(4, 4));
    }

    public void testSwitch6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/switch6.js");
    }

    public void testIf1() throws Exception {
        reformatFileContents("testfiles/formatter/if1.js",new IndentPrefs(4, 4));
    }

    public void testIf1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if1.js");
    }

    public void testIf1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if1.js", null);
    }

    public void testIf2Default() throws Exception {
        reformatFileContents("testfiles/formatter/if2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testIf2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinIfParens, true);
        reformatFileContents("testfiles/formatter/if2.js",
                options, ".inverted.formatted");
    }

    public void testIf2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if2.js");
    }

    public void testIf2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if2.js", null);
    }

    public void testIf3Default() throws Exception {
        reformatFileContents("testfiles/formatter/if3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testIf3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeIfLeftBrace, false);
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        reformatFileContents("testfiles/formatter/if3.js",
                options, ".inverted.formatted");
    }

    public void testIf3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if3.js");
    }

    public void testIf3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if3.js", null);
    }

    public void testIf4() throws Exception {
        reformatFileContents("testfiles/formatter/if4.js", new IndentPrefs(4, 4));
    }

    public void testIf4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if4.js");
    }

    public void testIf4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if4.js", null);
    }

    public void testIf5() throws Exception {
        reformatFileContents("testfiles/formatter/if5.js",new IndentPrefs(4, 4));
    }

    public void testIf5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if5.js");
    }

    public void testIf5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if5.js", null);
    }

    public void testIf6() throws Exception {
        reformatFileContents("testfiles/formatter/if6.js",new IndentPrefs(4, 4));
    }

    public void testIf6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if6.js");
    }

    public void testIf6Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if6.js", null);
    }

    public void testIf7() throws Exception {
        reformatFileContents("testfiles/formatter/if7.js",new IndentPrefs(4, 4));
    }

    public void testIf7Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if7.js");
    }

    public void testIf7Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if7.js", null);
    }

    public void testIf8Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/if8.js", options, ".wrapAlways.formatted");
    }

    public void testIf8Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/if8.js", options, ".wrapNever.formatted");
    }

    public void testIf8IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapIfStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/if8.js", options, ".wrapIfLong.formatted");
    }

    public void testIf8Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if8.js");
    }

    public void testIf8Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if8.js", null);
    }

    public void testIf9() throws Exception {
        reformatFileContents("testfiles/formatter/if9.js",new IndentPrefs(4, 4));
    }

    public void testIf9Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/if9.js");
    }

    public void testIf9Indented() throws Exception {
        reindentFileContents("testfiles/formatter/if9.js", null);
    }

    public void testDoWhile1() throws Exception {
        reformatFileContents("testfiles/formatter/dowhile1.js",new IndentPrefs(4, 4));
    }

    public void testDoWhile1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/dowhile1.js");
    }

    public void testDoWhile2Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/dowhile2.js", options, ".wrapAlways.formatted");
    }

    public void testDoWhile2Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/dowhile2.js", options, ".wrapNever.formatted");
    }

    public void testDoWhile2IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapDoWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/dowhile2.js", options, ".wrapIfLong.formatted");
    }

    public void testDoWhile2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/dowhile2.js");
    }

    public void testFor1() throws Exception {
        reformatFileContents("testfiles/formatter/for1.js",new IndentPrefs(4, 4));
    }

    public void testFor1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for1.js");
    }

    public void testFor1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for1.js", null);
    }

    public void testFor2() throws Exception {
        reformatFileContents("testfiles/formatter/for2.js",new IndentPrefs(4, 4));
    }

    public void testFor2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for2.js");
    }

    public void testFor2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for2.js", null);
    }

    public void testFor3Default() throws Exception {
        reformatFileContents("testfiles/formatter/for3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFor3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinForParens, true);
        reformatFileContents("testfiles/formatter/for3.js",
                options, ".inverted.formatted");
    }

    public void testFor3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for3.js");
    }

    public void testFor3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for3.js", null);
    }

    public void testFor4Default() throws Exception {
        reformatFileContents("testfiles/formatter/for4.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testFor4Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        reformatFileContents("testfiles/formatter/for4.js",
                options, ".inverted.formatted");
    }

    public void testFor4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for4.js");
    }

    public void testFor4Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for4.js", null);
    }

    public void testFor5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapAlways.formatted");
    }

    public void testFor5Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapNever.formatted");
    }

    public void testFor5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapIfLong.formatted");
    }

    public void testFor5IfLongNoSpace() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapForStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.spaceBeforeForLeftBrace, false);
        reformatFileContents("testfiles/formatter/for5.js", options, ".wrapIfLongNoSpace.formatted");
    }

    public void testFor5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for5.js");
    }

    public void testFor5Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for5.js", null);
    }

    public void testFor6Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/for6.js", options, ".wrapAlways.formatted");
    }

    public void testFor6Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/for6.js", options, ".wrapNever.formatted");
    }

    public void testFor6IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapFor, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/for6.js", options, ".wrapIfLong.formatted");
    }

    public void testFor6Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/for6.js");
    }

    public void testFor6Indented() throws Exception {
        reindentFileContents("testfiles/formatter/for6.js", null);
    }

    public void testWhile1() throws Exception {
        reformatFileContents("testfiles/formatter/while1.js",new IndentPrefs(4, 4));
    }

    public void testWhile1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while1.js");
    }

    public void testWhile2() throws Exception {
        reformatFileContents("testfiles/formatter/while2.js",new IndentPrefs(4, 4));
    }

    public void testWhile2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while2.js");
    }

    public void testWhile3Default() throws Exception {
        reformatFileContents("testfiles/formatter/while3.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testWhile3Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinWhileParens, true);
        reformatFileContents("testfiles/formatter/while3.js", options, ".inverted.formatted");
    }

    public void testWhile3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while3.js");
    }

    public void testWhile4Default() throws Exception {
        reformatFileContents("testfiles/formatter/while4.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testWhile4Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeWhileLeftBrace, false);
        options.put(FmtOptions.spaceBeforeDoLeftBrace, false);
        reformatFileContents("testfiles/formatter/while4.js", options, ".inverted.formatted");
    }

    public void testWhile4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while4.js");
    }

    public void testWhile5Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/while5.js", options, ".wrapAlways.formatted");
    }

    public void testWhile5Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/while5.js", options, ".wrapNever.formatted");
    }

    public void testWhile5IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapWhileStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/while5.js", options, ".wrapIfLong.formatted");
    }

    public void testWhile5Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/while5.js");
    }

    public void testWith1() throws Exception {
        reformatFileContents("testfiles/formatter/with1.js",new IndentPrefs(4, 4));
    }

    public void testWith1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/with1.js");
    }

    public void testWith2Default() throws Exception {
        reformatFileContents("testfiles/formatter/with2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testWith2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinWithParens, true);
        reformatFileContents("testfiles/formatter/with2.js", options, ".inverted.formatted");
    }

    public void testWith2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/with2.js");
    }

    public void testWith3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapWithStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/with3.js", options, ".wrapAlways.formatted");
    }

    public void testWith3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapWithStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/with3.js", options, ".wrapNever.formatted");
    }

    public void testWith3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapWithStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/with3.js", options, ".wrapIfLong.formatted");
    }

    public void testWith3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/with3.js");
    }

    public void testFormatting1() throws Exception {
        reformatFileContents("testfiles/formatter/formatting1.js",new IndentPrefs(4, 4));
    }

    public void testFormatting1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/formatting1.js");
    }

    public void testFormatting1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/formatting1.js", null);
    }

    public void testFormatting2() throws Exception {
        reformatFileContents("testfiles/formatter/formatting2.js",new IndentPrefs(4, 4));
    }

    public void testFormatting2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/formatting2.js");
    }

    public void testFormatting2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/formatting2.js", null);
    }

    public void testCommas1() throws Exception {
        reformatFileContents("testfiles/formatter/commas1.js",new IndentPrefs(4, 4));
    }

    public void testCommas1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/commas1.js");
    }

    public void testCommas1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/commas1.js", null);
    }

    public void testCommas2() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceAfterComma, false);
        options.put(FmtOptions.spaceBeforeComma, false);
        reformatFileContents("testfiles/formatter/commas2.js", options);
    }

    public void testCommas2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/commas2.js");
    }

    public void testCommas2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/commas2.js", null);
    }

    public void testCommas3() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceAfterComma, false);
        options.put(FmtOptions.spaceBeforeComma, true);
        reformatFileContents("testfiles/formatter/commas3.js", options);
    }

    public void testCommas3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/commas3.js");
    }

    public void testCommas3Indented() throws Exception {
        reindentFileContents("testfiles/formatter/commas3.js", null);
    }

    public void testPrototype() throws Exception {
        reformatFileContents("testfiles/formatter/prototype.js",new IndentPrefs(4, 4));
    }

    public void testDashboard() throws Exception {
        reformatFileContents("testfiles/formatter/dashboard.js",new IndentPrefs(4, 4));
    }

    public void testTabsIndents1Normal() throws Exception {
        reformatFileContents("testfiles/formatter/tabsIndents1.js",
                Collections.<String, Object>emptyMap(), ".normal.formatted");
    }

    public void testTabsIndents1Indented() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatter/tabsIndents1.js",
                options, ".indented.formatted");
    }

    public void testTabsIndents1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/tabsIndents1.js");
    }

    public void testSpaces1Enabled() throws Exception {
        reformatFileContents("testfiles/formatter/spaces1.js",
                Collections.<String, Object>emptyMap(), ".enabled.formatted");
    }

    public void testSpaces1Disabled() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeIfParen, false);
        options.put(FmtOptions.spaceBeforeWhileParen, false);
        options.put(FmtOptions.spaceBeforeForParen, false);
        options.put(FmtOptions.spaceBeforeWithParen, false);
        options.put(FmtOptions.spaceBeforeSwitchParen, false);
        options.put(FmtOptions.spaceBeforeCatchParen, false);
        options.put(FmtOptions.spaceBeforeWhile, false);
        options.put(FmtOptions.spaceBeforeElse, false);
        options.put(FmtOptions.spaceBeforeCatch, false);
        options.put(FmtOptions.spaceBeforeFinally, false);
        reformatFileContents("testfiles/formatter/spaces1.js", options, ".disabled.formatted");
    }

    public void testSpaces1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/spaces1.js");
    }

    public void testOperators1Default() throws Exception {
        reformatFileContents("testfiles/formatter/operators1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testOperators1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceAroundAssignOps, false);
        options.put(FmtOptions.spaceAroundBinaryOps, false);
        options.put(FmtOptions.spaceAroundUnaryOps, true);
        options.put(FmtOptions.spaceAroundTernaryOps, false);
        reformatFileContents("testfiles/formatter/operators1.js", options, ".inverted.formatted");
    }

    public void testOperators1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/operators1.js");
    }

    public void testOperators2BinaryWrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAlways.formatted");
    }

    public void testOperators2BinaryWrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapNever.formatted");
    }

    public void testOperators2BinaryWrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapIfLong.formatted");
    }

    public void testOperators2AssignmentWrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapAssignOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".assignment.wrapAlways.formatted");
    }

    public void testOperators2AssignmentWrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapAssignOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".assignment.wrapNever.formatted");
    }

    public void testOperators2AssignmentWrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapAssignOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".assignment.wrapIfLong.formatted");
    }

    public void testOperators2TernaryWrapAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAlways.formatted");
    }

    public void testOperators2TernaryWrapNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapNever.formatted");
    }

    public void testOperators2TernaryWrapIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapIfLong.formatted");
    }

    public void testOperators2BinaryWrapAfterAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAfterAlways.formatted");
    }

    public void testOperators2BinaryWrapAfterNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAfterNever.formatted");
    }

    public void testOperators2BinaryWrapAfterIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterBinaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".binary.wrapAfterIfLong.formatted");
    }

    public void testOperators2TernaryWrapAfterAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapAfterTernaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAfterAlways.formatted");
    }

    public void testOperators2TernaryWrapAfterNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapAfterTernaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAfterNever.formatted");
    }

    public void testOperators2TernaryWrapAfterIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapTernaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapAfterTernaryOps, true);
        reformatFileContents("testfiles/formatter/operators2.js", options, ".ternary.wrapAfterIfLong.formatted");
    }

    public void testOperators2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/operators2.js");
    }

    public void testSpacesSemicolons1Enabled() throws Exception {
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                Collections.<String, Object>emptyMap(), ".enabled.formatted");
    }

    public void testSpacesSemicolons1SemiDisabled() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                options, ".semiDisabled.formatted");
    }

    public void testSpacesSemicolons1WhileDisabled() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                options, ".whileDisabled.formatted");
    }

    public void testSpacesSemicolons1Disabled() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceAfterSemi, false);
        options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatter/spacesSemicolons1.js",
                options, ".disabled.formatted");
    }

    public void testSpacesSemicolons1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/spacesSemicolons1.js");
    }

    public void testCatch1Default() throws Exception {
        reformatFileContents("testfiles/formatter/catch1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testCatch1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinCatchParens, true);
        reformatFileContents("testfiles/formatter/catch1.js", options, ".inverted.formatted");
    }

    public void testCatch1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/catch1.js");
    }

    public void testCatch1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/catch1.js", null);
    }

    public void testCatch2Default() throws Exception {
        reformatFileContents("testfiles/formatter/catch2.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testCatch2Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeTryLeftBrace, false);
        options.put(FmtOptions.spaceBeforeCatchLeftBrace, false);
        options.put(FmtOptions.spaceBeforeFinallyLeftBrace, false);
        reformatFileContents("testfiles/formatter/catch2.js", options, ".inverted.formatted");
    }

    public void testCatch2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/catch2.js");
    }

    public void testCatch2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/catch2.js", null);
    }

    public void testParentheses1Default() throws Exception {
        reformatFileContents("testfiles/formatter/parentheses1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testParentheses1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinParens, true);
        reformatFileContents("testfiles/formatter/parentheses1.js", options, ".inverted.formatted");
    }

    public void testParentheses1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/parentheses1.js");
    }

    public void testArrays1Default() throws Exception {
        reformatFileContents("testfiles/formatter/arrays1.js",
                Collections.<String, Object>emptyMap(), ".default.formatted");
    }

    public void testArrays1Inverted() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceWithinArrayBrackets, true);
        reformatFileContents("testfiles/formatter/arrays1.js", options, ".inverted.formatted");
    }

    public void testArrays1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrays1.js");
    }

    public void testArrays1Indented() throws Exception {
        reindentFileContents("testfiles/formatter/arrays1.js", null);
    }

    public void testArrays2Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrays2.js", options, ".wrapAlways.formatted");
    }

    public void testArrays2Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays2.js", options, ".wrapNever.formatted");
    }

    public void testArrays2IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrays2.js", options, ".wrapIfLong.formatted");
    }

    public void testArrays2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrays2.js");
    }

    public void testArrays2Indented() throws Exception {
        reindentFileContents("testfiles/formatter/arrays2.js", null);
    }

    public void testArrays3() throws Exception {
        reformatFileContents("testfiles/formatter/arrays3.js",new IndentPrefs(4, 4));
    }

    public void testArrays3Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/arrays3.js");
    }

    public void testArrays4Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".wrapAlways.formatted");
    }

    public void testArrays4Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".wrapNever.formatted");
    }

    public void testArrays4IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".wrapIfLong.formatted");
    }

    public void testArrays4InitializerOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".initializerOnlyWrapAlways.formatted");
    }

    public void testArrays4InitializerOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".initializerOnlyWrapNever.formatted");
    }

    public void testArrays4InitializerOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_IF_LONG);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".initializerOnlyWrapIfLong.formatted");
    }

    public void testArrays4ItemsOnlyAlways() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".itemsOnlyWrapAlways.formatted");
    }

    public void testArrays4ItemsOnlyNever() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".itemsOnlyWrapNever.formatted");
    }

    public void testArrays4ItemsOnlyIfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapArrayInit, CodeStyle.WrapStyle.WRAP_NEVER);
        options.put(FmtOptions.wrapArrayInitItems, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/arrays4.js", options, ".itemsOnlyWrapIfLong.formatted");
    }

    public void testPartialFormat1() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat1.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat2() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat2.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat3() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat3.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat4() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat4.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat5() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat5.js", Collections.<String, Object>emptyMap());
    }

    public void testPartialFormat6() throws Exception {
        reformatFileContents("testfiles/formatter/partialFormat6.js", Collections.<String, Object>emptyMap());
    }

    public void testTernary1() throws Exception {
        reformatFileContents("testfiles/formatter/ternary1.js",new IndentPrefs(4, 4));
    }

    public void testTernary1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/ternary1.js");
    }

    public void testTernary2() throws Exception {
        reformatFileContents("testfiles/formatter/ternary2.js",new IndentPrefs(4, 4));
    }

    public void testTernary2Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/ternary2.js");
    }

    public void testVar1() throws Exception {
        reformatFileContents("testfiles/formatter/var1.js",new IndentPrefs(4, 4));
    }

    public void testVar1Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/var1.js");
    }

    public void testStatements1Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapStatement, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/statements1.js", options, ".wrapAlways.formatted");
    }

    public void testStatements1Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapStatement, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/statements1.js", options, ".wrapNever.formatted");
    }

    public void testStatements1IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapStatement, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/statements1.js", options, ".wrapIfLong.formatted");
    }

    public void testVar2Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/var2.js", options, ".wrapAlways.formatted");
    }

    public void testVar2Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/var2.js", options, ".wrapNever.formatted");
    }

    public void testVar2IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/var2.js", options, ".wrapIfLong.formatted");
    }

    public void testVar3Always() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/var3.js", options, ".wrapAlways.formatted");
    }

    public void testVar3Never() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_NEVER);
        reformatFileContents("testfiles/formatter/var3.js", options, ".wrapNever.formatted");
    }

    public void testVar3IfLong() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapVariables, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatter/var3.js", options, ".wrapIfLong.formatted");
    }

    public void testVar4() throws Exception {
        reformatFileContents("testfiles/formatter/var4.js",new IndentPrefs(4, 4));
    }

    public void testVar4Tokens() throws Exception {
        dumpFormatTokens("testfiles/formatter/var4.js");
    }

    public void testBroken1() throws Exception {
        reformatFileContents("testfiles/formatter/broken1.js",new IndentPrefs(4, 4));
    }

    public void testCodeTemplate1() throws Exception {
        reformatFileContents("testfiles/formatter/codeTemplate1.js",
                Collections.<String, Object>emptyMap(), null, true);
    }

    public void testIssue189745() throws Exception {
        reformatFileContents("testfiles/formatter/issue189745.js",new IndentPrefs(4, 4));
    }

    public void testIssue189745Indented() throws Exception {
        reindentFileContents("testfiles/formatter/issue189745.js", null);
    }

    public void testIssue218090() throws Exception {
        reformatFileContents("testfiles/formatter/issue218090.js",new IndentPrefs(4, 4));
    }

    public void testIssue218328() throws Exception {
        reformatFileContents("testfiles/formatter/issue218328.js",new IndentPrefs(4, 4));
    }

    public void testIssue219046() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.wrapObjects, CodeStyle.WrapStyle.WRAP_ALWAYS);
        options.put(FmtOptions.wrapProperties, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatter/issue219046.js", options);
    }

    public void testIssue220920() throws Exception {
        reformatFileContents("testfiles/formatter/issue220920.js",new IndentPrefs(4, 4));
    }

    public void testIssue221293() throws Exception {
        reformatFileContents("testfiles/formatter/issue221293.js",new IndentPrefs(4, 4));
    }

    public void testIssue221495() throws Exception {
        reformatFileContents("testfiles/formatter/issue221495.js",new IndentPrefs(4, 4));
    }

    public void testIssue224246() throws Exception {
        reformatFileContents("testfiles/formatter/issue224246.js",new IndentPrefs(4, 4));
    }

    public void testIssue225654Partial() throws Exception {
        reformatFileContents("testfiles/formatter/issue225654_partial.js", Collections.<String, Object>emptyMap());
    }

    public void testIssue225654Full() throws Exception {
        reformatFileContents("testfiles/formatter/issue225654_full.js",new IndentPrefs(4, 4));
    }

    public void testIssue226282() throws Exception {
        reformatFileContents("testfiles/formatter/issue226282.js",new IndentPrefs(4, 4));
    }

    public void testIssue228919() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.spaceBeforeElseLeftBrace, false);
        reformatFileContents("testfiles/formatter/issue228919.js", options);
    }

    public void testIssue231163() throws Exception {
        reformatFileContents("testfiles/formatter/issue231163.js",new IndentPrefs(4, 4));
    }

    public void testIssue210134() throws Exception {
        reformatFileContents("testfiles/formatter/issue210134.js",new IndentPrefs(4, 4));
    }

    // test from original formatter

    public void testSemi01() throws Exception {
        format(
                "var p; p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi02() throws Exception {
        format(
                "var p;                           p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi03() throws Exception {
        format(
                "var p;p = 'hello';",
                "var p;\n" +
                "p = 'hello';", null
                );
    }

    public void testSemi04() throws Exception {
        format(
                "var p; p = getName(); p = stripName(p);",
                "var p;\n" +
                "p = getName();\n" +
                "p = stripName(p);", null
                );
    }

    public void testSemi05() throws Exception {
        format(
                "var p; for(var i = 0, l = o.length; i < l; i++) {             createDom(o[i], el);   p = true;} p = stripName(p);",
                "var p;\n" +
                "for (var i = 0, l = o.length; i < l; i++) {\n" +
                "    createDom(o[i], el);\n" +
                "    p = true;\n" +
                "}\n" +
                "p = stripName(p);", null
                );
    }

    public void testSemi06() throws Exception {
        format(
                "if (a == b) { a=c;\n" +
                "    } else if (c == b) { v=d;}",

                "if (a == b) {\n" +
                "    a = c;\n" +
                "} else if (c == b) {\n" +
                "    v = d;\n" +
                "}", null);
    }

    public void testSemi07() throws Exception {
        format(
                "var test = function() { a = b; };",

                "var test = function() {\n" +
                "    a = b;\n" +
                "};", null);
    }

    public void testSemi08() throws Exception {
        format(
                "Spry.forwards = 1; // const\n" +
                "Spry.backwards = 2; // const\n",

                "Spry.forwards = 1; // const\n" +
                "Spry.backwards = 2; // const\n", null);
    }

    public void testCommentAtTheEdnOfLine() throws Exception {
        format (
                "for(var i = 0, l = o.length; i < l; i++) { // some comment \ncreateDom(o[i], el);  p = true;       } //comment2\n p = stripName(p);",
                "for (var i = 0, l = o.length; i < l; i++) { // some comment \n" +
                "    createDom(o[i], el);\n" +
                "    p = true;\n" +
                "} //comment2\n" +
                "p = stripName(p);", null);
    }
    
    // helper methods
    
    protected void dumpFormatTokens(String file) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        dumpFormatTokens(file, text, 0, text.length());
    }

    protected void dumpFormatTokens(String file, String text, int startOffset, int endOffset) throws Exception {

        Document doc = getDocument(text);
        Snapshot snapshot = Source.create(doc).createSnapshot();

        TokenSequence<? extends JsTokenId> ts = LexUtilities.getTokenSequence(
                snapshot.getTokenHierarchy(), startOffset, JsTokenId.javascriptLanguage());

        FormatTokenStream tokenStream = FormatTokenStream.create(
                ts, startOffset, endOffset);
        FormatVisitor visitor = new FormatVisitor(tokenStream,
                ts, endOffset);

        JsParser parser = new JsParser();
        parser.parse(snapshot, null, null);
        FunctionNode root = ((JsParserResult) parser.getResult(null)).getRoot();
        if (root != null) {
            root.accept(visitor);
        }

        StringBuilder sb = new StringBuilder();
        for (FormatToken token : tokenStream.getTokens()) {
            sb.append(token.toString()).append("\n");
        }
        assertDescriptionMatches(file, sb.toString(), false, ".formatTokens");
    }

    protected void reformatFileContents(String file, Map<String, Object> options) throws Exception {
        reformatFileContents(file, options, null, false);
    }

    protected void reformatFileContents(String file, Map<String, Object> options, String suffix) throws Exception {
        reformatFileContents(file, options, suffix, false);
    }

    protected void reformatFileContents(String file, Map<String, Object> options, String suffix, boolean template) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        int formatStart = 0;
        int formatEnd = text.length();
        int startMarkPos = text.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0){
            formatStart = startMarkPos;
            text = text.substring(0, formatStart) + text.substring(formatStart + FORMAT_START_MARK.length());
            formatEnd = text.indexOf(FORMAT_END_MARK);
            text = text.substring(0, formatEnd) + text.substring(formatEnd + FORMAT_END_MARK.length());
            formatEnd --;
            if (formatEnd == -1){
                throw new IllegalStateException();
            }
        }

        BaseDocument doc = getDocument(text);
        assertNotNull(doc);

        if (template) {
            Dictionary<Object, Object> dict = doc.getDocumentProperties();
            dict.put(JsFormatter.CT_HANDLER_DOC_PROPERTY, "test");
        }

        IndentPrefs preferences = new IndentPrefs(4, 4);
        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        // clear prefs
        prefs.clear();

        prefs = CodeStylePreferences.get(doc).getPreferences();
        for (String option : options.keySet()) {
            assertNull(prefs.get(option, null));
            Object value = options.get(option);
            if (value instanceof CodeStyle.BracePlacement) {
                prefs.put(option, ((CodeStyle.BracePlacement) value).name());
            } else if (value instanceof CodeStyle.WrapStyle) {
                prefs.put(option, ((CodeStyle.WrapStyle) value).name());
            } else {
                prefs.put(option, value.toString());
            }
        }

        try {
            format(doc, formatter, formatStart, formatEnd, false);
            // XXX tests fails randomly on this with JDK7
            // XXX so we aretrying to track down whats happening
            Logger.getAnonymousLogger().log(Level.INFO,
                    "Space before method call setting: " + CodeStyle.get(doc).spaceBeforeMethodCallParen());
        } finally {
            for (String option : options.keySet()) {
                prefs.remove(option);
                assertNull(prefs.get(option, null));
            }
        }
        String after = doc.getText(0, doc.getLength());
        String realSuffix = ".formatted";
        if (suffix != null) {
            realSuffix = suffix;
        }
 
        assertDescriptionMatches(file, after, false, realSuffix);
    }

    protected void reindentFileContents(String file, IndentPrefs preferences) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);

        String text = read(fo);

        int formatStart = 0;
        int formatEnd = text.length();

        BaseDocument doc = getDocument(text);
        assertNotNull(doc);

        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        indent(doc, formatter, formatStart, formatEnd);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".indented");
    }

    private void indent(Document document, Formatter formatter, int startPos, int endPos) throws BadLocationException {
        //assertTrue(SwingUtilities.isEventDispatchThread());
        configureIndenters(document, formatter, true);

        final Indent i = Indent.get(document);
        i.lock();
        try {
            if (document instanceof BaseDocument) {
                ((BaseDocument) document).atomicLock();
            }
            try {
                i.reindent(Math.min(document.getLength(), startPos), Math.min(document.getLength(), endPos));
            } finally {
                if (document instanceof BaseDocument) {
                    ((BaseDocument) document).atomicUnlock();
                }
            }
        } finally {
            i.unlock();
        }
    }
}
