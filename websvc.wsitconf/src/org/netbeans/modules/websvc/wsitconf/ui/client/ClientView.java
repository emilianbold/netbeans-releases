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

import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.namespace.QName;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.AdvancedConfigurationClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.CallbackClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.BindingContainerClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.KeystoreClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.STSClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.TransportClientNode;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RequiredConfigurationHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

import java.util.Collection;

/**
 * @author Martin Grebac
 */
public class ClientView extends SectionView {

    static final String KEYSTORE_NODE_ID = "keystore";                          //NOI18N
    static final String CALLBACK_NODE_ID = "callback";                          //NOI18N
    static final String STS_NODE_ID = "sts";                                    //NOI18N
    static final String TRANSPORT_NODE_ID = "transpotr";                        //NOI18N
    static final String ADVANCEDCONFIG_NODE_ID = "advancedconfig";              //NOI18N

    private SectionNode rootNode = null;
    
    private static final int REFRESH_DELAY = 40;
            
    private WSDLModel clientModel = null;
    private WSDLModel serviceModel = null;
    private Service service = null;
    private Node node = null;
    private Project project = null;
    
    ClientView(InnerPanelFactory factory, WSDLModel clientModel, WSDLModel serviceModel, Service s, Project project, Node node) {
    
        super(factory);
        this.clientModel = clientModel;
        this.serviceModel = serviceModel;
        this.service = s;
        this.node = node;
        this.project = project;

        //create root node
        Children rootChildren = new Children.Array();
        Node root = new AbstractNode(rootChildren);

        Collection<Binding> bindings = new HashSet();
        WSITModelSupport.fillImportedBindings(clientModel, bindings, new HashSet());
        
        Collection<Port> ports = s.getPorts();
        for (Port p : ports) {
            QName bqname = p.getBinding().getQName();
            Binding b = clientModel.findComponentByName(bqname, Binding.class);
            bindings.add(b);
        }

        ArrayList bindingNodes = new ArrayList();
        if (bindings.size() > 1) {
            for (Binding binding : bindings) {
                ArrayList nodes = new ArrayList();

                // main node container for a specific binding
                Children bindingChildren = new Children.Array();
                Node bindingNodeContainer = new BindingContainerClientNode(bindingChildren);
                SectionContainer bindingCont = new SectionContainer(this, 
                        bindingNodeContainer, 
                        NbBundle.getMessage(ClientView.class, "LBL_Binding", binding.getName()));

                Node transportNode = new TransportClientNode(this, binding);
                SectionPanel transportPanel = new SectionPanel(this, transportNode, 
                        TRANSPORT_NODE_ID + binding.getName());
                bindingCont.addSection(transportPanel);
                nodes.add(transportNode);
                
                Node keystoreNode = new KeystoreClientNode(this, binding);
                SectionPanel keystorePanel = new SectionPanel(this, keystoreNode, 
                        KEYSTORE_NODE_ID + binding.getName());
                bindingCont.addSection(keystorePanel);
                nodes.add(keystoreNode);

                Node callbackNode = new CallbackClientNode(this, binding);
                SectionPanel callbackPanel = new SectionPanel(this, callbackNode, 
                        CALLBACK_NODE_ID + binding.getName());
                bindingCont.addSection(callbackPanel);
                nodes.add(callbackNode);

                if (isClientSTSConfigRequired(binding)) {
                    Node stsNode = new STSClientNode(this, binding);
                    SectionPanel stsPanel = new SectionPanel(this, stsNode, 
                            STS_NODE_ID + binding.getName());
                    bindingCont.addSection(stsPanel);
                    nodes.add(stsNode);
                }

                if (isClientAdvancedConfigRequired(binding, serviceModel)) {
                    Node advancedConfigNode = new AdvancedConfigurationClientNode(this, binding);    
                    SectionPanel advancedConfigPanel = new SectionPanel(this, advancedConfigNode, 
                            ADVANCEDCONFIG_NODE_ID + binding.getName());
                    bindingCont.addSection(advancedConfigPanel);
                    nodes.add(advancedConfigNode);
                }
                
                bindingChildren.add((Node[]) nodes.toArray(new Node[nodes.size()]));
                addSection(bindingCont, false);
                bindingNodes.add(bindingNodeContainer);
            }
            rootChildren.add((Node[]) bindingNodes.toArray(new Node[bindingNodes.size()]));
        } else {
            Binding binding = (Binding) bindings.toArray()[0];
            ArrayList nodes = new ArrayList();

            Node transportNode = new TransportClientNode(this, binding);
            SectionPanel transportPanel = new SectionPanel(this, transportNode, 
                    TRANSPORT_NODE_ID + binding.getName());
            addSection(transportPanel);
            nodes.add(transportNode);
            
            Node keystoreNode = new KeystoreClientNode(this, binding);
            SectionPanel keystorePanel = new SectionPanel(this, keystoreNode, 
                    KEYSTORE_NODE_ID + binding.getName());
            addSection(keystorePanel);
            nodes.add(keystoreNode);

            Node callbackNode = new CallbackClientNode(this, binding);
            SectionPanel callbackPanel = new SectionPanel(this, callbackNode, 
                    CALLBACK_NODE_ID + binding.getName());
            addSection(callbackPanel);
            nodes.add(callbackNode);

            if (isClientSTSConfigRequired(binding)) {
                Node stsNode = new STSClientNode(this, binding);
                SectionPanel stsPanel = new SectionPanel(this, stsNode, 
                        STS_NODE_ID + binding.getName());
                addSection(stsPanel);
                nodes.add(stsNode);
            }

            if (isClientAdvancedConfigRequired(binding, serviceModel)) {
                Node advancedConfigNode = new AdvancedConfigurationClientNode(this, binding);
                SectionPanel advancedConfigPanel = new SectionPanel(this, advancedConfigNode, 
                        ADVANCEDCONFIG_NODE_ID + binding.getName());
                addSection(advancedConfigPanel);
                nodes.add(advancedConfigNode);
            }

            rootChildren.add((Node[]) nodes.toArray(new Node[nodes.size()]));
            
        }
        setRoot(root);
    }
    
    private final RequestProcessor.Task refreshTask = RequestProcessor.getDefault().create(new Runnable() {
        public void run() {
            getRootNode().refreshSubtree();
        }
    });

    public void refreshView() {
        rootNode.refreshSubtree();
    }
    
    public void scheduleRefreshView() {
        refreshTask.schedule(REFRESH_DELAY);
    }

    public void dataModelPropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {
        rootNode.dataModelPropertyChange(source, propertyName, oldValue, newValue);
    }
    
    public SectionNode getRootNode() {
        return rootNode;
    }    

    private boolean isClientSTSConfigRequired(Binding binding) {        
        return true;
    }

    private boolean isClientAdvancedConfigRequired(Binding binding, WSDLModel serviceModel) {
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        boolean rmEnabled = RMModelHelper.isRMEnabled(serviceBinding);
        boolean timestampEnabled = SecurityPolicyModelHelper.isIncludeTimestamp(serviceBinding); 
        boolean secConvRequired = RequiredConfigurationHelper.isSecureConversationParamRequired(serviceBinding);
        return rmEnabled || secConvRequired || timestampEnabled;
    }

}
