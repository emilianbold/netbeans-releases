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

package org.netbeans.modules.php.editor.nav;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.HtmlFormatter;
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.php.editor.api.ElementQuery.Index;
import org.netbeans.modules.php.editor.api.ElementQueryFactory;
import org.netbeans.modules.php.editor.api.NameKind;
import org.netbeans.modules.php.editor.api.QuerySupportFactory;
import org.netbeans.modules.php.editor.api.elements.ElementFilter;
import org.netbeans.modules.php.editor.api.elements.MethodElement;
import org.netbeans.modules.php.editor.api.elements.PhpElement;
import org.netbeans.modules.php.editor.api.elements.TypeElement;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.TypeScope;

/**
 * @author Radek Matous
 */
public class OverridingMethodsImpl implements OverridingMethods {
    private String classSignatureForInheritedMethods = "";//NOI18N
    private String classSignatureForInheritedByMethods = "";//NOI18N
    private String classSignatureForInheritedByTypes = "";//NOI18N
    /** just very simple implementation for now*/
    private Set<MethodElement> inheritedMethods = Collections.emptySet();
    private Set<MethodElement> inheritedByMethods = Collections.emptySet();
    private LinkedHashSet<TypeElement> inheritedByTypes = new LinkedHashSet<TypeElement>();
    @Override
    public Collection<? extends AlternativeLocation> overrides(ParserResult info, ElementHandle handle) {
        assert handle instanceof ModelElement;
        if (handle instanceof MethodScope) {
            MethodScope method = (MethodScope) handle;
            final ElementFilter methodNameFilter = ElementFilter.forName(NameKind.exact(method.getName()));
            final Set<MethodElement> overridenMethods = methodNameFilter.filter(getInheritedMethods(info, method));
            List<AlternativeLocation> retval = new ArrayList<AlternativeLocation>();
            for (MethodElement methodElement : overridenMethods) {
                retval.add(new MethodLocation(methodElement));
            }
            return retval;
        }
        return null;
    }

    @Override
    public Collection<? extends AlternativeLocation> overriddenBy(ParserResult info, ElementHandle handle) {
        assert handle instanceof ModelElement;
        if (handle instanceof MethodScope) {
            MethodScope method = (MethodScope) handle;
            final ElementFilter methodNameFilter = ElementFilter.forName(NameKind.exact(method.getName()));
            final Set<MethodElement> overridenByMethods = methodNameFilter.filter(getInheritedByMethods(info, method));
            List<AlternativeLocation> retval = new ArrayList<AlternativeLocation>();
            for (MethodElement methodElement : overridenByMethods) {
                retval.add(new MethodLocation(methodElement));
            }
            return retval;
        } else if (handle instanceof TypeScope) {
            List<AlternativeLocation> retval = new ArrayList<AlternativeLocation>();
            for (TypeElement typeElement : getInheritedByTypes(info, (TypeScope) handle)) {
                retval.add(new TypeLocation(typeElement));
            }
            return retval;
        }

        return null;
    }


    @Override
    public boolean isOverriddenBySupported(ParserResult info, ElementHandle handle) {
        return true;
    }

    /**
     * @return the inheritedMethods
     */
    private Set<MethodElement> getInheritedMethods(final ParserResult info, final MethodScope method) {
        final String signature = method.getInScope().getIndexSignature();
        if (!signature.equals(classSignatureForInheritedMethods)) {
            Index index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(info));
            inheritedMethods = index.getInheritedMethods((TypeScope) method.getInScope());
        }
        classSignatureForInheritedMethods = signature;
        return inheritedMethods;
    }


    /**
     * @return the inheritedByTypes
     */
    private LinkedHashSet<TypeElement> getInheritedByTypes(final ParserResult info, final TypeScope type) {
        final String signature = type.getIndexSignature();
        if (!signature.equals(classSignatureForInheritedByTypes)) {
            Index index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(info));
            inheritedByTypes = index.getInheritedByTypes(type);
        }
        classSignatureForInheritedByTypes = signature;
        return inheritedByTypes;
    }

    /**
     * @return the inheritedByMethods
     */
    private Set<MethodElement> getInheritedByMethods(final ParserResult info, final MethodScope method) {
        final String signature = method.getInScope().getIndexSignature();
        if (!signature.equals(classSignatureForInheritedByMethods)) {
            Index index = ElementQueryFactory.getIndexQuery(QuerySupportFactory.get(info));
            TypeScope type = (TypeScope) method.getInScope();
            inheritedByMethods = new HashSet<MethodElement>();
            for (TypeElement nextType : getInheritedByTypes(info,type)) {
                inheritedByMethods.addAll(index.getDeclaredMethods(nextType));
            }
        }
        classSignatureForInheritedByMethods = signature;
        return inheritedByMethods;
    }

    private static class MethodLocation extends DeclarationFinderImpl.AlternativeLocationImpl {
        public MethodLocation(PhpElement modelElement) {
            super(modelElement, new DeclarationLocation(modelElement.getFileObject(), modelElement.getOffset(), modelElement));
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder(30);
            MethodElement method = (MethodElement) getElement();
            final TypeElement type = method.getType();
            sb.append(type.getFullyQualifiedName().toNotFullyQualified().toString());
            sb.append(" ("); // NOI18N
            sb.append(type.getFileObject().getNameExt());
            sb.append(")"); // NOI18N
            return sb.toString();
        }
    }
    private static class TypeLocation extends DeclarationFinderImpl.AlternativeLocationImpl {
        public TypeLocation(PhpElement modelElement) {
            super(modelElement, new DeclarationLocation(modelElement.getFileObject(), modelElement.getOffset(), modelElement));
        }

        @Override
        public String getDisplayHtml(HtmlFormatter formatter) {
            StringBuilder sb = new StringBuilder(30);
            TypeElement type = (TypeElement) getElement();
            sb.append(type.getFullyQualifiedName().toNotFullyQualified().toString());
            sb.append(" ("); // NOI18N
            sb.append(type.getFileObject().getNameExt());
            sb.append(")"); // NOI18N
            return sb.toString();
        }
    }

}
