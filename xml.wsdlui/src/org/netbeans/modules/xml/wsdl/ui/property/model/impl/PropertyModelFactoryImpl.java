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
 * PropertyModelFactoryImpl.java
 *
 * Created on January 26, 2007, 11:36 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.wsdl.ui.property.model.impl;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElements;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.WSDLExtensibilityElementsFactory;
import org.netbeans.modules.xml.wsdl.ui.extensibility.model.XMLSchemaFileInfo;
import org.netbeans.modules.xml.wsdl.ui.property.model.ElementProperties;
import org.netbeans.modules.xml.wsdl.ui.property.model.PropertyModelException;
import org.netbeans.modules.xml.wsdl.ui.property.model.PropertyModelFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author radval
 */
public class PropertyModelFactoryImpl extends PropertyModelFactory {
   
    private Logger logger = Logger.getLogger(PropertyModelFactoryImpl.class.getName());
    
    private Map<QName, ElementProperties> elementToPropertyMap = new HashMap<QName, ElementProperties>();
    
    private Exception lastError;
    
    private QName lastElement;
    
    private MyErrorHandler errorHandler = new MyErrorHandler();
        
    private javax.xml.validation.Schema propertySchema = null;
    
    private URL propertySchemaUrl = PropertyModelFactoryImpl.class.getResource("/org/netbeans/modules/xml/wsdl/ui/property/model/propertyCustomization.xsd");
        
    
    /** Creates a new instance of PropertyModelFactoryImpl */
    public PropertyModelFactoryImpl() throws PropertyModelException {
        try {
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            sf.setErrorHandler(errorHandler);
            propertySchema = sf.newSchema(propertySchemaUrl);
        } catch(Exception ex) {
            throw new PropertyModelException(ex);
        }
    }

    @Override
    public ElementProperties getElementProperties(QName elementQName) throws PropertyModelException {
        ElementProperties ep = elementToPropertyMap.get(elementQName);

        if(ep != null) {
            return ep;
        }
        
        try {
            WSDLExtensibilityElements elements = WSDLExtensibilityElementsFactory.getInstance().getWSDLExtensibilityElements();
            XMLSchemaFileInfo info = elements.getXMLSchemaFileInfo(elementQName.getNamespaceURI());
            if(info != null) {
                Schema s = info.getSchema();
                if(s != null) {
                    GlobalElement ge = findElement(elementQName, s);
                    if(ge != null) {
                        processGlobalElementAnnotation(elementQName, ge);
                    }
                }
            }
        } catch(Throwable ex) {
            throw new PropertyModelException(ex);
        }
        
        ep = elementToPropertyMap.get(elementQName);
        return ep;
    }
    
    private GlobalElement findElement(QName elementQName, Schema schema) throws Exception {
        GlobalElement ge = null;
        SchemaModel sModel = schema.getModel();
        ge = sModel.findByNameAndType(elementQName.getLocalPart(), GlobalElement.class);
        return ge;
    }
    
    void processGlobalElementAnnotation(QName elementQName, GlobalElement ge) throws Exception {
        Annotation a = ge.getAnnotation();
        if(a != null) {
            Collection<AppInfo> appInfos =  a.getAppInfos();
            processAppInfos(elementQName, appInfos);
        }
    }
    
    void processAppInfos(QName elementQName, Collection<AppInfo> appInfos) throws Exception {
        Iterator<AppInfo> it = appInfos.iterator();
        while(it.hasNext()) {
            AppInfo ainfo = it.next();
            ElementProperties ep = processAppInfo(elementQName, ainfo);
            if(ep != null) {
                elementToPropertyMap.put(elementQName, ep);
            }
        }
    }
    
    ElementProperties processAppInfo(QName elementQName, AppInfo appInfo) throws Exception {
       ElementProperties ep = null;
       
       Element appInfoElement = appInfo.getAppInfoElement();
       if(appInfoElement != null) {
           NodeList list = appInfoElement.getChildNodes();
           Node elementProperties = null;
           if (list != null) {
               for (int i = 0; i < list.getLength(); i++) {
                       Node node = list.item(i);
                       if (node.getNodeType() == Node.ELEMENT_NODE) {
                           if (node.getNamespaceURI().equals(PropertyModelFactory.PROP_NAMESPACE) && node.getLocalName().equals("ElementProperties")) {
                               elementProperties = node;
                               break;
                           }
                       }
               }
           }
           if (elementProperties != null) {
               lastElement = elementQName;
               
/*               Validator v = propertySchema.newValidator();
               v.setErrorHandler(errorHandler);
               DOMSource s = new DOMSource(elementProperties);
               v.validate(s);*/
            
               ep = new ElementProperties();
               ep.readNode(elementProperties);
           }
           
       }
       
       if(lastError != null) {
           throw new PropertyModelException("Exception occured while parsing annotation for element "+ elementQName);
       }
       
       return ep;
       
    }
    
    class MyErrorHandler implements ErrorHandler {
        
        public void error(SAXParseException exception) throws SAXException {
            lastError = exception;
            logger.log(Level.SEVERE, "Exception occured while parsing annotation for element " + lastElement, exception );
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            lastError = exception;
            logger.log(Level.SEVERE, "Exception occured while parsing annotation for element " + lastElement, exception );
        }

        public void warning(SAXParseException exception) throws SAXException {
        }
        


    }
}
