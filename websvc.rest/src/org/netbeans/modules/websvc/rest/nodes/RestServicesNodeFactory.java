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
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Milan Kuchtiak
 */
public class RestServicesNodeFactory implements NodeFactory {
    
    /** Creates a new instance of WebServicesNodeFactory */
    public RestServicesNodeFactory() {
    }
    
    public NodeList createNodes(Project p) {
        assert p != null;
        return new RestNodeList(p);
    }
    
    private static class RestNodeList implements NodeList<String> {
        private static final String KEY_SERVICES = "rest_services"; // NOI18N
        private Project project;
        private MetadataModel<RestServicesMetadata> model;
        
        private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
            public void run() {
                fireChange();
            }
        });
        
        private List<ChangeListener> listeners = new ArrayList<ChangeListener>();
        private final RestServicesChangeListener restServicesListener;
        
        public RestNodeList(Project proj) {
            this.project = proj;
            this.model = RestUtils.getRestServicesMetadataModel(project);
            this.restServicesListener = new RestServicesChangeListener();
        }
        
        public List<String> keys() {
            final List<String> result = new ArrayList<String>();
            MetadataModel<RestServicesMetadata> model = RestUtils.getRestServicesMetadataModel(project);
            
            try {
                model.runReadAction(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        RestServices root = metadata.getRoot();
                        
                        if (root.sizeRestServiceDescription() > 0) {
                            result.add(KEY_SERVICES);
                        }
                        
                        return null;
                    }
                });
            } catch (IOException ex) {
                
            }
            
            return result;
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            listeners.add(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            listeners.remove(l);
        }
        
        private void fireChange() {
            ArrayList<ChangeListener> list = new ArrayList<ChangeListener>();
            synchronized (this) {
                list.addAll(listeners);
            }
            Iterator<ChangeListener> it = list.iterator();
            while (it.hasNext()) {
                ChangeListener elem = it.next();
                elem.stateChanged(new ChangeEvent( this ));
            }
        }
        
        public Node node(String key) {
            if (KEY_SERVICES.equals(key)) {
                return new RestServicesNode(project, model);
            }
            return null;
        }
        
        public void addNotify() {
            try {
                model.runReadActionWhenReady(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        metadata.getRoot().addPropertyChangeListener(restServicesListener);
                        
                        return null;
                    }
                });
            } catch (IOException ex) {
                
            }
        }
        
        public void removeNotify() {
            try {
                model.runReadActionWhenReady(new MetadataModelAction<RestServicesMetadata, Void>() {
                    public Void run(RestServicesMetadata metadata) throws IOException {
                        metadata.getRoot().removePropertyChangeListener(restServicesListener);
                        
                        return null;
                    }
                });
            } catch (IOException ex) {
                
            }
        }
        
        
        private final class RestServicesChangeListener implements PropertyChangeListener {
            public void propertyChange(PropertyChangeEvent evt) {
                updateNodeTask.schedule(2000);
            }
        }
    }
    
}
