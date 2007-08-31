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

package org.netbeans.api.autoupdate;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.ModuleManager;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.core.startup.Main;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateLicense;
import org.netbeans.spi.autoupdate.UpdateProvider;
/**
 *
 * @author Radek Matous, Jirka Rechtacek
 */
public class TestUtils {
    
    private static UpdateItem item = null;
    private static ModuleManager mgr = null;
        
    public static void setUserDir(String path) {    
        System.setProperty ("netbeans.user", path);
    }
    
    /** Returns the platform installatiion directory.
     * @return the File directory.
     */
    public static File getPlatformDir () {
        return new File (System.getProperty ("netbeans.home")); // NOI18N
    }
    
    public static void setPlatformDir (String path) {    
        System.setProperty ("netbeans.home", path);
    }
        
    public static void testInit() {
        mgr = Main.getModuleSystem().getManager();
        assert mgr != null;
    }
    
    public static class CustomItemsProvider implements UpdateProvider {
        public String getName() {
            return "items-with-custom-installer";
        }

        public String getDisplayName() {
            return "Provides item with own custom installer";
        }

        public String getDescription () {
            return null;
        }

        public Map<String, UpdateItem> getUpdateItems() {
            return Collections.singletonMap ("hello-installer", getUpdateItemWithCustomInstaller ());
        }

        public boolean refresh(boolean force) {
            return true;
        }

        public CATEGORY getCategory() {
            return CATEGORY.COMMUNITY;
        }
    }
    
    private static CustomInstaller customInstaller = new CustomInstaller () {
        public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
            assert false : "Don't call unset installer";
            return false;
        }
    };
    
    
    public static void setCustomInstaller (CustomInstaller installer) {
        customInstaller = installer;
    }

    public static UpdateItem getUpdateItemWithCustomInstaller () {
        if (item != null) return item;
        String codeName = "hello-installer";
        String specificationVersion = "0.1";
        String displayName = "Hello Component";
        String description = "Hello I'm a component with own installer";
        URL distribution = null;
        try {
            distribution = new URL ("nbresloc:/org/netbeans/api/autoupdate/data/org-yourorghere-engine-1-1.nbm");
            //distribution = new URL ("nbresloc:/org/netbeans/api/autoupdate/data/executable-jar.jar");
        } catch (MalformedURLException ex) {
            assert false : ex;
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "2815";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        CustomInstaller ci = createCustomInstaller ();
        assert ci != null;
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        item = UpdateItem.createNativeComponent (
                                                    codeName,
                                                    specificationVersion,
                                                    downloadSize,
                                                    null, // dependencies
                                                    displayName,
                                                    description,
                                                    false, false, "my-cluster",
                                                    ci,
                                                    license);
        return item;
    }
    
    private static CustomInstaller createCustomInstaller () {
        return new CustomInstaller () {
            public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
                assert item != null;
                return customInstaller.install (codeName, specificationVersion, handle);
            }
        };
    }
}
