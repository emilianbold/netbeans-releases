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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.ui;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.tree.JCTree;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.editor.Settings;
import org.netbeans.modules.java.source.JavaSourceAccessor;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.api.java.source.CodeStyle;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.SettingsNames;
import org.netbeans.modules.java.source.pretty.VeryPretty;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.Exceptions;

import static org.netbeans.api.java.source.CodeStyle.*;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author phrebejk
 */
public class FmtOptions {

    public static final String expandTabToSpaces = "expandTabToSpaces"; //NOI18N
    public static final String tabSize = "tabSize"; //NOI18N
    public static final String indentSize = "indentSize"; //NOI18N
    public static final String continuationIndentSize = "continuationIndentSize"; //NOI18N
    public static final String labelIndent = "labelIndent"; //NOI18N
    public static final String absoluteLabelIndent = "absoluteLabelIndent"; //NOI18N
    public static final String indentTopLevelClassMembers = "indentTopLevelClassMembers"; //NOI18N
    public static final String indentCasesFromSwitch = "indentCasesFromSwitch"; //NOI18N
    public static final String rightMargin = "rightMargin"; //NOI18N
    
    public static final String preferLongerNames = "preferLongerNames"; //NOI18N
    public static final String fieldNamePrefix = "fieldNamePrefix"; //NOI18N
    public static final String fieldNameSuffix = "fieldNameSuffix"; //NOI18N
    public static final String staticFieldNamePrefix = "staticFieldNamePrefix"; //NOI18N
    public static final String staticFieldNameSuffix = "staticFieldNameSuffix"; //NOI18N
    public static final String parameterNamePrefix = "parameterNamePrefix"; //NOI18N
    public static final String parameterNameSuffix = "parameterNameSuffix"; //NOI18N
    public static final String localVarNamePrefix = "localVarNamePrefix"; //NOI18N
    public static final String localVarNameSuffix = "localVarNameSuffix"; //NOI18N
    public static final String qualifyFieldAccess = "qualifyFieldAccess"; //NOI18N
    public static final String useIsForBooleanGetters = "useIsForBooleanGetters"; //NOI18N
    public static final String addOverrideAnnotation = "addOverrideAnnotation"; //NOI18N
    public static final String makeLocalVarsFinal = "makeLocalVarsFinal"; //NOI18N
    public static final String makeParametersFinal = "makeParametersFinal"; //NOI18N
    public static final String classMembersOrder = "classMembersOrder"; //NOI18N
    
    public static final String classDeclBracePlacement = "classDeclBracePlacement"; //NOI18N
    public static final String methodDeclBracePlacement = "methodDeclBracePlacement"; //NOI18N
    public static final String otherBracePlacement = "otherBracePlacement"; //NOI18N
    public static final String specialElseIf = "specialElseIf"; //NOI18N
    public static final String redundantIfBraces = "redundantIfBraces"; //NOI18N
    public static final String redundantForBraces = "redundantForBraces"; //NOI18N
    public static final String redundantWhileBraces = "redundantWhileBraces"; //NOI18N
    public static final String redundantDoWhileBraces = "redundantDoWhileBraces"; //NOI18N
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final String alignMultilineImplements = "alignMultilineImplements"; //NOI18N
    public static final String alignMultilineThrows = "alignMultilineThrows"; //NOI18N
    public static final String alignMultilineParenthesized = "alignMultilineParenthesized"; //NOI18N
    public static final String alignMultilineBinaryOp = "alignMultilineBinaryOp"; //NOI18N
    public static final String alignMultilineTernaryOp = "alignMultilineTernaryOp"; //NOI18N
    public static final String alignMultilineAssignment = "alignMultilineAssignment"; //NOI18N
    public static final String alignMultilineFor = "alignMultilineFor"; //NOI18N
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N
    public static final String placeElseOnNewLine = "placeElseOnNewLine"; //NOI18N
    public static final String placeWhileOnNewLine = "placeWhileOnNewLine"; //NOI18N
    public static final String placeCatchOnNewLine = "placeCatchOnNewLine"; //NOI18N
    public static final String placeFinallyOnNewLine = "placeFinallyOnNewLine"; //NOI18N
    public static final String placeNewLineAfterModifiers = "placeNewLineAfterModifiers"; //NOI18N
    
    public static final String wrapExtendsImplementsKeyword = "wrapExtendsImplementsKeyword"; //NOI18N
    public static final String wrapExtendsImplementsList = "wrapExtendsImplementsList"; //NOI18N
    public static final String wrapMethodParams = "wrapMethodParams"; //NOI18N
    public static final String wrapThrowsKeyword = "wrapThrowsKeyword"; //NOI18N
    public static final String wrapThrowsList = "wrapThrowsList"; //NOI18N
    public static final String wrapMethodCallArgs = "wrapMethodCallArgs"; //NOI18N
    public static final String wrapChainedMethodCalls = "wrapChainedMethodCalls"; //NOI18N
    public static final String wrapArrayInit = "wrapArrayInit"; //NOI18N
    public static final String wrapFor = "wrapFor"; //NOI18N
    public static final String wrapForStatement = "wrapForStatement"; //NOI18N
    public static final String wrapIfStatement = "wrapIfStatement"; //NOI18N
    public static final String wrapWhileStatement = "wrapWhileStatement"; //NOI18N
    public static final String wrapDoWhileStatement = "wrapDoWhileStatement"; //NOI18N
    public static final String wrapAssert = "wrapAssert"; //NOI18N
    public static final String wrapEnumConstants = "wrapEnumConstants"; //NOI18N
    public static final String wrapAnnotations = "wrapAnnotations"; //NOI18N
    public static final String wrapBinaryOps = "wrapBinaryOps"; //NOI18N
    public static final String wrapTernaryOps = "wrapTernaryOps"; //NOI18N
    public static final String wrapAssignOps = "wrapAssignOps"; //NOI18N
    
    public static final String blankLinesBeforePackage = "blankLinesBeforePackage"; //NOI18N
    public static final String blankLinesAfterPackage = "blankLinesAfterPackage"; //NOI18N
    public static final String blankLinesBeforeImports = "blankLinesBeforeImports"; //NOI18N
    public static final String blankLinesAfterImports = "blankLinesAfterImports"; //NOI18N
    public static final String blankLinesBeforeClass = "blankLinesBeforeClass"; //NOI18N
    public static final String blankLinesAfterClass = "blankLinesAfterClass"; //NOI18N
    public static final String blankLinesAfterClassHeader = "blankLinesAfterClassHeader"; //NOI18N
    public static final String blankLinesBeforeFields = "blankLinesBeforeFields"; //NOI18N
    public static final String blankLinesAfterFields = "blankLinesAfterFields"; //NOI18N
    public static final String blankLinesBeforeMethods = "blankLinesBeforeMethods"; //NOI18N
    public static final String blankLinesAfterMethods = "blankLinesAfterMethods"; //NOI18N
    
    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final String spaceBeforeFinally = "spaceBeforeFinally"; //NOI18N
    public static final String spaceBeforeMethodDeclParen = "spaceBeforeMethodDeclParen"; //NOI18N
    public static final String spaceBeforeMethodCallParen = "spaceBeforeMethodCallParen"; //NOI18N
    public static final String spaceBeforeIfParen = "spaceBeforeIfParen"; //NOI18N
    public static final String spaceBeforeForParen = "spaceBeforeForParen"; //NOI18N
    public static final String spaceBeforeWhileParen = "spaceBeforeWhileParen"; //NOI18N
    public static final String spaceBeforeCatchParen = "spaceBeforeCatchParen"; //NOI18N
    public static final String spaceBeforeSwitchParen = "spaceBeforeSwitchParen"; //NOI18N
    public static final String spaceBeforeSynchronizedParen = "spaceBeforeSynchronizedParen"; //NOI18N
    public static final String spaceBeforeAnnotationParen = "spaceBeforeAnnotationParen"; //NOI18N    
    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final String spaceBeforeClassDeclLeftBrace = "spaceBeforeClassDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeMethodDeclLeftBrace = "spaceBeforeMethodDeclLeftBrace"; //NOI18N
    public static final String spaceBeforeIfLeftBrace = "spaceBeforeIfLeftBrace"; //NOI18N
    public static final String spaceBeforeElseLeftBrace = "spaceBeforeElseLeftBrace"; //NOI18N
    public static final String spaceBeforeWhileLeftBrace = "spaceBeforeWhileLeftBrace"; //NOI18N
    public static final String spaceBeforeForLeftBrace = "spaceBeforeForLeftBrace"; //NOI18N
    public static final String spaceBeforeDoLeftBrace = "spaceBeforeDoLeftBrace"; //NOI18N
    public static final String spaceBeforeSwitchLeftBrace = "spaceBeforeSwitchLeftBrace"; //NOI18N
    public static final String spaceBeforeTryLeftBrace = "spaceBeforeTryLeftBrace"; //NOI18N
    public static final String spaceBeforeCatchLeftBrace = "spaceBeforeCatchLeftBrace"; //NOI18N
    public static final String spaceBeforeFinallyLeftBrace = "spaceBeforeFinallyLeftBrace"; //NOI18N
    public static final String spaceBeforeSynchronizedLeftBrace = "spaceBeforeSynchronizedLeftBrace"; //NOI18N
    public static final String spaceBeforeStaticInitLeftBrace = "spaceBeforeStaticInitLeftBrace"; //NOI18N
    public static final String spaceBeforeArrayInitLeftBrace = "spaceBeforeArrayInitLeftBrace"; //NOI18N
    public static final String spaceWithinParens = "spaceWithinParens"; //NOI18N
    public static final String spaceWithinMethodDeclParens = "spaceWithinMethodDeclParens"; //NOI18N
    public static final String spaceWithinMethodCallParens = "spaceWithinMethodCallParens"; //NOI18N
    public static final String spaceWithinIfParens = "spaceWithinIfParens"; //NOI18N
    public static final String spaceWithinForParens = "spaceWithinForParens"; //NOI18N
    public static final String spaceWithinWhileParens = "spaceWithinWhileParens"; //NOI18N
    public static final String spaceWithinSwitchParens = "spaceWithinSwitchParens"; //NOI18N
    public static final String spaceWithinCatchParens = "spaceWithinCatchParens"; //NOI18N
    public static final String spaceWithinSynchronizedParens = "spaceWithinSynchronizedParens"; //NOI18N
    public static final String spaceWithinTypeCastParens = "spaceWithinTypeCastParens"; //NOI18N
    public static final String spaceWithinAnnotationParens = "spaceWithinAnnotationParens"; //NOI18N
    public static final String spaceWithinBraces = "spaceWithinBraces"; //NOI18N
    public static final String spaceWithinArrayInitBrackets = "spaceWithinArrayInitBrackets"; //NOI18N
    public static final String spaceBeforeComma = "spaceBeforeComma"; //NOI18N
    public static final String spaceAfterComma = "spaceAfterComma"; //NOI18N
    public static final String spaceBeforeSemi = "spaceBeforeSemi"; //NOI18N
    public static final String spaceAfterSemi = "spaceAfterSemi"; //NOI18N
    public static final String spaceBeforeColon = "spaceBeforeColon"; //NOI18N
    public static final String spaceAfterColon = "spaceAfterColon"; //NOI18N
    public static final String spaceAfterTypeCast = "spaceAfterTypeCast"; //NOI18N
    
    public static final String useSingleClassImport = "useSingleClassImport"; //NOI18N
    public static final String useFQNs = "useFQNs"; //NOI18N
    public static final String countForUsingStarImport = "countForUsingStarImport"; //NOI18N
    public static final String countForUsingStaticStarImport = "countForUsingStaticStarImport"; //NOI18N
    public static final String packagesForStarImport = "packagesForStarImport"; //NOI18N
    public static final String importsOrder = "importsOrder"; //NOI18N
    
    public static CodeStyleProducer codeStyleProducer;
        
    public static Preferences lastValues;
    
    private static Class<? extends EditorKit> kitClass;
    
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    
    private FmtOptions() {}

    public static int getDefaultAsInt(String key) {
        return Integer.parseInt(defaults.get(key));
    }
    
    public static boolean getDefaultAsBoolean(String key) {
        return Boolean.parseBoolean(defaults.get(key));
    }
        
    public static String getDefaultAsString(String key) {
        return defaults.get(key);
    }
    
    public static Preferences getPreferences(String profileId) {
        return NbPreferences.forModule(CodeStyle.class).node("CodeStyle").node(profileId);
    }
    
    public static boolean getGlobalExpandTabToSpaces() {
        Formatter f = (Formatter)Settings.getValue(getKitClass(), "formatter");
        if (f != null)
            return f.expandTabs();
        return getDefaultAsBoolean(expandTabToSpaces);
    }
    
    public static int getGlobalTabSize() {
        Integer i = (Integer)Settings.getValue(getKitClass(), SettingsNames.TAB_SIZE);
        return i != null ? i.intValue() : getDefaultAsInt(tabSize);
    }
    
    public static int getGlobalIndentSize() {
        Formatter f = (Formatter)Settings.getValue(getKitClass(), "formatter");
        if (f != null)
            return f.getShiftWidth();
        return getDefaultAsInt(indentSize);
    }
    
    public static int getGlobalRightMargin() {
        Integer i = (Integer)Settings.getValue(getKitClass(), SettingsNames.TEXT_LIMIT_WIDTH);
        return i != null ? i.intValue() : getDefaultAsInt(rightMargin);
    }
    
    public static Class<? extends EditorKit> getKitClass() {
        if (kitClass == null) {
            EditorKit kit = MimeLookup.getLookup(MimePath.get("text/x-java")).lookup(EditorKit.class); //NOI18N
            kitClass = kit != null ? kit.getClass() : EditorKit.class;
        }
        return kitClass;
    }
    
    public static void flush() {
        try {
            getPreferences( getCurrentProfileId()).flush();
        }
        catch(BackingStoreException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    public static String getCurrentProfileId() {
        return DEFAULT_PROFILE;
    }
    
    public static CodeStyle createCodeStyle(Preferences p) {
        CodeStyle.getDefault(null);
        return codeStyleProducer.create(p);
    }
    
    public static boolean isInteger(String optionID) {
        String value = defaults.get(optionID);
        
        try {
            Integer.parseInt(value);
            return true;            
        } catch (NumberFormatException numberFormatException) {
            return false;
        }
    }
    
    public static String getLastValue(String optionID) {
        Preferences p = lastValues == null ? getPreferences(getCurrentProfileId()) : lastValues;
        return p.get(optionID, getDefaultAsString(optionID));
    }
 
    // Private section ---------------------------------------------------------
    
    private static final String TRUE = "true";      // NOI18N
    private static final String FALSE = "false";    // NOI18N
    
    private static final String WRAP_ALWAYS  = WrapStyle.WRAP_ALWAYS.name();
    private static final String WRAP_IF_LONG  = WrapStyle.WRAP_IF_LONG.name();
    private static final String WRAP_NEVER  = WrapStyle.WRAP_NEVER.name();
    
    private static final String BP_NEW_LINE = BracePlacement.NEW_LINE.name();
    private static final String BP_NEW_LINE_HALF_INDENTED = BracePlacement.NEW_LINE_HALF_INDENTED.name();
    private static final String BP_NEW_LINE_INDENTED = BracePlacement.NEW_LINE_INDENTED.name();
    private static final String BP_SAME_LINE = BracePlacement.SAME_LINE.name(); 
    
    private static final String BGS_ELIMINATE = BracesGenerationStyle.ELIMINATE.name(); 
    private static final String BGS_LEAVE_ALONE = BracesGenerationStyle.LEAVE_ALONE.name(); 
    private static final String BGS_GENERATE = BracesGenerationStyle.GENERATE.name(); 
    
    private static Map<String,String> defaults;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        String defaultValues[][] = {
            { expandTabToSpaces, TRUE}, //NOI18N
            { tabSize, "4"}, //NOI18N
            { indentSize, "4"}, //NOI18N
            { continuationIndentSize, "8"}, //NOI18N
            { labelIndent, "0"}, //NOI18N
            { absoluteLabelIndent, FALSE}, //NOI18N
            { indentTopLevelClassMembers, TRUE}, //NOI18N
            { indentCasesFromSwitch, TRUE}, //NOI18N
            { rightMargin, "120"}, //NOI18N

            { preferLongerNames, TRUE}, //NOI18N
            { fieldNamePrefix, ""}, //NOI18N // XXX null
            { fieldNameSuffix, ""}, //NOI18N // XXX null
            { staticFieldNamePrefix, ""}, //NOI18N // XXX null
            { staticFieldNameSuffix, ""}, //NOI18N // XXX null
            { parameterNamePrefix, ""}, //NOI18N // XXX null
            { parameterNameSuffix, ""}, //NOI18N // XXX null
            { localVarNamePrefix, ""}, //NOI18N // XXX null
            { localVarNameSuffix, ""}, //NOI18N // XXX null
            { qualifyFieldAccess, FALSE}, //NOI18N // XXX
            { useIsForBooleanGetters, TRUE}, //NOI18N
            { addOverrideAnnotation, TRUE}, //NOI18N
            { makeLocalVarsFinal, FALSE}, //NOI18N
            { makeParametersFinal, FALSE}, //NOI18N
            { classMembersOrder, ""}, //NOI18N // XXX

            { classDeclBracePlacement, BP_SAME_LINE}, //NOI18N
            { methodDeclBracePlacement, BP_SAME_LINE}, //NOI18N
            { otherBracePlacement, BP_SAME_LINE}, //NOI18N
            { specialElseIf, TRUE}, //NOI18N
            { redundantIfBraces, BGS_GENERATE}, //NOI18N
            { redundantForBraces, BGS_GENERATE}, //NOI18N
            { redundantWhileBraces, BGS_GENERATE}, //NOI18N
            { redundantDoWhileBraces, BGS_GENERATE}, //NOI18N
            { alignMultilineMethodParams, FALSE}, //NOI18N
            { alignMultilineCallArgs, FALSE}, //NOI18N
            { alignMultilineImplements, FALSE}, //NOI18N
            { alignMultilineThrows, FALSE}, //NOI18N
            { alignMultilineParenthesized, FALSE}, //NOI18N
            { alignMultilineBinaryOp, FALSE}, //NOI18N
            { alignMultilineTernaryOp, FALSE}, //NOI18N
            { alignMultilineAssignment, FALSE}, //NOI18N
            { alignMultilineFor, FALSE}, //NOI18N
            { alignMultilineArrayInit, FALSE}, //NOI18N
            { placeElseOnNewLine, FALSE}, //NOI18N 
            { placeWhileOnNewLine, FALSE}, //NOI18N
            { placeCatchOnNewLine, FALSE}, //NOI18N 
            { placeFinallyOnNewLine, FALSE}, //NOI18N 
            { placeNewLineAfterModifiers, FALSE}, //NOI18N

            { wrapExtendsImplementsKeyword, WRAP_NEVER}, //NOI18N
            { wrapExtendsImplementsList, WRAP_NEVER}, //NOI18N
            { wrapMethodParams, WRAP_NEVER}, //NOI18N
            { wrapThrowsKeyword, WRAP_NEVER}, //NOI18N
            { wrapThrowsList, WRAP_NEVER}, //NOI18N
            { wrapMethodCallArgs, WRAP_NEVER}, //NOI18N
            { wrapChainedMethodCalls, WRAP_NEVER}, //NOI18N
            { wrapArrayInit, WRAP_NEVER}, //NOI18N
            { wrapFor, WRAP_NEVER}, //NOI18N
            { wrapForStatement, WRAP_ALWAYS}, //NOI18N
            { wrapIfStatement, WRAP_ALWAYS}, //NOI18N
            { wrapWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapDoWhileStatement, WRAP_ALWAYS}, //NOI18N
            { wrapAssert, WRAP_NEVER}, //NOI18N
            { wrapEnumConstants, WRAP_NEVER}, //NOI18N
            { wrapAnnotations, WRAP_ALWAYS}, //NOI18N
            { wrapBinaryOps, WRAP_NEVER}, //NOI18N
            { wrapTernaryOps, WRAP_NEVER}, //NOI18N
            { wrapAssignOps, WRAP_NEVER}, //NOI18N

            { blankLinesBeforePackage, "0"}, //NOI18N
            { blankLinesAfterPackage, "1"}, //NOI18N
            { blankLinesBeforeImports, "1"}, //NOI18N 
            { blankLinesAfterImports, "1"}, //NOI18N
            { blankLinesBeforeClass, "1"}, //NOI18N 
            { blankLinesAfterClass, "0"}, //NOI18N
            { blankLinesAfterClassHeader, "1"}, //NOI18N 
            { blankLinesBeforeFields, "0"}, //NOI18N 
            { blankLinesAfterFields, "0"}, //NOI18N
            { blankLinesBeforeMethods, "1"}, //NOI18N
            { blankLinesAfterMethods, "0"}, //NOI18N

            { spaceBeforeWhile, TRUE}, //NOI18N // XXX
            { spaceBeforeElse, TRUE}, //NOI18N // XXX
            { spaceBeforeCatch, TRUE}, //NOI18N // XXX
            { spaceBeforeFinally, TRUE}, //NOI18N // XXX
            { spaceBeforeMethodDeclParen, FALSE}, //NOI18N
            { spaceBeforeMethodCallParen, FALSE}, //NOI18N
            { spaceBeforeIfParen, TRUE}, //NOI18N
            { spaceBeforeForParen, TRUE}, //NOI18N
            { spaceBeforeWhileParen, TRUE}, //NOI18N
            { spaceBeforeCatchParen, TRUE}, //NOI18N
            { spaceBeforeSwitchParen, TRUE}, //NOI18N
            { spaceBeforeSynchronizedParen, TRUE}, //NOI18N
            { spaceBeforeAnnotationParen, FALSE}, //NOI18N    
            { spaceAroundUnaryOps, FALSE}, //NOI18N
            { spaceAroundBinaryOps, TRUE}, //NOI18N
            { spaceAroundTernaryOps, TRUE}, //NOI18N
            { spaceAroundAssignOps, TRUE}, //NOI18N
            { spaceBeforeClassDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeMethodDeclLeftBrace, TRUE}, //NOI18N
            { spaceBeforeIfLeftBrace, TRUE}, //NOI18N
            { spaceBeforeElseLeftBrace, TRUE}, //NOI18N
            { spaceBeforeWhileLeftBrace, TRUE}, //NOI18N
            { spaceBeforeForLeftBrace, TRUE}, //NOI18N
            { spaceBeforeDoLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSwitchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeTryLeftBrace, TRUE}, //NOI18N
            { spaceBeforeCatchLeftBrace, TRUE}, //NOI18N
            { spaceBeforeFinallyLeftBrace, TRUE}, //NOI18N
            { spaceBeforeSynchronizedLeftBrace, TRUE}, //NOI18N
            { spaceBeforeStaticInitLeftBrace, TRUE}, //NOI18N
            { spaceBeforeArrayInitLeftBrace, FALSE}, //NOI18N
            { spaceWithinParens, FALSE}, //NOI18N
            { spaceWithinMethodDeclParens, FALSE}, //NOI18N
            { spaceWithinMethodCallParens, FALSE}, //NOI18N
            { spaceWithinIfParens, FALSE}, //NOI18N
            { spaceWithinForParens, FALSE}, //NOI18N
            { spaceWithinWhileParens, FALSE}, //NOI18N
            { spaceWithinSwitchParens, FALSE}, //NOI18N
            { spaceWithinCatchParens, FALSE}, //NOI18N
            { spaceWithinSynchronizedParens, FALSE}, //NOI18N
            { spaceWithinTypeCastParens, FALSE}, //NOI18N
            { spaceWithinAnnotationParens, FALSE}, //NOI18N
            { spaceWithinBraces, FALSE}, //NOI18N
            { spaceWithinArrayInitBrackets, FALSE}, //NOI18N
            { spaceBeforeComma, FALSE}, //NOI18N
            { spaceAfterComma, TRUE}, //NOI18N
            { spaceBeforeSemi, FALSE}, //NOI18N
            { spaceAfterSemi, TRUE}, //NOI18N
            { spaceBeforeColon, TRUE}, //NOI18N
            { spaceAfterColon, TRUE}, //NOI18N
            { spaceAfterTypeCast, TRUE}, //NOI18N

            { useSingleClassImport, TRUE}, //NOI18N
            { useFQNs, FALSE}, //NOI18N
            { countForUsingStarImport, "5"}, //NOI18N
            { countForUsingStaticStarImport, "3"}, //NOI18N // XXX
            { packagesForStarImport, ""}, //NOI18N // XXX
            { importsOrder, ""}, //NOI18N // XXX
        };
        
        defaults = new HashMap<String,String>();
        
        for (java.lang.String[] strings : defaultValues) {
            defaults.put(strings[0], strings[1]);
        }

    }
 
    
    // Support section ---------------------------------------------------------
      
    public static class CategorySupport extends FormatingOptionsPanel.Category implements ActionListener, DocumentListener {

        public static final String OPTION_ID = "org.netbeans.modules.java.ui.FormatingOptions.ID";
                
        private static final int LOAD = 0;
        private static final int STORE = 1;
        private static final int ADD_LISTENERS = 2;
        
        private static final ComboItem  bracePlacement[] = new ComboItem[] {
                new ComboItem( BracePlacement.SAME_LINE.name(), "LBL_bp_SAME_LINE" ), // NOI18N
                new ComboItem( BracePlacement.NEW_LINE.name(), "LBL_bp_NEW_LINE" ), // NOI18N
                new ComboItem( BracePlacement.NEW_LINE_HALF_INDENTED.name(), "LBL_bp_NEW_LINE_HALF_INDENTED" ), // NOI18N
                new ComboItem( BracePlacement.NEW_LINE_INDENTED.name(), "LBL_bp_NEW_LINE_INDENTED" ) // NOI18N
            };
        private static final ComboItem  bracesGeneration[] = new ComboItem[] {
                new ComboItem( BracesGenerationStyle.GENERATE.name(), "LBL_bg_GENERATE" ), // NOI18N
                new ComboItem( BracesGenerationStyle.LEAVE_ALONE.name(), "LBL_bg_LEAVE_ALONE" ), // NOI18N
                new ComboItem( BracesGenerationStyle.ELIMINATE.name(), "LBL_bg_ELIMINATE" ) // NOI18N       
            };
        
        private static final ComboItem  wrap[] = new ComboItem[] {
                new ComboItem( WrapStyle.WRAP_ALWAYS.name(), "LBL_wrp_WRAP_ALWAYS" ), // NOI18N
                new ComboItem( WrapStyle.WRAP_IF_LONG.name(), "LBL_wrp_WRAP_IF_LONG" ), // NOI18N
                new ComboItem( WrapStyle.WRAP_NEVER.name(), "LBL_wrp_WRAP_NEVER" ) // NOI18N
            };
        
        
        private String previewText = NbBundle.getMessage(FmtOptions.class, "SAMPLE_Default");
        private String forcedOptions[][];
        
        private boolean changed = false;
        private JPanel panel;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
        public CategorySupport(String nameKey, JPanel panel, String previewText, String[]... forcedOptions) {
            super(nameKey);
            this.panel = panel;            
            this.previewText = previewText == null ? this.previewText : previewText;
            this.forcedOptions = forcedOptions;
            addListeners();
        }
        
        protected void addListeners() {
            scan(panel, ADD_LISTENERS, null);
        }
        
        public void update() {
            scan(panel, LOAD, null);
        }

        public void applyChanges() {
            scan(panel, STORE, null);
        }

        public void storeTo(Preferences preferences) {
            scan(panel, STORE, preferences);
        }

        public void refreshPreview(JEditorPane pane, Preferences p ) {
            
            for (String[] option : forcedOptions) {
                p.put( option[0], option[1]);
            }
            
            
            try {
                int rm = p.getInt(rightMargin, getDefaultAsInt(rightMargin));
                pane.putClientProperty("TextLimitLine", rm);
            }
            catch( NumberFormatException e ) {
                // Ignore it
            }
            
            CodeStyle codeStyle = FmtOptions.createCodeStyle(p);
            
            ClassPath empty = ClassPathSupport.createClassPath(new URL[0]);
            
            ClasspathInfo cpInfo = ClasspathInfo.create(
                    JavaPlatform.getDefault().getBootstrapLibraries(), empty, empty);
            
            JavacTaskImpl javacTask = JavaSourceAccessor.INSTANCE.createJavacTask(cpInfo, null, null);
            
            try {
                CompilationUnitTree tree = javacTask.parse(FileObjects.memoryFileObject(previewText, "ClassA")).iterator().next();
                
                VeryPretty vp = new VeryPretty(javacTask.getContext(), codeStyle);
                vp.print((JCTree)tree);
                
                pane.setText(vp.toString());
                
            } catch (IOException ioException) {
                Exceptions.printStackTrace(ioException);
            }

        }
        
        public void cancel() {
            // Usually does not need to do anything
        }

        public boolean isValid() {
            return true; // Should almost always be OK
        }

        public boolean isChanged() {
            return changed;
        }

        public JComponent getComponent(Lookup masterLookup) {
            return panel;
        }

        public HelpCtx getHelpCtx() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }
        
        void changed() {
            if (!changed) {
                changed = true;
                pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
            }
            pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
        }

        // ActionListener implementation ---------------------------------------
        
        public void actionPerformed(ActionEvent e) {
            changed();
        }
        
        // DocumentListener implementation -------------------------------------
        
        public void insertUpdate(DocumentEvent e) {
            changed();
        }

        public void removeUpdate(DocumentEvent e) {
            changed();
        }

        public void changedUpdate(DocumentEvent e) {
            changed();
        }
                
        // Private methods -----------------------------------------------------
        
        private void scan( Container container, int what, Preferences p ) {
            for (Component c : container.getComponents() ) {
                if (c instanceof JComponent ) {
                    JComponent jc = (JComponent)c;
                    Object o = jc.getClientProperty(OPTION_ID);
                    if ( o != null && o instanceof String ) {
                        switch( what ) {
                        case LOAD:
                            loadData( jc, (String)o );
                            break;
                        case STORE:
                            storeData( jc, (String)o, p );
                            break;
                        case ADD_LISTENERS:
                            addListener( jc );
                            break;
                        }
                    }                    
                }
                if ( c instanceof Container ) {
                    scan((Container)c, what, p);
                }
            }

        }

        /** Very smart method which tries to set the values in the components correctly
         */ 
        private void loadData( JComponent jc, String optionID ) {
            
            Preferences node = getPreferences(getCurrentProfileId());
            
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;                
                field.setText( node.get(optionID, getDefaultAsString(optionID)) );
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                boolean df = getDefaultAsBoolean(optionID);
                checkBox.setSelected( node.getBoolean(optionID, df));                
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                String value = node.get(optionID, getDefaultAsString(optionID) );
                ComboBoxModel model = createModel(value);
                cb.setModel(model);
                ComboItem item = whichItem(value, model);
                cb.setSelectedItem(item);
            }
            
        }
        
        private void storeData( JComponent jc, String optionID, Preferences p ) {
            
            Preferences node = p == null ? getPreferences(getCurrentProfileId()) : p;
            
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                
                String text = field.getText();
                
                if ( isInteger(optionID) ) {
                    try {
                        int i = Integer.parseInt(text);                        
                    } catch (NumberFormatException e) {
                        text = getLastValue(optionID);
                    }
                }
                
                
                // XXX test for numbers
                node.put(optionID, text);                
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                node.putBoolean(optionID, checkBox.isSelected());
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                // Logger.global.info( cb.getSelectedItem() + " " + optionID);
                node.put(optionID, ((ComboItem)cb.getSelectedItem()).value);
            }         
        }
        
        private void addListener( JComponent jc ) {
            if ( jc instanceof JTextField ) {
                JTextField field = (JTextField)jc;
                field.addActionListener(this);
                field.getDocument().addDocumentListener(this);
            }
            else if ( jc instanceof JCheckBox ) {
                JCheckBox checkBox = (JCheckBox)jc;
                checkBox.addActionListener(this);
            } 
            else if ( jc instanceof JComboBox) {
                JComboBox cb  = (JComboBox)jc;
                cb.addActionListener(this);
            }         
        }
        
            
        private ComboBoxModel createModel( String value ) {
            
            // is it braces placement?            
            for (ComboItem comboItem : bracePlacement) {
                if ( value.equals( comboItem.value) ) {
                    return new DefaultComboBoxModel( bracePlacement );
                }
            }
            
            // is it braces generation?
            for (ComboItem comboItem : bracesGeneration) {
                if ( value.equals( comboItem.value) ) {
                    return new DefaultComboBoxModel( bracesGeneration );
                }
            }
            
            // is it wrap
            for (ComboItem comboItem : wrap) {
                if ( value.equals( comboItem.value) ) {
                    return new DefaultComboBoxModel( wrap );
                }
            }
            
            return null;
        }
        
        private static ComboItem whichItem(String value, ComboBoxModel model) {
            
            for (int i = 0; i < model.getSize(); i++) {
                ComboItem item = (ComboItem)model.getElementAt(i);
                if ( value.equals(item.value)) {
                    return item;
                }
            }    
            return null;
        }
        
        private static class ComboItem {
            
            String value;
            String displayName;

            public ComboItem(String value, String key) {
                this.value = value;
                this.displayName = NbBundle.getMessage(FmtOptions.class, key);
            }

            @Override
            public String toString() {
                return displayName;
            }
            
        }
        
     
    }
   
    public static interface CodeStyleProducer {
        
        public CodeStyle create( Preferences preferences );
    
    }
    
}
