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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.java.debug;

import com.sun.source.tree.Tree;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Lahoda
 */
public class ElementNode extends AbstractNode implements OffsetProvider {
    
    private Element element;
    private CompilationInfo info;
    
//    public static Node getTree(CompilationInfo info) {
//        return getTree(info, info.getElement(info.getTree().getTypeDecls().get(0)));
//    }
    
    public static Node getTree(CompilationInfo info, Element element) {
        List<Node> result = new ArrayList<Node>();
        
        new FindChildrenElementVisitor(info).scan(element, result);
        
        return result.get(0);
    }

    /** Creates a new instance of TreeNode */
    public ElementNode(CompilationInfo info, Element element, List<Node> nodes) {
        super(nodes.isEmpty() ? Children.LEAF: new NodeChilren(nodes));
        this.element = element;
        this.info = info;
        setDisplayName(element.getKind().toString() + ":" + element.toString()); //NOI18N
        setIconBaseWithExtension("org/netbeans/modules/java/debug/resources/element.png"); //NOI18N
    }

    public int getStart() {
        Tree tree = info.getTrees().getTree(element);
        
        if (tree != null)
            return (int)info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
        else
            return -1;
    }
    
    public int getEnd() {
        Tree tree = info.getTrees().getTree(element);
        
        if (tree != null)
            return (int)info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree);
        else
            return -1;
    }

    private static final class NodeChilren extends Children.Keys<Node> {
        
        public NodeChilren(List<Node> nodes) {
            setKeys(nodes);
        }
        
        protected Node[] createNodes(Node key) {
            return new Node[] {key};
        }
        
    }
    
    private static class FindChildrenElementVisitor extends ElementScanner6<Void, List<Node>> {
        
        private CompilationInfo info;
        
        public FindChildrenElementVisitor(CompilationInfo info) {
            this.info = info;
        }
        
        public Void visitPackage(PackageElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitPackage(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitType(TypeElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitType(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitVariable(VariableElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitVariable(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitExecutable(ExecutableElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitExecutable(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
        public Void visitTypeParameter(TypeParameterElement e, List<Node> p) {
            List<Node> below = new ArrayList<Node>();

            super.visitTypeParameter(e, below);

            p.add(new ElementNode(info, e, below));
            return null;
        }
        
    }
}
