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
import java.util.List;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;

/**
 *
 * @author Andrei Badea
 */
public class StatementAnalyzer {

    private final TokenSequence<SQLTokenId> seq;

    private final List<List<String>> selectValues = new ArrayList<List<String>>();
    private final List<FromTable> fromTableList = new ArrayList<FromTable>();
    private final FromTables fromTables;

    private State state = State.START;

    public StatementAnalyzer(TokenSequence<SQLTokenId> seq) {
        this.seq = seq;
        parse();
        fromTables = state.ordinal() >= State.FROM.ordinal() ? new FromTables(fromTableList) : null;
    }

    public List<List<String>> getSelectValues() {
        return selectValues;
    }

    public FromTables getFromTables() {
        return fromTables;
    }

    private void parse() {
        for (;;) {
            if (!nextToken()) {
                break;
            }
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
                            }
                            break;
                    }
                    break;
                case FROM:
                    switch (seq.token().id()) {
                        case IDENTIFIER:
                            fromTableList.add(parseFromTable());
                            break;
                        case KEYWORD:
                            if (isKeywordAfterFrom()) {
                                state = State.END;
                            }
                            break;
                    }
                    break;
            }
        }
    }

    private List<String> analyzeSelectValue() {
        List<String> parts = new ArrayList<String>();
        parts.add(seq.token().text().toString());
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
                        parts.add(seq.token().text().toString());
                    } else {
                        // Alias like "foo.bar baz".
                        parts.clear();
                        parts.add(seq.token().text().toString());
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
        parts.add(seq.token().text().toString());
        boolean afterDot = false;
        String alias = null;
        main: for (;;) {
            if (!nextToken()) {
                return new FromTable(parts, alias);
            }
            switch (seq.token().id()) {
                case DOT:
                    afterDot = true;
                    break;
                case IDENTIFIER:
                    if (afterDot && alias == null) {
                        afterDot = false;
                        parts.add(seq.token().text().toString());
                    } else {
                        alias = seq.token().text().toString();
                    }
                    break;
                case KEYWORD:
                    if (isKeyword("AS")) { // NOI18N
                        afterDot = false;
                    } else {
                        break main;
                    }
                    break;
                default:
                    break main;
            }
        }
        seq.movePrevious();
        return new FromTable(parts, alias);
    }

    private boolean nextToken() {
        while (seq.moveNext()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case LINE_COMMENT:
                case BLOCK_COMMENT:
                    break;
                default:
                    return true;
            }
        }
        return false;
    }

    private boolean isKeyword(CharSequence keyword) {
        return seq.token().id() == SQLTokenId.KEYWORD && textEqualsIgnoreCase(seq.token().text(), keyword);
    }

    private boolean isKeywordAfterFrom() {
        if (seq.token().id() != SQLTokenId.KEYWORD) {
            return false;
        }
        CharSequence keyword = seq.token().text();
        return textEqualsIgnoreCase("WHERE", keyword) || // NOI18N
               textEqualsIgnoreCase("HAVING", keyword) || // NOI18N
               textEqualsIgnoreCase("ORDER", keyword); // NOI18N
    }

    private static boolean textEqualsIgnoreCase(CharSequence seq1, CharSequence seq2) {
        if (seq1 == seq2) {
            return true;
        }
        int len1 = seq1.length();
        if (len1 != seq2.length()) {
            return false;
        }
        for (int i = 0; i < len1; i++) {
            char ch1 = Character.toLowerCase(seq1.charAt(i));
            char ch2 = Character.toLowerCase(seq2.charAt(i));
            if (ch1 != ch2) {
                return false;
            }
        }
        return true;
    }

    /**
     * Keep the order as in the SELECT statement.
     */
    private enum State {
        START,
        SELECT,
        FROM,
        END
    }
}
