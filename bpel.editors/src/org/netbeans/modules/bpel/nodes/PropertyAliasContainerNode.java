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
package org.netbeans.modules.bpel.nodes;

import org.netbeans.modules.bpel.nodes.BpelNode;
import java.util.List;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.extensions.bpel.PropertyAlias;

/**
 *
 * @author nk160297
 */
public class PropertyAliasContainerNode extends BpelNode<WSDLModel> {

    public PropertyAliasContainerNode(WSDLModel wsdlModel, Lookup lookup) {
        super(wsdlModel, new MyChildren(lookup), lookup);
        reload();
    }
    
    public NodeType getNodeType() {
        return NodeType.VARIABLE_CONTAINER;
    }
    
    public void reload() {
        MyChildren children = (MyChildren)getChildren();
        children.reload();
    }
    
    private static class MyChildren extends Children.Keys {
        
        private Lookup lookup;
        
        public MyChildren(Lookup lookup) {
            super();
            this.lookup = lookup;
            // reload();
        }
        
        protected Node[] createNodes(Object key) {
            assert key instanceof PropertyAlias;
            //
            return new Node[] {new PropertyAliasNode((PropertyAlias) key, lookup)};
        }
        
        public void reload()  {
            WSDLModel wsdlModel = ((BpelNode<WSDLModel>)getNode()).getReference();
            if (wsdlModel == null) {
                return;
            }
            
            List<PropertyAlias> cpList = 
                    wsdlModel.getDefinitions().
                    getExtensibilityElements(PropertyAlias.class);
            setKeys(cpList);
        }
    }
}
