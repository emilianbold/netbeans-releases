/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.spellchecker.bindings.ruby;

import javax.swing.text.BadLocationException;
import org.netbeans.api.html.lexer.HTMLTokenId;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.modules.ruby.rhtml.lexer.api.RhtmlTokenId;

/**
 * Tokenize RHTML for spell checking: Spell check Ruby comments AND HTML text content!
 *
 * @author Tor Norbye
 */
public class RhtmlTokenList extends RubyTokenList {
    public RhtmlTokenList(BaseDocument doc) {
        super(doc);
    }

    /** Given a sequence of Ruby tokens, return the next span of eligible comments */
    @Override
    protected int[] findNextSpellSpan(TokenSequence<? extends TokenId> ts, int offset) throws BadLocationException {
        if (ts == null) {
            return new int[]{-1, -1};
        }

        int diff = ts.move(offset);

        while (ts.moveNext()) {
            TokenId id = ts.token().id();
            /*if (id == RhtmlTokenId.RUBYCOMMENT) {
            return new int[] {ts.offset(), ts.offset() + ts.token().length()};
            } else*/
            if (id == RhtmlTokenId.HTML) {
                // Tokenize the text and
                TokenSequence<? extends HTMLTokenId> t = ts.embedded(HTMLTokenId.language());
                if (t != null) {
                    t.move(offset);
                    while (t.moveNext()) {
                        TokenId tid = t.token().id();
                        if (tid == HTMLTokenId.TEXT) {
                            return new int[]{t.offset(), t.offset() + t.token().length()};
                        }
                    }
                }
            } else if (id == RhtmlTokenId.RUBY || id == RhtmlTokenId.RUBY_EXPR) {
                TokenSequence<? extends RubyTokenId> t = ts.embedded(RubyTokenId.language());
                if (t != null) {
                    // Tokenize Ruby segment
                    int[] span = super.findNextSpellSpan(t, Math.max(offset,ts.offset()));
                    if (span[0] != -1) {
                        return span;
                    }
                }
            }
        }

        return new int[]{-1, -1};
    }

}