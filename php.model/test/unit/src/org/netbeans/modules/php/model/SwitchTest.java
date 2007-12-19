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
public class SwitchTest extends BaseCase {

    public void testSwitch() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<SwitchStatement> list = model.getStatements( SwitchStatement.class );
            assert list.size() >0 : "Expected to find at least one switch statement";
            SwitchStatement statement = list.get( 0 );
            
            assert statement.getSwitch()!= null ;
            assert statement.getSwitch().getElementType().equals( Switch.class ) :
                "Expected Switch class, found " + statement.getSwitch().getElementType();
            
            assert statement.getDefault()!= null;
            assert statement.getDefault().getElementType().equals( Default.class ) :
                "Expected Default class , found : " + statement.getDefault().getElementType();
            
            assert statement.getCases().size() > 0 : "Expected at least one " +
                    "case child in if";
            List<Case> children = statement.getCases();
            Case caze = children.get( 0 );
            assert caze.getElementType().equals( Case.class ) :
                "Expected Case class , found :" +caze.getElementType() ;
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testSwitchCondition() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<SwitchStatement> list = model.getStatements( SwitchStatement.class );
            SwitchStatement statement = list.get( 0 );
            
            Switch switc = statement.getSwitch();
            Expression expression = switc.getExpression();
            assert expression!= null :"Expected to find expression in switch" +
                    " main expression";
            
            assert expression.getElementType().equals( Variable.class );
            Variable var = (Variable)expression;
            assert var.getText().equals( "$d" ) :"Expected '$d' variable " +
                    "as switch expression , but found : " + var.getText();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testCase() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<SwitchStatement> list = model.getStatements( SwitchStatement.class );
            SwitchStatement statement = list.get( 0 );
            List<Case> cases = statement.getCases();
            assert cases.size() >0 : "Expected at least one statement in switch," +
                    " found : " +cases.size();
            
            Case caze = cases.get( 0 );
            assert caze.getElementType().equals( Case.class );
            List<Statement> statements = caze.getStatements();
            assert statements.size() > 0 : "Expected at least one statement " +
                    "inside case body";
            Statement stat = statements.get( 0 );
            
            assert stat instanceof BreakStatement : "Expected break statement ," +
                    " but found " +stat.getClass();
            assert stat.getElementType().equals( BreakStatement.class )
                    :"Expected Expression statement, but found :" + stat.getElementType(); 
            
            Expression expression = caze.getExpression();
            assert expression.getElementType().equals( Literal.class ):
                "Expected to find literal as case expression , but found :" 
                +expression.getElementType();
            
            statements = caze.getStatements();
            assert statements.size() > 0 :"Expected at least one statement in case";
            stat = statements.get( 0 );
            assert stat != null;
            
            assert stat.getElementType().equals( BreakStatement.class ) :
                "Expected to find break statement in case , but found :" + 
                stat.getElementType();
            
            BreakStatement breakStatement = (BreakStatement) stat;
            assert breakStatement.getText().equals("break;") : "Expected text " +
                    "'break;'  as break statement , but found :" +
                    breakStatement.getText();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testDefault() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<SwitchStatement> list = model.getStatements( SwitchStatement.class );
            SwitchStatement statement = list.get( 0 );
            Default def = statement.getDefault();
            assert def.getElementType().equals( Default.class ) : "Expected " +
                    " default element, found :" +def.getElementType();
            
            List<Statement> statements = def.getStatements();
            assert statements.size() > 0 : "Expected at least one statement " +
                    "inside default body";
            Statement stat = statements.get( 0 );
            
            assert stat instanceof ExpressionStatement : "Expected expression ," +
                    " but found " +stat.getClass();
            assert stat.getElementType().equals( ExpressionStatement.class )
                    :"Expected Expression statement, but found :" + stat.getElementType(); 
            
            assert stat.getText().equals(";") :"Expected to find expression " +
                    "statement text ';', but found :" +stat.getText();
            
            Expression expression = ((ExpressionStatement)stat).getExpression();
            assert expression == null :"Expected to find no epxression in empty " +
                    "expression inside default";
        }
        finally {
            model.readUnlock();
        }
    }   
    
    public void testEmptyCase() throws Exception {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            List<SwitchStatement> list = model.getStatements( SwitchStatement.class );
            SwitchStatement statement = list.get( 0 );
            List<Case> cases = statement.getCases();
            
            Case caze = cases.get(1 );
            assert caze != null:"Empty case not found";
            
            List<Statement> statements = caze.getStatements();
            assert statements.size() == 0:"Expected to find no statements in " +
                    "empty case , but found " +statements.size() +" statements";
        }
        finally {
            model.readUnlock();
        }
    }   
}
