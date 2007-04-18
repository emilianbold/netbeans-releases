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


package org.netbeans.modules.compapp.test.wsdl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Message;
import javax.wsdl.OperationType;
import javax.wsdl.Part;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPOperation;
import java.util.logging.Logger;
import javax.wsdl.BindingInput;
import org.openide.util.NbBundle;

/**
 * SoapBindingOperationSupport.java
 *
 * Created on February 2, 2006, 3:47 PM
 *
 *
 * @author Bing Lu
 */
public class SoapBindingOperationSupport {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.SoapOperationSupport");  // NOI18N
    
    private Binding mBinding;
    private BindingOperation mBindingOperation;
    
    /**
     * Creates a new instance of SoapBindingOperationSupport
     */
    public SoapBindingOperationSupport(Binding binding, BindingOperation bindingOperation) {
        mBinding = binding;
        mBindingOperation = bindingOperation;
    }
    
    public boolean isInputSoapEncoded() {
        OperationType operationType = mBindingOperation.getOperation().getStyle();
        if (operationType.equals(OperationType.NOTIFICATION)) {
            String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                    "MSG_No_Support_for_Notification_Style"); // NOI18N
            throw new RuntimeException(msg);            
        } else if (operationType.equals(OperationType.SOLICIT_RESPONSE)) {
            String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                    "MSG_No_Support_for_Solicit_Response_Style"); // NOI18N
            throw new RuntimeException(msg);            
        } 
        
        BindingInput input = mBindingOperation.getBindingInput();
        if (input == null) {
            // If the wsdl file has been validated, this should not happen 
            // because the error should have already been caught above.
            // Just to be safe...
            String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                    "MSG_No_Support_for_Notification_Style"); // NOI18N
            throw new RuntimeException(msg);    
        }
        
        List list = input.getExtensibilityElements();
        SOAPBody body = (SOAPBody) Util.getAssignableExtensiblityElement(list, SOAPBody.class);
        
        if (body != null &&
                body.getUse() != null &&
                body.getUse().equalsIgnoreCase("encoded")) { // NOI18N
            List encodingStyles = body.getEncodingStyles();
            if (encodingStyles == null) {
                String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                    "MSG_Missing_EncodingStyle_for_Encoded_Use"); // NOI18N
                throw new RuntimeException(msg);
            } else {
                return encodingStyles.contains("http://schemas.xmlsoap.org/soap/encoding/"); // NOI18N
            }
        } else { 
            return false;
        }
    }
    
    public boolean isRpc() {
        List list = mBindingOperation.getExtensibilityElements();
        SOAPOperation soapOperation = (SOAPOperation) Util.getAssignableExtensiblityElement(list, SOAPOperation.class);
        
        if(soapOperation != null && soapOperation.getStyle() != null) {
            return soapOperation.getStyle().equalsIgnoreCase("rpc"); // NOI18N
        }
        list = mBinding.getExtensibilityElements();
        SOAPBinding soapBinding = (SOAPBinding) Util.getAssignableExtensiblityElement(list, SOAPBinding.class);
        
        return soapBinding != null &&
               soapBinding.getStyle() != null &&
               soapBinding.getStyle().equalsIgnoreCase("rpc"); // NOI18N
    }
    
    public Part[] getInputParts() {
        ArrayList result = new ArrayList();
        Message msg = mBindingOperation.getOperation().getInput().getMessage();
        List list = mBindingOperation.getBindingInput().getExtensibilityElements();
        SOAPBody body = (SOAPBody) Util.getAssignableExtensiblityElement(list, SOAPBody.class);
        
        if (body == null || body.getParts() == null) {
            result.addAll(msg.getOrderedParts(null));
        } else {
            Iterator i = body.getParts().iterator();
            while (i.hasNext()) {
                String partName = (String) i.next();
                Part part = msg.getPart(partName);
                
                result.add(part);
            }
        }
        
        return (Part[])result.toArray(new Part[0]);
    }
    
    public BindingOperation getBindingOperation() {
        return mBindingOperation;
    }
}
