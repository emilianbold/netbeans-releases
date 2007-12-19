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
public class PhpModelVisitorAdaptor implements PhpModelVisitor {

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.PhpModel)
     */
    public void visit( PhpModel model ) {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Block)
     */
    public void visit( Block block ) {
        visitElement( block );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.BreakStatement)
     */
    public void visit( BreakStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ClassDefinition)
     */
    public void visit( ClassDefinition def ) {
        visitElement( def );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Comments)
     */
    public void visit( Comments comments ) {
        visitElement( comments );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ContinueStatement)
     */
    public void visit( ContinueStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.DoStatement)
     */
    public void visit( DoStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ExpressionStatement)
     */
    public void visit( ExpressionStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ForEachStatement)
     */
    public void visit( ForEachStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.FunctionDefinition)
     */
    public void visit( FunctionDefinition def ) {
        visitElement( def );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.IfStatement)
     */
    public void visit( IfStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.InterfaceDefinition)
     */
    public void visit( InterfaceDefinition def ) {
        visitElement( def );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ReturnStatement)
     */
    public void visit( ReturnStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.SwitchStatement)
     */
    public void visit( SwitchStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.WhileStatement)
     */
    public void visit( WhileStatement statement ) {
        visitElement( statement );        
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.While)
     */
    public void visit( While wile ) {
        visitElement( wile );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.FunctionDeclaration)
     */
    public void visit( FunctionDeclaration decl ) {
        visitElement( decl );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ForStatement)
     */
    public void visit( ForStatement statement ) {
        visitElement( statement );        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ClassBody)
     */
    public void visit( ClassBody classBody ) {
        visitElement( classBody );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Const)
     */
    public void visit( ConstDeclaration konst ) {
        visitElement( konst );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.InterfaceBody)
     */
    public void visit( InterfaceBody interfaceBody ) {
        visitElement( interfaceBody );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ForEach)
     */
    public void visit( ForEach forEach ) {
        visitElement(forEach);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.For)
     */
    public void visit( For forr ) {
        visitElement( forr );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ElseIf)
     */
    public void visit( ElseIf elseIf ) {
        visitElement(elseIf);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Else)
     */
    public void visit( Else els ) {
        visitElement(els);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.If)
     */
    public void visit( If iff ) {
        visitElement( iff );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Default)
     */
    public void visit( Default def ) {
        visitElement(def);        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Case)
     */
    public void visit( Case caze ) {
        visitElement(caze);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Switch)
     */
    public void visit( Switch switc ) {
        visitElement(switc);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.CallExpression)
     */
    public void visit( CallExpression expression ) {
        visitElement(expression);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.UnaryExpression)
     */
    public void visit( UnaryExpression expression ) {
        visitElement(expression);        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.BinaryExpression)
     */
    public void visit( BinaryExpression expression ) {
        visitElement(expression);        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.NewExpression)
     */
    public void visit( NewExpression expression ) {
        visitElement(expression);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Variable)
     */
    public void visit( Variable var ) {
        visitElement(var);        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Literal)
     */
    public void visit( Literal literal ) {
        visitElement(literal);        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Constant)
     */
    public void visit( Constant constant ) {
        visitElement(constant);        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.GlobalStatement)
     */
    public void visit( GlobalStatement statement ) {
        visitElement(statement);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ForExpression)
     */
    public void visit( ForExpression expression ) {
        visitElement( expression );        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.FormalParameterList)
     */
    public void visit( FormalParameterList list ) {
        visitElement( list );        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.FormalParameter)
     */
    public void visit( FormalParameter parameter ) {
        visitElement( parameter );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.StaticStatement)
     */
    public void visit( StaticStatement statement ) {
        visitElement( statement );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.TernaryExpression)
     */
    public void visit( TernaryExpression expression ) {
        visitElement( expression );        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.AttributesDeclaration)
     */
    public void visit( AttributesDeclaration decl ) {
        visitElement( decl );         
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ClassConst)
     */
    public void visit( ClassConst classConst ) {
        visitElement( classConst );         
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Attribute)
     */
    public void visit( Attribute attribute ) {
        visitElement( attribute );         
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ArrayMemberExpression)
     */
    public void visit( ArrayMemberExpression expression ) {
        visitElement( expression );                 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ClassMemberExpression)
     */
    public void visit( ClassMemberExpression expression ) {
        visitElement( expression );                 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.ArrayExpression)
     */
    public void visit( ArrayExpression expression ) {
        visitElement( expression );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Arguments)
     */
    public void visit( Arguments arguments ) {
        visitElement( arguments );        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.AssociativeArrayElement)
     */
    public void visit( AssociativeArrayElement element ) {
        visitElement( element );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.VariableDeclaration)
     */
    public void visit( VariableDeclaration declaration ) {
        visitElement( declaration );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.InitializedDeclaration)
     */
    public void visit( InitializedDeclaration declaration ) {
        visitElement( declaration );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.Error)
     */
    public void visit( Error error ) {
        visitElement( error );        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.DeclareStatement)
     */
    public void visit( DeclareStatement statement ) {
        visitElement( statement );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitor#accept(org.netbeans.modules.php.model.Declare)
     */
    public void visit( Declare declare ) {
        visitElement( declare );
    }
    
    protected void visitElement( SourceElement element ) {
    }

}
