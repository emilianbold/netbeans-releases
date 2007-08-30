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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.websvc.manager.nodes;

import java.util.Collections;
import java.util.List;
import org.netbeans.modules.websvc.manager.api.WebServiceManager;
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
            Runnable compile = new Runnable() {
                public void run() {
                    WebServiceManager.getInstance().compileWebService(wsData);
                }
            };
            WebServiceManager.getInstance().getCompilationRequestProcessor().post(compile);
            
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
