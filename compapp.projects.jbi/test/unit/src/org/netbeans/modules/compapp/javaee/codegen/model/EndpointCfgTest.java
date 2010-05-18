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
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.compapp.javaee.codegen.model;

import javax.xml.namespace.QName;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author gpatil
 */
public class EndpointCfgTest {
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws SAXException {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of equals method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    @Test
    public void testEquals() {       
        Object obj = null;
        Endpoint instance = new Endpoint();
        Endpoint instance2 = new Endpoint();
        
        boolean expResult = false;
        boolean result = instance.equals(obj);
        Assert.assertEquals(expResult, result);
        
        instance.setEndPointName("epName"); //NOI18N
        instance.setEndPointType(Endpoint.EndPointType.Provider);
        instance.setInterfaceName( new QName("http://someCorp.com/schema/interface", "PortTypeOne"));//NOI18N
        instance.setServiceName( new QName("http://someCorp.com/schema/service", "CorpSvcsOne"));//NOI18N
        
        instance2.setEndPointName("epName");//NOI18N
        instance2.setEndPointType(Endpoint.EndPointType.Provider);
        instance2.setInterfaceName( new QName("http://someCorp.com/schema/interface", "PortTypeOne"));//NOI18N
        instance2.setServiceName( new QName("http://someCorp.com/schema/service", "CorpSvcsOne"));//NOI18N
        Assert.assertEquals(instance, instance2);
        
        Assert.assertEquals(instance, instance2);
        
        instance2.setEndPointName("epName1");//NOI18N
        Assert.assertNotSame(instance, instance2);
    }
    
    /**
     * Test of hashCode method, of class com.sun.jbi.javee.codegen.model.Endpoint.
     */
    @Test    
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
        Assert.assertEquals(sb.toString().hashCode(), instance.hashCode());
        
        instance.setInterfaceName( new QName("http://someCorp.com/schema/interface1", "PortTypeOne"));//NOI18N
        sb = new StringBuffer();
        sb.append(instance.getEndPointName());
        sb.append(instance.getEndPointType());
        sb.append(instance.getInterfaceName());
        sb.append(instance.getServiceName());
        Assert.assertNotSame(sb.toString().hashCode(), instance.hashCode());       
    }
    
}
