package org.netbeans.modules.javascript2.editor.lexer;

import org.netbeans.spi.lexer.LexerInput;
import org.netbeans.spi.lexer.LexerRestartInfo;

%%

%public
%final
%class JsonColoringLexer
%type CommonTokenId
%unicode
%char

%{
    private LexerInput input;

    public JsonColoringLexer(LexerRestartInfo info) {
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

    public CommonTokenId nextToken() throws java.io.IOException {
        return yylex();
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
            int hash = 5;
            hash = 29 * hash + this.zzState;
            hash = 29 * hash + this.zzLexicalState;
            return hash;
        }

        @Override
        public String toString() {
            return "LexerState{" + "zzState=" + zzState + ", zzLexicalState=" + zzLexicalState + '}';
        }
    }

 // End user code

%}

/* main character classes */
LineTerminator = \r|\n|\r\n
InputCharacter = [^\r\n]

WhiteSpace = [ \t\f]+

OctDigit          = [0-7]

/* number literals */
NumberLiteral = "-"?[0-9]+({Fraction}|{Exponent}|{Fraction}{Exponent})?

Fraction = \. [0-9]*
Exponent = [eE] [+-]? [0-9]+

/* string and character literals */
StringCharacter  = [^\r\n\"\\] | \\{LineTerminator}

%state STRING
%state STRINGEND
%state ERROR

%%

<YYINITIAL> {

  /* boolean literals */
  "true"                         { return CommonTokenId.KEYWORD_TRUE; }
  "false"                        { return CommonTokenId.KEYWORD_FALSE; }

  /* null literal */
  "null"                         { return CommonTokenId.KEYWORD_NULL; }

  /* operators */
  "{"                            { return CommonTokenId.BRACKET_LEFT_CURLY; }
  "}"                            { return CommonTokenId.BRACKET_RIGHT_CURLY; }
  "["                            { return CommonTokenId.BRACKET_LEFT_BRACKET; }
  "]"                            { return CommonTokenId.BRACKET_RIGHT_BRACKET; }
  ","                            { return CommonTokenId.OPERATOR_COMMA; }
  ":"                            { return CommonTokenId.OPERATOR_COLON; }
  
  /* string literal */
  \"                             {
                                    yybegin(STRING);
                                    return CommonTokenId.STRING_BEGIN;
                                 }

  /* numeric literals */
  {NumberLiteral}                { return CommonTokenId.NUMBER; }

  /* whitespace */
  {WhiteSpace}                   { return CommonTokenId.WHITESPACE; }

  /* whitespace */
  {LineTerminator}               { return CommonTokenId.EOL; }

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


<ERROR> {
  .*{LineTerminator}             {
                                     yypushback(1);
                                     yybegin(YYINITIAL);
                                     if (tokenLength - 1 > 0) {
                                         return CommonTokenId.UNKNOWN;
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
