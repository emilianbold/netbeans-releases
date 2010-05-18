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

package org.netbeans.modules.xml.xam.ui;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.ui.cookies.GetComponentCookie;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.datatransfer.PasteType;

/**
 * Paste type for XAM-based components. Expects Nodes in the Transferable
 * and determines what is allowed to be pasted where, delegating most
 * of the work to the model.
 *
 * @author Nathan Fiedler
 */
public class ComponentPasteType {
    /** NodeTransfer operations we use by default. */
    private static final int[] STANDARD_OPERATIONS = new int[] {
        NodeTransfer.COPY,
        NodeTransfer.MOVE
    };

    /**
     * Create PasteType to receive the transferable into the given component.
     *
     * @param  target    the target component.
     * @param  transfer  the component(s) being pasted.
     * @param  type      type of the component to allow (e.g. Element.class),
     *                   or null to allow all types.
     * @return  the new paste type.
     */
    public static PasteType getPasteType(Component target,
            Transferable transfer, Class<? extends Component> type) {
        return getPasteType(target, transfer, type, STANDARD_OPERATIONS, -1);
    }

    /**
     * Create PasteType to receive the transferable into the given component.
     *
     * @param  target    the target component.
     * @param  transfer  the component(s) being pasted.
     * @param  type      type of the component to allow (e.g. Element.class),
     *                   or null to allow all types.
     * @param  action    the NodeTransfer constant for cut/copy.
     * @param  index     index at which to paste the component (-1 to append).
     * @return  the new paste type.
     */
    public static PasteType getDropType(Component target,
            Transferable transfer, Class<? extends Component> type,
            int action, int index) {
// The action value passed to AbstractNode.getDropType() is wrong so we
// must ignore it and use our standard values.
//        int[] operations = new int[] { action };
        return getPasteType(target, transfer, type, STANDARD_OPERATIONS, index);
    }

    /**
     * Create PasteType to receive the transferable into the given component.
     *
     * @param  target      the target component.
     * @param  transfer    the component(s) being pasted.
     * @param  type        type of the component to allow (e.g. Element.class),
     *                     or null to allow all types.
     * @param  operations  set of NodeTransfer constants for cut/copy.
     * @param  index       index at which to paste the component (-1 to append).
     * @return  the new paste type.
     */
    private static PasteType getPasteType(Component target,
            Transferable transfer, Class<? extends Component> type,
            int[] operations, int index) {
        PasteType retVal = null;
        // Check each operation until a supported one is found.
        for (int oper : operations) {
            // Attempt to retrieve the nodes from transferable.
            Node[] nodes = NodeTransfer.nodes(transfer, oper);
            if (nodes != null) {
                // Can any of these be pasted into the target?
                if (canPaste(nodes, target, oper, type)) {
                    retVal = new PasteTypeImpl(Arrays.asList(nodes), target,
                            oper, index);
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Determine if all of the given nodes can be pasted into the component.
     *
     * @param  nodes      the nodes being pasted.
     * @param  target     the target component.
     * @param  operation  the NodeTransfer constant for cut/copy.
     * @param  type       type of the component to allow (e.g. Element.class),
     *                    or null to allow all types.
     * @return  true if the nodes can be pasted.
     */
    private static boolean canPaste(Node[] nodes, Component target,
            int operation, Class<? extends Component> type) {
        Set<Node> pasteableNodes = new HashSet<Node>();
        for (Node pasteableNode : nodes) {
            // The node must provide a Component, otherwise we cannot use it.
            GetComponentCookie gcc = (GetComponentCookie) pasteableNode.
                    getLookup().lookup(GetComponentCookie.class);
            if (gcc != null) {
                Component pasteableComponent = gcc.getComponent();
                // Check that the target can receive this component.
                // Ensure that the model is still valid, in case the
                // component was deleted or moved elsewhere.
                if ((type == null ||
                        type.isAssignableFrom(gcc.getComponentType())) &&
                        pasteableComponent.getModel() != null &&
                        target.canPaste(pasteableComponent)) {
                    boolean isCopyPaste = (operation & NodeTransfer.COPY) != 0;
                    // Prevent cutting and pasting into the same component.
                    boolean isCutPaste = (operation & NodeTransfer.MOVE) != 0 &&
                            !(pasteableComponent.getParent().equals(target)) &&
                            pasteableNode.canDestroy();
                    if (isCopyPaste || isCutPaste) {
                        if (isCutPaste) {
                            // Prevent cutting/pasting into a child component.
                            Component parent = target;
                            while (parent != null) {
                                if (parent.equals(pasteableComponent)) {
                                    return false;
                                }
                                parent = parent.getParent();
                            }
                        }
                        // We could check for duplicates here, but at this
                        // time we are allowing them.
                        pasteableNodes.add(pasteableNode);
                    }
                }
            }
        }
        return pasteableNodes.size() == nodes.length;
    }

    /**
     * Our PasteType implementation for component nodes.
     */
    private static class PasteTypeImpl extends PasteType {
        /** The component to receive the paste. */
        private Component target;
        /** The nodes being pasted. */
        private List<Node> nodes;
        /** NodeTransfer constant (e.g. COPY, MOVE). */
        private int operation;
        /** Position at which to insert item (-1 to append). */
        private int index;

        /**
         * Creates a new instance of PasteTypeImpl.
         *
         * @param  nodes      those which are to be pasted.
         * @param  target     the paste recipient.
         * @param  operation  NodeTransfer constant indicating cut/copy.
         * @param  index      position to paste, or -1 to append.
         */
        private PasteTypeImpl(List<Node> nodes,
                Component target, int operation, int index) {
            this.target = target;
            this.nodes = nodes;
            this.operation = operation;
            if (index < 0) {
                // Instead of appending at the end, let's prepend at the
                // beginning, to avoid the subsequent re-ordering that the
                // NetBeans code performs if we were to append.
                this.index = 0;
            } else {
                this.index = index;
            }
        }

        @SuppressWarnings("unchecked")
        public Transferable paste() throws IOException {
            // Perform the cut or copy to the target component.
            if (target != null && nodes.size() > 0) {
                Model model = target.getModel();
                GetComponentCookie gcc = (GetComponentCookie) nodes.get(0).
                        getCookie(GetComponentCookie.class);
                if (gcc == null) {
                    // We can go nowhere without a valid node.
                    return null;
                }
                Model srcModel = gcc.getComponent().getModel();
                // Keep everything in a single transaction so the undo/redo
                // acts on the entire set rather than individual nodes.
                // This makes the assumption that the source nodes are all
                // coming from a single model, which should always be true.
                model.startTransaction();
                try {
                    for (Node node : nodes) {
                        gcc = (GetComponentCookie) node.getCookie(
                                GetComponentCookie.class);
                        Component child = gcc.getComponent();
                        // Always make a clone of the component, even for the cut
                        // operation, since it converts global to local and vice
                        // versa, and we want to be consistent with the copy
                        // operation in that respect.
                        Component copy = child.copy(target);
                        // 'copy' will be null if the copy was unsuccessful.
                        // Better to fail silently than throw assertion errors,
                        // so make sure the child is non-null before proceeding.
                        if (copy != null) {
                            if ((operation & NodeTransfer.MOVE) != 0) {
                                // For cut, remove the component from its model.
                                // This should allow it to be collected.
                                boolean srcInTransaction = srcModel.isIntransaction();
                                try {
                                    if (!srcInTransaction) {
                                        // Must be separate models, in which
                                        // case create a new transaction.
                                        srcModel.startTransaction();
                                    }
                                    srcModel.removeChildComponent(child);
                                } finally {
                                    if (!srcInTransaction) {
                                        srcModel.endTransaction();
                                    }
                                }
                            }
                            // Ensure the name of the copy is unique within
                            // the target component, if it is nameable.
                            if (copy instanceof Nameable) {
                                String name = ((Nameable) copy).getName();
                                String preferredName = name;
                                HashSet<String> nameSet = new HashSet<String>();
                                for (Object sibling : target.getChildren()) {
                                    if (sibling instanceof Named) {
                                        nameSet.add(((Named) sibling).getName());
                                    }
                                }
                                int unique = 1;
                                while (nameSet.contains(name)) {
                                    name = preferredName + unique;
                                    unique++;
                                }
                                ((Nameable) copy).setName(name);
                            }
                            // Add child to target model under component.
                            model.addChildComponent(target, copy, index);
                        }
                    }
                } finally {
                    model.endTransaction();
                }
            }
            return null;
        }

        public String toString() {
            return "PasteTypeImpl=[operation=" + operation + ",index=" + index + "]";
        }
    }
}
