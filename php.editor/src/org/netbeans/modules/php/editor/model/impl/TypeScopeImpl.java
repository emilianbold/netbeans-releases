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
import org.netbeans.modules.php.editor.model.*;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;

/**
 *
 * @author Radek Matous
 */
abstract class TypeScopeImpl extends ScopeImpl implements TypeScope {

    private Map<String, List<? extends InterfaceScope>> ifaces = new HashMap<String, List<? extends InterfaceScope>>();

    TypeScopeImpl(Scope inScope, ClassDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), nodeInfo.getOriginalNode().getBody());
        List<? extends Expression> interfaces = nodeInfo.getInterfaces();
        for (Expression identifier : interfaces) {
            ifaces.put(CodeUtils.extractTypeName(identifier), null);
        }
    }

    TypeScopeImpl(Scope inScope, InterfaceDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, new PhpModifiers(PhpModifiers.PUBLIC), nodeInfo.getOriginalNode().getBody());
        List<? extends Expression> interfaces = nodeInfo.getInterfaces();
        for (Expression identifier : interfaces) {
            ifaces.put(CodeUtils.extractTypeName(identifier), null);
        }
    }

    protected TypeScopeImpl(Scope inScope, IndexedClass element) {
        //TODO: in idx is no info about ifaces
        super(inScope, element, PhpKind.CLASS);
    }

    protected TypeScopeImpl(Scope inScope, IndexedInterface element) {
        //TODO: in idx is no info about ifaces
        super(inScope, element, PhpKind.IFACE);
    }


    public List<? extends InterfaceScope> getSuperInterfaces() {
        Set<InterfaceScope> retval = new LinkedHashSet<InterfaceScope>();
        Set<String> keySet = ifaces.keySet();
        for (String ifaceName : keySet) {
            List<? extends InterfaceScope> iface = ifaces.get(ifaceName);
            if (iface == null) {
                FileScope top = (FileScope) getInScope();
                FileScopeImpl ps = (FileScopeImpl) top;
                retval.addAll(iface = ModelUtils.filter(ps.getDeclaredInterfaces(), ifaceName));
                ifaces.put(ifaceName,iface);
                /*for (InterfaceScopeImpl interfaceScope : iface) {
                    retval.addAll(interfaceScope.getInterfaces());
                }*/
                if (retval.isEmpty() && top instanceof FileScopeImpl) {
                    IndexScope indexScope = ModelUtils.getIndexScope(ps);
                    if (indexScope != null) {
                        List<? extends InterfaceScope> cIfaces =CachingSupport.getInterfaces(ifaceName, this);
                        ifaces.put(ifaceName,(List<? extends InterfaceScopeImpl>)cIfaces);
                        for (InterfaceScope interfaceScope : cIfaces) {
                            retval.add((InterfaceScopeImpl)interfaceScope);                            
                        }
                    } else {
                        //TODO: create it from idx
                        throw new UnsupportedOperationException();
                        /*assert iface != null;
                        ifaces.put(key, iface);*/
                    }
                }
            } else {
                retval.addAll(iface);
            }
            assert iface != null;
        //duplicatesChecker.addAll(iface);
        }
        return new ArrayList<InterfaceScope>(retval);
    }

    public Collection<? extends MethodScope> getDeclaredMethods() {
        return getDeclaredMethodsImpl();
    }

    public Collection<? extends MethodScope> getDeclaredMethodsImpl(final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl = ModelUtils.getIndexScope(this);
            return indexScopeImpl.findMethods(this,"", modifiers);
        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.METHOD) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public Collection<? extends MethodScope> findDeclaredMethods(final String queryName, final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScopeImpl indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            return indexScopeImpl.findMethods(this, queryName, modifiers);
        }

        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.METHOD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), QuerySupport.Kind.EXACT, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public Collection<? extends MethodScope> findDeclaredMethods(final QuerySupport.Kind nameKind, final String queryName,
            final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl = ModelUtils.getIndexScope(this);
            return indexScopeImpl.findMethods(this, nameKind, queryName, modifiers);
        }

        //TODO: example how to improve perf. for regexp lookup
        if (nameKind.equals(QuerySupport.Kind.REGEXP) || nameKind.equals(QuerySupport.Kind.CASE_INSENSITIVE_REGEXP)) {
            final Pattern p = Pattern.compile(nameKind.equals(QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) ? queryName.toLowerCase() : queryName);
            return filter(getElements(), new ElementFilter() {

                public boolean isAccepted(ModelElement element) {
                    return element.getPhpKind().equals(PhpKind.METHOD) &&
                            ModelElementImpl.nameKindMatch(p, element.getName()) &&
                            (modifiers.length == 0 ||
                            (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
                }
            });

        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.METHOD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public final Collection<? extends ClassConstantElement> getDeclaredConstants() {
        return findDeclaredConstants();
    }

    public Collection<? extends ClassConstantElement> findDeclaredConstants(String... queryName) {
        return findDeclaredConstants(QuerySupport.Kind.EXACT, queryName);
    }

    public Collection<? extends ClassConstantElement> findDeclaredConstants(final QuerySupport.Kind nameKind, final String... queryName) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScopeImpl indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            return indexScopeImpl.findClassConstants(this, queryName);
        }

        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpKind().equals(PhpKind.CLASS_CONSTANT) &&
                        (queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName));
            }
        });
    }

    public final Collection<? extends ClassConstantElement> getInheritedConstants() {
        List<ClassConstantElement> allConstants = new ArrayList<ClassConstantElement>();
        allConstants.addAll(getDeclaredConstants());
        if (allConstants.isEmpty()) {
            IndexScope indexScope = ModelUtils.getIndexScope(this);
            PHPIndex index = indexScope.getIndex();
            TypeScope type = this;
            if (type instanceof ClassScope) {
                ClassScope clz = (ClassScope) type;
                while (clz != null && allConstants.isEmpty()) {
                    clz = ModelUtils.getFirst(clz.getSuperClasses());
                    if (clz != null) {
                        Collection<IndexedConstant> indexedConstants = index.getClassConstants(null, clz.getName(), "", QuerySupport.Kind.PREFIX);//NOI18N
                        for (IndexedConstant indexedConstant : indexedConstants) {
                            allConstants.add(new ClassConstantElementImpl((TypeScopeImpl) type, indexedConstant));
                        }
                    }
                }
            } else if (type instanceof InterfaceScope) {
                InterfaceScope iface = (InterfaceScope) type;
                Collection<? extends InterfaceScope> interfaceScopes = iface.getSuperInterfaces();
                if (allConstants.isEmpty()) {
                    for (InterfaceScope ifaceScope : interfaceScopes) {
                        Collection<IndexedConstant> indexedConstants = index.getClassConstants(null, ifaceScope.getName(), "", QuerySupport.Kind.PREFIX);//NOI18N
                        for (IndexedConstant indexedConstant : indexedConstants) {
                            allConstants.add(new ClassConstantElementImpl((TypeScopeImpl) ifaceScope, indexedConstant));
                            break;
                        }
                    }
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return allConstants;
    }

    public Collection<? extends ClassConstantElement> findInheritedConstants(String queryName) {
        List<ClassConstantElement> allConstants = new ArrayList<ClassConstantElement>();
        allConstants.addAll(findDeclaredConstants(queryName));
        if (allConstants.isEmpty()) {
            IndexScope indexScope = ModelUtils.getIndexScope(this);
            PHPIndex index = indexScope.getIndex();
            TypeScope type = this;
            if (type instanceof ClassScope) {
                ClassScope clz = (ClassScope) type;
                while (clz != null && allConstants.isEmpty()) {
                    clz = ModelUtils.getFirst(clz.getSuperClasses());
                    if (clz != null) {
                        Collection<IndexedConstant> indexedConstants = index.getClassConstants(null, clz.getName(), queryName, QuerySupport.Kind.PREFIX);
                        for (IndexedConstant indexedConstant : indexedConstants) {
                            allConstants.add(new ClassConstantElementImpl((TypeScopeImpl) type, indexedConstant));
                        }
                    }
                }
            } else if (type instanceof InterfaceScope) {
                InterfaceScope iface = (InterfaceScope) type;
                Collection<? extends InterfaceScope> interfaceScopes = iface.getSuperInterfaces();
                if (allConstants.isEmpty()) {
                    for (InterfaceScope ifaceScope : interfaceScopes) {
                        Collection<IndexedConstant> indexedConstants = index.getClassConstants(null, ifaceScope.getName(), "", QuerySupport.Kind.PREFIX);//NOI18N
                        for (IndexedConstant indexedConstant : indexedConstants) {
                            allConstants.add(new ClassConstantElementImpl((TypeScopeImpl) ifaceScope, indexedConstant));
                            break;
                        }
                    }
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return allConstants;
    }

      protected List<? extends MethodScope> getInheritedMethodsImpl() {
        List<MethodScope> allMethods = new ArrayList<MethodScope>();
        allMethods.addAll(getDeclaredMethods());
        if (allMethods.isEmpty()) {
            IndexScope indexScope = ModelUtils.getIndexScope(this);
            PHPIndex index = indexScope.getIndex();
            TypeScope type = this;
            if (type instanceof ClassScope) {
                ClassScope clz = (ClassScope) type;
                while (clz != null && allMethods.isEmpty()) {
                    clz = ModelUtils.getFirst(clz.getSuperClasses());
                    if (clz != null) {
                        Collection<IndexedFunction> indexedFunctions = index.getMethods(null, clz.getName(), "", QuerySupport.Kind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);//NOI18N
                        for (IndexedFunction indexedFunction : indexedFunctions) {
                            allMethods.add(new MethodScopeImpl((TypeScopeImpl) clz, indexedFunction, PhpKind.METHOD));
                        }
                    }
                }
            } else if (type instanceof InterfaceScope) {
                InterfaceScope iface = (InterfaceScope) type;
                Collection<? extends InterfaceScope> interfaceScopes = iface.getSuperInterfaces();
                if (allMethods.isEmpty()) {
                    for (InterfaceScope ifaceScope : interfaceScopes) {
                        Collection<IndexedFunction> indexedFunctions = index.getMethods(null, ifaceScope.getName(), "", QuerySupport.Kind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);//NOI18N
                        for (IndexedFunction indexedFunction : indexedFunctions) {
                            allMethods.add(new MethodScopeImpl((TypeScopeImpl) ifaceScope, indexedFunction, PhpKind.METHOD));
                            break;
                        }
                    }
                }
            } else {
                throw new IllegalStateException();
            }

        }
        return allMethods;
    }

    public List<? extends MethodScope> findInheritedMethods(String queryName) {
        List<MethodScope> allMethods = new ArrayList<MethodScope>();
        allMethods.addAll(findDeclaredMethods(queryName));
        if (allMethods.isEmpty()) {
            IndexScope indexScope = ModelUtils.getIndexScope(this);
            PHPIndex index = indexScope.getIndex();
            TypeScope type = this;
            if (type instanceof ClassScope) {
                ClassScope clz = (ClassScope) type;
                while (clz != null && allMethods.isEmpty()) {
                    clz = ModelUtils.getFirst(clz.getSuperClasses());
                    if (clz != null) {
                        Collection<IndexedFunction> indexedFunctions = index.getMethods(null, clz.getName(), queryName, QuerySupport.Kind.EXACT, Modifier.PUBLIC | Modifier.PROTECTED);
                        for (IndexedFunction indexedFunction : indexedFunctions) {
                            allMethods.add(new MethodScopeImpl((TypeScopeImpl) clz, indexedFunction, PhpKind.METHOD));
                        }
                    }
                }
            } else if (type instanceof InterfaceScope) {
                InterfaceScope iface = (InterfaceScope) type;
                Collection<? extends InterfaceScope> interfaceScopes = iface.getSuperInterfaces();
                if (allMethods.isEmpty()) {
                    for (InterfaceScope ifaceScope : interfaceScopes) {
                        Collection<IndexedFunction> indexedFunctions = index.getMethods(null, ifaceScope.getName(), "", QuerySupport.Kind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);//NOI18N
                        for (IndexedFunction indexedFunction : indexedFunctions) {
                            allMethods.add(new MethodScopeImpl((TypeScopeImpl) ifaceScope, indexedFunction, PhpKind.METHOD));
                            break;
                        }
                    }
                }

            } else {
                throw new IllegalStateException();
            }

        }
        return allMethods;
    }

    @Override
    public String getNormalizedName() {
        StringBuilder sb = new StringBuilder();
        Set<String> ifaceNames = ifaces.keySet();
        for (String ifName : ifaceNames) {
            sb.append(ifName);
        }
        return sb.toString()+super.getNormalizedName();
    }
}
