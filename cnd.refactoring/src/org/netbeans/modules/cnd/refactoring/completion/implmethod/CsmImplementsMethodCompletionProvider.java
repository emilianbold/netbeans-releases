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

package org.netbeans.modules.cnd.refactoring.completion.implmethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.cnd.api.lexer.CndTokenUtilities;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;

/**
 *
 */
public class CsmImplementsMethodCompletionProvider implements CompletionProvider {

    public CsmImplementsMethodCompletionProvider() {
        // default constructor to be created as lookup service
    }

    @Override
    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    @Override
    public CompletionTask createTask(int queryType, JTextComponent component) {
        if ((queryType & COMPLETION_QUERY_TYPE) != 0) {
            final int dot = component.getCaret().getDot();
            return new AsyncCompletionTask(new Query(dot), component);
        }
        return null;
    }

    // method for tests
    /*package*/ static Collection<CsmImplementsMethodCompletionItem> getFilteredData(BaseDocument doc, int caretOffset, int queryType) {
        Query query = new Query(caretOffset);
        Collection<CsmImplementsMethodCompletionItem> items = query.getItems(doc, caretOffset);
        return items;
    }

    private static final class Query extends AsyncCompletionQuery {

        private Collection<CsmImplementsMethodCompletionItem> results;
        private final int creationCaretOffset;
        private int queryAnchorOffset;
        private String filterPrefix;
        private boolean isApplicable = true;
        private CsmScope insertScope;

        /*package*/ Query(int caretOffset) {
            this.creationCaretOffset = caretOffset;
            this.queryAnchorOffset = -1;
        }

        @Override
        protected void query(CompletionResultSet resultSet, Document doc, int caretOffset) {
            Collection<CsmImplementsMethodCompletionItem> items = getItems((BaseDocument) doc, caretOffset);
            if (this.queryAnchorOffset >= 0) {
                if (items != null && items.size() > 0) {
                    this.results = items;
                    items = getFilteredData(items, this.filterPrefix);
                    resultSet.estimateItems(items.size(), -1);
                    resultSet.addAllItems(items);
                    resultSet.setAnchorOffset(queryAnchorOffset);
                }
                resultSet.setHasAdditionalItems(false);
            }
            resultSet.finish();
        }

        @Override
        protected boolean canFilter(JTextComponent component) {
            int caretOffset = component.getCaretPosition();
            filterPrefix = null;
            if (queryAnchorOffset > -1 && caretOffset >= queryAnchorOffset) {
                Document doc = component.getDocument();
                try {
                    filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                } catch (BadLocationException ex) {
                    Completion.get().hideCompletion();
                }
            } else {
                Completion.get().hideCompletion();
            }
            return filterPrefix != null;
        }

        @Override
        protected void filter(CompletionResultSet resultSet) {
            if (filterPrefix != null && results != null) {
                resultSet.setAnchorOffset(queryAnchorOffset);
                Collection<? extends CsmImplementsMethodCompletionItem> items = getFilteredData(results, filterPrefix);
                resultSet.estimateItems(items.size(), -1);
                resultSet.addAllItems(items);
            }
            resultSet.setHasAdditionalItems(false);
            resultSet.finish();
        }

        private void visitDeclarations(Set<CsmClass> classes, Collection<CsmOffsetableDeclaration> decls, final int caretOffset) {
            for(CsmOffsetableDeclaration decl : decls) {
                if (!isApplicable) {
                    return;
                }
                if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    if (decl.getStartOffset() <= caretOffset && caretOffset <= decl.getEndOffset()) {
                        insertScope = (CsmNamespaceDefinition)decl;
                        visitDeclarations(classes, ((CsmNamespaceDefinition)decl).getDeclarations(), caretOffset);
                    }
                } else {
                    if (decl.getStartOffset() <= caretOffset && caretOffset <= decl.getEndOffset()) {
                        isApplicable = false;
                    }
                    if (CsmKindUtilities.isMethodDefinition(decl)) {
                        CsmFunction declaration = ((CsmFunctionDefinition)decl).getDeclaration();
                        if (CsmKindUtilities.isFunctionDeclaration(declaration) && CsmKindUtilities.isClassMember(declaration)) {
                            CsmMember method = (CsmMember) declaration;
                            CsmClass cls = method.getContainingClass();
                            if (cls != null) {
                                classes.add(cls);
                            }
                        }
                    }
                }
            }
        }

        private void visitClasses(Set<CsmClass> classes, Collection<? extends CsmOffsetableDeclaration> decls, final int caretOffset) {
            for(CsmOffsetableDeclaration decl : decls) {
                if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    visitClasses(classes, ((CsmNamespaceDefinition)decl).getDeclarations(), caretOffset);
                } else if (CsmKindUtilities.isClass(decl)) {
                    visitClasses(classes, ((CsmClass)decl).getMembers(), caretOffset);
                    classes.add((CsmClass)decl);
                }
            }
        }
        
        private Collection<CsmImplementsMethodCompletionItem> getItems(final BaseDocument doc, final int caretOffset) {
            Collection<CsmImplementsMethodCompletionItem> items = new ArrayList<>();
            CsmCacheManager.enter();
            try {
                if (init(doc, caretOffset)) {
                    CsmFile csmFile = CsmUtilities.getCsmFile(doc, true, false);
                    if (csmFile != null) {
                        insertScope = csmFile;
                        Set<CsmClass> classes = new HashSet<>();
                        visitDeclarations(classes, csmFile.getDeclarations(), caretOffset);
                        if (isApplicable)  {
                            //if (classes.isEmpty()) {
                                // probably empty file
                                // try to find corresponded header
                                String name = CndPathUtilities.getBaseName(csmFile.getAbsolutePath().toString());
                                if (name.lastIndexOf('.') > 0) { //NOI18N
                                    name = name.substring(0, name.lastIndexOf('.')); //NOI18N
                                }
                                CsmFile bestInterface = null;
                                for(CsmInclude incl : csmFile.getIncludes()) {
                                    CsmFile includeFile = incl.getIncludeFile();
                                    if (includeFile != null) {
                                        String inclName = CndPathUtilities.getBaseName(includeFile.getAbsolutePath().toString());
                                        if (inclName.lastIndexOf('.') > 0) { //NOI18N
                                            inclName = inclName.substring(0, inclName.lastIndexOf('.')); //NOI18N
                                        }
                                        if (name.equals(inclName)) {
                                            bestInterface = includeFile;
                                            break;
                                        }
                                    }
                                }
                                if (bestInterface != null) {
                                    visitClasses(classes, bestInterface.getDeclarations(), caretOffset);
                                }
                            //}
                            for(CsmClass cls : classes) {
                                for(CsmMember member : cls.getMembers()) {
                                    if (CsmKindUtilities.isMethodDeclaration(member)) {
                                        if (((CsmMethod) member).isAbstract()) {
                                            continue;
                                        }
                                        CsmFunction method = (CsmFunction) member;
                                        CsmFunctionDefinition definition = method.getDefinition();
                                        if (definition == null) {
                                            items.add(CsmImplementsMethodCompletionItem.createImplementItem(queryAnchorOffset, caretOffset, cls, method, insertScope));
                                        } else if (method == definition){
                                            if (definition.getDefinitionKind() == CsmFunctionDefinition.DefinitionKind.REGULAR) {
                                                final CsmImplementsMethodCompletionItem item =
                                                        CsmImplementsMethodCompletionItem.createExtractBodyItem(queryAnchorOffset, caretOffset, cls, method, insertScope);
                                                if (item != null) {
                                                    items.add(item);
                                                }
                                            }
                                        }
                                    }
                                }
                                for(CsmFriend member : cls.getFriends()) {
                                    if (CsmKindUtilities.isFriendMethod(member)) {
                                        CsmFriendFunction method = (CsmFriendFunction) member;
                                        CsmFunctionDefinition definition = method.getDefinition();
                                        if (definition == null) {
                                            items.add(CsmImplementsMethodCompletionItem.createImplementItem(queryAnchorOffset, caretOffset, cls, method, insertScope));
                                        } else if (method == definition){
                                            if (definition.getDefinitionKind() == CsmFunctionDefinition.DefinitionKind.REGULAR) {
                                                final CsmImplementsMethodCompletionItem item =
                                                        CsmImplementsMethodCompletionItem.createExtractBodyItem(queryAnchorOffset, caretOffset, cls, method, insertScope);
                                                if (item != null) {
                                                    items.add(item);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (BadLocationException ex) {
                // no completion
            } finally {
                CsmCacheManager.leave();
            }
            return items;
        }
        
        private boolean init(final BaseDocument doc, final int caretOffset) throws BadLocationException {
            filterPrefix = "";
            queryAnchorOffset = caretOffset;
            if (doc != null) {
                doc.readLock();
                try {
                    TokenItem<TokenId> tok = CndTokenUtilities.getTokenCheckPrev(doc, caretOffset);
                    if (tok != null) {
                        TokenId id = tok.id();
                        if(id instanceof CppTokenId) {
                            if (!CppTokenId.WHITESPACE_CATEGORY.equals(id.primaryCategory())) {
                                queryAnchorOffset = tok.offset();
                                filterPrefix = doc.getText(queryAnchorOffset, caretOffset - queryAnchorOffset);
                            }
                        }
                    }
                } catch (BadLocationException ex) {
                    // skip
                } finally {
                    doc.readUnlock();
                }
            }
            return this.queryAnchorOffset >= 0;
        }

        private Collection<CsmImplementsMethodCompletionItem> getFilteredData(Collection<CsmImplementsMethodCompletionItem> data, String prefix) {
            Collection<CsmImplementsMethodCompletionItem> out;
            if (prefix == null) {
                out = data;
            } else {
                List<CsmImplementsMethodCompletionItem> ret = new ArrayList<>(data.size());
                for (CsmImplementsMethodCompletionItem itm : data) {
                    if (matchPrefix(itm, prefix)) {
                        ret.add(itm);
                    }
                }
                out = ret;
            }
            return out;
        }

        private boolean matchPrefix(CsmImplementsMethodCompletionItem itm, String prefix) {
            return CharSequenceUtils.startsWith(itm.getSortText(), prefix);
        }
    }
}
