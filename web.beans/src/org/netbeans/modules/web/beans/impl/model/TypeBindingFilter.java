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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.web.beans.impl.model;

import java.util.Iterator;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;


/**
 * @author ads
 *
 */
class TypeBindingFilter extends Filter<TypeElement> {
    
    static TypeBindingFilter get() {
        // could be changed to cached ThreadLocal access 
        return new TypeBindingFilter();
    }
    
    void init( VariableElement element,WebBeansModelImplementation modelImpl ){
        myElement = element;
        myImpl = modelImpl;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.TypeFilter#filter(java.util.Set)
     */
    @Override
    void filter( Set<TypeElement> set ) {
        super.filter(set);
        if ( set.size() == 0 ){
            return;
        }
        TypeMirror typeMirror = getElement().asType();
        TypeKind kind = typeMirror.getKind();
        if ( kind == TypeKind.DECLARED ){
            filterDeclaredTypes(set, typeMirror);
        }
        else if ( kind.isPrimitive()  ){
            WebBeansModelProviderImpl.LOGGER.fine("Variable element " +
                    getElement().getSimpleName()+ " " +
                    "couldn't have type as eligible for inection becuase its " +
                    "type is primitive. It is unproxyable bean types"); // NOI18N
            set.clear();
        }
        else if ( kind == TypeKind.ARRAY ){
            WebBeansModelProviderImpl.LOGGER.fine("Variable element " +
                    getElement().getSimpleName()+ " " +
                    "couldn't have type as eligible for inection becuase its " +
                    "type has array type. It is unproxyable bean types");// NOI18N
            set.clear();
        }
    }

    private void filterDeclaredTypes( Set<TypeElement> set,
            TypeMirror typeMirror )
    {
        for ( Iterator<TypeElement> iterator = set.iterator(); 
            iterator.hasNext(); )
        {
            TypeElement type = iterator.next();
            if ( getImplementation().getHelper().getCompilationController().
                    getTypes().isAssignable( type.asType(), typeMirror))
            {
                WebBeansModelProviderImpl.LOGGER.fine("Found type element " +
                        type.getQualifiedName() +
                        " for variable element " +getElement().getSimpleName()+ 
                        " by typesafe resolution");                 // NOI18N
            }
            else if ( checkAssignability(  type )){
                WebBeansModelProviderImpl.LOGGER.fine("Probably found " +
                        "castable parametrizied or raw type element " +
                        type.getQualifiedName() +
                        " for variable element " +getElement().getSimpleName()+ 
                        " by typesafe resolution");                 // NOI18N
            }
            else {
                iterator.remove();
            }
        }
    }
    
    private boolean checkAssignability( TypeElement type )
    {
        AssignabilityChecker checker = AssignabilityChecker.get();
        checker.init(getElement(), type, getImplementation());
        return checker.check();
    }

    private VariableElement getElement(){
        return myElement;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private VariableElement myElement;
    private WebBeansModelImplementation myImpl;

}
