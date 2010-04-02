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
package org.netbeans.modules.ruby;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.ruby.elements.ClassElement;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.MethodElement;

/**
 * Ruby impl of {@code OverridingMethods}.
 *
 * @author Erno Mononen
 */
final class OverridingMethodsImpl implements OverridingMethods {

    private static final class Cache {
        /** Cache for inherited methods */
        final Map<String, Set<IndexedMethod>> inherited = new HashMap<String, Set<IndexedMethod>>();
        /** Cache for subclasses */
        final Map<String, Set<IndexedClass>> subClasses = new HashMap<String, Set<IndexedClass>>();
    }

    private final Map<ParserResult, Cache> cacheHolder = new HashMap<ParserResult, Cache>(1);

    public OverridingMethodsImpl() {
    }

    @Override
    public Collection<? extends AlternativeLocation> overrides(ParserResult info, ElementHandle handle) {
        if (handle.getKind() == ElementKind.METHOD) {
            MethodElement methodElement = (MethodElement) handle;
            for (IndexedMethod each : getInheritedMethods(methodElement.getIn(), info)) {
                if (methodElement.getName().equals(each.getName())) {
                    return asLocations(Collections.singleton(each));
                }
            }
        }
        return null;
    }

    @Override
    public boolean isOverriddenBySupported(ParserResult info, ElementHandle handle) {
        return true;
    }

    @Override
    public Collection<? extends AlternativeLocation> overriddenBy(ParserResult info, ElementHandle handle) {
        // handles only classes (and method in classes), ignoring modules for now at least
        if (handle.getKind() == ElementKind.CLASS) {
            ClassElement classElement = (ClassElement) handle;
            return asLocations(getSubclasses(classElement.getFqn(), info));
        }
        if (handle.getKind() == ElementKind.METHOD) {
            MethodElement methodElement = (MethodElement) handle;
            Set<IndexedClass> subclzs = getSubclasses(methodElement.getIn(), info);
            if (subclzs.isEmpty()) {
                return null;
            }
            RubyIndex index = RubyIndex.get(info);
            Set<IndexedMethod> overriding = new LinkedHashSet<IndexedMethod>();
            Set<String> subClassNames = new HashSet<String>(subclzs.size());
            for (IndexedClass subClz : subclzs) {
                subClassNames.add(subClz.getFqn());
            }
            overriding.addAll(index.getMethods(methodElement.getName(), subClassNames, Kind.EXACT));
            return asLocations(overriding);
        }

        return null;
    }

    private Set<IndexedClass> getSubclasses(String fqn, ParserResult info) {
        Cache cache = getCache(info);
        if (cache.subClasses.containsKey(fqn)) {
            return cache.subClasses.get(fqn);
        }
        RubyIndex index = RubyIndex.get(info);
        Set<IndexedClass> result = index.getSubClasses(fqn, null, null, false);
        cache.subClasses.put(fqn, result);
        return result;

    }

    private Set<IndexedMethod> getInheritedMethods(String fqn, ParserResult info) {
        Cache cache = getCache(info);
        if (cache.inherited.containsKey(fqn)) {
            return cache.inherited.get(fqn);
        }
        RubyIndex index = RubyIndex.get(info);
        Set<IndexedMethod> result = index.getInheritedMethods(fqn, "", Kind.PREFIX, false);
        cache.inherited.put(fqn, result);
        return result;
    }

    private Cache getCache(ParserResult info) {
        if (cacheHolder.containsKey(info)) {
            return cacheHolder.get(info);
        }
        cacheHolder.clear();
        Cache result = new Cache();
        cacheHolder.put(info, result);
        return result;
    }

    private static Collection<RubyDeclarationFinderHelper.RubyAltLocation> asLocations(
            Collection<? extends IndexedElement> elements) {

        if (elements.isEmpty()) {
            return Collections.emptySet();
        }
        Collection<RubyDeclarationFinderHelper.RubyAltLocation> result =
                new HashSet<RubyDeclarationFinderHelper.RubyAltLocation>(elements.size());
        for (IndexedElement elem : elements) {
            result.add(new RubyDeclarationFinderHelper.RubyAltLocation(elem, false));
        }
        return result;

    }
}
