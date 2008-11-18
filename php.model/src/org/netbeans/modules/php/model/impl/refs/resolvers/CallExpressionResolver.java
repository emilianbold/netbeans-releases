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

import org.netbeans.modules.php.model.CallExpression;
import org.netbeans.modules.php.model.ExpressionStatement;
import org.netbeans.modules.php.model.IdentifierExpression;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.Statement;
import org.netbeans.modules.php.model.refs.ReferenceResolver;


/**
 * This resolver resolves call expressions with given function name.
 * It is used at least in constant resolving ( "define" can be used as
 * name of function , in this case resolver finds all "define" calls
 * and one could check its argument for constant name check ). 
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.model.refs.ReferenceResolver.class)
public class CallExpressionResolver implements ReferenceResolver {

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceResolver#isApplicable(java.lang.Class)
     */
    public <T extends SourceElement> boolean isApplicable( Class<T> clazz ) {
        return CallExpression.class.isAssignableFrom( clazz );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceResolver#resolve(org.netbeans.modules.php.model.SourceElement, java.lang.String, java.lang.Class, boolean)
     */
    public <T extends SourceElement> List<T> resolve( SourceElement source,
            String identifier, Class<T> clazz, boolean exactComparison )
    {
        List<T> result = new LinkedList<T>(); 
        
        collectCallExpressions(source, identifier, clazz, exactComparison,
                result);
        
        
        if ( exactComparison && result.size() >0 ){
            return result;
        }
        
        List<PhpModel> models = ModelResolver.ResolverUtility.getIncludedModels(
                source);
        
        for (PhpModel model : models) {
            model.readLock();
            try {
                List<Statement> statements = model.getStatements();
                findCallExpression(statements, identifier, clazz,
                        exactComparison, result);
            }
            finally {
                model.readUnlock();
            }
            
            if (exactComparison && result.size() > 0) {
                return result;
            }
        }
        return result;
    }

    private <T extends SourceElement> void collectCallExpressions( 
            SourceElement source,String identifier, Class<T> clazz, 
            boolean exactComparison,List<T> result )
    {
        List<? extends SourceElement> list;
        boolean proceed = false;
        if ( source.getParent() == null ){
            list = source.getModel().getStatements();
        }
        else {
            list = source.getParent().getChildren();
            proceed = true;
        }
        
        findCallExpression( list , identifier , clazz , 
                exactComparison , result);
        
        if ( proceed ){
            collectCallExpressions(source.getParent(), identifier, clazz, 
                exactComparison, result);
        }
    }
    
    /*
     * TODO : this is flat search algorithm. Possibly it should be changed with
     * visitor pattern like variable search.
     */ 
    private <T extends SourceElement> void findCallExpression( 
            List<? extends SourceElement> list , String identifier,Class<T> clazz , 
            boolean exactComparison , List<T> collected)
    {
        for (SourceElement element : list) {
            if ( element instanceof ExpressionStatement ){
                ExpressionStatement statement = (ExpressionStatement)element;
                element = statement.getExpression();
            }
            if ( !clazz.isAssignableFrom( element.getElementType()) ){
                continue;
            }
            IdentifierExpression idExpression = 
                ((CallExpression)element).getName();
            if ( exactComparison && idExpression.getText().equals( identifier) ){
                collected.add( clazz.cast( element) );
            }
            else if ( !exactComparison && idExpression.getText().
                    startsWith( identifier) )
            {
                collected.add( clazz.cast( element ) );
            }
        }
    }

}
