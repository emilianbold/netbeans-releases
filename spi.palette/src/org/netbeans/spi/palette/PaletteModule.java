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

package org.netbeans.spi.palette;

import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 *
 * @author S. Aubrecht
 * @since 1.10
 */
public class PaletteModule extends ModuleInstall {
    
    /** Creates a new instance of ModuleInstall */
    public PaletteModule() {
    }
    
    @Override
    public void restored() {
        super.restored();
        WindowManager.getDefault().invokeWhenUIReady( new Runnable() {
            public void run() {
                //start listening to activated TopComponents and Nodes 
                //to see if palette window should be displayed
                PaletteSwitch.getDefault().startListening();
            }
        });
    }
}
