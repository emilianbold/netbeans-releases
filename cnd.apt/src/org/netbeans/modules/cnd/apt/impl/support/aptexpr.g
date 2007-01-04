//
// The contents of this file are subject to the terms of the Common Development
// and Distribution License (the License). You may not use this file except in
// compliance with the License.
// 
// You can obtain a copy of the License at http://www.netbeans.org/cddl.html
// or http://www.netbeans.org/cddl.txt.
// 
// When distributing Covered Code, include this CDDL Header Notice in each file
// and include the License file at http://www.netbeans.org/cddl.txt.
// If applicable, add the following below the CDDL Header, with the fields
// enclosed by brackets [] replaced by your own identifying information:
// "Portions Copyrighted [year] [name of copyright owner]"
// 
// The Original Software is NetBeans. The Initial Developer of the Original
// Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
// Microsystems, Inc. All Rights Reserved.
//

/*
 *
 * Parser for preprocessor expressions
 */
header {

package org.netbeans.modules.cnd.apt.impl.support.generated;

import java.io.*;
import java.util.*;

import antlr.*;
import antlr.collections.*;
import antlr.debug.misc.*;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
}

options {
	language = "Java";
} 

class APTExprParser extends Parser;

options {
//	k = 2;
	importVocab = APTGenerated;
	codeGenMakeSwitchThreshold = 2;
	codeGenBitsetTestThreshold = 3;
//	noConstructors = true;
	buildAST = true;
}

imaginaryTokenDefinitions :
   SIGN_MINUS
   SIGN_PLUS
;

expr        :   ternCondExpr | EOF;
// ternCondExpr uses * because ? generates incorrect code in ANTLR 2.7.5
// don't want to use guessing, because it slows down code
ternCondExpr:   orExpr 
                (options{generateAmbigWarnings = false;}:
                    QUESTIONMARK^ ternCondExpr COLON! ternCondExpr
                )*
        ;
//rule        :   QUESTIONMARK^ ternCondExpr COLON! ternCondExpr;
orExpr      :   andExpr (OR^ andExpr)*;
andExpr     :   borExpr (AND^ borExpr)*;
borExpr     :   xorExpr (BITWISEOR^ xorExpr)*;
xorExpr     :   bandExpr (BITWISEXOR^ bandExpr)*;
bandExpr    :   eqExpr  (AMPERSAND^ eqExpr)*;
eqExpr      :   relExpr ((EQUAL^|NOTEQUAL^) relExpr)*;
relExpr     :   shiftExpr ((LESSTHAN^|LESSTHANOREQUALTO^|GREATERTHAN^|GREATERTHANOREQUALTO^) shiftExpr)*;
shiftExpr   :   sumExpr ((SHIFTLEFT^|SHIFTRIGHT^) sumExpr)*;
sumExpr     :   prodExpr ((PLUS^|MINUS^) prodExpr)* ;
prodExpr    :   signExpr ((STAR^|DIVIDE^|MOD^) signExpr)* ;
signExpr    :   (
                      m:MINUS^ {#m.setType(SIGN_MINUS);}
                    | p:PLUS^  {#p.setType(SIGN_PLUS);}
                    | NOT^
                    | TILDE^
                )? atom ;
atom        : constant | defined | (LPAREN^ expr RPAREN!) ;
//atom        : constant | NUMBER | defined | ID | (LPAREN^ expr RPAREN!) ;

defined: 
        DEFINED^
        (
            (LPAREN! ID_DEFINED RPAREN!) 
            | ID_DEFINED
        ) 
;

constant
            :	LITERAL_true
            |	LITERAL_false
            |   NUMBER
            |   ID
            |   OCTALINT
            |	DECIMALINT
            |	HEXADECIMALINT
            |   CHAR_LITERAL     
//            |	FLOATONE
//            |	FLOATTWO
	;

class APTExpressionWalker extends TreeParser;
{
    private APTMacroCallback callback = null;
    public APTExpressionWalker(APTMacroCallback callback) {
        this.callback = callback;
    }

    private boolean isDefined(AST id) {
        if (id != null && callback != null) {
            return callback.isDefined(getToken(id));
        }
        return false;
    }

    private Token astToken = new CommonToken();
    private Token getToken(AST ast) {
        astToken.setType(ast.getType());
        astToken.setText(ast.getText());
        return astToken;
    }

    private long evalID(AST id) {
        // each not expanded ID in expression is '0' by specification
        return 0;
    }

    private boolean toBoolean(long r) {
        return r == 0 ? false : true;
    }

    private long toLong(boolean b) {
        return b ? 1 : 0;
    }

    private long toLong(String str) {
        long val = Long.MAX_VALUE;
        try {
            val = Long.decode(remSuffix(str)).longValue();
        } catch (NumberFormatException ex) {
            //ex.printStackTrace(System.err);
        }
        return val;
    }

    private String remSuffix(String num) {
        int len = num.length();
        boolean stop;
        do {
            stop = true;
            if (len > 0) {
                char last = num.charAt(len - 1);
                // remove postfix like u, U, l, L
                if (last == 'u' || last == 'U' || last == 'l' || last == 'L') {
                    num = num.substring(0, len - 1);
                    len--;
                    stop = false;
                }
            }
        } while (!stop);
        return num;
    }
}

expr returns [long r]
    { 
        long a,b; 
        long q;
        boolean def;
        r=0; 
    }
    : #(QUESTIONMARK q=expr a=expr b=expr) { r = toBoolean(q)?a:b;}
    | #(OR a=expr b=expr) { r=toLong(toBoolean(a)||toBoolean(b)); }
    | #(AND a=expr b=expr) { r=toLong(toBoolean(a)&&toBoolean(b)); }
    | #(BITWISEOR a=expr b=expr) { r= a | b; }  
    | #(BITWISEXOR a=expr b=expr) { r= a ^ b; }  
    | #(AMPERSAND a=expr b=expr) { r= a & b; }  
    | #(EQUAL a=expr b=expr) { r= toLong(a == b); }
    | #(NOTEQUAL a=expr b=expr) { r= toLong(a != b); }
    | #(LESSTHAN a=expr b=expr) { r= toLong(a < b); }
    | #(LESSTHANOREQUALTO a=expr b=expr) { r= toLong(a <= b); }
    | #(GREATERTHAN a=expr b=expr) { r= toLong(a > b); }
    | #(GREATERTHANOREQUALTO a=expr b=expr) { r= toLong(a >= b); }
    | #(SHIFTLEFT a=expr b=expr) { r= a << b; }
    | #(SHIFTRIGHT a=expr b=expr) { r= a >> b; }
    | #(PLUS  a=expr b=expr) { r=a+b; }
    | #(MINUS a=expr b=expr) { r=a-b; }
    | #(STAR   a=expr b=expr) { r=a*b; }
    | #(DIVIDE   a=expr b=expr) 
            {
                try {
                    r=a/b;
                } catch (ArithmeticException ex) {
                    //System.err.println(ex);
                    r = 0;
                }
            }
    | #(MOD   a=expr b=expr) 
            {
                try {
                    r=a%b;
                } catch (ArithmeticException ex) {
                    //System.err.println(ex);
                    r = 0;
                }
            }
    | #(SIGN_MINUS a=expr)   { r=-1*a; } 
    | #(SIGN_PLUS  a=expr)   { r= (a<0) ? 0-a : a; }
    | #(DEFINED def=defined) {r=toLong(def);}
    | #(NOT a=expr)   { r=toLong(!toBoolean(a)); } 
    | #(TILDE a=expr)   { r=~a; } 
    | #(LPAREN a=expr)       { r=a; }
    | LITERAL_true { r=toLong(true);}
    | LITERAL_false { r=toLong(false);}
    | n:NUMBER {r=toLong(n.getText());}
    | id: ID       {r=evalID(id);}
//  | i:constant { r=(double)Integer.parseInt(i.getText()); }
    | o:OCTALINT {r=toLong(o.getText());}
    | d:DECIMALINT {r=toLong(d.getText());}
    | x:HEXADECIMALINT {r=toLong(x.getText());}
    | c: CHAR_LITERAL { r=c.getText().charAt(1); }
//    | f1: FLOATONE {r=Integer.parseInt(f1.getText());}
//    | f2: FLOATTWO {r=Integer.parseInt(f2.getText());}
    | EOF { r = 0; }
  ;

defined returns [boolean r]
  { r=false; } : id: ID_DEFINED { r = isDefined(id); }
;

/*
constant return [long r]
  { r=10; }
    :	#(OCTALINT)
    |	#(DECIMALINT)
    |	#(HEXADECIMALINT)
//    |	CharLiteral
//	|	(StringLiteral)+
    |	#(FLOATONE)
    |	#(FLOATTWO)
    |	#(LITERAL_true)
    |	#(LITERAL_false)
;
*/  
