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
public class StaticReferenceTest extends BaseCase {

    public void testStaticVar() throws Exception{
        PhpModel model = getModel( ResourceMarker.STATIC_REF) ;
        model.sync();
        model.readLock();
        try {
            List<Statement> list = model.getStatements();
            assert list.size() > 1 :"Expected to find at least 2 statements," +
            		" found :" +list.size();
            Statement statement = list.get( 1 );
            assert statement instanceof ExpressionStatement;
            ExpressionStatement expressionStatement = (ExpressionStatement)statement;
            Expression expression = expressionStatement.getExpression();
            
            assert expression!=null;
            assert expression.getElementType().equals( Variable.class ) :
                "Expected to find variable , but found :" +expression.getElementType();
            
            Variable variable = (Variable) expression;
            Reference<VariableAppearance> varRef = variable.getAppearance();
            assert varRef != null : "Expected to find not null reference";
            assert varRef.getIdentifier().equals( "Clazz::$var") :"Expected to " +
            		"find string 'Clazz::$var' as reference identifier," +
            		"but found : " +varRef.getIdentifier();
            
            VariableAppearance appearance = varRef.get();
            assert appearance!=null :"Expected to find not null referenced element";
            
            assert appearance.getElementType().equals( Attribute.class );
            Attribute attribute = (Attribute) appearance;
            assert attribute.getName().equals("$var") :"Expected to find $var" +
            		" as resolved attibute name , but found :" +attribute.getName();
            
            assert varRef instanceof ClassMemberReference : "Expect that accessed" +
            		" reference is ClassMemberReference";
            ClassMemberReference<VariableAppearance> ref = 
                (ClassMemberReference<VariableAppearance>) varRef;
            assert ref.getObject()!= null :"Expected to find not null container for" +
            		" reference";
            
            assert ref.getObject().getName().equals( "Clazz" ) :"Expected to " +
            		"find 'Clazz' as name for referenced container , but found :" +
            		ref.getObject().getName();
            
            assert ref.getObjectName().equals("Clazz" ) :"Expected to " +
                "find 'Clazz' as name for object in reference , but found :" +
                ref.getObjectName();
            
            assert ref.getMemberName().equals("$var") :"Expected to find $var" +
                    " as referenced maember name, but found  :" + ref.getMemberName();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testConst() throws Exception{
        PhpModel model = getModel( ResourceMarker.STATIC_REF) ;
        model.sync();
        model.readLock();
        try {
            List<Statement> list = model.getStatements();
            assert list.size() > 2 :"Expected to find at least 2 statements," +
                    " found :" +list.size();
            Statement statement = list.get( 2 );
            assert statement instanceof ExpressionStatement;
            ExpressionStatement expressionStatement = (ExpressionStatement)statement;
            Expression expression = expressionStatement.getExpression();
            
            assert expression!=null;
            assert expression.getElementType().equals( Constant.class ) :
                "Expected to find variable , but found :" +expression.getElementType();
            
            Constant constant = (Constant) expression;
            
            assert constant.getSourceElement() == null :"Not expected to find " +
            		"reference to defined source element should be null"; 
            
            ClassMemberReference<SourceElement> constRef = constant.getClassConstant();
            assert constRef != null : "Expected to find not null reference";
            assert constRef.getIdentifier().equals( "Clazz::CONST") :"Expected to " +
                    "find string 'Clazz::CONST' as reference identifier," +
                    "but found : " +constRef.getIdentifier();
            
            SourceElement constDecl = constRef.get();
            assert constDecl!=null :"Expected to find not null referenced element";
            
            assert constDecl.getElementType().equals( ClassConst.class );
            ClassConst classConst = (ClassConst) constDecl;
            assert classConst.getName().equals("CONST") :"Expected to find $var" +
                    " as resolved attibute name , but found :" +classConst.getName();

            assert constRef.getObject()!= null :"Expected to find not null container for" +
                    " reference";
            
            assert constRef.getObject().getName().equals( "Clazz" ) :"Expected to " +
                    "find 'Clazz' as name for referenced container , but found :" +
                    constRef.getObject().getName();
            
            assert constRef.getObjectName().equals("Clazz" ) :"Expected to " +
                "find 'Clazz' as name for object in reference , but found :" +
                constRef.getObjectName();
            
            assert constRef.getMemberName().equals("CONST") :"Expected to find $var" +
                    " as referenced maember name, but found  :" + constRef.getMemberName();
        }
        finally {
            model.readUnlock();
        }
    }
}
