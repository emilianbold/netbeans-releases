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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.manager.WebServiceManager;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceDataEvent;
import org.netbeans.modules.websvc.manager.model.WebServiceDataListener;
import org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlPort;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author quynguyen
 */
public class WebServiceNodeChildren extends Children.Keys<WsdlPort> implements WebServiceDataListener, PropertyChangeListener {
    private final WebServiceData wsData;
    
    public WebServiceNodeChildren(WebServiceData wsData) {
        this.wsData = wsData;
        wsData.addWebServiceDataListener(WeakListeners.create(WebServiceDataListener.class, this, wsData));
        wsData.addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, this, wsData));
    }

    @Override
    protected void addNotify() {
        boolean resolved = wsData.isResolved();
        if (resolved) {
            setKeys(filterNonSoapPorts(wsData.getWsdlService().getPorts()));
        } else {
            setKeys(new ArrayList<WsdlPort>());
        }
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        java.util.Set<WsdlPort> emptySet = Collections.emptySet();
        setKeys(emptySet);
        super.removeNotify();
    }
    
    protected Node[] createNodes(WsdlPort key) {
        if (!wsData.isResolved()) {
            return new Node[0];
        }else if (!wsData.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILED)) {

            final WebServiceManager manager = WebServiceManager.getInstance();
            final WebServiceData data = wsData;
            
            if (!data.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILING)) {
                Runnable compileThread = new Runnable() {
                    public void run() {
                        try {
                            WebServiceManager.compileService(data);
                        } finally {
                            if (!data.getState().equals(WebServiceData.State.WSDL_SERVICE_COMPILED)) {
                                manager.removeWebService(data);
                            }
                        }
                    }
                };
                
                manager.getRequestProcessor().post(compileThread);
            }
            
            AbstractNode waitNode = new AbstractNode(Children.LEAF);
            waitNode.setName(NbBundle.getMessage(WebServiceGroupNodeChildren.class, "NODE_LOAD_MSG"));
            waitNode.setIconBaseWithExtension("org/netbeans/modules/websvc/manager/resources/wait.gif"); // NOI18N
            
            return new Node[] { waitNode };
        }
        
        return new Node[] { new WebServicesPortNode(wsData, key) };
    }

    public void webServiceCompiled(WebServiceDataEvent evt) {
        setKeys(filterNonSoapPorts(wsData.getWsdlService().getPorts()));
    }
  
    private List<WsdlPort> filterNonSoapPorts(List<WsdlPort> ports) {
        List<WsdlPort> filterPorts = new java.util.ArrayList<WsdlPort>(ports.size());
        
        for (WsdlPort port : ports) {
            if (port.getAddress() != null) {
                filterPorts.add(port);
            }
        }
        
        return filterPorts;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("resolved")) { //NOI18N
            Object newValue = evt.getNewValue();
            if (newValue instanceof Boolean) {
                boolean resolved = ((Boolean) newValue).booleanValue();
                if (resolved) {
                    setKeys(filterNonSoapPorts(wsData.getWsdlService().getPorts()));
                } else {
                    setKeys(new ArrayList<WsdlPort>());
                }

            }
            
        }
    }
}
