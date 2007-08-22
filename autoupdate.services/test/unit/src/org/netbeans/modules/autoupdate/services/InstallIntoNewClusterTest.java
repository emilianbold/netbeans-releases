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
import java.io.IOException;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.junit.MockServices;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;

/**
 *
 * @author Radek Matous
 */
public class InstallIntoNewClusterTest extends OperationsTestImpl {

    public InstallIntoNewClusterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();        
        System.setProperty("netbeans.dirs", getWorkDirPath());
    }

    protected String moduleCodeNameBaseForTest() {
        return "com.sun.testmodule.cluster"; //NOI18N
    }

    public void testSelf() throws Exception {
        UpdateUnit toUnInstall = UpdateManagerImpl.getInstance().getUpdateUnit(moduleCodeNameBaseForTest());
        assertNotNull(toUnInstall);
        installModule(toUnInstall, null);
        unInstallModule(toUnInstall);
        installModule(toUnInstall, null);
        unInstallModule(toUnInstall);
    }

    public static final class NetBeansClusterCreator extends AutoupdateClusterCreator {
        protected  File findCluster(String clusterName) {
            String path = System.getProperty("netbeans.dirs", null);
            File f = path != null ? new File(path, clusterName) : null;
            return f != null ? f : null;
        }

        protected File[] registerCluster(String clusterName, File cluster) throws IOException {
            return new File[]{cluster};
        }
    }
}
