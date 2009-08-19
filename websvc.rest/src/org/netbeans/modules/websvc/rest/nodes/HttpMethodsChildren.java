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

import org.netbeans.modules.websvc.rest.support.Utils;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.websvc.rest.model.api.HttpMethod;
import org.netbeans.modules.websvc.rest.model.api.RestMethodDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServiceDescription;
import org.netbeans.modules.websvc.rest.model.api.RestServices;
import org.netbeans.modules.websvc.rest.model.api.RestServicesMetadata;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;



public class HttpMethodsChildren extends Children.Keys {
    private Project project;
    private RestServicesModel model;
    private String serviceName;
    private RestServicesListener listener;
    
    private RequestProcessor.Task updateNodeTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            updateKeys();
        }
    });
    
    public HttpMethodsChildren(Project project,RestServicesModel model, 
            String serviceName) {
        this.project = project;
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
            Exceptions.printStackTrace(ex);
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
            Exceptions.printStackTrace(ex);
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
                            if (method instanceof HttpMethod) {
                                keys.add(HttpMethodNode.getKey((HttpMethod) method));
                            }
                        }
                    }
                    
                    return null;
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
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
                            if (method instanceof HttpMethod) {
                                if (HttpMethodNode.getKey((HttpMethod) method).equals(key)) {
                                    return new Node[] {
                                        new HttpMethodNode(project, desc,(HttpMethod) method)};
                                }
                            }
                        }
                    }
                    return new Node[0];
                }
            });
            
            return nodes;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return new Node[0];
    }
  
    class RestServicesListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            updateNodeTask.schedule(0);
        }
    }
}
