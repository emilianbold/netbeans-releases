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
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openide.util.NbBundle;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.ExtensibilityElement;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.Port;
import org.netbeans.modules.xml.wsdl.model.Service;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPAddress;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBody;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPHeader;

/**
 * SoapBindingSupport.java
 *
 * Created on February 2, 2006, 3:35 PM
 *
 * @author Bing Lu
 * @author Jun Qian
 */
public class SoapBindingSupport implements BindingSupport {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.SoapBindingSupport"); // NOI18N
    
    private final QName mEnvelopeQName  = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope"); // NOI18N
    
    private final QName mBodyQName = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"); // NOI18N
    
    private final QName mHeaderQName = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header"); // NOI18N
    
    private final ResourceBundle mRb = ResourceBundle.getBundle("org.netbeans.modules.compapp.test.wsdl.Bundle"); // NOI18N
    
    private Binding mBinding;
    
    private Definitions mDefinition;
    
    private SchemaTypeLoader mSchemaTypeLoader;
    
    /** Creates a new instance of SoapBindingSupport */
    public SoapBindingSupport(
            Binding binding, 
            Definitions definition, 
            SchemaTypeLoader schemaTypeLoader) {
        mBinding = binding;
        mDefinition = definition;
        mSchemaTypeLoader = schemaTypeLoader;
    }
    
    public String[] getEndpoints() {
        List<String> result = new ArrayList<String>();
        
        for (Service service : mDefinition.getServices()) {
            for (Port port : service.getPorts()) {
                if (port.getBinding().get() == mBinding) {
                    List<ExtensibilityElement> eeList = 
                            port.getExtensibilityElements();
                    SOAPAddress soapAddress = (SOAPAddress)
                            Util.getAssignableExtensiblityElement(
                            eeList, SOAPAddress.class);
                    if (soapAddress != null) {
                        result.add(soapAddress.getLocation());
                    }
                }
            }
        }
        return (String[])result.toArray(new String[0]);
    }
    
    public String buildRequest(
            BindingOperation bindingOperation, 
            Map params) 
            throws Exception {
        
        boolean buildOptional = false;
        Object opt = params.get(BUILD_OPTIONAL);
        if (opt == null) {
            buildOptional = false;
        } else {
            buildOptional = ((Boolean)opt).booleanValue();
        }
        SoapBindingOperationSupport bindingOperationSupport =
                new SoapBindingOperationSupport(mBinding, bindingOperation);
        boolean inputSoapEncoded = bindingOperationSupport.isInputSoapEncoded();
        SampleXmlUtil xmlGenerator = new SampleXmlUtil(inputSoapEncoded);
        xmlGenerator.setIgnoreOptional(!buildOptional);
        
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        cursor.toNextToken();
        cursor.beginElement(mEnvelopeQName);
        
//      if(inputSoapEncoded) {
        cursor.insertNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
        cursor.insertNamespace("xsd", "http://www.w3.org/2001/XMLSchema"); // NOI18N
//      }
        cursor.insertAttributeWithValue("schemaLocation", // NOI18N
                "http://www.w3.org/2001/XMLSchema-instance", // NOI18N
                "http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/"); // NOI18N
        
        cursor.toFirstChild();
        
        cursor.beginElement(mBodyQName);
        cursor.toFirstChild();
        
        if(bindingOperationSupport.isRpc()) {
            buildRpcRequest(bindingOperationSupport, cursor, xmlGenerator);
        } else {
            buildDocumentRequest(bindingOperationSupport, cursor, xmlGenerator);
        }
        
        addHeaders(bindingOperation, cursor, xmlGenerator);
        cursor.dispose();
        
        try {
            return Util.getPrettyText(object);
        } catch(Exception e) {
            return object.xmlText();
        }
    }
    
    private void addHeaders(
            BindingOperation bindingOperation,
            XmlCursor cursor,
            SampleXmlUtil xmlGenerator)
            throws Exception {
        
        List<ExtensibilityElement> eeList =
                bindingOperation.getBindingInput().getExtensibilityElements();
        List headers =
                Util.getAssignableExtensiblityElementList(eeList, SOAPHeader.class);
        if (headers.isEmpty()) {
            return;
        }
        // reposition
        cursor.toStartDoc();
        cursor.toChild(mEnvelopeQName);
        cursor.toFirstChild();
        
        cursor.beginElement(mHeaderQName);
        cursor.toFirstChild();
        
        for (int i = 0, I = headers.size(); i < I; i++) {
            SOAPHeader header = (SOAPHeader) headers.get(i);
            String messageName = header.getMessage().get().getName(); 
            String partName = header.getPart();
            
            for (Message message : mDefinition.getMessages()) {
                if (message.getName().equals(messageName)) {
                    for (Part part : message.getParts()) {
                        if (part.getName().equals(partName)) {
                            if(part != null) {
                                createElementForPart(part, cursor, xmlGenerator);
                            } else {
                                mLog.log(Level.SEVERE,
                                        NbBundle.getMessage(SoapBindingSupport.class, 
                                        "LBL_Header_has_missing_part", partName)); // NOI18N
                            }
                            break;
                        }
                    }
                    break;  
                }
            }
        }
    }
    
    private void createElementForPart(
            Part part, 
            XmlCursor cursor, 
            SampleXmlUtil xmlGenerator) 
            throws Exception {
                
        if (part.getElement() != null) {
            QName elementName = part.getElement().getQName(); 
            cursor.beginElement(elementName);
            SchemaGlobalElement elm = mSchemaTypeLoader.findElement(elementName);
            if(elm != null) {
                cursor.toFirstChild();
                xmlGenerator.createSampleForType(elm.getType(), cursor);
            }
            cursor.toParent();
        } else {
            QName typeName = part.getType().getQName();
            cursor.beginElement(new QName(mDefinition.getTargetNamespace(), part.getName()));
            SchemaType type = mSchemaTypeLoader.findType(typeName);
            if(type != null) {
                cursor.toFirstChild();
                xmlGenerator.createSampleForType(type, cursor);
            }
            cursor.toParent();
        }
    }
    
    private void buildDocumentRequest(
            SoapBindingOperationSupport bindingOperationSupport,
            XmlCursor cursor,
            SampleXmlUtil xmlGenerator) 
            throws Exception {
        
        Part[] parts = bindingOperationSupport.getInputParts();
        if(parts.length > 1)
            mLog.log(Level.SEVERE,
                    NbBundle.getMessage(SoapBindingSupport.class,
                    "LBL_Document_style_operation_cannot_have_more_than_one_part")); // NOI18N
        
        for (int i = 0; i < parts.length; i++) {
            XmlCursor c = cursor.newCursor();
            c.toLastChild();
            createElementForPart(parts[i], c, xmlGenerator);
            c.dispose();
        }
    }
    
    private void buildRpcRequest(
            SoapBindingOperationSupport bindingOperationSupport,
            XmlCursor cursor,
            SampleXmlUtil xmlGenerator) 
            throws Exception {
        
        // rpc requests use the bindingOperation name as root element
        BindingOperation bindingOperation = bindingOperationSupport.getBindingOperation();
        List list = bindingOperation.getBindingInput().getExtensibilityElements();
        SOAPBody body = (SOAPBody) Util.getAssignableExtensiblityElement(list, SOAPBody.class);
        
        String ns = mDefinition.getTargetNamespace();
        if(body != null && body.getNamespace() != null) {
            ns = body.getNamespace();
        }
        cursor.beginElement(new QName(ns, bindingOperation.getName()));
        if(xmlGenerator.isSoapEnc()) {
            cursor.insertAttributeWithValue(
                    new QName("http://schemas.xmlsoap.org/soap/envelope/",  // NOI18N
                    "encodingStyle"), // NOI18N
                    "http://schemas.xmlsoap.org/soap/encoding/"); // NOI18N
        }
        
        Part[] inputParts = bindingOperationSupport.getInputParts(); 
        for (int i = 0; i < inputParts.length; i++) {
            Part part = inputParts[i];
            XmlCursor c = cursor.newCursor();
            c.toLastChild();
            c.insertElement(part.getName());
            c.toPrevToken();
            
            if (part.getType() != null && part.getType().getQName() != null) {
                SchemaType type = mSchemaTypeLoader.findType(part.getType().getQName());          
                xmlGenerator.createSampleForType(type, c);
            } else if (part.getElement() != null) { 
                // This is not BP 1.0 compliant. 
                createElementForPart(part, c, xmlGenerator);
            } else {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(SoapBindingSupport.class,
                        "LBL_Type_cannot_be_found",  // NOI18N
                        part.getType().getQName()));
            }
            c.dispose();
        }
    }
}
