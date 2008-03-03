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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.File;
import java.io.IOException;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver.Scope;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.completion.cplusplus.utils.TokenUtilities;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.UserQuestionException;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ReferencesSupport {

    private ReferencesSupport() {
    }
    
    /**
     * converts (line, col) into offset. Line and column info are 1-based, so 
     * the start of document is (1,1)
     */
    public static int getDocumentOffset(BaseDocument doc, int lineIndex, int colIndex) {
        return Utilities.getRowStartFromLineOffset(doc, lineIndex -1) + (colIndex - 1);
    }

    public static BaseDocument getBaseDocument(final String absPath) throws DataObjectNotFoundException, IOException {
        File file = new File(absPath);
        // convert file into file object
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null) {
            return null;
        }
        DataObject dataObject = DataObject.find(fileObject);
        EditorCookie  cookie = (EditorCookie)dataObject.getCookie(EditorCookie.class);
        if (cookie == null) {
            throw new IllegalStateException("Given file (\"" + dataObject.getName() + "\") does not have EditorCookie."); // NOI18N
        }
        
        StyledDocument doc = null;
        try {
            doc = cookie.openDocument();
        } catch (UserQuestionException ex) {
            ex.confirmed();
            doc = cookie.openDocument();
        }
        
        return doc instanceof BaseDocument ? (BaseDocument)doc : null;
    }
    
    public static CsmObject findReferencedObject(CsmFile csmFile, BaseDocument doc, int offset) {
        return findReferencedObject(csmFile, doc, offset, null);
    }
    
    /*static*/ static CsmObject findOwnerObject(CsmFile csmFile, BaseDocument baseDocument, int offset, Token token) {
        CsmObject csmOwner = CsmOffsetResolver.findObject(csmFile, offset);
        return csmOwner;
    }
    
    /*package*/ static CsmObject findReferencedObject(CsmFile csmFile, BaseDocument doc, int offset, Token jumpToken) {
        CsmObject csmItem = null;
        // emulate hyperlinks order
        // first ask includes handler if offset in include sring token  
        CsmInclude incl = null;
        jumpToken = jumpToken != null ? jumpToken : getTokenByOffset(doc, offset);
        if (jumpToken != null) {
            switch (jumpToken.getTokenID().getNumericID()) {
                case CCTokenContext.SYS_INCLUDE_ID:
                case CCTokenContext.USR_INCLUDE_ID:
                    // look for include directive
                    incl = findInclude(csmFile, offset);
                    break;
            }
        }
        
        csmItem = incl == null ? null : incl.getIncludeFile();

        // if failed => ask declarations handler
        if (csmItem == null) {
            csmItem = findDeclaration(csmFile, doc, jumpToken, offset);
        }
        return csmItem;
    }   
    
    public static CsmInclude findInclude(CsmFile csmFile, int offset) {
        assert (csmFile != null);
        return CsmOffsetUtilities.findObject(csmFile.getIncludes(), null, offset);
    }    

    public static CsmObject findDeclaration(final CsmFile csmFile, final BaseDocument doc, 
            Token tokenUnderOffset, final int offset) {
        // fast check, if possible
        SyntaxSupport sup = doc.getSyntaxSupport();
        int[] idFunBlk = null;
        CsmObject csmItem = null;
        try { 
            idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock(doc, offset);
        } catch (BadLocationException ex) {
            // skip it
        }
        // check but not for function call
        if (idFunBlk != null && idFunBlk.length != 3) {
            csmItem = findDeclaration(csmFile, doc, tokenUnderOffset, offset, true);
        }
        // then full check if needed
        csmItem = csmItem != null ? csmItem : findDeclaration(csmFile, doc, tokenUnderOffset, offset, false);
        return csmItem;
    }
    
    public static CsmObject findDeclaration(final CsmFile csmFile, final BaseDocument doc, 
            Token tokenUnderOffset, final int offset, final boolean onlyLocal) {
        assert csmFile != null;
        tokenUnderOffset = tokenUnderOffset != null ? tokenUnderOffset : getTokenByOffset(doc, offset);
        // no token in document under offset position
        if (tokenUnderOffset == null) {
            return null;
        }
        CsmObject csmObject = null;
        // support for overloaded operators
        if (tokenUnderOffset.getTokenID() == CCTokenContext.OPERATOR) {
            CsmObject foundObject = CsmOffsetResolver.findObject(csmFile, offset);
            csmObject = foundObject;
            if (CsmKindUtilities.isFunction(csmObject)) {
                CsmFunction decl = null;
                if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                    decl = ((CsmFunctionDefinition)csmObject).getDeclaration();
                } else if (CsmKindUtilities.isFriendMethod(csmObject)) {
                    decl = ((CsmFriendFunction)csmObject).getReferencedFunction();
                }
                if (decl != null) {
                    csmObject = decl;
                }
            } else {
                csmObject = null;
            }
        }
        if (csmObject == null) {
            // try with code completion engine
            csmObject = CompletionUtilities.findItemAtCaretPos(null, doc, CsmCompletionProvider.getCompletionQuery(csmFile, onlyLocal), offset);
        }     
        return csmObject;
    }
    
    private static Token getTokenByOffset(BaseDocument doc, int offset) {
        Token token = TokenUtilities.getToken(doc, offset);
//        if (token != null) {
//            // try prev token if it's end of prev token and prev token is interested one
//            if (!token.getText().contains("\n")) {
//                token = TokenUtilities.getToken(doc, offset-1);
//            }
//        }
        return token;
    }    

    /*package*/ static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, int offset) {
        Token token = getTokenByOffset(doc, offset);
        ReferenceImpl ref = null;
        if (isSupportedToken(token)) {
            ref = createReferenceImpl(file, doc, offset, token);
        }
        return ref;
    }

    /*package*/ static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, TokenItem tokenItem) {
        Token token = new Token(tokenItem);
        ReferenceImpl ref = createReferenceImpl(file, doc, tokenItem.getOffset(), token);
        return ref;
    }
    
    public static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, int offset, Token token) {
        assert token != null;
        assert file != null : "null file for document " + doc + " on offset " + offset + " " + token;
        ReferenceImpl ref = new ReferenceImpl(file, doc, offset, token);
        return ref;
    }

    private static boolean isSupportedToken(Token token) {
        return token != null &&
                (CsmIncludeHyperlinkProvider.isSupportedToken(token) || CsmHyperlinkProvider.isSupportedToken(token));
    }

    public static Scope fastCheckScope(CsmReference ref) {
        if (ref == null) {
            throw new NullPointerException("null reference is not allowed");
        }
        CsmObject target = getTargetIfPossible(ref);
        if (target == null) {
            // try to resolve using only local context
            int offset = getRefOffset(ref);
            BaseDocument doc = getRefDocument(ref);
            if (doc != null) {
                Token token = getRefTokenIfPossible(ref);
                target = findDeclaration(ref.getContainingFile(), doc, token, offset, true);
                setResolvedInfo(ref, target);
            }
        } 
        return getTargetScope(target);
    }  
    
    private static Scope getTargetScope(CsmObject obj) {
        if (obj == null) {
            return Scope.UNKNOWN;
        }
        if (isLocalElement(obj)) {
            return Scope.LOCAL;
        } else {
            return Scope.GLOBAL;
        }
    }
    
    private static CsmObject getTargetIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getTarget();
        }
        return null;
    }
    
    private static Token getRefTokenIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getToken();
        } else {
            return null;
        }
    }
    
    private static BaseDocument getRefDocument(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getDocument();
        } else {
            CsmFile file = ref.getContainingFile();
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
            Document doc = null;
            if (ces != null) {
                doc = ces.getDocument();
            }
            return doc instanceof BaseDocument ? (BaseDocument)doc : null;
        }
    }
    
    private static int getRefOffset(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl)ref).getOffset();
        } else {
            return (ref.getStartOffset() + ref.getEndOffset() + 1) / 2;
        }
    }
    
    private static void setResolvedInfo(CsmReference ref, CsmObject target) {
        if (target != null && (ref instanceof ReferenceImpl)) {
            ((ReferenceImpl)ref).setTarget(target);            
        }
    }
     
    private static boolean isLocalElement(CsmObject decl) {
        assert decl != null;
        CsmObject scopeElem = decl;
        while (CsmKindUtilities.isScopeElement(scopeElem)) {
            CsmScope scope = ((CsmScopeElement)scopeElem).getScope();
            if (CsmKindUtilities.isFunction(scope)) {
                return true;
            } else if (CsmKindUtilities.isScopeElement(scope)) {
                scopeElem = ((CsmScopeElement)scope);
            } else {
                break;
            }
        }
        return false;
    }    

    static BaseDocument getDocument(CsmFile file) {
        BaseDocument doc = null;
        try {
            doc = ReferencesSupport.getBaseDocument(file.getAbsolutePath().toString());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        return doc;
    }
    
    static CsmReferenceKind getReferenceKind(CsmReference ref) {
        CsmReferenceKind kind = CsmReferenceKind.UNKNOWN;
        CsmObject owner = ref.getOwner();
        if (CsmKindUtilities.isType(owner)) {
            kind = getReferenceUsageKind(ref);
        }
        return kind;
    }
    
    static CsmReferenceKind getReferenceUsageKind(CsmReference ref) {
        CsmReferenceKind kind = CsmReferenceKind.DIRECT_USAGE;
        if (ref instanceof ReferenceImpl) {
            Document doc = getRefDocument(ref);
            int offset = ref.getStartOffset();
            // check previous token
            TokenSequence<CppTokenId> ts = CndLexerUtilities.getCppTokenSequence(doc, offset);
            if (ts != null && ts.isValid()) {
                ts.move(offset);
                org.netbeans.api.lexer.Token<CppTokenId> token = null;
                if (ts.movePrevious()) {
                    token = ts.offsetToken();
                }
                while (token != null && CppTokenId.WHITESPACE_CATEGORY.equals(token.id().primaryCategory())) {
                    if (ts.movePrevious()) {
                        token = ts.offsetToken();
                    } else {
                        token = null;
                    }
                }
                if (token != null) {
                    switch (token.id()) {
                        case DOT:
                        case DOTMBR:
                        case ARROW:
                        case ARROWMBR:
                        case SCOPE:
                            kind = CsmReferenceKind.AFTER_DEREFERENCE_USAGE;
                    }
                }
            }
        }
        return kind;
    }
}
