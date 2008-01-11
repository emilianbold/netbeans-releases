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
public class ConstantRefTest extends BaseCase {

    public void testArrayOne() throws Exception {
        PhpModel model = getModel( ResourceMarker.CONST_REF );
        model.sync();
        model.readLock();
        try {
            List<Statement> statements = model.getStatements();
            assert statements.size() >0 :"Expected to find at least one statement";
            
            Statement statement = statements.get( 0 );
            assert statement != null;
            assert statement.getElementType().equals( ExpressionStatement.class ) :
                "Expected to find expressoin statement, but found :" +
                statement.getElementType();
            
            ExpressionStatement expressionStatement = (ExpressionStatement)statement;
            Expression expression = expressionStatement.getExpression();
            assert expression != null;
            
            assert expression.getElementType().equals( CallExpression.class ) :
                "Expected to find call expression , but found :" +
                expression.getElementType();
            
            CallExpression callExpression = (CallExpression)expression;
            IdentifierExpression name = callExpression.getName();
            
            assert name != null : "Unexpected null name of call expression";
            assert name.getElementType().equals( Constant.class ) :
                "Expected to find Constant as name of function , but found :"+
                name.getElementType();
            
            Constant cnst = (Constant) name;
            Reference<SourceElement> ref = cnst.getSourceElement();
            ClassMemberReference<SourceElement> ref1 = cnst.getClassConstant();
            assert ref != null :"Extected to find not null reference to method";
            assert ref1 == null :"Unexpected reference to Class memeber found";
            
            assert ref.getSource()!= null;
            
            assert ref.getSource() == cnst :"Source element of source sould be " +
            		"element which was used for accessing to reference, but found :"+
            		ref.getSource().getElementType();
            
            assert ref.getIdentifier().equals( "method" ) :"Expected to find " +
            		"'method' as identifier in reference, but found :" +
            		ref.getIdentifier();
            SourceElement referenced = ref.get();
            assert referenced != null : "Expected to find referenced element";
            
            assert referenced.getElementType().equals( FunctionDefinition.class )
                : "Expected to find function definition as referenced element," +
                		" but found :" +referenced.getElementType();
            
            FunctionDefinition def = (FunctionDefinition)referenced;
            FunctionDeclaration decl = def.getDeclaration();
            assert decl!= null;
            assert decl.getName().equals( "method" ) :"Expected to find 'method'" +
            		" in referenced function name, but found :" + decl.getName();
                 
        }
        finally {
            model.readUnlock();
        }
    }
    
}
