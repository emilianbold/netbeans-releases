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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;


/**
 * @author ads
 *
 */
class MemberCheckerFilter extends TypeFilter {
    
    public static MemberCheckerFilter get() {
        // could be changed to ThreadLocal cached access
        return new MemberCheckerFilter();
    }
    
    void init( Map<? extends ExecutableElement, ? extends AnnotationValue> 
        elementValues, Set<ExecutableElement> members, 
        WebBeansModelImplementation impl )
    {
        myImpl = impl;
        myValues = elementValues;
        myMembers = members;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.TypeFilter#filter(java.util.Set)
     */
    @Override
    void filter( Set<TypeElement> set ) {
        super.filter(set);
        for( Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
            getValues().entrySet())
        {
            ExecutableElement execElement = entry.getKey();
            AnnotationValue value = entry.getValue();
            if ( getMemebers().contains( execElement )) {
                checkMember( execElement, value, set );
            }
        }    
    }
    
    private void checkMember( ExecutableElement exec, AnnotationValue value,
                Set<TypeElement> typesWithBindings )
    {
     // annotation member should be checked for presence at Binding type
        for (Iterator<TypeElement> iterator = typesWithBindings.iterator(); 
            iterator.hasNext(); ) 
        {
            // TODO : care about specialize
            TypeElement element = iterator.next();
            List<? extends AnnotationMirror> allAnnotationMirrors = getImplementation()
                    .getHelper().getCompilationController().getElements()
                    .getAllAnnotationMirrors(element);
            for (AnnotationMirror annotationMirror : allAnnotationMirrors) {
                Map<? extends ExecutableElement, ? extends AnnotationValue> 
                    elementValues = annotationMirror.getElementValues();
                AnnotationValue valueForType = elementValues.get( exec );
                if ( !equals( value, valueForType)){
                    iterator.remove( );
                }
            }
        }
    }
    
    private boolean equals( AnnotationValue value1 , AnnotationValue value2 ){
        if ( value1== null ){
            return value2 == null;
        }
        else {
            if ( value1.getValue() == null ){
                return value2!= null && value2.getValue()==null;
            }
            else {
                return value1.getValue().equals( value2 == null ? null : value2.getValue());
            }
        }
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private Map<? extends ExecutableElement, ? extends AnnotationValue>  getValues(){
        return myValues;
    }
    
    private Set<ExecutableElement> getMemebers(){
        return myMembers;
    }
    
    private WebBeansModelImplementation myImpl;
    private Map<? extends ExecutableElement, ? extends AnnotationValue> myValues;
    private Set<ExecutableElement> myMembers;

}
