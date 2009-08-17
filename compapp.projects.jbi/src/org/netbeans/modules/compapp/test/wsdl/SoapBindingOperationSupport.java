/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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


package org.netbeans.modules.compapp.test.wsdl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Input;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Output;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding.Style;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPMessageBase.Use;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPOperation;
import org.openide.util.NbBundle;

/**
 * SoapBindingOperationSupport.java
 *
 * Created on February 2, 2006, 3:47 PM
 *
 *
 * @author Bing Lu
 * @author Jun Qian
 */
public class SoapBindingOperationSupport {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.SoapOperationSupport");  // NOI18N
    
    private Binding mBinding;
    private BindingOperation mBindingOperation;
    
    /**
     * Creates a new instance of SoapBindingOperationSupport
     */
    public SoapBindingOperationSupport(Binding binding, 
            BindingOperation bindingOperation) {
        mBinding = binding;
        mBindingOperation = bindingOperation;
    }
    
    public boolean isInputSoapEncoded() {
        Operation operation = mBindingOperation.getOperation().get();
        String operationName = operation.getName();
        String bindingName = ((Binding) mBindingOperation.getParent()).getName();
        Input input = operation.getInput();
        Output output = operation.getOutput();
        if (output != null) {
            if (input == null) {
                String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                        "MSG_No_Support_for_Notification_Style", // NOI18N
                        operationName, bindingName); 
                throw new RuntimeException(msg);   
            } else {
                List<WSDLComponent> children = operation.getChildren();  
                if (children.get(0) instanceof Output) {
                    String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                        "MSG_No_Support_for_Solicit_Response_Style", // NOI18N
                        operationName, bindingName); 
                    throw new RuntimeException(msg);    
                }
            }
        } else {
            if (input == null) {
                String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                        "MSG_No_Support_for_Unknown_Style", // NOI18N
                        operationName, bindingName); 
                throw new RuntimeException(msg);   
            }
        }
        
        BindingInput bindingInput = mBindingOperation.getBindingInput();
        if (bindingInput == null) {
            String msg = NbBundle.getMessage(SoapBindingOperationSupport.class, 
                    "MSG_Missing_Binding_Operation_Input", // NOI18N
                    operationName, bindingName);
            throw new RuntimeException(msg);    
        }
        
        List<ExtensibilityElement> eeList = 
                bindingInput.getExtensibilityElements();
        SOAPBody soapBody = 
                (SOAPBody) Util.getAssignableExtensiblityElement(eeList, SOAPBody.class);
        
        if (soapBody != null && soapBody.getUse() == Use.ENCODED) { 
            Collection<String> encodingStyles = soapBody.getEncodingStyles();
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
        List eeList = mBindingOperation.getExtensibilityElements();
        SOAPOperation soapOperation = 
                (SOAPOperation) Util.getAssignableExtensiblityElement(
                eeList, SOAPOperation.class);
        
        if(soapOperation != null && soapOperation.getStyle() != null) {
            return soapOperation.getStyle() == Style.RPC; 
        }
        eeList = mBinding.getExtensibilityElements();
        SOAPBinding soapBinding = 
                (SOAPBinding) Util.getAssignableExtensiblityElement(
                eeList, SOAPBinding.class);
        
        return soapBinding != null && soapBinding.getStyle() == Style.RPC; 
    }
    
    public Part[] getInputParts() {
        ArrayList result = new ArrayList();
        Operation operation = mBindingOperation.getOperation().get();
        Message msg = operation.getInput().getMessage().get();
        List<ExtensibilityElement> eeList = 
                mBindingOperation.getBindingInput().getExtensibilityElements();
        SOAPBody body = 
                (SOAPBody) Util.getAssignableExtensiblityElement(
                eeList, SOAPBody.class);
        
        if (body == null || body.getParts() == null) {
            result.addAll(msg.getParts());
        } else {
            for (String partName : body.getParts()) {
//            Iterator i = body.getParts().iterator();
//            while (i.hasNext()) {
//                String partName = (String) i.next();
//                Part part = msg.getParts(partName);
//                
//                result.add(part);
                result.addAll(msg.getParts());
            }
        }
        
        return (Part[])result.toArray(new Part[0]);
    }
    
    public BindingOperation getBindingOperation() {
        return mBindingOperation;
    }
}
