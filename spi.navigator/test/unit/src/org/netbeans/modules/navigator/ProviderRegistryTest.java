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

package org.netbeans.modules.navigator;

import java.util.Collection;
import javax.swing.JComponent;
import org.netbeans.junit.NbTest;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.navigator.NavigatorPanel;
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
    
    public void testGetProviders () throws Exception {
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/navigator/resources/testGetProvidersLayer.xml" });
        
        ProviderRegistry providerReg = ProviderRegistry.getInstance();
        
        System.out.println("Asking for non-existent type...");
        assertEquals(0, providerReg.getProviders("image/non_existent_type").size());
        
        System.out.println("Asking for non-existent class...");
        assertEquals(0, providerReg.getProviders("text/plain").size());
        
        System.out.println("Asking for valid type and provider...");
        Collection<? extends NavigatorPanel> result = providerReg.getProviders(MARVELOUS_DATA_TYPE);
        assertEquals(1, result.size());
        NavigatorPanel np = result.iterator().next();
        assertTrue(np instanceof MarvelousDataTypeProvider);
        MarvelousDataTypeProvider provider = (MarvelousDataTypeProvider)np;
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
