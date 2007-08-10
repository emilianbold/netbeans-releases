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

import java.io.IOException;
import org.netbeans.updater.UpdateTracking;

/** Issue http://www.netbeans.org/issues/show_bug.cgi?id=111701
 *
 * @author Jiri Rechtacek
 */
public class TargetClusterTest extends TargetClusterTestCase {
    
    public TargetClusterTest (String testName) {
        super (testName);
    }
    
    public void testInstallGloballyNewIntoDeclaredPlatform () throws IOException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + platformDir.getName (), platformDir.getName (), getTargetCluster (platformDir.getName (), true).getName ());
    }
    
    public void testInstallNewIntoDeclaredPlatform () throws IOException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + platformDir.getName (), platformDir.getName (), getTargetCluster (platformDir.getName (), null).getName ());
    }
    
    public void testInstallNewIntoDeclaredNextCluster () throws IOException {
        // Otherwise (new module), if a cluster name is specified in NBM, put it there
        assertEquals ("Goes into " + nextDir.getName (), nextDir.getName (), getTargetCluster (nextDir.getName (), null).getName ());
    }
    
    public void testInstallNewIntoDeclaredNextClusterAndFalseGlobal () throws IOException {
        // target cluster has precedence than global
        assertEquals ("Goes into " + nextDir.getName (), nextDir.getName (), getTargetCluster (nextDir.getName (), false).getName ());
    }
    
    public void testInstallGloballyNew () throws IOException {
        // Otherwise (no cluster name specified), if marked global, maybe put it into an "extra" cluster
        assertEquals ("Goes into " + UpdateTracking.EXTRA_CLUSTER_NAME,
                UpdateTracking.EXTRA_CLUSTER_NAME,
                getTargetCluster (null, true).getName ());
    }
    
    public void testInstallLocallyNew () throws IOException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals ("Goes into " + userDir.getName (),
                userDir.getName (),
                getTargetCluster (null, false).getName ());
    }
    
    public void testInstallNoDeclaredGlobalNew () throws IOException {
        // Otherwise (global="false" or unspecified), put it in user dir
        assertEquals ("Goes into " + userDir.getName (),
                userDir.getName (),
                getTargetCluster (null, null).getName ());
    }
    
}
