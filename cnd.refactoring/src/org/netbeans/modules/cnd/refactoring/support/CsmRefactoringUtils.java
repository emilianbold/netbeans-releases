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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.support;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.LanguagePath;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmRefactoringUtils {

    private CsmRefactoringUtils() {}
    
    public static CsmProject getContextCsmProject(CsmObject contextObject) {
        CsmFile contextFile = null;
        if (CsmKindUtilities.isOffsetable(contextObject)) {
            contextFile = ((CsmOffsetable)contextObject).getContainingFile();
        } else if (CsmKindUtilities.isFile(contextObject)) {
            contextFile = (CsmFile)contextObject;
        }
        CsmProject csmProject = null;
        if (contextFile != null) {
            csmProject = contextFile.getProject();
        } else if (CsmKindUtilities.isNamespace(contextObject)) {
            csmProject = ((CsmNamespace)contextObject).getProject();
        }
        return csmProject;
    }

    public static Collection<CsmProject> getRelatedCsmProjects(CsmObject origObject, boolean allProjects) {
        Collection<CsmProject> out = Collections.<CsmProject>emptyList();
        if (!allProjects) {
            CsmProject p = getContextCsmProject(origObject);
            out = Collections.singleton(p);
        } else {
            // for now return all...
            Collection<CsmProject> all = CsmModelAccessor.getModel().projects();
            out = all;
        }
        return out;
    }
    
    public static Project getContextProject(CsmObject contextObject) {
        CsmProject csmProject = getContextCsmProject(contextObject);
        Project out = null;
        if (csmProject != null) {
            Object o = csmProject.getPlatformProject();
            if (o instanceof NativeProject) {
                o = ((NativeProject)o).getProject();
            }                
            if (o instanceof Project) {
                out = (Project)o;
            }
        }
        
        return out;
    }
        
    public static CsmObject getReferencedElement(CsmObject csmObject) {
        if (csmObject instanceof CsmReference) {
            return getReferencedElement(((CsmReference)csmObject).getReferencedObject());
        } else {
            return csmObject;
        }
    } 
    
    public static String getSimpleText(CsmObject element) {
        String text = "";
        if (element != null) {
            if (CsmKindUtilities.isNamedElement(element)) {
                text = ((CsmNamedElement) element).getName().toString();
            } else if (CsmKindUtilities.isStatement((CsmObject)element)) {
                text = ((CsmStatement)element).getText().toString();
            } else if (CsmKindUtilities.isOffsetable(element) ) {
                text = ((CsmOffsetable)element).getText().toString();
            }
        }
        return text;
    }
    
    public static FileObject getFileObject(CsmObject object) {
        CsmFile container = null;
        if (CsmKindUtilities.isFile(object)) {
            container = (CsmFile)object;
        } else if (CsmKindUtilities.isOffsetable(object)) {
            container = ((CsmOffsetable)object).getContainingFile();
        }
        return container == null ? null : CsmUtilities.getFileObject(container);
    }

    public static CsmReference findReference(Lookup lookup) {
        CsmReference ref = lookup.lookup(CsmReference.class);
        if (ref == null) {
            Node node = lookup.lookup(Node.class);
            if (node != null) {
                ref = CsmReferenceResolver.getDefault().findReference(node);
            }
        }
        return ref;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> CsmUID<T> getHandler(T element) {
        CsmUID<T> uid = null;
        if (CsmKindUtilities.isIdentifiable(element)) {
            uid = ((CsmIdentifiable<T>)element).getUID();
            boolean checkAssert = false;
            assert checkAssert = true;
            if (checkAssert && (uid.getObject() == null)) {
                System.err.println("UID " + uid + "can't return object " + element);
                uid = null;
            }
        } 
        if (uid == null) {
            uid = new SelfUID(element);
        }
        return uid;
    }
    
    private static final class SelfUID<T> implements CsmUID<T> {
        private final T element;
        SelfUID(T element) {
            this.element = element;
        }
        public T getObject() {
            return this.element;
        }
    }
    
    public static <T> T getObject(CsmUID<T> handler) {
        return handler == null ? null : handler.getObject();
    }
    
    public static boolean isSupportedReference(CsmReference ref) {
        return ref != null;
    }    
    
    public static String getHtml(CsmObject obj) {
        if (CsmKindUtilities.isOffsetable(obj)) {
            return getHtml((CsmOffsetable)obj);
        } else if (CsmKindUtilities.isFile(obj)) {
            return htmlize(((CsmFile)obj).getName().toString());
        } else {
            return obj.toString();
        }
    }
    
    public static CsmObject getEnclosingElement(CsmObject decl) {
        assert decl != null;
        if (decl instanceof CsmReference) {
            decl = ((CsmReference)decl).getOwner();
        }
        if (CsmKindUtilities.isOffsetable(decl)) {
            return findInnerFileObject((CsmOffsetable)decl);
        }
        
        CsmObject scopeElem = decl instanceof CsmReference ? ((CsmReference)decl).getOwner() : decl;
        while (CsmKindUtilities.isScopeElement(scopeElem)) {
            CsmScope scope = ((CsmScopeElement)scopeElem).getScope();
            if (isLangContainerFeature(scope)) {
                return scope;
            } else if (CsmKindUtilities.isScopeElement(scope)) {
                scopeElem = ((CsmScopeElement)scope);
            } else {
                if (scope == null) System.err.println("scope element without scope " + scopeElem);
                break;
            }
        }
        if (CsmKindUtilities.isOffsetable(decl)) {
            return ((CsmOffsetable)decl).getContainingFile();
        }
        return null;
    }
    
    /*package*/ static boolean isLangContainerFeature(CsmObject obj) {
        assert obj != null;
        return CsmKindUtilities.isFunction(obj) ||
                    CsmKindUtilities.isClass(obj) ||
                    CsmKindUtilities.isEnum(obj) ||
                    CsmKindUtilities.isNamespaceDefinition(obj) ||
                    CsmKindUtilities.isFile(obj);
    }
    
    private static String getHtml(CsmOffsetable obj) {
        CsmFile csmFile = obj.getContainingFile();        
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        BaseDocument doc = null;
        String displayText = null;
        if (ces != null && (ces.getDocument() instanceof BaseDocument)) {
            doc = (BaseDocument)ces.getDocument();
            try {            
                int stOffset = obj.getStartOffset();
                int endOffset = obj.getEndOffset();
                int endLineOffset = 1;
                if (CsmKindUtilities.isNamespaceDefinition((CsmObject)obj) ||
                        CsmKindUtilities.isEnum((CsmObject)obj)) {
                    endOffset = stOffset;
                    endLineOffset = 0;
                } else if (CsmKindUtilities.isFunctionDefinition((CsmObject)obj)) {
                    endOffset = ((CsmFunctionDefinition)obj).getBody().getStartOffset()-1;
                } else if (CsmKindUtilities.isClass((CsmObject)obj)) {
                    endOffset = ((CsmClass)obj).getLeftBracketOffset()-1;
                }
                int startLine = org.netbeans.editor.Utilities.getRowFirstNonWhite(doc, stOffset);
                int endLine = org.netbeans.editor.Utilities.getRowLastNonWhite(doc, endOffset) + endLineOffset;
                displayText = CsmRefactoringUtils.getHtml(startLine, endLine, -1, -1, doc);
            } catch (BadLocationException ex) {
            }            
        }
        if (displayText == null) {
            displayText = htmlize(obj.getText().toString());
        }
        return displayText;
    }

    public static String getHtml(int startLine, int endLine, final int stToken, final int endToken, BaseDocument doc) throws BadLocationException {
        int startBold = stToken - startLine;
        int endBold = endToken - startLine;
        String content = doc.getText(startLine, endLine - startLine);

        String mime = (String) doc.getProperty("mimeType"); // NOI18N
        if (startBold >= 0 && endBold >= 0) {
            StringBuilder buf = new StringBuilder();
            buf.append(getHtml(mime, trimStart(content.substring(0, startBold))));
            buf.append("<b>"); //NOI18N
            buf.append(getHtml(mime, content.substring(startBold,endBold)));
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

    private static void appendHtml(StringBuilder buf, TokenSequence<?> ts) {
        FontColorSettings settings = null;
        LanguagePath languagePath = ts.languagePath();
        while (languagePath != null && settings == null) {
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
                    category = "whitespace"; //NOI18N
                }
                if (settings != null) {
                    AttributeSet set = settings.getTokenFontColors(category);
                    buf.append(color(htmlize(token.text().toString()), set));
                } else {
                    buf.append(token.text());
                }
            }
        }            
    }
    
    public static String htmlize(String input) {
        String temp = org.openide.util.Utilities.replaceString(input, "<", "&lt;"); // NOI18N
        temp = org.openide.util.Utilities.replaceString(temp, ">", "&gt;"); // NOI18N
        return temp;
    }
    
    private static String color(String string, AttributeSet set) {
        if (set==null)
            return string;
        if (string.trim().length() == 0) {
            return org.openide.util.Utilities.replaceString(org.openide.util.Utilities.replaceString(string, " ", "&nbsp;"), "\n", "<br>"); //NOI18N
        } 
        StringBuilder buf = new StringBuilder(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0,"<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0,"<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0,"<s>"); // NOI18N
            buf.append("</s>"); // NOI18N
        }
        buf.insert(0,"<font color=" + getHTMLColor(StyleConstants.getForeground(set)) + ">"); //NOI18N
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
    
    ////////////////////////////////////////////////////////////////////////////
    // by-offset methods
    
    private static boolean isInObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if ((offs.getStartOffset() <= offset) &&
                (offset <= offs.getEndOffset())) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isBeforeObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if (offset < offs.getStartOffset()) {
            return true;
        } else {
            return false;
        }
    }
    
    public static CsmObject findInnerFileObject(CsmFile file, int offset) {
        assert (file != null) : "can't be null file in findInnerFileObject";
        // check file declarations
        CsmObject lastObject = findInnerDeclaration(file.getDeclarations().iterator(), offset);
//        // check macros if needed
//        lastObject = lastObject != null ? lastObject : findObject(file.getMacros(), context, offset);
        return lastObject;
    }
    
    private static CsmDeclaration findInnerDeclaration(final Iterator<? extends CsmDeclaration> it, final int offset) {
        CsmDeclaration innerDecl = null;
        if (it != null) {
            // continue till has next and not yet found
            while (it.hasNext()) {
                CsmDeclaration decl = (CsmDeclaration) it.next();
                assert (decl != null) : "can't be null declaration";
                if (isInObject(decl, offset) && isLangContainerFeature(decl)) {
                    // we are inside declaration, but try to search deeper
                    innerDecl = findInnerDeclaration(decl, offset);
                    if (innerDecl != null) {
                        return innerDecl;
                    } else {
                        return decl;
                    }
                } else if (isBeforeObject(decl, offset)) {
                    break;
                }
            }
        }
        return innerDecl;
    }
        
    // must check before call, that offset is inside outDecl
    private static CsmDeclaration findInnerDeclaration(CsmDeclaration outDecl, int offset) {
        assert (isInObject(outDecl, offset)) : "must be in outDecl object!";
        Iterator<? extends CsmDeclaration> it = null;
        if (CsmKindUtilities.isNamespace(outDecl)) { 
            CsmNamespace ns = (CsmNamespace)outDecl;
            it = ns.getDeclarations().iterator();
        } else if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl  = (CsmClass)outDecl;
            it = cl.getMembers().iterator();
        } else if (CsmKindUtilities.isEnum(outDecl)) {
            CsmEnum en = (CsmEnum)outDecl;
            it = en.getEnumerators().iterator();
        }
        return findInnerDeclaration(it, offset);
    }      
    
    private static CsmObject findInnerFileObject(CsmOffsetable csmOffsetable) {
        CsmObject obj = findInnerFileObject(csmOffsetable.getContainingFile(), csmOffsetable.getStartOffset()-1);
        if (obj == null) {
            obj = csmOffsetable.getContainingFile();
        }
        return obj;
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
        for (int x = s.length()-1; x >=0; x--) {
            if (Character.isWhitespace(s.charAt(x))) {
                continue;
            } else {
                return s.substring(0, x + 1);
            }
        }
        return "";
    }    
}
