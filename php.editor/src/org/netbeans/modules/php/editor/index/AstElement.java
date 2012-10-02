/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.editor.index;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.spi.ParserResult;
/**
 *
 * @author Tor Norbye
 */
public class AstElement extends PHPElement {
    public static final Set<Modifier> STATIC = EnumSet.of(Modifier.STATIC);

    protected String name;
    protected String in;
    protected ParserResult info;
    protected String signature;
    protected ElementKind kind;

    @SuppressWarnings("unchecked")
    protected Set<Modifier> modifiers = Collections.EMPTY_SET;

    AstElement(ParserResult info) {//, Node node) {
        this.info = info;
        //this.node = node;
    }

    public String getSignature() {
        if (signature == null) {
            StringBuilder sb = new StringBuilder();
            String clz = getIn();
            if (clz != null && clz.length() > 0) {
                sb.append(clz);
                sb.append("."); // NOI18N
            }
            sb.append(getName());
            signature = sb.toString();
        }

        return signature;
    }

//    public Node getNode() {
//        return node;
//    }

    public void setName(String name, String in) {
        // Prototype.js hack
        if ("Element.Methods".equals(in)) { // NOI18N
            in = "Element"; // NOI18N
        }

        this.name = name;
        this.in = in;
    }

    public String getName() {
//        if (name == null) {
//            if (node.getType() == Token.VAR) {
//                // Must pull the name out of the child
//                if (node.hasChildren()) {
//                    Node child = node.getFirstChild();
//                    if (child.getType() == Token.NAME) {
//                        name = child.getString();
//                    }
//                }
//            } else if (node.isStringNode()) {
//                name = node.getString();
//            }
//        }

        return name;
    }

    @Override
    public String getIn() {
        if (in == null) {
            in = ""; // NOI18N
        }
        return in;
    }

    void setKind(ElementKind kind) {
        this.kind = kind;
    }

    public ElementKind getKind() {
        if (kind == null) {
//            switch (node.getType()) {
//            case Token.NAME:
//            case Token.BINDNAME:
//            case Token.PARAMETER:
//                return ElementKind.VARIABLE;
//            default:
//                return ElementKind.OTHER;
//            }
        }

        return kind;
    }

    public List<AstElement> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "JsElement:" + getName() + "(" + getKind() + ")"; // NOI18N
    }

    public ParserResult getInfo() {
        return info;
    }

    @Override
    public Set<Modifier> getModifiers() {
        return modifiers;
    }

    void setModifiers(Set<Modifier> modifiers) {
        this.modifiers = modifiers;
    }

//    public static AstElement getElement(CompilationInfo info, Node node) {
//        switch (node.getType()) {
//            case Token.FUNCTION:
//                if (node instanceof FunctionNode) {
//                    return new FunctionAstElement(info, (FunctionNode) node);
//                } else {
//                    // Fall through
//                }
//            default:
//                return new AstElement(info, node);
//        }
//    }
}
