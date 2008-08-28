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
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
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

    public static String getTooltipText(CsmObject item) {
        CharSequence tooltipText = null;
        if (CsmKindUtilities.isMethod(item)) {
            String functionDisplayName = htmlize(((CsmMethod) item).getSignature());
            CsmClass methodDeclaringClass = ((CsmMember) item).getContainingClass();
            String displayClassName = methodDeclaringClass.getName().toString();
            String key = "DSC_MethodTooltip";  // NOI18N
            if (CsmKindUtilities.isConstructor(item)) {
                key = "DSC_ConstructorTooltip";  // NOI18N
            } else if (CsmKindUtilities.isDestructor(item)) {
                key = "DSC_DestructorTooltip";  // NOI18N
            }
            tooltipText = getString(key, functionDisplayName, displayClassName);
        } else if (CsmKindUtilities.isFunction(item)) {
            String functionFQN = htmlize(((CsmFunction) item).getSignature());
            functionFQN = CsmDisplayUtilities.htmlize(functionFQN);
            tooltipText = getString("DSC_FunctionTooltip", functionFQN); // NOI18N
        } else if (CsmKindUtilities.isClass(item)) {
            CsmDeclaration.Kind classKind = ((CsmDeclaration) item).getKind();
            String key;
            if (classKind == CsmDeclaration.Kind.STRUCT) {
                key = "DSC_StructTooltip"; // NOI18N
            } else if (classKind == CsmDeclaration.Kind.UNION) {
                key = "DSC_UnionTooltip"; // NOI18N
            } else {
                key = "DSC_ClassTooltip"; // NOI18N
            }
            tooltipText = getString(key, ((CsmClassifier) item).getQualifiedName().toString());
        } else if (CsmKindUtilities.isTypedef(item)) {
            String tdName = ((CsmTypedef) item).getQualifiedName().toString();
            tooltipText = getString("DSC_TypedefTooltip", tdName, htmlize(((CsmTypedef) item).getText())); // NOI18N
        } else if (CsmKindUtilities.isEnum(item)) {
            tooltipText = getString("DSC_EnumTooltip", ((CsmEnum) item).getQualifiedName().toString()); // NOI18N
        } else if (CsmKindUtilities.isEnumerator(item)) {
            CsmEnumerator enmtr = ((CsmEnumerator) item);
            tooltipText = getString("DSC_EnumeratorTooltip", enmtr.getName().toString(), enmtr.getEnumeration().getName().toString()); // NOI18N
        } else if (CsmKindUtilities.isField(item)) {
            String fieldName = ((CsmField) item).getName().toString();
            String displayClassName = ((CsmField) item).getContainingClass().getName().toString();
            tooltipText = getString("DSC_FieldTooltip", fieldName, displayClassName, htmlize(((CsmField) item).getText())); // NOI18N
        } else if (CsmKindUtilities.isParamVariable(item)) {
            String varName = ((CsmParameter) item).getName().toString();
            tooltipText = getString("DSC_ParameterTooltip", varName, htmlize(((CsmParameter) item).getText())); // NOI18N
        } else if (CsmKindUtilities.isVariable(item)) {
            String varName = ((CsmVariable) item).getName().toString();
            tooltipText = getString("DSC_VariableTooltip", varName, htmlize(((CsmVariable) item).getText())); // NOI18N
        } else if (CsmKindUtilities.isFile(item)) {
            String fileName = ((CsmFile) item).getName().toString();
            tooltipText = getString("DSC_FileTooltip", fileName); // NOI18N
        } else if (CsmKindUtilities.isNamespace(item)) {
            String nsName = ((CsmNamespace) item).getQualifiedName().toString();
            tooltipText = getString("DSC_NamespaceTooltip", nsName); // NOI18N
        } else if (CsmKindUtilities.isMacro(item)) {
            CsmMacro macro = (CsmMacro)item;
            tooltipText = getString(macro.isSystem() ? "DSC_SysMacroTooltip" : "DSC_UsrMacroTooltip", macro.getName(), htmlize(macro.getText())); // NOI18N
        } else if (CsmKindUtilities.isInclude(item)) {
            CsmInclude incl = (CsmInclude)item;
            CsmFile target = incl.getIncludeFile();
            if (target == null) {
                tooltipText = getString("DSC_IncludeErrorTooltip", htmlize(incl.getText()));  // NOI18N
            } else {
                if (target.getProject().isArtificial()) {
                    tooltipText = getString("DSC_IncludeLibraryTooltip", target.getAbsolutePath());// NOI18N
                } else {
                    tooltipText = getString("DSC_IncludeTooltip", target.getAbsolutePath(), target.getProject().getName());  // NOI18N
                }
            }
        } else if (CsmKindUtilities.isQualified(item)) {
            tooltipText = ((CsmQualifiedNamedElement) item).getQualifiedName().toString();
        } else if (CsmKindUtilities.isNamedElement(item)) {
            tooltipText = ((CsmNamedElement)item).getName();
        } else {
            tooltipText = "unhandled object " + item;  // NOI18N
        }
        return tooltipText != null ? tooltipText.toString() : null;
    }
    
    private static String getString(String key, CharSequence value) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, value);
    }    
    
    private static String getString(String key, CharSequence value1, CharSequence value2) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, value1, value2);
    } 
    
    private static String getString(String key, CharSequence value1, CharSequence value2, CharSequence value3) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, value1, value2, value3);
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

    public static String htmlize(CharSequence input) {
        if (input == null) {
            System.err.println("null string");
            return "";
        }
        String temp = org.openide.util.Utilities.replaceString(input.toString(), "&", "&amp;");
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
