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
 * PropertyUtil.java
 *
 * Created on April 17, 2006, 12:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.PortType;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;

/**
 *
 * @author radval
 */
public class PropertyUtil {
    private static final String COLON = ":";// NOI18N
    /** Creates a new instance of PropertyUtil */
    public PropertyUtil() {
    }

    public static  String[] getAllPortTypes(WSDLModel model, boolean includeBlankItem) {
        ArrayList<String> portTypesList = new ArrayList<String>();
        if (includeBlankItem) {
            portTypesList.add("");
        }

        //ifist get all PortTypes in current wsdl document
        portTypesList.addAll(getAllAvailablePortTypes(model, model));

        //then get all port type in all explicitly imported documents
        for (WSDLModel imported : Utility.getImportedDocuments(model)) {
            portTypesList.addAll(getAllAvailablePortTypes(model, imported));
        }

        return portTypesList.toArray(new String[portTypesList.size()]);
    }


    private static List<String> getAllAvailablePortTypes(WSDLModel source,  WSDLModel document) {
        ArrayList<String> portTypesList = new ArrayList<String>();

        Definitions definition =  document.getDefinitions();
        
        for (PortType portType : definition.getPortTypes()) {
            String name = portType.getName();
            String targetNamespace = document.getDefinitions().getTargetNamespace();
            String prefix = Utility.getNamespacePrefix(targetNamespace, source);
            if(name != null) {
                if(prefix != null) {
                    String portTypeQNameStr = prefix + COLON + name;
                    portTypesList.add(portTypeQNameStr);
                } else {
                    portTypesList.add(name);
                }
            }
        }
        
        return  portTypesList;
    }

    public static String[] getAllMessages(WSDLModel model) {
        ArrayList<String> messageList = new ArrayList<String>();
        //messageList.add("");

        //first get all messages in current wsdl document
        messageList.addAll(getAllAvailableMessages(model, model));

        //then get all messages in all imported wsdl documents
        Collection<WSDLModel> allWsdls = Utility.getImportedDocuments(model);
        if (allWsdls != null) {
            for (WSDLModel doc : allWsdls) {
                messageList.addAll(getAllAvailableMessages(model, doc));

            }
        }
        return messageList.toArray(new String[messageList.size()]);
    }

    private static List<String> getAllAvailableMessages(WSDLModel source, WSDLModel document) {
        ArrayList<String> messageList = new ArrayList<String>();

        Definitions definition =  document.getDefinitions();
        for (Message msg : definition.getMessages()) {
            String name = msg.getName();
            String targetNamespace = definition.getTargetNamespace();
            String prefix = Utility.getNamespacePrefix(targetNamespace, source);
            if(name != null) {
                if(prefix != null) {
                    String messageQNameStr = prefix + COLON + name;
                    messageList.add(messageQNameStr);
                } else {
                    messageList.add(name);
                }
            }
            
        }

        return messageList;
    }


    public static String[] getAllBindings(WSDLModel model) {
        ArrayList<String> bindingList = new ArrayList<String>();
        bindingList.add("");

        //first get all binding in current wsdl document
        bindingList.addAll(getAllBindings(model, model));

        //then get all biniding in all explicitly imported documents
        for (WSDLModel doc : Utility.getImportedDocuments(model)) {
            bindingList.addAll(getAllBindings(model, doc));
        }

        return bindingList.toArray(new String[bindingList.size()]);
    }

    private static List<String> getAllBindings(WSDLModel source, WSDLModel document) {
        ArrayList<String> bindingList = new ArrayList<String>();

        Definitions definition =  document.getDefinitions();

        for (Binding binding : definition.getBindings()) {
            String name = binding.getName();
            String targetNamespace = definition.getTargetNamespace();
            String prefix = Utility.getNamespacePrefix(targetNamespace, source);
            if(name != null) {
                if(prefix != null) {
                    String messageQNameStr = prefix + COLON + name;
                    bindingList.add(messageQNameStr);
                } else {
                    bindingList.add(name);
                }
            }

        }

        return bindingList;
    }

    public static Message getMessage(MessageProvider prov, WSDLModel model) {
        Message msg = prov.getWSDLMessage();
        if (msg != null) {
            return msg;
        }
        
        String messageName = prov.getMessage();
        if (messageName == null) {
            return null;
        }
        String[] qnameParts = messageName.split(COLON);
        String prefix = "";
        String localPart = messageName;
        if (qnameParts.length > 1) {
            prefix = qnameParts[0];
            localPart = qnameParts[1];
        }
        String ns = Utility.getNamespaceURI(prefix, model);
        if (ns != null) {
            return model.findComponentByName(new QName(ns, localPart), Message.class);
        }
        return model.findComponentByName(localPart, Message.class);
    }
}
