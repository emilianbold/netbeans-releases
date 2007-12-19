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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.netbeans.modules.php.model.resources.ResourceMarker;


/**
 * @author ads
 *
 */
public class ClassDefTest extends BaseCase {

    public  void testClass() throws Exception {
        PhpModel model = getModel( ResourceMarker.CLASS );
        model.sync();
        model.readLock();
        try {
            List<ClassDefinition> list = model.getStatements( ClassDefinition.class );
            assert list.size() > 0 :"Expected to find at least one class " +
                    "definition";
            ClassDefinition def = list.get( 0 );
            assert def != null;
            
            assert def.getModifiers().size() == 0 :"Unexpected " + 
                def.getModifiers().size()+ "modifiers ";
            
            assert def.getSuperClass() == null : "Unexpected non-null reference" +
                    " to super class with identifier :" +def.getSuperClass().getIdentifier();
            
            assert def.getImplementedInterfaces().size() == 0 : "Unexpected non-empty" +
                    " list with implemented interfaces , size :" + 
                    def.getImplementedInterfaces().size();
            
            assert def.getName().equals( "Class") :"Expected 'Class' as class name" +
                    ", but found : " +def.getName();
            ClassBody body = def.getBody();
            assert body != null :"Expected not null class body";

            
            assert body.getElementType().equals( ClassBody.class ) :"Expected " +
                    "ClassBody element type for class body source element, but " +
                    "found :" +body.getElementType();
            
            List<ClassStatement> statements = body.getStatements();
            assert statements.size() > 0 :"Expected to find non-empty class " +
                    "statements list";
            
            ClassStatement statement = statements.get( 0 );
            assert statement.getElementType().equals( AttributesDeclaration.class ):
                "Expected to find attribute declaration as first statement in" +
                " class definition, but found :" +statement.getElementType();
            
            statement = statements.get( 1 );
            assert statement.getElementType().equals( ConstDeclaration.class ):
                "Expected to find constant declaration as second statement in class" +
                " definition , but found :" +statement.getElementType();
            
            statement = statements.get( 2 );
            assert statement.getElementType().equals( AttributesDeclaration.class ):
                "Expected to find attribute declaration as third statement in class" +
                " definition , but found :" +statement.getElementType();
            
            statement = statements.get( 3 );
            assert statement.getElementType().equals( ClassFunctionDeclaration.class ):
                "Expected to find function declaration as fourth statement in class" +
                " definition , but found :" +statement.getElementType();
            
            statement = statements.get( 4 );
            assert statement.getElementType().equals( ClassFunctionDefinition.class ):
                "Expected to find function definition as fifth statement in class" +
                " definition , but found :" +statement.getElementType();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public  void testVarAttribute() throws Exception {
        PhpModel model = getModel( ResourceMarker.CLASS );
        model.sync();
        model.readLock();
        try {
            List<ClassDefinition> list = model.getStatements( ClassDefinition.class );
            ClassDefinition def = list.get( 0 );
            
            ClassBody body = def.getBody();
            List<ClassStatement> statements = body.getStatements();
            ClassStatement statement = statements.get( 0 );
            
            AttributesDeclaration decl = (AttributesDeclaration) statement;
            List<Attribute> attrs = decl.getDeclaredAttributes();
            assert attrs.size() > 0 :"Expected to find at least one declared " +
                    "attribute with 'var' modifier";
            
            Attribute attr = attrs.get( 0 );
            assert attr!= null;
            assert attr.getElementType().equals( Attribute.class ) : "Expected" +
                    " to find attribute type as element type for attribute, " +
                    "but found : " +attr.getElementType();
            
            Expression expr = attr.getDefaultValue();
            assert expr == null;

            List<Modifier> modifiers = decl.getModifiers();
            assert modifiers.size() == 1: "Expected to find exactly one attribute " +
                    "modifier, but found :" + modifiers.size();
            assert modifiers.get( 0 ).equals( Modifier.VAR ) :"Expected " +
                    "to find 'var' modifier, but found : " + modifiers.get( 0 );
        }
        finally {
            model.readUnlock();
        }
    }
    
    public  void testConst() throws Exception {
        PhpModel model = getModel( ResourceMarker.CLASS );
        model.sync();
        model.readLock();
        try {
            List<ClassDefinition> list = model.getStatements( ClassDefinition.class );
            ClassDefinition def = list.get( 0 );
            
            ClassBody body = def.getBody();
            List<ClassStatement> statements = body.getStatements();
            ClassStatement statement = statements.get( 1 );
            
            ConstDeclaration decl = (ConstDeclaration)statement;
            
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
            
            assert conzt.getName().equals( "A" ) :"Expected name CONST " +
                    "as constant name in interface constant declaration," +
                    "but found : " +  conzt.getName();
        }
        finally {
            model.readUnlock();
        }
    }
    
    public  void testProtectedAttrs() throws Exception {
        PhpModel model = getModel( ResourceMarker.CLASS );
        model.sync();
        model.readLock();
        try {
            List<ClassDefinition> list = model.getStatements( ClassDefinition.class );
            ClassDefinition def = list.get( 0 );
            
            ClassBody body = def.getBody();
            List<ClassStatement> statements = body.getStatements();
            ClassStatement statement = statements.get( 2 );
            
            AttributesDeclaration decl = (AttributesDeclaration) statement;
            List<Attribute> attrs = decl.getDeclaredAttributes();
            assert attrs.size() > 1 :"Expected to find at least two declared " +
                    "attribute with 'protected' modifier";
            
            Attribute attr = attrs.get( 0 );
            assert attr!= null;
            assert attr.getElementType().equals( Attribute.class ) : "Expected" +
                    " to find attribute type as element type for attribute, " +
                    "but found : " +attr.getElementType();
            
            Expression expr = attr.getDefaultValue();
            assert expr!= null :"Expected to find default value expression for" +
                    " first protected attribute";
            assert expr.getElementType().equals( Literal.class ) : "Expected to " +
                    "find literal expression as default value for first protected" +
                    " attribute but found : " + expr.getElementType();
            

            List<Modifier> modifiers = decl.getModifiers();
            assert modifiers.size() == 1: "Expected to findf exactly one modifier," +
                    " but found :" + modifiers.size();
            assert modifiers.get( 0 ).equals( Modifier.PROTECTED ) : "Expected to " +
                    "find 'protected' modifier, but found :"  + modifiers.get( 0 );
            
            
            attr = attrs.get( 1 );
            assert attr!= null;
            assert attr.getElementType().equals( Attribute.class ) : "Expected" +
                    " to find attribute type as element type for attribute, " +
                    "but found : " +attr.getElementType();
            
            expr = attr.getDefaultValue();
            assert expr == null;
            
        }
        finally {
            model.readUnlock();
        }
    }
    
    public  void testAbstractMethod() throws Exception {
        PhpModel model = getModel( ResourceMarker.CLASS );
        model.sync();
        model.readLock();
        try {
            List<ClassDefinition> list = model.getStatements( ClassDefinition.class );
            ClassDefinition def = list.get( 0 );
            
            ClassBody body = def.getBody();
            List<ClassStatement> statements = body.getStatements();
            ClassStatement statement = statements.get( 3 );
            
            ClassFunctionDeclaration decl = (ClassFunctionDeclaration)statement;

            assert decl.getName().equals( "func" ) :"Expected to find method" +
                    " with name 'func' , but found :" +decl.getName();
            
            List<Modifier> modifiers = decl.getModifiers();
            assert modifiers.size() == 1 : "Expected to find exactly one modifier," +
                    "but found :" +modifiers.size();
            Modifier modifier = modifiers.get( 0 );
            assert modifier.equals( Modifier.ABSTRACT ) :"Expected to find" +
                    " 'abstract' modifier, but found :" +modifier;
            
            FormalParameterList parameters  = decl.getParamaterList();
            assert parameters != null;
            assert parameters.getElementType().equals( FormalParameterList.class )
                :"Expected to find formal parameter list type for method" +
                decl.getName() +" , but found :" +parameters.getElementType();
            
            List<FormalParameter> params = parameters.getParameters();
            assert params.size() >0 :"Expected to find at least one paramenter in " +
                    "parameter list ";
            assert params.get( 0 ).getElementType().equals( FormalParameter.class ) :
                "Expected to find formal parameter type for method parameter," +
                "but found : " +params.get( 0 ).getElementType();
            
            assert params.get( 0 ).getName().equals( "$arg" ) : "Expected to " +
                    "find parameter with name '$arg' , but found : " + 
                    params.get( 0 ).getName();
            
            assert params.get( 0 ).getDefaultValue() == null; 
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testStaticMethod() throws Exception {
        PhpModel model = getModel( ResourceMarker.CLASS );
        model.sync();
        model.readLock();
        try {
            List<ClassDefinition> list = model.getStatements( ClassDefinition.class );
            ClassDefinition def = list.get( 0 );
            
            ClassBody body = def.getBody();
            List<ClassStatement> statements = body.getStatements();
            ClassStatement statement = statements.get( 4 );
            
            ClassFunctionDefinition functionDefinition = 
                (ClassFunctionDefinition)statement;
            assert functionDefinition.getBody().getElementType().equals( Block.class ):
                "Expected to find block as type for function body, but found :" +
                functionDefinition.getBody().getElementType();
            
            List<Modifier> modifiers = functionDefinition.getModifiers();
            assert modifiers.size() == 1 :"Expected to find exactly one modifier " +
                    "for function , but found : "+  modifiers.size();
            assert modifiers.get( 0 ).equals( Modifier.STATIC ) : "Expected to " +
                    "find 'static' modifier for function definition , but found : "+
                    modifiers.get( 0 );
            FunctionDeclaration decl = functionDefinition.getDeclaration();
            assert decl != null;
            
            assert decl.getElementType().equals( FunctionDeclaration.class ) :
                "Expected to find function declaration type for function " +
                "declaration element , but found : "+decl.getElementType();
            
            assert decl.getName().equals( "method" ) : "Expected to find " +
                    "function with name 'method' but found : " +decl.getName();
            
            assert decl.getParamaterList() != null :"Expected not null ( but empty )" +
                    " parameter list";
            
            assert decl.getParamaterList().getParameters().size() == 0 :
                "Expected empty parameter list, but found : "  +
                decl.getParamaterList().getParameters().size() +" parameters"; 
        }
        finally {
            model.readUnlock();
        }
    }
    
    public void testAbstractClass() throws Exception {
        PhpModel model = getModel( ResourceMarker.CLASS );
        model.sync();
        model.readLock();
        try {
            List<ClassDefinition> list = model.getStatements( ClassDefinition.class );
            ClassDefinition def = list.get( 1 );
            
            List<Modifier> modifiers = def.getModifiers();
            assert modifiers.size() == 1 :"Expected to find exactly one " +
                    "modifier , but found : "+modifiers.size();
            
            assert modifiers.get( 0 ).equals( Modifier.ABSTRACT ) :"Expected to" +
                    "find 'abstract' class modifer , but found : "+modifiers.get( 0 );
            
            checkSuper( def );
            
            ClassBody body = def.getBody();
            assert body != null;
            assert body.getElementType().equals( ClassBody.class ):"Expected to find" +
                    " ClassBody type for class body , but found : " +body.getElementType();
            
            List<ClassStatement> statements = body.getStatements();
            assert statements.size() > 0 : "Expected to find at least one " +
                    " statement in class body , but found : " +statements.size();
            
            
            assert statements.get( 0 ).getElementType().equals( 
                    ClassFunctionDefinition.class );
            
            assert statements.get( 1  ).getElementType().equals( 
                    ClassFunctionDefinition.class );
            
            assert statements.get( 2 ).getElementType().equals( 
                    ConstDeclaration.class );
            
            assert statements.get( 3 ).getElementType().equals( 
                    AttributesDeclaration.class );
            
            assert statements.get( 4 ).getElementType().equals( 
                    AttributesDeclaration.class );
            
            modifiers = 
                ((ClassFunctionDefinition)statements.get( 0 )).getModifiers();
            
            assert modifiers.size() == 2 :"Expected to find exactly two modifiers" +
                    " for first function , but found :" +modifiers.size();
            
            Set<Modifier> set = new HashSet<Modifier>( modifiers );
            assert set.contains( Modifier.PUBLIC ) : "Expected " +
                    "to find 'public' as modifier for first method, found modifiers :"
                    + getModifiers(modifiers);
            assert set.contains( Modifier.FINAL ) : "Expected " +
                "to find 'final' as modifier for first method, found modifers:"
                + getModifiers(modifiers);
            
            modifiers = 
                ((ClassFunctionDefinition)statements.get( 1 )).getModifiers();
            
            assert modifiers.size() == 1 :"Expected to find exactly one modifiers" +
                    " for first function , but found :" +modifiers.size();
            
            assert modifiers.get( 0 ).equals( Modifier.PRIVATE ) : "Expected " +
                    "to find 'private' as modifier for first method, found "
                    + modifiers.get( 0 );
            
            modifiers = 
                ((AttributesDeclaration)statements.get( 3 )).getModifiers();
            assert modifiers.size() == 1 :"Expected to find exactly one modifiers" +
                " for first attr declaration , but found :" +modifiers.size();
    
            assert modifiers.get( 0 ).equals( Modifier.PROTECTED ) : "Expected " +
                "to find 'protected' as modifier for first attr decl, found "
                + modifiers.get( 0 );
            
            
            modifiers = 
                ((AttributesDeclaration)statements.get( 4 )).getModifiers();
            
            assert modifiers.size() == 2 :"Expected to find exactly two modifiers" +
                    " for second attributes, but found :" +modifiers.size();
            
            set = new HashSet<Modifier>( modifiers );
            assert set.contains( Modifier.PUBLIC ) : "Expected " +
                    "to find 'public' as modifier for second attr decl, " +
                    "found modifiers :"
                    + getModifiers(modifiers);
            assert set.contains( Modifier.STATIC ) : "Expected " +
                "to find 'static' as modifier for second attr decl, found modifers:"
                + getModifiers(modifiers);
            
        }
        finally {
            model.readUnlock();
        }
    }

    private void checkSuper( ClassDefinition def ) {
        List<Reference<InterfaceDefinition>> list = def.getImplementedInterfaces();
        assert list.size()== 2 :"Expected to find exactly two implemented" +
                " interfaces , but found : " + list.size();
        
        Reference<InterfaceDefinition> reference = list.get( 0 );
        assert reference != null :"Expected to find not null reference";
        
        assert reference.getIdentifier().equals( "InterfaceName" ) :
            "Expected to find 'InterfaceName' identifier for first implemented" +
            " interface name , but found :" + reference.getIdentifier();
        
        reference = list.get( 1 );
        assert reference != null :"Expected to find not null reference";
        
        assert reference.getIdentifier().equals( "Second" ) :
            "Expected to find 'Second' identifier for first implemented" +
            " interface name , but found :" + reference.getIdentifier();
        
        Reference<ClassDefinition> ref = def.getSuperClass();
        assert ref != null;
        
        assert ref.getIdentifier().equals( "Class" ) :"Expected to find " +
                "'Class' as identifier for super class , but found :" + 
                ref.getIdentifier();
    }

    private String getModifiers ( List<Modifier> list ) {
        StringBuilder builder = new StringBuilder();
        for (Modifier modifier : list) {
            builder.append( modifier );
            builder.append( ", ");
        }
        return builder.length() > 0 ? builder.substring( 0 , builder.length() -3) 
            : "";
    }
}
