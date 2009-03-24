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
package org.netbeans.modules.ruby.rhtml.editor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;

public final class RubyEmbeddingProvider extends EmbeddingProvider {

    public static class Factory extends TaskFactory {

        @Override
        public Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            if (snapshot.getSource().getMimeType().equals(RhtmlTokenId.MIME_TYPE)) {
                return Collections.singleton(new RubyEmbeddingProvider());
            } else {
                return Collections.<SchedulerTask>emptyList();
            }
        }
    }
    
    private static final String RUBY_MIMETYPE = "text/x-ruby"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        List<Embedding> ems = extractRuby(snapshot);
        if (ems.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(Embedding.create(ems));
        }
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void cancel() {
        //no cancel supported
    }

    private List<Embedding> extractRuby(Snapshot snapshot) {
        List<Embedding> ems = new ArrayList<Embedding>(40);
        // Add a super class such that code completion, goto declaration etc.
        // knows where to pull the various link_to etc. methods from

        // Pretend that this code is an extension to ActionView::Base such that
        // code completion, go to declaration etc. sees the inherited methods from
        // ActionView -- link_to and friends.

        ems.add(snapshot.create("class ActionView::Base\n", RUBY_MIMETYPE)); // NOI18N
        // TODO Try to include the helper class as well as the controller fields too;
        // for now this logic is hardcoded into Ruby's code completion engine (CodeCompleter)

        // Erubis uses _buf; I've seen eruby using something else (_erbout?)
        ems.add(snapshot.create("_buf='';", RUBY_MIMETYPE)); // NOI18N

        TokenHierarchy tokenHierarchy = TokenHierarchy.create(snapshot.getText(), RhtmlTokenId.language());
        TokenSequence<RhtmlTokenId> tokenSequence = tokenHierarchy.tokenSequence();

        boolean skipNewline = false;
        while (tokenSequence.moveNext()) {
            Token<RhtmlTokenId> token = tokenSequence.token();

            if (token.id() == RhtmlTokenId.HTML) {
                int sourceStart = token.offset(tokenHierarchy);
                int sourceEnd = sourceStart + token.length();

                String text = token.text().toString();

                // If there is leading whitespace in this token followed by a newline,
                // emit it directly first, then insert my buffer append. Otherwise,
                // insert a semicolon if we're on the same line as the previous output.
                boolean found = false;
                int i = 0;
                for (; i < text.length(); i++) {
                    char c = text.charAt(i);
                    if (c == '\n') {
                        i++; // include it
                        found = true;
                        break;
                    } else if (!Character.isWhitespace(c)) {
                        break;
                    }
                }

                if (found) {
                    ems.add(snapshot.create(text.substring(0, i), RUBY_MIMETYPE));
                    text = text.substring(i);
                } else {
                    ems.add(snapshot.create(";", RUBY_MIMETYPE));
                }
                ems.add(snapshot.create("_buf << '", RUBY_MIMETYPE));
                if (skipNewline && text.startsWith("\n")) { // NOI18N
                    text = text.substring(1);
                    sourceEnd--;
                }
                // Escape 's in the document so they don't escape out of the ruby code
                // I don't have to do this on lines that are in comments... But no big harm
                text = text.replace("'", "\\'");
                ems.add(snapshot.create(text, RUBY_MIMETYPE));
                // TODO: This "\n" shouldn't be there if the next "<%" is a "<%-" !
                ems.add(snapshot.create("';\n", RUBY_MIMETYPE)); // NOI18N

                skipNewline = false;
            } else if (token.id() == RhtmlTokenId.RUBY) {
                int sourceStart = token.offset(tokenHierarchy);
                int sourceEnd = sourceStart + token.length();

                String text = token.text().toString();
                skipNewline = false;
                if (text.endsWith("-")) { // NOI18N
                    skipNewline = true;
                }

                ems.add(snapshot.create(sourceStart, sourceEnd - sourceStart - (skipNewline ? 1 : 0), RUBY_MIMETYPE));

                if (tokenSequence.moveNext()) {
                    Token<RhtmlTokenId> nextToken = tokenSequence.token();
                    if (nextToken != null && nextToken.id() == RhtmlTokenId.DELIMITER) {
                        // Insert a semicolon if there is something else on this line
                        int delimiterEnd = tokenSequence.offset() + nextToken.length();
                        if (delimiterEnd <= snapshot.getText().length()) {
                            for (int i = delimiterEnd; i < snapshot.getText().length(); i++) {
                                char c = snapshot.getText().charAt(i);
                                if (c == '\n') {
                                    break;
                                } else if (!Character.isWhitespace(c)) {
                                    // Yep, we have more stuff on this line
                                    ems.add(snapshot.create(";", RUBY_MIMETYPE));
                                    break;
                                }
                            }

                        }
                    }
                    tokenSequence.movePrevious();
                }

                skipNewline = false;
            } else if (token.id() == RhtmlTokenId.RUBY_EXPR) {
                ems.add(snapshot.create("_buf << (", RUBY_MIMETYPE)); // NOI18N
                int sourceStart = token.offset(tokenHierarchy);
                int sourceEnd = sourceStart + token.length();

                String text = token.text().toString();
                skipNewline = false;
                if (text.endsWith("-")) { // NOI18N
                    skipNewline = true;
                }
                ems.add(snapshot.create(sourceStart, sourceEnd - sourceStart - (skipNewline ? 1 : 0), RUBY_MIMETYPE));

                // Make code sanitizing work better:  buffer.append("\n).to_s;"); // NOI18N
                ems.add(snapshot.create(").to_s;", RUBY_MIMETYPE)); // NOI18N
            }
        }

        // Close off the class
        // eruby also ends with this statement: _buf.to_s
        ems.add(snapshot.create("\nend\n", RUBY_MIMETYPE)); // NOI18N

        return ems;
    }
}
