/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.ruby;

import java.util.Set;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.SymbolNode;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.ruby.elements.IndexedClass;

/**
 * Tries to locate helper classes declared using the helper method, e.g. tries to find
 * SomeClassHelper for :some_class in the following:
 * <pre>
 * helper :some_class
 * </pre>
 *
 * @author Erno Mononen
 */
final class HelpersFinder {

    private static final String[] HELPER_METHODS = {"helper"};
    private final ParserResult info;
    private final SymbolNode closest;
    private final Node root;
    private final AstPath path;

    public HelpersFinder(ParserResult info, SymbolNode closest, Node root, AstPath path) {
        this.info = info;
        this.closest = closest;
        this.root = root;
        this.path = path;
    }

    private boolean isHelper(Node node) {
        if (!AstUtilities.isCall(node)) {
            return false;
        }
        return AstUtilities.isNodeNameIn(node, HELPER_METHODS);
    }

    private boolean isHelper() {
        // first argument
        for (Node child : closest.childNodes()) {
            if (isHelper(child)) {
                return true;
            }
        }
        // others
        return isHelper(path.leafParent()) || isHelper(path.leafGrandParent());
    }


    private String getClassName() {
        String className = AstUtilities.getName(closest);
        if (className.length() == 0) {
            return null;
        }
        return RubyUtils.underlinedNameToCamel(className) + "Helper"; //NOI18N
    }

    DeclarationLocation findHelperLocation() {
        if (!isHelper()) {
            return DeclarationLocation.NONE;
        }
        String className = getClassName();
        if (className == null) {
            return DeclarationLocation.NONE;
        }
        Set<IndexedClass> result = RubyIndex.get(info).getClasses(className, Kind.EXACT, true, false, false);
        if (result.isEmpty()) {
            return DeclarationLocation.NONE;
        }
        return RubyDeclarationFinder.getLocation(result);
    }
}
