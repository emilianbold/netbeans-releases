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






/**
 * @author ads
 *
 */
public interface PhpModelVisitor {

    void visit( PhpModel model );

    void visit( Block block );
    
    void visit( BreakStatement statement );
    
    void visit( ClassDefinition def );
    
    void visit( Comments comments );
    
    void visit( ContinueStatement statement );
    
    void visit( DoStatement statement );
    
    void visit( ExpressionStatement statement );
    
    void visit( ForEachStatement statement );
    
    void visit( ForStatement statement );
    
    void visit( FunctionDefinition def );
    
    void visit( IfStatement statement );
    
    void visit( InterfaceDefinition def );
    
    void visit( ReturnStatement statement );
    
    void visit( SwitchStatement statement );
    
    void visit( WhileStatement statement );

    void visit( While wile );

    void visit( FunctionDeclaration decl );

    void visit( ClassBody classBody );

    void visit( InterfaceBody interfaceBody );

    void visit( ConstDeclaration konst );

    void visit( ForEach forEach );

    void visit( For forr );

    void visit( ElseIf elseIf );

    void visit( Else els );

    void visit( If iff );

    void visit( Default def );

    void visit( Case caze );

    void visit( Switch switc );

    void visit( CallExpression expression );

    void visit( UnaryExpression expression );
    
    void visit( BinaryExpression expression );

    void visit( NewExpression expression );

    void visit( Variable var );

    void visit( Literal literal );

    void visit( Constant constant );

    void visit( ForExpression expression );

    void visit( GlobalStatement statement );

    void visit( FormalParameterList list  );

    void visit( FormalParameter parameter );

    void visit( StaticStatement statement );

    void visit( TernaryExpression expression );

    void visit( AttributesDeclaration attribute );

    void visit( ClassConst classConst );

    void visit( Attribute attribute );

    void visit( ArrayMemberExpression expression );

    void visit( ClassMemberExpression expression );

    void visit( ArrayExpression expression );

    void visit( Arguments arguments );

    void visit( AssociativeArrayElement element );

    void visit( VariableDeclaration declaration );
    
    void visit( InitializedDeclaration declaration );

    void visit( Error error );

    void visit( DeclareStatement statement );

    void visit( Declare declare );

}
