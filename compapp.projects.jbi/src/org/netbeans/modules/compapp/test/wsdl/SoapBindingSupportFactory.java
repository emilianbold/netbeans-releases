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

import java.util.List;
import org.apache.xmlbeans.SchemaTypeLoader;
import java.util.logging.Logger;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.extensions.soap.SOAPBinding;

/**
 * SoapBindingSupportFactory.java
 *
 * Created on February 2, 2006, 3:24 PM
 *
 * @author Bing Lu
 */
public class SoapBindingSupportFactory implements BindingSupportFactory {
    private static final Logger mLog = Logger.getLogger("org.netbeans.modules.compapp.test.wsdl.SoapBindingSupportFactory"); // NOI18N
    
    private static final String SOAP_TRANSPORT_URI = 
            "http://schemas.xmlsoap.org/soap/http"; // NOI18N
    
    /** Creates a new instance of SoapBindingSupportFactory */
    public SoapBindingSupportFactory() {
    }
    
   public boolean supports(Binding binding) {
      List eeList = binding.getExtensibilityElements();
      SOAPBinding soapBinding = (SOAPBinding) 
              Util.getAssignableExtensiblityElement(eeList, SOAPBinding.class);
      return soapBinding == null ? false : 
         soapBinding.getTransportURI().startsWith(SOAP_TRANSPORT_URI);         
   }
   
   public BindingSupport createBindingSupport(
           Binding binding, 
           Definitions definition, 
           SchemaTypeLoader schemaTypeLoader) 
           throws Exception {
       return new SoapBindingSupport(binding, definition, schemaTypeLoader);
   }
}
