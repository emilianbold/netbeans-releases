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
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.ConstantImpl;
import org.netbeans.modules.php.model.impl.ErrorImpl;
import org.netbeans.modules.php.model.impl.Utils;
import org.netbeans.modules.php.model.impl.VariableImpl;
import org.netbeans.modules.php.model.impl.factory.ExpressionFactory;


/**
 * @author ads
 *
 */
public class ClassMemberBuilder implements SourceElementBuilder {
    
    private ClassMemberBuilder() {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.PhpModel, org.netbeans.api.languages.ASTNode, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
     */
    public SourceElement build( PhpModel model, ASTNode node, ASTNode realNode, 
            TokenSequence<?> sequence ) 
    {
        assert false;
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.api.languages.ASTNode, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
     */
    public SourceElement build( SourceElement parent, ASTNode node, 
            ASTNode realNode, TokenSequence<?> sequence ) 
    {
        assert CallExpressionBuilder.MEMBER.equals( realNode.getNT() );

        ASTNode member = realNode.getNode( CallExpressionBuilder.MEMBER_EXPRESSION );
        if ( member == null ) {
            return new ErrorImpl( parent , node , realNode , sequence );
        }
        
        if ( member.getNode( ExpressionFactory.EXPRESSION ) != null ) {
            return new VariableImpl( parent , node , member , sequence );
        }
        
        List<ASTItem> list = member.getChildren();
        if ( list.size() == 0 ) {
            return null;
        }
        ASTItem item = list.get( 0 );
        assert item instanceof ASTToken;
        ASTToken token = (ASTToken) item;
        if ( token.getTypeName().equals( Utils.VARIABLE) ) {
            return new VariableImpl( parent , node , member , sequence );
        }
        else {
            return new ConstantImpl( parent , node , member , sequence );
        }
    }

    public static ClassMemberBuilder getInstance() {
        return INSTANCE;
    }
    
    private static final ClassMemberBuilder INSTANCE = new ClassMemberBuilder();

}
