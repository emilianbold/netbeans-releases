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
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.IndexedVariable;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.openide.filesystems.FileObject;
import org.openide.util.Union2;

/**
 *
 * @author Radek Matous
 */
class IndexScopeImpl extends ScopeImpl implements IndexScope {

    private PHPIndex index;

    IndexScopeImpl(ParserResult info) {
        this(info, "index", PhpKind.INDEX);//NOI18N
        this.index = PHPIndex.get(info);
    }

    IndexScopeImpl(PHPIndex idx) {
        this(null, "index", PhpKind.INDEX);//NOI18N
        this.index = idx;
    }

    private IndexScopeImpl(ParserResult info, String name, PhpKind kind) {
        super(null, name, Union2.<String, FileObject>createSecond(info != null ? info.getSnapshot().getSource().getFileObject() : null), new OffsetRange(0, 0), kind);//NOI18N
    }

    /**
     * @return the index
     */
    public PHPIndex getIndex() {
        return index;
    }

    public List<? extends InterfaceScope> findInterfaces(String... queryName) {
        return findInterfaces(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends InterfaceScope> findInterfaces(QuerySupport.Kind nameKind, String... queryName) {
        List<InterfaceScope> retval = new ArrayList<InterfaceScope>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedInterface> interfaces = getIndex().getInterfaces(null, name, nameKind);
            for (IndexedInterface indexedInterface : interfaces) {
                retval.add(new InterfaceScopeImpl(this, indexedInterface));
                //retval.add(new InterfaceScopeIdxImpl(this, indexedInterface));

}
        }
        return retval;
    }

    public List<? extends TypeScope> findTypes(String... queryName) {
        return findTypes(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends TypeScope> findTypes(QuerySupport.Kind nameKind, String... queryName) {
        List<TypeScope> retval = new ArrayList<TypeScope>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedClass> classes = getIndex().getClasses(null, name, nameKind);
            for (IndexedClass indexedClass : classes) {
                retval.add(new ClassScopeImpl(this, indexedClass));
            }
            Collection<IndexedInterface> interfaces = getIndex().getInterfaces(null, name, nameKind);
            for (IndexedInterface indexedInterface : interfaces) {
                retval.add(new InterfaceScopeImpl(this, indexedInterface));
            }
        }
        return retval;
    }

    public List<? extends ClassScope> findClasses(String... queryName) {
        return findClasses(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends ClassScope> findClasses(QuerySupport.Kind nameKind, String... queryName) {
        List<ClassScope> retval = new ArrayList<ClassScope>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedClass> classes = getIndex().getClasses(null, name, nameKind);
            for (IndexedClass indexedClass : classes) {
                retval.add(new ClassScopeImpl(this, indexedClass));
            }
        }
        return retval;
    }

    public List<? extends FunctionScope> findFunctions(String... queryName) {
        return findFunctions(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends FunctionScope> findFunctions(QuerySupport.Kind nameKind, String... queryName) {
        List<FunctionScope> retval = new ArrayList<FunctionScope>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedFunction> functions = getIndex().getFunctions(null, name, nameKind);
            for (IndexedFunction indexedFunction : functions) {
                retval.add(new FunctionScopeImpl(this, indexedFunction));
            }
        }
        return retval;
    }

    public List<? extends ConstantElement> findConstants(String... queryName) {
        return findConstants(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends ConstantElement> findConstants(QuerySupport.Kind nameKind, String... queryName) {
        List<ConstantElement> retval = new ArrayList<ConstantElement>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedConstant> constants = getIndex().getConstants(null, name, nameKind);
            for (IndexedConstant indexedConstant : constants) {
                retval.add(new ConstantElementImpl(this, indexedConstant));
            }
        }
        return retval;
    }

    public List<? extends VariableName> findVariables(String... queryName) {
        return findVariables(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends VariableName> findVariables(QuerySupport.Kind nameKind, String... queryName) {
        List<VariableName> retval = new ArrayList<VariableName>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedVariable> vars = getIndex().getTopLevelVariables(null, name, nameKind);
            for (IndexedVariable indexedVariable : vars) {
                retval.add(new VariableNameImpl(this, indexedVariable));
            }
        }
        return retval;
    }

    public List<? extends MethodScope> findMethods(TypeScope type, String queryName, int... modifiers) {
        return findMethods(type, QuerySupport.Kind.EXACT, queryName, modifiers);
    }

    public List<? extends MethodScope> findMethods(TypeScope type, QuerySupport.Kind nameKind, String... queryName) {
        return findMethods(type, nameKind, queryName);
    }

    public List<? extends MethodScope> findMethods(TypeScope type, QuerySupport.Kind nameKind, String queryName, int... modifiers) {
        List<MethodScope> retval = new ArrayList<MethodScope>();
        PhpModifiers attribs = new PhpModifiers(modifiers);
        //ClassScopeImpl cls = ModelUtils.getFirst(getClasses(className));
        //if (cls == null) return Collections.emptyList();
        //assert cls.getName().equals(className);
        Collection<IndexedFunction> methods = null;
        if (type instanceof InterfaceScope) {
            methods = getIndex().getMethods(null, type.getName(), queryName, nameKind, PHPIndex.ANY_ATTR);
        } else {
            methods = getIndex().getMethods(null, type.getName(), queryName, nameKind, modifiers.length == 0 ? PHPIndex.ANY_ATTR : attribs.toBitmask());
        }
        for (IndexedFunction idxFunc : methods) {
            MethodScopeImpl msi = new MethodScopeImpl((TypeScopeImpl) type, idxFunc);
            retval.add(msi);
        }
        return retval;
    }

    public List<? extends MethodScope> findInheritedMethods(TypeScope typeScope, String methodName) {
        List<MethodScope> retval = new ArrayList<MethodScope>();
        //ClassScopeImpl cls = ModelUtils.getFirst(getClasses(className));
        //if (cls == null) return Collections.emptyList();
        //assert cls.getName().equals(className);
        Collection<IndexedFunction> methods = getIndex().getMethods(null, typeScope.getName(), methodName, QuerySupport.Kind.EXACT, Modifier.PUBLIC | Modifier.PROTECTED);
        for (IndexedFunction idxFunc : methods) {
            MethodScopeImpl msi = new MethodScopeImpl(typeScope, idxFunc);
            retval.add(msi);
        }
        return retval;
    }

    public List<? extends ClassConstantElement> findClassConstants(TypeScope aThis, String... queryName) {
        return findClassConstants(QuerySupport.Kind.EXACT, aThis, queryName);
    }

    public List<? extends ClassConstantElement> findClassConstants(final QuerySupport.Kind nameKind, TypeScope type, final String... queryName) {
        List<ClassConstantElement> retval = new ArrayList<ClassConstantElement>();
        for (String name : queryName) {
            Collection<IndexedConstant> constants = getIndex().getTypeConstants(null, type.getName(), name, nameKind);
            for (IndexedConstant idxConst : constants) {
                ClassConstantElementImpl elementImpl = new ClassConstantElementImpl(type, idxConst);
                retval.add(elementImpl);
            }

        }
        return retval;
    }

    public List<? extends ClassConstantElement> findInheritedClassConstants(ClassScope clsScope, String constName) {
        List<ClassConstantElement> retval = new ArrayList<ClassConstantElement>();
        Collection<IndexedConstant> flds = getIndex().getTypeConstants(null, clsScope.getName(), constName, QuerySupport.Kind.EXACT);
        for (IndexedConstant idxConst : flds) {
            ClassConstantElementImpl elementImpl = new ClassConstantElementImpl(clsScope, idxConst);
            retval.add(elementImpl);
        }
        return retval;
    }

    public List<? extends FieldElement> findFields(final QuerySupport.Kind nameKind, ClassScope cls, final String queryName, final int... modifiers) {
        List<FieldElementImpl> retval = new ArrayList<FieldElementImpl>();
        PhpModifiers attribs = new PhpModifiers(modifiers);
        String name = (queryName.startsWith("$")) //NOI18N
                ? queryName.substring(1) : queryName;

        Collection<IndexedConstant> constants = getIndex().getFields(null, cls.getName(), name, nameKind, modifiers.length == 0 ? PHPIndex.ANY_ATTR : attribs.toBitmask());
        for (IndexedConstant idxConst : constants) {
            FieldElementImpl elementImpl = new FieldElementImpl(cls, idxConst);
            retval.add(elementImpl);
        }
        return retval;
    }

    public List<? extends FieldElement> findFields(ClassScope cls, final int... modifiers) {
        return findFields(QuerySupport.Kind.PREFIX, cls, "", modifiers);
    }

    public List<? extends FieldElement> findFields(ClassScope aThis, final String queryName, final int... modifiers) {
        return findFields(QuerySupport.Kind.EXACT, aThis, queryName, modifiers);
    }

    public CachingSupport getCachedModelSupport() {
        return null;
    }

    public List<? extends FieldElement> findInheritedFields(ClassScope clsScope, String fieldName) {
        List<FieldElement> retval = new ArrayList<FieldElement>();
        //ClassScopeImpl cls = ModelUtils.getFirst(getClasses(className));
        //if (cls == null) return Collections.emptyList();
        //assert cls.getName().equals(className);
        Collection<IndexedConstant> flds = getIndex().getFields(null, clsScope.getName(), fieldName, QuerySupport.Kind.EXACT, Modifier.PUBLIC | Modifier.PROTECTED);
        for (IndexedConstant idxConst : flds) {
            FieldElement fei = new FieldElementImpl(clsScope, idxConst);
            retval.add(fei);
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
