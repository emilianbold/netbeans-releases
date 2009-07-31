/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby;

import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.ruby.lexer.LexUtilities;
import org.netbeans.modules.ruby.lexer.RubyTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Implementation of BracesMatcher interface for Ruby. It is based on original code
 * from RubyKeystrokeHandler.findMatching
 *
 * @author Marek Slama
 */
public final class RubyBracesMatcher implements BracesMatcher {

    MatcherContext context;

    public RubyBracesMatcher (MatcherContext context) {
        this.context = context;
    }

    public int [] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();
            TokenSequence<?extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends RubyTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }
                
                TokenId id = token.id();
                
                if (id == RubyTokenId.QUOTED_STRING_BEGIN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.QUOTED_STRING_END) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.STRING_BEGIN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.STRING_END) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.REGEXP_BEGIN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.REGEXP_END) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.LPAREN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.RPAREN) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.LBRACE) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.RBRACE) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.LBRACKET) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id == RubyTokenId.DO && !LexUtilities.isEndmatchingDo(doc, ts.offset())) {
                    // No matching dot for "do" used in conditionals etc.
                    return null;
                } else if (id == RubyTokenId.RBRACKET) {
                    return new int [] { ts.offset(), ts.offset() + token.length() };
                } else if (id.primaryCategory().equals("keyword")) {
                    if (LexUtilities.isBeginToken(id, doc, ts)) {
                        return new int [] { ts.offset(), ts.offset() + token.length() };
                    } else if ((id == RubyTokenId.END) || LexUtilities.isIndentToken(id)) { // Find matching block
                        return new int [] { ts.offset(), ts.offset() + token.length() };
                    }
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    public int [] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();

            TokenSequence<?extends RubyTokenId> ts = LexUtilities.getRubyTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends RubyTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }
                
                TokenId id = token.id();
                
                OffsetRange r;
                if (id == RubyTokenId.QUOTED_STRING_BEGIN) {
                    // Heredocs should be treated specially
                    if (token.text().toString().startsWith("<<")) {
                        r = LexUtilities.findHeredocEnd(ts, token);
                        return new int [] {r.getStart(), r.getEnd() };
                    }
                    r = LexUtilities.findFwd(doc, ts, RubyTokenId.QUOTED_STRING_BEGIN,
                        RubyTokenId.QUOTED_STRING_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.QUOTED_STRING_END) {
                    String s = token.text().toString();
                    if (!"\"".equals(s) && !"\'".equals(s) && !")".equals(s)) {
                        r = LexUtilities.findHeredocBegin(ts, token);
                        if (r != OffsetRange.NONE) {
                            return new int [] {r.getStart(), r.getEnd() };
                        }
                        ts.move(offset);
                    }

                    r = LexUtilities.findBwd(doc, ts, RubyTokenId.QUOTED_STRING_BEGIN,
                        RubyTokenId.QUOTED_STRING_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.STRING_BEGIN) {
                    // Heredocs should be treated specially
                    if (token.text().toString().startsWith("<<")) {
                        r = LexUtilities.findHeredocEnd(ts, token);
                        return new int [] {r.getStart(), r.getEnd() };
                    }
                    r = LexUtilities.findFwd(doc, ts, RubyTokenId.STRING_BEGIN, RubyTokenId.STRING_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.STRING_END) {
                    String s = token.text().toString();
                    if (!"\"".equals(s) && !"\'".equals(s) && !")".equals(s)) {
                        r = LexUtilities.findHeredocBegin(ts, token);
                        if (r != OffsetRange.NONE) {
                            return new int [] {r.getStart(), r.getEnd() };
                        }
                        ts.move(offset);
                    }
                    r = LexUtilities.findBwd(doc, ts, RubyTokenId.STRING_BEGIN, RubyTokenId.STRING_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.REGEXP_BEGIN) {
                    r = LexUtilities.findFwd(doc, ts, RubyTokenId.REGEXP_BEGIN, RubyTokenId.REGEXP_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.REGEXP_END) {
                    r = LexUtilities.findBwd(doc, ts, RubyTokenId.REGEXP_BEGIN, RubyTokenId.REGEXP_END);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.LPAREN) {
                    r = LexUtilities.findFwd(doc, ts, RubyTokenId.LPAREN, RubyTokenId.RPAREN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.RPAREN) {
                    r = LexUtilities.findBwd(doc, ts, RubyTokenId.LPAREN, RubyTokenId.RPAREN);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.LBRACE) {
                    r = LexUtilities.findFwd(doc, ts, RubyTokenId.LBRACE, RubyTokenId.RBRACE);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.RBRACE) {
                    r = LexUtilities.findBwd(doc, ts, RubyTokenId.LBRACE, RubyTokenId.RBRACE);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.LBRACKET) {
                    r = LexUtilities.findFwd(doc, ts, RubyTokenId.LBRACKET, RubyTokenId.RBRACKET);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == RubyTokenId.DO && !LexUtilities.isEndmatchingDo(doc, ts.offset())) {
                    // No matching dot for "do" used in conditionals etc.
                    return null;
                } else if (id == RubyTokenId.RBRACKET) {
                    r = LexUtilities.findBwd(doc, ts, RubyTokenId.LBRACKET, RubyTokenId.RBRACKET);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id.primaryCategory().equals("keyword")) {
                    if (LexUtilities.isBeginToken(id, doc, ts)) {
                        r = LexUtilities.findEnd(doc, ts);
                        return new int [] {r.getStart(), r.getEnd() };
                    } else if ((id == RubyTokenId.END) || LexUtilities.isIndentToken(id)) { // Find matching block
                        r = LexUtilities.findBegin(doc, ts);
                        return new int [] {r.getStart(), r.getEnd() };
                    }
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }
    
}
