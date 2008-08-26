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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.java.debug;

import com.sun.source.tree.Tree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementScanner6;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

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
        final int[] result = new int[] {-1};

        try {
            JavaSource.create(info.getClasspathInfo()).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    Tree tree = info.getTrees().getTree(element);
                    if (tree != null) {
                        result[0] = (int) info.getTrees().getSourcePositions().getStartPosition(info.getCompilationUnit(), tree);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result[0];
    }
    
    public int getEnd() {
        final int[] result = new int[] {-1};

        try {
            JavaSource.create(info.getClasspathInfo()).runUserActionTask(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                    Tree tree = info.getTrees().getTree(element);
                    if (tree != null) {
                        result[0] = (int) info.getTrees().getSourcePositions().getEndPosition(info.getCompilationUnit(), tree);
                    }
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return result[0];
    }

    public int getPreferredPosition() {
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
