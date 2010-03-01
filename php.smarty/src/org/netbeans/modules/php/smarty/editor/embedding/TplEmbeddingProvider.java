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
package org.netbeans.modules.php.smarty.editor.embedding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.text.Document;
import org.netbeans.api.lexer.InputAttributes;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.php.smarty.editor.lexer.TplTokenId;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
import org.netbeans.modules.php.smarty.editor.utlis.EmbeddingUtils;
//import org.netbeans.modules.php.smarty.editor.utils.EditorUtils;

/**
 * Provides code completion for T_HTML tokens
 *
 */
public class TplEmbeddingProvider extends EmbeddingProvider {

    /**
     * Virtual code for entering between two commands of the same language.
     */
    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        // for sending atributes for FuseLexer (dynamic variables)
        Document doc = snapshot.getSource().getDocument(true);
        InputAttributes inputAttributes = new InputAttributes();
        doc.putProperty(InputAttributes.class, inputAttributes);

        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), TplTopTokenId.language());
        TokenSequence<TplTopTokenId> sequence = th.tokenSequence(TplTopTokenId.language());

        if (sequence == null) {
            Logger.getLogger("FuseEmbeddingProvider").warning(
                    "TokenHierarchy.tokenSequence(TplTopTokenId.language()) == null " +
                    "for static immutable Fuse TokenHierarchy!\nFile = '" +
                    snapshot.getSource().getFileObject().getPath() +
                    "' ;snapshot mimepath='" + snapshot.getMimePath() + "'");

            return Collections.emptyList();
        }

        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<Embedding>();

        int from = -1;
        int len = 0;
        int state = -1;
        boolean changed = false;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == TplTopTokenId.T_HTML) {
                if (from < 0) {
                    from = sequence.offset();
                }
                len += t.length();
                if (state != 1) {
                    changed = true;
                    state = 1;
                }
            } else if (t.id() == TplTopTokenId.T_SMARTY) {
//                TokenHierarchy<CharSequence> th2 = TokenHierarchy.create(t.text(), snapshot);
                TokenHierarchy<CharSequence> th2 = EmbeddingUtils.createTplTokenHierarchy(t.text(), snapshot);
                TokenSequence<TplTokenId> sequence2 = th2.tokenSequence(TplTokenId.language());
                int lenghtOfIngored = 0;
                while (sequence2.moveNext()) {
                    Token t2 = sequence2.token();
                    if (t2.id() == TplTokenId.IDENTIFIER) {
                        if (from < 0) {
                            from = sequence.offset() + lenghtOfIngored;
                        }
                        len += t2.length();
                        if (state != 2) {
                            changed = true;
                            state = 2;
                        }
                    } else {
                        lenghtOfIngored = t2.text().length();
                    }
                }
//                sequence2.moveStart();
            } else if (t.id() == TplTopTokenId.T_SMARTY_OPEN_DELIMITER) {
                embeddings.add(snapshot.create("<?", "text/x-php5"));
//                embeddings.add(snapshot.create("; ", "text/x-php5"));
//                embeddings.add(snapshot.create(GENERATED_CODE, "text/x-php5"));
                state = -1;
                changed = false;
            } else if (t.id() == TplTopTokenId.T_SMARTY_CLOSE_DELIMITER) {
                embeddings.add(snapshot.create("; ?>", "text/x-php5"));
                state = -1;
                changed = false;
            }
            if (changed) {
                if (from >= 0) {
                    if (state == 1) {
                        embeddings.add(snapshot.create(from, len, "text/x-php5")); //NOI18N
                        embeddings.add(snapshot.create(GENERATED_CODE, "text/x-php5"));
                    } else {
//                        embeddings.add(snapshot.create(";?><?php; ", "text/x-php5"));
                        embeddings.add(snapshot.create(from, len, "text/x-php5")); //NOI18N
//                        embeddings.add(snapshot.create("; ", "text/x-php5"));
//                        embeddings.add(snapshot.create(GENERATED_CODE, "text/x-php5"));
                    }
                }

                from = -1;
                len = 0;
            }
        }

        if (from >= 0) {
            embeddings.add(snapshot.create(from, len, "text/x-php5")); //NOI18N
        }

        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 90;
    }

    @Override
    public void cancel() {
        //do nothing
    }

    /**
     * Factory for creating new FuseEmbeddingProvider.
     */
    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new TplEmbeddingProvider());
        }
    }
}
