/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.io.IOException;

import org.netbeans.modules.xml.schema.ui.nodes.SchemaComponentNode;
import org.netbeans.modules.xml.wsdl.model.Types;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.xsd.WSDLSchema;
import org.netbeans.modules.xml.wsdl.ui.view.DesignGotoType;
import org.netbeans.modules.xml.wsdl.ui.view.StructureGotoType;
import org.netbeans.modules.xml.xam.ui.actions.GotoType;
import org.netbeans.modules.xml.xam.ui.actions.SourceGotoType;
import org.netbeans.modules.xml.xam.ui.actions.SuperGotoType;
import org.netbeans.modules.xml.xam.ui.cookies.GotoCookie;
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
    
    public EmbeddedSchemaNode(Node node, WSDLSchema schema, InstanceContent content) {
        super(node, new EmbeddedSchemaChildren(node), new ProxyLookup(new Lookup[] {new AbstractLookup(content), node.getLookup()}));
        component = schema;
        content.add(schema);
        content.add(new GotoCookie() {
            public GotoType[] getGotoTypes() {

                return GOTO_TYPES;
            }

        });
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
        
        
        public EmbeddedSchemaChildren(Node or) {
            super(or);
        }
        
        @Override
        protected Node copyNode(Node origNode) {
            InstanceContent content = new InstanceContent();
            Node node =  new EmbeddedReadOnlySchemaComponentNode(origNode, new EmbeddedSchemaChildren(origNode), new ProxyLookup(new Lookup[] {new AbstractLookup(content), origNode.getLookup()}));
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
