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

package org.netbeans.modules.php.editor;

import java.util.prefs.Preferences;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletion;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionContext;
import org.netbeans.modules.db.sql.editor.api.completion.SQLCompletionResultSet;
import org.netbeans.modules.db.sql.editor.api.completion.SubstitutionHandler;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;
import org.netbeans.spi.editor.completion.support.AsyncCompletionTask;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;

/**
 *
 * @author Andrei Badea
 */
public class PHPSQLCompletion implements CompletionProvider {

    private static final Boolean NO_COMPLETION = Boolean.getBoolean("netbeans.php.nosqlcompletion"); // NOI18N
    private static final String PROP_DBCONN = "dbconn"; // NOI18N

    public static DatabaseConnection getDatabaseConnection(Project project) {
        String name = getProjectPreferences(project).get(PROP_DBCONN, null);
        if (name != null) {
            return ConnectionManager.getDefault().getConnection(name);
        }
        return null;
    }

    public static void setDatabaseConnection(Project project, DatabaseConnection dbconn) {
        Preferences prefs = getProjectPreferences(project);
        if (dbconn != null) {
            prefs.put(PROP_DBCONN, dbconn.getName());
        } else {
            prefs.remove(PROP_DBCONN);
        }
    }

    private static Preferences getProjectPreferences(Project project) {
        return ProjectUtils.getPreferences(project, PHPSQLCompletion.class, false);
    }

    private static Project getProject(Document doc) {
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        if (fo != null) {
            return FileOwnerQuery.getOwner(fo);
        }
        return null;
    }

    public CompletionTask createTask(int queryType, JTextComponent component) {
        return new AsyncCompletionTask(new Query(), component);
    }

    public int getAutoQueryTypes(JTextComponent component, String typedText) {
        return 0;
    }

    /*
     * Finds the starting offset of a PHP string at or before the given offset.
     * It returns a -1 if a string cannot be found in the PHP statement at the
     * given offset.
     *
     * This is needed because the PHP lexer splits a string with variable references
     * into multiple parts. For example "foo{$ref}bar" is:
     *
     * PHP_CONSTANT_ENCAPSED_STRING: "\""
     * PHP_ENCAPSED_AND_WHITESPACE: "foo"
     * PHP_CURLY_OPEN: "{"
     * PHP_VARIABLE: "$ref"
     * PHP_CURLY_CLOSE: "}"
     * PHP_ENCAPSED_AND_WHITESPACE: "bar"
     * PHP_CONSTANT_ENCAPSED_STRING: "\""
     *
     * However, a string with no variable references, like "foobar", is simply:
     *
     * PHP_CONSTANT_ENCAPSED_STRING: "\"foobar\""
     */
    static int findStringOffset(TokenSequence<PHPTokenId> seq, int offset) {
        int result = -1;
        seq.move(offset);
        if (!seq.moveNext() && !seq.movePrevious()){
            return result;
        }
        boolean seenSingleQuote = false;
        boolean stringWithRefs = false;
        outer: for (;;) {
            switch (seq.token().id()) {
                case PHP_CONSTANT_ENCAPSED_STRING:
                    if (CharSequenceUtilities.textEquals(seq.token().text(), "\"")) { // NOI18N
                        if (stringWithRefs) {
                            // Then this must definitely be the starting quote.
                            result = seq.offset();
                            break outer;
                        } else {
                            if (seenSingleQuote) {
                                // If already seen a single quote, but still haven't managed to decide
                                // this is a string with variable refs, then give up. This could be handled
                                // better.
                                result = -1;
                                break outer;
                            }
                            // OK, we don't know anything. Perhaps it is a starting quote,
                            // perhaps an ending one.
                            seenSingleQuote = true;
                            result = seq.offset();
                        }
                    } else {
                        // Good, this is a string without variable references.
                        if (!seenSingleQuote) {
                            // If already seen a single quote, it is probably the starting
                            // quote, so return it. Otherwise return the current quote.
                            result = seq.offset();
                        }
                        break outer;
                    }
                    break;
                case PHP_ENCAPSED_AND_WHITESPACE:
                    // The previous single quote (if any) must have been the ending one, so we
                    // are inside a string with variable refs.
                    seenSingleQuote = false;
                    stringWithRefs = true;
                    break;
                case PHP_SEMICOLON:
                    break outer;
            }
            if (!seq.movePrevious()) {
                break;
            }
        }
        return result;
    }

    private static final class Query extends AsyncCompletionQuery {

        @Override
        protected void query(CompletionResultSet resultSet, Document document, int caretOffset) {
            doQuery(resultSet, document, caretOffset);
            resultSet.finish();
        }

        private void doQuery(CompletionResultSet resultSet, Document document, int caretOffset) {
            if (NO_COMPLETION) {
                return;
            }
            EmbeddedSQLStatement substHandler = computeSQLStatement(document, caretOffset);
            if (substHandler == null) {
                return;
            }
            int statementCaretOffset = caretOffset - substHandler.getStatementOffset();
            if (statementCaretOffset < 0) { // Just in case.
                return;
            }
            SQLCompletionContext context = SQLCompletionContext.empty();
            context = context.setStatement(substHandler.getStatement());
            if (!SQLCompletion.canComplete(context)) {
                return;
            }
            context = context.setOffset(statementCaretOffset);
            Project project = getProject(document);
            if (project == null) {
                return;
            }
            DatabaseConnection dbconn = getDatabaseConnection(project);
            if (dbconn == null) {
                resultSet.addItem(new SelectConnectionItem(project));
            } else {
                context = context.setDatabaseConnection(dbconn);
                SQLCompletion completion = SQLCompletion.create(context);
                SQLCompletionResultSet sqlResultSet = SQLCompletionResultSet.create();
                completion.query(sqlResultSet, substHandler);
                resultSet.addAllItems(sqlResultSet.getItems());
                resultSet.setAnchorOffset(substHandler.getStatementOffset() + sqlResultSet.getAnchorOffset());
            }
        }

        private EmbeddedSQLStatement computeSQLStatement(final Document document, final int caretOffset) {
            final EmbeddedSQLStatement[] result = { null };
            document.render(new Runnable() {
                public void run() {
                    TokenSequence<PHPTokenId> seq = LexUtilities.getPHPTokenSequence(document, caretOffset);
                    if (seq == null) {
                        return;
                    }
                    int startOffset = findStringOffset(seq, caretOffset);
                    if (startOffset < 0) {
                        return;
                    }
                    seq.move(startOffset);
                    assert seq.moveNext();
                    assert seq.token().id() == PHPTokenId.PHP_CONSTANT_ENCAPSED_STRING;
                    int contentStart = seq.offset() + 1; // Account for starting quote.
                    int contentEnd = contentStart;
                    if (CharSequenceUtilities.textEquals(seq.token().text(), "\"")) { // NOI18N
                        // A string with variable refs.
                        outer: while (seq.moveNext()) {
                            switch (seq.token().id()) {
                                case PHP_CONSTANT_ENCAPSED_STRING:
                                    // This must be the ending quote token.
                                    contentEnd = seq.offset();
                                    break outer;
                            }
                        }
                    } else {
                        contentEnd = seq.offset() + seq.token().length() - 1; // Account for ending quote.
                    }
                    if (contentEnd <= contentStart || caretOffset < contentStart || caretOffset > contentEnd) {
                        return;
                    }
                    result[0] = new EmbeddedSQLStatement(document, contentStart, contentEnd);
                }
            });
            return result[0];
        }
    }

    private static final class EmbeddedSQLStatement implements SubstitutionHandler {

        private final String statement;
        private final int statementOffset;

        public EmbeddedSQLStatement(Document document, int statementOffset, int statementEnd) {
            try {
                this.statement = document.getText(statementOffset, statementEnd - statementOffset);
            } catch (BadLocationException e) {
                throw new IllegalStateException(e);
            }
            this.statementOffset = statementOffset;
        }

        public void substituteText(JTextComponent component, final int offset, final String text) {
            final int caretOffset = component.getSelectionEnd();
            final StyledDocument document = (StyledDocument) component.getDocument();
            try {
                NbDocument.runAtomicAsUser(document, new Runnable() {
                    public void run() {
                        try {
                            int documentOffset = statementOffset + offset;
                            document.remove(documentOffset, caretOffset - documentOffset);
                            document.insertString(documentOffset, text, null);
                        } catch (BadLocationException ex) {
                            // No can do, document may have changed.
                        }
                    }
                });
            } catch (BadLocationException ex) {
                // Same reason as above. Moreover, why does runAtomicAsUser() throw BLE?
            }
        }

        public String getStatement() {
            return statement;
        }

        public int getStatementOffset() {
            return statementOffset;
        }
    }
}
