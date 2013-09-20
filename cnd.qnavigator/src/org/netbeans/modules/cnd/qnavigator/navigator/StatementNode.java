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
    private final List<CsmStatement> statements;
    private final List<List<CsmStatement>> bodies;
    private List<BreadcrumbsElement> children;
    private final BreadcrumbsElement parent;
    private final CharSequence displayName;
    private final int startOffset;
    private final int endOffset;
    private final Lookup lookup;
    private final DataObject cdo;

    static StatementNode createStatementNode(CsmStatement statement, BreadcrumbsElement parent, DataObject cdo) {
        switch (statement.getKind()) {
            case COMPOUND:
                return createBodyNode((CsmCompoundStatement) statement, parent, cdo);
            case IF:
                return createIfNode((CsmIfStatement) statement, parent, cdo);
            case TRY_CATCH:
                return createTryNode((CsmTryCatchStatement) statement, parent, cdo);
            case CATCH:
                return createBodyNode((CsmExceptionHandler) statement, parent, cdo);
            case DECLARATION:
                return createDeclarationNode((CsmDeclarationStatement) statement, parent, cdo);
            case WHILE:
            case DO_WHILE:
                return createLoopNode((CsmLoopStatement) statement, parent, cdo);
            case FOR:
                return createForNode((CsmForStatement) statement, parent, cdo);
            case RANGE_FOR:
                return createRangeForNode((CsmRangeForStatement) statement, parent, cdo);
            case SWITCH:
                return createSwitchNode((CsmSwitchStatement) statement, parent, cdo);
            case EXPRESSION:
                return createExpressionNode((CsmExpressionStatement) statement, parent, cdo);
            case RETURN:
                return createReturnNode((CsmReturnStatement) statement, parent, cdo);
            case CASE:
                return createCaseNode((CsmCaseStatement) statement, parent, cdo);
            case DEFAULT:
            case BREAK:
            case CONTINUE:
            case GOTO:
            case LABEL:
            default:
                return null;
        }
    }

    private static StatementNode createBodyNode(CsmCompoundStatement body, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(body, body.getStatements(), null, parent, cdo);
    }

    private static StatementNode createIfNode(CsmIfStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmStatement> st = new ArrayList<CsmStatement>();
        if (stmt.getThen() != null) {
            st.add(stmt.getThen());
        }
        if (stmt.getElse() != null) {
            st.add(stmt.getElse());
        }
        return new StatementNode(stmt, st, null, parent, cdo);
    }

    private static StatementNode createTryNode(CsmTryCatchStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmStatement> st = new ArrayList<CsmStatement>();
        if (stmt.getTryStatement() != null) {
            st.add(stmt.getTryStatement());
        }
        for (CsmExceptionHandler handler : stmt.getHandlers()) {
            st.add(handler);
        }
        return new StatementNode(stmt, st, null, parent, cdo);
    }

    private static StatementNode createLoopNode(CsmLoopStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmStatement> st = new ArrayList<CsmStatement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            st.addAll(((CsmCompoundStatement)body).getStatements());
        } else if (body != null) {
            st.add(body);
        }
        return new StatementNode(stmt, st, null, parent, cdo);
    }

    private static StatementNode createForNode(CsmForStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmStatement> st = new ArrayList<CsmStatement>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            st.addAll(((CsmCompoundStatement)body).getStatements());
        } else if (body != null) {
            st.add(body);
        }
        return new StatementNode(stmt, st, null, parent, cdo);
    }

    private static StatementNode createRangeForNode(CsmRangeForStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmStatement> st = new ArrayList<CsmStatement>();
        if (stmt.getDeclaration() != null) {
            st.add(stmt.getDeclaration());
        }
        if (stmt.getBody() != null) {
            st.add(stmt.getBody());
        }
        return new StatementNode(stmt, st, null, parent, cdo);
    }

    private static StatementNode createSwitchNode(CsmSwitchStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        List<CsmStatement> st = new ArrayList<CsmStatement>();
        List<List<CsmStatement>> bodies = new ArrayList<List<CsmStatement>>();
        final CsmStatement body = stmt.getBody();
        if (CsmKindUtilities.isCompoundStatement(body)) {
            CsmStatement currentCase = null;
            List<CsmStatement> currentCaseList = null;
            for(CsmStatement c : ((CsmCompoundStatement)body).getStatements()) {
                if(c.getKind() == CsmStatement.Kind.CASE || c.getKind() == CsmStatement.Kind.DEFAULT) {
                    if (currentCase != null) {
                        st.add(currentCase);
                        bodies.add(currentCaseList);
                    }
                    currentCase = c;
                    currentCaseList = new ArrayList<CsmStatement>();
                } else {
                    if (currentCaseList != null) {
                        currentCaseList.add(c);
                    }
                }
            }
            if (currentCase != null) {
                st.add(currentCase);
                bodies.add(currentCaseList);
            }
        }
        return new StatementNode(stmt, st, bodies, parent, cdo);
    }

    private static StatementNode createExpressionNode(CsmExpressionStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, Collections.<CsmStatement>emptyList(), null, parent, cdo);
    }

    private static StatementNode createReturnNode(CsmReturnStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, Collections.<CsmStatement>emptyList(), null, parent, cdo);
    }

    private static StatementNode createDeclarationNode(CsmDeclarationStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, Collections.<CsmStatement>emptyList(), null, parent, cdo);
    }

    private static StatementNode createCaseNode(CsmCaseStatement stmt, BreadcrumbsElement parent, DataObject cdo) {
        return new StatementNode(stmt, Collections.<CsmStatement>emptyList(), null, parent, cdo);
    }

    private StatementNode(CsmStatement owner, List<CsmStatement> statements, List<List<CsmStatement>> bodies, BreadcrumbsElement parent, DataObject cdo) {
        this.statements = statements;
        this.bodies = bodies;
        this.parent = parent;
        StringBuilder buf = new StringBuilder();
        switch(owner.getKind()) {
            case CASE:
                buf.append("case "); //NOI18N
                break;
            case DEFAULT:
                buf.append("default "); //NOI18N
                break;
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
                    buf.append("&lt;");
                    break;
                case '>':
                    buf.append("&gt;");
                    break;
                case '&':
                    buf.append("&amp;");
                    break;
                default:
                    buf.append(c);
            }
        }
        startOffset = owner.getStartOffset();
        int end = owner.getEndOffset();
        for(CsmStatement s : statements) {
            if (s.getEndOffset() > end) {
                end = s.getEndOffset();
            }
        }
        endOffset = end;
        displayName = CharSequences.create(buf);
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
            if (bodies == null) {
                for(CsmStatement st : statements) {
                    final StatementNode node = createStatementNode(st, this, cdo);
                    if (node != null) {
                        children.add(node);
                    }
                }
            } else {
                for(int i = 0; i < statements.size(); i++) {
                    final StatementNode node = new StatementNode(statements.get(i), bodies.get(i), null, this, cdo);
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
            CsmUtilities.openSource(node.cdo.getPrimaryFile(), node.startOffset);
        }
    }
}
