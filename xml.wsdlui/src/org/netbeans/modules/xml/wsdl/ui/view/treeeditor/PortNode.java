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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * Created on May 17, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.BindingAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;



/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class PortNode extends WSDLExtensibilityElementNode<Port> {
    
    
    private Port mWSDLConstruct;
    
    
    private ServicePortPropertyAdapter mPropertyAdapter = null;
    
    private static Image ICON  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/serviceport.png");
    
    public PortNode(Port wsdlConstruct) {
        super(new GenericWSDLComponentChildren<Port>(wsdlConstruct), wsdlConstruct, new ServicePortNewTypesFactory());
        mWSDLConstruct = wsdlConstruct;
        
        
        this.mPropertyAdapter = new ServicePortPropertyAdapter();
        super.setNamedPropertyAdapter(this.mPropertyAdapter);
    }
    
    @Override
    public String getNameInLayer() {
        return WSDLExtensibilityElements.ELEMENT_SERVICE_PORT;
    }
    
    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }
    
    @Override
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        try {
            String attrName = attrQName.getLocalPart();
            //name
            if(attrName.equals(Port.NAME_PROPERTY)) { //NOT I18N
                //name
                attrValueProperty = createNameProperty();
                
                
            } else if(attrName.equals(Port.BINDING_PROPERTY)) {
                attrValueProperty = createBindingProperty();
                
            } else {
                attrValueProperty = super.createAttributeProperty(attrQName);
            }
            
        } catch(Exception ex) {
            mLogger.log(Level.SEVERE, "failed to create property sheet for "+ getWSDLComponent(), ex);
            ErrorManager.getDefault().notify(ex);
        }
        return attrValueProperty;
    }
    
    @Override
     protected List<Node.Property> createAlwaysPresentAttributeProperty() throws Exception {
        ArrayList<Node.Property> alwaysPresentAttrProperties = new ArrayList<Node.Property>();
        alwaysPresentAttrProperties.add(createNameProperty());
        alwaysPresentAttrProperties.add(createBindingProperty());
        return alwaysPresentAttrProperties;
    }
    
    
    private Node.Property createNameProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new BaseAttributeProperty(mPropertyAdapter,
                String.class,
                Port.NAME_PROPERTY);
        attrValueProperty.setName(Port.NAME_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(PortNode.class, "PROP_NAME_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(PortNode.class, "PORT_NAME_DESC"));
        
        return attrValueProperty;
    }
    
    private Node.Property createBindingProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new BindingAttributeProperty(mPropertyAdapter,
                String.class,
                Port.BINDING_PROPERTY);
        
        attrValueProperty.setName(Port.BINDING_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(PortNode.class, "PROP_BINDING_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(PortNode.class, "PORT_BINDING_DESC"));
        
        return attrValueProperty;
    }
    
    
    public class ServicePortPropertyAdapter extends ConstraintNamedPropertyAdapter {
        
        public ServicePortPropertyAdapter() {
            super(mWSDLConstruct);
        }
        
        @Override
        public boolean isNameExists(String name) {
            return NameGenerator.getInstance().isServicePortExists(name,
                    (Service) mWSDLConstruct.getParent());
        }
        
        public void setBinding(String bindingName) {
            if(bindingName != null) {
                try {
                    org.netbeans.modules.xml.wsdl.ui.common.QName bindingQName = org.netbeans.modules.xml.wsdl.ui.common.QName.getQNameFromString(bindingName);
                    if(bindingQName == null) {
                        mWSDLConstruct.getModel().startTransaction();
                        mWSDLConstruct.setBinding(null);
                        mWSDLConstruct.getModel().endTransaction();
                    } else {
                        
                        String ns = bindingQName.getNamespaceURI();
                        String prefix = bindingQName.getPrefix();
                        if(ns == null || ns.trim().equals("")) {
                            ns = Utility.getNamespaceURI(prefix, mWSDLConstruct.getModel());
                        }
                        
                        
                        QName qname = null;
                        if (ns != null) {
                            qname = new QName(ns, bindingQName.getLocalName());
                        }
                        
                        if(qname != null) {
                            Binding binding = mWSDLConstruct.getModel().findComponentByName(qname, Binding.class);
                            if (binding == null) {
                                ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception("Not a valid type"));
                            } else {
                                mWSDLConstruct.getModel().startTransaction();
                                mWSDLConstruct.setBinding(mWSDLConstruct.createReferenceTo(binding, Binding.class));
                                
                                mWSDLConstruct.getModel().endTransaction();
                            }
                        }
                    }
                    fireDisplayNameChange(null, getDisplayName());
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
        
        public String getBinding() {
            NamedComponentReference binding = mWSDLConstruct.getBinding();
            if (binding != null) {
                QName bindingQName = binding.getQName();
                if(bindingQName != null) {
                    return Utility.fromQNameToString(bindingQName);
                }
            }
            return "";
            
        }
        
        public Port getServicePort() {
            return mWSDLConstruct;
        }
    }
    
    public static final class ServicePortNewTypesFactory implements NewTypesFactory{
        
        public NewType[] getNewTypes(WSDLComponent def) {
            ArrayList<NewType> list = new ArrayList<NewType>();
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            
            list.addAll(Arrays.asList(new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_SERVICE_PORT).getNewTypes(def)));
            return list.toArray(new NewType[]{});
        }
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(PortNode.class, "LBL_PortNode_TypeDisplayName");
    }
    
    @Override
    public String getHtmlDisplayName() {
        String htmlDisplayName = super.getHtmlDisplayName();
        NamedComponentReference<Binding> binding = mWSDLConstruct.getBinding();
        
        String decoration = null;
        if (binding != null && binding.get() != null) {
            String tns = binding.get().getModel().getDefinitions().getTargetNamespace();
            decoration = NbBundle.getMessage(OperationParameterNode.class, "LBL_Binding", 
                    Utility.getNameAndDropPrefixIfInCurrentModel(tns, binding.get().getName(), mWSDLConstruct.getModel()));
        }
        
        if (decoration == null) {
            //decoration = NbBundle.getMessage(OperationParameterNode.class, "LBL_BindingNotSet");
            return htmlDisplayName;
        }
        return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";
    }
}



