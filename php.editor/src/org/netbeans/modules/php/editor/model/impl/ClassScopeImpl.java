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
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
final class ClassScopeImpl extends TypeScopeImpl implements ClassScope {

    private Union2<String, List<ClassScopeImpl>> superClass;

    @Override
    void addElement(ModelElementImpl element) {
        assert element instanceof MethodScope || element instanceof FieldElement || element instanceof ClassConstantElement : element.getPhpKind();
        super.addElement(element);
    }

    //new contructors
    ClassScopeImpl(ScopeImpl inScope, ClassDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo);
        Identifier superId = nodeInfo.getSuperClass();
        String superName = (superId != null) ? superId.getName() : null;
        this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(superName);
    }

    ClassScopeImpl(IndexScopeImpl inScope, IndexedClass indexedClass) {
        //TODO: in idx is no info about ifaces
        super(inScope, indexedClass);
        this.superClass = Union2.<String, List<ClassScopeImpl>>createFirst(indexedClass.getSuperClass());
    }
    //old contructors

    @Override
    void checkModifiersAssert() {
        assert getPhpModifiers() != null;
        assert getPhpModifiers().isPublic();
        assert !getPhpModifiers().isFinal();
    }

    @Override
    void checkScopeAssert() {
        assert getInScope() != null;
        assert getInScope() instanceof FileScope;
    }

    @NonNull
    public List<? extends ClassScope> getSuperClasses() {
        List<? extends ClassScope> retval = null;
        retval = superClass.hasSecond() ? superClass.second() : null;
        if (retval == null) {
            assert superClass.hasFirst();
            String superClasName = superClass.first();
            if (superClasName != null) {
                retval = CachedModelSupport.getClasses(superClasName, this);
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
        List<? extends InterfaceScopeImpl> implementedInterfaces = getInterfaces();
        if (implementedInterfaces.size() > 0) {
            sb.append(" implements ");
            for (InterfaceScopeImpl interfaceScope : implementedInterfaces) {
                sb.append(interfaceScope.getName()).append(" ");
            }
        }
        return sb.toString();
    }

    public List<? extends FieldElementImpl> getAllFields() {
        return getFields();
    }

    public List<? extends FieldElementImpl> getFields(final int... modifiers) {
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public List<? extends FieldElementImpl> getFields(final String queryName, final int... modifiers) {
        IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
        if (indexScopeImpl != null) {
            return indexScopeImpl.getFields(this, queryName, modifiers);
        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), NameKind.EXACT_NAME, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public List<? extends FieldElementImpl> getFields(final NameKind nameKind, final String queryName, final int... modifiers) {
        IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
        if (indexScopeImpl != null) {
            return indexScopeImpl.getFields(nameKind, this, queryName, modifiers);
        }
        return filter(null, new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }


    public List<? extends MethodScope> getAllInheritedMethods() {
        List<MethodScope> allMethods = new ArrayList<MethodScope>();
        allMethods.addAll(getAllMethods());
        IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
        PHPIndex index = indexScopeImpl.getIndex();
        Set<? extends ClassScope> superClasses = new HashSet<ClassScope>(getSuperClasses());
        for (ClassScope clz : superClasses) {
            Collection<IndexedFunction> indexedFunctions = index.getAllMethods(null, clz.getName(), "", NameKind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
            for (IndexedFunction indexedFunction : indexedFunctions) {
                allMethods.add(new MethodScopeImpl((ClassScopeImpl) clz, indexedFunction, PhpKind.METHOD));
            }
        }
        return allMethods;
    }

    public List<? extends FieldElementImpl> getInheritedFields(String queryName) {
        assert queryName.startsWith("$");
        List<FieldElementImpl> allFields = new ArrayList<FieldElementImpl>();
        allFields.addAll(getFields(queryName));
        if (allFields.isEmpty()) {
            IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
            indexScopeImpl = ((indexScopeImpl != null) ? indexScopeImpl : ((FileScope) ModelUtils.getModelScope(this)).getIndexScope());
            PHPIndex index = indexScopeImpl.getIndex();
            ClassScope clz = this;
            while (clz != null && allFields.isEmpty()) {
                clz = ModelUtils.getFirst(clz.getSuperClasses());
                if (clz != null) {
                    String fldName = (queryName.startsWith("$")) //NOI18N
                            ? queryName.substring(1) : queryName;

                    Collection<IndexedConstant> indexedConstants = index.getFields(null, clz.getName(), fldName, NameKind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
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
        List<? extends ClassScope> superClasses = classScope.getSuperClasses();
        for (ClassScope superCls : superClasses) {
            collectSuperClassesChain(result, superCls);
        }
        return result;
    }
}
