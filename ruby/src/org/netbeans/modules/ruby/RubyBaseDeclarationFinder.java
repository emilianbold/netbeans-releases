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
package org.netbeans.modules.ruby;

import java.util.HashSet;
import java.util.Set;
import org.jrubyparser.ast.Node;
import org.netbeans.modules.csl.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.elements.IndexedElement;

abstract class RubyBaseDeclarationFinder<T extends IndexedElement> extends RubyDeclarationFinderHelper {

    protected final ParserResult info;
    protected final Node root;
    protected final AstPath path;
    protected final RubyIndex index;

    protected RubyBaseDeclarationFinder(
            final ParserResult info,
            final Node root,
            final AstPath path,
            final RubyIndex index) {
        this.info = info;
        this.root = root;
        this.path = path;
        this.index = index;
    }

    abstract T findBestMatchHelper(Set<? extends T> elements);
    
    protected final T findBestElementMatch(Set<? extends T> origElements) {
        // Make sure that the best fit method actually has a corresponding valid
        // source location and parse tree
        Set<T> elements = new HashSet<T>(origElements);

        while (!elements.isEmpty()) {
            T ele = findBestMatchHelper(elements);
            Node node = AstUtilities.getForeignNode(ele);

            if (node != null) {
                return ele;
            }

            // TODO: Sort results, then pick candidate number modulo methodSelector
            if (!elements.contains(ele)) {
                // Avoid infinite loop when we somehow don't find the node for
                // the best class and we keep trying it
                elements.remove(elements.iterator().next());
            } else {
                elements.remove(ele);
            }
        }

        return null;
    }


    protected final DeclarationLocation getElementDeclaration(
            final Set<? extends T> elements,
            final Node toFind) {

        final T candidate = findBestElementMatch(elements);

        if (candidate != null) {
            Node node = AstUtilities.getForeignNode(candidate);

            DeclarationLocation loc = new DeclarationLocation(candidate.getFileObject(),
                    node.getPosition().getStartOffset(), candidate);

            if (!CHOOSE_ONE_DECLARATION && elements.size() > 1) {
                // Could the :nodoc: alternatives: if there is only one nodoc'ed alternative
                // don't ask user!
                int not_nodoced = 0;
                for (final T e : elements) {
                    if (!e.isNoDoc()) {
                        not_nodoced++;
                    }
                }
                if (not_nodoced >= 2) {
                    for (final T e : elements) {
                        loc.addAlternative(new RubyAltLocation(e, e == candidate));
                    }
                }
            }

            return loc;
        }

        return DeclarationLocation.NONE;
    }

}
