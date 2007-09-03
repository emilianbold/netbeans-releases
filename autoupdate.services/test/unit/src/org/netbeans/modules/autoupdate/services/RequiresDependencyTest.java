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
import java.util.Map;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jiri Rechtacek
 */
public class RequiresDependencyTest extends NbmAdvancedTestCase {
    
    public RequiresDependencyTest (String testName) {
        super (testName);
    }
    
    private static String TOKEN = "org.netbeans.modules.autoupdate.test.token";
    
    public void testInstallModuleWhichRequires () throws IOException {
        testInstallModuleWhichWants ("Requires");
    }
    
    public void testInstallModuleWhichNeeds () throws IOException {
        testInstallModuleWhichWants ("Needs");
    }
    
    public void testInstallModuleWhichRecommends () throws IOException {
        testInstallModuleWhichWants ("Recommends");
    }
    
    public void testBrokenDepsOfModuleWhichRequires () throws IOException {
        testBrokenDepsOfModuleWhichWants ("Requires", true);
    }
    
    public void testBrokenDepsOfModuleWhichNeeds () throws IOException {
        testBrokenDepsOfModuleWhichWants ("Needs", true);
    }
    
    public void testBrokenDepsOfModuleWhichRecommends () throws IOException {
        testBrokenDepsOfModuleWhichWants ("Recommends", false);
    }
    
    @SuppressWarnings("unchecked")
    private void testInstallModuleWhichWants (String type) throws IOException {
        String providerModule = "org.yourorghere.provider.testtoken";
        String wantsModule = "org.yourorghere." + type + ".testtoken";
        String catalog = generateCatalog (
                generateModuleElementWithProviders (providerModule, "1.0", TOKEN),
                generateModuleElement (wantsModule, "1.0", "OpenIDE-Module-" + type, TOKEN, false, false)
                );

        AutoupdateCatalogProvider p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        
        // initial check of updates being and its states
        ModuleItem providerModuleItem = (ModuleItem) Trampoline.SPI.impl (updates.get (providerModule + "_1.0"));
        assertNotNull (providerModule + " found in UpdateItems.", providerModuleItem);
        
        ModuleItem wantsModuleItem = (ModuleItem) Trampoline.SPI.impl (updates.get (wantsModule + "_1.0"));
        assertNotNull (wantsModule + " found in UpdateItems.", wantsModuleItem);
        assertFalse (wantsModuleItem.getModuleInfo ().getDependencies () + " are not empty.",
                wantsModuleItem.getModuleInfo ().getDependencies ().isEmpty ());
        
        // acquire UpdateUnits for test modules
        UpdateUnitProviderFactory.getDefault ().create ("test-update-provider", "test-update-provider", generateFile (catalog));
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);
        UpdateUnit providerUU = UpdateManagerImpl.getInstance ().getUpdateUnit (providerModule);
        UpdateUnit wantsUU = UpdateManagerImpl.getInstance ().getUpdateUnit (wantsModule);
        assertNotNull ("Unit " + providerModule + " found.", providerUU);
        assertNotNull ("Unit " + wantsModule + " found.", wantsUU);
        
        // check states installed units, none of wants and provides are installed
        assertNull (wantsModule + " is not installed.", wantsUU.getInstalled ());
        assertNull (providerModule + " is not installed.", providerUU.getInstalled ());
        
        // check content of container
        OperationContainer ic = OperationContainer.createForInstall ();
        assertNotNull (wantsUU.getAvailableUpdates ().get (0));
        ic.add (wantsUU.getAvailableUpdates ().get (0));
        
        OperationInfo wantsInfo = (OperationInfo) ic.listAll ().iterator ().next ();
        UpdateElement providerUE = providerUU.getAvailableUpdates ().get (0);
        assertNotNull (wantsInfo);
        assertEquals (wantsUU, wantsInfo.getUpdateUnit ());
        assertNotNull (providerUE);
        
        assertTrue (providerUE + " is required for installation of " + wantsUU, wantsInfo.getRequiredElements ().contains (providerUE));
        
        // add required provider
        ic.add (wantsInfo.getRequiredElements ());
        
        // install wants module
        installUpdateUnit (wantsUU);

        // check states installed units, should be both wants and provides are installed
        assertNotNull (wantsModule + " is installed.", wantsUU.getInstalled ());
        assertNotNull (providerModule + " is installed.", providerUU.getInstalled ());
    }
    
    @SuppressWarnings("unchecked")
    private void testBrokenDepsOfModuleWhichWants (String type, boolean forceThisDependency) throws IOException {
        String wantsModule = "org.yourorghere." + type + ".testtoken";
        String catalog = generateCatalog (
                generateModuleElement (wantsModule, "1.0", "OpenIDE-Module-" + type, TOKEN, false, false)
                );

        AutoupdateCatalogProvider p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        
        // initial check of updates being and its states
        ModuleItem wantsModuleItem = (ModuleItem) Trampoline.SPI.impl (updates.get (wantsModule + "_1.0"));
        assertNotNull (wantsModule + " found in UpdateItems.", wantsModuleItem);
        assertFalse (wantsModuleItem.getModuleInfo ().getDependencies () + " are not empty.",
                wantsModuleItem.getModuleInfo ().getDependencies ().isEmpty ());
        
        assertTrue (wantsModule + " depends on " + TOKEN,
                wantsModuleItem.getModuleInfo ().getDependencies ().toString ().indexOf (TOKEN) != -1 &&
                wantsModuleItem.getModuleInfo ().getDependencies ().toString ().indexOf (type.toLowerCase ()) != -1);
        
        // acquire UpdateUnits for test modules
        UpdateUnitProviderFactory.getDefault ().create ("test-update-provider", "test-update-provider", generateFile (catalog));
        UpdateUnitProviderFactory.getDefault ().refreshProviders (null, true);
        UpdateUnit wantsUU = UpdateManagerImpl.getInstance ().getUpdateUnit (wantsModule);
        assertNotNull ("Unit " + wantsModule + " found.", wantsUU);
        
        // check states installed units, none of wants and provides are installed
        assertNull (wantsModule + " is not installed.", wantsUU.getInstalled ());
        
        // check content of container
        OperationContainer ic = OperationContainer.createForInstall ();
        assertNotNull (wantsUU.getAvailableUpdates ().get (0));
        ic.add (wantsUU.getAvailableUpdates ().get (0));
        
        OperationInfo wantsInfo = (OperationInfo) ic.listAll ().iterator ().next ();
        assertNotNull (wantsInfo);
        assertEquals (wantsUU, wantsInfo.getUpdateUnit ());
        
        if (forceThisDependency) {
            assertFalse ("OpenIDE-Module-" + type + " breaks installation of " + wantsUU + 
                    ", but broken deps are " + wantsInfo.getBrokenDependencies (), wantsInfo.getBrokenDependencies ().isEmpty ());
            assertTrue ("Installation of " + wantsUU + " requires " + TOKEN,
                    wantsInfo.getBrokenDependencies ().toString ().indexOf (TOKEN) != -1);
        } else {
            assertTrue ("OpenIDE-Module-" + type + " doesn't break installation of " + wantsUU + 
                    ", but broken deps are " + wantsInfo.getBrokenDependencies (), wantsInfo.getBrokenDependencies ().isEmpty ());
        }
    }
}
