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


/**
 * @author ads
 *
 */
public class IfTest extends BaseCase {

    public void testIf() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<IfStatement> list = model.getStatements( IfStatement.class );
            assert list.size() >0 : "Expected to find at least one if statement";
            IfStatement statement = list.get( 0 );
            
            assert statement.getIf()!= null ;
            assert statement.getIf().getElementType().equals( If.class ) :
                "Expected If class, found " + statement.getIf().getElementType();
            
            assert statement.getElse()!= null;
            assert statement.getElse().getElementType().equals( Else.class ) :
                "Expected Else class , found : " + statement.getElse().getElementType();
            
            assert statement.getElseIfs().size() > 0 : "Expected at least one " +
                    "else-if child in if";
            List<ElseIf> children = statement.getElseIfs();
            ElseIf elseIf = children.get( 0 );
            assert elseIf.getElementType().equals( ElseIf.class ) :
                "Expected ElseIf class , found :" +elseIf.getElementType() ;
            
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testIfHead() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<IfStatement> list = model.getStatements( IfStatement.class );
            IfStatement statement = list.get( 0 );
            
            Expression expression = statement.getIf().getExpression();
            assert expression != null;
            assert expression.getElementType().equals(Literal.class) : 
                "Expected literal conditional if expression , but found :"
                    + expression.getElementType();

            Literal literal = (Literal) expression;
            assert literal.getText().equals("1") : "Expected text in literal is '1',"
                    + "but found :" + literal.getText();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testIfBlock() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<IfStatement> list = model.getStatements( IfStatement.class );
            IfStatement statement = list.get( 0 );
            List<Statement> statements = statement.getStatements();
            assert statements.size() == 1: "Expected exactly one statement in if," +
                    " found : " +statements.size();
            
            Statement stat = statements.get( 0 );
            assert stat instanceof Block :"Expected Block, but found "+
                    stat.getClass();
            assert stat.getElementType().equals( Block.class );
            Block block = (Block) stat;
            statements = block.getStatements();
            assert statements.size() > 0 : "Expected at least one statement " +
                    "inside if body";
            stat = statements.get( 0 );
            
            assert stat instanceof ExpressionStatement : "Expected expression ," +
                    " but found " +stat.getClass();
            assert stat.getElementType().equals( ExpressionStatement.class )
                    :"Expected Expression statement, but found :" + stat.getElementType(); 
            
            Expression expression = ((ExpressionStatement)stat).getExpression();
            assert expression!= null;
            assert expression.getElementType().equals( BinaryExpression.class ) :
                "Expected binary expression in if block expression , but found :" +
                expression.getElementType();
            
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testElseIfBlock() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<IfStatement> list = model.getStatements( IfStatement.class );
            IfStatement statement = list.get( 0 );
            List<ElseIf> children = statement.getElseIfs();
            assert children.size() == 1: "Expected exactly one elseif in if," +
                    " found : " +children.size();
            
            ElseIf elseIf = children.get( 0 );
            assert elseIf.getElementType().equals( ElseIf.class ) :
                "Expected ElseIf , but found :" + elseIf.getElementType();
            List<Statement> statements = elseIf.getStatements();
            assert statements.size() == 1 : "Expected exactly one statement " +
                    "inside elseif body";
            Statement stat = statements.get( 0 );
            
            assert stat instanceof Block : "Expected block ," +
                    " but found " +stat.getClass();
            assert stat.getElementType().equals( Block.class ):
                "Expected to find block, found :" +stat.getElementType();
            
            Block block = (Block)stat;
            statements = block.getStatements();
            assert statements.size() >0 : "Expexcted to find at least one " +
                    "statement inside elseif block";
            
            stat = statements.get(0);
            assert stat instanceof ExpressionStatement: "Expected expression " +
                    "statement, found :" +stat.getClass();
            
            assert stat.getElementType().equals( ExpressionStatement.class );
            
            Expression expression = elseIf.getExpression();
            assert expression != null;
            assert expression.getElementType().equals(Literal.class) : 
                "Expected literal conditional ifelse expression , but found :"
                    + expression.getElementType();

            Literal literal = (Literal) expression;
            assert literal.getText().equals("1") : "Expected text in literal is '1',"
                    + "but found :" + literal.getText();
            
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testElseBlock() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<IfStatement> list = model.getStatements( IfStatement.class );
            IfStatement statement = list.get( 0 );
            Else els = statement.getElse();
            
            assert els.getElementType().equals( Else.class ) :
                "Expected Else , but found :" + els.getElementType();
            List<Statement> statements = els.getStatements();
            assert statements.size() == 1 : "Expected exactly one statement " +
                    "inside else body";
            Statement stat = statements.get( 0 );
            
            assert stat instanceof Block : "Expected block ," +
                    " but found " +stat.getClass();
            assert stat.getElementType().equals( Block.class ):
                "Expected to find block, found :" +stat.getElementType();
            
            Block block = (Block)stat;
            statements = block.getStatements();
            assert statements.size() >0 : "Expexcted to find at least one " +
                    "statement inside else block";
            
            stat = statements.get(0);
            assert stat instanceof ExpressionStatement: "Expected expression " +
                    "statement, found :" +stat.getClass();
            
            assert stat.getElementType().equals( ExpressionStatement.class );
            
            Expression expression = ((ExpressionStatement)stat).getExpression();
            assert expression != null :"Expected expression in else block";
            
            assert expression.getElementType().equals( UnaryExpression.class ) :
                "Expected to find unary expression in else block but found :" +
                expression.getElementType();
            
            UnaryExpression unary = (UnaryExpression) expression ;
            Expression operand = unary.getOperand();
            
            assert operand != null :"Expected to find operand in unary expression " +
                    "in else block";
            
            assert operand.getElementType().equals( Variable.class ) :
                "Expected to find varaible as operand in uanry expression , " +
                "but found :" +operand.getElementType();
            
            assert operand.getText().equals( "$e" ) :"Expected text in " +
                    "unary expression operand '$e' , but found :" +operand.getText();
        }
        finally {
            model.readUnlock();
        }
    }
}
