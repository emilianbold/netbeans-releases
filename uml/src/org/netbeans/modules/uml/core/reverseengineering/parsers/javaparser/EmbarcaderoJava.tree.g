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

/** Java 1.3 AST Recognizer Grammar
 *
 * Author: (see java.g preamble)
 *
 * This grammar is in the PUBLIC DOMAIN
 */
class EmbarcaderoJavaTreeParser extends TreeParser;

options {
	importVocab=Java;
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
        mController.errorFound(ex.getErrorMessage(), 
                -1, 
                -1, 
                ex.getFilename()); 
    }

    public void initializeStateNameMap()
    {
        mStateNameMap.put("Package", "Package");
        mStateNameMap.put("Dependency", "Dependency");
        mStateNameMap.put("Class Declaration", "Class Declaration");
        mStateNameMap.put("Interface Declaration", "Interface Declaration");
        mStateNameMap.put("Type", "Type");
        mStateNameMap.put("Array Declarator", "Array Declarator");
        mStateNameMap.put("Modifiers", "Modifiers");
        mStateNameMap.put("Generalization", "Generalization");
        mStateNameMap.put("Realization", "Realization");
        mStateNameMap.put("Body", "Body");
        mStateNameMap.put("Static Initializer", "Static Initializer");
        mStateNameMap.put("Constructor Definition", "Constructor Definition");
        mStateNameMap.put("Method Declaration", "Method Declaration");
        mStateNameMap.put("Method Definition", "Method Definition");
        mStateNameMap.put("Method Body", "Method Body");
        mStateNameMap.put("Destructor Definition", "Destructor Definition");
        mStateNameMap.put("Variable Definition", "Variable Definition");
        mStateNameMap.put("Parameter", "Parameter");
        mStateNameMap.put("Initializer", "Initializer");
        mStateNameMap.put("Array Initializer", "Array Initializer");
        mStateNameMap.put("Parameters", "Parameters");
        mStateNameMap.put("Throws Declaration", "Throws Declaration");
        mStateNameMap.put("Identifier", "Identifier");
        mStateNameMap.put("Constructor Body", "Constructor Body");
        mStateNameMap.put("Conditional", "Conditional");
        mStateNameMap.put("Test Condition", "Test Condition");
        mStateNameMap.put("Body", "Body");
        mStateNameMap.put("Else Conditional", "Else Conditional");
        mStateNameMap.put("Loop", "Loop");
        mStateNameMap.put("Loop Initializer", "Loop Initializer");
        mStateNameMap.put("Test Condition", "Test Condition");
        mStateNameMap.put("Loop PostProcess", "Loop PostProcess");
        mStateNameMap.put("Break", "Break");
        mStateNameMap.put("Continue", "Continue");
        mStateNameMap.put("Return", "Return");
        mStateNameMap.put("Option Conditional", "Option Conditional");
        mStateNameMap.put("Test Condition", "Test Condition");
        mStateNameMap.put("RaisedException", "RaisedException");
        mStateNameMap.put("Exception", "Exception");
        mStateNameMap.put("CriticalSection", "CriticalSection");
        mStateNameMap.put("Lock Object", "Lock Object");
        mStateNameMap.put("Option Group", "Option Group");
        mStateNameMap.put("Option", "Option");
        mStateNameMap.put("Default Option", "Default Option");
        mStateNameMap.put("Exception Processing", "Exception Processing");
        mStateNameMap.put("Default Processing", "Default Processing");
        mStateNameMap.put("Exception Handler", "Exception Handler");
        mStateNameMap.put("Expression List", "Expression List");
        mStateNameMap.put("Conditional Expression", "Conditional Expression");
        mStateNameMap.put("Assignment Expression", "Assignment Expression");
        mStateNameMap.put("Object Destruction", "Object Destruction");
        mStateNameMap.put("Assignment Expression", "Assignment Expression");
        mStateNameMap.put("Plus Assignment Expression", "Plus Assignment Expression");
        mStateNameMap.put("Minus Assignment Expression", "Minus Assignment Expression");
        mStateNameMap.put("Multiply Assignment Expression", "Multiply Assignment Expression");
        mStateNameMap.put("Divide Assignment Expression", "Divide Assignment Expression");
        mStateNameMap.put("Mod Assignment Expression", "Mod Assignment Expression");
        mStateNameMap.put("Shift Right Assignment Expression", "Shift Right Assignment Expression");
        mStateNameMap.put("Shift Right Assignment Expression", "Shift Right Assignment Expression");
        mStateNameMap.put("Shift Left Assignment Expression", "Shift Left Assignment Expression");
        mStateNameMap.put("Binary And Assignment Expression", "Binary And Assignment Expression");
        mStateNameMap.put("Binary XOR Assignment Expression", "Binary XOR Assignment Expression");
        mStateNameMap.put("Binary OR Assignment Expression", "Binary OR Assignment Expression");
        mStateNameMap.put("LogicalOR Expression", "LogicalOR Expression");
        mStateNameMap.put("LogicalAND Expression", "LogicalAND Expression");
        mStateNameMap.put("BinaryOR Expression", "BinaryOR Expression");
        mStateNameMap.put("ExclusiveOR Expression", "ExclusiveOR Expression");
        mStateNameMap.put("BinaryAND Expression", "BinaryAND Expression");
        mStateNameMap.put("Not Equality Expression", "Not Equality Expression");
        mStateNameMap.put("Equality Expression", "Equality Expression");
        mStateNameMap.put("LT Relational Expression", "LT Relational Expression");
        mStateNameMap.put("GT Relational Expression", "GT Relational Expression");
        mStateNameMap.put("LE Relational Expression", "LE Relational Expression");
        mStateNameMap.put("GE Relational Expression", "GE Relational Expression");
        mStateNameMap.put("Shift Left Expression", "Shift Left Expression");
        mStateNameMap.put("Right Shift Expression", "Right Shift Expression");
        mStateNameMap.put("Binary Shift Right Expression", "Binary Shift Right Expression");
        mStateNameMap.put("Plus Expression", "Plus Expression");
        mStateNameMap.put("Minus Expression", "Minus Expression");
        mStateNameMap.put("Divide Expression", "Divide Expression");
        mStateNameMap.put("Mod Expression", "Mod Expression");
        mStateNameMap.put("Multiply Expression", "Multiply Expression");
        mStateNameMap.put("Increment Unary Expression", "Increment Unary Expression");
        mStateNameMap.put("Decrement Unary Expression", "Decrement Unary Expression");
        mStateNameMap.put("Increment Post Unary Expression", "Increment Post Unary Expression");
        mStateNameMap.put("Decrement Post Unary Expression", "Decrement Post Unary Expression");
        mStateNameMap.put("Binary Not Unary Expression", "Binary Not Unary Expression");
        mStateNameMap.put("Logical Not Unary Expression", "Logical Not Unary Expression");
        mStateNameMap.put("Type Check Expression", "Type Check Expression");
        mStateNameMap.put("Minus Unary Expression", "Minus Unary Expression");
        mStateNameMap.put("Plus Unary Expression", "Plus Unary Expression");
        mStateNameMap.put("Identifier", "Identifier");
        mStateNameMap.put("Method Call", "Method Call");
        mStateNameMap.put("Type Cast", "Type Cast");
        mStateNameMap.put("Array Index", "Array Index");
        mStateNameMap.put("Object Creation", "Object Creation");
        mStateNameMap.put("Array Declarator", "Array Declarator");
        mStateNameMap.put("Constructor Call", "Constructor Call");
        mStateNameMap.put("Super Constructor Call", "Super Constructor Call");
    }

    private ParserEventController mController;
    private boolean               isInElsePart;
    private HashMap<String,String> mStateNameMap = new HashMap<String,String>();
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
      mController.stateBegin(mStateNameMap.get("Package"));
   }

   #( p:PACKAGE_DEF { mController.tokenFound(#p, "Keyword"); }
   
      identifier 

      s:SEMI { mController.tokenFound(#s, "Statement Terminator"); }
    )

   {      
      mController.stateEnd();
   }
	;


importDefinition
	:	
   {
      mController.stateBegin(mStateNameMap.get("Dependency"));
   }

   #( i:IMPORT { mController.tokenFound(#i, "Keyword"); } identifierStar s:SEMI { mController.tokenFound(#s, "Statement Terminator"); } )

   {      
      mController.stateEnd();
   }
	;

typeDefinition
	:
     ( 
     {
        mController.stateBegin(mStateNameMap.get("Class Declaration"));
     } 
  
        #(CLASS_DEF //{ mController.tokenFound(#ck, "Keyword"); }
             
          ck:"class"   { mController.tokenFound(#ck, "Keyword"); }

          modifiers 

          n:IDENT      { mController.tokenFound(#n, "Name"); }
         
          extendsClause 
       
          implementsClause 
       
          objBlock 
       
          )
     {
        mController.stateEnd();
     }
     )

	| (
       {
          mController.stateBegin(mStateNameMap.get("Interface Declaration"));
       }

       #(       
       
          INTERFACE_DEF  //{ mController.tokenFound(#ik, "Keyword"); }

          ik:"interface"        { mController.tokenFound(#ik, "Keyword"); }
             
          modifiers 
          
          in:IDENT          { mController.tokenFound(#in, "Name"); }
          
          extendsClause
          
          interfaceBlock        
       
       )

       {
          mController.stateEnd();
       }
     )
	;

typeSpec
{ mController.stateBegin(mStateNameMap.get("Type")); }

	:	#(TYPE typeSpecArray)

   { mController.stateEnd(); }
	;

typeSpecArray
	:	
      (
      { mController.stateBegin(mStateNameMap.get("Array Declarator")); }

         #( lb:ARRAY_DECLARATOR { mController.tokenFound(#lb, "Array Start"); }
         
            typeSpecArray 
            
            rb:RBRACK           { mController.tokenFound(#rb, "Array End"); }
          )

      { mController.stateEnd(); }
      )
	|	type
	;

type
   :	identifier
	|	builtInType
	;

builtInType
    :   v:"void"     { mController.tokenFound(v,  "Primitive Type"); }
    |   b:"boolean"  { mController.tokenFound(b,  "Primitive Type"); }
    |   by:"byte"    { mController.tokenFound(by, "Primitive Type"); }
    |   c:"char"     { mController.tokenFound(c,  "Primitive Type"); }
    |   s:"short"    { mController.tokenFound(s,  "Primitive Type"); } 
    |   i:"int"      { mController.tokenFound(i,  "Primitive Type"); } 
    |   f:"float"    { mController.tokenFound(f,  "Primitive Type"); }
    |   l:"long"     { mController.tokenFound(l,  "Primitive Type"); }
    |   d:"double"   { mController.tokenFound(d,  "Primitive Type"); }
    ;

modifiers
{ mController.stateBegin(mStateNameMap.get("Modifiers")); }
	:	#( MODIFIERS (modifier)* )

   { mController.stateEnd(); }
	;

modifier
    :   m1:"private"       { mController.tokenFound(m1,  "Modifier"); }
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
	 |	  m13:"strictfp"     { mController.tokenFound(m13, "Modifier"); }
    ;

extendsClause
{  mController.stateBegin(mStateNameMap.get("Generalization")); }

   :	#(EXTENDS_CLAUSE  //{ mController.tokenFound(#e, "Keyword"); }        

       (e:"extends" { mController.tokenFound(#e, "Keyword"); } (identifier)* )?

       )
   { mController.stateEnd(); }
	;

implementsClause
{  mController.stateBegin(mStateNameMap.get("Realization")); }

	:	#( IMPLEMENTS_CLAUSE //{ mController.tokenFound(#i, "Keyword"); }         

        (i:"implements" { mController.tokenFound(#i, "Keyword"); } (identifier)* )?

       )

   { mController.stateEnd(); }
	;


interfaceBlock
	:	#(	OBJBLOCK

         s:START_CLASS_BODY { mController.tokenFound(#s, "Class Body Start"); }

			(	methodDecl
			|	variableDef
         |  typeDefinition
			)*

         e:END_CLASS_BODY   { mController.tokenFound(#e, "Class Body End"); }
		)
	;
	
objBlock
	:	#(	OBJBLOCK
         
         s:START_CLASS_BODY 
         { 
            mController.stateBegin(mStateNameMap.get("Body"));
            mController.tokenFound(#s, "Class Body Start"); 
         }

			(	ctorDef
			|	methodDef
			|	variableDef
			|	typeDefinition
			|	#(STATIC_INIT { mController.stateBegin(mStateNameMap.get("Static Initializer")); } slist[""] { mController.stateEnd(); } )
			|	#(INSTANCE_INIT slist[""])
			)*

         e:END_CLASS_BODY   
         {
            mController.tokenFound(#e, "Class Body End"); 
            mController.stateEnd();
         }
		)
	;

parseMethodBody
{ isInElsePart = false; }
   : ( ctorDef
     | methodDef
     | variableDef
     | typeDefinition
	  | #(STATIC_INIT { mController.stateBegin(mStateNameMap.get("Static Initializer")); } slist[""] { mController.stateEnd(); } )
	  | #(INSTANCE_INIT slist[""])

     // parseMethodBody has evoled to handle anything that the 
     // UMS can possibly throw at it.  UMS can decide to parse any part
     // of the file.
     | packageDefinition
     | importDefinition
     )*
   ;

ctorDef
{  mController.stateBegin(mStateNameMap.get("Constructor Definition")); }
	:	#(CTOR_DEF modifiers methodHead ctorSList)

   { mController.stateEnd(); }
	;

methodDecl
{  mController.stateBegin(mStateNameMap.get("Method Declaration")); }

	:	#(METHOD_DEF modifiers typeSpec methodHead 
   
      // methodHead will now pick up the semicolon.
      //s:SEMI { mController.tokenFound(#s, "Statement Terminator"); } 
      
      )

   { mController.stateEnd(); }
	;

methodDef
	:	#( METHOD_DEF {  mController.stateBegin(mStateNameMap.get("Method Definition")); }
   
         modifiers 
         
         typeSpec 
         
         methodHead 
         
         (
           {
              mController.stateBegin(mStateNameMap.get("Method Body"));
           }
           slist["Method"]
           
           {
              mController.stateEnd();
           }
         )?
         { mController.stateEnd(); }
      )
   |  #( DESTRUCTOR_DEF {  mController.stateBegin(mStateNameMap.get("Destructor Definition")); }
   
         modifiers 
         
         typeSpec 
         
         methodHead 
         
         (
           {
              mController.stateBegin(mStateNameMap.get("Method Body"));
           }
           slist["Method"]
           
           {
              mController.stateEnd();
           }
         )?
         { mController.stateEnd(); }
      )   
	;

variableDef
{  mController.stateBegin(mStateNameMap.get("Variable Definition")); }

	: #(VARIABLE_DEF modifiers typeSpec variableDeclarator varInitializer (s:SEMI {mController.tokenFound(#s, "Statement Terminator"); })?) 

   { mController.stateEnd(); }
	;

parameterDef
{  mController.stateBegin(mStateNameMap.get("Parameter")); }

	:	#(PARAMETER_DEF modifiers typeSpec n:IDENT { mController.tokenFound(#n, "Name"); })

   { mController.stateEnd(); }
	;

objectinitializer
	:	#(INSTANCE_INIT slist[""])
	;

variableDeclarator
   :	i:IDENT    { mController.tokenFound(#i, "Name"); }
   |	l:LBRACK   { mController.tokenFound(#l, "Array Decl"); }
      variableDeclarator
	;

varInitializer
	:	(
         {  mController.stateBegin(mStateNameMap.get("Initializer")); }

         #(ASSIGN initializer)

         { mController.stateEnd(); }
      )
	|
	;

initializer
	:	expression
	|	arrayInitializer
	;

arrayInitializer
{  mController.stateBegin(mStateNameMap.get("Array Initializer")); }

	:	#(lc:ARRAY_INIT 
        { mController.tokenFound(#lc, "Start Array Init"); }

        (initializer)* 
        
        rc:RCURLY
        { mController.tokenFound(#rc, "End Array Init"); }
        
       )

   { mController.stateEnd(); }
	;

methodHead
	:	n:IDENT { mController.tokenFound(#n, "Name"); } 

      lp:LPAREN
      { 
         mController.stateBegin(mStateNameMap.get("Parameters")); 
         mController.tokenFound(#lp, "Parameter Start"); 
      }
      #( PARAMETERS  (parameterDef)* ) 

      rp:RPAREN
      {
         mController.tokenFound(#rp, "Parameter End"); 
         mController.stateEnd(); 
      }

      (throwsClause)?

      ( ms:SEMI { mController.tokenFound(#ms, "Statement Terminator"); } )?
	;

throwsClause
{ mController.stateBegin(mStateNameMap.get("Throws Declaration")); }

	:	#( t:"throws" { mController.tokenFound(#t, "Keyword"); } (identifier)* )

   { mController.stateEnd(); }
	;

identifier
	:	
   {
      mController.stateBegin(mStateNameMap.get("Identifier"));
   }

   (  id:IDENT      { mController.tokenFound(#id, "Identifier"); }
   |	#( d:DOT      { mController.tokenFound(#d, "Scope Operator"); }
   
         identifier //{ mController.tokenFound(#d, "Scope Operator"); }
        
         id2:IDENT  { mController.tokenFound(#id2, "Identifier"); }
       )
   )

   {
      mController.stateEnd();
   }
	;

identifierStar
	:	
   {
      mController.stateBegin(mStateNameMap.get("Identifier"));
   }
   ( id:IDENT        { mController.tokenFound(#id, "Identifier"); }
	| #( d:DOT        { mController.tokenFound(#d, "Scope Operator"); }
      
        identifier   //{ mController.tokenFound(#d, "Scope Operator"); }
         
        ( s:STAR  { mController.tokenFound(#s, "OnDemand Operator"); } 
        | id2:IDENT { mController.tokenFound(#id2, "Identifier"); }
        ) 
      )
   )

   {
      mController.stateEnd();
   }
	;

ctorSList
	:	#( s:SLIST 
         {
            mController.stateBegin(mStateNameMap.get("Constructor Body"));
            mController.tokenFound(#s, "Method Body Start");
         }

         (ctorCall)? (stat)*  
         
         e:END_SLIST
         {
            mController.tokenFound(#e, "Method Body End");
            mController.stateEnd();
         }
         
       )
	;

slist[String type]
	:	#( s:SLIST 
         {
            if(type == "Method")
            {
               mController.tokenFound(#s, "Method Body Start");
            }
            else if(type == "Option")
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
            e:END_SLIST
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

stat
{
//   boolean isInElsePart = false;
   boolean isProcessingIf   = true;
   boolean hasProcessedElse = false;
   boolean addConditional   = false;
}
   :	typeDefinition
	|	variableDef
	|	expression
	|	#(LABELED_STAT IDENT stat)

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

	|	#(f:"if" 
       {
         if(isInElsePart == false)
         {
            mController.stateBegin(mStateNameMap.get("Conditional")); 
            addConditional = true;
         }
         else
         {
            //isProcessingIf = true;
            isInElsePart = false;
         }
         mController.tokenFound(#f, "Keyword"); 
         mController.stateBegin(mStateNameMap.get("Test Condition"));          
       } 
       
       expression

       {
         mController.stateEnd(); // Test Condition State
         mController.stateBegin(mStateNameMap.get("Body"));
       }
       
        stat 

        (
          e:"else"
        {
           hasProcessedElse = true;           
           mController.tokenFound(#e, "Keyword"); 

           // Since the Else part is only represented by a statemenet
           // This optional statement is the else part
           // mController.stateEnd(); 
           // Previous Conditional Statement
           //if(_t.getType() != LITERAL_if)
           {
              mController.stateEnd(); // The Body part. 
              mController.stateBegin(mStateNameMap.get("Else Conditional"));

              isProcessingIf = true; 
              if(_t.getType() != LITERAL_if)
              {
                 mController.stateBegin(mStateNameMap.get("Body"));
                 isProcessingIf = false;
              }
              else
              {
                 isInElsePart = true;              
              }
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

         //if(isProcessingIf == false)
         if(addConditional == true)
         {
            mController.stateEnd(); // Conditional State             
         }
         isInElsePart = false;
       }       
      )
	|	#(	fo:"for"
       {
         mController.stateBegin(mStateNameMap.get("Loop")); 
         mController.tokenFound(#fo, "Keyword"); 
         mController.stateBegin(mStateNameMap.get("Loop Initializer")); 
       } 
			#(FOR_INIT (variableDef | elist)?)  is:SEMI     

       {         
         mController.stateEnd(); // Initializer State
         mController.stateBegin(mStateNameMap.get("Test Condition"));
         mController.tokenFound(#is, "Conditional Separator"); 
       }
			#(FOR_CONDITION (expression)?) cs:SEMI

       {
         mController.stateEnd(); // Test Condition State
         mController.stateBegin(mStateNameMap.get("Loop PostProcess"));
         mController.tokenFound(#cs, "PostProcessor Separator"); 
       }
			#(FOR_ITERATOR (elist)?)
      
       {         
         mController.stateEnd(); // PostProcess State
         mController.stateBegin(mStateNameMap.get("Body"));
       }
			stat
       { 
         mController.stateEnd(); // Body State 
         mController.stateEnd(); // Loop State 
       }
		)
	|	#(w:"while" 
       {
         mController.stateBegin(mStateNameMap.get("Loop")); 
         mController.tokenFound(#w, "Keyword"); 
         mController.stateBegin(mStateNameMap.get("Test Condition")); 
       } 
        expression 
       
       {
         mController.stateEnd(); // Test Condition State
         mController.stateBegin(mStateNameMap.get("Body"));
       }

        stat

       { 
         mController.stateEnd(); // Body State 
         mController.stateEnd(); // Conditional State 
       }
      )
	|	#(d:"do"
       {
         mController.stateBegin(mStateNameMap.get("Loop")); 
         mController.tokenFound(#d, "Keyword"); 
         mController.stateBegin(mStateNameMap.get("Body")); 
       } 
        stat 

       { 
         mController.stateEnd(); // Body State 
         mController.stateBegin(mStateNameMap.get("Test Condition")); 
       }
               
        expression

       { 
         mController.stateEnd(); // Test Condition State 
         mController.stateEnd(); // Conditional State 
       }
      )
	|	#( "break" 

      { mController.stateBegin(mStateNameMap.get("Break")); }
      
        ( bDest:IDENT {mController.tokenFound(#bDest, "Destination");})? 

      { mController.stateEnd(); }

      )

	|	#( "continue" 
         { mController.stateBegin(mStateNameMap.get("Continue")); }

         //(IDENT)? 
         ( contDest:IDENT {mController.tokenFound(#contDest, "Destination");})? 

         { mController.stateEnd(); }
      )
   |	#( returnKeyword:"return" 
      {
         mController.stateBegin(mStateNameMap.get("Return")); 
         mController.tokenFound(#returnKeyword, "Keyword"); 
      }

         (expression)? 
     {
         mController.stateEnd();
     }
     )
	|	#( sKey:"switch" 
      {
         mController.stateBegin(mStateNameMap.get("Option Conditional")); 
         mController.tokenFound(#sKey, "Keyword"); 
         mController.stateBegin(mStateNameMap.get("Test Condition")); 
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
	|	#(    
         throwKey:"throw" 

         { 
           mController.stateBegin(mStateNameMap.get("RaisedException"));
           mController.tokenFound(throwKey, "Keyword"); 
           mController.stateBegin(mStateNameMap.get("Exception"));
         }
         
         expression

         { 
           mController.stateEnd(); // Exception
           mController.stateEnd(); // RaisedException
         }
       )
	|	#( syncKeyword:"synchronized" 
      {
         mController.stateBegin(mStateNameMap.get("CriticalSection")); 
         mController.tokenFound(syncKeyword, "Keyword"); 
         mController.stateBegin(mStateNameMap.get("Lock Object")); 
      }
         expression 

      {
         mController.stateEnd(); // Lock Section
         mController.stateBegin(mStateNameMap.get("Body")); 
      }
         stat

      {
         mController.stateEnd(); // Body
         mController.stateEnd(); // CriticalSection
      }
      )

   // Support Java 1.4 assertion
   //|	#(ASSERT expression expression)
	|	tryBlock
	|	slist[""] // nested SLIST
	|	EMPTY_STAT
	;

caseGroup
	:	#(CASE_GROUP 
         // An option group is made up of one or more options.  However,
         // there can only be one body.
         { mController.stateBegin(mStateNameMap.get("Option Group")); }

         (#(c:"case" 
           {
             //mController.stateBegin(mStateNameMap.get("Option")); 
             mController.tokenFound(#c, "Keyword"); 
             mController.stateBegin(mStateNameMap.get("Test Condition")); 
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
            mController.stateBegin(mStateNameMap.get("Default Option"));             
            mController.stateEnd(); // Default Option
         }
         )+ 
         
         {
            mController.stateBegin(mStateNameMap.get("Body")); 
         }
         slist["Option"]
         {
            mController.stateEnd(); // Body
            mController.stateEnd(); // Option Group
         }
      )
	;

tryBlock
	:	#( key:"try"  
       {  
         mController.stateBegin(mStateNameMap.get("Exception Processing")); 
         mController.tokenFound(#key, "Keyword");
         mController.stateBegin(mStateNameMap.get("Body")); 
       }

         slist[""] 

       {
         mController.stateEnd(); // Body
       }
         
         (handler)* 
         
         (#("finally" {  mController.stateBegin(mStateNameMap.get("Default Processing")); } slist[""]) {  mController.stateEnd(); })? 

         {  mController.stateEnd(); }
         
       )
	;

handler
	:	#( "catch" {  mController.stateBegin(mStateNameMap.get("Exception Handler")); }

         parameterDef slist[""] 
         
         {  mController.stateEnd(); }
      )
	;

elist
{  mController.stateBegin(mStateNameMap.get("Expression List")); }
	:	#( ELIST (expression)* )
   {  mController.stateEnd(); }
	;

expression
	:	#(EXPR expr)
	;

expr
   :  //( lp:LPAREN {  mController.tokenFound(#lp, "Precedence Start"); } )?
   	(
         (
            {  mController.stateBegin(mStateNameMap.get("Conditional Expression")); }            
            #(q:QUESTION   { mController.tokenFound(#q, "Operator"); } expr expr qc:COLON { mController.tokenFound(#qc, "Operator"); } expr )	// trinary operator
            {  mController.stateEnd(); }
         )

// 	   |	(
//             {  mController.stateBegin(mStateNameMap.get("Assignment Expression")); }
//             #(a:ASSIGN { mController.tokenFound(#a, "Operator"); } expr expr)			// binary operators...
//             {  mController.stateEnd(); }
//          )
      | (            
           #( a:ASSIGN 
            {
               int type2 = 0;
               if(_t.getNextSibling() != null)
               {
                  type2 = _t.getNextSibling().getType();
               }

               if(type2 == LITERAL_null)
               {
                  mController.stateBegin(mStateNameMap.get("Object Destruction"));
               }
               else
               {
                  mController.stateBegin(mStateNameMap.get("Assignment Expression"));
                  mController.tokenFound(#a, "Operator"); 
               }
                
            } 

               expr expr

            )
            {  mController.stateEnd(); }
        )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Plus Assignment Expression")); }
            #(pa:PLUS_ASSIGN { mController.tokenFound(#pa, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Minus Assignment Expression")); }
            #(sa:MINUS_ASSIGN { mController.tokenFound(#sa, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Multiply Assignment Expression")); }
            #(ma:STAR_ASSIGN { mController.tokenFound(#ma, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Divide Assignment Expression")); }
            #(da:DIV_ASSIGN { mController.tokenFound(#da, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Mod Assignment Expression")); }
            #(modA:MOD_ASSIGN { mController.tokenFound(#modA, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Shift Right Assignment Expression")); }
            #(sra:SR_ASSIGN { mController.tokenFound(#sra, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Shift Right Assignment Expression")); }
            #(bsra:BSR_ASSIGN { mController.tokenFound(#bsra, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Shift Left Assignment Expression")); }
            #(sla:SL_ASSIGN { mController.tokenFound(#sla, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Binary And Assignment Expression")); }
            #(baa:BAND_ASSIGN { mController.tokenFound(#baa, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Binary XOR Assignment Expression")); }
            #(bxa:BXOR_ASSIGN { mController.tokenFound(#bxa, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Binary OR Assignment Expression")); }
            #(boa:BOR_ASSIGN { mController.tokenFound(#boa, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("LogicalOR Expression")); }
            #(lor:LOR { mController.tokenFound(#lor, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("LogicalAND Expression")); }
            #(land:LAND { mController.tokenFound(#land, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("BinaryOR Expression")); }
            #(bor:BOR { mController.tokenFound(#bor, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("ExclusiveOR Expression")); }
            #(bxor:BXOR { mController.tokenFound(#bxor, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("BinaryAND Expression")); }
            #(band:BAND { mController.tokenFound(#band, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Not Equality Expression")); }
            #(notEq:NOT_EQUAL { mController.tokenFound(#notEq, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Equality Expression")); }
            #(eq:EQUAL { mController.tokenFound(#eq, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("LT Relational Expression")); }
            #(lt:LT_ { mController.tokenFound(#lt, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("GT Relational Expression")); }
            #(gt:GT { mController.tokenFound(#gt, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("LE Relational Expression")); }
            #(le:LE { mController.tokenFound(#le, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("GE Relational Expression")); }
            #(ge:GE { mController.tokenFound(#ge, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Shift Left Expression")); }
            #(sl:SL { mController.tokenFound(#sl, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Right Shift Expression")); }
            #(sr:SR { mController.tokenFound(#sr, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Binary Shift Right Expression")); }
            #(bsr:BSR { mController.tokenFound(#bsr, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Plus Expression")); }
            #(p:PLUS { mController.tokenFound(#p, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Minus Expression")); }
            #(m:MINUS { mController.tokenFound(#m, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Divide Expression")); }
            #(d:DIV { mController.tokenFound(#d, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Mod Expression")); }
            #(mod:MOD { mController.tokenFound(#mod, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Multiply Expression")); }
            #(mul:STAR { mController.tokenFound(#mul, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Increment Unary Expression")); }
            #(inc:INC { mController.tokenFound(#inc, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Decrement Unary Expression")); }
            #(dec:DEC { mController.tokenFound(#dec, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Increment Post Unary Expression")); }
            #(pinc:POST_INC { mController.tokenFound(#pinc, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Decrement Post Unary Expression")); }
            #(pdec:POST_DEC { mController.tokenFound(#pdec, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Binary Not Unary Expression")); }
            #(bnot:BNOT { mController.tokenFound(#bnot, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Logical Not Unary Expression")); }
            #(lnot:LNOT { mController.tokenFound(#lnot, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Type Check Expression")); }
            #(insOf:"instanceof" { mController.tokenFound(#insOf, "Operator"); } expr expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Minus Unary Expression")); }
            #(um:UNARY_MINUS { mController.tokenFound(#um, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	(
            {  mController.stateBegin(mStateNameMap.get("Plus Unary Expression")); }
            #(up:UNARY_PLUS { mController.tokenFound(#up, "Operator"); } expr)
            {  mController.stateEnd(); }
         )

	   |	primaryExpression
   )

   //{  mController.stateEnd(); }
//    (rp:RPAREN {  mController.tokenFound(#rp, "Precedence End"); })?
   
	;

primaryExpression
    :   id:IDENT    
        { 
           mController.stateBegin(mStateNameMap.get("Identifier")); 
           mController.tokenFound(#id, "Identifier");
           mController.stateEnd();
        }
    |   #(	d:DOT   
            {
               mController.stateBegin(mStateNameMap.get("Identifier")); 
               mController.tokenFound(#d, "Scope Operator"); 
            }
			   (	expr
				   (	id2:IDENT     { mController.tokenFound(#id2, "Identifier"); }
				   |	arrayIndex
				   |	th1:"this"    { mController.tokenFound(#th1, "This Reference"); }
				   |	c:"class"     { mController.tokenFound(#c, "Class"); }
				   |	#( nOp:"new" 
                     {  
                        mController.stateBegin(mStateNameMap.get("Object Creation")); 
                        mController.tokenFound(#nOp, "Operator");
                     } 
                     
                     nIdent:IDENT 
                     {  
                        mController.stateBegin(mStateNameMap.get("Identifier")); 
                        mController.tokenFound(#nIdent, "Identifier");
                        mController.stateEnd();
                     } 

                     LPAREN

                     elist 

                     RPAREN

                     {
                        mController.stateEnd();
                     }
                  )

				   |  s1:"super"    { mController.tokenFound(#s1, "Super Class Reference"); }
				   )
			   |	#( lb:ARRAY_DECLARATOR { mController.tokenFound(#lb, "Array Start"); }
                  typeSpecArray 
                  rb:RBRACK           { mController.tokenFound(#rb, "Array End"); }
                )
			   |	builtInType ("class")?
			   )

            { mController.stateEnd(); }
   		)
	|	arrayIndex
	|	
      (  {  mController.stateBegin(mStateNameMap.get("Method Call")); }
         #(lp:METHOD_CALL 
           primaryExpression 
           
           { mController.tokenFound(#lp, "Argument Start"); }
           elist 
           
           rp:RPAREN
           { mController.tokenFound(#rp, "Argument End"); } 
           )
         {  mController.stateEnd(); }
      )

	|	(  {  mController.stateBegin(mStateNameMap.get("Type Cast")); }
         #(tlp:TYPECAST { mController.tokenFound(#tlp, "Argument Start"); } 
           
           ( LPAREN )?

           typeSpec     

           trp:RPAREN   { mController.tokenFound(#trp, "Argument End"); }

           expr 
         
           ( RPAREN )?
          )
         {  mController.stateEnd(); }
      )
	|   newExpression
	|   constant
   |   s:"super"    { mController.tokenFound(#s, "Super Class Reference"); }
   |   t:"true"     { mController.tokenFound(#t, "Boolean"); }
   |   f:"false"    { mController.tokenFound(#f, "Boolean"); }
   |   th:"this"    { mController.tokenFound(#th, "This Reference"); }
   |   n:"null"     { mController.tokenFound(#n, "NULL"); }
	|	typeSpec // type name used with instanceof
   |  #(lp2:LPAREN {  mController.tokenFound(#lp2, "Precedence Start"); } expr rp2:RPAREN {  mController.tokenFound(#rp2, "Precedence End"); })
	;

ctorCall
	:	#( CTOR_CALL { mController.stateBegin(mStateNameMap.get("Constructor Call")); }
         elist 

         {  mController.stateEnd(); }
      )
	|	#( SUPER_CTOR_CALL { mController.stateBegin(mStateNameMap.get("Super Constructor Call")); }
			(	elist
			|	primaryExpression elist
			)

         {  mController.stateEnd(); }
		 )
	;

arrayIndex
{  mController.stateBegin(mStateNameMap.get("Array Index")); }
	:	#(lb:INDEX_OP   

       primaryExpression
       { mController.tokenFound(#lb, "Array Start"); }

       expression 
       
       rb:RBRACK { mController.tokenFound(#rb, "Array End"); })
   {  mController.stateEnd(); }
	;

constant
   :   i:NUM_INT         { mController.tokenFound(#i, "Integer Constant"); } 
   |   c:CHAR_LITERAL    { mController.tokenFound(#c, "Character Constant"); } 
   |   s:STRING_LITERAL  { mController.tokenFound(#s, "String Constant"); } 
   |   f:NUM_FLOAT       { mController.tokenFound(#f, "Float Constant"); } 
   |   d:NUM_DOUBLE      { mController.tokenFound(#d, "Double Constant"); } 
   |   l:NUM_LONG        { mController.tokenFound(#l, "Long Constant"); } 
   ;

newExpression
{  mController.stateBegin(mStateNameMap.get("Object Creation")); }
   :	#(	n:"new" { mController.tokenFound(#n, "Operator"); } ( LPAREN )?  type 
			(	newArrayDeclarator (arrayInitializer)?
         |	lp:LPAREN { mController.tokenFound(#lp, "Argument Start"); } elist rp:RPAREN { mController.tokenFound(#rp, "Argument End"); } (objBlock)?
			)

         ( RPAREN )?
		)
	{  mController.stateEnd(); }		
	;

newArrayDeclarator
{ mController.stateBegin(mStateNameMap.get("Array Declarator")); }
	:	#( lb:ARRAY_DECLARATOR   { mController.tokenFound(#lb, "Array Start"); }
         (newArrayDeclarator)? 
         (expression)? 
         rb:RBRACK             { mController.tokenFound(#rb, "Array End"); }
       )
   {  mController.stateEnd(); }
	;
