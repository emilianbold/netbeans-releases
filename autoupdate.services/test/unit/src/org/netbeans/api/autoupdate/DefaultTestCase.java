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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import org.netbeans.api.autoupdate.TestUtils.CustomItemsProvider;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.autoupdate.services.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Radek Matous
 */
public class DefaultTestCase extends NbTestCase {
    private static File catalogFile;
    private static URL catalogURL;
    protected boolean modulesOnly = true;
    List<UpdateUnit> keepItNotToGC;
    public DefaultTestCase(String testName) {
        super(testName);
    }
        
    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", catalogURL);
        }
    }

    public void populateCatalog(InputStream is) throws FileNotFoundException, IOException {
        OutputStream os = new FileOutputStream(catalogFile);
        try {
            FileUtil.copy(is, os);
        } finally {
            is.close();
            os.close();
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        this.clearWorkDir ();
        catalogFile = new File(getWorkDir(), "updates.xml");
        if (!catalogFile.exists()) {
            catalogFile.createNewFile();
        }
        catalogURL = catalogFile.toURI().toURL();        
        populateCatalog(TestUtils.class.getResourceAsStream("data/updates.xml"));
        
        TestUtils.setUserDir (getWorkDirPath ());
        TestUtils.testInit();
        MockServices.setServices(MyProvider.class, CustomItemsProvider.class, InstallIntoNewClusterTest.NetBeansClusterCreator.class);
        assert Lookup.getDefault().lookup(MyProvider.class) != null;
        assert Lookup.getDefault().lookup(CustomItemsProvider.class) != null;
        UpdateUnitProviderFactory.getDefault().refreshProviders (null, true);
        
        File pf = new File (new File (getWorkDir(), "platform"), "installdir");
        pf.mkdirs ();
        new File (pf, "config").mkdir();
        TestUtils.setPlatformDir (pf.toString ());
        if (modulesOnly) {
            keepItNotToGC = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE);
        } else {
            keepItNotToGC = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.FEATURE);
        }
            
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
