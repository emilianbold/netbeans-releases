/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.cplusplus;

import java.awt.Font;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.KeyStroke;
import java.util.Map;
import java.util.TreeMap;

import org.netbeans.editor.Acceptor;
import org.netbeans.editor.AcceptorFactory;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsDefaults;
import org.netbeans.editor.SettingsUtil;
import org.netbeans.editor.TokenCategory;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.editor.ext.ExtSettingsDefaults;

/** Default settings values for C and C++ */
public class CCSettingsDefaults extends ExtSettingsDefaults {

    public static final Boolean defaultCaretSimpleBracketMatching = Boolean.FALSE;
    public static final Boolean defaultHighlightMatchingBracket = Boolean.TRUE;
    public static final Boolean defaultJavaDocAutoPopup = Boolean.FALSE;
    public static final Boolean defaultPairCharactersCompletion = Boolean.TRUE;
    public static final Acceptor defaultIdentifierAcceptor = AcceptorFactory.JAVA_IDENTIFIER;
    
    // Formatting
    public static final Boolean defaultCCFormatSpaceBeforeParenthesis = Boolean.FALSE;
    public static final Boolean defaultCCFormatSpaceAfterComma = Boolean.TRUE;
    public static final Boolean defaultCCFormatNewlineBeforeBrace = Boolean.FALSE;
    public static final Boolean defaultCCFormatLeadingSpaceInComment = Boolean.FALSE;
    public static final Boolean defaultCCFormatLeadingStarInComment = Boolean.TRUE;
    public static final Integer defaultCCFormatStatementContinuationIndent = new Integer(8);
    public static final Boolean defaulCCtFormatPreprocessorAtLineStart = Boolean.FALSE;
    
    /** @deprecated */
    public static final Boolean defaultFormatSpaceBeforeParenthesis = defaultCCFormatSpaceBeforeParenthesis;
    /** @deprecated */
    public static final Boolean defaultFormatSpaceAfterComma = defaultCCFormatSpaceAfterComma;
    /** @deprecated */
    public static final Boolean defaultFormatNewlineBeforeBrace = defaultCCFormatNewlineBeforeBrace;
    /** @deprecated */
    public static final Boolean defaultFormatLeadingSpaceInComment = defaultCCFormatLeadingSpaceInComment;


    // Code Folding
    public static final Boolean defaultCodeFoldingEnable = Boolean.TRUE;

    public static final Acceptor defaultIndentHotCharsAcceptor = new Acceptor() {
            public boolean accept(char ch) {
                switch (ch) {
                case '{':
                case '}':
                    return true;
                }

                return false;
            }
        };


    public static final String defaultWordMatchStaticWords = 
            "Exception IntrospectionException FileNotFoundException IOException" //NOI18N
          + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" //NOI18N
          + " CloneNotSupportedException NullPointerException NumberFormatException" //NOI18N
          + " SQLException IllegalAccessException IllegalArgumentException"; //NOI18N

    /**
     * Initialize the abbreviations based on which kitClas (CKit or CCKit).
     *
     * @param kitClass Which langauge (CKit.class is for C and CCKit.class for C++)
     */
    public static Map getCCAbbrevMap(Class kitClass) {
        Map ccAbbrevMap = new TreeMap();
        
	ccAbbrevMap.put("def", "#define "); //NOI18N
	ccAbbrevMap.put("inc", "#include "); //NOI18N
	ccAbbrevMap.put("ifd", "#ifdef "); //NOI18N
	ccAbbrevMap.put("ifn", "#ifndef "); //NOI18N
	ccAbbrevMap.put("eif", "#endif"); //NOI18N
	ccAbbrevMap.put("pra", "#pragma "); //NOI18N

	ccAbbrevMap.put("ca", "case "); //NOI18N
	ccAbbrevMap.put("de", "default"); //NOI18N
	ccAbbrevMap.put("dou", "double "); //NOI18N
	ccAbbrevMap.put("en", "enum "); //NOI18N
	ccAbbrevMap.put("fl", "float "); //NOI18N
        ccAbbrevMap.put("fori", "for (int i = 0; i < |; i++) {\n}\n"); //NOI18N
        ccAbbrevMap.put("forj", "for (int j = 0; j < |; j++) {\n}\n"); //NOI18N
	ccAbbrevMap.put("ife", "if (|) {\n} else {\n}\n"); //NOI18N
	ccAbbrevMap.put("iff", "if (|) {\n}\n"); //NOI18N
	ccAbbrevMap.put("lo", "long "); //NOI18N
	ccAbbrevMap.put("sh", "short "); //NOI18N
	ccAbbrevMap.put("stu", "struct "); //NOI18N
	ccAbbrevMap.put("sw", "switch "); //NOI18N
	ccAbbrevMap.put("ty", "typedef "); //NOI18N
	ccAbbrevMap.put("uns", "unsigned "); //NOI18N
	ccAbbrevMap.put("uni", "union "); //NOI18N
	ccAbbrevMap.put("voi", "void "); //NOI18N
	ccAbbrevMap.put("wh", "while (|) {\n}\n"); //NOI18N
        
        if (kitClass == CCKit.class) {
            ccAbbrevMap.put("bo", "bool "); //NOI18N
            ccAbbrevMap.put("cl", "class "); //NOI18N
            ccAbbrevMap.put("co", "const "); //NOI18N
            ccAbbrevMap.put("fa", "false"); //NOI18N
            ccAbbrevMap.put("tr", "true"); //NOI18N
            ccAbbrevMap.put("wc", "wchar_t "); //NOI18N
        }

	return ccAbbrevMap;
    }

    public static MultiKeyBinding[] getCCKeyBindings() {
        int MENU_MASK = java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        
        return new MultiKeyBinding[] {
            new MultiKeyBinding(
                    KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK),
                    BaseKit.formatAction),
            new MultiKeyBinding(
                    KeyStroke.getKeyStroke(KeyEvent.VK_T, MENU_MASK | InputEvent.SHIFT_MASK),
                    ExtKit.commentAction),
            new MultiKeyBinding(
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, MENU_MASK | InputEvent.SHIFT_MASK),
                    ExtKit.uncommentAction)
        };
    }
              
    static class CCTokenColoringInitializer extends SettingsUtil.TokenColoringInitializer {

        Font boldFont = SettingsDefaults.defaultFont.deriveFont(Font.BOLD);
        Font italicFont = SettingsDefaults.defaultFont.deriveFont(Font.ITALIC);
        Settings.Evaluator boldSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.BOLD);
        Settings.Evaluator italicSubst = new SettingsUtil.FontStylePrintColoringEvaluator(Font.ITALIC);
        Settings.Evaluator lightGraySubst = new SettingsUtil.ForeColorPrintColoringEvaluator(new Color(120, 120, 120));

        Coloring commentColoring = new Coloring(italicFont, Coloring.FONT_MODE_APPLY_STYLE,
                            new Color(115, 115, 115), null);

        Coloring numbersColoring = new Coloring(null, new Color(120, 0, 0), null);

        public CCTokenColoringInitializer() {
            super(CCTokenContext.context);
        }

        public Object getTokenColoring(TokenContextPath tokenContextPath,
        TokenCategory tokenIDOrCategory, boolean printingSet) {
            if (!printingSet) {
                switch (tokenIDOrCategory.getNumericID()) {
                    case CCTokenContext.WHITESPACE_ID:
                    case CCTokenContext.IDENTIFIER_ID:
                    case CCTokenContext.OPERATORS_ID:
                        return SettingsDefaults.emptyColoring;

                    case CCTokenContext.SYS_INCLUDE_ID:
                        return new Coloring(null, Color.white, Color.red); // any non empty to be registered
                    case CCTokenContext.USR_INCLUDE_ID:
                        return new Coloring(null, Color.white, Color.red); // any non empty to be registered
                    case CCTokenContext.ERRORS_ID:
                        return new Coloring(null, Color.white, Color.red); // any non empty to be registered

                    case CCTokenContext.KEYWORDS_ID:
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                            new Color(0, 0, 153), null); // any non empty to be registered

                    case CCTokenContext.CPP_ID:
			// For preprocessor, use keyword coloring or
			// something else? For now, try character literal color!
                        return new Coloring(boldFont, Coloring.FONT_MODE_APPLY_STYLE,
                            new Color(0, 111, 0), null); // any non empty to be registered

                    case CCTokenContext.LINE_COMMENT_ID:
                    case CCTokenContext.BLOCK_COMMENT_ID:
                        return commentColoring; // any non empty to be registered

                    case CCTokenContext.CHAR_LITERAL_ID:
                        return new Coloring(null, new Color(0, 111, 0), null); // any non empty to be registered

                    case CCTokenContext.STRING_LITERAL_ID:
                        return new Coloring(null, new Color(153, 0, 107), null); // any non empty to be registered

                    case CCTokenContext.NUMERIC_LITERALS_ID:
                        return numbersColoring; // any non empty to be registered

                }

            } else { // printing set
                switch (tokenIDOrCategory.getNumericID()) {
                    case CCTokenContext.LINE_COMMENT_ID:
                    case CCTokenContext.BLOCK_COMMENT_ID:
			return lightGraySubst; // print fore color will be gray

                    default:
                         return SettingsUtil.defaultPrintColoringEvaluator;
                }

            }

            return null;
        }

    } // end class CCTokenColoringInitializer
}
