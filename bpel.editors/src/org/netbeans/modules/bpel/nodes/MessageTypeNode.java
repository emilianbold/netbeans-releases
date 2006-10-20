/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.bpel.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.bpel.design.nodes.NodeType;
import org.netbeans.modules.bpel.properties.Constants;
import org.netbeans.modules.bpel.properties.Constants.VariableStereotype;
import org.netbeans.modules.bpel.properties.PropertyNodeFactory;
import org.netbeans.modules.bpel.properties.props.PropertyUtils;
import org.openide.nodes.Sheet;
import org.openide.util.Lookup;
import static org.netbeans.modules.bpel.properties.PropertyType.*;
import org.netbeans.modules.bpel.properties.editors.controls.filter.NodeChildFilter;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author nk160297
 */
public class MessageTypeNode extends BpelNode<Message> {
    
    public MessageTypeNode(Message message, Children children, Lookup lookup) {
        super(message, children, lookup);
    }
    
    public MessageTypeNode(Message message, Lookup lookup) {
        super(message, lookup);
        //
        initChildren();
    }
    
    private void initChildren() {
        if (isChildrenAllowed()) {
            Children.MUTEX.postWriteRequest(new Runnable() {
                public void run() {
                    setChildren(new MyChildren());
                }
            });
        }
    }
    
    public NodeType getNodeType() {
        return NodeType.MESSAGE_TYPE;
    }
    
    public Image getIcon(int type) {
        return getNodeType().getImage();
    }
    
    public String getDisplayName(){
        Message ref = getReference();
        return ref != null ? getReference().getName() : null; 
    }
    
    public VariableStereotype getStereotype() {
        return VariableStereotype.MESSAGE;
    }
    
    protected Sheet createSheet() {
        Sheet sheet = super.createSheet();
        //
        Sheet.Set mainPropertySet = 
                getPropertySet(sheet, Constants.PropertiesGroups.MAIN_SET);
        //
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                NAME, "getName", null); // NOI18N
        PropertyUtils.registerCalculatedProperty(this, mainPropertySet,
                VARIABLE_STEREOTYPE, "getStereotype", null); // NOI18N
        return sheet;
    }
    
    private class MyChildren extends Children.Keys {
        
        public MyChildren() {
            super();
            // It's necesary to specify any key. Otherwise it doesn't work.
            setKeys(new Object[] {new Object()});
        }
        
        protected Node[] createNodes(Object key) {
            //
            // Variable has a type related nodes as a child.
            Node parentNode = getNode();
            if (parentNode != null) {
                assert parentNode instanceof MessageTypeNode;
                Message message = ((MessageTypeNode)parentNode).getReference();
                if (message == null) {
                    return new Node[0];
                }
                Lookup lookup = parentNode.getLookup();
                //
                NodeChildFilter filter = (NodeChildFilter)getLookup().
                        lookup(NodeChildFilter.class);
                //
                Collection<Part> parts = message.getParts();
                ArrayList<Node> nodesList = new ArrayList<Node>();
                for (Part part : parts) {
                    Node node = PropertyNodeFactory.getInstance()
                            .createNode(NodeType.MESSAGE_PART, part, null, 
                            lookup);
                    // Node node = new MessagePartNode(part, lookup);
                    if (filter == null ||
                            filter.isPairAllowed(getNode(), node)) {
                        nodesList.add(node);
                    }
                }
                Node[] nodesArr = nodesList.toArray(new Node[nodesList.size()]);
                return nodesArr;
            }
            return null;
        }
    }
}
