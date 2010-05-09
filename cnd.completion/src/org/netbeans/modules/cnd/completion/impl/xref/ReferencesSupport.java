/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver.Scope;
import org.netbeans.modules.cnd.completion.cplusplus.CsmCompletionProvider;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CsmCompletionQuery.QueryScope;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmHyperlinkProvider;
import org.netbeans.modules.cnd.completion.cplusplus.hyperlink.CsmIncludeHyperlinkProvider;
import org.netbeans.modules.cnd.completion.csm.CompletionUtilities;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetResolver;
import org.netbeans.modules.cnd.completion.csm.CsmOffsetUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Parameters;
import org.openide.util.UserQuestionException;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.lib.editor.hyperlink.spi.HyperlinkType;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerType;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.deep.CsmGotoStatement;
import org.netbeans.modules.cnd.api.model.xref.CsmLabelResolver;
import org.openide.util.CharSequences;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class ReferencesSupport {

    private static ReferencesSupport instance = new ReferencesSupport();

    private ReferencesSupport() {
        progressListener = new CsmProgressAdapter() {

            @Override
            public void fileParsingFinished(CsmFile file) {
                clearFileReferences(file);
            }

            @Override
            public void projectParsingFinished(CsmProject project) {
                clearFileReferences(null);
            }
        };
        CsmListeners.getDefault().addProgressListener(progressListener);
    }

    public static ReferencesSupport instance() {
        return instance;
    }

    /**
     * converts (line, col) into offset. Line and column info are 1-based, so
     * the start of document is (1,1)
     */
    public static int getDocumentOffset(BaseDocument doc, int lineIndex, int colIndex) {
        return Utilities.getRowStartFromLineOffset(doc, lineIndex - 1) + (colIndex - 1);
    }

    public static BaseDocument getBaseDocument(final String absPath) throws DataObjectNotFoundException, IOException {
        File file = new File(absPath);
        // convert file into file object
        FileObject fileObject = FileUtil.toFileObject(file);
        if (fileObject == null) {
            return null;
        }
        DataObject dataObject = DataObject.find(fileObject);
        EditorCookie cookie = dataObject.getCookie(EditorCookie.class);
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

        return doc instanceof BaseDocument ? (BaseDocument) doc : null;
    }

    public CsmObject findReferencedObject(CsmFile csmFile, BaseDocument doc, int offset) {
        return findReferencedObject(csmFile, doc, offset, null, null);
    }

    /*static*/ static CsmObject findOwnerObject(CsmFile csmFile, BaseDocument baseDocument, int offset, TokenItem<CppTokenId> token) {
        CsmObject csmOwner = CsmOffsetResolver.findObject(csmFile, offset);
        return csmOwner;
    }

    /*package*/ CsmObject findReferencedObject(CsmFile csmFile, final BaseDocument doc,
            final int offset, TokenItem<CppTokenId> jumpToken, FileReferencesContext fileReferencesContext) {
        long oldVersion = CsmFileInfoQuery.getDefault().getFileVersion(csmFile);
        CsmObject csmItem = null;
        // emulate hyperlinks order
        // first ask includes handler if offset in include sring token
        CsmInclude incl = null;
        if (jumpToken == null) {
            doc.readLock();
            try {
                jumpToken = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            } finally {
                doc.readUnlock();
            }
        }
        if (jumpToken != null) {
            switch (jumpToken.id()) {
                case PREPROCESSOR_INCLUDE:
                case PREPROCESSOR_INCLUDE_NEXT:
                    // look for include directive
                    csmItem = findInclude(csmFile, offset);
                    break;
                case PREPROCESSOR_SYS_INCLUDE:
                case PREPROCESSOR_USER_INCLUDE:
                    // look for include file
                    csmItem = findInclude(csmFile, offset);
                    if (csmItem != null) {
                        csmItem = ((CsmInclude)csmItem).getIncludeFile();
                    }
                    break;
            }
        }

        // if failed => ask declarations handler
        if (csmItem == null) {
            int key = jumpToken.offset();
            if (key < 0) {
                key = offset;
            }
            csmItem = getReferencedObject(csmFile, key, oldVersion);
            if (csmItem == null) {
                csmItem = findDeclaration(csmFile, doc, jumpToken, key, fileReferencesContext);
                if (csmItem == null) {
                    putReferencedObject(csmFile, key, FAKE, oldVersion);
                } else {
                    putReferencedObject(csmFile, key, csmItem, oldVersion);
                }
            } else if (csmItem == FAKE) {
                csmItem = null;
            }
        }
        return csmItem;
    }

    public static CsmInclude findInclude(CsmFile csmFile, int offset) {
        assert (csmFile != null);
        return CsmOffsetUtilities.findObject(csmFile.getIncludes(), null, offset);
    }

    public static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            TokenItem<CppTokenId> tokenUnderOffset, final int offset) {
        return findDeclaration(csmFile, doc, tokenUnderOffset, offset, null);
    }

    private static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            TokenItem<CppTokenId> tokenUnderOffset, final int offset, FileReferencesContext fileReferencesContext) {

        // fast check, if possible
        CsmObject csmItem = null;
        // macros have max priority in file
        List<CsmReference> macroUsages = CsmFileInfoQuery.getDefault().getMacroUsages(csmFile);
        csmItem = findMacro(macroUsages, offset);
        if (csmItem != null) {
            return csmItem;
        }
        CsmObject objUnderOffset = CsmOffsetResolver.findObject(csmFile, offset, fileReferencesContext);
        // TODO: it would be great to check position in named element, but we don't
        // support this information yet, so

        // fast check for enumerators
        if (CsmKindUtilities.isEnumerator(objUnderOffset)) {
            CsmEnumerator enmrtr = (CsmEnumerator) objUnderOffset;
            if (enmrtr.getExplicitValue() == null) {
                csmItem = enmrtr;
            }
        } else if (CsmKindUtilities.isLabel(objUnderOffset)) {
            csmItem = objUnderOffset;
        } else if (CsmKindUtilities.isGotoStatement(objUnderOffset)) {
            CsmGotoStatement csmGoto = (CsmGotoStatement) objUnderOffset;
            CsmScope scope = csmGoto.getScope();
            while (scope != null && CsmKindUtilities.isScopeElement(scope) && !CsmKindUtilities.isFunctionDefinition(scope)) {
                scope = ((CsmScopeElement) scope).getScope();
            }
            if (CsmKindUtilities.isFunctionDefinition(scope)) {
                Collection<CsmReference> labels = CsmLabelResolver.getDefault().getLabels(
                        (CsmFunctionDefinition) scope, csmGoto.getLabel(),
                        EnumSet.of(CsmLabelResolver.LabelKind.Definiton));
                if (!labels.isEmpty()) {
                    csmItem = labels.iterator().next().getReferencedObject();
                }
            }
            if (csmItem == null) {
                // Exit now, don't look for variables, types and etc.
                return null;
            }
        } else if (CsmKindUtilities.isVariable(objUnderOffset) || CsmKindUtilities.isTypedef(objUnderOffset)) {
            CsmType type = CsmKindUtilities.isVariable(objUnderOffset) ? ((CsmVariable) objUnderOffset).getType() : ((CsmTypedef) objUnderOffset).getType();
            CsmParameter parameter = null;
            boolean repeat;
            do {
                repeat = false;
                if (CsmOffsetUtilities.isInObject(type, offset)) {
                    parameter = null;
                } else if (CsmKindUtilities.isFunctionPointerType(type)) {
                    CsmParameter deeperParameter = CsmOffsetUtilities.findObject(
                            ((CsmFunctionPointerType) type).getParameters(), null, offset);
                    if (deeperParameter != null) {
                        parameter = deeperParameter;
                        type = deeperParameter.getType();
                        repeat = true;
                    }
                }
            } while (repeat);
            csmItem = parameter;
        } else if (false && CsmKindUtilities.isVariableDeclaration(objUnderOffset)) {
            // turned off, due to the problems like
            // Cpu MyCpu(type, 0, amount);
            // initialization part is part of variable => we need info about name position exactly
            CsmVariable var = (CsmVariable) objUnderOffset;
            if (var.getName().length() > 0 && !var.isExtern()) {
                // not work yet for arrays declarations IZ#130678
                // not work yet for elements with init value IZ#130684
                if ((var.getInitialValue() == null) && (var.getType() != null) && (var.getType().getArrayDepth() == 0)) {
                    csmItem = var;
                }
            }
        }
        if (csmItem == null) {
            int[] idFunBlk = null;
            try {
                if (doc instanceof BaseDocument) {
                    idFunBlk = NbEditorUtilities.getIdentifierAndMethodBlock((BaseDocument) doc, offset);
                }
            } catch (BadLocationException ex) {
                // skip it
            }
            // check but not for function call
            if (idFunBlk != null && idFunBlk.length != 3) {
                csmItem = findDeclaration(csmFile, doc, tokenUnderOffset, offset, QueryScope.SMART_QUERY, fileReferencesContext);
            }
        }
        // then full check if needed
        csmItem = csmItem != null ? csmItem : findDeclaration(csmFile, doc, tokenUnderOffset, offset, QueryScope.GLOBAL_QUERY, fileReferencesContext);
        return csmItem;
    }

    private static CsmObject findDeclaration(final CsmFile csmFile, final Document doc,
            TokenItem<CppTokenId> tokenUnderOffset, final int offset, final QueryScope queryScope, FileReferencesContext fileReferencesContext) {
        assert csmFile != null;
        if (tokenUnderOffset == null && doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).readLock();
            try {
                tokenUnderOffset = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            } finally {
                ((AbstractDocument) doc).readUnlock();
            }
        }
        // no token in document under offset position
        if (tokenUnderOffset == null) {
            return null;
        }
        CsmObject csmObject = null;
        // support for overloaded operators
        if (tokenUnderOffset.id() == CppTokenId.OPERATOR) {
            CsmObject foundObject = CsmOffsetResolver.findObject(csmFile, offset, fileReferencesContext);
            csmObject = foundObject;
            if (CsmKindUtilities.isFunction(csmObject)) {
                CsmFunction decl = null;
                if (CsmKindUtilities.isFunctionDefinition(csmObject)) {
                    decl = ((CsmFunctionDefinition) csmObject).getDeclaration();
                } else if (CsmKindUtilities.isFriendMethod(csmObject)) {
                    decl = ((CsmFriendFunction) csmObject).getReferencedFunction();
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
            Collection<CsmObject> objs = CompletionUtilities.findItemsReferencedAtCaretPos(null, doc, CsmCompletionProvider.getCompletionQuery(csmFile, queryScope, fileReferencesContext), offset);
            if (!objs.isEmpty()) {
                csmObject = objs.iterator().next();
            }
        }
        return csmObject;
    }

    /*package*/ static ReferenceImpl createReferenceImpl(final CsmFile file, final BaseDocument doc, final int offset) {
        ReferenceImpl ref = null;
        doc.readLock();
        try {
            TokenItem<CppTokenId> token = CndTokenUtilities.getTokenCheckPrev(doc, offset);
            if (isSupportedToken(token)) {
                ref = createReferenceImpl(file, doc, offset, token, null);
            }
        } finally {
            doc.readUnlock();
        }
        return ref;
    }

    public static ReferenceImpl createReferenceImpl(CsmFile file, BaseDocument doc, int offset, TokenItem<CppTokenId> token, CsmReferenceKind kind) {
        assert token != null;
        assert file != null : "null file for document " + doc + " on offset " + offset + " " + token;
        if (token.id() == CppTokenId.THIS) {
            return new ThisReferenceImpl(file, doc, offset, token, kind);
        } else {
            return new ReferenceImpl(file, doc, offset, token, kind);
        }
    }

    private static boolean isSupportedToken(TokenItem<CppTokenId> token) {
        return token != null &&
                (CsmIncludeHyperlinkProvider.isSupportedToken(token, HyperlinkType.GO_TO_DECLARATION) || CsmHyperlinkProvider.isSupportedToken(token, HyperlinkType.GO_TO_DECLARATION));
    }

    public static Scope fastCheckScope(CsmReference ref) {
        Parameters.notNull("ref", ref); // NOI18N
        CsmObject target = getTargetIfPossible(ref);
        if (target == null) {
            // try to resolve using only local context
            int offset = getRefOffset(ref);
            BaseDocument doc = getRefDocument(ref);
            if (doc != null) {
                TokenItem<CppTokenId> token = getRefTokenIfPossible(ref);
                target = findDeclaration(ref.getContainingFile(), doc, token, offset, QueryScope.LOCAL_QUERY, null);
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
        } else if (isFileLocalElement(obj)) {
            return Scope.FILE_LOCAL;
        } else {
            return Scope.GLOBAL;
        }
    }

    private static CsmObject getTargetIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getTarget();
        }
        return null;
    }

    /*package*/
    static TokenItem<CppTokenId> getRefTokenIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getToken();
        } else {
            return null;
        }
    }

    private static CsmReferenceKind getRefKindIfPossible(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getKindImpl();
        } else {
            return null;
        }
    }

    private static BaseDocument getRefDocument(CsmReference ref) {
        if (ref instanceof DocOffsetableImpl) {
            return ((DocOffsetableImpl) ref).getDocument();
        } else {
            CsmFile file = ref.getContainingFile();
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
            Document doc = null;
            if (ces != null) {
                doc = ces.getDocument();
            }
            return doc instanceof BaseDocument ? (BaseDocument) doc : null;
        }
    }

    private static int getRefOffset(CsmReference ref) {
        if (ref instanceof ReferenceImpl) {
            return ((ReferenceImpl) ref).getOffset();
        } else {
            return (ref.getStartOffset() + ref.getEndOffset() + 1) / 2;
        }
    }

    private static void setResolvedInfo(CsmReference ref, CsmObject target) {
        if (target != null && (ref instanceof ReferenceImpl)) {
            ((ReferenceImpl) ref).setTarget(target);
        }
    }

    private static boolean isLocalElement(CsmObject decl) {
        assert decl != null;
        CsmObject scopeElem = decl;
        while (CsmKindUtilities.isScopeElement(scopeElem)) {
            CsmScope scope = ((CsmScopeElement) scopeElem).getScope();
            if (CsmKindUtilities.isFunction(scope)) {
                return true;
            } else if (CsmKindUtilities.isScopeElement(scope)) {
                scopeElem = ((CsmScopeElement) scope);
            } else {
                break;
            }
        }
        return false;
    }

    private static boolean isFileLocalElement(CsmObject decl) {
        assert decl != null;
        if (CsmBaseUtilities.isDeclarationFromUnnamedNamespace(decl)) {
            return true;
        } else if (CsmKindUtilities.isFileLocalVariable(decl)) {
            return true;
        } else if (CsmKindUtilities.isFunction(decl)) {
            return CsmBaseUtilities.isFileLocalFunction(((CsmFunction) decl));
        }
        return false;
    }

    private static class FileToDoc {
        BaseDocument doc;
        CsmFile file;
        FileToDoc(BaseDocument doc, CsmFile file){
            this.doc = doc;
            this.file = file;
        }
    }
    private static Reference<FileToDoc> lastCsmFile = null;

    static BaseDocument getDocument(CsmFile file) {
        BaseDocument doc = null;
        try {
            Reference<FileToDoc> lcf = lastCsmFile;
            if (lcf != null) {
                FileToDoc pair = lcf.get();
                if (pair != null && pair.file == file) {
                    return pair.doc;
                }
            }
            doc = ReferencesSupport.getBaseDocument(file.getAbsolutePath().toString());
        } catch (DataObjectNotFoundException ex) {
            ex.printStackTrace(System.err);
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
        if (doc != null) {
            lastCsmFile = new WeakReference<FileToDoc>(new FileToDoc(doc,file));
        }
        return doc;
    }

    static CsmReferenceKind getReferenceKind(CsmReference ref) {
        CsmReferenceKind kind = CsmReferenceKind.UNKNOWN;
        CsmObject owner = ref.getOwner();
        if (CsmKindUtilities.isType(owner) || CsmKindUtilities.isInheritance(owner)) {
            kind = getReferenceUsageKind(ref);
        } else if (CsmKindUtilities.isInclude(owner)) {
            kind = CsmReferenceKind.DIRECT_USAGE;
        } else {
            CsmObject target = ref.getReferencedObject();
            if (target == null) {
                kind = getReferenceUsageKind(ref);
            } else {
                CsmObject[] decDef = CsmBaseUtilities.getDefinitionDeclaration(target, true);
                CsmObject targetDecl = decDef[0];
                CsmObject targetDef = decDef[1];
                assert targetDecl != null;
                kind = CsmReferenceKind.DIRECT_USAGE;
                if (owner != null) {
                    if (owner.equals(targetDef)) {
                        kind = CsmReferenceKind.DEFINITION;
                    } else if (sameDeclaration(owner, targetDecl)) {
                        kind = CsmReferenceKind.DECLARATION;
                    } else {
                        kind = getReferenceUsageKind(ref);
                    }
                }
            }
        }
        return kind;
    }

    private static boolean sameDeclaration(CsmObject checkDecl, CsmObject targetDecl) {
        if (checkDecl.equals(targetDecl)) {
            return true;
        } else if (CsmKindUtilities.isQualified(checkDecl) && CsmKindUtilities.isQualified(targetDecl)) {
            CharSequence fqnCheck = ((CsmQualifiedNamedElement) checkDecl).getQualifiedName();
            CharSequence fqnTarget = ((CsmQualifiedNamedElement) targetDecl).getQualifiedName();
            if (fqnCheck.equals(fqnTarget)) {
                return true;
            }
            String strFqn = fqnCheck.toString().trim();
            // we consider const and not const methods as the same
            if (strFqn.endsWith("const")) { //NOI18N
                int cutConstInd = strFqn.lastIndexOf("const"); //NOI18N
                assert cutConstInd >= 0;
                fqnCheck = CharSequences.create(strFqn.substring(cutConstInd));
            }
            return fqnCheck.equals(fqnTarget);
        }
        return false;
    }

    static CsmReferenceKind getReferenceUsageKind(final CsmReference ref) {
        CsmReferenceKind kind = CsmReferenceKind.DIRECT_USAGE;
        if (ref instanceof ReferenceImpl) {
            CsmReferenceKind implKind = getRefKindIfPossible(ref);
            if (implKind != null) {
                return implKind;
            }
            final BaseDocument doc = getRefDocument(ref);
            doc.readLock();
            try {
                int offset = ref.getStartOffset();
                // check previous token
                TokenItem<CppTokenId> token = CndTokenUtilities.getFirstNonWhiteBwd(doc, offset);
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
            } finally {
                doc.readUnlock();
            }
        }
        return kind;
    }
    private final CsmProgressListener progressListener;
    private static final int MAX_CACHE_SIZE = 10;
    private final Object cacheLock = new CacheLock();
    private final static class CacheLock {};
    private Map<CsmFile, Map<Integer, CsmObject>> cache = new HashMap<CsmFile, Map<Integer, CsmObject>>();
    private static CsmObject FAKE = new CsmObject() {

        @Override
        public String toString() {
            return "FAKE REFERENCE"; // NOI18N
        }

    };

    private CsmObject getReferencedObject(CsmFile file, int offset, long oldVersion) {
        synchronized (cacheLock) {
            Map<Integer, CsmObject> map = cache.get(file);
            CsmObject out = null;
            if (map != null) {
                out = map.get(offset);
                if (out == FAKE && CsmFileInfoQuery.getDefault().getFileVersion(file) != oldVersion) {
                    // we don't beleive in such fake and put null instead
                    map.put(offset, null);
                    out = null;
                }
            }
            return out;
        }
    }

    private void putReferencedObject(CsmFile file, int offset, CsmObject object, long oldVersion) {
        synchronized (cacheLock) {
            if (object == FAKE && CsmFileInfoQuery.getDefault().getFileVersion(file) != oldVersion) {
                // we don't beleive in such fake
//                System.err.println("skip caching FAKE NULL at " + offset + " in " + file);
                return;
            }
            Map<Integer, CsmObject> map = cache.get(file);
            if (map == null) {
                if (cache.size() > MAX_CACHE_SIZE) {
                    cache.clear();
                }
                map = new HashMap<Integer, CsmObject>();
                cache.put(file, map);
            }
            map.put(offset, object);
        }
    }

    private void clearFileReferences(CsmFile file) {
        synchronized (cacheLock) {
            if (file == null) {
                cache.clear();
            } else {
                cache.remove(file);
            }
        }
    }

    /**
     * Searches for macro.
     *
     * @param macroUsages - list of macros
     * @param offset - macro offset
     * @return macro
     */
    public static CsmObject findMacro(List<CsmReference> macroUsages, final int offset) {
        int index = Collections.binarySearch(macroUsages, new RefOffsetKey(offset), new Comparator<CsmReference>() {
            public int compare(CsmReference o1, CsmReference o2) {
                if (o1 instanceof RefOffsetKey) {
                    if (o2.getStartOffset() <= o1.getStartOffset() &&
                            o1.getEndOffset() <= o2.getEndOffset()) {
                        return 0;
                    }
                } else if (o2 instanceof RefOffsetKey) {
                    if (o1.getStartOffset() <= o2.getStartOffset() &&
                            o2.getEndOffset() <= o1.getEndOffset()) {
                        return 0;
                    }
                }
                return o1.getStartOffset() - o2.getStartOffset();
            }
        });
        if (index >= 0) {
            CsmReference macroRef = macroUsages.get(index);
            CsmObject csmItem = macroRef.getReferencedObject();
            assert csmItem != null : "referenced macro is null. ref " + macroRef + ", file " + macroRef.getContainingFile() + ", name " + macroRef.getText();
            return csmItem;
        }
        return null;
    }

    private static final class RefOffsetKey implements CsmReference {

        private final int offset;

        private RefOffsetKey(int offset) {
            this.offset = offset;
        }

        public CsmReferenceKind getKind() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public CsmObject getReferencedObject() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public CsmObject getOwner() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public int getStartOffset() {
            return offset;
        }

        public int getEndOffset() {
            return offset;
        }

        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }

        public CharSequence getText() {
            throw new UnsupportedOperationException("Not supported yet."); // NOI18N
        }
    }
}
