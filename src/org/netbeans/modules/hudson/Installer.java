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

package org.netbeans.modules.hudson;

import org.netbeans.modules.hudson.impl.HudsonManagerImpl;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 *
 * @author Michal Mocnak
 */
public class Installer extends ModuleInstall {
    
    @Override
    public void restored() {
        // Perform ancestor method
        super.restored();
        
        // Initialize Hudson Support when IDE starts
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                HudsonManagerImpl.getInstance().getInstances();
            }
        });
    }
    
    @Override
    public void uninstalled() {
        HudsonManagerImpl.getInstance().terminate();
        
        // Perform ancestor method
        super.uninstalled();
    }
}