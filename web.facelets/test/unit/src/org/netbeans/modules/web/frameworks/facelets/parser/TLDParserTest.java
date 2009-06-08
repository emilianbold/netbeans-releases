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

package org.netbeans.modules.web.frameworks.facelets.parser;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import javax.servlet.jsp.tagext.TagAttributeInfo;
import javax.servlet.jsp.tagext.TagInfo;
import javax.servlet.jsp.tagext.TagLibraryInfo;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Petr Pisl
 */
public class TLDParserTest extends TestCase {
    
    public TLDParserTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of parse method, of class org.netbeans.modules.web.frameworks.facelets.parser.TLDParser.
     */
    public void testParse() {
        System.out.println("parse");
        
        TagLibraryInfo libInfo = null;
        
        InputStream tld = this.getClass().getClassLoader().getResourceAsStream("org/netbeans/modules/web/frameworks/facelets/resources/ui.tld");
        TLDParser.Result result = TLDParser.parse(tld, libInfo);
        assertNotNull("The result cannot be null.", result);
        assertEquals("The prefix is not the ","ui", result.getPrefix());
        assertEquals("The uri is not ", "http://java.sun.com/jsf/facelets", result.getUri());
        assertEquals("The number of tags of " + result.getUri() + " was", 11, result.getTagInfos().size());
        
        
    }
    
}
