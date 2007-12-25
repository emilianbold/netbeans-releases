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
package org.netbeans.modules.php.model.impl;

import java.util.Collections;
import java.util.List;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.Arguments;
import org.netbeans.modules.php.model.ArrayExpression;
import org.netbeans.modules.php.model.ArrayMemberExpression;
import org.netbeans.modules.php.model.AssociativeArrayElement;
import org.netbeans.modules.php.model.Attribute;
import org.netbeans.modules.php.model.AttributesDeclaration;
import org.netbeans.modules.php.model.BinaryExpression;
import org.netbeans.modules.php.model.Block;
import org.netbeans.modules.php.model.CallExpression;
import org.netbeans.modules.php.model.Case;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.ClassConst;
import org.netbeans.modules.php.model.ClassDefinition;
import org.netbeans.modules.php.model.ClassMemberExpression;
import org.netbeans.modules.php.model.ConstDeclaration;
import org.netbeans.modules.php.model.Declare;
import org.netbeans.modules.php.model.DeclareStatement;
import org.netbeans.modules.php.model.Default;
import org.netbeans.modules.php.model.DoStatement;
import org.netbeans.modules.php.model.Else;
import org.netbeans.modules.php.model.ElseIf;
import org.netbeans.modules.php.model.ExpressionStatement;
import org.netbeans.modules.php.model.For;
import org.netbeans.modules.php.model.ForEach;
import org.netbeans.modules.php.model.ForEachStatement;
import org.netbeans.modules.php.model.ForExpression;
import org.netbeans.modules.php.model.ForStatement;
import org.netbeans.modules.php.model.FormalParameter;
import org.netbeans.modules.php.model.FormalParameterList;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.GlobalStatement;
import org.netbeans.modules.php.model.If;
import org.netbeans.modules.php.model.IfStatement;
import org.netbeans.modules.php.model.InitializedDeclaration;
import org.netbeans.modules.php.model.InterfaceBody;
import org.netbeans.modules.php.model.InterfaceDefinition;
import org.netbeans.modules.php.model.NewExpression;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.PhpModelVisitor;
import org.netbeans.modules.php.model.PhpModelVisitorAdaptor;
import org.netbeans.modules.php.model.ReturnStatement;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.StatementContainer;
import org.netbeans.modules.php.model.StaticStatement;
import org.netbeans.modules.php.model.Switch;
import org.netbeans.modules.php.model.SwitchStatement;
import org.netbeans.modules.php.model.UnaryExpression;
import org.netbeans.modules.php.model.Variable;
import org.netbeans.modules.php.model.VariableDeclaration;
import org.netbeans.modules.php.model.While;
import org.netbeans.modules.php.model.WhileStatement;
import org.netbeans.modules.php.model.impl.builders.ArgumentsBuilder;
import org.netbeans.modules.php.model.impl.builders.ClassBodyBuilder;
import org.netbeans.modules.php.model.impl.builders.ExpressionBuilder;
import org.netbeans.modules.php.model.impl.builders.FormalParameterBuilder;
import org.netbeans.modules.php.model.impl.builders.InterfaceBodyBuilder;
import org.netbeans.modules.php.model.impl.builders.VariableDeclarationBuilder;
import org.netbeans.modules.php.model.impl.builders.WhileBuilder;
import org.netbeans.modules.php.model.impl.factory.ArgumentsFactory;
import org.netbeans.modules.php.model.impl.factory.ArrayExpressionFactory;
import org.netbeans.modules.php.model.impl.factory.AttrDeclarationFactory;
import org.netbeans.modules.php.model.impl.factory.BlockFactory;
import org.netbeans.modules.php.model.impl.factory.CallExpressionFactory;
import org.netbeans.modules.php.model.impl.factory.ClassBodyFactory;
import org.netbeans.modules.php.model.impl.factory.ConstDeclFactory;
import org.netbeans.modules.php.model.impl.factory.DeclareStatementFactory;
import org.netbeans.modules.php.model.impl.factory.ExpressionFactory;
import org.netbeans.modules.php.model.impl.factory.ForEachFactory;
import org.netbeans.modules.php.model.impl.factory.ForFactory;
import org.netbeans.modules.php.model.impl.factory.ForStatementFactory;
import org.netbeans.modules.php.model.impl.factory.FunctionDeclarationFactory;
import org.netbeans.modules.php.model.impl.factory.FunctionDefFactory;
import org.netbeans.modules.php.model.impl.factory.IfStatementFactory;
import org.netbeans.modules.php.model.impl.factory.InterfaceBodyFactory;
import org.netbeans.modules.php.model.impl.factory.VariableFactory;
import org.netbeans.modules.php.model.impl.factory.StatementsListFactory;
import org.netbeans.modules.php.model.impl.factory.SwitchStatementFactory;
import org.netbeans.modules.php.model.impl.factory.WhileStatementFactory;


/**
 * @author ads
 *
 */
public class FactoryVisitor extends PhpModelVisitorAdaptor {
    
    public static final String ERROR                = "ERROR";              // NOI18N
    
    public void visit( PhpModel model ) {
        mySource = StatementsListFactory.getInstance().build( model , getNode() , 
                mySequence );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.PhpModelVisitorAdaptor#visitElement(org.netbeans.modules.php.model.SourceElement)
     */
    @Override
    protected void visitElement( SourceElement element )
    {
        if ( isError(element) ){
            return;
        }
        else {
            PersonalVisitor visitor = new PersonalVisitor( );
            element.accept(visitor);
        }
    }
    
    
    List<SourceElement> build(){
        if ( mySource == null ){
            return myList;
        }
        else {
            return Collections.singletonList( mySource );
        }
    }

    /*
     * Offset was used for identifying real offset of node from
     * the beginning of document. Previously <code>node</node>
     * considered inside PHP block , so it has offset inside this
     * PHP block, not from beggining. 
     * <code>offset</code> value was exactly offset of PHP block
     * inside all document.
     * Now all PHP blocks are unioned together and all non-PHP
     * text is replaced by " ". Result text is parsed .
     * So we don't need further for this <code>offset</code>.
     * I keep this method just for the case.   
     */
    void init( ASTNode node , TokenSequence sequence , int offset ) {
        clean();
        myNode = node;
        mySequence = sequence;
        myList = null;
        //myOffset = offset ;
    }
    
    void init( ASTNode node , TokenSequence sequence ) {
        clean();
        myNode = node;
        mySequence = sequence;
        myList = null;
        /*
         * See coments above about <code>offset</code> argument.
         * Consider to delete this attribute at all
         * along with any mention this offset in SourceElementImpl. 
         */
        //myOffset = -1 ;
    }
    
    
    private boolean isError( SourceElement element ) {
        boolean flag = getNode().getNT().equals(ERROR);
        if ( flag ){
            mySource =  new ErrorImpl( element , getNode(), getNode(),
                    mySequence );
        }
        return flag;
    }
    
    private void clean(){
        myList = null;
        myNode = null;
        mySource = null;
        mySequence = null;
        //myOffset = 0;
    }
    
    private ASTNode getNode(){
        return myNode;
    }
    
    private class PersonalVisitor extends PhpModelVisitorAdaptor 
        implements PhpModelVisitor 
    {
        
        public void visit( Variable var ) {
            if ( ExpressionFactory.getInstance().isExpression(getNode())) {
                buildExpression(var);
            }
            else {
                mySource = VariableFactory.getInstance().build( var , 
                        getNode() , mySequence  );
            }
        }
        
        /* (non-Javadoc)
         * @see org.netbeans.modules.php.model.PhpModelVisitor#visit(org.netbeans.modules.php.model.VariableDeclaration)
         */
        public void visit( VariableDeclaration declaration ) {
            if ( ExpressionFactory.getInstance().isExpression(getNode())) {
                buildExpression(declaration);
            }
            else {
                mySource = VariableFactory.getInstance().build( declaration , 
                        getNode() , mySequence  );
            }
        }

        public void visit( Block block ) {
            myList = BlockFactory.getInstance().build( block , getNode() , 
                    mySequence  );
        }
        
        public void visit( CallExpression expression ){
            /*
             * In case of CallMemberExpressionImpl there are another
             * way to create children.
             */
            assert ! ( expression instanceof CallMemberExpressionImpl );
            mySource = CallExpressionFactory.getInstance().build(expression, 
                     getNode(), mySequence); 
        }
        
        public void visit( Arguments args ) {
            ASTNode parent = ((ArgumentsImpl)args).getNarrowNode();
            mySource = ArgumentsFactory.getInstance().build( args, 
                    parent, getNode(), mySequence );
        }
        
        public void visit( ArrayExpression expression ) {
            mySource = ArrayExpressionFactory.getInstance().build(expression, 
                    getNode(), mySequence);
        }
        
        public void visit( AssociativeArrayElement element ) {
            mySource = ExpressionBuilder.getInstance().build(element, 
                    getNode(), null, mySequence);
        }
        
        public void visit( ArrayMemberExpression expression ) {
            // Children should be handled by class impl itself.
            assert false; 
        }
        
        public void visit( ClassMemberExpression expression ) {
            // Children should be handled by class impl itself.
            assert false; 
        }
        
        public void visit( NewExpression expression ) {
            mySource = ArgumentsBuilder.getInstance().build(expression, 
                    getNode(), mySequence);
        }
        
        public void visit( UnaryExpression expression ){
            mySource = ExpressionBuilder.getInstance().build(expression, 
                    getNode(), null, mySequence);
        }
        
        public void visit( BinaryExpression expression ){
            mySource = ExpressionBuilder.getInstance().build(expression, 
                    getNode(), null, mySequence);
        }

        public void visit( DoStatement statement ) {
            assert buildWhileCommon(statement);
        }
        
        public void visit( ExpressionStatement statement ){
            buildExpression(statement);
        }
        
        public void visit( ReturnStatement statement ){
            buildExpression(statement);
        }
        
        public void visit( FunctionDefinition def ){
            mySource = FunctionDefFactory.getInstance().build( def, getNode(), 
                    mySequence);
        }
        
        public void visit ( ClassDefinition def ){
            if ( ClassBodyBuilder.getInstance().isClassBody( getNode() )){
                mySource = ClassBodyBuilder.getInstance().build( def , getNode(), 
                        getNode(), mySequence );
            }
            else {
                assert ClassDefinitionImpl.CLASS_NAME.equals( 
                        getNode().getNT() );
            }
        }
        
        public void visit ( ClassBody body ){
            mySource = ClassBodyFactory.getInstance().build( body , getNode(), 
                    mySequence );
        }

        public void visit ( InterfaceDefinition def  ){
            if ( InterfaceBodyBuilder.getInstance().isInterfaceBody( getNode() )){
                mySource = InterfaceBodyBuilder.getInstance().build( def , getNode(), 
                        getNode(), mySequence );
            }
            else {
                assert ClassDefinitionImpl.CLASS_NAME.equals( 
                        getNode().getNT() );
            }
        }
        
        public void visit ( InterfaceBody body  ){
            mySource = InterfaceBodyFactory.getInstance().build( body, getNode(), 
                    mySequence );
        }
        
        public void visit ( ConstDeclaration decl ){
            mySource = ConstDeclFactory.getInstance().build( decl, getNode(), 
                    mySequence );
        }
        
        public void visit ( ForEachStatement statement  ){
            mySource = ForEachFactory.getInstance().build( statement , getNode(),
                    mySequence );
            if ( mySource == null ) {
                myList = ForEachFactory.getInstance().buildAlternative( 
                        statement , getNode(),mySequence );
            }
        }
        
        public void visit ( ForEach forEach  ){
            if ( VariableDeclarationBuilder.getInstance().isVarDecl( getNode() ) ){
                mySource =  VariableDeclarationBuilder.getInstance().build( 
                        forEach , getNode(),mySequence );
            }
            else { 
                buildExpression(forEach);
            }
        }
        
        public void visit ( ForStatement statement ){
            mySource = ForStatementFactory.getInstance().build( statement , getNode(),
                    mySequence );
            if ( mySource == null ) {
                myList = ForStatementFactory.getInstance().buildAlternative( 
                        statement , getNode(),mySequence );
            }
        }
        
        public void visit ( For forr ){
            mySource = ForFactory.getInstance().build( forr , getNode(), 
                    mySequence );
        }
        
        public void visit ( ForExpression expression ){
            buildExpression( expression );
        }
        
        public void visit ( IfStatement statement ){
            myList = IfStatementFactory.getInstance().build( statement , getNode(),
                    mySequence );
        }
        
        public void visit ( If iff ){
            buildExpression(iff);
        }
        
        public void visit ( ElseIf elseIf ){
            if ( ExpressionFactory.getInstance().isExpression(getNode())) {
                buildExpression(elseIf);
            }
            else {
                buildStatements(elseIf);
            }
        }
        
        public void visit ( Else els ){
            buildStatements(els);
        }

        public void visit ( SwitchStatement statement ){
            myList = SwitchStatementFactory.getInstance().build( statement , 
                    getNode(), mySequence );
        }
        
        public void visit ( Switch switc ){
            buildExpression(switc);
        }
        
        public void visit ( Case caze ){
            if ( ExpressionFactory.getInstance().isExpression(getNode())) {
                buildExpression(caze);
            }
            else {
                buildStatements( caze );
            }
        }
        
        public void visit ( Default def ){
            buildStatements( def );
        }
        
        public void visit( WhileStatement wile ) {
            if ( !buildWhileCommon( wile ) ) {
                myList = WhileStatementFactory.getInstance().build( wile, 
                        getNode() , mySequence );
            }
        }
        
        public void visit( While wile ) {
            buildExpression(wile);
        }
        
        public void visit( FunctionDeclaration declaration ) {
            mySource = FunctionDeclarationFactory.getInstance().build( declaration, 
                    getNode() , mySequence );
        }
        
        public void visit( FormalParameter parameter ) {
            buildExpression( parameter );
        }
        
        public void visit( FormalParameterList parameterList ) {
            mySource = FormalParameterBuilder.getInstance().build( parameterList, 
                    getNode() , getNode(), mySequence );
        }
        
        public void visit( AttributesDeclaration decl ){
            mySource = AttrDeclarationFactory.getInstance().build( decl , 
                    getNode() , mySequence );
        }
        
        public void visit( ClassConst classConst ){
            buildConstant( classConst );
        }
        
        public void visit( Attribute attribute ){
            buildExpression(attribute);
        }
        
        public void visit( GlobalStatement statement ){
            mySource =  VariableDeclarationBuilder.getInstance().build( 
                    statement , getNode(),mySequence );
        }
        
        public void visit( StaticStatement statement ){
            mySource =  VariableDeclarationBuilder.getInstance().build( 
                    statement , getNode(),mySequence );
        }

        public void visit( InitializedDeclaration declaration ) {
            buildExpression( declaration );
        }
        

        @Override
        public void visit( DeclareStatement statement ) {
            mySource = DeclareStatementFactory.getInstance().build( 
                    statement , getNode(),mySequence );
            if ( mySource == null ){
                myList = DeclareStatementFactory.getInstance().buildAlternative( 
                        statement , getNode(),mySequence );
            }
        }
        
        @Override
        public void visit( Declare declare ){
            buildExpression( declare );
        }
        
        private void buildConstant( SourceElement parent ) {
            assert getNode().getNT().equals( ExpressionFactory.EXPRESSION );
            buildExpression( parent );
        }

        private void buildExpression( SourceElement element ) {
            mySource = ExpressionFactory.getInstance().build( element, getNode() , 
                    mySequence );
        }
        
        private boolean buildWhileCommon( SourceElement statement ) {
            if ( StatementsListFactory.getInstance().isStatement(getNode()) ){
                mySource = StatementsListFactory.getInstance().build( statement , 
                        getNode() , mySequence  );
                return true;
            }
            else if ( WhileBuilder.getInstance().isWhile( getNode())){
                mySource = WhileBuilder.getInstance().build( statement , 
                        getNode(), getNode(),mySequence );
                return true;
            }
            return false;
        }
        
        private void buildStatements( StatementContainer els ) {
            if ( StatementsListFactory.getInstance().isStatement(getNode()) )
            {
                mySource = StatementsListFactory.getInstance().build( els , 
                        getNode() , mySequence  );
            }
            else  {
                assert getNode().getNT().equals( BlockFactory.STATEMENT_LIST );
                myList = BlockFactory.getInstance().build( els, getNode() , 
                        mySequence);
            }
        }
        
    }

    
    private ASTNode myNode;
    
    private SourceElement mySource;
    
    private TokenSequence mySequence;
    
    private List<SourceElement> myList;

}
