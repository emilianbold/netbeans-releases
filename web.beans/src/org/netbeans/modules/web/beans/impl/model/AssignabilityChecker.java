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
    
    static AssignabilityChecker get() {
        // could be changed to cached ThreadLocal access
        return new AssignabilityChecker();
    }
    
    void init( DeclaredType varType, ReferenceType checkedType, 
            WebBeansModelImplementation impl)
    {
        myVarType = varType;
        myType = checkedType;
        myImpl = impl;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.Checker#check()
     */
    public boolean check(){
        boolean check = checkAssignability( getVarType(), getType());
        return check;
    }
    
    public boolean checkAssignability( DeclaredType variableType , 
            ReferenceType refType) 
    {
        Element variableElement = variableType.asElement();
        if ( !( variableElement instanceof TypeElement ) ){
            return false;
        }
        if ( ((TypeElement)variableElement).getTypeParameters().size() == 0 ){
            return getImplementation().getHelper().getCompilationController().
                getTypes().isAssignable( refType, variableType);
        }
        
        if ( !( refType instanceof DeclaredType ) ){
            return false;
        }
        DeclaredType type = (DeclaredType)refType;
        List<? extends TypeMirror> typeArguments = type.getTypeArguments();
        
        if (typeArguments.size() == 0) {
            // Type is not parameterized. Check its parents.
            List<? extends TypeMirror> interfaces = ((TypeElement)type.asElement()).
                getInterfaces();
            for (TypeMirror interfaze : interfaces) {
                if (interfaze instanceof DeclaredType
                        && checkAssignability( variableType, (DeclaredType)interfaze))
                {
                    return true;
                }
            }

            DeclaredType superClass = type;
            do {
                TypeMirror superType = ((TypeElement)superClass.asElement()).
                    getSuperclass();
                if (superType instanceof DeclaredType ){
                    superClass = (DeclaredType)superType;
                    if ( checkAssignability( variableType, superClass)){
                        return true;
                    }
                }
                else {
                    superClass = null;
                }
            }
            while( superClass != null);
            // If no parameterized parents found then it is not assignable. 
            return false;
        }

        TypeElement objectElement = getImplementation().getHelper().
            getCompilationController().getElements().getTypeElement(
                    Object.class.getCanonicalName());
        /*
         * Raw types should be identical for parameterized type by the spec.
         * It means that inheritance by parameterized types are not allowed.  
         */
        Types types = getImplementation().getHelper().getCompilationController().getTypes();
        if ( !types.isSameType( types.erasure( variableType ) , types.erasure(type)))
        {
            return false;
        }
        
        List<? extends TypeMirror> varTypeArguments = variableType.getTypeArguments();
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
            // variable type is raw.
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
    
    private boolean checkParameter( TypeMirror argType, TypeMirror varTypeArg )
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
        if ( argType.getKind()!= TypeKind.TYPEVAR && 
                varTypeArg.getKind()!= TypeKind.TYPEVAR &&
                (argType instanceof ReferenceType) && 
                (varTypeArg instanceof ReferenceType) )
                //types.isSameType( types.erasure(argType), types.erasure(varTypeArg)))
        {
            if ( isAssignable(argType, varTypeArg, types)){
                return true;
            }
            else if ( varTypeArg instanceof DeclaredType ){
                return checkAssignability((DeclaredType)varTypeArg, 
                        (ReferenceType)argType);
            }
            else {
                return false;
            }
        }
        
        if ( varTypeArg.getKind() == TypeKind.WILDCARD  )
        {
            return handleWildCard(argType, (WildcardType)varTypeArg, types);
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
            if ( isAssignable(upper, upperVar, getImplementation().getHelper().
                    getCompilationController().getTypes()) )
            {
                return true;
            }
            else if ( upperVar instanceof DeclaredType ){
                return checkAssignability( (DeclaredType)upperVar, 
                        (ReferenceType)upper);
            }
            else {
                return false;
            }
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
            if (  upper == null || upper.getKind()== TypeKind.NULL ||
                    isAssignable(varTypeArg, upper, getImplementation().
                            getHelper().getCompilationController().getTypes()) )
            {
                return true;
            }
            else if ( upper instanceof DeclaredType ){
                checkAssignability( (DeclaredType)upper , (ReferenceType)varTypeArg);
            }
            else {
                return false;
            }
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
                    if ( isAssignable(lowerBound, argType, types)){
                        return true;
                    }
                    else if( argType instanceof DeclaredType ){
                        return checkAssignability( (DeclaredType)argType, 
                                (ReferenceType)lowerBound);
                    }
                    else {
                        return false;
                    }
                }
            }
            else {
                if ( lowerBound == null || lowerBound.getKind() == TypeKind.NULL){
                    if ( isAssignable( argType, upperBound, types)){
                        return true;
                    }
                    else if( upperBound instanceof DeclaredType ){
                        return checkAssignability( (DeclaredType) upperBound, 
                                (ReferenceType)argType);
                    }
                    else {
                        return false;
                    }
                }
                else {
                    if ( isAssignable( argType, upperBound, types ) && 
                            isAssignable(lowerBound, argType, types )  )
                    {
                        return true;
                    }
                    else if ( argType instanceof DeclaredType && 
                            lowerBound instanceof DeclaredType)
                    {
                        return checkAssignability( (DeclaredType) upperBound, 
                                (ReferenceType)argType) && 
                                checkAssignability( (DeclaredType)argType, 
                                        (ReferenceType)lowerBound);
                    }
                    else {
                        return false;
                    }
                }
            }
        }            
        
        /*
         * Implementation of spec item :
         * the required type parameter is a wildcard, the bean type parameter 
         * is a type variable and the upper bound of the type
         * variable is assignable to the upper bound, if any, of 
         * the wildcard and assignable from the lower bound, if any, of the
         * wildcard
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
                    if ( isAssignable(lowerBound, typeUpper, types)){
                        return true;
                    }
                    else if( typeUpper instanceof DeclaredType ){
                        return checkAssignability( (DeclaredType)typeUpper, 
                                (ReferenceType)lowerBound);
                    }
                    else {
                        return false;
                    }
                }
            }
            else {
                if ( lowerBound == null || lowerBound.getKind() == TypeKind.NULL){
                    if ( isAssignable( typeUpper, upperBound, types)){
                        return true;
                    }
                    else if( upperBound instanceof DeclaredType ){
                        return checkAssignability( (DeclaredType) upperBound, 
                                (ReferenceType)typeUpper);
                    }
                    else {
                        return false;
                    }
                }
                else {
                    if ( isAssignable( typeUpper, upperBound, types ) && 
                            isAssignable(lowerBound, typeUpper, types)  )
                    {
                        return true;
                    }
                    else if ( typeUpper instanceof DeclaredType && 
                            lowerBound instanceof DeclaredType)
                    {
                        return checkAssignability( (DeclaredType) upperBound, 
                                (ReferenceType)typeUpper) && 
                                checkAssignability( (DeclaredType)typeUpper, 
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
    
    private boolean isAssignable( TypeMirror from , TypeMirror to , Types types){
        Element element = types.asElement(to );
        boolean isGeneric = ( element instanceof TypeElement ) && 
            ((TypeElement)element).getTypeParameters().size() != 0;
        if ( isGeneric ){
            return false;
        }
        else {
            return types.isAssignable(from , to);
        }
    }
    
    private DeclaredType getVarType(){
        return myVarType;
    }
    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private ReferenceType getType(){
        return myType;
    }
    
    private DeclaredType myVarType;
    private WebBeansModelImplementation myImpl;
    private ReferenceType myType;

}
