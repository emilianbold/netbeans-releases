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

package org.netbeans.modules.derby;

import java.io.File;
import org.netbeans.modules.derby.test.TestBase;
import org.netbeans.spi.db.explorer.DatabaseRuntime;

/**
 *
 * @author Andrei Badea
 */
public class RegisterDerbyTest extends TestBase {

    public RegisterDerbyTest(String testName) {
        super(testName);
    }

    public void testAcceptsDatabaseURL() {
        DatabaseRuntime runtime = RegisterDerby.getDefault();
        assertTrue(runtime.acceptsDatabaseURL("jdbc:derby://localhost"));
        assertTrue("Leading spaces should be ignored", runtime.acceptsDatabaseURL("   jdbc:derby://localhost"));
        assertFalse(runtime.acceptsDatabaseURL("jdbc:derby://remote"));
    }

    public void testCanStart() throws Exception {
        DatabaseRuntime runtime = RegisterDerby.getDefault();

        assertTrue(DerbyOptions.getDefault().getLocation().length() == 0);
        assertFalse(runtime.canStart());

        clearWorkDir();
        File derbyLocation = new File(getWorkDir(), "derby");
        createFakeDerbyInstallation(derbyLocation);
        DerbyOptions.getDefault().setLocation(derbyLocation.getAbsolutePath());

        assertTrue(runtime.canStart());
    }
}
