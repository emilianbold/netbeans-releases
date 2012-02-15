/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.indent;

import java.util.*;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.javascript2.editor.lexer.JsTokenId;

/**
 *
 * @author Petr Hejl
 */
public final class FormatTokenStream implements Iterable<FormatToken> {
    
    private final Map<Integer, FormatToken> tokenPosition = new TreeMap<Integer, FormatToken>();
    
    private FormatToken firstToken;
    
    private FormatToken lastToken;

    private FormatTokenStream() {
    }   
    
    public static FormatTokenStream create(TokenSequence<? extends JsTokenId> ts, int start, int end) {
        FormatTokenStream ret = new FormatTokenStream();
        int diff = ts.move(start);
        if (diff <= 0) {
            ts.movePrevious();
        }
        
        while (ts.moveNext() && ts.offset() < end) {
            Token<? extends JsTokenId> token = ts.token();
            JsTokenId id = token.id();
            switch (id) {
                case EOL:
                    ret.addToken(FormatToken.forAny(FormatToken.Kind.EOL, ts.offset(), token.text()));
                    break;
                case WHITESPACE:
                    ret.addToken(FormatToken.forAny(FormatToken.Kind.WHITESPACE, ts.offset(), token.text()));
                    break;
                case BLOCK_COMMENT:
                    ret.addToken(FormatToken.forAny(FormatToken.Kind.BLOCK_COMMENT, ts.offset(), token.text()));
                    break;
                case DOC_COMMENT:
                    ret.addToken(FormatToken.forAny(FormatToken.Kind.DOC_COMMENT, ts.offset(), token.text()));
                    break;
                case LINE_COMMENT:
                    ret.addToken(FormatToken.forFormat(FormatToken.Kind.BEFORE_LINE_COMMENT));
                    ret.addToken(FormatToken.forAny(FormatToken.Kind.LINE_COMMENT, ts.offset(), token.text()));
                    break;
                case OPERATOR_GREATER:
                case OPERATOR_LOWER:
                case OPERATOR_EQUALS:
                case OPERATOR_EQUALS_EXACTLY:
                case OPERATOR_LOWER_EQUALS:
                case OPERATOR_GREATER_EQUALS:
                case OPERATOR_NOT_EQUALS:
                case OPERATOR_NOT_EQUALS_EXACTLY:
                case OPERATOR_AND:
                case OPERATOR_OR:
                case OPERATOR_MULTIPLICATION:
                case OPERATOR_DIVISION:
                case OPERATOR_BITWISE_AND:
                case OPERATOR_BITWISE_OR:
                case OPERATOR_BITWISE_XOR:
                case OPERATOR_MODULUS:
                case OPERATOR_LEFT_SHIFT_ARITHMETIC:
                case OPERATOR_RIGHT_SHIFT_ARITHMETIC:
                case OPERATOR_RIGHT_SHIFT:
                    ret.addToken(FormatToken.forFormat(FormatToken.Kind.BEFORE_BINARY_OPERATOR));
                    ret.addToken(FormatToken.forText(ts.offset(), token.text()));
                    ret.addToken(FormatToken.forFormat(FormatToken.Kind.AFTER_BINARY_OPERATOR));
                    break;
                case OPERATOR_ASSIGNMENT:
                    ret.addToken(FormatToken.forFormat(FormatToken.Kind.BEFORE_ASSIGNMENT_OPERATOR));
                    ret.addToken(FormatToken.forText(ts.offset(), token.text()));
                    ret.addToken(FormatToken.forFormat(FormatToken.Kind.AFTER_ASSIGNMENT_OPERATOR));
                    break;
                case OPERATOR_COMMA:
                    ret.addToken(FormatToken.forFormat(FormatToken.Kind.BEFORE_COMMA));
                    ret.addToken(FormatToken.forText(ts.offset(), token.text()));
                    ret.addToken(FormatToken.forFormat(FormatToken.Kind.AFTER_COMMA));
                    break;
                default:
                    ret.addToken(FormatToken.forText(ts.offset(), token.text()));
                    break;
            }
        }
        return ret;
    }
    
    public FormatToken getToken(int offset) {
        return tokenPosition.get(offset);
    }

    @Override
    public Iterator<FormatToken> iterator() {
        return new FormatTokenIterator();
    }
    
    public List<FormatToken> getTokens() {
        List<FormatToken> tokens = new ArrayList<FormatToken>((int) (tokenPosition.size() * 1.5));
        for (FormatToken token : this) {
            tokens.add(token);
        }
        return tokens;
    }
    
    private void addToken(FormatToken token) {
        if (firstToken == null) {
            firstToken = token;
            lastToken = token;
        } else {
            lastToken.setNext(token);
            lastToken = token;
        }

        if (token.getOffset() >= 0) {
            tokenPosition.put(token.getOffset(), token);
        }
    }
    
    private class FormatTokenIterator implements Iterator<FormatToken> {

        private FormatToken current = firstToken;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public FormatToken next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            FormatToken ret = current;
            current = current.next();
            return ret;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Remove operation not supported.");
        }
        
    }
}
