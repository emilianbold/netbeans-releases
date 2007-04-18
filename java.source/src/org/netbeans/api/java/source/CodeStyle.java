/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.ui.FmtOptions;

import static org.netbeans.modules.java.ui.FmtOptions.*;

/** 
 *  XXX make sure the getters get the defaults from somewhere
 *  XXX add support for profiles
 *  XXX get the preferences node from somewhere else in odrer to be able not to
 *      use the getters and to be able to write to it.
 * 
 * @author Dusan Balek
 */
public final class CodeStyle {
    
    private static CodeStyle INSTANCE;

    static {
        FmtOptions.codeStyleProducer = new Producer();
    }
    
    private Preferences preferences;
    
    private CodeStyle(Preferences preferences) {
        this.preferences = preferences;
    }
    
    public synchronized static CodeStyle getDefault(Project project) {
        
        if ( FmtOptions.codeStyleProducer == null ) {
            FmtOptions.codeStyleProducer = new Producer();
        }
        
        if (INSTANCE == null)
            INSTANCE = create();
        return INSTANCE;
    }
    
    static CodeStyle create() {
        return new CodeStyle(FmtOptions.getPreferences(FmtOptions.getCurrentProfileId()));
    }
    
    // General tabs and indents ------------------------------------------------
    
    public boolean expandTabToSpaces() {
        return preferences.getBoolean(expandTabToSpaces, true);
    }

    public int getTabSize() {
        return preferences.getInt(tabSize, 4);
    }

    public int getIndentSize() {
        return preferences.getInt(indentSize, 4);
    }

    public int getContinuationIndentSize() {
        return preferences.getInt(continuationIndentSize, 8);
    }

    public int getLabelIndent() {
        return preferences.getInt(labelIndent, 0);
    }

    public boolean absoluteLabelIndent() {
        return preferences.getBoolean(absoluteLabelIndent, false);
    }

    public boolean indentTopLevelClassMembers() {
        return preferences.getBoolean(indentTopLevelClassMembers, true);
    }
    
    public boolean indentCasesFromSwitch() {
        return preferences.getBoolean(indentCasesFromSwitch, true);
    }

    public int getRightMargin() {
        return preferences.getInt(rightMargin, 120);
    }

    // Code generation ---------------------------------------------------------
    
    public boolean preferLongerNames() {
        return preferences.getBoolean(preferLongerNames, true);
    }

    public String getFieldNamePrefix() {
        return preferences.get(fieldNamePrefix, null);
    }

    public String getFieldNameSuffix() {
        return preferences.get(fieldNameSuffix, null);
    }

    public String getStaticFieldNamePrefix() {
        return preferences.get(staticFieldNamePrefix, null);
    }

    public String getStaticFieldNameSuffix() {
        return preferences.get(staticFieldNameSuffix, null);
    }

    public String getParameterNamePrefix() {
        return preferences.get(parameterNamePrefix, null);
    }

    public String getParameterNameSuffix() {
        return preferences.get(parameterNameSuffix, null);
    }

    public String getLocalVarNamePrefix() {
        return preferences.get(localVarNamePrefix, null);
    }

    public String getLocalVarNameSuffix() {
        return preferences.get(localVarNameSuffix, null);
    }

    public boolean qualifyFieldAccess() {
        return preferences.getBoolean(qualifyFieldAccess, false);
    }

    public boolean useIsForBooleanGetters() {
        return preferences.getBoolean(useIsForBooleanGetters, true);
    }

    public boolean addOverrideAnnotation() {
        return preferences.getBoolean(addOverrideAnnotation, true);
    }

    public boolean makeLocalVarsFinal() {
        return preferences.getBoolean(makeLocalVarsFinal, false);
    }

    public boolean makeParametersFinal() {
        return preferences.getBoolean(makeParametersFinal, false);
    }

    // Alignment and braces ----------------------------------------------------
    
    public BracePlacement getClassDeclBracePlacement() {
        String placement = preferences.get(classDeclBracePlacement, null);
        return placement != null ? BracePlacement.valueOf(placement): BracePlacement.SAME_LINE;
    }

    public BracePlacement getMethodDeclBracePlacement() {
        String placement = preferences.get(methodDeclBracePlacement, null);
        return placement != null ? BracePlacement.valueOf(placement): BracePlacement.SAME_LINE;
    }

    public BracePlacement getOtherBracePlacement() {
        String placement = preferences.get(otherBracePlacement, null);
        return placement != null ? BracePlacement.valueOf(placement): BracePlacement.SAME_LINE;
    }

    public boolean specialElseIf() {
        return preferences.getBoolean(specialElseIf, true);
    }

    public BracesGenerationStyle redundantIfBraces() {
        String redundant = preferences.get(redundantIfBraces, null);
        return redundant != null ? BracesGenerationStyle.valueOf(redundant): BracesGenerationStyle.GENERATE;
    }

    public BracesGenerationStyle redundantForBraces() {
        String redundant = preferences.get(redundantForBraces, null);
        return redundant != null ? BracesGenerationStyle.valueOf(redundant): BracesGenerationStyle.GENERATE;
    }

    public BracesGenerationStyle redundantWhileBraces() {
        String redundant = preferences.get(redundantWhileBraces, null);
        return redundant != null ? BracesGenerationStyle.valueOf(redundant): BracesGenerationStyle.GENERATE;
    }

    public BracesGenerationStyle redundantDoWhileBraces() {
        String redundant = preferences.get(redundantDoWhileBraces, null);
        return redundant != null ? BracesGenerationStyle.valueOf(redundant): BracesGenerationStyle.GENERATE;
    }

    public boolean alignMultilineMethodParams() {
        return preferences.getBoolean(alignMultilineMethodParams, false);
    }

    public boolean alignMultilineCallArgs() {
        return preferences.getBoolean(alignMultilineCallArgs, false);
    }

    public boolean alignMultilineImplements() {
        return preferences.getBoolean(alignMultilineImplements, false);
    }

    public boolean alignMultilineThrows() {
        return preferences.getBoolean(alignMultilineThrows, false);
    }

    public boolean alignMultilineParenthesized() {
        return preferences.getBoolean(alignMultilineParenthesized, false);
    }

    public boolean alignMultilineBinaryOp() {
        return preferences.getBoolean(alignMultilineBinaryOp, false);
    }

    public boolean alignMultilineTernaryOp() {
        return preferences.getBoolean(alignMultilineTernaryOp, false);
    }

    public boolean alignMultilineAssignment() {
        return preferences.getBoolean(alignMultilineAssignment, false);
    }

    public boolean alignMultilineFor() {
        return preferences.getBoolean(alignMultilineFor, false);
    }

    public boolean alignMultilineArrayInit() {
        return preferences.getBoolean(alignMultilineArrayInit, false);
    }

    public boolean placeElseOnNewLine() {
        return preferences.getBoolean(placeElseOnNewLine, false);
    }

    public boolean placeWhileOnNewLine() {
        return preferences.getBoolean(placeWhileOnNewLine, false);
    }

    public boolean placeCatchOnNewLine() {
        return preferences.getBoolean(placeCatchOnNewLine, false);
    }

    public boolean placeFinallyOnNewLine() {
        return preferences.getBoolean(placeFinallyOnNewLine, false);
    }
    
    // Wrapping ----------------------------------------------------------------
    
    public WrapStyle wrapExtendsImplementsKeyword() {
        String wrap = preferences.get(wrapExtendsImplementsKeyword, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapExtendsImplementsList() {
        String wrap = preferences.get(wrapExtendsImplementsList, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapMethodParams() {
        String wrap = preferences.get(wrapMethodParams, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapThrowsKeyword() {
        String wrap = preferences.get(wrapThrowsKeyword, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapThrowsList() {
        String wrap = preferences.get(wrapThrowsList, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapMethodCallArgs() {
        String wrap = preferences.get(wrapMethodCallArgs, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapChainedMethodCalls() {
        String wrap = preferences.get(wrapChainedMethodCalls, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapModifiers() {
        String wrap = preferences.get(wrapModifiers, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapArrayInit() {
        String wrap = preferences.get(wrapArrayInit, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapFor() {
        String wrap = preferences.get(wrapFor, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapForStatement() {
        String wrap = preferences.get(wrapForStatement, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_ALWAYS;
    }

    public WrapStyle wrapIfStatement() {
        String wrap = preferences.get(wrapIfStatement, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_ALWAYS;
    }

    public WrapStyle wrapWhileStatement() {
        String wrap = preferences.get(wrapWhileStatement, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_ALWAYS;
    }

    public WrapStyle wrapDoWhileStatement() {
        String wrap = preferences.get(wrapDoWhileStatement, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_ALWAYS;
    }

    public WrapStyle wrapAssert() {
        String wrap = preferences.get(wrapAssert, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapEnumConstants() {
        String wrap = preferences.get(wrapEnumConstants, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapAnnotations() {
        String wrap = preferences.get(wrapAnnotations, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_ALWAYS;
    }

    public WrapStyle wrapBinaryOps() {
        String wrap = preferences.get(wrapBinaryOps, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapTernaryOps() {
        String wrap = preferences.get(wrapTernaryOps, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    public WrapStyle wrapAssignOps() {
        String wrap = preferences.get(wrapAssignOps, null);
        return wrap != null ? WrapStyle.valueOf(wrap): CodeStyle.WrapStyle.WRAP_NEVER;
    }

    // Blank lines -------------------------------------------------------------
    
    public int getBlankLinesBeforePackage() {
        return preferences.getInt(blankLinesBeforePackage, 0);
    }

    public int getBlankLinesAfterPackage() {
        return preferences.getInt(blankLinesAfterPackage, 1);
    }

    public int getBlankLinesBeforeImports() {
        return preferences.getInt(blankLinesBeforeImports, 1);
    }

    public int getBlankLinesAfterImports() {
        return preferences.getInt(blankLinesAfterImports, 1);
    }

    public int getBlankLinesBeforeClass() {
        return preferences.getInt(blankLinesBeforeClass, 1);
    }

    public int getBlankLinesAfterClass() {
        return preferences.getInt(blankLinesAfterClass, 0);
    }

    public int getBlankLinesAfterClassHeader() {
        return preferences.getInt(blankLinesAfterClassHeader, 1);
    }

    public int getBlankLinesBeforeFields() {
        return preferences.getInt(blankLinesBeforeFields, 0);
    }

    public int getBlankLinesAfterFields() {
        return preferences.getInt(blankLinesAfterFields, 0);
    }

    public int getBlankLinesBeforeMethods() {
        return preferences.getInt(blankLinesBeforeMethods, 1);
    }

    public int getBlankLinesAfterMethods() {
        return preferences.getInt(blankLinesAfterMethods, 0);
    }

    // Spaces ------------------------------------------------------------------
    
    public boolean spaceBeforeWhile() {
        return preferences.getBoolean(spaceBeforeWhile, true);
    }

    public boolean spaceBeforeElse() {
        return preferences.getBoolean(spaceBeforeElse, true);
    }

    public boolean spaceBeforeCatch() {
        return preferences.getBoolean(spaceBeforeCatch, true);
    }

    public boolean spaceBeforeFinally() {
        return preferences.getBoolean(spaceBeforeFinally, true);
    }

    public boolean spaceBeforeMethodDeclParen() {
        return preferences.getBoolean(spaceBeforeMethodDeclParen, false);
    }

    public boolean spaceBeforeMethodCallParen() {
        return preferences.getBoolean(spaceBeforeMethodCallParen, false);
    }

    public boolean spaceBeforeIfParen() {
        return preferences.getBoolean(spaceBeforeIfParen, true);
    }

    public boolean spaceBeforeForParen() {
        return preferences.getBoolean(spaceBeforeForParen, true);
    }

    public boolean spaceBeforeWhileParen() {
        return preferences.getBoolean(spaceBeforeWhileParen, true);
    }

    public boolean spaceBeforeCatchParen() {
        return preferences.getBoolean(spaceBeforeCatchParen, true);
    }

    public boolean spaceBeforeSwitchParen() {
        return preferences.getBoolean(spaceBeforeSwitchParen, true);
    }

    public boolean spaceBeforeSynchronizedParen() {
        return preferences.getBoolean(spaceBeforeSynchronizedParen, true);
    }

    public boolean spaceBeforeAnnotationParen() {
        return preferences.getBoolean(spaceBeforeAnnotationParen, false);
    }

    public boolean spaceAroundUnaryOps() {
        return preferences.getBoolean(spaceAroundUnaryOps, false);
    }

    public boolean spaceAroundBinaryOps() {
        return preferences.getBoolean(spaceAroundBinaryOps, true);
    }

    public boolean spaceAroundTernaryOps() {
        return preferences.getBoolean(spaceAroundTernaryOps, true);
    }

    public boolean spaceAroundAssignOps() {
        return preferences.getBoolean(spaceAroundAssignOps, true);
    }

    public boolean spaceBeforeClassDeclLeftBrace() {
        return preferences.getBoolean(spaceBeforeClassDeclLeftBrace, true);
    }

    public boolean spaceBeforeMethodDeclLeftBrace() {
        return preferences.getBoolean(spaceBeforeMethodDeclLeftBrace, true);
    }

    public boolean spaceBeforeIfLeftBrace() {
        return preferences.getBoolean(spaceBeforeIfLeftBrace, true);
    }

    public boolean spaceBeforeElseLeftBrace() {
        return preferences.getBoolean(spaceBeforeElseLeftBrace, true);
    }

    public boolean spaceBeforeWhileLeftBrace() {
        return preferences.getBoolean(spaceBeforeWhileLeftBrace, true);
    }

    public boolean spaceBeforeForLeftBrace() {
        return preferences.getBoolean(spaceBeforeForLeftBrace, true);
    }

    public boolean spaceBeforeDoLeftBrace() {
        return preferences.getBoolean(spaceBeforeDoLeftBrace, true);
    }

    public boolean spaceBeforeSwitchLeftBrace() {
        return preferences.getBoolean(spaceBeforeSwitchLeftBrace, true);
    }

    public boolean spaceBeforeTryLeftBrace() {
        return preferences.getBoolean(spaceBeforeTryLeftBrace, true);
    }

    public boolean spaceBeforeCatchLeftBrace() {
        return preferences.getBoolean(spaceBeforeCatchLeftBrace, true);
    }

    public boolean spaceBeforeFinallyLeftBrace() {
        return preferences.getBoolean(spaceBeforeFinallyLeftBrace, true);
    }

    public boolean spaceBeforeSynchronizedLeftBrace() {
        return preferences.getBoolean(spaceBeforeSynchronizedLeftBrace, true);
    }

    public boolean spaceBeforeStaticInitLeftBrace() {
        return preferences.getBoolean(spaceBeforeStaticInitLeftBrace, true);
    }

    public boolean spaceBeforeArrayInitLeftBrace() {
        return preferences.getBoolean(spaceBeforeArrayInitLeftBrace, true);
    }

    public boolean spaceWithinParens() {
        return preferences.getBoolean(spaceWithinParens, false);
    }

    public boolean spaceWithinMethodDeclParens() {
        return preferences.getBoolean(spaceWithinMethodDeclParens, false);
    }

    public boolean spaceWithinMethodCallParens() {
        return preferences.getBoolean(spaceWithinMethodCallParens, false);
    }

    public boolean spaceWithinIfParens() {
        return preferences.getBoolean(spaceWithinIfParens, false);
    }

    public boolean spaceWithinForParens() {
        return preferences.getBoolean(spaceWithinForParens, false);
    }

    public boolean spaceWithinWhileParens() {
        return preferences.getBoolean(spaceWithinWhileParens, false);
    }

    public boolean spaceWithinSwitchParens() {
        return preferences.getBoolean(spaceWithinSwitchParens, false);
    }

    public boolean spaceWithinCatchParens() {
        return preferences.getBoolean(spaceWithinCatchParens, false);
    }

    public boolean spaceWithinSynchronizedParens() {
        return preferences.getBoolean(spaceWithinSynchronizedParens, false);
    }

    public boolean spaceWithinTypeCastParens() {
        return preferences.getBoolean(spaceWithinTypeCastParens, false);
    }

    public boolean spaceWithinAnnotationParens() {
        return preferences.getBoolean(spaceWithinAnnotationParens, false);
    }

    public boolean spaceWithinBraces() {
        return preferences.getBoolean(spaceWithinBraces, false);
    }

    public boolean spaceWithinArrayInitBrackets() {
        return preferences.getBoolean(spaceWithinArrayInitBrackets, false);
    }

    public boolean spaceBeforeComma() {
        return preferences.getBoolean(spaceBeforeComma, false);
    }

    public boolean spaceAfterComma() {
        return preferences.getBoolean(spaceAfterComma, true);
    }

    public boolean spaceBeforeSemi() {
        return preferences.getBoolean(spaceBeforeSemi, false);
    }

    public boolean spaceAfterSemi() {
        return preferences.getBoolean(spaceAfterSemi, true);
    }

    public boolean spaceAfterTypeCast() {
        return preferences.getBoolean(spaceAfterTypeCast, true);
    }

    // Imports -----------------------------------------------------------------

    public boolean useSingleClassImport() {
        return preferences.getBoolean(useSingleClassImport, true);
    }

    public boolean useFQNs() {
        return preferences.getBoolean(useFQNs, false);
    }

    public int countForUsingStarImport() {
        return preferences.getInt(countForUsingStarImport, 5);
    }

    public int countForUsingStaticStarImport() {
        return preferences.getInt(countForUsingStaticStarImport, 3);
    }

    public String[] getPackagesForStarImport() {
        return null;
    }

    // Nested classes ----------------------------------------------------------

    public enum BracePlacement {
        SAME_LINE,
        NEW_LINE,
        NEW_LINE_HALF_INDENTED,
        NEW_LINE_INDENTED
    }

    public enum BracesGenerationStyle {
        GENERATE,
        LEAVE_ALONE,
        ELIMINATE
    }
    
    public enum WrapStyle {
        WRAP_ALWAYS,
        WRAP_IF_LONG,
        WRAP_NEVER
    }
    
    // Communication with non public packages ----------------------------------
    
    private static class Producer implements FmtOptions.CodeStyleProducer {

        public CodeStyle create(Preferences preferences) {
            return new CodeStyle(preferences);
        }
        
    } 
    
}
