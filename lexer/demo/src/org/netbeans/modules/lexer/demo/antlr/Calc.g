
/* Antlr grammar file for the Calc example. */

header {
package org.netbeans.modules.lexer.demo.antlr;
}

class CalcScanner extends Lexer;
options { k=3; }

WHITESPACE  : (' '
            | '\t'
            | '\n'
            | '\r')+
            ;

PLUS        : '+'
            ;

MINUS       : '-'
            ;

MUL         : ("***") => MUL3 { $setType(MUL3); }
            | ("**" ~('*')) => '*'
            | '*'
            ;
            
protected 
MUL3        : "***"  
            ; 

DIV         : '/'
            ;

LPAREN      : '('
            ;

RPAREN      : ')'
            ;


CONSTANT    : FLOAT (('e' | 'E') ('+' | '-')? INTEGER )?
            ;

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

ERROR       : '\u0000'..'\ufffe' /* \uffff is EOF representation */
            ;
