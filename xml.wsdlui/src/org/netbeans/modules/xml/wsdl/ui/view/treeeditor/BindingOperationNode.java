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


package org.netbeans.modules.xml.wsdl.ui.view.treeeditor;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingOperationFaultNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingOperationInputNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.BindingOperationOutputNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.DocumentationNewType;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.ExtensibilityElementNewTypesFactory;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.newtype.NewTypesFactory;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.datatransfer.NewType;


/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class BindingOperationNode extends WSDLExtensibilityElementNode<BindingOperation> {
    private Image ICON;

    public BindingOperationNode(BindingOperation wsdlConstruct) {
        super(new GenericWSDLComponentChildren<BindingOperation>(wsdlConstruct), wsdlConstruct, new BindingOperationNewTypesFactory());
        // Must set the icon to something to honor getIcon() contract.
        ICON = Utilities.loadImage(
                "org/netbeans/modules/xml/wsdl/ui/view/resources/bindingoperation.png");
        if (wsdlConstruct.getOperation() != null) {
            Operation operation = wsdlConstruct.getOperation().get();
            if (operation != null) {
                setIcon(operation);
            }
        }
    }
    
    @Override
    public String getNameInLayer() {
        return WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION;
    }
    
    private void setIcon(Operation operation){
        if (operation instanceof RequestResponseOperation) {
            ICON = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/requestresponse_operation.png");
        } else if (operation instanceof OneWayOperation) {
            ICON = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/oneway_operation.png");
        } else if (operation instanceof NotificationOperation) {
            ICON = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/notification_operation.png");
        } else if (operation instanceof SolicitResponseOperation) {
            ICON =    Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/solicitresponse_operation.png");
        } else {
            ICON = Utilities.loadImage
            ("org/netbeans/modules/xml/wsdl/ui/view/resources/bindingoperation.png");
        }
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
    public boolean canRename() {
        return false;
    }

    @Override
    protected Node.Property createAttributeProperty(QName attrQName) {
        Node.Property attrValueProperty = null;
        try {
            String attrName = attrQName.getLocalPart();
            //name
            if(attrName.equals(BindingOperation.NAME_PROPERTY)) { //NOT I18N
                //name
                attrValueProperty = createNameProperty();

            }  else {
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
            
            return alwaysPresentAttrProperties;
    }
    
    
      private Node.Property createNameProperty() {
          Node.Property attrValueProperty;
          attrValueProperty = new BindingOperationNameProperty(BindingOperation.NAME_PROPERTY, 
                          NbBundle.getMessage(BindingOperationNode.class, "PROP_NAME_DISPLAYNAME"), 
                          NbBundle.getMessage(BindingOperationNode.class, "BINDINGOPERATION_NAME_DESCRIPTION"));
                
          return attrValueProperty;
      }
      

    public class BindingOperationNameProperty extends PropertySupport.ReadOnly<String> {
        
         public BindingOperationNameProperty(String name, String displayName, String shortDesc) {
             super(name, String.class, displayName, shortDesc);
         }

        @Override
        public String getValue() {
            String name = getWSDLComponent().getName();
            return name == null ? "" : name;
        }
    }
    
    public static final class BindingOperationNewTypesFactory implements NewTypesFactory{

        public NewType[] getNewTypes(WSDLComponent def) {
            
            ArrayList<NewType> list = new ArrayList<NewType>();
            if (def.getDocumentation() == null) {
                list.add(new DocumentationNewType(def));
            }
            BindingOperation bo = (BindingOperation) def;
            if (bo.getOperation() != null && bo.getOperation().get() != null) {
                if (needInput(bo)){
                    list.add(new BindingOperationInputNewType(def));
                }
                if (needOutput(bo)){
                    list.add(new BindingOperationOutputNewType(def));
                }
                if (needFault(bo)){
                    list.add(new BindingOperationFaultNewType(def));
                }
            }
            list.addAll(Arrays.asList(new ExtensibilityElementNewTypesFactory(WSDLExtensibilityElements.ELEMENT_BINDING_OPERATION).getNewTypes(def)));
            return list.toArray(new NewType[]{});
        }
        private boolean needInput(BindingOperation bo){
            if (bo.getBindingInput() == null) {
                Operation operation = bo.getOperation().get();
                return operation.getInput() != null;
            }
            return false;
        }
        private boolean needOutput(BindingOperation bo){
            if (bo.getBindingOutput() == null) {
                Operation operation = bo.getOperation().get();
                return operation.getOutput() != null;
            }
            return false;
        }
        private boolean needFault(BindingOperation bo){
            Operation operation = bo.getOperation().get();
            int operationFaultsSize = operation.getFaults() != null ? operation.getFaults().size() : 0;
            if (operationFaultsSize > 0) {
                if (bo.getBindingFaults() != null) {
                    return operationFaultsSize > bo.getBindingFaults().size();
                }
            }
            return false;
        }
        
    }

    @Override
    public String getTypeDisplayName() {
        return NbBundle.getMessage(BindingOperationNode.class, "LBL_BindingOperationNode_TypeDisplayName");
    }
}



