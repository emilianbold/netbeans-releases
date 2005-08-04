/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.navigator;

import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;


/**
 *
 * @author Dafe Simonek
 */
public class ProviderRegistryTest extends NbTestCase {
    
    /** test data type contants */
    private static final String MARVELOUS_DATA_TYPE_NAME = "MarvelousDataType";
    private static final String MARVELOUS_DATA_TYPE = "text/marvelous/data_type";
    
    /** Creates a new instance of ProviderRegistryTest */
    public ProviderRegistryTest() {
        super("");
    }
    
    public ProviderRegistryTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static NbTest suite() {
        NbTestSuite suite = new NbTestSuite(ProviderRegistryTest.class);
        return suite;
    }
    
    protected void setUp () throws Exception {
    }
    
    
    public void testGetProviders () throws Exception {
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/navigator/resources/testGetProvidersLayer.xml" });
        
        ProviderRegistry providerReg = ProviderRegistry.getInstance();
        
        System.out.println("Asking for non-existent type...");
        assertEquals(0, providerReg.getProviders("image/non_existent_type").size());
        
        System.out.println("Asking for non-existent class...");
        assertEquals(0, providerReg.getProviders("text/plain").size());
        
        System.out.println("Asking for valid type and provider...");
        List result = providerReg.getProviders(MARVELOUS_DATA_TYPE);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof MarvelousDataTypeProvider);
        MarvelousDataTypeProvider provider = (MarvelousDataTypeProvider)result.get(0);
        assertEquals(MARVELOUS_DATA_TYPE_NAME, provider.getDisplayName());
    }
    

    /** Dummy navigator panel provider, just to test right loading and instantiating
     * for certain data type
     */ 
    public static final class MarvelousDataTypeProvider implements NavigatorPanel {
        
        public String getDisplayName () {
            return MARVELOUS_DATA_TYPE_NAME;
        }
    
        public String getDisplayHint () {
            return null;
        }

        public JComponent getComponent () {
            return null;
        }

        public void panelActivated (Lookup context) {
        }

        public void panelDeactivated () {
        }
        
        public Lookup getLookup () {
            return null;
        }
        
    }
    
    
}
