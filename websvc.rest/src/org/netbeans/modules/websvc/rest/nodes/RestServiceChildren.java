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
package org.netbeans.modules.websvc.rest.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.Project;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;


public class RestServiceChildren extends Children.Keys {
    private Project project;
    private MetadataModel<RestServicesMetadata> model;
    private String serviceName;
  
    private static final String KEY_HTTP_METHODS = "http_methods";  //NOI18N
    private static final String KEY_SUB_RESOURCE_LOCATORS = "sub_resource_locators";        //NOI18N
    
    public RestServiceChildren(Project project, MetadataModel<RestServicesMetadata> model, 
            String serviceName) {
        this.project = project;
        this.model = model;
        this.serviceName = serviceName;
    }
    
    protected void addNotify() {
        super.addNotify();
 
        updateKeys();
    }
    
    protected void removeNotify() {
        super.removeNotify();
        
        setKeys(Collections.EMPTY_SET);
    }
    
    private void updateKeys() {
        final List keys = new ArrayList();
        keys.add(KEY_HTTP_METHODS);
        keys.add(KEY_SUB_RESOURCE_LOCATORS);
        
        setKeys(keys);
    }
    
    protected Node[] createNodes(final Object key) {
        if (key.equals(KEY_HTTP_METHODS)) {
            return new Node[] { new HttpMethodsNode(project, model, serviceName) };
        } else if (key.equals(KEY_SUB_RESOURCE_LOCATORS)) {
            return new Node[] { new SubResourceLocatorsNode(project, model, serviceName) };
        }
        
        return new Node[0];
    }
}
