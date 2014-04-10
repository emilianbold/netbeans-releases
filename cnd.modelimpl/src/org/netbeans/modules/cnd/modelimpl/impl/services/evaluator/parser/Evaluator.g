grammar Evaluator;

options {
    tokenVocab = APTTokenTypes;
}

@header {
package org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.parser.generated;

import java.util.HashMap;
import org.netbeans.modules.cnd.modelimpl.impl.services.evaluator.VariableProvider;
}

@members {
    /** Map variable name to Integer object holding value */
    HashMap memory = new HashMap();

    VariableProvider vp;

    public void setVariableProvider(VariableProvider vp) {
        this.vp = vp;
    }

    public void displayRecognitionError(String[] tokenNames,
                                        RecognitionException e) {
        // do nothing
    }
}

prog: expr;

expr returns [int value]
    :   e=equalityExpr {$value = $e.value;}
        (   PLUS e=equalityExpr {$value += $e.value;}
        |   MINUS e=equalityExpr {$value -= $e.value;}
        )*
    ;

equalityExpr returns [int value]
    :
        e = multExpr {$value = $e.value;}
        ( EQUAL e = multExpr {$value = ($value == $e.value) ? 1 : 0;}
        | NOTEQUAL e = multExpr {$value = ($value != $e.value) ? 1 : 0;}
        )*
    ;

multExpr returns [int value]
    :   e=unaryExpr {$value = $e.value;} (STAR e=unaryExpr {$value *= $e.value;})*
    ;

unaryExpr returns [int value]
    :   e=atom {$value = $e.value;}
    |   NOT e=unaryExpr {$value = ($e.value == 0 ? 1 : 0);}
    ;

atom returns [int value]
    :   
        DECIMALINT {$value = (($DECIMALINT.text) == null) ? 0 : Integer.parseInt(($DECIMALINT.text).replaceAll("[a-z,A-Z,_].*", "")) ;}
    |   (function_call) => function_call {$value = $function_call.value;}
    |   variable {$value = $variable.value;}
    |   LPAREN expr RPAREN {$value = $expr.value;}
    |   LITERAL_sizeof balance_lparen_rparen {$value = vp==null ? 0 : vp.getSizeOfValue($balance_lparen_rparen.s);}
    |   LITERAL_static_cast LESSTHAN (~GREATERTHAN)* GREATERTHAN LPAREN expr RPAREN {$value = $expr.value;}
    |   LITERAL_true
        {
            $value = vp==null?0:vp.getValue($LITERAL_true.text);
        }
    |   LITERAL_false
        {
            $value = vp==null?0:vp.getValue($LITERAL_false.text);
        }
    ;

function_call returns [int value]
    :
        id = qualified_id 
        args = balance_lparen_rparen
        { $value = vp==null ? 0 : vp.getValue($id.q + $args.s); }
    ;

variable returns [int value]
    :
        (LITERAL_const)*
        id = qualified_id
        { $value = vp==null ? 0 : vp.getValue($id.q); }
    ;

qualified_id returns [String q = ""] 
    :
        so = scope_override
        { q += ($so.s != null)? $so.s : ""; }
        (
            IDENT
            {q += $IDENT.text;}
            (
                inner = balance_less_greater {q += $inner.s;}
            )?
        )
    ;

scope_override returns [String s = ""]
    :
        (
            SCOPE { s += "::";}
        )?
        (
            (IDENT (balance_less_greater)? SCOPE)=> sp = scope_override_part
            {
                    s += ($sp.s != null) ? $sp.s : "";
            }
        )?
    ;

scope_override_part returns [String s = ""]
    :
        IDENT
        {
            s += $IDENT.text;
        }
        (
            inner = balance_less_greater {s += $inner.s;}
        )?
        SCOPE
        {
            s += "::";
        }

        ((IDENT (balance_less_greater)? SCOPE)=> sp = scope_override_part)?
        {
            s += ($sp.s != null) ? $sp.s : "";
        }
    ;

balance_less_greater returns [String s = ""]
    :
        LESSTHAN {s += "<";}
        (
            (LESSTHAN)=> inner = balance_less_greater {s += $inner.s;}
        |
            other = (~GREATERTHAN) {s += $other.text;}
        )*
        GREATERTHAN {s += "> ";}
    ;

balance_lparen_rparen returns [String s = ""]
    :
        LPAREN {s += "(";}
        (
            (LPAREN)=> inner = balance_lparen_rparen {s += $inner.s;}
        |
            other = (~RPAREN) {s += $other.text;}
        )*
        RPAREN {s += ")";}
    ;

// Suppressing warnings "no lexer rule corresponding to token"
fragment IDENT: ' ';
fragment DECIMALINT: ' ';

fragment PLUS: ' ';
fragment MINUS: ' ';
fragment STAR: ' ';
fragment LESSTHAN: ' ';
fragment GREATERTHAN: ' ';
fragment NOT: ' ';
fragment EQUAL: ' ';
fragment NOTEQUAL: ' ';

fragment SCOPE: ' ';

fragment LPAREN: ' ';
fragment RPAREN: ' ';

fragment LITERAL_sizeof: ' ';
fragment LITERAL_static_cast: ' ';
fragment LITERAL_true: ' ';
fragment LITERAL_false: ' ';

fragment LITERAL_const: ' ';

