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

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;


/**
 * @author ads
 *
 */
public class AssignExpressionTest extends BaseCase {

    public void testAssignmentExpression() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            ExpressionStatement statement = 
                model.getStatements( ExpressionStatement.class ).get( 2 );
            assert statement != null;
            
            Expression expression = statement.getExpression();
            assert expression != null :"Expected expression in expression statement";
            
            assert expression.getElementType().equals( BinaryExpression.class ) :
                "Expected binary expression for expression " +statement.getText() +
                "but found : " + expression.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testBinaryExpression() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            ExpressionStatement statement = 
                model.getStatements( ExpressionStatement.class ).get( 2 );
            
            Expression expression = statement.getExpression();
            
            BinaryExpression binary = (BinaryExpression) expression;
            Expression left = binary.getLeftOperand();
            Expression right = binary.getRightOperand();
            
            assert left != null :"Expected to find left operand in expression";
            assert left.getElementType().equals( Variable.class );
            
            assert right != null : "Expected to find right operand in expression";
            assert right.getElementType().equals( Literal.class );
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testLeftOperand() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            ExpressionStatement statement = 
                model.getStatements( ExpressionStatement.class ).get( 2 );
            
            Expression expression = statement.getExpression();
            
            BinaryExpression binary = (BinaryExpression) expression;
            Expression left = binary.getLeftOperand();
            
            Variable var = (Variable)left;
            String text = var.getText();
            assert text.equals( "$a" ) : "Expected '$a' as left operand , " +
                    "but found :" +text;
            
            TokenSequence sequence = var.getTokenSequence();
            Token token = null;
            int count = 0;
            while ( sequence.moveNext() ){
                token = sequence.token();
                count ++;
            }
            assert count == 1:"Expected to find only one token in left operand, " +
                    "but found :" +count;
            assert token != null:"Expected to find token in left operand";
            
            assert token.text().toString().equals( text ) : "Expected token :" +
                text+" , but found : "+token.text();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testRightOperand() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            ExpressionStatement statement = 
                model.getStatements( ExpressionStatement.class ).get( 2 );
            
            Expression expression = statement.getExpression();
            
            BinaryExpression binary = (BinaryExpression) expression;
            Expression left = binary.getRightOperand();
            
            Literal literal = (Literal)left;
            String text = literal.getText();
            assert text.equals( "1" ) : "Expected literal with value = 1 as right" +
                    " operand , but found :" +text;
            
            TokenSequence sequence = literal.getTokenSequence();
            Token token = null;
            int count = 0;
            while ( sequence.moveNext() ){
                token = sequence.token();
                count ++;
            }
            assert count == 1:"Expected to find only one token in right operand, " +
                    "but found :" +count;
            assert token != null:"Expected to find token in right operand";
            
            assert token.text().toString().equals( text ) : "Expected token :" +
                text+" , but found : "+token.text();

        }
        finally {
            model.readUnlock();
        }
    }
    
}
