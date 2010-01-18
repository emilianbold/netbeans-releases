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

}

prog: expr;

expr returns [int value]
    :   e=multExpr {$value = $e.value;}
        (   PLUS e=multExpr {$value += $e.value;}
        |   MINUS e=multExpr {$value -= $e.value;}
        )*
    ;

multExpr returns [int value]
    :   e=atom {$value = $e.value;} (STAR e=atom {$value *= $e.value;})*
    ;

atom returns [int value]
    :   DECIMALINT {$value = Integer.parseInt($DECIMALINT.text);}
    |   ID
        {
            $value = vp==null?0:vp.getValue($ID.text);
            //Integer v = (Integer)memory.get($ID.text);
            //if ( v!=null ) $value = v.intValue();
            //else System.err.println("undefined variable "+$ID.text);
        }
    |   LPAREN expr RPAREN {$value = $expr.value;}
    ;

// Suppressing warnings "no lexer rule corresponding to token"
fragment ID: ' ';
fragment DECIMALINT: ' ';

fragment PLUS: ' ';
fragment MINUS: ' ';
fragment STAR: ' ';

fragment LPAREN: ' ';
fragment RPAREN: ' ';

