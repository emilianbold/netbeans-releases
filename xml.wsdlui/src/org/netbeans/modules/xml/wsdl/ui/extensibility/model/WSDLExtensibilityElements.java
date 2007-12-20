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
 * Created on May 25, 2005
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.netbeans.modules.xml.wsdl.ui.extensibility.model;

import java.io.InputStream;

/**
 * @author radval
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface WSDLExtensibilityElements {

        public static final String ELEMENT_DEFINITIONS = "Definitions";// NOI18N

        public static final String ELEMENT_PORTTYPE_OPERATION = "PortTypeOperation";// NOI18N
        
        public static final String ELEMENT_MESSAGE = "Message";// NOI18N
        
        public static final String ELEMENT_BINDING = "Binding";// NOI18N
        
        public static final String ELEMENT_BINDING_OPERATION = "BindingOperation";// NOI18N
        
        public static final String ELEMENT_BINDING_OPERATION_INPUT = "BindingOperationInput";// NOI18N
        
        public static final String ELEMENT_BINDING_OPERATION_OUTPUT = "BindingOperationOutput";// NOI18N
        
        public static final String ELEMENT_BINDING_OPERATION_FAULT = "BindingOperationFault";// NOI18N
        
        public static final String ELEMENT_SERVICE = "Service";// NOI18N
        
        public static final String ELEMENT_SERVICE_PORT = "ServicePort";// NOI18N
        
        /**
         * get WSDLExtensibilityElement for a given wsdl extensibility element.
         * @param name name of WSDLExtensibilityElement, should be one of the String
         * constant defined in this interface.
         * @return WSDLExtensibilityElement
         */
        WSDLExtensibilityElement getWSDLExtensibilityElement(String name);
        
        /**
         * Get the array schema streams for all the extensions available
         * for wsdl editor. 
         * @return collection of schemas
         */
        InputStream[] getAllExtensionSchemas();
        
        /**
         * Get the XMLSchemaFileInfo based on name of the namespace of the schema
         * @param namespace
         * @return XMLSchemaFileInfo
         */
        XMLSchemaFileInfo getXMLSchemaFileInfo(String namespace);
        
        
        XMLSchemaFileInfo[] getAllXMLSchemaFileInfos();
}
