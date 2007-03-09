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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

public class EmbeddedSchemaNode extends FilterNode {

    private WSDLSchema component;
    
    public EmbeddedSchemaNode(Node node, WSDLSchema schema, Lookup lookup) {
        super(node, new EmbeddedSchemaChildren(node, lookup), lookup);
        component = schema;
    }

    @Override
    public boolean canDestroy() {
        WSDLModel model = component.getModel();
        return model != null && model.getModelSource().isEditable();
    }

    @Override
    public void destroy() throws IOException {
        SchemaComponentNode scn = (SchemaComponentNode) getOriginal().
                getCookie(SchemaComponentNode.class);
        if (scn != null) {
            // Let the schema node do its cleanup.
            scn.destroy();
        }
        // Remove the schema root from the WSDL model.
        WSDLModel model = component.getModel();
        Types types = model.getDefinitions().getTypes();
        model.startTransaction();
        types.removeExtensibilityElement(component);
            model.endTransaction();
        super.destroy();
    }
    

    
    static class EmbeddedSchemaChildren extends FilterNode.Children {
        
        private Lookup parentLookup;
        
        public EmbeddedSchemaChildren(Node or, Lookup lookup) {
            super(or);
            parentLookup = lookup;
        }
        
        @Override
        protected Node[] createNodes(Node n) {
            Node[] mynodes = super.createNodes(n);
            List<Node> list = new ArrayList<Node>();
            for (Node node : mynodes) {
                list.add(new FilterNode(node, new EmbeddedSchemaChildren(node, parentLookup), parentLookup));
            }
            return list.toArray(new Node[list.size()]);
        }
    }
}
