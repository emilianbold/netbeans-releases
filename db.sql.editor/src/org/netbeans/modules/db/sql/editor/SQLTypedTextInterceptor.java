/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.sql.editor;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.db.sql.editor.completion.SQLCompletionEnv;
import org.netbeans.modules.db.sql.lexer.SQLLexer;
import org.netbeans.modules.db.sql.lexer.SQLTokenId;
import org.netbeans.spi.editor.typinghooks.TypedTextInterceptor;

public class SQLTypedTextInterceptor implements TypedTextInterceptor {

    @MimeRegistrations({
        @MimeRegistration(mimeType = "text/x-sql", service = TypedTextInterceptor.Factory.class)
    })
    public static class Factory implements TypedTextInterceptor.Factory {

        @Override
        public TypedTextInterceptor createTypedTextInterceptor(org.netbeans.api.editor.mimelookup.MimePath mimePath) {
            return new SQLTypedTextInterceptor();
        }
    }

    @Override
    public boolean beforeInsert(Context context) throws BadLocationException {
        return false;
    }

    @Override
    public void insert(MutableContext context) throws BadLocationException {
    }

    @Override
    public void afterInsert(final Context context) throws BadLocationException {
        final Document doc = context.getDocument();

        Runnable r = new Runnable() {
            public void run() {
                int caretOffset = context.getOffset();
                String str = context.getText();

                Character nextChar;
                try {
                    nextChar = doc.getText(caretOffset + str.length(),
                            1).charAt(0);
                } catch (BadLocationException ex) {
                    nextChar = null;
                }

                if (!str.isEmpty()) {
                    char insertedChar = str.charAt(str.length() - 1);
                    if ( (SQLLexer.isStartIdentifierQuoteChar(insertedChar)
                            && (nextChar == null || Character.isWhitespace(nextChar) || nextChar.equals('.')))
                            ||
                         (SQLLexer.isStartStringQuoteChar(insertedChar)
                            && (nextChar == null || Character.isWhitespace(nextChar)))
                            ) {  //NOI18N
                        if (canCompleteQuote(doc, caretOffset)) {
                            try {
                                // add pair quote
                                doc.insertString(caretOffset + str.length(), String.valueOf((char) SQLLexer.getMatchingQuote(insertedChar)), null);
                                context.getComponent().getCaret().setDot(caretOffset + str.length());
                            } catch (BadLocationException ex) {
                            }
                        }
                    } else if (insertedChar == '(' && (nextChar == null || Character.isWhitespace(nextChar))) {
                        if (canCompleteBrace(doc, caretOffset)) {
                            try {
                                // add pair quote
                                doc.insertString(caretOffset + str.length(), ")", null);
                                context.getComponent().getCaret().setDot(caretOffset + str.length());
                            } catch (BadLocationException ex) {
                            }
                        }
                    }
                }
            }
        };

        if(doc instanceof BaseDocument) {
            ((BaseDocument) doc).runAtomic(r);
        } else {
            r.run();
        }
    }

    /**
     * Returns true if completion of quote is wanted, i.e. cursor is on token
     * boundary and previous token is dot or whitespace.
     */
    private static boolean canCompleteQuote(Document doc, int caretOffset) {
        SQLCompletionEnv env = SQLCompletionEnv.forDocument(doc, caretOffset);
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        if (seq.move(caretOffset) == 0 && seq.movePrevious()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                case DOT:
                    return true;
            }
        }
        return false;
    }

    private static boolean canCompleteBrace(Document doc, int caretOffset) {
        SQLCompletionEnv env = SQLCompletionEnv.forDocument(doc, caretOffset);
        TokenSequence<SQLTokenId> seq = env.getTokenSequence();
        if (seq.move(caretOffset) == 0 && seq.movePrevious()) {
            switch (seq.token().id()) {
                case WHITESPACE:
                    return true;
            }
        }
        return false;
    }

    @Override
    public void cancelled(Context context) {
    }
}
