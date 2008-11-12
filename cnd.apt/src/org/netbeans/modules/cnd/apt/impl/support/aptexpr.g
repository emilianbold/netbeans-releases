//
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
//
// Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
//
// The contents of this file are subject to the terms of either the GNU
// General Public License Version 2 only ("GPL") or the Common
// Development and Distribution License("CDDL") (collectively, the
// "License"). You may not use this file except in compliance with the
// License. You can obtain a copy of the License at
// http://www.netbeans.org/cddl-gplv2.html
// or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
// specific language governing permissions and limitations under the
// License.  When distributing the software, include this License Header
// Notice in each file and include the License file at
// nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
// particular file as subject to the "Classpath" exception as provided
// by Sun in the GPL Version 2 section of the License file that
// accompanied this code. If applicable, add the following below the
// License Header, with the fields enclosed by brackets [] replaced by
// your own identifying information:
// "Portions Copyrighted [year] [name of copyright owner]"
//
// Contributor(s):
//
// The Original Software is NetBeans. The Initial Developer of the Original
// Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
// Microsystems, Inc. All Rights Reserved.
//
// If you wish your version of this file to be governed by only the CDDL
// or only the GPL Version 2, indicate your decision by adding
// "[Contributor] elects to include this software in this distribution
// under the [CDDL or GPL Version 2] license." If you do not indicate a
// single choice of license, a recipient has the option to distribute
// your version of this file under either the CDDL, the GPL Version 2 or
// to extend the choice of license to its licensees as provided above.
// However, if you add GPL Version 2 code and therefore, elected the GPL
// Version 2 license, then the option applies only if the new code is
// made subject to such option by the copyright holder.
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
	language = "Java"; // NOI18N
} 

class APTExprParser extends Parser;

options {
//	k = 2;
	importVocab = APTGenerated;
	codeGenMakeSwitchThreshold = 2;
	codeGenBitsetTestThreshold = 3;
//	noConstructors = true;
	buildAST = false;
}

{
    private APTMacroCallback callback = null;
    
    public APTExprParser(TokenStream lexer, APTMacroCallback callback) {
        this(lexer,1);
        this.callback = callback;
    }

    private boolean isDefined(Token id) {
        if (id != null && callback != null) {
            return callback.isDefined(id);
        }
        return false;
    }

    private boolean toBoolean(long r) {
        return r == 0 ? false : true;
    }

    // Fixup: workaround is added due to bug in jdk6 Update 10 (see IZ#150693)
    private static long one = 1;
    private static long zero = 0;
    private long toLong(boolean b) {
        return b ? one : zero;
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

    private long evalID(Token id) {
        // each not expanded ID in expression is '0' by specification
        return 0;
    }

}

imaginaryTokenDefinitions :
   SIGN_MINUS
   SIGN_PLUS
;

expr      returns [long r] {r=0;} : r=ternCondExpr | EOF;
// ternCondExpr uses * because ? generates incorrect code in ANTLR 2.7.5
// don't want to use guessing, because it slows down code
ternCondExpr returns [long r] : {long b,c;}   r=orExpr 
                (options{generateAmbigWarnings = false;}:
                    QUESTIONMARK^ b=ternCondExpr COLON! c=ternCondExpr { r = toBoolean(r)?b:c;}
                )*
        ;
//rule        :   QUESTIONMARK^ ternCondExpr COLON! ternCondExpr;
orExpr    returns [long r] : {long b;}  r=andExpr (OR^ b=andExpr {r=toLong(toBoolean(r) || toBoolean(b));})*;
andExpr   returns [long r] : {long b;}  r=borExpr (AND^ b=borExpr {r=toLong(toBoolean(r) && toBoolean(b));})*;
borExpr   returns [long r] : {long b;}  r=xorExpr (BITWISEOR^ b=xorExpr {r=r|b;})*;
xorExpr   returns [long r] : {long b;}  r=bandExpr (BITWISEXOR^ b=bandExpr {r=r^b;})*;
bandExpr  returns [long r] : {long b;}  r=eqExpr  (AMPERSAND^ b=eqExpr {r=r&b;})*;
eqExpr    returns [long r] : {long b;}  r=relExpr (EQUAL^ b=relExpr {r= toLong(r == b);} 
                                                 | NOTEQUAL^ b=relExpr {r= toLong(r != b);})*;
relExpr   returns [long r] : {long b;}  r=shiftExpr (LESSTHAN^ b=shiftExpr { r= toLong(r < b); }
                                                    |LESSTHANOREQUALTO^ b=shiftExpr { r= toLong(r <= b); }
                                                    |GREATERTHAN^ b=shiftExpr { r= toLong(r > b); }
                                                    |GREATERTHANOREQUALTO^ b=shiftExpr { r= toLong(r >= b); })*;
shiftExpr returns [long r] : {long b;}  r=sumExpr (SHIFTLEFT^ b=sumExpr { r= r << b; }
                                                  |SHIFTRIGHT^ b=sumExpr { r= r >> b; })*;
sumExpr   returns [long r] : {long b;}  r=prodExpr (PLUS^ b=prodExpr { r= r + b; }
                                                   |MINUS^ b=prodExpr { r= r - b; })* ;
prodExpr  returns [long r] : {long b;}  r=signExpr (STAR^ b=signExpr { r=r*b; }
                                                   |DIVIDE^ b=signExpr { r=r/b; }
                                                   |MOD^ b=signExpr { r=r%b; } )* ;
signExpr  returns [long r] {r=0;}:   
                      MINUS^ r=atom { r=-1*r; }
                    | PLUS^  r=atom { r= (r<0) ? 0-r : r; }
                    | NOT^ r=atom { r=toLong(!toBoolean(r)); }
                    | TILDE^ r=atom { r=~r; }
                | r=atom ;
atom returns [long r]  {r=0;}     : r=constant | r=defined | (LPAREN^ r=expr RPAREN!) ;
//atom        : constant | NUMBER | defined | ID | (LPAREN^ expr RPAREN!) ;

defined returns [long r] {r=0;} : 
        DEFINED^
        (
            (LPAREN! id_1:ID_DEFINED RPAREN!) { r = toLong(isDefined(id_1)); }
            | id_2:ID_DEFINED { r = toLong(isDefined(id_2)); }
        )
;

constant returns [long r] {r=0;}
            :	LITERAL_true { r=toLong(true);}
            |	LITERAL_false { r=toLong(false);}
            |   n:NUMBER {r=toLong(n.getText());}
            |   id:ID {r=evalID(id);}
            | o:OCTALINT {r=toLong(o.getText());}
            | d:DECIMALINT {r=toLong(d.getText());}
            | x:HEXADECIMALINT {r=toLong(x.getText());}
            | c: CHAR_LITERAL { r=c.getText().charAt(1); }
//          | f1: FLOATONE {r=Integer.parseInt(f1.getText());}
//          | f2: FLOATTWO {r=Integer.parseInt(f2.getText());}
	;

/* APTExpressionWalker is not used any more, because all evaluations are done in APTExprParser
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
