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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.support;

import java.awt.Color;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.FontColorSettings;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmRefactoringUtils {
    private CsmRefactoringUtils() {}
    
    static FileObject getFileObject(CsmObject object) {
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
        if (CsmKindUtilities.isIdentifiable(element)) {
            return ((CsmIdentifiable<T>)element).getUID();
        } else {
            return new SelfUID(element);
        }
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
            return htmlize(((CsmFile)obj).getName());
        } else {
            return obj.toString();
        }
    }
    
    public static CsmScope getEnclosingScopeElement(CsmObject decl) {
        assert decl != null;
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
    
    private static boolean isLangContainerFeature(CsmObject obj) {
        assert obj != null;
        return CsmKindUtilities.isFunction(obj) ||
                    CsmKindUtilities.isClass(obj) ||
                    CsmKindUtilities.isEnum(obj) ||
                    CsmKindUtilities.isNamespaceDefinition(obj) ||
                    CsmKindUtilities.isFile(obj);
    }
    
    private static CsmObject getEnclosingFeature(CsmFile file, int offset) {
        CsmObject out = null;
        if (offset > 0) {
            
        }
        return out;
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
                int startLine = org.netbeans.editor.Utilities.getRowFirstNonWhite(doc, stOffset);
                int endLine = org.netbeans.editor.Utilities.getRowLastNonWhite(doc, endOffset)+1;
                displayText = CsmRefactoringUtils.getHtml(startLine, endLine, -1, -1, doc);
            } catch (BadLocationException ex) {
            }            
        }
        if (displayText == null) {
            displayText = htmlize(obj.getText());
        }
        return displayText;
    }
    
    public static String getHtml(int startLine, int endLine, final int stToken, final int endToken, BaseDocument doc) {
        final StringBuffer buf = new StringBuffer();
//        TokenHierarchy tokenH = TokenHierarchy.create(text, JavaTokenId.language());
        String mime = (String) doc.getProperty("mimeType"); // NOI18N
        Lookup lookup = MimeLookup.getLookup(MimePath.get(mime));
        SyntaxSupport sup = doc.getSyntaxSupport();
        final FontColorSettings settings = lookup.lookup(FontColorSettings.class);
//        TokenSequence tok = tokenH.tokenSequence();
//        while (tok.moveNext()) {
//            Token<JavaTokenId> token = (Token) tok.token();
//            String category = token.id().primaryCategory();
//            if (category == null) {
//                category = "whitespace"; //NOI18N
//            }
//            AttributeSet set = settings.getTokenFontColors(category);
//            String text = token.text().toString();
//            buf.append(color(htmlize(text)), set));
//        }
        boolean cont = true;
 
        
        TokenProcessor tp = new TokenProcessor() {

            private int bufferStartPos;
            private char[] buffer;

            public boolean token(TokenID tokenID, TokenContextPath tokenContextPath, int tokenBufferOffset, int tokenLength) {
                String text = new String(buffer, tokenBufferOffset-bufferStartPos, tokenLength);
                String category = tokenID.getCategory() == null ? tokenID.getName() : tokenID.getCategory().getName();
                if (category == null) {
                    category = "whitespace"; //NOI18N
                } else {
                    category = tokenContextPath.getNamePrefix() + category;
                }
                AttributeSet set = settings.getTokenFontColors(category);
                if (tokenBufferOffset+bufferStartPos == stToken) {
                    buf.append("<b>");
                }
                buf.append(color(htmlize(text), set));
                if (tokenBufferOffset+bufferStartPos+tokenLength == endToken) {
                    buf.append("</b>");
                }
                return true;
            }

            public int eot(int offset) {
                return 0;
            }

            public void nextBuffer(char[] buffer, int offset, int len, int startPos, int preScan, boolean lastBuffer) {
                this.buffer = buffer;
                this.bufferStartPos = startPos - offset;
            }

        };  
        while (cont) {
            try {
                sup.tokenizeText(tp, startLine, endLine, true); 
                cont = false;
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        return buf.toString();
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
        StringBuffer buf = new StringBuffer(string);
        if (StyleConstants.isBold(set)) {
            buf.insert(0,"<b>"); //NOI18N
            buf.append("</b>"); //NOI18N
        }
        if (StyleConstants.isItalic(set)) {
            buf.insert(0,"<i>"); //NOI18N
            buf.append("</i>"); //NOI18N
        }
        if (StyleConstants.isStrikeThrough(set)) {
            buf.insert(0,"<s>");
            buf.append("</s>");
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
    
    public static boolean isInObject(CsmObject obj, int offset) {
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
    
    // list is ordered by offsettable elements
    public static <T extends CsmObject> T findObject(List<T> list, int offset) {
        assert (list != null) : "expect not null list";
        for (Iterator<T> it = list.iterator(); it.hasNext();) {
            T obj = it.next();
            assert (obj != null) : "can't be null declaration";
            if (isInObject((CsmObject)obj, offset)) {
                // we are inside csm element
                return obj;
            }
        }
        return null;
    }   
    
//    public static CsmObject findInnerFileObject(CsmFile file, int offset, CsmContext context) {
//        assert (file != null) : "can't be null file in findInnerFileObject";
//        // check file declarations
//        CsmObject lastObject = findInnerDeclaration(file.getDeclarations().iterator(), context, offset);
//        // check includes if needed
//        lastObject = lastObject != null ? lastObject : findObject(file.getIncludes(), context, offset);
//        // check macros if needed
//        lastObject = lastObject != null ? lastObject : findObject(file.getMacros(), context, offset);
//        return lastObject;
//    }
//    
//    private static CsmDeclaration findInnerDeclaration(final Iterator<? extends CsmDeclaration> it, final int offset) {
//        CsmDeclaration innerDecl = null;
//        if (it != null) {
//            // continue till has next and not yet found
//            while (it.hasNext()) {
//                CsmDeclaration decl = (CsmDeclaration) it.next();
//                assert (decl != null) : "can't be null declaration";
//                if (isInObject(decl, offset)) {
//                    if (!CsmKindUtilities.isFunction(decl) || isInFunctionScope((CsmFunction)decl, offset)) {
//                        // add declaration scope to context
//                        CsmContextUtilities.updateContext(decl, offset, context);
//                        // we are inside declaration, but try to search deeper
//                        innerDecl = findInnerDeclaration(decl, offset, context);
//                    } else {
//                        context.setLastObject(decl);
//                    }
//                    innerDecl = innerDecl != null ? innerDecl : decl;
//                    // we can break loop, because list of declarations is sorted
//                    // by offset and we found already one of container declaration
//                    break;
//                }
//            }
//        }
//        return innerDecl;
//    }
//        
//    // must check before call, that offset is inside outDecl
//    private static CsmDeclaration findInnerDeclaration(CsmDeclaration outDecl, int offset, CsmContext context) {
//        assert (isInObject(outDecl, offset)) : "must be in outDecl object!";
//        Iterator<? extends CsmDeclaration> it = null;
//        if (CsmKindUtilities.isNamespace(outDecl)) { 
//            CsmNamespace ns = (CsmNamespace)outDecl;
//            it = ns.getDeclarations().iterator();
//        } else if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
//            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
//        } else if (CsmKindUtilities.isClass(outDecl)) {
//            CsmClass cl  = (CsmClass)outDecl;
//            List list = cl.getMembers();
//            if (cl.getFriends().size() > 0) {
//                // combine friends with members for search
//                list.addAll(cl.getFriends());
//            }
//            it = list.iterator();
//        } else if (CsmKindUtilities.isEnum(outDecl)) {
//            CsmEnum en = (CsmEnum)outDecl;
//            it = en.getEnumerators().iterator();
//        }
//        return findInnerDeclaration(it, context, offset);
//    }      
    
    
}
