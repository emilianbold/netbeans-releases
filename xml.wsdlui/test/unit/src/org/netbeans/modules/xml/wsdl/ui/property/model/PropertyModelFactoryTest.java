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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
