/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
import java.util.SortedMap;
import java.util.TreeMap;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.analyzer.SelectStatement.SelectContext;
import org.netbeans.modules.db.sql.editor.StringUtils;
import org.netbeans.modules.db.sql.editor.completion.SQLStatementAnalyzer;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class SelectStatementAnalyzer {

    // XXX SELECT-specific code should be refactored into a SelectStatementAnalyzer
    // (package-)private class.

    private final TokenSequence<SQLTokenId> seq;
    private final boolean detectKind;
    private final Quoter quoter;

    private final List<List<String>> selectValues = new ArrayList<List<String>>();
    private final List<FromTable> fromTables = new ArrayList<FromTable>();
    private final List<SelectStatement> subqueries = new ArrayList<SelectStatement>();
    private final SortedMap<Integer, SelectContext> offset2Context = new TreeMap<Integer, SelectContext>();

    private int startOffset;
    private State state = State.START;

    public static SelectStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        SelectStatementAnalyzer sa = doParse(seq, quoter, false);
        if (!sa.state.isAfter(State.START)) {
            return null;
        }
        // Return a non-null FromClause iff there was a FROM clause in the statement.
        FromClause fromClause = (sa.state.isAfter(State.FROM)) ? sa.createFromClause() : null;
        return new SelectStatement(SQLStatementKind.SELECT, sa.startOffset, seq.offset() + seq.token().length(), Collections.unmodifiableList(sa.selectValues), fromClause, Collections.unmodifiableList(sa.subqueries), sa.offset2Context);
    }

    private static SelectStatementAnalyzer doParse(TokenSequence<SQLTokenId> seq, Quoter quoter, boolean detectKind) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        SelectStatementAnalyzer sa = new SelectStatementAnalyzer(seq, quoter, detectKind);
        sa.parse();
        return sa;
    }

    private SelectStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter, boolean detectKind) {
        this.seq = seq;
        this.quoter = quoter;
        this.detectKind = detectKind;
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
        startOffset = seq.offset();
        boolean afterFromTableKeyword = false;
        do {
            switch (state) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword ("SELECT", seq)) { // NOI18N
                        moveToState(State.SELECT);
                        if (detectKind) {
                            return;
                        }
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
                            if (SQLStatementAnalyzer.isKeyword ("FROM", seq)) { // NOI18N
                                moveToState(State.FROM);
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
                            if (SQLStatementAnalyzer.isKeyword ("JOIN", seq)) { // NOI18N
                                afterFromTableKeyword = true;
                            } else {
                                State newState = getStateForKeywordAfterFrom();
                                if (newState != null) {
                                    moveToState(newState);
                                }
                            }
                            break;
                    }
                    break;
                case JOIN_CONDITION:
                    switch (seq.token().id()) {
                        case COMMA:
                            moveToState(State.FROM);
                            afterFromTableKeyword = true;
                            break;
                        case KEYWORD:
                            if (SQLStatementAnalyzer.isKeyword ("JOIN", seq)) { // NOI18N
                                moveToState(State.FROM);
                                afterFromTableKeyword = true;
                            }
                            break;
                    }
                    break;
                case GROUP_WITHOUT_BY:
                    if (SQLStatementAnalyzer.isKeyword ("BY", seq)) { // NOI18N
                        moveToState(State.GROUP_BY);
                    }
                    break;
                case ORDER_WITHOUT_BY:
                    if (SQLStatementAnalyzer.isKeyword ("BY", seq)) { // NOI18N
                        moveToState(State.ORDER_BY);
                    }
                    break;
                default:
                    State newState = getStateForKeywordAfterFrom();
                    if (newState != null) {
                        moveToState(newState);
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
                    if (SQLStatementAnalyzer.isKeyword ("AS", seq)) { // NOI18N
                        // Alias will follow.
                        afterDot = false;
                        parts.clear();
                    } else if (SQLStatementAnalyzer.isKeyword ("FROM", seq) || isKeywordAfterFrom()) { // NOI18N
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
                    if (SQLStatementAnalyzer.isKeyword ("AS", seq)) { // NOI18N
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
        if (state.isAfter(State.SELECT) && SQLStatementAnalyzer.isKeyword ("SELECT", seq)) { // NOI18N
            // Looks like a subquery.
            int subStartOffset = seq.offset();
            int parLevel = 1;
            main: while (move = seq.moveNext()) {
                switch (seq.token().id()) {
                    case LPAREN:
                        parLevel++;
                        break;
                    case RPAREN:
                        if (--parLevel == 0) {
                            TokenSequence<SQLTokenId> subSeq = seq.subSequence(subStartOffset, seq.offset());
                            SelectStatement subquery = SelectStatementAnalyzer.analyze(subSeq, quoter);
                            if (subquery != null) {
                                subqueries.add(subquery);
                            }
                            break main;
                        }
                        break;
                }
            }
        }
        return move;
    }

    private void moveToState(State state) {
        this.state = state;
        SelectContext context = state.getContext();
        if (context != null) {
            offset2Context.put(seq.offset() + seq.token().length(), context);
        }
    }

    private boolean isKeywordAfterFrom() {
        return getStateForKeywordAfterFrom() != null;
    }

    private State getStateForKeywordAfterFrom() {
        if (seq.token().id() != SQLTokenId.KEYWORD) {
            return null;
        }
        CharSequence keyword = seq.token().text();
        if (StringUtils.textEqualsIgnoreCase("ON", keyword)) { // NOI18N
            return State.JOIN_CONDITION;
        } else if (StringUtils.textEqualsIgnoreCase("WHERE", keyword)) { // NOI18N
            return State.WHERE;
        } else if (StringUtils.textEqualsIgnoreCase("GROUP", keyword)) { // NOI18N
            return State.GROUP_WITHOUT_BY;
        } else if (StringUtils.textEqualsIgnoreCase("HAVING", keyword)) { // NOI18N
            return State.HAVING;
        } else if (StringUtils.textEqualsIgnoreCase("ORDER", keyword)) { // NOI18N
            return State.ORDER_WITHOUT_BY;
        }
        return null;
    }

    private String getUnquotedIdentifier() {
        return quoter.unquote(seq.token().text().toString());
    }

    private enum State {

        START(0, null),
        SELECT(1, SelectContext.SELECT),
        FROM(2, SelectContext.FROM),
        JOIN_CONDITION(3, SelectContext.JOIN_CONDITION),
        WHERE(4, SelectContext.WHERE),
        GROUP_WITHOUT_BY(5, null),
        GROUP_BY(6, SelectContext.GROUP_BY),
        HAVING(7, SelectContext.HAVING),
        ORDER_WITHOUT_BY(8, null),
        ORDER_BY(9, SelectContext.ORDER_BY);

        private final int order;
        private final SelectContext context;

        private State(int order, SelectContext context) {
            this.order = order;
            this.context = context;
        }

        public boolean isAfter(State state) {
            return this.order >= state.order;
        }

        public SelectContext getContext() {
            return context;
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
