//  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
// 
//  Copyright 2011 Oracle and/or its affiliates. All rights reserved.
// 
//  Oracle and Java are registered trademarks of Oracle and/or its affiliates.
//  Other names may be trademarks of their respective owners.
// 
//  The contents of this file are subject to the terms of either the GNU
//  General Public License Version 2 only ("GPL") or the Common
//  Development and Distribution License("CDDL") (collectively, the
//  "License"). You may not use this file except in compliance with the
//  License. You can obtain a copy of the License at
//  http://www.netbeans.org/cddl-gplv2.html
//  or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
//  specific language governing permissions and limitations under the
//  License.  When distributing the software, include this License Header
//  Notice in each file and include the License file at
//  nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
//  particular file as subject to the "Classpath" exception as provided
//  by Oracle in the GPL Version 2 section of the License file that
//  accompanied this code. If applicable, add the following below the
//  License Header, with the fields enclosed by brackets [] replaced by
//  your own identifying information:
//  "Portions Copyrighted [year] [name of copyright owner]"
// 
//  If you wish your version of this file to be governed by only the CDDL
//  or only the GPL Version 2, indicate your decision by adding
//  "[Contributor] elects to include this software in this distribution
//  under the [CDDL or GPL Version 2] license." If you do not indicate a
//  single choice of license, a recipient has the option to distribute
//  your version of this file under either the CDDL, the GPL Version 2 or
//  to extend the choice of license to its licensees as provided above.
//  However, if you add GPL Version 2 code and therefore, elected the GPL
//  Version 2 license, then the option applies only if the new code is
//  made subject to such option by the copyright holder.
// 
//  Contributor(s):
// 
//  Portions Copyrighted 2011 Sun Microsystems, Inc.
//
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
//	k='*';
//}

@header {
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib;
    
}

@members {

    protected boolean isLessSource() {
        return false;
    }
    
    protected boolean isScssSource() {
        return false;
    }
    
    private boolean isCssPreprocessorSource() {
        return isLessSource() || isScssSource();
    }

    private boolean tokenNameEquals(String tokenImage) {
        return tokenImage.equalsIgnoreCase(input.LT(1).getText());
    }

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
    
    /**
         * synces to next RBRACE "}" taking nesting into account
         */
        protected void syncToRBRACE(int nest)
            {
                
                int mark = -1;
                //create error-recovery node
                //dbg.enterRule(getGrammarFileName(), "recovery");

                try {
                    mark = input.mark();
                    for(;;) {
                        //read char
                        int c = input.LA(1);
                        
                        switch(c) {
                            case Token.EOF:
                                input.rewind();
                                mark = -1;
                                return ;
                            case Css3Lexer.LBRACE:
                                nest++;
                                break;
                            case Css3Lexer.RBRACE:
                                nest--;
                                if(nest == 0) {
                                    //do not eat the final RBRACE
                                    return ;
                                }
                        }
                        
                        input.consume();
                                            
                    }

                } catch (Exception e) {

                  // Just ignore any errors here, we will just let the recognizer
                  // try to resync as normal - something must be very screwed.
                  //
                }
                finally {
                    if  (mark != -1) {
                        input.release(mark);
                    }
                    //dbg.exitRule(getGrammarFileName(), "recovery");
                }
            }
    
}

@lexer::header {
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
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
    :   
    	ws?
    	( charSet ws? )?
        imports?
        namespaces? 
        body?
     EOF
    ;

namespaces
	:
	( namespace ws? )+
	;

namespace
  : NAMESPACE_SYM ws? (namespacePrefixName ws?)? resourceIdentifier ws? SEMI
  ;

namespacePrefixName
  : IDENT
  ;
    
resourceIdentifier
  : STRING | URI
  ;

charSet
    :   CHARSET_SYM ws? charSetValue ws? SEMI
    ;

charSetValue
	: STRING
	;

imports
	:
	( importItem ws? SEMI ws? )+
	;
	
importItem
    :   
        IMPORT_SYM ws? resourceIdentifier ((ws? mediaQueryList)=>ws? mediaQueryList)?
        |
        //multiple imports in one directive
        {isScssSource()}? IMPORT_SYM ws? resourceIdentifier (ws? COMMA ws? resourceIdentifier)* ((ws? mediaQueryList)=>ws? mediaQueryList)?
    ;
media
    : MEDIA_SYM ws? 
    (
         mediaQueryList
    ) ws?
    LBRACE ws? syncToFollow
        mediaBody?
    RBRACE
    ;
    
mediaBody
    :
    (
         ( mediaBodyItem ((ws? SEMI)=>ws? SEMI)? ws? )
         |
         ( SEMI ws? )
    )+
    ;

mediaBodyItem
    :
    (SASS_MIXIN | (DOT IDENT ws? LPAREN (~RPAREN)* RPAREN (~LBRACE)* LBRACE))=>cp_mixin_declaration 
    | (cp_mixin_call)=>cp_mixin_call 
    |(~(LBRACE|SEMI|RBRACE|COLON)+ COLON ~(SEMI|LBRACE|RBRACE)+ SEMI | sass_declaration_interpolation_expression COLON )=>propertyDeclaration
    | {isScssSource()}? sass_extend
    | {isScssSource()}? sass_debug
    | {isScssSource()}? sass_control
    | {isScssSource()}? sass_content
    | rule
    | page
    | fontFace
    | vendorAtRule
    //Just a partial hotfix for nested MQ: complete grammar is defined in: http://www.w3.org/TR/css3-conditional/#processing
    | media
    ;

mediaQueryList
 : mediaQuery ( (ws? COMMA)=> ws? COMMA ws? mediaQuery )*
 ;
 
mediaQuery
 : (mediaQueryOperator ws? )?  mediaType ( ws? AND ws? mediaExpression )*
 | mediaExpression ( ws? AND ws? mediaExpression )*
 ;
 
mediaQueryOperator
 	: ONLY | NOT 		
 	;
 
mediaType
 : IDENT | GEN | {isCssPreprocessorSource()}? sass_interpolation_expression_var
 ;
 
mediaExpression
    : 
    LPAREN ws? mediaFeature mediaFeatureValue? ws? RPAREN
    ;
    
mediaFeatureValue
    :
    ws? COLON ws? expression
    ;
 
mediaFeature
 : IDENT | GEN | {isCssPreprocessorSource()}? ( cp_variable | sass_interpolation_expression_var )
 ;
 
 body	
    :	
    (
         ( bodyItem ((ws? SEMI)=>ws? SEMI)? ws? )
         |
         ( SEMI ws? )
    )+
    ;
 
bodyItem
    : 
        (SASS_MIXIN | (DOT IDENT ws? LPAREN (~RPAREN)* RPAREN (~LBRACE)* LBRACE))=>cp_mixin_declaration
        | (cp_mixin_call)=>cp_mixin_call
    	| rule
        | at_rule
        | {isCssPreprocessorSource()}? cp_variable_declaration
        //not exactly acc. to the spec, since just CP stuff can preceede, but is IMO satisfactory
        | {isCssPreprocessorSource()}? importItem
        | {isScssSource()}? sass_debug
        | {isScssSource()}? sass_control
        | {isScssSource()}? sass_function_declaration
    ; catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(NL)); 
    }
    
at_rule
    :
    media
    | page
    | counterStyle
    | fontFace
    | vendorAtRule
    ;
    
vendorAtRule
: moz_document | webkitKeyframes | generic_at_rule;
    
atRuleId
	:
	IDENT | STRING
	;
    
generic_at_rule
    : AT_IDENT WS* ( atRuleId WS* )? 
        LBRACE 
        	syncTo_RBRACE
        RBRACE
	;    
moz_document
	: 
	MOZ_DOCUMENT_SYM ws? ( moz_document_function ws?) ( COMMA ws? moz_document_function ws? )*
	LBRACE ws?
		body? //can be empty
	RBRACE
	;

moz_document_function
	:
	URI | MOZ_URL_PREFIX | MOZ_DOMAIN | MOZ_REGEXP
	;
    
//http://developer.apple.com/library/safari/#documentation/appleapplications/reference/SafariCSSRef/Articles/OtherStandardCSS3Features.html#//apple_ref/doc/uid/TP40007601-SW1
webkitKeyframes
	:
	WEBKIT_KEYFRAMES_SYM ws? atRuleId ws? 
	LBRACE ws?
		( webkitKeyframesBlock ws? )*
	RBRACE
	;
	
webkitKeyframesBlock
	:
	webkitKeyframeSelectors ws?
	LBRACE  ws? syncToFollow
		declarations?
	RBRACE 
	;	
	
webkitKeyframeSelectors
	:
	( IDENT | PERCENTAGE ) ( ws? COMMA ws? ( IDENT | PERCENTAGE ) )*
	;
    
page
    : PAGE_SYM ws? ( IDENT ws? )? (pseudoPage ws?)?
        LBRACE ws?
            //the grammar in the http://www.w3.org/TR/css3-page/ says the declaration/margins should be delimited by the semicolon,
            //but there's no such char in the examples => making it arbitrary
            ((propertyDeclaration|margin) ws?)? (SEMI ws? ((propertyDeclaration|margin) ws?)?)*
        RBRACE
    ;
    
counterStyle
    : COUNTER_STYLE_SYM ws? IDENT ws?
        LBRACE ws? syncToDeclarationsRule
		declarations?
        RBRACE
    ;
    
fontFace
    : FONT_FACE_SYM ws?
        LBRACE ws? syncToDeclarationsRule
		declarations?
        RBRACE
    ;

margin	
	: margin_sym ws? LBRACE ws? syncToDeclarationsRule declarations? RBRACE
       ;
       
margin_sym 
	:
       TOPLEFTCORNER_SYM | 
       TOPLEFT_SYM | 
       TOPCENTER_SYM | 
       TOPRIGHT_SYM | 
       TOPRIGHTCORNER_SYM |
       BOTTOMLEFTCORNER_SYM | 
       BOTTOMLEFT_SYM | 
       BOTTOMCENTER_SYM | 
       BOTTOMRIGHT_SYM |
       BOTTOMRIGHTCORNER_SYM |
       LEFTTOP_SYM |
       LEFTMIDDLE_SYM |
       LEFTBOTTOM_SYM |
       RIGHTTOP_SYM |
       RIGHTMIDDLE_SYM |
       RIGHTBOTTOM_SYM 
       ;
    
pseudoPage
    : COLON IDENT
    ;
    
operator
    : SOLIDUS
    | COMMA
    ;
        
unaryOperator
    : MINUS
    | PLUS
    ;  
    
property
    : 
    (
        //parse as scss_declaration_interpolation_expression only if it really contains some #{} content
        //(the IE allows also just ident as its content)
        (~(HASH_SYMBOL|COLON)* HASH_SYMBOL LBRACE)=>sass_declaration_interpolation_expression
        | IDENT 
        | GEN 
        | {isCssPreprocessorSource()}? cp_variable
    ) ws?
    ; catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(COLON)); 
    }
    
rule 
    :   
    selectorsGroup ws?
    LBRACE ws? syncToFollow 
        declarations?
    RBRACE
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(RBRACE));
        input.consume(); //consume the RBRACE as well   
    }
    
declarations
    :
    ( 
         ( declaration ((ws? SEMI)=>ws? SEMI)? ws? )
         |
         ( SEMI ws? )
    )+
    ;

declaration
    :
    (cp_variable_declaration)=>cp_variable_declaration 
    | (sass_nested_properties)=>sass_nested_properties 
    | (propertyDeclaration)=>propertyDeclaration 
    //for the error recovery - if the previous synt. predicate fails (an error in the declaration we'll still able to recover INSIDE the declaration
    | (property COLON ~(LBRACE|SEMI|RBRACE)* (RBRACE|SEMI) )=>propertyDeclaration 
    | (SASS_MIXIN | (DOT IDENT ws? LPAREN (~RPAREN)* RPAREN (~LBRACE)* LBRACE))=>cp_mixin_declaration 
    | (cp_mixin_call)=>cp_mixin_call 
    | (selectorsGroup ws? LBRACE)=>rule 
    | {isCssPreprocessorSource()}? at_rule 
    | {isScssSource()}? sass_control 
    | {isScssSource()}? sass_extend 
    | {isScssSource()}? sass_debug 
    | {isScssSource()}? sass_content 
    | {isScssSource()}? sass_function_return 
    | {isScssSource()}? importItem 
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(SEMI));
    }
    
selectorsGroup
    :	
        // looking for #{, lookeahead exited by { (rule beginning)
        ( ~( HASH_SYMBOL | LBRACE )* HASH_SYMBOL LBRACE)=> sass_selector_interpolation_expression
	|
        selector (ws? COMMA ws? selector)*
    ;
        
selector
    :  (combinator ws?)? simpleSelectorSequence ( ((ws? combinator ws?)|ws?) simpleSelectorSequence)*
    ;
 
combinator
    : 
    PLUS | GREATER | TILDE
    ;

simpleSelectorSequence
	:   
	( typeSelector ((ws? esPred)=>ws? elementSubsequent)* )
	| 
	elementSubsequent ((ws? esPred)=>ws? elementSubsequent)*
	;
	catch[ RecognitionException rce] {
            reportError(rce);
            consumeUntil(input, BitSet.of(LBRACE)); 
        }
        
//predicate
esPred
    : HASH_SYMBOL | HASH | DOT | LBRACKET | COLON | DCOLON | SASS_EXTEND_ONLY_SELECTOR
    ;        
       
typeSelector 
	options { k = 2; }
 	:  (((IDENT | STAR)? PIPE)=>namespacePrefix)? elementName
 	;

namespacePrefix
  : ( namespacePrefixName | STAR)? PIPE
  ;  

    
elementSubsequent
    : 
    (
        {isScssSource()}? sass_extend_only_selector
    	| cssId
    	| cssClass
        | slAttribute
        | pseudo
    )
    ;
    
//Error Recovery: Allow the parser to enter the cssId rule even if there's just hash char.
cssId
    : HASH | ( HASH_SYMBOL NAME )
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(WS, IDENT, LBRACE)); 
    }

cssClass
    : DOT ( IDENT | GEN  )
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(WS, IDENT, LBRACE)); 
    }
    
//using typeSelector even for the universal selector since the lookahead would have to be 3 (IDENT PIPE (IDENT|STAR) :-(
elementName
    : IDENT | GEN | LESS_AND | STAR
    ;

slAttribute
    : LBRACKET
    	namespacePrefix? ws?
        slAttributeName ws?
        
            (
                (
                      OPEQ
                    | INCLUDES
                    | DASHMATCH
                    | BEGINS
                    | ENDS
                    | CONTAINS
                )
                ws?
                slAttributeValue
                ws?
            )?
    
      RBRACKET
;
catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(IDENT, LBRACE)); 
    }

//bit similar naming to attrvalue, attrname - but these are different - for functions
slAttributeName
	: IDENT
	;
	
slAttributeValue
	: 
	(
  	      IDENT
              | STRING
        )
        ;

pseudo
    : ( COLON | DCOLON )
             (
                ( 
                    ( IDENT | GEN )
                    ( // Function
                        ws? LPAREN ws? ( (expression ws?) | STAR )? RPAREN
                    )?
                )
                |
                ( NOT ws? LPAREN ws? simpleSelectorSequence? RPAREN )
             )
    ;

propertyDeclaration
    : 
    STAR? property COLON ws? propertyValue (ws? prio)?
    | {isCssPreprocessorSource()}? STAR? property COLON ws? cp_propertyValue //cp_expression may contain the IMPORT_SYM
    ;
    catch[ RecognitionException rce] {
        reportError(rce);
        //recovery: if an mismatched token occures inside a declaration is found,
        //then skip all tokens until an end of the rule is found represented by right curly brace
        consumeUntil(input, BitSet.of(SEMI, RBRACE)); 
    }

//XXX this is a hack for the IMPORT_SYM inside cp_expression
cp_propertyValue
    : 
    {isCssPreprocessorSource()}? cp_expression_list
    | propertyValue
    ;

propertyValue
	:
        expression
	;

//an expression wich doesn't contain cp expression operators
expressionPredicate
    options { k = 1; }
    :
    ( ~ (AT_IDENT | STAR | SOLIDUS | LBRACE | SEMI | RBRACE | SASS_VAR) )+ ( SEMI | RBRACE )
    ;
    
//recovery: syncs the parser to the first identifier in the token input stream or the closing curly bracket
//since the rule matches epsilon it will always be entered
syncToDeclarationsRule
    @init {
        //why sync to DOT? - LESS allows class rules nested
        syncToSet(BitSet.of(IDENT, RBRACE, STAR, DOT)); 
    }
    	:	
    	;
    	
syncTo_RBRACE
    @init {
        syncToRBRACE(1); //initial nest == 1
    }
    	:	
    	;    	

syncTo_SEMI
    @init {
        syncToSet(BitSet.of(SEMI)); 
    }
    	:	
            SEMI
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
    
expression
    : term ( (( ws | (ws? operator ws?) | /* nothing */) term)=> ( ws | (ws? operator ws?) | /* nothing */) term)*
    ;
    
term
    : 
    ( unaryOperator ws? )?
    (
        (functionName ws? LPAREN)=>function //"myfunction(" as predicate
        | IDENT
        | NUMBER
        | PERCENTAGE
        | LENGTH
        | EMS
        | REM
        | EXS
        | ANGLE
        | TIME
        | FREQ
        | RESOLUTION
        | DIMENSION     //so we can match expression like a:nth-child(3n+1) -- the "3n" is lexed as dimension
        | STRING
        | GEN
        | URI
        | hexColor
        | {isCssPreprocessorSource()}? cp_variable
        | {isCssPreprocessorSource()}? sass_interpolation_expression_var
    )
    ;

function
	: 	functionName ws?
		LPAREN ws?
		(
                    fnAttributes
                    | //empty
		)
		RPAREN
	;
catch[ RecognitionException rce] {
        reportError(rce);
        consumeUntil(input, BitSet.of(RPAREN, SEMI, RBRACE)); 
}
    
functionName
        //css spec allows? here just IDENT, 
        //but due to some nonstandart MS extension like progid:DXImageTransform.Microsoft.gradien
        //the function name can be a bit more complicated
	: 
        (IDENT COLON)? IDENT (DOT IDENT)*
    	;

fnAttributes
    :
    fnAttribute (ws? COMMA ws? fnAttribute)* ws?
    ;

fnAttribute
	: 
        (fnAttributeName ws? (OPEQ|COLON) )=>fnAttributeName ws? (OPEQ|COLON) ws? fnAttributeValue
        | {isCssPreprocessorSource()}? cp_expression
        | expression
	;
    
fnAttributeName
	: 
            IDENT (DOT IDENT)*
            | {isCssPreprocessorSource()}? cp_variable
	;
	
fnAttributeValue
	: 
            term ( (( ws | (ws? SOLIDUS ws?) | /* nothing */) term)=> ( ws | (ws? SOLIDUS ws?) | /* nothing */) term)* //== expression w/o COMMAs
            | {isCssPreprocessorSource()}? cp_math_expression
	;
    
hexColor
    : HASH
    ;
    
ws
    : ( WS | NL | COMMENT )+
    ;
    
//*** LESS SYNTAX ***
//Some additional modifications to the standard syntax rules has also been done.
//ENTRY POINT FROM CSS GRAMMAR
cp_variable_declaration
    : 
        {isLessSource()}? cp_variable ws? COLON ws? cp_expression_list
        | 
        {isScssSource()}? cp_variable ws? COLON ws? cp_expression_list (ws? SASS_DEFAULT)?
    ;

//ENTRY POINT FROM CSS GRAMMAR    
cp_variable
    : 
        {isLessSource()}? ( AT_IDENT | MEDIA_SYM )//TODO add all meaningful at-rules here
        |
        {isScssSource()}? ( SASS_VAR )
    ;

//comma separated list of cp_expression-s
cp_expression_list
    :
    cp_expression
    ((ws? COMMA ws? cp_expression)=>ws? COMMA ws? cp_expression)*
    ;

//expression:
//-----------
//
//allowed content:
//- boolean expression binary operators: and, or, <, >, <=, ==, ...
//- boolean expression unary operator: not
//- mathematical expression as term: cp_math_expression
//- whitespace separated list of expression-s
//- comma separted list of expressions-s in parenthesis
//
cp_expression
    :    
    cp_expression_atom 
    ( 
        (ws? cp_expression_operator)=>(ws? cp_expression_operator ws?) cp_expression_atom 
        | (ws? cp_expression_atom)=>ws? cp_expression_atom
    )* 
    ;
    
cp_expression_operator
    :
    OR | AND | CP_EQ | CP_NOT_EQ | LESS | LESS_OR_EQ | GREATER | GREATER_OR_EQ
    ;

cp_expression_atom
    :    
        (NOT ws?)? 
        (
            (cp_math_expression)=>cp_math_expression
            | LPAREN ws? cp_expression_list ws? RPAREN
        )
    ;

//WS separated list of cp_math_expression-s
cp_math_expressions
    :
    cp_math_expression
    (ws cp_math_expression)*
    ;
//mathematical expression: 
//-------------------------
//allowed content: 
//- parens: ()
//- binary oparators: +,-,*
//- unary operators: +,-
//- terms
//- SASS interpolation expression where the term is allowed
//
//NOT ALLOWED: 
//- COMMAS
//- terms separated just by whitespace - e.g. "one two"
//
cp_math_expression
    :    cp_math_expression_atom 
         (
            (ws? (PLUS|MINUS|STAR|SOLIDUS) )=> ws? (PLUS|MINUS|STAR|SOLIDUS) ws? cp_math_expression_atom
         )* 
    ;

cp_math_expression_atom
    :    
    term
    | IMPORTANT_SYM //cp property value may contain any gargabe - TODO - possibly add other garbage tokens
    | ( unaryOperator ws? )? LPAREN ws? cp_math_expression ws? RPAREN 
    ;

//parametric mixins: 
//    .border-radius (@radius) 
//    .box-shadow (@x: 0, @y: 0, @blur: 1px, @color: #000)
//
//normal mixin has common css syntax: .mixin so cannot be distinguished from a css class
//ENTRY POINT FROM CSS GRAMMAR
cp_mixin_declaration
    :
    (
        {isLessSource()}? DOT cp_mixin_name ws? LPAREN ws? cp_args_list? RPAREN (ws? less_mixin_guarded)?
        |
        {isScssSource()}? SASS_MIXIN ws cp_mixin_name (ws? LPAREN ws? cp_args_list? RPAREN)?
    )
    ws? cp_mixin_block 
    ;

//allow: .mixin; .mixin(); .mixin(@param, #77aa00); 
//ENTRY POINT FROM CSS GRAMMAR
cp_mixin_call
    :    
    (
        {isLessSource()}? DOT cp_mixin_name (ws? LPAREN ws? cp_mixin_call_args? RPAREN)?
        |
        {isScssSource()}? SASS_INCLUDE ws cp_mixin_name (ws? LPAREN ws? cp_mixin_call_args? RPAREN)? (ws? cp_mixin_block)?
    )
    ;
    
cp_mixin_block
    :
    LBRACE ws? syncToFollow
        declarations?
    RBRACE
    ;
        
cp_mixin_name
    :
    IDENT
    ;
    
cp_mixin_call_args
    : 
    //the term separatos is supposed to be just COMMA, but in some weird old? samples
    //I found semicolon used as a delimiter between arguments
    cp_mixin_call_arg ( (COMMA | SEMI) ws? cp_mixin_call_arg)*  CP_DOTS?   
    ;
    
cp_mixin_call_arg
    :
    (
        cp_variable ws? COLON ws? cp_expression
        | cp_expression
    ) ws?
    ;

//.box-shadow ("@x: 0, @y: 0, @blur: 1px, @color: #000")
cp_args_list
    : 
    //the term separatos is supposed to be just COMMA, but in some weird old? samples
    //I found semicolon used as a delimiter between arguments
    
    //sass varargs:
    //@mixin box-shadow($shadows...) {} -- note that now also LESS parser allows this incorrectly (minor issue)

    ( cp_arg ( ( COMMA | SEMI ) ws? cp_arg)* ( ( (COMMA | SEMI) ws? )? (CP_DOTS | LESS_REST))?)
    | 
    (CP_DOTS | LESS_REST)
    ;
    
//.box-shadow ("@x: 0", @y: 0, @blur: 1px, @color: #000)
cp_arg
    :
    cp_variable ws? ( COLON ws? cp_expression ws?)?
    ;

//.mixin (@a) "when (lightness(@a) >= 50%)" {
//.mixin (@a) "when (@a > 10), (@a < -10)" { ... }
less_mixin_guarded
    :
    LESS_WHEN ws? less_condition ( (COMMA | AND) ws? less_condition)*
    ;
    
//.truth (@a) when (@a) { ... }
//.truth (@a) when (@a = true) { ... }
less_condition
    :
    (NOT ws?)?
    LPAREN ws? 
        (
            less_function_in_condition ws?
            |
            ( cp_variable (ws? less_condition_operator ws? cp_math_expression)?)  
        )        
    RPAREN
    ;
    
//.mixin (@a, @b: 0) when ("isnumber(@b)") { ... }
less_function_in_condition
    :
    less_fn_name ws? LPAREN ws? cp_variable ws? RPAREN
    ;

//.mixin (@a, @b: 0) when ("isnumber"(@b)) { ... }
less_fn_name
    :
    IDENT
    ;

less_condition_operator
    :
    GREATER | GREATER_OR_EQ | OPEQ | LESS | LESS_OR_EQ
    ;

//SCSS interpolation expression, e.g. #{$vert}

//why there're two almost same selector_interpolation_expression-s?
//the problem is that the one for selector can contain COLON inside the expression
//whereas the later cann't. 
sass_selector_interpolation_expression
    :
        ( 
            (sass_interpolation_expression_var)=>sass_interpolation_expression_var
            |
            (IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND | COMMA | STAR | GREATER )
        )
        ( 
            ws?
            (
                (sass_interpolation_expression_var)=>sass_interpolation_expression_var
                |
                (IDENT | MINUS | DOT | HASH_SYMBOL | HASH | COLON | LESS_AND | COMMA | STAR | GREATER)
            )
        )*

    ;
    
sass_declaration_interpolation_expression
    :
        ( 
            (sass_interpolation_expression_var)=>sass_interpolation_expression_var
            |
            (IDENT | MINUS | DOT | HASH_SYMBOL | HASH)
        )
        ( 
            ws?
            (
                (sass_interpolation_expression_var)=>sass_interpolation_expression_var
                |
                (IDENT | MINUS | DOT | HASH_SYMBOL | HASH)
            )
        )*

    ;
    
sass_interpolation_expression_var
    :
        HASH_SYMBOL LBRACE cp_expression RBRACE //XXX possibly allow cp_expression inside
    ;
    
//SASS nested properties:
//.funky {
//  font: 2px/3px {
//    family: fantasy;
//    size: 30em;
//    weight: bold;
//  }
//}
//
//or just:
//
//.funky {
//  font: {
//    family: fantasy;
//    size: 30em;
//    weight: bold;
//  }
//}
sass_nested_properties
    :
    property COLON ws? (propertyValue ws?)? LBRACE ws? syncToFollow declarations? RBRACE
    ;

sass_extend
    :
    SASS_EXTEND ws simpleSelectorSequence (ws? SASS_OPTIONAL)?
    ;
    
sass_extend_only_selector
    :
    SASS_EXTEND_ONLY_SELECTOR
    ;

sass_debug
    :
    ( SASS_DEBUG | SASS_WARN ) ws cp_expression
    ;
    
sass_control
    :
    sass_if | sass_for | sass_each | sass_while
    ;

sass_if
    :
    SASS_IF ws sass_control_expression ws? sass_control_block (ws? sass_else)?
    ;
    
sass_else
    :
    SASS_ELSE ws? sass_control_block 
    |
    SASS_ELSE ws? {tokenNameEquals("if")}? IDENT /* if */ ws? sass_control_expression ws? sass_control_block (ws? sass_else)?
    ;

sass_control_expression
    :
    cp_expression
    ;
    
sass_for
    :
    SASS_FOR ws cp_variable ws {tokenNameEquals("from")}? IDENT /*from*/ ws cp_math_expression ws {tokenNameEquals("to")|tokenNameEquals("through")}? IDENT /*to, through*/ ws cp_math_expression ws? sass_control_block
    ;

sass_each
    :
    SASS_EACH ws cp_variable ws {tokenNameEquals("in")}? IDENT /*in*/ ws cp_expression_list ws? sass_control_block
    ;
    
sass_while
    :
    SASS_WHILE ws sass_control_expression ws? sass_control_block
    ;

sass_control_block
    :
    LBRACE ws? declarations? RBRACE //likely not enough!
    ;
    
sass_function_declaration
    :
    //I assume there can be not only the return statement in the function block, 
    //but so far haven't found any such example so I put the declarations rule inside
    //and added the sass_function_return into the declarations rule itself (not fully correct) 
    //as the return should be allowed only from the sass function declaration
    SASS_FUNCTION ws sass_function_name ws? LPAREN cp_args_list? RPAREN ws? LBRACE ws? declarations? RBRACE
    ;
    
sass_function_name
    :
    IDENT
    ;

sass_function_return
    :
    SASS_RETURN ws cp_expression
    ;
    
sass_content
    :
    SASS_CONTENT
    ;

//*** END OF LESS SYNTAX ***

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
                              '['|'!'|'#'|'$'|'%'|'&'|'*'|'~'|'.'|':'|'/'|'?'|'='|';'|','|'+'
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
BEGINS          : '^='      ;
ENDS            : '$='      ;
CONTAINS        : '*='      ;

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

CP_EQ           : '=='       ;
CP_NOT_EQ       : '!='       ;
LESS            : '<'       ;
GREATER_OR_EQ   : '>=' | '=>'; //a weird operator variant supported by SASS
LESS_OR_EQ      : '=<' | '<='; //a weird operator variant supported by SASS
LESS_WHEN       : 'WHEN'    ;
LESS_AND        : '&'     ;
CP_DOTS         : '...';
LESS_REST       : '@rest...';

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


ONLY 		: 'ONLY';
NOT		: 'NOT'; 
AND		: 'AND';
OR		: 'OR';

// -------------
// Identifier.  Identifier tokens pick up properties names and values
//
IDENT           : '-'? NMSTART NMCHAR*  ;

// -------------
// Reference.   Reference to an element in the body we are styling, such as <XXXX id="reference">
//
HASH_SYMBOL     : '#';
HASH            : HASH_SYMBOL NAME;

IMPORTANT_SYM   : '!' (WS|COMMENT)* 'IMPORTANT'   ;

IMPORT_SYM          : '@IMPORT';
PAGE_SYM            : '@PAGE';
MEDIA_SYM           : '@MEDIA';
NAMESPACE_SYM       : '@NAMESPACE' ;
CHARSET_SYM         : '@CHARSET';
COUNTER_STYLE_SYM   : '@COUNTER-STYLE';
FONT_FACE_SYM       : '@FONT-FACE';

TOPLEFTCORNER_SYM     :'@TOP-LEFT-CORNER';
TOPLEFT_SYM           :'@TOP-LEFT';
TOPCENTER_SYM         :'@TOP-CENTER';
TOPRIGHT_SYM          :'@TOP-RIGHT';
TOPRIGHTCORNER_SYM    :'@TOP-RIGHT-CORNER';
BOTTOMLEFTCORNER_SYM  :'@BOTTOM-LEFT-CORNER'; 
BOTTOMLEFT_SYM        :'@BOTTOM-LEFT';
BOTTOMCENTER_SYM      :'@BOTTOM-CENTER';
BOTTOMRIGHT_SYM       :'@BOTTOM-RIGHT';
BOTTOMRIGHTCORNER_SYM :'@BOTTOM-RIGHT-CORNER';
LEFTTOP_SYM           :'@LEFT-TOP';
LEFTMIDDLE_SYM        :'@LEFT-MIDDLE';
LEFTBOTTOM_SYM        :'@LEFT-BOTTOM';
RIGHTTOP_SYM          :'@RIGHT-TOP';
RIGHTMIDDLE_SYM       :'@RIGHT-MIDDLE';
RIGHTBOTTOM_SYM       :'@RIGHT-BOTTOM';

MOZ_DOCUMENT_SYM      : '@-MOZ-DOCUMENT';
WEBKIT_KEYFRAMES_SYM  :	'@-WEBKIT-KEYFRAMES';

//this generic at rule must be after the last of the specific at rule tokens
SASS_CONTENT        : '@CONTENT';
SASS_MIXIN          : '@MIXIN';
SASS_INCLUDE        : '@INCLUDE';
SASS_EXTEND         : '@EXTEND';
SASS_DEBUG          : '@DEBUG';
SASS_WARN           : '@WARN';
SASS_IF             : '@IF';
SASS_ELSE           : '@ELSE';
//SASS_ELSEIF         : '@ELSE' WS? 'IF'; //@elseif, @else if, @else    if 
SASS_FOR            : '@FOR';
SASS_FUNCTION       : '@FUNCTION';
SASS_RETURN         : '@RETURN';

SASS_EACH           : '@EACH';
SASS_WHILE          : '@WHILE';

AT_IDENT	    : '@' NMCHAR+;	

SASS_VAR            : '$' NMCHAR+;
SASS_DEFAULT        : '!DEFAULT';
SASS_OPTIONAL       : '!OPTIONAL';

SASS_EXTEND_ONLY_SELECTOR
                    : '%' NMCHAR+;

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
fragment    REM		:;  // 'rem'
fragment    ANGLE       :;  // 'deg', 'rad', 'grad'
fragment    TIME        :;  // 'ms', 's'
fragment    FREQ        :;  // 'khz', 'hz'
fragment    DIMENSION   :;  // nnn'Somethingnotyetinvented'
fragment    PERCENTAGE  :;  // '%'
fragment    RESOLUTION  :;  //dpi,dpcm	

NUMBER
    :   (
              '0'..'9'+ ('.' '0'..'9'+)?
            | '.' '0'..'9'+
        )
        (
              (D P (I|C))=>
                D P
                (
                     I | C M     
                )
                { $type = RESOLUTION; }
        	
            | (E (M|X))=>
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
//            | (R A D)=>
//                R A D       { $type = ANGLE;        }

            | (R (A|E))=>
                R    
                ( 
                   A D       {$type = ANGLE;         }
                 | E M       {$type = REM;           }
                )
            
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
    
MOZ_URL_PREFIX
	:
	'URL-PREFIX('
            ((WS)=>WS)? (URL|STRING) WS?
        ')'
    
    	;

MOZ_DOMAIN
	:
	'DOMAIN('
            ((WS)=>WS)? (URL|STRING) WS?
        ')'
    
    	;

MOZ_REGEXP
	:
	'REGEXP('
            ((WS)=>WS)? STRING WS?
        ')'
    
        	;



// -------------
// Whitespace.  Though the W3 standard shows a Yacc/Lex style parser and lexer
//              that process the whitespace within the parser, ANTLR does not
//              need to deal with the whitespace directly in the parser.
//
WS      
    : 
    (' '|'\t')+
    ;

NL      
    : 
    ('\r' '\n'? | '\n')    
    ;

// Comments.    Comments may not be nested, may be multilined and are delimited
//              like C comments: /* ..... */
COMMENT         
    : 
    '/*' ( options { greedy=false; } : .*) '*/'
    ;

LINE_COMMENT
    :
    '//'( options { greedy=false; } : ~('\r' | '\n')* ) {
	$channel = HIDDEN;    
    }   
    ;

// -------------
//  Illegal.    Any other character shoudl not be allowed.
//
