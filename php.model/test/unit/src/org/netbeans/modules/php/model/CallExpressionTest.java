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
 * Contributor(s): The Original Software is NetBeans. The Initial
 * Developer of the Original Software is Sun Microsystems, Inc. Portions
 * Copyright 1997-2006 Sun Microsystems, Inc. All Rights Reserved.
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
 */
public class CallExpressionTest extends BaseCase {

    public void testEcho() throws Exception {
        PhpModel model = getModel(ResourceMarker.CALL_EXPRESSION);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(0);
            
            CallExpression expression = getCallExpression(statement);

            checkLiteralAndExpressionArgs(expression , "echo" , "\"a\"", "1+1");
        }
        finally {
            model.readUnlock();
        }
    }

    public void testDefine() throws Exception {
        PhpModel model = getModel(ResourceMarker.CALL_EXPRESSION);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(1);
            
            CallExpression expression = getCallExpression(statement);

            checkLiteralAndExpressionArgs(expression, "define", "\"MACRO\"" , 
                    "1 + 2");
        }
        finally {
            model.readUnlock();
        }
    }

    public void testInclude() throws Exception {
        PhpModel model = getModel(ResourceMarker.CALL_EXPRESSION);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(2);

            CallExpression expression = getCallExpression(statement);

            checkLiteralArg(expression, "include", "\"a.php\"");
        }
        finally {
            model.readUnlock();
        }
    }

    public void testEval() throws Exception {
        PhpModel model = getModel(ResourceMarker.CALL_EXPRESSION);
        model.sync();
        model.readLock();
        try {
            Statement statement = model.getStatements().get(3);

            CallExpression expression = getCallExpression(statement);

            checkLiteralArg(expression, "eval", "\"a\"");
        }
        finally {
            model.readUnlock();
        }
    }

    private void checkLiteralArg( CallExpression expression, String funcName,
            String argValue )
    {
        IdentifierExpression id = expression.getName();
        assert id instanceof Constant;
        assert id.getElementType().equals(Constant.class);

        Arguments args = expression.getArguments();
        assert args != null;
        assert args.getElementType().equals(Arguments.class);

        List<Expression> expressions = args.getArgumentsList();
        assert expressions.size() == 1 : "Expected to find 1 argument in echo"
                + " statement , but found :" + expressions.size();

        Expression expr = expressions.get(0);
        assert expr != null;

        assert expr instanceof Literal;
        assert expr.getElementType().equals(Literal.class) : "Expected to find "
                + " literal expression as argument, but found :"
                + expr.getElementType();

        assert id.getText().equals(funcName) : "Expected function name :'"
                + funcName + "' , but found :" + id.getText();

        assert expr.getText().equals(argValue) : "Expected argument text :+"
                + argValue + " but found : " + expr.getText();
    }

    private void checkLiteralAndExpressionArgs( CallExpression expression,
            String funcName, String firstArg, String secondArg )
    {
        IdentifierExpression id = expression.getName();
        assert id instanceof Constant;
        assert id.getElementType().equals(Constant.class);

        Arguments args = expression.getArguments();
        assert args != null;
        assert args.getElementType().equals(Arguments.class);

        List<Expression> expressions = args.getArgumentsList();
        assert expressions.size() == 2 : "Expected to find 2 arguments in call expression"
                + " statement , but found :" + expressions.size();

        Expression first = expressions.get(0);
        assert first != null;
        assert first.getElementType().equals(Literal.class) : "Expected to "
                + "find literal as first argument , but found : "
                + first.getElementType();

        Expression second = expressions.get(1);
        assert second != null;
        assert second instanceof BinaryExpression;
        assert second.getElementType().equals(BinaryExpression.class) : 
            "Expected to find binary expression , but found "
                + second.getElementType();
        
        assert id.getText().equals( funcName ) : "Expected to find function " +
                "name :'"+funcName+"' , but found :" +id.getText();
        
        assert first.getText().equals( firstArg ): "Expected to find first arg " +
                "with value :" +firstArg+" , but found " +first.getText();
        assert second.getText().equals( secondArg ): "Expected to find second arg " +
            "with value :" +secondArg+" , but found " +second.getText();
    }

    private CallExpression getCallExpression( Statement statement ) {
        assert statement instanceof ExpressionStatement;

        ExpressionStatement expressionStatement = (ExpressionStatement) statement;
        Expression expression = expressionStatement.getExpression();
        assert expression instanceof CallExpression : "Expected call expression ," +
                "but found :" +expression.getElementType();

        assert expression.getElementType().equals(CallExpression.class) : 
            "Expected to find Call expression , but found :"
                + expression.getElementType();

        return (CallExpression) expression;
    }

}
