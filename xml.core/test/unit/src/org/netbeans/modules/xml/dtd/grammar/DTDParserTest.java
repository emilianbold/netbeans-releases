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

package org.netbeans.modules.xml.dtd.grammar;

import java.io.*;
import java.net.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;
import org.openide.xml.*;
import org.openide.util.Lookup;
import org.netbeans.modules.xml.core.tree.ModuleEntityResolver;
import junit.framework.*;

/**
 * It tests if internal and external DTD is properly parsed.
 *
 * Warning: this test has knowledge for following resource files:
 * email.xml and email.dtd.
 *
 * @author Petr Kuzel
 */
public class DTDParserTest extends TestCase {
    
    public DTDParserTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testParse() {
        
        try {
            DTDParser parser = new DTDParser();
            InputSource in = new InputSource();
            URL url = getClass().getResource("email.xml");
            in.setSystemId(url.toExternalForm());
            in.setByteStream(url.openConnection().getInputStream());
            DTDGrammar dtd = parser.parse(in);    
            
            assertTrue("Missing entity!", dtd.entities.contains("testExternalEntity"));
            assertTrue("Missing notation!", dtd.notations.contains("testNotation"));
            assertTrue("Missing element!", dtd.elementDecls.keySet().contains("testANYElement"));
            assertTrue("Missing attribute!", dtd.attrDecls.keySet().contains("subject"));
            
            // ANY elements must contain all declared
            Set all = (Set) dtd.elementDecls.get("testANYElement");
            assertTrue("ANY must contain all declared!", all.containsAll(dtd.elementDecls.keySet()));

            // EMPTY must be empty
            assertTrue("EMPTY must be empty!", ((Set)dtd.elementDecls.get("attachment")).isEmpty());
            
            // #PCDATA mus be empty
            assertTrue("#PCDATA must be empty!", ((Set)dtd.elementDecls.get("name")).isEmpty());

        } catch (Exception ex) {
            // Add your test code below by replacing the default call to fail.
            ex.printStackTrace();
            fail(ex.toString());
        }
                
    }
    
}
