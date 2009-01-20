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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.PhpKind;
import org.netbeans.modules.php.editor.model.PhpModifiers;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TypeScope;

/**
 *
 * @author Radek Matous
 */
class CachedModelSupport {

    private FileScope fileScope;
    private List<ClassScopeImpl> classScopes = new ArrayList<ClassScopeImpl>();
    private List<InterfaceScopeImpl> ifaceScopes = new ArrayList<InterfaceScopeImpl>();
    private List<ConstantElementImpl> constantScopes = new ArrayList<ConstantElementImpl>();
    private List<FunctionScopeImpl> fncScopes = new ArrayList<FunctionScopeImpl>();
    private Map<TypeScopeImpl, List<MethodScopeImpl>> methodScopes =
            new LinkedHashMap <TypeScopeImpl,List<MethodScopeImpl>>();
    private Map<TypeScopeImpl, List<FieldElementImpl>> fldElems =
            new LinkedHashMap <TypeScopeImpl,List<FieldElementImpl>>();
    private Map<TypeScopeImpl, List<ClzConstantElementImpl>> clzConstantElems =
            new LinkedHashMap <TypeScopeImpl,List<ClzConstantElementImpl>>();


    void clearCaches() {
        /*classScopes.clear();
        fncScopes.clear();
        typeScopes.clear();
        methodScopes.clear();
         */
    }
    CachedModelSupport(FileScope fileScope) {
        this.fileScope = fileScope;
    }

    static List<? extends ClassConstantElement> getInheritedConstants(ClassScope clsScope, String constName, ModelElement elem) {
        List<? extends ClassConstantElement> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getInheritedMergedConstants((ClassScopeImpl)clsScope, constName);
        } else {
            retval = clsScope.getInheritedConstants(constName);
        }
        return retval;
    }

    //TODO: modifiers not taken into account
    static List<? extends FieldElement> getInheritedFields(ClassScope clsScope, String fieldName, ModelElement elem, final int... modifiers) {
        assert fieldName.startsWith("$");
        List<? extends FieldElement> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getInheritedMergedFields((ClassScopeImpl)clsScope, fieldName);
        } else {
            retval = clsScope.getInheritedFields(fieldName);
        }
        return retval;
    }

    static List<? extends MethodScope> getMethods(ClassScope clsScope, String methodName, ModelElement elem, final int... modifiers) {
        List<? extends MethodScope> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getMergedMethods((ClassScopeImpl)clsScope, methodName);
        } else {
            retval = clsScope.getMethods(methodName, modifiers);
        }
        return retval;
    }

    //TODO: modifiers not taken into account
    static List<? extends MethodScope> getInheritedMethods(TypeScope typeScope, String methodName, ModelElement elem, final int... modifiers) {
        List<? extends MethodScope> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getInheritedMergedMethods((TypeScopeImpl)typeScope, methodName);
        } else {
            retval = typeScope.getInheritedMethods(methodName);
        }
        return retval;
    }

    static List<? extends TypeScope> getTypes(String typeName, ModelElement elem) {
        List<? extends TypeScope> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getMergedTypes(typeName);
        } else {
            retval = top.getTypes(typeName);
        }
        return retval;
    }

    static List<? extends ClassScope> getClasses(String className, ModelElement elem) {
        List<? extends ClassScope> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getMergedClasses(className);
        } else {
            retval = top.getClasses(className);
        }
        return retval;
    }

    static List<? extends InterfaceScope> getInterfaces(String ifaceName, ModelElement elem) {
        List<? extends InterfaceScope> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getMergedIfaces(ifaceName);
        } else {
            retval = top.getInterfaces(ifaceName);
        }
        return retval;
    }

    static List<? extends ConstantElement> getConstants(String constantName, Scope scope) {
        List<? extends ConstantElement> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(scope);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getMergedConstants(constantName);
        } else {
            retval = top.getConstants(constantName);
        }
        return retval;
    }

    static List<? extends FunctionScope> getFunctions(String fncName, ModelElement elem) {
        List<? extends FunctionScope> retval;
        ModelScopeImpl top = (ModelScopeImpl) ModelUtils.getModelScope(elem);
        CachedModelSupport modelSupport = top.getCachedModelSupport();
        if (modelSupport != null) {
            retval = modelSupport.getMergedFunctions(fncName);
        } else {
            retval = top.getFunctions(fncName);
        }
        return retval;
    }


    private List<? extends ClassConstantElement> getInheritedMergedConstants(ClassScopeImpl clsScope, String constName) {
        List<? extends ClzConstantElementImpl> fields = getCachedClassConstants(clsScope, constName);
        if (fields.isEmpty()) {
            clsScope.getInheritedConstants(constName);
            fields = (clsScope != null ? clsScope.getInheritedConstants(constName) : Collections.<ClzConstantElementImpl>emptyList());
            if (fields.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                indexScope.getInheritedConstants(clsScope,constName);
                fields = (clsScope != null ? indexScope.getInheritedConstants(clsScope,constName) : Collections.<ClzConstantElementImpl>emptyList());
            }
            if (!fields.isEmpty()) {
                List<ClzConstantElementImpl> methList = clzConstantElems.get(clsScope);
                if (methList != null) {
                    methList.add(ModelUtils.getFirst(fields));
                }
            }
        }
        return fields;
    }



    private List<? extends FieldElement>  getInheritedMergedFields(ClassScopeImpl clsScope, String fieldName) {
        List<? extends FieldElementImpl> fields = getCachedFields(clsScope, fieldName);
        if (fields.isEmpty()) {
            fields = (clsScope != null ? clsScope.getInheritedFields(fieldName) : Collections.<FieldElementImpl>emptyList());
            if (fields.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                fields = (clsScope != null ? indexScope.getInheritedFields(clsScope,fieldName) : Collections.<FieldElementImpl>emptyList());
            }
            if (!fields.isEmpty()) {
                List<FieldElementImpl> methList = fldElems.get(clsScope);
                if (methList != null) {
                    methList.add(ModelUtils.getFirst(fields));
                }
            }
        }
        return fields;
    }


    private List<? extends MethodScope> getInheritedMergedMethods(TypeScopeImpl typeScope, String methodName) {
        List<? extends MethodScopeImpl> methods = getCachedMethods(typeScope, methodName);
        if (methods.isEmpty()) {
            methods = (typeScope != null ? typeScope.getInheritedMethods(methodName) : Collections.<MethodScopeImpl>emptyList());
            if (methods.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                methods = (typeScope != null ? indexScope.getInheritedMethods(typeScope,methodName) : Collections.<MethodScopeImpl>emptyList());
            }
            if (!methods.isEmpty()) {
                List<MethodScopeImpl> methList = methodScopes.get(typeScope);
                if (methList == null) {
                    methodScopes.put(typeScope, methList = new ArrayList<MethodScopeImpl>());
                }
                methList.add(ModelUtils.getFirst(methods));
            }
        }
        return methods;

    }

    private List<? extends MethodScope> getMergedMethods(ClassScopeImpl clsScope, String methodName, final int... modifiers) {
        List<? extends MethodScopeImpl> methods = getCachedMethods(clsScope, methodName);
        if (methods.isEmpty()) {
            methods = (clsScope != null ? clsScope.getMethods(methodName, modifiers) : Collections.<MethodScopeImpl>emptyList());
            if (methods.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                methods = (clsScope != null ? indexScope.getMethods(clsScope,methodName, modifiers) : Collections.<MethodScopeImpl>emptyList());
            }
            if (!methods.isEmpty()) {
                List<MethodScopeImpl> methList = methodScopes.get(clsScope);
                if (methList != null) {
                    methList.add(ModelUtils.getFirst(methods));
                }
            }
        }
        return methods;   
    }

    private List<? extends InterfaceScope> getMergedIfaces(String ifaceName) {
        List<? extends InterfaceScopeImpl> ifaces = getCachedInterfaces(ifaceName);
        if (ifaces.isEmpty()) {
            ifaces = (ifaceName != null ? fileScope.getInterfaces(ifaceName) : Collections.<InterfaceScopeImpl>emptyList());
            if (ifaces.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                ifaces = (ifaceName != null ? indexScope.getInterfaces(ifaceName) : Collections.<InterfaceScopeImpl>emptyList());
                //TODO: ModelUtils.getFirst
                InterfaceScopeImpl ifce = ModelUtils.getFirst(ifaces);
                if (ifce != null) {
                    ifaceScopes.add(ifce);
                    methodScopes.put(ifce, new ArrayList<MethodScopeImpl>());
                    fldElems.put(ifce, new ArrayList<FieldElementImpl>());
                    clzConstantElems.put(ifce, new ArrayList<ClzConstantElementImpl>());
                }
            }
        }
        return ifaces;
    }

    private List<? extends ClassScope> getMergedClasses(String clzName) {
        List<? extends ClassScopeImpl> classes = getCachedClasses(clzName);
        if (classes.isEmpty()) {            
            classes = (clzName != null ? fileScope.getClasses(clzName) : Collections.<ClassScopeImpl>emptyList());
            if (classes.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                classes = (clzName != null ? indexScope.getClasses(clzName) : Collections.<ClassScopeImpl>emptyList());
                //TODO: ModelUtils.getFirst
                ClassScopeImpl clz = ModelUtils.getFirst(classes);
                if (clz != null) {
                    classScopes.add(clz);
                    methodScopes.put(clz, new ArrayList<MethodScopeImpl>());
                    fldElems.put(clz, new ArrayList<FieldElementImpl>());
                    clzConstantElems.put(clz, new ArrayList<ClzConstantElementImpl>());
                }
            }
        }
        return classes;
    }

    private List<? extends TypeScope> getMergedTypes(String typeName) {
        List<? extends TypeScopeImpl> types = getCachedTypes(typeName);
        if (types.isEmpty()) {
            types = (typeName != null ? fileScope.getTypes(typeName) : Collections.<TypeScopeImpl>emptyList());
            if (types.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                types = (typeName != null ? indexScope.getTypes(typeName) : Collections.<TypeScopeImpl>emptyList());
                for (TypeScopeImpl tp : types) {
                    if (tp instanceof ClassScopeImpl) {
                        classScopes.add((ClassScopeImpl)tp);
                    } else if (tp instanceof InterfaceScopeImpl) {
                        ifaceScopes.add((InterfaceScopeImpl)tp);
                    }
                }
            }
        }
        return types;
    }

    private List<? extends ConstantElementImpl> getMergedConstants(String constantName) {
        List<? extends ConstantElementImpl> constants = getCachedConstants(constantName);
        if (constants.isEmpty()) {
            constants = (constantName != null ? fileScope.getConstants(constantName) : Collections.<ConstantElementImpl>emptyList());
            if (constants.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                constants = (constantName != null ? indexScope.getConstants(constantName) : Collections.<ConstantElementImpl>emptyList());
                constantScopes.addAll(constants);
            }
        }
        return constants;
    }

    private List<? extends FunctionScope> getMergedFunctions(String fncName) {
        List<? extends FunctionScopeImpl> functions = getCachedFunctions(fncName);
        if (functions.isEmpty()) {
            functions = (fncName != null ? fileScope.getFunctions(fncName) : Collections.<FunctionScopeImpl>emptyList());
            if (functions.isEmpty()) {
                IndexScopeImpl indexScope = fileScope.getIndexScope();
                functions = (fncName != null ? indexScope.getFunctions(fncName) : Collections.<FunctionScopeImpl>emptyList());
                fncScopes.addAll(functions);
            }
        }
        return functions;
    }


    private List<? extends InterfaceScopeImpl> getCachedInterfaces(final String... queryName) {
        return getCachedInterfaces(NameKind.EXACT_NAME, queryName);
    }

    private List<? extends InterfaceScopeImpl> getCachedInterfaces(final NameKind nameKind, final String... queryName) {
        return ScopeImpl.filter(ifaceScopes, new ScopeImpl.ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.IFACE) &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    private List<? extends ClassScopeImpl> getCachedClasses(final String... queryName) {
        return getCachedClasses(NameKind.EXACT_NAME, queryName);
    }

    private List<? extends ClassScopeImpl> getCachedClasses(final NameKind nameKind, final String... queryName) {
        return ScopeImpl.filter(classScopes, new ScopeImpl.ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CLASS) &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

       private List<? extends FieldElementImpl> getCachedFields(ClassScopeImpl clsScope, String queryName,final int... modifiers) {
        return getCachedFields(NameKind.EXACT_NAME, clsScope, queryName, modifiers);
    }

    private List<? extends FieldElementImpl> getCachedFields(final NameKind nameKind, final ClassScopeImpl clsScope, final String queryName, final int... modifiers) {
        List<FieldElementImpl> toFilter = fldElems.get(clsScope);
        if (toFilter == null) return Collections.emptyList();
        return ScopeImpl.filter(toFilter, new ScopeImpl.ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }
       private List<? extends ClzConstantElementImpl> getCachedClassConstants(ClassScopeImpl clsScope, String queryName) {
        return getCachedClassConstants(NameKind.EXACT_NAME, clsScope, queryName);
    }

    private List<? extends ClzConstantElementImpl> getCachedClassConstants(final NameKind nameKind, final ClassScopeImpl clsScope, final String queryName) {
        List<ClzConstantElementImpl> toFilter = clzConstantElems.get(clsScope);
        if (toFilter == null) return Collections.emptyList();
        return ScopeImpl.filter(toFilter, new ScopeImpl.ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CLASS_CONSTANT) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName);
            }
        });
    }

    private List<? extends MethodScopeImpl> getCachedMethods(TypeScopeImpl typeScope, final String queryName, final int... modifiers) {
        return getCachedMethods(NameKind.EXACT_NAME, typeScope, queryName, modifiers);
    }

    private List<? extends MethodScopeImpl> getCachedMethods(final NameKind nameKind, 
            TypeScopeImpl typeScope, final String queryName, final int... modifiers) {
        List<MethodScopeImpl> toFilter = methodScopes.get(typeScope);
        if (toFilter == null) return Collections.emptyList();
        List<? extends MethodScopeImpl> retval = ScopeImpl.filter(toFilter, new ScopeImpl.ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.METHOD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName) &&
                        (modifiers.length == 0 || (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
        /*for (MethodScopeImpl methodScopeImpl : retval) {
            if (!methodScopeImpl.getClassScope().getFileObject().equals(typeScope.getFileObject())) {
                return Collections.emptyList();
            }
        }*/
        return retval;
    }

    private List<? extends InterfaceScopeImpl> getCachedIfaces(final String... queryName) {
        return getCachedIfaces(NameKind.EXACT_NAME, queryName);
    }

    private List<? extends InterfaceScopeImpl> getCachedIfaces(final NameKind nameKind, final String... queryName) {
        return ScopeImpl.filter(ifaceScopes, new ScopeImpl.ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return (queryName.length == 0 || ScopeImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    private List<? extends TypeScopeImpl> getCachedTypes(final String... queryName) {
        return getCachedTypes(NameKind.EXACT_NAME, queryName);
    }

    @SuppressWarnings("unchecked")
    private List<? extends TypeScopeImpl> getCachedTypes(final NameKind nameKind, final String... queryName) {
        return ModelUtils.merge(getCachedClasses(nameKind, queryName),getCachedIfaces(nameKind, queryName));
    }

    public List<? extends FunctionScopeImpl> getCachedFunctions(final String... queryName) {
        return getCachedFunctions(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends FunctionScopeImpl> getCachedFunctions(final NameKind nameKind, final String... queryName) {
        return ScopeImpl.filter(fncScopes, new ScopeImpl.ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.FUNCTION)  &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    public List<? extends ConstantElementImpl> getCachedConstants(final String... queryName) {
        return getCachedConstants(NameKind.EXACT_NAME, queryName);
    }

    private List<? extends ConstantElementImpl> getCachedConstants(final NameKind nameKind, final String... queryName) {
        return ScopeImpl.filter(constantScopes, new ScopeImpl.ElementFilter() {
            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CONSTANT)  &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

}
