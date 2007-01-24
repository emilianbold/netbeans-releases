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

package org.netbeans.editor.ext.java;

import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.awt.Font;
import java.awt.Color;
import javax.swing.KeyStroke;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;
import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.ext.ExtSettingsDefaults;
import org.netbeans.editor.ext.ExtKit;

/**
* Default settings values for Java.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaSettingsDefaults extends ExtSettingsDefaults {

    public static final Boolean defaultCaretSimpleMatchBrace = Boolean.FALSE;
    public static final Boolean defaultHighlightMatchingBracket = Boolean.TRUE;

    public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.JAVA_IDENTIFIER;
    public static final Acceptor defaultAbbrevResetAcceptor = AcceptorFactory.NON_JAVA_IDENTIFIER;
    public static final Boolean defaultWordMatchMatchCase = Boolean.TRUE;

    // Formatting
    public static final Boolean defaultJavaFormatSpaceBeforeParenthesis = Boolean.FALSE;
    public static final Boolean defaultJavaFormatSpaceAfterComma = Boolean.TRUE;
    public static final Boolean defaultJavaFormatNewlineBeforeBrace = Boolean.FALSE;
    public static final Boolean defaultJavaFormatLeadingSpaceInComment = Boolean.FALSE;
    public static final Boolean defaultJavaFormatLeadingStarInComment = Boolean.TRUE;
    public static final Integer defaultJavaFormatStatementContinuationIndent = new Integer(8);

    public static final Boolean defaultPairCharactersCompletion = Boolean.TRUE;

    /** @deprecated */
    public static final Boolean defaultFormatSpaceBeforeParenthesis = defaultJavaFormatSpaceBeforeParenthesis;
    /** @deprecated */
    public static final Boolean defaultFormatSpaceAfterComma = defaultJavaFormatSpaceAfterComma;
    /** @deprecated */
    public static final Boolean defaultFormatNewlineBeforeBrace = defaultJavaFormatNewlineBeforeBrace;
    /** @deprecated */
    public static final Boolean defaultFormatLeadingSpaceInComment = defaultJavaFormatLeadingSpaceInComment;
    
    public static final Boolean defaultCodeFoldingEnable = Boolean.TRUE;
    public static final Boolean defaultCodeFoldingCollapseMethod = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseInnerClass = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseImport = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseJavadoc = Boolean.FALSE;
    public static final Boolean defaultCodeFoldingCollapseInitialComment = Boolean.FALSE;

    public static final Boolean defaultGotoClassCaseSensitive = Boolean.FALSE;
    public static final Boolean defaultGotoClassShowInnerClasses = Boolean.FALSE;
    public static final Boolean defaultGotoClassShowLibraryClasses = Boolean.TRUE;


    public static final Acceptor defaultIndentHotCharsAcceptor
        = new Acceptor() {
            public boolean accept(char ch) {
                switch (ch) {
                    case '{':
                    case '}':
                        return true;
                }

                return false;
            }
        };


    public static final String defaultWordMatchStaticWords
    = "Exception IntrospectionException FileNotFoundException IOException" // NOI18N
      + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" // NOI18N
      + " CloneNotSupportedException NullPointerException NumberFormatException" // NOI18N
      + " SQLException IllegalAccessException IllegalArgumentException"; // NOI18N

    public static Map getJavaAbbrevMap() {
        Map javaAbbrevMap = new TreeMap();
        javaAbbrevMap.put("sout", "System.out.println(\"|\");"); // NOI18N
        javaAbbrevMap.put("serr", "System.err.println(\"|\");"); // NOI18N

        javaAbbrevMap.put("psf", "private static final "); // NOI18N
        javaAbbrevMap.put("psfi", "private static final int "); // NOI18N
        javaAbbrevMap.put("psfs", "private static final String "); // NOI18N
        javaAbbrevMap.put("psfb", "private static final boolean "); // NOI18N
        javaAbbrevMap.put("Psf", "public static final "); // NOI18N
        javaAbbrevMap.put("Psfi", "public static final int "); // NOI18N
        javaAbbrevMap.put("Psfs", "public static final String "); // NOI18N
        javaAbbrevMap.put("Psfb", "public static final boolean "); // NOI18N


        javaAbbrevMap.put("ab", "abstract "); // NOI18N
        javaAbbrevMap.put("bo", "boolean "); // NOI18N
        javaAbbrevMap.put("br", "break"); // NOI18N
        javaAbbrevMap.put("ca", "catch ("); // NOI18N
        javaAbbrevMap.put("cl", "class "); // NOI18N
        javaAbbrevMap.put("cn", "continue"); // NOI18N
        javaAbbrevMap.put("df", "default:"); // NOI18N
        javaAbbrevMap.put("ex", "extends "); // NOI18N
        javaAbbrevMap.put("fa", "false"); // NOI18N
        javaAbbrevMap.put("fi", "final "); // NOI18N
        javaAbbrevMap.put("fl", "float "); // NOI18N
        javaAbbrevMap.put("fy", "finally "); // NOI18N
        javaAbbrevMap.put("im", "implements "); // NOI18N
        javaAbbrevMap.put("ir", "import "); // NOI18N
        javaAbbrevMap.put("iof", "instanceof "); // NOI18N
        javaAbbrevMap.put("ie", "interface "); // NOI18N
        javaAbbrevMap.put("pr", "private "); // NOI18N
        javaAbbrevMap.put("pe", "protected "); // NOI18N
        javaAbbrevMap.put("pu", "public "); // NOI18N
        javaAbbrevMap.put("re", "return "); // NOI18N
        javaAbbrevMap.put("st", "static "); // NOI18N
        javaAbbrevMap.put("sw", "switch ("); // NOI18N
        javaAbbrevMap.put("sy", "synchronized "); // NOI18N
        javaAbbrevMap.put("th", "throws "); // NOI18N
        javaAbbrevMap.put("tw", "throw "); // NOI18N
        javaAbbrevMap.put("twn", "throw new "); // NOI18N
        javaAbbrevMap.put("wh", "while ("); // NOI18N

        javaAbbrevMap.put("eq", "equals"); // NOI18N
        javaAbbrevMap.put("le", "length"); // NOI18N

        javaAbbrevMap.put("En", "Enumeration"); // NOI18N
        javaAbbrevMap.put("Ex", "Exception"); // NOI18N
        javaAbbrevMap.put("Ob", "Object"); // NOI18N
        javaAbbrevMap.put("St", "String"); // NOI18N

        javaAbbrevMap.put("pst", "printStackTrace();"); // NOI18N
        javaAbbrevMap.put("tds", "Thread.dumpStack();"); // NOI18N

        //Code templates
        
        javaAbbrevMap.put("fori", 
                "for (int ${IDX newVarName default=\"idx\"} = 0; ${IDX} < ${ARR array default=\"arr\"}.length; ${IDX}++) {\n"
                +    "${TYPE rightSideType default=\"Object\"} ${ELEM newVarName default=\"elem\"} = ${TYPE_CAST cast default=\"\" editable=false}${ARR}[${IDX}];\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("forc",
                "for (${IT_TYPE rightSideType type=\"java.util.Iterator\" default=\"Iterator\" editable=false} ${IT newVarName default=\"it\"} = ${COL instanceof=\"java.util.Collection\" default=\"col\"}.iterator(); ${IT}.hasNext();) {\n"
                +    "${TYPE rightSideType default=\"Object\"} ${ELEM newVarName default=\"elem\"} = ${TYPE_CAST cast default=\"\" editable=false}${IT}.next();\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("forl",
                "for (int ${IDX newVarName default=\"idx\"} = 0; ${IDX} < ${LIST instanceof=\"java.util.List\" default=\"lst\"}.size(); ${IDX}++) {\n"
                +    "${TYPE rightSideType default=\"Object\"} ${ELEM newVarName default=\"elem\"} = ${TYPE_CAST cast default=\"\" editable=false}${LIST}.get(${IDX});\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("forv",
                "for (int ${IDX newVarName default=\"idx\"} = 0; ${IDX} < ${VECTOR instanceof=\"java.util.Vector\" default=\"vct\"}.size(); ${IDX}++) {\n"
                +    "${TYPE rightSideType default=\"Object\"} ${ELEM newVarName default=\"elem\"} = ${TYPE_CAST cast default=\"\" editable=false}${VECTOR}.elementAt(${IDX});\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("fore",
                "for (${TYPE iterableElementType default=\"Object\" editable=false} ${ELEM newVarName default=\"elem\"} : ${ITER iterable default=\"col\"}) {\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("forst",
                "for (${STR_TOK type=\"java.util.StringTokenizer\" default=\"StringTokenizer\" editable=false} ${TOKENIZER newVarName} = new ${STR_TOK}(${STRING instanceof=\"java.lang.String\"}); ${TOKENIZER}.hasMoreTokens();) {\n"
                +     "String ${TOKEN default=\"token\"} = ${TOKENIZER}.nextToken();\n"
                +     "${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("inst",
                "if (${EXP instanceof=\"java.lang.Object\" default=\"exp\"} instanceof ${TYPE default=\"Object\"}) {\n"
                +    "${TYPE} ${VAR newVarName default=\"obj\"} = (${TYPE})${EXP};\n"
                +    "${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("soutv", "System.out.println(\"${EXP instanceof=\"<any>\" default=\"exp\"} = \" + ${EXP});");
        javaAbbrevMap.put("whilen",
                "while(${ENUM instanceof=\"java.util.Enumeration\" default=\"en\"}.hasMoreElements()) {\n"
                +    "${TYPE rightSideType default=\"Object\"} ${ELEM newVarName default=\"elem\"} = ${TYPE_CAST cast default=\"\" editable=false} ${ENUM}.nextElement();\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("whileit",
                "while(${IT instanceof=\"java.util.Iterator\" default=\"it\"}.hasNext()) {\n"
                +    "${TYPE rightSideType default=\"Object\"} ${ELEM newVarName default=\"elem\"} = ${TYPE_CAST cast default=\"\" editable=false} ${IT}.next();\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("iff",
                "if (${EXP instanceof=\"java.lang.Boolean\" default=\"exp\"}) {\n"
                +    "${selection line}${cursor}\n"
                + "}\n"
        );
        javaAbbrevMap.put("ifelse",
                "if (${EXP instanceof=\"java.lang.Boolean\" default=\"exp\"}) {\n"
                +    "${selection line}${cursor}\n"
                + "} else {\n"
                + "}\n"
        );
        javaAbbrevMap.put("whilexp", // NOI18N
                "while (${EXP instanceof=\"java.lang.Boolean\" default=\"exp\"}) {\n" // NOI18N
                +    "${selection line}${cursor}\n" // NOI18N
                + "}\n"
        );
        javaAbbrevMap.put("dowhile", // NOI18N
                "do {\n" // NOI18N
                +    "${selection line}${cursor}\n" // NOI18N
                + "} while (${EXP instanceof=\"java.lang.Boolean\" default=\"exp\"});\n" // NOI18N
        );
        javaAbbrevMap.put("runn",
                "${RUNN_TYPE type=\"java.lang.Runnable\" default=\"Runnable\" editable=false} ${RUNN newVarName default=\"r\"} = new ${RUNN_TYPE}() {\n"
                +    "public void run() {\n"
                +        "${selection line}${cursor}\n"
                +    "}\n"
                + "};\n"
        );
        javaAbbrevMap.put("trycatch",
                "try {\n"
                +    "${selection line}${cursor}\n"
                + "} catch (${EX_TYPE default=\"Exception\"} ${EX default=\"e\"}) {\n"
                + "}\n"
         );
        javaAbbrevMap.put("newo", // NOI18N
                "$TYPE default=\"Object\"} ${OBJ newVarName default=\"obj\"} = new ${TYPE}(${cursor});" // NOI18N
        );        
        javaAbbrevMap.put("psvm", // NOI18N
                "public static void main(String[] args) {\n" // NOI18N
                +    "${cursor}\n" // NOI18N
                + "}\n" //NOI18N
        );
        return javaAbbrevMap;
    }

    public static MultiKeyBinding[] getJavaKeyBindings() {
        int MENU_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
        return new MultiKeyBinding[] {
                   new MultiKeyBinding(
                       new KeyStroke[] {
                           KeyStroke.getKeyStroke(KeyEvent.VK_J, MENU_MASK),
                           KeyStroke.getKeyStroke(KeyEvent.VK_D, 0)
                       },
                       "macro-debug-var" // NOI18N
                   ),
                   new MultiKeyBinding(
                       KeyStroke.getKeyStroke(KeyEvent.VK_T, MENU_MASK | InputEvent.SHIFT_MASK),
                       ExtKit.commentAction
                   ),
                  new MultiKeyBinding(
                      KeyStroke.getKeyStroke(KeyEvent.VK_D, MENU_MASK | InputEvent.SHIFT_MASK),
                      ExtKit.uncommentAction
                  ),
                  new MultiKeyBinding(
                      new KeyStroke[] {
                          KeyStroke.getKeyStroke(KeyEvent.VK_R, MENU_MASK),
                      },
                      "in-place-refactoring"
                  ),
               };
    }
    
    public static Map getJavaMacroMap() {
        Map javaMacroMap = new HashMap();
        javaMacroMap.put( "debug-var", "select-identifier copy-to-clipboard " + // NOI18N
                "caret-up caret-end-line insert-break \"System.err.println(\\\"\"" + 
                "paste-from-clipboard \" = \\\" + \" paste-from-clipboard \" );" ); // NOI18N
        
        return javaMacroMap;
    }

    static class JavaTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
        Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
        Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
        Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(new Color(120, 120, 120));

        Coloring commentColoring = new Coloring(null, new Color(115, 115, 115), null);

        Coloring numbersColoring = new Coloring(null, new Color(120, 0, 0), null);

        public JavaTokenColoringInitializer() {
            super(JavaTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {
            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case JavaTokenContext.WHITESPACE_ID:
                    case JavaTokenContext.IDENTIFIER_ID:
                    case JavaTokenContext.OPERATORS_ID:
                        return SettingsDefaults.emptyColoring;

                    case JavaTokenContext.ERRORS_ID:
                        return new Coloring(null, Color.white, Color.red);

                    case JavaTokenContext.KEYWORDS_ID:
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                            new Color(0, 0, 153), null);


                    case JavaTokenContext.LINE_COMMENT_ID:
                    case JavaTokenContext.BLOCK_COMMENT_ID:
                        return commentColoring;

                    case JavaTokenContext.CHAR_LITERAL_ID:
                        return new Coloring(null, new Color(0, 111, 0), null);

                    case JavaTokenContext.STRING_LITERAL_ID:
                        return new Coloring(null, new Color(153, 0, 107), null);

                    case JavaTokenContext.NUMERIC_LITERALS_ID:
                        return numbersColoring;

                    case JavaTokenContext.ANNOTATION_ID: // JDK 1.5 annotations
                        return new Coloring(null, new Color(0, 111, 0), null);

                }

            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {
                    case JavaTokenContext.LINE_COMMENT_ID:
                    case JavaTokenContext.BLOCK_COMMENT_ID:
                         return lightGraySubst; // print fore color will be gray

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;

        }

    }

    static class JavaLayerTokenColoringInitializer
    extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);

        public JavaLayerTokenColoringInitializer() {
            super(JavaLayerTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {
            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case JavaLayerTokenContext.METHOD_ID:
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                            null, null);

                }

            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {
                    case JavaLayerTokenContext.METHOD_ID:
                        return italicSubst;

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;
        }

    }

}
