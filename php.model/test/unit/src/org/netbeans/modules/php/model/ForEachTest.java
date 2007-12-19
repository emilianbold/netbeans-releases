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
public class ForEachTest extends BaseCase {

    public void testForEach() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<ForEachStatement> children = model.getStatements( 
                    ForEachStatement.class );
            assert children.size() > 0 : "Expected at least one foreach statement";
            ForEachStatement forEach = children.get( 0 );
            
            assert !forEach.isAlternative() : "Foreach recognized as alternative, " +
                    "but shouldn't";
            
            assert forEach.getForEach()!= null: "Expected not null for each expression";
            assert forEach.getStatements().size() == 1 :"Expected exactly one statement " +
                    " body for foreach, found :" +forEach.getStatements().size();
            Statement statement = forEach.getStatements().get( 0 );
            assert statement instanceof Block : "Expected Block as " +
                    "body statement in foreach";
            
            assert statement.getElementType().equals( Block.class ); 
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testForEachBody() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<ForEachStatement> children = model.getStatements( 
                    ForEachStatement.class );
            ForEachStatement forEach = children.get( 0 );
            Statement statement = forEach.getStatements().get( 0 );
            Block block = (Block) statement;
            List<Statement> list = block.getStatements();
            
            assert list.size() > 0 : "Expected at least one statement in " +
                    " foreach body block";
            
            assert list.get( 0 ) instanceof ExpressionStatement : "Expected expression " +
                    "statement, but found : " + list.get( 0 );
            assert list.get( 0 ).getElementType().equals( ExpressionStatement.class );
            
            ExpressionStatement stat = (ExpressionStatement)list.get( 0 );
            Expression expression = stat.getExpression();
            assert expression != null;
            assert expression.getElementType().equals( BinaryExpression.class ) :
                "Expected to find binary expression in foreach body, but found :"+
                expression.getElementType();
            
            BinaryExpression binary = (BinaryExpression) expression ;
            Expression left = binary.getLeftOperand();
            Expression right = binary.getRightOperand();
            
            assert left != null;
            assert right != null;
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testForEachHead() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<ForEachStatement> children = model.getStatements( 
                    ForEachStatement.class );
            ForEachStatement forEachStatement = children.get( 0 );
            ForEach forEach = forEachStatement.getForEach();
            
            assert forEach != null ;
            
            assert forEach.getElementType().equals( ForEach.class ) :
                "Expected to find for each type , but found :" +forEach.getElementType();
            
            VariableDeclaration index = forEach.getIndexVariable();
            VariableDeclaration value = forEach.getValueVariable();

            assert index == null: "Expected absence of index , but found index :" +index;

            assert value != null;
            assert value.getElementType().equals( VariableDeclaration.class ) :
                "Expected variable declaration class , but found :" + value.getElementType();
            
            Expression expression = forEach.getExpression();

            assert expression != null;
            assert expression.getElementType().equals( Variable.class ) :
                "Expected to find variable as expression in for each but found :"
                +expression.getElementType();
            
        }
        finally {
            model.readUnlock();
        }
    }
}
