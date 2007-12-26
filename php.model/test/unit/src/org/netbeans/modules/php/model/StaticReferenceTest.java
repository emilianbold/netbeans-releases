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
            assert list.size() > 2 :"Expected to find at least 3 statements," +
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
    
    public void testMethod() throws Exception{
        PhpModel model = getModel( ResourceMarker.STATIC_REF) ;
        model.sync();
        model.readLock();
        try {
            List<Statement> list = model.getStatements();
            assert list.size() > 3 :"Expected to find at least 4 statements," +
                    " found :" +list.size();
            Statement statement = list.get( 3 );
            assert statement instanceof ExpressionStatement;
            ExpressionStatement expressionStatement = (ExpressionStatement)statement;
            Expression expression = expressionStatement.getExpression();
            
            assert expression!=null;
            assert expression.getElementType().equals( CallExpression.class ) :
                "Expected to find call expression m but found :" +
                expression.getElementType();
            CallExpression callExpression = (CallExpression) expression;
            
            IdentifierExpression id = callExpression.getName();
            assert id!= null;
            assert id.getElementType().equals( Constant.class ) :"Expected to " +
            		"find constant as name identifier for call expression, " +
            		"but found :" +id.getElementType();
            
            Constant constant = (Constant) id;
            Reference<SourceElement> sourceRef = constant.getSourceElement();
            assert sourceRef == null :"Unexpected reference to non-class member";
            
            ClassMemberReference<SourceElement> ref = constant.getClassConstant();
            assert ref != null : "Reference to class constant is null, but should not be";
            
            assert ref.getIdentifier().equals( "Clazz::method") : "Expected " +
            		"identifier string in reference is 'Clazz::method', but found :"
                    +ref.getIdentifier();
            
            assert ref.getMemberName().equals( "method" ):"Expected to find " +
            		"'method' as member name in reference , but found :" +
            		ref.getMemberName();
            
            assert ref.getObjectName().equals( "Clazz" ) :"Expected to find "+
                    "'Clazz' as owner class name in reference, but found :" +
                    ref.getObjectName();
            
            SourceElement resolved = ref.get();
            assert resolved != null :"Unable to resolve class method name";
            
            assert resolved.getElementType().equals( ClassFunctionDefinition.class ):
                "Expected to find ClassFunctionDefinition, but found :" +
                resolved.getElementType();
            ClassFunctionDefinition def = (ClassFunctionDefinition)resolved;
            
            assert def.getDeclaration().getName().equals( "method" ) :"Expected " +
            		"'method' name in resolved element via reference, but found :" +
            		def.getDeclaration();
            
            ObjectDefinition objectDefinition = ref.getObject();
            assert objectDefinition != null :"Expected to find not null owner " +
            		"object in reference";
            
            assert objectDefinition.getElementType().equals( ClassDefinition.class ):
                "Expected to find ClassDefinition as element type for owner object," +
                "but found :"+objectDefinition.getElementType();
            
            ClassDefinition classDef = (ClassDefinition)objectDefinition;
            assert classDef.getName().equals( "Clazz") :"Expected to find 'Clazz'" +
            		" as name for owner object resolved via reference, but found :" +
            		classDef.getName();
            
        }
        finally {
            model.readUnlock();
        }
    }
        
}
