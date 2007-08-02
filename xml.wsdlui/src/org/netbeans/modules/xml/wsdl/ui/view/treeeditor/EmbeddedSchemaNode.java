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
import java.util.List;

import javax.swing.Action;

import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.view.DesignGotoType;
import org.netbeans.modules.xml.wsdl.ui.view.StructureGotoType;
import org.netbeans.modules.xml.xam.ui.actions.GoToAction;
import org.netbeans.modules.xml.xam.ui.actions.GotoType;
import org.netbeans.modules.xml.xam.ui.actions.SourceGotoType;
import org.netbeans.modules.xml.xam.ui.actions.SuperGotoType;
import org.netbeans.modules.xml.xam.ui.cookies.GotoCookie;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

public class EmbeddedSchemaNode extends FilterNode {

    private WSDLSchema component;
    
    private static final GotoType[] GOTO_TYPES = new GotoType[] {
        new SourceGotoType(),
        new StructureGotoType(),
        new DesignGotoType(),
        new SuperGotoType(),
    };
    
    public EmbeddedSchemaNode(Node node, WSDLSchema schema, InstanceContent content, List objList) {
        super(node, new EmbeddedSchemaChildren(node, objList), new ProxyLookup(new Lookup[] {new AbstractLookup(content), node.getLookup()}));
        component = schema;
        if (objList != null) {
            for (Object obj : objList) {
                content.add(obj);
            }
            content.add(schema);
            content.add(new GotoCookie() {
			
				public GotoType[] getGotoTypes() {

					return GOTO_TYPES;
				}
			
			});
        }
    }

    @Override
    public boolean canDestroy() {
        WSDLModel model = component.getModel();
        return model != null && model.getModelSource().isEditable();
    }

    @Override
    public void destroy() throws IOException {
        SchemaComponentNode scn = getOriginal().getCookie(SchemaComponentNode.class);
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
        
        private List objList;
        
        public EmbeddedSchemaChildren(Node or, List objList) {
            super(or);
            this.objList = objList;
        }
        
        @Override
        protected Node copyNode(Node origNode) {
            InstanceContent content = new InstanceContent();
            Node node =  new EmbeddedReadOnlySchemaComponentNode(origNode, new EmbeddedSchemaChildren(origNode, objList), new ProxyLookup(new Lookup[] {new AbstractLookup(content), origNode.getLookup()}));
            if (objList != null) {
                for (Object obj : objList) {
                    content.add(obj);
                }
            }
            return node;
            
        }
    }
    
    static class EmbeddedReadOnlySchemaComponentNode extends FilterNode {
		
		public EmbeddedReadOnlySchemaComponentNode(Node original,
				Children children, Lookup lookup) {
			super(original, children, lookup);
		}

		@Override
		public boolean canCopy() {
			return true;
		}
    	
    }
}
