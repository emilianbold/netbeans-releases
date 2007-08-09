/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
import org.jruby.ast.SClassNode;
import org.jruby.ast.SymbolNode;
import org.netbeans.api.gsf.Element;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.ElementKind;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.Modifier;


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
        if (node instanceof MethodDefNode) {
            return new AstMethodElement(node);
        } else if (node instanceof ClassNode || node instanceof SClassNode) {
            return new AstClassElement(node);
        } else if (node instanceof ModuleNode) {
            return new AstModuleElement(node);
        } else if (node instanceof ConstDeclNode || node instanceof ClassVarDeclNode) {
            return new AstFieldElement(node);
        } else if (node instanceof ConstNode) {
            return new AstVariableElement(node, ((ConstNode)node).getName());
        } else if (node instanceof ClassVarNode || node instanceof ClassVarDeclNode ||
                node instanceof InstVarNode || node instanceof InstAsgnNode) {
            return new AstFieldElement(node);
        } else if (node instanceof ConstDeclNode) {
            return new AstConstantElement((ConstDeclNode)node);
        } else if (node instanceof SymbolNode) {
            return new AstAttributeElement((SymbolNode)node, null);
        } else {
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
