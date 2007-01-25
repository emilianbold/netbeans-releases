/*
 * SourceTreeModel.java
 *
 * Created on 19 Декабрь 2006 г., 19:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
        if (context == null || context.getSourceType() == null) {
            sourceType = constructFakeComponent();
        } else {
            sourceType = context.getSourceType();
        }
        //
        Node rootNode = NodeFactory.createNode(sourceType, myMapper);
        //
        assert rootNode instanceof TreeNode;
        setRootNode((TreeNode)rootNode);
    }

    public static AXIComponent constructFakeComponent() {
        AXIComponent sourceType = null;
        try {
            //
            // Load fake model temporary
            String packageName = SourceTreeModel.class.getPackage().getName();
            String packagePath = packageName.replace('.', '/');
            String schemaPath = packagePath + "/" + "OTA_TravelItinerary.xsd"; // NOI18N
            ClassLoader cl = SourceTreeModel.class.getClassLoader();
            URL schemaUrl = cl.getResource(schemaPath);
            FileObject fo = URLMapper.findFileObject(schemaUrl);
            ModelSource mSource = Utilities.getModelSource(fo, false);
            SchemaModel schemaModel =
                    SchemaModelFactory.getDefault().getModel(mSource);
            AXIModel sourceModel = AXIModelFactory.getDefault().
                    getModel(schemaModel);
            sourceType = sourceModel.getRoot().getElements().get(0);
            //
        } catch (Exception ex) {
            // ErrorManager.getDefault().notify(ex);
            // do nothing here
        }
        return sourceType;
    }
    
}
