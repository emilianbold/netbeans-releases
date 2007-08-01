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
package org.netbeans.modules.xslt.mapper.model;

import com.sun.org.apache.xerces.internal.dom.ParentNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.xml.namespace.QName;
import org.netbeans.modules.soa.mapper.common.IMapperEvent;
import org.netbeans.modules.soa.mapper.common.IMapperLink;
import org.netbeans.modules.soa.mapper.common.IMapperListener;
import org.netbeans.modules.soa.mapper.common.IMapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IFieldNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoid;
import org.netbeans.modules.soa.mapper.common.basicmapper.methoid.IMethoidNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;
import org.netbeans.modules.soa.ui.axinodes.AxiomUtils;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.netbeans.modules.xml.axi.AbstractAttribute;
import org.netbeans.modules.xml.axi.AbstractElement;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xpath.AbstractXPathModelHelper;
import org.netbeans.modules.xml.xpath.XPathExpression;
import org.netbeans.modules.xslt.mapper.methoid.Constants;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.model.targettree.AXIUtils;
import org.netbeans.modules.xslt.mapper.model.targettree.SchemaNode;
import org.netbeans.modules.xslt.mapper.model.targettree.StylesheetNode;
import org.netbeans.modules.xslt.mapper.model.targettree.TargetTreeModel;
import org.netbeans.modules.xslt.mapper.view.NodeCreatorVisitor;
import org.netbeans.modules.xslt.mapper.view.SetExpressionVisitor;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.netbeans.modules.xslt.model.AttrValueTamplateHolder;
import org.netbeans.modules.xslt.model.Attribute;
import org.netbeans.modules.xslt.model.AttributeValueTemplate;
import org.netbeans.modules.xslt.model.Element;
import org.netbeans.modules.xslt.model.NamespaceSpec;
import org.netbeans.modules.xslt.model.SequenceConstructor;
import org.netbeans.modules.xslt.model.SequenceElement;
import org.netbeans.modules.xslt.model.Stylesheet;
import org.netbeans.modules.xslt.model.StylesheetChild;
import org.netbeans.modules.xslt.model.Template;
import org.netbeans.modules.xslt.model.XslComponent;
import org.netbeans.modules.xslt.model.XslModel;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Alexey
 * Keeps mapper data model in sync with XSLT model:
 * -Propogates changes made on diagram to XSLT document
 * -reacts on changes made to XSLT model from source view by updating mapper data model
 */
public class XsltModelBridge extends ModelBridge implements IMapperListener {

    public XsltModelBridge(XsltMapper mapper) {
        super(mapper);
        super.subscribe(mapper.getContext().getXSLModel());
    }

    /**
     * This method gets and processes the user input events and makes
     * modifications to the model.
     */
    public void eventInvoked(IMapperEvent e) {

        if (IMapperEvent.LINK_ADDED.equals(e.getEventType()) || IMapperEvent.LINK_DEL.equals(e.getEventType())) {
            onGraphChanged(((IMapperLink) e.getTransferObject()).getEndNode());
        } else if (IMapperEvent.REQ_UPDATE_NODE.equals(e.getEventType())) {
            onGraphChanged((IMethoidNode) e.getTransferObject());
        } else if (IMapperEvent.REQ_NEW_NODE.equals(e.getEventType())) {
            onMethoidAdded((IMethoidNode) e.getTransferObject());
        }
    }

    private void onGraphChanged(IMapperNode target) {
        if (getMapper().getBuilder().isUpdating()) {
            return;
        }
        Node node = null;

        if (target instanceof IMapperTreeNode) {
            node = (Node) TreeNode.getNode((IMapperTreeNode) target);
        } else if (target instanceof IFieldNode) {
            node = (Node) ((IFieldNode) target).getGroupNode().getNodeObject();
        } else if (target instanceof IMethoidNode) {
            node = (Node) ((IMethoidNode) target).getNodeObject();
        }

        if (node == null) {
            return;
        }

        //step 1. Walk downstream of mapper graph to find node owning the subtree was changes
        Node owner = findOwnerNode(node);

        if (owner == null) {
            //current graph is not connected  to target tree, nothing to update
            return;
        }


        BuildExpressionVisitor visitor_ge = new BuildExpressionVisitor(getMapper().getContext());

        //check if owner node has graph connected
        if (!owner.getPreviousNodes().isEmpty()) {
            Node rootNode = owner.getPreviousNodes().get(0);
            if (rootNode != null) {
                /*
                 * If yes, accept visitor on root element of this graph.
                 * Visitor will perform recursion over the whole subtree and return
                 * expression as result
                 */
                rootNode.accept(visitor_ge);
            }
        }




        XslComponent xslc = null;
        if (owner instanceof SchemaNode && visitor_ge.getResult() != null && visitor_ge.getResult().getExpressionString() != null && visitor_ge.getResult().getExpressionString().length() > 0) {
            XslModel model = getMapper().getContext().getXSLModel();
            xslc = new BranchConstructor((SchemaNode) owner, getMapper()).construct();
        } else {
            xslc = (XslComponent) owner.getDataObject();
        }



        //push expression string to XSL model element
        SetExpressionVisitor visitor_ue = new SetExpressionVisitor(visitor_ge.getResult());



        if (xslc != null) {
            xslc.getModel().startTransaction();
            try {
                xslc.accept(visitor_ue);
            } finally {
                xslc.getModel().endTransaction();
            }
        } else {
            assert false : "Trying to assign expression to non-xslt node";
        }
    }

    private void onMethoidAdded(IMethoidNode node) {
        if (getMapper().getBuilder().isUpdating()) {
            return;
        }
        IMethoid methoid = (IMethoid) node.getMethoidObject();
        FileObject mfo = (FileObject) methoid.getData();

        String methodName = mfo.getName();
        if (methodName == null || ("").equals(methodName.trim())) {
            return;
        }
        XPathExpression expr = null;
        if (methodName.equals(Constants.NUMBER_LITERAL)) {
            expr = AbstractXPathModelHelper.getInstance().newXPathNumericLiteral(new Long(0));
        } else if (methodName.equals(Constants.DURATION_LITERAL)) {
            expr = AbstractXPathModelHelper.getInstance().newXPathStringLiteral("P0Y0M0DT0H0M0S");
        } else if (methodName.equals(Constants.STRING_LITERAL) || methodName.equals(Constants.XPATH_LITERAL)) {
            expr = AbstractXPathModelHelper.getInstance().newXPathStringLiteral("");
        } else {

            if (mfo.getAttribute(Constants.XPATH_FUNCTION) != null) {
                String fname = (String) mfo.getAttribute(Constants.XPATH_FUNCTION);
                int typeID = AbstractXPathModelHelper.getInstance().getFunctionType(fname).intValue();
                expr = AbstractXPathModelHelper.getInstance().newXPathCoreFunction(typeID);
            } else if (mfo.getAttribute(Constants.XPATH_OPERATOR) != null) {
                String opname = (String) mfo.getAttribute(Constants.XPATH_OPERATOR);

                //Workaround for bug in XPath OM to fix IZ95683
                if ("=".equals(opname)) {
                    opname = "==";
                }
                //end of woraround
                int typeID = AbstractXPathModelHelper.getInstance().getOperatorType(opname).intValue();
                expr = AbstractXPathModelHelper.getInstance().newXPathCoreOperation(typeID);
            }
        }
        if (expr != null) {
            NodeCreatorVisitor visitor_nc = new NodeCreatorVisitor(getMapper());
            expr.accept(visitor_nc);
            Node data_node = visitor_nc.getResult();
            if (node != null) {
                data_node.setMapperNode(node);
                node.setNodeObject(data_node);
            }
        }
    }

    protected void onModelChanged() {
        if (!checkErrors()) {
            return;
        }
        super.reloadTree(getMapper().getMapperViewManager().getDestView().getTree());
        
        getMapper().getBuilder().updateDiagram();
    }




    private Node findOwnerNode(Node node) {
        return findOwnerNode(node, new HashSet<Node>());
    }

    private Node findOwnerNode(Node node, Set<Node> visited) {

        //mark node as visited to avoid hangups if circular links are on diagram
        if (visited.contains(node)) {
            return null;
        } else {
            visited.add(node);
        }

        IMapperNode mn = node.getMapperNode();
        if (mn instanceof IMapperTreeNode && ((IMapperTreeNode) mn).isDestTreeNode()) {
            return node;
        }

        for (Node n : node.getNextNodes()) {
            Node result = findOwnerNode(n, visited);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
