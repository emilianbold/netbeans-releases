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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.javascript2.editor.formatter;

import java.util.prefs.Preferences;

import javax.swing.text.Document;
import org.netbeans.modules.editor.indent.spi.CodeStylePreferences;
import static org.netbeans.modules.javascript2.editor.formatter.FmtOptions.*;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;

/**
 *  XXX make sure the getters get the defaults from somewhere
 *  XXX add support for profiles
 *  XXX get the preferences node from somewhere else in odrer to be able not to
 *      use the getters and to be able to write to it.
 *
 * @author Dusan Balek
 * @author Petr Pisl
 */
public final class CodeStyle {

    static {
        FmtOptions.codeStyleProducer = new Producer();
    }

    private Preferences preferences;

    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }

    /** For testing purposes only */
    public static CodeStyle get(Preferences prefs) {
        return new CodeStyle(prefs);
    }

    public static CodeStyle get(Document doc) {
        return new CodeStyle(CodeStylePreferences.get(doc).getPreferences());
    }
    
    public static CodeStyle get(FormatContext context) {
        return get(context.getDocument(), context.isEmbedded());
    }

    public static CodeStyle get(IndentContext context) {
        return get(context.getDocument(), context.isEmbedded());
    }

    private static CodeStyle get(Document doc, boolean embedded) {
        // TODO should we have a separate setting for JSON
        //if (embedded) {
            return new CodeStyle(CodeStylePreferences.get(doc, JsTokenId.JAVASCRIPT_MIME_TYPE).getPreferences());
        //}
        //return new CodeStyle(CodeStylePreferences.get(doc).getPreferences());
    }

    // General tabs and indents ------------------------------------------------

    public boolean expandTabToSpaces () {
        return preferences.getBoolean(expandTabToSpaces,  getDefaultAsBoolean(expandTabToSpaces));
    }

    public int getTabSize() {
        return preferences.getInt(tabSize, getDefaultAsInt(tabSize));
    }

    public int getIndentSize() {
        return preferences.getInt(indentSize, getDefaultAsInt(indentSize));
    }

    public int getContinuationIndentSize() {
        return preferences.getInt(continuationIndentSize, getDefaultAsInt(continuationIndentSize));
    }

    public int getItemsInArrayDeclarationIndentSize() {
        return preferences.getInt(itemsInArrayDeclarationIndentSize, getDefaultAsInt(itemsInArrayDeclarationIndentSize));
    }

    public int getInitialIndent(){
        return preferences.getInt(initialIndent, getDefaultAsInt(initialIndent));
    }

    public boolean reformatComments() {
        return preferences.getBoolean(reformatComments, getDefaultAsBoolean(reformatComments));
    }

    public boolean indentHtml() {
        return preferences.getBoolean(indentHtml, getDefaultAsBoolean(indentHtml));
    }

    public int getRightMargin() {
        return preferences.getInt(rightMargin, getDefaultAsInt(rightMargin));
    }

    // Brace placement --------------------------------------------------------

    public BracePlacement getClassDeclBracePlacement() {
        String placement = preferences.get(classDeclBracePlacement, getDefaultAsString(classDeclBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getMethodDeclBracePlacement() {
        String placement = preferences.get(methodDeclBracePlacement, getDefaultAsString(methodDeclBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getIfBracePlacement() {
        String placement = preferences.get(ifBracePlacement, getDefaultAsString(ifBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getForBracePlacement() {
        String placement = preferences.get(forBracePlacement, getDefaultAsString(forBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getWhileBracePlacement() {
        String placement = preferences.get(whileBracePlacement, getDefaultAsString(whileBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getSwitchBracePlacement() {
        String placement = preferences.get(switchBracePlacement, getDefaultAsString(switchBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getCatchBracePlacement() {
        String placement = preferences.get(catchBracePlacement, getDefaultAsString(catchBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getUseTraitBodyBracePlacement() {
        String placement = preferences.get(useTraitBodyBracePlacement, getDefaultAsString(useTraitBodyBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getOtherBracePlacement() {
        String placement = preferences.get(otherBracePlacement, getDefaultAsString(otherBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    // Blank lines -------------------------------------------------------------

    public int getBlankLinesBeforeNamespace() {
        return preferences.getInt(blankLinesBeforeNamespace, getDefaultAsInt(blankLinesBeforeNamespace));
    }

    public int getBlankLinesAfterNamespace() {
        return preferences.getInt(blankLinesAfterNamespace, getDefaultAsInt(blankLinesAfterNamespace));
    }

    public int getBlankLinesBeforeUse() {
        return preferences.getInt(blankLinesBeforeUse, getDefaultAsInt(blankLinesBeforeUse));
    }

    public int getBlankLinesBeforeUseTrait() {
        return preferences.getInt(blankLinesBeforeUseTrait, getDefaultAsInt(blankLinesBeforeUseTrait));
    }

    public int getBlankLinesAfterUse() {
        return preferences.getInt(blankLinesAfterUse, getDefaultAsInt(blankLinesAfterUse));
    }

    public int getBlankLinesBeforeClass() {
        return preferences.getInt(blankLinesBeforeClass, getDefaultAsInt(blankLinesBeforeClass));
    }

    public int getBlankLinesAfterClass() {
        return preferences.getInt(blankLinesAfterClass, getDefaultAsInt(blankLinesAfterClass));
    }

    public int getBlankLinesAfterClassHeader() {
        return preferences.getInt(blankLinesAfterClassHeader, getDefaultAsInt(blankLinesAfterClassHeader));
    }

    public int getBlankLinesBeforeClassEnd() {
        return preferences.getInt(blankLinesBeforeClassEnd, getDefaultAsInt(blankLinesBeforeClassEnd));
    }

    public int getBlankLinesBeforeFields() {
        return preferences.getInt(blankLinesBeforeFields, getDefaultAsInt(blankLinesBeforeFields));
    }

    public int getBlankLinesBetweenFields() {
        return preferences.getInt(blankLinesBetweenFields, getDefaultAsInt(blankLinesBetweenFields));
    }

    public int getBlankLinesAfterFields() {
        return preferences.getInt(blankLinesAfterFields, getDefaultAsInt(blankLinesAfterFields));
    }

    /**
     *
     * @return true it the fields will be group without php doc together (no empty line between them)
     */
    public boolean getBlankLinesGroupFieldsWithoutDoc() {
	return preferences.getBoolean(blankLinesGroupFieldsWithoutDoc, getDefaultAsBoolean(blankLinesGroupFieldsWithoutDoc));
    }

    public int getBlankLinesBeforeFunction() {
        return preferences.getInt(blankLinesBeforeFunction, getDefaultAsInt(blankLinesBeforeFunction));
    }

    public int getBlankLinesAfterFunction() {
        return preferences.getInt(blankLinesAfterFunction, getDefaultAsInt(blankLinesAfterFunction));
    }

    public int getBlankLinesBeforeFunctionEnd() {
        return preferences.getInt(blankLinesBeforeFunctionEnd, getDefaultAsInt(blankLinesBeforeFunctionEnd));
    }

    public int getBlankLinesAfterOpenPHPTag() {
        return preferences.getInt(blankLinesAfterOpenPHPTag, getDefaultAsInt(blankLinesAfterOpenPHPTag));
    }

    public int getBlankLinesAfterOpenPHPTagInHTML() {
        return preferences.getInt(blankLinesAfterOpenPHPTagInHTML, getDefaultAsInt(blankLinesAfterOpenPHPTagInHTML));
    }

    public int getBlankLinesBeforeClosePHPTag() {
        return preferences.getInt(blankLinesBeforeClosePHPTag, getDefaultAsInt(blankLinesBeforeClosePHPTag));
    }

    // Spaces ------------------------------------------------------------------

    public boolean spaceBeforeWhile() {
        return preferences.getBoolean(spaceBeforeWhile, getDefaultAsBoolean(spaceBeforeWhile));
    }

    public boolean spaceBeforeElse() {
        return preferences.getBoolean(spaceBeforeElse, getDefaultAsBoolean(spaceBeforeElse));
    }

    public boolean spaceBeforeCatch() {
        return preferences.getBoolean(spaceBeforeCatch, getDefaultAsBoolean(spaceBeforeCatch));
    }

    public boolean spaceBeforeFinally() {
        return preferences.getBoolean(spaceBeforeFinally, getDefaultAsBoolean(spaceBeforeFinally));
    }

    public boolean spaceBeforeMethodDeclParen() {
        return preferences.getBoolean(spaceBeforeMethodDeclParen, getDefaultAsBoolean(spaceBeforeMethodDeclParen));
    }

    public boolean spaceBeforeMethodCallParen() {
        return preferences.getBoolean(spaceBeforeMethodCallParen, getDefaultAsBoolean(spaceBeforeMethodCallParen));
    }

    public boolean spaceBeforeIfParen() {
        return preferences.getBoolean(spaceBeforeIfParen, getDefaultAsBoolean(spaceBeforeIfParen));
    }

    public boolean spaceBeforeForParen() {
        return preferences.getBoolean(spaceBeforeForParen, getDefaultAsBoolean(spaceBeforeForParen));
    }

    public boolean spaceBeforeWhileParen() {
        return preferences.getBoolean(spaceBeforeWhileParen, getDefaultAsBoolean(spaceBeforeWhileParen));
    }

    public boolean spaceBeforeCatchParen() {
        return preferences.getBoolean(spaceBeforeCatchParen, getDefaultAsBoolean(spaceBeforeCatchParen));
    }

    public boolean spaceBeforeSwitchParen() {
        return preferences.getBoolean(spaceBeforeSwitchParen, getDefaultAsBoolean(spaceBeforeSwitchParen));
    }

    public boolean spaceBeforeWithParen() {
        return preferences.getBoolean(spaceBeforeWithParen, getDefaultAsBoolean(spaceBeforeWithParen));
    }

    public boolean spaceAroundUnaryOps() {
        return preferences.getBoolean(spaceAroundUnaryOps, getDefaultAsBoolean(spaceAroundUnaryOps));
    }

    public boolean spaceAroundBinaryOps() {
        return preferences.getBoolean(spaceAroundBinaryOps, getDefaultAsBoolean(spaceAroundBinaryOps));
    }

    public boolean spaceAroundStringConcatOps() {
        return preferences.getBoolean(spaceAroundStringConcatOps, getDefaultAsBoolean(spaceAroundStringConcatOps));
    }

    public boolean spaceAroundTernaryOps() {
        return preferences.getBoolean(spaceAroundTernaryOps, getDefaultAsBoolean(spaceAroundTernaryOps));
    }

    public boolean spaceAroundKeyValueOps() {
        return preferences.getBoolean(spaceAroundKeyValueOps, getDefaultAsBoolean(spaceAroundKeyValueOps));
    }

    public boolean spaceAroundAssignOps() {
        return preferences.getBoolean(spaceAroundAssignOps, getDefaultAsBoolean(spaceAroundAssignOps));
    }

    public boolean spaceAroundObjectOps() {
        return preferences.getBoolean(spaceAroundObjectOps, getDefaultAsBoolean(spaceAroundObjectOps));
    }

    public boolean spaceBeforeClassDeclLeftBrace() {
        return preferences.getBoolean(spaceBeforeClassDeclLeftBrace, getDefaultAsBoolean(spaceBeforeClassDeclLeftBrace));
    }

    public boolean spaceBeforeMethodDeclLeftBrace() {
        return preferences.getBoolean(spaceBeforeMethodDeclLeftBrace, getDefaultAsBoolean(spaceBeforeMethodDeclLeftBrace));
    }

    public boolean spaceBeforeIfLeftBrace() {
        return preferences.getBoolean(spaceBeforeIfLeftBrace, getDefaultAsBoolean(spaceBeforeIfLeftBrace));
    }

    public boolean spaceBeforeElseLeftBrace() {
        return preferences.getBoolean(spaceBeforeElseLeftBrace, getDefaultAsBoolean(spaceBeforeElseLeftBrace));
    }

    public boolean spaceBeforeWhileLeftBrace() {
        return preferences.getBoolean(spaceBeforeWhileLeftBrace, getDefaultAsBoolean(spaceBeforeWhileLeftBrace));
    }

    public boolean spaceBeforeForLeftBrace() {
        return preferences.getBoolean(spaceBeforeForLeftBrace, getDefaultAsBoolean(spaceBeforeForLeftBrace));
    }

    public boolean spaceBeforeDoLeftBrace() {
        return preferences.getBoolean(spaceBeforeDoLeftBrace, getDefaultAsBoolean(spaceBeforeDoLeftBrace));
    }

    public boolean spaceBeforeSwitchLeftBrace() {
        return preferences.getBoolean(spaceBeforeSwitchLeftBrace, getDefaultAsBoolean(spaceBeforeSwitchLeftBrace));
    }

    public boolean spaceBeforeTryLeftBrace() {
        return preferences.getBoolean(spaceBeforeTryLeftBrace, getDefaultAsBoolean(spaceBeforeTryLeftBrace));
    }

    public boolean spaceBeforeCatchLeftBrace() {
        return preferences.getBoolean(spaceBeforeCatchLeftBrace, getDefaultAsBoolean(spaceBeforeCatchLeftBrace));
    }

    public boolean spaceBeforeFinallyLeftBrace() {
        return preferences.getBoolean(spaceBeforeFinallyLeftBrace, getDefaultAsBoolean(spaceBeforeFinallyLeftBrace));
    }

    public boolean spaceBeforeWithLeftBrace() {
        return preferences.getBoolean(spaceBeforeWithLeftBrace, getDefaultAsBoolean(spaceBeforeWithLeftBrace));
    }
//
//    public boolean spaceBeforeSynchronizedLeftBrace() {
//        return preferences.getBoolean(spaceBeforeSynchronizedLeftBrace, getDefaultAsBoolean(spaceBeforeSynchronizedLeftBrace));
//    }
//
//    public boolean spaceBeforeStaticInitLeftBrace() {
//        return preferences.getBoolean(spaceBeforeStaticInitLeftBrace, getDefaultAsBoolean(spaceBeforeStaticInitLeftBrace));
//    }
//
//    public boolean spaceBeforeArrayInitLeftBrace() {
//        return preferences.getBoolean(spaceBeforeArrayInitLeftBrace, getDefaultAsBoolean(spaceBeforeArrayInitLeftBrace));
//    }
//
    public boolean spaceWithinParens() {
        return preferences.getBoolean(spaceWithinParens, getDefaultAsBoolean(spaceWithinParens));
    }

    public boolean spaceWithinMethodDeclParens() {
        return preferences.getBoolean(spaceWithinMethodDeclParens, getDefaultAsBoolean(spaceWithinMethodDeclParens));
    }

    public boolean spaceWithinMethodCallParens() {
        return preferences.getBoolean(spaceWithinMethodCallParens, getDefaultAsBoolean(spaceWithinMethodCallParens));
    }

    public boolean spaceWithinIfParens() {
        return preferences.getBoolean(spaceWithinIfParens, getDefaultAsBoolean(spaceWithinIfParens));
    }

    public boolean spaceWithinForParens() {
        return preferences.getBoolean(spaceWithinForParens, getDefaultAsBoolean(spaceWithinForParens));
    }

    public boolean spaceWithinWhileParens() {
        return preferences.getBoolean(spaceWithinWhileParens, getDefaultAsBoolean(spaceWithinWhileParens));
    }

    public boolean spaceWithinSwitchParens() {
        return preferences.getBoolean(spaceWithinSwitchParens, getDefaultAsBoolean(spaceWithinSwitchParens));
    }

    public boolean spaceWithinCatchParens() {
        return preferences.getBoolean(spaceWithinCatchParens, getDefaultAsBoolean(spaceWithinCatchParens));
    }

    public boolean spaceWithinWithParens() {
        return preferences.getBoolean(spaceWithinWithParens, getDefaultAsBoolean(spaceWithinWithParens));
    }
//
//    public boolean spaceWithinSynchronizedParens() {
//        return preferences.getBoolean(spaceWithinSynchronizedParens, getDefaultAsBoolean(spaceWithinSynchronizedParens));
//    }

    public boolean spaceWithinTypeCastParens() {
        return preferences.getBoolean(spaceWithinTypeCastParens, getDefaultAsBoolean(spaceWithinTypeCastParens));
    }

    public boolean spaceWithinArrayDeclParens() {
        return preferences.getBoolean(spaceWithinArrayDeclParens, getDefaultAsBoolean(spaceWithinArrayDeclParens));
    }

//    public boolean spaceWithinAnnotationParens() {
//        return preferences.getBoolean(spaceWithinAnnotationParens, getDefaultAsBoolean(spaceWithinAnnotationParens));
//    }
//
    public boolean spaceWithinBraces() {
        return preferences.getBoolean(spaceWithinBraces, getDefaultAsBoolean(spaceWithinBraces));
    }

    public boolean spaceWithinArrayBrackets() {
        return preferences.getBoolean(spaceWithinArrayBrackets, getDefaultAsBoolean(spaceWithinArrayBrackets));
    }

    public boolean spaceBeforeComma() {
        return preferences.getBoolean(spaceBeforeComma, getDefaultAsBoolean(spaceBeforeComma));
    }

    public boolean spaceAfterComma() {
        return preferences.getBoolean(spaceAfterComma, getDefaultAsBoolean(spaceAfterComma));
    }

    public boolean spaceBeforeSemi() {
        return preferences.getBoolean(spaceBeforeSemi, getDefaultAsBoolean(spaceBeforeSemi));
    }

    public boolean spaceAfterSemi() {
        return preferences.getBoolean(spaceAfterSemi, getDefaultAsBoolean(spaceAfterSemi));
    }

    public boolean spaceBeforeColon() {
        return preferences.getBoolean(spaceBeforeColon, getDefaultAsBoolean(spaceBeforeColon));
    }

    public boolean spaceAfterColon() {
        return preferences.getBoolean(spaceAfterColon, getDefaultAsBoolean(spaceAfterColon));
    }

    // alignment
    public boolean alignMultilineMethodParams() {
        return preferences.getBoolean(alignMultilineMethodParams, getDefaultAsBoolean(alignMultilineMethodParams));
    }

    public boolean alignMultilineCallArgs() {
        return preferences.getBoolean(alignMultilineCallArgs, getDefaultAsBoolean(alignMultilineCallArgs));
    }

    public boolean alignMultilineImplements() {
        return preferences.getBoolean(alignMultilineImplements, getDefaultAsBoolean(alignMultilineImplements));
    }

    public boolean alignMultilineParenthesized() {
        return preferences.getBoolean(alignMultilineParenthesized, getDefaultAsBoolean(alignMultilineParenthesized));
    }

    public boolean alignMultilineBinaryOp() {
        return preferences.getBoolean(alignMultilineBinaryOp, getDefaultAsBoolean(alignMultilineBinaryOp));
    }

    public boolean alignMultilineTernaryOp() {
        return preferences.getBoolean(alignMultilineTernaryOp, getDefaultAsBoolean(alignMultilineTernaryOp));
    }

    public boolean alignMultilineAssignment() {
        return preferences.getBoolean(alignMultilineAssignment, getDefaultAsBoolean(alignMultilineAssignment));
    }

    public boolean alignMultilineFor() {
        return preferences.getBoolean(alignMultilineFor, getDefaultAsBoolean(alignMultilineFor));
    }

    public boolean alignMultilineArrayInit() {
        return preferences.getBoolean(alignMultilineArrayInit, getDefaultAsBoolean(alignMultilineArrayInit));
    }

    public boolean placeElseOnNewLine() {
        return preferences.getBoolean(placeElseOnNewLine, getDefaultAsBoolean(placeElseOnNewLine));
    }

    public boolean placeWhileOnNewLine() {
        return preferences.getBoolean(placeWhileOnNewLine, getDefaultAsBoolean(placeWhileOnNewLine));
    }

    public boolean placeCatchOnNewLine() {
        return preferences.getBoolean(placeCatchOnNewLine, getDefaultAsBoolean(placeCatchOnNewLine));
    }

    public boolean placeNewLineAfterModifiers() {
        return preferences.getBoolean(placeNewLineAfterModifiers, getDefaultAsBoolean(placeNewLineAfterModifiers));
    }

    public boolean groupMulitlineAssignment() {
        return preferences.getBoolean(groupAlignmentAssignment, getDefaultAsBoolean(groupAlignmentAssignment));
    }

    public boolean groupMulitlineArrayInit() {
        return preferences.getBoolean(groupAlignmentArrayInit, getDefaultAsBoolean(groupAlignmentArrayInit));
    }

    // Wrapping ----------------------------------------------------------------

    public WrapStyle wrapStatement() {
        String wrap = preferences.get(wrapStatement, getDefaultAsString(wrapStatement));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapVariables() {
        String wrap = preferences.get(wrapVariables, getDefaultAsString(wrapVariables));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapMethodParams() {
        String wrap = preferences.get(wrapMethodParams, getDefaultAsString(wrapMethodParams));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapMethodCallArgs() {
        String wrap = preferences.get(wrapMethodCallArgs, getDefaultAsString(wrapMethodCallArgs));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapChainedMethodCalls() {
        String wrap = preferences.get(wrapChainedMethodCalls, getDefaultAsString(wrapChainedMethodCalls));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapAfterDotInChainedMethodCalls() {
        return preferences.getBoolean(wrapAfterDotInChainedMethodCalls, getDefaultAsBoolean(wrapAfterDotInChainedMethodCalls));
    }

    public WrapStyle wrapArrayInit() {
        String wrap = preferences.get(wrapArrayInit, getDefaultAsString(wrapArrayInit));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapFor() {
        String wrap = preferences.get(wrapFor, getDefaultAsString(wrapFor));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapForStatement() {
        String wrap = preferences.get(wrapForStatement, getDefaultAsString(wrapForStatement));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapIfStatement() {
        String wrap = preferences.get(wrapIfStatement, getDefaultAsString(wrapIfStatement));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapWhileStatement() {
        String wrap = preferences.get(wrapWhileStatement, getDefaultAsString(wrapWhileStatement));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapDoWhileStatement() {
        String wrap = preferences.get(wrapDoWhileStatement, getDefaultAsString(wrapDoWhileStatement));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapWithStatement() {
        String wrap = preferences.get(wrapWithStatement, getDefaultAsString(wrapWithStatement));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapBinaryOps() {
        String wrap = preferences.get(wrapBinaryOps, getDefaultAsString(wrapBinaryOps));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapAfterBinaryOps() {
        return preferences.getBoolean(wrapAfterBinaryOps, getDefaultAsBoolean(wrapAfterBinaryOps));
    }
    
    public WrapStyle wrapTernaryOps() {
        String wrap = preferences.get(wrapTernaryOps, getDefaultAsString(wrapTernaryOps));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAssignOps() {
        String wrap = preferences.get(wrapAssignOps, getDefaultAsString(wrapAssignOps));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapAfterTernaryOps() {
        return preferences.getBoolean(wrapAfterTernaryOps, getDefaultAsBoolean(wrapAfterTernaryOps));
    }

    public boolean wrapBlockBrace() {
        return preferences.getBoolean(wrapBlockBraces, getDefaultAsBoolean(wrapBlockBraces));
    }

    public boolean wrapStatementsOnTheSameLine() {
        return preferences.getBoolean(wrapStatementsOnTheLine, getDefaultAsBoolean(wrapStatementsOnTheLine));
    }

    public WrapStyle wrapObjects() {
        String wrap = preferences.get(wrapObjects, getDefaultAsString(wrapObjects));
        return WrapStyle.valueOf(wrap);
    }

    public boolean wrapProperties() {
        return preferences.getBoolean(wrapProperties, getDefaultAsBoolean(wrapProperties));
    }

    // Uses

    public boolean preferFullyQualifiedNames() {
        return preferences.getBoolean(preferFullyQualifiedNames, getDefaultAsBoolean(preferFullyQualifiedNames));
    }

    public boolean preferMultipleUseStatementsCombined() {
        return preferences.getBoolean(preferMultipleUseStatementsCombined, getDefaultAsBoolean(preferMultipleUseStatementsCombined));
    }

    public boolean startUseWithNamespaceSeparator() {
        return preferences.getBoolean(startUseWithNamespaceSeparator, getDefaultAsBoolean(startUseWithNamespaceSeparator));
    }

    private static class Producer implements FmtOptions.CodeStyleProducer {

        @Override
        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
    }

    public enum BracePlacement {
        SAME_LINE,
        NEW_LINE,
	NEW_LINE_INDENTED,
        PRESERVE_EXISTING
    }

    public enum WrapStyle {
        WRAP_ALWAYS,
        WRAP_IF_LONG,
        WRAP_NEVER
    }
}
