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
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.index.IndexedClass;
import org.netbeans.modules.php.editor.index.IndexedConstant;
import org.netbeans.modules.php.editor.index.IndexedFunction;
import org.netbeans.modules.php.editor.index.IndexedInterface;
import org.netbeans.modules.php.editor.index.PHPIndex;
import org.netbeans.modules.php.editor.model.nodes.ClassConstantDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;

/**
 *
 * @author Radek Matous
 */
abstract class TypeScopeImpl extends ScopeImpl implements TypeScope {

    private Map<String, List<? extends InterfaceScopeImpl>> ifaces = new HashMap<String, List<? extends InterfaceScopeImpl>>();

    ClzConstantElementImpl createElement(ClassConstantDeclarationInfo clsConst) {
        ClzConstantElementImpl retval = ClzConstantElementImpl.createClzConstantElementImpl(this, clsConst);
        addElement(retval);
        return retval;
    }

    TypeScopeImpl(ScopeImpl inScope, ClassDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, nodeInfo.getAccessModifiers(), nodeInfo.getOriginalNode().getBody());
        List<? extends Identifier> interfaces = nodeInfo.getInterfaces();
        for (Identifier identifier : interfaces) {
            ifaces.put(identifier.getName(), null);
        }
    }

    TypeScopeImpl(ScopeImpl inScope, InterfaceDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, new PhpModifiers(PhpModifiers.PUBLIC), nodeInfo.getOriginalNode().getBody());
        List<? extends Identifier> interfaces = nodeInfo.getInterfaces();
        for (Identifier identifier : interfaces) {
            ifaces.put(identifier.getName(), null);
        }
    }

    protected TypeScopeImpl(ScopeImpl inScope, IndexedClass element) {
        //TODO: in idx is no info about ifaces
        super(inScope, element, PhpKind.CLASS);
    }

    protected TypeScopeImpl(ScopeImpl inScope, IndexedInterface element) {
        //TODO: in idx is no info about ifaces
        super(inScope, element, PhpKind.IFACE);
    }


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

    public List<? extends InterfaceScopeImpl> getInterfaces() {
        Set<InterfaceScopeImpl> retval = new LinkedHashSet<InterfaceScopeImpl>();
        Set<String> keySet = ifaces.keySet();
        for (String ifaceName : keySet) {
            List<? extends InterfaceScopeImpl> iface = ifaces.get(ifaceName);
            if (iface == null) {
                ModelScope top = (ModelScope) getInScope();
                FileScope ps = (FileScope) top;
                retval.addAll(iface = ps.getInterfaces(ifaceName));
                ifaces.put(ifaceName,iface);
                /*for (InterfaceScopeImpl interfaceScope : iface) {
                    retval.addAll(interfaceScope.getInterfaces());
                }*/
                if (retval.isEmpty() && top instanceof FileScope) {
                    IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
                    if (indexScopeImpl == null) {
                        indexScopeImpl = ps.getIndexScope();
                    }
                    if (indexScopeImpl != null) {
                        List<? extends InterfaceScope> cIfaces =CachedModelSupport.getInterfaces(ifaceName, this);
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
        return new ArrayList<InterfaceScopeImpl>(retval);
    }

    public List<? extends MethodScope> getAllMethods() {
        return getMethods();
    }

    public List<? extends MethodScopeImpl> getMethods(final int... modifiers) {
        IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
        if (indexScopeImpl != null) {
            return indexScopeImpl.getMethods(this, modifiers);
        } 
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.METHOD) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public List<? extends MethodScopeImpl> getMethods(final String queryName, final int... modifiers) {
        IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
        if (indexScopeImpl != null) {
            return indexScopeImpl.getMethods(this, queryName, modifiers);
        }

        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.METHOD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), NameKind.EXACT_NAME, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public List<? extends MethodScopeImpl> getMethods(final NameKind nameKind, final String queryName,
            final int... modifiers) {
        IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
        if (indexScopeImpl != null) {
            return indexScopeImpl.getMethods(nameKind, this, queryName, modifiers);
        }

        //TODO: example how to improve perf. for regexp lookup
        if (nameKind.equals(NameKind.REGEXP) || nameKind.equals(NameKind.CASE_INSENSITIVE_REGEXP)) {
            final Pattern p = Pattern.compile(nameKind.equals(NameKind.CASE_INSENSITIVE_REGEXP) ? queryName.toLowerCase() : queryName);
            return filter(getElements(), new ElementFilter() {

                public boolean isAccepted(ModelElementImpl element) {
                    return element.getPhpKind().equals(PhpKind.METHOD) &&
                            ModelElementImpl.nameKindMatch(p, element.getName()) &&
                            (modifiers.length == 0 ||
                            (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
                }
            });

        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.METHOD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toBitmask() & new PhpModifiers(modifiers).toBitmask()) != 0);
            }
        });
    }

    public List<? extends ClzConstantElementImpl> getAllConstants() {
        return getConstants();
    }

    public List<? extends ClzConstantElementImpl> getConstants(String... queryName) {
        return getConstants(NameKind.EXACT_NAME, queryName);
    }

    public List<? extends ClzConstantElementImpl> getConstants(final NameKind nameKind, final String... queryName) {
        IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
        if (indexScopeImpl != null) {
            return indexScopeImpl.getConstants(this, queryName);
        }

        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElementImpl element) {
                return element.getPhpKind().equals(PhpKind.CLASS_CONSTANT) &&
                        queryName.length == 0 || ModelElementImpl.nameKindMatch(element.getName(), nameKind, queryName);
            }
        });
    }

    public List<? extends ClzConstantElementImpl> getInheritedConstants(String queryName) {
        List<ClzConstantElementImpl> allConstants = new ArrayList<ClzConstantElementImpl>();
        allConstants.addAll(getConstants(queryName));
        if (allConstants.isEmpty()) {
            IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
            indexScopeImpl = ((indexScopeImpl != null) ? indexScopeImpl : ((FileScope) ModelUtils.getModelScope(this)).getIndexScope());
            PHPIndex index = indexScopeImpl.getIndex();
            TypeScope type = this;
            if (type instanceof ClassScope) {
                ClassScope clz = (ClassScope) type;
                while (type != null && allConstants.isEmpty()) {
                    type = ModelUtils.getFirst(clz.getSuperClasses());
                    if (type != null) {
                        Collection<IndexedConstant> indexedConstants = index.getClassConstants(null, type.getName(), queryName, NameKind.PREFIX);
                        for (IndexedConstant indexedConstant : indexedConstants) {
                            allConstants.add(new ClzConstantElementImpl((TypeScopeImpl) type, indexedConstant));
                        }
                    }
                }
            } else if (type instanceof InterfaceScope) {
                InterfaceScope iface = (InterfaceScope) type;
                List<? extends InterfaceScope> interfaceScopes = iface.getInterfaces();
                for (int i = 0; allConstants.isEmpty() && i < interfaceScopes.size(); i++) {
                    InterfaceScope ifaceScope = interfaceScopes.get(i);
                    Collection<IndexedConstant> indexedConstants = index.getClassConstants(null, ifaceScope.getName(), queryName, NameKind.PREFIX);
                    for (IndexedConstant indexedConstant : indexedConstants) {
                        allConstants.add(new ClzConstantElementImpl((TypeScopeImpl) ifaceScope, indexedConstant));
                    }
                }
            } else {
                throw new IllegalStateException();
            }
        }
        return allConstants;
    }

    public List<? extends MethodScopeImpl> getInheritedMethods(String queryName) {
        List<MethodScopeImpl> allMethods = new ArrayList<MethodScopeImpl>();
        allMethods.addAll(getMethods(queryName));
        if (allMethods.isEmpty()) {
            IndexScopeImpl indexScopeImpl = getTopIndexScopeImpl();
            indexScopeImpl = ((indexScopeImpl != null) ? indexScopeImpl : ((FileScope) ModelUtils.getModelScope(this)).getIndexScope());
            PHPIndex index = indexScopeImpl.getIndex();
            TypeScope type = this;
            if (type instanceof ClassScope) {
                ClassScope clz = (ClassScope) type;
                while (clz != null && allMethods.isEmpty()) {
                    clz = ModelUtils.getFirst(clz.getSuperClasses());
                    if (clz != null) {
                        Collection<IndexedFunction> indexedFunctions = index.getMethods(null, clz.getName(), queryName, NameKind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
                        for (IndexedFunction indexedFunction : indexedFunctions) {
                            allMethods.add(new MethodScopeImpl((TypeScopeImpl) clz, indexedFunction, PhpKind.METHOD));
                        }
                    }
                }
            } else if (type instanceof InterfaceScope) {
                InterfaceScope iface = (InterfaceScope) type;
                List<? extends InterfaceScope> interfaceScopes = iface.getInterfaces();
                for (int i = 0; allMethods.isEmpty() && i < interfaceScopes.size(); i++) {
                    InterfaceScope ifaceScope = interfaceScopes.get(i);
                    Collection<IndexedFunction> indexedFunctions = index.getMethods(null, ifaceScope.getName(), queryName, NameKind.PREFIX, Modifier.PUBLIC | Modifier.PROTECTED);
                    for (IndexedFunction indexedFunction : indexedFunctions) {
                        allMethods.add(new MethodScopeImpl((TypeScopeImpl) ifaceScope, indexedFunction, PhpKind.METHOD));
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
