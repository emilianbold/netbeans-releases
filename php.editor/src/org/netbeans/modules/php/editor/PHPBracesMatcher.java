/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor;

import java.util.Arrays;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.spi.editor.bracesmatching.BracesMatcher;
import org.netbeans.spi.editor.bracesmatching.MatcherContext;

/**
 * Implementation of BracesMatcher interface for PHP. It is based on original code
 * from PHPBracketCompleter.findMatching
 *
 * @author Marek Slama
 */
public final class PHPBracesMatcher implements BracesMatcher {

    MatcherContext context;

    public PHPBracesMatcher(MatcherContext context) {
        this.context = context;
    }

    @Override
    public int [] findOrigin() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();

            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends PHPTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }

                TokenId id = token.id();

                if (LexUtilities.textEquals(token.text(), '(')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), ')')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (id == PHPTokenId.PHP_CURLY_OPEN) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (id == PHPTokenId.PHP_CURLY_CLOSE) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), '[')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), ']')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), '$', '{')) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (LexUtilities.textEquals(token.text(), ':')) {
                    do {
                        ts.movePrevious();
                        token = LexUtilities.findPreviousToken(ts,
                                Arrays.asList(PHPTokenId.PHP_IF, PHPTokenId.PHP_ELSE, PHPTokenId.PHP_ELSEIF,
                                PHPTokenId.PHP_FOR, PHPTokenId.PHP_FOREACH, PHPTokenId.PHP_WHILE, PHPTokenId.PHP_SWITCH,
                                PHPTokenId.PHP_OPENTAG, PHPTokenId.PHP_CURLY_CLOSE, PHPTokenId.PHP_CASE,
                                PHPTokenId.PHP_TOKEN));
                        id = token.id();
                    } while (id == PHPTokenId.PHP_TOKEN && !":".equals(token.text().toString()));
                    if (id == PHPTokenId.PHP_IF || id == PHPTokenId.PHP_ELSE || id == PHPTokenId.PHP_ELSEIF
                            || id == PHPTokenId.PHP_FOR || id == PHPTokenId.PHP_FOREACH || id == PHPTokenId.PHP_WHILE
                            || id == PHPTokenId.PHP_SWITCH) {
                        ts.move(offset);
                        ts.moveNext();
                        token = ts.token();
                        return new int [] {ts.offset(), ts.offset() + token.length()};
                    }
                } else if (id == PHPTokenId.PHP_ENDFOR || id == PHPTokenId.PHP_ENDFOREACH
                        || id == PHPTokenId.PHP_ENDIF || id == PHPTokenId.PHP_ENDSWITCH
                        || id == PHPTokenId.PHP_ENDWHILE) {
                    return new int [] {ts.offset(), ts.offset() + token.length()};
                } else if (id == PHPTokenId.PHP_ELSEIF || id == PHPTokenId.PHP_ELSE) {
                    while (token.id() != PHPTokenId.PHP_CURLY_OPEN && !":".equals(token.text().toString()) && ts.moveNext()) {
                            token = LexUtilities.findNextToken(ts, Arrays.asList(PHPTokenId.PHP_TOKEN, PHPTokenId.PHP_CURLY_OPEN));
                    }
                    if (token.id() == PHPTokenId.PHP_TOKEN && ":".equals(token.text().toString()) && ts.moveNext()) {
                        ts.move(offset);
                        ts.moveNext();
                        token = ts.token();
                        return new int [] {ts.offset(), ts.offset() + token.length()};
                    }

                }

            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

    @Override
    public int [] findMatches() throws InterruptedException, BadLocationException {
        ((AbstractDocument) context.getDocument()).readLock();
        try {
            BaseDocument doc = (BaseDocument) context.getDocument();
            int offset = context.getSearchOffset();

            TokenSequence<?extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, offset);

            if (ts != null) {
                ts.move(offset);

                if (!ts.moveNext()) {
                    return null;
                }

                Token<?extends PHPTokenId> token = ts.token();

                if (token == null) {
                    return null;
                }

                TokenId id = token.id();

                OffsetRange r;
                if (LexUtilities.textEquals(token.text(), '(')) {
                    r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_TOKEN, '(', PHPTokenId.PHP_TOKEN, ')');
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (LexUtilities.textEquals(token.text(), ')')) {
                    r = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_TOKEN, '(', PHPTokenId.PHP_TOKEN, ')');
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == PHPTokenId.PHP_CURLY_OPEN) {
                    r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_CURLY_OPEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == PHPTokenId.PHP_CURLY_CLOSE) {
                    r = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_CURLY_OPEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (LexUtilities.textEquals(token.text(), '[')) {
                    r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_TOKEN, '[', PHPTokenId.PHP_TOKEN, ']');
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (LexUtilities.textEquals(token.text(), ']')) {
                    r = LexUtilities.findBwd(doc, ts, PHPTokenId.PHP_TOKEN, '[', PHPTokenId.PHP_TOKEN, ']');
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (LexUtilities.textEquals(token.text(), '$', '{')) {
                    r = LexUtilities.findFwd(doc, ts, PHPTokenId.PHP_TOKEN, '{', PHPTokenId.PHP_CURLY_CLOSE, '}');
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (LexUtilities.textEquals(token.text(), ':')) {
                    r = LexUtilities.findFwdAlternativeSyntax(doc, ts, token);
                    return new int [] {r.getStart(), r.getEnd() };
                } else if (id == PHPTokenId.PHP_ENDFOR || id == PHPTokenId.PHP_ENDFOREACH
                        || id == PHPTokenId.PHP_ENDIF || id == PHPTokenId.PHP_ENDSWITCH
                        || id == PHPTokenId.PHP_ENDWHILE || id == PHPTokenId.PHP_ELSEIF
                        || id == PHPTokenId.PHP_ELSE) {
                    r = LexUtilities.findBwdAlternativeSyntax(doc, ts, token);
                    return new int [] {r.getStart(), r.getEnd() };
                }
            }
            return null;
        } finally {
            ((AbstractDocument) context.getDocument()).readUnlock();
        }
    }

}
