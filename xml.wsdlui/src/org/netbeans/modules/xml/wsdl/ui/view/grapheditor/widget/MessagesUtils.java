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

package org.netbeans.modules.xml.wsdl.ui.view.grapheditor.widget;

import java.util.Collection;
import java.util.LinkedList;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponentReference;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.nodes.Node;

/**
 *
 * @author anjeleevich
 */
public class MessagesUtils {
    

    public static NamedComponentReference<GlobalType> getDefaultTypeReference(
            WSDLModel model) 
    {
        GlobalSimpleType newType = null;
        
        Schema schema = SchemaModelFactory.getDefault()
                .getPrimitiveTypesModel().getSchema();
        
        for (GlobalSimpleType type : schema.getSimpleTypes()) {
            if ("string".equals(type.getName())) { // NOI18N
                newType = type;
                break;
            }
        }
        
        if (newType == null) return null;
        
        return model.getDefinitions().createSchemaReference(newType, 
                GlobalType.class);      
    }
    
    
    public static String createNewMessageName(WSDLModel model) {
        int i = 0;
        String name;
        
        do {
            name = "Message" + (++i);
        } while (getMessageByName(model, name) != null);
        
        return name;        
    }
    
    
    public static Collection<Message> getMessages(WSDLModel model) {
        Collection<Message> messages = model.getDefinitions().getMessages();
        return (messages != null) ? messages : new LinkedList<Message>();
    }
    
    
    public static String createNewPartName(Message message) {
        int i = 0;
        String name;
        
        do {
            name = "Part" + (++i); // NOI18N
        } while (getPartByName(message, name) != null);
        
        return name;        
    }    
    
    
    public static Collection<Part> getParts(Message message) {
        Collection<Part> parts = message.getParts();
        return (parts != null) ? parts : new LinkedList<Part>();
    }

    
    public static String getPartTypeOrElementString(Part part) {
        String s = getPartTypeString(part);
        
        if (s == null) {
            s = getPartElementString(part);
        }
        
        if (s == null) {
            s = "<Undefined>"; // NOI18N
        }
        
        return s;
    }
    
    
    public static String getPartTypeString(Part part) {
        if (part.getType() == null) return null;
        return convertQNameToString(part.getType().getQName());
    }
    
    
    public static String getPartElementString(Part part) {
        if (part.getElement() == null) return null;
        return convertQNameToString(part.getElement().getQName());
    }
    
    
    private static String convertQNameToString(QName qname) {
        if (qname == null) return null;
        return qname.getPrefix() + ":" + qname.getLocalPart(); // NOI18N
    }
    
    
    
    private static Message getMessageByName(WSDLModel model, String name) {
        for (Message message : getMessages(model)) {
            if (name.equals(message.getName())) return message;
        }
        return null;
    }
    
    
    private static Part getPartByName(Message message, String name) {
        for (Part part : getParts(message)) {
            if (name.equals(part.getName())) return part;
        }
        return null;
    }
    
    
    public static SchemaComponent extractSchemaComponent(Node node) {
        AXIComponent axiComponent = (AXIComponent) node.getLookup()
                .lookup(AXIComponent.class);

        SchemaComponent schemaComponent = null;

        if (axiComponent != null) {
            schemaComponent = axiComponent.getPeer();
        } else {
            SchemaComponentReference reference = (SchemaComponentReference) 
                    node.getLookup().lookup(SchemaComponentReference.class);

            if (reference != null) {
                schemaComponent = reference.get();
            }

            if (schemaComponent == null) {
                schemaComponent = (SchemaComponent) node.getLookup()
                        .lookup(SchemaComponent.class);
            }
        }

        if (schemaComponent != null 
                && (schemaComponent instanceof GlobalType 
                || schemaComponent instanceof GlobalElement)) 
        {
            return schemaComponent;
        }

        return null;
    }    
}
