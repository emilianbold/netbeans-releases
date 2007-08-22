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
package org.netbeans.modules.xslt.mapper.view;

import java.util.List;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.IBasicViewModel;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xml.xpath.XPathOperationOrFuntion;
import org.netbeans.modules.xslt.mapper.model.BuildExpressionVisitor;
import org.netbeans.modules.xslt.mapper.model.nodes.LiteralCanvasNode;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.targettree.StylesheetNode;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslComponent;

/**
 *
 * @author Alexey
 */
public class DiagramBuilder {

    boolean updating = false;
    private XsltMapper mapper;
    private boolean relayoutRequired;
    private PredicateFinderVisitor pfv;

    public DiagramBuilder(XsltMapper mapper) {
        this.mapper = mapper;
        pfv = new PredicateFinderVisitor(mapper);
    }

    public void updateDiagram(boolean reuse) {
        TreeNode root = (TreeNode) mapper.getMapperViewManager()
                .getDestView()
                .getTree()
                .getModel()
                .getRoot();

        if (root != null) {
            relayoutRequired = false;
            try {
                updating = true;
                updateDiagramRecursive(root, reuse);
            } finally {
                updating = false;
            }
            if (relayoutRequired) {
                //this flag indicates that at least 1 node was added to diagram
                mapper.getMapperViewManager().getCanvasView().getAutoLayout().autoLayout();
            }
        }
        //
        // Remove old automatically created predicates.
        mapper.getPredicateManager().clearTemporaryPredicates();
    }

    public boolean isUpdating() {
        return this.updating;
    }

    /**
     * Detects if given target tree node contains xpath expression.
     * If so, builds expression node graph starting from this tree node
     **/
    public void updateDiagram(TreeNode tree_node, boolean reuse) {

        Object data = tree_node.getDataObject();

        if (!(data instanceof XslComponent)) {
            return;
        }

        XslComponent xslc = (XslComponent) data;

        GetExpressionVisitor visitor_ge = new GetExpressionVisitor();

        xslc.accept(visitor_ge);
        XPathExpression new_expr = visitor_ge.getExpression();

        // Look for predicates in the specified XPath expression
        if (new_expr != null) {
            pfv.setContextXslComponent(xslc);
            new_expr.accept(pfv);
        }

        //first check, if current node is already connected to any graph
        List<Node> upstreams = tree_node.getPreviousNodes();

        Node upstream = null;
        for (Node n : upstreams ){
            if (n != null){
                upstream = n;
                break;
            }
        }

        XPathExpression current_expr = null;

        if (reuse) {
            if (!upstreams.isEmpty() && reuse) {

                if (upstream != null) {
                    BuildExpressionVisitor visitor_be = new BuildExpressionVisitor(mapper.getContext());

                    upstream.accept(visitor_be);
                    current_expr = visitor_be.getResult();
                }
            }

            if (current_expr != null) {
                if (new_expr == null || !current_expr.toString().equals(new_expr.toString())) {
                    destroyDiagramRecursive(tree_node);
                }
            }
        } else {
            destroyDiagramRecursive(tree_node);
        }

        if (new_expr != null) {
            if (current_expr == null || !new_expr.toString().equals(current_expr.toString())) {

                relayoutRequired = true;

                Node graph_root = buildDiagramRecursive(new_expr);
                if (graph_root != null) {
                    mapper.addLink(graph_root, tree_node);
                }
            }
        }
    }

    public void destroyDiagramRecursive(Node node) {

        List<Node> upstreams = node.getPreviousNodes();

        for (Node upstream : upstreams) {
            if (upstream == null) {
                continue;
            }

            IBasicViewModel model = mapper.getMapperViewManager().getMapperModel().getSelectedViewModel();

            mapper.removeDirectedChain(upstream.getMapperNode(), model);
        }


        List links = node.getMapperNode().getLinks();
        for (int i = 0; i < links.size(); i++) {
            mapper.removeLink((IMapperLink) links.get(i));
        }


        //        List<Node> upstreams = node.getPreviousNodes();
        //        List links = node.getOutputNode().getLinks();
        //
        //        mapper.removeNode(node.getMapperNode());
        //        for(Object link: links){
        //            mapper.removeLink((IMapperLink) link);
        //        }
        //        for (Node upstream: upstreams){
        //            destroyDiagramRecursive(upstream);
        //        }
        //
    }

    /**
     * Perform recursion deep into expression.
     * Create mapper nodes for all subexpressions and add them to canvas
     **/
    private Node buildDiagramRecursive(XPathExpression expression) {
        NodeCreatorVisitor visitor_cn = new NodeCreatorVisitor(mapper);

        expression.accept(visitor_cn);

        Node node = visitor_cn.getResult();

        if (node == null) {
            return null;
        }

        IMapperNode mapper_node = (IMapperNode) node.getMapperNode();

        if (!(mapper_node instanceof IMapperTreeNode)) {
            mapper.addNode(mapper_node);
            //hack required by stupid Mapper framework.
            //aaddNode resets the literalName.
            if (node instanceof LiteralCanvasNode) {
                IFieldNode field0 = (IFieldNode) node.getOutputNode();
                field0.setLiteralName(expression.toString());
            }

            if (mapper_node instanceof IMethoidNode && expression instanceof XPathOperationOrFuntion) {
                IMethoidNode methoidNode = (IMethoidNode) mapper_node;
                IMethoid methoid = (IMethoid) methoidNode.getMethoidObject();
                boolean isAccumulative = methoid.isAccumulative();
                int fieldIndex = 0;
                //
                for (Object expr : ((XPathOperationOrFuntion) expression).getChildren()) {
                    List inputFields = methoidNode.getInputFieldNodes();
                    if (fieldIndex >= inputFields.size()) {
                        break;
                    }
                    IFieldNode fn = (IFieldNode) inputFields.get(fieldIndex);
                    assert fn != null : "Filed shouldn't be null anyway"; // NOI18N
                    if (!expr.toString().startsWith(BuildExpressionVisitor.UNCONNECTED_INPUT)) {
                        Node upstream_node = buildDiagramRecursive((XPathExpression) expr);

                        fn.setNodeObject(upstream_node);

                        if (upstream_node != null) {
                            mapper.addLink(upstream_node, node);
                        }
                    }
                    fieldIndex++;
                }
            }
        }
        return node;
    }

    private boolean methoidHasField(IMethoidNode methoidNode, String fieldName) {
        for (Object fieldObj : methoidNode.getInputFieldNodes()) {
            if (fieldObj instanceof IFieldNode) {
                IFieldNode tempFieldNode = (IFieldNode) fieldObj;
                String tempFieldName = tempFieldNode.getName();
                if (fieldName.equals(tempFieldName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Iterates over target tree nodes
     * calls updateDiagram() for each node
     **/
    private void updateDiagramRecursive(TreeNode node, boolean reuse) {
        if (node instanceof StylesheetNode) {
            updateDiagram(node, reuse);
            for (TreeNode n : node.getChildren()) {
                updateDiagramRecursive(n, reuse);
            }
        }
    }
}
