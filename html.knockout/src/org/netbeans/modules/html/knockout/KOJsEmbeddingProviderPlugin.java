/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.knockout;

import java.util.List;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.html.editor.spi.embedding.JsEmbeddingProviderPlugin;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.Snapshot;

/**
 * Knockout javascript virtual source extension
 *
 * @author marekfukala
 */
@MimeRegistration(mimeType = "text/html", service = JsEmbeddingProviderPlugin.class)
public class KOJsEmbeddingProviderPlugin extends JsEmbeddingProviderPlugin {

    private final Language JS_LANGUAGE;

    public KOJsEmbeddingProviderPlugin() {
        JS_LANGUAGE = Language.find(KOUtils.JAVASCRIPT_MIMETYPE); //NOI18N
    }

    @Override
    public boolean processToken(Snapshot snapshot, TokenSequence<HTMLTokenId> ts, List<Embedding> embeddings) {
        boolean processed = false;
        switch (ts.token().id()) {
            case VALUE:
                TokenSequence<KODataBindTokenId> embedded = ts.embedded(KODataBindTokenId.language());
                if (embedded != null) {
                    embedded.moveStart();
                    while (embedded.moveNext()) {
                        if (embedded.embedded(JS_LANGUAGE) != null) {
                            processed = true;
                            //has javascript embedding
                            embeddings.add(snapshot.create("(function(){\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
                            embeddings.add(snapshot.create("ko.$bindings.", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N

                            CharSequence seq = embedded.token().text();
                            int emptyLength = 0;
                            for (int i = 0; i < seq.length(); i++) {
                                if (Character.isWhitespace(seq.charAt(i))) {
                                    emptyLength++;
                                } else {
                                    break;
                                }
                            }
                            if (emptyLength < seq.length()) {
                                embeddings.add(snapshot.create(embedded.offset() + emptyLength, embedded.token().length() - emptyLength, KOUtils.JAVASCRIPT_MIMETYPE));
                            } else {
                                embeddings.add(snapshot.create(embedded.offset(), embedded.token().length(), KOUtils.JAVASCRIPT_MIMETYPE));
                            }

                            embeddings.add(snapshot.create("\n});\n", KOUtils.JAVASCRIPT_MIMETYPE)); //NOI18N
                        }
                    }
                }
        }
        return processed;
    }
}
