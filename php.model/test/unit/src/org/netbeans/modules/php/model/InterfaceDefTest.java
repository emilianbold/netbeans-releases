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
public class InterfaceDefTest extends BaseCase {

    public  void testInterface() throws Exception{
        PhpModel model = getModel( ResourceMarker.INTERFACE );
        model.sync();
        model.readLock();
        try {
            List<InterfaceDefinition> list = 
                model.getStatements( InterfaceDefinition.class );
            
            assert list.size() > 0 :"Expected at least one interface defenition";
            InterfaceDefinition def = list.get( 0 );
            
            assert def.getSuperInterfaces().size() == 0 : "Unexpected " +
                def.getSuperInterfaces().size() +" super interfaces found";
            
            InterfaceBody body = def.getBody();
            assert body != null :"Expected not null interface body";
            
            List<InterfaceStatement> interfaceStats = body.getStatements();
            assert interfaceStats.size() > 0 :"Expected not empty interface" +
                    " statements list";
            
            InterfaceStatement statement = interfaceStats.get( 0 );
            assert statement != null ;
            assert statement.getElementType().equals( ConstDeclaration.class ) :
                "Expected to find first interface statement as const declaration," +
                "but found : " +statement.getElementType();
            
            statement = interfaceStats.get( 1 );
            assert statement != null ;
            assert statement.getElementType().equals( ClassFunctionDeclaration.class )
                : "Expected to find second interface statemant as function " +
                        "declaration, but found :" +statement.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public  void testFunctionDecl() throws Exception{
        PhpModel model = getModel( ResourceMarker.INTERFACE );
        model.sync();
        model.readLock();
        try {
            List<InterfaceDefinition> list = model
                    .getStatements(InterfaceDefinition.class);

            InterfaceDefinition def = list.get(0);
            List<InterfaceStatement> interfaceStats = def.getBody()
                    .getStatements();

            ClassFunctionDeclaration decl = (ClassFunctionDeclaration) interfaceStats
                    .get(1);

            assert decl.getName().equals("op") : "Expected to find method"
                    + " with name 'op' , but found :" + decl.getName();

            List<Modifier> modifiers = decl.getModifiers();
            assert modifiers.size() == 1 : "Expected to find exactly one modifier,"
                    + "but found :" + modifiers.size();
            Modifier modifier = modifiers.get(0);
            assert modifier.equals(Modifier.PUBLIC) : "Expected to find"
                    + " 'public' modifier, but found :" + modifier;

            FormalParameterList parameters = decl.getParamaterList();
            assert parameters != null;
            assert parameters.getElementType()
                    .equals(FormalParameterList.class) : 
                        "Expected to find formal parameter list type for method"
                    + decl.getName()
                    + " , but found :"
                    + parameters.getElementType();

            List<FormalParameter> params = parameters.getParameters();
            assert params.size() > 0 : "Expected to find at least one paramenter in "
                    + "parameter list ";
            assert params.get(0).getElementType().equals(FormalParameter.class) : "Expected to find formal parameter type for method parameter,"
                    + "but found : " + params.get(0).getElementType();

            assert params.get(0).getName().equals("$arg") : "Expected to "
                    + "find parameter with name '$arg' , but found : "
                    + params.get(0).getName();

            assert params.get(0).getDefaultValue() == null;
        }
        finally {
            model.readUnlock();
        }
    }
    
    public  void testConstDecl() throws Exception{
        PhpModel model = getModel( ResourceMarker.INTERFACE );
        model.sync();
        model.readLock();
        try {
            List<InterfaceDefinition> list = 
                model.getStatements( InterfaceDefinition.class );
            
            InterfaceDefinition def = list.get( 0 );
            List<InterfaceStatement> interfaceStats = def.getBody().getStatements();
            ConstDeclaration decl = (ConstDeclaration)interfaceStats.get( 0 );
            
            List<ClassConst> cnst = decl.getDeclaredConstants();
            
            assert cnst.size() > 0:"Expected to find at least one declared constants";
            ClassConst conzt = cnst.get( 0 );
            assert conzt != null;
            
            assert conzt.getElementType().equals( ClassConst.class ) :
                "Expected to find class const in constant declaration , " +
                "but found: " +conzt.getElementType();
            
            Expression expr = conzt.getValue();
            assert expr != null :"Expected to find not null expression as const value";
            assert expr.getElementType().equals( Literal.class ) :"Expected" +
                    " to find literal as expression in constant , but found :" +
                    expr.getElementType();
            
            assert conzt.getName().equals( "CONST" ) :"Expected name CONST " +
                    "as constant name in interface constant declaration," +
                    "but found : " +  conzt.getName();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public  void testExtendedInterface() throws Exception{
        PhpModel model = getModel( ResourceMarker.INTERFACE );
        model.sync();
        model.readLock();
        try {
            List<InterfaceDefinition> list = 
                model.getStatements( InterfaceDefinition.class );
            
            assert list.size() >1 : "Expected at least two interfaces";
            
            InterfaceDefinition def = list.get( 1 );
            
            List<Reference<InterfaceDefinition>> interfaces = 
                def.getSuperInterfaces();
            assert interfaces.size() == 2 : "Expected to find two super interfaces," +
                    " but found :" +interfaces.size();
            Reference<InterfaceDefinition> reference = interfaces.get( 0 );
            
            assert reference.getIdentifier().equals( "InterfaceName" ) :
                "Expected to find 'InterfaceName' identifier as first " +
                "super interface identifier, but found :" +reference.getIdentifier();
            
            reference = interfaces.get( 1 );
            assert reference.getIdentifier().equals( "One" ) :
                "Expected to find 'One' identifier as first " +
                "super interface identifier, but found :" +reference.getIdentifier();
            
            assert def!= null;
            InterfaceBody body = def.getBody();
            
            assert body != null :"Expected not null interface body";
            
            List<InterfaceStatement> interfaceStats = body.getStatements();
            assert interfaceStats.size() > 0 :"Expected not empty interface" +
                    " statements list";
            
            InterfaceStatement statement = interfaceStats.get( 0 );
            assert statement != null;
            assert statement.getElementType().equals( ClassFunctionDeclaration.class) :
                "Expected to find function declaration , but found :" +
                statement.getElementType();

            ClassFunctionDeclaration decl = (ClassFunctionDeclaration) statement;
            assert decl.getModifiers().size() ==0 :"Expected no modifiers for" +
                    " function declaration, but found " +decl.getModifiers().size()+
                    " modifiers";
            
            statement = interfaceStats.get( 1 );
            assert statement != null;
            assert statement.getElementType().equals( ConstDeclaration.class ) :
                "Expected to find constant declaration as second statement , but" +
                " found :" + statement.getElementType();
            
            statement = interfaceStats.get( 2 );
            assert statement != null;
            assert statement.getElementType().equals( ConstDeclaration.class ) :
                "Expected to find constant declaration as third statement , but" +
                " found :" + statement.getElementType();
            
            statement = interfaceStats.get( 3 );
            assert statement != null;
            assert statement.getElementType().equals( ClassFunctionDeclaration.class) :
                "Expected to find function declaration as fourth interface statement," +
                " but found :" +statement.getElementType();

            decl = (ClassFunctionDeclaration) statement;
            assert decl.getModifiers().size() ==1 :"Expected exactly one  modifier for" +
                    " function declaration, but found " +decl.getModifiers().size();
            assert decl.getModifiers().get( 0 ).equals( Modifier.PUBLIC ) :
                "Expected to find 'public' modifier for method, but found :" +
                decl.getModifiers().get( 0 );
        }
        finally {
            model.readUnlock();
        }
    }
}
