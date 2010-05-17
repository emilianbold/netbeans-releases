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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;


/**
 * @author ads
 *
 */
class TypeProductionFilter extends Filter<Element> {
 
    private TypeProductionFilter(  ){
    }
    
    static TypeProductionFilter get( ){
        // could be cached via ThreadLocal attribute
        return new TypeProductionFilter();
    }
    
    void init( VariableElement element , TypeMirror elementType ,
            WebBeansModelImplementation model)
    {
        myElement = element;
        myImpl = model;
        myType = elementType;
        myResult = new HashMap<Element, List<DeclaredType>>();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.beans.impl.model.Filter#filterElements(java.util.Set)
     */
    void filter( Set<Element> productionElements ){
        if ( filterPrimitives(productionElements ) ){
            fillSimpleResult(productionElements);
            return;
        }
        
        if ( filterArray(productionElements) ){
            fillSimpleResult(productionElements);
            return ;
        }
        
        TypeBindingFilter filter = TypeBindingFilter.get();
        filter.init(getElement(), getElementType(), getImplementation());
        
        // this cycle care only about declared types.
        for ( Iterator<? extends Element> iterator = productionElements.iterator() ; 
            iterator.hasNext() ; ) 
        {
            Element productionElement = iterator.next();

            TypeElement enclosingElement = getImplementation().getHelper().
                getCompilationController().getElementUtilities().
                enclosingTypeElement( productionElement );
            
            List<DeclaredType> derived = getDerived( enclosingElement);
            
            for (DeclaredType declaredType : derived) {
                TypeMirror mirror = null;
                try {
                    if (productionElement.getKind() == ElementKind.FIELD) {
                        mirror = getImplementation().getHelper()
                                .getCompilationController().getTypes()
                                .asMemberOf(declaredType, productionElement);
                    }
                    else if (productionElement.getKind() == ElementKind.METHOD)
                    {
                        mirror = getImplementation().getHelper()
                                .getCompilationController().getTypes()
                                .asMemberOf(declaredType, productionElement);
                        mirror = ((ExecutableType) mirror).getReturnType();
                    }
                    if ( filter.isAssignable(mirror) ){
                        addResult( productionElement , declaredType);
                    }
                }
                catch (IllegalArgumentException e) {
                    /*
                     * call <code>asMemberOf</code> could be a problem for
                     * productionElment and derived. In this case just skip
                     */
                    continue;
                }
            }
        }
    }
    
    Map<Element, List<DeclaredType>> getResult() {
        return myResult;
    }
    
    private void addResult( Element productionElement, DeclaredType type )
    {
        List<DeclaredType> list = myResult.get( productionElement );
        if ( list == null ){
            list = new ArrayList<DeclaredType>(2);
            myResult.put( productionElement , list );
        }
        list.add( type );
    }
    
    private void fillSimpleResult( Set<Element> productionElements ){
        for (Element element : productionElements) {
            TypeElement enclosingElement = getImplementation().getHelper().
                getCompilationController().getElementUtilities().
                    enclosingTypeElement(element);
            DeclaredType type = (DeclaredType)enclosingElement.asType();
            myResult.put( element , Collections.singletonList(type) );
        }
    }
    
    private List<DeclaredType> getDerived( TypeElement element ) 
    {
        if ( !isGeneric( element ) ){
            return Collections.singletonList( (DeclaredType)element.asType());
        }
        
        Set<TypeElement> implementors = FieldInjectionPointLogic.
            getImplementors(getImplementation(), element);
        List<DeclaredType> result = new ArrayList<DeclaredType>( 
                implementors.size());
        for (TypeElement typeElement : implementors) {
            result.add((DeclaredType)typeElement.asType());
        }
        
        return result;
        
    }
    
    private boolean isGeneric( TypeElement element ) {
        //DeclaredType type = (DeclaredType)element.asType();
        return element.getTypeParameters().size()!=0;
    }

    private boolean filterArray( Set<? extends Element> productionElements)
    {
        if  ( getElementType().getKind() == TypeKind.ARRAY ){
            TypeMirror arrayComponentType = ((ArrayType)getElementType()).getComponentType();
            for (Iterator<? extends Element> iterator = productionElements.iterator() ; 
                    iterator.hasNext() ; ) 
            {
                Element productionElement = iterator.next();
                TypeMirror productionType= null;
                if ( productionElement.getKind() == ElementKind.FIELD){
                    productionType = productionElement.asType();
                }
                else if ( productionElement.getKind() == ElementKind.METHOD){
                    productionType = ((ExecutableElement)productionElement).
                        getReturnType();
                }
                if ( productionType == null ){
                    continue;
                }
                if ( productionType.getKind() != TypeKind.ARRAY ){
                    iterator.remove();
                    continue;
                }
                if ( !getImplementation().getHelper().getCompilationController().
                        getTypes().isSameType( arrayComponentType,
                                ((ArrayType) productionType).getComponentType()))
                {
                      iterator.remove();              
                }
            }
            return true;
        }
        return false;
    }

    private boolean filterPrimitives( Set<? extends Element> productionElements )
    {
        PrimitiveType primitive = null;
        TypeElement boxedType = null;
        if ( getElementType().getKind().isPrimitive() ){
            primitive = getImplementation().getHelper().getCompilationController().
                getTypes().getPrimitiveType( getElementType().getKind());
            boxedType = getImplementation().getHelper().getCompilationController().
                getTypes().boxedClass( primitive);
        }
        else if ( getElementType().getKind() == TypeKind.DECLARED ){
            Element varElement = getImplementation().getHelper().
                getCompilationController().getTypes().asElement( getElementType() );
            if ( varElement instanceof TypeElement ){
                String typeName = ((TypeElement)varElement).getQualifiedName().
                    toString();
                if ( WRAPPERS.contains( typeName )){
                    primitive = getImplementation().getHelper().
                        getCompilationController().getTypes().unboxedType( 
                                varElement.asType());
                    boxedType = (TypeElement)varElement;
                }
                
            }
        }
        
        if ( primitive!= null ){
            for( Iterator<? extends Element> iterator = productionElements.iterator();
                iterator.hasNext(); )
            {
                Element productionElement =iterator.next();
                Types types = getImplementation().getHelper().
                    getCompilationController().getTypes();
                TypeMirror productionType = null;
                if ( productionElement.getKind() == ElementKind.FIELD){
                    productionType = productionElement.asType();
                }
                else if ( productionElement.getKind() == ElementKind.METHOD){
                    productionType = ((ExecutableElement)productionElement).
                        getReturnType();
                }
                if ( productionType!= null && 
                        !types.isSameType( productionType, primitive ) &&
                        !types.isSameType( productionType , boxedType.asType()))
                {
                    iterator.remove();
                }
            }
        }
        
        return primitive!= null;
    }

    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private VariableElement getElement(){
        return myElement;
    }
    
    private TypeMirror getElementType(){
        return myType;
    }

    private WebBeansModelImplementation myImpl;
    private VariableElement myElement;
    private TypeMirror myType;
    private Map<Element, List<DeclaredType>> myResult;
    
    
    private static final Set<String> WRAPPERS = new HashSet<String>();
    
    static {
        WRAPPERS.add(Boolean.class.getCanonicalName());
        WRAPPERS.add(Byte.class.getCanonicalName());
        WRAPPERS.add(Character.class.getCanonicalName());
        WRAPPERS.add(Double.class.getCanonicalName());
        WRAPPERS.add(Float.class.getCanonicalName());
        WRAPPERS.add(Integer.class.getCanonicalName());
        WRAPPERS.add(Long.class.getCanonicalName());
        WRAPPERS.add(Short.class.getCanonicalName());
    }
}
