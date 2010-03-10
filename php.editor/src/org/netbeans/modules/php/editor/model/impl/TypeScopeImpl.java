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
import org.netbeans.modules.php.editor.model.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.NamespaceIndexFilter;
import org.netbeans.modules.php.editor.api.PhpElementKind;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.InterfaceElement;
import org.netbeans.modules.php.editor.model.nodes.ClassDeclarationInfo;
import org.netbeans.modules.php.editor.model.nodes.InterfaceDeclarationInfo;
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
            ifaces.put(CodeUtils.extractUnqualifiedName(identifier), null);
        }
    }

    TypeScopeImpl(Scope inScope, InterfaceDeclarationInfo nodeInfo) {
        super(inScope, nodeInfo, PhpModifiers.fromBitMask(PhpModifiers.PUBLIC), nodeInfo.getOriginalNode().getBody());
        List<? extends Expression> interfaces = nodeInfo.getInterfaces();
        for (Expression identifier : interfaces) {
            ifaces.put(CodeUtils.extractUnqualifiedName(identifier), null);
        }
    }

    protected TypeScopeImpl(Scope inScope, ClassElement element) {
        //TODO: in idx is no info about ifaces
        super(inScope, element, PhpElementKind.CLASS);
    }

    protected TypeScopeImpl(Scope inScope, InterfaceElement element) {
        //TODO: in idx is no info about ifaces
        super(inScope, element, PhpElementKind.IFACE);
    }

    public List<? extends String> getSuperInterfaceNames() {
        if (indexedElement instanceof ClassElement) {
            List<String> retval = new ArrayList<String>();
            final Set<QualifiedName> superInterfaces = ((ClassElement) indexedElement).getSuperInterfaces();
            for (QualifiedName qualifiedName : superInterfaces) {
                retval.add(qualifiedName.toString());
            }
            return retval;
        }
        return new ArrayList<String>(ifaces.keySet());
    }

    public List<? extends InterfaceScope> getSuperInterfaceScopes() {
        Set<InterfaceScope> retval = new LinkedHashSet<InterfaceScope>();
        Set<String> keySet = (indexedElement instanceof ClassElement) ? new HashSet<String>(getSuperInterfaceNames()) : ifaces.keySet();
        for (String ifaceName : keySet) {
            List<? extends InterfaceScope> iface = ifaces.get(ifaceName);
            if (iface == null) {
                if (indexedElement == null) {
                    NamespaceScope top = (NamespaceScope) getInScope();
                    NamespaceScopeImpl ps = (NamespaceScopeImpl) top;
                    retval.addAll(iface = ModelUtils.filter(ps.getDeclaredInterfaces(), ifaceName));
                    ifaces.put(ifaceName,iface);
                    /*for (InterfaceScopeImpl interfaceScope : iface) {
                        retval.addAll(interfaceScope.getInterfaces());
                    }*/
                    if (retval.isEmpty() && top instanceof NamespaceScopeImpl) {
                        IndexScope indexScope = ModelUtils.getIndexScope(ps);
                        if (indexScope != null) {
                            Collection<? extends InterfaceScope> cIfaces =CachingSupport.getInterfaces(ifaceName, this);
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
                    iface = Collections.emptyList();
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
        if (ModelUtils.getFileScope(this) == null) {
            IndexScope indexScopeImpl = ModelUtils.getIndexScope(this);
            return indexScopeImpl.findMethods(this);
        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.METHOD);
            }
        });
    }

    public Collection<? extends MethodScope> findDeclaredMethods(final String queryName, final int... modifiers) {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScopeImpl indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            QualifiedName qn = getNamespaceName().append(getName());
            NamespaceIndexFilter filter = new NamespaceIndexFilter(qn.toString());
            List<? extends MethodScope> methods = indexScopeImpl.findMethods(this, queryName, modifiers);
            return filter.filterModelElements(methods, true);
        }

        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.METHOD) &&
                        ModelElementImpl.nameKindMatch(element.getName(), QuerySupport.Kind.EXACT, queryName) &&
                        (modifiers.length == 0 ||
                        (element.getPhpModifiers().toFlags() & PhpModifiers.fromBitMask(modifiers).toFlags()) != 0);
            }
        });
    }


    public final Collection<? extends ClassConstantElement> getDeclaredConstants() {
        if (ModelUtils.getFileScope(this) == null) {
            IndexScopeImpl indexScopeImpl = (IndexScopeImpl) ModelUtils.getIndexScope(this);
            return indexScopeImpl.findClassConstants(this);//NOI18N
        }
        return filter(getElements(), new ElementFilter() {

            public boolean isAccepted(ModelElement element) {
                return element.getPhpElementKind().equals(PhpElementKind.TYPE_CONSTANT);
            }
        });
    }

    @Override
    public String getNormalizedName() {
        StringBuilder sb = new StringBuilder();
        //Set<String> ifaceNames = ifaces.keySet();
        List<? extends String> ifaceNames = getSuperInterfaceNames();
        for (String ifName : ifaceNames) {
            sb.append(ifName);
        }
        return sb.toString()+super.getNormalizedName();
    }

    @Override
    public Set<QualifiedName> getSuperInterfaces() {
        Set<QualifiedName> retval = new HashSet<QualifiedName>();
        List<? extends String> superInterfaceNames = getSuperInterfaceNames();
        for (String name : superInterfaceNames) {
            retval.add(QualifiedName.create(name));
        }
        return retval;
    }

    @Override
    public final boolean isClass() {
        return this.getPhpElementKind().equals(PhpElementKind.CLASS);
    }

    @Override
    public final boolean isInterface() {
        return this.getPhpElementKind().equals(PhpElementKind.IFACE);
    }

}
