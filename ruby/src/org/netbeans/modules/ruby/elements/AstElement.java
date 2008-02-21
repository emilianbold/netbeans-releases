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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.ruby.elements;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.List;
import java.util.List;
import java.util.Set;
import java.util.Set;

import org.jruby.ast.ArgumentNode;
import org.jruby.ast.ClassNode;
import org.jruby.ast.ClassVarDeclNode;
import org.jruby.ast.ClassVarNode;
import org.jruby.ast.ConstDeclNode;
import org.jruby.ast.ConstNode;
import org.jruby.ast.InstAsgnNode;
import org.jruby.ast.InstVarNode;
import org.jruby.ast.MethodDefNode;
import org.jruby.ast.ModuleNode;
import org.jruby.ast.Node;
import org.jruby.ast.Node;
import org.jruby.ast.NodeTypes;
import org.jruby.ast.SClassNode;
import org.jruby.ast.SymbolNode;
import org.netbeans.modules.ruby.elements.Element;
import org.netbeans.fpi.gsf.ElementKind;
import org.netbeans.fpi.gsf.ElementKind;
import org.netbeans.fpi.gsf.Modifier;
import org.netbeans.fpi.gsf.Modifier;


/**
 * A Ruby element coming from a JRuby parse tree
 *
 * @author Tor Norbye
 */
public abstract class AstElement extends RubyElement {
    protected Node node;
    protected ArrayList<AstElement> children;
    protected String name;
    private String in;
    protected Set<Modifier> modifiers;

    public AstElement(Node node) {
        super();
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public abstract String getName();

    //    public String getName() {
    //        if (name == null) {
    //            name = node.toString();
    //        }
    //
    //        return name;
    //    }
    public String getDisplayName() {
        return getName();
    }

    public String getDescription() {
        // XXX TODO
        return getName();
    }

    @SuppressWarnings("unchecked")
    public List<AstElement> getChildren() {
        //        if (children == null) {
        //            children = new ArrayList<AstElement>();
        //
        //            for (Node child : (List<Node>)node.childNodes()) {
        //                addInterestingChildren(this, children, child);
        //            }
        //        }
        //
        if (children == null) {
            return Collections.emptyList();
        }

        return children;
    }

    public void addChild(AstElement child) {
        if (children == null) {
            children = new ArrayList<AstElement>();
        }

        children.add(child);
    }

    public static AstElement create(Node node) {
        switch (node.nodeId) {
        case NodeTypes.DEFNNODE:
        case NodeTypes.DEFSNODE:
            return new AstMethodElement(node);
        case NodeTypes.CLASSNODE:
        case NodeTypes.SCLASSNODE:
            return new AstClassElement(node);
        case NodeTypes.MODULENODE:
            return new AstModuleElement(node);
        case NodeTypes.CONSTNODE:
            return new AstVariableElement(node, ((ConstNode)node).getName());
        case NodeTypes.CLASSVARNODE:
        case NodeTypes.CLASSVARDECLNODE:
        case NodeTypes.INSTASGNNODE:
        case NodeTypes.INSTVARNODE:
            return new AstFieldElement(node);
        case NodeTypes.CONSTDECLNODE:
            return new AstConstantElement((ConstDeclNode)node);
        case NodeTypes.SYMBOLNODE:
            return new AstAttributeElement((SymbolNode)node, null);
        default:
            return null;
        }
    }

    public String toString() {
        String clz = getClass().getName();

        return clz.substring(0, clz.lastIndexOf('.')) + ":" + node.toString();
    }

    public Image getIcon() {
        return null;
    }

    public String getIn() {
        // TODO - compute signature via AstUtilities
        return in;
    }

    public void setIn(String in) {
        this.in = in;
    }

    public ElementKind getKind() {
        return ElementKind.OTHER;
    }

    public Set<Modifier> getModifiers() {
        return Collections.emptySet();
    }
}
