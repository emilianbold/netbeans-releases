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
package org.netbeans.modules.php.model.impl.refs.resolvers;

import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.php.model.Arguments;
import org.netbeans.modules.php.model.ArrayDefElement;
import org.netbeans.modules.php.model.ArrayExpression;
import org.netbeans.modules.php.model.ArrayMemberExpression;
import org.netbeans.modules.php.model.AssociativeArrayElement;
import org.netbeans.modules.php.model.BinaryExpression;
import org.netbeans.modules.php.model.Block;
import org.netbeans.modules.php.model.CallExpression;
import org.netbeans.modules.php.model.Case;
import org.netbeans.modules.php.model.Default;
import org.netbeans.modules.php.model.DoStatement;
import org.netbeans.modules.php.model.Else;
import org.netbeans.modules.php.model.ElseIf;
import org.netbeans.modules.php.model.Expression;
import org.netbeans.modules.php.model.ExpressionStatement;
import org.netbeans.modules.php.model.For;
import org.netbeans.modules.php.model.ForEach;
import org.netbeans.modules.php.model.ForEachStatement;
import org.netbeans.modules.php.model.ForExpression;
import org.netbeans.modules.php.model.ForStatement;
import org.netbeans.modules.php.model.FormalParameter;
import org.netbeans.modules.php.model.FormalParameterList;
import org.netbeans.modules.php.model.GlobalStatement;
import org.netbeans.modules.php.model.IdentifierExpression;
import org.netbeans.modules.php.model.If;
import org.netbeans.modules.php.model.IfStatement;
import org.netbeans.modules.php.model.InitializedDeclaration;
import org.netbeans.modules.php.model.Literal;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.PhpModelVisitorAdaptor;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.Statement;
import org.netbeans.modules.php.model.StaticStatement;
import org.netbeans.modules.php.model.Switch;
import org.netbeans.modules.php.model.SwitchStatement;
import org.netbeans.modules.php.model.TernaryExpression;
import org.netbeans.modules.php.model.Variable;
import org.netbeans.modules.php.model.VariableAppearance;
import org.netbeans.modules.php.model.VariableDeclaration;
import org.netbeans.modules.php.model.While;
import org.netbeans.modules.php.model.WhileStatement;


/**
 * @author ads
 *
 */
class VariableResolveVisitor<T extends SourceElement> 
    extends PhpModelVisitorAdaptor 
{

    public VariableResolveVisitor( String identifier, SourceElement subScope, 
            Class<T> clazz, boolean exactComparison ) 
    {
        myIdentifier = identifier;
        myStopScope = subScope;
        myType = clazz;
        isExactMatch = exactComparison;
        myResult = new LinkedList<T>();
    }
    
    @Override
    public void visit( PhpModel model )
    {
        List<Statement> statements = model.getStatements();
        visitExpressionStatements( statements );
    }
    
    public void visit( Arguments args )
    {
        List<Expression> arguments = args.getArgumentsList();
        visitExpressions( arguments );
    }
    
    @Override
    public void visit( CallExpression expression )
    {
        if ( expression.getName() != null && 
                !getStopScope().equals( expression.getName() )) 
        {
            visitDeeply( expression.getName() );
        }
    }

    @Override
    public void visit( ArrayMemberExpression expression )
    {
        CallExpression call = expression.getCallExpression();
        if ( call != null && !call.equals(getStopScope())){
            visitDeeply( call);
        }
        if( call!= null && call.equals(getStopScope())){
            return;
        }
        IdentifierExpression expr = expression.getOwnerIdentifier();
        if ( expr != null && !expr.equals( getStopScope())){
            visitDeeply( call );
        }
    }
    
    @Override
    public void visit( ArrayExpression expression )
    {
        List<ArrayDefElement> elements = expression.getElements();
        for (ArrayDefElement expr : elements) {
            if ( !expr.equals( getStopScope())){
                visitDeeply( expr );
            }
            else {
                return;
            }
        }
    }

    @Override
    public void visit( AssociativeArrayElement element )
    {
        Expression key = element.getKey();
        if ( key != null && !key.equals( getStopScope() )){
            visitDeeply( key );
        }
    }

    @Override
    public void visit( BinaryExpression expression )
    {
        Expression expr = expression.getLeftOperand();
        if ( getStopScope().equals(expr) ) {
            return;
        }
        visitDeeply(expr);
    }

    @Override
    public void visit( Block block )
    {
        List<SourceElement> children  = block.getChildren();
        visitExpressionStatements(children);
    }

    @Override
    public void visit( Case caze )
    {
        Expression expr = caze.getExpression();
        if ( getStopScope().equals( expr )){
            return;
        }
        visitDeeply( expr );
        visitExpressionStatements( caze.getStatements() );
    }

    @Override
    public void visit( Default def )
    {
        visitExpressionStatements( def.getStatements() );
    }

    @Override
    public void visit( DoStatement statement )
    {
        While wile = statement.getWhile();
        if ( getStopScope().equals( wile )){
            return;
        }
        visitDeeply( wile );
    }

    @Override
    public void visit( Else els )
    {
        visitExpressionStatements( els.getStatements() );
    }

    @Override
    public void visit( ElseIf elseIf )
    {
        Expression expression = elseIf.getExpression();
        if ( getStopScope().equals( expression )){
            return;
        }
        visitDeeply( expression );
        visitExpressionStatements( elseIf.getStatements() );
    }

    @Override
    public void visit( For forr )
    {
        ForExpression expression = forr.getInitialExpression();
        if ( getStopScope().equals( expression )){
            return;
        }
        visitDeeply( expression );
        expression = forr.getConditionalExpression();
        if ( getStopScope().equals( expression )){
            return;
        }
        visitDeeply( expression );
    }

    @Override
    public void visit( ForEach forEach )
    {
        Expression expr = forEach.getExpression();
        if ( getStopScope().equals( expr ) ){
            return;
        }
        visitDeeply( expr );
        VariableDeclaration var = forEach.getIndexVariable();
        if ( getStopScope().equals( var )){
            visitDeeply( var );
        }
    }

    @Override
    public void visit( ForEachStatement statement )
    {
        ForEach forEach = statement.getForEach();
        if( getStopScope().equals( forEach) ){
            return;
        }
        visitDeeply( forEach );
        visitExpressionStatements( statement.getStatements() );
    }

    @Override
    public void visit( ForExpression expression )
    {
        List<Expression> expressions = expression.getExpressions();
        visitExpressions( expressions );
    }

    @Override
    public void visit( FormalParameterList list )
    {
        List<FormalParameter> params = list.getParameters();
        for (FormalParameter parameter : params) {
            if ( getStopScope().equals(parameter) ) {
                return;
            }
            visitDeeply(parameter);
        }
    }

    @Override
    public void visit( ForStatement statement )
    {
        For forr = statement.getFor();
        if ( getStopScope().equals( forr )){
            return;
        }
        visitDeeply( forr );
        visitExpressionStatements( statement.getStatements() );
    }

    @Override
    public void visit( GlobalStatement statement )
    {
        List<VariableDeclaration> decls = statement.getVariableDeclarations();
        for (VariableDeclaration declaration : decls) {
            if ( getStopScope().equals( declaration )){
                return;
            }
            visitDeeply( declaration );
        }
    }

    @Override
    public void visit( IfStatement statement )
    {
        If iff = statement.getIf();
        if ( getStopScope().equals( iff )){
            return;
        }
        visitDeeply( iff );
        visitExpressionStatements( statement.getStatements() );
    }

    @Override
    public void visit( StaticStatement statement )
    {
        List<InitializedDeclaration> vars = statement.getVariables();
        for (InitializedDeclaration declaration : vars) {
            if ( getStopScope().equals( declaration) ){
                return;
            }
            visitDeeply( declaration );
        }
    }

    @Override
    public void visit( SwitchStatement statement )
    {
        Switch svitch = statement.getSwitch();
        if ( getStopScope().equals( svitch ) ){
            return;
        }
        visitDeeply( svitch );
    }

    @Override
    public void visit( TernaryExpression expression )
    {
        if ( getStopScope().equals( expression.getCondition() )){
            return;
        }
        visitDeeply( expression.getCondition());
        if ( getStopScope().equals( expression.getTrueExpression())) {
            return;
        }
        visitDeeply(expression.getTrueExpression());
    }

    @Override
    public void visit( Variable var )
    {
        visitDeeply(var);
    }

    @Override
    public void visit( VariableDeclaration declaration )
    {
        visitDeeply( declaration );
    }

    @Override
    public void visit( WhileStatement statement )
    {
        While wile = statement.getWhile();
        if ( getStopScope().equals( wile) ) {
            return;
        }
        if ( wile.getExpression() != null ) {
            visitDeeply( wile.getExpression() );
        }
        List<Statement> statements = statement.getStatements();
        visitExpressionStatements( statements );
    }
    
    List<T> getResult() {
        return myResult;
    }
    
    private SourceElement getStopScope() {
        return myStopScope;
    }
    
    private void visitExpressions( List<? extends SourceElement> children ) {
        for ( SourceElement child: children ) {
            if ( getStopScope().equals(child) ) {
                return;
            }
            if ( !( child instanceof Expression )) {
                continue;
            }
            visitDeeply( child );
        }
    }

    private void visitExpressionStatements( List<? extends SourceElement> children ) {
        for ( SourceElement child: children ) {
            if ( getStopScope().equals(child) ) {
                return;
            }
            if ( !( child instanceof ExpressionStatement ) && 
                    !( child instanceof GlobalStatement ) && 
                    !( child instanceof StaticStatement )) 
            {
                continue;
            }
            visitDeeply( child );
        }
    }

    private void visitDeeply( SourceElement element ) {
        if ( element == null ){
            return;
        }
        if ( element instanceof VariableAppearance ) {
            findVariable( (VariableAppearance)element );
        }
        List<SourceElement> children = element.getChildren();
        for (SourceElement child : children) {
            visitDeeply( child );
        }
    }

    private void findVariable( VariableAppearance appearance ) {
        if ( !getType().isAssignableFrom( appearance.getElementType() )){
            return;
        }
        if ( appearance instanceof Variable ) {
            Variable var = (Variable) appearance;
            if ( var.getName() == null || 
                    !var.getName().getElementType().equals( Literal.class ) ) 
            {
                return;
            }
        }
        String name = appearance.getText();
        if ( name == null ) {
            return;
        }
        if ( isExactMatch && name.equals( myIdentifier )) {
            getResult().add( getType().cast( appearance) );
        }
        if ( !isExactMatch && name.startsWith( myIdentifier) ) {
            getResult().add( getType().cast( appearance) );
        }
    }
    
    private Class<T> getType(){
        return myType;
    }

    private String myIdentifier;
    
    private SourceElement myStopScope;
    
    private Class<T> myType;
    
    private boolean isExactMatch;
    
    private List<T> myResult;
    
}
