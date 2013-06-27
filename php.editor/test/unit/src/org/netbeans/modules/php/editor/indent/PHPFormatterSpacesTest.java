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
public class PHPFormatterSpacesTest extends PHPFormatterTestBase {

    public PHPFormatterSpacesTest(String testName) {
        super(testName);
    }

    public void testSpacesBeforeAfterComma01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, false);
        options.put(FmtOptions.SPACE_AFTER_COMMA, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma01.php", options);
    }

    public void testSpacesBeforeAfterComma02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, false);
        options.put(FmtOptions.SPACE_AFTER_COMMA, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma02.php", options);
    }

    public void testSpacesBeforeAfterComma03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, true);
        options.put(FmtOptions.SPACE_AFTER_COMMA, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma03.php", options);
    }

    public void testSpacesBeforeAfterComma04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_COMMA, true);
        options.put(FmtOptions.SPACE_AFTER_COMMA, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterComma04.php", options);
    }

    public void testSpacesBeforeUnaryOps01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps01.php", options);
    }

    public void testSpacesBeforeUnaryOps02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_IF_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundUnaryOps02.php", options);
    }

    public void testSpacesBeforeUnaryOps03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_IF_PARENS, true);
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, true);
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
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords01.php", options);
    }

    public void testSpacesBeforeKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, true);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords02.php", options);
    }

    public void testSpacesBeforeKeywords03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, false);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords03.php", options);
    }

    public void testSpacesBeforeKeywords04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.PLACE_ELSE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_WHILE_ON_NEW_LINE, false);
        options.put(FmtOptions.PLACE_CATCH_ON_NEW_LINE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeKeywords04.php", options);
    }

    public void testIssue180859_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/issue180859_01.php", options);
    }

    public void testIssue180859_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_BINARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/issue180859_02.php", options);
    }

    public void testSpaceAfterShortPHPTag_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_SHORT_PHP_TAG, true);
        options.put(FmtOptions.SPACE_BEFORE_CLOSE_PHP_TAG, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterShortPHPTag01.php", options);
    }

    public void testSpaceAfterShortPHPTag_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_SHORT_PHP_TAG, false);
        options.put(FmtOptions.SPACE_BEFORE_CLOSE_PHP_TAG, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterShortPHPTag02.php", options);
    }

    public void testSpacesBeforeAfterSemi01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, false);
        options.put(FmtOptions.SPACE_AFTER_SEMI, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi01.php", options);
    }

    public void testSpacesBeforeAfterSemi02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, false);
        options.put(FmtOptions.SPACE_AFTER_SEMI, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi02.php", options);
    }

    public void testSpacesBeforeAfterSemi03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, true);
        options.put(FmtOptions.SPACE_AFTER_SEMI, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi03.php", options);
    }

    public void testSpacesBeforeAfterSemi04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SEMI, true);
        options.put(FmtOptions.SPACE_AFTER_SEMI, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeAfterSemi04.php", options);
    }

    public void xxxtestSpacesCheckAfterKeywords01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_CHECK_AFTER_KEYWORDS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords01.php", options);
    }

    public void xxxtestSpacesCheckAfterKeywords02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_CHECK_AFTER_KEYWORDS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceCheckAfterKeywords02.php", options);
    }

    public void testIssue210617() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.GROUP_ALIGNMENT_ARRAY_INIT, true);
        options.put(FmtOptions.GROUP_ALIGNMENT_ASSIGNMENT, true);
        options.put(FmtOptions.EXPAND_TAB_TO_SPACES, false);
        options.put(FmtOptions.TAB_SIZE, 4);
        reformatFileContents("testfiles/formatting/alignment/issue210617.php", options);
    }

    public void testIssue181624_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue181624_01.php", options);
    }

    public void testIssue186183_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue186183_01.php", options);
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

    public void testIssue188810_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue188810_01.php", options);
    }

    public void testIssue191893_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue191893_01.php", options);
    }

    public void testIssue195562() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CONTINUATION_INDENT_SIZE, 4);
        reformatFileContents("testfiles/formatting/spaces/issue195562.php", options);
    }

    public void testIssue203160() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue203160_01.php", options);
    }

    public void testTraitUsesSpaces_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/TraitUses01.php", options);
    }

    public void testTraitUsesSpaces_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_USE_TRAIT_BODY_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/TraitUses02.php", options);
    }

    public void testIssue202940_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_01.php", options);
    }

    public void testIssue202940_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_02.php", options);
    }

    public void testIssue202940_03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_03.php", options);
    }

    public void testIssue202940_04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_04.php", options);
    }

    public void testIssue202940_05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.CATCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.FOR_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.IF_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.SWITCH_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.USE_TRAIT_BODY_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.WHILE_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/issue202940_05.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace01.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace02.php", options);
    }

    public void testSpacesBeforeClassDecLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeClassDecLeftBrace03.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace01.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace02.php", options);
    }

    public void testSpacesBeforeMethodDeclLeftBrace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclLeftBrace03.php", options);
    }

    public void testSpacesBeforeIfElseIfLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfElseIfLeftBrace01.php", options);
    }

    public void testSpacesBeforeElseLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElseLeftBrace01.php", options);
    }

    public void testSpacesBeforeWhileLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileLeftBrace01.php", options);
    }

    public void testSpacesBeforeDoLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeDoLeftBrace01.php", options);
    }

    public void testSpacesBeforeForLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForLeftBrace01.php", options);
    }

    public void testSpacesBeforeSwitchLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchLeftBrace01.php", options);
    }

    public void testSpacesBeforeTryLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, true);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeTryLeftBrace01.php", options);
    }

    public void testSpacesBeforeCatchLeftBrace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_IF_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_ELSE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_WHILE_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_DO_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_FOR_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_TRY_LEFT_BRACE, false);
        options.put(FmtOptions.SPACE_BEFORE_CATCH_LEFT_BRACE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchLeftBrace01.php", options);
    }

    public void testSpacesBeforeWhile01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile01.php", options);
    }

    public void testSpacesBeforeWhile02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE, true);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile02.php", options);
    }

    public void testSpacesBeforeWhile03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE, false);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhile03.php", options);
    }

    public void testSpacesBeforeElse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse01.php", options);
    }

    public void testSpacesBeforeElse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse02.php", options);
    }

    public void testSpacesBeforeElse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse03.php", options);
    }

    public void testSpacesBeforeElse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_ELSE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeElse04.php", options);
    }

    public void testSpacesBeforeCatch01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch01.php", options);
    }

    public void testSpacesBeforeCatch02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatch02.php", options);
    }

    public void testSpacesBeforeMethodCallParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_CALL_PAREN, true);
        options.put(FmtOptions.SPACE_BEFORE_CLASS_DECL_LEFT_BRACE, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen01.php", options);
    }

    public void testSpacesBeforeMethodCallParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_CALL_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodCallParen02.php", options);
    }

    public void testSpacesBeforeMethodDeclParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen01.php", options);
    }

    public void testSpacesBeforeMethodDeclParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_METHOD_DECL_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeMethodDeclParen02.php", options);
    }

    public void testSpacesBeforeIfParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_IF_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen01.php", options);
    }

    public void testSpacesBeforeIfParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_IF_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeIfParen02.php", options);
    }

    public void testSpacesBeforeForParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FOR_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen01.php", options);
    }

    public void testSpacesBeforeForParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_FOR_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeForParen02.php", options);
    }

    public void testSpacesBeforeWhileParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen01.php", options);
    }

    public void testSpacesBeforeWhileParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_WHILE_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeWhileParen02.php", options);
    }

    public void testSpacesBeforeCatchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen01.php", options);
    }

    public void testSpacesBeforeCatchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_CATCH_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeCatchParen02.php", options);
    }

    public void testSpacesBeforeSwitchParen01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_PAREN, true);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen01.php", options);
    }

    public void testSpacesBeforeSwitchParen02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_PAREN, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        reformatFileContents("testfiles/formatting/spaces/spaceBeforeSwitchParen02.php", options);
    }

    public void testSpacesAroundStringConcat01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_BEFORE_SWITCH_PAREN, true);
        options.put(FmtOptions.SPACE_AROUND_STRING_CONCAT_OPS, false);
        options.put(FmtOptions.CLASS_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.METHOD_DECL_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.OTHER_BRACE_PLACEMENT, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundStringConcat01.php", options);
    }

    public void testSpacesAroundTernaryOp01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp01.php", options);
    }

    public void testSpacesAroundTernaryOp02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp02.php", options);
    }

    public void testSpacesAroundTernaryOp03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_TERNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundTernaryOp03.php", options);
    }

    public void testSpacesAroundKeyValue01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_KEY_VALUE_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp01.php", options);
    }

    public void testSpacesAroundKeyValue02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_KEY_VALUE_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAroundKeyValueOp02.php", options);
    }

    public void testSpacesWithinIfParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_IF_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens01.php", options);
    }

    public void testSpacesWithinForParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_FOR_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens02.php", options);
    }

    public void testSpacesWithinWhileParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_WHILE_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens03.php", options);
    }

    public void testSpacesWithinSwitchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_SWITCH_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens04.php", options);
    }

    public void testSpacesWithinCatchParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_CATCH_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens05.php", options);
    }

    public void testSpacesWithinParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens06.php", options);
    }

    public void testSpacesWithinMethodDeclParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens07.php", options);
    }

    public void testSpacesWithinMethodCallParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinParens08.php", options);
    }

    public void testSpacesWithinMethodDeclParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, true);
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinMethodDecl01.php", options);
    }

    public void testSpacesWithinMethodDeclParens03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_METHOD_DECL_PARENS, false);
        options.put(FmtOptions.SPACE_WITHIN_METHOD_CALL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinMethodDecl02.php", options);
    }

    public void testSpacesWithinTypeCastParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_TYPE_CAST_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinTypeCastParens01.php", options);
    }

    public void testSpacesWithinTypeCastParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_TYPE_CAST_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinTypeCastParens02.php", options);
    }

    public void testSpacesWithinArrayDeclParens01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens01.php", options);
    }

    public void testSpacesWithinArrayDeclParens02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_DECL_PARENS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayDeclParens02.php", options);
    }

    public void testSpacesWithinArrayBrackets01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, false);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets01.php", options);
    }

    public void testSpacesWithinArrayBrackets02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets02.php", options);
    }

    public void testSpacesWithinArrayBrackets03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets03.php", options);
    }

    public void testSpacesWithinArrayBrackets04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_WITHIN_ARRAY_BRACKETS, true);
        reformatFileContents("testfiles/formatting/spaces/spaceWithinArrayBrackets04.php", options);
    }

    public void testSpacesAfterTypeCast01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_TYPE_CAST, false);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast01.php", options);
    }

    public void testSpacesAfterTypeCast02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AFTER_TYPE_CAST, true);
        reformatFileContents("testfiles/formatting/spaces/spaceAfterTypeCast02.php", options);
    }

    public void testIssue228422_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/issue228422_01.php", options);
    }

    public void testIssue228422_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/issue228422_02.php", options);
    }

    public void testIssue230779_01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, true);
        reformatFileContents("testfiles/formatting/spaces/issue230779_01.php", options);
    }

    public void testIssue230779_02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.SPACE_AROUND_UNARY_OPS, false);
        reformatFileContents("testfiles/formatting/spaces/issue230779_02.php", options);
    }

    public void testIssue231387() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/spaces/issue231387.php", options);
    }
}
