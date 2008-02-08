/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.cnd.editor.options;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import org.openide.util.NbPreferences;

/**
 *
 * @author Alexander Simon
 */
public class EditorOptions {
    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final boolean spaceBeforeWhileDefault = true;
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final boolean spaceBeforeElseDefault = true;
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final boolean spaceBeforeCatchDefault = true;

    public static final String spaceBeforeMethodDeclParen = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final boolean spaceBeforeMethodDeclParenDefault = false;
    public static final String spaceBeforeMethodCallParen = "spaceBeforeMethodCallParen"; //NOI18N
    public static final boolean spaceBeforeMethodCallParenDefault = false;
    public static final String spaceBeforeIfParen = "spaceBeforeIfParen"; //NOI18N
    public static final boolean spaceBeforeIfParenDefault = true;
    public static final String spaceBeforeForParen = "spaceBeforeForParen"; //NOI18N
    public static final boolean spaceBeforeForParenDefault = true;
    public static final String spaceBeforeWhileParen = "spaceBeforeWhileParen"; //NOI18N
    public static final boolean spaceBeforeWhileParenDefault = true;
    public static final String spaceBeforeCatchParen = "spaceBeforeCatchParen"; //NOI18N
    public static final boolean spaceBeforeCatchParenDefault = true;
    public static final String spaceBeforeSwitchParen = "spaceBeforeSwitchParen"; //NOI18N
    public static final boolean spaceBeforeSwitchParenDefault = true;
    
    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final boolean spaceAroundUnaryOpsDefault = false;
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final boolean spaceAroundBinaryOpsDefault = true;
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final boolean spaceAroundTernaryOpsDefault = true;
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final boolean spaceAroundAssignOpsDefault = true;
    
    public static final String spaceBeforeClassDeclLeftBrace = "spaceBeforeClassDeclLeftBrace"; //NOI18N
    public static final boolean spaceBeforeClassDeclLeftBraceDefault = true;
    public static final String spaceBeforeMethodDeclLeftBrace = "spaceBeforeMethodDeclLeftBrace"; //NOI18N
    public static final boolean spaceBeforeMethodDeclLeftBraceDefault = true;
    public static final String spaceBeforeIfLeftBrace = "spaceBeforeIfLeftBrace"; //NOI18N
    public static final boolean spaceBeforeIfLeftBraceDefault = true;
    public static final String spaceBeforeElseLeftBrace = "spaceBeforeElseLeftBrace"; //NOI18N
    public static final boolean spaceBeforeElseLeftBraceDefault = true;
    public static final String spaceBeforeWhileLeftBrace = "spaceBeforeWhileLeftBrace"; //NOI18N
    public static final boolean spaceBeforeWhileLeftBraceDefault = true;
    public static final String spaceBeforeForLeftBrace = "spaceBeforeForLeftBrace"; //NOI18N
    public static final boolean spaceBeforeForLeftBraceDefault = true;
    public static final String spaceBeforeDoLeftBrace = "spaceBeforeDoLeftBrace"; //NOI18N
    public static final boolean spaceBeforeDoLeftBraceDefault = true;
    public static final String spaceBeforeSwitchLeftBrace = "spaceBeforeSwitchLeftBrace"; //NOI18N
    public static final boolean spaceBeforeSwitchLeftBraceDefault = true;
    public static final String spaceBeforeTryLeftBrace = "spaceBeforeTryLeftBrace"; //NOI18N
    public static final boolean spaceBeforeTryLeftBraceDefault = true;
    public static final String spaceBeforeCatchLeftBrace = "spaceBeforeCatchLeftBrace"; //NOI18N
    public static final boolean spaceBeforeCatchLeftBraceDefault = true;
    public static final String spaceBeforeArrayInitLeftBrace = "spaceBeforeArrayInitLeftBrace"; //NOI18N
    public static final boolean spaceBeforeArrayInitLeftBraceDefault = false;
    
    public static final String spaceWithinParens = "spaceWithinParens"; //NOI18N
    public static final boolean spaceWithinParensDefault = false;
    public static final String spaceWithinMethodDeclParens = "spaceWithinMethodDeclParens"; //NOI18N
    public static final boolean spaceWithinMethodDeclParensDefault = false;
    public static final String spaceWithinMethodCallParens = "spaceWithinMethodCallParens"; //NOI18N
    public static final boolean spaceWithinMethodCallParensDefault = false;
    public static final String spaceWithinIfParens = "spaceWithinIfParens"; //NOI18N
    public static final boolean spaceWithinIfParensDefault = false;
    public static final String spaceWithinForParens = "spaceWithinForParens"; //NOI18N
    public static final boolean spaceWithinForParensDefault = false;
    public static final String spaceWithinWhileParens = "spaceWithinWhileParens"; //NOI18N
    public static final boolean spaceWithinWhileParensDefault = false;
    public static final String spaceWithinSwitchParens = "spaceWithinSwitchParens"; //NOI18N
    public static final boolean spaceWithinSwitchParensDefault = false;
    public static final String spaceWithinCatchParens = "spaceWithinCatchParens"; //NOI18N
    public static final boolean spaceWithinCatchParensDefault = false;
    public static final String spaceWithinTypeCastParens = "spaceWithinTypeCastParens"; //NOI18N
    public static final boolean spaceWithinTypeCastParensDefault = false;
    public static final String spaceWithinBraces = "spaceWithinBraces"; //NOI18N
    public static final boolean spaceWithinBracesDefault = false;
    public static final String spaceWithinArrayInitBrackets = "spaceWithinArrayInitBrackets"; //NOI18N
    public static final boolean spaceWithinArrayInitBracketsDefault = false;
    
    public static final String spaceBeforeComma = "spaceBeforeComma"; //NOI18N
    public static final boolean spaceBeforeCommaDefault = false;
    public static final String spaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final boolean spaceAfterCommaDefault = true;
    public static final String spaceBeforeSemi = "spaceBeforeSemi"; //NOI18N
    public static final boolean spaceBeforeSemiDefault = false;
    public static final String spaceAfterSemi = "spaceAfterSemi"; //NOI18N
    public static final boolean spaceAfterSemiDefault = true;
    public static final String spaceBeforeColon = "spaceBeforeColon"; //NOI18N
    public static final boolean spaceBeforeColonDefault = true;
    public static final String spaceAfterColon = "spaceAfterColon"; //NOI18N
    public static final boolean spaceAfterColonDefault = true;
    public static final String spaceAfterTypeCast = "spaceAfterTypeCast"; //NOI18N
    public static final boolean spaceAfterTypeCastDefault = true;
    
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N
    public static final boolean alignMultilineArrayInitDefault = false;
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final boolean alignMultilineCallArgsDefault = false;
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final boolean alignMultilineMethodParamsDefault = false;

    public static final String indentCasesFromSwitch = "indentCasesFromSwitch"; //NOI18N
    public static final boolean indentCasesFromSwitchDefault = false;
    
    public static final String newLineCatch = "newLineCatch"; //NOI18N
    public static final boolean newLineCatchDefault = false;
    public static final String newLineElse = "newLineElse"; //NOI18N
    public static final boolean newLineElseDefault = false;
    public static final String newLineWhile = "newLineWhile"; //NOI18N
    public static final boolean newLineWhileDefault = false;

    public static final String blankLinesBeforeClass = "blankLinesBeforeClass"; //NOI18N
    public static final int blankLinesBeforeClassDefault = 1;    
    public static final String blankLinesAfterClass = "blankLinesAfterClass"; //NOI18N
    public static final int blankLinesAfterClassDefault = 0;    
    public static final String blankLinesAfterClassHeader = "blankLinesAfterClassHeader"; //NOI18N
    public static final int blankLinesAfterClassHeaderDefault = 0;    
    public static final String blankLinesBeforeFields = "blankLinesBeforeFields"; //NOI18N
    public static final int blankLinesBeforeFieldsDefault = 0;    
    public static final String blankLinesAfterFields = "blankLinesAfterFields"; //NOI18N
    public static final int blankLinesAfterFieldsDefault = 0;    
    public static final String blankLinesBeforeMethods = "blankLinesBeforeMethods"; //NOI18N
    public static final int blankLinesBeforeMethodsDefault = 1;    
    public static final String blankLinesAfterMethods = "blankLinesAfterMethods"; //NOI18N
    public static final int blankLinesAfterMethodsDefault = 0;    

    /**
     * Whether insert extra new-line before the compound bracket or not.
     * Values: java.lang.Boolean instances
     * Effect: if (test) {
     *           function();
     *         }
     *           becomes (when set to true)
     *         if (test)
     *         {
     *           function();
     *         }
     */
    public static final String CC_FORMAT_NEWLINE_BEFORE_BRACE = "cc-add-newline-before-brace"; //NOI18N
    public static final String defaultCCFormatNewlineBeforeBrace = BracePlacement.SAME_LINE.name();
    
    /**
     * Whether insert extra new-line before the declaration or not.
     * Values: java.lang.Boolean instances
     * Effect: int foo() {
     *           function();
     *         }
     *           becomes (when set to true)
     *         int foo(test)
     *         {
     *           function();
     *         }
     */
    public static final String CC_FORMAT_NEWLINE_BEFORE_BRACE_DECLARATION = "cc-add-newline-before-brace-declaratin"; //NOI18N
    public static final String defaultCCFormatNewlineBeforeBraceDeclaration = BracePlacement.NEW_LINE.name();

    public static final String CC_FORMAT_NEWLINE_BEFORE_BRACE_CLASS = "cc-add-newline-before-brace-class"; //NOI18N
    public static final String defaultCCFormatNewlineBeforeBraceClass = BracePlacement.NEW_LINE.name();

    public static final String CC_FORMAT_NEWLINE_BEFORE_BRACE_METHOD = "cc-add-newline-before-brace-method"; //NOI18N
    public static final String defaultCCFormatNewlineBeforeBraceMethod = BracePlacement.NEW_LINE.name();

    /**
     * Whether to indent preprocessors positioned at start of line.
     * Those not starting at column 0 of the line will automatically be indented.
     * This setting is to prevent C/C++ code that is compiled with compilers that
     * require the processors to have '#' in column 0.
     * <B>Note:</B>This will not convert formatted preprocessors back to column 0.
     */
    public static final String indentPreprocessorDirectives = "indentPreprocessorDirectives"; //NOI18N
    public static final boolean indentPreprocessorDirectivesDefault = false;

    /** Whether the '*' should be added at the new line * in comment */
    public static final String CC_FORMAT_LEADING_STAR_IN_COMMENT = "cc-format-leading-star-in-comment"; // NOI18N
    public static final Boolean defaultCCFormatLeadingStarInComment = true;
    
    /**
     * How many spaces should be added to the statement that continues
     * on the next line.
     */
    public static final String CC_FORMAT_STATEMENT_CONTINUATION_INDENT = "cc-format-statement-continuation-indent"; // NOI18N 
    public static final int defaultCCFormatStatementContinuationIndent = 8;
    
    private static final Preferences preferences = NbPreferences.forModule(EditorOptions.class);

    private static final String C_DEFAULT_PROFILE = "c_default"; // NOI18N
    private static final String CPP_DEFAULT_PROFILE = "cpp_default"; // NOI18N

    public static CodeStyleProducer codeStyleProducer;

    private static Map<String,Object> defaults;
    
    static Preferences lastValues;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        defaults = new HashMap<String,Object>();
        defaults.put(CC_FORMAT_NEWLINE_BEFORE_BRACE,defaultCCFormatNewlineBeforeBrace);
        defaults.put(CC_FORMAT_NEWLINE_BEFORE_BRACE_DECLARATION,defaultCCFormatNewlineBeforeBraceDeclaration);
        defaults.put(CC_FORMAT_NEWLINE_BEFORE_BRACE_CLASS,defaultCCFormatNewlineBeforeBraceClass);
        defaults.put(CC_FORMAT_NEWLINE_BEFORE_BRACE_METHOD,defaultCCFormatNewlineBeforeBraceMethod);
        defaults.put(indentPreprocessorDirectives,indentPreprocessorDirectivesDefault);
        defaults.put(CC_FORMAT_LEADING_STAR_IN_COMMENT,defaultCCFormatLeadingStarInComment);
        defaults.put(CC_FORMAT_STATEMENT_CONTINUATION_INDENT,defaultCCFormatStatementContinuationIndent);

        defaults.put(indentCasesFromSwitch, indentCasesFromSwitchDefault);
    
        defaults.put(newLineCatch,newLineCatchDefault);
        defaults.put(newLineElse,newLineElseDefault);
        defaults.put(newLineWhile,newLineWhileDefault);

        defaults.put(spaceBeforeWhile,spaceBeforeWhileDefault);
        defaults.put(spaceBeforeElse,spaceBeforeElseDefault);
        defaults.put(spaceBeforeCatch,spaceBeforeCatchDefault);

        defaults.put(spaceBeforeMethodDeclParen,spaceBeforeMethodDeclParenDefault);
        defaults.put(spaceBeforeMethodCallParen,spaceBeforeMethodCallParenDefault);
        defaults.put(spaceBeforeIfParen,spaceBeforeIfParenDefault);
        defaults.put(spaceBeforeForParen,spaceBeforeForParenDefault);
        defaults.put(spaceBeforeWhileParen,spaceBeforeWhileParenDefault);
        defaults.put(spaceBeforeCatchParen,spaceBeforeCatchParenDefault);
        defaults.put(spaceBeforeSwitchParen,spaceBeforeSwitchParenDefault);

        defaults.put(spaceAroundUnaryOps,spaceAroundUnaryOpsDefault);
        defaults.put(spaceAroundBinaryOps,spaceAroundBinaryOpsDefault);
        defaults.put(spaceAroundTernaryOps,spaceAroundTernaryOpsDefault);
        defaults.put(spaceAroundAssignOps,spaceAroundAssignOpsDefault);

        defaults.put(spaceBeforeClassDeclLeftBrace,spaceBeforeClassDeclLeftBraceDefault);
        defaults.put(spaceBeforeMethodDeclLeftBrace,spaceBeforeMethodDeclLeftBraceDefault);
        defaults.put(spaceBeforeIfLeftBrace,spaceBeforeIfLeftBraceDefault);
        defaults.put(spaceBeforeElseLeftBrace,spaceBeforeElseLeftBraceDefault);
        defaults.put(spaceBeforeWhileLeftBrace,spaceBeforeWhileLeftBraceDefault);
        defaults.put(spaceBeforeForLeftBrace,spaceBeforeForLeftBraceDefault);
        defaults.put(spaceBeforeDoLeftBrace,spaceBeforeDoLeftBraceDefault);
        defaults.put(spaceBeforeSwitchLeftBrace,spaceBeforeSwitchLeftBraceDefault);
        defaults.put(spaceBeforeTryLeftBrace,spaceBeforeTryLeftBraceDefault);
        defaults.put(spaceBeforeCatchLeftBrace,spaceBeforeCatchLeftBraceDefault);
        defaults.put(spaceBeforeArrayInitLeftBrace,spaceBeforeArrayInitLeftBraceDefault);

        defaults.put(spaceWithinParens,spaceWithinParensDefault);
        defaults.put(spaceWithinMethodDeclParens,spaceWithinMethodDeclParensDefault);
        defaults.put(spaceWithinMethodCallParens,spaceWithinMethodCallParensDefault);
        defaults.put(spaceWithinIfParens,spaceWithinIfParensDefault);
        defaults.put(spaceWithinForParens,spaceWithinForParensDefault);
        defaults.put(spaceWithinWhileParens,spaceWithinWhileParensDefault);
        defaults.put(spaceWithinSwitchParens,spaceWithinSwitchParensDefault);
        defaults.put(spaceWithinCatchParens,spaceWithinCatchParensDefault);
        defaults.put(spaceWithinTypeCastParens,spaceWithinTypeCastParensDefault);
        defaults.put(spaceWithinBraces,spaceWithinBracesDefault);
        defaults.put(spaceWithinArrayInitBrackets,spaceWithinArrayInitBracketsDefault);

        defaults.put(spaceBeforeComma,spaceBeforeCommaDefault);
        defaults.put(spaceAfterComma,spaceAfterCommaDefault);
        defaults.put(spaceBeforeSemi,spaceBeforeSemiDefault);
        defaults.put(spaceAfterSemi,spaceAfterSemiDefault);
        defaults.put(spaceBeforeColon,spaceBeforeColonDefault);
        defaults.put(spaceAfterColon,spaceAfterColonDefault);
        defaults.put(spaceAfterTypeCast,spaceAfterTypeCastDefault);
    
        defaults.put(blankLinesBeforeClass,blankLinesBeforeClassDefault);
        defaults.put(blankLinesAfterClass,blankLinesAfterClassDefault);
        defaults.put(blankLinesAfterClassHeader,blankLinesAfterClassHeaderDefault);
        defaults.put(blankLinesBeforeFields,blankLinesBeforeFieldsDefault);
        defaults.put(blankLinesAfterFields,blankLinesAfterFieldsDefault);
        defaults.put(blankLinesBeforeMethods,blankLinesBeforeMethodsDefault);
        defaults.put(blankLinesAfterMethods,blankLinesAfterMethodsDefault);      
        
        defaults.put(alignMultilineArrayInit,alignMultilineArrayInitDefault);
        defaults.put(alignMultilineCallArgs,alignMultilineCallArgsDefault);
        defaults.put(alignMultilineMethodParams,alignMultilineMethodParamsDefault);
    }

    public static Object getDefault(String id){
        return defaults.get(id);
    }
    
    public static String getCurrentProfileId(CodeStyle.Language language) {
        switch(language){
            case C:
                return C_DEFAULT_PROFILE;
            case CPP:
            default:
                return CPP_DEFAULT_PROFILE;
        }
    }

    public static Object getLastValue(CodeStyle.Language language, String optionID) {
        Preferences p = lastValues == null ? getPreferences(getCurrentProfileId(language)) : lastValues;
        Object def = getDefault(optionID);
        if (def instanceof Integer) {
            return p.getInt(optionID, (Integer)def);
        } else if (def instanceof Boolean) {
            return p.getBoolean(optionID, (Boolean)def);
        }
        return p.get(optionID, def.toString());
    }

    public static Preferences getPreferences(String profileId) {
        return NbPreferences.forModule(CodeStyle.class).node("CodeStyle").node(profileId);
    }

    public static CodeStyle createCodeStyle(CodeStyle.Language language, Preferences p) {
        CodeStyle.getDefault(language);
        return codeStyleProducer.create(p);
    }

    public static interface CodeStyleProducer {
        public CodeStyle create( Preferences preferences );
    }
}
