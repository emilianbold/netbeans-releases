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

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;


/**
 * @author ads
 *
 */
class AssignabilityChecker  implements Checker {
    
    static AssignabilityChecker get() {
        // could be changed to cached ThreadLocal access
        return new AssignabilityChecker();
    }
    
    void init( Element element,TypeElement type, WebBeansModelImplementation impl)
    {
        myElement = element;
        myType = type;
        myImpl = impl;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.Checker#check()
     */
    public boolean check(){
        boolean check = checkAssignability( getElement(), getType());
        return check;
    }
    
    public boolean checkAssignability( Element element , TypeElement type) {
        DeclaredType variableType = (DeclaredType)element.asType();
        Element variableElement = variableType.asElement();
        if ( !( variableElement instanceof TypeElement ) ){
            return false;
        }
        if ( ((TypeElement)variableElement).getTypeParameters().size() == 0 ){
            /*
             *  Variable type is not parameterized type.
             *  In this case previous check in filterBindingsByType should give 
             *  an answer. 
             */
            return false;
        }
        List<? extends TypeParameterElement> typeParameters = type.getTypeParameters();
        
        if (!getImplementation().getHelper().getCompilationController().
                getTypeUtilities().isCastable( type.asType() , variableType))
        {
            return false;
        }
        
        if (typeParameters.size() == 0) {
            // Type is not parameterized. Check its parents.
            List<? extends TypeMirror> interfaces = type.getInterfaces();
            for (TypeMirror interfaze : interfaces) {
                Element interfaceElement = getImplementation().getHelper()
                        .getCompilationController().getTypes().asElement(
                                interfaze);
                if (interfaceElement instanceof TypeElement
                        && checkAssignability(element,
                                (TypeElement) interfaceElement))
                {
                    return true;
                }
            }

            List<? extends TypeElement> superclasses = getImplementation().getHelper()
                    .getSuperclasses(type);
            for (TypeElement superClass : superclasses) {
                if (checkAssignability(element, superClass)) {
                    return true;
                }
            }
            // If no parameterized parents found then it is not assignable. 
            return false;
        }
        
        /*
         * Raw types should be identical for parameterized type by the spec.
         * It means that inheritance by parameterized types are not allowed.  
         */
        Types types = getImplementation().getHelper().getCompilationController().getTypes();
        if ( !types.isSameType( types.erasure( variableType ) , 
                types.erasure(type.asType())))
        {
            return false;
        }
        
        List<? extends TypeMirror> typeArguments = variableType.getTypeArguments();
        if ( typeArguments.size() == 0 ){
            // variable type is raw.
            for (TypeParameterElement typeParam : typeParameters) {
                List<? extends TypeMirror> bounds = typeParam.getBounds();
                /*
                 * From the spec:
                 * A parameterized bean type is considered assignable 
                 * to a raw required type if the raw types are identical and all type parameters
                 * of the bean type are either unbounded type variables or java.lang.Object.
                 */
                for (TypeMirror mirror : bounds) {
                    if (mirror.getKind() != TypeKind.DECLARED) {
                        return false;
                    }
                    Element boundElement = ((DeclaredType) mirror).asElement();
                    if (!(boundElement instanceof TypeElement)) {
                        return false;
                    }
                    if (((TypeElement) boundElement).getQualifiedName()
                            .contentEquals(Object.class.getCanonicalName()))
                    {
                        return false;
                    }
                }
            }
            return true;
        }
        if ( typeArguments.size() != typeParameters.size() ){
            /*
             *  This should not happen because raw types are checked before.
             *  So generic type is the same. As consequence size of parameters
             *  and arguments should be the same.
             */
            return false;
        }
        for ( int i=0; i< typeArguments.size() ; i++ ){
            TypeMirror argType = typeArguments.get(i);
            TypeParameterElement typeParam = typeParameters.get(i);
            if ( !checkParameter( argType , typeParam  ) ){
                return false;
            }
        }
        return true;
    }
    
    private boolean checkParameter( TypeMirror argType,
            TypeParameterElement typeParam)
    {
        Types types = getImplementation().getHelper().getCompilationController().
            getTypes();
        
        /*
         * Implementation of spec item :
         * the required type parameter and the bean type parameter are actual 
         * types with identical raw type, and, if the type is
         * parameterized, the bean type parameter is assignable to the required 
         * type parameter according to these rules
         */
        if ( types.isSameType( types.erasure(argType), 
                types.erasure(typeParam.asType())))
        {
            Element elementArg = types.asElement(argType);
            TypeMirror paramType = typeParam.asType();
            if ( types.isAssignable(paramType, argType)){
                return true;
            }
            Element paramElement = types.asElement( paramType );
            if ( elementArg instanceof TypeElement && 
                    ((TypeElement) elementArg).getTypeParameters().size()!= 0 
                    && paramElement instanceof TypeElement )
            {
                return checkAssignability(elementArg, (TypeElement)paramElement);
            }
        }
        
        if ( argType instanceof WildcardType ){
            TypeMirror paramMirror = typeParam.asType();
            TypeMirror upperBound = ((WildcardType)argType).getExtendsBound();
            TypeMirror lowerBound = ((WildcardType)argType).getSuperBound();
            
            /*
             * Implementation of spec item :
             * the required type parameter is a wildcard, the bean 
             * type parameter is an actual type and the actual type is assignable to
             * the upper bound, if any, of the wildcard and assignable from the 
             * lower bound, if any, of the wildcard
             */ 
            if ( types.isAssignable(paramMirror, upperBound) && 
                    types.isAssignable(lowerBound, paramMirror))
            {
                return true;
            }
            // probably need recursively call checkcheckAssignability...
            
            /*
             * Implementation of spec item :
             * the required type parameter is a wildcard, the bean type parameter 
             * is a type variable and the upper bound of the type
             * variable is assignable to the upper bound, if any, of 
             * the wildcard and assignable from the lower bound, if any, of the
             * wildcard
             */ 
            if ( paramMirror instanceof TypeVariable ){
                TypeMirror paramUpperBound = 
                    ((TypeVariable)paramMirror).getUpperBound();
                TypeMirror paramLowerBound = 
                    ((TypeVariable)paramMirror).getLowerBound();
                if ( types.isAssignable(paramUpperBound, upperBound) && 
                        types.isAssignable( lowerBound, paramLowerBound))
                {
                    return true;
                }
             // probably need recursively call checkcheckAssignability...
            }
            
            return false;
        }
        
        /*
         * Implementation of spec item :
         * the required type parameter and the bean type parameter are 
         * both type variables and the upper bound of the required
         * type parameter is assignable to the upper bound, if any, 
         * of the bean type parameter
         */
        if ( argType instanceof TypeVariable && 
                typeParam.asType() instanceof TypeVariable )
        {
            TypeMirror upperBoundArg = ((TypeVariable)argType).getUpperBound();
            TypeMirror upperBoundParam = ((TypeVariable)typeParam.asType()).
                getUpperBound();
            
            if (  getImplementation().getHelper().getCompilationController().
                    getTypes().isAssignable(upperBoundArg, upperBoundParam) )
            {
                return true;
            }
            // probably need recursively call checkcheckAssignability...
        }
        
        boolean result = true;
        /*
         * Implementation of spec item :
         * the required type parameter is an actual type, 
         * the bean type parameter is a type variable and the actual type is assignable
         * to the upper bound, if any, of the type variable
         */
        for (TypeMirror mirror : typeParam.getBounds()) {
            if (!getImplementation().getHelper().getCompilationController().getTypes()
                    .isAssignable(argType, mirror))
                    //.isSubtype( argType, mirror))
            {
                result = false;
            }
             //probably need recursively call checkcheckAssignability...
        }
        if ( result ){
            return true;
        }
        
        return false;
    }
    
    private Element getElement(){
        return myElement;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private TypeElement getType(){
        return myType;
    }
    
    private Element myElement;
    private WebBeansModelImplementation myImpl;
    private TypeElement myType;

}
