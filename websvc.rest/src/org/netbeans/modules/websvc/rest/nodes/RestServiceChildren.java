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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.openide.util.RequestProcessor;



public class RestServiceChildren extends Children.Keys {
    private MetadataModel<RestServicesMetadata> model;
    private String serviceName;
  
    private static final String KEY_HTTP_METHODS = "http_methods";  //NOI18N
    private static final String KEY_SUB_RESOURCE_LOCATORS = "sub_resource_locators";        //NOI18N
    
    public RestServiceChildren(MetadataModel<RestServicesMetadata> model, String serviceName) {
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
            return new Node[] { new HttpMethodsNode(model, serviceName) };
        } else if (key.equals(KEY_SUB_RESOURCE_LOCATORS)) {
            return new Node[] { new SubResourceLocatorsNode(model, serviceName) };
        }
        
        return new Node[0];
    }
}
