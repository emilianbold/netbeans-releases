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
public class VariableTest extends BaseCase {

    public void testSimpleVar() throws Exception {
        PhpModel model = getModel( ResourceMarker.VARIABLE );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements( ).get( 0 );
            
            assert statement.getElementType().equals( ExpressionStatement.class ):
                "Expected to find expression statement, but found :" +
                statement.getElementType();
            
            ExpressionStatement expressionStatement = 
                (ExpressionStatement) statement;
            Expression expression = expressionStatement.getExpression();
            
            assert expression != null;
            assert expression.getElementType().equals( Variable.class ):
                "Expected to find varaible type , but found  :" +
                expression.getElementType();
            
            Variable variable = (Variable) expression;
            Expression name = variable.getName();
            assert name != null;
            assert name.getElementType().equals( Literal.class ) :"Expected to " +
                    "find literal as variable name, but found :" +name.getElementType();
            
            assert name.getText().equals("$var"):"Expected to find '$var' as" +
                    " variable text, but found :" +name.getText();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testVarVar() throws Exception {
        PhpModel model = getModel( ResourceMarker.VARIABLE );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements( ).get( 1 );
            
            assert statement.getElementType().equals( ExpressionStatement.class ):
                "Expected to find expression statement, but found :" +
                statement.getElementType();
            
            ExpressionStatement expressionStatement = 
                (ExpressionStatement) statement;
            Expression expression = expressionStatement.getExpression();
            
            assert expression != null;
            assert expression.getElementType().equals( Variable.class ):
                "Expected to find varaible type , but found  :" +
                expression.getElementType();
            
            Variable variable = (Variable) expression;
            Expression name = variable.getName();
            assert name != null;
            assert name.getElementType().equals( Literal.class ) :"Expected to " +
                    "find literal as variable name, but found :" +name.getElementType();
            
            assert name.getText().equals("$$var"):"Expected to find '$var' as" +
                    " variable text, but found :" +name.getText();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testComplexVar() throws Exception {
        PhpModel model = getModel( ResourceMarker.VARIABLE );
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements( ).get( 2 );
            
            assert statement.getElementType().equals( ExpressionStatement.class ):
                "Expected to find expression statement, but found :" +
                statement.getElementType();
            
            ExpressionStatement expressionStatement = 
                (ExpressionStatement) statement;
            Expression expression = expressionStatement.getExpression();
            
            assert expression != null;
            assert expression.getElementType().equals( Variable.class ):
                "Expected to find varaible type , but found  :" +
                expression.getElementType();
            
            Variable variable = (Variable) expression;
            Expression name = variable.getName();
            assert name != null;
            assert name.getElementType().equals( BinaryExpression.class ) :
                "Expected to find binary expression as variable name, but found :" 
                +name.getElementType();
            
            BinaryExpression binaryExpression = (BinaryExpression) name;
            assert binaryExpression.getOperator().equals( "." ) :"Expected" +
                    " to find '.' operator in variable name, but found :" +
                    binaryExpression.getOperator();
            Expression left= binaryExpression.getLeftOperand();
            Expression right = binaryExpression.getRightOperand();
            
            assert left != null;
            assert left.getElementType().equals( Literal.class ) :
                "Expected to find literal as left operand , but found :" +
                left.getElementType();
            assert right.getElementType().equals( Variable.class ) :"Expected to " +
                    "find variable as right operand , but found :" +
                    right.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }
}
