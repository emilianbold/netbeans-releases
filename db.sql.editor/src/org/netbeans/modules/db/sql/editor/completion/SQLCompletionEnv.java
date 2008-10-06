/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.sql.editor.completion;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.api.sql.execute.SQLScript;
import org.netbeans.modules.db.api.sql.execute.SQLScriptStatement;
import org.netbeans.modules.db.sql.editor.StringUtils;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class SQLCompletionEnv {

    private final String statement;
    private int statementOffset;
    private final int caretOffset;

    private TokenSequence<SQLTokenId> seq;
    private boolean selectStatement;
    private Context context;
    private int contextOffset;

    public static SQLCompletionEnv create(Document doc, int caretOffset) {
        String documentText = getDocumentText(doc);
        if (documentText != null) {
            return create(documentText, caretOffset);
        }
        return null;
    }

    static SQLCompletionEnv create(String script, int caretOffset) {
        SQLScriptStatement statement = SQLScript.create(script).getStatementAtOffset(caretOffset);
        if (statement != null) {
            return new SQLCompletionEnv(statement.getText(), statement.getStartOffset(), caretOffset - statement.getStartOffset());
        }
        return null;
    }

    private SQLCompletionEnv(String statement, int statementOffset, int caretOffset) {
        this.statement = statement;
        this.statementOffset = statementOffset;
        this.caretOffset = caretOffset;
        if (statement != null) {
            TokenHierarchy<String> hi = TokenHierarchy.create(statement, SQLTokenId.language());
            seq = hi.tokenSequence(SQLTokenId.language());
            computeStatementType();
            if (selectStatement) {
                computeContext();
            }
        }
    }

    /**
     * The text of the SQL statement.
     */
    public String getStatement() {
        return statement;
    }

    /**
     * The offset of the SQL statement in the document or SQL script.
     */
    public int getStatementOffset() {
        return statementOffset;
    }

    /**
     * The caret offset, relative to {@link #getStatementOffset}.
     */
    public int getCaretOffset() {
        return caretOffset;
    }

    public TokenSequence<SQLTokenId> getTokenSequence() {
        return seq;
    }

    public boolean isSelect() {
        return selectStatement;
    }

    public Context getContext() {
        return context;
    }

    private static String getDocumentText(final Document doc) {
        final String[] result = { null };
        doc.render(new Runnable() {
            public void run() {
                try {
                    result[0] = doc.getText(0, doc.getLength());
                } catch (BadLocationException e) {
                    // Should not happen.
                }
            }
        });
        return result[0];
    }

    private void computeStatementType() {
        seq.moveStart();
        for (;;) {
            if (!seq.moveNext()) {
                return;
            }
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                    break;
                case KEYWORD:
                    if (StringUtils.textEqualsIgnoreCase("SELECT", seq.token().text())) { // NOI18N
                        selectStatement = true;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    /**
     * Walks the token sequence backwards from the caret offset trying
     * to find the SQL clause at the caret offset.
     */
    private void computeContext() {
        int offset = seq.move(caretOffset);
        if (offset > 0) {
            seq.moveNext();
        }
        boolean wasBy = false;
        for (;;) {
            if (!seq.movePrevious()) {
                return;
            }
            if (seq.token().id() == SQLTokenId.KEYWORD) {
                CharSequence keyword = seq.token().text();
                if (!wasBy) {
                    if (StringUtils.textEqualsIgnoreCase("FROM", keyword)) { // NOI18N
                        context = Context.FROM;
                        return;
                    } else if (StringUtils.textEqualsIgnoreCase("ON", keyword)) { // NOI18N
                        context = Context.JOIN_CONDITION;
                        return;
                    } else if (StringUtils.textEqualsIgnoreCase("SELECT", keyword)) { // NOI18N
                        context = Context.SELECT;
                        return;
                    } else if (StringUtils.textEqualsIgnoreCase("WHERE", keyword)) { // NOI18N
                        context = Context.WHERE;
                        return;
                    } else if (StringUtils.textEqualsIgnoreCase("HAVING", keyword)) { // NOI18N
                        context = Context.HAVING;
                        return;
                    } else if (StringUtils.textEqualsIgnoreCase("BY", keyword)) { // NOI18N
                        wasBy = true;
                        continue;
                    } else if (StringUtils.textEqualsIgnoreCase("GROUP", keyword)) { // NOI18N
                        // After GROUP, but before BY.
                        return;
                    } else if (StringUtils.textEqualsIgnoreCase("ORDER", keyword)) { // NOI18N
                        // After ORDER, but before BY.
                        return;
                    }
                } else {
                    if (StringUtils.textEqualsIgnoreCase("GROUP", keyword)) { // NOI18N
                        context = Context.GROUP_BY;
                    } else if (StringUtils.textEqualsIgnoreCase("ORDER", keyword)) { // NOI18N
                        context = Context.ORDER_BY;
                    }
                    return;
                }
            } else if (wasBy) {
                switch (seq.token().id()) {
                    case WHITESPACE:
                    case LINE_COMMENT:
                    case BLOCK_COMMENT:
                        continue;
                    default:
                        // Expected a keyword before BY, such as GROUP or ORDER.
                        return;
                }
            }
        }
    }

    public enum Context {

        SELECT,
        FROM,
        JOIN_CONDITION,
        WHERE,
        GROUP_BY,
        HAVING,
        ORDER_BY
    }
}
