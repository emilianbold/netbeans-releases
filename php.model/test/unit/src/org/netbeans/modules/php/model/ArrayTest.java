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
public class ArrayTest extends BaseCase {

    
    public void testArrayOne() throws Exception {
        PhpModel model = getModel( ResourceMarker.ARRAY );
        model.sync();
        model.readLock();
        try {
            List<Statement> statements = model.getStatements();
            assert statements.size() >0 :"Expected to find at least one statement";
            Statement statement = statements.get( 0 );
            assert statement instanceof ExpressionStatement : "Found "+
                statement.getElementType()+" instead of Expression statement";
            
            Expression expression = ((ExpressionStatement)statement).getExpression();
            assert expression != null;
            assert expression.getElementType().equals( ArrayExpression.class) :
                "Expected to find array expression m but found :" + expression.getElementType();
            ArrayExpression arrayExpression = (ArrayExpression)expression;
            
            List<ArrayDefElement> children = arrayExpression.getElements();
            assert children.size() == 1 :"Expected to find exactly 1  child, " +
            		"but found :"+ children.size(); 
            
            ArrayDefElement element = children.get(0);
            assert element.getElementType().equals( Literal.class ): "Expected to " +
            		"find literal as argument , but found :" +element.getElementType(); 
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testArrayTwo() throws Exception {
        PhpModel model = getModel( ResourceMarker.ARRAY );
        model.sync();
        model.readLock();
        try {
            List<Statement> statements = model.getStatements();
            assert statements.size() >1 :"Expected to find at least two statement";
            Statement statement = statements.get( 1 );
            assert statement instanceof ExpressionStatement : "Found "+
                statement.getElementType()+" instead of Expression statement";
            
            Expression expression = ((ExpressionStatement)statement).getExpression();
            assert expression != null;
            assert expression.getElementType().equals( ArrayExpression.class) :
                "Expected to find array expression m but found :" + expression.getElementType();
            ArrayExpression arrayExpression = (ArrayExpression)expression;
            
            List<ArrayDefElement> children = arrayExpression.getElements();
            assert children.size() == 2 :"Expected to find exactly 2  children, " +
                    "but found :"+ children.size(); 
            
            ArrayDefElement element = children.get(0);
            assert element.getElementType().equals( Literal.class ): "Expected to " +
                    "find literal as argument , but found :" +element.getElementType();
            
            element = children.get(1);
            assert element.getElementType().equals( Literal.class ): "Expected to " +
                    "find literal as argument , but found :" +element.getElementType(); 
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testArrayThree() throws Exception {
        PhpModel model = getModel( ResourceMarker.ARRAY );
        model.sync();
        model.readLock();
        try {
            List<Statement> statements = model.getStatements();
            assert statements.size() >2 :"Expected to find at least three statement";
            Statement statement = statements.get( 2 );
            assert statement instanceof ExpressionStatement : "Found "+
                statement.getElementType()+" instead of Expression statement";
            
            Expression expression = ((ExpressionStatement)statement).getExpression();
            assert expression != null;
            assert expression.getElementType().equals( ArrayExpression.class) :
                "Expected to find array expression m but found :" + expression.getElementType();
            ArrayExpression arrayExpression = (ArrayExpression)expression;
            
            List<ArrayDefElement> children = arrayExpression.getElements();
            assert children.size() == 2 :"Expected to find exactly 2  children, " +
                    "but found :"+ children.size(); 
            
            ArrayDefElement element = children.get(0);
            assert element.getElementType().equals( Literal.class ): "Expected to " +
                    "find literal as argument , but found :" +element.getElementType();
            
            element = children.get(1);
            assert element.getElementType().equals( AssociativeArrayElement.class ):
                "Expected to find AssociativeArrayElement, but found :" +
                element.getElementType();
            AssociativeArrayElement assocElement = (AssociativeArrayElement) element;
            assert assocElement.getKey() == null;
            assert assocElement.getValue() == null;
            assert assocElement.getText().trim().length() == 0 :"Expected to find" +
            		" empty string as last argument";
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testArrayFour() throws Exception {
        PhpModel model = getModel( ResourceMarker.ARRAY );
        model.sync();
        model.readLock();
        try {
            List<Statement> statements = model.getStatements();
            assert statements.size() >3 :"Expected to find at least four statement";
            Statement statement = statements.get( 3 );
            assert statement instanceof ExpressionStatement : "Found "+
                statement.getElementType()+" instead of Expression statement";
            
            Expression expression = ((ExpressionStatement)statement).getExpression();
            assert expression != null;
            assert expression.getElementType().equals( ArrayExpression.class) :
                "Expected to find array expression m but found :" + expression.getElementType();
            ArrayExpression arrayExpression = (ArrayExpression)expression;
            
            List<ArrayDefElement> children = arrayExpression.getElements();
            assert children.size() == 1 :"Expected to find exactly 1  child, " +
                    "but found :"+ children.size(); 
            
            ArrayDefElement element = children.get(0);
            assert element.getElementType().equals( AssociativeArrayElement.class ):
                "Expected to find AssociativeArrayElement, but found :" +
                element.getElementType();
            AssociativeArrayElement assocElement = (AssociativeArrayElement) element;
            assert assocElement.getKey() == null;
            assert assocElement.getValue() == null;
            assert assocElement.getText().trim().length() == 0 :"Expected to find" +
                    " empty string as last argument";
        }
        finally {
            model.readUnlock();
        }
    }
}
