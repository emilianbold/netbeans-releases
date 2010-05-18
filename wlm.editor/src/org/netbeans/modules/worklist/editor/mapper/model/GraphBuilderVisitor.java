/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.worklist.editor.mapper.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.TargetPin;
import org.netbeans.modules.soa.mappercore.model.TreeSourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.ui.tree.TreeItemFinder;
import org.netbeans.modules.soa.ui.tree.impl.TreeFinderProcessor;
import org.netbeans.modules.wlm.model.api.VariableDeclaration;
import org.netbeans.modules.wlm.model.xpath.XPathWlmVariable;
import org.netbeans.modules.worklist.editor.mapper.WlmDesignContext;
import org.netbeans.modules.worklist.editor.mapper.tree.MapperSwingTreeModel;
import org.netbeans.modules.worklist.editor.mapper.tree.search.ByNameVariableFinder;
import org.netbeans.modules.worklist.editor.mapper.tree.search.FinderListBuilder;
import org.netbeans.modules.worklist.editor.mapper.tree.search.PartFinder;
import org.netbeans.modules.worklist.editor.mapper.tree.search.VariableFinder;
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
    
    protected WlmDesignContext currentWlmDesignContext;
    
    private boolean propertyVariableWorkaround = false;
    
    public GraphBuilderVisitor(Graph graph, MapperSwingTreeModel leftTreeModel, 
            boolean connectToTargetTree, WlmDesignContext context) {
        mGraph = graph;
        mLeftTreeModel = leftTreeModel;
        mConnectToTargetTree = connectToTargetTree;
        currentWlmDesignContext = context;
    }

    @Override
    public void visit(XPathCoreFunction coreFunction) {
        CoreFunctionType functionType = coreFunction.getFunctionType();
        Vertex newVertex = VertexFactory.getInstance().createCoreFunction(functionType);
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
        Vertex newVertex = VertexFactory.getInstance().createCoreOperation(operationType);
        //
        mGraph.addVertex(newVertex);
        //
        linkToParent(newVertex);
        //
        processChildren(newVertex, coreOperation);
    }

    @Override
    public void visit(XPathExtensionFunction extensionFunction) {
        if (StubExtFunction.STUB_FUNC_NAME.equals(extensionFunction.getName())) {
            // The stub() function doesn't appear in the mapper's graph.
            // But it occupy a connection point of the parent vertex.
            stubLinkToParent();
        } else {
            ExtFunctionMetadata metadata = extensionFunction.getMetadata();
            
            Vertex newVertex = VertexFactory.getInstance().createExtFunction(metadata);
            //
            if (newVertex != null) {
                mGraph.addVertex(newVertex);
                //
                linkToParent(newVertex);
                //
                try {
                    processChildren(newVertex, extensionFunction);
                } finally {
                    propertyVariableWorkaround = false;
                }
            }
        }
    }
    
    @Override
    public void visit(XPathLocationPath locationPath) {
        // TODO:
        // Do nothing here for a while
        // It seems they are not supported in the WLM
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
        Vertex newVertex = VertexFactory.getInstance().createNumericLiteral(value);
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
        Vertex newVertex = VertexFactory.getInstance().createStringLiteral(value);
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
        if (xPathVar != null && xPathVar instanceof XPathWlmVariable) {
            connectToLeftTree((XPathWlmVariable)xPathVar);
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

    protected void connectToLeftTree(XPathWlmVariable xPathVar) {
        ArrayList<TreeItemFinder> finderList = new ArrayList<TreeItemFinder>();
        //
        if (xPathVar != null) {
            VariableDeclaration varDecl = xPathVar.getVarDecl();
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
        if ((! connectToLeftTree(FinderListBuilder.build(path))) && 
            (currentWlmDesignContext != null)) {
//            DesignContextControllerImpl.addErrMessage(
//                currentWlmDesignContext.getValidationErrMsgBuffer(),
//                path.getExpressionString(), "from");
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
                VertexItem newRealVItem = VertexFactory.constructVItem(
                        mVertex, (ArgumentDescriptor)dataObject);
                result.add(newRealVItem);
                //
                // A new hairline item has to be inserted after the real vertex item
                VertexItem newHirelineVItem = 
                        VertexFactory.constructHairline(mVertex, dataObject);
                result.add(newHirelineVItem);
            } else if (dataObject instanceof ArgumentGroup) {
                List<VertexItem> itemsList = VertexFactory.getInstance().
                        createGroupItems(mVertex, (ArgumentGroup)dataObject);
                //
                result.addAll(itemsList);
                //
                // A new hairline item will appear just after the group's items.
                VertexItem newHirelineVItem = 
                        VertexFactory.constructHairline(mVertex, dataObject);
                result.add(newHirelineVItem);
            }
            //
            return result;
        }
    
    }
    
}
    