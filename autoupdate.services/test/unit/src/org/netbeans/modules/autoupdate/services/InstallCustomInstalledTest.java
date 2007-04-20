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

package org.netbeans.modules.autoupdate.services;

import java.io.File;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.TestUtils;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.CustomInstaller;
import org.netbeans.spi.autoupdate.UpdateItem;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallCustomInstalledTest extends OperationsTestImpl {
    public InstallCustomInstalledTest(String testName) {
        super(testName);
    }
    
    protected String moduleCodeNameBaseForTest () {
        return "hello-installer";
    }
    
    @Override
    public void setUp () throws Exception {
        super.setUp ();
        TestUtils.setCustomInstaller (installer);
    }
    
    public void testSelf () throws Exception {
        UpdateManager.getDefault().getUpdateUnits();
        UpdateUnit toInstall = UpdateManagerImpl.getInstance ().getUpdateUnit (moduleCodeNameBaseForTest ());
        installModule(toInstall, null, true);
        assertTrue ("Custom installer was called.", installerCalled);
    }
    
    private boolean installerCalled = false;
    private CustomInstaller installer = new CustomInstaller () {

        public boolean install(UpdateItem item, File f) throws OperationException {
            UpdateItem exp = TestUtils.getUpdateItemWithCustomInstaller ();
            assertNotNull ("UpdateItem not null.", item);
            assertEquals ("Was called with as same UpdateItem as excepted.", exp, item);
            installerCalled = true;
            return true;
        }
        
    };
    
}

