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

package org.netbeans.modules.cnd.qnavigator.navigator;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.actions.Openable;
import org.netbeans.modules.cnd.api.model.deep.CsmCaseStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExceptionHandler;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmIfStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLoopStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmRangeForStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmReturnStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmSwitchStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmTryCatchStatement;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController;
import org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.DataObject;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Alexander Simon
 */
public final class StatementNode implements BreadcrumbsElement {
    private final List<CsmTrueElement> trueCsmElements;
    private List<BreadcrumbsElement> children;
    private final BreadcrumbsElement parent;
    private final CharSequence displayName;
    private final Lookup lookup;
    private final DataObject cdo;
    private final int openOffset;
    private int startOffset;
    private int endOffset;

    static StatementNode createStatementNode(CsmStatement statement, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        switch (statement.getKind()) {
            case COMPOUND:
                return createBodyNode((CsmCompoundStatement) statement, decoration, parent, cdo);
            case IF:
                return createIfNode((CsmIfStatement) statement, decoration, parent, cdo);
            case TRY_CATCH:
                return createTryNode((CsmTryCatchStatement) statement, decoration, parent, cdo);
            case CATCH:
                return createBodyNode((CsmExceptionHandler) statement, decoration, parent, cdo);
            case DECLARATION:
                return createDeclarationNode((CsmDeclarationStatement) statement, decoration, parent, cdo);
            case WHILE:
            case DO_WHILE:
                return createLoopNode((CsmLoopStatement) statement, decoration, parent, cdo);
            case FOR:
                return createForNode((CsmForStatement) statement, decoration, parent, cdo);
            case RANGE_FOR:
                return createRangeForNode((CsmRangeForStatement) statement, decoration, parent, cdo);
            case SWITCH:
                return createSwitchNode((CsmSwitchStatement) statement, decoration, parent, cdo);
            case EXPRESSION:
                return createExpressionNode((CsmExpressionStatement) statement, decoration, parent, cdo);
            case RETURN:
                return createReturnNode((CsmReturnStatement) statement, decoration, parent, cdo);
            case CASE:
                return createCaseNode((CsmCaseStatement) statement, decoration, parent, cdo);
            case DEFAULT:
            case BREAK:
            case CONTINUE:
            case GOTO:
            case LABEL:
            default:
                return null;
        }
    }

    private static StatementNode createBodyNode(CsmCompoundStatement body, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        for(CsmStatement s : ((CsmCompoundStatement)body).getStatements()) {
            st.add(new CsmTrueElement(s));
        }
        return new StatementNode(body, decoration, st, parent, cdo);
    }

    private static StatementNode createIfNode(CsmIfStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement thenStmt = stmt.getThen();
        int lastThenOffset = -1;
        if (thenStmt != null) {
            CsmTrueElement csmTrueElement = new CsmTrueElement(thenStmt);
            st.add(csmTrueElement);
            csmTrueElement.decoration = "then "; //NOI18N
            final ArrayList<CsmStatement> list = new ArrayList<CsmStatement>();
            list.add(thenStmt);
            csmTrueElement.body = list;
            lastThenOffset = thenStmt.getEndOffset();
        }
        CsmStatement elseStmt = stmt.getElse();
        while(elseStmt != null && elseStmt.getKind() == CsmStatement.Kind.IF) {
            CsmIfStatement elseIfStmt =  (CsmIfStatement) elseStmt;
            CsmTrueElement csmTrueElement = new CsmTrueElement(elseIfStmt);
            st.add(csmTrueElement);
            csmTrueElement.decoration = "else ";//NOI18N
            final ArrayList<CsmStatement> list = new ArrayList<CsmStatement>();
            csmTrueElement.body = list;
            CsmStatement elifThenStmt = elseIfStmt.getThen();
            if (elifThenStmt != null) {
                list.add(elifThenStmt);
                if (lastThenOffset >= 0) {
                    csmTrueElement.startOffset = lastThenOffset + 1;
                } else {
                    csmTrueElement.startOffset = elseStmt.getStartOffset();
                }
                csmTrueElement.endOffset = elifThenStmt.getEndOffset();
                lastThenOffset = elifThenStmt.getEndOffset();
            }
            elseStmt = elseIfStmt.getElse();
        }
        if (elseStmt != null) {
            CsmTrueElement csmTrueElement = new CsmTrueElement(elseStmt);
            st.add(csmTrueElement);
            csmTrueElement.decoration = "else ";//NOI18N
            if (lastThenOffset >= 0) {
                csmTrueElement.startOffset = lastThenOffset + 1;
            }
            final ArrayList<CsmStatement> list = new ArrayList<CsmStatement>();
            list.add(elseStmt);
            csmTrueElement.body = list;
        }
        return new StatementNode(stmt, decoration, st, parent, cdo);
    }

    private static StatementNode createTryNode(CsmTryCatchStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        if (stmt.getTryStatement() != null) {
            st.add(new CsmTrueElement(stmt.getTryStatement()));
        }
        for (CsmExceptionHandler handler : stmt.getHandlers()) {
            st.add(new CsmTrueElement(handler));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo);
    }

    private static StatementNode createLoopNode(CsmLoopStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            for(CsmStatement s : ((CsmCompoundStatement)body).getStatements()) {
                st.add(new CsmTrueElement(s));
            }
        } else if (body != null) {
            st.add(new CsmTrueElement(stmt.getBody()));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo);
    }

    private static StatementNode createForNode(CsmForStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            for(CsmStatement s : ((CsmCompoundStatement)body).getStatements()) {
                st.add(new CsmTrueElement(s));
            }
        } else if (body != null) {
            st.add(new CsmTrueElement(stmt.getBody()));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo);
    }

    private static StatementNode createRangeForNode(CsmRangeForStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        if (stmt.getDeclaration() != null) {
            st.add(new CsmTrueElement(stmt.getDeclaration()));
        }
        if (stmt.getBody() != null) {
            st.add(new CsmTrueElement(stmt.getBody()));
        }
        return new StatementNode(stmt, decoration, st, parent, cdo);
    }

    private static StatementNode createSwitchNode(CsmSwitchStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmTrueElement> st = new ArrayList<CsmTrueElement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            CsmTrueElement currElement = null;
            int lastTrueStart = body.getStartOffset()+1;
            for (CsmStatement c : ((CsmCompoundStatement) body).getStatements()) {
                if (c.getKind() == CsmStatement.Kind.CASE || c.getKind() == CsmStatement.Kind.DEFAULT) {
                    if (currElement != null) {
                        st.add(currElement);
                    }
                    currElement = new CsmTrueElement(c);
                    currElement.startOffset = lastTrueStart;
                    if (c.getKind() == CsmStatement.Kind.CASE) {
                        currElement.decoration = "case "; //NOI18N
                    } else {
                        currElement.decoration = "default "; //NOI18N
                    }
                    currElement.body = new ArrayList<CsmStatement>();
                    lastTrueStart = c.getEndOffset()+1;
                } else {
                    if (currElement != null) {
                        currElement.body.add(c);
                        currElement.endOffset = c.getEndOffset();
                        lastTrueStart = c.getEndOffset()+1;
                    }
                }
            }
            if (currElement != null) {
                st.add(currElement);
            }
        }
        return new StatementNode(stmt, decoration, st, parent, cdo);
    }

    private static StatementNode createExpressionNode(CsmExpressionStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo);
    }

    private static StatementNode createReturnNode(CsmReturnStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo);
    }

    private static StatementNode createDeclarationNode(CsmDeclarationStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo);
    }

    private static StatementNode createCaseNode(CsmCaseStatement stmt, String decoration, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, decoration, Collections.<CsmTrueElement>emptyList(), parent, cdo);
    }

    private StatementNode(CsmTrueElement owner, String decoration, List<CsmTrueElement> trueCsmElements, BreadcrumbsElement parent, DataObject cdo) {
        this(owner.statement, decoration, trueCsmElements, parent, cdo);
        startOffset = owner.startOffset;
        endOffset = owner.endOffset;
    }

    private StatementNode(CsmStatement owner, String decoration, List<CsmTrueElement> trueCsmElements, BreadcrumbsElement parent, DataObject cdo) {
        this.trueCsmElements = trueCsmElements;
        this.parent = parent;
        StringBuilder buf = new StringBuilder();
        if (decoration != null) {
            buf.append(decoration);
        }
        CharSequence text = owner.getText();
        loop: for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch(c) {
                case ' ':
                    if (buf.length() > 0) {
                        buf.append(c);
                    }
                    break;
                case '\t':
                    if (buf.length() > 0) {
                        buf.append(' ');
                    }
                    break;
                case '\n':
                    break loop;
                case '<':
                    buf.append("&lt;"); //NOI18N
                    break;
                case '>':
                    buf.append("&gt;"); //NOI18N
                    break;
                case '&':
                    buf.append("&amp;"); //NOI18N
                    break;
                case '/':
                    if (i+1 < text.length() && text.charAt(i) == '/')  {
                        break loop;
                    }
                    break;
                default:
                    buf.append(c);
            }
        }
        openOffset = owner.getStartOffset();
        startOffset = owner.getStartOffset();    
        int end = owner.getEndOffset();
        for(CsmTrueElement s : trueCsmElements) {
            if (s.endOffset > end) {
                end = s.endOffset;
            }
        }
        endOffset = end;
        int i = buf.indexOf("{"); //NOI18N
        if (i > 0) {
            buf.setLength(i);
        }
        displayName = CharSequences.create(buf.toString().trim());
        this.cdo = cdo;
        lookup =  Lookups.fixed(new OpenableImpl(this));
    }

    @Override
    public String getHtmlDisplayName() {
        return displayName.toString();
    }

    @Override
    public Image getIcon(int type) {
        return BreadcrumbsController.NO_ICON;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return BreadcrumbsController.NO_ICON;
    }

    @Override
    public List<BreadcrumbsElement> getChildren() {
        if (children == null) {
            children = new ArrayList<BreadcrumbsElement>();
            for(CsmTrueElement s : trueCsmElements) {
                List<CsmStatement> body = s.body;
                String decoration = s.decoration;
                StatementNode node;
                if (body != null) {
                    if (body.size() == 1) {
                        CsmStatement content = body.get(0);
                        if (content.getKind() == CsmStatement.Kind.COMPOUND) {
                            body = ((CsmCompoundStatement)content).getStatements();
                        }
                    }
                    if (body.size() == 1 && s.statement == body.get(0)) {
                        node = createStatementNode(s.statement, decoration, this, cdo);
                        if (node != null) {
                            node.startOffset = s.startOffset;
                            node.endOffset = s.endOffset;
                        }
                    } else {
                        List<CsmTrueElement> sts = new ArrayList<CsmTrueElement>();
                        for(CsmStatement st : body) {
                            sts.add(new CsmTrueElement(st));
                        }
                        node = new StatementNode(s, decoration, sts, this, cdo);
                    }
                } else {
                    node = createStatementNode(s.statement, decoration, this, cdo);
                }
                if (node != null) {
                    children.add(node);
                }
            }
        }
        return children;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public BreadcrumbsElement getParent() {
        return parent;
    }

    public int getStartOffset() {
        return startOffset;
    }

    public int getEndOffset() {
        return endOffset;
    }

    private static final class OpenableImpl implements Openable, OpenCookie {

        private final StatementNode node;

        public OpenableImpl(StatementNode node) {
            this.node = node;
        }

        @Override
        public void open() {
            CsmUtilities.openSource(node.cdo.getPrimaryFile(), node.openOffset);
        }
    }
    
    private static final class CsmTrueElement {
        final CsmStatement statement;
        List<CsmStatement> body;
        String decoration;
        int startOffset;
        int endOffset;
        
        private CsmTrueElement(CsmStatement statement) {
            this.statement = statement;
            startOffset = statement.getStartOffset();
            endOffset = statement.getEndOffset();
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();
            buf.append(Integer.toString(startOffset));
            buf.append('-');
            buf.append(Integer.toString(endOffset));
            buf.append(' ');
            if (decoration != null) {
                buf.append(decoration);
            }
            return buf.toString();
        }
    }
}
