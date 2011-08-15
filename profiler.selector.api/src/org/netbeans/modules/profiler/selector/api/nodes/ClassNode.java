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
 * Contributor(s):
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

package org.netbeans.modules.profiler.selector.api.nodes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.api.icons.LanguageIcons;
import org.netbeans.modules.profiler.api.java.SourceClassInfo;
import org.netbeans.modules.profiler.api.java.SourceMethodInfo;


/**
 *
 * @author Jaroslav Bachorik
 */
public class ClassNode extends ContainerNode {
    private SourceClassInfo cInfo;
    
    /**
     * A {@linkplain Comparator} able to compare {@linkplain ClassNode} instances
     */
    public static final Comparator COMPARATOR = new Comparator<ClassNode>() {
        @Override
        public int compare(ClassNode o1, ClassNode o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    private static class ClassChildren extends SelectorChildren<ClassNode> {
        @Override
        protected List<SelectorNode> prepareChildren(final ClassNode parent) {
            List<SelectorNode> contents = new ArrayList<SelectorNode>();
            SelectorNode content = null;

            if (!parent.isAnonymous()) { // no constructors for anonymous inner classes
                content = parent.getConstructorsNode();

                if (content != null && !content.isLeaf()) {
                    contents.add(content);
                }
            }

            content = parent.getMethodsNode();

            if (content != null && !content.isLeaf()) {
                contents.add(content);
            }

            content = parent.getInnerClassesNode();

            if (content != null && !content.isLeaf()) {
                contents.add(content);
            }

            return contents;
        }
    }


    /** Creates a new instance of ClassNode */
    public ClassNode(SourceClassInfo cInfo, String displayName, Icon icon, final ContainerNode parent) {
        super(cInfo.getQualifiedName(), displayName, icon, parent);
        this.cInfo = cInfo;
        
        if (isAnonymous()) {
            String implementing = null;
            Set<SourceClassInfo> ifcs = cInfo.getInterfaces();
            if (ifcs.size() == 1) {
                implementing = ifcs.iterator().next().getQualifiedName();
            } else {
                SourceClassInfo superType = cInfo.getSuperType();
                if (!superType.getQualifiedName().equals(Object.class.getName())) {
                    implementing = superType.getQualifiedName();
                }
            }
            if (implementing != null) {
                updateDisplayName(displayName + " [" + implementing + "]");
            }
        }
    }

    public ClassNode(SourceClassInfo cInfo, final ContainerNode parent) {
        this(cInfo, cInfo.getSimpleName(), Icons.getIcon(LanguageIcons.CLASS), parent);
    }

    final protected SelectorChildren getChildren() {
        return new ClassChildren();
    }

    /**
     * Is class an anonymous one
     * @return Returns true if the {@linkplain ClassNode} represents an anonymous class; false otherwise
     *
     */
    final public boolean isAnonymous() {
        return cInfo.isAnonymous();
    }

    /**
     * The implementation will take care of generating the appropriate {@linkplain ConstructorsNode} instance
     * @return Returns a specific {@linkplain ConstructorsNode} instance or NULL
     */
    protected ConstructorsNode getConstructorsNode() {
        return new ConstructorsNode(this) {

            @Override
            protected List<SelectorNode> getConstructorNodes(final ConstructorsNode parent) {
                final List<SelectorNode> constructorNodes = new ArrayList<SelectorNode>();
                for(SourceMethodInfo mi : cInfo.getConstructors()) {
                    ConstructorNode cn = new ConstructorNode(mi, parent);
                    constructorNodes.add(cn);
                }
                return constructorNodes;
            }
        };
    }

    /**
     * The implementation will take care of generating the appropriate {@linkplain MethodsNode} instance
     * @return Returns a specific {@linkplain MethodsNode} instance or NULL
     */
    protected MethodsNode getMethodsNode() {
        return new MethodsNode(this) {

            @Override
            protected List<MethodNode> getMethodNodes(final MethodsNode parent) {
                final List<MethodNode> methodNodes = new ArrayList<MethodNode>();
                for(SourceMethodInfo smi : cInfo.getMethods(false)) {
                    methodNodes.add(new MethodNode(smi, parent));
                }

                return methodNodes;
            }
        };
    }

    /**
     * 
     * @return Returns a specific {@linkplain InnerClassesNode} instance or NULL
     */
    private InnerClassesNode getInnerClassesNode() {
        return new InnerClassesNode(this) {
            @Override
            protected Set<ClassNode> getInnerClassNodes(final InnerClassesNode parent) {
                final Set<ClassNode> innerClassNodes = new HashSet<ClassNode>();

                for(SourceClassInfo inner : cInfo.getInnerClases()) {
                    innerClassNodes.add(new ClassNode(inner, parent));
                }

                return innerClassNodes;
            }
        };
    }
}
