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

import java.util.Collection;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
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
        assert element instanceof VariableName ||element instanceof MethodScope ||
                element instanceof FieldElement || element instanceof ClassConstantElement : element.getPhpKind();
        super.addElement(element);
    }

    //new contructors
    ClassScopeImpl(Scope inScope, ClassDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo);
        Expression superId = nodeInfo.getSuperClass();
        String superName = (superId != null) ? CodeUtils.extractUnqualifiedName(superId) : null;
        this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(superName);
    }

    ClassScopeImpl(IndexScope inScope, IndexedClass indexedClass) {
        //TODO: in idx is no info about ifaces
        super(inScope, indexedClass);
        this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(indexedClass.getSuperClass());
    }
    //old contructors

    @NonNull
    public List<? extends ClassScope> getSuperClasses() {
        List<? extends ClassScope> retval = null;
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
        List<? extends ClassScope> extendedClasses = getSuperClasses();
        ClassScope extClass = ModelUtils.getFirst(extendedClasses);
        if (extClass != null) {
            sb.append(" extends ").append(extClass.getName());//NOI18N
        }
        List<? extends InterfaceScope> implementedInterfaces = getSuperInterfaces();
        if (implementedInterfaces.size() > 0) {
            sb.append(" implements ");
            for (InterfaceScope interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }

    public Collection<? extends FieldElement> getDeclaredFields() {
        return findDeclaredFields();
    }

    public Collection<? extends FieldElement> findDeclaredFields(final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl =  ModelUtils.getIndexScope(this);
            return indexScopeImpl.findFields(this, modifiers);
        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public Collection<? extends FieldElement> findDeclaredFields(final String queryName, final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl = ModelUtils.getIndexScope(this);
            return indexScopeImpl.findFields(this, queryName, modifiers);
        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), QuerySupport.Kind.EXACT, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

   public Collection<? extends FieldElement> findDeclaredFields(final QuerySupport.Kind nameKind, final String queryName, final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            return indexScopeImpl.findFields(nameKind, this, queryName, modifiers);
        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }


    public Collection<? extends MethodScope> getInheritedMethods() {
        return findInheritedMethods("");//NOI18N
    }

    @Override
    public Collection<? extends MethodScope> getMethods() {
        Set<MethodScope> allMethods = new HashSet<MethodScope>();
        allMethods.addAll(getDeclaredMethods());
        allMethods.addAll(getInheritedMethods());
        return allMethods;
    }

    @Override
    public List<? extends FieldElement> getFields() {
        List<FieldElement> allFlds = new ArrayList<FieldElement>();
        allFlds.addAll(getDeclaredFields());
        IndexScope indexScope = ModelUtils.getIndexScope(this);
        PHPIndex index = indexScope.getIndex();
        ClassScope clz = ModelUtils.getFirst(getSuperClasses());
        while(clz != null) {
            Collection<IndexedConstant> indexedConsts = index.getFields(null, clz.getName(), "", QuerySupport.Kind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
            for (IndexedConstant indexedConstant : indexedConsts) {
                allFlds.add(new FieldElementImpl((ClassScopeImpl) clz, indexedConstant));
            }
            clz = ModelUtils.getFirst(clz.getSuperClasses());
        }

        return allFlds;
    }

    public List<? extends FieldElement> findInheritedFields(String queryName) {
        assert queryName.startsWith("$");
        List<FieldElement> allFields = new ArrayList<FieldElement>();
        allFields.addAll(findDeclaredFields(queryName));
        if (allFields.isEmpty()) {
            IndexScope indexScope = ModelUtils.getIndexScope(this);
            PHPIndex index = indexScope.getIndex();
            ClassScope clz = this;
            while (clz != null && allFields.isEmpty()) {
                clz = ModelUtils.getFirst(clz.getSuperClasses());
                if (clz != null) {
                    String fldName = (queryName.startsWith("$")) //NOI18N
                            ? queryName.substring(1) : queryName;

                    Collection<IndexedConstant> indexedConstants = index.getFields(null, clz.getName(), fldName, QuerySupport.Kind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
                    for (IndexedConstant indexedConstant : indexedConstants) {
                        allFields.add(new FieldElementImpl((ClassScopeImpl) clz,indexedConstant));
                    }
                }
            }
        }
        return allFields;
    }


    public List<? extends ClassScope> getSuperClassesChain() {
        Set<ClassScope> set = new HashSet<ClassScope>();
        return new ArrayList<ClassScope>(collectSuperClassesChain(set, this));
    }

    private static Set<ClassScope> collectSuperClassesChain(Set<ClassScope> result, ClassScope classScope) {
        result.add(classScope);
        Collection<? extends ClassScope> superClasses = classScope.getSuperClasses();
        for (ClassScope superCls : superClasses) {
            collectSuperClassesChain(result, superCls);
        }
        return result;
    }

    @Override
    public String getNormalizedName() {
        return super.getNormalizedName()+(getSuperClassName() != null ? getSuperClassName() : "");//NOI18N
    }

    @NonNull
    String getSuperClassName() {
        List<? extends ClassScope> retval = null;
        retval = superClass.hasSecond() ? superClass.second() : null;
        if (retval == null) {
            assert superClass.hasFirst();
            String superClasName = superClass.first();
            if (superClasName != null) {
                return superClasName;

            }
        } else if (retval.size() > 0) {
            ClassScope cls = ModelUtils.getFirst(retval);
            if (cls != null) {
                return cls.getName();
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
        final String superClassName = getSuperClassName();
        if (superClassName != null) {
            sb.append(superClassName);
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
        sb.append(getName()).append(";");//NOI18N
        sb.append(";");//NOI18N
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
        if (indexedElement instanceof IndexedClass) {
            IndexedClass indexedClass = (IndexedClass)indexedElement;
            return QualifiedName.create(indexedClass.getNamespaceName());
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
                return element.getPhpKind().equals(PhpKind.VARIABLE);
            }
        });
    }

    public VariableNameImpl createElement(Variable node) {
        VariableNameImpl retval = new VariableNameImpl(this, node, false);
        addElement(retval);
        return retval;
    }
}
