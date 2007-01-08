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

/*
 * WebServiceNodeManager.java
 *
 * Created on August 3, 2005, 1:36 PM
 *
 */
package org.netbeans.modules.mobility.end2end.util;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.client.WebServicesClientSupport;
import org.netbeans.modules.websvc.api.client.WebServicesClientView;
//import org.netbeans.modules.websvc.core.ServiceInformation;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;

/**
 *
 * @author suchys
 */
public final class WebServiceNodeManager {
    
    
    public static Node getAvailableWSRootNode(final Project p, final String wsdlFile){
        final WebServicesClientSupport wscs = WebServicesClientSupport.getWebServicesClientSupport(p.getProjectDirectory());
        final FileObject rootFolder = wscs.getWsdlFolder();
        final WebServicesClientSupport clientSupport  = WebServicesClientSupport.getWebServicesClientSupport(rootFolder);
        final WebServicesClientView clientView = WebServicesClientView.getWebServicesClientView(rootFolder);
        
        final Node clientRoot = clientView.createWebServiceClientView(clientSupport.getWsdlFolder());
        return new FilterNode(clientRoot, new WsFilteredChildren(clientRoot, wsdlFile));
    }
    
    private static class WsFilteredChildren extends FilterNode.Children{
        final private String name;
        
        public WsFilteredChildren(Node original, String name){
            super(original);
            this.name = name;
        }
        
        /** Create nodes representing copies of the original node's children.
         * The default implementation returns exactly one representative for each original node,
         * as returned by {@link #copyNode}.
         * Subclasses may override this to avoid displaying a copy of an original child at all,
         * or even to display multiple nodes representing the original.
         * @param key the original child node
         * @return zero or more nodes representing the original child node
         */
        protected Node[] createNodes(final Node n) {
//            final ServiceInformation si = (ServiceInformation)n.getCookie(ServiceInformation.class);
//            if (si != null){
//                if (name.equals(((DataObject)n.getCookie(DataObject.class)).getPrimaryFile().getNameExt())){
//                    // is run under read access lock so nobody can change children
//                    return new Node[] { copyNode(n) };
//                }
//            }
            return new Node[0];
        }
        
    }
}
