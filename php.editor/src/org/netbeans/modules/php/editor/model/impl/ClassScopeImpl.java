/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.model.impl;

import org.netbeans.modules.php.editor.api.QualifiedName;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.model.*;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class ClassScopeImpl extends TypeScopeImpl implements ClassScope, VariableNameFactory {

    private Union2<String, List<ClassScopeImpl>> superClass;

    @Override
    void addElement(ModelElementImpl element) {
        assert  element instanceof TypeScope || element instanceof VariableName ||element instanceof MethodScope ||
                element instanceof FieldElement || element instanceof ClassConstantElement : element.getPhpElementKind();
        if (element instanceof TypeScope) {
            Scope inScope = getInScope();
            if (inScope instanceof ScopeImpl) {
                ((ScopeImpl)inScope).addElement(element);
            }
        } else {
            super.addElement(element);
        }
    }

    //new contructors
    ClassScopeImpl(Scope inScope, ClassDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo);
        Expression superId = nodeInfo.getSuperClass();
        String superName = (superId != null) ? CodeUtils.extractUnqualifiedName(superId) : null;
        this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(superName);
    }

    ClassScopeImpl(IndexScope inScope, ClassElement indexedClass) {
        //TODO: in idx is no info about ifaces
        super(inScope, indexedClass);
        final QualifiedName superClassName = indexedClass.getSuperClassName();
        this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(superClassName != null ? superClassName.getName() : null);
    }
    //old contructors

    @NonNull
    public Collection<? extends ClassScope> getSuperClasses() {
        Collection<? extends ClassScope> retval = null;
        retval = superClass.hasSecond() ? superClass.second() : null;
        if (retval == null) {
            assert superClass.hasFirst();
            String superClasName = superClass.first();
            if (superClasName != null) {
                retval = CachingSupport.getClasses(superClasName, this);
                assert retval != null;
            }
        }
        return retval != null ? retval : Collections.<ClassScopeImpl>emptyList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        Collection<? extends ClassScope> extendedClasses = getSuperClasses();
        ClassScope extClass = ModelUtils.getFirst(extendedClasses);
        if (extClass != null) {
            sb.append(" extends ").append(extClass.getName());//NOI18N
        }
        List<? extends InterfaceScope> implementedInterfaces = getSuperInterfaceScopes();
        if (implementedInterfaces.size() > 0) {
            sb.append(" implements ");
            for (InterfaceScope interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }

    @Override
    public Collection<? extends FieldElement> getDeclaredFields() {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl =  ModelUtils.getIndexScope(this);
            return indexScopeImpl.findFields(this);
        }
        return filter(getElements(), new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.FIELD);
            }
        });
    }


    @Override
    public Collection<? extends MethodScope> getInheritedMethods() {
        Set<MethodScope> allMethods = new HashSet<MethodScope>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<ClassScope> superClasses = new HashSet<ClassScope>(getSuperClasses());
        for (ClassScope clz : superClasses) {
            Set<MethodElement> indexedFunctions = 
                    org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAllMethods(clz));
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                allMethods.add(new MethodScopeImpl(clz, indexedFunction));
            }
        }
        Set<InterfaceScope> interfaceScopes = new HashSet<InterfaceScope>(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Set<MethodElement> indexedFunctions =
                    org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAllMethods(iface));
            for (MethodElement classMember : indexedFunctions) {
                MethodElement indexedFunction = classMember;
                allMethods.add(new MethodScopeImpl(iface, indexedFunction));
            }
        }
        return allMethods;
    }

    @Override
    public Collection<? extends FieldElement> getInheritedFields() {
        Set<FieldElement> allFields = new HashSet<FieldElement>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<ClassScope> superClasses = new HashSet<ClassScope>(getSuperClasses());
        for (ClassScope classScope : superClasses) {
            Set<org.netbeans.modules.php.editor.api.elements.FieldElement> indexedFields =
                                        org.netbeans.modules.php.editor.api.elements.ElementFilter.forPrivateModifiers(false).filter(index.getAlllFields(classScope));
            for (org.netbeans.modules.php.editor.api.elements.FieldElement field : indexedFields) {
                allFields.add(new FieldElementImpl(classScope, field));
            }
        }
        return allFields;
    }

    @Override
    public final Collection<? extends ClassConstantElement> getInheritedConstants() {
        Set<ClassConstantElement> allConstants = new HashSet<ClassConstantElement>();
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        ElementQuery.Index index = indexScope.getIndex();
        Set<ClassScope> superClasses = new HashSet<ClassScope>(getSuperClasses());
        for (ClassScope classScope : superClasses) {
            Set<TypeConstantElement> indexedConstants = index.getAllTypeConstants(classScope);
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement constant = classMember;
                allConstants.add(new ClassConstantElementImpl(classScope, constant));
            }
        }
        Set<InterfaceScope> interfaceScopes = new HashSet<InterfaceScope>();
        interfaceScopes.addAll(getSuperInterfaceScopes());
        for (InterfaceScope iface : interfaceScopes) {
            Collection<TypeConstantElement> indexedConstants = index.getInheritedTypeConstants(iface);
            for (TypeConstantElement classMember : indexedConstants) {
                TypeConstantElement constant = classMember;
                allConstants.add(new ClassConstantElementImpl(iface, constant));
            }
        }
        return allConstants;
    }

    @Override
    public Collection<? extends MethodScope> getMethods() {
        Set<MethodScope> allMethods = new HashSet<MethodScope>();
        allMethods.addAll(getDeclaredMethods());
        allMethods.addAll(getInheritedMethods());
        return allMethods;
    }

    @Override
    public String getNormalizedName() {
        return super.getNormalizedName()+(getSuperClassName() != null ? getSuperClassName() : "");//NOI18N
    }



    @NonNull
    public QualifiedName getSuperClassName() {
        List<? extends ClassScope> retval = null;
        if (superClass != null) {
            retval = superClass.hasSecond() ? superClass.second() : null;//this
            if (retval == null) {
                assert superClass.hasFirst();
                String superClasName = superClass.first();
                if (superClasName != null) {
                    return QualifiedName.create(superClasName);

                }
            } else if (retval.size() > 0) {
                ClassScope cls = ModelUtils.getFirst(retval);
                if (cls != null) {
                    return QualifiedName.create(cls.getName());
                }
            }
        }
        return null;//NOI18N
    }

    @Override
    public String getIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        final QualifiedName superClassName = getSuperClassName();
        if (superClassName != null) {
            sb.append(superClassName.toString());
        }
        sb.append(";");//NOI18N
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(";");//NOI18N
        List<? extends String> superInterfaceNames = getSuperInterfaceNames();
        StringBuilder ifaceSb = new StringBuilder();
        for (String iface : superInterfaceNames) {
            if (ifaceSb.length() > 0) {
                ifaceSb.append(",");//NOI18N
            }
            ifaceSb.append(iface);//NOI18N
        }
        sb.append(ifaceSb);
        sb.append(";");//NOI18N
        sb.append(getPhpModifiers().toFlags()).append(";");
        //TODO: add ifaces
        return sb.toString();
    }

    public Collection<? extends MethodScope> getDeclaredConstructors() {
        return ModelUtils.filter(getDeclaredMethods(), new ModelUtils.ElementFilter<MethodScope>() {
            public boolean isAccepted(MethodScope methodScope) {
                return methodScope.isConstructor();
            }
        });
    }

    public String getDefaultConstructorIndexSignature() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().toLowerCase()).append(";");//NOI18N
        sb.append(getName()).append(";");//NOI18N
        sb.append(getOffset()).append(";");//NOI18N
        sb.append(";");//NOI18N
        sb.append(";");//NOI18N
        sb.append(BodyDeclaration.Modifier.PUBLIC).append(";");
        NamespaceScope namespaceScope = ModelUtils.getNamespaceScope(this);
        QualifiedName qualifiedName = namespaceScope.getQualifiedName();
        sb.append(qualifiedName.toString()).append(";");//NOI18N

        return sb.toString();

    }

    @Override
    public QualifiedName getNamespaceName() {
        if (indexedElement instanceof ClassElement) {
            ClassElement indexedClass = (ClassElement)indexedElement;
            return indexedClass.getNamespaceName();
        }
        return super.getNamespaceName();
    }

    public Collection<? extends String> getSuperClassNames() {
        String supeClsName = superClass.hasFirst() ? superClass.first() : null;
        if (supeClsName != null) {
            return Collections.singletonList(supeClsName);
        }
        List<ClassScopeImpl> supeClasses =  Collections.emptyList();
        if (superClass.hasSecond()) {
            supeClasses = superClass.second();
        }
        List<String> retval =  new ArrayList<String>();
        for (ClassScopeImpl cls : supeClasses) {
            retval.add(cls.getName());
        }
        return retval;
    }

    public Collection<? extends VariableName> getDeclaredVariables() {
        return filter(getElements(), new ElementFilter() {
            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.VARIABLE);
            }
        });
    }

    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }

    @Override
    public boolean isFinal() {
        return getPhpModifiers().isFinal();
    }

    @Override
    public boolean isAbstract() {
        return getPhpModifiers().isAbstract();
    }
}
