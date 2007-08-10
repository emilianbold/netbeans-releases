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
import org.netbeans.api.autoupdate.UpdateElement;

/**
 *
 * @author Jiri Rechtacek
 */
public class TargetClusterWhenTargerClusterDeclaredTest extends TargetClusterTestCase {
    
    public TargetClusterWhenTargerClusterDeclaredTest (String testName) {
        super (testName);
    }
    
    private static UpdateElement installed = null;
    
    @Override
    protected String getCodeName (String target, Boolean global) {
        return "org.yourorghere.testupdatemodule";
    }
    
    @Override
    protected UpdateElement getInstalledUpdateElement () throws IOException {
        if (installed == null) {
            // !!! origin module is installed in platformDir
            installed = installModule (getCodeName (null, null));
        }
        return installed;
    }
    
    public void testUpdateTargerClusterDeclared () throws IOException {
        // If an update, overwrite the existing location, wherever that is.
        assertEquals ("Goes into platformDir", platformDir.getName (), getTargetCluster (nextDir.getName (), null).getName ());
    }
    
}
