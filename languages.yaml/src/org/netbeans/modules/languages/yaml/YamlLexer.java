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

package org.netbeans.modules.languages.yaml;

import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;


/**
 *
 * @author Tor Norbye
 */
public final class YamlLexer implements Lexer<YamlTokenId> {
    private static final int EOF = LexerInput.EOF;
    private final LexerInput input;
    private final TokenFactory<YamlTokenId> tokenFactory;

    /**
     * A Lexer for ruby strings
     * @param substituting If true, handle substitution rules for double quoted strings, otherwise
     *    single quoted strings.
     */
    public YamlLexer(LexerRestartInfo<YamlTokenId> info) {
        this.input = info.input();
        this.tokenFactory = info.tokenFactory();
        assert (info.state() == null); // passed argument always null
    }

    public Object state() {
        return null;
    }

    public Token<YamlTokenId> nextToken() {
        // TODO - support embedded Ruby in <% %> tags.
        // This is used in fixtures files from Rails for example; see
        //   http://api.rubyonrails.com/classes/Fixtures.html
        while (true) {
            int ch = input.read();

            switch (ch) {
                case EOF: {
                    if (input.readLength() > 0) {
                        return token(YamlTokenId.TEXT);
                    } else {
                        return null;
                    }
                }
                    
                case '#': {
                    if (input.readLength() > 1) {
                        input.backup(1);
                        return token(YamlTokenId.TEXT);
                    }

                    ch = input.read();
                    while (!(ch == EOF || ch == '\r' || ch == '\n')) {
                        ch = input.read();
                    }
                    if (ch != EOF) {
                        input.backup(1);
                    }
                    return token(YamlTokenId.COMMENT);
                }
            }
        }
    }

    private Token<YamlTokenId> token(YamlTokenId id) {
        return tokenFactory.createToken(id);
    }

    public void release() {
    }
}
