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
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.Arguments;
import org.netbeans.modules.php.model.CallExpression;
import org.netbeans.modules.php.model.IdentifierExpression;
import org.netbeans.modules.php.model.PhpModelVisitor;
import org.netbeans.modules.php.model.SourceElement;


/**
 * It corresponds  not only CallExpression gramatic node
 * but also 
 * - echo expression.
 * - built-in functions call expression
 * - include expression 
 * @author ads
 *
 */
public class CallExpressionImpl extends SourceElementImpl implements
        CallExpression
{

    public CallExpressionImpl( SourceElement parent, ASTNode node, 
            ASTNode realNode , TokenSequence sequence ) 
    {
        super(parent, node, realNode , sequence);
    }
    
    public CallExpressionImpl( SourceElement parent, ASTNode node, 
            ASTNode realNode , ASTNode args , TokenSequence sequence ) 
    {
        this( parent , node, realNode , sequence );
        myArgs = args; 
    }
    

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.CallExpression#getName()
     */
    public IdentifierExpression getName() {
        return getChild( IdentifierExpression.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.SourceElement#getElementType()
     */
    public Class<? extends SourceElement> getElementType() {
        return CallExpression.class;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Acceptor#accept(org.netbeans.modules.php.model.PhpModelVisitor)
     */
    public void accept( PhpModelVisitor visitor ) {
        visitor.visit( this );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.CallExpression#getArguments()
     */
    public Arguments getArguments() {
        return getChild( Arguments.class );
    }

    protected List<SourceElement> createChildrenList( ) {
        if( getArgs() == null ){
            return super.createChildrenList();
        }
        ASTNode node = getNarrowNode();
        List<SourceElement> children = new LinkedList<SourceElement>();
        List<ASTItem> list = node.getChildren();
        for (ASTItem item : list) {
            if (item instanceof ASTNode) {
                addChildNode(children, (ASTNode) item);
                /*
                 * 
                 * This 'break' need for complex call expressions with 
                 * members ( array or class ) access. F.e.
                 * expression :
                 * func( $arg ) -> method( $var );
                 * will be treated as node without deep tree subnodes, 
                 * realNode here will be whole node represented expression
                 * 'func( $arg ) -> method( $var )'. But we don't want 
                 * recognize '->method' , $var as children for call expression:
                 * func( $arg ). ( This expression will be parsed in the way :
                 * Call Expression with identifier 'func( $arg ) -> method'
                 * and argument $var. Identifier here is ClassMemberExpression.
                 * ClassMember expression contains as child Call Expression 
                 * 'func( $arg )' but all these expressions have the same 
                 * realNode. ).
                 */
                if ( item == getArgs() ){
                    break;
                }
            }
            else if (item instanceof ASTToken) {
                // TODO : handle comments
            }
        }
        return children;
    }
    
    private ASTNode getArgs(){
        return myArgs;
    }
    
    private ASTNode myArgs;
}
