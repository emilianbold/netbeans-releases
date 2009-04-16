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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.cnd.lexer;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;


/**
 *
 * @author Jan Jancura
 */
class BatLexer implements Lexer<BatTokenId> {

    private static Set<String> keywords = new HashSet<String> ();
    private static Set<String> commands = new HashSet<String> ();

    static {
        keywords.add ("aux");
        keywords.add ("call");
        keywords.add ("choice");
        keywords.add ("defined");
        keywords.add ("do");
        keywords.add ("else");
        keywords.add ("errorlevel");
        keywords.add ("exist");
        keywords.add ("endlocal");
        keywords.add ("for");
        keywords.add ("goto");
        keywords.add ("if");
        keywords.add ("in");
        keywords.add ("not");
        keywords.add ("nul");
        keywords.add ("set");
        keywords.add ("setlocal");
        keywords.add ("shift");
        keywords.add ("prn");
        commands.add ("assign");
        commands.add ("attrib");
        commands.add ("cd");
        commands.add ("chdir");
        commands.add ("chkdsk");
        commands.add ("cls");
        commands.add ("comp");
        commands.add ("copy");
        commands.add ("date");
        commands.add ("defrag");
        commands.add ("del");
        commands.add ("deltree");
        commands.add ("dir");
        commands.add ("echo");
        commands.add ("echo.");
        commands.add ("erase");
        commands.add ("exit");
        commands.add ("fc");
        commands.add ("fdisk");
        commands.add ("find");
        commands.add ("format");
        commands.add ("help");
        commands.add ("label");
        commands.add ("md");
        commands.add ("mem");
        commands.add ("memmaker");
        commands.add ("mkdir");
        commands.add ("more");
        commands.add ("move");
        commands.add ("path");
        commands.add ("pause");
        commands.add ("ren");
        commands.add ("rename");
        commands.add ("rd");
        commands.add ("rmdir");
        commands.add ("sort");
        commands.add ("time");
        commands.add ("tree");
        commands.add ("type");
        commands.add ("undelete");
        commands.add ("ver");
        commands.add ("xcopy");
    }


    private LexerRestartInfo<BatTokenId> info;

    BatLexer (LexerRestartInfo<BatTokenId> info) {
        this.info = info;
    }

    public Token<BatTokenId> nextToken () {
        LexerInput input = info.input ();
        int i = input.read ();
        switch (i) {
            case LexerInput.EOF:
                return null;
            case '+':
            case '|':
            case '&':
            case '<':
            case '>':
            case '!':
            case ':':
            case '@':
            case '=':
            case '/':
            case '\\':
            case '(':
            case ')':
            case ',':
            case '%':
            case '^':
            case '#':
            case '{':
            case '}':
            case '?':
            case '.':
            case '$':
            case '*':
            case '_':
            case '-':
            case '`':
            case ';':
                return info.tokenFactory ().createToken (BatTokenId.OPERATOR);
            case ' ':
            case '\n':
            case '\r':
            case '\t':
                do {
                    i = input.read ();
                } while (
                    i == ' ' ||
                    i == '\n' ||
                    i == '\r' ||
                    i == '\t'
                );
                if (i != LexerInput.EOF)
                    input.backup (1);
                return info.tokenFactory ().createToken (BatTokenId.WHITESPACE);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                do {
                    i = input.read ();
                } while (
                    i >= '0' &&
                    i <= '9'
                );
                if (i == '.') {
                    do {
                        i = input.read ();
                    } while (
                        i >= '0' &&
                        i <= '9'
                    );
                }
                input.backup (1);
                return info.tokenFactory ().createToken (BatTokenId.NUMBER);
            case '"':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '"' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                return info.tokenFactory ().createToken (BatTokenId.STRING);
            case '\'':
                do {
                    i = input.read ();
                    if (i == '\\') {
                        i = input.read ();
                        i = input.read ();
                    }
                } while (
                    i != '\'' &&
                    i != '\n' &&
                    i != '\r' &&
                    i != LexerInput.EOF
                );
                return info.tokenFactory ().createToken (BatTokenId.STRING);
            default:
                if (
                    (i >= 'a' && i <= 'z') ||
                    (i >= 'A' && i <= 'Z')
                ) {
                    do {
                        i = input.read ();
                    } while (
                        (i >= 'a' && i <= 'z') ||
                        (i >= 'A' && i <= 'Z') ||
                        (i >= '0' && i <= '9') ||
                        i == '_' ||
                        i == '-' ||
                        i == '~'
                    );
                    input.backup (1);
                    String id = input.readText ().toString ();
                    String lcid = id.toLowerCase ();
                    if (keywords.contains (lcid))
                        return info.tokenFactory ().createToken (BatTokenId.KEYWORD);
                    if (commands.contains (lcid))
                        return info.tokenFactory ().createToken (BatTokenId.COMMAND);
                    if ("rem".equals (lcid)) {
                        do {
                            i = input.read ();
                        } while (
                            i != '\n' &&
                            i != '\r' &&
                            i != LexerInput.EOF
                        );
                        return info.tokenFactory ().createToken (BatTokenId.COMMENT);
                    }
                    return info.tokenFactory ().createToken (BatTokenId.IDENTIFIER);
                }
                return info.tokenFactory ().createToken (BatTokenId.ERROR);
        }
    }

    public Object state () {
        return null;
    }

    public void release () {
    }
}


