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

import java.util.List;

import org.netbeans.modules.php.model.Arguments;
import org.netbeans.modules.php.model.CallExpression;
import org.netbeans.modules.php.model.ClassFunctionDeclaration;
import org.netbeans.modules.php.model.Expression;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.Reference;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.refs.ReferenceResolver;


/**
 * This reference can return reference to call expression ( with 
 * 'define' function ) or user function definition .
 * So method "get()" return type could be CallExpression or FunctionDefinition. 
 * @author ads
 *
 */
class ConstantReferenceImpl extends ReferenceImpl<SourceElement> 
    implements Reference<SourceElement> {

    private static final String DEFINE = "define";      // NOI18N

    ConstantReferenceImpl( SourceElementImpl source, String identifier ) {
        super(source, identifier, null );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.Reference#get()
     */
    public SourceElement get() {
        if ( getSource().getParent() instanceof CallExpression ){
            return findFunctionDefinition( );
        }
        else {
            return findDeclareCall();
        }
    }

    private SourceElement findFunctionDefinition() {
        List<ReferenceResolver> resolvers = getResolvers( 
                FunctionDefinition.class );
        for ( ReferenceResolver resolver : resolvers ){
            List<FunctionDefinition> defs = resolver.resolve( getSource(), 
                    getIdentifier(), FunctionDefinition.class, true );
            if ( defs.size() >0 ){
                return defs.get(0);
            }
        }
        resolvers = getResolvers( 
                ClassFunctionDeclaration.class );
        for ( ReferenceResolver resolver : resolvers ){
            List<ClassFunctionDeclaration> decls = resolver.resolve( getSource(), 
                    getIdentifier(), ClassFunctionDeclaration.class, true );
            if ( decls.size() >0 ){
                return decls.get(0);
            }
        }
        return null;
    }

    private SourceElement findDeclareCall() {
        List<ReferenceResolver> resolvers = getResolvers( 
                CallExpression.class );
        for ( ReferenceResolver resolver : resolvers ){
            List<CallExpression> expressions = resolver.resolve(getSource(), 
                    DEFINE, CallExpression.class, true);
            SourceElement found = findDeclareCall( expressions );
            if( found != null ){
                return found;
            }
        }
        return null;
    }

    private SourceElement findDeclareCall( List<CallExpression> callExpressions ){
        for ( CallExpression expression : callExpressions ){
            Arguments args = expression.getArguments();
            if ( args == null ){
                continue;
            }
            List<Expression> arguments = args.getArgumentsList();
            Expression defineConstant = arguments.get( 0 );
            if ( defineConstant == null ){
                continue;
            }
            String constant = defineConstant.getText();
            if ( !constant.contains( getIdentifier()) ){
                continue;
            }
            constant = constant.replace( getIdentifier(), "");
            if ( constant.length() == 2){
                boolean isString = constant.charAt( 0  ) == '"' && 
                    constant.charAt( 1  ) == '"';
                if ( isString ){
                    return expression;
                }
                isString = constant.charAt( 0  ) == '\'' && 
                    constant.charAt( 1  ) == '\'';
                if ( isString ){
                    return expression;
                }
            }
        }
        return null;
    }
    
}
