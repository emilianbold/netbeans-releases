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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.EmbeddingProvider;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;
import org.netbeans.modules.php.smarty.editor.lexer.TplTopTokenId;
//import org.netbeans.modules.php.smarty.editor.utils.EditorUtils;

/**
 * Provides model for TPL files.
 *
 */
public class TplEmbeddingProvider extends EmbeddingProvider {

    public static final String GENERATED_CODE = "@@@"; //NOI18N

    @Override
    public List<Embedding> getEmbeddings(Snapshot snapshot) {
        TokenHierarchy<CharSequence> th = TokenHierarchy.create(snapshot.getText(), TplTopTokenId.language());
        TokenSequence<TplTopTokenId> sequence = th.tokenSequence(TplTopTokenId.language());

        //issue #159775 logging >>>
        if(sequence == null) {
            Logger.getLogger("TplEmbeddingProvider").warning(
                    "TokenHierarchy.tokenSequence(TplTopTokenId.language()) == null " +
                    "for static immutable TPL TokenHierarchy!\nFile = '"+
                    snapshot.getSource().getFileObject().getPath() +
                    "' ;snapshot mimepath='" + snapshot.getMimePath() + "'");

            return Collections.emptyList();
        }
        //<<< end of the logging

        sequence.moveStart();
        List<Embedding> embeddings = new ArrayList<Embedding>();

        int from = -1;
        int len = 0;
        while (sequence.moveNext()) {
            Token t = sequence.token();
            if (t.id() == TplTopTokenId.T_SMARTY) {
                if(from < 0) {
                    from = sequence.offset();
                }
                len += t.length();
            } else {
                if (from < 0) {
                        from = sequence.offset();
                    }
                len += t.length();
                if(from >= 0) {
                    embeddings.add(snapshot.create(from, len, "text/html")); //NOI18N
                    embeddings.add(snapshot.create(GENERATED_CODE, "text/html"));
                } 

                from = -1;
                len = 0;
            }
        }

        if(from >= 0) {
            embeddings.add(snapshot.create(from, len, "text/html")); //NOI18N
        }

        if (embeddings.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.singletonList(Embedding.create(embeddings));
        }
    }

    @Override
    public int getPriority() {
        return 130;
    }

    @Override
    public void cancel() {
        //do nothing
    }

    public static final class Factory extends TaskFactory {

        @Override
        public Collection<SchedulerTask> create(final Snapshot snapshot) {
            return Collections.<SchedulerTask>singletonList(new TplEmbeddingProvider());
        }
    }
}