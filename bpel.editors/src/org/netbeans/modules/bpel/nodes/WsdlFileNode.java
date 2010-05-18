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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.model.api.support.Utils;
import org.netbeans.modules.bpel.properties.ResolverUtility;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * @author ads
 *
 */
public class WsdlFileNode extends BpelNode<WSDLModel> {
    
    public WsdlFileNode(WSDLModel wsdlModel, Lookup lookup) {
        super(wsdlModel, new MessageTypeChildren(wsdlModel, lookup), lookup);
    }
    
    public WsdlFileNode(WSDLModel wsdlModel, Children children,  Lookup lookup) {
        super(wsdlModel, children, lookup);
    }
    
    public NodeType getNodeType() {
        return NodeType.WSDL_FILE;
    }
    
    @Override
    protected String getNameImpl() {
        WSDLModel ref = getReference();
        if (ref == null) {
            return null;
        }
        FileObject fo = ref.getModelSource().getLookup().lookup(FileObject.class);
        if (fo == null || !fo.isValid()) {
            return null;
        }
        Project modelProject = Utils.safeGetProject(ref);
        String relativePath = ResolverUtility.safeGetRelativePath(fo, modelProject);
        
        return relativePath != null ? relativePath : fo.getPath();
    }
    
    static class MessageTypeChildren extends Children.Keys {
        
        private Lookup myLookup;
        
        @SuppressWarnings("unchecked")
        public MessageTypeChildren(WSDLModel wsdlModel, Lookup lookup) {
            myLookup = lookup;
            setKeys(new Object[] {wsdlModel});
        }
        
        /* (non-Javadoc)
         * @see org.openide.nodes.Children.Keys#createNodes(java.lang.Object)
         */
        protected Node[] createNodes( Object key ) {
            if (key instanceof WSDLModel){
                List<Node> list = new ArrayList<Node>();
                Collection<Message> messages =
                        ((WSDLModel)key).getDefinitions().getMessages();
                for (Message message : messages) {
                    Node newNode = new MessageTypeNode(message, myLookup);
                    list.add(newNode);
                }
                //
                return list.toArray(new Node[list.size()]);
            }
            return null;
        }
    }
}
