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

package org.netbeans.modules.bpel.mapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import javax.swing.tree.TreePath;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelMapperDcc;
import org.netbeans.modules.bpel.mapper.tree.search.ByNameVariableFinder;
import org.netbeans.modules.bpel.mapper.tree.search.BpelFinderListBuilder;
import org.netbeans.modules.bpel.mapper.tree.search.PartFinder;
import org.netbeans.modules.bpel.mapper.tree.search.VariableAndNMPropertyFinder;
import org.netbeans.modules.bpel.mapper.tree.search.VariableAndPropertyFinder;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.bpel.mapper.tree.search.VariableFinder;
import org.netbeans.modules.bpel.model.api.AbstractVariableDeclaration;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.netbeans.modules.bpel.model.api.support.XPathBpelVariable;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.xpath.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.xml.xpath.ext.CoreFunctionType;
import org.netbeans.modules.xml.xpath.ext.CoreOperationType;
import org.netbeans.modules.xml.xpath.ext.XPathCoreFunction;
import org.netbeans.modules.xml.xpath.ext.XPathCoreOperation;
import org.netbeans.modules.xml.xpath.ext.XPathExpressionPath;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathLocationPath;
import org.netbeans.modules.xml.xpath.ext.XPathNumericLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathOperationOrFuntion;
import org.netbeans.modules.xml.xpath.ext.XPathStringLiteral;
import org.netbeans.modules.xml.xpath.ext.XPathVariableReference;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentGroup;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.metadata.StubExtFunction;
import org.netbeans.modules.xml.xpath.ext.visitor.XPathVisitorAdapter;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.xpath.ext.XPathExpression;
import org.netbeans.modules.xml.xpath.ext.spi.XPathVariable;

/**
 * Populates the Graph object with a complex content by an XPath expression.
 * 
 * @author nk160297
 */
public class GraphBuilderVisitor extends XPathVisitorAdapter {
        
    protected Graph mGraph;
    protected MapperSwingTreeModel mLeftTreeModel;
    
    // Indicates if it necessary to connect the builded graph to the target tree.
    protected boolean mConnectToTargetTree;

    protected Stack<VertexBuilderData> mVertexStack = new Stack<VertexBuilderData>();
    
    protected BpelDesignContext currentBpelDesignContext;   
    
    private boolean propertyVariableWorkaround = false;
    
    public GraphBuilderVisitor(Graph graph, MapperSwingTreeModel leftTreeModel,
            boolean connectToTargetTree, BpelDesignContext context) {
        mGraph = graph;
        mLeftTreeModel = leftTreeModel;
        mConnectToTargetTree = connectToTargetTree;
        currentBpelDesignContext = context;
    }

    @Override
    public void visit(XPathCoreFunction coreFunction) {
        CoreFunctionType functionType = coreFunction.getFunctionType();
        Vertex newVertex = BpelVertexFactory.getInstance().createCoreFunction(functionType);
        //
        mGraph.addVertex(newVertex);
        //
        linkToParent(newVertex);
        //
        processChildren(newVertex, coreFunction);
    }

    @Override
    public void visit(XPathCoreOperation coreOperation) {
        CoreOperationType operationType = coreOperation.getOperationType();
        Vertex newVertex = BpelVertexFactory.getInstance().createCoreOperation(operationType);
        //
        mGraph.addVertex(newVertex);
        //
        linkToParent(newVertex);
        //
        processChildren(newVertex, coreOperation);
    }

    @Override
    public void visit(XPathExtensionFunction extensionFunction) {
//System.out.println();
//System.out.println("VISIT: " + extensionFunction);

        if (StubExtFunction.STUB_FUNC_NAME.equals(extensionFunction.getName())) {
            // The stub() function doesn't appear in the mapper's graph.
            // But it occupy a connection point of the parent vertex.
            stubLinkToParent();
//System.out.println("  V1");
        } else {
//System.out.println("  V2");
            if (specialProcessingForGetVariableProperty(extensionFunction)) {
                return;
            }
//System.out.println("  V3");
            if (specialProcessingForGetVariableNMProperty(extensionFunction)) {
                return;
            }
//System.out.println("  V4");
            ExtFunctionMetadata metadata = extensionFunction.getMetadata();
            Vertex newVertex = BpelVertexFactory.getInstance().createExtFunction(metadata);
//System.out.println("  V5: " + newVertex);
//System.out.println("  metadata: " + metadata);
            if (newVertex != null) {
                mGraph.addVertex(newVertex);
                //
                
                linkToParent(newVertex);
                //
                
                if (metadata == BpelXPathExtFunctionMetadata.GET_VARIABLE_PROPERTY_METADATA
                        || metadata == BpelXPathExtFunctionMetadata.GET_VARIABLE_NM_PROPERTY_METADATA) 
                {
                    propertyVariableWorkaround = true;
                }
                try {
//System.out.println("  V6");
                    processChildren(newVertex, extensionFunction);
                } finally {
                    propertyVariableWorkaround = false;
                }
            }
//System.out.println("  V7");
        }
    }
    
    private boolean specialProcessingForGetVariableProperty(
            XPathExtensionFunction extensionFunction) 
    {
        ExtFunctionMetadata metadata = extensionFunction.getMetadata();
        if (metadata != BpelXPathExtFunctionMetadata
                .GET_VARIABLE_PROPERTY_METADATA)
        {
            return false;
        }
      
        if (extensionFunction.getChildCount() != 2) {
            return false;
        }

        XPathExpression variableExpression = extensionFunction
                .getChild(0);

        if (!(variableExpression instanceof XPathStringLiteral)) {
            return false;
        }
        
        XPathExpression propertyExpression = extensionFunction
                .getChild(1);
        
        if (!(propertyExpression instanceof XPathStringLiteral)) {
            return false;
        }
        
        String variableName = ((XPathStringLiteral) variableExpression)
                .getValue();
        if (variableName == null || variableName.trim().length() == 0) {
            return false;
        }
        
        String propertyName = ((XPathStringLiteral) propertyExpression)
                .getValue();
        if (propertyName == null || propertyName.trim().length() == 0) {
            return false;
        }
        
        List<TreeItemFinder> findersList = new ArrayList<TreeItemFinder>(1);
        
        findersList.add(new VariableAndPropertyFinder(currentBpelDesignContext, 
                variableName, propertyName));
        
        return connectToLeftTree(findersList);
    }

    private boolean specialProcessingForGetVariableNMProperty(
            XPathExtensionFunction extensionFunction) 
    {
        ExtFunctionMetadata metadata = extensionFunction.getMetadata();
        if (metadata != BpelXPathExtFunctionMetadata
                .GET_VARIABLE_NM_PROPERTY_METADATA)
        {
            return false;
        }
      
        if (extensionFunction.getChildCount() != 2) {
            return false;
        }

        XPathExpression variableExpression = extensionFunction
                .getChild(0);

        if (!(variableExpression instanceof XPathStringLiteral)) {
            return false;
        }
        
        XPathExpression propertyExpression = extensionFunction
                .getChild(1);
        
        if (!(propertyExpression instanceof XPathStringLiteral)) {
            return false;
        }
        
        String variableName = ((XPathStringLiteral) variableExpression)
                .getValue();
        if (variableName == null || variableName.trim().length() == 0) {
            return false;
        }
        
        String propertyName = ((XPathStringLiteral) propertyExpression)
                .getValue();
        if (propertyName == null || propertyName.trim().length() == 0) {
            return false;
        }
        
        List<TreeItemFinder> findersList = new ArrayList<TreeItemFinder>(1);
        
        findersList.add(new VariableAndNMPropertyFinder(currentBpelDesignContext, 
                variableName, propertyName));
        
        return connectToLeftTree(findersList);
    }
    
    @Override
    public void visit(XPathLocationPath locationPath) {
        // TODO:
        // Do nothing here for a while
        // It seems they are not supported in the BPEL
        // It can be used by predicates, but they will be shown 
        // in a separate view and will have separate loading code.
        //
        // TODO: 
        // In can be necessary if the Variable-Part-Query for of 
        // an assign->copy is used. Now it isn't supported by the runtime
        // But later it can be necessary to support. 
    }

    @Override
    public void visit(XPathNumericLiteral numericLiteral) { 
        Number value = numericLiteral.getValue();
        Vertex newVertex = BpelVertexFactory.getInstance().createNumericLiteral(value);
        //
        mGraph.addVertex(newVertex);
        //
        linkToParent(newVertex);
    }

    @Override
    public void visit(XPathStringLiteral stringLiteral) {
        if (propertyVariableWorkaround) {
            String value = stringLiteral.getValue();
            propertyVariableWorkaround = false;
            
            if (value != null) {
                List<TreeItemFinder> finders = Collections
                        .singletonList((TreeItemFinder)
                        new ByNameVariableFinder(value));
                if (connectToLeftTree(finders)) {
                    return;
                }
            }
        } 
        
        String value = stringLiteral.getValue();
        Vertex newVertex = BpelVertexFactory.getInstance().createStringLiteral(value);
        //
        mGraph.addVertex(newVertex);
        //
        linkToParent(newVertex);
    }

    @Override
    public void visit(XPathExpressionPath expressionPath) {
        connectToLeftTree(expressionPath);
    }

    @Override
    public void visit(XPathVariableReference vReference) {
        XPathVariable xPathVar = vReference.getVariable();
        if (xPathVar != null && xPathVar instanceof XPathBpelVariable) {
            connectToLeftTree((XPathBpelVariable)xPathVar);
        }
    }
    
    //--------------------------------------------------------------------------
    // Auxiliary methods
    //--------------------------------------------------------------------------

    /**
     * Link can be connected to the target tree (graph) or to vertex item 
     * of another vertex. If there is a vertex to which the link can be connected,
     * then it can be taken from the mVertexStack. 
     * 
     * @param newVertex
     */
    protected void linkToParent(SourcePin sourcePin) {
        TargetPin targetPin = null;
        if (mVertexStack.isEmpty()) {
            if (!mConnectToTargetTree) {
                // return if connection to the right tree is prohibited
                return;
            }
            targetPin = mGraph;
        } else {
            VertexBuilderData parentVertexBD = mVertexStack.peek();
            //
            // The links are connected to the target vertex according the order 
            // in which they are declared in the original XPath expression. 
            // So the first unconnected vertex items are taken from the target 
            // vertex. If target vertex doesn't have any free items to connect 
            // the link, then a new item should be created if it is allowed 
            // by signature of corresponding operation or function.
            VertexItem targetVertexItem = parentVertexBD.freeVertexItemRequired();
            targetPin = targetVertexItem;
        }
        if (targetPin != null) {
            Link newLink = new Link(sourcePin, targetPin);
            mGraph.addLink(newLink);
        }
    }

    /**
     * Occupies the next free input connection point of the parent vertex. 
     */
    protected void stubLinkToParent() {
        if (!mVertexStack.isEmpty()) {
            VertexBuilderData parentVertexBD = mVertexStack.peek();
            parentVertexBD.freeVertexItemRequired();
        }
    }

    protected void processChildren(Vertex newVertex, XPathOperationOrFuntion expr) {
        VertexBuilderData vbd = new VertexBuilderData(newVertex);
        mVertexStack.push(vbd);
        try {
            visitChildren(expr);
        } finally {
            mVertexStack.pop();
        }
    }

    protected void connectToLeftTree(XPathBpelVariable xPathVar) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        if (xPathVar != null) {
            AbstractVariableDeclaration varDecl = xPathVar.getVarDecl();
            if (varDecl != null) {
                finderList.add(new VariableFinder(varDecl));
                //
                Part part = xPathVar.getPart();
                if (part != null) {
                    finderList.add(new PartFinder(part));
                }
            }
        }
        //
        connectToLeftTree(finderList);
    }

    protected void connectToLeftTree(XPathExpressionPath path) {
        if ((! connectToLeftTree(BpelFinderListBuilder.singl().build(path))) &&
            (currentBpelDesignContext != null)) {
            BpelMapperDcc.addErrMessage(
                currentBpelDesignContext.getValidationErrMsgBuffer(), 
                path.getExpressionString(), "from");
        }
    }

    protected boolean connectToLeftTree(List<TreeItemFinder> finderList) {
        TreeFinderProcessor fProcessor = new TreeFinderProcessor(mLeftTreeModel);
        TreePath sourceTreePath = fProcessor.findFirstNode(finderList);
        // TreePath sourceTreePath = mLeftTreeModel.findFirstNode(finderList);
        if (sourceTreePath != null) {
            TreeSourcePin sourcePin = new TreeSourcePin(sourceTreePath);
            linkToParent(sourcePin);
            return true;
        }
        return false;
    }

    /**
     * Internal class which is intended to hold a vertex with a count of 
     * occupied connection points.
     */
    protected class VertexBuilderData {
        
        private Vertex mVertex;
        private int mLastOccupiedVertexItem = -1; // index of the last occupied vertex item
        
        public VertexBuilderData(Vertex vertex) {
            mVertex = vertex;
        }
        
        public Vertex getVertex() {
            return mVertex;
        }
        
        /**
         * It looks for the first unconnected VertexItem. 
         * If all are connected, then a new one is created and is returned.
         * 
         * TODO: It is necessary to support Matadata! 
         * TODO: Some functions can have arguments of different types. If 
         * an argument is not assigned, then the stub() is located in the place
         * of such argument in XPath text. The approach, when a link is connected
         * to any first unconnected item, can come to wrong connection place. 
         * 
         * @param vertex
         * @return
         */
        public VertexItem freeVertexItemRequired() {
            //
            // Get next free connection point
            VertexItem resultItem = null;
            for (int index = mLastOccupiedVertexItem + 1; 
            index < mVertex.getItemCount(); index++) {
                VertexItem item = mVertex.getItem(index);
                if (item != null && !item.isHairline()) {
                    Link ingoingLink = item.getIngoingLink();
                    if (ingoingLink == null) {
                        // An unconnected vertex item has found
                        resultItem = item;
                        mLastOccupiedVertexItem = index;
                        break;
                    }
                }
            }
            //
            if (resultItem == null) {
                // There is not any free vertex item.
                List<VertexItem> itemsList = createNewVertexItems();
                if (itemsList != null && !itemsList.isEmpty()) {
                    // Move the index to the last item + 1
                    mLastOccupiedVertexItem = mVertex.getItemCount();
                    resultItem = itemsList.get(0);
                    //
                    for (VertexItem item : itemsList) {
                        mVertex.addItem(item);
                    }
                }
            }
            //
            return resultItem;
        }

        private List<VertexItem> createNewVertexItems() {
            VertexItem lastVItem = mVertex.getItem(mVertex.getItemCount() - 1);
            if (lastVItem.isHairline()) {
                // Create a new real vertex item (group of items) based on 
                // the hairline.
                return calculateReplaceForHairline(lastVItem);
                //
            } else {
                // If the last vertex item isn't a hairline item, then it means 
                // that not any additional items are acceptable here. 
                // Add specific vertex items, which indicates unexpected arguments.
                VertexItem unexpected = new VertexItem(mVertex, "Unexpected"); // TODO I18N
                return Collections.singletonList(unexpected);
            }
        }

        public List<VertexItem> calculateReplaceForHairline(VertexItem hairline) {
            List<VertexItem> result = new ArrayList<VertexItem>();
            //
            Object dataObject = hairline.getDataObject();
            if (dataObject instanceof ArgumentDescriptor) {
                //
                // A new real vertex item has to be inserted after the hairline item
                VertexItem newRealVItem = BpelVertexFactory.constructVItem(
                        mVertex, (ArgumentDescriptor)dataObject);
                result.add(newRealVItem);
                //
                // A new hairline item has to be inserted after the real vertex item
                VertexItem newHirelineVItem = 
                        BpelVertexFactory.constructHairline(mVertex, dataObject);
                result.add(newHirelineVItem);
            } else if (dataObject instanceof ArgumentGroup) {
                List<VertexItem> itemsList = BpelVertexFactory.getInstance().
                        createGroupItems(mVertex, (ArgumentGroup)dataObject);
                //
                result.addAll(itemsList);
                //
                // A new hairline item will appear just after the group's items.
                VertexItem newHirelineVItem = 
                        BpelVertexFactory.constructHairline(mVertex, dataObject);
                result.add(newHirelineVItem);
            }
            //
            return result;
        }
    }
}
