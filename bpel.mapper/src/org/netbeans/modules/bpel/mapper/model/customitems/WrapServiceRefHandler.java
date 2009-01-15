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
package org.netbeans.modules.bpel.mapper.model.customitems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import org.netbeans.modules.soa.ui.SoaUtil;
import org.netbeans.modules.bpel.mapper.model.ItemHandler;
import org.netbeans.modules.bpel.mapper.model.VertexFactory;
import org.netbeans.modules.bpel.mapper.multiview.BpelDesignContext;
import org.netbeans.modules.bpel.mapper.multiview.BpelMapperMultiviewElement;
import org.netbeans.modules.bpel.mapper.multiview.BpelMapperMultiviewElementDesc;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.BpelXPathExtFunctionMetadata;
import org.netbeans.modules.soa.mappercore.Mapper;
import org.netbeans.modules.soa.mappercore.model.Constant;
import org.netbeans.modules.soa.mappercore.model.Function;
import org.netbeans.modules.soa.mappercore.model.GraphSubset;
import org.netbeans.modules.soa.mappercore.model.Link;
import org.netbeans.modules.soa.mappercore.model.Vertex;
import org.netbeans.modules.soa.mappercore.model.VertexItem;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.xml.xpath.ext.metadata.ArgumentDescriptor;
import org.netbeans.modules.xml.xpath.ext.metadata.XPathType;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;

/**
 * 
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class WrapServiceRefHandler implements ItemHandler {

    private BpelXPathCustomFunction myItem;
    private static String PATH_TO_WRAP2SERVICEREF = "bpelcustomfunction/wrap2serviceref.xsl"; // NOI18N
    private static String WRAP2SERVICEREF = "wrap2serviceref"; // NOI18N
    private static String WRAP2SERVICEREF_XSL = "wrap2serviceref.xsl"; // NOI18N
    private static String XSLTRANSFORM_URN_PREFIX = "urn:stylesheets:"; // NOI18N
    private static int xStep;
    private static int yStep;
    
    public WrapServiceRefHandler(BpelXPathCustomFunction item) {
        myItem = item;
    }
    
    public Icon getIcon() {
        return myItem.getIcon();
    }

    public String getDisplayName() {
        return myItem.getDisplayName();
    }

    public GraphSubset createGraphSubset() {
        GraphSubset customGraph = null;
        List<Vertex> verteces = new ArrayList<Vertex>();
        List<Link> links = new ArrayList<Link>();
        Function newVertex = null;
        
        // wrap with service ref = configured doXslTransform to perform 
        // wrapping with Service Reference
        VertexFactory vertexFactory = VertexFactory.getInstance();
        newVertex = vertexFactory.createExtFunction(
                BpelXPathExtFunctionMetadata.DO_XSL_TRANSFORM_METADATA);
        assert newVertex != null;
        VertexItem item0 = newVertex.getItem(0);

        Object vertexItemDataObject = item0.getDataObject();
        XPathType arg0Type = null;
        if ( vertexItemDataObject instanceof ArgumentDescriptor) {
            arg0Type = ((ArgumentDescriptor)vertexItemDataObject).getArgumentType();
        }


        assert arg0Type != null && arg0Type.equals(XPathType.STRING_TYPE);
        Constant uriVertex = vertexFactory.createStringLiteral(
                XSLTRANSFORM_URN_PREFIX+WRAP2SERVICEREF_XSL);

        Link ownLink = new Link(uriVertex, item0);

        verteces.add(uriVertex);
        verteces.add(newVertex);
        links.add(ownLink);
        
        if (!verteces.isEmpty()) {
            customGraph = new GraphSubset(null, null, verteces, links);
        }
        
        return customGraph;
    }

    private int getNextMapperX(Mapper mapper) {
        return xStep++;
    }
    
    private int getNextMapperY(Mapper mapper) {
        return yStep++;
    }

    public boolean canAddGraphSubset() {
        // todo m
////        TopComponent bpelMapperTc = WindowManager.getDefault()
////                .findTopComponent(BpelMapperTopComponent.ID);
//////        TopComponent bpelMapperTc = TopComponent.getRegistry().getActivated();
////        
//////////        Mapper mapper = null;
//////////        BpelDesignContext context = null;
//////////        if ( tc instanceof BpelMapperMultiviewElement) {
//////////            mapper = ((BpelMapperMultiviewElement)tc).getMapper();
//////////            context = ((BpelMapperMultiviewElement)tc).
//////////                    getDesignContextController().getContext();
//////////        }
//////////
//////////        BpelModel bpelModel = null;
//////////        if (context != null) {
////////////        if (bpelMapperTc instanceof BpelMapperTopComponent) {
////////////            DesignContextController designContextController = 
////////////                    ((BpelMapperTopComponent)bpelMapperTc).getDesignContextController();
////////////            BpelDesignContext designContext = designContextController == null 
////////////                    ? null : designContextController.getContext();
//////////            bpelModel = context.getBpelModel();
//////////        }
        
        BpelModel bpelModel = getActiveModel();
        
        FileObject bpelFo = SoaUtil.getFileObjectByModel(bpelModel);
        FileObject wrap2servicerefFo = null;
        if (bpelFo != null) {
            wrap2servicerefFo = bpelFo.getParent().getFileObject(WRAP2SERVICEREF_XSL);
            if (wrap2servicerefFo == null) {
                try {
                    wrap2servicerefFo = FileUtil.copyFile(FileUtil.getConfigFile(PATH_TO_WRAP2SERVICEREF),
                            bpelFo.getParent(), WRAP2SERVICEREF); //NOI18N            
                } catch (IOException ex) {
                    ErrorManager.getDefault().notify(ex);
                    return false;
                }
            } 
        }        

        return wrap2servicerefFo != null;
    }
    
    // todo m
    private BpelModel getActiveModel() {
        BpelModel model = null;
        Node[] aNodes = TopComponent.getRegistry().getActivatedNodes();
        Object aRefInstance = null;
        if (aNodes != null && aNodes.length > 0 
                && aNodes[0] instanceof InstanceRef) 
        {
            aRefInstance = ((InstanceRef)aNodes[0]).getReference();
        }
        
        model = aRefInstance instanceof BpelEntity 
                ? ((BpelEntity)aRefInstance).getBpelModel() : null;
        
        return model;
    }
}
