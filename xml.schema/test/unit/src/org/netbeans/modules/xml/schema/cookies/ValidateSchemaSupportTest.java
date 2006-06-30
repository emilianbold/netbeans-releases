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

package org.netbeans.modules.xml.schema.cookies;

import java.net.URL;
import junit.framework.TestCase;
import org.netbeans.api.xml.cookies.CookieMessage;
import org.netbeans.api.xml.cookies.CookieObserver;
import org.xml.sax.InputSource;

/**
 * Tries to parse severel schemas with imports, includes and errors.
 *
 * @author Petr Kuzel
 */
public class ValidateSchemaSupportTest extends TestCase {

    public ValidateSchemaSupportTest(String testName) {
        super(testName);
    }

    /** Test of createParser method, of class org.netbeans.modules.xml.schema.cookies.ValidateSchemaSupport. */
    public void testSchemaSupport() throws Exception {
        System.out.println("testSchemaSupport");
        
        URL invalid = getClass().getResource("data/Invalid.xsd");
        URL simple = getClass().getResource("data/Simple.xsd");
        URL chameleon = getClass().getResource("data/Chameleon.xsd");
        URL imports = getClass().getResource("data/Import.xsd");
        URL include = getClass().getResource("data/Include.xsd");
        
        assertTrue("Invalid.xsd must not pass!",  validate(invalid) == false);
        assertTrue("Simple.xsd was marked as invalid!", validate(simple));
        assertTrue("Chameleon.xsd was marked as invalid!", validate(chameleon));
        assertTrue("Import.xsd was marked as invalid!", validate(imports));
        assertTrue("Include.xsd was marked as invalid!", validate(include));
    }
    
    public boolean validate(URL schema) throws Exception {
        InputSource in = new InputSource(schema.toExternalForm());        
        ValidateSchemaSupport support = new ValidateSchemaSupport(in);
        return support.validateXML(new CookieObserver() {
            public void receive(CookieMessage msg) {
                System.out.println("MSG: " + msg.getMessage());
            }
        });
    }
    
}
