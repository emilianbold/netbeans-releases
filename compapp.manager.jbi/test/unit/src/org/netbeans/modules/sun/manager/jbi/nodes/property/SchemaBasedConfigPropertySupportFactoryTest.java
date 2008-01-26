/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
import javax.management.Attribute;
import junit.framework.TestCase;
import org.netbeans.modules.sun.manager.jbi.editors.ComboBoxPropertyEditor;
import org.netbeans.modules.sun.manager.jbi.editors.PasswordEditor;
import org.netbeans.modules.sun.manager.jbi.management.model.OldJBIComponentConfigurationDescriptor;
import org.netbeans.modules.sun.manager.jbi.management.OldConfigurationMBeanAttributeInfo;
import org.netbeans.modules.xml.schema.model.Schema;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author jqian
 */
public class SchemaBasedConfigPropertySupportFactoryTest extends TestCase {
    
    private Schema schema;
    
    private OldJBIComponentConfigurationDescriptor descriptor;

    public SchemaBasedConfigPropertySupportFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {        
        URI xsdURI = getClass().getResource(
                "resources/sun-http-binding-config.xsd").toURI();
        File xsdFile = new File(xsdURI);        
        String schemaText = getContent(xsdFile);
        schema = OldSchemaBasedConfigPropertySupportFactory.getSchema(schemaText, "whatever");
        
        URI xmlURI = getClass().getResource(
                "resources/sun-http-binding-config.xml").toURI();
        File xmlFile = new File(xmlURI);        
        String xmlText = getContent(xmlFile);
        descriptor = OldJBIComponentConfigurationDescriptor.parse(xmlText);        
    }

    protected void tearDown() throws Exception {        
    }
    
    public void testGetBaseTypeName() {
        // integer
        String type = OldSchemaBasedConfigPropertySupportFactory.
            getGlobalSimpleTypeName(schema, "OutboundThreads");
        assertEquals(type, "tns:SimpleRestrictedThreadType");
        
        // boolean
        type = OldSchemaBasedConfigPropertySupportFactory.
            getGlobalSimpleTypeName(schema, "UseJVMProxySettings");
        assertEquals(type, "xsd:boolean");
        
        // string
        type = OldSchemaBasedConfigPropertySupportFactory.
            getGlobalSimpleTypeName(schema, "ProxyHost");
        assertEquals(type, "tns:SimpleStringType");

        // string enumeration
        type = OldSchemaBasedConfigPropertySupportFactory.
            getGlobalSimpleTypeName(schema, "ProxyType");
        assertEquals(type, "tns:ProxyTypeSimpleType"); 
        
        // password
        type = OldSchemaBasedConfigPropertySupportFactory.
            getGlobalSimpleTypeName(schema, "ProxyPassword");
        assertEquals(type, "tns:SimpleStringType"); 
        
        // tabular data
        type = OldSchemaBasedConfigPropertySupportFactory.
            getGlobalSimpleTypeName(schema, "ApplicationConfigurations");
        assertEquals(type, null); 
    }

    public void testGetPropertySupport() throws Exception {
        
        // integer
        PropertySupport propSupport = OldSchemaBasedConfigPropertySupportFactory.
            getPropertySupport(schema, null, 
                new Attribute("OutboundThreads", 4), 
                new OldConfigurationMBeanAttributeInfo(
                descriptor.getChild("OutboundThreads"), "java.lang.Integer", true, true, false));
        assertTrue(propSupport.getValue() instanceof Integer);

        // boolean
        propSupport = OldSchemaBasedConfigPropertySupportFactory.
            getPropertySupport(schema, null, 
                new Attribute("UseJVMProxySettings", true), 
                new OldConfigurationMBeanAttributeInfo(
                descriptor.getChild("UseJVMProxySettings"), "java.lang.Boolean", true, true, false));
        assertTrue(propSupport.getValue() instanceof Boolean);
        
        // string
        propSupport = OldSchemaBasedConfigPropertySupportFactory.
            getPropertySupport(schema, null, 
                new Attribute("ProxyHost", "localhost"), 
                new OldConfigurationMBeanAttributeInfo(
                descriptor.getChild("ProxyHost"), "java.lang.String", true, true, false));
        assertTrue(propSupport.getValue() instanceof String);
        assertFalse(propSupport.getPropertyEditor() instanceof ComboBoxPropertyEditor);

        // string enumeration
        propSupport = OldSchemaBasedConfigPropertySupportFactory.
            getPropertySupport(schema, null, 
                new Attribute("ProxyType", "SOCKS"), 
                new OldConfigurationMBeanAttributeInfo(
                descriptor.getChild("ProxyType"), "java.lang.String", true, true, false));
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
        propSupport = OldSchemaBasedConfigPropertySupportFactory.
            getPropertySupport(schema, null, 
                new Attribute("ProxyPassword", "somePassword"), 
                new OldConfigurationMBeanAttributeInfo(
                descriptor.getChild("ProxyPassword"), "java.lang.String", true, true, false));
        assertTrue(propSupport.getValue() instanceof String);
        assertTrue(propSupport.getPropertyEditor() instanceof PasswordEditor);
    }


    private String getContent(File file) {
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
