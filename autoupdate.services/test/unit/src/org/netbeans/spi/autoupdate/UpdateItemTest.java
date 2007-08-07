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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.services.Utilities;
import org.netbeans.modules.autoupdate.updateprovider.ModuleItem;
import org.netbeans.modules.autoupdate.updateprovider.UpdateItemImpl;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateItemTest extends NbTestCase {
    
    public UpdateItemTest (String testName) {
        super (testName);
    }
    
    public void setUp () throws Exception {
    }

    public void tearDown () throws Exception {
    }

    public void testCreateLocalization() {
        String codeName = "test-localization";
        String specificationVersion = "1.0";
        String moduleSpecificationVersion = "1.0";
        Locale locale = Locale.GERMANY;
        String branding = "";
        String localizedName = "Lokal";
        String localizedDescription = "Lokal-modul";
        URL distribution = null;
        try {
            new URL ("http://netbeans.de/localization.nbm");
        } catch (MalformedURLException ex) {
            fail (ex.getMessage ());
        }
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        
        UpdateItem result = UpdateItem.createLocalization(codeName,
                                                          specificationVersion,
                                                          moduleSpecificationVersion,
                                                          locale, branding,
                                                          localizedName,
                                                          localizedDescription,
                                                          "test-category",
                                                          distribution,
                                                          false, false, null,
                                                          license);

        assertNotNull ("Localization UpdateItem was created.", result);
    }

    public void testCreateModule() {
        String codeName = "test-module";
        String specificationVersion = "1.0";
        URL distribution = null;
        try {
            new URL ("http://netbeans.de/module.nbm");
        } catch (MalformedURLException ex) {
            fail (ex.getMessage ());
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "12";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module"), "org.myorg.module/1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Implementation-Version"), "060216");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Long-Description"), "Real module Hello installs Hello menu item into Help menu.");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Module-Dependencies"), "org.openide.util &gt; 6.9.0.1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Name"), "module");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Requires"), "org.openide.modules.ModuleFormat1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Specification-Version"), "1.0");
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        UpdateItem result = UpdateItem.createModule(codeName,
                                                    specificationVersion,
                                                    distribution, author, downloadSize,
                                                    homepage, null, "test-category",
                                                    manifest, false, false, true, true, "my-cluster",
                                                    license);

        assertNotNull ("Module UpdateItem was created.", result);
    }

    public void testRelativeUrlPath () throws URISyntaxException {
        String codeName = "test-module";
        String specificationVersion = "1.0";
        String distributionSpec = "./module.nbm";
        URI base = new URI ("http://www.myorghere.org");
        URI uri = null;
        try {
            uri = new URI (distributionSpec);
        } catch (URISyntaxException x) {
            fail (x.getMessage ());
        }
        assertFalse (uri + " is relative.", uri.isAbsolute());
        uri = uri.resolve (base);
        URL distribution = null;
        try {
            distribution = uri.toURL ();
        } catch (MalformedURLException ex) {
            fail (ex.getMessage ());
        }
        String author = "Jiri Rechtacek";
        String downloadSize = "12";
        String homepage = "http://netbeans.de";
        Manifest manifest = new Manifest ();
        Attributes mfAttrs = manifest.getMainAttributes ();
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module"), "org.myorg.module/1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Implementation-Version"), "060216");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Long-Description"), "Real module Hello installs Hello menu item into Help menu.");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Module-Dependencies"), "org.openide.util &gt; 6.9.0.1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Name"), "module");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Requires"), "org.openide.modules.ModuleFormat1");
        mfAttrs.put (new Attributes.Name ("OpenIDE-Module-Specification-Version"), "1.0");
        UpdateLicense license = UpdateLicense.createUpdateLicense ("none-license", "no-license");
        UpdateItem result = UpdateItem.createModule(codeName,
                                                    specificationVersion,
                                                    distribution, author, downloadSize,
                                                    homepage, "2007/05/22", "test-category",
                                                    manifest, false, false, true, true, "my-cluster",
                                                    license);

        assertNotNull ("Module UpdateItem with releative path was created.", result);
        UpdateItemImpl impl = result.impl;
        assertNotNull ("UpdateItemImpl found for " + result, impl);
        assertTrue (impl + "is instanceof ModuleItem.", impl instanceof ModuleItem);
        ModuleItem mimpl = (ModuleItem) impl;
        assertNotNull ("Release date is not null.", mimpl.getDate());
        try {
            Date d = Utilities.DATE_FORMAT.parse (mimpl.getDate ());
            assertEquals ("2007/05/22", mimpl.getDate ());
        } catch (ParseException pe) {
            fail (pe.getMessage ());
        }
    }

    public void testCreateFeature() {
        String codeName = "test-feature";
        String specificationVersion = "1.0.1.0";
        Set<String> dependencies = new LinkedHashSet<String> (Arrays.asList ("org.myorg.module/1"));
        String displayName = "Test Feature";
        String description = "Something interesting about Test Feature";
        UpdateItem result = UpdateItem.createFeature(codeName,
                                                     specificationVersion,
                                                     dependencies, displayName,
                                                     description,
                                                     "test-category");

        assertNotNull ("Feature UpdateItem was created.", result);
    }

}
