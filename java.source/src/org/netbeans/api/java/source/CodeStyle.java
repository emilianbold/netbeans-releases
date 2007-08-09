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
        return preferences.getBoolean(expandTabToSpaces, getGlobalExpandTabToSpaces());
    }

    public int getTabSize() {
        return preferences.getInt(tabSize, getGlobalTabSize());
    }

    public int getIndentSize() {
        return preferences.getInt(indentSize, getGlobalIndentSize());
    }

    public int getContinuationIndentSize() {
        return preferences.getInt(continuationIndentSize, getDefaultAsInt(continuationIndentSize));
    }

    public int getLabelIndent() {
        return preferences.getInt(labelIndent, getDefaultAsInt(labelIndent));
    }

    public boolean absoluteLabelIndent() {
        return preferences.getBoolean(absoluteLabelIndent, getDefaultAsBoolean(absoluteLabelIndent));
    }

    public boolean indentTopLevelClassMembers() {
        return preferences.getBoolean(indentTopLevelClassMembers, getDefaultAsBoolean(indentTopLevelClassMembers));
    }
    
    public boolean indentCasesFromSwitch() {
        return preferences.getBoolean(indentCasesFromSwitch, getDefaultAsBoolean(indentCasesFromSwitch));
    }

    public int getRightMargin() {
        return preferences.getInt(rightMargin, getGlobalRightMargin());
    }

    // Code generation ---------------------------------------------------------
    
    public boolean preferLongerNames() {
        return preferences.getBoolean(preferLongerNames, getDefaultAsBoolean(preferLongerNames));
    }

    public String getFieldNamePrefix() {
        return preferences.get(fieldNamePrefix, getDefaultAsString(fieldNamePrefix));
    }

    public String getFieldNameSuffix() {
        return preferences.get(fieldNameSuffix, getDefaultAsString(fieldNameSuffix));
    }

    public String getStaticFieldNamePrefix() {
        return preferences.get(staticFieldNamePrefix, getDefaultAsString(staticFieldNamePrefix));
    }

    public String getStaticFieldNameSuffix() {
        return preferences.get(staticFieldNameSuffix, getDefaultAsString(staticFieldNameSuffix));
    }

    public String getParameterNamePrefix() {
        return preferences.get(parameterNamePrefix, getDefaultAsString(parameterNamePrefix));
    }

    public String getParameterNameSuffix() {
        return preferences.get(parameterNameSuffix, getDefaultAsString(parameterNameSuffix));
    }

    public String getLocalVarNamePrefix() {
        return preferences.get(localVarNamePrefix, getDefaultAsString(localVarNamePrefix));
    }

    public String getLocalVarNameSuffix() {
        return preferences.get(localVarNameSuffix, getDefaultAsString(localVarNameSuffix));
    }

    public boolean qualifyFieldAccess() {
        return preferences.getBoolean(qualifyFieldAccess, getDefaultAsBoolean(qualifyFieldAccess));
    }

    public boolean useIsForBooleanGetters() {
        return preferences.getBoolean(useIsForBooleanGetters, getDefaultAsBoolean(useIsForBooleanGetters));
    }

    public boolean addOverrideAnnotation() {
        return preferences.getBoolean(addOverrideAnnotation, getDefaultAsBoolean(addOverrideAnnotation));
    }

    public boolean makeLocalVarsFinal() {
        return preferences.getBoolean(makeLocalVarsFinal, getDefaultAsBoolean(makeLocalVarsFinal));
    }

    public boolean makeParametersFinal() {
        return preferences.getBoolean(makeParametersFinal, getDefaultAsBoolean(useFQNs));
    }

    // Alignment and braces ----------------------------------------------------
    
    public BracePlacement getClassDeclBracePlacement() {
        String placement = preferences.get(classDeclBracePlacement, getDefaultAsString(classDeclBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getMethodDeclBracePlacement() {
        String placement = preferences.get(methodDeclBracePlacement, getDefaultAsString(methodDeclBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public BracePlacement getOtherBracePlacement() {
        String placement = preferences.get(otherBracePlacement, getDefaultAsString(otherBracePlacement));
        return BracePlacement.valueOf(placement);
    }

    public boolean specialElseIf() {
        return preferences.getBoolean(specialElseIf, getDefaultAsBoolean(specialElseIf));
    }

    public BracesGenerationStyle redundantIfBraces() {
        String redundant = preferences.get(redundantIfBraces, getDefaultAsString(redundantIfBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    public BracesGenerationStyle redundantForBraces() {
        String redundant = preferences.get(redundantForBraces, getDefaultAsString(redundantForBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    public BracesGenerationStyle redundantWhileBraces() {
        String redundant = preferences.get(redundantWhileBraces, getDefaultAsString(redundantWhileBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    public BracesGenerationStyle redundantDoWhileBraces() {
        String redundant = preferences.get(redundantDoWhileBraces, getDefaultAsString(redundantDoWhileBraces));
        return BracesGenerationStyle.valueOf(redundant);
    }

    public boolean alignMultilineMethodParams() {
        return preferences.getBoolean(alignMultilineMethodParams, getDefaultAsBoolean(alignMultilineMethodParams));
    }

    public boolean alignMultilineCallArgs() {
        return preferences.getBoolean(alignMultilineCallArgs, getDefaultAsBoolean(alignMultilineCallArgs));
    }

    public boolean alignMultilineImplements() {
        return preferences.getBoolean(alignMultilineImplements, getDefaultAsBoolean(alignMultilineImplements));
    }

    public boolean alignMultilineThrows() {
        return preferences.getBoolean(alignMultilineThrows, getDefaultAsBoolean(alignMultilineThrows));
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

    public boolean placeFinallyOnNewLine() {
        return preferences.getBoolean(placeFinallyOnNewLine, getDefaultAsBoolean(placeFinallyOnNewLine));
    }
    
    public boolean placeNewLineAfterModifiers() {
        return preferences.getBoolean(placeNewLineAfterModifiers, getDefaultAsBoolean(placeNewLineAfterModifiers));
    }

    // Wrapping ----------------------------------------------------------------
    
    public WrapStyle wrapExtendsImplementsKeyword() {
        String wrap = preferences.get(wrapExtendsImplementsKeyword, getDefaultAsString(wrapExtendsImplementsKeyword));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapExtendsImplementsList() {
        String wrap = preferences.get(wrapExtendsImplementsList, getDefaultAsString(wrapExtendsImplementsList));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapMethodParams() {
        String wrap = preferences.get(wrapMethodParams, getDefaultAsString(wrapMethodParams));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapThrowsKeyword() {
        String wrap = preferences.get(wrapThrowsKeyword, getDefaultAsString(wrapThrowsKeyword));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapThrowsList() {
        String wrap = preferences.get(wrapThrowsList, getDefaultAsString(wrapThrowsList));
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

    public WrapStyle wrapAssert() {
        String wrap = preferences.get(wrapAssert, getDefaultAsString(wrapAssert));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapEnumConstants() {
        String wrap = preferences.get(wrapEnumConstants, getDefaultAsString(wrapEnumConstants));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAnnotations() {
        String wrap = preferences.get(wrapAnnotations, getDefaultAsString(wrapAnnotations));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapBinaryOps() {
        String wrap = preferences.get(wrapBinaryOps, getDefaultAsString(wrapBinaryOps));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapTernaryOps() {
        String wrap = preferences.get(wrapTernaryOps, getDefaultAsString(wrapTernaryOps));
        return WrapStyle.valueOf(wrap);
    }

    public WrapStyle wrapAssignOps() {
        String wrap = preferences.get(wrapAssignOps, getDefaultAsString(wrapAssignOps));
        return WrapStyle.valueOf(wrap);
    }

    // Blank lines -------------------------------------------------------------
    
    public int getBlankLinesBeforePackage() {
        return preferences.getInt(blankLinesBeforePackage, getDefaultAsInt(blankLinesBeforePackage));
    }

    public int getBlankLinesAfterPackage() {
        return preferences.getInt(blankLinesAfterPackage, getDefaultAsInt(blankLinesAfterPackage));
    }

    public int getBlankLinesBeforeImports() {
        return preferences.getInt(blankLinesBeforeImports, getDefaultAsInt(blankLinesBeforeImports));
    }

    public int getBlankLinesAfterImports() {
        return preferences.getInt(blankLinesAfterImports, getDefaultAsInt(blankLinesAfterImports));
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

    public int getBlankLinesBeforeFields() {
        return preferences.getInt(blankLinesBeforeFields, getDefaultAsInt(blankLinesBeforeFields));
    }

    public int getBlankLinesAfterFields() {
        return preferences.getInt(blankLinesAfterFields, getDefaultAsInt(blankLinesAfterFields));
    }

    public int getBlankLinesBeforeMethods() {
        return preferences.getInt(blankLinesBeforeMethods, getDefaultAsInt(blankLinesBeforeMethods));
    }

    public int getBlankLinesAfterMethods() {
        return preferences.getInt(blankLinesAfterMethods, getDefaultAsInt(blankLinesAfterMethods));
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

    public boolean spaceBeforeSynchronizedParen() {
        return preferences.getBoolean(spaceBeforeSynchronizedParen, getDefaultAsBoolean(spaceBeforeSynchronizedParen));
    }

    public boolean spaceBeforeAnnotationParen() {
        return preferences.getBoolean(spaceBeforeAnnotationParen, getDefaultAsBoolean(spaceBeforeAnnotationParen));
    }

    public boolean spaceAroundUnaryOps() {
        return preferences.getBoolean(spaceAroundUnaryOps, getDefaultAsBoolean(spaceAroundUnaryOps));
    }

    public boolean spaceAroundBinaryOps() {
        return preferences.getBoolean(spaceAroundBinaryOps, getDefaultAsBoolean(spaceAroundBinaryOps));
    }

    public boolean spaceAroundTernaryOps() {
        return preferences.getBoolean(spaceAroundTernaryOps, getDefaultAsBoolean(spaceAroundTernaryOps));
    }

    public boolean spaceAroundAssignOps() {
        return preferences.getBoolean(spaceAroundAssignOps, getDefaultAsBoolean(spaceAroundAssignOps));
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

    public boolean spaceBeforeSynchronizedLeftBrace() {
        return preferences.getBoolean(spaceBeforeSynchronizedLeftBrace, getDefaultAsBoolean(spaceBeforeSynchronizedLeftBrace));
    }

    public boolean spaceBeforeStaticInitLeftBrace() {
        return preferences.getBoolean(spaceBeforeStaticInitLeftBrace, getDefaultAsBoolean(spaceBeforeStaticInitLeftBrace));
    }

    public boolean spaceBeforeArrayInitLeftBrace() {
        return preferences.getBoolean(spaceBeforeArrayInitLeftBrace, getDefaultAsBoolean(spaceBeforeArrayInitLeftBrace));
    }

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

    public boolean spaceWithinSynchronizedParens() {
        return preferences.getBoolean(spaceWithinSynchronizedParens, getDefaultAsBoolean(spaceWithinSynchronizedParens));
    }

    public boolean spaceWithinTypeCastParens() {
        return preferences.getBoolean(spaceWithinTypeCastParens, getDefaultAsBoolean(spaceWithinTypeCastParens));
    }

    public boolean spaceWithinAnnotationParens() {
        return preferences.getBoolean(spaceWithinAnnotationParens, getDefaultAsBoolean(spaceWithinAnnotationParens));
    }

    public boolean spaceWithinBraces() {
        return preferences.getBoolean(spaceWithinBraces, getDefaultAsBoolean(spaceWithinBraces));
    }

    public boolean spaceWithinArrayInitBrackets() {
        return preferences.getBoolean(spaceWithinArrayInitBrackets, getDefaultAsBoolean(spaceWithinArrayInitBrackets));
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

    public boolean spaceAfterTypeCast() {
        return preferences.getBoolean(spaceAfterTypeCast, getDefaultAsBoolean(spaceAfterTypeCast));
    }

    // Imports -----------------------------------------------------------------

    public boolean useSingleClassImport() {
        return preferences.getBoolean(useSingleClassImport, getDefaultAsBoolean(useSingleClassImport));
    }

    public boolean useFQNs() {
        return preferences.getBoolean(useFQNs, getDefaultAsBoolean(useFQNs));
    }

    public int countForUsingStarImport() {
        return preferences.getInt(countForUsingStarImport, getDefaultAsInt(countForUsingStarImport));
    }

    public int countForUsingStaticStarImport() {
        return preferences.getInt(countForUsingStaticStarImport, getDefaultAsInt(countForUsingStaticStarImport));
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
