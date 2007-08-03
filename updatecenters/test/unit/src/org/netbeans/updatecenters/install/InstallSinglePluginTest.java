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
package org.netbeans.updatecenters.install;

import org.netbeans.api.autoupdate.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.InstallSupport.Installer;
import org.netbeans.api.autoupdate.InstallSupport.Restarter;
import org.netbeans.api.autoupdate.InstallSupport.Validator;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.autoupdate.UpdateUnitProviderFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.util.Lookup;

/**
 *
 * @author Jaromir Uhrik
 */

public class InstallSinglePluginTest extends NbTestCase {

    private static String primaryCatalogLocation = null;
    private static String secondaryCatalogLocation = null;
    private static String pluginToInstall = null;

    private static URL primaryURL = null; //static URL representation of primaryCatalogLocation
    private static URL secondaryURL = null; //static URL representation of secondaryCatalogLocation
    List<UpdateUnit> availableUnits = null;

    List<UpdateElement> installedPlugins = null;
    List<UpdateElement> newPlugins = null;
    List<UpdateElement> updatePlugins = null;
    int numberOfPluginsToInstall = 0;

    private static boolean useConfigFile = false;
    static {
        setUpProperties();
    }

    public static class PrimaryCatalogProvider extends AutoupdateCatalogProvider {

        public PrimaryCatalogProvider() {
            super("primary uc", "Primary Update Center", primaryURL);
        }
    }
    public static class SecondaryCatalogProvider extends AutoupdateCatalogProvider {

        public SecondaryCatalogProvider() {
            super("secondary uc", "Secondary Update Center", secondaryURL);
        }
    }

    public static void setUpProperties() {
        String useThisConfig = System.getProperty("useThisConfig");
        if (useThisConfig != null) {
            if (useThisConfig.equalsIgnoreCase("true") || useThisConfig.equalsIgnoreCase("yes")) {
                useConfigFile = true;
                primaryCatalogLocation = System.getProperty("primaryCatalogLocation");
                secondaryCatalogLocation = System.getProperty("secondaryCatalogLocation");
                pluginToInstall = System.getProperty("pluginToInstall");
            }
        }

        acceptConfigFile(useConfigFile);
        if (primaryCatalogLocation == null) {
            fail("System property 'primaryCatalogLocation' hasn't been set");
        }
        if (pluginToInstall == null) {
            fail("System property 'pluginToInstall' hasn't been set");
        }
        try {
            primaryURL = new URL(primaryCatalogLocation);
            if (secondaryCatalogLocation != null) {
                secondaryURL = new URL(secondaryCatalogLocation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public InstallSinglePluginTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir();
        TestUtils.setUserDir(getWorkDirPath());
        TestUtils.testInit();
        if(secondaryURL == null){
            MockServices.setServices(PrimaryCatalogProvider.class);
        }else{
            MockServices.setServices(PrimaryCatalogProvider.class, SecondaryCatalogProvider.class);
        }
        assert Lookup.getDefault().lookup(PrimaryCatalogProvider.class) != null;        
        UpdateUnitProviderFactory.getDefault().refreshProviders(null, true);
        File pf = new File(new File(getWorkDir(), "platform"), "installdir");
        pf.mkdirs();
        new File(pf, "config").mkdir();
        TestUtils.setPlatformDir(pf.toString());
        availableUnits = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new InstallSinglePluginTest("testInstallSinglePlugin"));
        return suite;
    }

    public void initLists() {
        installedPlugins = null;
        installedPlugins = new ArrayList<UpdateElement>();
        newPlugins = null;
        newPlugins = new ArrayList<UpdateElement>();
        updatePlugins = null;
        updatePlugins = new ArrayList<UpdateElement>();
    }

    public void readLists() {
        availableUnits = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        for (UpdateUnit updateUnit : availableUnits) {
            UpdateElement element = updateUnit.getInstalled();
            if (updateUnit.getInstalled() != null) {
                //all plugins that are installed or available updates
                List<UpdateElement> availableUpdates = updateUnit.getAvailableUpdates();
                if (!availableUpdates.isEmpty()) {
                    //updatePlugins
                    updatePlugins.add(availableUpdates.get(0));
                } else {
                    //installedPlugins
                    installedPlugins.add(element);
                }
            } else {
                //newPlugins (available not installed plugins)
                List<UpdateElement> availableUpdates = updateUnit.getAvailableUpdates();
                if (!availableUpdates.isEmpty()) {
                    newPlugins.add(availableUpdates.get(0));
                }
            }
        }
    }

    public void installPluginByDisplayName() {
        for (UpdateElement updateElement : newPlugins) {
            if (updateElement.getDisplayName().startsWith(pluginToInstall)) {
                installPlugin(updateElement); //install found plugin with all required plugins
                return;
            }
        }
        //if such plugin cannot be found -> fail
        fail("Plugin '" + pluginToInstall + "' cannot be installed - it is not available");
    }

    private int installPlugin(UpdateElement updateElement) {
        log("Trying to install plugin:" + updateElement.getDisplayName());
        System.out.println("Trying to install plugin:" + updateElement.getDisplayName());
        numberOfPluginsToInstall = 0;
        OperationContainer<InstallSupport> install = OperationContainer.createForInstall();
        OperationInfo info = install.add(updateElement);
        log("Broken dependencies:");
        log(info.getBrokenDependencies().toString());
        numberOfPluginsToInstall++;
        for (OperationInfo i : install.listAll()) {
            Set<UpdateElement> reqElements = i.getRequiredElements();
            log("List of required plugins is:");
            for (UpdateElement reqElm : reqElements) {
                install.add(reqElm);
                log(reqElm.getDisplayName());
                numberOfPluginsToInstall++;
            }
        }
        List<OperationInfo<InstallSupport>> lst = install.listAll();
        assertTrue("List of invalid is empty.", install.listInvalid().isEmpty());
        assertTrue("Dependencies broken for plugin '" + updateElement.getDisplayName() + "'." + info.getBrokenDependencies().toString(), info.getBrokenDependencies().size() == 0);
        InstallSupport is = install.getSupport();
        try {
            Validator v = is.doDownload(null, false);
            assertNotNull("Validator for " + updateElement + " is not null.", v);
            Installer installer;
            try {
                installer = is.doValidate(v, null);
                Restarter r = is.doInstall(installer, null);
                if (r == null) {
                } else {
                    is.doRestartLater(r); //Restart later! (try is.doRestart(r, null);)
                }
            } catch (OperationException oex) {
                fail("Unsuccessful operation!");
                oex.printStackTrace();
            }
        } catch (Exception e) {
            fail("Cannot download required plugin or some dependency - probably it doesn't exist");
            e.printStackTrace();
        }
        return numberOfPluginsToInstall;
    }

    public void moveElement(List<UpdateElement> from, List<UpdateElement> to, UpdateElement element) {
        from.remove(element);
        to.add(element);
    }

    public void testInstallSinglePlugin() {
        initLists();
        readLists();
        log("\n*******BEFORE INSTALL********");
        logElementsInfo();
        int newBeforeInstall = newPlugins.size();
        int installedBeforeInstall = installedPlugins.size();
        int updateBeforeInstall = updatePlugins.size();
        installPluginByDisplayName();
        initLists();
        readLists();
        log("\n******* AFTER INSTALL *******");
        logElementsInfo();
        int newAfterInstall = newPlugins.size();
        int installedAfterInstall = installedPlugins.size();
        int updateAfterInstall = updatePlugins.size();
        assertTrue("Number of plugins to install is < 1" + numberOfPluginsToInstall, numberOfPluginsToInstall > 0);
        assertEquals("New number differs", newBeforeInstall + updateBeforeInstall, newAfterInstall + updateAfterInstall + numberOfPluginsToInstall);
        assertEquals("Installed number differs", installedBeforeInstall + numberOfPluginsToInstall, installedAfterInstall);
        assertEquals("Numbers sum differs", newBeforeInstall + updateBeforeInstall + installedBeforeInstall, newAfterInstall + updateAfterInstall + installedAfterInstall);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
        System.exit(0);
    }

    //-----------------------------------------------------------------------
    //following code  is just for purpose of running inside of NetBeans IDE
    //-----------------------------------------------------------------------
    public static void acceptConfigFile(boolean accept) {
        if (accept) {
            return;
        }
        System.out.println("");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("!!! CONFIG FILE IS NOT USED FOR THIS RUN !!!");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("");

        primaryCatalogLocation = "http://dhcp-eprg06-20-214.czech.sun.com/testy/dev_1.22__new.xml";
        secondaryCatalogLocation = "http://dhcp-eprg06-20-214.czech.sun.com/testy/catalog_new.xml";
        pluginToInstall = "Maven Embedder library";
    }

    public void logElementsInfo() {
        log("NEW ELEMENTS:" + newPlugins.size());
        log("INSTALLED ELEMENTS:" + installedPlugins.size());
        log("UPDATE ELEMENTS:" + updatePlugins.size());
    }
}
