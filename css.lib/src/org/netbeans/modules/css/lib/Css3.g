// A complete lexer and grammar for CSS 2.1 as defined by the
// W3 specification.
//
// This grammar is free to use providing you retain everyhting in this header comment
// section.
//
// Author      : Jim Idle, Temporal Wave LLC.
// Contact     : jimi@temporal-wave.com
// Website     : http://www.temporal-wave.com
// License     : ANTLR Free BSD License
//
// Please visit our Web site at http://www.temporal-wave.com and try our commercial
// parsers for SQL, C#, VB.Net and more.
//
// This grammar is free to use providing you retain everything in this header comment
// section.
//

//Modifications to the original css21 source file by Jim Idle have been done to fulfill the 
//css3 parsing rules and making the parser more error prone.
//1) incorporated the grammar changes from selectors module: http://www.w3.org/TR/css3-selectors/#grammar
//      a. There's no 'universal' selector node, 'typeSelector' is used instead where instead of the identifier there's the star token.
//         This solves the too long (==3) lookahead problem in the simpleSelectorSequence rule
//2) implemented custom error recovery
//3) removed whitespaces from the alpha token fragments
//
//Author: Marek Fukala (mfukala@netbeans.org)
//Please be aware that the grammar doesn't properly and fully reflect the whole css3 specification!!!

grammar Css3;

//options {
//	output=AST;
//}

@header {
    package org.netbeans.modules.css.lib;
    
}

@members {
/**
     * Use the current stacked followset to work out the valid tokens that
     * can follow on from the current point in the parse, then recover by
     * eating tokens that are not a member of the follow set we compute.
     *
     * This method is used whenever we wish to force a sync, even though
     * the parser has not yet checked LA(1) for alt selection. This is useful
     * in situations where only a subset of tokens can begin a new construct
     * (such as the start of a new statement in a block) and we want to
     * proactively detect garbage so that the current rule does not exit on
     * on an exception.
     *
     * We could override recover() to make this the default behavior but that
     * is too much like using a sledge hammer to crack a nut. We want finer
     * grained control of the recovery and error mechanisms.
     */
    protected void syncToSet()
    {
        // Compute the followset that is in context wherever we are in the
        // rule chain/stack
        //
         BitSet follow = state.following[state._fsp]; //computeContextSensitiveRuleFOLLOW();

         syncToSet(follow);
    }

    protected void syncToSet(BitSet follow)
    {
        int mark = -1;

        //create error-recovery node
        dbg.enterRule(getGrammarFileName(), "recovery");

        try {

            mark = input.mark();

            // Consume all tokens in the stream until we find a member of the follow
            // set, which means the next production should be guaranteed to be happy.
            //
            while (! follow.member(input.LA(1)) ) {

                if  (input.LA(1) == Token.EOF) {

                    // Looks like we didn't find anything at all that can help us here
                    // so we need to rewind to where we were and let normal error handling
                    // bail out.
                    //
                    input.rewind();
                    mark = -1;
                    return;
                }
                input.consume();

                // Now here, because you are consuming some tokens, yu will probably want
                // to raise an error message such as "Spurious elements after the class member were discarded"
                // using whatever your override of displayRecognitionError() routine does to record
                // error messages. The exact error my depend on context etc.
                //
            }
        } catch (Exception e) {

          // Just ignore any errors here, we will just let the recognizer
          // try to resync as normal - something must be very screwed.
          //
        }
        finally {
            dbg.exitRule(getGrammarFileName(), "recovery");

            // Always release the mark we took
            //
            if  (mark != -1) {
                input.release(mark);
            }
        }
    }
    
    
}

@lexer::header {
    package org.netbeans.modules.css.lib;
}

// -------------
// Main rule.   This is the main entry rule for the parser, the top level
//              grammar rule.
//
// A style sheet consists of an optional character set specification, an optional series
// of imports, and then the main body of style rules.
//
styleSheet  
    :   charSet?
    	WS*
        (imports WS*)*  
        namespace*
        bodylist
     EOF
    ;

namespace
  : NAMESPACE_SYM WS* (namespace_prefix WS*)? (resourceIdentifier) WS* ';' WS*
  ;

namespace_prefix
  : IDENT
  ;
    
resourceIdentifier
  : STRING|URI
  ;

// -----------------
// Character set.   Picks up the user specified character set, should it be present.
//
charSet
    :   CHARSET_SYM WS* STRING WS* SEMI
    ;

// ---------
// Import.  Location of an external style sheet to include in the ruleset.
//
imports
    :   IMPORT_SYM WS* (resourceIdentifier) WS* mediaList? SEMI
    ;

// ---------
// Media.   Introduce a set of rules that are to be used if the consumer indicates
//          it belongs to the signified medium.
//
media
    : MEDIA_SYM WS* mediaList
        LBRACE WS*
            ruleSet
        WS* RBRACE
    ;

mediaList
        : medium (COMMA WS* medium)*
	;

// ---------    
// Medium.  The name of a medim that are particulare set of rules applies to.
//
medium
    : ( IDENT | GEN ) WS*
    ;
    

bodylist
    : bodyset*
    ;
    
bodyset
    : (
    	ruleSet
        | media
        | page
      )
      WS*
    ;
    
page
    : PAGE_SYM WS? (pseudoPage WS*)?
        LBRACE WS*
            declaration SEMI (declaration SEMI)*
        RBRACE
    ;
    
pseudoPage
    : COLON IDENT
    ;
    
operator
    : SOLIDUS WS*
    | COMMA WS*
    |
    ;
    
combinator
    : PLUS WS*
    | GREATER WS*
    | TILDE WS*//use this rule preferably
    | 
    ;
    
unaryOperator
    : MINUS
    | PLUS
    ;  
    
property
    : (IDENT | GEN) WS*
    ;
    
ruleSet 
    :   selectorsGroup
        LBRACE WS* syncTo_IDENT_RBRACE
            declarations
        RBRACE
    ;
    
declarations
    :
        //Allow empty rule. Allows multiple semicolons
        declaration? (SEMI WS* declaration?)*
    ;
    
selectorsGroup
    :	selector (COMMA WS* selector)*
    ;
    
selector
    : simpleSelectorSequence (combinator simpleSelectorSequence)*
    ;
 

simpleSelectorSequence
	/* typeSelector and universal are ambiguous for lookahead==1 since namespace name and element name both starts with IDENT */
	:   
//	(  ( typeSelector | universal ) ((esPred)=>elementSubsequent)* )
	
        //using typeSelector even for the universal selector since the lookahead would have to be 3 (IDENT PIPE (IDENT|STAR) :-(
	(  typeSelector ((esPred)=>elementSubsequent)* )
	| 
	( ((esPred)=>elementSubsequent)+ )
	;
	catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(LBRACE)); 
    }
    
/*simpleSelector
    : elementName 
        ((esPred)=>elementSubsequent)*
        
    | ((esPred)=>elementSubsequent)+
    ;
 */
   
typeSelector 
	options { k = 2; }
 	:  ((nsPred)=>namespace_wqname_prefix)? ( elementName WS* )
 	;
 	 	 
 nsPred
 	:	
 	(IDENT | STAR) PIPE
 	;
    
 /*
qname_prefix
  : ( namespace_prefix WS*)?  PIPE
  ;
*/
      
 namespace_wqname_prefix
  : ( namespace_prefix WS*)?  PIPE
   | namespace_wildcard_prefix WS* PIPE
  ;  
  
namespace_wildcard_prefix
  	:	
  	STAR
  	;
        
esPred
    : HASH | DOT | LBRACKET | COLON | DCOLON
    ;
    
elementSubsequent
    : 
    (
    	cssId
    	| cssClass
        | attrib
        | pseudo
    )
    WS*
    ;
    
cssId
    : HASH
    ;

cssClass
    : DOT ( IDENT | GEN  )
    ;
    
//using typeSelector even for the universal selector since the lookahead would have to be 3 (IDENT PIPE (IDENT|STAR) :-(
elementName
    : ( IDENT | GEN ) | '*'
    ;
    
attrib
    : LBRACKET
    	namespace_wqname_prefix? WS*
        attrib_name WS*
        
            (
                (
                      OPEQ
                    | INCLUDES
                    | DASHMATCH
                )
                WS*
                attrib_value
                WS*
            )?
    
      RBRACKET
;
catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
    }

syncTo_IDENT_RBRACKET_LBRACE
    @init {
        syncToSet(BitSet.of(IDENT, RBRACKET, LBRACE));
    }
    	:	
    	;

//bit similar naming to attrvalue, attrname - but these are different - for functions
attrib_name
	: IDENT
	;
	
attrib_value
	: 
	(
  	      IDENT
              | STRING
        )
        ;

pseudo
    : ( COLON | DCOLON )
            ( IDENT | GEN )
                ( // Function
                    WS* LPAREN WS* (( IDENT | GEN ) WS*)? RPAREN
                )?
    ;

declaration
    : 
    //syncToIdent //recovery: this will sync the parser the identifier (property) if there's a gargabe in front of it
    property COLON WS* expr prio?
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        //recovery: if an mismatched token occures inside a declaration is found,
        //then skip all tokens until an end of the rule is found represented by right curly brace
        consumeUntil(input, BitSet.of(SEMI, RBRACE)); 
    }

//recovery: syncs the parser to the first identifier in the token input stream or the closing curly bracket
//since the rule matches epsilon it will always be entered
syncTo_IDENT_RBRACE
    @init {
        syncToSet(BitSet.of(IDENT, RBRACE));
    }
    	:	
    	;

//synct to computed follow set in the rule
syncToFollow
    @init {
        syncToSet();
    }
    	:	
    	;
    
    
prio
    : IMPORTANT_SYM
    ;
    
expr
    : term (operator term)*
    ;
    
term
    : unaryOperator?
        (
        (
              NUMBER
            | PERCENTAGE
            | LENGTH
            | EMS
            | EXS
            | ANGLE
            | TIME
            | FREQ
        )
    | STRING
    | IDENT
    | GEN
    | URI
    | hexColor
    | function
    )
    WS*
    ;

function
	: 	function_name WS*
		LPAREN WS*
		( 
			expr
		| 
		  	(
				attribute (COMMA WS* attribute )*				
			) 
		)
		RPAREN
	;
catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 
}
    
function_name
        //css spec allows here just IDENT, 
        //but due to some nonstandart MS extension like progid:DXImageTransform.Microsoft.gradien
        //the function name can be a bit more complicated
	: (IDENT COLON)? IDENT (DOT IDENT)*
    	;
    	
attribute
	: attrname WS* OPEQ WS* attrvalue
	;
    
attrname
	: IDENT
	;
	
attrvalue
	: expr
	;
    
hexColor
    : HASH
    ;
    
// ==============================================================
// LEXER
//
// The lexer follows the normative section of WWW standard as closely
// as it can. For instance, where the ANTLR lexer returns a token that
// is unambiguous for both ANTLR and lex (the standard defines tokens
// in lex notation), then the token names are equivalent.
//
// Note however that lex has a match order defined as top to bottom 
// with longest match first. This results in a fairly inefficent, match,
// REJECT, match REJECT set of operations. ANTLR lexer grammars are actaully
// LL grammars (and hence LL recognizers), which means that we must
// specifically disambiguate longest matches and so on, when the lex
// like normative grammar results in ambiguities as far as ANTLR is concerned.
//
// This means that some tokens will either be combined compared to the
// normative spec, and the paresr will recognize them for what they are.
// In this case, the token will named as XXX_YYY where XXX and YYY are the
// token names used in the specification.
//
// Lex style macro names used in the spec may sometimes be used (in upper case
// version) as fragment rules in this grammar. However ANTLR fragment rules
// are not quite the same as lex macros, in that they generate actual 
// methods in the recognizer class, and so may not be as effecient. In
// some cases then, the macro contents are embedded. Annotation indicate when
// this is the case.
//
// See comments in the rules for specific details.
// --------------------------------------------------------------
//
// N.B. CSS 2.1 is defined as case insensitive, but because each character
//      is allowed to be written as in escaped form we basically define each
//      character as a fragment and reuse it in all other rules.
// ==============================================================


// --------------------------------------------------------------
// Define all the fragments of the lexer. These rules neither recognize
// nor create tokens, but must be called from non-fragment rules, which
// do create tokens, using these fragments to either purely define the
// token number, or by calling them to match a certain portion of
// the token string.
//

GEN                     : '@@@';

fragment    HEXCHAR     : ('a'..'f'|'A'..'F'|'0'..'9')  ;

fragment    NONASCII    : '\u0080'..'\uFFFF'            ;   // NB: Upper bound should be \u4177777

fragment    UNICODE     : '\\' HEXCHAR 
                                (HEXCHAR 
                                    (HEXCHAR 
                                        (HEXCHAR 
                                            (HEXCHAR HEXCHAR?)?
                                        )?
                                    )?
                                )? 
                                ('\r'|'\n'|'\t'|'\f'|' ')*  ;
                                
fragment    ESCAPE      : UNICODE | '\\' ~('\r'|'\n'|'\f'|HEXCHAR)  ;

fragment    NMSTART     : '_'
                        | 'a'..'z'
                        | 'A'..'Z'
                        | NONASCII
                        | ESCAPE
                        ;

fragment    NMCHAR      : '_'
                        | 'a'..'z'
                        | 'A'..'Z'
                        | '0'..'9'
                        | '-'
                        | NONASCII
                        | ESCAPE
                        ;
                        
fragment    NAME        : NMCHAR+   ;

fragment    URL         : ( 
                              '['|'!'|'#'|'$'|'%'|'&'|'*'|'~'|'.'|':'|'/'
                            | NMCHAR
                          )*
                        ;

                        
// Basic Alpha characters in upper, lower and escaped form. 

fragment    A   :   ('a'|'A')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'1'
                ;
fragment    B   :   ('b'|'B')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'2'
                ;
fragment    C   :   ('c'|'C')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'3'
                ;
fragment    D   :   ('d'|'D')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'4'
                ;
fragment    E   :   ('e'|'E')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'5'
                ;
fragment    F   :   ('f'|'F')     
                |   '\\' ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'6'
                ;
fragment    G   :   ('g'|'G')  
                |   '\\'
                        (
                              'g'
                            | 'G'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'7'
                        )
                ;
fragment    H   :   ('h'|'H')  
                | '\\' 
                        (
                              'h'
                            | 'H'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'8'
                        )   
                ;
fragment    I   :   ('i'|'I')  
                | '\\' 
                        (
                              'i'
                            | 'I'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')'9'
                        )
                ;
fragment    J   :   ('j'|'J')  
                | '\\' 
                        (
                              'j'
                            | 'J'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('A'|'a')
                        )   
                ;
fragment    K   :   ('k'|'K')  
                | '\\' 
                        (
                              'k'
                            | 'K'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('B'|'b')
                        )   
                ;
fragment    L   :   ('l'|'L')  
                | '\\' 
                        (
                              'l'
                            | 'L'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('C'|'c')
                        )   
                ;
fragment    M   :   ('m'|'M')  
                | '\\' 
                        (
                              'm'
                            | 'M'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('D'|'d')
                        )   
                ;
fragment    N   :   ('n'|'N')  
                | '\\' 
                        (
                              'n'
                            | 'N'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('E'|'e')
                        )   
                ;
fragment    O   :   ('o'|'O')  
                | '\\' 
                        (
                              'o'
                            | 'O'
                            | ('0' ('0' ('0' '0'?)?)?)? ('4'|'6')('F'|'f')
                        )   
                ;
fragment    P   :   ('p'|'P')  
                | '\\'
                        (
                              'p'
                            | 'P'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('0')
                        )   
                ;
fragment    Q   :   ('q'|'Q')  
                | '\\' 
                        (
                              'q'
                            | 'Q'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('1')
                        )   
                ;
fragment    R   :   ('r'|'R')  
                | '\\' 
                        (
                              'r'
                            | 'R'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('2')
                        )   
                ;
fragment    S   :   ('s'|'S')  
                | '\\' 
                        (
                              's'
                            | 'S'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('3')
                        )   
                ;
fragment    T   :   ('t'|'T')  
                | '\\' 
                        (
                              't'
                            | 'T'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('4')
                        )   
                ;
fragment    U   :   ('u'|'U')  
                | '\\' 
                        (
                              'u'
                            | 'U'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('5')
                        )
                ;
fragment    V   :   ('v'|'V')  
                | '\\' 
                        (     'v'
                            | 'V'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('6')
                        )
                ;
fragment    W   :   ('w'|'W')  
                | '\\' 
                        (
                              'w'
                            | 'W'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('7')
                        )   
                ;
fragment    X   :   ('x'|'X')  
                | '\\' 
                        (
                              'x'
                            | 'X'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('8')
                        )
                ;
fragment    Y   :   ('y'|'Y')  
                | '\\' 
                        (
                              'y'
                            | 'Y'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('9')
                        )
                ;
fragment    Z   :   ('z'|'Z')  
                | '\\' 
                        (
                              'z'
                            | 'Z'
                            | ('0' ('0' ('0' '0'?)?)?)? ('5'|'7')('A'|'a')
                        )
                ;


// -------------
// Comments.    Comments may not be nested, may be multilined and are delimited
//              like C comments: /* ..... */
//              COMMENTS are hidden from the parser which simplifies the parser 
//              grammar a lot.
//
COMMENT         : '/*' ( options { greedy=false; } : .*) '*/'
    
                    {
                        $channel = 2;   // Comments on channel 2 in case we want to find them
                    }
                ;

// ---------------------
// HTML comment open.   HTML/XML comments may be placed around style sheets so that they
//                      are hidden from higher scope parsing engines such as HTML parsers.
//                      They comment open is therfore ignored by the CSS parser and we hide
//                      it from the ANLTR parser.
//
CDO             : '<!--'

                    {
                        $channel = 3;   // CDO on channel 3 in case we want it later
                    }
                ;
    
// ---------------------            
// HTML comment close.  HTML/XML comments may be placed around style sheets so that they
//                      are hidden from higher scope parsing engines such as HTML parsers.
//                      They comment close is therfore ignored by the CSS parser and we hide
//                      it from the ANLTR parser.
//
CDC             : '-->'

                    {
                        $channel = 4;   // CDC on channel 4 in case we want it later
                    }
                ;
                
INCLUDES        : '~='      ;
DASHMATCH       : '|='      ;

GREATER         : '>'       ;
LBRACE          : '{'       ;
RBRACE          : '}'       ;
LBRACKET        : '['       ;
RBRACKET        : ']'       ;
OPEQ            : '='       ;
SEMI            : ';'       ;
COLON           : ':'       ;
DCOLON          : '::'       ;
SOLIDUS         : '/'       ;
MINUS           : '-'       ;
PLUS            : '+'       ;
STAR            : '*'       ;
LPAREN          : '('       ;
RPAREN          : ')'       ;
COMMA           : ','       ;
DOT             : '.'       ;
TILDE		: '~'       ;
PIPE            : '|'       ;

// -----------------
// Literal strings. Delimited by either ' or "
//
fragment    INVALID :;
STRING          : '\'' ( ~('\n'|'\r'|'\f'|'\'') )* 
                    (
                          '\''
                        | { $type = INVALID; }
                    )
                    
                | '"' ( ~('\n'|'\r'|'\f'|'"') )*
                    (
                          '"'
                        | { $type = INVALID; }
                    )
                ;

// -------------
// Identifier.  Identifier tokens pick up properties names and values
//
IDENT           : '-'? NMSTART NMCHAR*  ;

// -------------
// Reference.   Reference to an element in the body we are styling, such as <XXXX id="reference">
//
HASH            : '#' NAME              ;

IMPORT_SYM      : '@' I M P O R T       ;
PAGE_SYM        : '@' P A G E           ;
MEDIA_SYM       : '@' M E D I A         ;
CHARSET_SYM     : '@charset '           ;
NAMESPACE_SYM       : '@' N A M E S P A C E ;

IMPORTANT_SYM   : '!' (WS|COMMENT)* I M P O R T A N T   ;

// ---------
// Numbers. Numbers can be followed by pre-known units or unknown units
//          as well as '%' it is a precentage. Whitespace cannot be between
//          the numebr and teh unit or percent. Hence we scan any numeric, then
//          if we detect one of the lexical sequences for unit tokens, we change
//          the lexical type dynamically.
//
//          Here we first define the various tokens, then we implement the
//          number parsing rule.
//
fragment    EMS         :;  // 'em'
fragment    EXS         :;  // 'ex'
fragment    LENGTH      :;  // 'px'. 'cm', 'mm', 'in'. 'pt', 'pc'
fragment    ANGLE       :;  // 'deg', 'rad', 'grad'
fragment    TIME        :;  // 'ms', 's'
fragment    FREQ        :;  // 'khz', 'hz'
fragment    DIMENSION   :;  // nnn'Somethingnotyetinvented'
fragment    PERCENTAGE  :;  // '%'

NUMBER
    :   (
              '0'..'9'+ ('.' '0'..'9'+)?
            | '.' '0'..'9'+
        )
        (
              (E (M|X))=>
                E
                (
                      M     { $type = EMS;          }
                    | X     { $type = EXS;          }
                )
            | (P(X|T|C))=>
                P
                (
                      X     
                    | T
                    | C
                )
                            { $type = LENGTH;       }   
            | (C M)=>
                C M         { $type = LENGTH;       }
            | (M (M|S))=> 
                M
                (
                      M     { $type = LENGTH;       }
            
                    | S     { $type = TIME;         }
                )
            | (I N)=>
                I N         { $type = LENGTH;       }
            
            | (D E G)=>
                D E G       { $type = ANGLE;        }
            | (R A D)=>
                R A D       { $type = ANGLE;        }
            
            | (S)=>S        { $type = TIME;         }
                
            | (K? H Z)=>
                K? H    Z   { $type = FREQ;         }
            
            | IDENT         { $type = DIMENSION;    }
            
            | '%'           { $type = PERCENTAGE;   }
            
            | // Just a number
        )
    ;

// ------------
// url and uri.
//
URI :   U R L
        '('
            ((WS)=>WS)? (URL|STRING) WS?
        ')'
    ;

// -------------
// Whitespace.  Though the W3 standard shows a Yacc/Lex style parser and lexer
//              that process the whitespace within the parser, ANTLR does not
//              need to deal with the whitespace directly in the parser.
//
//WS      : (' '|'\t')+           { $channel = HIDDEN;    }   ;
WS      : (' '|'\t')+;
NL      : ('\r' '\n'? | '\n')   { $channel = HIDDEN;    }   ;


// -------------
//  Illegal.    Any other character shoudl not be allowed.
//
