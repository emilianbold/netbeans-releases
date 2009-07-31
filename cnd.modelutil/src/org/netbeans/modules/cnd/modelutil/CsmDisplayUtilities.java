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
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmDisplayUtilities {

    private CsmDisplayUtilities() {
    }

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

    public static String getContextLine(CsmFile csmFile, final int stToken, final int endToken) {
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
                int startLine = Utilities.getRowFirstNonWhite(doc, stToken);
                int endLine = Utilities.getRowLastNonWhite(doc, endToken) + 1;
                displayText = doc.getText(startLine, endLine - startLine);
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

    public static CharSequence getTooltipText(CsmObject item) {
        CharSequence tooltipText = null;
        if (CsmKindUtilities.isMethod(item)) {
            CharSequence functionDisplayName = getFunctionText((CsmFunction)item);
            CsmClass methodDeclaringClass = ((CsmMember) item).getContainingClass();
            CharSequence displayClassName = methodDeclaringClass.getQualifiedName();
            String key = "DSC_MethodTooltip";  // NOI18N
            if (CsmKindUtilities.isConstructor(item)) {
                key = "DSC_ConstructorTooltip";  // NOI18N
            } else if (CsmKindUtilities.isDestructor(item)) {
                key = "DSC_DestructorTooltip";  // NOI18N
            }
            tooltipText = getHtmlizedString(key, functionDisplayName, displayClassName);
        } else if (CsmKindUtilities.isFunction(item)) {
            CharSequence functionDisplayName = getFunctionText((CsmFunction)item);
            tooltipText = getHtmlizedString("DSC_FunctionTooltip", functionDisplayName); // NOI18N
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
            tooltipText = getHtmlizedString(key, ((CsmClassifier) item).getQualifiedName());
        } else if (CsmKindUtilities.isTypedef(item)) {
            CharSequence tdName = ((CsmTypedef) item).getQualifiedName();
            tooltipText = getHtmlizedString("DSC_TypedefTooltip", tdName, ((CsmTypedef) item).getText()); // NOI18N
        } else if (CsmKindUtilities.isEnum(item)) {
            tooltipText = getHtmlizedString("DSC_EnumTooltip", ((CsmEnum) item).getQualifiedName()); // NOI18N
        } else if (CsmKindUtilities.isEnumerator(item)) {
            CsmEnumerator enmtr = ((CsmEnumerator) item);
            tooltipText = getHtmlizedString("DSC_EnumeratorTooltip", enmtr.getName(), enmtr.getEnumeration().getQualifiedName()); // NOI18N
        } else if (CsmKindUtilities.isField(item)) {
            CharSequence fieldName = ((CsmField) item).getName();
            CsmClass containingClass = ((CsmField) item).getContainingClass();
            CharSequence displayClassName = containingClass.getQualifiedName();
            CharSequence classKind = "class";//NOI18N
            if (containingClass.getKind() == CsmDeclaration.Kind.STRUCT) {
                classKind = "struct"; //NOI18N
            } else if (containingClass.getKind() == CsmDeclaration.Kind.UNION) {
                classKind = "union"; // NOI18N
            }
            tooltipText = getHtmlizedString("DSC_FieldTooltip", fieldName, classKind, displayClassName, ((CsmField) item).getText()); // NOI18N
        } else if (CsmKindUtilities.isParamVariable(item)) {
            CharSequence varName = ((CsmParameter) item).getName();
            tooltipText = getHtmlizedString("DSC_ParameterTooltip", varName, ((CsmParameter) item).getText()); // NOI18N
        } else if (CsmKindUtilities.isVariable(item)) {
            CharSequence varName = ((CsmVariable) item).getName();
            tooltipText = getHtmlizedString("DSC_VariableTooltip", varName, ((CsmVariable) item).getText()); // NOI18N
        } else if (CsmKindUtilities.isFile(item)) {
            CharSequence fileName = ((CsmFile) item).getName();
            tooltipText = getHtmlizedString("DSC_FileTooltip", fileName); // NOI18N
        } else if (CsmKindUtilities.isNamespace(item)) {
            CharSequence nsName = ((CsmNamespace) item).getQualifiedName();
            tooltipText = getHtmlizedString("DSC_NamespaceTooltip", nsName); // NOI18N
        } else if (CsmKindUtilities.isMacro(item)) {
            CsmMacro macro = (CsmMacro)item;
            switch (macro.getKind()){
                case DEFINED:
                    tooltipText = getHtmlizedString("DSC_MacroTooltip", macro.getName(), macro.getText()); // NOI18N
                    break;
                case COMPILER_PREDEFINED:
                    tooltipText = getHtmlizedString("DSC_SysMacroTooltip", macro.getName(), macro.getText()); // NOI18N
                    break;
                case POSITION_PREDEFINED:
                    tooltipText = getHtmlizedString("DSC_PosMacroTooltip", macro.getName(), macro.getText()); // NOI18N
                    break;
                case USER_SPECIFIED:
                    tooltipText = getHtmlizedString("DSC_ProjectMacroTooltip", macro.getName(), macro.getText()); // NOI18N
                    break;
                default:
                    throw new IllegalArgumentException("unexpected macro kind:" + macro.getKind() + " in macro:" + macro); // NOI18N
            }
        } else if (CsmKindUtilities.isInclude(item)) {
            CsmInclude incl = (CsmInclude)item;
            CsmFile target = incl.getIncludeFile();
            if (target == null) {
                tooltipText = getHtmlizedString("DSC_IncludeErrorTooltip", incl.getText());  // NOI18N
            } else {
                if (target.getProject().isArtificial()) {
                    tooltipText = getHtmlizedString("DSC_IncludeLibraryTooltip", target.getAbsolutePath());// NOI18N
                } else {
                    tooltipText = getHtmlizedString("DSC_IncludeTooltip", target.getAbsolutePath(), target.getProject().getName());  // NOI18N
                }
            }
        } else if (CsmKindUtilities.isQualified(item)) {
            tooltipText = ((CsmQualifiedNamedElement) item).getQualifiedName();
        } else if (CsmKindUtilities.isLabel(item)) {
            tooltipText = getHtmlizedString("DSC_LabelTooltip", ((CsmLabel)item).getLabel());  // NOI18N
        } else if (CsmKindUtilities.isNamedElement(item)) {
            tooltipText = ((CsmNamedElement)item).getName();
        } else {
            tooltipText = "unhandled object " + item;  // NOI18N
        }
        return tooltipText;
    }

    private static CharSequence getFunctionText(CsmFunction fun) {
        StringBuilder txt = new StringBuilder();
        if (CsmKindUtilities.isMethod(fun) && ((CsmMethod)fun).isVirtual()) {
            txt.append("virtual "); // NOI18N
        }
        txt.append(fun.getReturnType().getText()).append(' '); // NOI18N
        // NOI18N
        txt.append(fun.getName());
        txt.append('(');
        @SuppressWarnings("unchecked")
        Iterator<CsmParameter> params = fun.getParameters().iterator();
        while(params.hasNext()) {
            CsmParameter param = params.next();
            txt.append(param.getText());
            if (params.hasNext()) {
                txt.append(", "); // NOI18N
            }
        }
        txt.append(')');
        if (CsmKindUtilities.isMethod(fun)) {
            CsmMethod mtd = (CsmMethod)fun;
            if (mtd.isConst()) {
                txt.append(" const"); // NOI18N
            }
            if (mtd.isAbstract()) {
                txt.append(" = 0"); // NOI18N
            }
        }
        return txt.toString();
    }

    private static String getHtmlizedString(String key, CharSequence value) {
        return getString(key, htmlize(value));
    }

    private static String getHtmlizedString(String key, CharSequence value1, CharSequence value2) {
        return getString(key, htmlize(value1), htmlize(value2));
    }

    private static String getHtmlizedString(String key, CharSequence value1, CharSequence value2, CharSequence value3, CharSequence value4) {
        return getString(key, htmlize(value1), htmlize(value2), htmlize(value3), htmlize(value4));
    }

    private static String getString(String key, CharSequence value) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, value);
    }    
    
    private static String getString(String key, CharSequence value1, CharSequence value2) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, value1, value2);
    } 
    
    private static String getString(String key, CharSequence value1, CharSequence value2, CharSequence value3, CharSequence value4) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, new Object[] {value1, value2, value3, value4});
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

    public static String htmlize(CharSequence input) {
        if (input == null) {
            System.err.println("null string");// NOI18N
            return "";// NOI18N
        }
        String temp = input.toString().replace("&", "&amp;");// NOI18N
        temp = temp.replace("<", "&lt;"); // NOI18N
        temp = temp.replace(">", "&gt;"); // NOI18N
        return temp;
    }

    public static String addHTMLColor(String string, AttributeSet set) {
        if (set == null) {
            return string;
        }
        if (string.trim().length() == 0) {
            return string.replace(" ", "&nbsp;").replace("\n", "<br>"); //NOI18N
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
