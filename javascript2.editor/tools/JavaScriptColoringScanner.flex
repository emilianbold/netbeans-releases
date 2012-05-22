package org.netbeans.modules.javascript2.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

%%

%public
%final
%class JavaScriptColoringLexer
%type JsTokenId
%unicode
%char

%{
    private LexerInput input;

    private boolean canFollowLiteral = true;

    public JavaScriptColoringLexer(LexerRestartInfo info) {
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
        if (zzState == YYINITIAL && zzLexicalState == YYINITIAL
                && canFollowLiteral) {
            return null;
        }
        return new LexerState(zzState, zzLexicalState, canFollowLiteral);
    }

    public void setState(LexerState state) {
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
        this.canFollowLiteral = state.canFollowLiteral;
    }

    public JsTokenId nextToken() throws java.io.IOException {
        JsTokenId token = yylex();
        if (token != null && !JsTokenId.UNKNOWN.equals(token)
                && !JsTokenId.WHITESPACE.equals(token)
                && !JsTokenId.LINE_COMMENT.equals(token)
                && !JsTokenId.BLOCK_COMMENT.equals(token)
                && !JsTokenId.DOC_COMMENT.equals(token)) {
            canFollowLiteral = canFollowLiteral(token);
        }
        return token;
    }

    private static boolean canFollowLiteral(JsTokenId token) {
        if ("operator".equals(token.primaryCategory())) {
            return true;
        }

        switch (token) {
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

    public static final class LexerState  {
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;
        /** can be the literal used here */
        final boolean canFollowLiteral;

        LexerState (int zzState, int zzLexicalState, boolean canFollowLiteral) {
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
            this.canFollowLiteral = canFollowLiteral;
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
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 29 * hash + this.zzState;
            hash = 29 * hash + this.zzLexicalState;
            hash = 29 * hash + (this.canFollowLiteral ? 1 : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{" + "zzState=" + zzState + ", zzLexicalState=" + zzLexicalState + ", canFollowLiteral=" + canFollowLiteral + '}';
        }
    }

 // End user code

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f]+

/* comments */
TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
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

  /* keywords 7.6.1.1 */
  "break"                        { return JsTokenId.KEYWORD_BREAK; }
  "case"                         { return JsTokenId.KEYWORD_CASE; }
  "catch"                        { return JsTokenId.KEYWORD_CATCH; }
  "continue"                     { return JsTokenId.KEYWORD_CONTINUE; }
  "debugger"                     { return JsTokenId.KEYWORD_DEBUGGER; }
  "default"                      { return JsTokenId.KEYWORD_DEFAULT; }
  "delete"                       { return JsTokenId.KEYWORD_DELETE; }
  "do"                           { return JsTokenId.KEYWORD_DO; }
  "else"                         { return JsTokenId.KEYWORD_ELSE; }
  "finally"                      { return JsTokenId.KEYWORD_FINALLY; }
  "for"                          { return JsTokenId.KEYWORD_FOR; }
  "function"                     { return JsTokenId.KEYWORD_FUNCTION; }
  "if"                           { return JsTokenId.KEYWORD_IF; }
  "in"                           { return JsTokenId.KEYWORD_IN; }
  "instanceof"                   { return JsTokenId.KEYWORD_INSTANCEOF; }
  "new"                          { return JsTokenId.KEYWORD_NEW; }
  "return"                       { return JsTokenId.KEYWORD_RETURN; }
  "switch"                       { return JsTokenId.KEYWORD_SWITCH; }
  "this"                         { return JsTokenId.KEYWORD_THIS; }
  "throw"                        { return JsTokenId.KEYWORD_THROW; }
  "try"                          { return JsTokenId.KEYWORD_TRY; }
  "typeof"                       { return JsTokenId.KEYWORD_TYPEOF; }
  "var"                          { return JsTokenId.KEYWORD_VAR; }
  "void"                         { return JsTokenId.KEYWORD_VOID; }
  "while"                        { return JsTokenId.KEYWORD_WHILE; }
  "with"                         { return JsTokenId.KEYWORD_WITH; }

  /* reserved keywords 7.6.1.2 */
  "class"                        { return JsTokenId.RESERVED_CLASS; }
  "const"                        { return JsTokenId.RESERVED_CONST; }
  "enum"                         { return JsTokenId.RESERVED_ENUM; }
  "export"                       { return JsTokenId.RESERVED_EXPORT; }
  "extends"                      { return JsTokenId.RESERVED_EXTENDS; }
  "import"                       { return JsTokenId.RESERVED_IMPORT; }
  "super"                        { return JsTokenId.RESERVED_SUPER; }

  "implements"                   { return JsTokenId.RESERVED_IMPLEMENTS; }
  "interface"                    { return JsTokenId.RESERVED_INTERFACE; }
  "let"                          { return JsTokenId.RESERVED_LET; }
  "package"                      { return JsTokenId.RESERVED_PACKAGE; }
  "private"                      { return JsTokenId.RESERVED_PRIVATE; }
  "protected"                    { return JsTokenId.RESERVED_PROTECTED; }
  "public"                       { return JsTokenId.RESERVED_PUBLIC; }
  "static"                       { return JsTokenId.RESERVED_STATIC; }
  "yield"                        { return JsTokenId.RESERVED_YIELD; }


  /* boolean literals */
  "true"                         { return JsTokenId.KEYWORD_TRUE; }
  "false"                        { return JsTokenId.KEYWORD_FALSE; }

  /* null literal */
  "null"                         { return JsTokenId.KEYWORD_NULL; }

  "/"[*]                         { return JsTokenId.UNKNOWN; }
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
                                     yybegin(YYINITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.UNKNOWN;
                                     }
                                 }
}

<STRINGEND> {
  \"                             {
                                     yybegin(YYINITIAL);
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
                                     yybegin(YYINITIAL);
                                     if (tokenLength -1 > 0) {
                                         return JsTokenId.UNKNOWN;
                                     }
                                 }
}

<SSTRINGEND> {
  \'                             {
                                     yybegin(YYINITIAL);
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
                                     yybegin(YYINITIAL);
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
                                     yybegin(YYINITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return JsTokenId.UNKNOWN;
                                     }
                                 }
}

<LCOMMENTEND> {
  {LineTerminator}?              {
                                     yybegin(YYINITIAL);
                                     if (tokenLength > 0) {
                                         return JsTokenId.EOL;
                                     }
                                 }
}

/* error fallback */
.|\n                             { return JsTokenId.UNKNOWN; }
<<EOF>>                          {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JsTokenId.UNKNOWN;
    } else {
        return null;
    }
}
