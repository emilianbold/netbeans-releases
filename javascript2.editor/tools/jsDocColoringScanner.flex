/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript2.editor.jsdoc;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

%%

%public
%final
%class JsDocColoringLexer
%type JsDocTokenId
%unicode
%caseless
%char

%{
    private LexerInput input;

    public JsDocColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        if(info.state() != null) {
            //reset state
            setState((LexerState)info.state());
        } else {
            //initial state
            zzState = zzLexicalState = YYINITIAL;
        }
    }

    public LexerState getState() {
        if (zzState == YYINITIAL && zzLexicalState == YYINITIAL) {
            return null;
        }
        return new LexerState(zzState, zzLexicalState);
    }

    public void setState(LexerState state) {
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
    }

    public JsDocTokenId nextToken() throws java.io.IOException {
        JsDocTokenId token = yylex();
        return token;
    }

    public static final class LexerState  {
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;

        LexerState (int zzState, int zzLexicalState) {
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LexerState other = (LexerState) obj;
            if (this.zzState != other.zzState) {
                return false;
            }
            if (this.zzLexicalState != other.zzLexicalState) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + this.zzState;
            hash = 31 * hash + this.zzLexicalState;
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{" + "zzState=" + zzState + ", zzLexicalState=" + zzLexicalState + '}';
        }
    }

 // End user code

%}

/* states */
%state JSDOC
%state AT
%state STRING
%state STRINGEND

/* base structural elements */
AnyChar = (.|[\n])
HtmlString = [<] [^"\r"|"\n"|"\r\n"|">"|"*"]* [>]?
Identifier=[[:letter:][:digit:]]+
LineTerminator = \r|\n|\r\n
StringCharacter  = [^\r\n\"\\] | \\{LineTerminator}
WhiteSpace = [ \t\f]+

/* comment types */
DocumentationComment = "/**"
CommentEnd = ["*"]+ + "/"


%%

<YYINITIAL> {

    /* No code comments */
    "/**#nocode+*/"                 { return JsDocTokenId.COMMENT_NOCODE_BEGIN; }
    "/**#nocode-*/"                 { return JsDocTokenId.COMMENT_NOCODE_END; }

    /* Shared tag comments */
    "/**#@+"                        { yybegin(JSDOC); return JsDocTokenId.COMMENT_SHARED_BEGIN; }
    "/**#@-*/"                      { return JsDocTokenId.COMMENT_SHARED_END; }

    {DocumentationComment}          { yybegin(JSDOC); return JsDocTokenId.COMMENT_START; }
    {CommentEnd}                    { return JsDocTokenId.COMMENT_END; }
    {AnyChar}                       { }
}

<JSDOC> {
    {CommentEnd}                    { return JsDocTokenId.COMMENT_END; }
    {WhiteSpace}                    { return JsDocTokenId.WHITESPACE; }
    {LineTerminator}                { return JsDocTokenId.EOL; }
    {HtmlString}                    { return JsDocTokenId.HTML; }

    "@"                             { yybegin(AT); yypushback(1); }
    "*"                             { return JsDocTokenId.ASTERISK; }
    "{"                             { return JsDocTokenId.BRACKET_LEFT_CURLY; }
    "}"                             { return JsDocTokenId.BRACKET_RIGHT_CURLY; }
    "["                             { return JsDocTokenId.BRACKET_LEFT_BRACKET; }
    "]"                             { return JsDocTokenId.BRACKET_RIGHT_BRACKET; }
    "="                             { return JsDocTokenId.ASSIGNMENT; }

    "\""                            { yybegin(STRING); return JsDocTokenId.STRING_BEGIN; }

    ~({WhiteSpace}
        |{LineTerminator}
        |"*"|"@"|"<"|"{"|"}"|"\"")  { yypushback(1); return JsDocTokenId.OTHER; }
}

<STRING> {
    \"                              { yypushback(1); yybegin(STRINGEND);
                                        if (tokenLength - 1 > 0) {
                                            return JsDocTokenId.STRING;
                                        }
                                    }

    {StringCharacter}+              { }

    /* escape sequences */
    \\.                             { }
    {LineTerminator}                { yypushback(1); yybegin(JSDOC);
                                        if (tokenLength - 1 > 0) {
                                            return JsDocTokenId.UNKNOWN;
                                        }
                                    }
}

<STRINGEND> {
    \"                              { yybegin(JSDOC); return JsDocTokenId.STRING_END; }
}

<AT> {
    "@"{Identifier}                 { yybegin(JSDOC); return JsDocTokenId.KEYWORD; }
    {AnyChar}                       { yybegin(JSDOC); return JsDocTokenId.AT; }
}

<<EOF>> {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JsDocTokenId.UNKNOWN;
    } else {
        return null;
    }
}