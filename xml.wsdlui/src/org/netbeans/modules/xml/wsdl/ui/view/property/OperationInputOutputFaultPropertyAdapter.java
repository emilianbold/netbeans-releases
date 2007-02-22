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

package org.netbeans.modules.xml.wsdl.ui.view.property;

import javax.xml.namespace.QName;

import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.OperationParameter;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.ui.commands.ConstraintNamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.commands.NamedPropertyAdapter;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.Utility;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
import org.openide.ErrorManager;

/**
 * 
 * @author radval
 *
 */
public class OperationInputOutputFaultPropertyAdapter extends ConstraintNamedPropertyAdapter implements NamedPropertyAdapter {
    
    private OperationParameter mOParameter;
    
    public OperationInputOutputFaultPropertyAdapter(WSDLComponent wsdlComponent) {
        super(wsdlComponent);
        this.mOParameter  = (OperationParameter) wsdlComponent;
    }
     
     public String getMessage() {
         NamedComponentReference message = mOParameter.getMessage();
         if(message == null) {
             return "";
         }
         QName messageQName = message.getQName();
         return Utility.fromQNameToString(messageQName);
     }
     
     public void setMessage(String message) {
         if(message != null) {
             try {
                 org.netbeans.modules.xml.wsdl.ui.common.QName messageQName = org.netbeans.modules.xml.wsdl.ui.common.QName.getQNameFromString(message);
                 if(messageQName == null) {
                     mOParameter.getModel().startTransaction();
                     mOParameter.setMessage(null);
                     mOParameter.getModel().endTransaction();
                 } else {
                     
                     String ns = messageQName.getNamespaceURI();
                     String prefix = messageQName.getPrefix();
                     if(ns == null || ns.trim().equals("")) {
                         ns = Utility.getNamespaceURI(prefix, mOParameter.getModel());
                     }
                     
                     QName qname = null;
                     if (ns != null) {
                         qname = new QName(ns, messageQName.getLocalName());
                     }
                     
                     if(qname != null) {
                         Message msg = mOParameter.getModel().findComponentByName(qname, Message.class);
                         if (msg == null) {
                             ErrorManager.getDefault().notify(ErrorManager.ERROR, new Exception ("Not a valid type"));
                         } else {
                             mOParameter.getModel().startTransaction();
                             mOParameter.setMessage(mOParameter.createReferenceTo(msg, Message.class));
                             
                             mOParameter.getModel().endTransaction();
                         }
                     }
                 }
             } catch (Exception e) {
                 ErrorManager.getDefault().notify(e);
             }
         }
     }

    @Override
    public boolean isNameExists(String name) {
        boolean exists = false;
        Operation operation = (Operation) mOParameter.getParent();
        if (operation.getInput() != null) {
            exists = operation.getInput().getName().equals(name);
        }
        
        if (operation.getOutput() != null) {
            exists = operation.getOutput().getName().equals(name);
        }
        
        return exists;
    }

}
