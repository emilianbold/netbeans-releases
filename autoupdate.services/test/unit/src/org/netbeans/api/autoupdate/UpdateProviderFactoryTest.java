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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.autoupdate;

import junit.framework.*;
import java.net.URL;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.autoupdate.services.UpdateUnitFactoryTest;
import org.netbeans.modules.autoupdate.updateprovider.AutoupdateCatalogProvider;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateProviderFactoryTest extends NbTestCase {
    
    public UpdateProviderFactoryTest (String testName) {
        super (testName);
    }

    protected void setUp () throws Exception {
        MockServices.setServices (MyProvider.class, MyProvider2.class);
    }
    
    protected void tearDown () throws  Exception {
        clearWorkDir ();
    }

    public void testGetUpdatesProviders () {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        
        assertFalse ("Providers found in lookup.", result.isEmpty ());
        assertEquals ("Two providers found.", 2, result.size ());
    }

    public void testSetEnable () {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);

        UpdateUnitProvider provider = result.get (1);
        boolean state = false;
        provider.setEnable (state);
        
        assertEquals ("New state stored.", state, provider.isEnabled ());

        List<UpdateUnitProvider> resultOnlyEnabled = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (true);
        
        assertFalse ("Providers still found in lookup.", resultOnlyEnabled.isEmpty ());
        assertEquals ("Only one enable provider found.", 1, resultOnlyEnabled.size ());
        assertTrue ("Provider in only enabled must be enabled.", resultOnlyEnabled.get (0).isEnabled ());
    }

    public void testCreate () throws Exception {
        String name = "new-one";
        String displayName = "Newone";
        URL url = UpdateUnitFactoryTest.class.getResource ("data/catalog.xml");
        
        UpdateUnitProvider newone = UpdateUnitProviderFactory.getDefault ().create(name, displayName, url);
        assertNotNull ("New provider was created.", newone);
        
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        assertEquals ("More providers.", 3, result.size ());
        
        boolean found = false;
        
        for (UpdateUnitProvider p : result) {
            found = found || name.equals (p.getName ());
        }
        
        assertTrue ("Found enabled", found);
        
        assertTrue ("New one provider is enabled.", newone.isEnabled ());
    }

    public static class MyProvider extends AutoupdateCatalogProvider {
        public MyProvider () {
            super ("test-updates-provider", "test-updates-provider", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
    
    public static class MyProvider2 extends AutoupdateCatalogProvider {
        public MyProvider2 () {
            super ("test-updates-provider-2", "test-updates-provider-2", UpdateUnitFactoryTest.class.getResource ("data/catalog.xml"));
        }
    }
}
