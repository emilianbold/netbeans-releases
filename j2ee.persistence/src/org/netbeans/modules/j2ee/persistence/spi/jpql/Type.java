/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.persistence.jpa.jpql.spi.IConstructor;
import org.eclipse.persistence.jpa.jpql.spi.IManagedTypeProvider;
import org.eclipse.persistence.jpa.jpql.spi.IType;
import org.eclipse.persistence.jpa.jpql.spi.ITypeDeclaration;
import org.eclipse.persistence.jpa.jpql.spi.ITypeRepository;

/**
 *
 * @author sp153251
 */
public class Type implements IType{
    
    private final Element element;
    private final ITypeRepository repository;
    private ITypeDeclaration tDeclaration;

    public Type(TypeRepository typeRepository, Element element){
        this.element = element;
        this.repository = typeRepository;
    }
    
    @Override
    public Iterable<IConstructor> constructors() {
        ArrayList<IConstructor> ret = new ArrayList<IConstructor>();
        collectConstructors(ret, element);
        return ret;
    }

    @Override
    public boolean equals(IType itype) {
        return this==itype || getName().equals(itype.getName());
    }

    @Override
    public String[] getEnumConstants() {
        ArrayList<String> constants = new ArrayList<String>();
        for( Element el:element.getEnclosedElements() ){
            if(el.getKind() == ElementKind.ENUM_CONSTANT){
                constants.add(el.getSimpleName().toString());
            }
        }
        return constants.toArray(new String[]{});
    }

    @Override
    public String getName() {
        if(element instanceof TypeElement) return ((TypeElement) element).getQualifiedName().toString();
        else return element.asType().toString();
    }

    @Override
    public ITypeDeclaration getTypeDeclaration() {
        if(tDeclaration == null){
            tDeclaration = new TypeDeclaration(this, new ITypeDeclaration[0], 0);
        }
        return tDeclaration;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type) {
        return element.getAnnotation(type) != null;
    }

    @Override
    public boolean isAssignableTo(IType itype) {
        if(this == itype) return true;
        String rootName = itype.getName();
        TypeElement tEl = (TypeElement) (element instanceof TypeElement ? element : null);
        return haveInHierarchy(tEl, rootName);
    }

    @Override
    public boolean isEnum() {
        return  (element instanceof TypeElement ? ((TypeElement)element).getKind() == ElementKind.ENUM : false);
    }

    @Override
    public boolean isResolvable() {
        return true;//is it always true?
    }

    @Override
    public String toString() {
        return super.toString() + ", name = " + getName();
    }
    
    private void collectConstructors(ArrayList<IConstructor> constructors, Element element){
        if(element == null || element.getKind()!=ElementKind.CLASS)return;
        TypeElement el = (TypeElement) element;
        for(Element sub: el.getEnclosedElements()){
            if(sub.getKind() == ElementKind.CONSTRUCTOR){
                constructors.add(new Constructor(this, (ExecutableElement)sub));
            } else if ((sub.getKind() == ElementKind.CLASS) && (((TypeElement) sub).getSuperclass() != null)){
                TypeMirror supMirror = ((TypeElement) sub).getSuperclass();
                if (supMirror.getKind() == TypeKind.DECLARED) {
                    DeclaredType superclassDeclaredType = (DeclaredType)supMirror;
                    Element superclassElement = superclassDeclaredType.asElement();  
                    collectConstructors(constructors, superclassElement);
                }
            }
        }
    }
    
    private boolean haveInHierarchy(TypeElement el, String name){
        
        TypeElement tmpEl = el;
        while(tmpEl != null){
            if(tmpEl.getQualifiedName().toString().equals(name)) return true;
            else {
                TypeMirror supMirror = tmpEl.getSuperclass();
                if (supMirror.getKind() == TypeKind.DECLARED) {
                    DeclaredType superclassDeclaredType = (DeclaredType)supMirror;
                    Element superclassElement = superclassDeclaredType.asElement();  
                    if(superclassElement instanceof TypeElement)tmpEl = (TypeElement) superclassElement;
                    else tmpEl = null;
                }
            }
        }
        for(TypeMirror tmpMirr: el.getInterfaces()){
            if(tmpMirr.getKind()== TypeKind.DECLARED) {
                    DeclaredType intDeclType = (DeclaredType)tmpMirr;
                    Element intElement = intDeclType.asElement();  
                    if(intElement instanceof TypeElement){
                        tmpEl = (TypeElement) intElement;
                        if(haveInHierarchy(tmpEl, name))return true;
                    }
            }
        }
        return false;
    }
}
