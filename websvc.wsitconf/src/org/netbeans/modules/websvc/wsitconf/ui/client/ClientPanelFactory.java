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

import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Martin Grebac
 */
public class ClientPanelFactory implements org.netbeans.modules.xml.multiview.ui.InnerPanelFactory {
    
    private ToolBarDesignEditor editor;
    private WSDLModel clientModel;
    private WSDLModel serviceModel;
    private Node node;
    private JaxWsModel jaxwsmodel;
    
    /**
     * Creates a new instance of ClientPanelFactory
     */
    ClientPanelFactory(ToolBarDesignEditor editor, WSDLModel model, Node node, WSDLModel serviceModel, JaxWsModel jxwsmodel) {
        this.editor=editor;
        this.clientModel = model;
        this.serviceModel = serviceModel;
        this.jaxwsmodel = jxwsmodel;
        this.node = node;        
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if (key instanceof String) {
            String id = (String)key;
            if (id.startsWith(ClientView.CALLBACK_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.CALLBACK_NODE_ID.length()));
                return new CallbackPanel((SectionView) editor.getContentView(), node, b, jaxwsmodel, serviceModel);
            }
            if (id.startsWith(ClientView.STS_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.STS_NODE_ID.length()));
                return new STSClientPanel((SectionView) editor.getContentView(), node, b, jaxwsmodel);
            }
            if (id.startsWith(ClientView.TRANSPORT_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.TRANSPORT_NODE_ID.length()));
                return new TransportPanelClient((SectionView) editor.getContentView(), node, b, jaxwsmodel);
            }
            if (id.startsWith(ClientView.ADVANCEDCONFIG_NODE_ID)) {
                Binding b = PolicyModelHelper.getBinding(clientModel, id.substring(ClientView.ADVANCEDCONFIG_NODE_ID.length()));
                return new AdvancedConfigPanelClient((SectionView) editor.getContentView(), b, serviceModel);
            }
        }
        return null;
    }

}
