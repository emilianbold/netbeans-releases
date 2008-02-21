/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.editing;

import java.util.Collections;
import java.util.Set;
import java.util.List;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.Token;
import org.mozilla.javascript.FunctionNode;
import org.netbeans.fpi.gsf.Modifier;
import org.netbeans.fpi.gsf.ElementKind;

/**
 *
 * @author Tor Norbye
 */
public class AstElement implements Element {

    protected List<AstElement> children;
    protected Node node;
    protected String name;

    AstElement(Node node) {
        this.node = node;
    }
    
    public Node getNode() {
        return node;
    }

    public String getName() {
        if (name == null) {
            if (node.getType() == Token.VAR) {
                // Must pull the name out of the child
                if (node.hasChildren()) {
                    Node child = node.getFirstChild();
                    if (child.getType() == Token.NAME) {
                        name = child.getString();
                    }
                }
            } else if (node.isStringNode()) {
                name = node.getString();
            }
        }

        return name;
    }

    public String getIn() {
        return "";
    }

    public ElementKind getKind() {
        switch (node.getType()) {
        case Token.NAME:
        case Token.BINDNAME:
        case Token.PARAMETER:
            return ElementKind.VARIABLE;
        default:
            return ElementKind.OTHER;
        }
    }

    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }

    public List<AstElement> getChildren() {
//        if (children == null) {
//            // Functions need special treatment: Rhino has a weird thing in the AST
//            // where the AST for each function is not made part of the overall tree...
//            // So in these cases I've gotta go looking for it myself...
//            if (node.getType() == Token.FUNCTION) {
//                String name;
//                if (node instanceof FunctionNode) {
//                    name = ((FunctionNode) node).getFunctionName();
//                } else {
//                    name = node.getString();
//                }
////                children = new ArrayList<AstElement>();
////                if (node.getParentNode() instanceof ScriptOrFnNode) {
////                    ScriptOrFnNode sn = (ScriptOrFnNode) node.getParentNode();
////                    for (int i = 0,  n = sn.getFunctionCount(); i < n; i++) {
////                        FunctionNode func = sn.getFunctionNode(i);
////                        if (name.equals(func.getFunctionName())) {
////                            // Found the function
////
////                            Node current = func.getFirstChild();
////
////                            for (; current != null; current = current.getNext()) {
////                                AstElement child = getElement(current);
////                                children.add(child);
////                            }
////                            return children;
////                        }
////                    }
////                }
////                System.err.println("SURPRISE! It's not a script node... revisit code--- some kind of error");
//////                children = new ArrayList<ComObject>(0); // TODO - cache
//////                return children.iterator();
//            }
//            if (node.hasChildren()) {
//                children = new ArrayList<AstElement>();
//
//                Node current = node.getFirstChild();
//
//                for (; current != null; current = current.getNext()) {
//                    // Already added above?
//                    //if (current.getType() == Token.FUNCTION) {
//                    //    continue;
//                    //}
//                    //JavaScriptNode child = JavaScriptNodeFactory.getComObject(current);
//                    AstElement child = getElement(current);
//                    children.add(child);
//                }
////            } else {
////                //children = Collections.emptySet();
////                children = EMPTY_SET;
//            }
//
//            if (children == null) {
//                children = Collections.emptyList();
//            }
//        }
//
//        return children;
        return Collections.emptyList();
    }
    
    @Override
    public String toString() {
        return "JsElement:" + getName() + "(" + getKind() + ")"; // NOI18N
    }
    
    public static AstElement getElement(Node node) {
        switch (node.getType()) {
            case Token.FUNCTION:
                if (node instanceof FunctionNode) {
                    return new FunctionAstElement((FunctionNode) node);
                } else {
                // Fall through
                }
            default:
                return new AstElement(node);
        }
    }
}
