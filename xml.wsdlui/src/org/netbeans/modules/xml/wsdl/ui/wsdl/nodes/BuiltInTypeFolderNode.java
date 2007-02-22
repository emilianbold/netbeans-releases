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

package org.netbeans.modules.xml.wsdl.ui.wsdl.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.CategorizedSchemaNodeFactory;
import org.netbeans.modules.xml.schema.ui.nodes.categorized.PrimitiveSimpleType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.FolderNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;



/**
 *
 * @author radval
 *
 */
public class BuiltInTypeFolderNode extends AbstractNode {
    
    private static final Image ICON  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/simpleType_badge.png");
    
    
    public BuiltInTypeFolderNode() {
        super(new BuiltInTypeNodeChildren(SchemaModelFactory.getDefault().getPrimitiveTypesModel()));
        this.setName(NbBundle.getMessage(BuiltInTypeFolderNode.class, "BUILTIN_SCHEMATYPE_NAME"));
        this.setDisplayName(NbBundle.getMessage(BuiltInTypeFolderNode.class, "BUILTIN_SCHEMATYPE_NAME"));
        this.setShortDescription(NbBundle.getMessage(BuiltInTypeFolderNode.class, "BUILTIN_SCHEMATYPE_NAME"));
        
    }
    
    @Override
    public Image getIcon(int type) {
        Image icon = FolderNode.FolderIcon.getClosedIcon();
        return Utilities.mergeImages(icon, ICON, 8, 8);
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        Image icon = FolderNode.FolderIcon.getOpenedIcon();
        return Utilities.mergeImages(icon, ICON, 8, 8);
    }
    
    public static class BuiltInTypeNodeChildren extends Children.Keys {
        SchemaModel model = null;
        
        BuiltInTypeNodeChildren(SchemaModel model) {
            this.model = model;
        }
        
        @Override
        protected Node[] createNodes(Object key) {
            List<Class<? extends SchemaComponent>> filters = new ArrayList<Class<? extends SchemaComponent>>();
            filters.add(PrimitiveSimpleType.class);
            CategorizedSchemaNodeFactory factory = new CategorizedSchemaNodeFactory(
                    model, filters, Lookup.EMPTY);
            Node node = factory.createNode((GlobalSimpleType) key);
            return new Node[] { new ChildLessNode(node) };
        }
        
        
        @Override
        protected void addNotify() {
            resetKeys();
        }
        
        @Override
        protected void removeNotify() {
            this.setKeys(Collections.EMPTY_SET);
            
        }
        
        @SuppressWarnings("unchecked")
        private void resetKeys() {
            ArrayList keys = new ArrayList();
            keys.addAll(model.getSchema().getSimpleTypes());
            this.setKeys(keys);
        }
        
        @Override
        public boolean remove(final Node[] arr) {
            return super.remove(arr);
        }
    }
    
    private static class ChildLessNode extends FilterNode {

        public ChildLessNode(Node node) {
            super(node, Children.LEAF);
        }
    }
}

