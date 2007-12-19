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
 * @author ads
 *
 */
public class MemberTest extends BaseCase {

    public void testConstant() throws Exception{
        PhpModel model = getModel( ResourceMarker.MEMBER );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get( 0 );
            
            assert (( ExpressionStatement ) statement).getExpression().
                getElementType().equals( Constant.class ) :"Expected to find" +
                        " constant, but found : " + 
                        (( ExpressionStatement ) statement).getExpression().
                        getElementType();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testClassMethodCall() throws Exception{
        PhpModel model = getModel( ResourceMarker.MEMBER );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get( 1 );
            
            Expression expression = ((ExpressionStatement) statement).getExpression();
            assert expression.getElementType().equals( CallExpression.class ):
                "Expected to find call expression type , but found :" +
                expression.getElementType();
            
            CallExpression call = (CallExpression) expression;
            Arguments args = call.getArguments();
            
            assert args.getElementType().equals( Arguments.class ):"Expected to " +
                    "find arguments in call expression , but found : " +
                    args.getElementType();
            
            List<Expression> list = args.getArgumentsList();
            assert list.size() >0 :"Expected to find ad least one expression " +
                    "as argument, but found : " +list.size();
            
            expression = list.get( 0 );
            assert expression != null;
            assert expression.getElementType().equals( Variable.class ) :
                "Expected to find variable as argument in call expression , but" +
                " found :" + expression.getElementType();
            
            IdentifierExpression identifier = call.getName();
            assert identifier!= null;
            assert identifier.getElementType().equals( ClassMemberExpression.class ):
                "Expected  to find class memeber expression as name in call " +
                "expression , but found : " + identifier.getElementType();
            ClassMemberExpression member = (ClassMemberExpression) identifier;
            
            assert member.getCallExpression() == null;
            IdentifierExpression id =  member.getOwnerIdentifier() ;
            
            assert id != null;
            assert id.getElementType().equals( Variable.class ) : 
                "Exprected variable as class identifier in method call expression," +
                " but found : " +id.getElementType();
            
            assert id.getText().equals( "$clazz" ) :"Expected '$clazz' as text" +
                    " for class identifier , but found :" +id.getText();
            expression = member.getExpression();
            
            assert expression != null;
            assert expression.getElementType().equals( Constant.class ) :
                "Expected to find constant as type for class method name ," +
                " but found :" +expression.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testArrayMemberAccess() throws Exception{
        PhpModel model = getModel( ResourceMarker.MEMBER );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get( 2 );
            
            Expression expression = ((ExpressionStatement) statement).getExpression();
            assert expression.getElementType().equals( ArrayMemberExpression.class):
                "Expected to find array member expression type , but found :" +
                expression.getElementType();
            
            ArrayMemberExpression memberExpr = ( ArrayMemberExpression) expression;
            
            assert memberExpr.getCallExpression() == null;
            
            expression = memberExpr.getExpression();
            assert expression != null :"Expected not null expression in '[]'";
            
            assert expression.getElementType().equals( UnaryExpression.class ):
                "Expected to find unary (incremental ) expression in '[]', " +
                "but found  :" + expression.getElementType() ;
            
            assert expression.getText().equals( "$i++" ) :"Expected to find " +
                    "text '$i++' as text in '[]', but found : '" +expression.getText()+
                    "'";
            
            IdentifierExpression id = memberExpr.getOwnerIdentifier();
            assert id != null;
            assert id.getElementType().equals( Variable.class ):"Expected to find" +
                    " variable as array identifier, but found :" + id.getElementType();
            assert id.getText().equals( "$a" ) :"Expected to find  '$a' as " +
                    " array identifier text , but found :" +id.getText();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testFunctionCall() throws Exception{
        PhpModel model = getModel( ResourceMarker.MEMBER );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get( 3 );
            
            Expression expression = ((ExpressionStatement) statement).getExpression();
            assert expression.getElementType().equals( CallExpression.class );
            
            CallExpression call = (CallExpression) expression;
            Arguments args = call.getArguments();
            
            assert args!= null;
            assert args.getElementType().equals( Arguments.class ) :"Expected" +
                    " to find arguments type , but found :" + args.getElementType();
            assert args.getArgumentsList().size() > 0 :"Expected to find at least " +
                    " one argument";
            
            IdentifierExpression identifier = call.getName();
            
            assert identifier!= null;
            assert identifier.getElementType().equals( Constant.class ) :
                "Expeced to find constant function name , but found : " +
                identifier.getElementType();
            assert identifier.getText().equals( "func" ) : "Expected to find 'func'" +
                    " as function name , but found :" + identifier.getText();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testAdditiveExpression() throws Exception{
        PhpModel model = getModel( ResourceMarker.MEMBER );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get( 4 );
            
            Expression expression = ((ExpressionStatement) statement).getExpression();
            assert expression.getElementType().equals( BinaryExpression.class );
            
            BinaryExpression binaryExpression = (BinaryExpression) expression;
            
            Expression left = binaryExpression.getLeftOperand();
            Expression right = binaryExpression.getRightOperand();
            
            assert left.getElementType().equals( CallExpression.class ) : 
                "Expected to find call expression as first operand in additive" +
                " expression , but found :" +left.getElementType();
            
            assert right.getElementType().equals( BinaryExpression.class ) :
                "Expected to find binary expression as right " +
                " operand in additive expression , but found : " +right.getElementType();
            
            binaryExpression = (BinaryExpression) right;
            
            left = binaryExpression.getLeftOperand();
            right = binaryExpression.getRightOperand();
            
            assert left.getElementType().equals( CallExpression.class ) : 
                "Expected to find call expression as second operand in additive" +
                " expression , but found :" +left.getElementType();
            
            assert right.getElementType().equals( ArrayMemberExpression.class ) :
                "Expected to find array memeber expression as third operand in" +
                " additive expression, but found : " +right.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }    
    
    public void testAttributeAccess() throws Exception {
        PhpModel model = getModel(ResourceMarker.MEMBER);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(5);

            Expression expression = ((ExpressionStatement) statement)
                    .getExpression();
            assert expression.getElementType().equals( ClassMemberExpression.class);
            
            ClassMemberExpression member = (ClassMemberExpression) expression;
            
            Expression attribute = member.getExpression();
            assert attribute!= null ;
            assert attribute.getElementType().equals( Variable.class ) :"Expected" +
                    " to find variable as class member, but found :" + 
                    attribute.getElementType();
            
            IdentifierExpression identifierExpression = member.getOwnerIdentifier();
            assert identifierExpression!= null;
            assert identifierExpression.getElementType().equals( Variable.class ) :
                "Expected to find variable as class identifier, but found :" +
                identifierExpression.getElementType();
        }
        finally {
            model.readUnlock();
        }
    } 
    
    public void testArrayAndClassAccess() throws Exception {
        PhpModel model = getModel(ResourceMarker.MEMBER);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(6);

            Expression expression = ((ExpressionStatement) statement)
                    .getExpression();
            assert expression.getElementType().equals( ArrayMemberExpression.class);
            
            ArrayMemberExpression member = (ArrayMemberExpression) expression;
            expression = member.getExpression();
            assert expression!= null;
            assert expression.getElementType().equals( Literal.class ) :"Expected" +
                    " to find literal inside '[]' but found :" +  expression.getElementType();
            
            assert member.getCallExpression() == null;
            
            IdentifierExpression id = member.getOwnerIdentifier();
            assert id != null :"Expected to find array identifier";
            assert id.getElementType().equals( ClassMemberExpression.class );
            
            ClassMemberExpression classMember = (ClassMemberExpression) id;
            expression = classMember.getExpression();
            assert expression != null;
            assert expression.getElementType().equals( Variable.class ) : 
                "Expected to find variable type in member expression , but found :"
                + expression.getElementType();
            
            
            id = classMember.getOwnerIdentifier();
            assert id != null :"Expected to find class identifier";
            assert id.getElementType().equals( Variable.class ) :"Expected to " +
                "find variable as class identifier , but found :" + id.getElementType();
            
            assert id.getText().equals( "$clazz" ) :"Expected to find '$clazz' as" +
                    " text for class identifier, but found : " + id.getText();
        }
        finally {
            model.readUnlock();
        }
    } 
    
    
    public void testMixedMemberAccess() throws Exception {
        PhpModel model = getModel(ResourceMarker.MEMBER);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(7);

            Expression expression = ((ExpressionStatement) statement)
                    .getExpression();
            assert expression.getElementType().equals( CallExpression.class);
            
            CallExpression callExpression = (CallExpression) expression;
            IdentifierExpression identifierExpression = callExpression.getName();
            assert identifierExpression!= null;
            assert identifierExpression.getElementType().equals( 
                    ArrayMemberExpression.class ) : "Expected to find array member" +
                            " expression as name for call expression , but found :" +
                            identifierExpression.getElementType();
                    
            ArrayMemberExpression memberExpression = 
                (ArrayMemberExpression) identifierExpression;
            expression = memberExpression.getExpression();
            assert expression != null ;
            assert expression.getElementType().equals( Variable.class ) :
                "Expected to find variable in '[]' , but found :" +
                expression.getElementType();
            
            assert expression.getText().equals( "$i" ) : "Expeced to find '$i'" +
                    " as expression text in '[]' , but found :" +expression.getText();
            
            assert memberExpression.getCallExpression() == null;
            
            identifierExpression = memberExpression.getOwnerIdentifier();
            assert identifierExpression!= null :"Expected identifier expression" +
                    " as owner for array";
            assert identifierExpression.getElementType().equals( 
                    ClassMemberExpression.class ) :"Expected class member expression " +
                            " type as owner for array, but found : " + 
                            identifierExpression.getElementType();
                    /*
                     * class member is '$clazz->op( $arg )->method'
                     */
            ClassMemberExpression classMemberExpression = 
                (ClassMemberExpression) identifierExpression;
            
            assert classMemberExpression.getOwnerIdentifier() == null :
                "Unexpected owner identifier in class member expression";
            
            
            callExpression = classMemberExpression.getCallExpression();
            assert callExpression != null :"Expected to find call expression" +
                    " as identifier for member access";
            assert callExpression.getElementType().equals( CallExpression.class ) :
                "Expected to find call expression as identifier expression " +
                "in class memeber access , but found : " + callExpression.getElementType();
            
            expression = classMemberExpression.getExpression();
            assert expression != null :"Expected to find expression for method name";
            assert expression.getElementType().equals( Constant.class ) :"Expected" +
                    " to find constant type for class method name , but found :" +
                    expression.getElementType();
            
            /*
             * call expression is $clazz->op( $arg )
             */
            Arguments args = callExpression.getArguments();
            assert args != null;
            
            assert args.getArgumentsList().size() > 0;
            assert args.getArgumentsList().get( 0 ) != null;
            
            identifierExpression = callExpression.getName();
            assert identifierExpression != null :"Expected to find identifier " +
                    "expression for first call expression";
            assert identifierExpression.getElementType().equals( ClassMemberExpression.class ):
                "Expected to find class member expression type as identifier type," +
                "but found : "  + identifierExpression.getElementType();
            
            identifierExpression = callExpression.getName();
            
            assert identifierExpression != null:"Expected class identifier";
            assert identifierExpression.getElementType().equals( ClassMemberExpression.class ):
                "Expected to find class member expression type for function call " +
                "identifier, but found :"+
                identifierExpression.getElementType();
            
            classMemberExpression = (ClassMemberExpression)identifierExpression;
            assert classMemberExpression.getCallExpression() == null;
            
            identifierExpression = classMemberExpression.getOwnerIdentifier();
            assert identifierExpression != null;
            assert identifierExpression.getElementType().equals( Variable.class ):
                "Expected to find variable as class identifier , but found :"+
                identifierExpression.getElementType();
            
            expression = classMemberExpression.getExpression();
            assert expression != null;
            assert expression.getElementType().equals( Constant.class ) :
                "Expected constant type for class method name , but found :" +
                expression.getElementType();
        }
        finally {
            model.readUnlock();
        }
    } 
    
    
    public void testVariableMethod() throws Exception {
        PhpModel model = getModel(ResourceMarker.MEMBER);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(8);

            Expression expression = ((ExpressionStatement) statement)
                    .getExpression();
            assert expression.getElementType().equals( CallExpression.class);
            
            CallExpression callExpression = (CallExpression) expression;
            Arguments args = callExpression.getArguments();
            
            assert args != null;
            assert args.getArgumentsList().size() == 0 :"Unexpected " +
                args.getArgumentsList().size()+" arguments";
            
            IdentifierExpression id = callExpression.getName();
            assert id != null;
            assert id.getElementType().equals( ClassMemberExpression.class  ):
                "Expected to find ClassMemberExpression, but found :" +
                id.getElementType();
            
            ClassMemberExpression classMember = (ClassMemberExpression) id;
            expression = classMember.getExpression();
            assert expression != null;
            assert expression.getElementType().equals( Variable.class );
            Variable variable = (Variable)expression;
            
            expression = variable.getName();
            assert expression != null;
            assert expression.getElementType().equals( BinaryExpression.class ) :
                "Expected to find binary expression inside {} but found :" +
                expression.getElementType();
            
            BinaryExpression binaryExpression = (BinaryExpression) expression ;
            
            assert binaryExpression.getOperator().equals( "." ) :"Expected to " +
                    "find operator '.' , but found :" + binaryExpression.getOperator();
            
            Expression left = binaryExpression.getLeftOperand();
            Expression right = binaryExpression.getRightOperand();
            
            assert left.getElementType().equals( Literal.class ):"Expected to " +
                    "find Literal as first operand inside {} , but found :" +
                    left.getElementType();
            assert right.getElementType().equals( Variable.class ) :"Expected to " +
                    "find Variable as second operand inside {}, but found :" +
                    right.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }      
}
