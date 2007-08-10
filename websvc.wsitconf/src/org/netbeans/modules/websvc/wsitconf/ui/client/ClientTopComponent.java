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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.ui.client;

import java.util.Collection;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import java.awt.*;
import java.util.Iterator;

/**
 * @author Martin Grebac
 */
public class ClientTopComponent extends TopComponent {

    static final long serialVersionUID=6021472310161712674L;
    private boolean initialized = false;
    private InnerPanelFactory panelFactory = null;

    private JaxWsModel jaxWsModel;
    private WSDLModel  clientWsdlModel;
    private WSDLModel  serviceModel;
    private Client client;
    private Node node;
    
    public ClientTopComponent(Client client, JaxWsModel jaxWsModel, WSDLModel clientWsdlModel, WSDLModel serviceWsdlModel, Node node) {
        setLayout(new BorderLayout());
        this.jaxWsModel = jaxWsModel;
        this.clientWsdlModel = clientWsdlModel;
        this.serviceModel = serviceWsdlModel;
        this.initialized = false;
        this.client = client;
        this.node = node;
    }
    
    @Override
    protected String preferredID(){
        return "WSITClientTopComponent";    //NOI18N
    }

    private org.netbeans.modules.xml.wsdl.model.Service getService(String name, WSDLModel m) {
        if ((name != null) && (m != null)) {
            Collection services = m.getDefinitions().getServices();
            if (services != null) {
                Iterator i = services.iterator();
                org.netbeans.modules.xml.wsdl.model.Service s = null;
                while (i.hasNext()) {
                    s = (org.netbeans.modules.xml.wsdl.model.Service)i.next();
                    if ((s != null) && ((name.equals(s.getName())) || (services.size() == 1))) {
                        return s;
                    }
                }
            }
        }    
        return null;
    }
    
    /**
     * #38900 - lazy addition of GUI components
     */
    private void doInitialize() {
        initAccessibility();

        ToolBarDesignEditor tb = new ToolBarDesignEditor();
        panelFactory = new ClientPanelFactory(tb, clientWsdlModel, node, serviceModel, jaxWsModel);
        
        org.netbeans.modules.xml.wsdl.model.Service s = getService(client.getName(), clientWsdlModel); //TODO - the client name just won't work!!!
        if (s != null) {
            ClientView mview = new ClientView(panelFactory, clientWsdlModel, serviceModel, s);
            tb.setContentView(mview);
            add(tb);
        }
        setFocusable(true);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }
    
    private void initAccessibility(){
        getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(ClientTopComponent.class, "ACS_Tab_DESC")); // NOI18N
    }

    /**
     * #38900 - lazy addition of GUI components
     */    
    @Override
    public void addNotify() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.addNotify();
    }
    
    /**
     * Called when <code>TopComponent</code> is about to be shown.
     * Shown here means the component is selected or resides in it own cell
     * in container in its <code>Mode</code>. The container is visible and not minimized.
     * <p><em>Note:</em> component
     * is considered to be shown, even its container window
     * is overlapped by another window.</p>
     * @since 2.18
     *
     * #38900 - lazy addition of GUI components
     *
     */
    @Override
    protected void componentShowing() {
        if (!initialized) {
            initialized = true;
            doInitialize();
        }
        super.componentShowing();
    }
    
}

