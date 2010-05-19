/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

/*
 * SchemaBasedConfigPropertySupportFactoryTest.java
 * JUnit 4.x based test
 *
 * Created on August 3, 2007, 6:54 PM
 */

package org.netbeans.modules.sun.manager.jbi.nodes.property;

import java.beans.PropertyEditor;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.management.Attribute;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.modules.sun.manager.jbi.editors.ComboBoxPropertyEditor;
import org.netbeans.modules.sun.manager.jbi.editors.PasswordEditor;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationMBeanAttributeInfo;
import org.netbeans.modules.sun.manager.jbi.management.model.JBIComponentConfigurationParser;
import org.openide.nodes.PropertySupport;
import org.xml.sax.SAXException;
import static org.junit.Assert.*;
/**
 *
 * @author jqian
 */
public class SchemaBasedConfigPropertySupportFactoryTest  {

    private static JBIComponentConfigurationDescriptor configDescriptor;
    
    public SchemaBasedConfigPropertySupportFactoryTest() {        
    }

    @BeforeClass
    public static void setUpClass() throws URISyntaxException, 
            ParserConfigurationException, IOException, SAXException {
        
        URI xmlURI = SchemaBasedConfigPropertySupportFactoryTest.class.
                getResource("resources/sun-http-binding-jbi.xml").toURI();
        File xmlFile = new File(xmlURI);
        String xmlText = getContent(xmlFile);
        
        configDescriptor = JBIComponentConfigurationParser.parse(xmlText);
    }
    
    @Test
    public void testGetPropertySupport() throws Exception {
        
        // integer
        PropertySupport propSupport = SchemaBasedConfigPropertySupportFactory.
            getPropertySupport(null, 
                new Attribute("OutboundThreads", 4), 
                new JBIComponentConfigurationMBeanAttributeInfo(
                configDescriptor.getChild("OutboundThreads"), "java.lang.Integer", true, true, false));
        assertTrue(propSupport.getValue() instanceof Integer);

        // boolean
        propSupport = SchemaBasedConfigPropertySupportFactory.
            getPropertySupport(null, 
                new Attribute("UseJVMProxySettings", true), 
                new JBIComponentConfigurationMBeanAttributeInfo(
                configDescriptor.getChild("UseJVMProxySettings"), "java.lang.Boolean", true, true, false));
        assertTrue(propSupport.getValue() instanceof Boolean);
        
        // string
        propSupport = SchemaBasedConfigPropertySupportFactory.
            getPropertySupport(null, 
                new Attribute("ProxyHost", "localhost"), 
                new JBIComponentConfigurationMBeanAttributeInfo(
                configDescriptor.getChild("ProxyHost"), "java.lang.String", true, true, false));
        assertTrue(propSupport.getValue() instanceof String);
        assertFalse(propSupport.getPropertyEditor() instanceof ComboBoxPropertyEditor);

        // string enumeration
        propSupport = SchemaBasedConfigPropertySupportFactory.
            getPropertySupport(null, 
                new Attribute("ProxyType", "SOCKS"), 
                new JBIComponentConfigurationMBeanAttributeInfo(
                configDescriptor.getChild("ProxyType"), "java.lang.String", true, true, false));
        assertTrue(propSupport.getValue() instanceof String);
        PropertyEditor propEditor = propSupport.getPropertyEditor();
        assertTrue(propEditor instanceof ComboBoxPropertyEditor);
        ((ComboBoxPropertyEditor) propEditor).setValue("HTTP");
        try {
            ((ComboBoxPropertyEditor) propEditor).setValue("INVALID_VALUE");
            assertTrue(false);
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        // password
        propSupport = SchemaBasedConfigPropertySupportFactory.
            getPropertySupport(null, 
                new Attribute("ProxyPassword", "somePassword"), 
                new JBIComponentConfigurationMBeanAttributeInfo(
                configDescriptor.getChild("ProxyPassword"), "java.lang.String", true, true, false));
        assertTrue(propSupport.getValue() instanceof String);
        assertTrue(propSupport.getPropertyEditor() instanceof PasswordEditor);
    }
    
    private static String getContent(File file) {
        String ret = "";

        BufferedReader is = null;
        try {
            is = new BufferedReader(new FileReader(file));
            String inputLine;
            while ((inputLine = is.readLine()) != null) {
                ret += inputLine;
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }

        return ret;
   }    

}
