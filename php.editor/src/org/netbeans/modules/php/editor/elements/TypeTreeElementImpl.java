/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.elements;

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.php.editor.api.ElementQuery;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QualifiedName;
import org.netbeans.modules.php.editor.api.elements.ClassElement;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.api.elements.TypeTreeElement;

/**
 * @author Radek Matous
 */
final class TypeTreeElementImpl  implements TypeTreeElement {
    private final TypeElement delegate;
    private final Set<TypeElement> preferredTypes;
    TypeTreeElementImpl(final TypeElement delegate) {
        this(delegate, new HashSet<TypeElement>());
    }
    TypeTreeElementImpl(final TypeElement delegate, final Set<TypeElement> preferredTypes) {
        this.delegate = delegate;
        this.preferredTypes = preferredTypes;
    }    

    @Override
    public Set<TypeTreeElement> getDirectlyInherited() {
        final HashSet<TypeTreeElement> directTypes = new HashSet<TypeTreeElement>();
        if (delegate instanceof ClassElement) {
            final QualifiedName superClassName = ((ClassElement) delegate).getSuperClassName();
            if (superClassName != null) {
                final ElementFilter forName = ElementFilter.forName(NameKind.exact(superClassName));
                Set<TypeElement> types = forName.filter(preferredTypes);
                if (types.isEmpty()) {
                    Index index = getIndex();
                    types= index.getTypes(NameKind.exact(superClassName));
                }
                for (TypeElement typeElementImpl : types) {
                    directTypes.add(new TypeTreeElementImpl(typeElementImpl, preferredTypes));
                }
            }
        }
        for (final QualifiedName iface : delegate.getSuperInterfaces()) {
            final ElementFilter forName = ElementFilter.forName(NameKind.exact(iface));
            Set<TypeElement> types = forName.filter(preferredTypes);
            if (types.isEmpty()) {
                Index index = getIndex();
                types = index.getTypes(NameKind.exact(iface));
            }
            for (TypeElement typeElementImpl : types) {
                directTypes.add(new TypeTreeElementImpl(typeElementImpl, preferredTypes));
            }
        }
        return directTypes;
    }

    @Override
    public TypeElement getType() {
        return delegate;
    }

    private Index getIndex() {
        final ElementQuery elementQuery = delegate.getElementQuery();
        boolean indexScope = elementQuery.getQueryScope().isIndexScope();
        if (indexScope && (elementQuery instanceof Index)) {
            return (Index) elementQuery;
        }
        return null;
    }
}
