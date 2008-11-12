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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.php.model.InterfaceDefinition;
import org.netbeans.modules.php.model.ObjectDefinition;
import org.netbeans.modules.php.model.PhpModel;
import org.netbeans.modules.php.model.SourceElement;
import org.netbeans.modules.php.model.refs.ReferenceResolver;


/**
 * @author ads
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.php.model.refs.ReferenceResolver.class)
public class InterfaceReferenceResolver implements ReferenceResolver {

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceResolver#isApplicable(java.lang.Class)
     */
    public <T extends SourceElement> boolean isApplicable( Class<T> clazz ) {
        return ObjectDefinition.class.isAssignableFrom( clazz );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.php.model.refs.ReferenceResolver#resolve(org.netbeans.modules.php.model.SourceElement, java.lang.String, java.lang.Class, boolean)
     */
    public <T extends SourceElement> List<T> resolve( SourceElement source,
            String identifier, Class<T> clazz, boolean exactComparison )
    {
        if ( !clazz.isAssignableFrom( InterfaceDefinition.class ) ){
            return Collections.emptyList();
        }
        List<PhpModel> models = ModelResolver.ResolverUtility.getIncludedModels(
                source);
        List<T> result = new LinkedList<T>();
        for (PhpModel model : models) {
            List<T> classes;
            model.readLock();
            try {
                classes = getInterfaces(identifier, clazz, model,exactComparison);
            }
            finally {
                model.readUnlock();
            }
            result.addAll(classes);
        }
        return result;
    }
    
    private <T extends SourceElement> List<T> getInterfaces( 
            String identifier ,  Class<T> clazz, PhpModel model , 
            boolean exactComparison)
    {
        List<InterfaceDefinition> ifaceDefs = 
            model.getStatements( InterfaceDefinition.class );
        List<T> result = new LinkedList<T>();
        for (InterfaceDefinition classDef : ifaceDefs) {
            String name = classDef.getName();
            if ( exactComparison && identifier.equals( name )){
                result.add( clazz.cast( classDef ));
            }
            if ( !exactComparison && name.startsWith( identifier )){
                result.add( clazz.cast( classDef ) );
            }
        }
        return result;
    }

}
