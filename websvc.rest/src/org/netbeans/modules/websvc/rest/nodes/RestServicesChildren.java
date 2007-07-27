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
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.openide.util.RequestProcessor;



public class RestServicesChildren extends Children.Keys {
    private MetadataModel<RestServicesMetadata> model;
    private RestServicesListener listener;
    
    private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public RestServicesChildren(MetadataModel<RestServicesMetadata> model) {
        this.model = model;
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
        final List<String> keys = new ArrayList<String>();
        
        try {
            model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                public Void run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    
                    for (RestServiceDescription desc : root.getRestServiceDescription()) {
                        keys.add(desc.getName());
                    }
                    
                    return null;
                }
            });
        } catch (IOException ex) {
            
        }
        
        setKeys(sortKeys(keys));
    }
    
    protected Node[] createNodes(final Object key) {
        try {
            Node[] nodes = model.runReadAction(new MetadataModelAction<RestServicesMetadata, Node[]>() {
                public Node[] run(RestServicesMetadata metadata) throws IOException {
                    RestServices root = metadata.getRoot();
                    RestServiceDescription desc = root.getRestServiceDescription((String) key);
                    
                    if (desc != null) {
                        return new Node[] {new RestServiceNode(model, desc)};
                    }
                    
                    return new Node[0];
                }
            });
            
            return nodes;
        } catch (IOException ex) {
            
        }
        
        return new Node[0];
    }
    
    private Collection<String> sortKeys(Collection<String> keys) {
        Collection<String> sortedKeys = new TreeSet<String>(
                new Comparator<String> () {
            public int compare(String str1, String str2) {
                return str1.compareTo(str2);
            }
        });
        
        sortedKeys.addAll(keys);
        return sortedKeys;
    }
    
    
    class RestServicesListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(2000);
        }
    }
}
