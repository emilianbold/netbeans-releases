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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sherold
 */
public class J2eeModuleProviderTest extends NbTestCase {
    
    /** Creates a new instance of J2eeModuleProviderTest */
    public J2eeModuleProviderTest(String testName) {
        super(testName);
    }
    
    public void testCreateJ2eeModule() {
        try {
            J2eeModuleProvider.class.getDeclaredMethod("resetConfigSupport");
        } catch (NoSuchMethodException e) {
            fail("J2eeModuleProvider.resetConfigSupport() method is missing! See the issue #109507. " +
                    "Do not remove the method unless the reporter of the issue agrees!");
        }
    }
}
