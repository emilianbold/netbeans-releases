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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.IndexScope;
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
class CachingSupport {

    @CheckForNull
    static CachingSupport getInstance(ModelElement elem) {
        FileScopeImpl fileScope = (FileScopeImpl) ModelUtils.getFileScope(elem);
        return (fileScope != null) ? fileScope.getCachingSupport() : null;
    }

    private FileScopeImpl fileScope;
    private List<ClassScope> classScopes = new ArrayList<ClassScope>();
    private List<InterfaceScope> ifaceScopes = new ArrayList<InterfaceScope>();
    private List<ConstantElement> constantScopes = new ArrayList<ConstantElement>();
    private List<FunctionScope> fncScopes = new ArrayList<FunctionScope>();
    private Map<TypeScope, List<MethodScope>> methodScopes =
            new LinkedHashMap <TypeScope,List<MethodScope>>();
    private Map<TypeScope, List<FieldElement>> fldElems =
            new LinkedHashMap <TypeScope,List<FieldElement>>();
    private Map<TypeScope, List<ClassConstantElement>> clzConstantElems =
            new LinkedHashMap <TypeScope,List<ClassConstantElement>>();


    CachingSupport(FileScopeImpl fileScope) {
        this.fileScope = fileScope;
    }

    static Collection<? extends ClassConstantElement> getInheritedConstants(ClassScope clsScope, String constName, ModelElement elem) {
        Collection<? extends ClassConstantElement> retval;
        CachingSupport modelSupport = CachingSupport.getInstance(elem);
        if (modelSupport != null) {
            retval = modelSupport.getInheritedMergedConstants((ClassScopeImpl)clsScope, constName);
        } else {
            retval = clsScope.findInheritedConstants(constName);
        }
        return retval;
    }

    //TODO: modifiers not taken into account
    static Collection<? extends FieldElement> getInheritedFields(ClassScope clsScope, String fieldName, ModelElement elem, final int... modifiers) {
        assert fieldName.startsWith("$") : fieldName;
        Collection<? extends FieldElement> retval;
        CachingSupport cachingSupport = CachingSupport.getInstance(elem);
        if (cachingSupport != null) {
            retval = cachingSupport.getInheritedMergedFields((ClassScopeImpl)clsScope, fieldName);
        } else {
            retval = clsScope.findInheritedFields(fieldName);
        }
        return retval;
    }

    static Collection<? extends MethodScope> getMethods(ClassScope clsScope, String methodName, ModelElement elem, final int... modifiers) {
        Collection<? extends MethodScope> retval;
        CachingSupport cachingSupport = CachingSupport.getInstance(elem);
        if (cachingSupport != null) {
            retval = cachingSupport.getMergedMethods((ClassScopeImpl)clsScope, methodName);
        } else {
            retval = clsScope.findDeclaredMethods(methodName, modifiers);
        }
        return retval;
    }

    //TODO: modifiers not taken into account
    static Collection<? extends MethodScope> getInheritedMethods(TypeScope typeScope, String methodName, ModelElement elem, final int... modifiers) {
        Collection<? extends MethodScope> retval;
        CachingSupport cachingSupport = CachingSupport.getInstance(elem);
        if (cachingSupport != null) {
            retval = cachingSupport.getInheritedMergedMethods((TypeScopeImpl)typeScope, methodName);
        } else {
            retval = typeScope.findInheritedMethods(methodName);
        }
        return retval;
    }

    static Collection<? extends TypeScope> getTypes(String typeName, ModelElement elem) {
        Collection<? extends TypeScope> retval;
        IndexScope indexScope = ModelUtils.getIndexScope(elem);
        CachingSupport cachingSupport = CachingSupport.getInstance(elem);
        if (cachingSupport != null) {
            retval = cachingSupport.getMergedTypes(typeName);
        } else {
            retval = indexScope.findTypes(typeName);
        }
        return retval;
    }

    static List<? extends ClassScope> getClasses(String className, ModelElement elem) {
        List<? extends ClassScope> retval;
        IndexScope indexScope = ModelUtils.getIndexScope(elem);
        CachingSupport modelSupport = CachingSupport.getInstance(elem);
        if (modelSupport != null) {
            retval = modelSupport.getMergedClasses(className);
        } else {
            retval = indexScope.findClasses(className);
        }
        return retval;
    }

    static List<? extends InterfaceScope> getInterfaces(String ifaceName, ModelElement elem) {
        List<? extends InterfaceScope> retval;
        IndexScope indexScope = ModelUtils.getIndexScope(elem);
        CachingSupport cachingSupport = CachingSupport.getInstance(elem);
        if (cachingSupport != null) {
            retval = cachingSupport.getMergedIfaces(ifaceName);
        } else {
            retval = ModelUtils.filter(indexScope.findInterfaces(ifaceName));
        }
        return retval;
    }

    static List<? extends ConstantElement> getConstants(String constantName, Scope scope) {
        List<? extends ConstantElement> retval;
        IndexScope indexScope = ModelUtils.getIndexScope(scope);
        CachingSupport cachingSupport = CachingSupport.getInstance(scope);
        if (cachingSupport != null) {
            retval = cachingSupport.getMergedConstants(constantName);
        } else {
            retval = ModelUtils.filter(indexScope.findConstants(constantName));
        }
        return retval;
    }

    static Collection<? extends FunctionScope> getFunctions(String fncName, ModelElement elem) {
        Collection<? extends FunctionScope> retval;
        IndexScope indexScope = ModelUtils.getIndexScope(elem);
        CachingSupport cachingSupport = CachingSupport.getInstance(elem);
        if (cachingSupport != null) {
            retval = cachingSupport.getMergedFunctions(fncName);
        } else {
            retval = ModelUtils.filter(indexScope.findFunctions(fncName));
        }
        return retval;
    }


    private Collection<? extends ClassConstantElement> getInheritedMergedConstants(ClassScopeImpl clsScope, String constName) {
        Collection<? extends ClassConstantElement> fields = getCachedClassConstants(clsScope, constName);
        if (fields.isEmpty()) {
            clsScope.findInheritedConstants(constName);
            fields = (clsScope != null ? clsScope.findInheritedConstants(constName) : Collections.<ClassConstantElementImpl>emptyList());
            if (fields.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                fields = (clsScope != null ? indexScope.findInheritedClassConstants(clsScope,constName) : Collections.<ClassConstantElementImpl>emptyList());
            }
            if (!fields.isEmpty()) {
                List<ClassConstantElement> methList = clzConstantElems.get(clsScope);
                if (methList != null) {
                    methList.add(ModelUtils.getFirst(fields));
                }
            }
        }
        return fields;
    }



    private List<? extends FieldElement>  getInheritedMergedFields(ClassScopeImpl clsScope, String fieldName) {
        List<? extends FieldElement> fields = getCachedFields(clsScope, fieldName);
        if (fields.isEmpty()) {
            fields = (clsScope != null ? clsScope.findInheritedFields(fieldName) : Collections.<FieldElementImpl>emptyList());
            if (fields.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                fields = (clsScope != null ? indexScope.findInheritedFields(clsScope,fieldName) : Collections.<FieldElementImpl>emptyList());
            }
            if (!fields.isEmpty()) {
                List<FieldElement> methList = fldElems.get(clsScope);
                if (methList != null) {
                    methList.add(ModelUtils.getFirst(fields));
                }
            }
        }
        return fields;
    }


    private List<? extends MethodScope> getInheritedMergedMethods(TypeScopeImpl typeScope, String methodName) {
        List<? extends MethodScope> methods = getCachedMethods(typeScope, methodName);
        if (methods.isEmpty()) {
            methods = (typeScope != null ? typeScope.findInheritedMethods(methodName) : Collections.<MethodScopeImpl>emptyList());
            if (methods.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                methods = (typeScope != null ? indexScope.findInheritedMethods(typeScope,methodName) : Collections.<MethodScopeImpl>emptyList());
            }
            if (!methods.isEmpty()) {
                List<MethodScope> methList = methodScopes.get(typeScope);
                if (methList == null) {
                    methodScopes.put(typeScope, methList = new ArrayList<MethodScope>());
                }
                methList.add(ModelUtils.getFirst(methods));
            }
        }
        return methods;

    }

    private Collection<? extends MethodScope> getMergedMethods(ClassScopeImpl clsScope, String methodName, final int... modifiers) {
        Collection<? extends MethodScope> methods = getCachedMethods(clsScope, methodName);
        if (methods.isEmpty()) {
            methods = (clsScope != null ? clsScope.findDeclaredMethods(methodName, modifiers) : Collections.<MethodScopeImpl>emptyList());
            if (methods.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                methods = (clsScope != null ? indexScope.findMethods(clsScope,methodName, modifiers) : Collections.<MethodScopeImpl>emptyList());
            }
            if (!methods.isEmpty()) {
                List<MethodScope> methList = methodScopes.get(clsScope);
                if (methList != null) {
                    methList.add(ModelUtils.getFirst(methods));
                }
            }
        }
        return methods;   
    }

    private List<? extends InterfaceScope> getMergedIfaces(String ifaceName) {
        List<? extends InterfaceScope> ifaces = getCachedInterfaces(ifaceName);
        if (ifaces.isEmpty()) {
            ifaces = (ifaceName != null ? ModelUtils.filter(ModelUtils.getDeclaredInterfaces(fileScope), ifaceName) : Collections.<InterfaceScopeImpl>emptyList());
            if (ifaces.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                ifaces = (ifaceName != null ? indexScope.findInterfaces(ifaceName) : Collections.<InterfaceScopeImpl>emptyList());
                //TODO: ModelUtils.getFirst
                InterfaceScope ifce = ModelUtils.getFirst(ifaces);
                if (ifce != null) {
                    ifaceScopes.add(ifce);
                    methodScopes.put(ifce, new ArrayList<MethodScope>());
                    fldElems.put(ifce, new ArrayList<FieldElement>());
                    clzConstantElems.put(ifce, new ArrayList<ClassConstantElement>());
                }
            }
        }
        return ifaces;
    }

    private List<? extends ClassScope> getMergedClasses(String clzName) {
        List<? extends ClassScope> classes = getCachedClasses(clzName);
        if (classes.isEmpty()) {            
            classes = (clzName != null ? ModelUtils.filter(ModelUtils.getDeclaredClasses(fileScope), clzName) : Collections.<ClassScopeImpl>emptyList());
            if (classes.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                classes = (clzName != null ? indexScope.findClasses(clzName) : Collections.<ClassScopeImpl>emptyList());
                //TODO: ModelUtils.getFirst
                ClassScope clz = ModelUtils.getFirst(classes);
                if (clz != null) {
                    classScopes.add(clz);
                    methodScopes.put(clz, new ArrayList<MethodScope>());
                    fldElems.put(clz, new ArrayList<FieldElement>());
                    clzConstantElems.put(clz, new ArrayList<ClassConstantElement>());
                }
            }
        }
        return classes;
    }

    private Collection<? extends TypeScope> getMergedTypes(String typeName) {
        Collection<? extends TypeScope> types = getCachedTypes(typeName);
        if (types.isEmpty()) {
            types = (typeName != null ? ModelUtils.filter(ModelUtils.getDeclaredTypes(fileScope), typeName) : Collections.<TypeScope>emptyList());
            if (types.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                types =  (typeName != null) ? indexScope.findTypes(typeName) : Collections.<TypeScopeImpl>emptyList();
                for (TypeScope tp : types) {
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

    private List<? extends ConstantElement> getMergedConstants(String constantName) {
        List<? extends ConstantElement> constants = getCachedConstants(constantName);
        if (constants.isEmpty()) {
            constants = (constantName != null ? ModelUtils.filter(ModelUtils.getDeclaredConstants(fileScope), constantName) : Collections.<ConstantElement>emptyList());
            if (constants.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                constants = (constantName != null ? indexScope.findConstants(constantName) : Collections.<ConstantElementImpl>emptyList());
                constantScopes.addAll(constants);
            }
        }
        return constants;
    }

    private List<? extends FunctionScope> getMergedFunctions(String fncName) {
        List<? extends FunctionScope> functions = getCachedFunctions(fncName);
        if (functions.isEmpty()) {
            functions = (fncName != null ? ModelUtils.filter(ModelUtils.getDeclaredFunctions(fileScope), fncName) : Collections.<FunctionScope>emptyList());
            if (functions.isEmpty()) {
                IndexScope indexScope = fileScope.getIndexScope();
                functions = (fncName != null ? indexScope.findFunctions(fncName) : Collections.<FunctionScope>emptyList());
                fncScopes.addAll(functions);
            }
        }
        return functions;
    }


    private List<? extends InterfaceScope> getCachedInterfaces(final String... queryName) {
        return getCachedInterfaces(QuerySupport.Kind.EXACT, queryName);
    }

    private List<? extends InterfaceScope> getCachedInterfaces(final QuerySupport.Kind nameKind, final String... queryName) {
        return ModelUtils.filter(ifaceScopes, new ModelUtils.ElementFilter<InterfaceScope>() {
            public boolean isAccepted(InterfaceScope element) {
                return element.getPhpKind().equals(PhpKind.IFACE) &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    private List<? extends ClassScope> getCachedClasses(final String... queryName) {
        return getCachedClasses(QuerySupport.Kind.EXACT, queryName);
    }

    private List<? extends ClassScope> getCachedClasses(final QuerySupport.Kind nameKind, final String... queryName) {
        return ModelUtils.filter(classScopes, new ModelUtils.ElementFilter<ClassScope>() {
            public boolean isAccepted(ClassScope element) {
                return element.getPhpKind().equals(PhpKind.CLASS) &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

       private List<? extends FieldElement> getCachedFields(ClassScope clsScope, String queryName,final int... modifiers) {
        return getCachedFields(QuerySupport.Kind.EXACT, clsScope, queryName, modifiers);
    }

    private List<? extends FieldElement> getCachedFields(final QuerySupport.Kind nameKind, final ClassScope clsScope, final String queryName, final int... modifiers) {
        List<FieldElement> toFilter = fldElems.get(clsScope);
        if (toFilter == null) return Collections.emptyList();
        return ModelUtils.filter(toFilter, new ModelUtils.ElementFilter<FieldElement>() {
            public boolean isAccepted(FieldElement element) {
                return element.getPhpKind().equals(PhpKind.FIELD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }
       private List<? extends ClassConstantElement> getCachedClassConstants(ClassScopeImpl clsScope, String queryName) {
        return getCachedClassConstants(QuerySupport.Kind.EXACT, clsScope, queryName);
    }

    private List<? extends ClassConstantElement> getCachedClassConstants(final QuerySupport.Kind nameKind, final ClassScopeImpl clsScope, final String queryName) {
        List<ClassConstantElement> toFilter = clzConstantElems.get(clsScope);
        if (toFilter == null) return Collections.emptyList();
        return ModelUtils.filter(toFilter, new ModelUtils.ElementFilter<ClassConstantElement>() {
            public boolean isAccepted(ClassConstantElement element) {
                return element.getPhpKind().equals(PhpKind.CLASS_CONSTANT) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName);
            }
        });
    }

    private List<? extends MethodScope> getCachedMethods(TypeScopeImpl typeScope, final String queryName, final int... modifiers) {
        return getCachedMethods(QuerySupport.Kind.EXACT, typeScope, queryName, modifiers);
    }

    private List<? extends MethodScope> getCachedMethods(final QuerySupport.Kind nameKind, 
            TypeScopeImpl typeScope, final String queryName, final int... modifiers) {
        List<MethodScope> toFilter = methodScopes.get(typeScope);
        if (toFilter == null) return Collections.emptyList();

        List<? extends MethodScope> retval = ModelUtils.filter(toFilter, new ModelUtils.ElementFilter<MethodScope>() {
            public boolean isAccepted(MethodScope element) {
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

    private Collection<? extends InterfaceScope> getCachedIfaces(final String... queryName) {
        return getCachedIfaces(QuerySupport.Kind.EXACT, queryName);
    }

    private Collection<? extends InterfaceScope> getCachedIfaces(final QuerySupport.Kind nameKind, final String... queryName) {
        return ModelUtils.filter(ifaceScopes, new ModelUtils.ElementFilter<InterfaceScope>() {
            public boolean isAccepted(InterfaceScope element) {
                return (queryName.length == 0 || ScopeImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    private Collection<? extends TypeScope> getCachedTypes(final String... queryName) {
        return getCachedTypes(QuerySupport.Kind.EXACT, queryName);
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends TypeScope> getCachedTypes(final QuerySupport.Kind nameKind, final String... queryName) {
        return ModelUtils.merge(getCachedClasses(nameKind, queryName),getCachedIfaces(nameKind, queryName));
    }

    public List<? extends FunctionScope> getCachedFunctions(final String... queryName) {
        return getCachedFunctions(QuerySupport.Kind.EXACT, queryName);
    }

    public List<? extends FunctionScope> getCachedFunctions(final QuerySupport.Kind nameKind, final String... queryName) {
        return ModelUtils.filter(fncScopes, new ModelUtils.ElementFilter<FunctionScope>() {
            public boolean isAccepted(FunctionScope element) {
                return element.getPhpKind().equals(PhpKind.FUNCTION)  &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    public List<? extends ConstantElement> getCachedConstants(final String... queryName) {
        return getCachedConstants(QuerySupport.Kind.EXACT, queryName);
    }

    private List<? extends ConstantElement> getCachedConstants(final QuerySupport.Kind nameKind, final String... queryName) {
        return ModelUtils.filter(constantScopes, new ModelUtils.ElementFilter<ConstantElement>() {
            public boolean isAccepted(ConstantElement element) {
                return element.getPhpKind().equals(PhpKind.CONSTANT)  &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

}
