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

import org.netbeans.modules.php.model.Acceptor;
import org.netbeans.modules.php.model.FormalParameter;
import org.netbeans.modules.php.model.FormalParameterList;
import org.netbeans.modules.php.model.FunctionDeclaration;
import org.netbeans.modules.php.model.FunctionDefinition;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.VariableAppearance;
import org.netbeans.modules.php.model.refs.ReferenceResolver;


/**
 * This resolver is responsible ONLY for simple variable resolving.
 * Complex variables like $a{$b} are not resolvable at all.
 * ( we can resolve $b but this is already other variable inside complex "$a{$b}" 
 * variable ).
 * Variables that are static class attributes are resolved via resolver that is 
 * used for ClassMemberReferenceImpl.  
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.model.refs.ReferenceResolver.class)
public class VariableReferenceResolver implements  ReferenceResolver 
{
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceBuilder#build(org.netbeans.modules.php.model.SourceElement, org.netbeans.modules.php.model.SourceElement, java.lang.Class)
     */
    public <T extends SourceElement> List<T> resolve( SourceElement source, 
            String identifier, Class<T> clazz , boolean exactComparison ) 
    {
        List<T> result = 
            findVariable( source , identifier , clazz , exactComparison);
        
        List<PhpModel> models = ModelResolver.ResolverUtility.getIncludedModels(
                source);
        
        for (PhpModel model : models) {
            model.readLock();
            List<T> vars;
            try {
                vars = findInScope(identifier, clazz, model, null,
                        exactComparison);
            }
            finally {
                model.readUnlock();
            }
            if (vars != null) {
                result.addAll(vars);
            }
            if (exactComparison && result.size() > 0) {
                return result;
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceBuilder#isApplicable(java.lang.Class)
     */
    public <T extends SourceElement> boolean isApplicable( Class<T> clazz ) {
        return VariableAppearance.class.isAssignableFrom( clazz );
    }
    
    private <T extends SourceElement> List<T>  findVariable( SourceElement source, 
            String identifier, Class<T> clazz,  boolean exactComparison) 
    {
        Acceptor scope = 
            source.getParent() == null ? source.getModel(): source.getParent();
        SourceElement childScope = source;
        List<T> result = new LinkedList<T>();
        while ( true ) {
            if ( scope instanceof FunctionDefinition ) {
                List<T> params  = findInFunctionParams( identifier , 
                        (FunctionDefinition)scope , clazz ,  exactComparison );
                if ( params != null ) {
                    result.addAll( params );
                }
                return result;
            }
            else {
                List<T> vars =  findInScope( identifier , clazz , scope ,
                        childScope, exactComparison);
                if ( vars != null ) {
                    result.addAll( vars );
                }
            }
            
            if ( scope instanceof PhpModel ) {
                break;
            }
            
            if ( scope instanceof SourceElement ) {
                childScope = (SourceElement)scope;
                scope = 
                    childScope.getParent() == null ? childScope.getModel():
                        childScope.getParent();
            }
        }
        return result;
    }

    private <T extends SourceElement> List<T> findInScope(  String identifier, 
            Class<T> clazz, Acceptor scope , SourceElement subScope,  
            boolean exactComparison) 
    {
        VariableResolveVisitor<T> visitor = new VariableResolveVisitor<T>( 
                identifier, subScope , clazz , exactComparison );
        scope.accept(visitor);
        return visitor.getResult();
    }

    private <T extends SourceElement> List<T> findInFunctionParams( 
            String identifier, FunctionDefinition definition ,  Class<T> clazz, 
            boolean exactComparison) 
    {
        FunctionDeclaration decl = definition.getDeclaration();
        if ( decl == null ) {
            return null;
        }
        FormalParameterList params = decl.getParamaterList();
        if ( params == null ) {
            return null;
        }
        List<T> result = new LinkedList<T>();
        List<FormalParameter> parameters = params.getParameters();
        for (FormalParameter parameter : parameters) {
            if ( !clazz.isAssignableFrom( parameter.getElementType() )){
                continue;
            }
            String name = parameter.getName();
            if ( name == null ) {
                continue;
            }
            if ( exactComparison && identifier.equals( name )) {
                result.add( clazz.cast( parameter ));
            }
            if ( !exactComparison && name.startsWith( identifier )) {
                result.add( clazz.cast( parameter ));
            }
        }
        return result;
    }

    
}
