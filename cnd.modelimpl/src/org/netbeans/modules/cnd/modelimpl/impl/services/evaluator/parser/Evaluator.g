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
    |   id = qualified_id
        {
            $value = vp==null?0:vp.getValue($id.q);
            //Integer v = (Integer)memory.get($ID.text);
            //if ( v!=null ) $value = v.intValue();
            //else System.err.println("undefined variable "+$ID.text);
        }
    |   LPAREN expr RPAREN {$value = $expr.value;}
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

qualified_id returns [String q = ""] 
    :
        so = scope_override
        { q += ($so.s != null)? $so.s : ""; }
        (
            ID
            {q += $ID.text;}
            (
                LESSTHAN
                {q += "<";}
                (x=~GREATERTHAN {q += $x.text;})*
                GREATERTHAN
                {q += ">";}
            )?
        )
    ;

scope_override returns [String s = ""]
    :
        (
            SCOPE { s += "::";}
        )?
        (
            sp = scope_override_part
            {
                    s += ($sp.s != null) ? $sp.s : "";
            }
        )?
    ;

scope_override_part returns [String s = ""]
    :
        ID
        {
            s += $ID.text;
        }
        (
            LESSTHAN
            {s += "<";}
            (x=~GREATERTHAN {s += $x.text;})*
            GREATERTHAN
            {s += ">";}
        )?
        SCOPE
        {
            s += "::";
        }

        ((ID SCOPE) => sp = scope_override_part)?
        {
            s += ($sp.s != null) ? $sp.s : "";
        }
    ;

// Suppressing warnings "no lexer rule corresponding to token"
fragment ID: ' ';
fragment DECIMALINT: ' ';

fragment PLUS: ' ';
fragment MINUS: ' ';
fragment STAR: ' ';
fragment LESSTHAN: ' ';
fragment GREATERTHAN: ' ';

fragment SCOPE: ' ';

fragment LPAREN: ' ';
fragment RPAREN: ' ';

fragment LITERAL_static_cast: ' ';
fragment LITERAL_true: ' ';
fragment LITERAL_false: ' ';

