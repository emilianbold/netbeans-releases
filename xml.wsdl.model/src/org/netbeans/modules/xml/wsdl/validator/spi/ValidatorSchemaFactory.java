
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

package org.netbeans.modules.xml.wsdl.validator.spi;

import java.io.InputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.ls.LSResourceResolver;


/**
 * Factory for getting the schema inputstream.
 * This will be plugged in into WSDLSchemaValidator to support 
 * extensibility element schema validation. 
 * 
 *
 * @author Shivanand Kini
 * 
 */
public abstract class ValidatorSchemaFactory {
    /**
     * Returns the targetnamespace of the schema
     */
    public abstract String getNamespaceURI();
    
  
    /**
     * Returns the Inputstream related to this schema
     */
    public abstract Source getSchemaSource();
    
    /**
     * Returns the LSResourceResolver related to this schema
     * for resolution of resources defined in schema like import location etc
     */
    public  LSResourceResolver getLSResourceResolver() {
        return null;
    }
    
    
            
}
