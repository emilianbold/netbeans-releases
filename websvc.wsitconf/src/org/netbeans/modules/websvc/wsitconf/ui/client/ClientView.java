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
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.AdvancedConfigurationClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.CallbackClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.BindingContainerClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.STSClientNode;
import org.netbeans.modules.websvc.wsitconf.ui.client.nodes.TransportClientNode;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RMModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.RequiredConfigurationHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.UsernameToken;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 * @author Martin Grebac
 */
public class ClientView extends SectionView {

    static final String KEYSTORE_NODE_ID = "keystore";                          //NOI18N
    static final String CALLBACK_NODE_ID = "callback";                          //NOI18N
    static final String STS_NODE_ID = "sts";                                    //NOI18N
    static final String TRANSPORT_NODE_ID = "transpotr";                        //NOI18N
    static final String ADVANCEDCONFIG_NODE_ID = "advancedconfig";              //NOI18N

    ClientView(InnerPanelFactory factory, WSDLModel clientModel, WSDLModel serviceModel, Service s, Project project, Node node) {
    
        super(factory);

        //create root node
        Children rootChildren = new Children.Array();
        Node root = new AbstractNode(rootChildren);

        Collection<Binding> bindings = new HashSet<Binding>();
        WSITModelSupport.fillImportedBindings(clientModel, bindings, new HashSet());
        
        Collection<Port> ports = s.getPorts();
        for (Port p : ports) {
            QName bqname = p.getBinding().getQName();
            Binding b = clientModel.findComponentByName(bqname, Binding.class);
            bindings.add(b);
        }

        ArrayList<Node> bindingNodes = new ArrayList<Node>();
        if (bindings.size() > 1) {
            for (Binding binding : bindings) {
                ArrayList<Node> nodes = new ArrayList<Node>();

                // main node container for a specific binding
                Children bindingChildren = new Children.Array();
                Node bindingNodeContainer = new BindingContainerClientNode(bindingChildren);
                SectionContainer bindingCont = new SectionContainer(this, 
                        bindingNodeContainer, 
                        NbBundle.getMessage(ClientView.class, "LBL_Binding", binding.getName()));

                Node transportNode = new TransportClientNode();
                SectionPanel transportPanel = new SectionPanel(this, transportNode, 
                        TRANSPORT_NODE_ID + binding.getName(), false);
                bindingCont.addSection(transportPanel);
                nodes.add(transportNode);
    
                if (isCallBackConfigRequired(binding, serviceModel) || isStoreConfigRequired(binding, serviceModel)) {
                    Node callbackNode = new CallbackClientNode();
                    SectionPanel callbackPanel = new SectionPanel(this, callbackNode, 
                            CALLBACK_NODE_ID + binding.getName(), true);
                    bindingCont.addSection(callbackPanel);
                    nodes.add(callbackNode);
                }

                if (isClientSTSConfigRequired(binding, serviceModel)) {
                    Node stsNode = new STSClientNode();
                    SectionPanel stsPanel = new SectionPanel(this, stsNode, 
                            STS_NODE_ID + binding.getName());
                    bindingCont.addSection(stsPanel);
                    nodes.add(stsNode);
                }

                if (isClientAdvancedConfigRequired(binding, serviceModel)) {
                    Node advancedConfigNode = new AdvancedConfigurationClientNode();    
                    SectionPanel advancedConfigPanel = new SectionPanel(this, advancedConfigNode, 
                            ADVANCEDCONFIG_NODE_ID + binding.getName());
                    bindingCont.addSection(advancedConfigPanel);
                    nodes.add(advancedConfigNode);
                }
                
                bindingChildren.add(nodes.toArray(new Node[nodes.size()]));
                addSection(bindingCont, false);
                bindingNodes.add(bindingNodeContainer);
            }
            rootChildren.add(bindingNodes.toArray(new Node[bindingNodes.size()]));
        } else {
            Binding binding = (Binding) bindings.toArray()[0];
            ArrayList<Node> nodes = new ArrayList<Node>();

            Node transportNode = new TransportClientNode();
            SectionPanel transportPanel = new SectionPanel(this, transportNode, 
                    TRANSPORT_NODE_ID + binding.getName(), false);
            addSection(transportPanel);
            nodes.add(transportNode);
            
            if (isCallBackConfigRequired(binding, serviceModel) || isStoreConfigRequired(binding, serviceModel)) {
                Node callbackNode = new CallbackClientNode();
                SectionPanel callbackPanel = new SectionPanel(this, callbackNode, 
                        CALLBACK_NODE_ID + binding.getName(), true);
                addSection(callbackPanel);
                nodes.add(callbackNode);
            }

            if (isClientSTSConfigRequired(binding, serviceModel)) {
                Node stsNode = new STSClientNode();
                SectionPanel stsPanel = new SectionPanel(this, stsNode, 
                        STS_NODE_ID + binding.getName());
                addSection(stsPanel);
                nodes.add(stsNode);
            }

            if (isClientAdvancedConfigRequired(binding, serviceModel)) {
                Node advancedConfigNode = new AdvancedConfigurationClientNode();
                SectionPanel advancedConfigPanel = new SectionPanel(this, advancedConfigNode, 
                        ADVANCEDCONFIG_NODE_ID + binding.getName());
                addSection(advancedConfigPanel);
                nodes.add(advancedConfigNode);
            }
            rootChildren.add(nodes.toArray(new Node[nodes.size()]));
        }
        setRoot(root);
    }
    
    private boolean isClientSTSConfigRequired(Binding binding, WSDLModel serviceModel) {        
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        String profile = ProfilesModelHelper.getWSITSecurityProfile(serviceBinding);
        if (ComboConstants.PROF_STSISSUED.equals(profile) ||
            ComboConstants.PROF_STSISSUEDENDORSE.equals(profile) ||
            ComboConstants.PROF_STSISSUEDCERT.equals(profile)) {
                return true;
        }
        return false;
    }

    private boolean isClientAdvancedConfigRequired(Binding binding, WSDLModel serviceModel) {
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        boolean rmEnabled = RMModelHelper.isRMEnabled(serviceBinding);
//        boolean timestampEnabled = SecurityPolicyModelHelper.isIncludeTimestamp(serviceBinding); 
        boolean secConvRequired = RequiredConfigurationHelper.isSecureConversationParamRequired(serviceBinding);
        
        //TODO - enable when timestamp becomes supported
        
        return rmEnabled || secConvRequired /* || timestampEnabled*/;
    }

    private boolean isStoreConfigRequired(Binding binding, WSDLModel serviceModel) {
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        String profile = ProfilesModelHelper.getWSITSecurityProfile(serviceBinding);
        if (!SecurityPolicyModelHelper.isSecurityEnabled(serviceBinding)) {
            return false;
        }
        return CallbackPanel.isStoreConfigRequired(profile, false, serviceBinding) || 
               CallbackPanel.isStoreConfigRequired(profile, true, serviceBinding);
    }

    private boolean isCallBackConfigRequired(Binding binding, WSDLModel serviceModel) {
        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        String profile = ProfilesModelHelper.getWSITSecurityProfile(serviceBinding);
        if (!SecurityPolicyModelHelper.isSecurityEnabled(serviceBinding)) {
            return false;
        }
        
        ArrayList<WSDLComponent> compsToTry = new ArrayList<WSDLComponent>();
        compsToTry.add(serviceBinding);
        Collection<BindingOperation> ops = serviceBinding.getBindingOperations();
        for (BindingOperation op : ops) {
            BindingInput bi = op.getBindingInput();
            if (bi != null) compsToTry.add(bi);
            BindingOutput bo = op.getBindingOutput();
            if (bo != null) compsToTry.add(bo);
            Collection<BindingFault> bfs = op.getBindingFaults();
            for(BindingFault bf : bfs) {
                if (bf != null) compsToTry.add(bf);
            }
        }

        for (WSDLComponent wc : compsToTry) {
            List<WSDLComponent> suppTokens = SecurityTokensModelHelper.getSupportingTokens(wc);
            if ((suppTokens != null) && (suppTokens.size() > 0)) {
                for (WSDLComponent suppToken : suppTokens) {
                    WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(suppToken);
                    if (token instanceof UsernameToken) {
                        return true;
                    }
                }
            }
        }
        
        if ((ComboConstants.PROF_MUTUALCERT.equals(profile) ||
            ComboConstants.PROF_ENDORSCERT.equals(profile) ||
            ComboConstants.PROF_MSGAUTHSSL.equals(profile)) ||
            ComboConstants.PROF_TRANSPORT.equals(profile)) {
            return false;
        }
        return true;
    }
    
}
