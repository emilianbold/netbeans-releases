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

import java.util.List;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.ArrayMemberExpressionImpl;
import org.netbeans.modules.php.model.impl.CallExpressionImpl;
import org.netbeans.modules.php.model.impl.CallMemberExpressionImpl;
import org.netbeans.modules.php.model.impl.ClassMemberExpressionImpl;
import org.netbeans.modules.php.model.impl.factory.ExpressionFactory;


/**
 * @author ads
 *
 */
public class CallExpressionBuilder implements SourceElementBuilder {
    
    public  static final String CALL_EXPR           = "CallExpression";         // NOI18N
    
    public  static final String MEMBER              = "MemberAccess";           // NOI18N
    
    public  static final String ARGS                = "Arguments";              // NOI18N
    
    public  static final String MEMBER_EXPRESSION   = "MemberExpression";       // NOI18N
    
    
    private CallExpressionBuilder() {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.PhpModel, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
     */
    public SourceElement build( PhpModel model, ASTNode node, ASTNode realNode ,
            TokenSequence<?> sequence )
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
        if ( !realNode.getNT().equals( CALL_EXPR ) ){
            // this is case of simple call expression for buil-in functions, echo , include,..
            return new CallExpressionImpl( parent , node , realNode , sequence );
        }
        
        List<ASTItem> children = realNode.getChildren();
        ASTNode last = null;
        ASTNode beforeLast = null;
        for (int i = children.size() - 1; i >= 0; i--) {
            ASTItem item = children.get(i);
            if ( !(item instanceof ASTNode)) {
                continue;
            }
            ASTNode child = (ASTNode) item;
            if (child.getNT().equals(MEMBER)) {
                if (last == null) {
                    last = child;
                }
                else {
                    beforeLast = child;
                    break;
                }
            }
        }
        assert last!= null;
        
        if ( last.getNode(ARGS) != null ) {
            // this is case when we have call expression
            if ( beforeLast == null ) {
                // only one MemberAccess which is Arguments, so this is just usual function call
                return new CallExpressionImpl( parent, node , realNode, sequence );
            }
            else {
                /*
                 *  This is the case when we have function call but in complex case :
                 *  function is either class member or via array memeber access.
                 */ 
                return new CallMemberExpressionImpl( parent, node , realNode, 
                        beforeLast , last , sequence );
            }
        }
        
        ASTNode classMemberAccess = last.getNode( MEMBER_EXPRESSION );
        if ( classMemberAccess != null ) {
            // In this case this is class attribute.
            return new ClassMemberExpressionImpl( parent , node,
                    realNode , last , sequence );
        }
        
        ASTNode arrayMemberAccess = last.getNode( ExpressionFactory.EXPRESSION );
        if ( arrayMemberAccess != null ) {
            // In this case this is array member access .
            return new ArrayMemberExpressionImpl( parent , node,
                    realNode , last , sequence );
        }
        
        assert false;
        return null;
    }
    
    public static CallExpressionBuilder getInstance() {
        return INSTANCE;
    }
    
    
    private static final CallExpressionBuilder INSTANCE = new CallExpressionBuilder();

}
