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

import java.util.Set;
import org.jruby.nb.ast.Colon2Node;
import org.jruby.nb.ast.Node;
import org.jruby.nb.ast.types.INameNode;
import org.netbeans.modules.gsf.api.CompilationInfo;
import org.netbeans.modules.gsf.api.DeclarationFinder.DeclarationLocation;
import org.netbeans.modules.ruby.elements.IndexedConstant;

final class RubyConstantDeclarationFinder extends RubyBaseDeclarationFinder<IndexedConstant> {

    private final Node constantNode;

    RubyConstantDeclarationFinder(
            final CompilationInfo info,
            final Node root,
            final AstPath path,
            final RubyIndex index,
            final Node constantNode) {
        super(info, root, path, index);
        this.constantNode = constantNode;
    }

    DeclarationLocation findConstantDeclaration() {
        Set<? extends IndexedConstant> constants;
        if (constantNode instanceof Colon2Node) {
            String constantFqn = AstUtilities.getFqn((Colon2Node) constantNode);
            constants = index.getConstants(constantFqn);
        } else {
            // inside of class or module?
            String constantName = ((INameNode) constantNode).getName();
            String className = AstUtilities.getFqnName(path);
            constants = index.getConstants(className, constantName);
        }

        return getElementDeclaration(constants, constantNode);
    }

    @Override
    IndexedConstant findBestMatchHelper(final Set<? extends IndexedConstant> constants) {
        // trivial implementation - seems to be enough for now
        return constants.isEmpty() ? null : constants.iterator().next();
    }

}
