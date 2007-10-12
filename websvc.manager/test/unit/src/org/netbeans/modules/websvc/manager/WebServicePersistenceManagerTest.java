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

package org.netbeans.modules.websvc.manager;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.websvc.manager.model.WebServiceData;
import org.netbeans.modules.websvc.manager.model.WebServiceGroup;
import org.netbeans.modules.websvc.manager.model.WebServiceListModel;
import org.netbeans.modules.websvc.manager.test.SetupData;
import org.netbeans.modules.websvc.manager.test.SetupUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author quynguyen
 */
public class WebServicePersistenceManagerTest extends NbTestCase {
    private SetupData setupData;
    
    public WebServicePersistenceManagerTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        setupData = SetupUtil.commonSetUp(getWorkDir());
        
        FileSystem sfs = Repository.getDefault().getDefaultFileSystem();
        FileObject fo = sfs.findResource("RestComponents");
        assertNotNull(fo);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SetupUtil.commonTearDown();
        WebServiceListModel.resetInstance();
    }

    public void testSaveLoad() {
        System.out.println("save & load");
        
        WebServiceListModel model = WebServiceListModel.getInstance();
        int serviceCount = model.getWebServiceSet().size();
        int groupCount = model.getWebServiceGroupSet().size();
        int numPartners = model.getPartnerServices().size();
        
        WebServicePersistenceManager mgr = new WebServicePersistenceManager();
        mgr.save();
        
        WebServiceListModel.resetInstance();
        model = WebServiceListModel.getInstance();
        
        // load automatically occurs since the model was reset
        
        assertTrue("All services not save/loaded correctly", serviceCount == model.getWebServiceSet().size());
        assertTrue("All groups not saved/loaded correctly", groupCount == model.getWebServiceGroupSet().size());
        assertTrue("Partner service list not saved/loaded correctly", numPartners == model.getPartnerServices().size());
    }
    
    /**
     * Test of loadPartnerService method, of class WebServicePersistenceManager.
     */
    public void testLoadPartnerService() {
        System.out.println("loadPartnerService");
        
        WebServiceListModel model = WebServiceListModel.getInstance();
        int serviceCount = model.getWebServiceSet().size();
        int groupCount = model.getWebServiceGroupSet().size();
        int numPartners = model.getPartnerServices().size();
        
        List<String> wsIds = new ArrayList<String>();
        for (WebServiceData data : model.getWebServiceSet()) {
            wsIds.add(data.getId());
        }
        for (String wsId : wsIds) {
            model.removeWebService(wsId);
        }
        List<String> groupIds = new ArrayList<String>();
        for (WebServiceGroup group : model.getWebServiceGroupSet()) {
            groupIds.add(group.getId());
        }
        int removed = 0;
        for (String groupId : groupIds) {
            if (!groupId.equals(WebServiceListModel.DEFAULT_GROUP)) {
                removed++;
                model.removeWebServiceGroup(groupId);
            }
        }
        
        assertTrue("Could not remove all web services", model.getWebServiceSet().size() == 0);
        assertTrue("Could not remove all non-default groups", model.getWebServiceGroupSet().size() == (groupCount-removed));
        
        String serviceFolder = "RestComponents/StrikeIron"; // NOI18N
        String partnerName = null;
        WebServicePersistenceManager.loadPartnerService(serviceFolder, partnerName);
        
        assertTrue("All web services not reloaded", serviceCount == model.getWebServiceSet().size());
        assertTrue("All partner service groups not reloaded", groupCount == model.getWebServiceGroupSet().size());
        assertTrue("Inconsistent number of partner services loaded", numPartners == model.getPartnerServices().size());
    }

    /**
     * Test of loadPartnerServices method, of class WebServicePersistenceManager.
     */
    public void testLoadPartnerServices() {
        System.out.println("loadPartnerServices");
        WebServiceListModel model = WebServiceListModel.getInstance();
        
        boolean partnerLoaded = false;
        for (WebServiceGroup group : model.getWebServiceGroupSet()) {
            if (!group.getId().equals(WebServiceListModel.DEFAULT_GROUP)) {
                partnerLoaded = !group.isUserDefined();
                if (partnerLoaded) break;
            }
        }
        
        assertTrue("No partner services loaded", partnerLoaded && model.getWebServiceSet().size() > 0);
        assertTrue("Some partner services found but not loaded", model.getWebServiceSet().size() == model.getPartnerServices().size());
    }
    
    public void testReloadPartnerServices() {
        System.out.println("loadPartnerServices(reload)");
        
        WebServiceListModel model = WebServiceListModel.getInstance();        
        
        List<String> wsIds = new ArrayList<String>();
        for (WebServiceData data : model.getWebServiceSet()) {
            wsIds.add(data.getId());
        }
        
        for (String wsId : wsIds) {
            model.removeWebService(wsId);
        }
        
        assertTrue("Could not remove all web services", model.getWebServiceSet().size() == 0);
        WebServicePersistenceManager.loadPartnerServices();
        assertTrue("loadPartnerServices() should not add previously added web services", model.getWebServiceSet().size() == 0);
    }
    
}
