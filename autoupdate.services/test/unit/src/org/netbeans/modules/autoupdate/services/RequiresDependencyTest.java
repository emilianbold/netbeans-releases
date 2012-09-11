/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.RandomlyFails;
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

    @RandomlyFails
    public void testInstallModuleWhichRequires () throws IOException {
        testInstallModuleWhichWants ("Requires");
    }
    
    @RandomlyFails
    public void testInstallModuleWhichNeeds () throws IOException {
        testInstallModuleWhichWants ("Needs");
    }
    
    @RandomlyFails
    public void testInstallModuleWhichRecommends () throws IOException {
        testInstallModuleWhichWants ("Recommends");
    }
    
    public void testBrokenDepsOfModuleWhichRequires () throws IOException {
        testBrokenDepsOfModuleWhichWants ("Requires", true);
    }

    @RandomlyFails
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
        providerUU = UpdateManagerImpl.getInstance ().getUpdateUnit (providerModule);

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
    
    @SuppressWarnings("unchecked")
    public void testInstallModuleWhichRecommendsBrokenModule() throws IOException {
        String providerModule = "org.yourorghere.provider.testtoken";
        String wantsModule = "org.yourorghere.recommends.testtoken";
        String catalog = generateCatalog(
                generateModuleElementWithProviders(providerModule, "1.0", TOKEN, "org.yourorghere.nothere"),
                generateModuleElement(wantsModule, "1.0", "OpenIDE-Module-Recommends", TOKEN, false, false));

        AutoupdateCatalogProvider p = createUpdateProvider(catalog);
        p.refresh(true);
        Map<String, UpdateItem> updates = p.getUpdateItems();

        // initial check of updates being and its states
        ModuleItem providerModuleItem = (ModuleItem) Trampoline.SPI.impl(updates.get(providerModule + "_1.0"));
        assertNotNull(providerModule + " found in UpdateItems.", providerModuleItem);

        ModuleItem wantsModuleItem = (ModuleItem) Trampoline.SPI.impl(updates.get(wantsModule + "_1.0"));
        assertNotNull(wantsModule + " found in UpdateItems.", wantsModuleItem);
        assertFalse(wantsModuleItem.getModuleInfo().getDependencies() + " are not empty.",
                wantsModuleItem.getModuleInfo().getDependencies().isEmpty());

        // acquire UpdateUnits for test modules
        UpdateUnitProviderFactory.getDefault().create("test-update-provider", "test-update-provider", generateFile(catalog));
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
        UpdateUnit providerUU = UpdateManagerImpl.getInstance().getUpdateUnit(providerModule);
        UpdateUnit wantsUU = UpdateManagerImpl.getInstance().getUpdateUnit(wantsModule);
        assertNotNull("Unit " + providerModule + " found.", providerUU);
        assertNotNull("Unit " + wantsModule + " found.", wantsUU);

        // check states installed units, none of wants and provides are installed
        assertNull(wantsModule + " is not installed.", wantsUU.getInstalled());
        assertNull(providerModule + " is not installed.", providerUU.getInstalled());

        // check content of container
        OperationContainer ic = OperationContainer.createForInstall();
        assertNotNull(wantsUU.getAvailableUpdates().get(0));
        
        OperationInfo wantsInfo = ic.add(wantsUU.getAvailableUpdates().get(0));
        assertNotNull(wantsInfo);
        assertEquals(wantsUU, wantsInfo.getUpdateUnit());
        
        Set<String> brokenDeps = wantsInfo.getBrokenDependencies();
        assertTrue("No broken dependencies if install " + wantsUU + ", but " + brokenDeps, brokenDeps.isEmpty());

        // install wants module
        installUpdateUnit(wantsUU);

        // check states of installed units, module wants is installed, but the broken provider not
        assertNotNull(wantsModule + " is installed.", wantsUU.getInstalled());
        assertNull(providerModule + " is not installed.", providerUU.getInstalled());
    }
    
}
