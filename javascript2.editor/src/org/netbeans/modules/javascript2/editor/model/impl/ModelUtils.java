/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.javascript2.editor.model.*;
import org.netbeans.modules.javascript2.editor.model.impl.ScopeImpl.ElementFilter;

/**
 *
 * @author Petr Pisl
 */
public class ModelUtils {

    public static FileScopeImpl getFileScope(ModelElement element) {
        FileScopeImpl result = null;

        while (element != null && !(element instanceof FileScope)) {
            element = element.getInElement();
        }

        if (element != null && element instanceof FileScope) {
            result = (FileScopeImpl) element;
        }
        return result;
    }
    
    public static String getNameWithoutPrototype(List<Identifier> fqName) {
        StringBuilder name = new StringBuilder();
        int size = fqName.size();
        String part;
        for(int i = 0; i < size; i++) {
            part = fqName.get(i).getName();
            if ("prototype".equals(part)) {   //NOI18N
                break;
            }
            name.append(part);
            if (i < (size - 1) && !("prototype".equals(fqName.get(i+1).getName()))) {
               name.append(".");                //NOI18N
            }
        }
        return name.toString();
    }
    
    public static String getPartName(List<Identifier> fqName, int parts) {
        StringBuilder name = new StringBuilder();
        int size = fqName.size();
        String part;
        for(int i = 0; i < size && i < parts; i++) {
            part = fqName.get(i).getName();
            name.append(part);
            if (i < (size - 1) && i < (parts - 1)) {
               name.append(".");                //NOI18N
            }
        }
        return name.toString();
    }
    
    public static boolean isPrototype(FunctionScope function) {
        boolean result = false;
        int size = function.getFQDeclarationName().size();
        if(size > 1) {
            result = "prototype".equals(function.getFQDeclarationName().get(size - 2).getName()); // NOI18N
        }
        return result;
    }
    
    public static String getObjectName(FunctionScope function) {
        String name = null;
        int size = function.getFQDeclarationName().size();
        if(size > 1) {
            if (isPrototype(function)) {
                name = getNameWithoutPrototype(function.getFQDeclarationName());
            } else {
                name = getPartName(function.getFQDeclarationName(), size - 1);
            }
        }
        return name;
    }
    
    @CheckForNull
    public static <T extends ModelElement> T getFirst(Collection<? extends T> all) {
        if (all instanceof List) {
            return all.size() > 0 ? ((List<T>)all).get(0) : null;
        }
        return all.size() > 0 ? all.iterator().next() : null;
    }
    
    @CheckForNull
    public static <T extends ModelElement> List<? extends T>  getFirst(Collection<? extends T> elements, final String name) {
        return filter(elements, new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getName().equals(name);
            }
        });
    }
    
    public static <T extends ModelElement> List<? extends T> filter(final Collection<? extends T> instances, final ElementFilter<T> filter) {
        List<T> retval = new ArrayList<T>();
        for (T baseElement : instances) {
            boolean accepted = filter.isAccepted(baseElement);
            if (accepted) {
                retval.add(baseElement);
            }
        }
        return retval;
    }
    
    public static ModelElement find(final Collection<? extends ModelElement> instances, final JsElement.Kind kind, final String name) {
        return getFirst(filter(instances, new ElementFilter() {
            
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getJSKind().equals(kind) && element.getName().equals(name);
            }
        }));
    }
    
    public static Collection<? extends ObjectScope> getObjects(FileScope fileScope) {
        Collection<? extends Scope> elements = fileScope.getLogicalElements();
        return filter(elements, new ElementFilter() {
            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getJSKind().equals(JsElement.Kind.OBJECT);
            }
        });
    }
    
    public static Collection<? extends FunctionScope> getMethods(Scope scope) {
        Collection<? extends FunctionScope> result = filter(scope.getElements(), new ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                return element.getJSKind().equals(JsElement.Kind.METHOD);
            }
        });
        return result;
    }
    
    public static ObjectScopeImpl findObjectWithName(final Scope scope, final String name) {
        ObjectScope result = null;
        Collection<? extends ObjectScope> objects = ModelUtils.filter(scope.getElements(), new ScopeImpl.ElementFilter() {

            @Override
            public boolean isAccepted(ModelElement element) {
                boolean accept = false;
                if (element.getJSKind() == JsElement.Kind.OBJECT) {
                    List<Identifier> fqName = ((ObjectScope) element).getFQDeclarationName();
                    accept = fqName.get(0).getName().equals(name);
                }
                return accept;
            }
        });

        if (!objects.isEmpty()) {
            result = objects.iterator().next();
        } else {
            if (!(scope instanceof FileScope)) {
                result = findObjectWithName((Scope) scope.getInElement(), name);
            }
        }
        return (ObjectScopeImpl) result;
    }
}
