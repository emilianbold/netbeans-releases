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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.tree.TreePath;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.SourcePin;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.bpel.mapper.model.customitems.XmlLiteralDataObject;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContextController;
import org.netbeans.modules.bpel.mapper.tree.models.VariableDeclarationWrapper;
import org.netbeans.modules.bpel.mapper.tree.search.BpelFinderListBuilder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.bpel.model.api.Catch;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.Requester;
import org.netbeans.modules.bpel.model.api.Responder;
import org.netbeans.modules.bpel.model.api.VariableDeclaration;
import org.netbeans.modules.bpel.model.api.VariableReference;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.FilterableMapperModel;
import org.netbeans.modules.soa.mappercore.model.Graph;
import org.netbeans.modules.soa.ui.tree.SoaTreeModel;
import org.netbeans.modules.soa.ui.tree.TreeItem;
import org.netbeans.modules.soa.xpath.mapper.model.ConnectionConstraint;
import org.netbeans.modules.soa.xpath.mapper.model.XPathMapperModel;
import org.netbeans.modules.soa.xpath.mapper.model.updater.GraphChangeProcessor;

/**
 * The default implementation of the MapperModel interface for the BPEL Mapper.
 * 
 * @author Nikita Krjukov
 * @author Vitaly Bychkov
 * @author AlexanderPermyakov
 */
public class BpelMapperModel extends XPathMapperModel implements FilterableMapperModel {

    // filtres
    private Set<Object> inSet = null;
    private Set<Object> outSet = null;

    public BpelMapperModel(MapperTcContext mapperTcContext, 
            GraphChangeProcessor changeProcessor, 
            SoaTreeModel leftModel, BpelExtManagerHolder leftEmh,
            SoaTreeModel rightModel, BpelExtManagerHolder rightEmh) {
        //
		super(mapperTcContext, changeProcessor);
        //
        mLeftTreeModel = new BpelMapperSwingTreeModel(
                mapperTcContext, leftModel, true,
                leftEmh, BpelPathConverter.singleton(),
                BpelFinderListBuilder.singl());
        if (leftEmh != null) {
            leftEmh.attachToTree(mLeftTreeModel);
        }
        //
        mRightTreeModel = new BpelMapperSwingTreeModel(
                mapperTcContext, rightModel, false,
                rightEmh, BpelPathConverter.singleton(),
                BpelFinderListBuilder.singl());
        if (rightEmh != null) {
            rightEmh.attachToTree(mRightTreeModel);
        }
        //
        init();
    }

    @Override
    protected void init() {
        mConnectionConstraints = new ConnectionConstraint[] {
                new ConnectionConstraint.GeneralConstraint(this),
                new BpelConnectionConstraints.PlConstraint(),
                new BpelConnectionConstraints.MVarConstraint()
        };
    }

    @Override
    public MapperTcContext getMapperStaticContext() {
        return (MapperTcContext)mStaticContext;
    }

    @Override
    public BpelMapperSwingTreeModel getRightTreeModel() {
        return (BpelMapperSwingTreeModel)mRightTreeModel;
    }

    @Override
    public BpelMapperSwingTreeModel getLeftTreeModel() {
        return (BpelMapperSwingTreeModel)mLeftTreeModel;
    }

    //==========================================================================
    //   Modification methods
    //==========================================================================

    @Override
    protected boolean isConnectable(TreePath treePath) {
        boolean result = super.isConnectable(treePath);
        if (result) {
            Graph graph = graphRequired(treePath);
            BpelGraphInfoCollector info = new BpelGraphInfoCollector(graph);
            if (info.isXmlLiteral()) {
                return false;
            }
        }
        //
        return result;
    }

    @Override
    protected void copyExtension(GraphSubset graphSubset, Graph graph) {
        if (graphSubset.getVertexCount() == 1) {
            Vertex vertex = graphSubset.getVertex(0);
            if (vertex instanceof Constant && vertex.getItemCount() > 0 && vertex.getItem(0).getDataObject() instanceof XmlLiteralDataObject) {
                graph.addLink(new Link(vertex, graph));
            }
        }
    }
    
    @Override
    protected void moveExtension(GraphSubset graphSubset, Graph oldGraph, Graph graph) {
        if (oldGraph != graph && graphSubset.getVertexCount() == 1) {
            Vertex vertex = graphSubset.getVertex(0);
            if (vertex instanceof Constant && vertex.getItemCount() > 0 &&
                    vertex.getItem(0).getDataObject() instanceof XmlLiteralDataObject) {
                graph.addLink(new Link(vertex, graph));
            }
        }
    }

    @Override
    protected boolean specialDelete(HashSet<Link> linkSet, Graph graph, 
            TreePath currentTreePath) {
        //
        // Provides special delete processing for Literals
        // It deletes leteral vertex in case the connecting link is deleted.
        // It because the literal can't be detached from the owner graph.
        if (linkSet.size() == 1) {
            Link link = linkSet.iterator().next();
            SourcePin source = link.getSource();
            if (link.getTarget() instanceof Graph && source instanceof Constant && 
                    (((Vertex) source).getItemCount() > 0 &&
                    ((Vertex) source).getItem(0).getDataObject() instanceof XmlLiteralDataObject)) {
                //
                link.disconnect();
                graph.removeVertex((Vertex) (source));
                //
                // Initiate graph repaint
                fireGraphChanged(currentTreePath);
                return true;
            }
        }
        //
        return false;
    }

    //===================================================================
    // LSMs support methods
    //===================================================================

    private void collectInputOutputVariables() {
        if (inSet != null) {
            return;
        }
        
        inSet = new HashSet<Object>();
        outSet = new HashSet<Object>();
        
        BpelDesignContextController contextController = getMapperStaticContext()
                .getDesignContextController();
        BpelDesignContext designContext = contextController.getContext();
        if (designContext != null) {
            BpelModel bpelModel = designContext.getBpelModel();
            Process process = bpelModel.getProcess();
            populateOutInVariable(process, inSet, outSet);
        }
    }
    
    private void populateOutInVariable(Process process,
            Set<Object> inPutVariable, Set<Object> outPutVariable)
    {
        List<BpelEntity> children = process.getChildren();
        for (BpelEntity child : children) {
            populateOutInVariable(child, inPutVariable, outPutVariable);
        }
    }
    
    private void populateOutInVariable(BpelEntity entity,
            Set<Object> inPutVariable, Set<Object> outPutVariable) 
    {
        if (entity instanceof Responder) {
            if (entity instanceof VariableReference) {
                if (((VariableReference) entity).getVariable() != null) {
                    inPutVariable.add(((VariableReference) entity).getVariable().get());
                }
            }
            if (entity instanceof Invoke) {
                if (((Invoke) entity).getInputVariable() != null) {
                    outPutVariable.add(((Invoke) entity).getInputVariable().get());
                }
            }
            if (entity instanceof OnEvent) {
                inPutVariable.add(entity);
            }
            if (entity instanceof Catch) {
                inPutVariable.add(entity);
            }
        }
        
        if (entity instanceof Requester) {
            if (entity instanceof Invoke) {
                if (((Invoke) entity).getOutputVariable() != null) {
                    inPutVariable.add(((Invoke) entity).getOutputVariable().get());
                }
            }
            if (entity instanceof VariableReference) {
                if (((VariableReference) entity).getVariable() != null) {
                    outPutVariable.add(((VariableReference) entity).getVariable().get());
                }
            }
        }
        
        List<BpelEntity> children = entity.getChildren();
        if (children == null) {return; }
        
        for (BpelEntity child : children) {
            populateOutInVariable(child, inPutVariable, outPutVariable);
        }
    }
    
    

    public boolean showRight(Object parent, Object node) {
        collectInputOutputVariables();
        
        Object dataObj = ((TreeItem) node).getDataObject();
        
        if (dataObj instanceof VariableDeclarationWrapper) {
            return outSet.contains(((VariableDeclarationWrapper) node).getDelegate());
        }
        if (dataObj instanceof VariableDeclaration) {
            return outSet.contains(dataObj);
        }
        return true;
    }

    public boolean showLeft(Object parent, Object node) {
        collectInputOutputVariables();
        
        Object dataObj = ((TreeItem) node).getDataObject();
        
        if (dataObj instanceof VariableDeclarationWrapper) {
            return inSet.contains(((VariableDeclarationWrapper) node).getDelegate());
        }
        if (dataObj instanceof VariableDeclaration) {
            return inSet.contains(dataObj);
        }
        return true;
    }
    
    public boolean isFromInVariable(Object node) {
        collectInputOutputVariables();
        
        Object dataObj = ((TreeItem) node).getDataObject();
        return inSet.contains(dataObj);
    }
        
    public boolean isFromOutVariable(Object node) {
        collectInputOutputVariables();
        
        Object dataObj = ((TreeItem) node).getDataObject();
        return outSet.contains(dataObj);
    }
}
