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
package org.netbeans.modules.websvc.jaxrpc;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.websvc.api.webservices.WebServicesSupport;
import org.netbeans.modules.websvc.api.webservices.WebServicesView;
import org.netbeans.modules.websvc.core.ServiceNodesProvider;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
/**
 *
 * @author rico
 */
public class JaxrpcServiceNodesProvider implements ServiceNodesProvider{
    
    public JaxrpcServiceNodesProvider() {
    }
    
    public Node[] getServiceNodes(Project project) {
        WebServicesSupport jaxrpcWsSupport = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
        
        if(jaxrpcWsSupport != null){
            Sources sources = (Sources)project.getLookup().lookup(Sources.class);
            if (sources!=null) {
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (groups!=null) {
                    List<FileObject> roots = new ArrayList<FileObject>();
                    for (SourceGroup group: groups) {
                        roots.add(group.getRootFolder());
                    }
                    if (jaxrpcWsSupport.getServices().size() >0  && roots.size() > 0) {
                        FileObject srcRoot = roots.get(0);
                        Node servicesNode = WebServicesView.getWebServicesView(srcRoot).createWebServicesView(srcRoot);
                        if(servicesNode != null){
                            return servicesNode.getChildren().getNodes();
                        }
                    }
                }
            }
        }
        return null;
    }
    
}
