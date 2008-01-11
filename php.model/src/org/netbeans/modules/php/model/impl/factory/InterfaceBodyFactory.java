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

import java.util.HashSet;
import java.util.Set;

import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.php.model.InterfaceStatement;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.impl.AbstractFunctionDeclarationImpl;
import org.netbeans.modules.php.model.impl.Utils.NodeFinder;
import org.netbeans.modules.php.model.impl.builders.ConstDeclarationBuilder;


/**
 * @author ads
 *
 */
public class InterfaceBodyFactory {
    
    public static final String CONST_DECL  = "ClassConstDeclaration";       // NOI18N
    
    public static final String FUNC_DECL   = "InterfaceFunctionDeclaration";// NOI18N

    private InterfaceBodyFactory(){
    }
    
    public static final InterfaceBodyFactory getInstance(){
        return INSTANCE;
    }
    
    public InterfaceStatement build( SourceElement parent , ASTNode node , 
            TokenSequence<?> sequence )
    {
        NodeFinder finder = new NodeFinder( node , CHILDREN_TYPES );
        finder.check();
        assert finder.isFound();
        ASTNode real = finder.getNode();
        String type = finder.getType();
        if ( FUNC_DECL.equals( type ) ){
            ASTNode declNode = real.getNode( FunctionDefFactory.FUNCTION_DECL );
            return new AbstractFunctionDeclarationImpl( parent , node, real , 
                    declNode , sequence );
        }
        else if ( CONST_DECL.equals(  type )){
            return ConstDeclarationBuilder.getInstance().build(parent, node, real , sequence);
        }
        else {
            assert false;
            return null;
        }
    }
    
    private static final InterfaceBodyFactory INSTANCE = 
        new InterfaceBodyFactory();
    
    private static Set<String> CHILDREN_TYPES;
    
    static {
        CHILDREN_TYPES = new HashSet<String>();
        CHILDREN_TYPES.add( FUNC_DECL );
        CHILDREN_TYPES.add( CONST_DECL );
    }
}
