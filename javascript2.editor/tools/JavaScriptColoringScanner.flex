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

package org.netbeans.modules.javascript2.editor.lexer;

import org.netbeans.modules.javascript2.editor.api.lexer.JsTokenId;
import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

@org.netbeans.api.annotations.common.SuppressWarnings({"SF_SWITCH_FALLTHROUGH", "URF_UNREAD_FIELD", "DLS_DEAD_LOCAL_STORE", "DM_DEFAULT_ENCODING"})
%%

%public
%final
%class JavaScriptColoringLexer
%type JsTokenId
%unicode
%char

%{
    private LexerInput input;

    private boolean embedded;

    private boolean canFollowLiteral = true;

    private boolean canFollowKeyword = true;

    public JavaScriptColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        this.embedded = !JsTokenId.JAVASCRIPT_MIME_TYPE.equals(info.languagePath().mimePath());
        if(info.state() != null) {
            //reset state
            setState((LexerState)info.state());
        } else {
            //initial state
            zzState = zzLexicalState = YYINITIAL;
        }
    }

    public LexerState getState() {
        if (zzState == YYINITIAL && zzLexicalState == YYINITIAL
                && canFollowLiteral && canFollowKeyword) {
            return null;
        }
        return new LexerState(zzState, zzLexicalState, canFollowLiteral, canFollowKeyword);
    }

    public void setState(LexerState state) {
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
        this.canFollowLiteral = state.canFollowLiteral;
        this.canFollowKeyword = state.canFollowKeyword;
    }

    public JsTokenId nextToken() throws java.io.IOException {
        JsTokenId token = yylex();
        if (token != null && !JsTokenId.ERROR.equals(token)
                && !JsTokenId.UNKNOWN.equals(token)
                && !JsTokenId.WHITESPACE.equals(token)
                && !JsTokenId.LINE_COMMENT.equals(token)
                && !JsTokenId.BLOCK_COMMENT.equals(token)
                && !JsTokenId.DOC_COMMENT.equals(token)) {
            canFollowLiteral = canFollowLiteral(token);
            canFollowKeyword = canFollowKeyword(token);
        }
        return token;
    }

    private JsTokenId getErrorToken() {
        if (embedded) {
            return JsTokenId.UNKNOWN;
        }
        return JsTokenId.ERROR;
    }

    private static boolean canFollowLiteral(JsTokenId token) {
        if ("operator".equals(token.primaryCategory())) {
            return true;
        }

        switch (token) {
            case BRACKET_LEFT_CURLY:
            case BRACKET_LEFT_PAREN:
            case BRACKET_LEFT_BRACKET:
            case KEYWORD_RETURN:
            case KEYWORD_THROW:
            case RESERVED_YIELD:
            case EOL:
                return true;
        }
        return false;
    }

    private static boolean canFollowKeyword(JsTokenId token) {
        if (JsTokenId.OPERATOR_DOT.equals(token)) {
            return false;
        }
        return true;
    }

    public static final class LexerState  {
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        /** can be the literal used here */
        final boolean canFollowLiteral;
        /** can be the literal used here */
        final boolean canFollowKeyword;

        LexerState (int zzState, int zzLexicalState, boolean canFollowLiteral, boolean canFollowKeyword) {
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.canFollowLiteral = canFollowLiteral;
            this.canFollowKeyword = canFollowKeyword;
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
            if (this.canFollowLiteral != other.canFollowLiteral) {
                return false;
            }
            if (this.canFollowKeyword != other.canFollowKeyword) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + this.zzState;
            hash = 29 * hash + this.zzLexicalState;
            hash = 29 * hash + (this.canFollowLiteral ? 1 : 0);
            hash = 29 * hash + (this.canFollowKeyword ? 1 : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{" + "zzState=" + zzState + ", zzLexicalState=" + zzLexicalState + ", canFollowLiteral=" + canFollowLiteral + ", canFollowKeyword=" + canFollowKeyword + '}';
        }
    }

 // End user code

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f\u00A0\u000B]+

/* comments */
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
ShebangComment = "#!" {InputCharacter}*
EndOfLineComment = "//" {InputCharacter}*
DocumentationComment = "/*" "*"+ [^/*] ~"*/"

/* identifiers */
IdentifierPart = [:jletterdigit:]
Identifier = [:jletter:]{IdentifierPart}*

/* integer literals */
DecIntegerLiteral = 0 | [1-9][0-9]*
DecLongLiteral    = {DecIntegerLiteral} [lL]

HexIntegerLiteral = 0 [xX] 0* {HexDigit} {1,8}
HexLongLiteral    = 0 [xX] 0* {HexDigit} {1,16} [lL]
HexDigit          = [0-9a-fA-F]

OctIntegerLiteral = 0+ [1-3]? {OctDigit} {1,15}
OctLongLiteral    = 0+ 1? {OctDigit} {1,21} [lL]
OctDigit          = [0-7]

/* floating point literals */
FloatLiteral  = ({FLit1}|{FLit2}|{FLit3}) {Exponent}? [fF]
DoubleLiteral = ({FLit1}|{FLit2}|{FLit3}) {Exponent}?

FLit1    = [0-9]+ \. [0-9]*
FLit2    = \. [0-9]+
FLit3    = [0-9]+
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter  = [^\r\n\"\\] | \\{LineTerminator}
SStringCharacter = [^\r\n\'\\] | \\{LineTerminator}

RegexpBackslashSequence = \\{InputCharacter}
RegexpClass = "["([^\x5d\r\n\\] | {RegexpBackslashSequence})*"]"
RegexpCharacter = [^\x5b/\r\n\\] | {RegexpBackslashSequence} | {RegexpClass}
RegexpFirstCharacter = [^*\x5b/\r\n\\] | {RegexpBackslashSequence} | {RegexpClass}

%state INITIAL
%state STRING
%state STRINGEND
%state SSTRING
%state SSTRINGEND
%state REGEXP
%state REGEXPEND
%state LCOMMENTEND
%state ERROR

%%

<YYINITIAL> {
  {ShebangComment}               {
                                   yybegin(LCOMMENTEND);
                                   return JsTokenId.LINE_COMMENT;
                                 }
  .|\n                           {
                                   yypushback(1);
                                   yybegin(INITIAL);
                                 }
}

<INITIAL> {

  /* keywords 7.6.1.1 */
  "break"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_BREAK; }
  "case"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CASE; }
  "catch"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CATCH; }
  "continue"                     { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_CONTINUE; }
  "debugger"                     { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DEBUGGER; }
  "default"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DEFAULT; }
  "delete"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DELETE; }
  "do"                           { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_DO; }
  "else"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_ELSE; }
  "finally"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_FINALLY; }
  "for"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_FOR; }
  "function"                     { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_FUNCTION; }
  "if"                           { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_IF; }
  "in"                           { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_IN; }
  "instanceof"                   { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_INSTANCEOF; }
  "new"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_NEW; }
  "return"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_RETURN; }
  "switch"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_SWITCH; }
  "this"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_THIS; }
  "throw"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_THROW; }
  "try"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_TRY; }
  "typeof"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_TYPEOF; }
  "var"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_VAR; }
  "void"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_VOID; }
  "while"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_WHILE; }
  "with"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.KEYWORD_WITH; }

  /* reserved keywords 7.6.1.2 */
  "class"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_CLASS; }
  "const"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_CONST; }
  "enum"                         { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_ENUM; }
  "export"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_EXPORT; }
  "extends"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_EXTENDS; }
  "import"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_IMPORT; }
  "super"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_SUPER; }

  "implements"                   { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_IMPLEMENTS; }
  "interface"                    { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_INTERFACE; }
  "let"                          { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_LET; }
  "package"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PACKAGE; }
  "private"                      { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PRIVATE; }
  "protected"                    { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PROTECTED; }
  "public"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_PUBLIC; }
  "static"                       { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_STATIC; }
  "yield"                        { if (!canFollowKeyword) { return JsTokenId.IDENTIFIER; } return JsTokenId.RESERVED_YIELD; }


  /* boolean literals */
  "true"                         { return JsTokenId.KEYWORD_TRUE; }
  "false"                        { return JsTokenId.KEYWORD_FALSE; }

  /* null literal */
  "null"                         { return JsTokenId.KEYWORD_NULL; }

  "/"[*]                         { return getErrorToken(); }
  "/"
                                 {
                                     if (canFollowLiteral) {
                                       yybegin(REGEXP);
                                       return JsTokenId.REGEXP_BEGIN;
                                     } else {
                                       return JsTokenId.OPERATOR_DIVISION;
                                     }
                                 }
  "/="
                                 {
                                     if (canFollowLiteral) {
                                       yypushback(1);
                                       yybegin(REGEXP);
                                       return JsTokenId.REGEXP_BEGIN;
                                     } else {
                                       return JsTokenId.OPERATOR_DIVISION_ASSIGNMENT;
                                     }
                                 }
  /* operators */

  "("                            { return JsTokenId.BRACKET_LEFT_PAREN; }
  ")"                            { return JsTokenId.BRACKET_RIGHT_PAREN; }
  "{"                            { return JsTokenId.BRACKET_LEFT_CURLY; }
  "}"                            { return JsTokenId.BRACKET_RIGHT_CURLY; }
  "["                            { return JsTokenId.BRACKET_LEFT_BRACKET; }
  "]"                            { return JsTokenId.BRACKET_RIGHT_BRACKET; }
  ";"                            { return JsTokenId.OPERATOR_SEMICOLON; }
  ","                            { return JsTokenId.OPERATOR_COMMA; }
  "."                            { return JsTokenId.OPERATOR_DOT; }
  "="                            { return JsTokenId.OPERATOR_ASSIGNMENT; }
  ">"                            { return JsTokenId.OPERATOR_GREATER; }
  "<"                            { return JsTokenId.OPERATOR_LOWER; }
  "!"                            { return JsTokenId.OPERATOR_NOT; }
  "~"                            { return JsTokenId.OPERATOR_BITWISE_NOT; }
  "?"                            { return JsTokenId.OPERATOR_TERNARY; }
  ":"                            { return JsTokenId.OPERATOR_COLON; }
  "=="                           { return JsTokenId.OPERATOR_EQUALS; }
  "==="                          { return JsTokenId.OPERATOR_EQUALS_EXACTLY; }
  "<="                           { return JsTokenId.OPERATOR_LOWER_EQUALS; }
  ">="                           { return JsTokenId.OPERATOR_GREATER_EQUALS; }
  "!="                           { return JsTokenId.OPERATOR_NOT_EQUALS; }
  "!=="                          { return JsTokenId.OPERATOR_NOT_EQUALS_EXACTLY; }
  "&&"                           { return JsTokenId.OPERATOR_AND; }
  "||"                           { return JsTokenId.OPERATOR_OR; }
  "++"                           { return JsTokenId.OPERATOR_INCREMENT; }
  "--"                           { return JsTokenId.OPERATOR_DECREMENT; }
  "+"                            { return JsTokenId.OPERATOR_PLUS; }
  "-"                            { return JsTokenId.OPERATOR_MINUS; }
  "*"                            { return JsTokenId.OPERATOR_MULTIPLICATION; }
  "&"                            { return JsTokenId.OPERATOR_BITWISE_AND; }
  "|"                            { return JsTokenId.OPERATOR_BITWISE_OR; }
  "^"                            { return JsTokenId.OPERATOR_BITWISE_XOR; }
  "%"                            { return JsTokenId.OPERATOR_MODULUS; }
  "<<"                           { return JsTokenId.OPERATOR_LEFT_SHIFT_ARITHMETIC; }
  ">>"                           { return JsTokenId.OPERATOR_RIGHT_SHIFT_ARITHMETIC; }
  ">>>"                          { return JsTokenId.OPERATOR_RIGHT_SHIFT; }
  "+="                           { return JsTokenId.OPERATOR_PLUS_ASSIGNMENT; }
  "-="                           { return JsTokenId.OPERATOR_MINUS_ASSIGNMENT; }
  "*="                           { return JsTokenId.OPERATOR_MULTIPLICATION_ASSIGNMENT; }
  "&="                           { return JsTokenId.OPERATOR_BITWISE_AND_ASSIGNMENT; }
  "|="                           { return JsTokenId.OPERATOR_BITWISE_OR_ASSIGNMENT; }
  "^="                           { return JsTokenId.OPERATOR_BITWISE_XOR_ASSIGNMENT; }
  "%="                           { return JsTokenId.OPERATOR_MODULUS_ASSIGNMENT; }
  "<<="                          { return JsTokenId.OPERATOR_LEFT_SHIFT_ARITHMETIC_ASSIGNMENT; }
  ">>="                          { return JsTokenId.OPERATOR_RIGHT_SHIFT_ARITHMETIC_ASSIGNMENT; }
  ">>>="                         { return JsTokenId.OPERATOR_RIGHT_SHIFT_ASSIGNMENT; }

  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    return JsTokenId.STRING_BEGIN;
                                 }

  \'                             {
                                    yybegin(SSTRING);
                                    return JsTokenId.STRING_BEGIN;
                                 }

  /* numeric literals */

  {DecIntegerLiteral}            |
  {DecLongLiteral}               |

  {HexIntegerLiteral}            |
  {HexLongLiteral}               |

  {OctIntegerLiteral}            |
  {OctLongLiteral}               |

  {FloatLiteral}                 |
  {DoubleLiteral}                |
  {DoubleLiteral}[dD]            { return JsTokenId.NUMBER; }

  /* comments */
  {DocumentationComment}         { return JsTokenId.DOC_COMMENT; }

  /* comments */
  {TraditionalComment}           { return JsTokenId.BLOCK_COMMENT; }

  /* comments */
  {EndOfLineComment}             {
                                   yybegin(LCOMMENTEND);
                                   return JsTokenId.LINE_COMMENT;
                                 }

  /* whitespace */
  {WhiteSpace}                   { return JsTokenId.WHITESPACE; }

  /* whitespace */
  {LineTerminator}               { return JsTokenId.EOL; }

  /* identifiers */
  {Identifier}                   { return JsTokenId.IDENTIFIER; }
}

<STRING> {
  \"                             {  
                                     yypushback(1);
                                     yybegin(STRINGEND);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.STRING;
                                     }
                                 }

  {StringCharacter}+             { }

  \\[0-3]?{OctDigit}?{OctDigit}  { }

  /* escape sequences */

  \\.                            { }
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(INITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return getErrorToken();
                                     }
                                 }
}

<STRINGEND> {
  \"                             {
                                     yybegin(INITIAL);
                                     return JsTokenId.STRING_END;
                                 }
}

<SSTRING> {
  \'                             {
                                     yypushback(1);
                                     yybegin(SSTRINGEND);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.STRING;
                                     }
                                 }

  {SStringCharacter}+            { }

  \\[0-3]?{OctDigit}?{OctDigit}  { }

  /* escape sequences */

  \\.                            { }
  {LineTerminator}               {
                                     yypushback(1);
                                     yybegin(INITIAL);
                                     if (tokenLength -1 > 0) {
                                         return getErrorToken();
                                     }
                                 }
}

<SSTRINGEND> {
  \'                             {
                                     yybegin(INITIAL);
                                     return JsTokenId.STRING_END;
                                 }
}

<REGEXP> {
  {RegexpFirstCharacter}{RegexpCharacter}*"/"
                                 {
                                     yypushback(1);
                                     yybegin(REGEXPEND);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.REGEXP;
                                     }
                                 }
  .                              {
                                     yypushback(1);
                                     yybegin(ERROR);
                                 }
}

<REGEXPEND> {
  "/"{IdentifierPart}*           {
                                     yybegin(INITIAL);
                                     return JsTokenId.REGEXP_END;
                                 }
  .                              {
                                     yypushback(1);
                                     yybegin(ERROR);
                                 }
}
<ERROR> {
  .*{LineTerminator}             {
                                     yypushback(1);
                                     yybegin(INITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return getErrorToken();
                                     }
                                 }
}

<LCOMMENTEND> {
  {LineTerminator}?              {
                                     yybegin(INITIAL);
                                     if (tokenLength > 0) {
                                         return JsTokenId.EOL;
                                     }
                                 }
}

/* error fallback */
.|\n                             { return getErrorToken(); }
<<EOF>>                          {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return getErrorToken();
    } else {
        return null;
    }
}
