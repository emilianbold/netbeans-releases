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
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Message;
import javax.wsdl.Part;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.xml.namespace.QName;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.apache.xmlbeans.SchemaTypeLoader;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.openide.util.NbBundle;
import java.util.logging.Logger;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;

/**
 * SoapBindingSupport.java
 *
 * Created on February 2, 2006, 3:35 PM
 *
 * @author Bing Lu
 */
public class SoapBindingSupport implements BindingSupport {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.SoapBindingSupport"); // NOI18N
    
    private final QName mEnvelopeQName  = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Envelope"); // NOI18N
    
    private final QName mBodyQName = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Body"); // NOI18N
    
    private final QName mHeaderQName = new QName("http://schemas.xmlsoap.org/soap/envelope/", "Header"); // NOI18N
    
    private final ResourceBundle mRb = ResourceBundle.getBundle("org.netbeans.modules.compapp.test.wsdl.Bundle"); // NOI18N
    
    private Binding mBinding;
    
    private Definition mDefinition;
    
    private SchemaTypeLoader mSchemaTypeLoader;
    
    /** Creates a new instance of SoapBindingSupport */
    public SoapBindingSupport(Binding binding, Definition definition, SchemaTypeLoader schemaTypeLoader) {
        mBinding = binding;
        mDefinition = definition;
        mSchemaTypeLoader = schemaTypeLoader;
    }
    
    public String[] getEndpoints() {
        List result = new ArrayList();
        Map map = mDefinition.getServices();
        for(Iterator i = map.values().iterator(); i.hasNext(); ) {
            Service service = (Service) i.next();
            Map portMap = service.getPorts();
            for( Iterator i2 = portMap.values().iterator(); i2.hasNext(); ) {
                Port port = (Port) i2.next();
                if( port.getBinding() == mBinding) {
                    SOAPAddress soapAddress = (SOAPAddress) Util.getAssignableExtensiblityElement(port.getExtensibilityElements(), SOAPAddress.class);
                    result.add(soapAddress.getLocationURI());
                }
            }
        }
        return (String[])result.toArray( new String[0]);
    }
    
    public String buildRequest(BindingOperation bindingOperation, Map params) throws Exception {
        boolean buildOptional = false;
        Object opt = params.get(BUILD_OPTIONAL);
        if (opt == null) {
            buildOptional = false;
        } else {
            buildOptional = ((Boolean)opt).booleanValue();
        }
        SoapBindingOperationSupport bindingOperationSupport = new SoapBindingOperationSupport(mBinding, bindingOperation);
        boolean inputSoapEncoded = bindingOperationSupport.isInputSoapEncoded();
        SampleXmlUtil xmlGenerator = new SampleXmlUtil(inputSoapEncoded);
        xmlGenerator.setIgnoreOptional(!buildOptional);
        
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        cursor.toNextToken();
        cursor.beginElement(mEnvelopeQName);
        
//        if(inputSoapEncoded) {  
            cursor.insertNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"); // NOI18N
            cursor.insertNamespace("xsd", "http://www.w3.org/2001/XMLSchema"); // NOI18N
//        }        
        cursor.insertAttributeWithValue("schemaLocation", "http://www.w3.org/2001/XMLSchema-instance", "http://schemas.xmlsoap.org/soap/envelope/ http://schemas.xmlsoap.org/soap/envelope/"); // NOI18N
                
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
    
    private void addHeaders(BindingOperation bindingOperation, XmlCursor cursor, SampleXmlUtil xmlGenerator) throws Exception {
        List list = bindingOperation.getBindingInput().getExtensibilityElements();
        List headers = Util.getAssignableExtensiblityElementList(list, SOAPHeader.class);
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
            QName messageName = header.getMessage();
            String partName = header.getPart();
            Message message = mDefinition.getMessage(messageName);
            Part part = message.getPart(partName);
            
            if(part != null) {
                createElementForPart(part, cursor, xmlGenerator);
            } else {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(SoapBindingSupport.class, "LBL_Header_has_missing_part", partName)); // NOI18N
            }
        }
    }
    
    private void createElementForPart(Part part, XmlCursor cursor, SampleXmlUtil xmlGenerator) throws Exception {
        QName elementName = part.getElementName();
        if(elementName != null) {
            cursor.beginElement(elementName);
            SchemaGlobalElement elm = mSchemaTypeLoader.findElement(elementName);
            if(elm != null) {
                cursor.toFirstChild();
                xmlGenerator.createSampleForType(elm.getType(), cursor);
            }
            cursor.toParent();
        } else {
            QName typeName = part.getTypeName();
            cursor.beginElement(new QName(mDefinition.getTargetNamespace(), part.getName()));
            SchemaType type = mSchemaTypeLoader.findType(typeName);
            if(type != null) {
                cursor.toFirstChild();
                xmlGenerator.createSampleForType(type, cursor);
            }
            cursor.toParent();
        }
    }
    
    private void buildDocumentRequest(SoapBindingOperationSupport bindingOperationSupport,
            XmlCursor cursor,
            SampleXmlUtil xmlGenerator) throws Exception {
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
    
    private void buildRpcRequest(SoapBindingOperationSupport bindingOperationSupport,
            XmlCursor cursor,
            SampleXmlUtil xmlGenerator) throws Exception {
        // rpc requests use the bindingOperation name as root element
        BindingOperation bindingOperation = bindingOperationSupport.getBindingOperation();
        List list = bindingOperation.getBindingInput().getExtensibilityElements();
        SOAPBody body = (SOAPBody) Util.getAssignableExtensiblityElement(list, SOAPBody.class);
        
        String ns = mDefinition.getTargetNamespace();
        if(body != null && body.getNamespaceURI() != null) {
            ns = body.getNamespaceURI();
        }
        cursor.beginElement(new QName(ns, bindingOperation.getName()));
        if(xmlGenerator.isSoapEnc())
            cursor.insertAttributeWithValue(new QName("http://schemas.xmlsoap.org/soap/envelope/",  // NOI18N
                    "encodingStyle"), // NOI18N
                    "http://schemas.xmlsoap.org/soap/encoding/"); // NOI18N
        
        Part[] inputParts = bindingOperationSupport.getInputParts();
        for (int i = 0; i < inputParts.length; i++) {
            Part part = inputParts[i];
            XmlCursor c = cursor.newCursor();
            c.toLastChild();
            c.insertElement(part.getName());
            c.toPrevToken();
            
            SchemaType type = mSchemaTypeLoader.findType(part.getTypeName());
            if(type != null) {
                xmlGenerator.createSampleForType(type, c);
            } else {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(SoapBindingSupport.class,
                        "LBL_Type_cannot_be_found",  // NOI18N
                        part.getTypeName()));
            }
            c.dispose();
        }
    }
}
