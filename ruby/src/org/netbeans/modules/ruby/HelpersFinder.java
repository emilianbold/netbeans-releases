/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.Set;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.SymbolNode;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.ruby.elements.IndexedClass;

/**
 * Tries to locate class passed to methods that take a class name as an argument.
 * E.g. tries to find
 * SomeClassHelper for :some_class in the following:
 * <pre>
 * helper :some_class
 * </pre>
 *
 * <i>TODO: should rename as this isn't about helpers only anymore,
 * just can't come up with a good name now</i>.
 * 
 * @author Erno Mononen
 */

final class HelpersFinder {

    private static final MethodInfo[] APPLICABLE_METHODS = {
        new MethodInfo("helper", "Helper"),
        new MethodInfo("cache_sweeper")
    };

    private final RubyIndex index;
    private final SymbolNode closest;
    private final Node root;
    private final AstPath path;

    public HelpersFinder(RubyIndex index, SymbolNode closest, Node root, AstPath path) {
        this.index = index;
        this.closest = closest;
        this.root = root;
        this.path = path;
    }

    private MethodInfo getMethodInfo(Node node) {
        if (!AstUtilities.isCall(node)) {
            return null;
        }
        for (MethodInfo each : APPLICABLE_METHODS) {
            if (AstUtilities.isNodeNameIn(node, each.name)) {
                return each;
            }
        }
        return null;
    }

    private MethodInfo getMethodInfo() {
        MethodInfo result = null;
        // first argument
        for (Node child : closest.childNodes()) {
            result = getMethodInfo(child);
            if (result != null) {
                return result;
            }
        }
        // others
        result = getMethodInfo(path.leafParent());
        if (result == null) {
            result = getMethodInfo(path.leafGrandParent());
        }
        return result;
    }


    private String getClassName(MethodInfo methodInfo) {
        String className = AstUtilities.getName(closest);
        if (className.length() == 0) {
            return null;
        }
        return methodInfo.getClassName(RubyUtils.underlinedNameToCamel(className)); //NOI18N
    }

    DeclarationLocation findHelperLocation() {
        MethodInfo methodInfo = getMethodInfo();
        if (methodInfo == null) {
            return DeclarationLocation.NONE;
        }
        String className = getClassName(methodInfo);
        if (className == null) {
            return DeclarationLocation.NONE;
        }
        Set<IndexedClass> result = index.getClasses(className, Kind.EXACT, true, false, false);
        if (result.isEmpty()) {
            return DeclarationLocation.NONE;
        }
        return RubyDeclarationFinder.getLocation(result);
    }

    private static class MethodInfo {
        /**
         * the name of the method.
         */
        private final String name;
        /**
         * the suffix to add to the class name
         */
        private final String suffix;

        public MethodInfo(String name) {
            this(name, "");
        }

        public MethodInfo(String name, String suffix) {
            this.name = name;
            this.suffix = suffix;
        }

        public String getClassName(String className) {
            return className + suffix;
        }

    }
}
