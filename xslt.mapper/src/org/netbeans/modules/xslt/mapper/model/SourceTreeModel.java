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

import java.net.URL;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIModel;
import org.netbeans.modules.xml.axi.AXIModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xslt.mapper.model.nodes.Node;
import org.netbeans.modules.xslt.mapper.model.nodes.NodeFactory;
import org.netbeans.modules.xslt.mapper.model.nodes.TreeNode;
import org.netbeans.modules.xslt.mapper.view.XsltMapper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Alexey
 */
public class SourceTreeModel extends XsltNodesTreeModel {

    private XsltMapper myMapper;
    
    public SourceTreeModel(XsltMapper mapper) {
        myMapper = mapper;
        
        MapperContext context = myMapper.getContext();
        //
        // STUB
        //
        AXIComponent sourceType = null;
        if (context != null) {
            sourceType = context.getSourceType();
        }
        //
//        if (context == null || context.getSourceType() == null) {
//            sourceType = constructFakeComponent();
//        } else {
//            sourceType = context.getSourceType();
//        }
        //
        Node rootNode = NodeFactory.createNode(sourceType, myMapper);
        //
        if (rootNode != null && rootNode instanceof TreeNode) {
            setRootNode((TreeNode)rootNode);
        }
    }

//    public static AXIComponent constructFakeComponent() {
//        AXIComponent sourceType = null;
//        try {
//            //
//            // Load fake model temporary
//            String packageName = SourceTreeModel.class.getPackage().getName();
//            String packagePath = packageName.replace('.', '/');
//            String schemaPath = packagePath + "/" + "OTA_TravelItinerary.xsd"; // NOI18N
//            ClassLoader cl = SourceTreeModel.class.getClassLoader();
//            URL schemaUrl = cl.getResource(schemaPath);
//            FileObject fo = URLMapper.findFileObject(schemaUrl);
//            ModelSource mSource = Utilities.getModelSource(fo, false);
//            SchemaModel schemaModel =
//                    SchemaModelFactory.getDefault().getModel(mSource);
//            AXIModel sourceModel = AXIModelFactory.getDefault().
//                    getModel(schemaModel);
//            sourceType = sourceModel.getRoot().getElements().get(0);
//            //
//        } catch (Exception ex) {
//            // ErrorManager.getDefault().notify(ex);
//            // do nothing here
//        }
//        return sourceType;
//    }
    
}
