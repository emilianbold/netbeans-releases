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
public class ForTest extends BaseCase {

    public void testFor() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<ForStatement> children = model.getStatements( 
                    ForStatement.class );
            assert children.size() > 0 : "Expected at least one for statement";
            ForStatement forr = children.get( 0 );
            
            assert !forr.isAlternative() : "For recognized as alternative, " +
                    "but shouldn't";
            
            assert forr.getFor()!= null: "Expected not null 'for' expression";
            assert forr.getStatements().size() == 1 :"Expected exactly one statement " +
                    " body for 'for', found :" +forr.getStatements().size();
            Statement statement = forr.getStatements().get( 0 );
            assert statement instanceof Block : "Expected Block as " +
                    "body statement in for";
            
            assert statement.getElementType().equals( Block.class ); 
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testForBody() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<ForStatement> children = model.getStatements( 
                    ForStatement.class );
            ForStatement forr = children.get( 0 );
            Statement statement = forr.getStatements().get( 0 );
            Block block = (Block) statement;
            List<Statement> list = block.getStatements();
            
            assert list.size() > 0 : "Expected at least one statement in " +
                    " for body block";
            
            assert list.get( 0 ) instanceof ExpressionStatement : "Expected expression " +
                    "statement, but found : " + list.get( 0 );
            assert list.get( 0 ).getElementType().equals( ExpressionStatement.class );
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testForHead() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<ForStatement> children = model.getStatements( 
                    ForStatement.class );
            ForStatement forr = children.get( 0 );
            For foR = forr.getFor();
            
            assert foR.getElementType().equals( For.class ) : "Expected to find" +
                    " For type , but found :" +foR.getElementType();
            
            ForExpression first = foR.getInitialExpression();
            ForExpression second = foR.getConditionalExpression();
            ForExpression last  = foR.getTerminalExpression();
            
            assert first != null :"Expected to find initail expression in For";
            assert second != null :"Expected to find conditional expression in For";
            assert last != null :"Expected to find last (incremental) " +
                    "expression in For";
            
            assert first.getElementType().equals( ForExpression.class ) :
                "Expected to find forExpression as initial expression in For, " +
                "but found :" +first.getElementType();
            assert second.getElementType().equals( ForExpression.class ) :
                "Expected to find forExpression as conditional expression in For, " +
                "but found :" +second.getElementType();
            assert last.getElementType().equals( ForExpression.class ) :
                "Expected to find forExpression as last ( incremental ) " +
                "expression in For, but found :" +last.getElementType();
            
            checkInitialExpression(first);
            
            checkConditionalExpression(second);
            
            checkTerminalExpression(last);
        }
        finally {
            model.readUnlock();
        }
    }
    
    private void checkTerminalExpression( ForExpression terminal ) {
        List<Expression> expressions;
        Expression expr;
        expressions = terminal.getExpressions();
        assert expressions.size() > 0 : "Expected to find at least one " +
                "expression in last (incremental) expression ";
        expr = expressions.get( 0 );
        assert expr != null;
        
        assert expr.getElementType().equals( UnaryExpression.class ):
            "Expected to find unary expression as last For expression ," +
            "but found :" +expr.getElementType();
        
        assert expr.getText().equals( "$i++" ) :"Expected to find text '$i<0'" +
            " in last (incremental) expression , but found :"+expr.getText();
        
    }

    private void checkConditionalExpression( ForExpression second ) {
        List<Expression> expressions;
        Expression expr;
        expressions = second.getExpressions();
        assert expressions.size() > 0 : "Expected to find at least one " +
                "expression in conditional expression ";
        expr = expressions.get( 0 );
        assert expr != null;
        
        assert expr.getElementType().equals( BinaryExpression.class ) :
            "Expected to find binary expression as conditional For expression ," +
            "but found :" +expr.getElementType();
        assert expr.getText().equals( "$i<0" ) :"Expected to find text '$i<0'" +
            " in conditional expression , but found :"+expr.getText();
    }

    private void checkInitialExpression( ForExpression first ) {
        List<Expression> expressions = first.getExpressions();
        assert expressions.size() > 0 : "Expected to find at least one " +
                "expression in incremental expression ";
        Expression expr = expressions.get( 0 );
        assert expr != null;
        
        assert expr.getElementType().equals( BinaryExpression.class ) :
            "Expected to find binary expression as initial For expression ," +
            "but found :" +expr.getElementType();;
        assert expr.getText().equals( "$i=0" ) :"Expected to find text '$i=0'" +
                " in initial expression , but found :"+expr.getText();
    }
}
