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
package org.netbeans.modules.db.sql.lexer;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.junit.NbTestCase;
import org.netbeans.lib.lexer.test.ModificationTextDocument;

/**
 *
 * @author Andrei Badea, Jiri Skrivanek
 */
public class SQLLexerTest extends NbTestCase {

    public SQLLexerTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("select -/ from 'a' + 1, dto");
        assertTokens(seq, SQLTokenId.KEYWORD, SQLTokenId.WHITESPACE, SQLTokenId.OPERATOR,
                SQLTokenId.OPERATOR, SQLTokenId.WHITESPACE, SQLTokenId.KEYWORD,
                SQLTokenId.WHITESPACE, SQLTokenId.STRING, SQLTokenId.WHITESPACE,
                SQLTokenId.OPERATOR, SQLTokenId.WHITESPACE, SQLTokenId.INT_LITERAL,
                SQLTokenId.COMMA, SQLTokenId.WHITESPACE, SQLTokenId.IDENTIFIER,
                SQLTokenId.WHITESPACE);
    }

    public void testQuotedIdentifiers() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("select \"derby\", `mysql`, [mssql], `quo + ted`");
        assertTokens(seq, SQLTokenId.KEYWORD, SQLTokenId.WHITESPACE, SQLTokenId.IDENTIFIER,
                SQLTokenId.COMMA, SQLTokenId.WHITESPACE, SQLTokenId.IDENTIFIER,
                SQLTokenId.COMMA, SQLTokenId.WHITESPACE, SQLTokenId.IDENTIFIER,
                SQLTokenId.COMMA, SQLTokenId.WHITESPACE, SQLTokenId.IDENTIFIER,
                SQLTokenId.WHITESPACE);
    }

    public void testComments() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("-- line comment\n# mysql comment\n/* block \ncomment*/");
        assertTokens(seq, SQLTokenId.LINE_COMMENT, SQLTokenId.LINE_COMMENT,
                SQLTokenId.BLOCK_COMMENT, SQLTokenId.WHITESPACE);
    }

    public void testNewLineInString() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("'new\nline'");
        assertTokens(seq, SQLTokenId.STRING, SQLTokenId.WHITESPACE);
    }

    public void testIncompleteString() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("'incomplete");
        assertTokens(seq, SQLTokenId.INCOMPLETE_STRING);
    }

    public void testEscapeSingleQuote() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("'Frank\\'s Book'");
        assertTrue(seq.moveNext());
        assertEquals(SQLTokenId.STRING, seq.token().id());
        assertEquals("'Frank\\'s Book'", seq.token().text().toString());

        seq = getTokenSequence("'Frank\\s Book'");
        assertTrue(seq.moveNext());
        assertEquals(SQLTokenId.STRING, seq.token().id());
        assertEquals("'Frank\\s Book'", seq.token().text().toString());

        seq = getTokenSequence("'Frank\\");
        assertTokens(seq, SQLTokenId.INCOMPLETE_STRING);

        seq = getTokenSequence("'Frank\\'");
        assertTokens(seq, SQLTokenId.INCOMPLETE_STRING);
    }

    private static TokenSequence<SQLTokenId> getTokenSequence(String sql) throws BadLocationException {
        Document doc = new ModificationTextDocument();
        doc.insertString(0, sql, null);
        doc.putProperty(Language.class, SQLTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<SQLTokenId> seq = hi.tokenSequence(SQLTokenId.language());
        seq.moveStart();
        return seq;
    }

    private static CharSequence dumpTokens(TokenSequence<?> seq) {
        seq.moveStart();
        StringBuilder builder = new StringBuilder();
        Token<?> token = null;
        while (seq.moveNext()) {
            if (token != null) {
                builder.append('\n');
            }
            token = seq.token();
            builder.append(token.id());
            PartType part = token.partType();
            if (part != PartType.COMPLETE) {
                builder.append(' ');
                builder.append(token.partType());
            }
            builder.append(' ');
            builder.append('\'');
            builder.append(token.text());
            builder.append('\'');
        }
        return builder;
    }

    private static void assertTokens(TokenSequence<SQLTokenId> seq, SQLTokenId... ids) {
        if (ids == null) {
            ids = new SQLTokenId[0];
        }
        assertEquals("Wrong token count.", ids.length, seq.tokenCount());
        seq.moveNext();
        for (SQLTokenId id : ids) {
            assertEquals("Wrong token ID at index " + seq.index(), id, seq.token().id());
            seq.moveNext();
        }
    }
}
