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

package org.netbeans.modules.groovy.gsp.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.groovy.editor.api.lexer.GroovyTokenId;
import org.netbeans.modules.groovy.gsp.lexer.GspTokenId;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author Petr Hejl
 */
public class GroovyEmbeddingProvider extends EmbeddingProvider {

    private static final Logger LOG = Logger.getLogger(GroovyEmbeddingProvider.class.getName());
    
    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        if (GspTokenId.MIME_TYPE.equals(snapshot.getMimeType())) {
            List<Embedding> embeddings = translate(snapshot);
            if(embeddings.isEmpty()) {
                return Collections.<Embedding>emptyList();
            } else {
                return Collections.singletonList(Embedding.create(embeddings));
            }
        } else {
            LOG.warning("Unexpected snapshot type: '" + snapshot.getMimeType() + "'; expecting '" + GspTokenId.MIME_TYPE + "'"); //NOI18N
            return Collections.<Embedding>emptyList();
        }
    }

    @Override
    public int getPriority() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void cancel() {
        // FIXME parsing API
    }

    private static TokenSequence<? extends TokenId> getTokenSequence(Snapshot snapshot) {
        Language<? extends TokenId> l = Language.find(snapshot.getMimeType());
        if (l != null) {
            return TokenHierarchy.create(snapshot.getText(), l).tokenSequence();
        } else {
            return null;
        }
    }

    private List<Embedding> translate(Snapshot snapshot) {
        TokenSequence<? extends TokenId> tokenSequence = getTokenSequence(snapshot);
        if (tokenSequence == null) {
            return Collections.emptyList();
        }
        
        List<Embedding> embeddings = new ArrayList<Embedding>();

        @SuppressWarnings("unchecked")
        TokenSequence<? extends GspTokenId> gspTokenSequence = (TokenSequence<? extends GspTokenId>) tokenSequence;
        translate(snapshot, gspTokenSequence, embeddings);

        return embeddings;
    }
    /** Perform groovy translation
     * @param outputBuffer The buffer to emit the translation to
     * @param tokenHierarchy The token hierarchy for the RHTML code
     * @param tokenSequence  The token sequence for the RHTML code
     */
    private void translate(Snapshot snapshot, /*TokenHierarchy<Document> tokenHierarchy,*/
            TokenSequence<? extends GspTokenId> tokenSequence, List<Embedding> embeddings) {
        //StringBuilder buffer = outputBuffer;

        embeddings.add(snapshot.create("def _buf ='';", GroovyTokenId.GROOVY_MIME_TYPE));
//        buffer.append("def _buf ='';"); // NOI18N
//        codeBlocks.add(new CodeBlockData(0, 0, 0, buffer.length()));

        boolean skipNewline = false;
        while(tokenSequence.moveNext()) {
            Token<? extends GspTokenId> token = tokenSequence.token();

            if (token.id() == GspTokenId.HTML){
                int sourceStart = tokenSequence.offset(); //token.offset(tokenHierarchy);
                int sourceEnd = sourceStart + token.length();
                //int generatedStart = buffer.length();

                CharSequence charSequence = token.text();
                String text = charSequence == null ? "" : charSequence.toString();

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
                    embeddings.add(snapshot.create(sourceStart, i, GroovyTokenId.GROOVY_MIME_TYPE));
                    //buffer.append(text.substring(0, i));
                    text = text.substring(i);
                }

                embeddings.add(snapshot.create("_buf += \"\"\"", GroovyTokenId.GROOVY_MIME_TYPE));
                //buffer.append("_buf += \"\"\""); // NOI18N
                if (skipNewline && text.startsWith("\n")) { // NOI18N
                    text = text.substring(1);
                    sourceEnd--;
                }
                // FIXME what to do with this ?
                // FIXME this does not seem to be correct
                embeddings.add(snapshot.create(text.replace("\"", "\\\""), GroovyTokenId.GROOVY_MIME_TYPE));
                //text = text.replace("\"", "\\\"");
                //buffer.append(text);

                embeddings.add(snapshot.create("\"\"\";", GroovyTokenId.GROOVY_MIME_TYPE));
                //buffer.append("\"\"\";"); // NOI18N
//                int generatedEnd = buffer.length();

//                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart, generatedEnd);
//                codeBlocks.add(blockData);

                skipNewline = false;
            } else if (token.id() == GspTokenId.GROOVY){
                int sourceStart = tokenSequence.offset();//token.offset(tokenHierarchy);
                //int sourceEnd = sourceStart + token.length();
                //int generatedStart = buffer.length();

                String text = token.text().toString();
                // handle <%-- foo --%> and %{-- bar --%} comments
                String trimmedText = text.trim();
                if (trimmedText.startsWith("--") && trimmedText.endsWith("--")) { // NOI18N
                    int first = text.indexOf("--");
                    int last = text.lastIndexOf("--");
                    if (first != last && (last - 2) > 0) {
                        embeddings.add(snapshot.create("/*", GroovyTokenId.GROOVY_MIME_TYPE));
                        //buffer.append("/*");
                        embeddings.add(snapshot.create(sourceStart + first + 2, last - 2, GroovyTokenId.GROOVY_MIME_TYPE));
                        //buffer.append(text.substring(first + 2, last));
                        embeddings.add(snapshot.create("*/", GroovyTokenId.GROOVY_MIME_TYPE));
                        //buffer.append("*/");
                    }
                } else {
                    embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
                    //buffer.append(text);
                    embeddings.add(snapshot.create(";", GroovyTokenId.GROOVY_MIME_TYPE));
                    //buffer.append(';');
                }
                skipNewline = false;

//                int generatedEnd = buffer.length();
//
//                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart, generatedEnd);
//                codeBlocks.add(blockData);

                skipNewline = false;
            } else if (token.id() == GspTokenId.GROOVY_EXPR) {
                embeddings.add(snapshot.create("_buf += (", GroovyTokenId.GROOVY_MIME_TYPE));
                //buffer.append("_buf += ("); // NOI18N
                int sourceStart = tokenSequence.offset();//token.offset(tokenHierarchy);
                //int sourceEnd = sourceStart + token.length();
                //int generatedStart = buffer.length();

                String text = token.text().toString();
                skipNewline = false;
                embeddings.add(snapshot.create(sourceStart, text.length(), GroovyTokenId.GROOVY_MIME_TYPE));
                //buffer.append(text);

                embeddings.add(snapshot.create(";)", GroovyTokenId.GROOVY_MIME_TYPE));
//                buffer.append(';');
//                int generatedEnd = buffer.length();
//
//                CodeBlockData blockData = new CodeBlockData(sourceStart, sourceEnd, generatedStart, generatedEnd);
//                codeBlocks.add(blockData);
//                buffer.append(")"); // NOI18N
            }
        }

    }

    public static final class Factory extends TaskFactory {
        public Factory() {
            // no-op
        }

        public @Override Collection<? extends SchedulerTask> create(Snapshot snapshot) {
            if (!GspTokenId.MIME_TYPE.equals(snapshot.getMimeType())) {
                return Collections.<SchedulerTask>emptyList();
            }

            return Collections.singleton(new GroovyEmbeddingProvider());
        }
    }

}
