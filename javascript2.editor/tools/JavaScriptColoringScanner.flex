package org.netbeans.modules.javascript2.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

%%

%public
%final
%class JavaScriptColoringLexer
%type CommonTokenId
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

    public CommonTokenId nextToken() throws java.io.IOException {
        CommonTokenId token = yylex();
        if (token != null && !CommonTokenId.UNKNOWN.equals(token)
                && !CommonTokenId.WHITESPACE.equals(token)
                && !CommonTokenId.LINE_COMMENT.equals(token)
                && !CommonTokenId.BLOCK_COMMENT.equals(token)
                && !CommonTokenId.DOC_COMMENT.equals(token)) {
            canFollowLiteral = canFollowLiteral(token);
        }
        return token;
    }

    private static boolean canFollowLiteral(CommonTokenId token) {
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
  "break"                        { return CommonTokenId.KEYWORD_BREAK; }
  "case"                         { return CommonTokenId.KEYWORD_CASE; }
  "catch"                        { return CommonTokenId.KEYWORD_CATCH; }
  "continue"                     { return CommonTokenId.KEYWORD_CONTINUE; }
  "debugger"                     { return CommonTokenId.KEYWORD_DEBUGGER; }
  "default"                      { return CommonTokenId.KEYWORD_DEFAULT; }
  "delete"                       { return CommonTokenId.KEYWORD_DELETE; }
  "do"                           { return CommonTokenId.KEYWORD_DO; }
  "else"                         { return CommonTokenId.KEYWORD_ELSE; }
  "finally"                      { return CommonTokenId.KEYWORD_FINALLY; }
  "for"                          { return CommonTokenId.KEYWORD_FOR; }
  "function"                     { return CommonTokenId.KEYWORD_FUNCTION; }
  "if"                           { return CommonTokenId.KEYWORD_IF; }
  "in"                           { return CommonTokenId.KEYWORD_IN; }
  "instanceof"                   { return CommonTokenId.KEYWORD_INSTANCEOF; }
  "new"                          { return CommonTokenId.KEYWORD_NEW; }
  "return"                       { return CommonTokenId.KEYWORD_RETURN; }
  "switch"                       { return CommonTokenId.KEYWORD_SWITCH; }
  "this"                         { return CommonTokenId.KEYWORD_THIS; }
  "throw"                        { return CommonTokenId.KEYWORD_THROW; }
  "try"                          { return CommonTokenId.KEYWORD_TRY; }
  "typeof"                       { return CommonTokenId.KEYWORD_TYPEOF; }
  "var"                          { return CommonTokenId.KEYWORD_VAR; }
  "void"                         { return CommonTokenId.KEYWORD_VOID; }
  "while"                        { return CommonTokenId.KEYWORD_WHILE; }
  "with"                         { return CommonTokenId.KEYWORD_WITH; }

  /* reserved keywords 7.6.1.2 */
  "class"                        { return CommonTokenId.RESERVED_CLASS; }
  "const"                        { return CommonTokenId.RESERVED_CONST; }
  "enum"                         { return CommonTokenId.RESERVED_ENUM; }
  "export"                       { return CommonTokenId.RESERVED_EXPORT; }
  "extends"                      { return CommonTokenId.RESERVED_EXTENDS; }
  "import"                       { return CommonTokenId.RESERVED_IMPORT; }
  "super"                        { return CommonTokenId.RESERVED_SUPER; }

  "implements"                   { return CommonTokenId.RESERVED_IMPLEMENTS; }
  "interface"                    { return CommonTokenId.RESERVED_INTERFACE; }
  "let"                          { return CommonTokenId.RESERVED_LET; }
  "package"                      { return CommonTokenId.RESERVED_PACKAGE; }
  "private"                      { return CommonTokenId.RESERVED_PRIVATE; }
  "protected"                    { return CommonTokenId.RESERVED_PROTECTED; }
  "public"                       { return CommonTokenId.RESERVED_PUBLIC; }
  "static"                       { return CommonTokenId.RESERVED_STATIC; }
  "yield"                        { return CommonTokenId.RESERVED_YIELD; }


  /* boolean literals */
  "true"                         { return CommonTokenId.KEYWORD_TRUE; }
  "false"                        { return CommonTokenId.KEYWORD_FALSE; }

  /* null literal */
  "null"                         { return CommonTokenId.KEYWORD_NULL; }

  "/"[*]                         { return CommonTokenId.UNKNOWN; }
  "/"
                                 {
                                     if (canFollowLiteral) {
                                       yybegin(REGEXP);
                                       return CommonTokenId.REGEXP_BEGIN;
                                     } else {
                                       return CommonTokenId.OPERATOR_DIVISION;
                                     }
                                 }
  "/="
                                 {
                                     if (canFollowLiteral) {
                                       yypushback(1);
                                       yybegin(REGEXP);
                                       return CommonTokenId.REGEXP_BEGIN;
                                     } else {
                                       return CommonTokenId.OPERATOR_DIVISION_ASSIGNMENT;
                                     }
                                 }
  /* operators */

  "("                            { return CommonTokenId.BRACKET_LEFT_PAREN; }
  ")"                            { return CommonTokenId.BRACKET_RIGHT_PAREN; }
  "{"                            { return CommonTokenId.BRACKET_LEFT_CURLY; }
  "}"                            { return CommonTokenId.BRACKET_RIGHT_CURLY; }
  "["                            { return CommonTokenId.BRACKET_LEFT_BRACKET; }
  "]"                            { return CommonTokenId.BRACKET_RIGHT_BRACKET; }
  ";"                            { return CommonTokenId.OPERATOR_SEMICOLON; }
  ","                            { return CommonTokenId.OPERATOR_COMMA; }
  "."                            { return CommonTokenId.OPERATOR_DOT; }
  "="                            { return CommonTokenId.OPERATOR_ASSIGNMENT; }
  ">"                            { return CommonTokenId.OPERATOR_GREATER; }
  "<"                            { return CommonTokenId.OPERATOR_LOWER; }
  "!"                            { return CommonTokenId.OPERATOR_NOT; }
  "~"                            { return CommonTokenId.OPERATOR_BITWISE_NOT; }
  "?"                            { return CommonTokenId.OPERATOR_TERNARY; }
  ":"                            { return CommonTokenId.OPERATOR_COLON; }
  "=="                           { return CommonTokenId.OPERATOR_EQUALS; }
  "==="                          { return CommonTokenId.OPERATOR_EQUALS_EXACTLY; }
  "<="                           { return CommonTokenId.OPERATOR_LOWER_EQUALS; }
  ">="                           { return CommonTokenId.OPERATOR_GREATER_EQUALS; }
  "!="                           { return CommonTokenId.OPERATOR_NOT_EQUALS; }
  "!=="                          { return CommonTokenId.OPERATOR_NOT_EQUALS_EXACTLY; }
  "&&"                           { return CommonTokenId.OPERATOR_AND; }
  "||"                           { return CommonTokenId.OPERATOR_OR; }
  "++"                           { return CommonTokenId.OPERATOR_INCREMENT; }
  "--"                           { return CommonTokenId.OPERATOR_DECREMENT; }
  "+"                            { return CommonTokenId.OPERATOR_PLUS; }
  "-"                            { return CommonTokenId.OPERATOR_MINUS; }
  "*"                            { return CommonTokenId.OPERATOR_MULTIPLICATION; }
  "&"                            { return CommonTokenId.OPERATOR_BITWISE_AND; }
  "|"                            { return CommonTokenId.OPERATOR_BITWISE_OR; }
  "^"                            { return CommonTokenId.OPERATOR_BITWISE_XOR; }
  "%"                            { return CommonTokenId.OPERATOR_MODULUS; }
  "<<"                           { return CommonTokenId.OPERATOR_LEFT_SHIFT_ARITHMETIC; }
  ">>"                           { return CommonTokenId.OPERATOR_RIGHT_SHIFT_ARITHMETIC; }
  ">>>"                          { return CommonTokenId.OPERATOR_RIGHT_SHIFT; }
  "+="                           { return CommonTokenId.OPERATOR_PLUS_ASSIGNMENT; }
  "-="                           { return CommonTokenId.OPERATOR_MINUS_ASSIGNMENT; }
  "*="                           { return CommonTokenId.OPERATOR_MULTIPLICATION_ASSIGNMENT; }
  "&="                           { return CommonTokenId.OPERATOR_BITWISE_AND_ASSIGNMENT; }
  "|="                           { return CommonTokenId.OPERATOR_BITWISE_OR_ASSIGNMENT; }
  "^="                           { return CommonTokenId.OPERATOR_BITWISE_XOR_ASSIGNMENT; }
  "%="                           { return CommonTokenId.OPERATOR_MODULUS_ASSIGNMENT; }
  "<<="                          { return CommonTokenId.OPERATOR_LEFT_SHIFT_ARITHMETIC_ASSIGNMENT; }
  ">>="                          { return CommonTokenId.OPERATOR_RIGHT_SHIFT_ARITHMETIC_ASSIGNMENT; }
  ">>>="                         { return CommonTokenId.OPERATOR_RIGHT_SHIFT_ASSIGNMENT; }

  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    return CommonTokenId.STRING_BEGIN;
                                 }

  \'                             {
                                    yybegin(SSTRING);
                                    return CommonTokenId.STRING_BEGIN;
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
  {DoubleLiteral}[dD]            { return CommonTokenId.NUMBER; }

  /* comments */
  {DocumentationComment}         { return CommonTokenId.DOC_COMMENT; }

  /* comments */
  {TraditionalComment}           { return CommonTokenId.BLOCK_COMMENT; }

  /* comments */
  {EndOfLineComment}             {
                                   yybegin(LCOMMENTEND);
                                   return CommonTokenId.LINE_COMMENT;
                                 }

  /* whitespace */
  {WhiteSpace}                   { return CommonTokenId.WHITESPACE; }

  /* whitespace */
  {LineTerminator}               { return CommonTokenId.EOL; }

  /* identifiers */
  {Identifier}                   { return CommonTokenId.IDENTIFIER; }
}

<STRING> {
  \"                             {  
                                     yypushback(1);
                                     yybegin(STRINGEND);
                                     if (tokenLength - 1 > 0) {
                                         return CommonTokenId.STRING;
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
                                         return CommonTokenId.UNKNOWN;
                                     }
                                 }
}

<STRINGEND> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     return CommonTokenId.STRING_END;
                                 }
}

<SSTRING> {
  \'                             {
                                     yypushback(1);
                                     yybegin(SSTRINGEND);
                                     if (tokenLength - 1 > 0) {
                                         return CommonTokenId.STRING;
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
                                         return CommonTokenId.UNKNOWN;
                                     }
                                 }
}

<SSTRINGEND> {
  \'                             {
                                     yybegin(YYINITIAL);
                                     return CommonTokenId.STRING_END;
                                 }
}

<REGEXP> {
  {RegexpFirstCharacter}{RegexpCharacter}*"/"
                                 {
                                     yypushback(1);
                                     yybegin(REGEXPEND);
                                     if (tokenLength - 1 > 0) {
                                         return CommonTokenId.REGEXP;
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
                                     return CommonTokenId.REGEXP_END;
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
                                         return CommonTokenId.UNKNOWN;
                                     }
                                 }
}

<LCOMMENTEND> {
  {LineTerminator}?              {
                                     yybegin(YYINITIAL);
                                     if (tokenLength > 0) {
                                         return CommonTokenId.EOL;
                                     }
                                 }
}

/* error fallback */
.|\n                             { return CommonTokenId.UNKNOWN; }
<<EOF>>                          {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return CommonTokenId.UNKNOWN;
    } else {
        return null;
    }
}
