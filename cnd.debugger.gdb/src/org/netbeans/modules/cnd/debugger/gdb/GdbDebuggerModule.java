/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.debugger.gdb;

import java.io.File;
import org.netbeans.modules.cnd.debugger.gdb.actions.GdbActionHandlerProvider;
import org.openide.modules.ModuleInstall;

import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerRootNodeProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ui.CustomizerNode;
import org.netbeans.modules.cnd.makeproject.api.DefaultProjectActionHandler;

import org.netbeans.modules.cnd.debugger.gdb.profiles.GdbProfileProvider;
import org.netbeans.modules.cnd.debugger.gdb.profiles.ui.ProfileNodeProvider;
import org.netbeans.api.debugger.DebuggerManager;

/**
 *  Module installer for cnd gdb debugger. 
 *
 *  @author gordonp
 */
public class GdbDebuggerModule extends ModuleInstall {
    
    private CustomizerNode debugCustomizerNode;
    private boolean isDbxLoaded;
    
    public void restored() {
        // Profiles
        if (!isDbxGuiLoaded()) {
            debugCustomizerNode = new ProfileNodeProvider().createDebugNode();
            CustomizerRootNodeProvider.getInstance().addCustomizerNode(debugCustomizerNode);

            // Set project action handler
            DefaultProjectActionHandler.getInstance().setCustomDebugActionHandlerProvider(
                        new GdbActionHandlerProvider());  
        }
    }

    public void uninstalled() {
        // Profiles
        if (!isDbxGuiLoaded()) {
            CustomizerRootNodeProvider.getInstance().removeCustomizerNode(debugCustomizerNode);
            DefaultProjectActionHandler.getInstance().setCustomDebugActionHandlerProvider(null);
        }
    }
    
    public void close() {
        // Kill all debug sessions
        DebuggerManager.getDebuggerManager().finishAllSessions();
        super.close();
    }
    
    private boolean isDbxGuiLoaded() {
        return false;
    }
}
