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
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.Formatter;
import org.netbeans.editor.Settings;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import org.netbeans.modules.cnd.editor.api.CodeStyle.PreprocessorIndent;
import org.openide.util.NbPreferences;

/**
 *
 * @author Alexander Simon
 */
public class EditorOptions {   
    public static CodeStyleFactory codeStyleFactory;
    static {
        Class c = CodeStyle.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //indents
    /**
     * How many spaces should be added to the statement that continues
     * on the next line.
     */
    public static final String statementContinuationIndent = "statementContinuationIndent"; // NOI18N 
    public static final int statementContinuationIndentDefault = 8;
    /**
     * Whether to indent preprocessors positioned at start of line.
     * Those not starting at column 0 of the line will automatically be indented.
     * This setting is to prevent C/C++ code that is compiled with compilers that
     * require the processors to have '#' in column 0.
     * <B>Note:</B>This will not convert formatted preprocessors back to column 0.
     */
    public static final String indentPreprocessorDirectives = "indentPreprocessorDirectives"; //NOI18N
    public static final String indentPreprocessorDirectivesDefault = PreprocessorIndent.START_LINE.name();
    public static final String sharpAtStartLine = "sharpAtStartLine"; //NOI18N
    public static final boolean sharpAtStartLineDefault = true;
    public static final String indentCasesFromSwitch = "indentCasesFromSwitch"; //NOI18N
    public static final boolean indentCasesFromSwitchDefault = true;

    //BracesPlacement
    public static final String newLineBeforeBraceNamespace = "newLineBeforeBraceNamespace"; //NOI18N
    public static final String newLineBeforeBraceNamespaceDefault = BracePlacement.NEW_LINE.name();
    public static final String newLineBeforeBraceClass = "newLineBeforeBraceClass"; //NOI18N
    public static final String newLineBeforeBraceClassDefault = BracePlacement.NEW_LINE.name();
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
    public static final String newLineBeforeBraceDeclaration = "newLineBeforeBraceDeclaration"; //NOI18N
    public static final String newLineBeforeBraceDeclarationDefault = BracePlacement.NEW_LINE.name();
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
    public static final String newLineBeforeBrace = "newLineBeforeBrace"; //NOI18N
    public static final String newLineBeforeBraceDefault = BracePlacement.SAME_LINE.name();

    //MultilineAlignment
    public static final String alignMultilineArrayInit = "alignMultilineArrayInit"; //NOI18N
    public static final boolean alignMultilineArrayInitDefault = false;
    public static final String alignMultilineCallArgs = "alignMultilineCallArgs"; //NOI18N
    public static final boolean alignMultilineCallArgsDefault = false;
    public static final String alignMultilineMethodParams = "alignMultilineMethodParams"; //NOI18N
    public static final boolean alignMultilineMethodParamsDefault = false;

    //NewLine
    public static final String newLineCatch = "newLineCatch"; //NOI18N
    public static final boolean newLineCatchDefault = false;
    public static final String newLineElse = "newLineElse"; //NOI18N
    public static final boolean newLineElseDefault = false;
    public static final String newLineWhile = "newLineWhile"; //NOI18N
    public static final boolean newLineWhileDefault = false;

    //SpacesBeforeKeywords
    public static final String spaceBeforeWhile = "spaceBeforeWhile"; //NOI18N
    public static final boolean spaceBeforeWhileDefault = true;
    public static final String spaceBeforeElse = "spaceBeforeElse"; //NOI18N
    public static final boolean spaceBeforeElseDefault = true;
    public static final String spaceBeforeCatch = "spaceBeforeCatch"; //NOI18N
    public static final boolean spaceBeforeCatchDefault = true;

    //SpacesBeforeParentheses
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
    
    //SpacesAroundOperators
    public static final String spaceAroundUnaryOps = "spaceAroundUnaryOps"; //NOI18N
    public static final boolean spaceAroundUnaryOpsDefault = false;
    public static final String spaceAroundBinaryOps = "spaceAroundBinaryOps"; //NOI18N
    public static final boolean spaceAroundBinaryOpsDefault = true;
    public static final String spaceAroundTernaryOps = "spaceAroundTernaryOps"; //NOI18N
    public static final boolean spaceAroundTernaryOpsDefault = true;
    public static final String spaceAroundAssignOps = "spaceAroundAssignOps"; //NOI18N
    public static final boolean spaceAroundAssignOpsDefault = true;
    
    //SpacesBeforeLeftBraces
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
    
    //SpacesWithinParentheses
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
    
    //SpacesOther
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
    
    //BlankLines
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

    //Other
    /** Whether the '*' should be added at the new line * in comment */
    public static final String addLeadingStarInComment = "addLeadingStarInComment"; // NOI18N
    public static final Boolean addLeadingStarInCommentDefault = true;
    

    private static final Preferences preferences = NbPreferences.forModule(EditorOptions.class);

    public static final String DEFAULT_PROFILE = "Default"; // NOI18N
    public static final String APACHE_PROFILE = "Apache"; // NOI18N

    private static Map<String,Object> defaults;
    private static Map<String,Map<String,Object>> namedDefaults;
    
    static Preferences lastValues;
    
    static {
        createDefaults();
    }
    
    private static void createDefaults() {
        defaults = new HashMap<String,Object>();
        // Indents
        defaults.put(statementContinuationIndent,statementContinuationIndentDefault);
        defaults.put(indentPreprocessorDirectives,indentPreprocessorDirectivesDefault);
        defaults.put(sharpAtStartLine, sharpAtStartLineDefault);
        defaults.put(indentCasesFromSwitch, indentCasesFromSwitchDefault);
        //BracesPlacement
        defaults.put(newLineBeforeBraceNamespace,newLineBeforeBraceNamespaceDefault);
        defaults.put(newLineBeforeBraceClass,newLineBeforeBraceClassDefault);
        defaults.put(newLineBeforeBraceDeclaration,newLineBeforeBraceDeclarationDefault);
        defaults.put(newLineBeforeBrace,newLineBeforeBraceDefault);
        //MultilineAlignment
        defaults.put(alignMultilineArrayInit,alignMultilineArrayInitDefault);
        defaults.put(alignMultilineCallArgs,alignMultilineCallArgsDefault);
        defaults.put(alignMultilineMethodParams,alignMultilineMethodParamsDefault);
        //NewLine
        defaults.put(newLineCatch,newLineCatchDefault);
        defaults.put(newLineElse,newLineElseDefault);
        defaults.put(newLineWhile,newLineWhileDefault);
        //SpacesBeforeKeywords
        defaults.put(spaceBeforeWhile,spaceBeforeWhileDefault);
        defaults.put(spaceBeforeElse,spaceBeforeElseDefault);
        defaults.put(spaceBeforeCatch,spaceBeforeCatchDefault);
        //SpacesBeforeParentheses
        defaults.put(spaceBeforeMethodDeclParen,spaceBeforeMethodDeclParenDefault);
        defaults.put(spaceBeforeMethodCallParen,spaceBeforeMethodCallParenDefault);
        defaults.put(spaceBeforeIfParen,spaceBeforeIfParenDefault);
        defaults.put(spaceBeforeForParen,spaceBeforeForParenDefault);
        defaults.put(spaceBeforeWhileParen,spaceBeforeWhileParenDefault);
        defaults.put(spaceBeforeCatchParen,spaceBeforeCatchParenDefault);
        defaults.put(spaceBeforeSwitchParen,spaceBeforeSwitchParenDefault);
        //SpacesAroundOperators
        defaults.put(spaceAroundUnaryOps,spaceAroundUnaryOpsDefault);
        defaults.put(spaceAroundBinaryOps,spaceAroundBinaryOpsDefault);
        defaults.put(spaceAroundTernaryOps,spaceAroundTernaryOpsDefault);
        defaults.put(spaceAroundAssignOps,spaceAroundAssignOpsDefault);
        //SpacesBeforeLeftBraces
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
        //SpacesWithinParentheses
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
        //SpacesOther
        defaults.put(spaceBeforeComma,spaceBeforeCommaDefault);
        defaults.put(spaceAfterComma,spaceAfterCommaDefault);
        defaults.put(spaceBeforeSemi,spaceBeforeSemiDefault);
        defaults.put(spaceAfterSemi,spaceAfterSemiDefault);
        defaults.put(spaceBeforeColon,spaceBeforeColonDefault);
        defaults.put(spaceAfterColon,spaceAfterColonDefault);
        defaults.put(spaceAfterTypeCast,spaceAfterTypeCastDefault);
        //BlankLines
        defaults.put(blankLinesBeforeClass,blankLinesBeforeClassDefault);
        defaults.put(blankLinesAfterClass,blankLinesAfterClassDefault);
        defaults.put(blankLinesAfterClassHeader,blankLinesAfterClassHeaderDefault);
        defaults.put(blankLinesBeforeFields,blankLinesBeforeFieldsDefault);
        defaults.put(blankLinesAfterFields,blankLinesAfterFieldsDefault);
        defaults.put(blankLinesBeforeMethods,blankLinesBeforeMethodsDefault);
        defaults.put(blankLinesAfterMethods,blankLinesAfterMethodsDefault);      
        //Other
        defaults.put(addLeadingStarInComment,addLeadingStarInCommentDefault);

        namedDefaults = new HashMap<String,Map<String,Object>>();
//Apache style
//This style can be generated with the following arguments to GNU indent:
//    -i4 -npsl -di0 -br -nce -d0 -cli0 -npcs -nfc1 -nut
//The Guidelines
//        * Opening braces are given on the same lines as statements,
//          or on the following line at the start of a function definition.
//        * Code inside a block (whether surrounded by braces or not) is indented by four space characters.
//          Tab characters are not used. Comments are indented to the same level as the surrounding code.
//        * Closing braces are always on a separate line from surrounding code,
//          and are indented to line up with the start of the text on the line containing the corresponding
//          opening brace.
//        * Functions are declared with ANSI-style arguments.
//        * There is no space between the function name and the opening bracket of the arguments to the function.
//          There is a single space following commas in argument lists and the semi-colons in for statements.
//        * Inside a switch() statement, the case keywords are indented to the same level as the switch line.
//        * Operators in expressions should be surrounded by a single space before and after,
//          except for unary increment (++), decrement (--), and negation (!) operators.
//        * There is no whitespace between a cast and the item modified (e.g., "(int)j" and not "(int) j").
//        * If a cast is to a pointer type, there is a space between the type and
//          the * character (e.g., "(char *)i" instead of "(char*)i")
        Map<String,Object> apache = new HashMap<String,Object>();
        namedDefaults.put(APACHE_PROFILE, apache);
        apache.put(indentCasesFromSwitch, false);
    }

    public static Object getDefault(CodeStyle.Language language, String styleId, String id){
        Map<String,Object> map = namedDefaults.get(styleId);
        if (map != null){
            Object res = map.get(id);
            if (res != null){
                return res;
            }
        }
        return defaults.get(id);
    }
    
    public static String getCurrentProfileId(CodeStyle.Language language) {
        switch(language){
            case C:
                return NbPreferences.forModule(CodeStyle.class).node("CodeStyle").get("C_Style", DEFAULT_PROFILE); // NOI18N
            case CPP:
            default:
                return NbPreferences.forModule(CodeStyle.class).node("CodeStyle").get("CPP_Style", DEFAULT_PROFILE); // NOI18N
        }
    }

    public static void setCurrentProfileId(CodeStyle.Language language, String style) {
        switch(language){
            case C:
                NbPreferences.forModule(CodeStyle.class).node("CodeStyle").put("C_Style", style); // NOI18N
                break;
            case CPP:
            default:
                NbPreferences.forModule(CodeStyle.class).node("CodeStyle").put("CPP_Style", style); // NOI18N
                break;
        }
    }

    public static Preferences getPreferences(CodeStyle.Language language, String profileId) {
        switch(language){
            case C:
                return NbPreferences.forModule(CodeStyle.class).node("C_CodeStyles").node(profileId); // NOI18N
            case CPP:
            default:
                return NbPreferences.forModule(CodeStyle.class).node("CPP_CodeStyles").node(profileId); // NOI18N
        }
    }


    public static int getGlobalIndentSize(CodeStyle.Language language) {
        Formatter f = (Formatter)Settings.getValue(getKitClass(language), "formatter"); // NOI18N
        if (f != null) {
            return f.getShiftWidth();
        }
        return 4;
    }
    
    private static Class<? extends EditorKit> cKitClass;
    private static Class<? extends EditorKit> cppKitClass;
    private static Class<? extends EditorKit> getKitClass(CodeStyle.Language language) {
        if (language == CodeStyle.Language.C) {
            if (cKitClass == null) {
                EditorKit kit = MimeLookup.getLookup(MimePath.get("text/x-c")).lookup(EditorKit.class); //NOI18N
                cKitClass = kit != null ? kit.getClass() : EditorKit.class;
            }
            return cKitClass;
        } else {
            if (cppKitClass == null) {
                EditorKit kit = MimeLookup.getLookup(MimePath.get("text/x-c++")).lookup(EditorKit.class); //NOI18N
                cppKitClass = kit != null ? kit.getClass() : EditorKit.class;
            }
            return cppKitClass;
        }
    }
    

    public static CodeStyle createCodeStyle(CodeStyle.Language language, Preferences p) {
        CodeStyle.getDefault(language);
        return codeStyleFactory.create(language, p);
    }

    public static Preferences getPreferences(CodeStyle codeStyle){
        return codeStyleFactory.getPreferences(codeStyle);
    }

    public static void setPreferences(CodeStyle codeStyle, Preferences preferences){
        codeStyleFactory.setPreferences(codeStyle, preferences);
    }
    
    public static interface CodeStyleFactory {
        CodeStyle create(CodeStyle.Language language, Preferences preferences);
        Preferences getPreferences(CodeStyle codeStyle);
        void setPreferences(CodeStyle codeStyle, Preferences preferences);
    }
}
