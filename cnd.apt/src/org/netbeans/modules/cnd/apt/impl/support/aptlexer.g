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
// Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
// Microsystems, Inc. All Rights Reserved.
//

// Start of APTLexer.cpp block
header {

package org.netbeans.modules.cnd.apt.impl.support.generated;

import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.apt.support.APTToken;


}

options {
	language = "Java"; // NOI18N
} 

{
@SuppressWarnings({"unchecked", "cast"})
}
class APTLexer extends Lexer;

options {
    k = 2;
    exportVocab = APTGenerated;
    testLiterals = false;
    charVocabulary = '\u0003'..'\ufffe';  // NOI18N
}

// DW 4/11/02 put in to support manual hoisting
tokens {
    FLOATONE;
    FLOATTWO;
    ELLIPSIS;
    HEXADECIMALINT;
    DOT;
    OCTALINT;
    DECIMALINT;
    // preprocessor directives
    INCLUDE;
    INCLUDE_NEXT;
    DEFINE;
    UNDEF;
    IFDEF;
    IFNDEF;
    IF;
    ELIF;
    ELSE;
    ENDIF;
    PRAGMA;
    LINE;
    ERROR;
    PREPROC_DIRECTIVE; // unrecongnized #-directive

    LITERAL_OPERATOR = "operator"; // NOI18N
    LITERAL_alignof="alignof"; // NOI18N
    LITERAL___alignof__="__alignof__"; // NOI18N
    LITERAL_typeof="typeof"; // NOI18N
    LITERAL___typeof="__typeof"; // NOI18N
    LITERAL___typeof__="__typeof__"; // NOI18N
    LITERAL_template="template"; // NOI18N
    LITERAL_typedef="typedef"; // NOI18N
    LITERAL_enum="enum"; // NOI18N
    LITERAL_namespace="namespace"; // NOI18N
    LITERAL_extern="extern"; // NOI18N
    LITERAL_inline="inline"; // NOI18N
    LITERAL__inline="_inline"; // NOI18N
    LITERAL___inline="__inline"; // NOI18N
    LITERAL___inline__="__inline__"; // NOI18N
    LITERAL_virtual="virtual"; // NOI18N
    LITERAL_explicit="explicit"; // NOI18N
    LITERAL_friend="friend"; // NOI18N
    LITERAL__stdcall="_stdcall"; // NOI18N
    LITERAL___stdcall="__stdcall"; // NOI18N
    LITERAL_typename="typename"; // NOI18N
    LITERAL_auto="auto"; // NOI18N
    LITERAL_register="register"; // NOI18N
    LITERAL_static="static"; // NOI18N
    LITERAL_mutable="mutable"; // NOI18N
    LITERAL_const="const"; // NOI18N
    LITERAL___const="__const"; // NOI18N
    LITERAL_const_cast="const_cast"; // NOI18N
    LITERAL_volatile="volatile"; // NOI18N
    LITERAL___volatile__="__volatile__"; // NOI18N
    LITERAL_char="char"; // NOI18N
    LITERAL_wchar_t="wchar_t"; // NOI18N
    LITERAL_bool="bool"; // NOI18N
    LITERAL_short="short"; // NOI18N
    LITERAL_int="int"; // NOI18N
    LITERAL_long="long"; // NOI18N
    LITERAL_signed="signed"; // NOI18N
    LITERAL___signed__="__signed__"; // NOI18N
    LITERAL_unsigned="unsigned"; // NOI18N
    LITERAL___unsigned__="__unsigned__"; // NOI18N
    LITERAL_float="float"; // NOI18N
    LITERAL_double="double"; // NOI18N
    LITERAL_void="void"; // NOI18N
    LITERAL__declspec="_declspec"; // NOI18N
    LITERAL___declspec="__declspec"; // NOI18N
    LITERAL_class="class"; // NOI18N
    LITERAL_struct="struct"; // NOI18N
    LITERAL_union="union"; // NOI18N    
    LITERAL_this="this"; // NOI18N
    LITERAL_true="true"; // NOI18N
    LITERAL_false="false"; // NOI18N
    LITERAL_public="public"; // NOI18N
    LITERAL_protected="protected"; // NOI18N
    LITERAL_private="private"; // NOI18N
    LITERAL_throw="throw"; // NOI18N
    LITERAL_case="case"; // NOI18N
    LITERAL_default="default"; // NOI18N
    LITERAL_if="if"; // NOI18N
    LITERAL_else="else"; // NOI18N
    LITERAL_switch="switch"; // NOI18N
    LITERAL_while="while"; // NOI18N
    LITERAL_do="do"; // NOI18N
    LITERAL_for="for"; // NOI18N
    LITERAL_goto="goto"; // NOI18N
    LITERAL_continue="continue"; // NOI18N
    LITERAL_break="break"; // NOI18N
    LITERAL_return="return"; // NOI18N
    LITERAL_try="try"; // NOI18N
    LITERAL_catch="catch"; // NOI18N
    LITERAL_using="using"; // NOI18N
    LITERAL_export="export"; // NOI18N
    LITERAL_asm="asm"; // NOI18N
    LITERAL__asm="_asm"; // NOI18N
    LITERAL___asm="__asm"; // NOI18N
    LITERAL___asm__="__asm__"; // NOI18N
    LITERAL_sizeof="sizeof"; // NOI18N
    LITERAL_dynamic_cast="dynamic_cast"; // NOI18N
    LITERAL_static_cast="static_cast"; // NOI18N
    LITERAL_reinterpret_cast="reinterpret_cast"; // NOI18N
    LITERAL_new="new"; // NOI18N
    LITERAL__cdecl="_cdecl"; // NOI18N
    LITERAL___cdecl="__cdecl"; // NOI18N
    LITERAL__near="_near"; // NOI18N
    LITERAL___near="__near"; // NOI18N
    LITERAL__far="_far"; // NOI18N
    LITERAL___far="__far"; // NOI18N
    LITERAL___interrupt="__interrupt"; // NOI18N
    LITERAL_pascal="pascal"; // NOI18N
    LITERAL__pascal="_pascal"; // NOI18N
    LITERAL___pascal="__pascal"; // NOI18N
    LITERAL_delete="delete"; // NOI18N
    LITERAL__int64="_int64"; // NOI18N
    LITERAL___int64="__int64"; // NOI18N
    LITERAL___w64="__w64"; // NOI18N
    LITERAL___extension__="__extension__"; // NOI18N
    LITERAL___attribute__="__attribute__"; // NOI18N
    LITERAL___restrict="__restrict"; // NOI18N
    LITERAL___complex="__complex__"; // NOI18N
    LITERAL___imag="__imag__"; // NOI18N
    LITERAL___real="__real__"; // NOI18N          
    LITERAL___global="__global"; // NOI18N   

	ASSIGNEQUAL;
	COLON;
	COMMA;
	QUESTIONMARK;
	SEMICOLON;
	POINTERTO;
	LPAREN;
	RPAREN;
	LSQUARE;
	RSQUARE;
	LCURLY;
	RCURLY;
	EQUAL;
	NOTEQUAL;
	LESSTHANOREQUALTO;
	LESSTHAN;
	GREATERTHANOREQUALTO;
	GREATERTHAN;
	DIVIDE;
	DIVIDEEQUAL;
	PLUS;
	PLUSEQUAL;
	PLUSPLUS;
	MINUS;
	MINUSEQUAL;
	MINUSMINUS;
	STAR;
	TIMESEQUAL;
	MOD;
	MODEQUAL;
	SHIFTRIGHT;
	SHIFTRIGHTEQUAL;
	SHIFTLEFT;
	SHIFTLEFTEQUAL;
	AND;
	NOT;
	OR;
	AMPERSAND;
	BITWISEANDEQUAL;
	TILDE;
	BITWISEOR;
	BITWISEOREQUAL;
	BITWISEXOR;
	BITWISEXOREQUAL;
	POINTERTOMBR;
	DOTMBR;
	SCOPE;
	Whitespace;
	EndOfLine;
	DEFINED;
	DBL_SHARP;
	SHARP;
	Skip;
	PreProcComment;
	PPLiterals;
	Space;
	PreProcBlockComment;
	PreProcLineComment;
	Comment;
	CPPComment;
	CHAR_LITERAL;
	STRING_LITERAL;
	InterStringWhitespace;
	StringPart;
	Escape;
	Digit;
	Decimal;
	LongSuffix;
	UnsignedSuffix;
	FloatSuffix;
	Exponent;
	Vocabulary;
	NUMBER;
	ID;

    // preprocessor specific tokens
    INCLUDE_STRING;
    SYS_INCLUDE_STRING;
    END_PREPROC_DIRECTIVE;
    FUN_LIKE_MACRO_LPAREN;
    BACK_SLASH;
}
{


    private boolean reportErrors;
    public void init(String filename, int flags) {
        preprocPossible = true;
        preprocPending = false;
        reportErrors = true;

        setFilename(filename);
//        if ((flags & CPPParser.CPP_SUPPRESS_ERRORS) > 0) {
//            reportErrors = false;
//        }
    }

    // is not used any more, override createToken method instead
    /*public void setTokenObjectClass(Class cl) {
	tokenObjectClass = cl;
    }*/

    // Used instead of setTokenObjectClass method to avoid reflection usage
    protected Token createToken(int type) {
        return APTUtils.createAPTToken(type);
    }

    public void traceIn(String rname) {
        traceDepth ++;
        traceIndent();
        char c = LA(1);
        Object ch = (c == '\n') ? "\\n" : c == '\t' ? "\\t" : ("" + c); // NOI18N
        System.out.println("> lexer " + rname + "; c==" + ch); // NOI18N
    }

    public void traceOut(String rname) {
        traceIndent();
        char c = LA(1);
        Object ch = c == '\n' ? "\\n" : c == '\t' ? "\\t" : ("" + c); // NOI18N
        System.out.println("< lexer " + rname + "; c==" + ch); // NOI18N
        traceDepth--;
    }

    private int errorCount = 0;

    public int getErrorCount() {
        return errorCount;
    }

    public void reportError(RecognitionException e) {

        if (reportErrors) {
            super.reportError(e);
        }
        errorCount++;
    }

    public void reportError(String s) {
        if (reportErrors) {
            super.reportError(s);
        }
        errorCount++;
    }
/*
    protected void printf(String pattern, int i) {
        Printf.printf(pattern, new Object[] { new Integer(i) });
    }

    protected void printf(String pattern, int i, boolean b) {
        Printf.printf(pattern, new Object[] { new Integer(i), Boolean.valueOf(b) });
    }

    protected void printf(String pattern) {
        Printf.printf(pattern, new Object[] {});
    }
*/
	
    private static final int PREPROC_POSSIBLE = 0;

    private static final int PREPROC_PENDING = 1;

    private static final int AFTER_DEFINE = 2;
    /**
     * INCLUDE_STRING token is expected while in this state
     */ 
    private static final int AFTER_INLUDE = 3;


    /**
     *  A '#' character read while in this state would be treated as the
     *  start of a PrprocDirective. Other '#' chars would be treated as
     *  POUND chars.
     */	
    private boolean preprocPossible;
    private boolean isPreprocPossible() {
            return preprocPossible;
    }
    private void setPreprocPossible(boolean possible) {
            this.preprocPossible = possible;
    }

    /**
     *  EndOfLine read while in this state whould be treated as the end
     * of a PreprocDirective and token END_PREPROC_DIRECTIVE will be created
     */
    private boolean preprocPending;
    private boolean isPreprocPending() {
        return preprocPending;
    }
    private void setPreprocPending(boolean pending) {
        this.preprocPending = pending;
    }

    private boolean afterInclude = false;
    private boolean isAfterInclude() {
        return afterInclude;
    }
    private void setAfterInclude(boolean afterInclude) {
        this.afterInclude = afterInclude;
    }


    /**
     * ID read while in this state whould be treated as ID_DEFINED, 
     * need for not expanding ID in expresions like:
     * #if defined MACRO
     */
    private boolean afterPPDefined = false;
    private boolean isAfterPPDefined() {
        return afterPPDefined;
    }
    private void setAfterPPDefined(boolean afterPPDefined) {
        this.afterPPDefined = afterPPDefined;
    }


    /**
     * ID read while in this state whould be treated as ID, but 
     * LA(1) will be checked to switch into "funLikeMacro" state upon 
     * (LA(1) == '(') without leading whitespace 
     * (need for FUN_LIKE_MACRO_LPAREN token)
     */
    private boolean afterDefine = false;
    private boolean isAfterDefine() {
        return afterDefine;
    }
    private void setAfterDefine(boolean afterDefine) {
        this.afterDefine = afterDefine;
    }

    /**
     * FUN_LIKE_MACRO_LPAREN token is expected while in this state
     */
    private boolean funLikeMacro = false;
    private boolean isFunLikeMacro() {
        return funLikeMacro;
    }
    private void setFunLikeMacro(boolean funLikeMacro) {
        this.funLikeMacro = funLikeMacro;
    }
    
    private void clearPrepProcFlags() {
        setFunLikeMacro(false);
        setAfterDefine(false);
        setAfterPPDefined(false);
        setAfterInclude(false);
        setPreprocPending(false);
    }

    protected Token makeToken(int t) {
        APTToken k = (APTToken)super.makeToken(t);
        k.setOffset(tokenStartOffset);
        k.setEndOffset(offset);
        k.setEndColumn(inputState.getColumn());
        k.setEndLine(inputState.getLine());
        // it should be impossible to have preprocessor directive 
        // after valid token. preprocessor directive valid only
        // at start of line @see newline()
        setPreprocPossible(t == END_PREPROC_DIRECTIVE);
        return k;
    }

    public void resetText() {
        super.resetText();
        tokenStartOffset = offset;
    }

    public void consume() {
        super.consume();
        if (inputState.guessing == 0) {
            offset++;
        }
    }

/*
    boolean wasTab;
    public void consume() throws CharStreamException {
        wasTab = false;
        super.consume();
        if (!wasTab) {
            offset++;
        }
    }

    public void tab() {
        wasTab = true;
        int c = getColumn();
        super.tab();
        offset += getColumn() - c;
    }
*/
    public int mark() {
        mkOffset = offset;
        return super.mark(); 
    }

    public void rewind(int mark) {
        super.rewind(mark);
        offset = mkOffset;
    }

    /*public int getOffset() {
        return offset;
    }*/

    int offset = 0;
    int tokenStartOffset = 0;
    int mkOffset = 0;

    public void newline() 
    {
        super.newline();
        if (!isPreprocPending()) {
            setPreprocPossible(true);
        }
    }

    private void deferredNewline() 
    {
        super.newline();
    }
}

/* Operators: */

COMMA          options { constText=true; } : ',' ;
QUESTIONMARK   options { constText=true; } : '?' ;
SEMICOLON      options { constText=true; } : ';' ;

/*
// DOT & ELLIPSIS are commented out since they are generated as part of
// the Number rule below due to some bizarre lexical ambiguity shme.
// DOT  :       '.' ;
// ELLIPSIS      : "..." ;
*/

LPAREN  options { constText=true; }        : '(' 
                    { 
                        if (isFunLikeMacro()) {
                            setFunLikeMacro(false);
                            $setType(FUN_LIKE_MACRO_LPAREN);
                        }
                    }
                ;
RPAREN options { constText=true; } : ')' ;
LSQUARE options { constText=true; }        : '[' ;
RSQUARE options { constText=true; }        : ']' ;
LCURLY	options { constText=true; }	: '{' ;
RCURLY	options { constText=true; }	: '}' ;

TILDE  options { constText=true; }    : '~' ;

FIRST_ASSIGN options { constText=true; } :
    '=' ({$setType(ASSIGNEQUAL);}           //ASSIGNEQUAL     : '=' ;
    | '=' {$setType(EQUAL);});              //EQUAL           : "==" ;

FIRST_DIVIDE :
    '/' ( {$setType(DIVIDE);}               //DIVIDE          : '/' ;
    | '=' {$setType(DIVIDEEQUAL);} )        //DIVIDEEQUAL     : "/=" ;
    | COMMENT {$setType(COMMENT);}
    | CPP_COMMENT {$setType(CPP_COMMENT);};

FIRST_STAR options { constText=true; } :
    '*' ( {$setType(STAR);}                 //STAR            : '*' ;
    | '=' {$setType(TIMESEQUAL);});         //TIMESEQUAL      : "*=" ;

FIRST_MOD options { constText=true; } :
    '%' ( {$setType(MOD);}                  //MOD             : '%' ;
    | '=' {$setType(MODEQUAL);});           //MODEQUAL        : "%=" ;

FIRST_NOT options { constText=true; } :
    '!' ( {$setType(NOT);}                  //NOT             : '!' ;
    | '=' {$setType(NOTEQUAL);});           //NOTEQUAL        : "!=" ;

FIRST_AMPERSAND options { constText=true; } :
    '&' ( {$setType(AMPERSAND);}            //AMPERSAND       : '&' ;
    | '&' {$setType(AND);}                  //AND             : "&&" ;
    | '=' {$setType(BITWISEANDEQUAL);});    //BITWISEANDEQUAL : "&=" ;

FIRST_OR options { constText=true; } :
    '|' ({$setType(BITWISEOR);}             //BITWISEOR       : '|' ;
    | '=' {$setType(BITWISEOREQUAL);}       //BITWISEOREQUAL  : "|=" ;
    | '|' {$setType(OR);});                 //OR              : "||" ;

protected COMMENT : 
		"/*"   
		( options {greedy=false;}:
			EndOfLine {deferredNewline();}
                        | . )*
		"*/"              
	;

protected CPP_COMMENT 
	:	
		"//" ( '\\' ('\n' | '\r') 
                     |  ~('\n' | '\r')
                     )* 
	;

FIRST_BITWISEXOR options { constText=true; } :
    '^' ( {$setType(BITWISEXOR);}           //BITWISEXOR      : '^' ;
    | '=' {$setType(BITWISEXOREQUAL);} );   //BITWISEXOREQUAL : "^=" ;

FIRST_COLON options { constText=true; } :
    ':' ( {$setType(COLON);}                //COLON : ':' ;
    | ':' {$setType(SCOPE);} );             //SCOPE : "::"  ;

FIRST_LESS :
    ( 
    '<' (options{generateAmbigWarnings = false;}:
        {isAfterInclude()}? H_char_sequence ('>')? {$setType(SYS_INCLUDE_STRING);setAfterInclude(false);}
        | '=' {$setType(LESSTHANOREQUALTO);}            //LESSTHANOREQUALTO     : "<=" ;
        | {$setType(LESSTHAN);}                     //LESSTHAN              : "<" ;
        | '<' ({$setType(SHIFTLEFT);}                   //SHIFTLEFT             : "<<" ;
               | '=' {$setType(SHIFTLEFTEQUAL);}))      //SHIFTLEFTEQUAL        : "<<=" ;
    );

DOLLAR options { constText=true; }  :  '$' ;

AT  options { constText=true; }     :  '@' ;

FIRST_GREATER options { constText=true; } : 
    '>' ( {$setType(GREATERTHAN);}                  //GREATERTHAN           : ">" ;
    | '=' {$setType(GREATERTHANOREQUALTO);}         //GREATERTHANOREQUALTO  : ">=" ;
    | '>' ( {$setType(SHIFTRIGHT);}                 //SHIFTRIGHT            : ">>" ;
            | '=' {$setType(SHIFTRIGHTEQUAL);}));   //SHIFTRIGHTEQUAL       : ">>=" ;

FIRST_MINUS options { constText=true; } :
    '-' ( {$setType(MINUS);}                        //MINUS           : '-' ;
    | '=' {$setType(MINUSEQUAL);}                   //MINUSEQUAL      : "-=" ;
    | '-' {$setType(MINUSMINUS);}                   //MINUSMINUS      : "--" ;
    | '>' ( {$setType(POINTERTO);}                  //POINTERTO       : "->" ;
            | '*' {$setType(POINTERTOMBR);}));      //POINTERTOMBR    : "->*" ;

FIRST_PLUS options { constText=true; } : 
    '+' ( {$setType(PLUS);}             //PLUS            : '+' ;
    | '=' {$setType(PLUSEQUAL);}        //PLUSEQUAL       : "+=" ;
    | '+' {$setType(PLUSPLUS);});       //PLUSPLUS        : "++" ;


// Whitespace
Whitespace options {checkSkip=true;} :	
                { 
                        $setType(Token.SKIP);
                }
                (	(' ' |'\t' | '\f') 
			// handle newlines
		|	(	"\r\n"  {offset--;} // MS
			|	'\r'    // Mac
			|	'\n'    // Unix 
			)	
                        { 
                            if (isPreprocPending()) {
                                $setType(END_PREPROC_DIRECTIVE);
                                clearPrepProcFlags();
                            }
                            newline(); 
                        }
			// handle continuation lines
		|	'\\' 
                        ( {$setType(BACK_SLASH);} |
                            (	"\r\n"  // MS
                            |	"\r"    // Mac
                            |	"\n"    // Unix 
                            )	{$setType(Token.SKIP); deferredNewline();}
                        )
		)	
	;

protected
EndOfLine
	:	(	options{generateAmbigWarnings = false;}:
			"\r\n"  {offset--;}// MS
		|	'\r'    // Mac
		|	'\n'    // Unix
		) 
	;

FIRST_QUOTATION :
        '"' (
            {isAfterInclude()}? 
            Q_char_sequence '"' 
            {setAfterInclude(false);$setType(INCLUDE_STRING);}
            |STRING_LITERAL_BODY {$setType(STRING_LITERAL);}
            )
;

// preprocessor expressions

protected H_char_sequence : (~('>'|'\r'|'\n'))* ;

protected Q_char_sequence : (~('\"'|'\r'|'\n'))* ;

PREPROC_DIRECTIVE :
         '#'
                (   
                    {isPreprocPending()}? {$setType(SHARP);}
                 |
                    {isPreprocPending()}? '#' {$setType(DBL_SHARP);}
                 | 
                    {!isPreprocPossible()}? {$setType(SHARP);}
                 |
                    {isPreprocPossible()}? 
                    {
                        setPreprocPossible(false);
                        setPreprocPending(true);
                    }
                    (options{greedy = true;}:Space)*
                    (  // lexer has no token labels
                      ("include") => "include"{ $setType(INCLUDE); setAfterInclude(true); } 
                                ( {LA(1)=='_' && LA(2)=='n'&&LA(3)=='e'&&LA(4)=='x'&&LA(5)=='t'}? 
                                "_next" { $setType(INCLUDE_NEXT); })?
                    | ("define") => "define" { $setType(DEFINE); setAfterDefine(true); }
                    | ("ifdef") => "ifdef" { $setType(IFDEF); }
                    | ("ifndef") => "ifndef" { $setType(IFNDEF); }
                    | ("if") =>  "if"   { $setType(IF); }
                    | ("undef") => "undef"  { $setType(UNDEF);  }
                    | ("elif") => "elif"  { $setType(ELIF);  }
                    | ("else") =>  "else" { $setType(ELSE); }
                    | ("endif") => "endif" { $setType(ENDIF); }
                    | ("pragma") => "pragma" { $setType(PRAGMA); } DirectiveBody
                    | ("error") => "error" { $setType(ERROR); } DirectiveBody
                    | ("line") => "line" { $setType(LINE); } DirectiveBody
                    | DirectiveBody)
                    // Do not need this here, can be skipped
                    (options{greedy = true;}:Space)*
                )
	;

/*protected
AfterPragma:DirectiveBody;

protected
AfterError:DirectiveBody;

protected
AfterLine:DirectiveBody;*/

// eat everything till the end of line
protected
DirectiveBody
        :
		( 
                        options{warnWhenFollowAmbig = false; }:
                        '\\'
                        (	"\r\n"   // MS 
			|	"\r"     // MAC
			|	"\n"     // Unix
			)	{deferredNewline();}
		|	~('\r' | '\n' )
		)*
        ;

protected  Space : (options {combineChars=true;}:' ' | '\t' | '\f');

/* Literals: */

/*
 * Note that we do NOT handle tri-graphs nor multi-byte sequences.
 */

CHAR_LITERAL
        :   
            '\'' (Escape | ~( '\'' | '\\' ))* '\''
        ;

protected STRING_LITERAL
        :
            '"' STRING_LITERAL_BODY
        ;


protected STRING_LITERAL_BODY :
		(       
                        '\\'                        
                        (   options{greedy=true;}:
                            (	"\r\n" // MS 
                            |	"\r"     // MAC
                            |	"\n"     // Unix
                            ) {deferredNewline();}
                        | 
                            '"'
                        |   
                            '\\'    
                        )?
		|	
                         ~('"' | '\r' | '\n' | '\\')
		)*
            '"'
        ;


/*
 * Handle the various escape sequences.
 *
 * Note carefully that these numeric escape *sequences* are *not* of the
 * same form as the C language numeric *constants*.
 *
 * There is no such thing as a binary numeric escape sequence.
 *
 * Octal escape sequences are either 1, 2, or 3 octal digits exactly.
 *
 * There is no such thing as a decimal escape sequence.
 *
 * Hexadecimal escape sequences are begun with a leading \x and continue
 * until a non-hexadecimal character is found.
 *
 * No real handling of tri-graph sequences, yet.
 */

protected
Escape:
	'\\'
		('a' | 'b' | 'f' | 'n' | 'r' | 't' | 'v' | '"' | '\'' | '\\' | '?' | 
                    ('0'..'3') (options{greedy=true;}: Digit)? (options{greedy=true;}: Digit)?
		| ('4'..'7') (options{greedy=true;}: Digit)?
		| 'x' (options{greedy=true;}: Digit | 'a'..'f' | 'A'..'F')+
		)
	;

/* Numeric Constants: */

protected Digit:	'0'..'9' ;

//protected Decimal:	('0'..'9')+ ;

protected LongSuffix:	   (options {combineChars=true;} :'l' | 'L') ;

protected UnsignedSuffix:  (options {combineChars=true;} :'u' | 'U') ;

protected FloatSuffix:     (options {combineChars=true;} :'f' | 'F') ;

protected Exponent:	('e' | 'E') ('+' | '-')? (Digit)* ;

//protected Vocabulary:	'\3'..'\377' ;

NUMBER
        :
		( (Digit)+ ('.' | 'e' | 'E') )=> (Digit)+
		( '.' (Digit)* (Exponent)? {$setType(FLOATONE);} //Zuo 3/12/01
		| Exponent                 {$setType(FLOATTWO);} //Zuo 3/12/01
		)                          //{type = DoubleDoubleConst;}
		(FloatSuffix               //{type = FloatDoubleConst;}
		|LongSuffix                //{type = LongDoubleConst;}
		)?

	|	'.'  (                     {$setType(DOT);}	//TODO: solve "dot & ellipsis"! 
		| 	(Digit)+ (Exponent)?   {$setType(FLOATONE);} //Zuo 3/12/01
                                   //{type = DoubleDoubleConst;}
			(FloatSuffix           //{type = FloatDoubleConst;}
			|LongSuffix            //{type = LongDoubleConst;}
			)?
		| '*' {$setType(DOTMBR);}
                | {(LA(2)=='.')}? ".." {$setType(ELLIPSIS);}
                )

	|	'0' ('0'..'7')*            //{type = IntOctalConst;}
		(LongSuffix                //{type = LongOctalConst;}
		|UnsignedSuffix            //{type = UnsignedOctalConst;}
		)*                         {$setType(OCTALINT);}

	|	'1'..'9' (Digit)*          //{type = IntIntConst;}
		(LongSuffix                //{type = LongIntConst;}
		|UnsignedSuffix            //{type = UnsignedIntConst;}
		)*                         {$setType(DECIMALINT);}  

	|	'0' ('x' | 'X') ('a'..'f' | 'A'..'F' | Digit)*
                                   //{type = IntHexConst;}
		(LongSuffix                //{type = LongHexConst;}
		|UnsignedSuffix            //{type = UnsignedHexConst;}
		)*                         {$setType(HEXADECIMALINT);}   
	;

// Everything that can be treated lke ID
ID_LIKE:
        {isPreprocPending()}?
        ("defined") => "defined"
        {setAfterPPDefined(true); $setType(DEFINED);}
     |
        {!isAfterPPDefined()}?
        Identifier
        {
            if (isAfterDefine()) {
                setAfterDefine(false);
                if (LA(1) == '(') {
                    setFunLikeMacro(true);
                }
            }
            $setType(ID);
        }
     |
        // We have checked opposite above
        //{isAfterPPDefined()}? 
        Identifier 
        {setAfterPPDefined(false);$setType(ID_DEFINED);}
     |  'L' (CHAR_LITERAL {$setType(CHAR_LITERAL);}
            |STRING_LITERAL {$setType(STRING_LITERAL);})
;

// FAKE , just to get the correct type number for this token
protected ID_DEFINED : ;

protected
Identifier      
        :
            // I think this check should have been done before
            //{ LA(1)!='L' || (LA(2)!='\'' && LA(2) != '\"') }? // L"" and L'' are StringLiterals/CharLiterals, not ID
            (
		(options {combineChars=true;} : 'a'..'z'|'A'..'Z'|'_')
		(options {combineChars=true;} : 'a'..'z'|'A'..'Z'|'_'|'0'..'9')*
            )
        ;
