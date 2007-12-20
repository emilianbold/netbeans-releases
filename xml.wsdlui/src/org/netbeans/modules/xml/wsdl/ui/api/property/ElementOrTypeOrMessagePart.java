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
package org.netbeans.modules.xml.wsdl.ui.api.property;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.actions.NameGenerator;
import org.netbeans.modules.xml.wsdl.ui.api.property.ElementOrTypeOrMessagePartProvider.ParameterType;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

public class ElementOrTypeOrMessagePart {
    private GlobalElement mElement;
    private GlobalType mType;
    private Part mPart;
    private Message mMessage;
    private QName mQName;
    private ParameterType pType = ParameterType.NONE;
    private WSDLModel mModel;
    private String mPartName;

    public ElementOrTypeOrMessagePart(QName elementOrTypeQName, WSDLModel model, ParameterType elementOrType) {
        this.pType = elementOrType;
        mQName = elementOrTypeQName;
        mModel = model;
        if (pType == ParameterType.ELEMENT) {
            mElement = Utility.findGlobalElement(model, elementOrTypeQName);
        } else if (pType == ParameterType.TYPE){
            mType = Utility.findGlobalType(model, elementOrTypeQName);
        }
    }
    
    public Part getMessagePart() {
        return mPart;
    }

    public ParameterType getParameterType() {
        return pType;
    }

    public ElementOrTypeOrMessagePart(QName messageQName, WSDLModel model, String partName) {
        pType = ParameterType.MESSAGEPART;
        mModel = model;
        mQName = messageQName;
        mPartName = partName;
        mMessage = mModel.findComponentByName(messageQName, Message.class);
        if (mMessage != null) {
            for (Part part : mMessage.getParts()) {
                if (part.getName().equals(partName)) {
                    mPart = part;
                    break;
                }
            }
        }
    }

    public ElementOrTypeOrMessagePart(GlobalElement element, WSDLModel model) {
        mElement = element;
        mModel = model;
        pType = ParameterType.ELEMENT;
    }

    public ElementOrTypeOrMessagePart(GlobalType type, WSDLModel model) {
        mType= type;
        mModel = model;
        pType = ParameterType.TYPE;
    }
    
    public ElementOrTypeOrMessagePart(Part part, WSDLModel model) {
        mPart= part;
        mMessage = (Message) part.getParent();
        mModel = model;
        pType = ParameterType.MESSAGEPART;
    }
    
    public GlobalElement getElement() {
        return mElement;
    }
    
    public GlobalType getType() {
        return mType;
    }
    
    @Override
    public String toString() {
        if (mQName != null) {
            String str = Utility.fromQNameToString(mQName);
            if (pType == ParameterType.MESSAGEPART) {
                str = str + "/" + mPartName;
            }
            return str;
        }
        
        String namespace = null;
        String localPart = "";
        if (mElement != null) {
            namespace = mElement.getModel().getSchema().getTargetNamespace();
            localPart = mElement.getName();
        }
        if (mType != null) {
            namespace = mType.getModel().getSchema().getTargetNamespace();
            localPart = mType.getName();
        }
        
        if (mMessage != null) {
            namespace = mMessage.getModel().getDefinitions().getTargetNamespace();
            localPart = mMessage.getName();
        }
        
        if (namespace == null) {
            return localPart;
        }
        if (mModel == null) {
            return new QName(namespace, localPart).toString();
        }
        String namespacePrefix = Utility.getNamespacePrefix(namespace, mModel);
        if (namespacePrefix == null) {
            namespacePrefix = NameGenerator.getInstance().generateNamespacePrefix(null, mModel);
            boolean isInTransaction = Utility.startTransaction(mModel);
            ((AbstractDocumentComponent)mModel.getDefinitions()).addPrefix(namespacePrefix, namespace);
            
            Utility.endTransaction(mModel, isInTransaction);
        }
        if (pType == ParameterType.MESSAGEPART) {
            return namespacePrefix + ":" + localPart + "/" + mPart.getName();
        }
        return namespacePrefix + ":" + localPart;
    }
}
