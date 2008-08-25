/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.cnd.modelutil;

import java.awt.Color;
import java.io.IOException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmDisplayUtilities {
    
    public static String getContextLineHtml(CsmFile csmFile, final int stToken, final int endToken, boolean tokenInBold) {
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        StyledDocument stDoc = null;
        try {
            stDoc = ces.openDocument();
        } catch (IOException iOException) {
            // skip
        }

        String displayText = null;
        if (stDoc instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument) stDoc;
            try {
                int stOffset = stToken;
                int endOffset = endToken;
                int startLine = Utilities.getRowFirstNonWhite(doc, stOffset);
                int endLine = Utilities.getRowLastNonWhite(doc, endOffset) + 1;
                if (!tokenInBold) {
                    stOffset = -1;
                    endOffset = -1;
                }
                displayText = getLineHtml(startLine, endLine, stOffset, endOffset, doc);
            } catch (BadLocationException ex) {
                // skip
            }
        }
        return displayText;
    }
    
    public static String getLineHtml(int startLine, int endLine, final int stToken, final int endToken, BaseDocument doc) throws BadLocationException {
        int startBold = stToken - startLine;
        int endBold = endToken - startLine;
        String content = doc.getText(startLine, endLine - startLine);

        String mime = (String) doc.getProperty("mimeType"); // NOI18N
        if (startBold >= 0 && endBold >= 0) {
            StringBuilder buf = new StringBuilder();
            buf.append(getHtml(mime, trimStart(content.substring(0, startBold))));
            buf.append("<b>"); //NOI18N
            buf.append(getHtml(mime, content.substring(startBold, endBold)));
            buf.append("</b>");//NOI18N
            buf.append(getHtml(mime, trimEnd(content.substring(endBold))));
            return buf.toString();
        } else {
            return getHtml(mime, content);
        }
    }

    public static String getHtml(String mime, String content) {
        final StringBuilder buf = new StringBuilder();
        Language<CppTokenId> lang = CndLexerUtilities.getLanguage(mime);
        if (lang == null) {
            return content;
        }
        TokenHierarchy tokenH = TokenHierarchy.create(content, lang);
        TokenSequence<?> tok = tokenH.tokenSequence();
        appendHtml(buf, tok);
        return buf.toString();
    }

    public static String getTooltipText(CsmOffsetable item) {
        String displayName = null;
        if (CsmKindUtilities.isNamedElement(item)) {
            displayName = ((CsmNamedElement) item).getName().toString();
        }
        if (displayName != null) {
            displayName = CsmDisplayUtilities.htmlize(displayName);
        }
        if (CsmKindUtilities.isClassMember(item)) {
            if (displayName == null) {
                displayName = "";
            }
            CsmClass cls = ((CsmMember) item).getContainingClass();
            if (cls != null && cls.getName().length() > 0) {
                String className = CsmDisplayUtilities.htmlize(cls.getName().toString());
                displayName = NbBundle.getMessage(CsmDisplayUtilities.class, "MSG_ITEM_TOOLTIP2", displayName, className); //NOI18N
            }
        } else if (CsmKindUtilities.isMacro(item)) {
            CsmMacro macro = (CsmMacro)item;
            if (macro.isSystem()) {
                displayName = NbBundle.getMessage(CsmDisplayUtilities.class, "DISPLAY_TEXT_SYS_MACRO", macro.getBody()); //NOI18N
            } else {
                displayName = NbBundle.getMessage(CsmDisplayUtilities.class, "DISPLAY_TEXT_USR_MACRO", macro.getBody()); //NOI18N
            }
        }
        return displayName;
    }
    
    private final static boolean SKIP_COLORING = Boolean.getBoolean("cnd.test.skip.coloring");

    private static void appendHtml(StringBuilder buf, TokenSequence<?> ts) {
        FontColorSettings settings = null;
        LanguagePath languagePath = ts.languagePath();
        while (!SKIP_COLORING && languagePath != null && settings == null) {
            String mime = languagePath.mimePath();
            Lookup lookup = MimeLookup.getLookup(mime);
            settings = lookup.lookup(FontColorSettings.class);
        }
        while (ts.moveNext()) {
            Token<?> token = ts.token();
            TokenSequence<?> es = ts.embedded();
            if (es != null && es.language() == CppTokenId.languagePreproc()) {
                appendHtml(buf, es);
            } else {
                String category = token.id().primaryCategory();
                if (category == null) {
                    category = CppTokenId.WHITESPACE_CATEGORY; //NOI18N
                }
                String text;
                if (CppTokenId.WHITESPACE_CATEGORY.equals(category)) {
                    // whitespace
                    text = " "; // NOI18N
                } else {
                    text = token.text().toString();
                }
                if (settings != null) {
                    AttributeSet set = settings.getTokenFontColors(category);
                    buf.append(color(htmlize(text), set));
                } else {
                    buf.append(htmlize(text));
                }
            }
        }
    }

    public static String htmlize(String input) {
        String temp = org.openide.util.Utilities.replaceString(input, "&", "&amp;");
        temp = org.openide.util.Utilities.replaceString(temp, "<", "&lt;"); // NOI18N
        temp = org.openide.util.Utilities.replaceString(temp, ">", "&gt;"); // NOI18N
        return temp;
    }

    private static String color(String string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return org.openide.util.Utilities.replaceString(org.openide.util.Utilities.replaceString(string, " ", "&nbsp;"), "\n", "<br>"); //NOI18N
        }
        StringBuilder buf = new StringBuilder(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0, "<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0, "<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0, "<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
        }
        buf.insert(0, "<font color=" + getHTMLColor(StyleConstants.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf.toString();
    }

    private static String getHTMLColor(Color c) {
        String colorR = "0" + Integer.toHexString(c.getRed()); //NOI18N
        colorR = colorR.substring(colorR.length() - 2);
        String colorG = "0" + Integer.toHexString(c.getGreen()); //NOI18N
        colorG = colorG.substring(colorG.length() - 2);
        String colorB = "0" + Integer.toHexString(c.getBlue()); //NOI18N
        colorB = colorB.substring(colorB.length() - 2);
        String html_color = "#" + colorR + colorG + colorB; //NOI18N
        return html_color;
    }
    
    private static String trimStart(String s) {
        for (int x = 0; x < s.length(); x++) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(x, s.length());
            }
        }
        return "";
    }

    private static String trimEnd(String s) {
        for (int x = s.length() - 1; x >= 0; x--) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(0, x + 1);
            }
        }
        return "";
    }    
}
