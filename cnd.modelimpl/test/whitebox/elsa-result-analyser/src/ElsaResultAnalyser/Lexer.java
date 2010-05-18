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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package ElsaResultAnalyser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author nk220367
 */
public class Lexer {

    BufferedReader input;
    Token currentToken;

    public Lexer(BufferedReader s) {
        input = s;
    }
    int indent = 0;

    public Token getNextToken() {
        Token token = new Token();

        char c;

        c = getSymbol();

        boolean calcIndent = false;

        while (Character.isSpaceChar(c) || (c == '\n')) {
            if (calcIndent) {
                indent++;
            }

            if (c == '\n') {
                line++;
                row = 0;
                calcIndent = true;
                indent = 0;
            }

            c = getSymbol();
        }

        /*        if (c == '/') {
        c = GetSymbol();
        if (c == '/') {
        while (c != '\n' && c != 0) {
        c = GetSymbol();
        
        if (c == '\n') {
        line++;
        }
        }
        
        return GetNextToken();
        } else {
        PutBackSymbol(c);
        c = '/';
        }
        }
         */
        /*        if (c == '{') {
        int bn = 1;
        
        while (bn != 0 && c != 0) {
        c = GetSymbol();
        if (c == '{') {
        bn++;
        }
        if (c == '}') {
        bn--;
        }
        if (c == '\n') {
        line++;
        }
        }
        
        return GetNextToken();
        }
         */
        /*        if (c == '[') {
        while (c != ']' && c != 0) {
        c = GetSymbol();
        
        if (c == '\n') {
        line++;
        }
        }
        
        return GetNextToken();
        }
         */
        /*if (c == '/') {
        c = GetSymbol();
        if (c == '*') {
        while (true) {
        c = GetSymbol();
        
        if (c == 0) {
        break;
        }
        if (c == '*') {
        c = GetSymbol();
        if (c == '/') {
        return GetNextToken();
        }
        }
        
        if (c == '\n') {
        line++;
        }
        }
        } else {
        PutBackSymbol(c);
        c = '/';
        }
        }*/

        token.row = row - 1;
        token.line = line;

        if (Character.isLetter(c) || (c == '_')) {
            token.name += c;

            c = getSymbol();

            while (Character.isLetter(c) || (c == '_') || Character.isDigit(c)) {
                token.name += c;
                c = getSymbol();
            }

            token.type = Token.TT.TOKEN_ID;
        } else if (Character.isDigit(c)) {
            token.name += c;

            c = getSymbol();

            while (Character.isDigit(c)) {
                token.name += c;
                c = getSymbol();
            }
            token.type = Token.TT.TOKEN_DIGIT;
        } else if (c == ':') {
            token.name += c;
            c = getSymbol();

            if (c == ':') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '(') {
            token.name += c;
            c = getSymbol();

            if (c == ')') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '[') {
            token.name += c;
            c = getSymbol();

            if (c == ']') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '+') {
            token.name += c;
            c = getSymbol();

            if (c == '+') {
                token.name += c;
                c = getSymbol();
            }
            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '-') {
            token.name += c;
            c = getSymbol();

            if (c == '-') {
                token.name += c;
                c = getSymbol();
            }
            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
            if (c == '>') {
                token.name += c;
                c = getSymbol();

                if (c == '*') {
                    token.name += c;
                    c = getSymbol();
                }
            }
        } else if (c == '=') {
            token.name += c;
            c = getSymbol();

            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '.') {
            token.name += c;
            c = getSymbol();

            if (c == '*') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '|') {
            token.name += c;
            c = getSymbol();

            if (c == '|') {
                token.name += c;
                c = getSymbol();
            }
            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '&') {
            token.name += c;
            c = getSymbol();

            if (c == '&') {
                token.name += c;
                c = getSymbol();
            }
            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '>') {
            token.name += c;
            c = getSymbol();

            if (c == '>') {
                token.name += c;
                c = getSymbol();
                if (c == '=') {
                    token.name += c;
                    c = getSymbol();
                }
            }
            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '<') {
            token.name += c;
            c = getSymbol();

            if (c == '<') {
                token.name += c;
                c = getSymbol();
                if (c == '=') {
                    token.name += c;
                    c = getSymbol();
                }
            }
            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '!') {
            token.name += c;
            c = getSymbol();

            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '?') {
            token.name += c;
            c = getSymbol();

            if (c == ':') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '*') {
            token.name += c;
            c = getSymbol();

            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '/') {
            token.name += c;
            c = getSymbol();

            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '%') {
            token.name += c;
            c = getSymbol();

            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if (c == '^') {
            token.name += c;
            c = getSymbol();

            if (c == '=') {
                token.name += c;
                c = getSymbol();
            }
        } else if ((c == '{') ||
                (c == '}') ||
                (c == ',') ||
                (c == '~') ||
                (c == '"') ||
                (c == ')') ||
                (c == ']') ||
                (c == '\'') ||
                (c == '\\') ||
                (c == ';')) {
            token.name += c;
            c = getSymbol();
        } else if (c == 0) {
        } else {
            System.out.println("Line " + line + " : " + " bad symbol: " + c);

            c = getSymbol();
        }

        putBackSymbol(c);

        currentToken = token;

        return currentToken;
    }

    public boolean isEndOfLine() {
        char c = getSymbol();

        while (Character.isSpaceChar(c) || (c == '\n')) {
            if (c == '\n') {
                putBackSymbol(c);
                return true;
            }
            c = getSymbol();
        }

        putBackSymbol(c);

        return false;
    }

    public Token getEndOfString() {
        Token token = new Token();

        char c = getSymbol();
        while (Character.isSpaceChar(c) || (c == '\n')) {
            if (c == '\n') {
                break;
            }
            c = getSymbol();
        }

        while (c != '\n' && c != 0) {
            token.name += c;
            c = getSymbol();
        }
        putBackSymbol(c);

        return token;
    }

    public Token getCurrentToken() {
        return currentToken;
    }

    public int getIndent() {
        return indent;
    }
    int line;
    int row;

    char getSymbol() {
        row++;
        if (isPutBack) {
            isPutBack = false;
            return putBack;
        }
        try {
            return (char) input.read();
        } catch (IOException ex) {
            Logger.getLogger(Lexer.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
    }
    boolean isPutBack = false;
    char putBack;

    void putBackSymbol(char c) {
        row--;
        if (isPutBack) {
            System.out.println("Double putBack!");
        }

        isPutBack = true;
        putBack = c;
    }

    void seekTo(int line, int colomn) {
        this.line = line;
        this.row = colomn;
        for (int i = 0; i < line - 1; i++) {
            try {
                input.readLine();
            } catch (IOException ex) {
                Logger.getLogger(Lexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (int i = 0; i < colomn - 1; i++) {
            try {
                input.read();
            } catch (IOException ex) {
                Logger.getLogger(Lexer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
