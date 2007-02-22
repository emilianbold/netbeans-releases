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

/*
 * PropertyModelFactoryTest.java
 * JUnit based test
 *
 * Created on January 23, 2007, 5:12 PM
 */

package org.netbeans.modules.xml.wsdl.ui.property.model;

import javax.xml.namespace.QName;
import junit.framework.*;
import org.netbeans.modules.xml.wsdl.ui.TestLookup;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;


/**
 *
 * @author radval
 */
public class PropertyModelFactoryTest extends TestCase {
     
    static {
        try {
           System.setProperty("org.openide.util.Lookup", TestLookup.class.getName());
           Lookup l = Lookup.getDefault();
           if(l instanceof TestLookup) {
               XMLFileSystem x = new XMLFileSystem(PropertyModelFactoryTest.class.getResource("/org/netbeans/modules/wsdlextensions/jms/resources/layer.xml"));
              
               ((TestLookup) l).setup(x);
           }
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
     
    public PropertyModelFactoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }
    
    public void testGetInstance() throws Exception {
        PropertyModelFactory instance = PropertyModelFactory.getInstance();
        assertNotNull(instance);
    }
    
    public void testGetElementProperties() throws Exception {
        PropertyModelFactory instance = PropertyModelFactory.getInstance();
        
        QName messageQName = new QName("http://schemas.sun.com/jbi/wsdl-extensions/jms/", "message");
        ElementProperties ep = instance.getElementProperties(messageQName);
        assertNotNull(ep);
        
        PropertyGroup[] pg = ep.getPropertyGroup();
        assertNotNull(pg);
        assertTrue(pg.length == 2);
        assertTrue(pg[0].getName().equals("basic"));
        assertTrue(pg[0].getGroupOrder() == 1);
        assertTrue(pg[1].getName().equals("advance"));
        assertTrue(pg[1].getGroupOrder() == 2);
        
        
        Property[] p = ep.getProperty();
        assertNotNull(p);
        assertTrue(p.length == 1);
        assertTrue(p[0].getAttributeName().equals("correlationIdPart"));
        assertTrue(p[0].getGroupName().equals("basic"));
        assertTrue(p[0].getPropertyOrder() == 1);
        
        
    }
}
