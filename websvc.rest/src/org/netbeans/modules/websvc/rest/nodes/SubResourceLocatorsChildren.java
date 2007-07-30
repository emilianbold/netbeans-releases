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
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.SubResourceLocator;
import org.openide.util.RequestProcessor;



public class SubResourceLocatorsChildren extends Children.Keys {
    private MetadataModel<RestServicesMetadata> model;
    private String serviceName;
    private RestServicesListener listener;
    
    private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public SubResourceLocatorsChildren(MetadataModel<RestServicesMetadata> model, String serviceName) {
        this.model = model;
        this.serviceName = serviceName;
    }
    
    protected void addNotify() {
        super.addNotify();
        listener = new RestServicesListener();
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(RestServicesMetadata metadata) throws IOException {
                    metadata.getRoot().addPropertyChangeListener(listener);
                    
                    return null;
                }
            });
        } catch (IOException ex) {
            
        }
        
        updateKeys();
    }
    
    protected void removeNotify() {
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(RestServicesMetadata metadata) throws IOException {
                    metadata.getRoot().removePropertyChangeListener(listener);
                    
                    return null;
                }
            });
        } catch (IOException ex) {
            
        }
        
        setKeys(Collections.EMPTY_SET);
    }
    
    private void updateKeys() {
        final List keys = new ArrayList();
        
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    RestServiceDescription desc = root.getRestServiceDescription(serviceName);
                    
                    if (desc != null) {
                        for (RestMethodDescription method : desc.getMethods()) {
                            if (method instanceof SubResourceLocator) {
                                keys.add(method.getName());
                            }
                        }
                    }
                    
                    return null;
                }
            });
        } catch (IOException ex) {
            
        }
        
        setKeys(Utils.sortKeys(keys));
    }
    
    protected Node[] createNodes(final Object key) {
        try {
            Node[] nodes = model.runReadAction(new MetadataModelAction<RestServicesMetadata, Node[]>() {
                public Node[] run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    RestServiceDescription desc = root.getRestServiceDescription(serviceName);
                    
                    if (desc != null) {
                        for (RestMethodDescription method : desc.getMethods()) {
                            if (method instanceof SubResourceLocator) {
                                if (method.getName().equals(key)) {
                                    return new Node[] { new SubResourceLocatorNode(model, (SubResourceLocator) method) };
                                }
                            }
                        }
                    }
                    return new Node[0];
                }
            });
            
            return nodes;
        } catch (IOException ex) {
            
        }
        
        return new Node[0];
    }
   
    class RestServicesListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }
    }
}
