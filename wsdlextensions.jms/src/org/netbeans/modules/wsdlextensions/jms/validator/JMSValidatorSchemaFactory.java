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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.wsdlextensions.jms.validator;

import java.io.InputStream;
import org.netbeans.modules.xml.wsdl.validator.spi.ValidatorSchemaFactory;

import org.netbeans.modules.wsdlextensions.jms.JMSQName;

/**
 * This class implements ValidatorSchemaFactory interface.
 *
 * @author Sun Microsystems
 */
public class JMSValidatorSchemaFactory extends ValidatorSchemaFactory{
    static final String jmsXSDUrl = "/org/netbeans/modules/wsdlextensions/jms/resources/jms-ext.xsd";
    
    public String getNamespaceURI() {
        return "http://schemas.sun.com/jbi/wsdl-extensions/jms/";
    }
    
    public InputStream getSchemaInputStream() {
        InputStream is = JMSValidatorSchemaFactory.class.getResourceAsStream(jmsXSDUrl);
        return is;
    }
}
