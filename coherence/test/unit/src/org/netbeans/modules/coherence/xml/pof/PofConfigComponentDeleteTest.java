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
package org.netbeans.modules.coherence.xml.pof;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.junit.NbTestCase;

/**
 * @author ads
 *
 */
public class PofConfigComponentDeleteTest extends NbTestCase {

    public PofConfigComponentDeleteTest(String name) {
        super(name);
    }

    public void testInitParam() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        DefaultSerializer child = root.getDefaultSerializer();

        assertNotNull(child);
        assertNotNull(child.getClassName());

        // Test InitParams
        InitParams ips = child.getInitParams();
        assertNotNull(ips);

        // Test InitParam
        List<InitParam> ipList = ips.getInitParams();
        assertEquals(3, ipList.size());

        InitParam ip = ipList.get(0);
        assertEquals("String 1", ip.getParamValue().getValue());

        // Start Transaction
        model.startTransaction();

        ips.removeInitParam(ip);
        ipList = ips.getInitParams();

        assertEquals(2, ipList.size());

        ip = ipList.get(0);
        assertEquals("String 2", ip.getParamValue().getValue());

        // End Transaction
        model.endTransaction();

    }

    public void testInitParams() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        DefaultSerializer child = root.getDefaultSerializer();

        assertNotNull(child);
        assertNotNull(child.getClassName());

        // Test InitParams
        InitParams ips = child.getInitParams();
        assertNotNull(ips);

        // Start Transaction
        model.startTransaction();

        child.setInitParams(null);
        ips = child.getInitParams();
        assertNull(ips);

        // End Transaction
        model.endTransaction();

    }

    public void testClassName() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        DefaultSerializer child = root.getDefaultSerializer();

        assertNotNull(child);
        assertNotNull(child.getClassName());

        // Test ClassName
        ClassName name = child.getClassName();
        assertEquals("com.tangosol.io.pof.SubjectPofSerializer", name.getValue());

        // Start Transaction
        model.startTransaction();
        child.setClassName(null);

        name = child.getClassName();
        assertNull(name);

        // End Transaction
        model.endTransaction();

    }

    public void testDefaultSerializer() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        DefaultSerializer child = root.getDefaultSerializer();

        assertNotNull(child);

        // Start Transaction
        model.startTransaction();
        
        root.setDefaultSerializer(null);

        child = root.getDefaultSerializer();
        assertNull(child);
        // End Transaction
        model.endTransaction();

    }
    
    public void testAllowSubclasses() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();
        
        PofConfig root = model.getPofConfig();
        
        AllowSubclasses child = root.getAllowSubclasses();
        
        assertNotNull(child);
        assertTrue(child.isAllowSubclasses());
        assertEquals(Boolean.TRUE, child.getValue());

        // Start Transaction
        model.startTransaction();
        root.setAllowSubclasses(null);
        child = root.getAllowSubclasses();
        assertNull(child);
        // End Transaction
        model.endTransaction();
        
    }

    public void testAllowInterfaces() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();
        
        PofConfig root = model.getPofConfig();
        
        AllowInterfaces child = root.getAllowInterfaces();
        
        assertNotNull(child);
        assertTrue(child.isAllowInterfaces());
        assertEquals(Boolean.TRUE, child.getValue());
        
        // Start Transaction
        model.startTransaction();
        root.setAllowInterfaces(null);
        child = root.getAllowInterfaces();
        assertNull(child);
        // End Transaction
        model.endTransaction();
        
    }
    
    public void testTypeId() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();
        
        PofConfig root = model.getPofConfig();
        
        UserTypeList child = root.getUserTypeList();
               
        List<UserType> elements = child.getUserTypes();
        
        assertEquals(2,elements.size());
        
        UserType element = elements.get(0);
        assertNotNull(element.getTypeId());
        assertEquals(Integer.valueOf(950), element.getTypeId().getValue());
        
        // Start Transaction
        model.startTransaction();
        element.setTypeId(null);
        assertNull(element.getTypeId());
        // End Transaction
        model.endTransaction();
    }

    public void testUserType() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        UserTypeList child = root.getUserTypeList();

        List<UserType> elements = child.getUserTypes();

        assertEquals(2, elements.size());

        UserType element = elements.get(0);
        // Start Transaction
        model.startTransaction();
        child.removeElement(element);
        elements = child.getUserTypes();
        assertEquals(1, elements.size());
        // End Transaction
        model.endTransaction();
    }

    public void testInclude() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        UserTypeList child = root.getUserTypeList();

        List<Include> elements = child.getIncludes();
        assertEquals(1, elements.size());

        Include element = elements.get(0);
        assertEquals("coherence-pof-config.xml", element.getValue());

        // Start Transaction
        model.startTransaction();
        child.removeElement(element);
        elements = child.getIncludes();
        assertEquals(0, elements.size());
        // End Transaction
        model.endTransaction();
    }

    public void testUserTypeList() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        UserTypeList child = root.getUserTypeList();

        List<UserTypeListElement> elements = child.getElements();
        assertEquals(1, elements.size());

        // Start Transaction
        model.startTransaction();
        root.setUserTypeList(null);
        child = root.getUserTypeList();
        assertNull(child);
        // End Transaction
        model.endTransaction();
    }
    
    public void testPofConfig() throws Exception {
        PofConfigModel model = Util.loadRegistryModel("pof-config.xml");
        model.sync();

        PofConfig root = model.getPofConfig();

        assertNull(root.getUserTypeList());
        assertNull(root.getAllowInterfaces());
        assertNull(root.getAllowSubclasses());
        assertNull(root.getDefaultSerializer());

    }
}
