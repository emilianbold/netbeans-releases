
/* Antlr grammar file for the Calc example. */

header {
package org.netbeans.modules.lexer.demo.antlr;
}

class CalcScanner extends Lexer;
options {
    k = 3;
    charVocabulary = '\0'..'\ufffe';
}

{

    /**
     * State variable used to hold current lexer state.
     * In this case it's used for incomplete tokens only.
     */
    private int state;

    int getState() {
        return state;
    }

    void resetState() {
        state = 0;
    }

}

WHITESPACE  : (' '
            | '\t'
            | '\n'
            | '\r')+
            ;

PLUS        : '+'
            ;

MINUS       : '-'
            ;

MUL         : '*'
            ;
            
DIV         : '/'
            ;

LPAREN      : '('
            ;

RPAREN      : ')'
            ;

ABC         : "abc"
            ;

CONSTANT    : FLOAT (('e' | 'E') ('+' | '-')? INTEGER )?
            ;

ML_COMMENT  : INCOMPLETE_ML_COMMENT { state = CalcScannerTokenTypes.INCOMPLETE_ML_COMMENT; }
            (  { LA(2) != '/' }? '*'
               | ~('*')
            )*
            "*/" { state = 0; }
            ;

/* Protected tokens are used internally by the scanner only */
protected
FLOAT       : (INTEGER ('.' INTEGER)?
            | '.' INTEGER)
            ;

protected
INTEGER     : (DIGIT)+
            ;

protected
DIGIT       : '0'..'9'
            ;

protected
INCOMPLETE_ML_COMMENT   : "/*"
                        ;
