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
package org.netbeans.modules.compapp.javaee.codegen.model;

import javax.xml.namespace.QName;
import junit.framework.TestCase;

/**
 *
 * @author gpatil
 */
public class EndpointTest extends TestCase {
    public EndpointTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
    }
    
    @Override
    protected void tearDown() throws Exception {
    }
    
    /**
     * Test of setEndPointType method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    public void testSetEndPointType() {
        Endpoint.EndPointType nEpt = null;
        Endpoint instance = new Endpoint();
        
        instance.setEndPointType(nEpt);
        assertEquals(null, instance.getEndPointType());
        
        nEpt = Endpoint.EndPointType.Provider;
        instance.setEndPointType(nEpt);
        assertEquals(Endpoint.EndPointType.Provider, instance.getEndPointType());
    }
    
    /**
     * Test of setEndPointName method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    public void testSetEndPointName() {
        String nName = ""; //NOI18N
        Endpoint instance = new Endpoint();
        instance.setEndPointName(nName);
        assertEquals(nName, instance.getEndPointName());
        
        nName = "epName" ; //NOI18N
        instance.setEndPointName(nName);
        assertEquals(nName, instance.getEndPointName());
        
    }
    
    
    /**
     * Test of setInterfaceName method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    public void testSetInterfaceName() {
        QName nName = null;
        Endpoint instance = new Endpoint();
        
        instance.setInterfaceName(nName);
        assertEquals(null, instance.getInterfaceName());
        
        nName = new QName("http://someCorp.com/Schema", "localName"); //NOI18N
        instance.setInterfaceName(nName);
        assertEquals(nName, instance.getInterfaceName());
        
        QName ret = instance.getInterfaceName();
        assertEquals("http://someCorp.com/Schema", ret.getNamespaceURI()); //NOI18N
        assertEquals("localName", ret.getLocalPart()); //NOI18N
    }
    
    /**
     * Test of setServiceName method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    public void testSetServiceName() {
        QName nName = null;
        Endpoint instance = new Endpoint();
        
        instance.setServiceName(nName);
        assertEquals(nName, instance.getServiceName());
        
        nName = new QName("http://someCorp.com/Schema", "localName"); //NOI18N
        instance.setServiceName(nName);
        assertEquals(nName, instance.getServiceName());
        
        QName ret = instance.getServiceName();
        assertEquals("http://someCorp.com/Schema", ret.getNamespaceURI()); //NOI18N
        assertEquals("localName", ret.getLocalPart()); //NOI18N
    }
    
    
    /**
     * Test of toEndpointConfigXML method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    public void testToEndpointConfigXML() {
        String indent = ""; //NOI18N
        Endpoint instance = new Endpoint();
        
        String expResult = "<endpoint \n  endpointType=\"Provider\"\n  portName=\"epName\"\n  portTypeLocalName=\"PortTypeOne\"\n  portTypeNamespace=\"http://someCorp.com/schema/interface\"\n  serviceLocalName=\"CorpSvcsOne\"\n  serviceNamespace=\"http://someCorp.com/schema/service\"/>\n"; //NOI18N
        instance.setEndPointName("epName");
        instance.setEndPointType(Endpoint.EndPointType.Provider);
        instance.setInterfaceName( new QName("http://someCorp.com/schema/interface", "PortTypeOne")); //NOI18N
        instance.setServiceName( new QName("http://someCorp.com/schema/service", "CorpSvcsOne")); //NOI18N
        String result = instance.toEndpointConfigXML(indent);
        assertEquals(expResult, result);
    }
    
    /**
     * Test of equals method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    public void testEquals() {       
        Object obj = null;
        Endpoint instance = new Endpoint();
        Endpoint instance2 = new Endpoint();
        
        boolean expResult = false;
        boolean result = instance.equals(obj);
        assertEquals(expResult, result);
        
        instance.setEndPointName("epName"); //NOI18N
        instance.setEndPointType(Endpoint.EndPointType.Provider);
        instance.setInterfaceName( new QName("http://someCorp.com/schema/interface", "PortTypeOne"));//NOI18N
        instance.setServiceName( new QName("http://someCorp.com/schema/service", "CorpSvcsOne"));//NOI18N
        
        instance2.setEndPointName("epName");//NOI18N
        instance2.setEndPointType(Endpoint.EndPointType.Provider);
        instance2.setInterfaceName( new QName("http://someCorp.com/schema/interface", "PortTypeOne"));//NOI18N
        instance2.setServiceName( new QName("http://someCorp.com/schema/service", "CorpSvcsOne"));//NOI18N
        assertEquals(instance, instance2);
        
        assertEquals(instance, instance2);
        
        instance2.setEndPointName("epName1");//NOI18N
        assertNotSame(instance, instance2);
    }
    
    
    /**
     * Test of hashCode method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    public void testHashCode() {
        Endpoint instance = new Endpoint();
        instance.setEndPointName("epName");//NOI18N
        instance.setEndPointType(Endpoint.EndPointType.Provider);
        instance.setInterfaceName( new QName("http://someCorp.com/schema/interface", "PortTypeOne"));//NOI18N
        instance.setServiceName( new QName("http://someCorp.com/schema/service", "CorpSvcsOne"));//NOI18N
        
        StringBuffer sb = new StringBuffer();
        sb.append(instance.getEndPointName());
        sb.append(instance.getEndPointType());
        sb.append(instance.getInterfaceName());
        sb.append(instance.getServiceName());
        assertEquals(sb.toString().hashCode(), instance.hashCode());
        
        instance.setInterfaceName( new QName("http://someCorp.com/schema/interface1", "PortTypeOne"));//NOI18N
        sb = new StringBuffer();
        sb.append(instance.getEndPointName());
        sb.append(instance.getEndPointType());
        sb.append(instance.getInterfaceName());
        sb.append(instance.getServiceName());
        assertNotSame(sb.toString().hashCode(), instance.hashCode());       
    }
}
