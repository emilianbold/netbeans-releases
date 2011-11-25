package org.netbeans.modules.javascript2.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

%%

%public
%final
%class JavaScriptColoringLexer
%type JsTokenId
%function nextToken
%unicode
%caseless
%char

%{
    private StateStack stack = new StateStack();

    private LexerInput input;

    public JavaScriptColoringLexer(LexerRestartInfo info) {
        this.input = info.input();

        if(info.state() != null) {
            //reset state
            setState((LexerState)info.state());
        } else {
            //initial state
            zzState = zzLexicalState = YYINITIAL;
            stack.clear();
        }
    }

    public static final class LexerState  {
        final StateStack stack;
        /** the current state of the DFA */
        final int zzState;
        /** the current lexical state */
        final int zzLexicalState;


        LexerState (StateStack stack, int zzState, int zzLexicalState) {
            this.stack = stack;
            this.zzState = zzState;
            this.zzLexicalState = zzLexicalState;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                    return true;
            }

            if (obj == null || obj.getClass() != this.getClass()) {
                    return false;
            }

            LexerState state = (LexerState) obj;
            return (this.stack.equals(state.stack)
                && (this.zzState == state.zzState)
                && (this.zzLexicalState == state.zzLexicalState));
        }

        @Override
        public int hashCode() {
            int hash = 11;
            hash = 31 * hash + this.zzState;
            hash = 31 * hash + this.zzLexicalState;
            if (stack != null) {
                hash = 31 * hash + this.stack.hashCode();
            }
            return hash;
        }
    }

    public LexerState getState() {
        return new LexerState(stack.createClone(), zzState, zzLexicalState);
    }

    public void setState(LexerState state) {
        this.stack.copyFrom(state.stack);
        this.zzState = state.zzState;
        this.zzLexicalState = state.zzLexicalState;
    }

 // End user code

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = {LineTerminator} | [ \t\f]+

/* comments */
Comment = {TraditionalComment} | {EndOfLineComment}

TraditionalComment = "/*" [^*] ~"*/" | "/*" "*"+ "/"
EndOfLineComment = "//" {InputCharacter}* {LineTerminator}?

/* identifiers */
Identifier = [:jletter:][:jletterdigit:]*

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
StringCharacter  = [^\r\n\"\\]
SStringCharacter = [^\r\n\'\\]

%state STRING SSTRING

%%

<YYINITIAL> {

  /* keywords */
  "break"                        { return JsTokenId.KEYWORD_BREAK; }
  "case"                         { return JsTokenId.KEYWORD_CASE; }
  "catch"                        { return JsTokenId.KEYWORD_CATCH; }
  "continue"                     { return JsTokenId.KEYWORD_CONTINUE; }
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

  /* reserved keywords */
  "abstract"                     { return JsTokenId.RESERVED_ABSTRACT; }
  "boolean"                      { return JsTokenId.RESERVED_BOOLEAN; }
  "byte"                         { return JsTokenId.RESERVED_BYTE; }
  "char"                         { return JsTokenId.RESERVED_CHAR; }
  "class"                        { return JsTokenId.RESERVED_CLASS; }
  "const"                        { return JsTokenId.RESERVED_CONST; }
  "debugger"                     { return JsTokenId.RESERVED_DEBUGGER; }
  "double"                       { return JsTokenId.RESERVED_DOUBLE; }
  "enum"                         { return JsTokenId.RESERVED_ENUM; }
  "export"                       { return JsTokenId.RESERVED_EXPORT; }
  "extends"                      { return JsTokenId.RESERVED_EXTENDS; }
  "final"                        { return JsTokenId.RESERVED_FINAL; }
  "float"                        { return JsTokenId.RESERVED_FLOAT; }
  "goto"                         { return JsTokenId.RESERVED_GOTO; }
  "implements"                   { return JsTokenId.RESERVED_IMPLEMENTS; }
  "import"                       { return JsTokenId.RESERVED_IMPORT; }
  "int"                          { return JsTokenId.RESERVED_INT; }
  "interface"                    { return JsTokenId.RESERVED_INTERFACE; }
  "long"                         { return JsTokenId.RESERVED_LONG; }
  "native"                       { return JsTokenId.RESERVED_NATIVE; }
  "package"                      { return JsTokenId.RESERVED_PACKAGE; }
  "private"                      { return JsTokenId.RESERVED_PRIVATE; }
  "protected"                    { return JsTokenId.RESERVED_PROTECTED; }
  "public"                       { return JsTokenId.RESERVED_PUBLIC; }
  "short"                        { return JsTokenId.RESERVED_SHORT; }
  "static"                       { return JsTokenId.RESERVED_STATIC; }
  "super"                        { return JsTokenId.RESERVED_SUPER; }
  "synchronized"                 { return JsTokenId.RESERVED_SYNCHRONIZED; }
  "throws"                       { return JsTokenId.RESERVED_THROWS; }
  "transient"                    { return JsTokenId.RESERVED_TRANSIENT; }
  "volatile"                     { return JsTokenId.RESERVED_VOLATILE; }

  /* boolean literals */
  "true"                         { return JsTokenId.KEYWORD_TRUE; }
  "false"                        { return JsTokenId.KEYWORD_FALSE; }

  /* null literal */
  "null"                         { return JsTokenId.KEYWORD_NULL; }

  /* operators */

  "("                            { return JsTokenId.OPERATOR_LEFT_PARAN; }
  ")"                            { return JsTokenId.OPERATOR_RIGHT_PARAN; }
  "{"                            { return JsTokenId.OPERATOR_LEFT_CURLY; }
  "}"                            { return JsTokenId.OPERATOR_RIGHT_CURLY; }
  "["                            { return JsTokenId.OPERATOR_LEFT_BRACKET; }
  "]"                            { return JsTokenId.OPERATOR_RIGHT_BRACKET; }
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
  "<="                           { return JsTokenId.OPERATOR_LOWER_EQUALS; }
  ">="                           { return JsTokenId.OPERATOR_GREATER_EQUALS; }
  "!="                           { return JsTokenId.OPERATOR_NOT_EQUALS; }
  "&&"                           { return JsTokenId.OPERATOR_AND; }
  "||"                           { return JsTokenId.OPERATOR_OR; }
  "++"                           { return JsTokenId.OPERATOR_INCREMENT; }
  "--"                           { return JsTokenId.OPERATOR_DECREMENT; }
  "+"                            { return JsTokenId.OPERATOR_PLUS; }
  "-"                            { return JsTokenId.OPERATOR_MINUS; }
  "*"                            { return JsTokenId.OPERATOR_MULTIPLICATION; }
  "/"                            { return JsTokenId.OPERATOR_DIVISION; }
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
  "/="                           { return JsTokenId.OPERATOR_DIVISION_ASSIGNMENT; }
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
                                 }

  \'                             {
                                    yybegin(SSTRING);
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
  {Comment}                      { return JsTokenId.COMMENT; }

  /* whitespace */
  {WhiteSpace}                   { return JsTokenId.WHITESPACE; }

  /* identifiers */
  {Identifier}                   { return JsTokenId.IDENTIFIER; }
}

<STRING> {
  \"                             {
                                     yybegin(YYINITIAL);
                                     return JsTokenId.STRING;
                                 }

  {StringCharacter}+             { }

  \\[0-3]?{OctDigit}?{OctDigit}  { }

  /* escape sequences */

  \\.                            { }
  {LineTerminator}               { yybegin(YYINITIAL);  }
}

<SSTRING> {
  \'                             {
                                     yybegin(YYINITIAL);
                                     return JsTokenId.STRING;
                                 }

  {SStringCharacter}+            { }

  \\[0-3]?{OctDigit}?{OctDigit}  { }

  /* escape sequences */

  \\.                            { }
  {LineTerminator}               { yybegin(YYINITIAL);  }
}

/* error fallback */
.|\n                             { }
<<EOF>>                          {
    if (input.readLength() > 0) {
        // backup eof
        input.backup(1);
        //and return the text as error token
        return JsTokenId.UNKNOWN_TOKEN;
    } else {
        return null;
    }
}
