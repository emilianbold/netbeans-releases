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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.FunctionElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.TypeConstantElement;
import org.netbeans.modules.php.editor.api.elements.VariableElement;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class IndexScopeImpl extends ScopeImpl implements IndexScope {

    private ElementQuery.Index index;

    IndexScopeImpl(ParserResult info) {
        this(info, "index", PhpElementKind.INDEX);//NOI18N
        this.index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(info));
    }

    IndexScopeImpl(ElementQuery.Index idx) {
        this(null, "index", PhpElementKind.INDEX);//NOI18N
        this.index = idx;
    }

    private IndexScopeImpl(ParserResult info, String name, PhpElementKind kind) {
        super(null, name, Union2.<String, FileObject>createSecond(info != null ? info.getSnapshot().getSource().getFileObject() : null), new OffsetRange(0, 0), kind);//NOI18N
    }

    /**
     * @return the index
     */
    @Override
    public ElementQuery.Index getIndex() {
        return index;
    }

    @Override
    public List<? extends InterfaceScope> findInterfaces(String queryName) {
        List<InterfaceScope> retval = new ArrayList<InterfaceScope>();
        Set<InterfaceElement> interfaces = getIndex().getInterfaces(NameKind.exact(queryName));
        for (InterfaceElement indexedInterface : interfaces) {
            retval.add(new InterfaceScopeImpl(this, indexedInterface));
        }
        return retval;
    }

    @Override
    public List<? extends ClassScope> findClasses(String queryName) {
        List<ClassScope> retval = new ArrayList<ClassScope>();
        Set<ClassElement> classes = getIndex().getClasses(NameKind.exact(queryName));
        for (ClassElement indexedClass : classes) {
            retval.add(new ClassScopeImpl(this, indexedClass));
        }
        return retval;
    }

    @Override
    public List<? extends TypeScope> findTypes(String queryName) {
        List<TypeScope> retval = new ArrayList<TypeScope>();
        retval.addAll(findClasses(queryName));
        retval.addAll(findInterfaces(queryName));
        return retval;

    }

    @Override
    public List<? extends FunctionScope> findFunctions(String queryName) {
        List<FunctionScope> retval = new ArrayList<FunctionScope>();
        Set<FunctionElement> functions = getIndex().getFunctions(NameKind.exact(queryName));
        for (FunctionElement indexedFunction : functions) {
            retval.add(new FunctionScopeImpl(this, indexedFunction));
        }
        return retval;
    }

    @Override
    public List<? extends ConstantElement> findConstants(String queryName) {
        List<ConstantElement> retval = new ArrayList<ConstantElement>();
        Set<org.netbeans.modules.php.editor.api.elements.ConstantElement> constants =
                getIndex().getConstants(NameKind.exact(queryName));
        for (org.netbeans.modules.php.editor.api.elements.ConstantElement constant : constants) {
            retval.add(new ConstantElementImpl(this, constant));
        }
        return retval;
    }

    @Override
    public List<? extends VariableName> findVariables(String queryName) {
        List<VariableName> retval = new ArrayList<VariableName>();
        Set<VariableElement> vars = getIndex().getTopLevelVariables(NameKind.exact(queryName));
            for (VariableElement indexedVariable : vars) {
                retval.add(new VariableNameImpl(this, indexedVariable));
            }

        return retval;
    }

    @Override
    public List<? extends MethodScope> findMethods(TypeScope type) {
        List<MethodScope> retval = new ArrayList<MethodScope>();
        //PhpModifiers attribs = new PhpModifiers(modifiers);
        Set<MethodElement> methods = getIndex().getDeclaredMethods(type);
        for (MethodElement idxFunc : methods) {
            retval.add(new MethodScopeImpl(type, idxFunc));
        }
        return retval;
    }

    @Override
    public List<? extends ClassConstantElement> findClassConstants(TypeScope type) {
        List<ClassConstantElement> retval = new ArrayList<ClassConstantElement>();
        Set<TypeConstantElement> constants = getIndex().getDeclaredTypeConstants(type);
        for (TypeConstantElement con : constants) {
            retval.add(new ClassConstantElementImpl(type, con));
        }
        return retval;
    }

    @Override
    public List<? extends MethodScope> findMethods(TypeScope type, String queryName, int... modifiers) {
        List<MethodScope> retval = new ArrayList<MethodScope>();
        //PhpModifiers attribs = new PhpModifiers(modifiers);
        Set<MethodElement> methods = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredMethods(type));
        for (MethodElement idxFunc : methods) {
            retval.add(new MethodScopeImpl(type, idxFunc));
        }
        return retval;
    }

    @Override
    public List<? extends MethodScope> findInheritedMethods(TypeScope typeScope, String queryName) {
        List<MethodScope> retval = new ArrayList<MethodScope>();
        Set<MethodElement> methods = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getInheritedMethods(typeScope));
        for (MethodElement idxFunc : methods) {
            retval.add(new MethodScopeImpl(typeScope, idxFunc));
        }
        return retval;
    }

    @Override
    public List<? extends ClassConstantElement> findClassConstants(TypeScope type, String queryName) {
        List<ClassConstantElement> retval = new ArrayList<ClassConstantElement>();
        Set<TypeConstantElement> constants = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredTypeConstants(type));
        for (TypeConstantElement con : constants) {
            retval.add(new ClassConstantElementImpl(type, con));
        }
        return retval;
    }

    @Override
    public List<? extends ClassConstantElement> findInheritedClassConstants(ClassScope type, String queryName) {
        List<ClassConstantElement> retval = new ArrayList<ClassConstantElement>();
        Set<TypeConstantElement> constants = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getInheritedTypeConstants(type));
        for (TypeConstantElement con : constants) {
            retval.add(new ClassConstantElementImpl(type, con));
        }
        return retval;
    }

    @Override
    public List<? extends FieldElement> findFields(ClassScope clsScope, final int... modifiers) {
        List<FieldElement> retval = new ArrayList<FieldElement>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = getIndex().getDeclaredFields(clsScope);
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : fields) {
            retval.add(new FieldElementImpl(clsScope, fld));
        }
        return retval;
    }

    @Override
    public List<? extends FieldElement> findFields(ClassScope clsScope, final String queryName, final int... modifiers) {
        List<FieldElement> retval = new ArrayList<FieldElement>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getDeclaredFields(clsScope));
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : fields) {
            retval.add(new FieldElementImpl(clsScope, fld));
        }
        return retval;
    }

    public CachingSupport getCachedModelSupport() {
        return null;
    }

    @Override
    public List<? extends FieldElement> findInheritedFields(ClassScope clsScope, String queryName) {
        List<FieldElement> retval = new ArrayList<FieldElement>();
        Set<org.netbeans.modules.php.editor.api.elements.FieldElement> fields = org.netbeans.modules.php.editor.api.elements.ElementFilter.
                    forName(NameKind.exact(queryName)).filter(getIndex().getInheritedFields(clsScope));
        for (org.netbeans.modules.php.editor.api.elements.FieldElement fld : fields) {
            retval.add(new FieldElementImpl(clsScope, fld));
        }
        return retval;
    }

    @Override
    public OffsetRange getBlockRange() {
        return getNameRange();
    }

    @Override
    public List<? extends ModelElementImpl> getElements() {
        throw new IllegalStateException();
    }
}
