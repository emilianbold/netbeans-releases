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
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.analyzer.DropStatement.DropContext;
import org.netbeans.modules.db.sql.editor.completion.SQLStatementAnalyzer;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Jiri Skrivanek
 */
public class DropStatementAnalyzer {

    private final TokenSequence<SQLTokenId> seq;
    private final boolean detectKind;
    private final Quoter quoter;
    private final SortedMap<Integer, DropContext> offset2Context = new TreeMap<Integer, DropContext>();
    private QualIdent table = null;
    private int startOffset;
    private State state = State.START;

    public static DropStatement analyze(TokenSequence<SQLTokenId> seq, Quoter quoter) {
        DropStatementAnalyzer sa = doParse(seq, quoter, false);
        if (!sa.state.isAfter(State.START)) {
            return null;
        }
        return new DropStatement(SQLStatementKind.DROP, sa.startOffset, seq.offset() + seq.token().length(), sa.table, sa.offset2Context);
    }

    private static DropStatementAnalyzer doParse(TokenSequence<SQLTokenId> seq, Quoter quoter, boolean detectKind) {
        seq.moveStart();
        if (!seq.moveNext()) {
            return null;
        }
        DropStatementAnalyzer sa = new DropStatementAnalyzer(seq, quoter, detectKind);
        sa.parse();
        return sa;
    }

    private DropStatementAnalyzer(TokenSequence<SQLTokenId> seq, Quoter quoter, boolean detectKind) {
        this.seq = seq;
        this.quoter = quoter;
        this.detectKind = detectKind;
    }

    private void parse() {
        startOffset = seq.offset();
        do {
            switch (state) {
                case START:
                    if (SQLStatementAnalyzer.isKeyword("DROP", seq)) { // NOI18N
                        moveToState(State.DROP);
                        if (detectKind) {
                            return;
                        }
                    }
                    break;
                case DROP:
                    if (SQLStatementAnalyzer.isKeyword("TABLE", seq)) { // NOI18N
                        moveToState(State.TABLE);
                    }
                    break;
                case TABLE:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            table = parseIdentifier();
                            break;
                    }
                    break;
                default:
            }
        } while (nextToken());
    }

    //TODO - move to super class
    /** Returns fully qualified identifier or null. */
    private QualIdent parseIdentifier() {
        List<String> parts = new ArrayList<String>();
        parts.add(getUnquotedIdentifier());
        boolean afterDot = false;
        main:
        while (nextToken()) {
            switch (seq.token().id()) {
                case DOT:
                    afterDot = true;
                    break;
                case IDENTIFIER:
                    if (afterDot) {
                        afterDot = false;
                        parts.add(getUnquotedIdentifier());
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
        if (!parts.isEmpty()) {
            return new QualIdent(parts);
        }
        return null;
    }

    private boolean nextToken() {
        boolean move;
        skip:
        while (move = seq.moveNext()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                    break;
                default:
                    break skip;
            }
        }
        return move;
    }

    private void moveToState(State state) {
        this.state = state;
        DropContext context = state.getContext();
        if (context != null) {
            offset2Context.put(seq.offset() + seq.token().length(), context);
        }
    }

    private String getUnquotedIdentifier() {
        return quoter.unquote(seq.token().text().toString());
    }

    private enum State {

        START(0, null),
        DROP(1, DropContext.DROP),
        TABLE(2, DropContext.TABLE);

        private final int order;
        private final DropContext context;

        private State(int order, DropContext context) {
            this.order = order;
            this.context = context;
        }

        public boolean isAfter(State state) {
            return this.order >= state.order;
        }

        public DropContext getContext() {
            return context;
        }
    }
}
