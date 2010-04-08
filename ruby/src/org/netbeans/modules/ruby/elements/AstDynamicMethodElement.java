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

package org.netbeans.modules.ruby.elements;

import java.util.Collections;
import java.util.List;
import org.jrubyparser.ast.AliasNode;
import org.jrubyparser.ast.Node;
import org.jrubyparser.ast.NodeType;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.ruby.AstUtilities;

/**
 * An element for dynamic methods, such as named_scope.
 *
 * @author Erno Mononen
 */
public final class AstDynamicMethodElement extends AstMethodElement {

    private final Node methodNode;
    private List<String> parameters;

    public AstDynamicMethodElement(ParserResult info, Node node) {
        this(info, node, null);
    }

    public AstDynamicMethodElement(ParserResult info, Node node, String name) {
        super(info, node);
        this.name = name;
        this.methodNode = node;
    }

    @Override
    public String getName() {
        if (name != null) {
            return name;
        }
        String result = null;
        if (methodNode.getNodeType() == NodeType.ALIASNODE) {
            result = AstUtilities.getNameOrValue(((AliasNode) methodNode).getNewName());
        }
        if (result == null) {
            result = AstUtilities.getNameOrValue(methodNode);
        }
        return result == null ? "" : result;
    }

    @Override
    public List<String> getParameters() {
        if (parameters == null) {
            return Collections.emptyList();
        }
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    @Override
    public ElementKind getKind() {
        return ElementKind.METHOD;
    }

}
