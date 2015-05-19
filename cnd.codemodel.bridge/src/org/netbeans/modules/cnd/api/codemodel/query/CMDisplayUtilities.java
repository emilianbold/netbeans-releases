/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.codemodel.query;

import java.awt.Color;
import java.io.File;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.cnd.api.codemodel.CMCursor;
import org.netbeans.modules.cnd.api.codemodel.visit.CMDeclarationContext;
import org.netbeans.modules.cnd.api.codemodel.visit.CMEntity;
import org.netbeans.modules.cnd.api.codemodel.visit.CMReference;
import org.netbeans.swing.plaf.LFCustoms;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Kvashin
 */
public class CMDisplayUtilities {

    public static CharSequence getContextLineHtml(CMReference ref, boolean refNameInBold) {
//        return ref.getDisplayName();
        int stToken = ref.getRange().getStart().getOffset();
        int endToken = ref.getRange().getEnd().getOffset();
        CloneableEditorSupport ces = CMUtilities.findCloneableEditorSupport(ref);
        CharSequence out = CMDisplayUtilities.getContextLineHtml(ces, stToken, endToken, refNameInBold);
        if (out == null) {
            out = CMModelStubs.getReferenceText(ref);
        }
        return out;

//        CsmFile csmFile = ref.getContainingFile();
//        int stToken = ref.getStartOffset();
//        int endToken = ref.getEndOffset();
//        CharSequence out = CMDisplayUtilities.getContextLineHtml(csmFile, stToken, endToken, refNameInBold);
//        if (out == null) {
//            out = ref.getText();
//        }
//        return out;
    }

    public static CharSequence getContextLineHtml(CMCursor ref, boolean refNameInBold) {
//        return ref.getDisplayName();
        int stToken = ref.getExtent().getStart().getOffset();
        int endToken = ref.getExtent().getEnd().getOffset();
        CloneableEditorSupport ces = CMUtilities.findCloneableEditorSupport(ref);
        CharSequence out = CMDisplayUtilities.getContextLineHtml(ces, stToken, endToken, refNameInBold);
        if (out == null) {
            out = ref.getDisplayName();
        }
        return out;

//        CsmFile csmFile = ref.getContainingFile();
//        int stToken = ref.getStartOffset();
//        int endToken = ref.getEndOffset();
//        CharSequence out = CMDisplayUtilities.getContextLineHtml(csmFile, stToken, endToken, refNameInBold);
//        if (out == null) {
//            out = ref.getText();
//        }
//        return out;
    }

    public static CharSequence getContextLine(CMCursor ref) {
        return ref.getDisplayName();
//        CMFile csmFile = ref.getLocation().getFile();
//        int stToken = CMUtilities.getStartOffset(ref);
//        int endToken = CMUtilities.getEndOffset(ref);
//        CharSequence out = CMDisplayUtilities.getContextLine(csmFile, stToken, endToken);
//        if (out == null) {
//            out = ref.getDText();
//        }
//        return out;
    }

    public static CharSequence getContextLineHtml(CloneableEditorSupport ces, final int stToken, final int endToken, boolean tokenInBold) {
        StyledDocument stDoc = CMUtilities.openDocument(ces);
        CharSequence displayText = null;
        if (stDoc instanceof LineDocument) {
            LineDocument doc = (LineDocument) stDoc;
            try {
                int stOffset = stToken;
                int endOffset = endToken;
                int startLine = LineDocumentUtils.getLineFirstNonWhitespace(doc, stOffset);
                int endLine = LineDocumentUtils.getLineLastNonWhitespace(doc, endOffset) + 1;
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

    public static CharSequence getLineHtml(int startLine, int endLine, final int stToken, final int endToken, LineDocument doc) throws BadLocationException {
        int startBold = stToken - startLine;
        int endBold = endToken - startLine;
        String content = doc.getText(startLine, endLine - startLine);
        String mime = DocumentUtilities.getMimeType(doc);
        if (startBold >= 0 && endBold >= 0 && startBold <= content.length() && endBold <= content.length()  && startBold < endBold) {
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

    public static CharSequence getHtml(CMDeclarationContext declContext) {
        CloneableEditorSupport ces = CMUtilities.findCloneableEditorSupport(declContext.getRange());
        LineDocument doc = null;
        CharSequence displayText = null;
        if (ces != null) {
            Document d = CMUtilities.openDocument(ces);
            if (d instanceof LineDocument) {
                doc = (LineDocument) d;
            }
        }
        if (doc != null) {
            try {
                int stOffset = declContext.getRange().getStart().getOffset();
                int endOffset = declContext.getRange().getEnd().getOffset();
                int endLineOffset = 1;
                CMEntity referencedEntity = declContext.getDeclarationEntity();
                if (CMKindUtilities.isNamespace(referencedEntity) ||
                        CMKindUtilities.isEnum(referencedEntity)) {
                    endOffset = stOffset;
                    endLineOffset = 0;
                } else if (CMKindUtilities.isFunctionDefinition(referencedEntity)) {
                    //TODO: implement: endOffset = ((CsmFunctionDefinition)obj).getBody().getStartOffset()-1;
                } else if (CMKindUtilities.isClass(referencedEntity)) {
                    //TODO: implement: endOffset = ((CsmClass)obj).getLeftBracketOffset()-1;
                }
                int startLine = LineDocumentUtils.getLineFirstNonWhitespace(doc, stOffset);
                int endLine = LineDocumentUtils.getLineLastNonWhitespace(doc, endOffset) + endLineOffset;
                displayText = CMDisplayUtilities.getLineHtml(startLine, endLine, -1, -1, doc);
            } catch (BadLocationException ex) {
            }
        }
        if (displayText == null) {
            //displayText = CMDisplayUtilities.htmlize(obj.getText().toString());
            displayText = CMDisplayUtilities.htmlize(CMModelStubs.getText(declContext.getRange()));
        }
        return displayText;
    }

    public static CharSequence getHtml(CMReference obj) {
        CloneableEditorSupport ces = CMUtilities.findCloneableEditorSupport(obj);
        LineDocument doc = null;
        CharSequence displayText = null;
        if (ces != null) {
            Document d = CMUtilities.openDocument(ces);
            if (d instanceof LineDocument) {
                doc = (LineDocument) d;
            }
        }
        if (doc != null) {
            try {
                int stOffset = CMUtilities.getStartOffset(obj);
                int endOffset = CMUtilities.getEndOffset(obj);
                int endLineOffset = 1;
                CMEntity referencedEntity = obj.getReferencedEntity();
                if (CMKindUtilities.isNamespace(referencedEntity) ||
                        CMKindUtilities.isEnum(referencedEntity)) {
                    endOffset = stOffset;
                    endLineOffset = 0;
                } else if (CMKindUtilities.isFunctionDefinition(referencedEntity)) {
                    //TODO: implement: endOffset = ((CsmFunctionDefinition)obj).getBody().getStartOffset()-1;
                } else if (CMKindUtilities.isClass(referencedEntity)) {
                    //TODO: implement: endOffset = ((CsmClass)obj).getLeftBracketOffset()-1;
                }
                int startLine = LineDocumentUtils.getLineFirstNonWhitespace(doc, stOffset);
                int endLine = LineDocumentUtils.getLineLastNonWhitespace(doc, endOffset) + endLineOffset;
                displayText = CMDisplayUtilities.getLineHtml(startLine, endLine, -1, -1, doc);
            } catch (BadLocationException ex) {
            }
        }
        if (displayText == null) {
            //displayText = CMDisplayUtilities.htmlize(obj.getText().toString());
            displayText = CMDisplayUtilities.htmlize(CMModelStubs.getReferenceText(obj));
        }
        return displayText;
    }

    public static CharSequence getHtml(String mime, String content) {
        final StringBuilder buf = new StringBuilder();
        Language<CppTokenId> lang = CndLexerUtilities.getLanguage(mime);
        if (lang == null) {
            return content;
        }
        TokenHierarchy<?> tokenH = TokenHierarchy.create(content, lang);
        TokenSequence<?> tok = tokenH.tokenSequence();
        appendHtml(buf, tok);
        return buf;
    }

    private final static boolean SKIP_COLORING = Boolean.getBoolean("cnd.test.skip.coloring");// NOI18N
   
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
                    buf.append(addHTMLColor(htmlize(text), set));
                } else {
                    buf.append(htmlize(text));
                }
            }
        }
    }

    public static CharSequence htmlize(CharSequence input) {
        if (input == null) {
            System.err.println("null string");// NOI18N
            return "";// NOI18N
        }
        String temp = input.toString().replace("&", "&amp;");// NOI18N
        temp = temp.replace("<", "&lt;"); // NOI18N
        temp = temp.replace(">", "&gt;"); // NOI18N
        return temp;
    }

    public static CharSequence addHTMLColor(CharSequence string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        {
            String t = string.toString();
            if (t.trim().length() == 0) {
                return t.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
            }
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
        buf.insert(0, "<font color=" + getHTMLColor(LFCustoms.getForeground(set)) + ">"); //NOI18N
        buf.append("</font>"); //NOI18N
        return buf;
    }

    public static CharSequence getHTMLColor(Color c) {
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

    public static String shrinkPath(CharSequence path, int maxDisplayedDirLen, int nrDisplayedFrontDirs, int nrDisplayedTrailingDirs) {
        return shrinkPath(path, true, File.separator, maxDisplayedDirLen, nrDisplayedFrontDirs, nrDisplayedTrailingDirs);
    }

    public static String shrinkPath(CharSequence path, boolean shrink, String separator, int maxDisplayedDirLen, int nrDisplayedFrontDirs, int nrDisplayedTrailingDirs) {
        final String SLASH = "/"; //NOI18N
        StringBuilder builder = new StringBuilder(path);
        String toReplace = null;
        if (SLASH.equals(separator)) {
            if (builder.indexOf("\\") >= 0) { // NOI18N
                toReplace = "\\"; // NOI18N
            }
        } else {
            if (builder.indexOf(SLASH) >= 0) {
                toReplace = SLASH;
            }
        }
        if (toReplace != null) {
            // replace all "/" or "\" to system separator
            builder = new StringBuilder(builder.toString().replace(toReplace, separator));
        }
        int len = builder.length();
        if (shrink && len > maxDisplayedDirLen) {

            StringBuilder reverse = new StringBuilder(builder).reverse();
            int st = builder.indexOf(separator);
            if (st < 0) {
                st = 0;
            } else {
                st++;
            }
            int end = 0;
            while (reverse.charAt(end) == separator.charAt(0)) {
                end++;
            }
            int firstSlash = nrDisplayedFrontDirs > 0 ? Integer.MAX_VALUE : -1;
            for (int i = nrDisplayedFrontDirs; i > 0 && firstSlash > 0; i--) {
                firstSlash = builder.indexOf(separator, st);
                st = firstSlash + 1;
            }
            int lastSlash = nrDisplayedTrailingDirs > 0 ? Integer.MAX_VALUE : -1;
            for (int i = nrDisplayedTrailingDirs; i > 0 && lastSlash > 0; i--) {
                lastSlash = reverse.indexOf(separator, end);
                end = lastSlash + 1;
            }
            if (lastSlash > 0 && firstSlash > 0) {
                lastSlash = len - lastSlash;
                if (firstSlash < lastSlash - 1) {
                    builder.replace(firstSlash, lastSlash - 1, "..."); // NOI18N
                }
            }
        }
        return builder.toString(); // NOI18N
    }
}
