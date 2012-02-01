/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.inspect.ui;

import java.util.*;
import javax.swing.Action;
import org.netbeans.modules.web.inspect.PageModel.ResourceInfo;
import org.netbeans.modules.web.inspect.PageModel.ResourceInfo.Type;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * A node representing resources of a web page.
 *
 * @author Jan Stola
 */
public class ResourcesNode extends AbstractNode {
    /** Icon base of the node. */
    static final String ICON_BASE = "org/netbeans/modules/web/inspect/resources/resources.png"; // NOI18N
    /** Actions of the node. */
    private Action[] actions;

    /**
     * Creates a new {@code ResourcesNode}.
     * 
     * @param actions actions of the node.
     */
    ResourcesNode(Action[] actions) {
        super(new ResourcesChildren());
        setDisplayName(NbBundle.getMessage(ResourcesNode.class, "ResourcesNode.name")); // NOI18N
        setIconBaseWithExtension(ICON_BASE);
        this.actions = actions;
    }

    /**
     * Sets resources that this node should represent.
     * 
     * @param resources resources that this node should represent.
     */
    void setResources(Collection<ResourceInfo> resources) {
        ((ResourcesChildren)getChildren()).setResources(resources);
    }

    @Override
    public Action[] getActions(boolean context) {
        return actions;
    }

    /**
     * Children used by {@code ResourcesNode}.
     */
    static class ResourcesChildren extends Children.Keys<ResourceInfo.Type> {
        /** Keys for the children. */
        private static Type[] KEYS = new Type[] {Type.HTML, Type.STYLESHEET, Type.SCRIPT, Type.IMAGE};
        /** Resources represented by this {@code ResourcesChildren} object. */
        private Collection<ResourceInfo> resources;
        /** Map from keys to childrens. */
        private java.util.Map<Type,ResourceGroupNode> childrenMap = new EnumMap<Type,ResourceGroupNode>(Type.class);

        /**
         * Set resources that this {@code ResourcesChildren} object should represent.
         * 
         * @param resources resources that this {@code ResourcesChildren} object should represent.
         */
        void setResources(Collection<ResourceInfo> resources) {
            this.resources = resources;
            for (Type key : KEYS) {
                ResourceGroupNode node = childrenMap.get(key);
                if (node != null) {
                    node.setResources(filter(key));
                }
            }
            setKeys(KEYS);
        }

        @Override
        protected Node[] createNodes(Type key) {
            ResourceGroupNode node = new ResourceGroupNode(key);
            node.setResources(filter(key));
            childrenMap.put(key, node);
            return new Node[] {node};
        }

        /**
         * Returns resources with the specified type.
         * 
         * @param type type of the resource.
         * @return resources with the specified type.
         */
        private Collection<ResourceInfo> filter(Type type) {
            List<ResourceInfo> result = new LinkedList<ResourceInfo>();
            for (ResourceInfo resource : resources) {
                if (resource.getType() == type) {
                    result.add(resource);
                }
            }
            return result;
        }

    }

}
