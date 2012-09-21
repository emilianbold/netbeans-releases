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
public class PHPFormatterBlankLinesTest extends PHPFormatterTestBase {

    public PHPFormatterBlankLinesTest(String testName) {
        super(testName);
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

    public void testIssue201994() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/issue201994_01.php", options);
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

    public void testBracePlacement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement01.php", options);
    }

    public void testBracePlacement02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement02.php", options);
    }

    public void testBracePlacement03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.ifBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.forBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.whileBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.switchBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.catchBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        reformatFileContents("testfiles/formatting/blankLines/BracePlacement03.php", options);
    }

    public void testAlternativeSyntaxPlacement01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/AlternativeSyntaxPlacement01.php", options);
    }

    // blank lines
    public void testBLClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Class01.php", options);
    }

    public void testBLTrait01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Trait01.php", options);
    }

    public void testBLClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Class02.php", options);
    }

    public void testBLTrait02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.PRESERVE_EXISTING);
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Trait02.php", options);
    }

    public void testBLClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Class03.php", options);
    }

    public void testBLTrait03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, false);
        reformatFileContents("testfiles/formatting/blankLines/Trait03.php", options);
    }

    public void testBLFields01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.spaceBeforeClassDeclLeftBrace, true);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.otherBracePlacement, CodeStyle.BracePlacement.SAME_LINE);
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields01.php", options);
    }

    public void testBLFields02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields02.php", options);
    }

    public void testBLFields03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields03.php", options);
    }

    public void testBLFields04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields04.php", options);
    }

    public void testBLFields05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields05.php", options);
    }

    public void testBLFields06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Fields06.php", options);
    }

    public void testBLFields07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.blankLinesAfterFields, 3);
        reformatFileContents("testfiles/formatting/blankLines/Fields07.php", options);
    }

    public void testBLFields08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/Fields08.php", options);
    }

    public void testBLFields09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/Fields09.php", options);
    }

    public void testBLFields10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/Fields10.php", options);
    }

    public void testBLFields11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.blankLinesBetweenFields, 2);
        options.put(FmtOptions.blankLinesGroupFieldsWithoutDoc, false);
        reformatFileContents("testfiles/formatting/blankLines/Fields11.php", options);
    }

    public void testBLFunction01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function01.php", options);
    }

    public void testBLFunction02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function02.php", options);
    }

    public void testBLFunction04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Function04.php", options);
    }

    public void testBLNamespace01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace01.php", options);
    }

    public void testBLNamespace02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace02.php", options);
    }

    public void testBLNamespace03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Namespace03.php", options);
    }

    public void testBLSimpleClass01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass01.php", options);
    }

    public void testBLSimpleClass02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass02.php", options);
    }

    public void testBLSimpleClass03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass03.php", options);
    }

    public void testBLSimpleClass04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass04.php", options);
    }

    public void testBLSimpleClass05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 1);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass05.php", options);
    }

    public void testBLSimpleClass06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 1);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass06.php", options);
    }

    public void testBLSimpleClass07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 1);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass07.php", options);
    }

    public void testBLSimpleClass08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 1);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass08.php", options);
    }

    public void testBLSimpleClass09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass09.php", options);
    }

    public void testBLSimpleClass10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 1);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass10.php", options);
    }

    public void testBLSimpleClass11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass11.php", options);
    }

    public void testBLSimpleClass12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 1);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 1);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 1);
        options.put(FmtOptions.blankLinesAfterFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass12.php", options);
    }

    public void testBLSimpleClass13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass13.php", options);
    }

    public void testBLSimpleClass14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass14.php", options);
    }

    public void testBLSimpleClass15() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass15.php", options);
    }

    public void testBLSimpleClass16() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass16.php", options);
    }

    public void testBLSimpleClass17() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleClass17.php", options);
    }

    public void testBLSimpleTrait01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait01.php", options);
    }

    public void testBLSimpleTrait02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait02.php", options);
    }

    public void testBLSimpleTrait03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait03.php", options);
    }

    public void testBLSimpleTrait04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait04.php", options);
    }

    public void testBLSimpleTrait05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 1);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait05.php", options);
    }

    public void testBLSimpleTrait06() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 1);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait06.php", options);
    }

    public void testBLSimpleTrait07() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 1);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait07.php", options);
    }

    public void testBLSimpleTrait08() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 1);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait08.php", options);
    }

    public void testBLSimpleTrait09() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait09.php", options);
    }

    public void testBLSimpleTrait10() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 1);
        options.put(FmtOptions.blankLinesAfterFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait10.php", options);
    }

    public void testBLSimpleTrait11() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 0);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 0);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 0);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 0);
        options.put(FmtOptions.blankLinesAfterFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait11.php", options);
    }

    public void testBLSimpleTrait12() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.blankLinesBeforeClass, 0);
        options.put(FmtOptions.blankLinesAfterClassHeader, 1);
        options.put(FmtOptions.blankLinesBeforeClassEnd, 1);
        options.put(FmtOptions.blankLinesAfterClass, 0);
        options.put(FmtOptions.blankLinesBeforeFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFunctionEnd, 1);
        options.put(FmtOptions.blankLinesAfterFunction, 1);
        options.put(FmtOptions.blankLinesBeforeFields, 0);
        options.put(FmtOptions.blankLinesAfterFields, 0);
        options.put(FmtOptions.blankLinesBeforeNamespace, 0);
        options.put(FmtOptions.blankLinesAfterNamespace, 0);
        options.put(FmtOptions.blankLinesBeforeUse, 0);
        options.put(FmtOptions.blankLinesAfterUse, 0);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait12.php", options);
    }

    public void testBLSimpleTrait13() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait13.php", options);
    }

    public void testBLSimpleTrait14() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait14.php", options);
    }

    public void testBLSimpleTrait15() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait15.php", options);
    }

    public void testBLSimpleTrait16() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait16.php", options);
    }

    public void testBLSimpleTrait17() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        options.put(FmtOptions.classDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        options.put(FmtOptions.methodDeclBracePlacement, CodeStyle.BracePlacement.NEW_LINE_INDENTED);
        reformatFileContents("testfiles/formatting/blankLines/SimpleTrait17.php", options);
    }

    public void testBLSimpleUse01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>();
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use01.php", options);
    }

    public void testBLSimpleUse02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use02.php", options);
    }

    public void testBLSimpleUse03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use03.php", options);
    }

    public void testBLSimpleUse04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 0);
        reformatFileContents("testfiles/formatting/blankLines/Use04.php", options);
    }

    public void testOpenClosePHPTag01() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag01.php", options);
    }

    public void testOpenClosePHPTag02() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag02.php", options);
    }

    public void testOpenClosePHPTag03() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag03.php", options);
    }

    public void testOpenClosePHPTag04() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag04.php", options);
    }

    public void testOpenClosePHPTag05() throws Exception {
        HashMap<String, Object> options = new HashMap<String, Object>(FmtOptions.getDefaults());
        options.put(FmtOptions.initialIndent, 4);
        reformatFileContents("testfiles/formatting/blankLines/OpenClosePHPTag05.php", options);
    }
}
