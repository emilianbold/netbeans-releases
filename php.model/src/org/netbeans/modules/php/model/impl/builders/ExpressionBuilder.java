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
package org.netbeans.modules.php.model.impl.builders;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.ArrayExpressionImpl;
import org.netbeans.modules.php.model.impl.BinaryExpressionImpl;
import org.netbeans.modules.php.model.impl.CallExpressionImpl;
import org.netbeans.modules.php.model.impl.ErrorImpl;
import org.netbeans.modules.php.model.impl.NewExpressionImpl;
import org.netbeans.modules.php.model.impl.TernaryExpressionImpl;
import org.netbeans.modules.php.model.impl.UnaryExpressionImpl;
import org.netbeans.modules.php.model.impl.Utils;
import org.netbeans.modules.php.model.impl.VariableImpl;
import org.netbeans.modules.php.model.impl.Utils.ErrorFinder;
import org.netbeans.modules.php.model.impl.Utils.NodeFinder;
import org.netbeans.modules.php.model.impl.factory.CallExpressionFactory;
import org.netbeans.modules.php.model.impl.factory.ExpressionFactory;


/**
 * @author ads
 *
 */
public class ExpressionBuilder implements SourceElementBuilder {
    
    private static final String LOGICAL_OR      = "LogicalORExpression";    // NOI18N
    
    private static final String LOGICAL_XOR     = "LogicalXORExpression";   // NOI18N
    
    private static final String LOGICAL_AND     = "LogicalANDExpression";   // NOI18N
    
    private static final String ASSIGN_EXPR     = "AssignmentExpression";   // NOI18N
    
    private static final String CONDITIION      = "ConditionalExpression";  // NOI18N
    
    private static final String LOGICAL_OR_OR   = "LogicalORORExpression";  // NOI18N
    
    private static final String LOGICAL_AND_AND = "LogicalANDANDExpression";// NOI18N    
    
    private static final String BITWISE_OR      = "BitwiseORExpression";    // NOI18N
    
    private static final String BITWISE_XOR     = "BitwiseXORExpression";   // NOI18N
    
    private static final String BITWISE_AND     = "BitwiseANDExpression";   // NOI18N
    
    private static final String EQUALITY_EXPR   = "EqualityExpression";     // NOI18N
    
    private static final String RELATION_EXPR   = "RelationalExpression";   // NOI18N
    
    private static final String BIT_SHIFT_EXPR  = "BitShiftExpression";     // NOI18N
    
    private static final String ADDITIVE_EXPR   = "AdditiveExpression";     // NOI18N
    
    private static final String MULT_EXPR       = "MultiplicativeExpression";// NOI18N
    
    private static final String INSTANCE_OF     = "InstanceOfExpression";   //  NOI18N  
    
    private static final String POSTFIX_EXPR    = "PostfixExpression";      // NOI18N    
    
    private static final String UNARY           = "UnaryExpression";        // NOI18N
    
    
    private static final String NEW_EXPR        = "NewExpression";          // NOI18N
    
    public static final String BUILT_IN_CALL    = "BuiltInCallExpression";  // NOI18N
    
    public static final String LIST_CALL        = "ListExpression";         // NOI18N
    
    public static final String INCLUDE          = 
					"LanguageConstructExpression";      // NOI18N
    
    private static final String ARRAY           = "ArrayExpression";        // NOI18N
    
    private static final String PRIMARY         = "PrimaryExpression";      // NOI18N
    
    public  static final String CONSTANT        = "ConstantExpression";     // NOI18N
    
    /*
     * AST node types that are not represented by OM ( they should be tokens
     * but they are nodes for simplicity of nbs file ).
     */
    
    public static final String ASSIGN_OP        = "AssignmentOperator";     // NOI18N
    
    public static final String EQUAL_OP         = "EqualityOperator";       // NOI18N
    
    public static final String REL_OP           = "RelationalOperator";     // NOI18N
    
    public static final String BIT_SHIFT_OP     = "BitShiftOperator";       // NOI18N
    
    public static final String ADD_OP           = "AdditiveOperator";       // NOI18N
    
    public static final String MULT_OP          = "MultiplicativeOperator"; // NOI18N
    
    public static final String UNARY_OP         = "UnaryOperator";          // NOI18N
    
    private ExpressionBuilder() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.PhpModel, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
     */
    public SourceElement build( PhpModel model, ASTNode node,
            ASTNode realNode ,TokenSequence<?> sequence )
    {
        assert false;
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
     */
    public SourceElement build( SourceElement parent, ASTNode node,
            ASTNode realNode ,TokenSequence<?> sequence )
    {
        ASTNode startNode = realNode == null ? node : realNode;
        ASTNode real = Utils.getNarrowNode( startNode );
        String type = real.getNT();
        if ( CONDITIION.equals( type )){
            return new TernaryExpressionImpl( parent , node, real , sequence );
        }
        if ( BINARY_EXPRESSIONS.contains( type ) ) {
            return new BinaryExpressionImpl( parent , node , real, sequence );
        }
        else if ( UNARY_EXPRESSIONS.contains( type )) {
            return new UnaryExpressionImpl( parent , node , real, sequence ,
                    type.equals( POSTFIX_EXPR ));
        }
        else if ( OPERATORS.contains( type )){
            return null;
        }
        else {
            NodeFinder finder = new NodeFinder( startNode , TYPES.keySet());
            finder.check();
            
            if ( finder.isFound() ){
                real = finder.getNode();
                type = finder.getType();
                return TYPES.get( type ).build(parent, node, real, sequence);
            }
            else {
                finder = new ErrorFinder( startNode );
                finder.check();
                assert finder.isFound(): "Not found expected type inside " +
                        startNode.getNT();
                if ( !finder.isFound() ){
                    // avoiding NPE when assertions are switched off
                    return null;
                }
                return new ErrorImpl( parent , node , finder.getNode() , sequence );
            }
        }
    }
    
    public SourceElementBuilder get( String type ) {
        return TYPES.get(type);
    }
    
    public static ExpressionBuilder getInstance() {
        return INSTANCE;
    }
    
    private static final ExpressionBuilder INSTANCE = new ExpressionBuilder();
 
    private static final Set<String>  BINARY_EXPRESSIONS = new HashSet<String>();
    
    private static final Set<String>  UNARY_EXPRESSIONS  = new HashSet<String>();
    
    private static final Set<String>  OPERATORS          = new HashSet<String>();
    
    private static final Map<String,SourceElementBuilder> TYPES 
                = new HashMap<String,SourceElementBuilder>();

    static {
        BINARY_EXPRESSIONS.add( LOGICAL_AND );
        BINARY_EXPRESSIONS.add( LOGICAL_AND_AND );
        BINARY_EXPRESSIONS.add( LOGICAL_OR );
        BINARY_EXPRESSIONS.add( LOGICAL_OR_OR );
        BINARY_EXPRESSIONS.add( LOGICAL_XOR );
        BINARY_EXPRESSIONS.add( ASSIGN_EXPR );
        BINARY_EXPRESSIONS.add( BIT_SHIFT_EXPR );
        BINARY_EXPRESSIONS.add( BITWISE_AND );
        BINARY_EXPRESSIONS.add( BITWISE_OR );
        BINARY_EXPRESSIONS.add( BITWISE_XOR );
        BINARY_EXPRESSIONS.add( EQUALITY_EXPR );
        BINARY_EXPRESSIONS.add( RELATION_EXPR );
        BINARY_EXPRESSIONS.add( ADDITIVE_EXPR );
        BINARY_EXPRESSIONS.add( MULT_EXPR );
        BINARY_EXPRESSIONS.add( INSTANCE_OF );
        
        UNARY_EXPRESSIONS.add( POSTFIX_EXPR );
        UNARY_EXPRESSIONS.add( UNARY );
        
        OPERATORS.add( ASSIGN_OP );
        OPERATORS.add( EQUAL_OP );
        OPERATORS.add( REL_OP );
        OPERATORS.add( BIT_SHIFT_OP );
        OPERATORS.add( ADD_OP );
        OPERATORS.add( MULT_OP );
        OPERATORS.add( UNARY_OP );

        TYPES.put( CallExpressionBuilder.CALL_EXPR,  
                CallExpressionBuilder.getInstance() );
        TYPES.put( NEW_EXPR , new NewExpressionBuilder() );
        TYPES.put( BUILT_IN_CALL ,  CallExpressionBuilder.getInstance() );
        TYPES.put( LIST_CALL ,  new ListExpressionBuilder());
        TYPES.put( INCLUDE , CallExpressionBuilder.getInstance()  ); 
        TYPES.put( ARRAY , new ArrayExpressionBuilder() );
        TYPES.put( PRIMARY , new PrimaryExpressionBuilder() );
        TYPES.put( CONSTANT, LiteralBuilder.getInstance() );
        TYPES.put( StaticExpressionBuilder.CLASS_STATIC, StaticExpressionBuilder
                .getInstance() );
        TYPES.put( CallExpressionFactory.IDENTIFIER , 
                IdentifierBuilder.getInstance());
    }
    
    static class NewExpressionBuilder implements SourceElementBuilder {

        public SourceElement build( PhpModel model, ASTNode node, 
                ASTNode realNode ,TokenSequence<?> sequence ) 
        {
            assert false;
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
         */
        public SourceElement build( SourceElement parent, ASTNode node, 
                ASTNode realNode ,TokenSequence<?> sequence ) 
        {
            return new NewExpressionImpl( parent , node , realNode, sequence );
        }
        
    }
    
    static class PrimaryExpressionBuilder implements SourceElementBuilder {

        public SourceElement build( PhpModel model, ASTNode node, 
                ASTNode realNode ,TokenSequence<?> sequence ) 
        {
            assert false;
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
         */
        public SourceElement build( SourceElement parent, ASTNode node, 
                ASTNode realNode ,TokenSequence<?> sequence ) 
        {
            ASTNode expr = realNode.getNode( ExpressionFactory.EXPRESSION );
            if ( expr != null ) {
                /*
                 * This is not exactly unary epxression , this 
                 * is expression surrounded by "(" , ")".
                 */
                return new UnaryExpressionImpl( parent , node , realNode,
                        sequence );
            }
            ASTNode var = realNode.getNode( StaticExpressionBuilder.VARIABLE );
            if ( var!= null ) {
                return new VariableImpl( parent , node , var, 
                        sequence );
            }
            assert false;
            return null;
        }
        
    }
    
    static class ArrayExpressionBuilder implements SourceElementBuilder {

        public SourceElement build( PhpModel model, ASTNode node, 
                ASTNode realNode ,TokenSequence<?> sequence ) 
        {
            assert false;
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
         */
        public SourceElement build( SourceElement parent, ASTNode node, 
                ASTNode realNode ,TokenSequence<?> sequence ) 
        {
            return new ArrayExpressionImpl( parent , node, realNode , sequence );
        }
        
    }
    
    static class ListExpressionBuilder implements SourceElementBuilder {

        public SourceElement build( PhpModel model, ASTNode node,
                ASTNode realNode, TokenSequence<?> sequence )
        {
            assert false;
            return null;
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
         */
        public SourceElement build( SourceElement parent, ASTNode node,
                ASTNode realNode, TokenSequence<?> sequence )
        {
            return new CallExpressionImpl(parent, node, realNode, sequence);
        }
    }
}
