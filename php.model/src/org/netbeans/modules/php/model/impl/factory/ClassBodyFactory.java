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
package org.netbeans.modules.php.model.impl.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.ClassBody;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.ErrorImpl;
import org.netbeans.modules.php.model.impl.builders.AttributeDeclBuilder;
import org.netbeans.modules.php.model.impl.builders.ClassFuncDefBuilder;
import org.netbeans.modules.php.model.impl.builders.ClassFunctionDeclarationBuilder;
import org.netbeans.modules.php.model.impl.builders.ConstDeclarationBuilder;
import org.netbeans.modules.php.model.impl.builders.SourceElementBuilder;


/**
 * @author ads
 *
 */
public class ClassBodyFactory implements SourceElementBuilder {
    
    private static final String  ATTRIBUTE  = "ClassVarDeclaration";        // NOI81N
    
    private static final String  ABSTRACT_FUNC
                                            = "AbstractFunctionDeclaration";// NOI81N
    
    private static final String STATEMENT   = "ClassStatement";             // NOI81N
    
    private static final String FUNC_DEF    = "ClassFunctionDefinition";    // NOI81N
    
    public  static final String CLASS_MEMBER= "ClassMember";                // NOI81N
    
    private ClassBodyFactory(){
    }
    
    public static ClassBodyFactory getInstance(){
        return INSTANCE;
    }
    
    public SourceElement build( ClassBody body, ASTNode node, 
            TokenSequence sequence ) 
    {
        String type = node.getNT();
        assert type.equals( STATEMENT );
        if ( node.getNode( InterfaceBodyFactory.CONST_DECL) != null ){
            return ConstDeclarationBuilder.getInstance().build( body, node, 
                    node.getNode( InterfaceBodyFactory.CONST_DECL), sequence);
        }
        else if ( node.getNode( CLASS_MEMBER) != null ){
            return build( body , node, node.getNode( CLASS_MEMBER) , sequence );
        }
        else {
            return new ErrorImpl( body , node , node , sequence );
        }
    } 
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.PhpModel, org.netbeans.api.languages.ASTNode, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
     */
    public SourceElement build( PhpModel model, ASTNode node, ASTNode realNode, 
            TokenSequence sequence ) 
    {
        assert false;
        return null;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.impl.factory.SourceElementBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.api.languages.ASTNode, org.netbeans.api.languages.ASTNode, org.netbeans.api.lexer.TokenSequence)
     */
    public SourceElement build( SourceElement parent, ASTNode node, 
            ASTNode realNode, TokenSequence sequence ) 
    {
        String type = realNode.getNT();
        assert type.equals( CLASS_MEMBER );
        for( Entry<String, SourceElementBuilder> entry : TYPES.entrySet() ){
            String subType = entry.getKey();
            ASTNode subNode= realNode.getNode( subType);
            if ( subNode != null ){
                return entry.getValue().build(parent, node, subNode, sequence);
            }
        }
        return new ErrorImpl( parent , node , realNode , sequence );
    }

    private static final Map<String,SourceElementBuilder> TYPES = new 
        HashMap<String, SourceElementBuilder>();
    
    private static final ClassBodyFactory INSTANCE = new ClassBodyFactory();

    static {
        TYPES.put( FUNC_DEF , ClassFuncDefBuilder.getInstance() );
        TYPES.put( ABSTRACT_FUNC, ClassFunctionDeclarationBuilder.getInstance() );
        TYPES.put( ATTRIBUTE , AttributeDeclBuilder.getInstance());
    }

}
