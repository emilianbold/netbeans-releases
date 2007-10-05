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

/**
 *
 * @author quynguyen
 */
public class WebServiceNodeChildren extends Children.Keys<WsdlPort> implements WebServiceDataListener {
    private final WebServiceData wsData;
    
    public WebServiceNodeChildren(WebServiceData wsData) {
        this.wsData = wsData;
        wsData.addWebServiceDataListener(this);
    }

    @Override
    protected void addNotify() {
        setKeys(filterNonSoapPorts(wsData.getWsdlService().getPorts()));
        super.addNotify();
    }
    
    @Override
    protected void removeNotify() {
        java.util.Set<WsdlPort> emptySet = Collections.emptySet();
        setKeys(emptySet);
        super.removeNotify();
    }
    
    protected Node[] createNodes(WsdlPort key) {
        if (!wsData.isCompiled()) {
            // start the compilation
            WebServiceManager.getInstance().compileWebService(wsData);
            
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
    
}
