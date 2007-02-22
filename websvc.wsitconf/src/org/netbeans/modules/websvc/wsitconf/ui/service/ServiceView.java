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
package org.netbeans.modules.websvc.wsitconf.ui.service;

import java.util.ArrayList;
import java.util.HashSet;
//import org.netbeans.jmi.javamodel.JavaClass;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingFaultNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.OperationNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.ServiceNode;
import org.netbeans.modules.websvc.wsitconf.util.JMIUtils;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.OperationContainerServiceNode;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.WSITModelSupport;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ui.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;

import java.util.Collection;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingContainerServiceNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingInputNode;
import org.netbeans.modules.websvc.wsitconf.ui.nodes.BindingOutputNode;

/**
 * @author Martin Grebac
 */
public class ServiceView extends SectionView {

    private SectionNode rootNode;
    
    private static final int REFRESH_DELAY = 40;
    
    private WSDLModel model;
       
    ServiceView(InnerPanelFactory factory, WSDLModel model, Node node, Service s) {
        this(factory,  model, node,  s, null);
    }
    
    ServiceView(InnerPanelFactory factory, WSDLModel model, Node node, Service s, Collection<Binding> bs) {
        super(factory);
        this.model = model;

        if (model == null) return;
        
        Collection<Binding> bindings = bs;
        if (bindings == null) {
            bindings = new HashSet();
            WSITModelSupport.fillImportedBindings(model, bindings, new HashSet());
        }
        
        //add binding section
        Node[] bindingNodes = new Node[bindings.size()];
        Children rootChildren = new Children.Array();

//        JavaClass jc = null;
//        if (s != null) {
//            String wsdlUrl = s.getWsdlUrl();
//            if (wsdlUrl == null) { // WS from Java
//                jc = (JavaClass)node.getLookup().lookup(JavaClass.class);
//            }
//        }
        
        // if there's only one binding, make the dialog simpler
        if (bindingNodes.length > 1) {
            Node root = new AbstractNode(rootChildren);
            int i = 0;
            setRoot(root);

            for (Binding binding : bindings) {
                
//                if (jc != null) {
//                    JMIUtils.refreshOperations(binding, jc);
//                }

                // main node container for a specific binding
                Children bindingChildren = new Children.Array();
                Node bindingNodeContainer = new BindingContainerServiceNode(bindingChildren);
                SectionContainer bindingCont = new SectionContainer(this, 
                        bindingNodeContainer, binding.getName() + " Binding");      //NOI18N
                addSection(bindingCont, false);

                ArrayList nodes = new ArrayList();

                Node serviceNode = new ServiceNode(this, binding);
                nodes.add(serviceNode);
                SectionPanel servicePanel = new SectionPanel(this, serviceNode, binding, true);
                bindingCont.addSection(servicePanel, false);

                Collection<BindingOperation> operations = binding.getBindingOperations();
                for (BindingOperation op : operations) {
                    Children opChildren = new Children.Array();
                    Node opNodeContainer = new OperationContainerServiceNode(opChildren);
                    SectionContainer opCont = new SectionContainer(this, opNodeContainer, op.getName() + " Operation"); //NOI18N
                    bindingCont.addSection(opCont, false);
                    
                    ArrayList subNodes = new ArrayList();

                    Node opNode = new OperationNode(this, op);
                    subNodes.add(opNode);
                    SectionPanel opPanel = new SectionPanel(this, opNode, op, false);
                    opCont.addSection(opPanel, false);

                    BindingInput bi = op.getBindingInput();
                    if (bi != null) {
                        Node biNode = new BindingInputNode(this, bi);
                        subNodes.add(biNode);
                        SectionPanel biPanel = new SectionPanel(this, biNode, bi, false);
                        opCont.addSection(biPanel, false);
                    }
                    BindingOutput bo = op.getBindingOutput();
                    if (bo != null) {
                        Node boNode = new BindingOutputNode(this, bo);
                        subNodes.add(boNode);
                        SectionPanel boPanel = new SectionPanel(this, boNode, bo, false);
                        opCont.addSection(boPanel, false);
                    }
                    Collection<BindingFault> bfs = op.getBindingFaults();
                    for (BindingFault bf : bfs) {
                        Node bfNode = new BindingFaultNode(this, bf);
                        subNodes.add(bfNode);
                        SectionPanel bfPanel = new SectionPanel(this, bfNode, bf, false);
                        opCont.addSection(bfPanel, false);
                    }
                    opChildren.add((Node[]) subNodes.toArray(new Node[subNodes.size()]));
                    nodes.add(opNodeContainer);
                }

                bindingChildren.add((Node[]) nodes.toArray(new Node[nodes.size()]));

                bindingNodes[i++] = bindingNodeContainer;
            }
            rootChildren.add(bindingNodes);
        } else {
            Binding b = (Binding) bindings.toArray()[0];
//            if (jc != null) {
//                JMIUtils.refreshOperations(b, jc);
//            }
            Node root = new AbstractNode(rootChildren);
            setRoot(root);

            Node serviceNode = new ServiceNode(this, b);
            ArrayList nodes = new ArrayList();
            nodes.add(serviceNode);
            
            SectionPanel servicePanel = new SectionPanel(this, serviceNode, b, true);
            addSection(servicePanel, false);

            Collection<BindingOperation> operations = b.getBindingOperations();
            for (BindingOperation op : operations) {

                Children opChildren = new Children.Array();
                Node opNodeContainer = new OperationContainerServiceNode(opChildren);
                SectionContainer opCont = new SectionContainer(this, opNodeContainer, op.getName() + " Operation"); //NOI18N
                addSection(opCont, false);

                ArrayList subNodes = new ArrayList();

                Node opNode = new OperationNode(this, op);
                subNodes.add(opNode);
                SectionPanel opPanel = new SectionPanel(this, opNode, op, false);
                opCont.addSection(opPanel, false);

                BindingInput bi = op.getBindingInput();
                if (bi != null) {
                    Node biNode = new BindingInputNode(this, bi);
                    subNodes.add(biNode);
                    SectionPanel biPanel = new SectionPanel(this, biNode, bi, false);
                    opCont.addSection(biPanel, false);
                }
                BindingOutput bo = op.getBindingOutput();
                if (bo != null) {
                    Node boNode = new BindingOutputNode(this, bo);
                    subNodes.add(boNode);
                    SectionPanel boPanel = new SectionPanel(this, boNode, bo, false);
                    opCont.addSection(boPanel, false);
                }
                Collection<BindingFault> bfs = op.getBindingFaults();
                for (BindingFault bf : bfs) {
                    Node bfNode = new BindingFaultNode(this, bf);
                    subNodes.add(bfNode);
                    SectionPanel bfPanel = new SectionPanel(this, bfNode, bf, false);
                    opCont.addSection(bfPanel, false);
                }
                opChildren.add((Node[]) subNodes.toArray(new Node[subNodes.size()]));
                nodes.add(opNodeContainer);
            }
            rootChildren.add((Node[]) nodes.toArray(new Node[nodes.size()]));            
            servicePanel.open();
        }
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

}
