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

package org.netbeans.modules.db.sql.analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.editor.StringUtils;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class SQLStatementAnalyzer {

    private final TokenSequence<SQLTokenId> seq;
    private final Quoter quoter;

    private final List<List<String>> selectValues = new ArrayList<List<String>>();
    private final List<FromTable> fromTables = new ArrayList<FromTable>();
    private final List<SQLStatement> subqueries = new ArrayList<SQLStatement>();

    private State state = State.START;

    public static SQLStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return new SQLStatement(0, 0, Collections.<List<String>>emptyList(), null, Collections.<SQLStatement>emptyList());
        }
        int startOffset = seq.offset();
        SQLStatementAnalyzer sa = new SQLStatementAnalyzer(seq, quoter);
        sa.parse();
        // Return a non-null FromClause iff there was a FROM clause in the statement.
        FromClause fromClause = (sa.state.isAfter(State.FROM)) ? sa.createFromClause() : null;
        return new SQLStatement(startOffset, seq.offset() + seq.token().length(), Collections.unmodifiableList(sa.selectValues), fromClause, Collections.unmodifiableList(sa.subqueries));
    }

    private SQLStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        this.seq = seq;
        this.quoter = quoter;
    }

    private FromClause createFromClause() {
        Set<QualIdent> unaliasedTableNames = new HashSet<QualIdent>();
        Map<String, QualIdent> aliasedTableNames = new HashMap<String, QualIdent>();
        for (FromTable table : fromTables) {
            if (table.alias == null) {
                unaliasedTableNames.add(table.tableName);
            } else {
                if (!aliasedTableNames.containsKey(table.alias)) {
                    aliasedTableNames.put(table.alias, table.tableName);
                }
            }
        }
        return new FromClause(Collections.unmodifiableSet(unaliasedTableNames), Collections.unmodifiableMap(aliasedTableNames));
    }

    private void parse() {
        boolean afterFromTableKeyword = false;
        do {
            switch (state) {
                case START:
                    if (isKeyword("SELECT")) { // NOI18N
                        state = State.SELECT;
                    }
                    break;
                case SELECT:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            List<String> selectValue = analyzeSelectValue();
                            if (!selectValue.isEmpty()) {
                                selectValues.add(selectValue);
                            }
                            break;
                        case KEYWORD:
                            if (isKeyword("FROM")) { // NOI18N
                                state = State.FROM;
                                afterFromTableKeyword = true;
                            }
                            break;
                    }
                    break;
                case FROM:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            if (afterFromTableKeyword) {
                                FromTable fromTable = parseFromTable();
                                if (fromTable != null) {
                                    fromTables.add(fromTable);
                                }
                                afterFromTableKeyword = false;
                            }
                            break;
                        case COMMA:
                            afterFromTableKeyword = true;
                            break;
                        case KEYWORD:
                            if (isKeyword("FROM") || isKeyword("JOIN")) { // NOI18N
                                afterFromTableKeyword = true;
                            } else if (isKeywordAfterFrom()) {
                                state = State.END;
                            }
                            break;
                    }
                    break;
            }
        } while (nextToken());
    }

    private List<String> analyzeSelectValue() {
        List<String> parts = new ArrayList<String>();
        parts.add(getUnquotedIdentifier());
        boolean afterDot = false;
        main: for (;;) {
            if (!nextToken()) {
                return parts;
            }
            switch (seq.token().id()) {
                case DOT:
                    // Tentatively considering this a qualified identifier.
                    afterDot = true;
                    break;
                case IDENTIFIER:
                    if (afterDot) {
                        afterDot = false;
                        parts.add(getUnquotedIdentifier());
                    } else {
                        // Alias like "foo.bar baz".
                        parts.clear();
                        parts.add(getUnquotedIdentifier());
                    }
                    break;
                case LPAREN:
                    // Looks like function call.
                    parts.clear();
                    break;
                case COMMA:
                    break main;
                case KEYWORD:
                    if (isKeyword("AS")) { // NOI18N
                        // Alias will follow.
                        afterDot = false;
                        parts.clear();
                    } else if (isKeyword("FROM") || isKeywordAfterFrom()) { // NOI18N
                        break main;
                    }
                    break;
                default:
            }
        }
        // Need to process the current token,
        // which doesn't belong to the current SELECT value, once again.
        seq.movePrevious();
        return parts;
    }

    private FromTable parseFromTable() {
        List<String> parts = new ArrayList<String>();
        parts.add(getUnquotedIdentifier());
        boolean afterDot = false;
        String alias = null;
        main: while (nextToken()) {
            switch (seq.token().id()) {
                case DOT:
                    afterDot = true;
                    break;
                case IDENTIFIER:
                    if (afterDot && alias == null) {
                        afterDot = false;
                        parts.add(getUnquotedIdentifier());
                    } else {
                        alias = getUnquotedIdentifier();
                    }
                    break;
                case KEYWORD:
                    if (isKeyword("AS")) { // NOI18N
                        afterDot = false;
                    } else {
                        seq.movePrevious();
                        break main;
                    }
                    break;
                default:
                    seq.movePrevious();
                    break main;
            }
        }
        // Remove empty quoted identifiers, like in '"FOO".""."BAR"'.
        // Actually, the example above would obviously be an invalid identifier,
        // but safer and simpler to be forgiving.
        for (Iterator<String> i = parts.iterator(); i.hasNext();) {
            if (i.next().length() == 0) {
                i.remove();
            }
        }
        if (alias != null && alias.length() == 0) {
            alias = null;
        }
        if (!parts.isEmpty()) {
            return new FromTable(new QualIdent(parts), alias);
        }
        return null;
    }

    private boolean nextToken() {
        boolean move;
        skip: while (move = seq.moveNext()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                    break;
                default:
                    break skip;
            }
        }
        if (state.isAfter(State.SELECT) && isKeyword("SELECT")) { // NOI18N
            // Looks like a subquery.
            int startOffset = seq.offset();
            int parLevel = 1;
            main: while (move = seq.moveNext()) {
                switch (seq.token().id()) {
                    case LPAREN:
                        parLevel++;
                        break;
                    case RPAREN:
                        if (--parLevel == 0) {
                            TokenSequence<SQLTokenId> subSeq = seq.subSequence(startOffset, seq.offset());
                            subqueries.add(SQLStatementAnalyzer.analyze(subSeq, quoter));
                            break main;
                        }
                        break;
                }
            }
        }
        return move;
    }

    private boolean isKeyword(CharSequence keyword) {
        return seq.token().id() == SQLTokenId.KEYWORD && StringUtils.textEqualsIgnoreCase(seq.token().text(), keyword);
    }

    private boolean isKeywordAfterFrom() {
        if (seq.token().id() != SQLTokenId.KEYWORD) {
            return false;
        }
        CharSequence keyword = seq.token().text();
        return StringUtils.textEqualsIgnoreCase("WHERE", keyword) || // NOI18N
               StringUtils.textEqualsIgnoreCase("HAVING", keyword) || // NOI18N
               StringUtils.textEqualsIgnoreCase("ORDER", keyword) || // NOI18N
               StringUtils.textEqualsIgnoreCase("GROUP", keyword); // NOI18N
    }

    private String getUnquotedIdentifier() {
        return quoter.unquote(seq.token().text().toString());
    }

    private enum State {

        START(0),
        SELECT(1),
        FROM(2),
        END(3);

        private int order;

        private State(int order) {
            this.order = order;
        }

        public boolean isAfter(State state) {
            return this.order >= state.order;
        }
    }

    private static class FromTable {

        private final QualIdent tableName;
        private final String alias;

        public FromTable(QualIdent tableName, String alias) {
            this.tableName = tableName;
            this.alias = alias;
        }
    }
}
