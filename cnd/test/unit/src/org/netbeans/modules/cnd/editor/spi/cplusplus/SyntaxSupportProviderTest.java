/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.editor.spi.cplusplus;

import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.modules.cnd.test.BaseTestCase;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Voskresensky
 */
public class SyntaxSupportProviderTest extends BaseTestCase {
    
    public SyntaxSupportProviderTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of createSyntaxSupport method, of class org.netbeans.modules.cnd.editor.spi.cplusplus.SyntaxSupportProvider.
     */
    public void testCreateSyntaxSupport() {
        //System.out.println("createSyntaxSupport");
        
        BaseDocument doc = null;
        SyntaxSupportProvider instance = (SyntaxSupportProvider) Lookup.getDefault().lookup(SyntaxSupportProvider.class);
        assertNotNull("Provider is not found", instance);
    }
    
}
