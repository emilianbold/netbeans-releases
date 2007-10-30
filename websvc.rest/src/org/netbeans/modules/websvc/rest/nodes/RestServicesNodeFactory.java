/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.websvc.rest.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
            if (model == null) {
                return Collections.emptyList();
            }
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
                if (model == null) {
                    return;
                }
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
                if (model == null) {
                    return;
                }
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
