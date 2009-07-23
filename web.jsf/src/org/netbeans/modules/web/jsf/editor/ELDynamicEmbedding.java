/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor;

import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.el.lexer.api.ELTokenId;
import org.netbeans.modules.html.editor.gsf.api.HtmlParserResult;

/**
 *
 * @author marekfukala
 */
public class ELDynamicEmbedding {
    
    /*  hmm, this doesn't look like it can ever work! I cannot create multiple
    partial embeddings for one token. For example for a text token with
    ... ${"sss"}    ${} I need to create two embedding with different
    start and end skip length which doesn't seem to be supported by lexer.
    ...ok it works, but I had to tweak the html lexer so it creates separated TEXT tokens
    for EL content.
    'A${el}B' creates three TEXT tokens 'A', '${el}', 'B' */
    public static void updateEmbedding(final HtmlParserResult result) {
        final BaseDocument doc = (BaseDocument) result.getSnapshot().getSource().getDocument(true);
        doc.runAtomic(new Runnable() {

            public void run() {
                TokenHierarchy th = TokenHierarchy.get(doc);
                TokenSequence<HTMLTokenId> ts = th.tokenSequence();
                ts.moveStart();
                while (ts.moveNext()) {
                    Token token = ts.token();
                    if (token.length() < 2) {
                        continue; //at least ${ must be in the token
                    }
                    if (token.id() == HTMLTokenId.TEXT) {
                        //in a text
                        createELEmbedding(ts, 0, 0);
                    } else if (token.id() == HTMLTokenId.VALUE) {
                        //attribute value
                        int startSkipLen = 0;
                        int endSkipLen = 0;
                        CharSequence tokenImage = token.text();
                        if ((tokenImage.charAt(0) == '"' || //NOI18N
                                tokenImage.charAt(0) == '\'')) { //NOI18N
                            startSkipLen = 1;
                        }
                        if ((tokenImage.charAt(tokenImage.length() - 1) == '"' || //NOI18N
                                tokenImage.charAt(tokenImage.length() - 1) == '\'')) { //NOI18N
                            endSkipLen = 1;
                        }
                        createELEmbedding(ts, startSkipLen, endSkipLen);
                    }
                }
            }
        });

    }

    private static boolean createELEmbedding(TokenSequence ts, int startSkipLen, int endSkipLen) {
        Token token = ts.token();
        CharSequence tokenImage = token.text();

        if (tokenImage.length() - startSkipLen - endSkipLen > 2) { //at least two chars in the token
            if ((tokenImage.charAt(startSkipLen) == '$' || tokenImage.charAt(startSkipLen) == '#') && //NOI18N
                    tokenImage.charAt(startSkipLen + 1) == '{' && //NOI18N
                    tokenImage.charAt(token.length() - 1 - endSkipLen) == '}') { //NOI18N

                return ts.createEmbedding(ELTokenId.language(), startSkipLen + 2, endSkipLen + 1); //+ '${'.len - '}'.len - those must not be a part of the EL
            }
        }
        return false;
    }
}
