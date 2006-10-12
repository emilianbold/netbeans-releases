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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * WSPanelFactory.java
 *
 * Created on February 27, 2006, 11:58 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.customization.multiview;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.customization.multiview.WSCustomizationView.BindingKey;
import org.netbeans.modules.xml.multiview.ui.InnerPanelFactory;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Fault;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.nodes.Node;

/**
 *
 * @author Roderico Cruz
 */
public class WSPanelFactory implements InnerPanelFactory {
    private ToolBarDesignEditor editor;
    private Node node;
    private JaxWsModel jmodel;
    
    private Map<Object, SaveableSectionInnerPanel> panels;
    
    //panels
    private DefinitionsPanel definitionsPanel;
    private PortTypePanel portTypePanel;
    private PortTypeOperationPanel portTypeOperationPanel;
    private PortTypeOperationFaultPanel portTypeOperationFaultPanel;
    private BindingPanel bindingPanel;
    private BindingOperationPanel bindingOperationPanel;
    private ServicePanel servicePanel;
    private PortPanel portPanel;
    private ExternalBindingPanel externalBindingPanel;
    private Definitions primaryDefinitions;
    /**
     * Creates a new instance of WSPanelFactory
     */
    public WSPanelFactory(ToolBarDesignEditor editor,
            Node node, Definitions primaryDefinitions, JaxWsModel jmodel) {
        this.editor = editor;
        this.node = node;
        this.primaryDefinitions = primaryDefinitions;
        this.jmodel = jmodel;
        
        panels = new HashMap<Object, SaveableSectionInnerPanel>();
    }
    
    public Collection<SaveableSectionInnerPanel> getPanels(){
        return panels.values();
    }
    
    public SectionInnerPanel createInnerPanel(Object key) {
        if(key instanceof Definitions){
            Definitions definitions = (Definitions)key;
            definitionsPanel = (DefinitionsPanel)panels.get(definitions);
            if(definitionsPanel == null){
                definitionsPanel =  new DefinitionsPanel((SectionView) editor.getContentView(),
                        definitions, node);
                panels.put(definitions, definitionsPanel);
            }
            return definitionsPanel;
        } else if (key instanceof PortType){
            PortType portType = (PortType)key;
            portTypePanel = (PortTypePanel)panels.get(portType);
            if(portTypePanel == null){
                portTypePanel = new PortTypePanel((SectionView) editor.getContentView(),
                        portType, node, primaryDefinitions);
                panels.put(portType, portTypePanel);
            }
            return portTypePanel;
        } else if (key instanceof Operation){
            Operation operation = (Operation)key;
            portTypeOperationPanel = (PortTypeOperationPanel)panels.get(operation);
            if(portTypeOperationPanel == null){
                portTypeOperationPanel = new PortTypeOperationPanel((SectionView) editor.getContentView(),
                        operation,  node, primaryDefinitions);
                panels.put(operation, portTypeOperationPanel);
            }
            return portTypeOperationPanel;
        } else if (key instanceof Fault){
            Fault fault = (Fault)key;
            portTypeOperationFaultPanel = (PortTypeOperationFaultPanel)panels.get(fault);
            if(portTypeOperationFaultPanel == null){
                portTypeOperationFaultPanel =  new PortTypeOperationFaultPanel((SectionView) editor.getContentView(),
                        fault);
                panels.put(fault, portTypeOperationFaultPanel);
            }
            return portTypeOperationFaultPanel;
        } else if (key instanceof Binding){
            Binding binding = (Binding)key;
            bindingPanel = (BindingPanel)panels.get(binding);
            if(bindingPanel == null){
                bindingPanel =  new BindingPanel((SectionView) editor.getContentView(),
                        binding, primaryDefinitions);
                panels.put(binding, bindingPanel);
            }
            return bindingPanel;
            
        } else if (key instanceof BindingOperation){
            BindingOperation bindingOperation = (BindingOperation)key;
            bindingOperationPanel  = (BindingOperationPanel)panels.get(bindingOperation);
            if(bindingOperationPanel == null){
                bindingOperationPanel =  new BindingOperationPanel((SectionView) editor.getContentView(),
                        bindingOperation, primaryDefinitions);
                panels.put(bindingOperation, bindingOperationPanel);
            }
            return bindingOperationPanel;
        } else if (key instanceof Service){
            Service service = (Service)key;
            servicePanel = (ServicePanel)panels.get(service);
            if(servicePanel == null){
                servicePanel =  new ServicePanel((SectionView) editor.getContentView(),
                        service);
                panels.put(service, servicePanel);
            }
            return servicePanel;
        } else if (key instanceof Port){
            Port port = (Port)key;
            portPanel = (PortPanel)panels.get(port);
            if(portPanel == null){
                portPanel =  new PortPanel((SectionView) editor.getContentView(),
                        port, node);
                panels.put(port, portPanel);
            }
            return portPanel;
        } else if(key instanceof BindingKey){
            BindingKey bindingKey = (BindingKey)key;
            externalBindingPanel = (ExternalBindingPanel)panels.get(bindingKey);
            if(externalBindingPanel == null){
                externalBindingPanel =  new ExternalBindingPanel((SectionView) editor.getContentView(),
                        node, jmodel);
                panels.put(bindingKey,externalBindingPanel);
            }
            return externalBindingPanel;
        }
        return null;
    }
}
