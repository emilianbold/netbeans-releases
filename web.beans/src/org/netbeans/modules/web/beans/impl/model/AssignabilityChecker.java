/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ReferenceType;
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
    
    AssignabilityChecker(boolean eventCase ){
        isEventAssignability = eventCase;
    }
    
    static AssignabilityChecker get(boolean eventAssignability) {
        // could be changed to cached ThreadLocal access
        return new AssignabilityChecker( eventAssignability );
    }
    
    void init( ReferenceType varType, ReferenceType checkedType, 
            Element originalElement, WebBeansModelImplementation impl)
    {
        myVarType = varType;
        myType = checkedType;
        myImpl = impl;
        myOriginalElement = originalElement;
    }
    
    void init( ReferenceType varType, ReferenceType checkedType, 
            WebBeansModelImplementation impl)
    {
        init( varType , checkedType , null, impl );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.Checker#check()
     */
    public boolean check(){
        boolean check = checkAssignability( getVarType(), getType(), 
                myOriginalElement);
        return check;
    }
    
    public boolean checkAssignability( ReferenceType variableType , 
            ReferenceType refType , Element originalElement ) 
    {
        boolean isDeclaredType = variableType instanceof DeclaredType ;
        if ( !isDeclaredType ){
            return checkParameter(refType, variableType);
        }
        Element variableElement = ((DeclaredType)variableType).asElement();
        if ( !( variableElement instanceof TypeElement ) ){
            return false;
        }
        
        if ( !( refType instanceof DeclaredType ) ){
            return false;
        }
        DeclaredType type = (DeclaredType)refType;

        Element refElement = getImplementation().getHelper().
            getCompilationController().getTypes().asElement( type );
        if ( !( refElement instanceof TypeElement ) ){
            return false;
        }

        Types types = getImplementation().getHelper().getCompilationController().getTypes();
        Collection<TypeMirror> restrictedTypes = RestrictedTypedFilter.
            getRestrictedTypes( originalElement, getImplementation());
        // return false if restricted types don't contain injection point type
        if ( !isEventAssignability && restrictedTypes != null ) {
            boolean hasBeanType = false;
            for( TypeMirror restrictedType : restrictedTypes ){
                if ( types.isSameType( types.erasure( restrictedType ) , 
                        types.erasure(variableType)))
                {
                    hasBeanType = true;
                    break;
                }
            }
            if ( !hasBeanType ){
                return false;
            }
        }
        
        /*
         * Find ancestor of refType with the same raw type.
         * Raw types should be identical for parameterized type by the spec.
         * It means that inheritance by parameterized types are not allowed.  
         */
        if ( !types.isSameType( types.erasure( variableType ) , types.erasure(type)) ){
            TypeMirror ancestor = getAncestor((TypeElement)refElement ,
                types.erasure( variableType ) , types );
            // no appropriate type 
            if ( !(ancestor instanceof DeclaredType)){
                return false;
            }
            type = (DeclaredType) ancestor;
        }

        List<? extends TypeMirror> typeArguments = type.getTypeArguments();
        
        TypeElement objectElement = getImplementation().getHelper().
        getCompilationController().getElements().getTypeElement(
                Object.class.getCanonicalName());
        
        List<? extends TypeMirror> varTypeArguments = ((DeclaredType)variableType).
            getTypeArguments();
        if ( varTypeArguments.size() == 0 || types.isSameType( variableType,
                types.erasure( variableType )  ))
        /*
         *  I'm not sure how to detect variable declaration with generic type:
         *  - it is unclear how many arguments has such type as result
         *  - probably such type could have typevar argument ( the same as generic
         *  declaration ). In the letter case this type mirror should be the same
         *  as generic declaration type mirror. So I put here comparison with 
         *  type after erasure.             
         */
        {
            // variable type is a raw.
            if ( isEventAssignability ){
                /* from the spec for event type : A parameterized event type 
                 * is considered assignable to a raw observed event type 
                 * if the raw types are identical. 
                 */
                return true;
            }
            for (TypeMirror typeParam : typeArguments) {
                /*
                 * From the spec:
                 * A parameterized bean type is considered assignable 
                 * to a raw required type if the raw types are identical and all type parameters
                 * of the bean type are either unbounded type variables or java.lang.Object.
                 */
                if (typeParam.getKind() == TypeKind.DECLARED) {
                    if (!((TypeElement)((DeclaredType) typeParam).asElement()).
                        getQualifiedName().contentEquals(Object.class.getCanonicalName()))
                    {
                        return false;
                    }
                }
                else if ( typeParam.getKind() == TypeKind.TYPEVAR){
                    TypeMirror lowerBound = ((TypeVariable)typeParam).getLowerBound();
                    if ( lowerBound != null && lowerBound.getKind() != TypeKind.NULL ){
                        return false;
                    }
                    TypeMirror upperBound = ((TypeVariable)typeParam).getUpperBound();
                    if ( upperBound != null && upperBound.getKind() != TypeKind.NULL ){
                        return types.isSameType(upperBound, objectElement.asType());
                    }
                }
                /*else if ( typeParam.getKind() == TypeKind.WILDCARD){
                    continue;
                }*/
                else {
                    return false;
                }
            }
            return true;
        }
        if ( varTypeArguments.size() != typeArguments.size() ){
            /*
             *  This should not happen because raw types are checked before.
             *  So generic type is the same. As consequence size of parameters
             *  and arguments should be the same.
             */
            return false;
        }
        for ( int i=0; i< varTypeArguments.size() ; i++ ){
            TypeMirror argType = typeArguments.get(i);
            if ( !checkParameter( argType , varTypeArguments.get(i)  ) ){
                return false;
            }
        }
        return true;
    }
    
    public boolean checkAssignability( ReferenceType variableType , 
            ReferenceType refType ) 
    {
        if ( !isEventAssignability && ( refType instanceof DeclaredType )){
            return checkAssignability(variableType, refType, 
                    ((DeclaredType)refType).asElement());
        }
        else {
            return checkAssignability(variableType, refType, null);
        }
    }
    
    private boolean checkParameter( TypeMirror argType, TypeMirror varTypeArg )
    {
        Types types = getImplementation().getHelper().getCompilationController().
            getTypes();

        if ( isEventAssignability ){
            if ( varTypeArg.getKind()== TypeKind.TYPEVAR ){
                TypeMirror upperBound = ((TypeVariable)varTypeArg).getUpperBound();
                if ( upperBound == null || upperBound.getKind() == TypeKind.NULL ){
                    return true;
                }
                else {
                    return checkIsAssignable(types, argType, upperBound);
                }
            }
        }
        
        /*
         * Implementation of spec item :
         * the required type parameter and the bean type parameter are actual 
         * types with identical raw type, and, if the type is
         * parameterized, the bean type parameter is assignable to the required 
         * type parameter according to these rules
         */
        if ( argType.getKind()!= TypeKind.TYPEVAR && 
                varTypeArg.getKind()!= TypeKind.TYPEVAR &&
                (argType instanceof ReferenceType) && 
                (varTypeArg instanceof ReferenceType) )
                //types.isSameType( types.erasure(argType), types.erasure(varTypeArg)))
        {
            return checkIsAssignable(getImplementation().getHelper().
                    getCompilationController().getTypes(), argType, varTypeArg);
        }
        
        if ( varTypeArg.getKind() == TypeKind.WILDCARD  )
        {
            return handleWildCard(argType, (WildcardType)varTypeArg, types);
        }
        
        if ( isEventAssignability ){
            return false;
        }
        
        /*
         * Implementation of spec item :
         * the required type parameter and the bean type parameter are 
         * both type variables and the upper bound of the required
         * type parameter is assignable to the upper bound, if any, 
         * of the bean type parameter
         */
        if ( argType.getKind() == TypeKind.TYPEVAR &&
                varTypeArg.getKind() == TypeKind.TYPEVAR)
        {
            TypeMirror upper = ((TypeVariable)argType).getUpperBound();
            TypeMirror upperVar = ((TypeVariable)varTypeArg).getUpperBound();
            
            if ( upper == null || upper.getKind() == TypeKind.NULL ){
                return true;
            }
            if ( upperVar == null || upperVar.getKind() == TypeKind.NULL ){
                return false;
            }
            return checkIsAssignable(types, upperVar, upper);
        }
        
        if (varTypeArg.getKind() != TypeKind.TYPEVAR
                && argType.getKind() == TypeKind.TYPEVAR)
        {
            /*
             * Implementation of spec item : the required type parameter is an
             * actual type, the bean type parameter is a type variable and the
             * actual type is assignable to the upper bound, if any, of the type
             * variable
             */

            TypeMirror upper = ((TypeVariable)argType).getUpperBound();
            if (  upper == null || upper.getKind()== TypeKind.NULL ){
                return true;
            }
            return checkIsAssignable(types, varTypeArg, upper);
        }
        
        return false;
    }

    private boolean handleWildCard( TypeMirror argType, WildcardType varTypeArg,
            Types types )
    {
        TypeMirror upperBound = varTypeArg.getExtendsBound();
        TypeMirror lowerBound = varTypeArg.getSuperBound();

        if ( argType instanceof ReferenceType && 
                argType.getKind()!=TypeKind.TYPEVAR)
        {
            /*
             * Implementation of spec item : the required type parameter is
             * a wildcard, the bean type parameter is an actual type and the
             * actual type is assignable to the upper bound, if any, of the
             * wildcard and assignable from the lower bound, if any, of the
             * wildcard
             */
            if ( upperBound == null || upperBound.getKind() == TypeKind.NULL){
                if ( lowerBound == null || lowerBound.getKind() == TypeKind.NULL){
                    return true;
                }
                else {
                    return checkIsAssignable(types, lowerBound, argType);
                }
            }
            else {
                if ( lowerBound == null || lowerBound.getKind() == TypeKind.NULL){
                    return checkIsAssignable(types, argType, upperBound);
                }
                else {
                    return checkIsAssignable(types, argType, upperBound) &&
                        checkIsAssignable(types, lowerBound, argType);
                }
            }
        }            
        
        if ( isEventAssignability ){
            return false;
        }
        /*
         * Implementation of spec item :
         * the required type parameter is a wildcard, 
         * the bean type parameter is a type variable and the upper bound of the type
         * variable is assignable to or assignable from the upper bound, 
         * if any, of the wildcard and assignable from the lower
          * bound, if any, of the wildcard
         */ 
        if ( argType.getKind() == TypeKind.TYPEVAR ){
            TypeMirror typeUpper = ((TypeVariable)argType).getUpperBound();
            
            if ( typeUpper == null || typeUpper.getKind() == TypeKind.NULL){
                return upperBound == null || upperBound.getKind() == TypeKind.NULL;
            }
            
            if ( upperBound == null || upperBound.getKind() == TypeKind.NULL){
                if ( lowerBound == null || lowerBound.getKind() == TypeKind.NULL){
                    return true;
                }
                else {
                    return checkIsAssignable(types, lowerBound, typeUpper);
                }
            }
            else {
                if ( lowerBound == null || lowerBound.getKind() == TypeKind.NULL){
                    return checkIsAssignable(types, typeUpper, upperBound) ||
                        checkIsAssignable(types, upperBound, typeUpper );
                }
                else {
                    if ( (isAssignable( typeUpper, upperBound, types ) || 
                            isAssignable( upperBound, typeUpper, types ) )&& 
                            isAssignable(lowerBound, typeUpper, types)  )
                    {
                        return true;
                    }
                    else if ( typeUpper instanceof ReferenceType && 
                            lowerBound instanceof ReferenceType)
                    {
                        return (checkAssignability( (ReferenceType) upperBound, 
                                (ReferenceType)typeUpper) || 
                                checkAssignability( (ReferenceType) typeUpper, 
                                        (ReferenceType)upperBound))&& 
                                checkAssignability( (ReferenceType)typeUpper, 
                                        (ReferenceType)lowerBound);
                    }
                    else {
                        return false;
                    }
                }
            }
        }
        
        return false;
    }

    private boolean checkIsAssignable( Types types, TypeMirror from,
            TypeMirror to )
    {
        if ( isAssignable(from, to, types)){
            return true;
        }
        else if( to instanceof ReferenceType  && from instanceof ReferenceType )
        {
            return checkAssignability( (ReferenceType)to, 
                    (ReferenceType)from);
        }
        else {
            return false;
        }
    }
    
    private boolean isAssignable( TypeMirror from, TypeMirror to, Types types )
    {
        Element element = types.asElement(to);
        boolean isGeneric = (element instanceof TypeElement)
                && ((TypeElement) element).getTypeParameters().size() != 0;
        if (isGeneric || !( to instanceof DeclaredType )) {
            return false;
        }
        else {
            Element fromElement = types.asElement(from);
            Collection<TypeMirror> restrictedTypes = RestrictedTypedFilter
                    .getRestrictedTypes(fromElement,
                            getImplementation());
            if (isEventAssignability || restrictedTypes == null) {
                return getImplementation().getHelper()
                        .getCompilationController().getTypes()
                            .isAssignable(from, to);
            }
            for ( TypeMirror restrictedType : restrictedTypes ){
                if ( types.isSameType( types.erasure(restrictedType), 
                        types.erasure( to )))
                {
                    return true;
                }
            }
            return false;
        }
    }
    
    private TypeMirror getAncestor( TypeElement element , TypeMirror rawType ,
            Types types)
    {
        Collection<TypeMirror> classes = new LinkedList<TypeMirror>();
        
        TypeMirror found = findInterface(element, rawType , types );
        if ( found != null ){
            return found;
        }
        
        collecSuperClasses( element , classes );
        for( TypeMirror clazz : classes ){
            if ( types.isSameType ( types.erasure( clazz), rawType )){
                return clazz;
            }
            Element classElement = getImplementation().getHelper().
                getCompilationController().getTypes().asElement( clazz);
            if ( classElement instanceof TypeElement ){
                found = findInterface((TypeElement)classElement, 
                        rawType , types);
                if ( found != null ){
                    return found;
                }
                
            }
        }
        return null;
    }
    
    private void  collecSuperClasses(TypeElement element, 
            Collection<TypeMirror> collected )
    {
        TypeMirror superClass = element.getSuperclass();
        if ( superClass != null ){
            collected.add( superClass );
            Element superElement = getImplementation().getHelper().
                getCompilationController().getTypes().asElement( superClass);
            if ( superElement instanceof TypeElement ){
                TypeElement clazz = (TypeElement)superElement;
                collecSuperClasses(clazz, collected);
            }
        }
    }
    
    private TypeMirror findInterface( TypeElement element , 
            TypeMirror rawType , Types types)
    {
        List<? extends TypeMirror> interfaces = element.getInterfaces();
        for (TypeMirror typeMirror : interfaces) {
            if ( types.isSameType ( types.erasure( typeMirror), rawType )){
                return typeMirror;
            }
            Element interfaceElement = getImplementation().getHelper().
                getCompilationController().getTypes().asElement( typeMirror);
            if ( interfaceElement instanceof TypeElement ){
                TypeElement interfaze = (TypeElement)interfaceElement;
                TypeMirror found = findInterface(interfaze , rawType, types );
                if ( found != null ){
                    return found;
                }
            }
        }
        return null;
    }
    
    private ReferenceType getVarType(){
        return myVarType;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private ReferenceType getType(){
        return myType;
    }
    
    private Element myOriginalElement;
    private ReferenceType myVarType;
    private WebBeansModelImplementation myImpl;
    private ReferenceType myType;
    private boolean isEventAssignability;
}
