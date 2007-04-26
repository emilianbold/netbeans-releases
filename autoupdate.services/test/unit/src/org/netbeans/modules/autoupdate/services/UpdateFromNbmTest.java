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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import java.net.URL;
import java.util.List;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.TestUtils.CustomItemsProvider;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.MockServices;

/**
 *
 * @author Radek Matous
 */
public class UpdateFromNbmTest extends OperationsTestImpl {
    
    public UpdateFromNbmTest(String testName) {
        super(testName);
    }
    
    protected String moduleCodeNameBaseForTest() {
        return "org.yourorghere.engine";
    }
        
    public void testSelf() throws Exception {        
        UpdateUnit toUpdate = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull(toUpdate);
        assertEquals(2, toUpdate.getAvailableUpdates ().size());
        UpdateElement engine1_0 = toUpdate.getAvailableUpdates ().get (1);
        assertNotNull(engine1_0);
        assertEquals("1.0",engine1_0.getSpecificationVersion().toString());
        installModule(toUpdate, engine1_0);
        toUpdate = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull(toUpdate.getInstalled());
        
        MockServices.setServices(MyProvider.class, CustomItemsProvider.class);
        URL engineURL = TestUtils.class.getResource("data/org-yourorghere-engine-1-2.nbm");
        assertNotNull(engineURL);
        File engineFile = new File(engineURL.getFile());
        assertTrue(engineFile.exists());
        
        URL independentURL = TestUtils.class.getResource("data/org-yourorghere-independent-1-1.nbm");
        assertNotNull(independentURL);
        File independentFile = new File(independentURL.getFile());
        assertTrue(independentFile.exists());
        
        String source = "local-downloaded";
        List<UpdateUnit> units =  UpdateUnitProviderFactory.getDefault ().create (source, new File[] {engineFile, independentFile}).getUpdateUnits ();
        assertEquals(2, units.size());
        UpdateUnit nbmsEngine =  units.get(0);
        assertNotNull(nbmsEngine.getInstalled());        
        assertEquals(1, nbmsEngine.getAvailableUpdates().size());
        UpdateElement engine1_2 = nbmsEngine.getAvailableUpdates().get(0);
        assertEquals(source,engine1_2.getSource());
        assertEquals("1.2",engine1_2.getSpecificationVersion().toString());
        OperationContainer<InstallSupport> oc =  OperationContainer.createForUpdate();
        OperationContainer.OperationInfo info = oc.add(nbmsEngine, engine1_2);
        assertNotNull(info);
        assertEquals(1, info.getBrokenDependencies().size());
        String brokenDep = (String)info.getBrokenDependencies().toArray()[0];
        assertEquals("module org.yourorghere.independent > 1.1",brokenDep);
        assertEquals(0, info.getRequiredElements().size());
        UpdateUnit independentEngine =  units.get(1);
        assertNotNull(independentEngine.getInstalled());        
        
        UpdateElement independent1_1 = independentEngine.getAvailableUpdates().get(0);
        assertEquals(source,independent1_1.getSource());
        assertEquals("1.1",independent1_1.getSpecificationVersion().toString());
        
        OperationContainer.OperationInfo info2 = oc.add(independentEngine, independent1_1);        
        assertEquals(0, info.getBrokenDependencies().size());
        assertEquals(0, info.getRequiredElements().size());        
        assertEquals(0, info2.getBrokenDependencies().size());
        assertEquals(0, info2.getRequiredElements().size());        
        
        InstallSupport support = oc.getSupport();
        assertNotNull(support);
        
        InstallSupport.Validator v = support.doDownload(null);
        assertNotNull(v);
        InstallSupport.Installer i = support.doValidate(v, null);
        assertNotNull(i);
        //assertNotNull(support.getCertificate(i, upEl));
        support.doInstall(i, null);
        
        MockServices.setServices(MyProvider.class, CustomItemsProvider.class);
        engine1_2 = nbmsEngine.getInstalled();                
        assertEquals("1.2",engine1_2.getSpecificationVersion().toString());                
    }
    
}

