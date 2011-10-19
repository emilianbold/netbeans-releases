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

import org.netbeans.junit.NbTestCase;


/**
 * @author ads
 *
 */
public class PofConfigComponentSyncUpdateTest extends NbTestCase {

    public PofConfigComponentSyncUpdateTest( String name ) {
        super(name);
    }
    
    public void testPofConfig() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");

        PofConfig root = model.getPofConfig();
        
        assertNotNull(root.getUserTypeList());
        assertNotNull(root.getAllowInterfaces());
        assertNotNull(root.getAllowSubclasses());
        assertNotNull(root.getDefaultSerializer());

    }
    
    public void testUserTypeList() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        UserTypeList child = root.getUserTypeList();
        
        List<UserTypeListElement> elements = child.getElements();
        
        assertEquals(3, elements.size());
        
        List<Include> includes = child.getIncludes();
        
        assertEquals(1, includes.size());
        
        List<UserType> userTypes = child.getUserTypes();
        
        assertEquals(2,userTypes.size());
    }

    public void testInclude() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        UserTypeList child = root.getUserTypeList();
        
        List<Include> elements = child.getIncludes();
        
        assertEquals(1, elements.size());
        
        Include element = elements.get(0);
        assertEquals("coherence-pof-config.xml", element.getValue());
        
    }

    public void testUserType() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        UserTypeList child = root.getUserTypeList();
               
        List<UserType> elements = child.getUserTypes();
        
        assertEquals(2,elements.size());
        
        UserType element = elements.get(0);
        assertNotNull(element.getTypeId());
        assertNotNull(element.getClassName());
        assertNotNull(element.getSerializer());
    }

    public void testTypeId() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        UserTypeList child = root.getUserTypeList();
               
        List<UserType> elements = child.getUserTypes();
        
        assertEquals(2,elements.size());
        
        UserType element = elements.get(0);
        assertNotNull(element.getTypeId());
        assertEquals(Integer.valueOf(950), element.getTypeId().getValue());
    }

    public void testAllowInterfaces() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        AllowInterfaces child = root.getAllowInterfaces();
        
        assertNotNull(child);
        assertTrue(child.isAllowInterfaces());
        assertEquals(Boolean.TRUE, child.getValue());
        
    }
    
    public void testAllowSubclasses() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        AllowSubclasses child = root.getAllowSubclasses();
        
        assertNotNull(child);
        assertTrue(child.isAllowSubclasses());
        assertEquals(Boolean.TRUE, child.getValue());
        
    }
    
    public void testDefaultSerializer() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        DefaultSerializer child = root.getDefaultSerializer();
        
        assertNotNull(child);
        assertNotNull(child.getClassName());
        
        // Test ClassName
        ClassName name = child.getClassName();
        assertEquals("com.tangosol.io.pof.SubjectPofSerializer", name.getValue());
        
        // Test InitParams
        InitParams ip = child.getInitParams();
        assertNotNull(ip);
        
        // Test InitParam
        List<InitParam> ipList = ip.getInitParams();
        assertEquals(3, ipList.size());
    }
    
    public void testClassName() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        DefaultSerializer child = root.getDefaultSerializer();
        
        assertNotNull(child);
        assertNotNull(child.getClassName());
        
        // Test ClassName
        ClassName name = child.getClassName();
        assertEquals("com.tangosol.io.pof.SubjectPofSerializer", name.getValue());
    }
    
    public void testInitParams() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        DefaultSerializer child = root.getDefaultSerializer();
        
        assertNotNull(child);
        assertNotNull(child.getClassName());
        
        // Test InitParams
        InitParams ip = child.getInitParams();
        assertNotNull(ip);
    }
    
    public void testInitParam() throws Exception{
        PofConfigModel model = Util.loadRegistryModel("empty-pof-config.xml");
        Util.setDocumentContentTo(model, "pof-config.xml");
        
        PofConfig root = model.getPofConfig();
        
        DefaultSerializer child = root.getDefaultSerializer();
        
        assertNotNull(child);
        assertNotNull(child.getClassName());
        
        // Test InitParams
        InitParams ip = child.getInitParams();
        assertNotNull(ip);
        
        // Test InitParam
        List<InitParam> ipList = ip.getInitParams();
        assertEquals(3, ipList.size());
    }
}
