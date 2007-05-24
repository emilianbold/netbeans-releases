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

import java.util.Set;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;

/**
 *
 * @author Jiri Rechtacek
 */
public class InstallFeatureWithDependentModulesTest extends OperationsTestImpl {
    public InstallFeatureWithDependentModulesTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp () throws Exception {
        modulesOnly = false;
        super.setUp ();
    }
    
    protected String moduleCodeNameBaseForTest() {
        return "feature-depending-on-engine";//NOI18N
    }     

    public void testSelf() throws Exception {
        UpdateUnit toInstall = UpdateManagerImpl.getInstance ().getUpdateUnit (moduleCodeNameBaseForTest ());
        installModule (toInstall);
    }
    
    public UpdateElement installModule(UpdateUnit toInstall) throws Exception {
        assertNotNull ("I have to have something toInstall.", toInstall);
        assertNull ("... and no installed.", toInstall.getInstalled ());
        assertNotNull ("... and some available updates.", toInstall.getAvailableUpdates ());
        assertFalse ("... and some available updates are not empty.", toInstall.getAvailableUpdates ().isEmpty ());
        UpdateElement toInstallElement = toInstall.getAvailableUpdates ().get (0);
        OperationContainer<InstallSupport> container = OperationContainer.createForInstall ();
        OperationInfo<InstallSupport> info = container.add (toInstallElement);
        assertNotNull ("OperationInfo for element " + toInstallElement, info);
        Set<UpdateElement> reqs = info.getRequiredElements ();
        assertNotNull ("getRequiredElements() cannot returns null.", reqs);
        assertFalse ("Something missing", reqs.isEmpty ());
        return super.installModule(toInstall, toInstallElement);
    }

    
}

