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
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.wsdl.ui.view.property.BaseAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.property.BindingTypeAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingOperationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;


/**
 * @author Ritesh Adval
 *
 */
public class BindingNode extends WSDLExtensibilityElementNode<Binding> {
    
    
    private Binding mWSDLConstruct;
    
    private BindingPropertyAdapter mPropertyAdapter = null;
    
    private static Image ICON  = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/binding.png");

    public BindingNode(Binding wsdlConstruct) {
        super(new GenericWSDLComponentChildren<Binding>(wsdlConstruct), wsdlConstruct, new BindingNewTypesFactory());
        mWSDLConstruct = wsdlConstruct;
        
        this.mPropertyAdapter = new BindingPropertyAdapter();
        this.setNamedPropertyAdapter(this.mPropertyAdapter);
    }
    
    @Override
    public String getNameInLayer() {
        return WSDLExtensibilityElements.ELEMENT_BINDING;
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
            if(attrName.equals(Binding.NAME_PROPERTY)) { //NOT I18N
                attrValueProperty = createNameProperty();
                
            } else if(attrName.equals(Binding.TYPE_PROPERTY)) { //NOT I18N
                //type
                attrValueProperty = createTypeProperty();
                
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
        alwaysPresentAttrProperties.add(createTypeProperty());
        return alwaysPresentAttrProperties;
    }
    
    
    private Node.Property createNameProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new BaseAttributeProperty(mPropertyAdapter, String.class, Binding.NAME_PROPERTY);
        attrValueProperty.setName(Binding.NAME_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(BindingNode.class, "PROP_NAME_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(BindingNode.class, "BINDINGNODE_NAME_DESCRIPTION"));
        
        return attrValueProperty;
    }
    
    private Node.Property createTypeProperty() throws NoSuchMethodException {
        Node.Property attrValueProperty;
        attrValueProperty = new BindingTypeAttributeProperty(mPropertyAdapter,
                String.class, Binding.TYPE_PROPERTY);
        
        attrValueProperty.setName(Binding.TYPE_PROPERTY);
        attrValueProperty.setDisplayName(NbBundle.getMessage(BindingNode.class, "PROP_TYPE_DISPLAYNAME"));
        attrValueProperty.setShortDescription(NbBundle.getMessage(BindingNode.class, "BINDINGNODE_TYPE_DESCRIPTION"));
        
        return attrValueProperty;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(BindingNode.class);
    }
    
    public class BindingPropertyAdapter extends ConstraintNamedPropertyAdapter {
        public BindingPropertyAdapter() {
            super(mWSDLConstruct);
        }
        
        @Override
        public boolean isNameExists(String name) {
            WSDLModel model = mWSDLConstruct.getModel();
            return NameGenerator.getInstance().isBindingExists(name, model);
        }
        
        public void setType(String type) {
            if(type != null) {
                try {
                    Binding binding = getWSDLComponent();
                    org.netbeans.modules.xml.wsdl.ui.common.QName portTypeQName = org.netbeans.modules.xml.wsdl.ui.common.QName.getQNameFromString(type);
                    if(portTypeQName == null) {
                        binding.getModel().startTransaction();
                        binding.setType(null);
                        binding.getModel().endTransaction();
                    } else {
                        
                        String ns = portTypeQName.getNamespaceURI();
                        String prefix = portTypeQName.getPrefix();
                        if(ns == null || ns.trim().equals("")) {
                            ns = Utility.getNamespaceURI(prefix, binding.getModel());
                        }
                        
                        QName qname = null;
                        if (ns != null) {
                            qname = new QName(ns, portTypeQName.getLocalName());
                        }
                        
                        if(qname != null) {
                            PortType pType = binding.getModel().findComponentByName(qname, PortType.class);
                            if (pType == null) {
                                ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception("Not a valid type"));
                            } else {
                                binding.getModel().startTransaction();
                                binding.setType(binding.createReferenceTo(pType, PortType.class));
                                
                                getWSDLComponent().getModel().endTransaction();
                            }
                        }
                    }
                } catch (Exception e) {
                    ErrorManager.getDefault().notify(e);
                }
            }
        }
        
        public String getType() {
            NamedComponentReference pType = mWSDLConstruct.getType();
            if (pType != null) {
                QName portTypeQName = pType.getQName();
                if(portTypeQName != null) {
                    return Utility.fromQNameToString(portTypeQName);
                }
            }
            return "";
        }
        
    }
    
    
    public static final class BindingNewTypesFactory implements NewTypesFactory{
        
        public NewType[] getNewTypes(WSDLComponent def) {
            Binding binding = (Binding) def;
            ArrayList<NewType> list = new ArrayList<NewType>();
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            if (isBindingOperationActionEnabled(binding)) {
                list.add(new BindingOperationNewType(def));
            }
            
            list.addAll(Arrays.asList(new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_BINDING).getNewTypes(def)));
            return list.toArray(new NewType[]{});
        }
        
        private boolean isBindingOperationActionEnabled(Binding binding) {
            boolean enable = false;
            PortType type = binding.getType() != null ? binding.getType().get() : null;
            if (type == null) return true;
            Collection operations = type.getOperations();
            if (operations != null) {
                int boSize =  binding.getBindingOperations() != null ? binding.getBindingOperations().size() : 0;
                if (type.getOperations().size() > boSize) {
                    enable = true;
                }
            }
            
            return enable;
        }
    }


    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(BindingNode.class, "LBL_BindingNode_TypeDisplayName");
    }
    
    @Override
    public String getHtmlDisplayName() {
        String htmlDisplayName = super.getHtmlDisplayName();
        NamedComponentReference<PortType> type = mWSDLConstruct.getType();
        
        String decoration = null;
        if (type != null && type.get() != null) {
            String tns = type.get().getModel().getDefinitions().getTargetNamespace();
            
            decoration = NbBundle.getMessage(OperationParameterNode.class, "LBL_PortType",
                    Utility.getNameAndDropPrefixIfInCurrentModel(tns, type.get().getName(), mWSDLConstruct.getModel()));
        }
        
        if (decoration == null) {
            return htmlDisplayName;
        }
        return htmlDisplayName + " <font color='#999999'>"+decoration+"</font>";
    }
}


