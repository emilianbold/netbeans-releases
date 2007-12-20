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
 * BuiltInCustomizerFactory.java
 *
 * Created on January 29, 2007, 6:16 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.property.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.api.property.ExtensibilityElementPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.api.property.MessageAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.MessageProvider;
import org.netbeans.modules.xml.wsdl.ui.api.property.PartAttributeProperty;
import org.netbeans.modules.xml.wsdl.ui.api.property.PortTypeAttributeProperty;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;

/**
 *
 * @author radval
 */
public class BuiltInCustomizerFactory {
   
    public static final String BUILTIN_MESSAGE_CHOOSER =  "MessageChooser"; //NO I18N
    
    public static final String BUILTIN_PART_CHOOSER =  "PartChooser"; //NO I18N
    public static final String BUILTIN_PARTS_CHOOSER =  "PartsChooser"; //NO I18N
    public static final String BUILTIN_PORTTYPE_CHOOSER =  "PortTypeChooser"; //NO I18N
    
    
    /** Creates a new instance of BuiltInCustomizerFactory */
    public BuiltInCustomizerFactory() {
    }
    
    /**
     * For SimpleCustomizer
     * 
     * @param extensibilityElement
     * @param attributeQName
     * @param chooserType
     * @return
     */
    public static Node.Property getProperty(ExtensibilityElement extensibilityElement, 
                              QName attributeQName, 
                              String chooserType, boolean isOptional) {
        Node.Property property = null;
        ExtensibilityElementPropertyAdapter adapter = new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeQName.getLocalPart(), isOptional);
        try {
            if(chooserType.equals(BUILTIN_PART_CHOOSER)) {
                MessageProvider prov = new MessageProviderImpl(extensibilityElement);
                property = new PartAttributeProperty(prov, extensibilityElement.getModel(), adapter, String.class, "getValue", "setValue", false);
                property.setName(attributeQName.getLocalPart());
            } else if(chooserType.equals(BUILTIN_PARTS_CHOOSER)) {
                MessageProvider prov = new MessageProviderImpl(extensibilityElement);
                property = new PartAttributeProperty(prov, extensibilityElement.getModel(), adapter, String.class, "getValue", "setValue", true);
                property.setName(attributeQName.getLocalPart());
            } else if(chooserType.equals(BUILTIN_MESSAGE_CHOOSER)) {
                property = new MessageAttributeProperty(adapter, extensibilityElement, String.class, "getValue", "setValue");
                property.setName(attributeQName.getLocalPart());
            } else if(chooserType.equals(BUILTIN_PORTTYPE_CHOOSER)) {
                property = new PortTypeAttributeProperty(adapter, String.class, "getValue", "setValue");
                property.setName(attributeQName.getLocalPart());
            } else {
                Logger.getLogger(BuiltInCustomizerFactory.class.getName()).log(Level.INFO, "Not a recognized builtin in chooser");
            }
        } catch (Exception e) {
            //TODO:
            e.printStackTrace();
        }
        
        return property;
    }

    
    /**
     * For DependsOn StaticCustomizer
     * 
     * @param extensibilityElement
     * @param attributeQName
     * @param dependsOnAttributeQName
     * @param chooserType
     * @return
     */
    public static Node.Property getProperty(ExtensibilityElement extensibilityElement, 
            QName attributeQName, QName dependsOnAttributeQName,
            String chooserType, boolean isOptional) {
        Node.Property property = null;
        try {
            if(chooserType.equals(BUILTIN_PART_CHOOSER)) {
                MessageProvider prov = new DependsOnMessageProviderImpl(extensibilityElement, dependsOnAttributeQName);
                property = new PartAttributeProperty(prov, extensibilityElement.getModel(), new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeQName.getLocalPart(), isOptional), String.class, "getValue", "setValue", false);
                property.setName(attributeQName.getLocalPart());

            } else {
                Logger.getLogger(BuiltInCustomizerFactory.class.getName()).log(Level.INFO, "Not a recognized builtin in chooser");
            }
        } catch (Exception e) {
//          TODO:
            e.printStackTrace();
        }

        return property;
    }
    
    
    /**
     * For DependsOn DynamicCustomizer.. Not yet used.
     * 
     * @param extensibilityElement
     * @param attributeQName
     * @param nameOfCustomizer
     * @param className
     * @param valueType
     * @return
     */
    public static Property getProperty(ExtensibilityElement extensibilityElement, QName attributeQName, String nameOfCustomizer, String className, String valueType, boolean isOptional) {
        Node.Property property = null;
        try {
            if(nameOfCustomizer.equals(BUILTIN_PART_CHOOSER)) {
                MessageProvider prov = null; //TODO: to create a provider using the className.
                property = new PartAttributeProperty(prov, extensibilityElement.getModel(), new ExtensibilityElementPropertyAdapter(extensibilityElement, attributeQName.getLocalPart(), isOptional), String.class, "getValue", "setValue", false);
                property.setName(attributeQName.getLocalPart());

            } else {
                Logger.getLogger(BuiltInCustomizerFactory.class.getName()).log(Level.INFO, "Not a recognized builtin in chooser");
            }
        } catch (Exception e) {
//          TODO:
            e.printStackTrace();
        }

        return property;
    }
    
    /**
     * Default MessageProvider for elements wanting to get parts of the message that are linked to parent binding input/output/fault.
     */
    static class MessageProviderImpl implements MessageProvider {
        
        private ExtensibilityElement element;
        
        
        public MessageProviderImpl (ExtensibilityElement elem) {
            element = elem;
        }
        
        public String getMessage() {
            return null;
        }

        public Message getWSDLMessage() {
            //look for parent of this ExtensibilityElement which is 
            //either binding input/output/fault, once you get that, get the message
            //from it
            Message message = null;
            WSDLComponent parent = element;
            while (parent != null) {
                if (parent instanceof BindingOutput) {
                    BindingOutput output = (BindingOutput) parent;
                    OperationParameter op = output.getOutput() != null ? output.getOutput().get() : null;
                    return op == null ? null : (op.getMessage() == null ? null : op.getMessage().get());
                } else if (parent instanceof BindingInput) {
                    BindingInput input = (BindingInput) parent;
                    OperationParameter op = input.getInput() != null ? input.getInput().get() : null;
                    return op == null ? null : (op.getMessage() == null ? null : op.getMessage().get());
                } else if (parent instanceof BindingFault) {
                    BindingFault fault = (BindingFault) parent;
                    OperationParameter op = fault.getFault() != null ? fault.getFault().get() : null;
                    return op == null ? null : (op.getMessage() == null ? null : op.getMessage().get());
                }
                parent = parent.getParent();
            }
            
            return message;
        }
        
    }
    
    static class DependsOnMessageProviderImpl implements MessageProvider {

        private ExtensibilityElement element;
        private QName dependsOnAttributeQName;


        public DependsOnMessageProviderImpl (ExtensibilityElement elem, QName dependsOnAttributeQName) {
            element = elem;
            this.dependsOnAttributeQName = dependsOnAttributeQName;
        }

        public String getMessage() {
            return element.getAttribute(dependsOnAttributeQName.getLocalPart());
        }

        public Message getWSDLMessage() {
            return null;
        }

    }


}
