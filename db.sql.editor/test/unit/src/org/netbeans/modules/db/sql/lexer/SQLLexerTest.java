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
 * @author Andrei Badea
 */
public class SQLLexerTest extends NbTestCase {

    // XXX turn these into proper golden file tests.

    public SQLLexerTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {
        Document doc = new ModificationTextDocument();
        doc.putProperty(Language.class, SQLTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence seq = hi.tokenSequence();
        System.out.println(dumpTokens(seq));
        assertFalse(seq.moveNext());
        doc.insertString(0, "select -/ from 'a' + 1, dto", null);
        seq = hi.tokenSequence();
        System.out.println(dumpTokens(seq));
    }

    public void testQuotedIdentifiers() throws Exception {
        System.out.println(dumpTokens(getTokenSequence("select \"derby\", `mysql`, [mssql], `quo + ted`")));
    }

    public void testComments() throws Exception {
        System.out.println(dumpTokens(getTokenSequence("-- line comment\n# mysql comment\n/* block \ncomment*/")));
    }

    public void testNewLineInString() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("'new\nline'");
        assertEquals(SQLTokenId.STRING, seq.token().id());
    }

    public void testIncompleteString() throws Exception {
        TokenSequence<SQLTokenId> seq = getTokenSequence("'incomplete");
        assertEquals(SQLTokenId.INCOMPLETE_STRING, seq.token().id());
    }

    private static TokenSequence<SQLTokenId> getTokenSequence(String sql) throws BadLocationException {
        Document doc = new ModificationTextDocument();
        doc.insertString(0, sql, null);
        doc.putProperty(Language.class, SQLTokenId.language());
        TokenHierarchy<?> hi = TokenHierarchy.get(doc);
        TokenSequence<SQLTokenId> seq = hi.tokenSequence(SQLTokenId.language());
        seq.moveNext();
        return seq;
    }

    private static CharSequence dumpTokens(TokenSequence<?> seq) {
        StringBuilder builder = new StringBuilder();
        Token token = null;
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
}
