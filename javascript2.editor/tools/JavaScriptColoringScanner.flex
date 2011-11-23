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
  "break"                        |
  "case"                         |
  "catch"                        |
  "continue"                     |
  "do"                           |
  "else"                         |
  "finally"                      |
  "for"                          |
  "default"                      |
  "delete"                       |
  "new"                          |
  "goto"                         |
  "if"                           |
  "switch"                       |
  "return"                       |
  "while"                        |
  "this"                         |
  "try"                          |
  "var"                          |
  "function"                     |
  "with"                         |
  "in"                           |

  /* boolean literals */
  "true"                         |
  "false"                        |

  /* null literal */
  "null"                         { return JsTokenId.KEYWORD; }

  /* standard / builtin functions */
  "Infinity"                     |
  "NaN"                          |
  "undefined"                    |
  "decodeURI"                    |
  "encodeURIComponent"           |
  "escape"                       |
  "eval"                         |
  "isFinite"                     |
  "isNaN"                        |
  "parseFloat"                   |
  "parseInt"                     |
  "unescape"                     { return JsTokenId.KEYWORD2; }

  /* Built-in Types*/
  "Array"                        |
  "Boolean"                      |
  "Date"                         |
  "Math"                         |
  "Number"                       |
  "Object"                       |
  "RegExp"                       |
  "String"                       |
  {Identifier} ":"               { return JsTokenId.TYPE; }


  /* operators */

  "("                            { return JsTokenId.OPERATOR_LPARAN; }
  ")"                            { return JsTokenId.OPERATOR_RPARAN; }
  "{"                            { return JsTokenId.OPERATOR_LCURLY; }
  "}"                            { return JsTokenId.OPERATOR_RCURLY; }
  "["                            { return JsTokenId.OPERATOR_LBRACKET; }
  "]"                            { return JsTokenId.OPERATOR_RBRACKET; }
  ";"                            |
  ","                            |
  "."                            |
  "="                            |
  ">"                            |
  "<"                            |
  "!"                            |
  "~"                            |
  "?"                            |
  ":"                            |
  "=="                           |
  "<="                           |
  ">="                           |
  "!="                           |
  "&&"                           |
  "||"                           |
  "++"                           |
  "--"                           |
  "+"                            |
  "-"                            |
  "*"                            |
  "/"                            |
  "&"                            |
  "|"                            |
  "^"                            |
  "%"                            |
  "<<"                           |
  ">>"                           |
  ">>>"                          |
  "+="                           |
  "-="                           |
  "*="                           |
  "/="                           |
  "&="                           |
  "|="                           |
  "^="                           |
  "%="                           |
  "<<="                          |
  ">>="                          |
  ">>>="                         { return JsTokenId.OPERATOR; }

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
<<EOF>>                          { return JsTokenId.UNKNOWN_TOKEN; }
