/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.php.model;

import java.util.List;

import org.netbeans.modules.php.model.resources.ResourceMarker;


/**
 *
 * @author ads
 *
 */
public class ExpressionTest extends BaseCase {

   public  void testOrExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(0);
           assert statement instanceof ExpressionStatement;
           
           ExpressionStatement expressionStatement = 
               (ExpressionStatement) statement ;
           Expression expression = expressionStatement.getExpression();
           
           assert expression.getElementType().equals( BinaryExpression.class );
           BinaryExpression binaryExpression = (BinaryExpression) expression ;

           assert binaryExpression.getOperator().equals( "or" ) :"Expected to find" +
                " 'or' operator, but found :" + binaryExpression.getOperator();
           
           Expression left  = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();

           String text = binaryExpression.getText();
           assert left.getElementType().equals( Literal.class ) :"Expected to " +
                "find literal as first operand in expression '" +
                text +"' , but found :" +left.getElementType();
           
           assert right.getElementType().equals( BinaryExpression.class ):
               "Expected to " +
               "find binary expression as second operand in expression '" +
               text +"' , but found :" +right.getElementType();
           
           binaryExpression = (BinaryExpression) right;
           
           assert binaryExpression.getOperator().equals( "or" ) :"Expected to find" +
           " 'or' operator, but found :" + binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();

           assert left.getElementType().equals( Literal.class ): 
               "Expected to find literal as second operand in complex expression :"
               + text+" , but found :" +left.getElementType();
           
           assert right.getElementType().equals( Literal.class ): 
               "Expected to find literal as third operand in complex expression :"
               + text+" , but found :" +right.getElementType();
               
       }
       finally {
           model.readUnlock();
       }
   }
   
   public  void testSecondExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(1);
           assert statement instanceof ExpressionStatement;
           
           ExpressionStatement expressionStatement = 
               (ExpressionStatement) statement ;
           Expression expression = expressionStatement.getExpression();
           
           assert expression.getElementType().equals( BinaryExpression.class );
           BinaryExpression binaryExpression = (BinaryExpression) expression ;
           
           assert binaryExpression.getOperator() .equals( "+=" ) : "Expected" +
                " to find '+=' operator in second expression , but found :" +
                binaryExpression.getOperator();
           
           Expression left = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Variable.class ) :"Expected to " +
                "find variable as left operand in second expression , but found :"+
                left.getElementType();
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand in second" +
               " expression, but found :" +right.getElementType();
          
           binaryExpression = (BinaryExpression) right;
           assert binaryExpression.getOperator().equals( "xor" ) :"Expected to " +
                "find 'xor' as operator in left operand , but found :" +
                binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Variable.class ) :"Expected to " +
               "find variable as left operand in second expression inside " +
               "assignent, but found :"+left.getElementType();
           
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand in second" +
               " expression inside assignment, but found :" +right.getElementType();
           
           binaryExpression = (BinaryExpression) right;
           
           assert binaryExpression.getOperator().equals("&&") : "Expected to find" +
                " operator '&&' after 'xor' operator, but found :" +
                binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Literal.class ): "Expected to " +
                "find literal expression as left operand in && operator, but " +
                "found :"+left.getElementType(); 
           
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand in second" +
               " expression with && operator, but found :" +right.getElementType();
           
           binaryExpression = (BinaryExpression) right;
           
           assert binaryExpression.getOperator().equals("|") : "Expected to find" +
               " operator '|' after '&&' operator, but found :" +
               binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Literal.class ): "Expected to " +
                "find literal expression as left operand in '|' operator, but " +
                "found :"+left.getElementType(); 
           
           assert right.getElementType().equals( Literal.class ): "Expected to " +
               "find literal expression as left operand in '|' operator, but " +
               "found :"+right.getElementType(); 
       }
       finally {
           model.readUnlock();
       }
   }
   
   public  void testThirdExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(2);
           assert statement instanceof ExpressionStatement;
           
           ExpressionStatement expressionStatement = 
               (ExpressionStatement) statement ;
           Expression expression = expressionStatement.getExpression();
           
           assert expression.getElementType().equals( BinaryExpression.class );
           BinaryExpression binaryExpression = (BinaryExpression) expression ;
           
           assert binaryExpression.getOperator() .equals( "=" ) : "Expected" +
                " to find '=' operator in third expression , but found :" +
                binaryExpression.getOperator();
           
           Expression left = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Variable.class ) :"Expected to " +
                "find variable as left operand in third expression , but found :"+
                left.getElementType();
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand in third" +
               " expression, but found :" +right.getElementType();
          
           binaryExpression = (BinaryExpression) right;
           assert binaryExpression.getOperator().equals( "+" ) :"Expected to " +
                "find '+' as operator in left operand , but found :" +
                binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Variable.class ) :"Expected to " +
               "find variable as left operand inside assignment" +
               " in third expression , but found :"+left.getElementType();
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand in third" +
               " expression after assignment, but found :" +right.getElementType();
           
           binaryExpression = (BinaryExpression) right;
           assert binaryExpression.getOperator().equals( "-" ) :"Expected to " +
                "find '-' as operator in left operand , but found :" +
                binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Literal.class ) :"Expected to " +
               "find literal as left operand with '-' operator " +
               " in third expression , but found :"+left.getElementType();
           assert right.getElementType().equals( UnaryExpression.class ) :
               "Expected to find unary expression as right operand in third" +
               " expression after assignment, but found :" +right.getElementType();
           
           UnaryExpression unaryExpression = (UnaryExpression) right;
           assert unaryExpression.getOperator().equals( "++" ) :"Expected" +
                " to find '++' unary operator , but found :" +
                unaryExpression.getOperator();
           
           assert unaryExpression.isPostfix() :"Expected to find postfix operator";
           assert !unaryExpression.isPrefix() :"Unexpected prefix operator";
           
           expression = unaryExpression.getOperand();
           assert expression.getElementType().equals( Variable.class ) :
               "Expected to find variable in incremental operator '++', " +
               "but found : " + expression.getElementType();
       }
       finally {
           model.readUnlock();
       }
   }
   
   public  void testFourthExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(3);
           assert statement instanceof ExpressionStatement;
           
           ExpressionStatement expressionStatement = 
               (ExpressionStatement) statement ;
           Expression expression = expressionStatement.getExpression();
           
           assert expression.getElementType().equals( BinaryExpression.class );
           BinaryExpression binaryExpression = (BinaryExpression) expression ;
           
           assert binaryExpression.getOperator().equals( "!==" ) :
               "Expected to find '!==' operator but found : " 
               +binaryExpression.getOperator();
           
           Expression left = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Variable.class ) :"Expected to " +
                "find variable as left operand in fourth expression , but found :"+
                left.getElementType();
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand in fourth" +
               " expression, but found :" +right.getElementType();
           
           binaryExpression = (BinaryExpression) right;
           assert binaryExpression.getOperator().equals( "/" ) :"Expected to " +
                "find '/' as operator in right operand , but found :" +
                binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( UnaryExpression.class ) :
               "Expected to find unary expression as left operand with '/' operator " +
               " in fourth expression , but found :"+left.getElementType();
           assert right.getElementType().equals( Literal.class ) :
               "Expected to find literal expression as right operand in third" +
               " expression after assignment, but found :" +right.getElementType();
           
           UnaryExpression unaryExpression = (UnaryExpression) left;
           assert unaryExpression.getOperator().equals( UnaryExpression.PARENS ) 
               :"Expected to find '()' unary operator , but found :" +
               unaryExpression.getOperand() ;
           
           assert !unaryExpression.isPostfix():"Unexpected postfix unary expression";
           assert !unaryExpression.isPrefix() : "Unexpected prefix unary expression";
           
           expression = unaryExpression.getOperand();
           assert expression.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression inside () , but found :" +
               expression.getElementType();
           
           binaryExpression = (BinaryExpression) expression;
           
           assert binaryExpression.getOperator().equals( "<>" ) :"Expected to " +
               "find '<>' as operator in right operand , but found :" +
               binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Constant.class ) :
               "Expected to find unary expression as left operand with '/' operator " +
               " in fourth expression inside (), but found :"+left.getElementType();
           assert right.getElementType().equals( Variable.class ) :
               "Expected to find variable expression as right operand in fourth" +
               " expression inside (), but found :" +right.getElementType();
           
           Constant conzt = (Constant)left;
           // TODO : Constant interface should be extended with more methods
           // and there should be set of tests for these methods. 
       }
       finally {
           model.readUnlock();
       }
   }  
   
   public  void testFifthExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           
           Statement statement = model.getStatements().get(4);
           assert statement instanceof ExpressionStatement;
           
           ExpressionStatement expressionStatement = 
               (ExpressionStatement) statement ;
           Expression expression = expressionStatement.getExpression();
           
           assert expression.getElementType().equals( BinaryExpression.class );
           BinaryExpression binaryExpression = (BinaryExpression) expression ;
           
           assert binaryExpression.getOperator().equals( ".=" ) :
               "Expected to find '.=' operator but found : " 
               +binaryExpression.getOperator();
           
           Expression left = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();
           
           assert left.getElementType().equals( Variable.class ) :
               "Expected to find variable as left operand in fifth expression , " +
               "but found :"+left.getElementType();
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand in fifth" +
               " expression , but found :" +right.getElementType();
           
           binaryExpression = ( BinaryExpression) right;
           
           assert binaryExpression.getOperator().equals( "&" ) :"Expected" +
                " to find '&' operator as first operator in left assignment " +
                "expression , but found : " +binaryExpression.getOperator();
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           
           assert left.getElementType().equals( CallExpression.class ) :
               "Expected to find call expression as left operand in fifth" +
               " expression after assignment with '&' operator, but found :" +
               right.getElementType();
           
           assert right.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression as right operand with '&' operator " +
               " in fifth expression , but found :"+left.getElementType();
           
           CallExpression callExpression = (CallExpression) left;
           IdentifierExpression name = callExpression.getName();
           assert name != null;
           assert name.getElementType().equals( Constant.class ) :"Expected to find" +
                " constant as function name , but found :" + name.getElementType();
           
           binaryExpression = (BinaryExpression) right;
           
           left = binaryExpression.getLeftOperand();
           right = binaryExpression.getRightOperand();
           
           assert binaryExpression.getOperator().equals( "+" ) :"Expected " +
                "operator '+' as lst operator in right expression, but found :"+
                binaryExpression.getOperator();
           
           assert left.getElementType().equals( UnaryExpression.class ) :
               "Expected to find unary expression as left expression with '+'" +
               " operator, but found : " +left.getElementType();
           
           assert right.getElementType().equals( CallExpression.class ) :
               "Expected to find call expression as last expression in " +
               "right operand of assignment expression, but found: " +
               right.getElementType();
           
           UnaryExpression unaryExpression = (UnaryExpression) left;
           assert unaryExpression.isPrefix() :"Expected prefix unary expression";
           assert !unaryExpression.isPostfix() : "Unexpected postfix unary expression";
           
           assert unaryExpression.getOperator().equals("!") :"Expected to find " +
                "unary operand '!', but found :" + unaryExpression.getOperand();
           
           
           callExpression = (CallExpression) right;
           
           assert callExpression.getArguments() != null;
           Arguments args = callExpression.getArguments();
           assert args.getArgumentsList().size() == 2 :"Expected to find two arguments," +
                " but found :" + args.getArgumentsList();
           name = callExpression.getName();
           assert name != null;
           assert name.getElementType().equals( ClassMemberExpression.class ) :
               "Expected to find class memeber expression in last call expression," +
               "but found :" +name.getElementType();
           
           ClassMemberExpression memberExpression = (ClassMemberExpression) name;
           IdentifierExpression identifierExpression = 
               memberExpression.getOwnerIdentifier();
           assert identifierExpression!= null;
           assert identifierExpression.getElementType().equals( Variable.class )
               :"Expected to find variable as identifier expression for class" +
                    " in last method call expression, but found :" +
                    identifierExpression.getElementType();
           
           assert identifierExpression.getText().equals( "$clazz" ) 
               :"Expected text '$clazz' for identifier expression , but found :"
                   + identifierExpression.getText();
           
           /*
            * unary expression is !$arr[ ++$i ]
            */
           expression = unaryExpression.getOperand();
           
           assert expression.getElementType().equals( ArrayMemberExpression.class )
               :"Expected to find array member access in unary expression ," +
                    "but found : " + expression.getElementType();
           
           ArrayMemberExpression arrayMember = (ArrayMemberExpression) expression;
           assert arrayMember.getCallExpression() == null;
           
           expression = arrayMember.getExpression();
           assert expression != null :"Expression in '[]' should be not null";
           assert expression.getElementType().equals( UnaryExpression.class ) :
               "Expected to find unary expression in '[]', but found :" +
               expression.getElementType();
           assert ((UnaryExpression) expression).getOperator().equals("++") : 
               "Expected to find unary operator '++' in '[]', but found :" +
               ((UnaryExpression) expression).getOperator();
           
           identifierExpression = arrayMember.getOwnerIdentifier();
           assert identifierExpression != null;
           assert identifierExpression.getElementType().equals( Variable.class ) :
               "Expected to find variable identifier for array , but found :" +
               identifierExpression.getElementType();
       }
       finally {
           model.readUnlock();
       }
   }    

   
   public  void testSixthExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(5);
           assert statement instanceof ExpressionStatement;
           
           ExpressionStatement expressionStatement = 
               (ExpressionStatement) statement ;
           Expression expression = expressionStatement.getExpression();
           
           assert expression.getElementType().equals( ArrayExpression.class ):
               "Expected to find array expression in sixth expression, but " +
               "found :" + expression.getElementType();
           
           ArrayExpression arrayExpr = (ArrayExpression) expression;
           List<ArrayDefElement> elements = arrayExpr.getElements();
           
           assert elements.size() >1 :"Expected to find at least two " +
                "array def elements, but found : "+ elements.size();
           ArrayDefElement element = elements.get( 0 );
           assert element != null;
           assert element.getElementType().equals( UnaryExpression.class ) :
               "Expected to find unary expression as first array element," +
               "but found :" + element.getElementType();
           
           UnaryExpression unary = (UnaryExpression) element;
           assert unary.getOperator().equals( "--" ) :"Expected to find unary" +
                " operator '--' , but found :" + unary.getOperator();
           
           assert unary.getOperand().getElementType().equals( Variable.class ):
               "Expected to find variable type for operand in unary expression," +
               "but found : " + unary.getOperand();
           
           element = elements.get( 1 );
           assert element.getElementType().equals( AssociativeArrayElement.class ):
               "Expected to find associative element as second element in " +
               "array definition, but found : " + element.getElementType();
           AssociativeArrayElement assoc = (AssociativeArrayElement) element;
           assert assoc != null;
           Expression key = assoc.getKey();
           Expression value = assoc.getValue();
           
           assert key != null :"Expected to find key element in associative " +
                "array element";
           assert value != null :"Expected to find value element in associative " +
               "array element";
           
           assert key.getElementType().equals( UnaryExpression.class ) :
               "Expected to find unary expression '()' as key , but found :" +
               key.getElementType();
           assert value.getElementType().equals( Variable.class ) :"Expected " +
                "to find variable as value , but found :" +value.getElementType();
           
           unary = (UnaryExpression) key;
           assert unary.getOperator().equals( UnaryExpression.PARENS ):"Expected" +
                " '()' operator in unary expression , but found : " +unary.getOperator();
           expression = unary.getOperand();
           
           assert expression.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression inside '()' , but found :" +
               expression.getElementType();
           BinaryExpression binaryExpression = (BinaryExpression) expression;
           
           assert binaryExpression.getOperator().equals( "+" ) :
               "Expected to find '+' operator as operator inside '()' , but found :"
               + binaryExpression.getOperator() ;
           
           assert binaryExpression.getLeftOperand().getElementType().equals( 
                   Variable.class ) :"Expected variable type for left operand" +
                        " in additive expression," +
                        " but found :" + binaryExpression.getLeftOperand().getElementType();
           assert binaryExpression.getRightOperand().getElementType().equals( 
                   Variable.class ) :"Expected variable type for right operand " +
                        "in additive expression," +
                   " but found :" + binaryExpression.getLeftOperand().getElementType();
       }
       finally {
           model.readUnlock();
       }
   } 
   
   public  void testSeventhExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(6);
           assert statement instanceof ExpressionStatement;
           
           Expression expression = ((ExpressionStatement)statement).getExpression();
           assert expression.getElementType().equals( CallExpression.class ) :
               "Expected to find call expression , but found : " +
               expression.getElementType();
           
           CallExpression callExpression = (CallExpression) expression;
           Arguments args= callExpression.getArguments();
           assert args != null ;
           List<Expression> argums = args.getArgumentsList();
           assert argums.size() > 0 :"Expected to find at least one arument";
           expression = argums.get( 0 );
           assert expression.getElementType().equals( UnaryExpression.class ) :
               "Expected to find unary expression as argument for include" +
               " function, but found  :" +expression.getElementType();
           
           assert callExpression.getName().getElementType().equals( Constant.class ) 
               :"Expected to find constant name for include function , but found :" +
               callExpression.getName().getElementType();
           
           assert callExpression.getName().getText().equals( "include" ) :
               "Expected to find 'include' as text for function , but found :"+
               callExpression.getName().getText();
                
           UnaryExpression unary = (UnaryExpression) expression;
           assert unary.getOperator().equals( UnaryExpression.PARENS ) :"Expected" +
                " '()' unary operator, but found :" +unary.getOperator();
           
           expression = unary.getOperand();
           assert expression.getElementType().equals( Literal.class ) :"Expected" +
                " literal element as argument for include, but found :" +
                expression.getElementType();
       }
       finally {
           model.readUnlock();
       }
   } 
   
   public  void testEighthExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(7);
           assert statement instanceof ExpressionStatement;
           
           Expression expression = ((ExpressionStatement)statement).getExpression();
           assert expression.getElementType().equals( CallExpression.class ) :
               "Expected to find call expression , but found : " +
               expression.getElementType();
           
           CallExpression callExpression = (CallExpression) expression;
           Arguments args= callExpression.getArguments();
           assert args != null;
           assert args.getArgumentsList().size() > 0 :"Expected to find at least" +
                " one argument";
           
           assert args.getArgumentsList().get( 0 ).getElementType().equals(
                   Variable.class ) :"Expected to find variable as argument," +
                        " but found : " + args.getArgumentsList().get( 0 ).getElementType();
                   
           IdentifierExpression identifierExpression = callExpression.getName();
           assert identifierExpression != null;
           assert identifierExpression.getElementType().equals( Constant.class )
               :"Expected to find constant function name, but found :" +
               identifierExpression.getElementType();
           
           assert identifierExpression.getText().equals( "unset" ) :
               "Expected to find 'unset' as text for function name , but found :" 
               + identifierExpression.getText();
       }
       finally {
           model.readUnlock();
       }
   }     
     
   public  void testNinthExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(8);
           assert statement instanceof ExpressionStatement;
           
           Expression expression = ((ExpressionStatement)statement).getExpression();
           assert expression.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression , but found : " +
               expression.getElementType();

           BinaryExpression binaryExpression = ( BinaryExpression )expression ;
           Expression left = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();
           
           assert left != null;
           assert left.getElementType().equals( Variable.class ) : 
                "Expected to find variable in left operand of assignment" +
                " expression , but found :"  +  left.getElementType();
           
           assert right != null;
           assert right.getElementType().equals( NewExpression.class ) :
               "Expected to find new expression in right operand of" +
               " assignment expression , but found :" + right.getElementType();
           
           NewExpression newExpression = (NewExpression) right;
           assert ! newExpression.isByReference() :"Unexpexted reference in new" +
                " expression";
           Arguments args = newExpression.getArguments();
           assert args != null;
           assert args.getElementType().equals( Arguments.class ) : "Expected" +
                " arguments type in new expression , but found : " +
                args.getElementType();
           List<Expression> argums = args.getArgumentsList();
           assert argums.size() > 0 :"Expected at least one argument in new " +
                "expression";
           expression = argums.get( 0 );
           assert expression != null;
           assert expression.getElementType().equals( Variable.class ) : 
                "Expected to fund variable as argument in new expression, " +
                "but found : " + expression.getElementType();

           Reference<ClassDefinition> ref = newExpression.getClassName();
           assert ref != null;
           assert ref.getIdentifier().equals( "Class" ) :"Expected 'Class' " +
                "identifier for class name in new expression, but found :" +
                ref.getIdentifier();
       }
       finally {
           model.readUnlock();
       }
   }    
   
   public  void testTenthExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(9);
           assert statement instanceof ExpressionStatement;
           
           Expression expression = ((ExpressionStatement)statement).getExpression();
           
           assert expression.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression , but found : " +
               expression.getElementType();

           BinaryExpression binaryExpression = ( BinaryExpression )expression ;
           Expression left = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();
           
           assert binaryExpression.getOperator().equals("instanceof"):
               "Expected instanceof operator but found :" + 
               binaryExpression.getOperator();
           
           assert left.getElementType().equals( Variable.class ) :"Expected " +
                "variable as left expression in instanceof operator, but found:"+
                left.getElementType();
           assert right.getElementType().equals( Variable.class ) :"Expected " +
               "variable as right expression in instanceof operator, but found:"+
               left.getElementType();

       }
       finally {
           model.readUnlock();
       }
   }    
   
   public  void testEleventhExpression() throws Exception{
       PhpModel model = getModel( ResourceMarker.EXPRESSION );
       model.sync();
       model.readLock();
       try {
           Statement statement = model.getStatements().get(10);
           assert statement instanceof ExpressionStatement;
           
           Expression expression = ((ExpressionStatement)statement).getExpression();
           
           assert expression.getElementType().equals( BinaryExpression.class ) :
               "Expected to find binary expression , but found : " +
               expression.getElementType();

           BinaryExpression binaryExpression = ( BinaryExpression )expression ;
           Expression left = binaryExpression.getLeftOperand();
           Expression right = binaryExpression.getRightOperand();
           
           assert binaryExpression.getOperator().equals("instanceof"):
               "Expected instanceof operator but found :" + 
               binaryExpression.getOperator();
           
           assert left.getElementType().equals( Variable.class ) :"Expected " +
                "variable as left expression in instanceof operator, but found:"+
                left.getElementType();
           assert right.getElementType().equals( Constant.class ) :"Expected " +
               "constant as right expression in instanceof operator, but found:"+
               left.getElementType();

       }
       finally {
           model.readUnlock();
       }
   }  
}
