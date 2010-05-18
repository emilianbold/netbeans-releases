/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
