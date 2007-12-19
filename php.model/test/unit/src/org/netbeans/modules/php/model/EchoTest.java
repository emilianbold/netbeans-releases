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
public class EchoTest extends BaseCase {

    public void testEchoStatement () throws Exception  {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            ExpressionStatement statement = 
                model.getStatements( ExpressionStatement.class ).get(0);
            Expression expression = statement.getExpression();
            assert expression != null :
                "Expected to find expression in expression statement";  // NOI18N
            assert expression.getElementType().equals( CallExpression.class ):
                "Expected to find call expression , but found : " +
                expression.getElementType();
            
            CallExpression callExpression = (CallExpression) expression;
            
            IdentifierExpression id = callExpression.getName();
            assert id != null;
            
            Arguments args = callExpression.getArguments();
            assert args != null;
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testEchoName () throws Exception  {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            ExpressionStatement statement = 
                model.getStatements( ExpressionStatement.class ).get(0);
            Expression expression = statement.getExpression();
            
            CallExpression callExpression = (CallExpression) expression;
            
            IdentifierExpression id = callExpression.getName();
            assert id.getElementType().equals( Constant.class );
            
            Constant cnst = (Constant) id;
            assert cnst.getText().equals( "echo" ) :
                "Expected 'echo' constant name for function call expression, " +
                "but found : '"+cnst.getText()+"'"; // NOI18N
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testEchoArgs () throws Exception  {
        PhpModel model = getModel();
        model.sync();
        model.readLock();
        try {
            ExpressionStatement statement = 
                model.getStatements( ExpressionStatement.class ).get(0);
            Expression expression = statement.getExpression();
            
            CallExpression callExpression = (CallExpression) expression;
            
            Arguments args = callExpression.getArguments();
            List<Expression> list = args.getArgumentsList();
            assert list.size() == 1 : "Expected exactly one echo argument, " +
                    "but found :" + list.size();
            Expression expr = list.get(0);
            
            assert expr != null;
            assert expr.getElementType().equals( Literal.class ) :
                "Expected to find literal , but found : " +expr.getElementType() ;
            
            Literal literal = (Literal)expr;
            assert literal.getText().equals( "\"a\"" ) : 
                "Expected to find literal \"a\" but found " +literal.getText();
            
        }
        finally {
            model.readUnlock();
        }
    }
}
