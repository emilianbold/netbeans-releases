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

package org.netbeans.modules.cnd.completion.impl.xref;

import java.io.File;
import java.io.IOException;
import javax.swing.text.StyledDocument;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.utils.Token;
import org.netbeans.modules.cnd.completion.cplusplus.utils.TokenUtilities;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetUtilities;
import org.netbeans.modules.cnd.editor.cplusplus.CCTokenContext;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

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
            throw new IllegalStateException("Given file (\"" + dataObject.getName() + "\") does not have EditorCookie.");
        }
        
        StyledDocument doc = cookie.openDocument();

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
        // first ask includes handler
        CsmInclude incl = findInclude(csmFile, offset);
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

    public static CsmObject findDeclaration(final CsmFile csmFile, final BaseDocument doc, Token tokenUnderOffset, final int offset) {
        CsmOffsetable item = null;
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
            csmObject = CompletionUtilities.findItemAtCaretPos(null, doc, CsmCompletionProvider.getCompletionQuery(csmFile), offset);
        }     
//        if (csmObject == null && foundObject != null) {
//            csmObject = foundObject;
//        }
        return csmObject;
    }
    
    private static Token getTokenByOffset(BaseDocument doc, int offset) {
        return TokenUtilities.getToken(doc, offset);
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
    
    private static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, int offset, Token token) {
        assert token != null;
        ReferenceImpl ref = new ReferenceImpl(file, doc, offset, token);
        return ref;
    }

    private static boolean isSupportedToken(Token token) {
        return token != null &&
                (CsmIncludeHyperlinkProvider.isSupportedToken(token) || CsmHyperlinkProvider.isSupportedToken(token));
    }


  
}
