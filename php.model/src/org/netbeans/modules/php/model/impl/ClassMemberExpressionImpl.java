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

import java.util.LinkedList;
import java.util.List;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.CallExpression;
import org.netbeans.modules.php.model.ClassMemberExpression;
import org.netbeans.modules.php.model.Expression;
import org.netbeans.modules.php.model.IdentifierExpression;
import org.netbeans.modules.php.model.PhpModelVisitor;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.builders.CallExpressionBuilder;
import org.netbeans.modules.php.model.impl.builders.ClassMemberBuilder;
import org.netbeans.modules.php.model.impl.builders.IdentifierBuilder;
import org.netbeans.modules.php.model.impl.factory.CallExpressionFactory;
import org.netbeans.modules.php.model.impl.factory.ExpressionFactory;


/**
 * @author ads
 *
 */
public class ClassMemberExpressionImpl extends SourceElementImpl implements
        ClassMemberExpression
{

    public ClassMemberExpressionImpl( SourceElement parent, ASTNode node, 
            ASTNode realNode, ASTNode memberAccessNode ,
            TokenSequence<?> sequence ) 
    {
        super(parent, node, realNode, sequence);
        myMemberNode = memberAccessNode;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.ClassMemberExpression#getExpression()
     */
    public Expression getExpression() {
        List<Expression> children = getChildren( Expression.class );
        for (Expression expression : children) {
            /*
             * No need in this check.
             * If expression is complex ( owner identidier is itself 
             * member access ) then owner has the same node as this class
             * ( and this is MemberAccessExpression type ).
             * If expression is simple then owner has IdentifierExpression
             * type. In both cases check below failed. 
            if ( expression == getOwnerIdentifier() ){
                continue;
            }*/
            assert expression instanceof SourceElementImpl;
            SourceElementImpl impl = (SourceElementImpl) expression;
            String nt = impl.getNarrowNode().getNT();
            if ( nt.equals( CallExpressionBuilder.MEMBER_EXPRESSION )) {
                return expression;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.MemberExpression#getCallExpression()
     */
    public CallExpression getCallExpression() {
        List<CallExpression> children = getChildren( CallExpression.class );
        for ( CallExpression expression : children ){
            assert expression instanceof SourceElementImpl;
            SourceElementImpl impl = (SourceElementImpl) expression;
            if ( impl.getNode() == getNode() ) {
                return (CallExpression)impl;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.MemberExpression#getOwnerIdentifier()
     */
    public IdentifierExpression getOwnerIdentifier() {
        if ( getCallExpression() != null ) {
            return null;
        }
        return getChild( IdentifierExpression.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getElementType()
     */
    public Class<? extends SourceElement> getElementType() {
        return ClassMemberExpression.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Acceptor#accept(org.netbeans.modules.php.model.PhpModelVisitor)
     */
    public void accept( PhpModelVisitor visitor ) {
        visitor.visit( this );
    }
    
    @Override
    protected List<SourceElement> createChildrenList()
    {
        ASTNode node = getNarrowNode();
        List<ASTItem> nodes = node.getChildren();
        ASTNode before = null;
        ASTNode beforeBefore = null;
        for (ASTItem item : nodes) {
            if ( !(item instanceof ASTNode ) ){
                continue;
            }
            ASTNode child = (ASTNode) item;
            if ( child != getMemberNode() ) {
                beforeBefore = before;
                before = child;
            }
            else {
                break;
            }
        }
        
        SourceElement memberExpression =  
            ClassMemberBuilder.getInstance().build( this , getMemberNode() ,
                    getMemberNode(), getTokenSequence() );
                        
        List<SourceElement> children = new LinkedList<SourceElement>();
        assert before != null;
        if ( before.getNT().equals( CallExpressionFactory.IDENTIFIER )) {
            // this is the case : $a[ $i ];
            SourceElement sourceElement = IdentifierBuilder.getInstance().
                build( this, before , before , getTokenSequence() );
            children.add( sourceElement );
        }
        else {
            assert before.getNT().equals( CallExpressionBuilder.MEMBER) ;
            if ( before.getNode( CallExpressionBuilder.ARGS) != null ) {
                SourceElement callExpression = null;
                assert beforeBefore!= null;
                if ( beforeBefore.getNT().equals(CallExpressionBuilder.MEMBER ) ) {
                    callExpression = new CallMemberExpressionImpl(
                        this , getNode() , getNarrowNode() , beforeBefore,
                        before , getTokenSequence()); 
                }
                else {
                    assert beforeBefore.getNT().equals( 
                            CallExpressionFactory.IDENTIFIER );
                    callExpression = new CallExpressionImpl( this , getNode() ,
                            getNarrowNode() , before , getTokenSequence() );
                }
                children.add( callExpression );
            }
            else if ( before.getNode( ExpressionFactory.EXPRESSION) != null ) {
                SourceElement arrayMember = new ArrayMemberExpressionImpl( this ,
                        getNode() , getNarrowNode() , before , getTokenSequence() );
                children.add( arrayMember );
            }
            else if ( before.getNode( CallExpressionBuilder.MEMBER_EXPRESSION) 
                    != null) 
            {
                /*
                 * 'before' could be method name or attribute name 
                 */
                SourceElement classMember = new ClassMemberExpressionImpl( this ,
                        getNode() , getNarrowNode() , before , getTokenSequence() );
                children.add( classMember );
            }
            else {
                assert false;
            }
        }

        children.add( memberExpression );
        return children;
    }
    
    private ASTNode getMemberNode() {
        return myMemberNode;
    }

    private ASTNode myMemberNode;
}
