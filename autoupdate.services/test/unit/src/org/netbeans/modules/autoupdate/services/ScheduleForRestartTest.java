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

import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateUnit;

/**
 *
 * @author Jirka Rechtacek
 */
public class ScheduleForRestartTest extends OperationsTestImpl {
    public ScheduleForRestartTest (String testName) {
        super (testName);
    }
    
    protected String moduleCodeNameBaseForTest() {
        return "org.yourorghere.engine"; //NOI18N
    }
    
    public void testSelf() throws Exception {
        UpdateUnit toInstall = UpdateManagerImpl.getInstance ().getUpdateUnit (moduleCodeNameBaseForTest ());
        installModule (toInstall);
        UpdateUnit toUpdate = toInstall;
        
        // try to update, should pass
        UpdateElement toUpElement = toUpdate.getAvailableUpdates ().get (0);
        OperationContainer<InstallSupport> container = OperationContainer.createForUpdate();
        try {
            container.add (toUpElement);
        } catch (IllegalArgumentException x) {
            fail ("Don't throw IAE when trying update " + toUpElement);
        }
        toUpElement = updateModule (toUpdate);
        
        // try to update again => should throw IAE
        container = OperationContainer.createForUpdate();
        try {
            container.add (toUpElement);
            fail ("Throws IAE when trying update " + toUpElement);
        } catch (IllegalArgumentException x) {
        }
    }
    
}
