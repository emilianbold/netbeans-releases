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
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.lib.lexer.test.TestLanguageProvider;
import org.netbeans.modules.csl.api.Formatter;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import org.netbeans.modules.html.editor.lib.api.HtmlVersion;
import org.netbeans.modules.php.editor.PHPCodeCompletionTestBase;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class PHPFormatter2Test extends PHPCodeCompletionTestBase {
    private String FORMAT_START_MARK = "/*FORMAT_START*/"; //NOI18N
    private String FORMAT_END_MARK = "/*FORMAT_END*/"; //NOI18N

    public PHPFormatter2Test(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        try {
            TestLanguageProvider.register(HTMLTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        try {
            TestLanguageProvider.register(PHPTokenId.language());
        } catch (IllegalStateException ise) {
            // Ignore -- we've already registered this either via layers or other means
        }
        HtmlVersion.DEFAULT_VERSION_UNIT_TESTS_OVERRIDE = HtmlVersion.HTML41_TRANSATIONAL;
    }

    protected void reformatFileContents(String file, IndentPrefs preferences, int initialIndent) throws Exception {
        FileObject fo = getTestFile(file);
        assertNotNull(fo);
        BaseDocument doc = getDocument(fo);
        assertNotNull(doc);
        String fullTxt = doc.getText(0, doc.getLength());
        int formatStart = 0;
        int formatEnd = doc.getLength();
        int startMarkPos = fullTxt.indexOf(FORMAT_START_MARK);

        if (startMarkPos >= 0){
            formatStart = startMarkPos + FORMAT_START_MARK.length();
            formatEnd = fullTxt.indexOf(FORMAT_END_MARK);

            if (formatEnd == -1){
                throw new IllegalStateException();
            }
        }

        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        prefs.putInt(FmtOptions.initialIndent, initialIndent);
        prefs.putInt(FmtOptions.continuationIndentSize, 4);

        format(doc, formatter, formatStart, formatEnd, false);

        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }

    protected void reformatFileContents(String file, Map<String, Object> options) throws Exception {
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


        IndentPrefs preferences = new IndentPrefs(4, 4);
        Formatter formatter = getFormatter(preferences);
        //assertNotNull("getFormatter must be implemented", formatter);

        setupDocumentIndentation(doc, preferences);

        Preferences prefs = CodeStylePreferences.get(doc).getPreferences();
        for (String option : options.keySet()) {
            Object value = options.get(option);
            if (value instanceof Integer) {
                prefs.putInt(option, ((Integer)value).intValue());
            }
            else if (value instanceof String) {
                prefs.put(option, (String)value);
            }
            else if (value instanceof Boolean) {
                prefs.put(option, ((Boolean)value).toString());
            }
	    else if (value instanceof CodeStyle.BracePlacement) {
		prefs.put(option, ((CodeStyle.BracePlacement)value).name());
	    }
	    else if (value instanceof CodeStyle.WrapStyle) {
		prefs.put(option, ((CodeStyle.WrapStyle)value).name());
	    }
        }

        format(doc, formatter, formatStart, formatEnd, false);
        String after = doc.getText(0, doc.getLength());
        assertDescriptionMatches(file, after, false, ".formatted");
    }


    public void testSpacesBeforeAfterComma01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, false);
	options.put(FmtOptions.spaceAfterComma, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma01.php", options);
    }

    public void testSpacesBeforeAfterComma02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, false);
	options.put(FmtOptions.spaceAfterComma, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma02.php", options);
    }

    public void testSpacesBeforeAfterComma03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, true);
	options.put(FmtOptions.spaceAfterComma, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma03.php", options);
    }

    public void testSpacesBeforeAfterComma04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeComma, true);
	options.put(FmtOptions.spaceAfterComma, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma04.php", options);
    }

    public void testSpacesBeforeUnaryOps01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps01.php", options);
    }

    public void testSpacesBeforeUnaryOps02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceWithinIfParens, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps02.php", options);
    }

    public void testSpacesBeforeUnaryOps03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceWithinIfParens, true);
	options.put(FmtOptions.spaceAroundUnaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps03.php", options);
    }

    public void testSpacesBeforeUseStatementPart01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart01.php", options);
    }

    public void testSpacesBeforeUseStatementPart02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart02.php", options);
    }

    public void testSpacesBeforeUseStatementPart03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart03.php", options);
    }

    public void testSpacesBeforeUseStatementPart04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart04.php", options);
    }

    public void testSpacesBeforeUseStatementPart05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart05.php", options);
    }

    public void testSpacesBeforeUseStatementPart06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeUseStatementPart06.php", options);
    }

    public void testSpacesBeforeKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords01.php", options);
    }

    public void testSpacesBeforeKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords02.php", options);
    }

    public void testSpacesBeforeKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeWhile, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords03.php", options);
    }

    public void testSpacesBeforeKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceBeforeElse, true);
	options.put(FmtOptions.spaceBeforeWhile, true);
	options.put(FmtOptions.spaceBeforeCatch, true);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords04.php", options);
    }

    public void testIssue180859_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAroundBinaryOps, true);
        reformatFileContents("testfiles/formatting/spaces/issue180859_01.php", options);
    }

    public void testIssue180859_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAroundBinaryOps, false);
        reformatFileContents("testfiles/formatting/spaces/issue180859_02.php", options);
    }

    public void testSpaceAfterShortPHPTag_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAfterShortPHPTag, true);
        options.put(FmtOptions.spaceBeforeClosePHPTag, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterShortPHPTag01.php", options);
    }

    public void testSpaceAfterShortPHPTag_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.spaceAfterShortPHPTag, false);
        options.put(FmtOptions.spaceBeforeClosePHPTag, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterShortPHPTag02.php", options);
    }

    public void testSpacesBeforeAfterSemi01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, false);
	options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi01.php", options);
    }

    public void testSpacesBeforeAfterSemi02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, false);
	options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi02.php", options);
    }

    public void testSpacesBeforeAfterSemi03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, true);
	options.put(FmtOptions.spaceAfterSemi, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi03.php", options);
    }

    public void testSpacesBeforeAfterSemi04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeSemi, true);
	options.put(FmtOptions.spaceAfterSemi, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi04.php", options);
    }

    public void xxxtestSpacesCheckAfterKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceCheckAfterKeywords, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords01.php", options);
    }

    public void xxxtestSpacesCheckAfterKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceCheckAfterKeywords, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords02.php", options);
    }

    public void testIssue181003_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue181003_01.php", options);
    }

    public void testIssue181003_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_02.php", options);
    }

    public void testIssue181003_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_03.php", options);
    }

    public void testIssue181003_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.blankLinesAfterClassHeader, 0);
	options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
	options.put(FmtOptions.blankLinesBeforeFunction, 0);
	options.put(FmtOptions.blankLinesAfterFunction, 0);
	options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        reformatFileContents("testfiles/formatting/blankLines/issue181003_04.php", options);
    }

    public void testAlignmentKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
	options.put(FmtOptions.placeNewLineAfterModifiers, true);
        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords01.php", options);
    }

    public void testAlignmentKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
	options.put(FmtOptions.placeNewLineAfterModifiers, true);

	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.spaceBeforeWhile, false);
        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords02.php", options);
    }

    public void testAlignmentKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, true);
	options.put(FmtOptions.placeElseOnNewLine, true);
	options.put(FmtOptions.placeCatchOnNewLine, true);
	options.put(FmtOptions.placeNewLineAfterModifiers, true);

	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.spaceBeforeWhile, false);

	options.put(FmtOptions.classDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.methodDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.ifBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.whileBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.catchBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.otherBracePlacement, FmtOptions.OBRACE_NEWLINE);

        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords03.php", options);
    }

    public void testAlignmentKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.placeWhileOnNewLine, false);
	options.put(FmtOptions.placeElseOnNewLine, false);
	options.put(FmtOptions.placeCatchOnNewLine, false);
	options.put(FmtOptions.placeNewLineAfterModifiers, false);

	options.put(FmtOptions.spaceBeforeElse, false);
	options.put(FmtOptions.spaceBeforeCatch, false);
	options.put(FmtOptions.spaceBeforeWhile, false);

	options.put(FmtOptions.classDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.methodDeclBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.ifBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.whileBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.catchBracePlacement, FmtOptions.OBRACE_NEWLINE);
	options.put(FmtOptions.otherBracePlacement, FmtOptions.OBRACE_NEWLINE);

        reformatFileContents("testfiles/formatting/alignment/alignmentKeywords04.php", options);
    }

    public void testGroupAlignmentAssignment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.groupAlignmentAssignment, true);

        reformatFileContents("testfiles/formatting/alignment/groupAlignmentAssignment01.php", options);
    }

    public void testIssue209030() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.groupAlignmentAssignment, true);
        reformatFileContents("testfiles/formatting/alignment/issue209030.php", options);
    }

    public void testIssue211482() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.groupAlignmentAssignment, true);
        reformatFileContents("testfiles/formatting/alignment/issue211482.php", options);
    }

    public void testGroupAlignmentArrayInit01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.groupAlignmentArrayInit, true);

        reformatFileContents("testfiles/formatting/alignment/groupAlignmentArrayInit01.php", options);
    }

    public void testIssue210617() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.groupAlignmentArrayInit, true);
        options.put(FmtOptions.groupAlignmentAssignment, true);
        options.put(FmtOptions.expandTabToSpaces, false);
        options.put(FmtOptions.tabSize, 4);
        reformatFileContents("testfiles/formatting/alignment/issue210617.php", options);
    }

    public void testIssue181624_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue181624_01.php", options);
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
        reformatFileContents("testfiles/formatting/wrapping/binaryOps02.php", options);
    }

    public void testWrappingBinaryOps03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_IF_LONG);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps03.php", options);
    }

    public void testWrappingBinaryOps04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.wrapBinaryOps, CodeStyle.WrapStyle.WRAP_ALWAYS);
        reformatFileContents("testfiles/formatting/wrapping/binaryOps04.php", options);
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

    public void testIssue181588() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/issue181588.php", options);
    }

    public void testLineComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment01.php", options);
    }

    public void testLineComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment02.php", options);
    }

    public void testLineComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment03.php", options);
    }

    public void testLineComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment04.php", options);
    }

    public void testLineComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/lineComment05.php", options);
    }

    public void testComment01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment01.php", options);
    }

    public void testComment02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment02.php", options);
    }

    public void testComment03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment03.php", options);
    }

    public void testComment04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment04.php", options);
    }

    public void testComment05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment05.php", options);
    }

    public void testComment06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment06.php", options);
    }

    public void testComment07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        // Be careful during editing the test file. The space after /*  is important.
	reformatFileContents("testfiles/formatting/comment07.php", options);
    }

    public void testComment08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment08.php", options);
    }

    public void testComment09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment09.php", options);
    }

    public void testComment10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment10.php", options);
    }

    public void testComment11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/comment11.php", options);
    }

    // the html tests doesn't work properly, the results are deferent then in the ide. i don't know why.
    public void testHtml01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html01.php", options);
    }

    public void testHtml02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
	reformatFileContents("testfiles/formatting/html/html02.php", options);
    }

    public void testHtml03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	options.put(FmtOptions.initialIndent, 4);
	reformatFileContents("testfiles/formatting/html/html03.php", options);
    }

    public void testHtml04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html04.php", options);
    }

    public void testHtml05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/html05.php", options);
    }

    public void testIssue175229() throws Exception {

        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue175229.php", options);
    }

    public void testIssue183268() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue183268.php", options);
    }

    public void testIssue179108_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue179108_01.php", options);
    }

    public void testIssue179108_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue179108_02.php", options);
    }

    public void testIssue187309() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue187309.php", options);
    }

    public void testIssue190652() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
	reformatFileContents("testfiles/formatting/html/issue190652.php", options);
    }

    public void test183200_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_01.php", options);
    }

    public void test183200_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue183200_02.php", options);
    }

    public void test182072_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue182072_01.php", options);
    }

    public void test180332_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue180332_01.php", options);
    }

    public void test168396_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue168396_01.php", options);
    }

    public void testIssue184687_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/issue184687_01.php", options);
    }

    public void testIssue184687_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/issue184687_02.php", options);
    }

    public void testIssue185353_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_01.php", options);
    }

    public void testIssue185353_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_02.php", options);
    }

    public void testIssue185353_03() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_03.php", options);
    }

    public void testIssue185353_04() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_04.php", options);
    }

    public void testIssue185353_05() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue185353_05.php", options);
    }

    public void testIssue186183_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue186183_01.php", options);
    }

    public void testIssue186461_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186461_01.php", options);
    }

    public void testIssue186461_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186461_02.php", options);
    }

    public void testIssue186738_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue186738_01.php", options);
    }

    public void testIssue187264_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue187264_01.php", options);
    }

    public void testIssue187264_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue187264_02.php", options);
    }

    public void testIssue187665_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187665_01.php", options);
    }

    public void testIssue187665_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187665_02.php", options);
    }

    public void testIssue187888_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187888_01.php", options);
    }

    public void testIssue187888_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187888_02.php", options);
    }

    public void testIssue187864_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue187864_01.php", options);
    }

    public void testIssue189002_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue189002_01.php", options);
    }

    public void testIssue189002_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue189002_02.php", options);
    }

    public void testHtmlIf_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/if_01.php", options);
    }

    public void testHtmlIf_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/if_02.php", options);
    }

    public void testHtmlIf_03() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/if_03.php", options);
    }

    public void testIssue188810_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue188810_01.php", options);
    }

    public void testIssue189850() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue189850.php", options);
    }

    public void testIssue189835_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_01.php", options);
    }

    public void testIssue189835_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_02.php", options);
    }

    public void testIssue189835_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_03.php", options);
    }

    public void testIssue189835_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_04.php", options);
    }

    public void testIssue189835_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_05.php", options);
    }

    public void testIssue189835_06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue189835_06.php", options);
    }

    public void testIssue190544() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue190544.php", options);
    }

    public void testIssue179184() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue179184.php", options);
    }

    public void testIssue179184_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/html/issue179184_02.php", options);
    }

    public void testIssue190426() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue190426.php", options);
    }

    public void testIssue19893_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue191893_01.php", options);
    }

    public void testIssue197074_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.continuationIndentSize, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_01.php", options);
    }

    public void testIssue197074_02() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.continuationIndentSize, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_02.php", options);
    }

    public void testIssue197074_03() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.continuationIndentSize, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_03.php", options);
    }

    public void testIssue197074_04() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.continuationIndentSize, 4);
        reformatFileContents("testfiles/formatting/broken/issue197074_04.php", options);
    }

    public void testIssue197617_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue197617_01.php", options);
    }

     public void testIssue197304_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue197304_01.php", options);
    }

    public void testIssue199298_01() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue199298_01.php", options);
    }

    public void testIssue195562() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.continuationIndentSize, 4);
        reformatFileContents("testfiles/formatting/spaces/issue195562.php", options);
    }

    public void testIssue196405() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue196405.php", options);
    }

    public void testIssue197698() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue197698.php", options);
    }

    public void testIssue199654() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue199654.php", options);
    }

    public void testIssue201994() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue201994_01.php", options);
    }

    public void testIssue203160() throws Exception {
	HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue203160_01.php", options);
    }

    public void testTraitUsesBlankLines_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.blankLinesBeforeUseTrait, 0);
        reformatFileContents("testfiles/formatting/blankLines/TraitUses01.php", options);
    }

    public void testTraitUsesBlankLines_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.blankLinesBeforeUseTrait, 1);
        reformatFileContents("testfiles/formatting/blankLines/TraitUses02.php", options);
    }

    public void testTraitUsesSpaces_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeUseTraitBodyLeftBrace, true);
        reformatFileContents("testfiles/formatting/spaces/TraitUses01.php", options);
    }

    public void testTraitUsesSpaces_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeUseTraitBodyLeftBrace, false);
        reformatFileContents("testfiles/formatting/spaces/TraitUses02.php", options);
    }

    public void testTraitUsesBracePlacement_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/TraitUses01.php", options);
    }

    public void testTraitUsesBracePlacement_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/TraitUses02.php", options);
    }

    public void testTraitUsesBracePlacement_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/TraitUses03.php", options);
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

    public void testIssue202940_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_01.php", options);
    }

    public void testIssue202940_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_02.php", options);
    }

    public void testIssue202940_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_03.php", options);
    }

    public void testIssue202940_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_04.php", options);
    }

    public void testIssue202940_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.useTraitBodyBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_05.php", options);
    }

    public void testIssue187757() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/issue187757.php", options);
    }

}
