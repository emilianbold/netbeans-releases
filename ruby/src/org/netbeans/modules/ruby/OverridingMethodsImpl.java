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
import java.util.HashSet;
import org.netbeans.modules.csl.api.DeclarationFinder.AlternativeLocation;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OverridingMethods;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.elements.ClassElement;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.MethodElement;

/**
 * Ruby impl of {@code OverridingMethods}.
 *
 * @author Erno Mononen
 */
final class OverridingMethodsImpl implements OverridingMethods {

    public OverridingMethodsImpl() {
    }

    @Override
    public Collection<? extends AlternativeLocation> overrides(ParserResult info, ElementHandle handle) {
        if (handle.getKind() == ElementKind.METHOD) {
            MethodElement methodElement = (MethodElement) handle;
            RubyIndex index = RubyIndex.get(info);
            IndexedMethod superMethod = index.getSuperMethod(methodElement.getIn(), methodElement.getName(), true, false);
            if (superMethod != null) {
                return asLocations(Collections.singleton(superMethod));
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
        if (handle.getKind() == ElementKind.CLASS) {
            ClassElement classElement = (ClassElement) handle;
            RubyIndex index = RubyIndex.get(info);
            return asLocations(index.getSubClasses(classElement.getFqn(), null, null, false));
        }
        if (handle.getKind() == ElementKind.METHOD) {
            MethodElement methodElement = (MethodElement) handle;
            RubyIndex index = RubyIndex.get(info);
            return asLocations(index.getOverridingMethods(methodElement.getName(), methodElement.getIn(), true));
        }

        return null;
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
