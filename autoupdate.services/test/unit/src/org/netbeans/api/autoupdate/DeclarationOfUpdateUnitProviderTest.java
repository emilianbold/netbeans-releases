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

import java.net.URL;
import java.util.List;
import junit.framework.TestCase;
import org.netbeans.api.autoupdate.UpdateUnitProvider.CATEGORY;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.Repository;

/**
 *
 * @author Radek Matous
 */
public class DeclarationOfUpdateUnitProviderTest extends NbTestCase {
    private UpdateUnitProvider enabled;
    private UpdateUnitProvider disabled;
    private UpdateUnitProvider beta;    
    private UpdateUnitProvider fallback;
    static {
        String[] layers = new String[]{"org/netbeans/api/autoupdate/mf-layer.xml"}; //NOI18N
        Object[] instances = new Object[]{};
        IDEInitializer.setup(layers, instances);
    }

    public DeclarationOfUpdateUnitProviderTest(String testName) {
        super(testName);                
    }

    protected void setUp() throws Exception {
        super.setUp();
        List<UpdateUnitProvider> providers = UpdateUnitProviderFactory.getDefault().getUpdateUnitProviders(false);
        for (UpdateUnitProvider updateUnitProvider : providers) {
            String name = updateUnitProvider.getName();
            if (name.equals("UC_ENABLED")) {
                enabled = updateUnitProvider;
            } else if (name.equals("UC_DISABLED")) {
                disabled = updateUnitProvider;
            } else if (name.equals("UC_BETA")) {
                beta = updateUnitProvider;
            } else if (name.equals("UC_FALLBACK_CATEGORY")) {
                fallback = updateUnitProvider;
            }
        }
        assertNotSame(enabled, disabled);
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testEnabledDisabled() {
        assertTrue(enabled.isEnabled());
        assertFalse(disabled.isEnabled());        
    }
    
    public void testCategory() {
        assertEquals(CATEGORY.STANDARD, enabled.getCategory());
        assertEquals(CATEGORY.STANDARD, disabled.getCategory());
        assertEquals(CATEGORY.BETA, beta.getCategory());
        assertEquals(CATEGORY.COMMUNITY, fallback.getCategory());//fallback if no declaration                        
    }
}
