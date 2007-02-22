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

package org.netbeans.modules.xml.wsdlextui.property;

import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;

/**
 *
 * @author skini
 */
public class SOAPValidatorSchemaFactory extends ValidatorSchemaFactory{
    static final String soapXSDUrl = "/org/netbeans/modules/xml/wsdlextui/resources/soap/soap.xsd";
    
    @Override
    public String getNamespaceURI() {
        return "http://schemas.xmlsoap.org/wsdl/soap/";
    }
    
//    @Override
    public InputStream getSchemaInputStream() {
        return SOAPValidatorSchemaFactory.class.getResourceAsStream(soapXSDUrl);
    }
    
     /**
     * Returns the Inputstream related to this schema
     */
    public Source getSchemaSource() {
        InputStream in = SOAPValidatorSchemaFactory.class.getResourceAsStream(soapXSDUrl);
        Source s = new StreamSource(in);
        s.setSystemId(SOAPValidatorSchemaFactory.class.getResource(soapXSDUrl).toString());
        return s;
    }
}
