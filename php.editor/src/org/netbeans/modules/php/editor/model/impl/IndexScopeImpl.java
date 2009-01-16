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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedElement;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.IndexedVariable;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.IndexScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.VariableName;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;

/**
 *
 * @author Radek Matous
 */
class IndexScopeImpl extends ModelScopeImpl implements IndexScope {

    private PHPIndex index;

    IndexScopeImpl(ParserResult info) {
        super(info, "index", PhpKind.INDEX);//NOI18N
        this.index = PHPIndex.get(info);
    }

    IndexScopeImpl(PHPIndex idx) {
        super(null, "index", PhpKind.INDEX);//NOI18N
        this.index = idx;
    }

    public List<? extends TypeScopeImpl> getAllTypes() {
        List<TypeScopeImpl> retval = new ArrayList<TypeScopeImpl>();
        for (IndexedElement element : getIndex().getAllTopLevel(null, "", QuerySupport.Kind.PREFIX)) {
            if (element instanceof IndexedClass) {
                retval.add(new ClassScopeImpl(this, (IndexedClass) element));
            } else if (element instanceof IndexedInterface) {
                retval.add(new InterfaceScopeImpl(this, (IndexedInterface) element));
            }
        }
        return retval;
    }

    public List<? extends TypeScopeImpl> getTypes(String... queryName) {
        return getTypes(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends TypeScopeImpl> getTypes(QuerySupport.Kind nameKind, String... queryName) {
        List<TypeScopeImpl> retval = new ArrayList<TypeScopeImpl>();
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

    public List<? extends ClassScopeImpl> getAllClasses() {
        List<ClassScopeImpl> retval = new ArrayList<ClassScopeImpl>();
        for (IndexedElement element : getIndex().getAllTopLevel(null, "", QuerySupport.Kind.PREFIX)) {
            if (element instanceof IndexedClass) {
                retval.add(new ClassScopeImpl(this, (IndexedClass) element));
            }
        }
        return retval;
    }

    public List<? extends ClassScopeImpl> getClasses(String... queryName) {
        return getClasses(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends ClassScopeImpl> getClasses(QuerySupport.Kind nameKind, String... queryName) {
        List<ClassScopeImpl> retval = new ArrayList<ClassScopeImpl>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedClass> classes = getIndex().getClasses(null, name, nameKind);
            for (IndexedClass indexedClass : classes) {
                retval.add(new ClassScopeImpl(this, indexedClass));
            }
        }
        return retval;
    }

    public List<? extends InterfaceScopeImpl> getAllInterfaces() {
        List<InterfaceScopeImpl> retval = new ArrayList<InterfaceScopeImpl>();
        for (IndexedElement element : getIndex().getAllTopLevel(null, "", QuerySupport.Kind.PREFIX)) {
            if (element instanceof IndexedInterface) {
                retval.add(new InterfaceScopeImpl(this, (IndexedInterface) element));
            }
        }
        return retval;
    }

    public List<? extends InterfaceScopeImpl> getInterfaces(String... queryName) {
        return getInterfaces(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends InterfaceScopeImpl> getInterfaces(QuerySupport.Kind nameKind, String... queryName) {
        List<InterfaceScopeImpl> retval = new ArrayList<InterfaceScopeImpl>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedInterface> interfaces = getIndex().getInterfaces(null, name, nameKind);
            for (IndexedInterface indexedInterface : interfaces) {
                retval.add(new InterfaceScopeImpl(this, indexedInterface));
            }
        }
        return retval;
    }

    public List<? extends ConstantElementImpl> getAllConstants() {
        List<ConstantElementImpl> retval = new ArrayList<ConstantElementImpl>();
        for (IndexedElement element : getIndex().getAllTopLevel(null, "", QuerySupport.Kind.PREFIX)) {
            if (element instanceof IndexedConstant) {
                retval.add(new ConstantElementImpl(this, (IndexedConstant) element));
            }
        }
        return retval;
    }

    public List<? extends ConstantElementImpl> getConstants(String... queryName) {
        return getConstants(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends ConstantElementImpl> getConstants(QuerySupport.Kind nameKind, String... queryName) {
        List<ConstantElementImpl> retval = new ArrayList<ConstantElementImpl>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedConstant> constants = getIndex().getConstants(null, name, nameKind);
            for (IndexedConstant indexedConstant : constants) {
                retval.add(new ConstantElementImpl(this, indexedConstant));
            }
        }
        return retval;
    }

    public List<? extends FunctionScopeImpl> getAllFunctions() {
        List<FunctionScopeImpl> retval = new ArrayList<FunctionScopeImpl>();
        for (IndexedElement element : getIndex().getAllTopLevel(null, "", QuerySupport.Kind.PREFIX)) {
            if (element instanceof IndexedFunction) {
                retval.add(new FunctionScopeImpl(this, (IndexedFunction) element));
            }
        }
        return retval;
    }

    public List<? extends FunctionScopeImpl> getFunctions(String... queryName) {
        return getFunctions(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends FunctionScopeImpl> getFunctions(QuerySupport.Kind nameKind, String... queryName) {
        List<FunctionScopeImpl> retval = new ArrayList<FunctionScopeImpl>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedFunction> functions = getIndex().getFunctions(null, name, nameKind);
            for (IndexedFunction indexedFunction : functions) {
                retval.add(new FunctionScopeImpl(this, indexedFunction));
            }
        }
        return retval;
    }

    public List<? extends VariableName> getAllVariables() {
        List<VariableNameImpl> retval = new ArrayList<VariableNameImpl>();
        for (IndexedElement element : getIndex().getAllTopLevel(null, "", QuerySupport.Kind.PREFIX)) {
            if (element instanceof IndexedVariable) {
                retval.add(new VariableNameImpl(this, (IndexedVariable) element));
            }
        }
        return retval;
    }

    public List<? extends VariableName> getVariables(String... queryName) {
        return getVariables(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends VariableName> getVariables(QuerySupport.Kind nameKind, String... queryName) {
        List<VariableNameImpl> retval = new ArrayList<VariableNameImpl>();
        for (String name : queryName) {
            assert name != null && name.trim().length() > 0;
            Collection<IndexedVariable> vars = getIndex().getTopLevelVariables(null, name, nameKind);
            for (IndexedVariable indexedVariable : vars) {
                retval.add(new VariableNameImpl(this, indexedVariable));
            }
        }
        return retval;
    }

    @Override
    public OffsetRange getBlockRange() {
        return getNameRange();
    }

    public List<? extends ModelElementImpl> getElements() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @CheckForNull
    @Override
    CachedModelSupport getCachedModelSupport() {
        return null;
    }

    /**
     * @return the index
     */
    PHPIndex getIndex() {
        return index;
    }

    public List<? extends ClzConstantElementImpl>getInheritedConstants(ClassScopeImpl cls, String queryName) {
        List<ClzConstantElementImpl> retval = new ArrayList<ClzConstantElementImpl>();
        //ClassScopeImpl cls = ModelUtils.getFirst(getClasses(className));
        //if (cls == null) return Collections.emptyList();
        //assert cls.getName().equals(className);
        Collection<IndexedConstant> flds = getIndex().getClassConstants(null, cls.getName(), queryName, QuerySupport.Kind.EXACT);
        for (IndexedConstant idxConst : flds) {
            //assert cls.getName().equals(idxConst.getIn());
            ClzConstantElementImpl elementImpl = new ClzConstantElementImpl(cls, idxConst);
            retval.add(elementImpl);
        }
        return retval;
    }

    public List<? extends FieldElementImpl>getInheritedFields(ClassScopeImpl cls, String queryName) {
        List<FieldElementImpl> retval = new ArrayList<FieldElementImpl>();
        //ClassScopeImpl cls = ModelUtils.getFirst(getClasses(className));
        //if (cls == null) return Collections.emptyList();
        //assert cls.getName().equals(className);
        Collection<IndexedConstant> flds = getIndex().getFields(null, cls.getName(), queryName, QuerySupport.Kind.EXACT, Modifier.PUBLIC | Modifier.PROTECTED);
        for (IndexedConstant idxConst : flds) {
            FieldElementImpl fei = new FieldElementImpl(cls, idxConst);
            retval.add(fei);
        }
        return retval;
    }

    public List<? extends MethodScopeImpl> getInheritedMethods(final TypeScopeImpl cls, final String queryName) {
        List<MethodScopeImpl> retval = new ArrayList<MethodScopeImpl>();
        //ClassScopeImpl cls = ModelUtils.getFirst(getClasses(className));
        //if (cls == null) return Collections.emptyList();
        //assert cls.getName().equals(className);
        Collection<IndexedFunction> methods = getIndex().getMethods(null, cls.getName(), queryName, QuerySupport.Kind.EXACT, Modifier.PUBLIC | Modifier.PROTECTED);
        for (IndexedFunction idxFunc : methods) {
            MethodScopeImpl msi = new MethodScopeImpl(cls, idxFunc, PhpKind.METHOD);
            retval.add(msi);
        }
        return retval;
    }


    public List<? extends MethodScopeImpl> getMethods(final QuerySupport.Kind nameKind, TypeScopeImpl cls, final String queryName, final int... modifiers) {
        List<MethodScopeImpl> retval = new ArrayList<MethodScopeImpl>();
        PhpModifiers attribs = new PhpModifiers(modifiers);
        //ClassScopeImpl cls = ModelUtils.getFirst(getClasses(className));
        //if (cls == null) return Collections.emptyList();
        //assert cls.getName().equals(className);
        Collection<IndexedFunction> methods = null;
        if (cls instanceof InterfaceScope) {
            methods = getIndex().getMethods(null, cls.getName(), queryName, nameKind, PHPIndex.ANY_ATTR);
        } else {
            methods = getIndex().getMethods(null, cls.getName(), queryName, nameKind, modifiers.length == 0 ? PHPIndex.ANY_ATTR : attribs.toBitmask());
        }
        for (IndexedFunction idxFunc : methods) {
            MethodScopeImpl msi = new MethodScopeImpl(cls, idxFunc, PhpKind.METHOD);
            retval.add(msi);
        }
        return retval;
    }

    public List<? extends MethodScopeImpl> getMethods(TypeScopeImpl cls, final String queryName, final int... modifiers) {
        return getMethods(QuerySupport.Kind.EXACT, cls, queryName, modifiers);
    }

    public List<? extends MethodScopeImpl> getMethods(TypeScopeImpl cls, final int... modifiers) {
        return getMethods(QuerySupport.Kind.PREFIX, cls, "", modifiers);
    }

    public List<? extends ClzConstantElementImpl> getConstants(final QuerySupport.Kind nameKind, TypeScopeImpl cls, final String... queryName) {
        List<ClzConstantElementImpl> retval = new ArrayList<ClzConstantElementImpl>();
        for (String name : queryName) {
            Collection<IndexedConstant> constants = getIndex().getClassConstants(null, cls.getName(), name, nameKind);
            for (IndexedConstant idxConst : constants) {
                ClzConstantElementImpl elementImpl = new ClzConstantElementImpl(cls, idxConst);
                retval.add(elementImpl);
            }

        }
        return retval;
    }

    public List<? extends ClzConstantElementImpl> getConstants(TypeScopeImpl aThis, final String... queryName) {
        return getConstants(QuerySupport.Kind.EXACT, aThis, queryName);
    }

    public List<? extends FieldElementImpl> getFields(final QuerySupport.Kind nameKind, ClassScopeImpl cls, final String queryName, final int... modifiers) {
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

    public List<? extends FieldElementImpl> getFields(ClassScopeImpl aThis, final String queryName, final int... modifiers) {
        return getFields(QuerySupport.Kind.EXACT, aThis, queryName, modifiers);
    }

    @Override
    IndexScopeImpl getIndexScope() {
        return this;
    }



}
