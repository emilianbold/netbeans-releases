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

package org.netbeans.modules.bpel.properties;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.model.api.Assign;
import org.netbeans.modules.bpel.properties.editors.FormBundle;
import org.netbeans.modules.soa.ui.form.CustomNodeEditor;
import org.netbeans.modules.soa.ui.form.valid.SoaDialogDisplayer;
import org.netbeans.modules.bpel.editors.api.ui.valid.NodeEditorDescriptor;
import org.netbeans.modules.bpel.nodes.actions.OpenJavaScriptEditorAction;
import org.netbeans.modules.bpel.nodes.BpelNode;
import org.netbeans.modules.bpel.nodes.ContainerBpelNode;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author nk160297
 */
public class NodeUtils {
    
    /**
     * Shows a custom editor for the specified Node.
     * @returns TRUE if dialog was submited with OK buppon
     */
    public static boolean showNodeCustomEditor(Node node, CustomNodeEditor.EditingMode editingMode) {
        if (node == null) {
            return false;
        }
        Component c = null;

        if (node instanceof BpelNode) {
            c = ((BpelNode)node).getCustomizer(editingMode);
        } else {
            c = node.getCustomizer();
        }
        //
        if (c == null) {
            return false;
        }
        //
        String title;

        if (CustomNodeEditor.EditingMode.CREATE_NEW_INSTANCE == editingMode && 
                node instanceof BpelNode) {
            String nodeTypeName = ((BpelNode)node).getNodeType().getDisplayName();
            String createNew = NbBundle.getMessage(
                    FormBundle.class, "LBL_Create_New"); // NOI18N
            title = createNew + " " + nodeTypeName; // NOI18N
        } else {
            String nodeName = node.getDisplayName();
            String propEditor = NbBundle.getMessage(
                    FormBundle.class, "LBL_Property_Editor"); // NOI18N
            if (nodeName != null && nodeName.length() > 0) {
                title = nodeName + " - " + propEditor; // NOI18N
            } else {
                title = propEditor;
            }
        }
        //
        if (c instanceof CustomNodeEditor) {
            CustomNodeEditor editor = (CustomNodeEditor)c;
            NodeEditorDescriptor descriptor = new NodeEditorDescriptor(editor, title);
            Dialog dialog = SoaDialogDisplayer.getDefault().createDialog(descriptor);
            SoaUtil.setInitialFocusComponentFor((Container)c);
            dialog.setVisible(true);
            
            return descriptor.isOkHasPressed();
        }
        if (node instanceof BpelNode) {
            Object ref = ((BpelNode) node).getReference();
        
            if (ref instanceof Assign && ((Assign) ref).isJavaScript()) {
                new OpenJavaScriptEditorAction((Assign) ref).actionPerformed(null);
                return true;
            }
        }
        return false;
    }
    
    public static List<Node> findNodes(
            Node sourceNode, SearchVisitor visitor, int maxDepth) {
        List<Node> result = new ArrayList<Node>();
        fillNodesList(result, sourceNode, visitor, maxDepth, false);
        return result;
    }
    
    /**
     * An auxiliary method is intended to help seach nodes recursively.
     */
    private static void fillNodesList(
            List<Node> nodesList,
            Node sourceNode,
            SearchVisitor visitor,
            int maxDepth,
            boolean lookDeeperIfFound) {
        //
        if (visitor.accept(sourceNode)) {
            nodesList.add(sourceNode);
            if (!lookDeeperIfFound) {
                return;
            }
        }
        //
        if (maxDepth == 0) {
            return;
        }
        //
        if (visitor.drillDeeper(sourceNode)) {
            Node[] nodes = sourceNode.getChildren().getNodes();
            maxDepth--;
            for (Node node : nodes) {
                fillNodesList(nodesList, node, visitor, maxDepth, lookDeeperIfFound);
            }
        }
        return;
    }
    
    /**
     * An auxiliary method is intended to help seach nodes recursively.
     * The sourceNode parameter specifies the search root.
     * <p>
     * The maxDepth parameter specifies the maximum depth do
     * which the recursive algorithm can go.
     * <p>
     * If it equals to -1, then infinite depth is emplied.
     * <p>
     * if it equals to 0 than it means that it only necessary to check
     * if the sourceNode satisfies to the searching conditions.
     * <p>
     * if it equals to 1 than it means that searching is requested
     * only among direct children of the source node.
     */
    public static Node findFirstNode(
            Node sourceNode,
            SearchVisitor visitor,
            int maxDepth) {
        //
        if (visitor.accept(sourceNode)) {
            return sourceNode;
        }
        //
        if (maxDepth == 0) {
            return null;
        }
        //
        if (visitor.drillDeeper(sourceNode)) {
            Node[] nodes = sourceNode.getChildren().getNodes();
            maxDepth--;
            for (Node node : nodes) {
                Node resultNode = findFirstNode(node, visitor, maxDepth);
                if (resultNode != null) {
                    return resultNode;
                }
            }
        }
        return null;
    }
    
    public interface SearchVisitor {
        /**
         * Indicates if the node satisfies the search conditions
         */
        boolean accept(Node node);
        
        /**
         * Indicates if it necessary to search diiper than the specified node.
         */
        boolean drillDeeper(Node node);
    }
    
    //--------------------------------------------------------------
    // Special implementations
    //--------------------------------------------------------------
    
    /**
     * Look for the first Node in the Nodes' hierarchy recursively.
     * <p>
     * If it equals to -1, then infinite depth is emplied.
     * <p>
     * if it equals to 0 than it means that it only necessary to check
     * if the sourceNode satisfies to the searching conditions.
     * <p>
     * if it equals to 1 than it means that searching is requested
     * only among direct children of the source node.
     * <p>
     * CAUTION! This method do simple iteration over th� hierarchy.
     * So it can take a lot of time depending on the data structure
     * if the maxDepth restriction isn't specified!
     */
    public static <S> BpelNode<S> findFirstNode(
            final S requiredObj,
            Node sourceNode,
            int maxDepth) {
        //
        NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
            public boolean accept(Node node) {
                if (node instanceof ContainerBpelNode) {
                    Object container =
                            ((ContainerBpelNode)node).getContainerReference();
                    if (requiredObj.equals(container)) {
                        return true;
                    }
                } else if (node instanceof BpelNode) {
                    Object subject = ((BpelNode)node).getReference();
                    if (requiredObj.equals(subject)) {
                        return true;
                    }
                }
                //
                return false;
            }
            
            public boolean drillDeeper(Node node) {
                return true;
            }
        };
        //
        Node resultNode = findFirstNode(sourceNode, visitor, maxDepth);
        if (resultNode != null && resultNode instanceof BpelNode) {
            return (BpelNode)resultNode;
        } else {
            return null;
        }
    }
    
    /**
     * Look for the first Node in the Nodes' hierarchy recursively.
     * <p>
     * If it equals to -1, then infinite depth is emplied.
     * <p>
     * if it equals to 0 than it means that it only necessary to check
     * if the sourceNode satisfies to the searching conditions.
     * <p>
     * if it equals to 1 than it means that searching is requested
     * only among direct children of the source node.
     * <p>
     * If the requiredObj parameter is null then only the node class is
     * taken into consideration.
     * <p>
     * CAUTION! This method do simple iteration over th� hierarchy.
     * So it can take a lot of time depending on the data structure
     * if the maxDepth restriction isn't specified!
     */
    public static  <P extends BpelNode> P findFirstNode(
            final Object requiredObj,
            final Class<P> requiredNodeClass,
            Node sourceNode,
            int maxDepth) {
        //
        NodeUtils.SearchVisitor visitor = new NodeUtils.SearchVisitor() {
            public boolean accept(Node node) {
                if (requiredNodeClass.isAssignableFrom(node.getClass())) {
                    if (requiredObj == null) {
                        return true;
                    }
                    if (node instanceof ContainerBpelNode) {
                        Object container =
                                ((ContainerBpelNode)node).getContainerReference();
                        if (requiredObj.equals(container)) {
                            return true;
                        }
                    } else if (node instanceof BpelNode) {
                        Object subject = ((BpelNode)node).getReference();
                        if (requiredObj.equals(subject)) {
                            return true;
                        }
                    }
                }
                //
                return false;
            }
            
            public boolean drillDeeper(Node node) {
                return true;
            }
        };
        //
        Node resultNode = findFirstNode(sourceNode, visitor, maxDepth);
        if (resultNode != null && resultNode instanceof BpelNode) {
            return (P)resultNode;
        } else {
            return null;
        }
    }
    
    public static <P extends BpelNode> P findFirstNode(
            Object requiredObj,
            Class<P> requiredNodeClass,
            Node sourceNode) {
        return findFirstNode(requiredObj, requiredNodeClass, sourceNode, -1);
    }
    
    public static <S> BpelNode<S> findFirstNode(
            S requiredObj,
            Node sourceNode) {
        return findFirstNode(requiredObj, sourceNode, -1);
    }
    
}
