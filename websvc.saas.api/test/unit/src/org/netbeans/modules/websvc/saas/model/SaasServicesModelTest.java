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

package org.netbeans.modules.websvc.saas.model;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData;
import org.netbeans.modules.websvc.saas.util.SetupUtil;

/**
 *
 * @author nam
 */
public class SaasServicesModelTest extends NbTestCase {
    
    public static void resetSaasServicesModel() {
        SaasServicesModel.getInstance().reset();
    }
    
    public static void setWsdlData(WsdlSaas saas, WsdlData data) {
        saas.setWsdlData(data);
    }
    
    public SaasServicesModelTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testLoading() throws Exception {
        SetupUtil.commonSetUp(super.getWorkDir());

        SaasServicesModel instance = SaasServicesModel.getInstance();
        assertEquals("YouTube", instance.getGroups().get(1).getName());
        //No Sub-group for now
        //SaasGroup group = instance.getGroups().get(0).getChildGroup("Videos");
        //assertNotNull(group);
        SaasGroup group = instance.getGroups().get(1);
        WadlSaas service = (WadlSaas) group.getServices().get(0);
        assertEquals("YouTubeVideos", service.getDisplayName());
        assertNotNull(service.getWadlModel());

        SetupUtil.commonTearDown();
    }
    
    public void testAddGroup() {
        System.out.println("addGroup");
        SaasServicesModel instance = SaasServicesModel.getInstance();
        instance.createGroup(instance.getRootGroup(), "groupA");
        SaasGroup added = instance.getRootGroup().getChildGroup("groupA");
        assertEquals("groupA", added.getName());
        instance.createGroup(added, "child1");
        SaasGroup child2 = instance.createGroup(added, "child2");
        instance.createGroup(child2, "grandChild");
        
        instance.reset();
        instance.initRootGroup();
        
        SaasGroup reloaded = instance.getRootGroup().getChildGroup("groupA");
        assertEquals("groupA", reloaded.getName());
        assertEquals(2, reloaded.getChildrenGroups().size());
        assertEquals("child1", reloaded.getChildGroup("child1").getName());
        assertEquals("grandChild", reloaded.getChildGroup("child2").getChildGroup("grandChild").getName());
    }

    /*public void testRemoveGroup() {
        System.out.println("removeGroup");
        SaasGroup child = null;
        SaasServicesModel instance = new SaasServicesModel();
        instance.removeGroup(child);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public void testAddWsdlService() {
        System.out.println("addWsdlService");
        SaasGroup parent = null;
        String displayName = "";
        String url = "";
        String packageName = "";
        SaasServicesModel instance = new SaasServicesModel();
        instance.addWsdlService(parent, displayName, url, packageName);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    public void testRemoveWsdlService() {
        System.out.println("removeSaasService");
        SaasGroup parent = null;
        Saas service = null;
        SaasServicesModel instance = new SaasServicesModel();
        instance.removeSaasService(parent, service);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    */
}
