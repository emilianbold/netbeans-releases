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

import java.io.IOException;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.modules.autoupdate.updateprovider.*;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.autoupdate.UpdateUnitProvider;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitProviderImplTest extends NbTestCase {
    
    private static URL URL_TO_TEST_CATALOG = null;
    private static Preferences p = null;
    
    public UpdateUnitProviderImplTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(TestUpdateProvider.class);
        URL_TO_TEST_CATALOG = AutoupdateCatalogParserTest.class.getResource("data/catalog.xml");
        System.out.println("getWorkDirPath (): " + getWorkDirPath());
        System.setProperty("netbeans.user", getWorkDirPath());
        System.out.println("NbPreferences.root (): " + NbPreferences.root());
        p = NbPreferences.root().node("/org/netbeans/modules/autoupdate");
        p.clear();
        String[] childNodes = p.childrenNames();
        for (String childName : childNodes) {
            p.node(childName).removeNode();    
        }        
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
        clearWorkDir();
    }
    
    public void testRemoveUpdateUnitProvider() throws Exception {
        testCreateUpdateProvider();
        UpdateUnitProviderFactory factory = UpdateUnitProviderFactory.getDefault();
        List<UpdateUnitProvider> providers =  factory.getUpdateUnitProviders(false);
        int originalSize = providers.size();
        assertTrue(originalSize > 0);
        for (UpdateUnitProvider updateUnitProvider : providers) {
            factory.remove(updateUnitProvider);
            assertEquals(--originalSize, factory.getUpdateUnitProviders(false).size());
        }        
    }
    
    public void testCreateUpdateProvider() throws Exception {                
        String codeName1 = "test-update-provider-1";
        String displayName1 = "1st Update Provider";
        
        String codeName2 = "test-update-provider-2";
        String displayName2 = "2nd Update Provider";
        URL url = URL_TO_TEST_CATALOG;
        
        UpdateUnitProvider result1 = UpdateUnitProviderFactory.getDefault().create(codeName1, displayName1, url);
        assertNotNull(codeName1 + " provider found.", result1);
        assertEquals("Code name equals.", codeName1, result1.getName());
        assertEquals("Display name equals.", displayName1, result1.getDisplayName());
        
        UpdateUnitProvider result2 = UpdateUnitProviderFactory.getDefault().create(codeName2, displayName2, url);
        assertNotNull(codeName2 + " provider found.", result2);
        assertEquals("Code name equals.", codeName2, result2.getName());
        assertEquals("Display name equals.", displayName2, result2.getDisplayName());
        
        assertFalse ("Different objects.", result2.equals (result1));
        
        String[] children = p.childrenNames();
        assertNotNull("Some instances stored.", children);
        assertEquals("Two instances stored.", 2, children.length);
        
        assertEquals("First storage " + codeName1, codeName1, children [0]);
        assertEquals("Second storage " + codeName1, codeName2, children [1]);
        
        UpdateProvider load1 = UpdateUnitProviderImpl.loadProvider(children [0]);
        assertNotNull(children [0] + " loaded.", load1);
        
        UpdateProvider load2 = UpdateUnitProviderImpl.loadProvider(children [1]);
        assertNotNull(children [1] + " loaded.", load2);
        
        assertEquals("Original and loaded are identical " + codeName1, result1.getName(), load1.getName());
        assertEquals("Display name equals.", result1.getDisplayName(), load1.getDisplayName());
        
        assertEquals("Original and loaded are identical " + codeName1, result2.getName(), load2.getName());
        assertEquals("Display name equals.", result2.getDisplayName(), load2.getDisplayName());
        
        clearWorkDir();
    }
    
    public void testGetUpdatesProviders() throws Exception {
        p.clear();
        
        //        List<UpdateProvider> expResult = Collections.emptyList ();
        //        List<UpdateProvider> result = UpdateUnitProviderFactory.getUpdatesProviders ();
        //        assertEquals ("Update Providers are empty at init.", expResult, result);
        List<UpdateUnitProvider> result;
        
        // create new one
        String codeName = "test-update-provider";
        String displayName = "2nd Update Provider";
        URL url = URL_TO_TEST_CATALOG;
        
        UpdateUnitProvider newProvider = UpdateUnitProviderImpl.createUpdateUnitProvider(codeName, displayName, url);
        assertNotNull(codeName + " provider found.", newProvider);
        
        result = UpdateUnitProviderImpl.getUpdateUnitProviders(false);
        assertFalse("Update Providers are non empty.", result.isEmpty());
        boolean found = false;
        for (UpdateUnitProvider provider : result) {
            found = found || provider.getName().equals(newProvider.getName());
        }
        assertTrue("New Provider " + newProvider.getName() + " found among Update Providers.", found);
    }
    
    public static class TestUpdateProvider implements UpdateProvider {
        private Map<String, UpdateItem> providers =  new HashMap<String, UpdateItem>();
        public String getName() {
            return "lookup-based-updateProvider";
        }
        
        public String getDisplayName() {
            return "lookupBasedUpdateProvider";
        }
        
        public String getDescription () {
            return null;
        }

        public Map<String, UpdateItem> getUpdateItems() throws IOException {
            return providers;
        }
        
        public boolean refresh(boolean force) throws IOException {
            return true;
        }

        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }
    }        
}
