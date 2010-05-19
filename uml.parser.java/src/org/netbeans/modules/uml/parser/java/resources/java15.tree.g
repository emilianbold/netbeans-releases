header
{
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.uml.parser.java;

import org.netbeans.modules.uml.core.reverseengineering.reframework.parsingframework.ParserEventController;
}

/** Java 1.5 AST Recognizer Grammar
 *
 * Author: (see java.g preamble)
 *
 * This grammar is in the PUBLIC DOMAIN
 */
class JavaTreeParser extends TreeParser;

options {
   importVocab = Java;
   genHashLines=false;
}

{
    public void setEventController(ParserEventController newVal)
    {
        mController = newVal;
    }

    /**
     * Parser error-reporting function can be overridden in subclass.
     * @param ex The exception that occured.
     */
    public void reportError(RecognitionException ex)
    {
        mController.errorFound(ex.getMessage(), 
                -1, 
                -1, 
                ex.getFilename()); 
    }

    private ParserEventController mController;
    private boolean               isInElsePart;
}


compilationUnit
{ isInElsePart = false; }
	:	(packageDefinition)?
		(importDefinition)*
		(typeDefinition)*
	;

packageDefinition
	:
          {
             mController.stateBegin("Package");
          }	
          #( p:PACKAGE_DEF { mController.tokenFound(#p, "Keyword"); }

             // TODO: Handle Package Annotations
             annotations 

             identifier 

             (s:SEMI 
                   { mController.tokenFound(#s, "Statement Terminator"); }
             )?

          )
          
          {      
             mController.stateEnd();
          }
	;


importDefinition
   : #( i:IMPORT {mController.stateBegin("Dependency"); } { mController.tokenFound(#i, "Keyword"); } identifierStar (s:SEMI { mController.tokenFound(#s, "Statement Terminator"); })? { mController.stateEnd(); } )

     // TODO Handle Static Imports
   | #( ii:STATIC_IMPORT {mController.stateBegin("Static Dependency"); } { mController.tokenFound(#ii, "Keyword"); } identifierStar { mController.stateEnd(); }
   )
   ;

typeDefinition
   : (  { mController.stateBegin("Class Declaration"); } 
        #(CLASS_DEF kwc:"class" { mController.tokenFound(#kwc, "Keyword"); } 
          modifiers n:IDENT { mController.tokenFound(#n, "Name"); } 
          (typeParameters)? extendsClause implementsClause objBlock )

        { mController.stateEnd(); }
     )

   | ( { mController.stateBegin("Interface Declaration"); }
       #(INTERFACE_DEF kwi:"interface" { mController.tokenFound(#kwi, "Keyword"); } 
         modifiers in:IDENT { mController.tokenFound(#in, "Name"); }
         (typeParameters)? extendsClause interfaceBlock )

       { mController.stateEnd(); }
     )          
     
   | ( { mController.stateBegin("Enumeration Declaration"); }
       #(ENUM_DEF kwe:"enum" { mController.tokenFound(#kwe, "Keyword"); } 
         modifiers en:IDENT { mController.tokenFound(#en, "Name"); } 
         implementsClause enumBlock )

       { mController.stateEnd(); }
     )

     // TODO: Handle new annotations
   | #(ANNOTATION_DEF modifiers IDENT annotationBlock )
	;

typeParameters
   : #(TYPE_PARAMETERS (typeParameter)+)
   ;

typeParameter
{ mController.stateBegin("Template Parameter"); }
   : #(TYPE_PARAMETER n:IDENT { mController.tokenFound(#n, "Name"); } (typeUpperBounds)?)
     { mController.stateEnd(); }
   ;

// TODO: Generic Wildcard stuff (Upper and Lower bounds)
typeUpperBounds
   : #(TYPE_UPPER_BOUNDS (classOrInterfaceType)+)
   ;

typeSpec
{ mController.stateBegin("Type"); }

	:	#(TYPE typeSpecArray)

         { mController.stateEnd(); }
	;

typeSpecArray
	: ( { mController.stateBegin("Array Declarator"); } 
            #( lb:ARRAY_DECLARATOR { mController.tokenFound(#lb, "Array Start"); }
               typeSpecArray 
               rb:RBRACK           { mController.tokenFound(#rb, "Array End"); }
             ) 

             { mController.stateEnd(); }
          )
	|	type
	;

type
	:	classOrInterfaceType
	|	builtInType
	;
classOrInterfaceType
//{ mController.stateBegin("Identifier");  }

   : //( id:IDENT { mController.tokenFound(#id, "Identifier"); } //(typeArguments)?
     //| #( d:DOT { mController.tokenFound(#d, "Scope Operator"); } classOrInterfaceType )
     //)
     identifier
   | ( #(GENERIC_TYPE { mController.stateBegin("Template Instantiation"); } identifier typeArguments)
    
        { mController.stateEnd(); }
     )
   ;

typeArguments
   : #(TYPE_ARGUMENTS (typeArgument)+)
   ;

typeArgument
   : #( TYPE_ARGUMENT
         ( typeSpec
         | wildcardType
         )
      )
    ;

wildcardType
{ mController.stateBegin("Type"); }
   : #(WILDCARD_TYPE (typeArgumentBounds)?)
     { mController.stateEnd(); }
   ;

typeArgumentBounds
   : #(TYPE_UPPER_BOUNDS (classOrInterfaceType)+)
   | #(TYPE_LOWER_BOUNDS (classOrInterfaceType)+)
   ;

builtInType
    : v:"void"     { mController.tokenFound(v,  "Primitive Type"); }
    | b:"boolean"  { mController.tokenFound(b,  "Primitive Type"); }
    | by:"byte"    { mController.tokenFound(by, "Primitive Type"); }
    | c:"char"     { mController.tokenFound(c,  "Primitive Type"); }
    | s:"short"    { mController.tokenFound(s,  "Primitive Type"); } 
    | i:"int"      { mController.tokenFound(i,  "Primitive Type"); } 
    | f:"float"    { mController.tokenFound(f,  "Primitive Type"); }
    | l:"long"     { mController.tokenFound(l,  "Primitive Type"); }
    | d:"double"   { mController.tokenFound(d,  "Primitive Type"); }
    ;

modifiers
{ mController.stateBegin("Modifiers"); }
	: #( MODIFIERS (modifier)* )

          { mController.stateEnd(); }
	;

// TODO: Support Annotations
modifier
    : m1:"private"       { mController.tokenFound(m1,  "Modifier"); }
    |   m2:"public"        { mController.tokenFound(m2,  "Modifier"); }
    |   m3:"protected"     { mController.tokenFound(m3,  "Modifier"); }
    |   m4:"static"        { mController.tokenFound(m4,  "Modifier"); }
    |   m5:"transient"     { mController.tokenFound(m5,  "Modifier"); }
    |   m6:"final"         { mController.tokenFound(m6,  "Modifier"); }
    |   m7:"abstract"      { mController.tokenFound(m7,  "Modifier"); }
    |   m8:"native"        { mController.tokenFound(m8,  "Modifier"); }
    |   m9:"threadsafe"    { mController.tokenFound(m9,  "Modifier"); }
    |   m10:"synchronized" { mController.tokenFound(m10, "Modifier"); }
    |   m11:"const"        { mController.tokenFound(m11, "Modifier"); }
    |   m12:"volatile"     { mController.tokenFound(m12, "Modifier"); }
    | m13:"strictfp"     { mController.tokenFound(m13, "Modifier"); }
    | annotation
    ;

// TODO: More annotation work
annotations
	:	#(ANNOTATIONS (annotation)* )
	;

annotation
	:	#(ANNOTATION identifier (annotationMemberValueInitializer | (anntotationMemberValuePair)+)? )
	;

annotationMemberValueInitializer
	:	conditionalExpr | annotation | annotationMemberArrayInitializer
	;

anntotationMemberValuePair
	:	#(ANNOTATION_MEMBER_VALUE_PAIR IDENT annotationMemberValueInitializer)
	;

annotationMemberArrayInitializer
	:	#(ANNOTATION_ARRAY_INIT (annotationMemberArrayValueInitializer)* )
	;

annotationMemberArrayValueInitializer
	:	conditionalExpr | annotation
	;

extendsClause
{  mController.stateBegin("Generalization"); }

	: #(EXTENDS_CLAUSE (classOrInterfaceType)* )

          { mController.stateEnd(); }
	;

implementsClause
{  mController.stateBegin("Realization"); }
	: #(IMPLEMENTS_CLAUSE (classOrInterfaceType)* )
    
          { mController.stateEnd(); }
	;

// TODO: Determine if i need the start class body and end class body tokens
interfaceBlock
   : #(	OBJBLOCK   
        s:START_CLASS_BODY { mController.tokenFound(#s, "Class Body Start"); }
   
        ( methodDecl
	| variableDef
	| typeDefinition
	)*

        e:END_CLASS_BODY   { mController.tokenFound(#e, "Class Body End"); }
      )
   ;

objBlock
   : #(	OBJBLOCK

        s:START_CLASS_BODY 
        { 
           mController.stateBegin("Body"); 
           mController.tokenFound(#s, "Class Body Start"); 
        }

	( ctorDef
	| methodDef
	| variableDef
	| typeDefinition
	| #(STATIC_INIT { mController.stateBegin("Static Initializer"); } slist[""] { mController.stateEnd(); })
	| #(INSTANCE_INIT slist[""])
	)*

       e:END_CLASS_BODY  
       { 
          mController.tokenFound(#e, "Class Body End"); 
          mController.stateEnd(); 
       }
     )
   ;

// TODO: More Annotation work
annotationBlock
   : #(	OBJBLOCK
	(	annotationFieldDecl
	|	variableDef
	|	typeDefinition
	)*
     )
   ;

enumBlock
   : #(	OBJBLOCK

        s:START_CLASS_BODY { mController.tokenFound(#s, "Class Body Start"); }

        { mController.stateBegin("Body"); }
         ( enumConstantDef )* 
         ( sm:SEMI {mController.tokenFound(#sm, "Literal Section Terminator"); } 
                ( ctorDef
                | methodDef
                | variableDef
                | typeDefinition
                | #(STATIC_INIT { mController.stateBegin("Static Initializer"); } slist[""] { mController.stateEnd(); })
                | #(INSTANCE_INIT slist[""])
                )*
          )?
        { mController.stateEnd(); }
        e:END_CLASS_BODY   { mController.tokenFound(#e, "Class Body End"); }
      )
   ;

parseMethodBody
{ isInElsePart = false; }
   : ( ctorDef
     | methodDef
     | variableDef
     | typeDefinition
     | #(STATIC_INIT { mController.stateBegin("Static Initializer"); } slist[""] { mController.stateEnd(); } )
     | #(INSTANCE_INIT slist[""])

     // parseMethodBody has evoled to handle anything that the 
     // UMS can possibly throw at it.  UMS can decide to parse any part
     // of the file.
     | packageDefinition
     | importDefinition
     )*
   ;

ctorDef
{  mController.stateBegin("Constructor Definition"); }
   : #(CTOR_DEF modifiers (typeParameters)? methodHead 

        ({mController.stateBegin("Constructor Body"); }  ctor_slist { mController.stateEnd(); })? 

        { mController.stateEnd(); }
      )     
   ;

methodDecl
{  mController.stateBegin("Method Declaration"); }
   : #(METHOD_DEF modifiers (typeParameters)? typeSpec methodHead s:SEMI {mController.tokenFound(#s, "Statement Terminator"); })
     { mController.stateEnd(); }
   ;

// TODO: What to do about the Descructor Def
methodDef
   : #(METHOD_DEF {  mController.stateBegin("Method Definition"); }

       modifiers (typeParameters)? typeSpec methodHead 

      (
        (
         {
           mController.stateBegin("Method Body");
         }

         slist["Method"]

         {
            mController.stateEnd();
         }
        )
        | s:SEMI {mController.tokenFound(#s, "Statement Terminator"); }
      )

         { mController.stateEnd(); }
      ) 
   ;

variableDef
{  mController.stateBegin("Variable Definition"); }

   : #(VARIABLE_DEF modifiers typeSpec variableDeclarator varInitializer (s:SEMI {mController.tokenFound(#s, "Statement Terminator"); })?)

     { mController.stateEnd(); }
   ;

parameterDef[boolean isVar]
{  if(isVar == false) 
   {
      mController.stateBegin("Parameter"); 
   }
   else
   {
      mController.stateBegin("Variable Definition"); 
   }
}

   : #(PARAMETER_DEF modifiers typeSpec n:IDENT { mController.tokenFound(#n, "Name"); })

     { mController.stateEnd(); }
   ;

// TODO: What to do about variable length parameter def.
variableLengthParameterDef
	:	#(VARIABLE_PARAMETER_DEF modifiers typeSpec IDENT )
	;

// TODO: Attribute Annotations
annotationFieldDecl
	:	#(ANNOTATION_FIELD_DEF modifiers typeSpec IDENT (annotationMemberValueInitializer)?)
	;

// TODO: Handle enum values with arguments
enumConstantDef
{ mController.stateBegin("Enum Member"); }
   : #(ENUM_CONSTANT_DEF annotations n:IDENT { mController.tokenFound(#n, "Name"); } (elist rp:RPAREN { mController.tokenFound(#rp, "Parameter End");} )? (enumConstantBlock)? (cm:COMMA {mController.tokenFound(#cm, "Literal Separator");})? )  

     { mController.stateEnd(); }
   ;

enumConstantBlock
   : #(	OBJBLOCK
        { mController.stateBegin("Body"); }

        ( methodDef
        | variableDef
        | typeDefinition
        | #(INSTANCE_INIT slist[""])
        )*
        
        rc:RCURLY { mController.tokenFound(#rc, "Body End"); }

        { mController.stateEnd(); }
      )
   ;

objectinitializer
   : #(INSTANCE_INIT slist[""])
   ;

variableDeclarator
   : i:IDENT    { mController.tokenFound(#i, "Name"); }
   | l:LBRACK   { mController.tokenFound(#l, "Array Decl"); } variableDeclarator
   ;

varInitializer
   : ( {  mController.stateBegin("Initializer"); }

        #(ASSIGN initializer) 

        { mController.stateEnd(); }
     )
   |
   ;

initializer
   : expression
   | arrayInitializer
   ;

// TODO: Need the right curly brace
arrayInitializer
{  mController.stateBegin("Array Initializer"); }

   : #(lc:ARRAY_INIT { mController.tokenFound(#lc, "Start Array Init"); } 
       (initializer)*

       rc:RCURLY
       { mController.tokenFound(#rc, "End Array Init"); }
      )

     { mController.stateEnd(); }
   ;

// TODO: May need RPAREN token
methodHead
   : n:IDENT { mController.tokenFound(#n, "Name"); } 

     { 
        mController.stateBegin("Parameters"); 
        //mController.tokenFound(#lp, "Parameter Start"); 
     } 
     #( PARAMETERS (parameterDef[false])* ) 

    { mController.stateEnd(); }

    (throwsClause)?
   ;

throwsClause
{ mController.stateBegin("Throws Declaration"); }

   : #( t:"throws" { mController.tokenFound(#t, "Keyword"); } (classOrInterfaceType)* )

     { mController.stateEnd(); }
   ;

identifier
   : { mController.stateBegin("Identifier"); }

     ( id:IDENT { mController.tokenFound(#id, "Identifier"); }
     | #( d:DOT        { mController.tokenFound(#d, "Scope Operator"); }
          identifier 
          id2:IDENT    { mController.tokenFound(#id2, "Identifier"); }
        )
     )

     {  mController.stateEnd(); }
   ;

identifierStar
   :  { mController.stateBegin("Identifier"); }

     ( id:IDENT        { mController.tokenFound(#id, "Identifier"); }
     | #( d:DOT        { mController.tokenFound(#d, "Scope Operator"); } 
          identifier 

          ( s:STAR     { mController.tokenFound(#s, "OnDemand Operator"); } 
          | id2:IDENT  { mController.tokenFound(#id2, "Identifier"); }
          ) 
        )
     )

     {  mController.stateEnd(); }
   ;

// TODO: I need the closing curly
slist[String type]
   : #( s:SLIST 

        {
            if(type.equals("Method") == true)
            {
               mController.tokenFound(#s, "Method Body Start");
            }
            else if(type.equals("Option") == true)
            {
//                mController.tokenFound(#s, "Option Statements");
            }
            else
            {
               mController.tokenFound(#s, "Body Start");
            }            
         }

        (stat)* 

        (
            //e:END_SLIST
            e:RCURLY
            {
               if(type == "Method")
               {
                  mController.tokenFound(#e, "Method Body End");
               }
               else
               {
                  mController.tokenFound(#e, "Body End");
               }
            }
         )?
      )
   ;

ctor_slist
   : #( s:SLIST 

         {
            mController.tokenFound(#s, "Method Body Start");
         }

        (stat | ctorCall)* 

        (
            //e:END_SLIST
            e:RCURLY
            {
               mController.tokenFound(#e, "Method Body End");
            }
         )?
      )
   ;

stat
{
   boolean isProcessingIf   = true;
   boolean hasProcessedElse = false;
   boolean addConditional   = false;
} 
   : typeDefinition
   | variableDef
   | expression

     // TODO: May want to do something for activity diagrams
   | #(LABELED_STAT IDENT stat)

   // Alright this is pretty confusing.  But, here is the scope.
   // I want to events to look like
   // Conditional [ for "if(...)" ]
   //   Begin Test Condition
   //   End Test Condition
   //   Begin Body
   //   End Body
   //   Begin Else Conditional [ "else if(...)" ]
   //      Begin Test Condition
   //      End Test Condition
   //      Begin Body
   //      End Body
   //   End Else Conditional
   //   Begin Else Conditional [ "else" ]
   //      Begin Body
   //      End Body
   //   End Else Conditional
   // End Conditional
   //
   // Notice that all of the Else Conditional events or on the 
   // same level as the first conditional body.  Also notice that
   // the begin coditional statements are all on the same level
   // (no nesting).  This is kind of like a switch statement.
   // So, I must suppress the recursive nature of if-else if statements.
   // Thus the reason for all of the checks before sending out state
   // events. 
   //
   // In the future I may want to simplify how event need to be sent
   // for conditional.
   | #(f:"if" 
       {
           if(isInElsePart == false)
           {
              mController.stateBegin("Conditional"); 
              addConditional = true;
           }
           else
           {
              //isProcessingIf = true;
              isInElsePart = false;
           }
           mController.tokenFound(#f, "Keyword"); 
           mController.stateBegin("Test Condition");          
       }
 
       expression 
       {
         mController.stateEnd(); // Test Condition State
         mController.stateBegin("Body");
       }

       stat 
  
       // TODO: May need the "else" keyword
       ( e:"else" 
         
         {
            hasProcessedElse = true;           
            mController.tokenFound(#e, "Keyword"); 

            // Since the Else part is only represented by a statemenet
            // This optional statement is the else part
            // mController.stateEnd(); 
            // Previous Conditional Statement
            
            mController.stateEnd(); // The Body part. 
            mController.stateBegin("Else Conditional");

            isProcessingIf = true; 
            if(_t.getType() != LITERAL_if)
            {
               mController.stateBegin("Body");
               isProcessingIf = false;
            }
            else
            {
               isInElsePart = true;              
            }
         }

         stat

         {
            if(isProcessingIf == false) 
            {
               mController.stateEnd(); // The Body part.               
            }
            mController.stateEnd(); // Else Conditional State 
         }
       )? 

       { 
          if(hasProcessedElse == false)
          {
             mController.stateEnd(); // Body State 
          }

          if(addConditional == true)
          {
             mController.stateEnd(); // Conditional State             
          }
          isInElsePart = false;
       }  
     )
   | #(fo:"for"
       {
         mController.stateBegin("Loop"); 
         mController.tokenFound(#fo, "Keyword");           
       } 

        ( #(FOR_INIT { mController.stateBegin("Loop Initializer"); } ((variableDef)+ | elist)?) is:SEMI
          {         
             mController.stateEnd(); // Initializer State
             mController.stateBegin("Test Condition");
             mController.tokenFound(#is, "Conditional Separator"); 
          }

          #(FOR_CONDITION (expression)?) cs:SEMI
          {
             mController.stateEnd(); // Test Condition State
             mController.stateBegin("Loop PostProcess");
             mController.tokenFound(#cs, "PostProcessor Separator"); 
          }

          #(FOR_ITERATOR (elist)?)
          {         
            mController.stateEnd(); // PostProcess State           
          }
	| #(FOR_EACH_CLAUSE 
              { mController.stateBegin("Loop Initializer"); } parameterDef[true] { mController.stateEnd(); }
              { mController.stateBegin("Test Condition"); } expression { mController.stateEnd(); }
           )
	)
	
        {
           mController.stateBegin("Body");
        } 
        stat

        { 
           mController.stateEnd(); // Body State 
           mController.stateEnd(); // Loop State 
        }
      )
   | #( w:"while" 
        {
          mController.stateBegin("Loop"); 
          mController.tokenFound(#w, "Keyword"); 
          mController.stateBegin("Test Condition"); 
        } 

        expression 
        {
          mController.stateEnd(); // Test Condition State
          mController.stateBegin("Body");
        }
        
        stat
        { 
          mController.stateEnd(); // Body State 
          mController.stateEnd(); // Conditional State 
        }
      )

   | #( d:"do" 
        {
           mController.stateBegin("Loop"); 
           mController.tokenFound(#d, "Keyword"); 
           mController.stateBegin("Body"); 
        } 

        stat 
        { 
           mController.stateEnd(); // Body State 
           mController.stateBegin("Test Condition"); 
        }

        expression
        { 
           mController.stateEnd(); // Test Condition State 
           mController.stateEnd(); // Conditional State 
        }
      )
   | #("break" 
         { mController.stateBegin("Break"); } 
         
         (bDest:IDENT {mController.tokenFound(#bDest, "Destination");} )? 

         {mController.stateEnd(); }
      )
   | #("continue" 
         { mController.stateBegin("Continue"); } 

         (contDest:IDENT {mController.tokenFound(#contDest, "Destination");})? 

         {mController.stateEnd(); }
      )
   | #( returnKeyword:"return" 
        {
           mController.stateBegin("Return"); 
           mController.tokenFound(#returnKeyword, "Keyword"); 
        }

        (expression)? 

        {
           mController.stateEnd();
        }
      )
   | #( sKey:"switch" 
        {
           mController.stateBegin("Option Conditional"); 
           mController.tokenFound(#sKey, "Keyword"); 
           mController.stateBegin("Test Condition"); 
        }
        expression 
        {
           mController.stateEnd(); // Test Condition
        }
        
        (caseGroup)*
        {
           mController.stateEnd(); // Conditional
        }
      )
   | #( throwKey:"throw" 
        { 
           mController.stateBegin("RaisedException");
           mController.tokenFound(throwKey, "Keyword"); 
           mController.stateBegin("Exception");
        }

        expression
        { 
           mController.stateEnd(); // Exception
           mController.stateEnd(); // RaisedException
        }
      )
   | #( syncKeyword:"synchronized"
        {
           mController.stateBegin("CriticalSection"); 
           mController.tokenFound(syncKeyword, "Keyword"); 
           mController.stateBegin("Lock Object"); 
        }
 
        expression 
        {
           mController.stateEnd(); // Lock Section
           mController.stateBegin("Body"); 
        }

        stat
        {
           mController.stateEnd(); // Body
           mController.stateEnd(); // CriticalSection
        }
      )
   | tryBlock
   | slist[""] // nested SLIST
   | #("assert" expression (expression)?)
   | EMPTY_STAT
   ;

caseGroup
   : #( CASE_GROUP 
        // An option group is made up of one or more options.  However,
         // there can only be one body.
         { mController.stateBegin("Option Group"); }

        ( #( c:"case" 
             {
               mController.tokenFound(#c, "Keyword"); 
               mController.stateBegin("Test Condition"); 
             } 
             
             expression
             {
               mController.stateEnd(); // Test Condition
               //mController.stateEnd(); // Option
             } 
           ) 
        | d:"default"
          {
             mController.tokenFound(#d, "Keyword"); 
             mController.stateBegin("Default Option");             
             mController.stateEnd(); // Default Option
          }
        )+ 
  
        {
           mController.stateBegin("Body"); 
        }
        slist["Option"]
        {
           mController.stateEnd(); // Body
           mController.stateEnd(); // Option Group
        }
      )
   ;

tryBlock
   : #( key:"try" 
        {  
          mController.stateBegin("Exception Processing"); 
          mController.tokenFound(#key, "Keyword");
          mController.stateBegin("Body"); 
        }

        slist[""]
        {
          mController.stateEnd(); // Body
        }

        (handler)* 

        (#("finally"{  mController.stateBegin("Default Processing"); } slist[""]) {  mController.stateEnd(); })? 

        {  mController.stateEnd(); }
      )
   ;

handler
   : #( "catch" {  mController.stateBegin("Exception Handler"); } 
         parameterDef[false] slist[""] 
  
        {  mController.stateEnd(); }
      )
   ;

elist
{  mController.stateBegin("Expression List"); }

   : #( ELIST (expression)* )

     {  mController.stateEnd(); }
   ;

expression
   : #(EXPR expr)
   ;

expr
   : conditionalExpr
    
   // The following are binary operators...
   | #(a:ASSIGN 
       {
          // need to determine if the assignment expresion is assigning a 
          // null value to an object.  If the value is a null literal then\
          // we are causing an object destruction.
          int type2 = 0;
          if(_t.getNextSibling() != null)
          {
             type2 = _t.getNextSibling().getType();
          }

          if(type2 == LITERAL_null)
          {
             mController.stateBegin("Object Destruction");
          }
          else
          {
             mController.stateBegin("Assignment Expression");
             mController.tokenFound(#a, "Operator"); 
          }

       } 

       expr expr

       {  mController.stateEnd(); }
     )
			
   | #( pa:PLUS_ASSIGN 
        {
           mController.stateBegin("Plus Assignment Expression");
           mController.tokenFound(#pa, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( sa:MINUS_ASSIGN  
        {
           mController.stateBegin("Minus Assignment Expression");
           mController.tokenFound(#sa, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( ma:STAR_ASSIGN
        {
           mController.stateBegin("Multiply Assignment Expression");
           mController.tokenFound(#ma, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( da:DIV_ASSIGN
        {
           mController.stateBegin("Divide Assignment Expression");
           mController.tokenFound(#da, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( modA:MOD_ASSIGN
        {
           mController.stateBegin("Mod Assignment Expression");
           mController.tokenFound(#modA, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( sra:SR_ASSIGN
        {
           mController.stateBegin("Shift Right Assignment Expression");
           mController.tokenFound(#sra, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( bsra:BSR_ASSIGN
        {
           mController.stateBegin("Shift Right Assignment Expression");
           mController.tokenFound(#bsra, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( sla:SL_ASSIGN
        {
           mController.stateBegin("Shift Left Assignment Expression");
           mController.tokenFound(#sla, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( baa:BAND_ASSIGN
        {
           mController.stateBegin("Binary And Assignment Expression");
           mController.tokenFound(#baa, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #( bxa:BXOR_ASSIGN
        {
           mController.stateBegin("Binary XOR Assignment Expression");
           mController.tokenFound(#bxa, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   | #(boa: BOR_ASSIGN
        {
           mController.stateBegin("Binary OR Assignment Expression");
           mController.tokenFound(#boa, "Operator");
        }

        expr expr
     
        {  mController.stateEnd(); }
      )

   ;

conditionalExpr
   : #(q:QUESTION 	// trinary operator  [condition, ifTrue, ifFalse]
       {
          mController.stateBegin("Conditional Expression");
          mController.tokenFound(#q, "Operation");
          mController.stateBegin("Test Condition");
       }

       expr 
       {
         mController.stateEnd(); // Test Condition State
         mController.stateBegin("Body");
       } 

       expr 
       {
         mController.stateEnd(); // The Body part. 
         mController.stateBegin("Else Conditional");
         mController.stateBegin("Body");
       }

       expr
       {
         mController.stateEnd(); // The Else Body
         mController.stateEnd(); // Else Conditional
         mController.stateEnd(); // Conditional Expression
       }
      )
   | #(lor:LOR 
        {
           mController.stateBegin("LogicalOR Expression");
           mController.tokenFound(#lor, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(land:LAND
        {
           mController.stateBegin("LogicalAND Expression");
           mController.tokenFound(#land, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(bor:BOR
        {
           mController.stateBegin("BinaryOR Expression");
           mController.tokenFound(#bor, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(bxor:BXOR
        {
           mController.stateBegin("ExclusiveOR Expression");
           mController.tokenFound(#bxor, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(band:BAND
        {
           mController.stateBegin("BinaryAND Expression");
           mController.tokenFound(#band, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(notEq:NOT_EQUAL
        {
           mController.stateBegin("Not Equality Expression");
           mController.tokenFound(#notEq, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(eq:EQUAL
        {
           mController.stateBegin("Equality Expression");
           mController.tokenFound(#eq, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(lt:LT
        {
           mController.stateBegin("LT Relational Expression");
           mController.tokenFound(#lt, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(gt:GT
        {
           mController.stateBegin("GT Relational Expression");
           mController.tokenFound(#gt, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(le:LE
        {
           mController.stateBegin("LE Relational Expression");
           mController.tokenFound(#le, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(ge:GE
        {
           mController.stateBegin("GE Relational Expression");
           mController.tokenFound(#ge, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(sl:SL
        {
           mController.stateBegin("Shift Left Expression");
           mController.tokenFound(#sl, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(sr:SR
        {
           mController.stateBegin("Right Shift Expression");
           mController.tokenFound(#sr, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(bsr:BSR
        {
           mController.stateBegin("Binary Shift Right Expression");
           mController.tokenFound(#bsr, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(p:PLUS
        {
           mController.stateBegin("Plus Expression");
           mController.tokenFound(#p, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(m:MINUS
        {
           mController.stateBegin("Minus Expression");
           mController.tokenFound(#m, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(d:DIV
        {
           mController.stateBegin("Divide Expression");
           mController.tokenFound(#d, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(mod:MOD
        {
           mController.stateBegin("Mod Expression");
           mController.tokenFound(#mod, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(mul:STAR
        {
           mController.stateBegin("Multiply Expression");
           mController.tokenFound(#mul, "Operator");
        }

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(inc:INC
        {
           mController.stateBegin("Increment Unary Expression");
           mController.tokenFound(#inc, "Operator");
        } 

      expr
     
        {  mController.stateEnd(); }
     )
   | #(dec:DEC
        {
           mController.stateBegin("Decrement Unary Expression");
           mController.tokenFound(#dec, "Operator");
        } 

      expr
     
        {  mController.stateEnd(); }
     )

   | #(pinc:POST_INC 
        {
           mController.stateBegin("Increment Post Unary Expression");
           mController.tokenFound(#pinc, "Operator");
        } 

       expr
     
        {  mController.stateEnd(); }
      )

   | #(pdec:POST_DEC
        {
           mController.stateBegin("Decrement Post Unary Expression");
           mController.tokenFound(#pdec, "Operator");
        } 

       expr
     
        {  mController.stateEnd(); }
      )

   | #(bnot:BNOT
        {
           mController.stateBegin("Binary Not Unary Expression");
           mController.tokenFound(#bnot, "Operator");
        } 

       expr
     
        {  mController.stateEnd(); }
      )

   | #(lnot:LNOT
        {
           mController.stateBegin("Logical Not Unary Expression");
           mController.tokenFound(#lnot, "Operator");
        } 

       expr
     
        {  mController.stateEnd(); }
      )

   | #(insOf:"instanceof"
        {
           mController.stateBegin("Type Check Expression");
           mController.tokenFound(#insOf, "Operator");
        } 

       expr expr
     
        {  mController.stateEnd(); }
      )

   | #(um:UNARY_MINUS
        {
           mController.stateBegin("Minus Unary Expression");
           mController.tokenFound(#um, "Operator");
        } 

       expr
     
        {  mController.stateEnd(); }
      )

   | #(up:UNARY_PLUS
        {
           mController.stateBegin("Plus Unary Expression");
           mController.tokenFound(#up, "Operator");
        } 

       expr
     
        {  mController.stateEnd(); }
      )

   | primaryExpression
   ;

primaryExpression
   : id:IDENT
     { 
       mController.stateBegin("Identifier"); 
       mController.tokenFound(#id, "Identifier");
       mController.stateEnd();
     }
   | #( d:DOT
        {
           mController.stateBegin("Identifier"); 
           mController.tokenFound(#d, "Scope Operator"); 
        }
        ( expr
           ( id2:IDENT     { mController.tokenFound(#id2, "Identifier"); }
           | arrayIndex
           | th1:"this"    { mController.tokenFound(#th1, "This Reference"); }
           | c:"class"     { mController.tokenFound(#c, "Class"); }
           | newExpression
           | s1:"super"    { mController.tokenFound(#s1, "Super Class Reference"); }

             // TODO: Research this generic construct
           | (typeArguments)? // for generic methods calls
           )

        // TODO: I need the Array RBRACK token
        | #(lb:ARRAY_DECLARATOR { mController.tokenFound(#lb, "Array Start"); } 
            typeSpecArray
            rb:RBRACK           { mController.tokenFound(#rb, "Array End"); }
           )
        | builtInType ("class")?
        )

        { mController.stateEnd(); }
      )
   | arrayIndex
   | ( {  mController.stateBegin("Method Call"); }
       #( lp:METHOD_CALL 
          primaryExpression 
          (typeArguments)? 

          { mController.tokenFound(#lp, "Argument Start"); }
          elist

          rp:RPAREN
          { mController.tokenFound(#rp, "Argument End"); } 
        )

        { mController.stateEnd(); }
     )
   | ctorCall
   | (
       #( tlp:TYPECAST 
          {  
             mController.stateBegin("Type Cast"); 
             mController.tokenFound(#tlp, "Argument Start");
          }

          ( LPAREN )?

          typeSpec 

          trp:RPAREN   { mController.tokenFound(#trp, "Argument End"); }

          expr

          {  mController.stateEnd(); }
        )
         
     )
   | newExpression
   | constant
   | s:"super"    { mController.tokenFound(#s, "Super Class Reference"); }
   | t:"true"     { mController.tokenFound(#t, "Boolean"); }
   | f:"false"    { mController.tokenFound(#f, "Boolean"); }
   | th:"this"    { mController.tokenFound(#th, "This Reference"); }
   | n:"null"     { mController.tokenFound(#n, "NULL"); }
   | #(lp2:LPAREN {  mController.tokenFound(#lp2, "Precedence Start"); } expr rp2:RPAREN {  mController.tokenFound(#rp2, "Precedence End"); })
   | typeSpec // type name used with instanceof
   ;

ctorCall
   : #( CTOR_CALL { mController.stateBegin("Constructor Call"); } elist {  mController.stateEnd(); })
   | #( SUPER_CTOR_CALL { mController.stateBegin("Super Constructor Call"); }
        ( elist
        | primaryExpression elist
        )
        {  mController.stateEnd(); }
      )
   ;

// TODO: I need the RBRACK token there
arrayIndex
{  mController.stateBegin("Array Index"); }
   : #( lb:INDEX_OP 
        expr 

        { mController.tokenFound(#lb, "Array Start"); }
        expression

        // rb:RBRACK { mController.tokenFound(#rb, "Array End"); })
        {  mController.stateEnd(); }
      )
   ;

constant
   :   i:NUM_INT         { mController.tokenFound(#i, "Integer Constant"); } 
   |   c:CHAR_LITERAL    { mController.tokenFound(#c, "Character Constant"); } 
   |   s:STRING_LITERAL  { mController.tokenFound(#s, "String Constant"); } 
   |   f:NUM_FLOAT       { mController.tokenFound(#f, "Float Constant"); } 
   |   d:NUM_DOUBLE      { mController.tokenFound(#d, "Double Constant"); } 
   |   l:NUM_LONG        { mController.tokenFound(#l, "Long Constant"); } 
   ;

// TODO: Handle generic
newExpression
{  mController.stateBegin("Object Creation"); }
   : #(	n:"new" { mController.tokenFound(#n, "Operator"); } (typeArguments)? type
	( newArrayDeclarator (arrayInitializer)?
	| lp:LPAREN { mController.tokenFound(#lp, "Argument Start"); } elist rp:RPAREN { mController.tokenFound(#rp, "Argument End"); } (objBlock)?
	)

        ( RPAREN )?
      )
   {  mController.stateEnd(); }	
   ;

newArrayDeclarator
{ mController.stateBegin("Array Declarator"); }

   : #( lb:ARRAY_DECLARATOR   { mController.tokenFound(#lb, "Array Start"); }
        (newArrayDeclarator)? 
        (expression)? 

        rb:RBRACK             { mController.tokenFound(#rb, "Array End"); }
      )

   {  mController.stateEnd(); }
   ;
