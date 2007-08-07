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

package org.netbeans.spi.autoupdate;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author Jiri Rechtacek
 */
public class CustomProviderFactory {
    
    public static UpdateProvider getCustomUpdateProvider () {
        UpdateProvider provider = new UpdateProvider () {
            public String getName() {
                return "test-custom-provider";
            }

            public String getDisplayName() {
                return "Test Provider provides self-installed components.";
            }

            public String getDescription () {
                return null;
            }

            public Map<String, UpdateItem> getUpdateItems() {
                Map<String, UpdateItem> res = new HashMap<String, UpdateItem> ();
                res.put ("test-module", createNbmModule ());
                res.put ("test-custom-component", createCustomComponent ());
                return res;
            }

            public boolean refresh(boolean force) {
                return true;
            }
        };
        return provider;
    }
    
    private static UpdateItem createNbmModule () {
        String codeName = "test-module";
        String specificationVersion = "1.0";
        URL distribution = null;
        try {
            distribution = new URL ("http://netbeans.de/module.nbm");
        } catch (MalformedURLException ex) {
            assert false : ex;
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "12";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module"), "org.test.module/1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Implementation-Version"), "060216");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Long-Description"), "Real module Hello installs Hello menu item into Help menu.");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Module-Dependencies"), "org.openide.util > 6.9.0.1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Name"), "module");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Requires"), "org.openide.modules.ModuleFormat1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Specification-Version"), "1.0");
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        UpdateItem result = UpdateItem.createModule(codeName,
                                                    specificationVersion,
                                                    distribution, author,
                                                    downloadSize, homepage, null, "test-category",
                                                    manifest, false, false, true, true, "my-cluster",
                                                    license);
        return result;
    }
    
    private static UpdateItem createCustomComponent () {
        String codeName = "test-custom-component";
        String specificationVersion = "0.1";
        URL distribution = null;
        try {
            distribution = new URL ("http://netbeans.org/org/netbeans/api/autoupdate/data/org-yourorghere-engine-1-1.nbm");
        } catch (MalformedURLException ex) {
            assert false : ex;
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "2815";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module"), "org.test.custom.module/1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Implementation-Version"), "060216");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Long-Description"), "Real module Hello installs Hello menu item into Help menu.");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Name"), "module");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Requires"), "org.openide.modules.ModuleFormat1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Specification-Version"), "0.1");
        CustomInstaller ci = createCustomInstaller ();
        assert ci != null;
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        UpdateItem result = UpdateItem.createModule(codeName,
                                                    specificationVersion,
                                                    distribution, author,
                                                    downloadSize, homepage, null, "test-category",
                                                    manifest, false, false, true, true, "my-cluster",
                                                    license);
        return result;
    }
    
    private static CustomInstaller createCustomInstaller () {
        return new CustomInstaller () {
            public boolean install (String codeName, String specificationVersion, ProgressHandle handle) throws OperationException {
                assert codeName != null && specificationVersion != null;
                return true;
            }
        };
    }
}
